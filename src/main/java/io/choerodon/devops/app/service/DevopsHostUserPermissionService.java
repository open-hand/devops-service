package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsHostDTO;
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

    /**
     * 检查用户拥有使用权限（包括部署、查看、删除、修改、停止、重启应用）,有 返回true，没有 返回false
     *
     * @param projectId
     * @param hostId
     * @param userId
     */
    Boolean checkUserOwnUsePermission(Long projectId, Long hostId, Long userId);

    /**
     * 检查用户拥有使用权限（包括部署、查看、删除、修改、停止、重启应用），没有则抛出异常
     *
     * @param projectId
     * @param devopsHostDTO
     * @param userId
     */
    void checkUserOwnUsePermissionOrThrow(Long projectId, DevopsHostDTO devopsHostDTO, Long userId);

    /**
     * 检查用户拥有管理权限（包括部署、查看、删除、修改、停止、重启应用、连接主机、断开连接、删除、修改），没有则抛出异常
     *
     * @param projectId
     * @param devopsHostDTO
     * @param userId
     */
    void checkUserOwnManagePermissionOrThrow(Long projectId, DevopsHostDTO devopsHostDTO, Long userId);

    /**
     * 根据hostId以及用户id查出所有权限
     *
     * @param userId
     * @param hostId
     * @return
     */
    List<DevopsHostUserPermissionDTO> listUserHostPermissionByUserIdAndHostIds(Long userId, List<Long> hostId);

    /**
     * 删除主机指定用户的权限
     *
     * @param hostId
     * @param userIds
     */
    void deleteByHostIdAndUserIds(Long hostId, List<Long> userIds);
}
