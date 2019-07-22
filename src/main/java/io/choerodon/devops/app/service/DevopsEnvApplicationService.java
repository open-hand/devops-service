package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.*;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public interface DevopsEnvApplicationService {
    List<DevopsEnvApplicationDTO> batchCreate(DevopsEnvApplicationCreationDTO devopsEnvApplicationCreationDTO);

    List<ApplicationRepDTO> queryAppByEnvId(Long envId);

    List<DevopsEnvLabelDTO> queryLabelByAppEnvId(Long envId, Long appId);

    List<DevopsEnvPortDTO> queryPortByAppEnvId(Long envId, Long appId);
}
