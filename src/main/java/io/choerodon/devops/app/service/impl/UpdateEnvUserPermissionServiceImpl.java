package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.eventhandler.payload.DevopsEnvUserPayload;
import io.choerodon.devops.app.service.DevopsEnvUserPermissionService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.TypeUtil;


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
    private final BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    public UpdateEnvUserPermissionServiceImpl(DevopsEnvironmentService devopsEnvironmentService, DevopsEnvUserPermissionService devopsEnvUserPermissionService, UserAttrService userAttrService, DevopsProjectService devopsProjectService, GitlabServiceClientOperator gitlabServiceClientOperator, BaseServiceClientOperator baseServiceClientOperator, AppServiceMapper appServiceMapper, DevopsEnvironmentMapper devopsEnvironmentMapper) {
        super(gitlabServiceClientOperator, userAttrService, appServiceMapper, devopsEnvironmentMapper,baseServiceClientOperator);
        this.devopsEnvironmentService = devopsEnvironmentService;
        this.devopsEnvUserPermissionService = devopsEnvUserPermissionService;
        this.userAttrService = userAttrService;
        this.devopsProjectService = devopsProjectService;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.baseServiceClientOperator = baseServiceClientOperator;
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
                        .map(MemberDTO::getId).collect(Collectors.toList());

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

    private List<Long> getAllGitlabMemberIsOrgRoot(List<Long> iamUserIds) {
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(iamUserIds);
        if (!CollectionUtils.isEmpty(iamUserDTOS)) {
            return iamUserDTOS.stream().filter(iamUserDTO -> baseServiceClientOperator.isOrganzationRoot(iamUserDTO.getId(), iamUserDTO.getOrganizationId()))
                    .map(IamUserDTO::getId).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    /**
     * 获取iam项目下所有的项目成员的gitlabUserId，过滤掉项目所有者
     */
    private List<Integer> getAllGitlabMemberWithoutOwner(Long projectId) {
        return userAttrService.baseListByUserIds(baseServiceClientOperator.getAllMemberIdsWithoutOwner(projectId)).stream()
                .map(UserAttrDTO::getGitlabUserId)
                .map(TypeUtil::objToInteger).collect(Collectors.toList());
    }
}
