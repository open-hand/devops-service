package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.app.service.DeployDetailService;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 14:40
 * Description:
 */
@Component
public class DeployDetailServiceImpl implements DeployDetailService {
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper;

    @Override
    public List<DevopsEnvPodVO> baseQueryPods(Long instanceId) {
        return devopsEnvPodMapper.select(new DevopsEnvPodDTO(instanceId)).stream().map(pod -> {
            DevopsEnvPodVO devopsEnvPodVO = new DevopsEnvPodVO();
            BeanUtils.copyProperties(pod, devopsEnvPodVO);
            return devopsEnvPodVO;
        }).collect(Collectors.toList());
    }
}
