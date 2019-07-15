package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsNotificationUserRelE;
import io.choerodon.devops.infra.dto.DevopsNotificationUserRelDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:38 2019/5/13
 * Description:
 */
public interface DevopsNotificationUserRelRepository {
    
    DevopsNotificationUserRelDTO baseCreate(Long notificationId, Long userId);

    void baseDelete(Long notificationId, Long userId);

    List<DevopsNotificationUserRelDTO> baseListByNotificationId(Long notificationId);

}
