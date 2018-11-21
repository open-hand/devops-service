package io.choerodon.devops.domain.application.repository;


import java.util.List;

import io.choerodon.devops.domain.application.entity.AppUserPermissionE;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:45
 * Description:
 */

public interface AppUserPermissionRepository {

    List<AppUserPermissionE> listAll(Long appId);

    void updateAppUserPermission(Long appId, List<Long> addUserIds, List<Long> deleteUserIds);
}
