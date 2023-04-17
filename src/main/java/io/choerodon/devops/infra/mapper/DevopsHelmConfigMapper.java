package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHelmConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsHelmConfigMapper extends BaseMapper<DevopsHelmConfigDTO> {
    void updateAllHelmConfigRepoDefaultToFalse(@Param("projectId") Long projectId);

    void updateHelmConfigRepoDefaultToTrue(@Param("projectId") Long projectId, @Param("helmConfigId") Long helmConfigId);

    void updateDevopsHelmConfigToNonDefaultRepoOnOrganization(@Param("resourceId") Long resourceId);

    boolean checkNameExists(@Param("projectId") Long projectId, @Param("helmConfigId") Long helmConfigId, @Param("name") String name);

    List<DevopsHelmConfigDTO> listHelmConfigWithIdAndName(@Param("resourceId") Long resourceId, @Param("resourceType") String resourceType);

    DevopsHelmConfigDTO selectOneWithIdAndName(@Param("resourceId") Long resourceId, @Param("resourceType") String resourceType, @Param("defaultRepo") Boolean defaultRepo);

    DevopsHelmConfigDTO selectWithIdAndNameByAppServiceId(@Param("appServiceId") Long appServiceId);

    void batchInsert(@Param("devopsHelmConfigDTOS") List<DevopsHelmConfigDTO> devopsHelmConfigDTOS);

    DevopsHelmConfigDTO listObjectVersionNumberById(@Param("helmConfigId") Long helmConfigId);

    void updateUsernameAndPasswordToNull(@Param("id") Long id);
}
