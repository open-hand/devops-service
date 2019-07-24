package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvResourceDTO;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceMapper extends Mapper<DevopsEnvResourceDTO> {
    List<DevopsEnvResourceDTO> listJobs(@Param("commandId") Long commandId);

    DevopsEnvResourceDTO queryResource(@Param("instanceId") Long instanceId,
                                       @Param("commandId") Long commandId,
                                       @Param("envId") Long envId,
                                       @Param("kind") String kind,
                                       @Param("name") String name);

    List<DevopsEnvResourceDTO> listByEnvAndType(@Param("envId") Long envId,
                                                @Param("type") String type);

    DevopsEnvResourceDTO queryLatestJob(@Param("kind") String kind,
                                        @Param("name") String name);

    String getResourceDetailByNameAndTypeAndInstanceId(@Param("instanceId") Long instanceId, @Param("name") String name,  @Param("kind") String resourceType);
}
