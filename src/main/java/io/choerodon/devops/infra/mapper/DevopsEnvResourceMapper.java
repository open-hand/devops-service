package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvResourceDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceMapper extends BaseMapper<DevopsEnvResourceDO> {
    List<DevopsEnvResourceDO> listJobByInstanceId(@Param("instanceId") Long instanceId);

    DevopsEnvResourceDO queryResource(@Param("instanceId") Long instanceId,
                                      @Param("command_id") Long commandId,
                                      @Param("env_id") Long envId,
                                      @Param("kind") String kind,
                                      @Param("name") String name);

    List<DevopsEnvResourceDO> listByEnvAndType(@Param("envId") Long envId,
                                               @Param("type") String type);

    DevopsEnvResourceDO queryLatestJob(@Param("kind") String kind,
                                       @Param("name") String name);
}
