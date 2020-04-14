package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsConfigMapDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsConfigMapMapper extends BaseMapper<DevopsConfigMapDTO> {

    List<DevopsConfigMapDTO> listByEnv(@Param("envId") Long envId,
                                       @Param("searchParam") Map<String, Object> searchParam,
                                       @Param("params") List<String> params,
                                       @Param("appServiceId") Long appServiceId);

    DevopsConfigMapDTO queryById(@Param("configMapId") Long configMapId);
}
