package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsIngressDTO;
import org.apache.ibatis.annotations.Param;

import io.choerodon.mybatis.common.Mapper;


/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:30
 * Description:
 */
public interface DevopsIngressMapper extends Mapper<DevopsIngressDTO> {
    List<String> listIngressNameByServiceId(@Param("serviceId") Long serviceId);

    DevopsIngressDTO queryById(@Param("id") Long ingressId);

    List<DevopsIngressDTO> selectIngress(
            @Param("projectId") Long projectId,
            @Param("envId") Long envId,
            @Param("serviceId") Long serviceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param);

    Boolean checkEnvHasIngress(@Param("envId") Long envId);
}
