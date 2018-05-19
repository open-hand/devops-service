package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvCommandDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsEnvCommandMapper extends BaseMapper<DevopsEnvCommandDO> {

    DevopsEnvCommandDO queryByObject(@Param("objectType") String objectType, @Param("objectId") Long objectId);
}
