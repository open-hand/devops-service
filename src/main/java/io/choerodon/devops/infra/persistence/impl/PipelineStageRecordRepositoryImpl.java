package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.PipelineStageRecordE;
import io.choerodon.devops.domain.application.repository.PipelineStageRecordRepository;
import io.choerodon.devops.infra.dto.PipelineStageRecordDO;
import io.choerodon.devops.infra.mapper.PipelineStageRecordMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:09 2019/4/4
 * Description:
 */
@Component
public class PipelineStageRecordRepositoryImpl implements PipelineStageRecordRepository {
    @Autowired
    private PipelineStageRecordMapper stageRecordMapper;

    @Override
    public List<PipelineStageRecordE> list(Long projectId, Long pipelineRecordId) {
        return ConvertHelper.convertList(stageRecordMapper.listByOptions(projectId, pipelineRecordId), PipelineStageRecordE.class);
    }

    @Override
    public PipelineStageRecordE createOrUpdate(PipelineStageRecordE stageRecordE) {
        PipelineStageRecordDO stageRecordDO = ConvertHelper.convert(stageRecordE, PipelineStageRecordDO.class);
        if (stageRecordDO.getId() == null) {
            if (stageRecordMapper.insert(stageRecordDO) != 1) {
                throw new CommonException("error.insert.pipeline.stage.record");
            }
        } else {
            stageRecordDO.setObjectVersionNumber(stageRecordMapper.selectByPrimaryKey(stageRecordDO).getObjectVersionNumber());
            if (stageRecordMapper.updateByPrimaryKeySelective(stageRecordDO) != 1) {
                throw new CommonException("error.update.pipeline.stage.record");
            }
        }
        return ConvertHelper.convert(stageRecordDO, PipelineStageRecordE.class);
    }

    @Override
    public List<PipelineStageRecordE> queryByPipeRecordId(Long pipelineRecordId, Long stageId) {
        PipelineStageRecordDO stageRecordDO = new PipelineStageRecordDO();
        stageRecordDO.setPipelineRecordId(pipelineRecordId);
        stageRecordDO.setStageId(stageId);
        return ConvertHelper.convertList(stageRecordMapper.select(stageRecordDO), PipelineStageRecordE.class);
    }

    @Override
    public PipelineStageRecordE queryById(Long recordId) {
        PipelineStageRecordDO stageRecordDO = new PipelineStageRecordDO();
        stageRecordDO.setId(recordId);
        return ConvertHelper.convert(stageRecordMapper.selectByPrimaryKey(stageRecordDO), PipelineStageRecordE.class);
    }

    @Override
    public PipelineStageRecordE update(PipelineStageRecordE stageRecordE) {
        PipelineStageRecordDO stageRecordDO = ConvertHelper.convert(stageRecordE, PipelineStageRecordDO.class);
        if (stageRecordMapper.updateByPrimaryKey(stageRecordDO) != 1) {
            throw new CommonException("error.update.pipeline.stage.record");
        }
        return ConvertHelper.convert(stageRecordMapper.selectByPrimaryKey(stageRecordDO), PipelineStageRecordE.class);
    }

    @Override
    public PipelineStageRecordE queryPendingCheck(Long pipelineRecordId) {
        return ConvertHelper.convert(stageRecordMapper.queryPendingCheck(pipelineRecordId), PipelineStageRecordE.class);
    }
}
