package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.ApplicationUserPermissionDTO;

/**
 * Created by Sheep on 2019/7/12.
 */
public interface ApplicationUserPermissionService {
    void baseCreate(Long userId, Long appId);

    void baseDeleteByAppId(Long appId);

    void baseDeleteByUserIdAndAppIds(List<Long> appIds, Long userId);

    List<ApplicationUserPermissionDTO> baseListByAppId(Long appId);

    List<ApplicationUserPermissionDTO> baseListAll(Long appId);

    List<ApplicationUserPermissionDTO> baseListByUserId(Long userId);

    void baseUpdate(Long appId, List<Long> addUserIds, List<Long> deleteUserIds);
}
