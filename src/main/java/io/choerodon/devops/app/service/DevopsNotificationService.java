package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsNotificationDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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

    Page<DevopsNotificationDTO> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest);

    Set<String> check(Long projectId, Long envId);
}
