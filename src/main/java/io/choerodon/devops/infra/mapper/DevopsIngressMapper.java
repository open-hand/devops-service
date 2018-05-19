package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:30
 * Description:
 */
public interface DevopsIngressMapper extends BaseMapper<DevopsIngressDO> {
    List<String> queryIngressNameByServiceId(@Param("serviceId") Long serviceId);

    List<DevopsIngressDO> selectIngerss(
            @Param("projectId") Long projectId,
            @Param("searchParam") Map<String, Object> searchParam,
            @Param("param") String param);
}
