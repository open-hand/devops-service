package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.AppUserPermissionDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:08
 * Description:
 */
public interface AppUserPermissionMapper extends BaseMapper<AppUserPermissionDO> {
    List<AppUserPermissionDO> listAllUserPermissionByAppId(@Param("appId") Long appId);

    void deleteByUserIdWithAppIds(@Param("appIds") List<Long> appIds, @Param("userId") Long userId);
}
