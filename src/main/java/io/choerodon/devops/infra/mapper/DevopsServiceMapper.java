package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.dto.DevopsServiceQueryDTO;
import io.choerodon.mybatis.common.BaseMapper;


/**
 * Created by Zenger on 2018/4/15.
 */
public interface DevopsServiceMapper extends BaseMapper<DevopsServiceDTO> {

    List<DevopsServiceQueryDTO> listDevopsServiceByPage(
            @Param("projectId") Long projectId,
            @Param("envId") Long envId,
            @Param("instanceId") Long instanceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("params") List<String> params,
            @Param("sort") String sort,
            @Param("appServiceId") Long appServiceId);

    List<DevopsServiceQueryDTO> listRunningService(@Param("envId") Long envId, @Param("appServiceId") Long appServiceId);

    List<Long> selectDeployedEnv();

    DevopsServiceQueryDTO queryById(@Param("id") Long id);

    int selectCountByOptions(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("name") String name);

    int selectCountByName(@Param("projectId") Long projectId,
                          @Param("envId") Long envId,
                          @Param("instanceId") Long instanceId,
                          @Param("searchParam") Map<String, Object> searchParam,
                          @Param("params") List<String> params,
                          @Param("appServiceId") Long appServiceId);

    Boolean checkEnvContainingService(@Param("envId") Long envId);

    int countNonDeletedServiceWithEnv(@Param("envId") Long envId, @Param("serviceId") Long serviceId);

    void updateSelectorsToNull(@Param("serviceId") Long serviceId);

    void updateEndPointToNull(@Param("serviceId") Long serviceId);

    void updateTargetAppServiceIdToNull(@Param("serviceId") Long serviceId);

    void updateTargetInstanceCodeToNull(@Param("serviceId") Long serviceId);

    void updateTargetDeploymentIdToNull(@Param("serviceId") Long serviceId);

    void setExternalIpNull(@Param("serviceId") Long serviceId);

    void deleteServiceInstance(@Param("serviceIds") List<Long> serviceIds);

    void updateStatus(@Param("serviceId") Long serviceId, @Param("status") String status);

    void updateAppServiceIdToNull(@Param("serviceId") Long serviceId);

    boolean isAllServicesInTheAppService(@Param("serviceIds") Set<Long> serviceIds,
                                         @Param("appServiceId") Long appServiceId);

    void updateAnnotationsToNull(@Param("id") Long id);
}
