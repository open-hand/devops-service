package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborImageTagDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:30
 * Description:
 */
public interface HarborService {

    List<HarborRepoConfigDTO> listAllCustomRepoByProject(Long projectId);

    DevopsConfigDTO queryRepoConfigToDevopsConfig(Long projectId, Long id, String operateType);

    DevopsConfigDTO queryRepoConfigByIdToDevopsConfig(Long appServiceId, Long projectId, Long harborConfigId, String repoType, String operateType);

    void batchDeleteImageTags(List<HarborImageTagDTO> deleteImagetags);

    Map<Long, DevopsConfigDTO> listRepoConfigByAppVersionIds(List<Long> appVersionIds);

    List<DevopsConfigDTO> queryHarborConfigByHarborConfigIds(Set<Long> harborConfigIds);
}
