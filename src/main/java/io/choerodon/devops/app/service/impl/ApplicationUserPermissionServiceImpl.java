package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.devops.app.service.ApplicationUserPermissionService;
import io.choerodon.devops.infra.dto.ApplicationUserPermissionDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.ApplicationUserPermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Sheep on 2019/7/12.
 */

@Service
public class ApplicationUserPermissionServiceImpl implements ApplicationUserPermissionService {


    @Autowired
    private ApplicationUserPermissionMapper applicationUserPermissionMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;


@Override
    public void baseCreate(Long userId, Long appId) {
        applicationUserPermissionMapper.insert(new ApplicationUserPermissionDTO(userId, appId));
    }

    @Override
    public void baseDeleteByAppId(Long appId) {
        ApplicationUserPermissionDTO applicationUserPermissionDTO = new ApplicationUserPermissionDTO();
        applicationUserPermissionDTO.getAppServiceId(appId);
        applicationUserPermissionMapper.delete(applicationUserPermissionDTO);
    }

    @Override
    public void baseDeleteByUserIdAndAppIds(List<Long> appIds, Long userId) {
        applicationUserPermissionMapper.deleteByUserIdWithAppIds(appIds, userId);
    }

    @Override
    public List<ApplicationUserPermissionDTO> baseListByAppId(Long appId) {
        return applicationUserPermissionMapper.listAllUserPermissionByAppId(appId);
    }

    @Override
    public List<ApplicationUserPermissionDTO> baseListByUserId(Long userId) {
        ApplicationUserPermissionDTO applicationUserPermissionDTO = new ApplicationUserPermissionDTO();
        applicationUserPermissionDTO.setIamUserId(userId);
        return applicationUserPermissionMapper.select(applicationUserPermissionDTO);
    }


    @Override
    public List<ApplicationUserPermissionDTO> baseListAll(Long appId) {
        return applicationUserPermissionMapper.listAllUserPermissionByAppId(appId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(Long appId, List<Long> addUserIds, List<Long> deleteUserIds) {
        // 待添加的用户列表
        List<IamUserDTO> addIamUsers = iamServiceClientOperator.listUsersByIds(addUserIds);
        addIamUsers.forEach(e -> applicationUserPermissionMapper.insert(new ApplicationUserPermissionDTO(e.getId(), appId)));
        // 待删除的用户列表
        deleteUserIds.forEach(e -> {
            ApplicationUserPermissionDTO applicationUserPermissionDTO = new ApplicationUserPermissionDTO();
            applicationUserPermissionDTO.setIamUserId(e);
            applicationUserPermissionMapper.delete(applicationUserPermissionDTO);
        });
    }

}
