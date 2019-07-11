package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvUserPermissionE;

/**
 * Created by Sheep on 2019/7/11.
 */
public interface DevopsEnvUserPermissionService {


    void create(DevopsEnvUserPermissionE devopsEnvUserPermissionE);

    void delete(Long envId, Long userId);

    PageInfo<DevopsEnvUserPermissionDTO> pageUserPermissionByOption(Long envId, PageRequest pageRequest, String params);

    List<DevopsEnvUserPermissionDTO> listALlUserPermission(Long envId);

    List<DevopsEnvUserPermissionE> listAll(Long envId);

    void updateEnvUserPermission(Long envId, List<Long> addUsersList, List<Long> deleteUsersList);

    List<DevopsEnvUserPermissionE> listByUserId(Long userId);

    void checkEnvDeployPermission(Long userId, Long envId);

}
