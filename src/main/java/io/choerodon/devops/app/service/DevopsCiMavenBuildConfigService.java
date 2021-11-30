package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.infra.dto.DevopsCiMavenBuildConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 15:03
 */
public interface DevopsCiMavenBuildConfigService {

    DevopsCiMavenBuildConfigDTO baseQuery(Long id);

    DevopsCiMavenBuildConfigVO queryById(Long id);

    void baseCreate(DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO);
}
