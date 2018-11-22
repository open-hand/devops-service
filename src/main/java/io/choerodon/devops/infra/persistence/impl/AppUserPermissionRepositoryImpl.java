package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.AppUserPermissionE;
import io.choerodon.devops.domain.application.repository.AppUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.dataobject.AppUserPermissionDO;
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
    public List<AppUserPermissionE> listAll(Long appId) {
        return ConvertHelper
                .convertList(appUserPermissionMapper.listAllUserPermissionByAppId(appId), AppUserPermissionE.class);
    }

    @Override
    @Transactional
    public void updateAppUserPermission(Long appId, List<Long> addUserIds, List<Long> deleteUserIds) {
        // 待添加的用户列表
        addUserIds.forEach(e -> appUserPermissionMapper.insert(new AppUserPermissionDO(e, appId)));
        // 待删除的用户列表
        deleteUserIds.forEach(e -> appUserPermissionMapper.delete(new AppUserPermissionDO(e, appId)));
    }
}
