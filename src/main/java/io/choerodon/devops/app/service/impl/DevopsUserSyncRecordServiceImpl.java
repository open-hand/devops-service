package io.choerodon.devops.app.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.hzero.boot.file.FileClient;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.DevopsUserSyncRecordVO;
import io.choerodon.devops.api.vo.kubernetes.MockMultipartFile;
import io.choerodon.devops.app.service.DevopsUserSyncRecordService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.DevopsUserSyncRecordDTO;
import io.choerodon.devops.infra.enums.UserSyncRecordStatus;
import io.choerodon.devops.infra.enums.UserSyncType;
import io.choerodon.devops.infra.mapper.DevopsUserSyncRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * @author zmf
 * @since 2021/1/21
 */
@Service
public class DevopsUserSyncRecordServiceImpl implements DevopsUserSyncRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsUserSyncRecordServiceImpl.class);

    @Autowired
    private DevopsUserSyncRecordMapper devopsUserSyncRecordMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private FileClient fileClient;

    @Override
    public DevopsUserSyncRecordVO queryLatestRecord() {
        DevopsUserSyncRecordDTO devopsUserSyncRecordDTO = devopsUserSyncRecordMapper.queryLatestRecord();
        DevopsUserSyncRecordVO devopsUserSyncRecordVO = ConvertUtils.convertObject(devopsUserSyncRecordDTO, DevopsUserSyncRecordVO.class);

        if (devopsUserSyncRecordVO == null) {
            return constructEmptyVO();
        }

        // 如果同步中，填充进度数据
        if (!UserSyncRecordStatus.FINISHED.getValue().equals(devopsUserSyncRecordVO.getStatus())) {
            fillProcess(devopsUserSyncRecordVO);
        }

        return devopsUserSyncRecordVO;
    }

    private void fillProcess(DevopsUserSyncRecordVO devopsUserSyncRecordVO) {
        // 进度的模板 已处理/总数
        String processString = (String) redisTemplate.opsForValue().get(MiscConstants.USER_SYNC_REDIS_KEY);
        if (processString == null) {
            return;
        }

        String[] values = processString.split(BaseConstants.Symbol.SLASH);
        if (values.length != 2) {
            LOGGER.warn("unexpected length for user process length, the user process string is {}", processString);
            return;
        }

        devopsUserSyncRecordVO.setTotal(Long.valueOf(values[1]));
        devopsUserSyncRecordVO.setCurrent(Long.valueOf(values[0]));
    }

    private DevopsUserSyncRecordVO constructEmptyVO() {
        DevopsUserSyncRecordVO devopsUserSyncRecordVO = new DevopsUserSyncRecordVO();
        devopsUserSyncRecordVO.setStatus(UserSyncRecordStatus.FINISHED.getValue());
        return devopsUserSyncRecordVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DevopsUserSyncRecordDTO initRecord(UserSyncType userSyncType) {
        DevopsUserSyncRecordDTO devopsUserSyncRecordDTO = new DevopsUserSyncRecordDTO();
        devopsUserSyncRecordDTO.setType(userSyncType.getValue());
        devopsUserSyncRecordDTO.setStartTime(new Date());
        devopsUserSyncRecordDTO.setStatus(UserSyncRecordStatus.OPERATING.getValue());
        MapperUtil.resultJudgedInsertSelective(devopsUserSyncRecordMapper, devopsUserSyncRecordDTO, "error.insert.user.sync.record");
        return devopsUserSyncRecordMapper.selectByPrimaryKey(devopsUserSyncRecordDTO.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void finish(Long recordId, Long successCount, Long failCount, String errorInformationCsv) {
        DevopsUserSyncRecordDTO devopsUserSyncRecordDTO = devopsUserSyncRecordMapper.selectByPrimaryKey(recordId);
        devopsUserSyncRecordDTO.setSuccessCount(successCount);
        devopsUserSyncRecordDTO.setFailCount(failCount);
        devopsUserSyncRecordDTO.setEndTime(new Date());
        devopsUserSyncRecordDTO.setStatus(UserSyncRecordStatus.FINISHED.getValue());

        // 如果有失败的用户，上传失败信息文件
        if (failCount > 0) {
            String fileName = "user-sync-" + recordId + ".csv";
            try {
                String resultFileUrl = fileClient.uploadFile(0L, MiscConstants.USER_SYNC_ERROR_FILE_BUCKET_NAME, null, fileName, "text/csv", errorInformationCsv.getBytes(StandardCharsets.UTF_8));
                devopsUserSyncRecordDTO.setErrorUserResultUrl(resultFileUrl);
            } catch (Exception ex) {
                LOGGER.warn("Failed to upload user error information", ex);
            }
        }

        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsUserSyncRecordMapper, devopsUserSyncRecordDTO, "error.update.user.sync.record");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void finishEmptyRecord(Long recordId) {
        DevopsUserSyncRecordDTO devopsUserSyncRecordDTO = devopsUserSyncRecordMapper.selectByPrimaryKey(recordId);
        devopsUserSyncRecordDTO.setSuccessCount(0L);
        devopsUserSyncRecordDTO.setFailCount(0L);
        devopsUserSyncRecordDTO.setEndTime(new Date());
        devopsUserSyncRecordDTO.setStatus(UserSyncRecordStatus.FINISHED.getValue());

        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsUserSyncRecordMapper, devopsUserSyncRecordDTO, "error.update.user.sync.record");
    }
}
