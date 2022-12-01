package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.cd.PipelineAuditCfgVO;
import io.choerodon.devops.app.service.PipelineAuditCfgService;
import io.choerodon.devops.app.service.PipelineAuditRecordService;
import io.choerodon.devops.app.service.PipelineAuditUserRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineAuditRecordDTO;
import io.choerodon.devops.infra.dto.PipelineAuditUserRecordDTO;
import io.choerodon.devops.infra.enums.cd.CdAuditStatusEnum;
import io.choerodon.devops.infra.mapper.PipelineAuditRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 人工卡点审核记录表(PipelineAuditRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:03
 */
@Service
public class PipelineAuditRecordServiceImpl implements PipelineAuditRecordService {

    private static final String DEVOPS_SAVE_AUDIT_RECORD_FAILED = "devops.save.audit.record.failed";

    @Autowired
    private PipelineAuditRecordMapper pipelineAuditRecordMapper;
    @Autowired
    private PipelineAuditCfgService pipelineAuditCfgService;
    @Autowired
    private PipelineAuditUserRecordService pipelineAuditUserRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineAuditRecordDTO pipelineAuditRecordDTO = new PipelineAuditRecordDTO();
        pipelineAuditRecordDTO.setPipelineId(pipelineId);

        pipelineAuditRecordMapper.delete(pipelineAuditRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineAuditRecordDTO pipelineAuditRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineAuditRecordMapper, pipelineAuditRecordDTO, DEVOPS_SAVE_AUDIT_RECORD_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initAuditRecord(Long pipelineId, Long jobRecordId, Long configId) {
        PipelineAuditCfgVO pipelineAuditCfgVO = pipelineAuditCfgService.queryConfigWithUsersById(configId);
        PipelineAuditRecordDTO pipelineAuditRecordDTO = new PipelineAuditRecordDTO(pipelineId, jobRecordId, pipelineAuditCfgVO.getCountersigned());
        baseCreate(pipelineAuditRecordDTO);

        Long auditRecordId = pipelineAuditRecordDTO.getId();

        List<Long> auditUserIds = pipelineAuditCfgVO.getAuditUserIds();
        auditUserIds.forEach(uid -> {
            PipelineAuditUserRecordDTO pipelineAuditUserRecordDTO = new PipelineAuditUserRecordDTO(pipelineId,
                    auditRecordId,
                    uid,
                    CdAuditStatusEnum.NOT_AUDIT.value());
            pipelineAuditUserRecordService.baseCreate(pipelineAuditUserRecordDTO);
        });
    }

    @Override
    public PipelineAuditRecordDTO queryByJobRecordId(Long jobRecordId) {
        Assert.notNull(jobRecordId, PipelineCheckConstant.DEVOPS_JOB_RECORD_ID_IS_NULL);
        PipelineAuditRecordDTO pipelineAuditRecordDTO = new PipelineAuditRecordDTO();
        pipelineAuditRecordDTO.setJobRecordId(jobRecordId);
        return pipelineAuditRecordMapper.selectOne(pipelineAuditRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineAuditRecordDTO queryByJobRecordIdForUpdate(Long jobRecordId) {
        return pipelineAuditRecordMapper.queryByJobRecordIdForUpdate(jobRecordId);
    }

}

