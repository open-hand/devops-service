package io.choerodon.devops.app.eventhandler;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.devops.api.vo.GitlabGroupMemberVO;
import io.choerodon.devops.api.vo.GitlabUserRequestVO;
import io.choerodon.devops.api.vo.GitlabUserVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.util.TypeUtil;


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
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private GitlabUserService gitlabUserService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private OrgAppMarketService orgAppMarketService;


    private void loggerInfo(Object o) {
        LOGGER.info("data: {}", JSONObject.toJSONString(o));
    }


    /**
     * 消费创建应用事件，为应用创建组
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_GITLAB_GROUP,
            description = "消费创建应用事件",
            sagaCode = SagaTopicCodeConstants.BASE_CREATE_APPLICATION,
            maxRetryCount = 3,
            seq = 1)
    public String handleApplicationCreation(String msg) {
        ApplicationEventPayload applicationEventPayload = gson.fromJson(msg, ApplicationEventPayload.class);
        loggerInfo(msg);
        applicationService.handleApplicationCreation(applicationEventPayload);
        return msg;
    }

    /**
     * 创建组事件，消费创建项目事件
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
        gitlabGroupService.createEnvGroup(gitlabGroupPayload);
        return msg;
    }


    /**
     * 消费更新应用事件，更新应用对应的组
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_UPDATE_GITLAB_GROUP,
            description = "devops  更新 应用 Group",
            sagaCode = SagaTopicCodeConstants.BASE_UPDATE_APPLICATION,
            maxRetryCount = 3,
            seq = 1)
    public String handleUpdateGitlabGroupEvent(String msg) {
        loggerInfo(msg);
        ApplicationEventPayload projectPayload = gson.fromJson(msg, ApplicationEventPayload.class);
        gitlabGroupService.updateApplicationGroup(projectPayload);
        return msg;
    }

    /**
     * 更新项目事件，为项目更新环境组
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_UPDATE_GITOPS_GROUP,
            description = "devops  更新 GitOps Group",
            sagaCode = SagaTopicCodeConstants.IAM_UPDATE_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String handleUpdateGitOpsGroupEvent(String msg) {
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectPayload, gitlabGroupPayload);
        loggerInfo(msg);
        gitlabGroupService.updateEnvGroup(gitlabGroupPayload);
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
        harborService.createHarborForProject(harborPayload);
        return msg;
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

    /**
     * 应用上传
     */
    @SagaTask(code = SagaTaskCodeConstants.APIM_UPLOAD_APP,
            description = "应用上传",
            sagaCode = SagaTopicCodeConstants.APIM_UPLOAD_APP,
            maxRetryCount = 3, seq = 1)
    public String uploadApp(String payload) {
        AppMarketUploadPayload appMarketUploadVO = gson.fromJson(payload, AppMarketUploadPayload.class);
        loggerInfo(appMarketUploadVO);
        orgAppMarketService.uploadAPP(appMarketUploadVO);
        return payload;
    }

    /**
     * 应用上传,修复版本
     */
    @SagaTask(code = SagaTaskCodeConstants.APIM_UPLOAD_APP_FIX_VERSION,
            description = "应用上传,修复版本",
            sagaCode = SagaTopicCodeConstants.APIM_UPLOAD_APP_FIX_VERSION,
            maxRetryCount = 3, seq = 1)
    public String uploadAppFixVersion(String payload) {
        AppMarketFixVersionPayload fixVersionPayload = gson.fromJson(payload, AppMarketFixVersionPayload.class);
        loggerInfo(fixVersionPayload);
        orgAppMarketService.uploadAPPFixVersion(fixVersionPayload);
        return payload;
    }


    /**
     * 应用下载
     */
    @SagaTask(code = SagaTaskCodeConstants.APIM_DOWNLOAD_APP,
            description = "应用下载",
            sagaCode = SagaTopicCodeConstants.APIM_DOWNLOAD_APP,
            maxRetryCount = 3, seq = 1)
    public String downloadApp(String payload) {
        AppMarketDownloadPayload appMarketDownloadPayload = gson.fromJson(payload, AppMarketDownloadPayload.class);
        loggerInfo(appMarketDownloadPayload);
        orgAppMarketService.downLoadApp(appMarketDownloadPayload);
        return payload;
    }
}
