package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

<<<<<<< HEAD
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.repository.DeployDetailRepository;
<<<<<<< HEAD
=======
>>>>>>> [IMP] 重构Repository
=======
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.repository.DeployDetailRepository;
>>>>>>> [IMP]修改后端代码结构
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
<<<<<<< HEAD
    public List<DevopsEnvPodDTO> baseGetPods(Long instanceId) {
        return devopsEnvPodMapper.select(new DevopsEnvPodDTO(instanceId));
=======
    public List<DevopsEnvPodDTO> getPods(Long instanceId) {
        return devopsEnvPodMapper.select(new DevopsEnvPodDTO(instanceId));
>>>>>>> [IMP] 重构Repository
    }

}
