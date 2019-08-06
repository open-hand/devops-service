package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.AppServiceUserRelDTO;

/**
 * Created by Sheep on 2019/7/12.
 */
public interface AppServiceUserPermissionService {
    void baseCreate(Long userId, Long appServiceId);

    void baseDeleteByAppServiceId(Long appServiceId);

    void baseDeleteByUserIdAndAppIds(List<Long> appServiceIds, Long userId);

    List<AppServiceUserRelDTO> baseListByAppId(Long appServiceId);

    List<AppServiceUserRelDTO> baseListAll(Long appServiceId);

    List<AppServiceUserRelDTO> baseListByUserId(Long userId);

    void baseUpdate(Long appServiceId, List<Long> addUserIds, List<Long> deleteUserIds);
}
