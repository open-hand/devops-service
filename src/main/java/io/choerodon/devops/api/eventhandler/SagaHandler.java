package io.choerodon.devops.api.eventhandler;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import feign.FeignException;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.impl.DevopsGitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;
import io.choerodon.devops.api.dto.GitlabUserDTO;
import io.choerodon.devops.api.dto.GitlabUserRequestDTO;
import io.choerodon.devops.api.dto.RegisterOrganizationDTO;
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

    private final GitlabGroupService gitlabGroupService;
    private final HarborService harborService;
    private final OrganizationService organizationService;
    private final GitlabGroupMemberService gitlabGroupMemberService;
    private final GitlabUserService gitlabUserService;


    @Autowired
    public SagaHandler(ProjectService projectService, GitlabGroupService gitlabGroupService,
                       HarborService harborService, OrganizationService organizationService,
                       GitlabGroupMemberService gitlabGroupMemberService, GitlabUserService gitlabUserService) {
        this.gitlabGroupService = gitlabGroupService;
        this.harborService = harborService;
        this.organizationService = organizationService;
        this.gitlabGroupMemberService = gitlabGroupMemberService;
        this.gitlabUserService = gitlabUserService;
    }

    private void loggerInfo(Object o) {
        LOGGER.info("data: {}", o);
    }


    @SagaTask(code = "devopsCreateGitLabGroup",
            description = "devops 创建 GitLab Group",
            sagaCode = "iam-create-project",
            maxRetryCount = 3,
            seq = 1)
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
            maxRetryCount = 3,
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
            maxRetryCount = 3,
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
            maxRetryCount = 3,
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
            maxRetryCount = 3,
            seq = 1)
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
            maxRetryCount = 3,
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
            sagaCode = "iam-update-memberRole", maxRetryCount = 3,
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
            sagaCode = "iam-delete-memberRole", maxRetryCount = 3,
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
            sagaCode = "iam-create-user", maxRetryCount = 1,
            seq = 1)
    public List<GitlabUserDTO> handleCreateUserEvent(String payload) {
        List<GitlabUserDTO> gitlabUserDTO = gson.fromJson(payload, new TypeToken<List<GitlabUserDTO>>() {
        }.getType());
        loggerInfo(gitlabUserDTO);
        gitlabUserDTO.forEach(t -> {
            GitlabUserRequestDTO gitlabUserReqDTO = new GitlabUserRequestDTO();
            gitlabUserReqDTO.setProvider("oauth2_generic");
            gitlabUserReqDTO.setExternUid(t.getId());
            gitlabUserReqDTO.setSkipConfirmation(true);
            gitlabUserReqDTO.setUsername(t.getUsername());
            gitlabUserReqDTO.setEmail(t.getEmail());
            gitlabUserReqDTO.setName(t.getName());
            gitlabUserReqDTO.setCanCreateGroup(true);
            gitlabUserReqDTO.setProjectsLimit(100);
            try{
                gitlabUserService.createGitlabUser(gitlabUserReqDTO);
            } catch (FeignException e) {
                LOGGER.info("the sync failed username is:" + gitlabUserReqDTO.getUsername());
                throw new CommonException(e);
            }
        });
        return gitlabUserDTO;
    }

    /**
     * 用户更新事件
     */
    @SagaTask(code = "devopsUpdateUser", description = "用户更新事件",
            sagaCode = "iam-update-user", maxRetryCount = 3,
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
            sagaCode = "iam-enable-user", maxRetryCount = 3,
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
            sagaCode = "iam-disable-user", maxRetryCount = 3,
            seq = 1)
    public String handleDisEnabledUserEvent(String payload) {
        GitlabUserDTO gitlabUserDTO = gson.fromJson(payload, GitlabUserDTO.class);
        loggerInfo(gitlabUserDTO);

        gitlabUserService.disEnabledGitlabUser(TypeUtil.objToInteger(gitlabUserDTO.getId()));
        return payload;
    }

    /**
     * 注册组织事件
     */
    @SagaTask(code = "devopsOrgRegister", description = "注册组织",
            sagaCode = "org-register", maxRetryCount = 3,
            seq = 1)
    public String registerOrganization(String payload) {
        RegisterOrganizationDTO registerOrganizationDTO = gson.fromJson(payload, RegisterOrganizationDTO.class);
        loggerInfo(registerOrganizationDTO);

        organizationService.registerOrganization(registerOrganizationDTO);
        return payload;
    }
}
