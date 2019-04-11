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
    public List<PipelineStageRecordE> list(Long projectId, Long pipelineId) {
        PipelineStageRecordDO stageRecordDO = new PipelineStageRecordDO(projectId, pipelineId);
        return ConvertHelper.convertList(stageRecordMapper.select(stageRecordDO), PipelineStageRecordE.class);
    }

    @Override
    public PipelineStageRecordE create(PipelineStageRecordE stageRecordE) {
        PipelineStageRecordDO stageRecordDO = ConvertHelper.convert(stageRecordE, PipelineStageRecordDO.class);
        if (stageRecordMapper.insert(stageRecordDO) != 1) {
            throw new CommonException("error.insert.pipeline.stage.record");
        }
        return ConvertHelper.convert(stageRecordMapper.selectOne(stageRecordDO), PipelineStageRecordE.class);
    }
}
