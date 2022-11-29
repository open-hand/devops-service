package io.choerodon.devops.infra.mapper;

import java.util.List;

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

    List<PipelineJobRecordDTO> listByStageIdForUpdate(@Param("stageRecordId") Long stageRecordId);
}

