package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.DevopsJobVO;
import io.choerodon.devops.api.vo.JobInfoVO;
import io.choerodon.devops.app.service.DevopsEnvResourceDetailService;
import io.choerodon.devops.app.service.DevopsJobService;
import io.choerodon.devops.app.service.DevopsWorkloadResourceContentService;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;
import io.choerodon.devops.infra.dto.DevopsJobDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.mapper.DevopsJobMapper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:21
 */
@Service
public class DevopsJobServiceImpl implements DevopsJobService {
    @Autowired
    private DevopsJobMapper devopsJobMapper;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;

    private JSON json = new JSON();

    @Override
    public Page<JobInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance) {
        Page<DevopsJobVO> devopsJobVOPage = PageHelper.doPage(pageable,
                () -> devopsJobMapper.listByEnvId(envId, name, fromInstance));
        Page<JobInfoVO> jobInfoVOPage = new Page<>();
        if (CollectionUtils.isEmpty(devopsJobVOPage.getContent())) {
            return jobInfoVOPage;
        }
        Set<Long> detailsIds = devopsJobVOPage.getContent().stream().map(DevopsJobVO::getResourceDetailId)
                .collect(Collectors.toSet());
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByMessageIds(detailsIds);
        Map<Long, DevopsEnvResourceDetailDTO> detailDTOMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        return ConvertUtils.convertPage(devopsJobVOPage, v -> {
            JobInfoVO jobInfoVO = ConvertUtils.convertObject(v, JobInfoVO.class);
            if (detailDTOMap.get(v.getResourceDetailId()) != null) {
                // 参考实例详情查询逻辑
                V1Job v1Job = json.deserialize(
                        detailDTOMap.get(v.getResourceDetailId()).getMessage(),
                        V1Job.class);

                jobInfoVO.setCompletions(v1Job.getSpec().getCompletions());
                jobInfoVO.setActive(v1Job.getStatus().getActive());
                jobInfoVO.setLabels(v1Job.getSpec().getSelector().getMatchLabels());
                List<Integer> portRes = new ArrayList<>();
                for (V1Container container : v1Job.getSpec().getTemplate().getSpec().getContainers()) {
                    List<V1ContainerPort> ports = container.getPorts();
                    Optional.ofNullable(ports).ifPresent(portList -> {
                        for (V1ContainerPort port : portList) {
                            portRes.add(port.getContainerPort());
                        }
                    });
                }
                jobInfoVO.setPorts(portRes);
                if (v1Job.getStatus() != null && v1Job.getStatus().getConditions() != null) {
                    v1Job.getStatus().getConditions().forEach(v1beta2DeploymentCondition -> {
                        if ("NewReplicaSetAvailable".equals(v1beta2DeploymentCondition.getReason())) {
                            jobInfoVO.setAge(v1beta2DeploymentCondition.getLastTransitionTime().toString());
                        }
                    });
                }
            }
            return jobInfoVO;
        });
    }

    @Override
    public DevopsJobDTO selectByPrimaryKey(Long resourceId) {
        return devopsJobMapper.selectByPrimaryKey(resourceId);
    }

    @Override
    public void checkExist(Long envId, String name) {
        if (devopsJobMapper.selectCountByEnvIdAndName(envId, name) != 0) {
            throw new CommonException("error.workload.exist", "Deployment", name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseCreate(DevopsJobDTO devopsStatefulSetDTO) {
        devopsJobMapper.insert(devopsStatefulSetDTO);
        return devopsStatefulSetDTO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(DevopsJobDTO devopsStatefulSetDTOToUpdate) {
        if (devopsStatefulSetDTOToUpdate.getObjectVersionNumber() == null) {
            DevopsJobDTO devopsStatefulSetDTO = devopsJobMapper.selectByPrimaryKey(devopsStatefulSetDTOToUpdate.getId());
            devopsStatefulSetDTOToUpdate.setObjectVersionNumber(devopsStatefulSetDTO.getObjectVersionNumber());
        }
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsJobMapper, devopsStatefulSetDTOToUpdate, "error.statefulset.update");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(Long id) {
        devopsJobMapper.deleteByPrimaryKey(id);
        devopsWorkloadResourceContentService.deleteByResourceId(ResourceType.DEPLOYMENT.getType(), id);
    }

    @Override
    public DevopsJobDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsJobDTO devopsStatefulSetDTO = new DevopsJobDTO();
        devopsStatefulSetDTO.setEnvId(envId);
        devopsStatefulSetDTO.setName(name);
        return devopsJobMapper.selectOne(devopsStatefulSetDTO);
    }
}
