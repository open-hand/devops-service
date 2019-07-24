package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCommandEventDTO;

/**
 * @author zmf
 */
public interface DevopsCommandEventService {
    void baseCreate(DevopsCommandEventDTO devopsCommandEventDTO);

    List<DevopsCommandEventDTO> baseListByCommandIdAndType(Long commandId, String type);

    void baseDeletePreInstanceCommandEvent(Long instanceId);

    void baseDeleteByCommandId(Long commandId);
}
