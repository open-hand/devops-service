package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsEnvPodDTO;

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 14:40
 * Description:
 */
public interface DeployDetailService {
    List<DevopsEnvPodDTO> getPods(Long instanceId);

}
