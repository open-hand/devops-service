package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
}
