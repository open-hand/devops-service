package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:41 2019/4/4
 * Description:
 */
public interface PipelineRecordMapper extends Mapper<PipelineRecordDTO> {
    List<PipelineRecordDTO> listByOptions(@Param("projectId") Long projectId,
                                          @Param("pipelineId") Long pipelineId,
                                          @Param("searchParam") Map<String, Object> searchParam,
                                          @Param("params") List<String> params,
                                          @Param("classifyParam") Map<String, Object> classifyParam);

    PipelineRecordDTO queryById(@Param("pipelineRecordId") Long pipelineRecordId);

    void updateEdited(@Param("pipelineId") Long pipelineId);

    List<Long> queryAllRecordUserIds(@Param("pipelineRecordId") Long pipelineRecordId);

    List<PipelineRecordDTO> listAllPipelineRecordAndEnv(@Param("pipelineRecordId") Long pipelineRecordId);
}
