package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.dto.DevopsNotificationDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:18 2019/5/13
 * Description:
 */
public interface DevopsNotificationRepository {

    DevopsNotificationDTO baseCreateOrUpdate(DevopsNotificationDTO devopsNotificationDTO);

    void baseDelete(Long notificationId);

    DevopsNotificationDTO baseQuery(Long notificationId);

    PageInfo<DevopsNotificationDTO> basePageByOptions(Long projectId, Long envId, String params, PageRequest pageRequest);

    List<DevopsNotificationDTO> baseListByEnvId(Long projectId, Long envId);
}
