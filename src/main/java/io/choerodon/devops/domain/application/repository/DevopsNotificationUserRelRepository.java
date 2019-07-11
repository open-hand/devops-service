package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsNotificationUserRelE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:38 2019/5/13
 * Description:
 */
public interface DevopsNotificationUserRelRepository {
    DevopsNotificationUserRelE create(Long notificationId, Long userId);

    void delete(Long notificationId, Long userId);

    List<DevopsNotificationUserRelE> queryByNoticaionId(Long notificationId);

}
