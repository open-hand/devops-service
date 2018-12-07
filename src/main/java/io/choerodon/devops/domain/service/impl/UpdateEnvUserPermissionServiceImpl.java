package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvUserPermissionE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.repository.DevopsEnvUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.service.UpdateUserPermissionService;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 16:42
 * Description:
 */
public class UpdateEnvUserPermissionServiceImpl extends UpdateUserPermissionService {
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    private UserAttrRepository userAttrRepository;

    public UpdateEnvUserPermissionServiceImpl() {
        this.devopsEnviromentRepository = ApplicationContextHelper.getSpringFactory()
                .getBean(DevopsEnvironmentRepository.class);
        this.devopsEnvUserPermissionRepository = ApplicationContextHelper.getSpringFactory()
                .getBean(DevopsEnvUserPermissionRepository.class);
        this.userAttrRepository = ApplicationContextHelper.getSpringFactory().getBean(UserAttrRepository.class);
    }

    @Override
    public Boolean updateUserPermission(Long projectId, Long id, List<Long> userIds, Integer option) {
        // 更新以前所有有权限的用户
        List<Long> currentUserIds = devopsEnvUserPermissionRepository.listAll(id).stream()
                .map(DevopsEnvUserPermissionE::getIamUserId).collect(Collectors.toList());
        // 待添加的用户
        List<Long> addIamUserIds = userIds.stream().filter(e -> !currentUserIds.contains(e))
                .collect(Collectors.toList());
        List<Integer> addgitlabUserIds = userAttrRepository.listByUserIds(addIamUserIds).stream()
                .map(UserAttrE::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());
        // 待删除的用户
        List<Long> deleteIamUserIds = currentUserIds.stream().filter(e -> !userIds.contains(e))
                .collect(Collectors.toList());
        List<Integer> deleteGitlabUserIds = userAttrRepository.listByUserIds(deleteIamUserIds).stream()
                .map(UserAttrE::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());
        // 更新gitlab权限
        Integer gitlabProjectId = TypeUtil
                .objToInteger(devopsEnviromentRepository.queryById(id).getGitlabEnvProjectId());

        super.updateGitlabUserPermission(gitlabProjectId, addgitlabUserIds, deleteGitlabUserIds);
        devopsEnvUserPermissionRepository.updateEnvUserPermission(id, addIamUserIds, deleteIamUserIds);
        return true;
    }
}
