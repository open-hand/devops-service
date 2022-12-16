package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;


/**
 * 流水线任务记录(PipelineJobRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:34
 */
public interface PipelineJobRecordMapper extends BaseMapper<PipelineJobRecordDTO> {

    List<PipelineJobRecordDTO> listPendingJobs(@Param("number") int number);

    int updatePendingJobToRunning(@Param("id") Long id);

    List<PipelineJobRecordDTO> listCreatedByStageIdForUpdate(@Param("stageRecordId") Long stageRecordId);

    void cancelPipelineJobs(@Param("pipelineRecordId") Long pipelineRecordId);

    void retryPipelineJobs(@Param("pipelineRecordId") Long pipelineRecordId);

    List<PipelineJobRecordDTO> listCreatedAndPendingJobsForUpdate(@Param("pipelineRecordId") Long pipelineRecordId);

    List<PipelineJobRecordDTO> listByStageRecordIdForUpdate(@Param("stageRecordId") Long stageRecordId);

    List<PipelineJobRecordDTO> listByStatusForUpdate(@Param("pipelineRecordId") Long pipelineRecordId,
                                                     @Param("statusList") Set<String> statusList);

    List<PipelineJobRecordDTO> listRunningTaskBeforeDate(@Param("date") Date date);
}

