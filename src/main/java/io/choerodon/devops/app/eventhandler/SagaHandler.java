package io.choerodon.devops.app.eventhandler;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.devops.api.vo.GitlabGroupMemberVO;
import io.choerodon.devops.api.vo.GitlabUserRequestVO;
import io.choerodon.devops.api.vo.GitlabUserVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


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


    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_GITLAB_GROUP,
            description = "devops创建应用Group",
            sagaCode = SagaTopicCodeConstants.IAM_CREATE_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String handleGitlabGroupEvent(String msg) {
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectPayload, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.createGroup(gitlabGroupPayload, "");
        return msg;
    }

    /**
     * 创建组事件
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_GITOPS_GROUP,
            description = "devops 创建 GitOps Group",
            sagaCode = SagaTopicCodeConstants.IAM_CREATE_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String handleGitOpsGroupEvent(String msg) {
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectPayload, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.createGroup(gitlabGroupPayload, "-gitops");
        return msg;
    }


    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_UPDATE_GITLAB_GROUP,
            description = "devops  更新 应用 Group",
            sagaCode = SagaTopicCodeConstants.BASE_UPDATE_APPLICATION,
            maxRetryCount = 3,
            seq = 1)
    public String handleUpdateGitlabGroupEvent(String msg) {
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectPayload, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.updateGroup(gitlabGroupPayload, "");
        return msg;
    }

    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_UPDATE_GITOPS_GROUP,
            description = "devops  更新 GitOps Group",
            sagaCode = SagaTopicCodeConstants.IAM_UPDATE_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String handleUpdateGitOpsGroupEvent(String msg) {
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectPayload, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.updateGroup(gitlabGroupPayload, "-gitops");
        return msg;
    }

    /**
     * 创建harbor项目事件
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_HARBOR,
            description = "devops 创建 Harbor",
            sagaCode = SagaTopicCodeConstants.IAM_CREATE_PROJECT,
            maxRetryCount = 3,
            seq = 5)
    public String handleHarborEvent(String msg) {
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        HarborPayload harborPayload = new HarborPayload(
                projectPayload.getProjectId(),
                projectPayload.getOrganizationCode() + "-" + projectPayload.getProjectCode()
        );
        loggerInfo(harborPayload);
        harborService.createHarbor(harborPayload);
        return msg;
    }

    /**
     * 创建组织事件
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_ORGANIZATION,
            description = "创建组织事件",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_ORGANIZATION,
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
    @SagaTask(code = SagaTaskCodeConstants.IAM_UPDATE_MEMBER_ROLE,
            description = "角色同步事件",
            sagaCode = SagaTopicCodeConstants.IAM_UPDATE_MEMBER_ROLE,
            maxRetryCount = 3, seq = 1)
    public List<GitlabGroupMemberVO> handleGitlabGroupMemberEvent(String payload) {
        List<GitlabGroupMemberVO> gitlabGroupMemberVOList = gson.fromJson(payload,
                new TypeToken<List<GitlabGroupMemberVO>>() {
                }.getType());
        loggerInfo(gitlabGroupMemberVOList);
        gitlabGroupMemberService.createGitlabGroupMemberRole(gitlabGroupMemberVOList);
        return gitlabGroupMemberVOList;
    }

    /**
     * 删除角色同步事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_DELETE_MEMBER_ROLE,
            description = "删除角色同步事件",
            sagaCode = SagaTopicCodeConstants.IAM_DELETE_MEMBER_ROLE,
            maxRetryCount = 3, seq = 1)
    public List<GitlabGroupMemberVO> handleDeleteMemberRoleEvent(String payload) {
        List<GitlabGroupMemberVO> gitlabGroupMemberVOList = gson.fromJson(payload,
                new TypeToken<List<GitlabGroupMemberVO>>() {
                }.getType());
        loggerInfo(gitlabGroupMemberVOList);
        gitlabGroupMemberService.deleteGitlabGroupMemberRole(gitlabGroupMemberVOList);
        return gitlabGroupMemberVOList;
    }

    /**
     * 用户创建事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_CREATE_USER,
            description = "用户创建事件",
            sagaCode = SagaTopicCodeConstants.IAM_CREATE_USER,
            maxRetryCount = 5, seq = 1)
    public List<GitlabUserVO> handleCreateUserEvent(String payload) {
        List<GitlabUserVO> gitlabUserDTO = gson.fromJson(payload, new TypeToken<List<GitlabUserVO>>() {
        }.getType());
        loggerInfo(gitlabUserDTO);
        gitlabUserDTO.forEach(t -> {
            GitlabUserRequestVO gitlabUserReqDTO = new GitlabUserRequestVO();
            gitlabUserReqDTO.setProvider("oauth2_generic");
            gitlabUserReqDTO.setExternUid(t.getId());
            gitlabUserReqDTO.setSkipConfirmation(true);
            gitlabUserReqDTO.setUsername(t.getUsername());
            gitlabUserReqDTO.setEmail(t.getEmail());
            gitlabUserReqDTO.setName(t.getName());
            if (t.getName() == null) {
                gitlabUserReqDTO.setName(t.getUsername());
            }
            gitlabUserReqDTO.setCanCreateGroup(true);
            gitlabUserReqDTO.setProjectsLimit(100);

            gitlabUserService.createGitlabUser(gitlabUserReqDTO);
        });
        return gitlabUserDTO;
    }

    /**
     * 用户更新事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_UPDATE_USER,
            description = "用户更新事件",
            sagaCode = SagaTopicCodeConstants.IAM_UPDATE_USER,
            maxRetryCount = 3, seq = 1)
    public String handleUpdateUserEvent(String payload) {
        GitlabUserVO gitlabUserVO = gson.fromJson(payload, GitlabUserVO.class);
        loggerInfo(gitlabUserVO);

        GitlabUserRequestVO gitlabUserReqDTO = new GitlabUserRequestVO();
        gitlabUserReqDTO.setProvider("oauth2_generic");
        gitlabUserReqDTO.setExternUid(gitlabUserVO.getId());
        gitlabUserReqDTO.setSkipConfirmation(true);
        gitlabUserReqDTO.setUsername(gitlabUserVO.getUsername());
        gitlabUserReqDTO.setEmail(gitlabUserVO.getEmail());
        gitlabUserReqDTO.setName(gitlabUserVO.getName());
        gitlabUserReqDTO.setCanCreateGroup(true);
        gitlabUserReqDTO.setProjectsLimit(100);

        gitlabUserService.updateGitlabUser(gitlabUserReqDTO);
        return payload;
    }

    /**
     * 用户启用事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_ENABLE_USER,
            description = "用户启用事件",
            sagaCode = SagaTopicCodeConstants.IAM_ENABLE_USER,
            maxRetryCount = 3, seq = 1)
    public String handleIsEnabledUserEvent(String payload) {
        GitlabUserVO gitlabUserVO = gson.fromJson(payload, GitlabUserVO.class);
        loggerInfo(gitlabUserVO);

        gitlabUserService.isEnabledGitlabUser(TypeUtil.objToInteger(gitlabUserVO.getId()));
        return payload;
    }

    /**
     * 用户禁用事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_DISABLE_USER,
            description = "用户禁用事件",
            sagaCode = SagaTopicCodeConstants.IAM_DISABLE_USER,
            maxRetryCount = 3, seq = 1)
    public String handleDisEnabledUserEvent(String payload) {
        GitlabUserVO gitlabUserVO = gson.fromJson(payload, GitlabUserVO.class);
        loggerInfo(gitlabUserVO);

        gitlabUserService.disEnabledGitlabUser(TypeUtil.objToInteger(gitlabUserVO.getId()));
        return payload;
    }

}
