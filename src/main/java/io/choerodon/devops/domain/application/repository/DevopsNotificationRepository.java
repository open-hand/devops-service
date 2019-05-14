package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsNotificationE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:18 2019/5/13
 * Description:
 */
public interface DevopsNotificationRepository {

    DevopsNotificationE createOrUpdate(DevopsNotificationE notificationE);

    void deleteById(Long notificationId);

    DevopsNotificationE queryById(Long notificationId);

    Page<DevopsNotificationE> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest);
}
