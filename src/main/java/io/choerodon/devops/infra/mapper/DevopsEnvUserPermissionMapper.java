package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvUserPermissionDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 9:17
 * Description:
 */
public interface DevopsEnvUserPermissionMapper extends BaseMapper<DevopsEnvUserPermissionDO> {

    List<DevopsEnvUserPermissionDO> pageUserEnvPermissionByOption(@Param("envId") Long envId,
                                                                  @Param("searchParam") Map<String, Object> searchParam,
                                                                  @Param("param") String param);

    void initUserPermission(@Param("envId") Long envId);

    List<DevopsEnvUserPermissionDO> listAllUserPermission(@Param("envId") Long envId);

    void updateEnvUserPermission(@Param("envId") Long envId, @Param("userIds") List<Long> userIds);
}
