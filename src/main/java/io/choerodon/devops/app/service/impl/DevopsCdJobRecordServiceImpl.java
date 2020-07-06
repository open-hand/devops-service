package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdJobRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;
import io.choerodon.devops.infra.util.TypeUtil;

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
    private static final String ERROR_UPDATE_JOB_RECORD_FAILED = "error.update.job.record.failed";

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

    @Override
    public DevopsCdJobRecordDTO queryFirstByStageRecordId(Long stageRecordId) {
        return devopsCdJobRecordMapper.queryFirstByStageRecordId(stageRecordId);
    }

    @Override
    @Transactional
    public void update(DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
        if (devopsCdJobRecordMapper.updateByPrimaryKeySelective(devopsCdJobRecordDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_JOB_RECORD_FAILED);
        }
    }

    @Override
    public void deleteByStageRecordId(Long projectId) {

    }

    @Override
    public void updateStatusById(Long jobRecordId, String status) {
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(jobRecordId);
        cdJobRecordDTO.setStatus(status);
        if (status.equals(WorkFlowStatus.FAILED.toValue())
                || status.equals(WorkFlowStatus.SUCCESS.toValue())
                || status.equals(WorkFlowStatus.STOP.toValue())) {
            long time = System.currentTimeMillis() - TypeUtil.objToLong(cdJobRecordDTO.getExecutionTime());
            cdJobRecordDTO.setExecutionTime(Long.toString(time));
        }
        if (status.equals(WorkFlowStatus.RUNNING.toValue())) {
            cdJobRecordDTO.setExecutionTime(Long.toString(System.currentTimeMillis()));
        }
        if (devopsCdJobRecordMapper.updateByPrimaryKey(cdJobRecordDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_JOB_RECORD_FAILED);
        }
    }

    @Override
    public DevopsCdJobRecordDTO queryById(Long id) {
        Assert.notNull(id, PipelineCheckConstant.ERROR_JOB_RECORD_ID_IS_NULL);
        return devopsCdJobRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void updateJobStatusFailed(Long jobRecordId) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = queryById(jobRecordId);
        devopsCdJobRecordDTO.setStatus(PipelineStatus.FAILED.toValue());
        devopsCdJobRecordDTO.setFinishedDate(new Date());
        update(devopsCdJobRecordDTO);
    }
}
