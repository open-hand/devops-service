package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;

/**
 * 流水线任务记录(PipelineJobRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:34
 */
public interface PipelineJobRecordService {


    List<PipelineJobRecordDTO> listPendingJobs(int number);

    PipelineJobRecordDTO baseQueryById(Long id);

    void deleteByPipelineId(Long pipelineId);

    void baseCreate(PipelineJobRecordDTO pipelineJobRecordDTO);

    void baseUpdate(PipelineJobRecordDTO pipelineJobRecordDTO);

    int updatePendingJobToRunning(Long id);

    void updateStatus(Long jobRecordId, PipelineStatusEnum status);

    List<PipelineJobRecordDTO> listByStageRecordIdForUpdate(Long stageRecordId);

    List<PipelineJobRecordDTO> listByStageRecordId(Long nextStageRecordId);

    AuditResultVO auditJob(Long projectId, Long id, String result);

    AduitStatusChangeVO checkAuditStatus(Long projectId, Long id);
}

