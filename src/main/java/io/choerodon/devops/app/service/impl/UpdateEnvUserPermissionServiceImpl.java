package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.eventhandler.payload.DevopsEnvUserPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created by n!Ck
 * Date: 2018//21
 * Time: 16:42
 * Description:
 */
@Service
public class UpdateEnvUserPermissionServiceImpl extends UpdateUserPermissionService {
    private final DevopsEnvironmentService devopsEnvironmentService;
    private final DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    private final UserAttrService userAttrService;
    private final DevopsProjectService devopsProjectService;
    private final GitlabServiceClientOperator gitlabServiceClientOperator;
    private final IamService iamService;

    @Autowired
    public UpdateEnvUserPermissionServiceImpl(DevopsEnvironmentService devopsEnvironmentService, DevopsEnvUserPermissionService devopsEnvUserPermissionService, UserAttrService userAttrService, DevopsProjectService devopsProjectService, GitlabServiceClientOperator gitlabServiceClientOperator, IamService iamService) {
        super(gitlabServiceClientOperator);
        this.devopsEnvironmentService = devopsEnvironmentService;
        this.devopsEnvUserPermissionService = devopsEnvUserPermissionService;
        this.userAttrService = userAttrService;
        this.devopsProjectService = devopsProjectService;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.iamService = iamService;
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

    /**
     * 更新环境库相应权限
     *
     * @param devopsEnvUserPayload 相应权限分配信息
     */
    public Boolean updateUserPermission(DevopsEnvUserPayload devopsEnvUserPayload) {
        List<Integer> allMemberGitlabIdsWithoutOwner;
        List<Integer> addGitlabUserIds;
        List<Integer> deleteGitlabUserIds;
        List<Integer> updateGitlabUserIds;

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsEnvUserPayload.getEnvId());
        Integer gitlabProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(devopsEnvUserPayload.getIamProjectId());
        Integer gitlabGroupId = devopsProjectDTO.getDevopsAppGroupId().intValue();

        // 如果之前对应的gitlab project同步失败时，不进行后续操作
        if (gitlabProjectId == null) {
            throw new CommonException("error.gitlab.project.sync.failed");
        }

        switch (devopsEnvUserPayload.getOption()) {
            // 原来跳过，现在不跳过，需要更新权限表，而且需要去掉原来gitlab中的权限
            case 1:
                updateGitlabUserIds = userAttrService.baseListByUserIds(devopsEnvUserPayload.getIamUserIds())
                        .stream().map(e -> TypeUtil.objToInteger(e.getGitlabUserId())).collect(Collectors.toList());
                // 获取项目下所有项目成员的gitlabUserIds，过滤掉项目所有者
                allMemberGitlabIdsWithoutOwner = getAllGitlabMemberWithoutOwner(devopsEnvUserPayload.getIamProjectId());

                addGitlabUserIds = new ArrayList<>(updateGitlabUserIds);
                addGitlabUserIds.removeAll(allMemberGitlabIdsWithoutOwner);

                deleteGitlabUserIds = new ArrayList<>(allMemberGitlabIdsWithoutOwner);
                deleteGitlabUserIds.removeAll(updateGitlabUserIds);

                super.updateGitlabUserPermission("env", gitlabGroupId, gitlabProjectId, addGitlabUserIds, deleteGitlabUserIds);
                return true;
            // 原来不跳过，现在跳过，需要删除权限表中的所有人，然后把项目下所有项目成员加入gitlab权限
            case 2:
                // 获取项目下所有项目成员的gitlabUserIds，过滤掉项目所有者
                allMemberGitlabIdsWithoutOwner = getAllGitlabMemberWithoutOwner(devopsEnvUserPayload.getIamProjectId());
                List<Integer> currentGitlabProjectMember = gitlabServiceClientOperator.listMemberByProject(gitlabProjectId).stream()
                        .map(MemberDTO::getUserId).collect(Collectors.toList());

                addGitlabUserIds = allMemberGitlabIdsWithoutOwner.stream()
                        .filter(e -> !currentGitlabProjectMember.contains(e))
                        .collect(Collectors.toList());

                super.updateGitlabUserPermission("env", gitlabGroupId, gitlabProjectId, addGitlabUserIds, new ArrayList<>());
                return true;
            // 原来不跳过，现在也不跳过，需要更新权限表
            case 3:
                super.updateGitlabUserPermission("env", gitlabGroupId, gitlabProjectId, devopsEnvUserPayload.getAddGitlabUserIds(), devopsEnvUserPayload.getDeleteGitlabUserIds());
                return true;
            default:
                return true;
        }
    }


    /**
     * 获取iam项目下所有的项目成员的gitlabUserId，过滤掉项目所有者
     */
    private List<Integer> getAllGitlabMemberWithoutOwner(Long projectId) {
        return userAttrService.baseListByUserIds(iamService.getAllMemberIdsWithoutOwner(projectId)).stream()
                .map(UserAttrDTO::getGitlabUserId)
                .map(TypeUtil::objToInteger).collect(Collectors.toList());
    }
}
