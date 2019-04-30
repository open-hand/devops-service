package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.PipelineTaskDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:39 2019/4/8
 * Description:
 */
public interface PipelineTaskMapper extends BaseMapper<PipelineTaskDO> {

    PipelineTaskDO queryByAppDeployId(@Param("appDeployId") Long appDeployId);
}
