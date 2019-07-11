package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineRecordDO;
import io.choerodon.devops.infra.dto.PipelineStageRecordDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:13 2019/4/4
 * Description:
 */
public interface PipelineStageRecordMapper extends Mapper<PipelineStageRecordDO> {
    List<PipelineRecordDO> listByOptions(@Param("projectId") Long projectId,
                                         @Param("pipelineRecordId") Long pipelineRecordId);

    PipelineStageRecordDO queryPendingCheck(@Param("pipelineRecordId") Long pipelineRecordId);
}
