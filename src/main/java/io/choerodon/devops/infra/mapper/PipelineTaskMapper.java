package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.PipelineTaskDTO;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:39 2019/4/8
 * Description:
 */
public interface PipelineTaskMapper extends BaseMapper<PipelineTaskDTO> {

    PipelineTaskDTO queryByAppDeployId(@Param("appServiceDeployId") Long appServiceDeployId);

    void deletePipelineTask();
}
