package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.LogUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * @author zmf
 * @since 12/5/19
 */
@Service
public class SendNotificationServiceImpl implements SendNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendNotificationServiceImpl.class);

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Value("${sendMessages:false}")
    private boolean sendMessages;

    @Autowired
    private NotifyClient notifyClient;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsMergeRequestService devopsMergeRequestService;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    /**
     * 发送和应用服务失败、启用和停用的消息(调用此方法时注意在外层捕获异常，此方法不保证无异常抛出)
     *
     * @param appServiceId    应用服务id
     * @param sendSettingCode 消息code
     * @param targetSupplier  转换目标用户
     */
    private void sendNoticeAboutAppService(Long appServiceId, String sendSettingCode, Function<AppServiceDTO, List<NoticeSendDTO.User>> targetSupplier) {
        if (!sendMessages) {
            return;
        }
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        if (appServiceDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("AppService", appServiceId, LOGGER);
            return;
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
        if (projectDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("Project", appServiceDTO.getProjectId(), LOGGER);
            return;
        }
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        if (organizationDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("Organization", projectDTO.getOrganizationId(), LOGGER);
            return;
        }

        sendNotices(sendSettingCode, projectDTO.getId(), targetSupplier.apply(appServiceDTO), makeAppServiceParams(organizationDTO.getId(), projectDTO.getId(), projectDTO.getName(), projectDTO.getCategory(), appServiceDTO.getName()));
    }


    /**
     * 应用服务相关模板所需要的参数
     *
     * @param organizationId  组织id
     * @param projectId       项目id
     * @param projectName     项目名称
     * @param projectCategory 项目类别
     * @param appServiceName  应用服务名称
     * @return 参数映射
     */
    private Map<String, Object> makeAppServiceParams(Long organizationId, Long projectId, String projectName, String projectCategory, String appServiceName) {
        Map<String, Object> params = new HashMap<>();
        params.put("organizationId", organizationId);
        params.put("projectId", projectId);
        params.put("projectName", projectName);
        params.put("projectCategory", projectCategory);
        params.put("appServiceName", appServiceName);
        return params;
    }

    @Override
    public void sendNotices(String sendSettingCode, Long sourceId, List<NoticeSendDTO.User> targetUsers, Map<String, Object> params) {
        if (!sendMessages) {
            return;
        }

        notifyClient.sendMessage(constructNotice(sendSettingCode, sourceId, targetUsers, params));
    }

    @Override
    public void sendWhenAppServiceFailure(Long appServiceId) {
        if (!sendMessages) {
            return;
        }
        doWithTryCatchAndLog(
                () -> {
                    // TODO by zmf
                    sendNoticeAboutAppService(appServiceId, null, app ->
                            ArrayUtil.singleAsList(constructTargetUser(app.getCreatedBy()))
                    );
                },
                ex -> LOGGER.info("Error occurred when sending message about failure of app-service. The exception is {}.", ex));
    }

    @Override
    public void sendWhenAppServiceEnabled(Long appServiceId) {
        if (!sendMessages) {
            return;
        }
        doWithTryCatchAndLog(
                () -> {
                    // TODO by zmf
                    sendNoticeAboutAppService(appServiceId, null,
                            app -> appServiceService.listAllUserPermission(app.getId())
                                    .stream()
                                    .map(p -> constructTargetUser(p.getIamUserId()))
                                    .collect(Collectors.toList()));
                },
                ex -> LOGGER.info("Error occurred when sending message about app-service-enable. The exception is {}.", ex));
    }

    @Override
    public void sendWhenAppServiceDisabled(Long appServiceId) {
        if (!sendMessages) {
            return;
        }
        doWithTryCatchAndLog(
                () -> {
                    // TODO by zmf
                    sendNoticeAboutAppService(appServiceId, null,
                            app -> appServiceService.listAllUserPermission(app.getId())
                                    .stream()
                                    .map(p -> constructTargetUser(p.getIamUserId()))
                                    .collect(Collectors.toList()));
                },
                ex -> LOGGER.info("Error occurred when sending message about app-service-disable. The exception is {}.", ex));
    }


    @Override
    public void sendWhenCDFailure(Long gitlabPipelineId, AppServiceDTO appServiceDTO, String pipelineOperatorUserName) {
        doWithTryCatchAndLog(() -> {
                    if (appServiceDTO == null) {
                        LOGGER.info("Parameter appServiceDTO is null when sending gitlab pipeline failure notice");
                        return;
                    }

                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
                    if (projectDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("Project", appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("Organization", projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, Object> params = new HashMap<>();
                    params.put("gitlabUrl", gitlabUrl);
                    params.put("organizationCode", organizationDTO.getCode());
                    params.put("projectCode", projectDTO.getCode());
                    params.put("projectName", projectDTO.getName());
                    params.put("appServiceCode", appServiceDTO.getCode());
                    params.put("appServiceName", appServiceDTO.getName());
                    params.put("gitlabPipelineId", gitlabPipelineId);

                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(pipelineOperatorUserName);

                    // TODO by zmf
                    sendNotices(null, projectDTO.getId(), ArrayUtil.singleAsList(constructTargetUser(iamUserDTO.getId())), params);
                },
                ex -> LOGGER.info("Error occurred when sending message about gitlab-pipeline-failure. The exception is {}.", ex));
    }

    /**
     * 构造merge request审核，关闭和合并三个事件的所需参数
     *
     * @param gitlabUrl        gitlab的http(s)地址
     * @param organizationCode 组织code
     * @param projectCode      项目code
     * @param projectName      项目名称
     * @param appServiceCode   应用服务code
     * @param appServiceName   应用服务名称
     * @param realName         对于审核merge request的消息，是合并请求提交者的名称；
     *                         对于merge request关闭和合并的事件，是merge request提出者的名称
     * @param mergeRequestId   合并请求的id
     */
    private Map<String, Object> makeMergeRequestEventParams(String gitlabUrl,
                                                            String organizationCode,
                                                            String projectCode,
                                                            String projectName,
                                                            String appServiceCode,
                                                            String appServiceName,
                                                            String realName,
                                                            Long mergeRequestId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gitlabUrl", gitlabUrl);
        params.put("organizationCode", organizationCode);
        params.put("projectCode", projectCode);
        params.put("projectName", projectName);
        params.put("appServiceCode", appServiceCode);
        params.put("appServiceName", appServiceName);
        params.put("realName", realName);
        params.put("mergeRequestId", mergeRequestId);
        return params;
    }

    @Override
    public void sendWhenMergeRequestAuditEvent(Integer gitlabProjectId, Long mergeRequestId) {
        if (!sendMessages) {
            return;
        }
        doWithTryCatchAndLog(
                () -> {
                    DevopsMergeRequestDTO devopsMergeRequestDTO = devopsMergeRequestService.baseQueryByAppIdAndMergeRequestId(TypeUtil.objToLong(gitlabProjectId), mergeRequestId);
                    if (devopsMergeRequestDTO == null) {
                        LOGGER.info("Merge Request with id {} and gitlab project id {} is null", mergeRequestId, gitlabProjectId);
                        return;
                    }

                    UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(devopsMergeRequestDTO.getAssigneeId());
                    if (userAttrDTO == null) {
                        LOGGER.info("DevopsUser with gitlab user id {} is null.", devopsMergeRequestDTO.getAssigneeId());
                        return;
                    }

                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userAttrDTO.getIamUserId());
                    if (iamUserDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("IamUser", userAttrDTO.getIamUserId(), LOGGER);
                        return;
                    }

                    AppServiceDTO appServiceDTO = queryAppServiceByGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId));
                    if (appServiceDTO == null) {
                        LOGGER.info("AppService is null with gitlab project id {}", gitlabProjectId);
                        return;
                    }

                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
                    if (projectDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("Project", appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("Organization", projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, Object> params = makeMergeRequestEventParams(gitlabUrl, organizationDTO.getCode(), projectDTO.getCode(), projectDTO.getName(), appServiceDTO.getCode(), appServiceDTO.getName(), iamUserDTO.getRealName(), mergeRequestId);

                    // TODO by zmf
                    sendNotices(null, projectDTO.getId(), ArrayUtil.singleAsList(constructTargetUser(iamUserDTO.getId())), params);
                },
                ex -> LOGGER.info("Error occurred when sending message about merge-request-audit. The exception is {}.", ex));
    }

    private AppServiceDTO queryAppServiceByGitlabProjectId(Integer gitlabProjectId) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setGitlabProjectId(Objects.requireNonNull(gitlabProjectId));
        return appServiceMapper.selectOne(appServiceDTO);
    }

    /**
     * 当merge request关闭或者合并时发送消息
     *
     * @param sendSettingCode 发送消息的code
     * @param gitlabProjectId merge request 所属gitlab项目id
     * @param mergeRequestId  merge request id
     */
    private void doSendWhenMergeRequestClosedOrMerged(String sendSettingCode, Integer gitlabProjectId, Long mergeRequestId) {
        if (!sendMessages) {
            return;
        }
        doWithTryCatchAndLog(
                () -> {
                    DevopsMergeRequestDTO devopsMergeRequestDTO = devopsMergeRequestService.baseQueryByAppIdAndMergeRequestId(TypeUtil.objToLong(gitlabProjectId), mergeRequestId);
                    if (devopsMergeRequestDTO == null) {
                        LOGGER.info("Merge Request with id {} and gitlab project id {} is null", mergeRequestId, gitlabProjectId);
                        return;
                    }

                    // merge request的发起者
                    UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(devopsMergeRequestDTO.getAuthorId());
                    if (userAttrDTO == null) {
                        LOGGER.info("DevopsUser with gitlab user id {} is null.", devopsMergeRequestDTO.getAuthorId());
                        return;
                    }

                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userAttrDTO.getIamUserId());
                    if (iamUserDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("IamUser", userAttrDTO.getIamUserId(), LOGGER);
                        return;
                    }

                    AppServiceDTO appServiceDTO = queryAppServiceByGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId));
                    if (appServiceDTO == null) {
                        LOGGER.info("AppService is null with gitlab project id {}", gitlabProjectId);
                        return;
                    }

                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
                    if (projectDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("Project", appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("Organization", projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, Object> params = makeMergeRequestEventParams(gitlabUrl, organizationDTO.getCode(), projectDTO.getCode(), projectDTO.getName(), appServiceDTO.getCode(), appServiceDTO.getName(), iamUserDTO.getRealName(), mergeRequestId);

                    sendNotices(sendSettingCode, projectDTO.getId(), ArrayUtil.singleAsList(constructTargetUser(iamUserDTO.getId())), params);
                },
                ex -> LOGGER.info("Error occurred when sending message about {}. The exception is {}.", sendSettingCode, ex));
    }

    @Override
    public void sendWhenMergeRequestClosed(Integer gitlabProjectId, Long mergeRequestId) {
        // TODO by zmf
        doSendWhenMergeRequestClosedOrMerged(null, gitlabProjectId, mergeRequestId);
    }


    @Override
    public void sendWhenMergeRequestPassed(Integer gitlabProjectId, Long mergeRequestId) {
        // TODO by zmf
        doSendWhenMergeRequestClosedOrMerged(null, gitlabProjectId, mergeRequestId);
    }

    /**
     * 发送资源创建相关的失败通知
     *
     * @param sendSettingCode 通知的code
     * @param envId           环境的id
     * @param resourceName    资源的名称
     * @param creatorId       创建者的id
     */
    private void doSendWhenResourceCreationFailure(String sendSettingCode, Long envId, String resourceName, Long creatorId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);

        if (devopsEnvironmentDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("Environment", envId, LOGGER);
            return;
        }

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        if (projectDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("Project", devopsEnvironmentDTO.getProjectId(), LOGGER);
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("projectName", Objects.requireNonNull(projectDTO.getName()));
        params.put("envName", Objects.requireNonNull(devopsEnvironmentDTO.getName()));
        params.put("resourceName", Objects.requireNonNull(resourceName));

        sendNotices(sendSettingCode, projectDTO.getId(), ArrayUtil.singleAsList(constructTargetUser(Objects.requireNonNull(creatorId))), params);
    }

    @Override
    public void sendWhenInstanceCreationFailure(Long envId, String resourceName, Long creatorId) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
        doSendWhenResourceCreationFailure(null, envId, resourceName, creatorId);
    }

    @Override
    public void sendWhenServiceCreationFailure(Long envId, String resourceName, Long creatorId) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
        doSendWhenResourceCreationFailure(null, envId, resourceName, creatorId);
    }

    @Override
    public void sendWhenIngressCreationFailure(Long envId, String resourceName, Long creatorId) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
        doSendWhenResourceCreationFailure(null, envId, resourceName, creatorId);
    }

    @Override
    public void sendWhenCertificationCreationFailure(Long envId, String resourceName, Long creatorId) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
        doSendWhenResourceCreationFailure(null, envId, resourceName, creatorId);
    }


    /**
     * 保证在执行逻辑时不抛出异常的包装方法
     *
     * @param actionInTry   正常处理的逻辑
     * @param actionInCatch 处理异常的逻辑
     */
    private static void doWithTryCatchAndLog(Runnable actionInTry, Consumer<Exception> actionInCatch) {
        if (actionInTry == null) {
            LOGGER.info("Internal fault: parameter actionInTry is unexpectedly null. Action abort.");
            return;
        }
        if (actionInCatch == null) {
            LOGGER.info("Internal fault: parameter actionInCatch is unexpectedly null. Action abort.");
            return;
        }

        try {
            actionInTry.run();
        } catch (Exception ex) {
            try {
                actionInCatch.accept(ex);
            } catch (Exception e) {
                LOGGER.info("Exception occurred in actionInCatch.accept...");
            }
        }
    }

    private static NoticeSendDTO.User constructTargetUser(Long id) {
        NoticeSendDTO.User targetUser = new NoticeSendDTO.User();
        targetUser.setId(id);
        return targetUser;
    }

    private static NoticeSendDTO constructNotice(String sendSettingCode, Long sourceId, List<NoticeSendDTO.User> targetUsers, Map<String, Object> params) {
        NoticeSendDTO noticeSendDTO = new NoticeSendDTO();
        noticeSendDTO.setCode(Objects.requireNonNull(sendSettingCode));
        noticeSendDTO.setSourceId(Objects.requireNonNull(sourceId));
        noticeSendDTO.setTargetUsers(Objects.requireNonNull(targetUsers));
        noticeSendDTO.setParams(Objects.requireNonNull(params));
        return noticeSendDTO;
    }
}
