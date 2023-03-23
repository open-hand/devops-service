package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:33 2019/4/10
 * Description:
 */
public interface DevopsDeployValueMapper extends BaseMapper<DevopsDeployValueDTO> {
    List<DevopsDeployValueDTO> listByOptions(@Param("projectId") Long projectId,
                                             @Param("appServiceId") Long appServiceId,
                                             @Param("envId") Long envId,
                                             @Param("userId") Long userId,
                                             @Param("searchParam") Map<String, Object> searchParam,
                                             @Param("params") List<String> params);

    DevopsDeployValueDTO queryById(@Param("valueId") Long id);

    List<DevopsDeployValueDTO> listByOptionsWithOwner(@Param("projectId") Long projectId,
                                                      @Param("appServiceId") Long appServiceId,
                                                      @Param("envId") Long envId,
                                                      @Param("userId") Long userId,
                                                      @Param("searchParam") Map<String, Object> searchParam,
                                                      @Param("params") List<String> params);

    List<DevopsDeployValueDTO> listByOptionsWithMember(@Param("projectId") Long projectId,
                                                       @Param("appServiceId") Long appServiceId,
                                                       @Param("envId") Long envId,
                                                       @Param("userId") Long userId,
                                                       @Param("searchParam") Map<String, Object> searchParam,
                                                       @Param("params") List<String> params);

    List<DevopsDeployValueDTO> listByAppServiceIdAndEnvId(@Param("projectId") Long projectId,
                                                          @Param("appServiceId") Long appServiceId,
                                                          @Param("envId") Long envId,
                                                          @Param("name") String name);

    List<DevopsDeployValueDTO> listByInstanceId(@Param("instanceId") Long instanceId);
}
