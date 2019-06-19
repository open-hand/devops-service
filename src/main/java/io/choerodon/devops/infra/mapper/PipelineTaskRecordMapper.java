package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dataobject.PipelineTaskRecordDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:27 2019/4/9
 * Description:
 */
public interface PipelineTaskRecordMapper extends Mapper<PipelineTaskRecordDO> {
    List<PipelineTaskRecordDO> queryByStageRecordId(@Param("stageRecordId") Long stageRecordId,
                                                    @Param("taskId") Long taskId);

    List<PipelineTaskRecordDO> queryAllAutoTaskRecord(@Param("pipelineRecordId") Long pipelineRecordId);
}
