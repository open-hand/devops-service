package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineAuditUserRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineAuditUserRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineAuditUserRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 人工卡点用户审核记录表(PipelineAuditUserRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:20
 */
@Service
public class PipelineAuditUserRecordServiceImpl implements PipelineAuditUserRecordService {

    private static final String DEVOPS_SAVE_AUDIT_USER_RECORD_FAILED = "devops.save.audit.user.record.failed";
    private static final String DEVOPS_UPDATE_AUDIT_USER_RECORD_FAILED = "devops.update.audit.user.record.failed";
    private static final String DEVOPS_AUDIT_RECORD_ID_IS_NULL = "devops.audit.record.id.is.null";

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineAuditUserRecordDTO pipelineAuditUserRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineAuditUserRecordMapper, pipelineAuditUserRecordDTO, DEVOPS_SAVE_AUDIT_USER_RECORD_FAILED);
    }

    @Override
    public List<PipelineAuditUserRecordDTO> listByAuditRecordId(Long auditRecordId) {
        Assert.notNull(auditRecordId, DEVOPS_AUDIT_RECORD_ID_IS_NULL);

        PipelineAuditUserRecordDTO pipelineAuditUserRecordDTO = new PipelineAuditUserRecordDTO();
        pipelineAuditUserRecordDTO.setAuditRecordId(auditRecordId);

        return pipelineAuditUserRecordMapper.select(pipelineAuditUserRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineAuditUserRecordDTO pipelineAuditUserRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineAuditUserRecordMapper, pipelineAuditUserRecordDTO, DEVOPS_UPDATE_AUDIT_USER_RECORD_FAILED);

    }
}

