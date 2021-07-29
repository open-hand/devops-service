package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsHostUserPermissionDTO;

public interface DevopsHostUserPermissionService {
    /**
     * 插入关系
     */
    void baseCreate(DevopsHostUserPermissionDTO devopsHostUserPermissionDTO);

    /**
     * 批量插入关系
     */
    void batchInsert(List<DevopsHostUserPermissionDTO> devopsHostUserPermissionDTOList);

    /**
     * 删除所有关系
     */
    void deleteByHostId(Long hostId);

    /**
     * 更新关系
     */
    void baseUpdate(Long hostId, List<Long> addIamUserIds);

    /**
     * 删除关系
     */
    void baseDelete(DevopsHostUserPermissionDTO devopsHostUserPermissionDTO);

    List<DevopsHostUserPermissionDTO> baseListByHostId(Long hostId);
}
