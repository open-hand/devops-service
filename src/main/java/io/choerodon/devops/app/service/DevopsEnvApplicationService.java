package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public interface DevopsEnvApplicationService {
    List<DevopsEnvApplicationVO> batchCreate(DevopsEnvApplicationCreationVO devopsEnvApplicationCreationVO);

    List<ApplicationRepVO> queryAppByEnvId(Long envId);

    List<DevopsEnvLabelDTO> queryLabelByAppEnvId(Long envId, Long appId);

    List<DevopsEnvPortDTO> queryPortByAppEnvId(Long envId, Long appId);

    DevopsEnvApplicationDTO baseCreate(DevopsEnvApplicationDTO devopsEnvApplicationE);

    List<Long> baseQueryAppByEnvId(Long envId);

    List<DevopsEnvMessageVO> baseListResourceByEnvAndApp(Long envId, Long appId);
}
