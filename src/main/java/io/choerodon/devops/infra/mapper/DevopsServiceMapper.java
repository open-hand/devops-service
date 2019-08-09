package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.dto.DevopsServiceQueryDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * Created by Zenger on 2018/4/15.
 */
public interface DevopsServiceMapper extends Mapper<DevopsServiceDTO> {

    List<DevopsServiceQueryDTO> listDevopsServiceByPage(
            @Param("projectId") Long projectId,
            @Param("envId") Long envId,
            @Param("instanceId") Long instanceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("params") List<String> params,
            @Param("sort") String sort,
            @Param("appServiceId") Long appServiceId);

    List<DevopsServiceQueryDTO> listRunningService(@Param("envId") Long envId);

    List<Long> selectDeployedEnv();

    DevopsServiceQueryDTO queryById(@Param("id") Long id);

    int selectCountByOptions(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("name") String name);

    int selectCountByName(@Param("projectId") Long projectId,
                          @Param("envId") Long envId,
                          @Param("instanceId") Long instanceId,
                          @Param("searchParam") Map<String, Object> searchParam,
                          @Param("params") List<String> params,
                          @Param("appServiceId") Long appServiceId);

    Boolean checkServiceByEnv(@Param("envId") Long envId);

    void setLabelsToNull(@Param("serviceId") Long serviceId);

    void setEndPointToNull(@Param("serviceId") Long serviceId);

    void setExternalIpNull(@Param("serviceId") Long serviceId);

    void deleteServiceInstance(@Param("serviceIds") List<Long> serviceIds);
}
