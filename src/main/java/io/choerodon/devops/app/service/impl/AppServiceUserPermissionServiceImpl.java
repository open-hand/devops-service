package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.devops.app.service.AppServiceUserPermissionService;
import io.choerodon.devops.infra.dto.AppServiceUserRelDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
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
    private BaseServiceClientOperator baseServiceClientOperator;


    @Override
    public void baseCreate(Long userId, Long appServiceId) {
        appServiceUserRelMapper.insert(new AppServiceUserRelDTO(userId, appServiceId));
    }

    @Override
    public void baseDeleteByAppServiceId(Long appServiceId) {
        AppServiceUserRelDTO appServiceUserRelDTO = new AppServiceUserRelDTO();
        appServiceUserRelDTO.setAppServiceId(appServiceId);
        appServiceUserRelMapper.delete(appServiceUserRelDTO);
    }

    @Override
    public void baseDeleteByUserIdAndAppIds(List<Long> appServiceIds, Long userId) {
        appServiceUserRelMapper.deleteByUserIdWithAppIds(appServiceIds, userId);
    }

    @Override
    public List<AppServiceUserRelDTO> baseListByAppId(Long appServiceId) {
        return appServiceUserRelMapper.listAllUserPermissionByAppId(appServiceId);
    }

    @Override
    public List<AppServiceUserRelDTO> baseListByUserId(Long userId) {
        AppServiceUserRelDTO appServiceUserRelDTO = new AppServiceUserRelDTO();
        appServiceUserRelDTO.setIamUserId(userId);
        return appServiceUserRelMapper.select(appServiceUserRelDTO);
    }


    @Override
    public List<AppServiceUserRelDTO> baseListAll(Long appServiceId) {
        return appServiceUserRelMapper.listAllUserPermissionByAppId(appServiceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(Long appServiceId, List<Long> addUserIds, List<Long> deleteUserIds) {
        // 待添加的用户列表
        List<IamUserDTO> addIamUsers = baseServiceClientOperator.listUsersByIds(addUserIds);
        addIamUsers.forEach(e -> appServiceUserRelMapper.insert(new AppServiceUserRelDTO(e.getId(), appServiceId)));
        // 待删除的用户列表
        deleteUserIds.forEach(e -> {
            AppServiceUserRelDTO appServiceUserRelDTO = new AppServiceUserRelDTO();
            appServiceUserRelDTO.setIamUserId(e);
            appServiceUserRelMapper.delete(appServiceUserRelDTO);
        });
    }

}
