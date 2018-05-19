package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvPodDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 11:53
 * Description:
 */
public interface DevopsEnvPodMapper extends BaseMapper<DevopsEnvPodDO> {

    List<DevopsEnvPodDO> listAppPod(@Param("projectId") Long projectId,
                                    @Param("searchParam") Map<String, Object> searchParam,
                                    @Param("param") String param);
}
