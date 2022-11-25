package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线执行记录(PipelineRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:02
 */
@Service
public class PipelineRecordServiceImpl implements PipelineRecordService {

    private static final String DEVOPS_SAVE_PIPELINE_RECORD_FAILED = "devops.save.pipeline.record.failed";

    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO();
        pipelineRecordDTO.setPipelineId(pipelineId);
        pipelineRecordMapper.delete(pipelineRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineRecordDTO pipelineRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineRecordMapper,
                pipelineRecordDTO,
                DEVOPS_SAVE_PIPELINE_RECORD_FAILED);
    }
}

