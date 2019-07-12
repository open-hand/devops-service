package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dataobject.AppUserPermissionDTO;
import io.choerodon.devops.infra.dto.ApplicationUserPermissionDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:08
 * Description:
 */

public interface ApplicationUserPermissionMapper extends Mapper<ApplicationUserPermissionDTO> {
        List<ApplicationUserPermissionDTO> listAllUserPermissionByAppId(@Param("appId") Long appId);

        void deleteByUserIdWithAppIds(@Param("appIds") List<Long> appIds, @Param("userId") Long userId);
    }
