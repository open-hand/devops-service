package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsEnvironmentPodDTO;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.repository.DeployDetailRepository;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 13:53
 * Description:
 */
@Component
public class DeployDetailRepositoryImpl implements DeployDetailRepository {

    private DevopsEnvPodMapper devopsEnvPodMapper;

    public DeployDetailRepositoryImpl(DevopsEnvPodMapper devopsEnvPodMapper) {

        this.devopsEnvPodMapper = devopsEnvPodMapper;
    }


    @Override
    public List<DevopsEnvironmentPodDTO> baseGetPods(Long instanceId) {
        return devopsEnvPodMapper.select(new DevopsEnvironmentPodDTO(instanceId));
    }

}
