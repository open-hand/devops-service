package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineLogService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineLogDTO;
import io.choerodon.devops.infra.mapper.PipelineLogMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线执行日志(PipelineLog)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:45
 */
@Service
public class PipelineLogServiceImpl implements PipelineLogService {

    private static final String DEVOPS_SAVE_PIPELINE_LOG_FAILED = "devops.save.pipeline.log.failed";

    @Autowired
    private PipelineLogMapper pipelineLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
        PipelineLogDTO pipelineLogDTO = new PipelineLogDTO();
        pipelineLogDTO.setPipelineId(pipelineId);

        pipelineLogMapper.delete(pipelineLogDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineLogDTO saveLog(Long pipelineId, Long jobRecordId, String log) {
        PipelineLogDTO pipelineLogDTO = new PipelineLogDTO();
        pipelineLogDTO.setPipelineId(pipelineId);
        pipelineLogDTO.setJobRecordId(jobRecordId);
        pipelineLogDTO.setLog(log);
        return MapperUtil.resultJudgedInsertSelective(pipelineLogMapper, pipelineLogDTO, DEVOPS_SAVE_PIPELINE_LOG_FAILED);
    }

    @Override
    public String queryLastedByJobRecordId(Long jobRecordId) {
        return pipelineLogMapper.queryLastedByJobRecordId(jobRecordId);
    }
}

