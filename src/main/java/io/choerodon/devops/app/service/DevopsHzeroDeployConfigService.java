package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 10:01
 */
public interface DevopsHzeroDeployConfigService {

    DevopsHzeroDeployConfigDTO baseSave(String values);

    DevopsHzeroDeployConfigDTO baseQueryById(Long valueId);
}
