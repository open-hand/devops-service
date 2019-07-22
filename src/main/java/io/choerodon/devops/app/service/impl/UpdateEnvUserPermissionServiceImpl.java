package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.app.service.DevopsEnvUserPermissionService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by n!Ck
 * Date: 2018//21
 * Time: 16:42
 * Description:
 */
public class UpdateEnvUserPermissionServiceImpl extends UpdateUserPermissionService {
    private DevopsEnvironmentService devopsEnvironmentService;
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    private UserAttrService userAttrService;
    private DevopsProjectService devopsProjectService;


    public UpdateEnvUserPermissionServiceImpl() {
        this.devopsEnvironmentService = ApplicationContextHelper.getSpringFactory()
                .getBean(DevopsEnvironmentService.class);
        this.devopsEnvUserPermissionService = ApplicationContextHelper.getSpringFactory()
                .getBean(DevopsEnvUserPermissionService.class);
        this.userAttrService = ApplicationContextHelper.getSpringFactory().getBean(UserAttrService.class);
        this.devopsProjectService = ApplicationContextHelper.getSpringFactory()
                .getBean(DevopsProjectService.class);
    }

    @Override
    public Boolean updateUserPermission(Long projectId, Long id, List<Long> userIds, Integer option) {
        // 更新以前所有有权限的用户
        List<Long> currentUserIds = devopsEnvUserPermissionService.baseListAll(id).stream()
                .map(DevopsEnvUserPermissionDTO::getIamUserId).collect(Collectors.toList());
        // 待添加的用户
        List<Long> addIamUserIds = userIds.stream().filter(e -> !currentUserIds.contains(e))
                .collect(Collectors.toList());
        List<Integer> addgitlabUserIds = userAttrService.baseListByUserIds(addIamUserIds).stream()
                .map(UserAttrDTO::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());
        // 待删除的用户
        List<Long> deleteIamUserIds = currentUserIds.stream().filter(e -> !userIds.contains(e))
                .collect(Collectors.toList());
        List<Integer> deleteGitlabUserIds = userAttrService.baseListByUserIds(deleteIamUserIds).stream()
                .map(UserAttrDTO::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());
        // 更新gitlab权限

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(id);

        Integer gitlabProjectId = devopsEnvironmentDTO.getGitlabEnvProjectId().intValue();
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(devopsEnvironmentDTO.getProjectId());
        Integer gitlabGroupId = devopsProjectDTO.getDevopsEnvGroupId().intValue();

        super.updateGitlabUserPermission("env", gitlabGroupId, gitlabProjectId, addgitlabUserIds, deleteGitlabUserIds);
        devopsEnvUserPermissionService.baseUpdate(id, addIamUserIds, deleteIamUserIds);
        return true;
    }
}
