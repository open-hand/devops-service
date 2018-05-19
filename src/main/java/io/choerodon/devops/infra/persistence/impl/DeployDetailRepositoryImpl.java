package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.repository.DeployDetailRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodDO;
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
    public List<DevopsEnvPodDO> getPods(Long instanceId) {
        return devopsEnvPodMapper.select(new DevopsEnvPodDO(instanceId));
    }

}
