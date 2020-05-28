package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.PipelineAppServiceDeployDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:37 2019/4/4
 * Description:
 */
public interface PipelineAppServiceDeployMapper extends BaseMapper<PipelineAppServiceDeployDTO> {
    PipelineAppServiceDeployDTO queryById(@Param("appServiceDeployId") Long appServiceDeployId);

    void updateInstanceId(@Param("instanceId") Long instanceId);

    boolean checkNameExist(@Param("name") String name, @Param("envIds") List<Long> envIds);

}
