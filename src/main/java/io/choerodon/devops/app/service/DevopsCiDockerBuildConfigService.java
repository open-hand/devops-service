package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiDockerBuildConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 16:25
 */
public interface DevopsCiDockerBuildConfigService {

    DevopsCiDockerBuildConfigDTO baseQuery(Long id);
}
