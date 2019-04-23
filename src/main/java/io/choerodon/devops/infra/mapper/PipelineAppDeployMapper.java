package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.PipelineAppDeployDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:37 2019/4/4
 * Description:
 */
public interface PipelineAppDeployMapper extends BaseMapper<PipelineAppDeployDO> {
    PipelineAppDeployDO queryById(@Param("appDeployId") Long appDeployId);
}
