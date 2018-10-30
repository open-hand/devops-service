package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvUserPermissionE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 9:36
 * Description:
 */
public interface DevopsEnvUserPermissionRepository {

    void create(DevopsEnvUserPermissionE devopsEnvUserPermissionE);

    Page<DevopsEnvUserPermissionDTO> pageUserPermissionByOption(Long envId, PageRequest pageRequest, String params);

    List<DevopsEnvUserPermissionDTO> listALlUserPermission(Long envId);

    void updateEnvUserPermission(Long envId, List<Long> userIds);
}
