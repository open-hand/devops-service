package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineStageRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineStageRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线阶段记录(PipelineStageRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:13
 */
@Service
public class PipelineStageRecordServiceImpl implements PipelineStageRecordService {

    private static final String DEVOPS_SAVE_STAGE_RECORD_FAILED = "devops.save.stage.record.failed";

    @Autowired
    private PipelineStageRecordMapper pipelineStageRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO();
        pipelineStageRecordDTO.setPipelineId(pipelineId);
        pipelineStageRecordMapper.delete(pipelineStageRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineStageRecordDTO pipelineStageRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineStageRecordMapper, pipelineStageRecordDTO, DEVOPS_SAVE_STAGE_RECORD_FAILED);

    }
}

