package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;

import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

public interface DevopsEnvCommandMapper extends Mapper<DevopsEnvCommandDTO> {

    DevopsEnvCommandDTO queryByObject(@Param("objectType") String objectType, @Param("objectId") Long objectId);

    List<DevopsEnvCommandDTO> listInstanceCommand(@Param("objectType") String objectType, @Param("objectId") Long objectId);

    List<DevopsEnvCommandDTO> listByObject(@Param("objectType") String objectType, @Param("objectId") Long objectId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
