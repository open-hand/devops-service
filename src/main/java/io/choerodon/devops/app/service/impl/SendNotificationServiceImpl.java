package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.DevopsUserPermissionVO;
import io.choerodon.devops.app.eventhandler.payload.DevopsEnvUserPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.EnvironmentType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.SendSettingEnum;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.LogUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 发送DevOps相关通知的实现类
 * 其中数字类型的参数要转成字符串，否则在notify-service中会被转为逗号分隔的形式，如`11,111` (0.20版本)
 *
 * @author zmf
 * @since 12/5/19
 */
@Service
public class SendNotificationServiceImpl implements SendNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendNotificationServiceImpl.class);
    private static final String PROJECT = "Project";
    private static final String ORGANIZATION = "Organization";
    private static final String NOTIFY_TYPE = "devops";

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

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
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsClusterService devopsClusterService;

    // TODO 发通知
//    /**
//     * 发送和应用服务失败、启用和停用的消息(调用此方法时注意在外层捕获异常，此方法不保证无异常抛出)
//     *
//     * @param appServiceId    应用服务id
//     * @param sendSettingCode 消息code
//     * @param targetSupplier  转换目标用户
//     */
//    private void sendNoticeAboutAppService(Long appServiceId, String sendSettingCode, Function<AppServiceDTO, List<NoticeSendDTO.User>> targetSupplier, WebHookJsonSendDTO webHookJsonSendDTO) {
//        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
//        if (appServiceDTO == null) {
//            LogUtil.loggerInfoObjectNullWithId("AppService", appServiceId, LOGGER);
//            return;
//        }
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
//        if (projectDTO == null) {
//            LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
//            return;
//        }
//        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
//        if (organizationDTO == null) {
//            LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
//            return;
//        }
//
//        List<NoticeSendDTO.User> targetUsers = targetSupplier.apply(appServiceDTO);
//        LOGGER.debug("AppService notice {}. Target users size: {}", sendSettingCode, targetUsers.size());
//
//        sendNotices(sendSettingCode, projectDTO.getId(), targetUsers, makeAppServiceParams(organizationDTO.getId(), projectDTO.getId(), projectDTO.getName(), projectDTO.getCategory(), appServiceDTO.getName()), webHookJsonSendDTO);
//    }

    // TODO 发通知
//    /**
//     * 创建，删除应用服务发送消息
//     *
//     * @param appServiceDTO
//     * @param sendSettingCode
//     * @param targetSupplier
//     */
//    private void sendNoticeAboutAppService(AppServiceDTO appServiceDTO, String sendSettingCode, Function<AppServiceDTO, List<NoticeSendDTO.User>> targetSupplier, WebHookJsonSendDTO webHookJsonSendDTO) {
//        if (appServiceDTO == null) {
//            LogUtil.loggerInfoObjectNullWithId("AppService", null, LOGGER);
//            return;
//        }
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
//        if (projectDTO == null) {
//            LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
//            return;
//        }
//        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
//        if (organizationDTO == null) {
//            LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
//            return;
//        }
//
//        List<NoticeSendDTO.User> targetUsers = targetSupplier.apply(appServiceDTO);
//        LOGGER.debug("AppService notice {}. Target users size: {}", sendSettingCode, targetUsers.size());
//
//        sendNotices(sendSettingCode, projectDTO.getId(), targetUsers, makeAppServiceParams(organizationDTO.getId(), projectDTO.getId(), projectDTO.getName(), projectDTO.getCategory(), appServiceDTO.getName()), webHookJsonSendDTO);
//    }

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
        params.put("organizationId", String.valueOf(organizationId));
        params.put("projectId", String.valueOf(projectId));
        params.put("projectName", projectName);
        params.put("projectCategory", projectCategory);
        params.put("appServiceName", appServiceName);
        return params;
    }

    // TODO 发通知
//    /**
//     * 发送通知
//     *
//     * @param sendSettingCode 通知code
//     * @param sourceId        projectId, organizationId, 0L
//     * @param targetUsers     目标用户
//     * @param params          参数映射
//     */
//    public void sendNotices(String sendSettingCode, Long sourceId, List<NoticeSendDTO.User> targetUsers, Map<String, Object> params, WebHookJsonSendDTO webHookJsonSendDTO) {
//        notifyClient.sendMessage(constructNotice(sendSettingCode, sourceId, targetUsers, params, webHookJsonSendDTO));
//    }

    @Override
    public void sendWhenAppServiceCreate(AppServiceDTO appServiceDTO) {
        JSONObject JSONObject = new JSONObject();
        JSONObject.put("appServerId", appServiceDTO.getId());
        JSONObject.put("appServerCode", appServiceDTO.getCode());
        JSONObject.put("appServerName", appServiceDTO.getName());
        JSONObject.put("appServerType", appServiceDTO.getType());
        JSONObject.put("projectId", appServiceDTO.getProjectId());
        doWithTryCatchAndLog(
                () -> {
                    // TODO 发通知
//                    sendNoticeAboutAppService(appServiceDTO, SendSettingEnum.CREATE_APPSERVICE.value(),
//                            app -> ArrayUtil.singleAsList(constructTargetUser(app.getCreatedBy())),
//                            getWebHookJsonSendDTO(JSONObject,
//                                    SendSettingEnum.CREATE_APPSERVICE.value(),
//                                    appServiceDTO.getCreatedBy(), appServiceDTO.getCreationDate())
//                    )
                },
                ex -> LOGGER.info("Error occurred when sending message about of app-service-create. The exception is {}.", ex));
    }

    // TODO 发通知
