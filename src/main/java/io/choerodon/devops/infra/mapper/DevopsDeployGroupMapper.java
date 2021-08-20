package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsDeployGroupDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface DevopsDeployGroupMapper extends BaseMapper<DevopsDeployGroupDTO> {

    DevopsDeployGroupDTO queryById(@Param("projectId") Long projectId,
                                   @Param("devopsConfigGroupId") Long devopsConfigGroupId);
}