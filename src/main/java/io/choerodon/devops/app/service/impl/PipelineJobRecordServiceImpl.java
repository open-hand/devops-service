package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineAuditRecordService;
import io.choerodon.devops.app.service.PipelineAuditUserRecordService;
import io.choerodon.devops.app.service.PipelineJobRecordService;
import io.choerodon.devops.app.service.PipelineLogService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineJobRecordMapper;

/**
 * 流水线任务记录(PipelineJobRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:34
 */
@Service
public class PipelineJobRecordServiceImpl implements PipelineJobRecordService {
    @Autowired
    private PipelineJobRecordMapper pipelineJobRecordMapper;
    @Autowired
    private PipelineAuditRecordService pipelineAuditRecordService;
    @Autowired
    private PipelineAuditUserRecordService pipelineAuditUserRecordService;
    @Autowired
    private PipelineLogService pipelineLogService;

    @Override
    public List<PipelineJobRecordDTO> listPendingJobs(int number) {
        return pipelineJobRecordMapper.listPendingJobs(number);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        pipelineAuditRecordService.deleteByPipelineId(pipelineId);
        pipelineAuditUserRecordService.deleteByPipelineId(pipelineId);
        pipelineLogService.deleteByPipelineId(pipelineId);

        PipelineJobRecordDTO pipelineJobRecordDTO = new PipelineJobRecordDTO();
        pipelineJobRecordDTO.setPipelineId(pipelineId);
        pipelineJobRecordMapper.delete(pipelineJobRecordDTO);
    }
}

