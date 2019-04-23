package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
import io.choerodon.devops.infra.dataobject.PipelineStageRecordDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:13 2019/4/4
 * Description:
 */
public interface PipelineStageRecordMapper extends BaseMapper<PipelineStageRecordDO> {
    List<PipelineRecordDO> listByOptions(@Param("projectId") Long projectId,
                                         @Param("pipelineRecordId") Long pipelineRecordId);
}
