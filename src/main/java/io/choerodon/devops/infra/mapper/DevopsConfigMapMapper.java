package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dataobject.DevopsConfigMapDO;
import io.choerodon.mybatis.common.Mapper;

public interface DevopsConfigMapMapper extends Mapper<DevopsConfigMapDO> {

    List<DevopsConfigMapDO> listByEnv(@Param("envId") Long envId,
                                      @Param("searchParam") Map<String, Object> searchParam,
                                      @Param("param") String param,
                                      @Param("appId") Long appId);
}
