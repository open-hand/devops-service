package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsNotificationUserRelDTO;

import java.util.List;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsNotificationUserRelService {

    DevopsNotificationUserRelDTO baseCreate(Long notificationId, Long userId);

    void baseDelete(Long notificationId, Long userId);

    List<DevopsNotificationUserRelDTO> baseListByNotificationId(Long notificationId);

    void batchInsert(List<DevopsNotificationUserRelDTO> devopsNotificationUserRelDTOS);

    void baseDeleteByNotificationId(Long id);
}
