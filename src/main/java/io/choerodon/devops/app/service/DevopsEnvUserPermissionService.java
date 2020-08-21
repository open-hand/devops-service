package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsEnvUserVO;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/11.
 */
public interface DevopsEnvUserPermissionService {


    void create(DevopsEnvUserVO devopsEnvUserPermissionE);

    Page<DevopsEnvUserVO> pageByOptions(Long envId, PageRequest pageable, String params);

    void deleteByEnvId(Long envId);

    List<DevopsEnvUserVO> listByEnvId(Long envId);

    List<DevopsEnvUserPermissionDTO> listByUserId(Long userId);

    void checkEnvDeployPermission(Long userId, Long envId);

    void baseCreate(DevopsEnvUserPermissionDTO devopsEnvUserPermissionE);

    List<DevopsEnvUserPermissionDTO> baseListByEnvId(Long envId);

    List<DevopsEnvUserPermissionDTO> baseListAll(Long envId);

    void baseUpdate(Long envId, List<Long> addUsersList, List<Long> deleteUsersList);

    void baseDelete(Long envId, Long userId);

    void batchDelete(List<Long> envIds, Long userId);

    Boolean checkUserEnvPermission(Long envId, Long userId);
}
