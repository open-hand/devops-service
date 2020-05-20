package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineTaskRecordDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:27 2019/4/9
 * Description:
 */
public interface PipelineTaskRecordMapper extends Mapper<PipelineTaskRecordDTO> {
    List<PipelineTaskRecordDTO> queryByStageRecordId(@Param("stageRecordId") Long stageRecordId,
                                                     @Param("taskId") Long taskId);

    List<PipelineTaskRecordDTO> queryAllAutoTaskRecord(@Param("pipelineRecordId") Long pipelineRecordId);
}
