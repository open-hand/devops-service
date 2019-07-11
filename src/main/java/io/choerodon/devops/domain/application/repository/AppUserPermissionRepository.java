package io.choerodon.devops.domain.application.repository;


import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.AppUserPermissionE;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:45
 * Description:
 */

public interface AppUserPermissionRepository {

    void create(Long userId, Long appId);

    void deleteByAppId(Long appId);

    void deleteByUserIdWithAppIds(List<Long> appIds, Long userId);

    List<AppUserPermissionE> listAll(Long appId);

    List<AppUserPermissionE> listByUserId(Long userId);

    void updateAppUserPermission(Long appId, List<Long> addUserIds, List<Long> deleteUserIds);
}
