package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.api.vo.DevopsCiMavenPublishConfigVO;
import io.choerodon.devops.infra.dto.DevopsCiMavenPublishConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 18:04
 */
public interface DevopsCiMavenPublishConfigService {

    DevopsCiMavenPublishConfigDTO baseQueryById(Long id);

    DevopsCiMavenPublishConfigVO queryById(Long id);

    void baseCreate(DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO);

    void batchDeleteByIds(Set<Long> ids);
}
