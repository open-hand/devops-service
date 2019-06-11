package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import org.apache.ibatis.annotations.Param;

import io.choerodon.mybatis.common.Mapper;


/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:30
 * Description:
 */
public interface DevopsIngressMapper extends Mapper<DevopsIngressDO> {
    List<String> queryIngressNameByServiceId(@Param("serviceId") Long serviceId);

    List<DevopsIngressDO> selectIngress(
            @Param("projectId") Long projectId,
            @Param("envId") Long envId,
            @Param("serviceId") Long serviceId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param);

    Boolean checkEnvHasIngress(@Param("envId") Long envId);
}
