package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsNotificationE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:18 2019/5/13
 * Description:
 */
public interface DevopsNotificationRepository {

    DevopsNotificationE createOrUpdate(DevopsNotificationE notificationE);

    void deleteById(Long notificationId);

    DevopsNotificationE queryById(Long notificationId);

    List<DevopsNotificationE> ListByEnvId(Long envId);

    PageInfo<DevopsNotificationE> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest);

    List<DevopsNotificationE> queryByEnvId(Long projectId,Long envId);
}
