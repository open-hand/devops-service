package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdStageRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.mapper.DevopsCdStageRecordMapper;
import io.choerodon.devops.infra.util.TypeUtil;

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
    private static final String UPDATE_STAGE_RECORD_FAILED = "update.stage.record.failed";

    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;

    @Override
    @Transactional
    public void save(DevopsCdStageRecordDTO devopsCdStageRecordDTO) {
        if (devopsCdStageRecordMapper.insert(devopsCdStageRecordDTO) != 1) {
            throw new CommonException(SAVE_STAGE_RECORD_FAILED);
        }
    }

    @Override
    public List<DevopsCdStageRecordDTO> queryByPipelineRecordId(Long pipelineRecordId) {
        DevopsCdStageRecordDTO recordDTO = new DevopsCdStageRecordDTO();
        recordDTO.setPipelineRecordId(pipelineRecordId);
        return devopsCdStageRecordMapper.select(recordDTO);
    }

    @Override
    public DevopsCdStageRecordDTO queryFirstByPipelineRecordId(Long pipelineRecordId) {
        return devopsCdStageRecordMapper.queryFirstByPipelineRecordId(pipelineRecordId);
    }

    @Override
    @Transactional
    public void update(DevopsCdStageRecordDTO devopsCdStageRecord) {
        if (devopsCdStageRecordMapper.updateByPrimaryKeySelective(devopsCdStageRecord) != 1) {
            throw new CommonException(UPDATE_STAGE_RECORD_FAILED);
        }
    }

    @Override
    public void updateStatusById(Long stageRecordId, String status) {
        DevopsCdStageRecordDTO recordDTO = devopsCdStageRecordMapper.selectByPrimaryKey(stageRecordId);
        recordDTO.setStatus(status);
        if (status.equals(WorkFlowStatus.FAILED.toValue())
                || status.equals(WorkFlowStatus.SUCCESS.toValue())
                || status.equals(WorkFlowStatus.STOP.toValue())) {
            long time = System.currentTimeMillis() - TypeUtil.objToLong(recordDTO.getExecutionTime());
            recordDTO.setExecutionTime(Long.toString(time));
        }
        if (status.equals(WorkFlowStatus.RUNNING.toValue())) {
            recordDTO.setExecutionTime(Long.toString(System.currentTimeMillis()));
        }
        if (devopsCdStageRecordMapper.updateByPrimaryKey(recordDTO) != 1) {
            throw new CommonException(UPDATE_STAGE_RECORD_FAILED);
        }
    }

    @Override
    public DevopsCdStageRecordDTO queryById(Long id) {
        Assert.notNull(id, PipelineCheckConstant.ERROR_STAGE_RECORD_ID_IS_NULL);
        return devopsCdStageRecordMapper.selectByPrimaryKey(id);
    }
}
