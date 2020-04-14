package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.DevopsNotificationTransferDataVO;
import io.choerodon.devops.api.vo.DevopsNotificationVO;
import io.choerodon.devops.api.vo.NotificationEventVO;
import io.choerodon.devops.infra.dto.DevopsNotificationDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:47 2019/5/13
 * Description:
 */
public interface DevopsNotificationMapper extends BaseMapper<DevopsNotificationDTO> {
    List<DevopsNotificationTransferDataVO> transferData();
}
