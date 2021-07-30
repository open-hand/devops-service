package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.InstanceSagaPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper;
import io.choerodon.devops.infra.util.*;

/**
 * 为集群的组件部署对应的Release
 *
 * @author zmf
 * @since 10/29/19
 */
@Service
public class ComponentReleaseServiceImpl implements ComponentReleaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentReleaseServiceImpl.class);

    @Value("${services.gateway.url}")
    private String apiHost;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsEnvCommandValueService devopsEnvCommandValueService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;

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
    private AppServiceInstanceDTO createOrUpdateComponentRelease(ClusterResourceType componentType, CommandType commandType, Object componentDTO, Long systemEnvId, @Nullable Long instanceId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(systemEnvId);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        Map extraData = new HashMap<>();
        extraData.put("apiHost", apiHost);
        String values = ComponentValuesTemplateUtil.convert(componentType, componentDTO, extraData);

        //校验values
        FileUtil.checkYamlFormat(values);

        AppServiceVersionDTO appServiceVersionDTO = ComponentVersionUtil.getComponentVersion(componentType);

        //初始化实例,command, commandValue
        AppServiceInstanceDTO appServiceInstanceDTO = initApplicationInstanceDTO(systemEnvId, instanceId, commandType);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initEnvCommandDTO(commandType);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initEnvCommandValueDTO(values);

        // 初始化实例名
        String code;
        if (CommandType.CREATE == commandType) {
            code = String.format("%s-%s", appServiceVersionDTO.getChartName(), GenerateUUID.generateUUID().substring(0, 5));
        } else {
            code = appServiceInstanceDTO.getCode();
            //更新实例的时候校验GitOps库文件是否存在,处理部署实例时，由于没有创GitOps文件导致的部署失败
            resourceFileCheckHandler.check(devopsEnvironmentDTO, instanceId, code, ResourceType.C7NHELMRELEASE.getType());
        }

        // 设置componentVersion便于之后升级组件的版本
        appServiceInstanceDTO.setComponentVersion(appServiceVersionDTO.getVersion());
        appServiceInstanceDTO.setComponentChartName(appServiceVersionDTO.getChartName());

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

        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO();
        appServiceDeployVO.setEnvironmentId(systemEnvId);
        appServiceDeployVO.setValues(values);
        appServiceDeployVO.setType(commandType.getType());
        appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
        appServiceDeployVO.setInstanceName(code);
        AppServiceDTO fakeAppService = new AppServiceDTO();
        fakeAppService.setCode(appServiceVersionDTO.getChartName());

        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), null, appServiceInstanceDTO.getCommandId());
        instanceSagaPayload.setApplicationDTO(fakeAppService);
        instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
        instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE)
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()), builder -> {
                });


        return appServiceInstanceDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public AppServiceInstanceDTO createReleaseForPrometheus(Long systemEnvId, DevopsPrometheusDTO devopsPrometheusDTO) {
        return createOrUpdateComponentRelease(ClusterResourceType.PROMETHEUS, CommandType.CREATE, devopsPrometheusDTO, systemEnvId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public AppServiceInstanceDTO updateReleaseForPrometheus(DevopsPrometheusDTO devopsPrometheusDTO, Long instanceId, Long systemEnvId) {
        return createOrUpdateComponentRelease(ClusterResourceType.PROMETHEUS, CommandType.UPDATE, devopsPrometheusDTO, systemEnvId, instanceId);
    }

    // 开新事务
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean retryPushingToGitLab(Long instanceId, ClusterResourceType clusterResourceType) {
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(instanceId);
        if (appServiceInstanceDTO == null) {
            LOGGER.info("Retry pushing instance: the instance with id {} is unexpectedly null.", instanceId);
            return false;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        // 判断是否解析过了
        if (devopsEnvFileResourceMapper.countRecords(devopsEnvironmentDTO.getId(), ResourceType.C7NHELMRELEASE.getType(), appServiceInstanceDTO.getId()) > 0) {
            LOGGER.info("Retry pushing instance: the instance with code {} has passed the GitOps flow since the env-file-resource record exists", appServiceInstanceDTO.getCode());
            return false;
        }

        // 校验远程文件是否存在
        String remoteFileName = GitOpsConstants.RELEASE_PREFIX + appServiceInstanceDTO.getCode() + GitOpsConstants.YAML_FILE_SUFFIX;
        if (doesRemoteFileExist(remoteFileName,
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()))) {
            LOGGER.info("Retry pushing instance: retry cancels since the remote file {} exists.", remoteFileName);
            return false;
        }

        // 校验command
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        if (!StringUtils.isEmpty(devopsEnvCommandDTO.getSha())) {
            LOGGER.info("Retry pushing instance: it seems that this instance had passed the GitOps flow due to the command sha {}", devopsEnvCommandDTO.getSha());
            return false;
        }

        // 校验实例状态
        if (!Objects.equals(appServiceInstanceDTO.getStatus(), InstanceStatus.FAILED.getStatus())
                && !Objects.equals(appServiceInstanceDTO.getStatus(), InstanceStatus.OPERATING.getStatus())) {
            LOGGER.info("Retry pushing instance: unexpected status {} for instance", appServiceInstanceDTO.getStatus());
            return false;
        }

        // 将实例状态置为初始状态
        appServiceInstanceDTO.setStatus(InstanceStatus.OPERATING.getStatus());
        appServiceInstanceService.baseUpdate(appServiceInstanceDTO);

        // 将command的状态置为初始状态
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setError(null);
        devopsEnvCommandMapper.updateByPrimaryKey(devopsEnvCommandDTO);

        // 准备构造saga的payload
        AppServiceVersionDTO appServiceVersionDTO = ComponentVersionUtil.getComponentVersion(clusterResourceType);

        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO();
        appServiceDeployVO.setEnvironmentId(devopsEnvironmentDTO.getId());
        appServiceDeployVO.setValues(appServiceInstanceService.baseQueryValueByInstanceId(instanceId));
        appServiceDeployVO.setType(devopsEnvCommandDTO.getCommandType());
        appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
        appServiceDeployVO.setInstanceName(appServiceInstanceDTO.getCode());
        AppServiceDTO fakeAppService = new AppServiceDTO();
        fakeAppService.setCode(appServiceInstanceDTO.getComponentChartName());

        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), null, appServiceInstanceDTO.getCommandId());
        instanceSagaPayload.setApplicationDTO(fakeAppService);
        instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
        instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE)
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()), builder -> {
                });

        return true;
    }


    private boolean doesRemoteFileExist(String remoteFileName, Integer gitlabGroupId) {
        return gitlabServiceClientOperator.getFile(gitlabGroupId, GitOpsConstants.MASTER, remoteFileName);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean restartComponentInstance(Long instanceId, ClusterResourceType clusterResourceType) {
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(instanceId);

        if (appServiceInstanceDTO == null) {
            LOGGER.info("Restart component instance: the instance with id {} to be restarted is unexpectedly null.", instanceId);
            return false;
        }

        if (InstanceStatus.OPERATING.getStatus().equals(appServiceInstanceDTO.getStatus())) {
            LOGGER.info("Restart component instance: the instance with id {} to be restarted is still operating.", instanceId);
            return false;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());

        AppServiceDTO fakeAppService = new AppServiceDTO();
        fakeAppService.setCode(appServiceInstanceDTO.getComponentChartName());

        AppServiceVersionDTO appServiceVersionDTO = ComponentVersionUtil.getComponentVersion(clusterResourceType);

        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        appServiceInstanceDTO.setStatus(InstanceStatus.OPERATING.getStatus());
        appServiceInstanceDTO.setCommandId(devopsEnvCommandDTO.getId());
        appServiceInstanceService.baseUpdate(appServiceInstanceDTO);

        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO();
        appServiceDeployVO.setEnvironmentId(devopsEnvironmentDTO.getId());
        appServiceDeployVO.setValues(appServiceInstanceService.baseQueryValueByInstanceId(instanceId));
        appServiceDeployVO.setType(devopsEnvCommandDTO.getCommandType());
        appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
        appServiceDeployVO.setInstanceName(appServiceInstanceDTO.getCode());
        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), null, devopsEnvCommandDTO.getId());
        instanceSagaPayload.setApplicationDTO(fakeAppService);
        instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
        instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE)
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()), builder -> {
                });
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteReleaseForComponent(Long instanceId, Boolean deletePrometheus) {
        appServiceInstanceService.deleteInstance(null, instanceId, deletePrometheus);
    }

    private DevopsEnvCommandValueDTO initEnvCommandValueDTO
            (String values) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDTO.setValue(values);
        return devopsEnvCommandValueDTO;
    }

    private AppServiceInstanceDTO initApplicationInstanceDTO(Long systemEnvId, @Nullable Long instanceId, CommandType commandType) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setSource(AppServiceInstanceSource.NORMAL.getValue());
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
