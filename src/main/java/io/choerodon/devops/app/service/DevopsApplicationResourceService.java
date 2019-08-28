package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsAppServiceResourceDTO;

/**
 * @author zmf
 */
public interface DevopsApplicationResourceService {

    void handleAppServiceResource(List<Long> appServiceIds, Long resourceId, String type);

    void baseCreate(DevopsAppServiceResourceDTO devopsAppServiceResourceDTO);

    void baseDeleteByResourceIdAndType(Long resourceId, String type);

    List<DevopsAppServiceResourceDTO> baseQueryByResourceIdAndType(Long resourceId, String type);

    List<DevopsAppServiceResourceDTO> baseQueryByApplicationAndType(Long appServiceId, String type);
}
