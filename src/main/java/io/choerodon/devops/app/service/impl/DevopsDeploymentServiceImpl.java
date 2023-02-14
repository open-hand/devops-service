package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.CREATE_TYPE;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeploymentMapper;
import io.choerodon.devops.infra.util.*;
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
public class DevopsDeploymentServiceImpl implements DevopsDeploymentService, ChartResourceOperatorService {
    public static final String EXTRA_INFO_KEY_APP_CONFIG = "appConfig";
    public static final String EXTRA_INFO_KEY_CONTAINER_CONFIG = "containerConfig";
    public static final String EXTRA_INFO_KEY_SOURCE_TYPE = "sourceType";
    public static final String MAP_KEY_NAME = "name";
    public static final String MAP_KEY_PROTOCOL = "protocol";
    public static final String MAP_KEY_CONTAINER_PORT = "containerPort";

    @Autowired
    private DevopsDeploymentMapper devopsDeploymentMapper;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    private static JSON json = new JSON();


    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;

    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;

    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private AgentCommandService agentCommandService;

    @Override
    public DevopsDeploymentVO selectByPrimaryWithCommandInfo(Long id) {
        return devopsDeploymentMapper.selectByPrimaryKeyWithCommandInfo(id);
    }

    @Override
    public DevopsDeploymentDTO selectByPrimaryKey(Long id) {
        return devopsDeploymentMapper.selectByPrimaryKey(id);
    }

    @Override
    public void checkExist(Long envId, String name) {
        if (devopsDeploymentMapper.selectCountByEnvIdAndName(envId, name) != 0) {
            throw new CommonException("devops.workload.exist", "Deployment", name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseCreate(DevopsDeploymentDTO devopsDeploymentDTO) {
        devopsDeploymentMapper.insert(devopsDeploymentDTO);
        return devopsDeploymentDTO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(DevopsDeploymentDTO devopsDeploymentDTOToUpdate) {
        if (devopsDeploymentDTOToUpdate.getObjectVersionNumber() == null) {
            DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentMapper.selectByPrimaryKey(devopsDeploymentDTOToUpdate.getId());
            devopsDeploymentDTOToUpdate.setObjectVersionNumber(devopsDeploymentDTO.getObjectVersionNumber());
        }
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeploymentMapper, devopsDeploymentDTOToUpdate, "devops.deployment.update");
    }

    @Override
    public DevopsDeploymentDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsDeploymentDTO devopsDeploymentDTO = new DevopsDeploymentDTO();
        devopsDeploymentDTO.setEnvId(envId);
        devopsDeploymentDTO.setName(name);
        return devopsDeploymentMapper.selectOne(devopsDeploymentDTO);
    }

    @Override
    public void deleteByGitOps(Long id) {
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentMapper.selectByPrimaryKey(id);
        //校验环境是否链接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsDeploymentDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        devopsEnvCommandService.baseListByObject(ObjectType.DEPLOYMENT.getType(), id).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        if (WorkloadSourceTypeEnums.DEPLOY_GROUP.getType().equals(devopsDeploymentDTO.getSourceType()) && devopsDeploymentDTO.getInstanceId() != null) {
            devopsDeployAppCenterService.deleteByEnvIdAndObjectIdAndRdupmType(devopsDeploymentDTO.getEnvId(), devopsDeploymentDTO.getId(), RdupmTypeEnum.DEPLOYMENT.value());
        }
        devopsDeploymentMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.DEPLOYMENT.getType(), id);
    }

    @Override
    @Transactional
    public DevopsDeploymentVO createOrUpdateByGitOps(DevopsDeploymentVO devopsDeploymentVO, Long userId, String content) {
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsDeploymentVO.getEnvId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        if (devopsDeploymentVO.getCommandId() == null) {
            devopsEnvCommandDTO = WorkloadServiceImpl.initDevopsEnvCommandDTO(ResourceType.DEPLOYMENT.getType(), devopsDeploymentVO.getOperateType(), userId);
            devopsEnvCommandDTO.setCreatedBy(userId);
        } else {
            devopsEnvCommandDTO.setId(devopsDeploymentVO.getCommandId());
        }

        DevopsDeploymentDTO devopsDeploymentDTO = ConvertUtils.convertObject(devopsDeploymentVO, DevopsDeploymentDTO.class);

        if (devopsDeploymentVO.getOperateType().equals(CREATE_TYPE)) {
            Long deployId = baseCreate(devopsDeploymentDTO);
            devopsWorkloadResourceContentService.create(ResourceType.DEPLOYMENT.getType(), deployId, content);
            devopsEnvCommandDTO.setObjectId(deployId);
            devopsDeploymentDTO.setId(deployId);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsDeploymentDTO.getId());
            devopsWorkloadResourceContentService.update(ResourceType.DEPLOYMENT.getType(), devopsDeploymentDTO.getId(), content);
        }

