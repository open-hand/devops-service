package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineTaskRecordE;
import io.choerodon.devops.domain.application.repository.PipelineTaskRecordRepository;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.dataobject.PipelineTaskRecordDO;
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
    public PipelineTaskRecordE createOrUpdate(PipelineTaskRecordE taskRecordE) {
        PipelineTaskRecordDO recordDO = ConvertHelper.convert(taskRecordE, PipelineTaskRecordDO.class);
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
    public PipelineTaskRecordE queryById(Long taskRecordId) {
        return ConvertHelper.convert(taskRecordMapper.selectByPrimaryKey(taskRecordId), PipelineTaskRecordE.class);
    }

    @Override
    public List<PipelineTaskRecordE> queryByStageRecordId(Long stageRecordId, Long taskId) {
        return ConvertHelper.convertList(taskRecordMapper.queryByStageRecordId(stageRecordId, taskId), PipelineTaskRecordE.class);
    }

    @Override
    public void delete(Long recordId) {
        taskRecordMapper.deleteByPrimaryKey(recordId);
    }

    @Override
    public List<PipelineTaskRecordE> queryAllAutoTaskRecord(Long pipelineRecordId) {
        return ConvertHelper.convertList(taskRecordMapper.queryAllAutoTaskRecord(pipelineRecordId), PipelineTaskRecordE.class);
    }

    @Override
    public PipelineTaskRecordE queryPendingCheckTask(Long stageRecordId) {
        PipelineTaskRecordDO taskRecordDO = new PipelineTaskRecordDO();
        taskRecordDO.setStageRecordId(stageRecordId);
        taskRecordDO.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
        return ConvertHelper.convert(taskRecordMapper.selectOne(taskRecordDO), PipelineTaskRecordE.class);
    }
}
