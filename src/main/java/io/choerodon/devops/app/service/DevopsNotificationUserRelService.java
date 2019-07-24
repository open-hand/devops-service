package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.DevopsNotificationUserRelDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsNotificationUserRelService {

    DevopsNotificationUserRelDTO baseCreate(Long notificationId, Long userId);

    void baseDelete(Long notificationId, Long userId);

    List<DevopsNotificationUserRelDTO> baseListByNotificationId(Long notificationId);
}
