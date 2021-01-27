package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCdEnvDeployInfoDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/8 15:31
 */
public interface DevopsCdEnvDeployInfoService {

    DevopsCdEnvDeployInfoDTO save(DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO);

    DevopsCdEnvDeployInfoDTO queryById(Long deployInfoId);

    void update(DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO);

    void updateOrUpdateByCdJob(Long cdJobId, String jarName);

    DevopsCdEnvDeployInfoDTO queryByCdJobId(Long cdJobId);

    List<DevopsCdEnvDeployInfoDTO> queryCurrentByValueId(Long valueId);

    List<DevopsCdEnvDeployInfoDTO> queryCurrentByEnvId(Long environmentId);
}
