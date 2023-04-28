package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvResourceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceMapper extends BaseMapper<DevopsEnvResourceDTO> {
    List<DevopsEnvResourceDTO> listJobs(@Param("commandId") Long commandId);

    List<DevopsEnvResourceDTO> queryResource(@Param("instanceId") Long instanceId,
                                             @Param("commandId") Long commandId,
                                             @Param("envId") Long envId,
                                             @Param("kind") String kind,
                                             @Param("name") String name);

    List<DevopsEnvResourceDTO> listByEnvAndType(@Param("envId") Long envId,
                                                @Param("type") String type);

    DevopsEnvResourceDTO queryLatestJob(@Param("envId") Long envId,
                                        @Param("kind") String kind,
                                        @Param("name") String name);

    String getResourceDetailByNameAndTypeAndInstanceId(@Param("instanceId") Long instanceId, @Param("name") String name, @Param("kind") String resourceType);

    List<DevopsEnvResourceDTO> listEnvResourceByOptions(@Param("envId") Long envId, @Param("kind") String type, @Param("names") List<String> names);

    String getResourceDetailByEnvIdAndKindAndName(@Param("envId") Long envId,
                                                  @Param("kind") String kind,
                                                  @Param("name") String name);

    List<DevopsEnvResourceDTO> getResourceWithDetailByInstanceIdAndKind(@Param("instanceId") Long instanceId, @Param("kind") String kind);

    List<Long> listInstanceIdsDuplicatedStatefulset();

    List<DevopsEnvResourceDTO> listStatefulsetByInstanceIds(@Param("instanceIds") List<Long> instanceIds);

    void deleteByIds(@Param("resourceIdsToDelete") List<Long> resourceIdsToDelete);
}
