package io.choerodon.devops.app.service.impl;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.enums.ClusterResourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.InstanceSagaPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.util.*;

import io.choerodon.devops.app.service.ComponentReleaseService;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;

/**
 * 为集群的组件部署对应的Release
 *
 * @author zmf
 * @since 10/29/19
 */
@Service
public class ComponentReleaseServiceImpl implements ComponentReleaseService {

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsEnvCommandValueService devopsEnvCommandValueService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;

    /**
     * 更新时要保证是真的更新了组件的配置值
     *
     * @param componentType 组件类型
     * @param commandType   操作类型
     * @param componentDTO  组件配置
     * @param systemEnvId   集群环境ID
     * @param instanceId    要更新时才传，创建时为空
     * @return 创建或者更新的实例纪录
     */
    private AppServiceInstanceDTO createOrUpdateComponentRelease(String componentType, CommandType commandType, Object componentDTO, Long systemEnvId, @Nullable Long instanceId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(systemEnvId);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        String values = componentConfigToYaml(componentType, componentDTO);

        //校验values
        FileUtil.checkYamlFormat(values);

        AppServiceVersionDTO appServiceVersionDTO = getAppServiceVersionForComponent(componentType);

        //初始化实例,command, commandValue
        AppServiceInstanceDTO appServiceInstanceDTO = initApplicationInstanceDTO(systemEnvId, instanceId, commandType);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initEnvCommandDTO(commandType);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initEnvCommandValueDTO(values);

        // 初始化实例名
        String code;
        if (CommandType.CREATE == commandType) {
            code = String.format("%s-%s", componentType, GenerateUUID.generateUUID().substring(0, 5));
        } else {
            code = appServiceInstanceDTO.getCode();
            //更新实例的时候校验GitOps库文件是否存在,处理部署实例时，由于没有创GitOps文件导致的部署失败
            resourceFileCheckHandler.check(devopsEnvironmentDTO, instanceId, code, ResourceType.C7NHELMRELEASE.getType());
        }

        //存储数据
        if (CommandType.CREATE == commandType) {
            appServiceInstanceDTO.setCode(code);
            appServiceInstanceDTO.setId(appServiceInstanceService.baseCreate(appServiceInstanceDTO).getId());
            devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
            devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
            appServiceInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            appServiceInstanceService.baseUpdate(appServiceInstanceDTO);
        } else {
            devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
            devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
            appServiceInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            appServiceInstanceService.baseUpdate(appServiceInstanceDTO);
        }

        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO(null, systemEnvId, values, null, commandType.getType(), instanceId, code, null, null);
        AppServiceDTO fakeAppService = new AppServiceDTO();
        fakeAppService.setCode(componentType);

        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), null, appServiceInstanceDTO.getCommandId().intValue());
        instanceSagaPayload.setApplicationDTO(fakeAppService);
        instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
        instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE),
                builder -> builder
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));


        return appServiceInstanceDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public AppServiceInstanceDTO createReleaseForPrometheus(Long systemEnvId, DevopsPrometheusDTO devopsPrometheusDTO) {
        return createOrUpdateComponentRelease(ClusterResourceType.PROMETHEUS.getType(), CommandType.CREATE, devopsPrometheusDTO, systemEnvId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public AppServiceInstanceDTO updateReleaseForPrometheus(DevopsPrometheusDTO devopsPrometheusDTO, Long instanceId, Long systemEnvId) {
        return createOrUpdateComponentRelease(ClusterResourceType.PROMETHEUS.getType(), CommandType.UPDATE, devopsPrometheusDTO, systemEnvId, instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteReleaseForComponent(Long instanceId) {
        appServiceInstanceService.deleteInstance(instanceId);
    }


    /**
     * 获取组件对应的版本信息
     *
     * @param componentType 组件类型
     * @return 版本信息
     */
    private AppServiceVersionDTO getAppServiceVersionForComponent(String componentType) {
        return ComponentConfigUtil.getComponentVersion(componentType);
    }


    private String componentConfigToYaml(String componentType, Object component) {
        // TODO
        return null;
    }

    private DevopsEnvCommandValueDTO initEnvCommandValueDTO
            (String values) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDTO.setValue(values);
        return devopsEnvCommandValueDTO;
    }

    private AppServiceInstanceDTO initApplicationInstanceDTO(Long systemEnvId, @Nullable Long instanceId, CommandType commandType) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setEnvId(systemEnvId);
        appServiceInstanceDTO.setStatus(InstanceStatus.OPERATING.getStatus());
        if (CommandType.UPDATE == commandType) {
            AppServiceInstanceDTO oldAppServiceInstanceDTO = appServiceInstanceService.baseQuery(instanceId);
            appServiceInstanceDTO.setCode(oldAppServiceInstanceDTO.getCode());
            appServiceInstanceDTO.setId(instanceId);
        }
        return appServiceInstanceDTO;
    }

    private DevopsEnvCommandDTO initEnvCommandDTO(CommandType commandType) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        devopsEnvCommandDTO.setCommandType(commandType.getType());
        devopsEnvCommandDTO.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }
}
