package io.choerodon.devops.app.eventhandler;

import static io.choerodon.devops.infra.constant.GitOpsConstants.NEW_LINE;
import static io.choerodon.devops.infra.constant.MiscConstants.DEVOPS;
import static io.choerodon.devops.infra.constant.MiscConstants.OPERATIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.AssignAdminVO;
import io.choerodon.devops.api.vo.iam.DeleteAdminVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.CreateAndUpdateUserEventPayload;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.exception.NoTraceException;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.LogUtil;
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
    private ChartService chartService;
    @Autowired
    private GitlabHandleService gitlabHandleService;
    @Autowired
    private DevopsAppTemplateService devopsAppTemplateService;

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
        if (projectPayload.getProjectCategoryVOS().stream().map(ProjectCategoryVO::getCode).noneMatch(s -> DEVOPS.equals(s) || s.equals(OPERATIONS))) {
            return msg;
        }
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectPayload, gitlabGroupPayload);
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.createGroups(gitlabGroupPayload);
        //为新项目的三个组添加组织下管理员角色
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectPayload.getProjectId());
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
        LOGGER.info(">>>>>>>>>start sync project devops category,playLoad={}", msg);
        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
        //不包含devops项目类型不做同步
        if (CollectionUtils.isEmpty(projectPayload.getProjectCategoryVOS())) {
            return msg;
        }
        if (projectPayload.getProjectCategoryVOS().stream().map(ProjectCategoryVO::getCode).noneMatch(s -> DEVOPS.equals(s) || s.equals(OPERATIONS))) {
            return msg;
        }
        gitlabHandleService.handleProjectCategoryEvent(projectPayload);
        LOGGER.info(">>>>>>>>>end sync project devops category<<<<<<<<<<");
        return msg;
    }

    /**
     * 角色同步事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_UPDATE_MEMBER_ROLE,
            description = "角色同步事件",
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE,
            concurrentLimitNum = 30,
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
        List<GitlabGroupMemberVO> tempList = new ArrayList<>(gitlabGroupMemberVOList);
        tempList.forEach(t -> {
            if (t.getResourceType().equals(ResourceLevel.PROJECT.value())) {
                if (baseServiceClientOperator.listProjectCategoryById(t.getResourceId()).stream().noneMatch(s -> DEVOPS.equals(s) || s.equals(OPERATIONS))) {
                    gitlabGroupMemberVOList.remove(t);
                }
            }
        });
        if (CollectionUtils.isEmpty(gitlabGroupMemberVOList)) {
            return tempList;
        }
        LOGGER.info("delete gitlab role start");
        loggerInfo(gitlabGroupMemberVOList);
        gitlabGroupMemberService.deleteGitlabGroupMemberRole(gitlabGroupMemberVOList);
        LOGGER.info("delete gitlab role end");
        return tempList;
    }

    /**
     * 用户创建事件
     */
    @SagaTask(code = SagaTaskCodeConstants.IAM_CREATE_USER,
            description = "用户创建事件",
            sagaCode = SagaTopicCodeConstants.IAM_CREATE_USER,
            maxRetryCount = 5, seq = 1)
    public List<GitlabUserVO> handleCreateUserEvent(String payload) {
        List<GitlabUserVO> gitlabUserDTO = JsonHelper.unmarshalByJackson(payload, new TypeReference<List<GitlabUserVO>>() {
        });
        loggerInfo(gitlabUserDTO);
        StringBuilder failedUsers = new StringBuilder();
        List<Exception> exs = new ArrayList<>();

        gitlabUserDTO.forEach(t -> {
            LOGGER.info("Start to create user {}", t);
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
            try {
                gitlabUserService.createGitlabUserInNewTx(gitlabUserReqDTO);
                LOGGER.info("Finished to create user {}", t);
            } catch (Exception ex) {
                // 补偿机制
                // 根据邮箱查询到devops_user 但是对应的iam_user_id 不存在
                // 更新gitlab用户 重新建立关联关系
                if (!gitlabUserService.updateGitlabUserInNewTx(gitlabUserReqDTO)) {
                    failedUsers.append("User with loginName ")
                            .append(t.getUsername())
                            .append("and email ")
                            .append(t.getEmail())
                            .append(" Failed, due to: ")
                            .append(ex.getMessage())
                            .append(NEW_LINE);
                    exs.add(ex);
                    LOGGER.warn("Failed to create user {}", t);
                    LOGGER.warn("And the ex is", ex);
                }
            }
        });

        // 如果有错误信息，抛出没有trace新的异常，用于界面上通过事务实例重试
        if (failedUsers.length() > 0) {
            // 拼接所有的异常的trace信息
            for (int i = 0; i < exs.size(); i++) {
                failedUsers.append("Ex ").append(i).append(" trace:").append(NEW_LINE);
                failedUsers.append(LogUtil.readContentOfRootCause(exs.get(i)));
            }

            throw new NoTraceException(failedUsers.toString());
        }
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
            concurrentLimitNum = 20,
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
     * 处理删除habor镜像
     *
     * @param payload
     * @return
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_DELETE_HABOR_IMAGE_TAGS,
            description = "处理删除habor镜像",
            sagaCode = SagaTopicCodeConstants.DEVOPS_DELETE_APPLICATION_SERVICE_VERSION,
            maxRetryCount = 5, seq = 10)
    public String deleteHaborImageTags(String payload) {
        CustomResourceVO customResourceVO = gson.fromJson(payload, CustomResourceVO.class);
        harborService.batchDeleteImageTags(customResourceVO.getHarborImageTagDTOS());
        return payload;
    }

    /**
     * 处理删除chart version
     *
     * @param payload
     * @return
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_DELETE_CHART_VERSIONS,
            description = "处理删除chart version",
            sagaCode = SagaTopicCodeConstants.DEVOPS_DELETE_APPLICATION_SERVICE_VERSION,
            maxRetryCount = 5, seq = 10)
    public String deleteChartTags(String payload) {
        CustomResourceVO customResourceVO = gson.fromJson(payload, CustomResourceVO.class);
        chartService.batchDeleteChartVersion(customResourceVO.getChartTagVOS());
        return payload;
    }

    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_APP_TEMPLATE,
            description = "创建应用模板",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_APP_TEMPLATE,
            maxRetryCount = 5, seq = 10)
    public String createAppTemplate(String payload) {
        DevopsAppTemplateCreateVO devopsAppTemplateCreateVO = gson.fromJson(payload, DevopsAppTemplateCreateVO.class);
        try {
            devopsAppTemplateService.createTemplateSagaTask(devopsAppTemplateCreateVO);
        } catch (Exception e) {
            devopsAppTemplateService.updateAppTemplateStatus(devopsAppTemplateCreateVO.getAppTemplateId());
            throw e;
        }
        return payload;
    }

    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_DELETE_APP_TEMPLATE,
            description = "创建应用模板",
            sagaCode = SagaTopicCodeConstants.DEVOPS_DELETE_APP_TEMPLATE,
            maxRetryCount = 5, seq = 10)
    public String deleteAppTemplate(String payload) {
        Long appTemplateId = gson.fromJson(payload, Long.class);
        devopsAppTemplateService.deleteAppTemplateSagaTask(appTemplateId);
        return payload;
    }

    /**
     * devops 同步项目类型的处理
     *
     * @param msg
     * @return string
     */
//    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_PROJECT_CATEGORY_SYNC,
//            description = "devops 同步项目类型的处理(group与角色同步事件)",
//            sagaCode = SagaTopicCodeConstants.ADD_PROJECT_CATEGORY,
//            maxRetryCount = 3,
//            seq = 1)
//    public String handleProjectCategoryEvent(String msg) {
//        LOGGER.info(">>>>>>>>>start sync project devops category,playLoad={}", msg);
//        ProjectPayload projectPayload = gson.fromJson(msg, ProjectPayload.class);
//        //不包含devops项目类型不做同步
//        if (! projectPayload.getProjectCategoryVOS().stream().map(ProjectCategoryVO::getCode).collect(Collectors.toList()).contains(devops)) {
//            return msg;
//        }
//        gitlabHandleService.handleProjectCategoryEvent(projectPayload);
//        LOGGER.info(">>>>>>>>>end sync project devops category<<<<<<<<<<");
//        return msg;
//    }
}
