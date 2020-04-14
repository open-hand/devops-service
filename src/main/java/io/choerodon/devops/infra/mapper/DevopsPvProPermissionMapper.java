package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsPvProPermissionMapper extends BaseMapper<DevopsPvProPermissionDTO> {
    void batchInsert(@Param("pvId") Long pvId, @Param("projectIds") List<Long> projectIds);

    void batchDeleteByPvIdsAndProjectId(@Param("pvIds") List<Long> pvIds, @Param("projectId") Long projectId);

    List<DevopsPvProPermissionDTO> listByClusterId(@Param("clusterId") Long clusterId);

    void batchDelete(@Param("devopsPvProPermissionDTOS") List<DevopsPvProPermissionDTO> devopsPvProPermissionDTOS);

    List<Long> listByProjectId(@Param("projectId") Long projectId);
}
