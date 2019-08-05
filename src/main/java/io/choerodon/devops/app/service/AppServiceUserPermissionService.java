package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.AppServiceUserRelDTO;

/**
 * Created by Sheep on 2019/7/12.
 */
public interface AppServiceUserPermissionService {
    void baseCreate(Long userId, Long appId);

    void baseDeleteByAppServiceId(Long appId);

    void baseDeleteByUserIdAndAppIds(List<Long> appIds, Long userId);

    List<AppServiceUserRelDTO> baseListByAppId(Long appId);

    List<AppServiceUserRelDTO> baseListAll(Long appId);

    List<AppServiceUserRelDTO> baseListByUserId(Long userId);

    void baseUpdate(Long appId, List<Long> addUserIds, List<Long> deleteUserIds);
}
