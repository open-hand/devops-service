package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsUserSyncRecordVO;
import io.choerodon.devops.infra.dto.DevopsUserSyncRecordDTO;
import io.choerodon.devops.infra.enums.UserSyncType;

/**
 * @author zmf
 * @since 2021/1/21
 */
public interface DevopsUserSyncRecordService {
    /**
     * 查询最新的记录
     *
     * @return 最新的同步记录
     */
    DevopsUserSyncRecordVO queryLatestRecord();

    /**
     * 初始化记录
     *
     * @param userSyncType 用户同步类型
     * @return 初始化的记录
     */
    DevopsUserSyncRecordDTO initRecord(UserSyncType userSyncType);

    /**
     * 结束一次同步
     *
     * @param recordId            记录id
     * @param successCount        同步数
     * @param failCount           失败数
     * @param errorInformationCsv 错误信息
     */
    void finish(Long recordId, Long successCount, Long failCount, String errorInformationCsv);

    /**
     * 记录一次未同步用户的记录
     *
     * @param recordId 记录id
     */
    void finishEmptyRecord(Long recordId);
}
