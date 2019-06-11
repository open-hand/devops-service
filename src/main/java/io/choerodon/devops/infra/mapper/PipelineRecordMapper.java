package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:41 2019/4/4
 * Description:
 */
public interface PipelineRecordMapper extends Mapper<PipelineRecordDO> {
    List<PipelineRecordDO> listByOptions(@Param("projectId") Long projectId,
                                         @Param("pipelineId") Long pipelineId,
                                         @Param("searchParam") Map<String, Object> searchParam,
                                         @Param("param") String param,
                                         @Param("classifyParam") Map<String, Object> classifyParam);

    PipelineRecordDO queryById(@Param("pipelineRecordId") Long pipelineRecordId);

    void updateEdited(@Param("pipelineId") Long pipelineId);

    List<Long> queryAllRecordUserIds(@Param("pipelineRecordId") Long pipelineRecordId);

}
