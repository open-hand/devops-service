package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdJobRecordService;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/3 13:47
 */
@Service
public class DevopsCdJobRecordServiceImpl implements DevopsCdJobRecordService {

    private static final String ERROR_SAVE_JOB_RECORD_FAILED = "error.save.job.record.failed";

    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;

    @Override
    public List<DevopsCdJobRecordDTO> queryByStageRecordId(Long stageRecordId) {
        DevopsCdJobRecordDTO jobRecordDTO = new DevopsCdJobRecordDTO();
        jobRecordDTO.setStageRecordId(stageRecordId);
        return devopsCdJobRecordMapper.select(jobRecordDTO);
    }

    @Override
    @Transactional
    public void save(DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
        if (devopsCdJobRecordMapper.insert(devopsCdJobRecordDTO) != 1) {
            throw new CommonException(ERROR_SAVE_JOB_RECORD_FAILED);
        }
    }
}