        if (devopsDeploymentVO.getCommandId() == null) {
            devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        } else {
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        }

        devopsDeploymentDTO.setCommandId(devopsEnvCommandDTO.getId());
        baseUpdate(devopsDeploymentDTO);
        return io.choerodon.devops.infra.util.ConvertUtils.convertObject(devopsDeploymentDTO, DevopsDeploymentVO.class);
    }

    @Override
    public Page<DeploymentInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance) {
        Page<DevopsDeploymentVO> devopsDeploymentDTOPage = PageHelper.doPage(pageable,
                () -> devopsDeploymentMapper.listByEnvId(envId, name, fromInstance));
        Page<DeploymentInfoVO> deploymentVOPage = new Page<>();
        if (CollectionUtils.isEmpty(devopsDeploymentDTOPage.getContent())) {
            return deploymentVOPage;
        }
        Set<Long> detailsIds = devopsDeploymentDTOPage.getContent().stream().map(DevopsDeploymentVO::getResourceDetailId)
                .collect(Collectors.toSet());
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByResourceDetailsIds(detailsIds);
        Map<Long, DevopsEnvResourceDetailDTO> detailDTOMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        deploymentVOPage = ConvertUtils.convertPage(devopsDeploymentDTOPage, v -> {
            DeploymentInfoVO deploymentVO = ConvertUtils.convertObject(v, DeploymentInfoVO.class);
            if (detailDTOMap.get(v.getResourceDetailId()) != null) {
                // 参考实例详情查询逻辑
                V1Deployment v1beta2Deployment = json.deserialize(
                        detailDTOMap.get(v.getResourceDetailId()).getMessage(),
                        V1Deployment.class);
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
            deploymentVO.setEnvId(envId);
            return deploymentVO;
        });
        return deploymentVOPage;
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1Deployment v1beta2Deployment = json.deserialize(detailsJson, V1Deployment.class);

        DevopsDeploymentDTO oldDevopsDeploymentDTO = baseQueryByEnvIdAndName(appServiceInstanceDTO.getEnvId(), v1beta2Deployment.getMetadata().getName());
        if (oldDevopsDeploymentDTO != null) {
            oldDevopsDeploymentDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            oldDevopsDeploymentDTO.setLastUpdatedBy(appServiceInstanceDTO.getLastUpdatedBy());
            devopsDeploymentMapper.updateByPrimaryKeySelective(oldDevopsDeploymentDTO);
        } else {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());

            DevopsDeploymentDTO devopsDeploymentDTO = new DevopsDeploymentDTO();
            devopsDeploymentDTO.setEnvId(appServiceInstanceDTO.getEnvId());
            devopsDeploymentDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsDeploymentDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            devopsDeploymentDTO.setProjectId(devopsEnvironmentDTO.getProjectId());
            devopsDeploymentDTO.setSourceType(WorkloadSourceTypeEnums.CHART.getType());
            devopsDeploymentDTO.setName(v1beta2Deployment.getMetadata().getName());
            devopsDeploymentDTO.setCreatedBy(appServiceInstanceDTO.getCreatedBy());
            devopsDeploymentDTO.setLastUpdatedBy(appServiceInstanceDTO.getLastUpdatedBy());
            devopsDeploymentMapper.insertSelective(devopsDeploymentDTO);
        }
    }

    @Override
    @Transactional
    public void deleteByEnvIdAndName(Long envId, String name) {
        Assert.notNull(envId, ResourceCheckConstant.DEVOPS_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.DEVOPS_INSTANCE_NAME_IS_NULL);
        DevopsDeploymentDTO deploymentDTO = new DevopsDeploymentDTO();
        deploymentDTO.setName(name);
        deploymentDTO.setEnvId(envId);
        devopsDeploymentMapper.delete(deploymentDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(Long id) {
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentMapper.selectByPrimaryKey(id);
        if (WorkloadSourceTypeEnums.DEPLOY_GROUP.getType().equals(devopsDeploymentDTO.getSourceType()) && devopsDeploymentDTO.getInstanceId() != null) {
            devopsDeployAppCenterService.deleteByEnvIdAndObjectIdAndRdupmType(devopsDeploymentDTO.getEnvId(), devopsDeploymentDTO.getId(), RdupmTypeEnum.DEPLOYMENT.value());
        }
        devopsDeploymentMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.DEPLOYMENT.getType(), id);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.DEPLOYMENT;
    }

    @Override
    public DevopsDeploymentVO queryByDeploymentIdWithResourceDetail(Long deploymentId) {
        return devopsDeploymentMapper.queryByDeploymentIdWithResourceDetail(deploymentId);
    }

    @Override
    public InstanceControllerDetailVO getInstanceResourceDetailYaml(Long deploymentId) {
        DevopsDeploymentVO deploymentVO = queryByDeploymentIdWithResourceDetail(deploymentId);
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = devopsEnvResourceDetailService.baseQueryByResourceDetailId(deploymentVO.getResourceDetailId());
        try {
            return new InstanceControllerDetailVO(deploymentId, JsonYamlConversionUtil.json2yaml(devopsEnvResourceDetailDTO.getMessage()));
        } catch (IOException e) {
            throw new CommonException(JsonYamlConversionUtil.ERROR_JSON_TO_YAML_FAILED, devopsEnvResourceDetailDTO.getMessage());
        }
    }

    @Override
    public InstanceControllerDetailVO getInstanceResourceDetailJson(Long deploymentId) {
        DevopsDeploymentVO deploymentVO = queryByDeploymentIdWithResourceDetail(deploymentId);
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = devopsEnvResourceDetailService.baseQueryByResourceDetailId(deploymentVO.getResourceDetailId());
        try {
            return new InstanceControllerDetailVO(deploymentId, new ObjectMapper().readTree(devopsEnvResourceDetailDTO.getMessage()));
        } catch (IOException e) {
            throw new CommonException(JsonYamlConversionUtil.ERROR_JSON_TO_YAML_FAILED, devopsEnvResourceDetailDTO.getMessage());
        }
    }

    @Override
    public void startDeployment(Long projectId, Long deploymentId) {
        handleStartOrStopDeployment(projectId, deploymentId, CommandType.RESTART.getType());
    }

    @Override
    public void stopDeployment(Long projectId, Long deploymentId) {
        handleStartOrStopDeployment(projectId, deploymentId, CommandType.STOP.getType());
    }

    @Override
    public List<DevopsEnvPortVO> listPortByDeploymentId(Long deploymentId) {
        List<DevopsEnvPortVO> devopsEnvPortVOS = new ArrayList<>();
        DevopsDeploymentDTO devopsDeploymentDTO = selectByPrimaryKey(deploymentId);
        if (!ObjectUtils.isEmpty(devopsDeploymentDTO)) {
            String containerConfig = devopsDeploymentDTO.getContainerConfig();
            List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOS = JsonHelper.unmarshalByJackson(containerConfig, new TypeReference<List<DevopsDeployGroupContainerConfigVO>>() {
            });
            if (!CollectionUtils.isEmpty(devopsDeployGroupContainerConfigVOS)) {
                devopsEnvPortVOS = listPortByDevopsEnvMessageVOS(devopsDeployGroupContainerConfigVOS);
            }
        }
        return devopsEnvPortVOS;
    }

    @Override
    public List<DevopsEnvPortVO> listPortByDevopsEnvMessageVOS(List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOS) {
        List<DevopsEnvPortVO> devopsEnvPortVOS = new ArrayList<>();
        devopsDeployGroupContainerConfigVOS.forEach(devopsDeployGroupContainerConfigVO -> {
            List<Map<String, String>> ports = devopsDeployGroupContainerConfigVO.getPorts();
            if (!CollectionUtils.isEmpty(ports)) {
                for (Map<String, String> portMap : ports) {
                    DevopsEnvPortVO devopsEnvPortVO = new DevopsEnvPortVO();
                    devopsEnvPortVO.setResourceName(portMap.get(MAP_KEY_NAME));
                    devopsEnvPortVO.setPortName(portMap.get(MAP_KEY_PROTOCOL));
                    devopsEnvPortVO.setPortValue(Integer.parseInt(portMap.get(MAP_KEY_CONTAINER_PORT)));
                    devopsEnvPortVOS.add(devopsEnvPortVO);
                }
            }
        });
        return devopsEnvPortVOS;
    }

    private void handleStartOrStopDeployment(Long projectId, Long deploymentId, String type) {

        DevopsDeploymentDTO devopsDeploymentDTO = selectByPrimaryKey(deploymentId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsDeploymentDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        devopsDeploymentDTO.setStatus(InstanceStatus.OPERATING.getStatus());
        baseUpdate(devopsDeploymentDTO);


        // 改变pod数量
        if (CommandType.RESTART.getType().equals(type)) {
            agentCommandService.operatePodCount(ResourceType.DEPLOYMENT.getType(), devopsDeploymentDTO.getName(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId(), 1L, null);
        } else {
            agentCommandService.operatePodCount(ResourceType.DEPLOYMENT.getType(), devopsDeploymentDTO.getName(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId(), 0L, null);
        }
    }
}
