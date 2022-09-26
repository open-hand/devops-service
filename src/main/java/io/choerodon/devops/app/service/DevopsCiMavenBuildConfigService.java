package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.infra.dto.DevopsCiMavenBuildConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 15:03
 */
public interface DevopsCiMavenBuildConfigService {

    DevopsCiMavenBuildConfigDTO baseQuery(Long id);

    DevopsCiMavenBuildConfigVO queryUnmarshalByStepId(Long stepId);

    DevopsCiMavenBuildConfigDTO queryByStepId(Long stepId);

    void baseCreate(DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO);

    void batchDeleteByStepIds(Set<Long> stepIds);

    DevopsCiMavenBuildConfigDTO voToDto(DevopsCiMavenBuildConfigVO mavenBuildConfigVO);

    DevopsCiMavenBuildConfigVO dtoToVo(DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO);

    DevopsCiMavenSettingsDTO buildAndSaveMavenSettings(Long projectId, Long jobId, Long sequence, DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO);
}
