package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsCommandEventDTO;

/**
 * @author zmf
 */
public interface DevopsCommandEventService {
    void baseCreate(DevopsCommandEventDTO devopsCommandEventDTO);

    List<DevopsCommandEventDTO> baseListByCommandIdAndType(Long commandId, String type);

    void baseDeletePreInstanceCommandEvent(Long instanceId);

    void baseDeleteByCommandId(Long commandId);

    /**
     * 根据commandIds 批量查询(最多查询1000条)
     *
     * @param commandIds
     * @param type
     * @return
     */
    List<DevopsCommandEventDTO> listLastByCommandIdsAndType(Set<Long> commandIds, String type);

    /**
     * 根据commandIds 批量查询
     *
     * @param commandId
     * @return
     */
    List<DevopsCommandEventDTO> listByCommandId(Long commandId);

    void batchDeleteByCommandIds(Set<Long> commandIds);
}
