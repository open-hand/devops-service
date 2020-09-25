package io.choerodon.devops.app.eventhandler;

import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.AssignAdminVO;
import io.choerodon.devops.api.vo.iam.DeleteAdminVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.HostDeployType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;
import io.choerodon.devops.infra.util.ArrayUtil;
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
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;
    @Autowired
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;

    private void loggerInfo(Object o) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("data: {}", JSONObject.toJSONString(o));
        }
    }

    /**
     * 创建组事件，消费创建项目事件
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_GITLAB_GROUP,
            description = "devops 创建对应项目的三个Group",
            sagaCode = SagaTopicCodeConstants.IAM_CREATE_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String handleGitOpsGroupEvent(String msg) {
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectPayload, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.createGroups(gitlabGroupPayload);
        //为新项目的三个组添加组织下管理员角色
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectPayload.getProjectId());
        List<OrgAdministratorVO> orgAdministratorVOS = baseServiceClientOperator.listOrgAdministrator(projectDTO.getOrganizationId()).getContent();
        if (!CollectionUtils.isEmpty(orgAdministratorVOS)) {
            orgAdministratorVOS.forEach(orgAdministratorVO -> gitlabGroupMemberService.assignGitLabGroupMemberForOwner(projectDTO, orgAdministratorVO.getId()));
        }
        return msg;
    }

    /**
     * 更新项目事件，为项目更新组
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_UPDATE_GITLAB_GROUP,
            description = "devops更新项目对应的三个GitLab组",
            sagaCode = SagaTopicCodeConstants.IAM_UPDATE_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String handleUpdateGitOpsGroupEvent(String msg) {
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectPayload, gitlabGroupPayload);
        loggerInfo(msg);
        gitlabGroupService.updateGroups(gitlabGroupPayload);
        return msg;
    }

//    /**
//     * 创建harbor项目事件
//     */
//    //todo
//    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_HARBOR,
//            description = "devops 创建 Harbor",
//            sagaCode = SagaTopicCodeConstants.IAM_CREATE_PROJECT,
//            maxRetryCount = 3,
//            seq = 5)
//    public String handleHarborEvent(String msg) {
//        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
//        HarborPayload harborPayload = new HarborPayload(
//                projectPayload.getProjectId(),
//                projectPayload.getOrganizationCode() + "-" + projectPayload.getProjectCode()
//        );
//        loggerInfo(harborPayload);
//        harborService.createHarborForProject(harborPayload);
//        return msg;
//    }

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
        LOGGER.info("update user role start");
        loggerInfo(gitlabGroupMemberVOList);
        gitlabGroupMemberService.createGitlabGroupMemberRole(gitlabGroupMemberVOList, false);
        LOGGER.info("update user role end");
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
        LOGGER.info("delete gitlab role start");
        loggerInfo(gitlabGroupMemberVOList);
        gitlabGroupMemberService.deleteGitlabGroupMemberRole(gitlabGroupMemberVOList);
        LOGGER.info("delete gitlab role end");
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
        LOGGER.info("create user start");
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
            LOGGER.info("create user end");
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

        gitlabUserService.isEnabledGitlabUser(TypeUtil.objToLong(gitlabUserVO.getId()));
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

        gitlabUserService.disEnabledGitlabUser(TypeUtil.objToLong(gitlabUserVO.getId()));
        return payload;
    }

    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_ADD_ADMIN,
            description = "创建Root用户事件",
            sagaCode = SagaTopicCodeConstants.ASSIGN_ADMIN,
            maxRetryCount = 3,
            seq = 1)
    public String handleAssignAdminEvent(String payload) {
        AssignAdminVO assignAdminVO = JSONObject.parseObject(payload, AssignAdminVO.class);
        gitlabUserService.assignAdmins(assignAdminVO == null ? Collections.emptyList() : assignAdminVO.getAdminUserIds());
        return payload;
    }

    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_DELETE_ADMIN,
            description = "删除Root用户事件",
            sagaCode = SagaTopicCodeConstants.DELETE_ADMIN,
            maxRetryCount = 3,
            seq = 1)
    public String handleDeleteAdminEvent(String payload) {
        DeleteAdminVO deleteAdminVO = JSONObject.parseObject(payload, DeleteAdminVO.class);
        gitlabUserService.deleteAdmin(deleteAdminVO == null ? null : deleteAdminVO.getAdminUserId());
        return payload;
    }


    /**
     * 处理组织层创建用户
     *
     * @param payload
     * @return
     */
    @SagaTask(code = SagaTaskCodeConstants.ORG_USER_CREAT,
            description = "组织层创建用户并分配角色",
            sagaCode = SagaTaskCodeConstants.ORG_USER_CREAT,
            maxRetryCount = 5, seq = 1)
    public String createAndUpdateUser(String payload) {
        LOGGER.info("Org create user: the payload is {}", payload);
        CreateAndUpdateUserEventPayload createAndUpdateUserEventPayload = gson.fromJson(payload, CreateAndUpdateUserEventPayload.class);
        handleCreateUserEvent(gson.toJson(ArrayUtil.singleAsList(createAndUpdateUserEventPayload.getUserEventPayload())));

        LOGGER.info("Org create user: update user role start");
        gitlabGroupMemberService.createGitlabGroupMemberRole(createAndUpdateUserEventPayload.getUserMemberEventPayloads(), true);
        LOGGER.info("Org create user: update user role end");
        return payload;
    }

    /**
     * 处理组织层创建用户
     *
     * @param payload
     * @return
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_HOST_FEPLOY,
            description = "主机部署",
            sagaCode = SagaTopicCodeConstants.DEVOPS_HOST_FEPLOY,
            maxRetryCount = 5, seq = 1)
    public void hostDeploy(String payload) {
        HostDeployPayload hostDeployPayload = gson.fromJson(payload, HostDeployPayload.class);

        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>> Userdetails is {}", DetailsHelper.getUserDetails());
        if (DetailsHelper.getUserDetails().getUserId().equals(BaseConstants.ANONYMOUS_USER_ID)) {
            DetailsHelper.setCustomUserDetails(0L, BaseConstants.DEFAULT_LOCALE_STR);
        }
        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(hostDeployPayload.getJobRecordId());
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.IMAGED_DEPLOY.getValue())) {
            devopsCdPipelineRecordService.cdHostImageDeploy(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
        } else if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
            devopsCdPipelineRecordService.cdHostJarDeploy(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
        } else  {
            devopsCdPipelineRecordService.cdHostCustomDeploy(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
        }
    }
}
