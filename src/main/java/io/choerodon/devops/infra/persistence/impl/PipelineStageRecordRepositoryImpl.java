package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineStageRecordE;
import io.choerodon.devops.domain.application.repository.PipelineStageRecordRepository;
import io.choerodon.devops.infra.dataobject.PipelineStageRecordDO;
import io.choerodon.devops.infra.mapper.PipelineStageRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
            stageRecordDO.setObjectVersionNumber(null);
        }
        return ConvertHelper.convert(stageRecordMapper.selectOne(stageRecordDO), PipelineStageRecordE.class);
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
}
