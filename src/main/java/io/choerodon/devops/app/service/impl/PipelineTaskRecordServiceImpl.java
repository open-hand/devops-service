package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PipelineTaskRecordService;
import io.choerodon.devops.infra.dto.PipelineTaskRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineTaskRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zmf
 */
@Service
public class PipelineTaskRecordServiceImpl implements PipelineTaskRecordService {
    @Autowired
    private PipelineTaskRecordMapper taskRecordMapper;

    @Override
    public PipelineTaskRecordDTO baseCreateOrUpdateRecord(PipelineTaskRecordDTO taskRecordDTO) {
        if (taskRecordDTO.getId() == null) {
            if (taskRecordMapper.insert(taskRecordDTO) != 1) {
                throw new CommonException("error.insert.pipeline.task.record");
            }
        } else {
            taskRecordDTO.setObjectVersionNumber(taskRecordMapper.selectByPrimaryKey(taskRecordDTO).getObjectVersionNumber());
            if (taskRecordMapper.updateByPrimaryKeySelective(taskRecordDTO) != 1) {
                throw new CommonException("error.update.pipeline.task.record");
            }
        }
        return taskRecordDTO;
    }

    @Override
    public PipelineTaskRecordDTO baseQueryRecordById(Long taskRecordId) {
        return taskRecordMapper.selectByPrimaryKey(taskRecordId);
    }

    @Override
    public List<PipelineTaskRecordDTO> baseQueryByStageRecordId(Long stageRecordId, Long taskId) {
        return taskRecordMapper.queryByStageRecordId(stageRecordId, taskId);
    }

    @Override
    public void baseDeleteRecordById(Long recordId) {
        taskRecordMapper.deleteByPrimaryKey(recordId);
    }

    @Override
    public List<PipelineTaskRecordDTO> baseQueryAllAutoTaskRecord(Long pipelineRecordId) {
        return taskRecordMapper.queryAllAutoTaskRecord(pipelineRecordId);
    }
}
