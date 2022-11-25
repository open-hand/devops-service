package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineAuditUserRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineAuditUserRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineAuditUserRecordMapper;

/**
 * 人工卡点用户审核记录表(PipelineAuditUserRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:20
 */
@Service
public class PipelineAuditUserRecordServiceImpl implements PipelineAuditUserRecordService {
    @Autowired
    private PipelineAuditUserRecordMapper pipelineAuditUserRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineAuditUserRecordDTO pipelineAuditUserRecordDTO = new PipelineAuditUserRecordDTO();
        pipelineAuditUserRecordDTO.setPipelineId(pipelineId);
        pipelineAuditUserRecordMapper.delete(pipelineAuditUserRecordDTO);
    }
}

