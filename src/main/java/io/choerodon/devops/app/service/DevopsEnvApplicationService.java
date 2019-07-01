package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.ApplicationRepDTO;
import io.choerodon.devops.api.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvApplicationE;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public interface DevopsEnvApplicationService {
    DevopsEnvApplicationDTO create(DevopsEnvApplicationDTO devopsEnvApplicationDTO);

    List<ApplicationRepDTO> queryAppByEnvId(Long envId);
}
