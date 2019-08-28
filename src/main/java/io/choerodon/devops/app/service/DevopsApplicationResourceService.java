package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsAppServiceResourceDTO;

/**
 * @author zmf
 */
public interface DevopsApplicationResourceService {
    void baseCreate(DevopsAppServiceResourceDTO devopsAppServiceResourceDTO);

    void baseDeleteByAppIdAndType(Long appServiceId, String type);

    void baseDeleteByResourceIdAndType(Long resourceId, String type);

    List<DevopsAppServiceResourceDTO> baseQueryByApplicationAndType(Long appServiceId, String type);
}
