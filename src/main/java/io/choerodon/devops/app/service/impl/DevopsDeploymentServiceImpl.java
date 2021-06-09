package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1beta2Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.DeploymentVO;
import io.choerodon.devops.api.vo.DevopsDeploymentVO;
import io.choerodon.devops.app.service.DevopsDeploymentService;
import io.choerodon.devops.app.service.DevopsEnvResourceDetailService;
import io.choerodon.devops.app.service.SaveChartResourceService;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.mapper.DevopsDeploymentMapper;
import io.choerodon.devops.infra.util.JsonHelper;
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
public class DevopsDeploymentServiceImpl implements DevopsDeploymentService, SaveChartResourceService {
    @Autowired
    private DevopsDeploymentMapper devopsDeploymentMapper;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;

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
                V1beta2Deployment v1beta2Deployment = JsonHelper.unmarshalByJackson(
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
    public DevopsDeploymentDTO queryByEnvIdAndName(Long envId, String name) {
        DevopsDeploymentDTO record = new DevopsDeploymentDTO();
        record.setName(name);
        record.setEnvId(envId);
        return devopsDeploymentMapper.selectOne(record);
    }

    @Override
    @Transactional
    public void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1beta2Deployment v1beta2Deployment = JsonHelper.unmarshalByJackson(detailsJson, V1beta2Deployment.class);

        DevopsDeploymentDTO oldDevopsDeploymentDTO = queryByEnvIdAndName(appServiceInstanceDTO.getEnvId(), v1beta2Deployment.getMetadata().getName());
        if (oldDevopsDeploymentDTO != null) {
            oldDevopsDeploymentDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            devopsDeploymentMapper.updateByPrimaryKeySelective(oldDevopsDeploymentDTO);
        } else {
            DevopsDeploymentDTO devopsDeploymentDTO = new DevopsDeploymentDTO();
            devopsDeploymentDTO.setEnvId(appServiceInstanceDTO.getEnvId());
            devopsDeploymentDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsDeploymentDTO.setCommandId(appServiceInstanceDTO.getId());
            devopsDeploymentDTO.setProjectId(appServiceInstanceDTO.getProjectId());
            devopsDeploymentDTO.setName(v1beta2Deployment.getMetadata().getName());
            devopsDeploymentMapper.insertSelective(devopsDeploymentDTO);
        }

    }

    @Override
    public ResourceType getType() {
        return ResourceType.DEPLOYMENT;
    }


}
