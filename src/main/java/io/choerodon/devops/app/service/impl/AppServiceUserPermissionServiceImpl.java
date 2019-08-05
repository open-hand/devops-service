package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.devops.app.service.AppServiceUserPermissionService;
import io.choerodon.devops.infra.dto.AppServiceUserRelDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceUserRelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Sheep on 2019/7/12.
 */

@Service
public class AppServiceUserPermissionServiceImpl implements AppServiceUserPermissionService {


    @Autowired
    private AppServiceUserRelMapper appServiceUserRelMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;


@Override
    public void baseCreate(Long userId, Long appId) {
        appServiceUserRelMapper.insert(new AppServiceUserRelDTO(userId, appId));
    }

    @Override
    public void baseDeleteByAppServiceId(Long appId) {
        AppServiceUserRelDTO appServiceUserRelDTO = new AppServiceUserRelDTO();
        appServiceUserRelDTO.setAppServiceId(appId);
        appServiceUserRelMapper.delete(appServiceUserRelDTO);
    }

    @Override
    public void baseDeleteByUserIdAndAppIds(List<Long> appIds, Long userId) {
        appServiceUserRelMapper.deleteByUserIdWithAppIds(appIds, userId);
    }

    @Override
    public List<AppServiceUserRelDTO> baseListByAppId(Long appId) {
        return appServiceUserRelMapper.listAllUserPermissionByAppId(appId);
    }

    @Override
    public List<AppServiceUserRelDTO> baseListByUserId(Long userId) {
        AppServiceUserRelDTO appServiceUserRelDTO = new AppServiceUserRelDTO();
        appServiceUserRelDTO.setIamUserId(userId);
        return appServiceUserRelMapper.select(appServiceUserRelDTO);
    }


    @Override
    public List<AppServiceUserRelDTO> baseListAll(Long appId) {
        return appServiceUserRelMapper.listAllUserPermissionByAppId(appId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(Long appId, List<Long> addUserIds, List<Long> deleteUserIds) {
        // 待添加的用户列表
        List<IamUserDTO> addIamUsers = iamServiceClientOperator.listUsersByIds(addUserIds);
        addIamUsers.forEach(e -> appServiceUserRelMapper.insert(new AppServiceUserRelDTO(e.getId(), appId)));
        // 待删除的用户列表
        deleteUserIds.forEach(e -> {
            AppServiceUserRelDTO appServiceUserRelDTO = new AppServiceUserRelDTO();
            appServiceUserRelDTO.setIamUserId(e);
            appServiceUserRelMapper.delete(appServiceUserRelDTO);
        });
    }

}
