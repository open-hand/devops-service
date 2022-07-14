package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHelmConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsHelmConfigMapper extends BaseMapper<DevopsHelmConfigDTO> {
    void updateAllHelmConfigRepoDefaultToFalse(@Param("projectId") Long projectId);

    void updateHelmConfigRepoDefaultToTrue(@Param("projectId") Long projectId, @Param("helmConfigId") Long helmConfigId);
}
