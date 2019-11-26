package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface DevopsPvProPermissionMapper extends Mapper<DevopsPvProPermissionDTO> {
    void batchInsert(@Param("pvId") Long pvId, @Param("projectIds") List<Long> projectIds);

    void batchDeleteByPvIdsAndProjectId(@Param("pvIds") List<Long> pvIds, @Param("projectId") Long projectId);

    List<DevopsPvProPermissionDTO> listByClusterId(@Param("clusterId") Long clusterId);

    void batchDelete(@Param("devopsPvProPermissionDTOS") List<DevopsPvProPermissionDTO> devopsPvProPermissionDTOS);
}
