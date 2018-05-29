package io.choerodon.devops.api.eventhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.event.EventPayload;
import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;
import io.choerodon.devops.api.dto.GitlabProjectEventDTO;
import io.choerodon.devops.api.dto.GitlabUserDTO;
import io.choerodon.devops.api.dto.GitlabUserRequestDTO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.event.*;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.event.consumer.annotation.EventListener;

/**
 * Created by younger on 2018/4/10.
 */
@Component
public class DevopsEventHandler {

    private static final String DEVOPS_SERVICE = "devops-service";
    private static final String IAM_SERVICE = "iam-service";
    private static final String TEMPLATE = "template";
    private static final String APPLICATION = "application";

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsEventHandler.class);

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationTemplateService applicationTemplateService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private GitlabGroupService gitlabGroupService;
    @Autowired
    private HarborService harborService;
    @Autowired
    private GitlabUserService gitlabUserService;
    @Autowired
    private GitFlowService gitFlowService;

    private void loggerInfo(Object o) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("data: {}", o);
        }
    }

    /**
     * 创建项目事件
     */
    @EventListener(topic = IAM_SERVICE, businessType = "createProject")
    public void handleProjectCreateEvent(EventPayload<ProjectEvent> payload) {
        ProjectEvent projectEvent = payload.getData();
        loggerInfo(projectEvent);
        projectService.createProject(projectEvent);
    }

    /**
     * 创建应用事件
     */
    @EventListener(topic = DEVOPS_SERVICE, businessType = "OperationGitlabProject")
    public void handleGitlabProjectEvent(EventPayload<GitlabProjectEventDTO> payload) {
        GitlabProjectEventDTO gitlabProjectEventDTO = payload.getData();
        loggerInfo(gitlabProjectEventDTO);
        if (gitlabProjectEventDTO.getType().equals(TEMPLATE)) {
            applicationTemplateService.operationApplicationTemplate(gitlabProjectEventDTO);
        }
        if (gitlabProjectEventDTO.getType().equals(APPLICATION)) {
            applicationService.operationApplication(gitlabProjectEventDTO);
        }
    }

    /**
     * 角色同步事件
     */
    @EventListener(topic = IAM_SERVICE, businessType = "updateMemberRole")
    public void handleGitlabGroupMemberEvent(EventPayload<List<GitlabGroupMemberDTO>> payload) {
        List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList = payload.getData();
        loggerInfo(gitlabGroupMemberDTOList);
        gitlabGroupMemberService.createGitlabGroupMemberRole(gitlabGroupMemberDTOList);
    }

    /**
     * 角色同步事件
     */
    @EventListener(topic = IAM_SERVICE, businessType = "deleteMemberRole")
    public void handledeleteMemberRoleEvent(EventPayload<List<GitlabGroupMemberDTO>> payload) {
        List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList = payload.getData();
        loggerInfo(gitlabGroupMemberDTOList);
        gitlabGroupMemberService.deleteGitlabGroupMemberRole(gitlabGroupMemberDTOList);
    }

    /**
     * 创建组事件
     */
    @EventListener(topic = DEVOPS_SERVICE, businessType = "GitlabGroup")
    public void handleGitlabGroupEvent(EventPayload<GitlabGroupPayload> payload) {
        GitlabGroupPayload gitlabGroupPayload = payload.getData();
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.createGroup(gitlabGroupPayload);
    }

    /**
     * 创建harbor项目事件
     */
    @EventListener(topic = DEVOPS_SERVICE, businessType = "Harbor")
    public void handleHarborEvent(EventPayload<HarborPayload> payload) {
        HarborPayload harborPayload = payload.getData();
        loggerInfo(harborPayload);
        harborService.createHarbor(harborPayload);
    }

    /**
     * 用户创建事件
     */
    @EventListener(topic = IAM_SERVICE, businessType = "createUser")
    public void handleCreateUserEvent(EventPayload<GitlabUserDTO> payload) {
        GitlabUserDTO gitlabUserDTO = payload.getData();
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

        gitlabUserService.createGitlabUser(gitlabUserReqDTO);
    }

    /**
     * 用户更新事件
     */
    @EventListener(topic = IAM_SERVICE, businessType = "updateUser")
    public void handleUpdateUserEvent(EventPayload<GitlabUserDTO> payload) {
        GitlabUserDTO gitlabUserDTO = payload.getData();
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
    }

    /**
     * 用户启用事件
     */
    @EventListener(topic = IAM_SERVICE, businessType = "enableUser")
    public void handleIsEnabledUserEvent(EventPayload<GitlabUserDTO> payload) {
        GitlabUserDTO gitlabUserDTO = payload.getData();
        loggerInfo(gitlabUserDTO);

        gitlabUserService.isEnabledGitlabUser(TypeUtil.objToInteger(gitlabUserDTO.getId()));
    }

    /**
     * 用户禁用事件
     */
    @EventListener(topic = IAM_SERVICE, businessType = "disableUser")
    public void handleDisEnabledUserEvent(EventPayload<GitlabUserDTO> payload) {
        GitlabUserDTO gitlabUserDTO = payload.getData();
        loggerInfo(gitlabUserDTO);

        gitlabUserService.disEnabledGitlabUser(TypeUtil.objToInteger(gitlabUserDTO.getId()));
    }

    /**
     * 分支事件
     */
    @EventListener(topic = DEVOPS_SERVICE, businessType = "gitFlowStart")
    public void handleGitFlowStartEvent(EventPayload<GitFlowStartPayload> payload) {
        GitFlowStartPayload gitFlowStartPayload = payload.getData();
        loggerInfo(gitFlowStartPayload);
        gitFlowService.gitFlowStart(gitFlowStartPayload);
    }

    /**
     * GitFlow 结束事件消费
     *
     * @param payload GitFlow 结束事件消息
     */
    @EventListener(topic = DEVOPS_SERVICE, businessType = "gitFlowFinish")
    public void handleGitFlowFinishEvent(EventPayload<GitFlowFinishPayload> payload) {
        GitFlowFinishPayload gitFlowFinishPayload = payload.getData();
        loggerInfo(gitFlowFinishPayload);
        gitFlowService.gitFlowFinish(gitFlowFinishPayload);
    }
}
