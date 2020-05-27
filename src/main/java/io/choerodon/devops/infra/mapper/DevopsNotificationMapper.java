package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsNotificationTransferDataVO;
import io.choerodon.devops.infra.dto.DevopsNotificationDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:47 2019/5/13
 * Description:
 * @deprecated 迁移数据到notify-service(hzero-message)了
 */
@Deprecated
public interface DevopsNotificationMapper extends BaseMapper<DevopsNotificationDTO> {
    List<DevopsNotificationTransferDataVO> transferData();
}
