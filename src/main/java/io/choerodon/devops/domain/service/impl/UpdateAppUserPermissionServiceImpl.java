package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.AppUserPermissionE;
import io.choerodon.devops.domain.application.repository.AppUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.service.UpdateUserPermissionService;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 16:08
 * Description:
 */
public class UpdateAppUserPermissionServiceImpl extends UpdateUserPermissionService {

    private ApplicationRepository applicationRepository;
    private AppUserPermissionRepository appUserPermissionRepository;

    public UpdateAppUserPermissionServiceImpl() {
        this.applicationRepository = ApplicationContextHelper.getSpringFactory().getBean(ApplicationRepository.class);
        this.appUserPermissionRepository = ApplicationContextHelper.getSpringFactory()
                .getBean(AppUserPermissionRepository.class);
    }

    @Override
    public Boolean updateUserPermission(Long id, List<Long> userIds) {
        // 更新以前所有有权限的用户
        List<Long> currentUserIds = appUserPermissionRepository.listAll(id).stream()
                .map(AppUserPermissionE::getIamUserId).collect(Collectors.toList());
        // 待添加的用户
        List<Long> addUserIds = userIds.stream().filter(e -> !currentUserIds.contains(e)).collect(Collectors.toList());
        // 待删除的用户
        List<Long> deleteUserIds = currentUserIds.stream().filter(e -> !userIds.contains(e))
                .collect(Collectors.toList());
        // 更新gitlab权限
        Long gitlabProjectId = TypeUtil.objToLong(applicationRepository.query(id).getGitlabProjectE().getId());

        super.updateGitlabUserPermission(gitlabProjectId, addUserIds, deleteUserIds);
        appUserPermissionRepository.updateAppUserPermission(id, addUserIds, deleteUserIds);
        return true;
    }
}
