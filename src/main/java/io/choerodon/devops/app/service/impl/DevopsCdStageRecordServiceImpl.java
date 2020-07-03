package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdStageRecordService;
import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;
import io.choerodon.devops.infra.mapper.DevopsCdStageRecordMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:07
 */
@Service
public class DevopsCdStageRecordServiceImpl implements DevopsCdStageRecordService {

    private static final String SAVE_STAGE_RECORD_FAILED = "save.stage.record.failed";

    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;

    @Override
    @Transactional
    public void save(DevopsCdStageRecordDTO devopsCdStageRecordDTO) {
        if (devopsCdStageRecordMapper.insert(devopsCdStageRecordDTO) != 1) {
            throw new CommonException(SAVE_STAGE_RECORD_FAILED);
        }
    }
}
