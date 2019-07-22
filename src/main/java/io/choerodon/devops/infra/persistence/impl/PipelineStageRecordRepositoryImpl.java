package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.repository.PipelineStageRecordRepository;
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
    public List<PipelineStageRecordE> baseListByRecordId(Long projectId, Long pipelineRecordId) {
        return ConvertHelper.convertList(stageRecordMapper.listByOptions(projectId, pipelineRecordId), PipelineStageRecordE.class);
    }

}
