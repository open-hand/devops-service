package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.DevopsNotificationDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:08 2019/5/13
 * Description:
 */
public interface DevopsNotificationService {
    DevopsNotificationDTO create(Long projectId, DevopsNotificationDTO notificationDTO);

    DevopsNotificationDTO update(Long projectId, DevopsNotificationDTO notificationDTO);

    void delete(Long notificationId);

    DevopsNotificationDTO queryById(Long notificationId);
}