//    private WebHookJsonSendDTO.User getWebHookuser(Long createdBy) {
//        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(Arrays.asList(createdBy));
//        return CollectionUtils.isEmpty(iamUserDTOS) ?
//                new WebHookJsonSendDTO.User("0", "unknown") :
//                new WebHookJsonSendDTO.User(iamUserDTOS.get(0).getLoginName(), iamUserDTOS.get(0).getRealName());
//    }

    @Override
    public void sendWhenAppServiceFailure(Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (Objects.isNull(appServiceDTO)) {
            return;
        }
        JSONObject JSONObject = new JSONObject();
        JSONObject.put("appServerId", appServiceDTO.getId());
        JSONObject.put("appServerCode", appServiceDTO.getCode());
        JSONObject.put("appServerName", appServiceDTO.getName());
        JSONObject.put("appServerType", appServiceDTO.getType());
        JSONObject.put("projectId", appServiceDTO.getProjectId());
        // TODO 发通知
//        doWithTryCatchAndLog(
//                () -> sendNoticeAboutAppService(appServiceId, NoticeCodeConstants.APP_SERVICE_CREATION_FAILED,
//                        app -> ArrayUtil.singleAsList(constructTargetUser(app.getCreatedBy())),
//                        getWebHookJsonSendDTO(JSONObject, SendSettingEnum.APPSERVICE_CREATIONFAILURE.value(),
//                                appServiceDTO.getCreatedBy(), appServiceDTO.getLastUpdateDate())),
//                ex -> LOGGER.info("Error occurred when sending message about failure of app-service. The exception is {}.", ex));
    }

    private static <T> List<T> mapNullListToEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    @Async
    public void sendWhenAppServiceEnabled(Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (Objects.isNull(appServiceDTO)) {
            return;
        }
        JSONObject JSONObject = new JSONObject();
        JSONObject.put("appServerId", appServiceDTO.getId());
        JSONObject.put("appServerCode", appServiceDTO.getCode());
        JSONObject.put("appServerName", appServiceDTO.getName());
        JSONObject.put("appServerType", appServiceDTO.getType());
        JSONObject.put("projectId", appServiceDTO.getProjectId());
        JSONObject.put("enabled", appServiceDTO.getActive());
        // TODO 发通知
//        doWithTryCatchAndLog(
//                () -> sendNoticeAboutAppService(appServiceId, NoticeCodeConstants.APP_SERVICE_ENABLED,
//                        app -> mapNullListToEmpty(appServiceService.pagePermissionUsers(app.getProjectId(), app.getId(), new PageRequest(0, 0), null)
//                                .getContent())
//                                .stream()
//                                .map(p -> constructTargetUser(p.getIamUserId()))
//                                .collect(Collectors.toList()),
//                        getWebHookJsonSendDTO(JSONObject, SendSettingEnum.ENABLE_APPSERVICE.value(), appServiceDTO.getCreatedBy(), appServiceDTO.getLastUpdateDate())
//                ),
//                ex -> LOGGER.info("Error occurred when sending message about app-service-enable. The exception is {}.", ex));
    }

    // TODO 发通知
