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

    DevopsCiMavenPublishConfigDTO queryByStepId(Long stepId);

    void baseCreate(DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO);

    void batchDeleteByStepIds(Set<Long> stepIds);
}
