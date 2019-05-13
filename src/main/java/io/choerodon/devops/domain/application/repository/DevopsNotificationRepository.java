package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsNotificationE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:18 2019/5/13
 * Description:
 */
public interface DevopsNotificationRepository {

    DevopsNotificationE createOrUpdate(DevopsNotificationE notificationE);

    void deleteById(Long notificationId);

    List<DevopsNotificationE> listByOptions();

    DevopsNotificationE queryById(Long notificationId);
}
