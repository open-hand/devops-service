package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsCiSonarConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:26
 */
public interface DevopsCiSonarConfigService {

    void baseCreate(DevopsCiSonarConfigDTO devopsCiSonarConfigDTO);

    void batchDeleteByStepIds(Set<Long> stepIds);

    DevopsCiSonarConfigDTO baseQuery(Long id);

    DevopsCiSonarConfigDTO queryByStepId(Long stepId);


}
