package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsUserSyncRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 2021/1/21
 */
public interface DevopsUserSyncRecordMapper extends BaseMapper<DevopsUserSyncRecordDTO> {
    DevopsUserSyncRecordDTO queryLatestRecord();
}
