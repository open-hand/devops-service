package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsHostUserPermissionDTO;

public interface DevopsHostUserPermissionService {

    /**
     * 校验用户是否有主机权限，没有权限则抛出异常
     * @param hostId 主机id
     * @param userId 用户id
     */
    void checkUserPermissionAndThrow(Long projectId, Long hostId, Long userId);

    /**
     * 校验用户是否有主机权限，没有权限则抛出异常
     * @param projectId
     * @param hostId 主机id
     * @param userId 用户id
     * @return 有权限返回true,否则false
     */
    Boolean checkUserPermission(Long projectId, Long hostId, Long userId);

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

    /**
     * 根据hostId查出所有有权限的用户id
     */
    List<Long> listUserIdsByHostId(Long hostId);

    /**
     * 查询主机用户权限记录
     */
    List<DevopsHostUserPermissionDTO> listUserHostPermissionByOption(Long hostId, Map<String, Object> searchParamMap, List<String> paramList);
}
