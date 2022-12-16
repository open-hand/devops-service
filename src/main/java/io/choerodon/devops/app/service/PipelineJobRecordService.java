package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.api.vo.cd.PipelineJobRecordVO;
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

    void update(PipelineJobRecordDTO pipelineJobRecordDTO);

    int updatePendingJobToRunning(Long id);

    void updateStatus(Long jobRecordId, PipelineStatusEnum status);

    List<PipelineJobRecordDTO> listByStageRecordIdForUpdate(Long stageRecordId);

    List<PipelineJobRecordDTO> listCreatedByStageRecordIdForUpdate(Long stageRecordId);

    List<PipelineJobRecordDTO> listByStageRecordId(Long nextStageRecordId);

    List<PipelineJobRecordDTO> listByPipelineRecordId(Long pipelineRecordId);

    List<PipelineJobRecordVO> listVOByPipelineRecordId(Long pipelineRecordId);

    AuditResultVO auditJob(Long projectId, Long id, String result);

    AduitStatusChangeVO checkAuditStatus(Long projectId, Long id);

    List<PipelineJobRecordDTO> listByIds(List<Long> jobRecordIds);

    void cancelPipelineJobs(Long pipelineRecordId);

    void retryPipelineJobs(Long pipelineRecordId);

    List<PipelineJobRecordDTO> listCreatedAndPendingJobsForUpdate(Long pipelineRecordId);

    List<PipelineJobRecordDTO> listByStatusForUpdate(Long pipelineRecordId, Set<String> statusList);

    String queryLog(Long projectId, Long id);

    List<PipelineJobRecordDTO> listRunningTaskBeforeDate(Date date);
}