//    public WebHookJsonSendDTO getWebHookJsonSendDTO(JSONObject JSONObject, String code, Long createdBy, Date lastUpdateDate) {
//        WebHookJsonSendDTO webHookJsonSendDTO = new WebHookJsonSendDTO(
//                code,
//                SendSettingEnum.getEventName(code),
//                JSONObject,
//                lastUpdateDate,
//                getWebHookuser(createdBy));
//        return webHookJsonSendDTO;
//    }


    @Override
    @Async
    public void sendWhenAppServiceDisabled(Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (Objects.isNull(appServiceDTO)) {
            return;
        }
        JSONObject JSONObject = new JSONObject();

        JSONObject.put("appServerId", appServiceDTO.getId());
        JSONObject.put("appServerCode", appServiceDTO.getCode());
        JSONObject.put("appServerName", appServiceDTO.getName());
        JSONObject.put("appServerType", appServiceDTO.getType());
        JSONObject.put("projectId", appServiceDTO.getProjectId());
        JSONObject.put("enabled", JSON.toJSONString(appServiceDTO.getActive()));

        // TODO 发通知
//        doWithTryCatchAndLog(
//                () -> sendNoticeAboutAppService(appServiceId, NoticeCodeConstants.APP_SERVICE_DISABLE,
//                        app -> mapNullListToEmpty(appServiceService.pagePermissionUsers(app.getProjectId(), app.getId(), new PageRequest(0, 0), null)
//                                .getContent())
//                                .stream()
//                                .map(p -> constructTargetUser(p.getIamUserId()))
//                                .collect(Collectors.toList()),
//                        getWebHookJsonSendDTO(JSONObject, SendSettingEnum.ENABLE_APPSERVICE.value(), appServiceDTO.getCreatedBy(), appServiceDTO.getLastUpdateDate())),
//                ex -> LOGGER.info("Error occurred when sending message about app-service-disable. The exception is {}.", ex));
    }

    /**
     * 删除数据消息发送同步执行
     */
    @Override
    @Async
    public void sendWhenAppServiceDelete(List<DevopsUserPermissionVO> devopsUserPermissionVOS, AppServiceDTO appServiceDTO) {
        JSONObject JSONObject = new JSONObject();
        JSONObject.put("appServerId", appServiceDTO.getId());
        JSONObject.put("appServerCode", appServiceDTO.getCode());
        JSONObject.put("appServerName", appServiceDTO.getName());
        JSONObject.put("appServerType", appServiceDTO.getType());
        JSONObject.put("projectId", appServiceDTO.getProjectId());
        // TODO 发通知
//        doWithTryCatchAndLog(
//                () -> sendNoticeAboutAppService(appServiceDTO, NoticeCodeConstants.DELETE_APP_SERVICE,
//                        app -> mapNullListToEmpty(devopsUserPermissionVOS)
//                                .stream()
//                                .map(p -> constructTargetUser(p.getIamUserId()))
//                                .collect(Collectors.toList()),
//                        getWebHookJsonSendDTO(JSONObject, SendSettingEnum.DELETE_APPSERVICE.value(), appServiceDTO.getCreatedBy(), new Date())),
//                ex -> LOGGER.info("Error occurred when sending message about app-service-delete. The exception is {}.", ex));
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
                        LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, Object> params = new HashMap<>();
                    params.put("gitlabUrl", gitlabUrl);
                    params.put("organizationCode", organizationDTO.getCode());
                    params.put("projectCode", projectDTO.getCode());
                    params.put("projectName", projectDTO.getName());
                    params.put("appServiceCode", appServiceDTO.getCode());
                    params.put("appServiceName", appServiceDTO.getName());
                    params.put("gitlabPipelineId", String.valueOf(gitlabPipelineId));

                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(pipelineOperatorUserName);

                    //
                    JSONObject JSONObject = new JSONObject();
                    JSONObject.put("projectId", projectDTO.getId());
                    JSONObject.put("projectName", projectDTO.getName());
                    JSONObject.put("appServiceId", appServiceDTO.getId());
                    JSONObject.put("appServiceName", appServiceDTO.getName());
                    JSONObject.put("status", "failed");
                    // TODO 发通知
//                    WebHookJsonSendDTO webHookJsonSendDTO = new WebHookJsonSendDTO(
//                            SendSettingEnum.GITLAB_CD_FAILURE.value(),
//                            SendSettingEnum.getEventName(SendSettingEnum.GITLAB_CD_FAILURE.value()),
//                            JSONObject,
//                            new Date(),
//                            getWebHookuser(appServiceDTO.getCreatedBy())
//                    );
//                    sendNotices(NoticeCodeConstants.GITLAB_CONTINUOUS_DELIVERY_FAILURE, projectDTO.getId(), ArrayUtil.singleAsList(constructTargetUser(iamUserDTO.getId())), params, webHookJsonSendDTO);
                },
                ex -> LOGGER.info("Error occurred when sending message about gitlab-pipeline-failure. The exception is {}.", ex));
    }

    @Override
    public void sendWhenCDSuccess(AppServiceDTO appServiceDTO, String pipelineOperatorUserName) {
        doWithTryCatchAndLog(() -> {
            if (appServiceDTO == null) {
                LOGGER.info("Parameter appServiceDTO is null when sending gitlab pipeline success notice");
                return;
            }

            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
            if (projectDTO == null) {
                LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
                return;
            }

            OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            if (organizationDTO == null) {
                LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
                return;
            }
            IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(pipelineOperatorUserName);

            JSONObject JSONObject = new JSONObject();
            JSONObject.put("projectId", projectDTO.getId());
            JSONObject.put("projectName", projectDTO.getName());
            JSONObject.put("appServiceId", appServiceDTO.getId());
            JSONObject.put("appServiceName", appServiceDTO.getName());
            JSONObject.put("status", "success");
            // TODO 发通知
//            WebHookJsonSendDTO webHookJsonSendDTO = new WebHookJsonSendDTO(
//                    SendSettingEnum.GITLAB_CD_SUCCESS.value(),
//                    SendSettingEnum.getEventName(SendSettingEnum.GITLAB_CD_SUCCESS.value()),
//                    JSONObject,
//                    new Date(),
//                    getWebHookuser(appServiceDTO.getCreatedBy())
//            );
//            sendNotices(SendSettingEnum.GITLAB_CD_SUCCESS.value(),
//                    projectDTO.getId(),
//                    ArrayUtil.singleAsList(constructTargetUser(iamUserDTO.getId())),
//                    null,
//                    webHookJsonSendDTO);
        }, ex -> LOGGER.info("Error occurred when sending message about gitlab-pipeline-success. The exception is {}.", ex));
    }

    @Override
    public void sendWhenAppServiceVersion(AppServiceVersionDTO appServiceVersionDTO, AppServiceDTO appServiceDTO, ProjectDTO projectDTO) {
        doWithTryCatchAndLog(() -> {
                    JSONObject JSONObject = new JSONObject();
                    JSONObject.put("projectid", projectDTO.getId());
                    JSONObject.put("projectName", projectDTO.getName());
                    JSONObject.put("appServiceId", appServiceDTO.getId());
                    JSONObject.put("appServiceName", appServiceDTO.getName());
                    JSONObject.put("appServiceVersionId", appServiceVersionDTO.getId());
                    JSONObject.put("version", appServiceVersionDTO.getVersion());
                    // TODO 发通知
//                    WebHookJsonSendDTO webHookJsonSendDTO = new WebHookJsonSendDTO(
//                            SendSettingEnum.CREATE_APPSERVICE_VERSION.value(),
//                            SendSettingEnum.getEventName(SendSettingEnum.CREATE_APPSERVICE_VERSION.value()),
//                            JSONObject,
//                            appServiceVersionDTO.getCreationDate(),
//                            getWebHookuser(appServiceVersionDTO.getCreatedBy()));
//                    sendNotices(
//                            SendSettingEnum.CREATE_APPSERVICE_VERSION.value(),
//                            projectDTO.getId(),
//                            ArrayUtil.singleAsList(constructTargetUser(appServiceVersionDTO.getCreatedBy())),
//                            null,
//                            webHookJsonSendDTO
//                    );
                },
                ex -> LOGGER.info("Error occurred when sending message about appservice-version. The exception is {}.", ex));
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
        params.put("mergeRequestId", String.valueOf(mergeRequestId));
        return params;
    }

    @Override
    public void sendWhenMergeRequestAuditEvent(Integer gitlabProjectId, Long mergeRequestId) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsMergeRequestDTO devopsMergeRequestDTO = devopsMergeRequestService.baseQueryByAppIdAndMergeRequestId(TypeUtil.objToLong(gitlabProjectId), mergeRequestId);
                    if (devopsMergeRequestDTO == null) {
                        LOGGER.info("Merge Request with id {} and gitlab project id {} is null", mergeRequestId, gitlabProjectId);
                        return;
                    }

                    // 如果没有指定审核人，不发送通知
                    if (devopsMergeRequestDTO.getAssigneeId() == null) {
                        LOGGER.info("Abort sending merge request (gitlab project id: {}, gitlab merge request id : {}) audit notification due to the null assigneeId", gitlabProjectId, mergeRequestId);
                        return;
                    }

                    // merge request的发起者
                    UserAttrDTO author = userAttrService.baseQueryByGitlabUserId(devopsMergeRequestDTO.getAuthorId());
                    if (author == null) {
                        LOGGER.info("DevopsUser with gitlab user id {} is null.", devopsMergeRequestDTO.getAuthorId());
                        return;
                    }

                    IamUserDTO authorUser = baseServiceClientOperator.queryUserByUserId(author.getIamUserId());
                    if (authorUser == null) {
                        LogUtil.loggerInfoObjectNullWithId("IamUser", author.getIamUserId(), LOGGER);
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
                        LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, Object> params = makeMergeRequestEventParams(gitlabUrl, organizationDTO.getCode(), projectDTO.getCode(), projectDTO.getName(), appServiceDTO.getCode(), appServiceDTO.getName(), authorUser.getRealName(), mergeRequestId);

                    // TODO 发通知
//                    sendNotices(NoticeCodeConstants.AUDIT_MERGE_REQUEST, projectDTO.getId(), ArrayUtil.singleAsList(constructTargetUser(iamUserDTO.getId())), params, null);
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
    private void doSendWhenMergeRequestClosedOrMerged(String sendSettingCode, Integer gitlabProjectId, Long mergeRequestId, String userIdFromGitlab) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsMergeRequestDTO devopsMergeRequestDTO = devopsMergeRequestService.baseQueryByAppIdAndMergeRequestId(TypeUtil.objToLong(gitlabProjectId), mergeRequestId);
                    if (devopsMergeRequestDTO == null) {
                        LOGGER.info("Merge Request with id {} and gitlab project id {} is null", mergeRequestId, gitlabProjectId);
                        return;
                    }

                    // merge request的操作者
                    IamUserDTO authorUser = baseServiceClientOperator.queryUserByLoginName(TypeUtil.objToString(userIdFromGitlab));
                    if (authorUser == null) {
                        LogUtil.loggerInfoObjectNullWithId("IamUser", TypeUtil.objToLong(userIdFromGitlab), LOGGER);
                        return;
                    }

                    // merge request的接收者
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
                        LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, Object> params = makeMergeRequestEventParams(gitlabUrl, organizationDTO.getCode(), projectDTO.getCode(), projectDTO.getName(), appServiceDTO.getCode(), appServiceDTO.getName(), authorUser.getRealName(), mergeRequestId);

                    // TODO 发通知
//                    sendNotices(sendSettingCode, projectDTO.getId(), ArrayUtil.singleAsList(constructTargetUser(iamUserDTO.getId())), params, null);
                },
                ex -> LOGGER.info("Error occurred when sending message about {}. The exception is {}.", sendSettingCode, ex));
    }

    @Override
    public void sendWhenMergeRequestClosed(Integer gitlabProjectId, Long mergeRequestId, String userLoginName) {
        doSendWhenMergeRequestClosedOrMerged(NoticeCodeConstants.MERGE_REQUEST_CLOSED, gitlabProjectId, mergeRequestId, userLoginName);
    }


    @Override
    public void sendWhenMergeRequestPassed(Integer gitlabProjectId, Long mergeRequestId, String userLoginName) {
        doSendWhenMergeRequestClosedOrMerged(NoticeCodeConstants.MERGE_REQUEST_PASSED, gitlabProjectId, mergeRequestId, userLoginName);
    }

    // TODO 发通知
