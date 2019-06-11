package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvResourceDO;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceMapper extends Mapper<DevopsEnvResourceDO> {
    List<DevopsEnvResourceDO> listJobs(@Param("commandId") Long commandId);

    DevopsEnvResourceDO queryResource(@Param("instanceId") Long instanceId,
                                      @Param("commandId") Long commandId,
                                      @Param("envId") Long envId,
                                      @Param("kind") String kind,
                                      @Param("name") String name);

    List<DevopsEnvResourceDO> listByEnvAndType(@Param("envId") Long envId,
                                               @Param("type") String type);

    DevopsEnvResourceDO queryLatestJob(@Param("kind") String kind,
                                       @Param("name") String name);

    String getResourceDetailByNameAndTypeAndInstanceId(@Param("instanceId") Long instanceId, @Param("name") String name,  @Param("kind") String resourceType);
}
