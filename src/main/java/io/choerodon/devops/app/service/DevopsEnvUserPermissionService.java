package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsEnvUserPermissionVO;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO;

/**
 * Created by Sheep on 2019/7/11.
 */
public interface DevopsEnvUserPermissionService {


    void create(DevopsEnvUserPermissionVO devopsEnvUserPermissionE);

    PageInfo<DevopsEnvUserPermissionVO> pageByOptions(Long envId, PageRequest pageRequest, String params);

    void deleteByEnvId(Long envId);

    List<DevopsEnvUserPermissionVO> listByEnvId(Long envId);

    List<DevopsEnvUserPermissionDTO> listByUserId(Long userId);

    void checkEnvDeployPermission(Long userId, Long envId);

    void baseCreate(DevopsEnvUserPermissionDTO devopsEnvUserPermissionE);

    PageInfo<DevopsEnvUserPermissionDTO> basePageByOptions(Long envId, PageRequest pageRequest, String params);

    List<DevopsEnvUserPermissionDTO> baseListByEnvId(Long envId);

    List<DevopsEnvUserPermissionDTO> baseListAll(Long envId);

    void baseUpdate(Long envId, List<Long> addUsersList, List<Long> deleteUsersList);

    List<DevopsEnvUserPermissionDTO> baseListByUserId(Long userId);

    void baseCheckEnvDeployPermission(Long userId, Long envId);

    void baseDelete(Long envId, Long userId);

}