//    /**
//     * 发送资源创建相关的失败通知
//     *
//     * @param sendSettingCode   通知的code
//     * @param envId             环境的id
//     * @param resourceName      资源的名称
//     * @param creatorId         创建者的id
//     * @param resourceCommandId 资源commandId用于判断资源是否是在创建时失败的
//     */
//    private void doSendWhenResourceCreationFailure(String sendSettingCode, Long envId, String resourceName, Long creatorId, @Nullable Long resourceCommandId, WebHookJsonSendDTO webHookJsonSendDTO) {
//        doWithTryCatchAndLog(() -> {
//            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
//            // 校验资源是否是创建时失败
//            if (resourceCommandId != null) {
//                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(resourceCommandId);
//                if (devopsEnvCommandDTO == null) {
//                    LogUtil.loggerInfoObjectNullWithId("DevOpsEnvCommand", resourceCommandId, LOGGER);
//                    return;
//                } else {
//                    if (!CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
//                        LOGGER.debug("Resource {} with name {} failed after updating instead of creating.", devopsEnvCommandDTO.getObject(), resourceName);
//                        return;
//                    }
//                }
//            }
//
//            if (devopsEnvironmentDTO == null) {
//                LogUtil.loggerInfoObjectNullWithId("Environment", envId, LOGGER);
//                return;
//            }
//
//            // 系统环境的实例失败不发送通知
//            if (EnvironmentType.SYSTEM.getValue().equals(devopsEnvironmentDTO.getType())) {
//                return;
//            }
//
//            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
//            if (projectDTO == null) {
//                LogUtil.loggerInfoObjectNullWithId(PROJECT, devopsEnvironmentDTO.getProjectId(), LOGGER);
//                return;
//            }
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("projectName", Objects.requireNonNull(projectDTO.getName()));
//            params.put("envName", Objects.requireNonNull(devopsEnvironmentDTO.getName()));
//            params.put("resourceName", Objects.requireNonNull(resourceName));
//
//
//            // TODO 发通知
////            sendNotices(sendSettingCode, projectDTO.getId(), ArrayUtil.singleAsList(constructTargetUser(Objects.requireNonNull(creatorId))), params, webHookJsonSendDTO);
//        }, ex -> LOGGER.info("Exception occurred when send failure message about failed resource creation. the message code is {}, env id is {}, resource name is {}, and the ex is: {}", sendSettingCode, envId, resourceName, ex));
//    }

    @Override
    public void sendWhenInstanceCreationFailure(AppServiceInstanceDTO appServiceInstanceDTO, Long creatorId, Long resourceCommandId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceInstanceDTO.getProjectId());
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
        JSONObject JSONObject = getJSONObject(
                appServiceInstanceDTO.getId(),
                appServiceInstanceDTO.getCode(),
                ObjectType.INSTANCE.getType(),
                projectDTO.getId(),
                projectDTO.getName(),
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName()
        );
        // TODO 发通知
