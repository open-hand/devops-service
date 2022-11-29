package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineJobRecordService;
import io.choerodon.devops.app.service.PipelineRecordService;
import io.choerodon.devops.app.service.PipelineStageRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.enums.cd.CdJobTypeEnum;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
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
    private static final String DEVOPS_UPDATE_PIPELINE_RECORD_FAILED = "devops.update.pipeline.record.failed";

    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;
    @Autowired
    @Lazy
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    @Lazy
    private PipelineJobRecordService pipelineJobRecordService;

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

    @Override
    public PipelineRecordDTO baseQueryById(Long pipelineRecordId) {
        return pipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineRecordDTO pipelineRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineRecordMapper,
                pipelineRecordDTO,
                DEVOPS_UPDATE_PIPELINE_RECORD_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateToEndStatus(Long pipelineRecordId, PipelineStatusEnum status) {
        pipelineRecordMapper.updateStatusToFailed(pipelineRecordId, new Date(), status.value());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startNextStage(PipelineRecordDTO pipelineRecordDTO, PipelineStageRecordDTO firstStageRecordDTO, List<PipelineJobRecordDTO> firstJobRecordList) {
        boolean hasAuditJob = false;
        for (PipelineJobRecordDTO pipelineJobRecordDTO : firstJobRecordList) {
            if (CdJobTypeEnum.AUDIT.value().equals(pipelineJobRecordDTO.getType())) {
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
                hasAuditJob = true;
            } else {
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.PENDING.value());
            }
            pipelineJobRecordService.baseUpdate(pipelineJobRecordDTO);
        }
        if (Boolean.TRUE.equals(hasAuditJob)) {
            firstStageRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
            pipelineRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
        } else {
            firstStageRecordDTO.setStatus(PipelineStatusEnum.PENDING.value());
            pipelineRecordDTO.setStatus(PipelineStatusEnum.RUNNING.value());
        }
        pipelineStageRecordService.baseUpdate(firstStageRecordDTO);
        baseUpdate(pipelineRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startNextStage(Long nextStageRecordId) {
        PipelineStageRecordDTO pipelineStageRecordDTO = pipelineStageRecordService.baseQueryById(nextStageRecordId);
        PipelineRecordDTO pipelineRecordDTO = baseQueryById(pipelineStageRecordDTO.getPipelineRecordId());
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listByStageRecordId(nextStageRecordId);
        startNextStage(pipelineRecordDTO, pipelineStageRecordDTO, pipelineJobRecordDTOS);
    }
}

