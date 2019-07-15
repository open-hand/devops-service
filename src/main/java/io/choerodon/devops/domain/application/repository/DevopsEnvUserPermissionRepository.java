package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsEnvUserPermissionVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvUserPermissionE;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 9:36
 * Description:
 */
public interface DevopsEnvUserPermissionRepository {

    void baseCreate(DevopsEnvUserPermissionE devopsEnvUserPermissionE);

    PageInfo<DevopsEnvUserPermissionVO> basePageByOptions(Long envId, PageRequest pageRequest, String params);

    List<DevopsEnvUserPermissionVO> baseListByEnvId(Long envId);

    List<DevopsEnvUserPermissionE> baseListAll(Long envId);

    void baseUpdate(Long envId, List<Long> addUsersList, List<Long> deleteUsersList);

    List<DevopsEnvUserPermissionE> baseListByUserId(Long userId);

    void baseCheckEnvDeployPermission(Long userId, Long envId);
}
