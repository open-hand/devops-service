package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;

import java.util.List;

public interface DevopsPvProPermissionService {

    void baseInsertPermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO);

    /**
     * 批量插入，忽视已经存在的关联关系
     *
     * @param clusterId  集群id
     * @param projectIds 项目id
     */
    void batchInsertIgnore(Long clusterId, List<Long> projectIds);

    List<DevopsPvProPermissionDTO> baseListByClusterId(Long clusterId);

    void baseDeletePermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO);

    void baseDeleteByPvId(Long pvId);

}
