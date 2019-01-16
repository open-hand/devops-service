package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;

import io.choerodon.devops.infra.dataobject.DevopsEnvCommandDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface DevopsEnvCommandMapper extends BaseMapper<DevopsEnvCommandDO> {

    DevopsEnvCommandDO queryByObject(@Param("objectType") String objectType, @Param("objectId") Long objectId);

    List<DevopsEnvCommandDO> queryInstanceCommand(@Param("objectType") String objectType, @Param("objectId") Long objectId);

    List<DevopsEnvCommandDO> listByObject(@Param("objectType") String objectType, @Param("objectId") Long objectId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
