package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: Runge
 * Date: 2018/5/8
 * Time: 15:47
 * Description:
 */
public interface DevopsEnvPodContainerMapper extends BaseMapper<DevopsEnvPodContainerDO> {

    List<DevopsEnvPodContainerDO> listContainer(@Param("podId") Long podId,
                                                @Param("searchParam") Map<String, Object> searchParam,
                                                @Param("param") String param);
}
