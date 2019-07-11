package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsServiceDO;
import io.choerodon.devops.infra.dto.DevopsServiceQueryDO;
import org.apache.ibatis.annotations.Param;
import io.choerodon.mybatis.common.Mapper;


/**
 * Created by Zenger on 2018/4/15.
 */
public interface DevopsServiceMapper extends Mapper<DevopsServiceDO> {

    List<DevopsServiceQueryDO> listDevopsServiceByPage(
            @Param("projectId") Long projectId,
            @Param("envId") Long envId,
            @Param("instanceId") Long instanceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param,
            @Param("sort") String sort,
            @Param("appId") Long appId);

    List<DevopsServiceQueryDO> listDevopsService(@Param("envId") Long envId);

    List<Long> selectDeployedEnv();

    DevopsServiceQueryDO selectById(@Param("id") Long id);

    int selectCountByOptions(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("name") String name);

    int selectCountByName(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("instanceId") Long instanceId, @Param("searchParam") Map<String, Object> searchParam,
                          @Param("param") String param,@Param("appId") Long appId);

    Boolean checkEnvHasService(@Param("envId") Long envId);

    void setLablesToNull(@Param("serviceId") Long serviceId);

    void setEndPointToNull(@Param("serviceId") Long serviceId);

    void setExternalIpNull(@Param("serviceId") Long serviceId);

    void deleteServiceInstance(@Param("serviceIds") List<Long> serviceIds);
}
