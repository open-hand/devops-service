package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsConfigMapDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsConfigMapMapper extends BaseMapper<DevopsConfigMapDO> {

    List<DevopsConfigMapDO> listByEnv(@Param("envId") Long envId,
                                      @Param("searchParam") Map<String, Object> searchParam,
                                      @Param("param") String param);

}
