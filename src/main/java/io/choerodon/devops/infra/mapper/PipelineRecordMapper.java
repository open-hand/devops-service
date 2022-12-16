package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.cd.PipelineRecordVO;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线执行记录(PipelineRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:02
 */
public interface PipelineRecordMapper extends BaseMapper<PipelineRecordDTO> {

    void updateStatusToFailed(@Param("pipelineRecordId") Long pipelineRecordId,
                              @Param("finishDate") Date finishDate,
                              @Param("status") String status);

    List<PipelineRecordVO> listByPipelineId(@Param("pipelineId") Long pipelineId);

    void retryPipeline(@Param("id") Long id,
                       @Param("startDate") Date startDate);

    PipelineRecordDTO queryByIdForUpdate(@Param("id") Long id);

    List<PipelineRecordVO> listUserAuditRecordsByPipelineId(@Param("pipelineId") Long pipelineId, @Param("userId") Long userId);
}

