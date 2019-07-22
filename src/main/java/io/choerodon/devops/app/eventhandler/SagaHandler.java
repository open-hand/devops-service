package io.choerodon.devops.app.eventhandler;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.devops.api.vo.GitlabGroupMemberDTO;
import io.choerodon.devops.api.vo.GitlabUserRequestDTO;
import io.choerodon.devops.api.vo.GitlabUserVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.ApplicationDTO;
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
    @Autowired
    private ApplicationService applicationService;


    private void loggerInfo(Object o) {
        LOGGER.info("data: {}", o);
    }


    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_GITLAB_GROUP,
            description = "devops 创建 GitLab Group",
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
            description = "devops  更新 GitLab Group",
            sagaCode = SagaTopicCodeConstants.IAM_UPDATE_PROJECT,
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
     * Iam创建应用事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_CREATE_APPLICATION,
            description = "Iam创建应用事件",
            sagaCode = SagaTopicCodeConstants.IAM_CREATE_APPLICATION,
            maxRetryCount = 3,
            seq = 1)
    public String handleIamCreateApplication(String payload) {
        IamAppPayLoad iamAppPayLoad = gson.fromJson(payload, IamAppPayLoad.class);
        loggerInfo(iamAppPayLoad);
        applicationService.createIamApplication(iamAppPayLoad);
        return payload;
    }


    /**
     * GitOps 事件处理
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_DELETE_APPLICATION,
            description = "iam delete application ",
            sagaCode = SagaTopicCodeConstants.IAM_DELETE_APPLICATION,
            maxRetryCount = 3,
            seq = 1)
    public String deleteApp(String payload) {
        IamAppPayLoad iamAppPayLoad = gson.fromJson(payload, IamAppPayLoad.class);
        loggerInfo(iamAppPayLoad);
        applicationService.deleteIamApplication(iamAppPayLoad);
        return payload;
    }


    /**
     * Iam更新应用事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_UPDATE_APPLICATION,
            description = "Iam更新应用事件",
            sagaCode = SagaTopicCodeConstants.IAM_UPDATE_APPLICATION,
            maxRetryCount = 3,
            seq = 1)
    public String handleIamUpdateApplication(String payload) {
        IamAppPayLoad iamAppPayLoad = gson.fromJson(payload, IamAppPayLoad.class);
        loggerInfo(iamAppPayLoad);
        applicationService.updateIamApplication(iamAppPayLoad);
        return payload;
    }

    /**
     * Iam启用应用事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_ENABLE_APPLICATION,
            description = "Iam启用应用事件",
            sagaCode = SagaTopicCodeConstants.IAM_ENABLE_APPLICATION,
            maxRetryCount = 3,
            seq = 1)
    public String handleIamEnableApplication(String payload) {
        IamAppPayLoad iamAppPayLoad = gson.fromJson(payload, IamAppPayLoad.class);
        loggerInfo(iamAppPayLoad);
        ApplicationDTO applicationDTO = applicationService.baseQueryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        applicationService.updateActive(applicationDTO.getId(), true);
        return payload;
    }

    /**
     * Iam停用应用事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_DISABLE_APPLICATION,
            description = "Iam停用应用事件",
            sagaCode = SagaTopicCodeConstants.IAM_DISABLE_APPLICATION,
            maxRetryCount = 3,
            seq = 1)
    public String handleIamDisableApplication(String payload) {
        IamAppPayLoad iamAppPayLoad = gson.fromJson(payload, IamAppPayLoad.class);
        loggerInfo(iamAppPayLoad);
        ApplicationDTO applicationDTO = applicationService.baseQueryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        applicationService.updateActive(applicationDTO.getId(), false);
        return payload;
    }

    /**
     * 角色同步事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_UPDATE_MEMBER_ROLE,
            description = "角色同步事件",
            sagaCode = SagaTopicCodeConstants.IAM_UPDATE_MEMBER_ROLE,
            maxRetryCount = 3, seq = 1)
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
    @SagaTask(code = SagaTaskCodeConstants.IAM_DELETE_MEMBER_ROLE,
            description = "删除角色同步事件",
            sagaCode = SagaTopicCodeConstants.IAM_DELETE_MEMBER_ROLE,
            maxRetryCount = 3, seq = 1)
    public List<GitlabGroupMemberDTO> handleDeleteMemberRoleEvent(String payload) {
        List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList = gson.fromJson(payload,
                new TypeToken<List<GitlabGroupMemberDTO>>() {
                }.getType());
        loggerInfo(gitlabGroupMemberDTOList);
        if (gitlabGroupMemberDTOList.size() > 0) {
            List<Long> envIds = devopsEnviromentRepository.queryByprojectAndActive(gitlabGroupMemberDTOList.get(0).getResourceId(), null)
                    .stream().map(DevopsEnvironmentE::getId).collect(Collectors.toList());
            if (envIds != null && !envIds.isEmpty()) {
                envIds.forEach(envId -> gitlabGroupMemberDTOList.forEach(gitlabGroupMemberDTO -> devopsEnvUserPermissionRepository.delete(envId, gitlabGroupMemberDTO.getUserId())));
            }
        }
        gitlabGroupMemberService.deleteGitlabGroupMemberRole(gitlabGroupMemberDTOList);
        return gitlabGroupMemberDTOList;
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
            GitlabUserRequestDTO gitlabUserReqDTO = new GitlabUserRequestDTO();
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

        GitlabUserRequestDTO gitlabUserReqDTO = new GitlabUserRequestDTO();
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
