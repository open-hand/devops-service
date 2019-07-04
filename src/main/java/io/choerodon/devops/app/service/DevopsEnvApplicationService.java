package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.ApplicationRepDTO;
import io.choerodon.devops.api.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.api.dto.DevopsEnvLabelDTO;
import io.choerodon.devops.api.dto.DevopsEnvPortDTO;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public interface DevopsEnvApplicationService {
    DevopsEnvApplicationDTO create(DevopsEnvApplicationDTO devopsEnvApplicationDTO);

    List<ApplicationRepDTO> queryAppByEnvId(Long envId);

    List<DevopsEnvLabelDTO> queryLabelByAppEnvId(Long envId, Long appId);

    List<DevopsEnvPortDTO> queryPortByAppEnvId(Long envId, Long appId);
}
