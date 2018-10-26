package io.choerodon.devops.infra.mapper;

import java.util.List;

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
    List<DevopsEnvUserPermissionDO> pageUserEnvPermission(@Param("envId") Long envId);

    void updateEnvUserPermission(@Param("updateMap") List<String> updateMap,
                                 @Param("permission") Boolean permission,
                                 @Param("envId") Long envId);
}
