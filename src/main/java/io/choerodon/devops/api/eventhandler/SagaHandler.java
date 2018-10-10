package io.choerodon.devops.api.eventhandler;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;
import io.choerodon.devops.api.dto.GitlabUserDTO;
import io.choerodon.devops.api.dto.GitlabUserRequestDTO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.event.GitlabGroupPayload;
import io.choerodon.devops.domain.application.event.HarborPayload;
import io.choerodon.devops.domain.application.event.OrganizationEventPayload;
import io.choerodon.devops.domain.application.event.ProjectEvent;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Creator: Runge
 * Date: 2018/7/27
 * Time: 10:06
 * Description: External saga msg
 */
@Component
public class SagaHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaHandler.class);
    private final Gson gson = new Gson();

    @Autowired
    private ProjectService projectService;
    @Autowired
    private GitlabGroupService gitlabGroupService;
    @Autowired
    private HarborService harborService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private GitlabUserService gitlabUserService;

    private void loggerInfo(Object o) {
        LOGGER.info("data: {}", o);
    }

    /**
     * 创建项目saga
     */
    @SagaTask(code = "devopsCreateProject",
            description = "devops创建项目",
            sagaCode = "iam-create-project",
            maxRetryCount = 0,
            seq = 1)
    public String handleProjectCreateEvent(String msg) {
        ProjectEvent projectEvent = gson.fromJson(msg, ProjectEvent.class);
        loggerInfo(projectEvent);
        projectService.createProject(projectEvent);
        return msg;
    }

    @SagaTask(code = "devopsCreateGitLabGroup",
            description = "devops 创建 GitLab Group",
            sagaCode = "iam-create-project",
            maxRetryCount = 0,
            seq = 2)
    public String handleGitlabGroupEvent(String msg) {
        ProjectEvent projectEvent = gson.fromJson(msg, ProjectEvent.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectEvent, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.createGroup(gitlabGroupPayload, "");
        return msg;
    }

    /**
     * 创建组事件
     */
    @SagaTask(code = "devopsCreateGitOpsGroup",
            description = "devops 创建 GitOps Group",
            sagaCode = "iam-create-project",
            maxRetryCount = 0,
            seq = 2)
    public String handleGitOpsGroupEvent(String msg) {
        ProjectEvent projectEvent = gson.fromJson(msg, ProjectEvent.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectEvent, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.createGroup(gitlabGroupPayload, "-gitops");
        return msg;
    }


    @SagaTask(code = "devopsUpdateGitLabGroup",
            description = "devops  更新 GitLab Group",
            sagaCode = "iam-update-project",
            maxRetryCount = 0,
            seq = 1)
    public String handleUpdateGitlabGroupEvent(String msg) {
        ProjectEvent projectEvent = gson.fromJson(msg, ProjectEvent.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectEvent, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.updateGroup(gitlabGroupPayload, "");
        return msg;
    }

    @SagaTask(code = "devopsUpdateGitOpsGroup",
            description = "devops  更新 GitOps Group",
            sagaCode = "iam-update-project",
            maxRetryCount = 0,
            seq = 1)
    public String handleUpdateGitOpsGroupEvent(String msg) {
        ProjectEvent projectEvent = gson.fromJson(msg, ProjectEvent.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectEvent, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.updateGroup(gitlabGroupPayload, "-gitops");
        return msg;
    }

    /**
     * 创建harbor项目事件
     */
    @SagaTask(code = "devopsCreateHarbor",
            description = "devops 创建 Harbor",
            sagaCode = "iam-create-project",
            maxRetryCount = 0,
            seq = 2)
    public String handleHarborEvent(String msg) {
        ProjectEvent projectEvent = gson.fromJson(msg, ProjectEvent.class);
        HarborPayload harborPayload = new HarborPayload(
                projectEvent.getProjectId(),
                projectEvent.getOrganizationCode() + "-" + projectEvent.getProjectCode()
        );
        loggerInfo(harborPayload);
        harborService.createHarbor(harborPayload);
        return msg;
    }

    /**
     * 创建组织事件
     */
    @SagaTask(code = "devopsCreateOrganization",
            description = "创建组织事件",
            sagaCode = "org-create-organization",
            maxRetryCount = 0,
            seq = 1)
    public String handleOrganizationCreateEvent(String payload) {
        OrganizationEventPayload organizationEventPayload = gson.fromJson(payload, OrganizationEventPayload.class);
        loggerInfo(organizationEventPayload);
        organizationService.create(organizationEventPayload);
        return payload;
    }


    /**
     * 角色同步事件
     */
    @SagaTask(code = "devopsUpdateMemberRole", description = "角色同步事件",
            sagaCode = "iam-update-memberRole", maxRetryCount = 0,
            seq = 1)
    public List<GitlabGroupMemberDTO> handleGitlabGroupMemberEvent(String payload) {
        List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList = gson.fromJson(payload,
                new TypeToken<List<GitlabGroupMemberDTO>>() {
                }.getType());
        loggerInfo(gitlabGroupMemberDTOList);
        gitlabGroupMemberService.createGitlabGroupMemberRole(gitlabGroupMemberDTOList);
        return gitlabGroupMemberDTOList;
    }

    /**
     * 删除角色同步事件
     */
    @SagaTask(code = "devopsDeleteMemberRole", description = "删除角色同步事件",
            sagaCode = "iam-delete-memberRole", maxRetryCount = 0,
            seq = 1)
    public List<GitlabGroupMemberDTO> handleDeleteMemberRoleEvent(String payload) {
        List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList = gson.fromJson(payload,
                new TypeToken<List<GitlabGroupMemberDTO>>() {
                }.getType());
        loggerInfo(gitlabGroupMemberDTOList);
        gitlabGroupMemberService.deleteGitlabGroupMemberRole(gitlabGroupMemberDTOList);
        return gitlabGroupMemberDTOList;
    }

    /**
     * 用户创建事件
     */
    @SagaTask(code = "devopsCreateUser", description = "用户创建事件",
            sagaCode = "iam-create-user", maxRetryCount = 0,
            seq = 1)
    public List<GitlabUserDTO> handleCreateUserEvent(String payload) {
        List<GitlabUserDTO> gitlabUserDTO = gson.fromJson(payload, new TypeToken<List<GitlabUserDTO>>() {
        }.getType());
        loggerInfo(gitlabUserDTO);
        gitlabUserDTO.stream().forEach(t -> {
            GitlabUserRequestDTO gitlabUserReqDTO = new GitlabUserRequestDTO();
            gitlabUserReqDTO.setProvider("oauth2_generic");
            gitlabUserReqDTO.setExternUid(t.getId());
            gitlabUserReqDTO.setSkipConfirmation(true);
            gitlabUserReqDTO.setUsername(t.getUsername());
            gitlabUserReqDTO.setEmail(t.getEmail());
            gitlabUserReqDTO.setName(t.getName());
            gitlabUserReqDTO.setCanCreateGroup(true);
            gitlabUserReqDTO.setProjectsLimit(100);

            gitlabUserService.createGitlabUser(gitlabUserReqDTO);
        });
        return gitlabUserDTO;
    }

    /**
     * 用户更新事件
     */
    @SagaTask(code = "devopsUpdateUser", description = "用户更新事件",
            sagaCode = "iam-update-user", maxRetryCount = 0,
            seq = 1)
    public String handleUpdateUserEvent(String payload) {
        GitlabUserDTO gitlabUserDTO = gson.fromJson(payload, GitlabUserDTO.class);
        loggerInfo(gitlabUserDTO);

        GitlabUserRequestDTO gitlabUserReqDTO = new GitlabUserRequestDTO();
        gitlabUserReqDTO.setProvider("oauth2_generic");
        gitlabUserReqDTO.setExternUid(gitlabUserDTO.getId());
        gitlabUserReqDTO.setSkipConfirmation(true);
        gitlabUserReqDTO.setUsername(gitlabUserDTO.getUsername());
        gitlabUserReqDTO.setEmail(gitlabUserDTO.getEmail());
        gitlabUserReqDTO.setName(gitlabUserDTO.getName());
        gitlabUserReqDTO.setCanCreateGroup(true);
        gitlabUserReqDTO.setProjectsLimit(100);

        gitlabUserService.updateGitlabUser(gitlabUserReqDTO);
        return payload;
    }

    /**
     * 用户启用事件
     */
    @SagaTask(code = "devopsEnableUser", description = "用户启用事件",
            sagaCode = "iam-enable-user", maxRetryCount = 0,
            seq = 1)
    public String handleIsEnabledUserEvent(String payload) {
        GitlabUserDTO gitlabUserDTO = gson.fromJson(payload, GitlabUserDTO.class);
        loggerInfo(gitlabUserDTO);

        gitlabUserService.isEnabledGitlabUser(TypeUtil.objToInteger(gitlabUserDTO.getId()));
        return payload;
    }

    /**
     * 用户禁用事件
     */
    @SagaTask(code = "devopsDisableUser", description = "用户禁用事件",
            sagaCode = "iam-disable-user", maxRetryCount = 0,
            seq = 1)
    public String handleDisEnabledUserEvent(String payload) {
        GitlabUserDTO gitlabUserDTO = gson.fromJson(payload, GitlabUserDTO.class);
        loggerInfo(gitlabUserDTO);

        gitlabUserService.disEnabledGitlabUser(TypeUtil.objToInteger(gitlabUserDTO.getId()));
        return payload;
    }

}
