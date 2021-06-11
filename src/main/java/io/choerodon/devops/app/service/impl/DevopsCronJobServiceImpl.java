package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1beta1CronJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.workload.CronJobInfoVO;
import io.choerodon.devops.api.vo.workload.DevopsCronjobVO;
import io.choerodon.devops.app.service.DevopsCronJobService;
import io.choerodon.devops.app.service.DevopsEnvResourceDetailService;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;
import io.choerodon.devops.infra.mapper.DevopsCronJobMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:22
 */
@Service
public class DevopsCronJobServiceImpl implements DevopsCronJobService {
    @Autowired
    private DevopsCronJobMapper devopsCronJobMapper;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    private JSON json = new JSON();

    @Override
    public Page<CronJobInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance) {
        Page<DevopsCronjobVO> devopsCronjobVOPage = PageHelper.doPage(pageable,
                () -> devopsCronJobMapper.listByEnvId(envId, name, fromInstance));
        Page<CronJobInfoVO> cronJobInfoVOPage = new Page<>();
        if (CollectionUtils.isEmpty(devopsCronjobVOPage.getContent())) {
            return cronJobInfoVOPage;
        }
        Set<Long> detailsIds = devopsCronjobVOPage.getContent().stream().map(DevopsCronjobVO::getResourceDetailId)
                .collect(Collectors.toSet());
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByMessageIds(detailsIds);
        Map<Long, DevopsEnvResourceDetailDTO> detailDTOMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        return ConvertUtils.convertPage(devopsCronjobVOPage, v -> {
            CronJobInfoVO cronJobInfoVO = ConvertUtils.convertObject(v, CronJobInfoVO.class);
            if (detailDTOMap.get(v.getResourceDetailId()) != null) {
                // 参考实例详情查询逻辑

                V1beta1CronJob v1beta1CronJob = json.deserialize(
                        detailDTOMap.get(v.getResourceDetailId()).getMessage(),
                        V1beta1CronJob.class);


                cronJobInfoVO.setLabels(v1beta1CronJob.getMetadata().getLabels());
                List<Integer> portRes = new ArrayList<>();
                for (V1Container container : v1beta1CronJob.getSpec().getJobTemplate().getSpec().getTemplate().getSpec().getContainers()) {
                    List<V1ContainerPort> ports = container.getPorts();
                    Optional.ofNullable(ports).ifPresent(portList -> {
                        for (V1ContainerPort port : portList) {
                            portRes.add(port.getContainerPort());
                        }
                    });
                }
                cronJobInfoVO.setPorts(portRes);

            }
            return cronJobInfoVO;
        });
    }
}
