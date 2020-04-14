package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 9:17
 * Description:
 */
public interface DevopsEnvUserPermissionMapper extends BaseMapper<DevopsEnvUserPermissionDTO> {

    List<DevopsEnvUserPermissionDTO> listUserEnvPermissionByOption(@Param("envId") Long envId,
                                                                   @Param("searchParam") Map<String, Object> searchParam,
                                                                   @Param("params") List<String> params);

    List<DevopsEnvUserPermissionDTO> listByEnvId(@Param("envId") Long envId);

    List<DevopsEnvUserPermissionDTO> listAll(@Param("envId") Long envId);

    List<Long> listUserIdsByEnvId(@Param("envId") Long envId);

    void batchDelete(@Param("envIds") List<Long> envIds, @Param("userId") Long userId);
}
