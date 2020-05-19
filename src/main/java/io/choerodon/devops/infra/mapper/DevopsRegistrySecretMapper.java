package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsRegistrySecretDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Sheep on 2019/3/14.
 */
public interface DevopsRegistrySecretMapper extends BaseMapper<DevopsRegistrySecretDTO> {


    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") Boolean status);

    DevopsRegistrySecretDTO baseQueryByClusterIdAndNamespace(
            @Param("configId") Long configId,
            @Param("clusterId") Long clusterId,
            @Param("namespace") String namespace,
            @Param("project_id") Long projectId);
}
