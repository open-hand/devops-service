package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.PipelineAppDeployDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:37 2019/4/4
 * Description:
 */
public interface PipelineAppDeployMapper extends Mapper<PipelineAppDeployDO> {
    PipelineAppDeployDO queryById(@Param("appDeployId") Long appDeployId);

    void updateInstanceId(@Param("instanceId") Long instanceId);
}
