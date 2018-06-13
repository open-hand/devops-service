package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:30
 * Description:
 */
public interface DevopsIngressPathMapper extends BaseMapper<DevopsIngressPathDO> {

    List<DevopsIngressPathDO> selectByEnvIdAndServiceName(@Param("envId") Long envId,
                                                          @Param("serviceName") String serviceName);

    List<DevopsIngressPathDO> selectByEnvIdAndServiceId(@Param("envId") Long envId,
                                                        @Param("serviceId") Long serviceId);

    boolean checkDomainAndPath(@Param("ingressId") Long ingressId, @Param("domain") String domain, @Param("path") String path);
}
