package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.PipelineTaskRecordDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:27 2019/4/9
 * Description:
 */
public interface PipelineTaskRecordMapper extends BaseMapper<PipelineTaskRecordDO> {
    List<PipelineTaskRecordDO> queryByStageRecordId(@Param("stageRecordId") Long stageRecordId,
                                                    @Param("taskId") Long taskId);
}
