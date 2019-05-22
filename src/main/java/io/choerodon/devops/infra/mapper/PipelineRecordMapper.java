package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:41 2019/4/4
 * Description:
 */
public interface PipelineRecordMapper extends BaseMapper<PipelineRecordDO> {
    List<PipelineRecordDO> listByOptions(@Param("projectId") Long projectId,
                                         @Param("pipelineId") Long pipelineId,
                                         @Param("searchParam") Map<String, Object> searchParam,
                                         @Param("param") String param);

    PipelineRecordDO queryById(@Param("pipelineRecordId") Long pipelineRecordId);

    void updateEdited(@Param("pipelineId") Long pipelineId);
}
