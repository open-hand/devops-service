package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;

import java.util.List;

public interface DevopsPvProPermissionService {

    void baseInsertPermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO);

    /***
     * 忽略之前的权限情况，批量插入
     * @param pvId  pvId
     * @param projectIds 项目id
     */
    void batchInsertIgnore(Long pvId, List<Long> projectIds);

    List<DevopsPvProPermissionDTO> baseListByPvId(Long pvId);

    void baseDeletePermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO);

    /***
     * 删除权限表中pv相应的权限
     * @param  pvId
     */
    void baseDeleteByPvId(Long pvId);

    List<Long> baseListPvIdsByProjectId(Long projectId);

}
