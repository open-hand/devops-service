package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.PipelineTaskRecordRepository;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.dto.PipelineTaskRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineTaskRecordMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:24 2019/4/9
 * Description:
 */
@Component
public class PipelineTaskRecordRepositoryImpl implements PipelineTaskRecordRepository {
    @Autowired
    private PipelineTaskRecordMapper taskRecordMapper;

    @Override
    public PipelineTaskRecordE baseCreateOrUpdateRecord(PipelineTaskRecordE taskRecordE) {
        PipelineTaskRecordDTO recordDO = ConvertHelper.convert(taskRecordE, PipelineTaskRecordDTO.class);
        if (recordDO.getId() == null) {
            if (taskRecordMapper.insert(recordDO) != 1) {
                throw new CommonException("error.insert.pipeline.task.record");
            }
        } else {
            recordDO.setObjectVersionNumber(taskRecordMapper.selectByPrimaryKey(recordDO).getObjectVersionNumber());
            if (taskRecordMapper.updateByPrimaryKeySelective(recordDO) != 1) {
                throw new CommonException("error.update.pipeline.task.record");
            }
        }
        return ConvertHelper.convert(recordDO, PipelineTaskRecordE.class);
    }

    @Override
    public PipelineTaskRecordE baseQueryRecordById(Long taskRecordId) {
        return ConvertHelper.convert(taskRecordMapper.selectByPrimaryKey(taskRecordId), PipelineTaskRecordE.class);
    }

    @Override
    public List<PipelineTaskRecordE> baseQueryByStageRecordId(Long stageRecordId, Long taskId) {
        return ConvertHelper.convertList(taskRecordMapper.queryByStageRecordId(stageRecordId, taskId), PipelineTaskRecordE.class);
    }

    @Override
    public void baseDeleteRecordById(Long recordId) {
        taskRecordMapper.deleteByPrimaryKey(recordId);
    }

    @Override
    public List<PipelineTaskRecordE> baseQueryAllAutoTaskRecord(Long pipelineRecordId) {
        return ConvertHelper.convertList(taskRecordMapper.queryAllAutoTaskRecord(pipelineRecordId), PipelineTaskRecordE.class);
    }

    @Override
    public PipelineTaskRecordE baseQueryPendingCheckTask(Long stageRecordId) {
        PipelineTaskRecordDTO taskRecordDO = new PipelineTaskRecordDTO();
        taskRecordDO.setStageRecordId(stageRecordId);
        taskRecordDO.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
        return ConvertHelper.convert(taskRecordMapper.selectOne(taskRecordDO), PipelineTaskRecordE.class);
    }
}
