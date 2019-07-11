package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDO;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 9:17
 * Description:
 */
public interface DevopsEnvUserPermissionMapper extends Mapper<DevopsEnvUserPermissionDO> {

    List<DevopsEnvUserPermissionDO> pageUserEnvPermissionByOption(@Param("envId") Long envId,
                                                                  @Param("searchParam") Map<String, Object> searchParam,
                                                                  @Param("param") String param);

    List<DevopsEnvUserPermissionDO> listByEnvId(@Param("envId") Long envId);

    List<DevopsEnvUserPermissionDO> listAll(@Param("envId") Long envId);
}
