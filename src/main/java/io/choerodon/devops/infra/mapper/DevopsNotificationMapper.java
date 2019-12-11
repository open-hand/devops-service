package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.DevopsNotificationVO;
import io.choerodon.devops.api.vo.NotificationEventVO;
import io.choerodon.devops.infra.dto.DevopsNotificationDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:47 2019/5/13
 * Description:
 */
public interface DevopsNotificationMapper extends Mapper<DevopsNotificationDTO> {
    List<DevopsNotificationDTO> listByOptions(@Param("projectId") Long projectId,
                                              @Param("envId") Long envId,
                                              @Param("searchParam") Map<String, Object> searchParam,
                                              @Param("params") List<String> params);

    Integer queryByEnvIdAndEvent(@Param("projectId") Long projectId,
                                 @Param("envId") Long envId,
                                 @Param("notifyTriggerEvent") List<String> notifyTriggerEvent);

    List<NotificationEventVO> queryByProjectIdAndEnvId(@Param("projectId") Long projectId, @Param("envId") Long envId);

    /**
     * 查询预置的通知事件
     * @return
     */
    List<NotificationEventVO> listDefaultNotifyEvent();

    List<DevopsNotificationDTO> listOldNotificationDTO();

    List<DevopsNotificationVO> transferData();
}
