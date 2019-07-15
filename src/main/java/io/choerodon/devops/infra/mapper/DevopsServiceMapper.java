package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.dto.DevopsServiceQueryDTO;
import org.apache.ibatis.annotations.Param;
import io.choerodon.mybatis.common.Mapper;


/**
 * Created by Zenger on 2018/4/15.
 */
public interface DevopsServiceMapper extends Mapper<DevopsServiceDTO> {

    List<DevopsServiceQueryDTO> listDevopsServiceByPage(
            @Param("projectId") Long projectId,
            @Param("envId") Long envId,
            @Param("instanceId") Long instanceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param,
            @Param("sort") String sort,
            @Param("appId") Long appId);

    List<DevopsServiceQueryDTO> listByEnvId(@Param("envId") Long envId);

    List<Long> selectDeployedEnv();

    DevopsServiceQueryDTO queryById(@Param("id") Long id);

    int selectCountByOptions(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("name") String name);

    int selectCountByName(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("instanceId") Long instanceId, @Param("searchParam") Map<String, Object> searchParam,
                          @Param("param") String param,@Param("appId") Long appId);

    Boolean checkServiceByEnv(@Param("envId") Long envId);

    void updateLables(@Param("serviceId") Long serviceId);

    void updateEndPoint(@Param("serviceId") Long serviceId);

    void setExternalIpNull(@Param("serviceId") Long serviceId);

    void deleteServiceInstance(@Param("serviceIds") List<Long> serviceIds);
}
