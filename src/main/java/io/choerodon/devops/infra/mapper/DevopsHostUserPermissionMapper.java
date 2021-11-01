package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHostUserPermissionDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsHostUserPermissionMapper extends BaseMapper<DevopsHostUserPermissionDTO> {
    List<Long> listUserIdsByHostId(@Param("hostId") Long hostId);

    List<DevopsHostUserPermissionDTO> listUserHostPermissionByOption(@Param("hostId") Long hostId, @Param("searchParam") Map<String, Object> searchParamMap, @Param("params") List<String> paramList);

    String queryPermissionLabelByHostIdAndUserId(@Param("hostId") Long hostId, @Param("userId") Long userId);

    List<DevopsHostUserPermissionDTO> listUserHostPermissionByUserIdAndHostIds(@Param("userId") Long userId, @Param("hostIds") List<Long> hostIds);

    void deleteByHostIdAndUserIds(@Param("hostId") Long hostId, @Param("userIds") List<Long> userIds);

    void deleteByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
