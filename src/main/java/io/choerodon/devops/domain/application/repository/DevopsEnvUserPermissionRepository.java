package io.choerodon.devops.domain.application.repository;

import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.EnvUserPermissionDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 9:36
 * Description:
 */
public interface DevopsEnvUserPermissionRepository {

    Page<EnvUserPermissionDTO> pageUserPermission(Long envId, PageRequest pageRequest);

    void updateEnvUserPermission(Map<String, Boolean> updateMap, Long envId);
}