//        WebHookJsonSendDTO webHookJsonSendDTO = getWebHookJsonSendDTO(
//                JSONObject,
//                SendSettingEnum.CREATE_RESOURCE_FAILED.value(),
//                creatorId,
//                new Date()
//        );
//        doSendWhenResourceCreationFailure(NoticeCodeConstants.INSTANCE_CREATION_FAILURE, devopsEnvironmentDTO.getId(), appServiceInstanceDTO.getEnvName(), creatorId, resourceCommandId, webHookJsonSendDTO);
    }

    @Override
    public void sendWhenServiceCreationFailure(DevopsServiceDTO devopsServiceDTO, Long creatorId, DevopsEnvironmentDTO devopsEnvironmentDTO, Long resourceCommandId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        JSONObject JSONObject = getJSONObject(devopsServiceDTO.getId(),
                devopsServiceDTO.getName(),
                ObjectType.SERVICE.getType(),
                devopsEnvironmentDTO.getProjectId(),
                projectDTO.getName(),
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName());
        // TODO 发通知
//        WebHookJsonSendDTO webHookJsonSendDTO = getWebHookJsonSendDTO(JSONObject, SendSettingEnum.CREATE_RESOURCE_FAILED.value(), creatorId, new Date());
//        doSendWhenResourceCreationFailure(NoticeCodeConstants.SERVICE_CREATION_FAILURE, devopsEnvironmentDTO.getId(), devopsServiceDTO.getName(), creatorId, resourceCommandId, webHookJsonSendDTO);
    }

    @Override
    public void sendWhenServiceCreationSuccessOrDelete(DevopsServiceDTO devopsServiceDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
                    JSONObject JSONObject = getJSONObject(devopsServiceDTO.getId(),
                            devopsServiceDTO.getName(),
                            ObjectType.SERVICE.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    // TODO 发通知
//                    sendNotices(code, projectDTO.getId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, code, devopsServiceDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));

    }

    @Override
    public void sendWhenInstanceSuccessOrDelete(AppServiceInstanceDTO appServiceInstanceDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceInstanceDTO.getProjectId());
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
                    JSONObject JSONObject = getJSONObject(appServiceInstanceDTO.getId(),
                            appServiceInstanceDTO.getCode(),
                            ObjectType.INSTANCE.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    // TODO 发通知
//                    sendNotices(code, projectDTO.getId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, code, appServiceInstanceDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));

    }

    @Override
    public void sendWhenIngressSuccessOrDelete(DevopsIngressDTO devopsIngressDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsIngressDTO.getProjectId());
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());
                    JSONObject JSONObject = getJSONObject(devopsIngressDTO.getId(),
                            devopsIngressDTO.getName(),
                            ObjectType.INGRESS.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    // TODO 发通知
//                    sendNotices(code, projectDTO.getId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, code, devopsIngressDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));

    }

    @Override
    public void sendWhenCertSuccessOrDelete(CertificationDTO certificationDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(environmentDTO.getProjectId());
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId());
                    JSONObject JSONObject = getJSONObject(certificationDTO.getId(),
                            certificationDTO.getName(),
                            ObjectType.CERTIFICATE.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    // TODO 发通知
//                    sendNotices(code, projectDTO.getId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, code, certificationDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));

    }

    @Override
    public void sendWhenConfigMap(DevopsConfigMapDTO devopsConfigMapDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsConfigMapDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
                    JSONObject JSONObject = getJSONObject(devopsConfigMapDTO.getId(),
                            devopsConfigMapDTO.getName(),
                            ObjectType.CONFIGMAP.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    // TODO 发通知
//                    sendNotices(code, projectDTO.getId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, code, devopsConfigMapDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));

    }

    @Override
    public void sendWhenSecret(DevopsSecretDTO devopsSecretDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsSecretDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
                    JSONObject JSONObject = getJSONObject(devopsSecretDTO.getId(),
                            devopsSecretDTO.getName(),
                            ObjectType.SECRET.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    // TODO 发通知
//                    sendNotices(code, projectDTO.getId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, code, devopsSecretDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));

    }

    @Override
    public void sendWhenIngressCreationFailure(DevopsIngressDTO devopsIngressDTO, Long creatorId, Long resourceCommandId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsIngressDTO.getProjectId());
        JSONObject JSONObject = getJSONObject(
                devopsIngressDTO.getId(),
                devopsIngressDTO.getName(),
                ObjectType.INGRESS.getType(),
                projectDTO.getId(),
                projectDTO.getName(),
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName()
        );
        // TODO 发通知
//        WebHookJsonSendDTO webHookJsonSendDTO = getWebHookJsonSendDTO(JSONObject, SendSettingEnum.CREATE_RESOURCE_FAILED.value(), devopsIngressDTO.getCreatedBy(), new Date());
//        doSendWhenResourceCreationFailure(NoticeCodeConstants.INGRESS_CREATION_FAILURE, devopsEnvironmentDTO.getId(), devopsIngressDTO.getName(), creatorId, resourceCommandId, webHookJsonSendDTO);
    }

    @Override
    public void sendWhenCertificationCreationFailure(CertificationDTO certificationDTO, Long creatorId, Long resourceCommandId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(certificationDTO.getProjectId());
        JSONObject JSONObject = getJSONObject(
                certificationDTO.getId(),
                certificationDTO.getName(),
                ObjectType.CERTIFICATE.getType(),
                projectDTO.getId(),
                projectDTO.getName(),
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName()
        );
        // TODO 发通知
//        WebHookJsonSendDTO webHookJsonSendDTO = getWebHookJsonSendDTO(JSONObject, SendSettingEnum.CREATE_RESOURCE_FAILED.value(), certificationDTO.getCreatedBy(), new Date());
//        doSendWhenResourceCreationFailure(NoticeCodeConstants.CERTIFICATION_CREATION_FAILURE, certificationDTO.getEnvId(), certificationDTO.getName(), creatorId, resourceCommandId, webHookJsonSendDTO);
    }

    @Override
    public void sendForUserDefaultPassword(String userId, String password) {
        doWithTryCatchAndLog(() -> {
                    Long id = TypeUtil.objToLong(userId);
                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(id);
                    if (iamUserDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("user", id, LOGGER);
                        return;
                    }

                    Long organizationId = iamUserDTO.getOrganizationId();
                    if (organizationId == null) {
                        LOGGER.info("The organization is is null of user with id {}", userId);
                        return;
                    }

                    Map<String, Object> params = new HashMap<>();
                    params.put("organizationId", organizationId);
                    params.put("gitlabPassword", Objects.requireNonNull(password));

                    // TODO 发通知
//                    sendNotices(NoticeCodeConstants.GITLAB_PWD, 0L, ArrayUtil.singleAsList(constructTargetUser(iamUserDTO.getId())), params, null);
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenEnvCreate(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizatioinId) {
        JSONObject JSONObject = new JSONObject();
        // TODO envId
        JSONObject.put("enveId", devopsEnvironmentDTO.getId());
        JSONObject.put("envCode", devopsEnvironmentDTO.getCode());
        JSONObject.put("envName", devopsEnvironmentDTO.getName());
        JSONObject.put("clusterId", devopsEnvironmentDTO.getClusterId());
        JSONObject.put("organizationId", organizatioinId);
        doWithTryCatchAndLog(
                () -> {
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.CREATE_ENV.value(), devopsEnvironmentDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.CREATE_ENV.value(), devopsEnvironmentDTO.getCreatedBy(), devopsEnvironmentDTO.getCreationDate()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenEnvEnable(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        JSONObject JSONObject = new JSONObject();
        // TODO envId
        JSONObject.put("enveId", devopsEnvironmentDTO.getId());
        JSONObject.put("envCode", devopsEnvironmentDTO.getCode());
        JSONObject.put("envName", devopsEnvironmentDTO.getName());
        JSONObject.put("clusterId", devopsEnvironmentDTO.getClusterId());
        JSONObject.put("organizationId", organizationId);
        doWithTryCatchAndLog(
                () -> {
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.ENABLE_ENV.value(), devopsEnvironmentDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.ENABLE_ENV.value(), devopsEnvironmentDTO.getCreatedBy(), devopsEnvironmentDTO.getLastUpdateDate()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenEnvDisable(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        JSONObject JSONObject = new JSONObject();
        // TODO envId
        JSONObject.put("enveId", devopsEnvironmentDTO.getId());
        JSONObject.put("envCode", devopsEnvironmentDTO.getCode());
        JSONObject.put("envName", devopsEnvironmentDTO.getName());
        JSONObject.put("clusterId", devopsEnvironmentDTO.getClusterId());
        JSONObject.put("organizationId", organizationId);
        doWithTryCatchAndLog(
                () -> {
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.DISABLE_ENV.value(), devopsEnvironmentDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.DISABLE_ENV.value(), devopsEnvironmentDTO.getCreatedBy(), devopsEnvironmentDTO.getLastUpdateDate()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenEnvDelete(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        JSONObject JSONObject = new JSONObject();
        // TODO envId
        JSONObject.put("enveId", devopsEnvironmentDTO.getId());
        JSONObject.put("envCode", devopsEnvironmentDTO.getCode());
        JSONObject.put("envName", devopsEnvironmentDTO.getName());
        JSONObject.put("clusterId", devopsEnvironmentDTO.getClusterId());
        JSONObject.put("organizationId", organizationId);
        doWithTryCatchAndLog(
                () -> {
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.DELETE_ENV.value(), devopsEnvironmentDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.DELETE_ENV.value(), devopsEnvironmentDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenCreateEnvFailed(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        JSONObject JSONObject = new JSONObject();
        // TODO envId?
        JSONObject.put("enveId", devopsEnvironmentDTO.getId());
        JSONObject.put("envCode", devopsEnvironmentDTO.getCode());
        JSONObject.put("envName", devopsEnvironmentDTO.getName());
        JSONObject.put("clusterId", devopsEnvironmentDTO.getClusterId());
        JSONObject.put("organizationId", organizationId);
        doWithTryCatchAndLog(
                () -> {
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.CREATE_ENVFAILED.value(), devopsEnvironmentDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.CREATE_ENVFAILED.value(), devopsEnvironmentDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenEnvUpdatePermissions(DevopsEnvUserPayload devopsEnvUserPayload, ProjectDTO projectDTO) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvUserPayload.getDevopsEnvironmentDTO();
                    JSONObject JSONObject = new JSONObject();
                    JSONObject.put("enveId", devopsEnvironmentDTO.getId());
                    JSONObject.put("envCode", devopsEnvironmentDTO.getCode());
                    JSONObject.put("envName", devopsEnvironmentDTO.getName());
                    JSONObject.put("clusterId", devopsEnvironmentDTO.getClusterId());
                    JSONObject.put("organizationId", projectDTO.getOrganizationId());
                    List<Long> iamUserIds = devopsEnvUserPayload.getIamUserIds();
                    List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(iamUserIds);
                    // TODO 发通知
//                    List<WebHookJsonSendDTO.User> userList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(iamUserDTOS)) {
                        iamUserDTOS.stream().forEach(iamUserDTO -> {
//                            WebHookJsonSendDTO.User user = new WebHookJsonSendDTO.User(iamUserDTO.getLoginName(), iamUserDTO.getRealName());
//                            userList.add(user);
                        });
                    }
//                    JSONObject.put("users", JSON.toJSONString(userList));
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.UPDATE_ENV_PERMISSIONS.value(), devopsEnvironmentDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.UPDATE_ENV_PERMISSIONS.value(), devopsEnvironmentDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenCreateCluster(DevopsClusterDTO devopsClusterDTO, ProjectDTO iamProject) {
        doWithTryCatchAndLog(
                () -> {
                    JSONObject JSONObject = new JSONObject();
                    JSONObject.put("clusterId", devopsClusterDTO.getId());
                    JSONObject.put("clusterCode", devopsClusterDTO.getCode());
                    JSONObject.put("clusterName", devopsClusterDTO.getName());
                    JSONObject.put("organizationId", iamProject.getOrganizationId());
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.CREATE_CLUSTER.value(), devopsClusterDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.CREATE_CLUSTER.value(), devopsClusterDTO.getCreatedBy(), devopsClusterDTO.getCreationDate()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenCreateClusterFail(DevopsClusterDTO devopsClusterDTO, ProjectDTO iamProject, String msg) {
        doWithTryCatchAndLog(() -> {
                    JSONObject JSONObject = new JSONObject();
                    JSONObject.put("clusterId", devopsClusterDTO.getId());
                    JSONObject.put("clusterCode", devopsClusterDTO.getCode());
                    JSONObject.put("clusterName", devopsClusterDTO.getName());
                    JSONObject.put("organizationId", iamProject.getOrganizationId());
                    JSONObject.put("msg", msg);
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.CREATE_CLUSTERFAILED.value(), devopsClusterDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.CREATE_CLUSTERFAILED.value(), devopsClusterDTO.getCreatedBy(), devopsClusterDTO.getCreationDate()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenPVCResource(DevopsPvcDTO devopsPvcDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsPvcDTO.getProjectId());
                    JSONObject JSONObject = getJSONObject(devopsPvcDTO.getId(),
                            devopsPvcDTO.getName(),
                            ObjectType.PERSISTENTVOLUMECLAIM.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    // TODO 发通知
//                    sendNotices(code, projectDTO.getId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, code, devopsPvcDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    private JSONObject getJSONObject(Long resourceId, String resourceName, String k8sKind, Long projectId, String projectName, Long envId, String envName) {
        JSONObject JSONObject = new JSONObject();
        JSONObject.put("resourceId", resourceId);
        JSONObject.put("resourceName", resourceName);
        JSONObject.put("k8sKind", k8sKind);
        JSONObject.put("projectid", projectId);
        JSONObject.put("projectName", projectName);
        JSONObject.put("envId", envId);
        JSONObject.put("envName", envName);
        return JSONObject;
    }

    @Override
    public void sendWhenActiviteCluster(DevopsClusterDTO devopsClusterDTO) {
        doWithTryCatchAndLog(
                () -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsClusterDTO.getProjectId());
                    if (Objects.isNull(projectDTO)) {
                        return;
                    }
                    JSONObject JSONObject = new JSONObject();
                    JSONObject.put("clusterId", devopsClusterDTO.getId());
                    JSONObject.put("clusterCode", devopsClusterDTO.getCode());
                    JSONObject.put("clusterName", devopsClusterDTO.getName());
                    JSONObject.put("organizationId", projectDTO.getOrganizationId());
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.ACTIVITE_CLUSTER.value(), devopsClusterDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.ACTIVITE_CLUSTER.value(), devopsClusterDTO.getCreatedBy(), devopsClusterDTO.getLastUpdateDate()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }

    @Override
    public void sendWhenDeleteCluster(DevopsClusterDTO devopsClusterDTO) {
        doWithTryCatchAndLog(
                () -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsClusterDTO.getProjectId());
                    if (Objects.isNull(projectDTO)) {
                        return;
                    }
                    JSONObject JSONObject = new JSONObject();
                    JSONObject.put("clusterId", devopsClusterDTO.getId());
                    JSONObject.put("clusterCode", devopsClusterDTO.getCode());
                    JSONObject.put("clusterName", devopsClusterDTO.getName());
                    JSONObject.put("organizationId", projectDTO.getOrganizationId());
                    // TODO 发通知
//                    sendNotices(SendSettingEnum.DELETE_CLUSTER.value(), devopsClusterDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, SendSettingEnum.DELETE_CLUSTER.value(), devopsClusterDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
    }


    @Override
    public void sendWhenResourceInstallFailed(DevopsClusterResourceDTO devopsClusterResourceDTO, String value, String type, Long clusterId, String payload) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
                    if (Objects.isNull(devopsClusterDTO)) {
                        return;
                    }
                    JSONObject JSONObject = new JSONObject();
                    JSONObject.put("resourceId", devopsClusterResourceDTO.getId());
                    JSONObject.put("resourceType", type);
                    JSONObject.put("clusterId", clusterId);
                    JSONObject.put("organizationId", devopsClusterDTO.getOrganizationId());
                    JSONObject.put("msg", payload);
                    // TODO 发通知
//                    sendNotices(value, devopsClusterDTO.getProjectId(),
//                            null,
//                            null,
//                            getWebHookJsonSendDTO(JSONObject, value, devopsClusterResourceDTO.getCreatedBy(), new Date()));
                },
                ex -> LOGGER.info("Error occurred when sending message about user's default password. The exception is {}.", ex));
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

//    private static NoticeSendDTO.User constructTargetUser(Long id) {
//        NoticeSendDTO.User targetUser = new NoticeSendDTO.User();
//        targetUser.setId(id);
//        return targetUser;
//    }

//    private static NoticeSendDTO constructNotice(String sendSettingCode, Long sourceId, List<NoticeSendDTO.User> targetUsers, Map<String, Object> params, WebHookJsonSendDTO webHookJsonSendDTO) {
//        NoticeSendDTO noticeSendDTO = new NoticeSendDTO();
//        noticeSendDTO.setCode(Objects.requireNonNull(sendSettingCode));
//        noticeSendDTO.setSourceId(Objects.requireNonNull(sourceId));
//        noticeSendDTO.setTargetUsers(targetUsers);
//        noticeSendDTO.setParams(params);
//        noticeSendDTO.setNotifyType(NOTIFY_TYPE);
//        noticeSendDTO.setWebHookJsonSendDTO(webHookJsonSendDTO);
//        return noticeSendDTO;
//    }
}
