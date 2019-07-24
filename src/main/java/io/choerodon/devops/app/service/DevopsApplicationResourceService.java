package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsApplicationResourceDTO;

/**
 * @author zmf
 */
public interface DevopsApplicationResourceService {
    void baseCreate(DevopsApplicationResourceDTO devopsApplicationResourceDTO);

    void baseDeleteByAppIdAndType(Long appId, String type);

    void baseDeleteByResourceIdAndType(Long resourceId, String type);

    List<DevopsApplicationResourceDTO> baseQueryByApplicationAndType(Long appId, String type);
}
