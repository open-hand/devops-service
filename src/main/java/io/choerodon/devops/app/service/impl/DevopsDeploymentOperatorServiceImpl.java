package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.CREATE_TYPE;

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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.DeploymentInfoVO;
import io.choerodon.devops.api.vo.DevopsDeploymentVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeploymentMapper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;
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

    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;

    @Override
    public DevopsDeploymentDTO selectByPrimaryKey(Long id) {
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
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeploymentMapper, devopsDeploymentDTOToUpdate, "error.deployment.update");
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
        devopsDeploymentMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.DEPLOYMENT.getType(), id);
    }

    @Override
    public DevopsDeploymentVO createOrUpdateByGitOps(DevopsDeploymentVO devopsDeploymentVO, Long userId, String content) {
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsDeploymentVO.getEnvId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(devopsDeploymentVO.getOperateType());
        devopsEnvCommandDTO.setCreatedBy(userId);

        DevopsDeploymentDTO devopsDeploymentDTO = ConvertUtils.convertObject(devopsDeploymentVO, DevopsDeploymentDTO.class);

        if (devopsDeploymentVO.getOperateType().equals(CREATE_TYPE)) {
            Long deployId = baseCreate(devopsDeploymentDTO);
            devopsWorkloadResourceContentService.create(ResourceType.DEPLOYMENT.getType(), deployId, content);
            devopsEnvCommandDTO.setObjectId(deployId);
            devopsDeploymentDTO.setId(deployId);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsDeploymentDTO.getId());
        }

        devopsDeploymentDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
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
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByMessageIds(detailsIds);
        Map<Long, DevopsEnvResourceDetailDTO> detailDTOMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        deploymentVOPage = ConvertUtils.convertPage(devopsDeploymentDTOPage, v -> {
            DeploymentInfoVO deploymentVO = ConvertUtils.convertObject(v, DeploymentInfoVO.class);
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
        Assert.notNull(envId, ResourceCheckConstant.ERROR_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_INSTANCE_NAME_IS_NULL);
        DevopsDeploymentDTO record = new DevopsDeploymentDTO();
        record.setName(name);
        record.setEnvId(envId);
        devopsDeploymentMapper.delete(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(Long id) {
        devopsDeploymentMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.DEPLOYMENT.getType(), id);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.DEPLOYMENT;
    }
}
