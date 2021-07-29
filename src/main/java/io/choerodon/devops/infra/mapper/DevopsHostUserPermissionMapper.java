package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHostUserPermissionDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsHostUserPermissionMapper extends BaseMapper<DevopsHostUserPermissionDTO> {
    List<DevopsHostUserPermissionDTO> listByHostId(@Param("hostId") Long hostId);
}
