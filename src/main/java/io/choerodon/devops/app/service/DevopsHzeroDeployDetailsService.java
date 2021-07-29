package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
import io.choerodon.devops.infra.enums.HzeroDeployDetailsStatusEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 10:04
 */
public interface DevopsHzeroDeployDetailsService {

    DevopsHzeroDeployDetailsDTO baseSave(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO);

    DevopsHzeroDeployDetailsDTO baseQueryById(Long detailsRecordId);

    void updateStatusById(Long id, HzeroDeployDetailsStatusEnum status);

    DevopsHzeroDeployDetailsDTO baseQueryDeployingByEnvIdAndInstanceCode(Long envId, String instanceCode);
}
