package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.dto.DevopsEnvApplicationCreationDTO;
import io.choerodon.devops.api.vo.ApplicationRepDTO;
import io.choerodon.devops.api.vo.DevopsEnvApplicationDTO;
import io.choerodon.devops.api.vo.DevopsEnvLabelDTO;
import io.choerodon.devops.api.vo.DevopsEnvPortDTO;

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
