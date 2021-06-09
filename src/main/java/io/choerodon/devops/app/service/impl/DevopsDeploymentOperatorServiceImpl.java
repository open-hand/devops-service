package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1beta2Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.DeploymentVO;
import io.choerodon.devops.api.vo.DevopsDeploymentVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.app.service.ChartResourceOperatorService;
import io.choerodon.devops.app.service.DevopsDeploymentService;
import io.choerodon.devops.app.service.DevopsEnvResourceDetailService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeploymentMapper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.domain.AuditDomain;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:17
 */
@Service
public class DevopsDeploymentOperatorServiceImpl implements DevopsDeploymentService, ChartResourceOperatorService {
    private static final String CREATE_TYPE = "create";
    private static final String UPDATE_TYPE = "update";
    @Autowired
    private DevopsDeploymentMapper devopsDeploymentMapper;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    private static JSON json = new JSON();


    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;

    @Override
    public AuditDomain selectByPrimaryKey(Long id) {
        return devopsDeploymentMapper.selectByPrimaryKey(id);
    }

    @Override
    public void checkExist(Long envId, String name) {
        if (devopsDeploymentMapper.selectCountByEnvIdAndName(envId, name) != 0) {
            throw new CommonException("error.workload.exist", "Deployment", name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseCreate(AuditDomain auditDomain) {
        devopsDeploymentMapper.insert((DevopsDeploymentDTO) auditDomain);
        return ((DevopsDeploymentDTO) auditDomain).getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(AuditDomain auditDomain) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeploymentMapper, (DevopsDeploymentDTO) auditDomain, "error.deployment.update");
    }

    @Override
    public DevopsDeploymentDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsDeploymentDTO devopsDeploymentDTO = new DevopsDeploymentDTO();
        devopsDeploymentDTO.setEnvId(envId);
        devopsDeploymentDTO.setName(name);
        return devopsDeploymentMapper.selectOne(devopsDeploymentDTO);
    }

    @Override
    public DevopsDeploymentDTO baseQuery(Long resourceId) {
        return devopsDeploymentMapper.queryById(resourceId);
    }

    @Override
    public void deleteByGitOps(Long id) {
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentMapper.selectByPrimaryKey(id);
        //校验环境是否链接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsDeploymentDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        devopsEnvCommandService.baseListByObject(ObjectType.CONFIGMAP.getType(), id).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        devopsDeploymentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public DevopsDeploymentVO createOrUpdateByGitOps(DevopsDeploymentVO devopsDeploymentVO, Long userId) {
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsDeploymentVO.getEnvId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(devopsDeploymentVO.getOperateType());
        devopsEnvCommandDTO.setCreatedBy(userId);

        DevopsDeploymentDTO devopsDeploymentDTO = ConvertUtils.convertObject(devopsDeploymentVO, DevopsDeploymentDTO.class);

        if (devopsDeploymentVO.getOperateType().equals(CREATE_TYPE)) {
            Long deployId = baseCreate(devopsDeploymentDTO);
            devopsEnvCommandDTO.setObjectId(deployId);
            devopsDeploymentDTO.setId(deployId);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsDeploymentDTO.getId());
        }

        devopsDeploymentDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsDeploymentDTO);
        return io.choerodon.devops.infra.util.ConvertUtils.convertObject(devopsDeploymentDTO, DevopsDeploymentVO.class);
    }

    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        if (type.equals(CREATE_TYPE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
        } else if (type.equals(UPDATE_TYPE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        } else {
            devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        }
        devopsEnvCommandDTO.setObject(ObjectType.CONFIGMAP.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

    @Override
    public Page<DeploymentVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name) {
        Page<DevopsDeploymentVO> devopsDeploymentDTOPage = PageHelper.doPage(pageable, () -> devopsDeploymentMapper.listByEnvId(envId, name));
        Page<DeploymentVO> deploymentVOPage = new Page<>();
        if (CollectionUtils.isEmpty(devopsDeploymentDTOPage.getContent())) {
            return deploymentVOPage;
        }
        Set<Long> detailsIds = devopsDeploymentDTOPage.getContent().stream().map(DevopsDeploymentVO::getResourceDetailId)
                .collect(Collectors.toSet());
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByMessageIds(detailsIds);
        Map<Long, DevopsEnvResourceDetailDTO> detailDTOMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        deploymentVOPage = ConvertUtils.convertPage(devopsDeploymentDTOPage, v -> {
            DeploymentVO deploymentVO = new DeploymentVO();
            deploymentVO.setInstanceId(v.getInstanceId());
            deploymentVO.setName(v.getName());
            if (detailDTOMap.get(v.getResourceDetailId()) != null) {
                // 参考实例详情查询逻辑
                V1beta2Deployment v1beta2Deployment = json.deserialize(
                        detailDTOMap.get(v.getResourceDetailId()).getMessage(),
                        V1beta2Deployment.class);
                deploymentVO.setDesired(TypeUtil.objToLong(v1beta2Deployment.getSpec().getReplicas()));
                deploymentVO.setCurrent(TypeUtil.objToLong(v1beta2Deployment.getStatus().getReplicas()));
                deploymentVO.setUpToDate(TypeUtil.objToLong(v1beta2Deployment.getStatus().getUpdatedReplicas()));
                deploymentVO.setAvailable(TypeUtil.objToLong(v1beta2Deployment.getStatus().getAvailableReplicas()));
                deploymentVO.setAge(v1beta2Deployment.getMetadata().getCreationTimestamp().toString());
                deploymentVO.setLabels(v1beta2Deployment.getSpec().getSelector().getMatchLabels());
                List<Integer> portRes = new ArrayList<>();
                for (V1Container container : v1beta2Deployment.getSpec().getTemplate().getSpec().getContainers()) {
                    List<V1ContainerPort> ports = container.getPorts();
                    Optional.ofNullable(ports).ifPresent(portList -> {
                        for (V1ContainerPort port : portList) {
                            portRes.add(port.getContainerPort());
                        }
                    });
                }
                deploymentVO.setPorts(portRes);
                if (v1beta2Deployment.getStatus() != null && v1beta2Deployment.getStatus().getConditions() != null) {
                    v1beta2Deployment.getStatus().getConditions().forEach(v1beta2DeploymentCondition -> {
                        if ("NewReplicaSetAvailable".equals(v1beta2DeploymentCondition.getReason())) {
                            deploymentVO.setAge(v1beta2DeploymentCondition.getLastUpdateTime().toString());
                        }
                    });
                }
            }
            return deploymentVO;
        });
        return deploymentVOPage;
    }

    @Override
    @Transactional
    public void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1beta2Deployment v1beta2Deployment = json.deserialize(detailsJson, V1beta2Deployment.class);

        DevopsDeploymentDTO oldDevopsDeploymentDTO = baseQueryByEnvIdAndName(appServiceInstanceDTO.getEnvId(), v1beta2Deployment.getMetadata().getName());
        if (oldDevopsDeploymentDTO != null) {
            oldDevopsDeploymentDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            devopsDeploymentMapper.updateByPrimaryKeySelective(oldDevopsDeploymentDTO);
        } else {

            // todo devopsEnvironmentDTO是否需要判空处理，抛出异常还是打印日志？
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());

            DevopsDeploymentDTO devopsDeploymentDTO = new DevopsDeploymentDTO();
            devopsDeploymentDTO.setEnvId(appServiceInstanceDTO.getEnvId());
            devopsDeploymentDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsDeploymentDTO.setCommandId(appServiceInstanceDTO.getId());
            devopsDeploymentDTO.setProjectId(devopsEnvironmentDTO.getProjectId());
            devopsDeploymentDTO.setName(v1beta2Deployment.getMetadata().getName());
            devopsDeploymentMapper.insertSelective(devopsDeploymentDTO);
        }

    }

    @Override
    @Transactional
    public void deleteByEnvIdAndName(Long envId, String name) {
        DevopsDeploymentDTO record = new DevopsDeploymentDTO();
        record.setName(name);
        record.setEnvId(envId);
        devopsDeploymentMapper.delete(record);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.DEPLOYMENT;
    }
}
