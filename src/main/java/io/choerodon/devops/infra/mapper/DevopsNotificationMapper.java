package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsNotificationDO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:47 2019/5/13
 * Description:
 */
public interface DevopsNotificationMapper extends Mapper<DevopsNotificationDO> {
    List<DevopsNotificationDO> listByOptions(@Param("projectId") Long projectId,
                                             @Param("envId") Long envId,
                                             @Param("searchParam") Map<String, Object> searchParam,
                                             @Param("param") String param);

    Integer queryByEnvIdAndEvent(@Param("projectId") Long projectId,
                                 @Param("envId") Long envId,
                                 @Param("notifyTriggerEvent") List<String> notifyTriggerEvent);
}
