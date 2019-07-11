package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.iam.entity.AppUserPermissionE;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.AppUserPermissionRepository;
<<<<<<< HEAD
import io.choerodon.devops.infra.dto.AppUserPermissionDO;
=======
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.dataobject.AppUserPermissionDTO;
>>>>>>> [IMP] 修改AppControler重构
import io.choerodon.devops.infra.mapper.AppUserPermissionMapper;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:47
 * Description:
 */

@Service
public class AppUserPermissionRepositoryImpl implements AppUserPermissionRepository {
    @Autowired
    private AppUserPermissionMapper appUserPermissionMapper;
    @Autowired
    private IamRepository iamRepository;

    @Override
    public void create(Long userId, Long appId) {
        appUserPermissionMapper.insert(new AppUserPermissionDTO(userId, appId));
    }

    @Override
    public void deleteByAppId(Long appId) {
        AppUserPermissionDTO appUserPermissionDO = new AppUserPermissionDTO();
        appUserPermissionDO.setAppId(appId);
        appUserPermissionMapper.delete(appUserPermissionDO);
    }

    @Override
    public void deleteByUserIdWithAppIds(List<Long> appIds, Long userId) {
        appUserPermissionMapper.deleteByUserIdWithAppIds(appIds, userId);
    }

    @Override
    public List<AppUserPermissionE> listAll(Long appId) {
        return ConvertHelper
                .convertList(appUserPermissionMapper.listAllUserPermissionByAppId(appId), AppUserPermissionE.class);
    }

    @Override
    public List<AppUserPermissionE> listByUserId(Long userId) {
        AppUserPermissionDTO appUserPermissionDO = new AppUserPermissionDTO();
        appUserPermissionDO.setIamUserId(userId);
        return ConvertHelper.convertList(appUserPermissionMapper.select(appUserPermissionDO), AppUserPermissionE.class);
    }

    @Override
    @Transactional
    public void updateAppUserPermission(Long appId, List<Long> addUserIds, List<Long> deleteUserIds) {
        // 待添加的用户列表
        List<UserE> addIamUsers = iamRepository.listUsersByIds(addUserIds);
        addIamUsers.forEach(e -> appUserPermissionMapper.insert(new AppUserPermissionDTO(e.getId(), appId)));
        // 待删除的用户列表
        deleteUserIds.forEach(e -> {
            AppUserPermissionDTO appUserPermissionDO = new AppUserPermissionDTO();
            appUserPermissionDO.setIamUserId(e);
            appUserPermissionMapper.delete(appUserPermissionDO);
        });
    }
}
