package io.choerodon.devops.app.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.MessageSender;
import org.hzero.boot.message.entity.Receiver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.enums.MessageAdditionalType;
import io.choerodon.core.enums.ServiceNotifyType;
import io.choerodon.core.enums.TargetUserType;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.api.vo.notify.TargetUserDTO;
import io.choerodon.devops.app.eventhandler.payload.DevopsEnvUserPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.HzeroMessageClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.CiCdPipelineMapper;
import io.choerodon.devops.infra.util.*;

/**
 * 发送DevOps相关通知的实现类
 * 其中数字类型的参数要转成字符串，否则在notify-service中会被转为逗号分隔的形式，如`11,111` (0.20版本)
 *
 * @author zmf
 * @since 12/5/19
 */
@Service
@Async
public class SendNotificationServiceImpl implements SendNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendNotificationServiceImpl.class);
    private static final String PROJECT = "Project";
    private static final String ORGANIZATION = "Organization";
    private static final String WEB_HOOK = "WEB_HOOK";
    private static final String STAGE_NAME = "stageName";
    private static final String STAGE_ID = "stageId";
    private static final String APP_SERVICE = "AppService";
    private static final String ENV_NAME = "envName";
    private static final String IAM_USER = "IamUser";
    private static final String APP_SERVICE_ID = "appServiceId";
    private static final String CURRENT_STATUS = "currentStatus";
    private static final String CLUSTER_ID = "clusterId";

    private static final String ORGANIZATION_ID = "organizationId";

    private static final String PROJECT_ID = "projectId";
    private static final String APP_SERVICE_NAME = "appServiceName";
    private static final String LINK = "link";
    private static final String BASE_URL = "%s/#/devops/pipeline-manage?type=project&id=%s&name=%s&organizationId=%s&pipelineId=%s&pipelineIdRecordId=%s";
    private static final String LOGIN_NAME = "loginName";
    private static final String USER_NAME = "userName";
    private static final String DEPLOY_RESOURCES_URL = "%s/#/devops/resource?type=project&id=%s&name=%s&organizationId=%s&envId=%s&activeKey=resource&itemType=%s";
    private static final String APP_SERVICE_URL = "%s/#/devops/app-service?type=project&id=%s&name=%s&organizationId=%s";
    private static final String MERGE_REQUEST_URL = "%s/#/devops/code-management?type=project&id=%s&name=%s&organizationId=%s&appServiceId=%s";

    private static final String INSTANCE_URL = "%s/#/devops/resource?type=project&id=%s&name=%s&organizationId=%s&envId=%s&activeKey=resource&itemType=instances";

    public static final String ENV_AND_CERTIFICATION_LINK = "%s/#/devops/resource?type=project&id=%s&name=%s&organizationId=%s&searchName=%s&searchId=%s";

    @Value(value = "${services.front.url: http://app.example.com}")
    private String frontUrl;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    @Lazy
    private AppServiceService appServiceService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private DevopsMergeRequestService devopsMergeRequestService;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    @Lazy
    private UserAttrService userAttrService;
    @Autowired
    @Lazy
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    @Lazy
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    @Lazy
    private DevopsClusterService devopsClusterService;
    @Autowired
    private MessageClient messageClient;
    //    @Autowired
//    @Lazy
//    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;
//    @Autowired
//    private DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper;
    @Autowired
    private HzeroMessageClientOperator messageClientOperator;
    @Autowired
    @Lazy
    private DevopsCiPipelineRecordService ciPipelineRecordService;
    @Autowired
    private CiCdPipelineMapper ciCdPipelineMapper;
    @Autowired
    @Lazy
    private DevopsCiPipelineService devopsCiPipelineService;
    @Autowired
    @Lazy
    private PipelineService pipelineService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    /**
     * 发送和应用服务失败、启用和停用的消息(调用此方法时注意在外层捕获异常，此方法不保证无异常抛出)
     *
     * @param appServiceId    应用服务id
     * @param sendSettingCode 消息code
     * @param targetSupplier  转换目标用户
     */
    private void sendNoticeAboutAppService(Long appServiceId,
                                           String sendSettingCode,
                                           List<Receiver> targetUsers) {
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        if (appServiceDTO == null) {
            LogUtil.loggerInfoObjectNullWithId(APP_SERVICE, appServiceId, LOGGER);
            return;
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
        if (projectDTO == null) {
            LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
            return;
        }
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        if (organizationDTO == null) {
            LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
            return;
        }

        LOGGER.debug("AppService notice {}. Target users size: {}", sendSettingCode, targetUsers.size());
        Map<String, String> makeAppServiceParams = makeAppServiceParams(organizationDTO.getTenantId(), projectDTO.getId(), projectDTO.getName(), projectDTO.getCategory(), appServiceDTO);
        makeAppServiceParams.put(LINK, String.format(APP_SERVICE_URL, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId()));
        sendNotices(sendSettingCode, targetUsers, makeAppServiceParams, projectDTO.getId());
    }

    /**
     * 创建，删除应用服务发送消息
     *
     * @param appServiceDTO   应用服务信息
     * @param sendSettingCode 发送的消息code
     * @param targetUsers     接收者
     */
    private void sendNoticeAboutAppService(AppServiceDTO appServiceDTO, String sendSettingCode, List<Receiver> targetUsers) {
        if (appServiceDTO == null) {
            LogUtil.loggerInfoObjectNullWithId(APP_SERVICE, null, LOGGER);
            return;
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
        if (projectDTO == null) {
            LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
            return;
        }
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        if (organizationDTO == null) {
            LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
            return;
        }

        LOGGER.debug("AppService notice {}. Target users size: {}", sendSettingCode, targetUsers.size());
        Map<String, String> stringStringMap = makeAppServiceParams(organizationDTO.getTenantId(), projectDTO.getId(), projectDTO.getName(), projectDTO.getCategory(), appServiceDTO);
        stringStringMap.put(LINK, String.format(APP_SERVICE_URL, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId()));
        sendNotices(sendSettingCode, targetUsers, stringStringMap, projectDTO.getId());
    }

    /**
     * 应用服务相关模板所需要的参数
     *
     * @param organizationId  组织id
     * @param projectId       项目id
     * @param projectName     项目名称
     * @param projectCategory 项目类别
     * @param appServiceDTO   应用服务信息
     * @return 参数映射
     */
    private Map<String, String> makeAppServiceParams(Long organizationId, Long projectId, String projectName, String projectCategory, AppServiceDTO appServiceDTO) {
        return StringMapBuilder.newBuilder()
                .put(ORGANIZATION_ID, organizationId)
                .put(PROJECT_ID, projectId)
                .put(MessageCodeConstants.PROJECT_NAME, projectName)
                .put("projectCategory", projectCategory)
                .put(APP_SERVICE_NAME, appServiceDTO.getName())
                .put("appServerId", appServiceDTO.getId())
                .put("appServerCode", appServiceDTO.getCode())
                .put("appServerName", appServiceDTO.getName())
                .put("appServerType", appServiceDTO.getType())
                .put("enabled", appServiceDTO.getActive())
                .build();
    }


    @Override
    public void sendWhenAppServiceCreate(AppServiceDTO appServiceDTO) {
        doWithTryCatchAndLog(
                () -> sendNoticeAboutAppService(appServiceDTO, SendSettingEnum.CREATE_APPSERVICE.value(),
                        ArrayUtil.singleAsList(constructReceiver(appServiceDTO.getCreatedBy()))),
                ex -> LOGGER.info("Error occurred when sending message about of app-service-create. The exception is ", ex));
    }

    @Override
    public void sendWhenAppServiceFailure(Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (Objects.isNull(appServiceDTO)) {
            LogUtil.loggerWarnObjectNullWithId(APP_SERVICE, appServiceId, LOGGER);
            return;
        }
        doWithTryCatchAndLog(
                () -> sendNoticeAboutAppService(appServiceId, MessageCodeConstants.APP_SERVICE_CREATION_FAILED,
                        ArrayUtil.singleAsList(constructReceiver(appServiceDTO.getCreatedBy()))),
                ex -> LOGGER.info("Error occurred when sending message about failure of app-service. The exception is", ex));
    }

    private static <T> List<T> mapNullListToEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public void sendWhenAppServiceEnabled(Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (Objects.isNull(appServiceDTO)) {
            LogUtil.loggerWarnObjectNullWithId(APP_SERVICE, appServiceId, LOGGER);
            return;
        }
        doWithTryCatchAndLog(
                () -> sendNoticeAboutAppService(appServiceId, MessageCodeConstants.APP_SERVICE_ENABLED, getAppReceivers(appServiceDTO)),
                ex -> LOGGER.info("Error occurred when sending message about app-service-enable. The exception is ", ex));
    }


    @Override
    public void sendWhenAppServiceDisabled(Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (Objects.isNull(appServiceDTO)) {
            LogUtil.loggerWarnObjectNullWithId(APP_SERVICE, appServiceId, LOGGER);
            return;
        }


        doWithTryCatchAndLog(
                () -> sendNoticeAboutAppService(appServiceId,
                        MessageCodeConstants.APP_SERVICE_DISABLE,
                        getAppReceivers(appServiceDTO)),
                ex -> LOGGER.info("Error occurred when sending message about app-service-disable. The exception is ", ex));
    }

    @NotNull
    private List<Receiver> getAppReceivers(AppServiceDTO appServiceDTO) {
        List<MemberDTO> memberDTOS = gitlabServiceClientOperator.listMemberByProject(appServiceDTO.getGitlabProjectId(), null);
        return getReceivers(memberDTOS);
    }

    private List<Receiver> getReceivers(List<MemberDTO> memberDTOS) {
        List<Receiver> targetUsers = new ArrayList<>();
        if (!CollectionUtils.isEmpty(memberDTOS)) {
            Set<Long> guids = memberDTOS.stream().map(m -> m.getId().longValue()).collect(Collectors.toSet());
            List<UserAttrVO> userAttrVOS = userAttrService.listUsersByGitlabUserIds(guids);
            List<Long> iamUserIds = userAttrVOS.stream()
                    .map(UserAttrVO::getIamUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(iamUserIds);
            targetUsers = iamUserDTOS.stream().map(user -> constructReceiver(user.getId(),
                            user.getEmail(),
                            user.getPhone(),
                            user.getOrganizationId()))
                    .collect(Collectors.toList());
        }
        return targetUsers;
    }

    /**
     * 删除数据消息发送同步执行
     */
    @Override
    public void sendWhenAppServiceDelete(List<MemberDTO> memberDTOS, AppServiceDTO appServiceDTO) {
        doWithTryCatchAndLog(
                () -> sendNoticeAboutAppService(appServiceDTO, MessageCodeConstants.DELETE_APP_SERVICE,
                        getReceivers(memberDTOS)),
                ex -> LOGGER.info("Error occurred when sending message about app-service-delete. The exception is ", ex));
    }


    @Override
    public void sendWhenCDFailure(Long gitlabPipelineId, AppServiceDTO appServiceDTO, String pipelineOperatorUserName) {
        doWithTryCatchAndLog(() -> {
                    if (appServiceDTO == null) {
                        LOGGER.info("Parameter appServiceDTO is null when sending gitlab pipeline failure notice");
                        return;
                    }

                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
                    if (projectDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, String> params = StringMapBuilder.newBuilder()
                            .put("gitlabUrl", gitlabUrl)
                            .put("organizationCode", organizationDTO.getTenantNum())
                            .put("projectCode", projectDTO.getCode())
                            .put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName())
                            .put("appServiceCode", appServiceDTO.getCode())
                            .put(APP_SERVICE_NAME, appServiceDTO.getName())
                            .put("gitlabPipelineId", gitlabPipelineId)
                            .put(PROJECT_ID, projectDTO.getId())
                            .put(APP_SERVICE_ID, appServiceDTO.getId())
                            .put("status", "failed")
                            .build();

                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(pipelineOperatorUserName);

                    sendNotices(MessageCodeConstants.GITLAB_CONTINUOUS_DELIVERY_FAILURE, ArrayUtil.singleAsList(constructReceiver(iamUserDTO.getId())), params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message about gitlab-pipeline-failure. The exception is", ex));
    }

    @Override
    public void sendWhenCDSuccess(AppServiceDTO appServiceDTO, String pipelineOperatorUserName) {
        doWithTryCatchAndLog(() -> {
            if (appServiceDTO == null) {
                LOGGER.info("Parameter appServiceDTO is null when sending gitlab pipeline success notice");
                return;
            }

            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
            if (projectDTO == null) {
                LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
                return;
            }

            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            if (organizationDTO == null) {
                LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
                return;
            }
            IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(pipelineOperatorUserName);

            Map<String, String> params = StringMapBuilder.newBuilder()
                    .put(PROJECT_ID, projectDTO.getId())
                    .put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName())
                    .put(APP_SERVICE_ID, appServiceDTO.getId())
                    .put(APP_SERVICE_NAME, appServiceDTO.getName())
                    .put("status", "success")
                    .build();
            sendNotices(SendSettingEnum.GITLAB_CD_SUCCESS.value(),
                    ArrayUtil.singleAsList(constructReceiver(iamUserDTO.getId())),
                    params, projectDTO.getId());
        }, ex -> LOGGER.info("Error occurred when sending message about gitlab-pipeline-success. The exception is", ex));
    }

    @Override
    public void sendWhenAppServiceVersion(AppServiceVersionDTO appServiceVersionDTO, AppServiceDTO appServiceDTO, ProjectDTO projectDTO) {
        doWithTryCatchAndLog(() -> {
                    Map<String, String> params = StringMapBuilder.newBuilder()
                            .put("projectid", projectDTO.getId())
                            .put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName())
                            .put(APP_SERVICE_ID, appServiceDTO.getId())
                            .put(APP_SERVICE_NAME, appServiceDTO.getName())
                            .put("appServiceVersionId", appServiceVersionDTO.getId())
                            .put("version", appServiceVersionDTO.getVersion())
                            .build();

                    sendNotices(
                            SendSettingEnum.CREATE_APPSERVICE_VERSION.value(),
                            WEB_HOOK,
                            params, projectDTO.getId()
                    );
                },
                ex -> LOGGER.info("Error occurred when sending message about appservice-version. The exception is", ex));
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
    private Map<String, String> makeMergeRequestEventParams(String gitlabUrl,
                                                            String organizationCode,
                                                            String projectCode,
                                                            String projectName,
                                                            String appServiceCode,
                                                            String appServiceName,
                                                            String realName,
                                                            Long mergeRequestId) {
        return StringMapBuilder.newBuilder()
                .put("gitlabUrl", gitlabUrl)
                .put("organizationCode", organizationCode)
                .put("projectCode", projectCode)
                .put(MessageCodeConstants.PROJECT_NAME, projectName)
                .put("appServiceCode", appServiceCode)
                .put(APP_SERVICE_NAME, appServiceName)
                .put("realName", realName)
                .put("mergeRequestId", mergeRequestId)
                .build();
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
                        LogUtil.loggerInfoObjectNullWithId(IAM_USER, author.getIamUserId(), LOGGER);
                        return;
                    }

                    UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(devopsMergeRequestDTO.getAssigneeId());
                    if (userAttrDTO == null) {
                        LOGGER.info("DevopsUser with gitlab user id {} is null.", devopsMergeRequestDTO.getAssigneeId());
                        return;
                    }

                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userAttrDTO.getIamUserId());
                    if (iamUserDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(IAM_USER, userAttrDTO.getIamUserId(), LOGGER);
                        return;
                    }

                    AppServiceDTO appServiceDTO = queryAppServiceByGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId));
                    if (appServiceDTO == null) {
                        LOGGER.info("AppService is null with gitlab project id {}", gitlabProjectId);
                        return;
                    }

                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
                    if (projectDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, String> params = makeMergeRequestEventParams(gitlabUrl, organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), projectDTO.getName(), appServiceDTO.getCode(), appServiceDTO.getName(), authorUser.getRealName(), mergeRequestId);
                    params.put(LINK, String.format(MERGE_REQUEST_URL, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), appServiceDTO.getId()));
                    sendNotices(MessageCodeConstants.AUDIT_MERGE_REQUEST, ArrayUtil.singleAsList(constructReceiver(iamUserDTO.getId())), params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message about merge-request-audit. The exception is: ", ex));
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
                        LogUtil.loggerInfoObjectNullWithId(IAM_USER, TypeUtil.objToLong(userIdFromGitlab), LOGGER);
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
                        LogUtil.loggerInfoObjectNullWithId(IAM_USER, userAttrDTO.getIamUserId(), LOGGER);
                        return;
                    }

                    AppServiceDTO appServiceDTO = queryAppServiceByGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId));
                    if (appServiceDTO == null) {
                        LOGGER.info("AppService is null with gitlab project id {}", gitlabProjectId);
                        return;
                    }

                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
                    if (projectDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(PROJECT, appServiceDTO.getProjectId(), LOGGER);
                        return;
                    }

                    Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                    if (organizationDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId(ORGANIZATION, projectDTO.getOrganizationId(), LOGGER);
                        return;
                    }

                    Map<String, String> params = makeMergeRequestEventParams(gitlabUrl, organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), projectDTO.getName(), appServiceDTO.getCode(), appServiceDTO.getName(), authorUser.getRealName(), mergeRequestId);
                    params.put(LINK, String.format(MERGE_REQUEST_URL, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), appServiceDTO.getId()));
                    sendNotices(sendSettingCode, ArrayUtil.singleAsList(constructReceiver(iamUserDTO.getId())), params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message about {}. The exception is {}.", sendSettingCode, ex));
    }

    @Override
    public void sendWhenMergeRequestClosed(Integer gitlabProjectId, Long mergeRequestId, String userLoginName) {
        doSendWhenMergeRequestClosedOrMerged(MessageCodeConstants.MERGE_REQUEST_CLOSED, gitlabProjectId, mergeRequestId, userLoginName);
    }


    @Override
    public void sendWhenMergeRequestPassed(Integer gitlabProjectId, Long mergeRequestId, String userLoginName) {
        doSendWhenMergeRequestClosedOrMerged(MessageCodeConstants.MERGE_REQUEST_PASSED, gitlabProjectId, mergeRequestId, userLoginName);
    }

    /**
     * 发送资源创建相关的失败通知
     *
     * @param sendSettingCode   通知的code
     * @param envId             环境的id
     * @param resourceName      资源的名称
     * @param creatorId         创建者的id
     * @param resourceCommandId 资源commandId用于判断资源是否是在创建时失败的
     */
    private void doSendWhenResourceCreationFailure(String sendSettingCode, Long envId, String resourceName, Long creatorId, @Nullable Long resourceCommandId, Map<String, String> webHookParams) {
        doWithTryCatchAndLog(() -> {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
            // 校验资源是否是创建时失败
            if (resourceCommandId != null) {
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(resourceCommandId);
                if (devopsEnvCommandDTO == null) {
                    LogUtil.loggerInfoObjectNullWithId("DevOpsEnvCommand", resourceCommandId, LOGGER);
                    return;
                } else {
                    if (!CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
                        LOGGER.debug("Resource {} with name {} failed after updating instead of creating.", devopsEnvCommandDTO.getObject(), resourceName);
                        return;
                    }
                }
            }

            if (devopsEnvironmentDTO == null) {
                LogUtil.loggerInfoObjectNullWithId("Environment", envId, LOGGER);
                return;
            }

            // 系统环境的实例失败不发送通知
            if (EnvironmentType.SYSTEM.getValue().equals(devopsEnvironmentDTO.getType())) {
                return;
            }

            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
            if (projectDTO == null) {
                LogUtil.loggerInfoObjectNullWithId(PROJECT, devopsEnvironmentDTO.getProjectId(), LOGGER);
                return;
            }

            String finalResourceName = StringUtils.isEmpty(resourceName) ? devopsEnvironmentDTO.getName() : resourceName;

            Map<String, String> params = StringMapBuilder.newBuilder()
                    .put(MessageCodeConstants.PROJECT_NAME, Objects.requireNonNull(projectDTO.getName()))
                    .put(ENV_NAME, Objects.requireNonNull(devopsEnvironmentDTO.getName()))
                    .put("resourceName", Objects.requireNonNull(finalResourceName))
                    .putAll(webHookParams)
                    .build();

            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(sendSettingCode, MessageCodeConstants.INGRESS_CREATION_FAILURE)) {
                params.put(LINK, String.format(DEPLOY_RESOURCES_URL, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), devopsEnvironmentDTO.getId(), "ingresses"));
            }
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(sendSettingCode, MessageCodeConstants.CERTIFICATION_CREATION_FAILURE)) {
                params.put(LINK, String.format(DEPLOY_RESOURCES_URL, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), devopsEnvironmentDTO.getId(), "certifications"));
            }
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(sendSettingCode, MessageCodeConstants.SERVICE_CREATION_FAILURE)) {
                params.put(LINK, String.format(DEPLOY_RESOURCES_URL, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), devopsEnvironmentDTO.getId(), "services"));
            }


            sendNotices(sendSettingCode, ArrayUtil.singleAsList(constructReceiver(Objects.requireNonNull(creatorId))), params, projectDTO.getId());
        }, ex -> LOGGER.info("Exception occurred when send failure message about failed resource creation. the message code is {}, env id is {}, resource name is {}, and the ex is: {}", sendSettingCode, envId, resourceName, ex));
    }

    @Override
    public void sendWhenInstanceCreationFailure(AppServiceInstanceDTO appServiceInstanceDTO, Long creatorId, Long resourceCommandId) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    Map<String, String> webHookParams = buildResourceParams(
                            appServiceInstanceDTO.getId(),
                            appServiceInstanceDTO.getCode(),
                            ObjectType.INSTANCE.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    doSendWhenResourceCreationFailure(MessageCodeConstants.INSTANCE_CREATION_FAILURE, devopsEnvironmentDTO.getId(), appServiceInstanceDTO.getEnvName(), creatorId, resourceCommandId, webHookParams);
                },
                ex -> LOGGER.info("Failed to send message WhenInstanceCreationFailure.", ex)
        );
    }

    @Override
    public void sendWhenServiceCreationFailure(DevopsServiceDTO devopsServiceDTO, Long creatorId, DevopsEnvironmentDTO devopsEnvironmentDTO, Long resourceCommandId) {
        doWithTryCatchAndLog(
                () -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    Map<String, String> webHookParams = buildResourceParams(devopsServiceDTO.getId(),
                            devopsServiceDTO.getName(),
                            ObjectType.SERVICE.getType(),
                            devopsEnvironmentDTO.getProjectId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName());
                    doSendWhenResourceCreationFailure(MessageCodeConstants.SERVICE_CREATION_FAILURE, devopsEnvironmentDTO.getId(), devopsServiceDTO.getName(), creatorId, resourceCommandId, webHookParams);
                    doSendWhenResourceCreationFailure(SendSettingEnum.CREATE_RESOURCE_FAILED.value(), devopsEnvironmentDTO.getId(), devopsServiceDTO.getName(), creatorId, resourceCommandId, webHookParams);
                },
                ex -> LOGGER.info("Failed to send message WhenServiceCreationFailure.", ex)
        );
    }

    @Override
    public void sendWhenServiceCreationSuccessOrDelete(DevopsServiceDTO devopsServiceDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    Map<String, String> params = buildResourceParams(devopsServiceDTO.getId(),
                            devopsServiceDTO.getName(),
                            ObjectType.SERVICE.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    sendNotices(code, WEB_HOOK, params, devopsEnvironmentDTO.getProjectId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", code, ex));

    }

    @Override
    public void sendWhenInstanceSuccessOrDelete(AppServiceInstanceDTO appServiceInstanceDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    Map<String, String> params = buildResourceParams(appServiceInstanceDTO.getId(),
                            appServiceInstanceDTO.getCode(),
                            ObjectType.INSTANCE.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    sendNotices(code, WEB_HOOK, params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", code, ex));
    }

    @Override
    public void sendWhenIngressSuccessOrDelete(DevopsIngressDTO devopsIngressDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsIngressDTO.getProjectId());
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());
                    Map<String, String> params = buildResourceParams(devopsIngressDTO.getId(),
                            devopsIngressDTO.getName(),
                            ObjectType.INGRESS.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    sendNotices(code, WEB_HOOK, params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", code, ex));

    }

    @Override
    public void sendWhenCertSuccessOrDelete(CertificationDTO certificationDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(environmentDTO.getProjectId());
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId());
                    Map<String, String> params = buildResourceParams(certificationDTO.getId(),
                            certificationDTO.getName(),
                            ObjectType.CERTIFICATE.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    sendNotices(code, WEB_HOOK, params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", code, ex));

    }

    @Override
    public void sendWhenConfigMap(DevopsConfigMapDTO devopsConfigMapDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsConfigMapDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    Map<String, String> params = buildResourceParams(devopsConfigMapDTO.getId(),
                            devopsConfigMapDTO.getName(),
                            ObjectType.CONFIGMAP.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    sendNotices(code, WEB_HOOK, params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", code, ex));

    }

    @Override
    public void sendWhenSecret(DevopsSecretDTO devopsSecretDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsSecretDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    Map<String, String> params = buildResourceParams(devopsSecretDTO.getId(),
                            devopsSecretDTO.getName(),
                            ObjectType.SECRET.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    sendNotices(code, WEB_HOOK, params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", code, ex));

    }

    protected static Receiver constructReceiver(Long userId, String email, String phone, Long userTenantId) {
        Receiver receiver = new Receiver();
        receiver.setUserId(Objects.requireNonNull(userId));
        receiver.setEmail(Objects.requireNonNull(email));
        receiver.setPhone(phone);
        receiver.setTargetUserTenantId(Objects.requireNonNull(userTenantId));
        return receiver;
    }


//    private void sendCdPipelineMessage(Long pipelineRecordId, String type, List<Receiver> users, Map<String, String> params, Long stageId, String stageName) {
//        DevopsCdPipelineRecordDTO recordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(recordDTO.getProjectId());
//        LOGGER.info(">>>>>>>>>>>>>>>> sendCdPipelineMessage >>>>>>>>>>>>>>>>>>>>, DevopsCdPipelineRecordDTO is {}", recordDTO);
//        params.put("pipelineId", KeyDecryptHelper.encryptValueWithoutToken(recordDTO.getPipelineId()));
//        //pipelineRecordId是relID
//        DevopsPipelineRecordRelDTO recordRelDTO = new DevopsPipelineRecordRelDTO();
//        recordRelDTO.setCdPipelineRecordId(recordDTO.getId());
//        DevopsPipelineRecordRelDTO relDTO = devopsPipelineRecordRelMapper.selectOne(recordRelDTO);
//        params.put("pipelineIdRecordId", relDTO.getId().toString());
//        //加上查看详情的url
//        params.put(LINK, String.format(BASE_URL, frontUrl, projectDTO.getId(), projectDTO.getName(),
//                projectDTO.getOrganizationId(), KeyDecryptHelper.encryptValueWithoutToken(recordDTO.getPipelineId()), relDTO.getId().toString()));
//        addSpecifierList(type, projectDTO.getId(), users);
//        sendNotices(type, users, constructCdParamsForPipeline(recordDTO, projectDTO, params, stageId, stageName), projectDTO.getId());
//    }

    private void sendCiPipelineMessage(Long pipelineRecordId, String type, List<Receiver> users, Map<String, String> params, Long stageId, String stageName) {
        DevopsCiPipelineRecordDTO recordDTO = ciPipelineRecordService.queryById(pipelineRecordId);
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(recordDTO.getCiPipelineId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(ciCdPipelineDTO.getProjectId());
        LOGGER.info(">>>>>>>>>>>>>>>> sendCiPipelineMessage >>>>>>>>>>>>>>>>>>>>, DevopsCiPipelineRecordDTO is {}", recordDTO);
        params.put("pipelineId", KeyDecryptHelper.encryptValueWithoutToken(recordDTO.getCiPipelineId()));
        //pipelineRecordId是relID
        DevopsPipelineRecordRelDTO recordRelDTO = new DevopsPipelineRecordRelDTO();
        recordRelDTO.setCiPipelineRecordId(recordDTO.getId());
//        DevopsPipelineRecordRelDTO relDTO = devopsPipelineRecordRelMapper.selectOne(recordRelDTO);
        params.put("pipelineIdRecordId", recordDTO.getId().toString());
        addSpecifierList(type, projectDTO.getId(), users);
        params.put(LINK, String.format(BASE_URL, frontUrl, projectDTO.getId(), projectDTO.getName(),
                projectDTO.getOrganizationId(), recordDTO.getCiPipelineId(), recordDTO.getId().toString()));
        sendNotices(type, users, constructCiParamsForPipeline(ciCdPipelineDTO.getName(), projectDTO, params, stageId, stageName), projectDTO.getId());
    }

    protected void addSpecifierList(String messageCode, Long projectId, List<Receiver> users) {
        if (messageCode.equals(MessageCodeConstants.PIPELINE_FAILED)
                || messageCode.equals(MessageCodeConstants.PIPELINE_SUCCESS)) {
            MessageSettingVO messageSettingVO = messageClientOperator.getMessageSettingVO(ServiceNotifyType.DEVOPS_NOTIFY.getTypeName(), projectId, messageCode);
            List<Long> specifierList = new ArrayList<>();
            if (messageSettingVO != null) {
                Optional<TargetUserDTO> pipelineTriggers = messageSettingVO.getTargetUserDTOS().stream().filter(t -> t.getType().equals(TargetUserType.PIPELINE_TRIGGERS.getTypeName())).findFirst();
                if (!pipelineTriggers.isPresent()) {
                    users.clear();
                }
                List<Long> userIds = users.stream().map(Receiver::getUserId).collect(Collectors.toList());
                messageSettingVO.getTargetUserDTOS().forEach(t -> {
                    if (t.getType().equals(TargetUserType.SPECIFIER.getTypeName()) && !userIds.contains(t.getUserId())) {
                        specifierList.add(t.getUserId());
                    }
                });
            }
            baseServiceClientOperator.listUsersByIds(specifierList).forEach(t -> users.add(constructReceiver(t.getId(), t.getEmail(), t.getPhone(), t.getOrganizationId())));
        }
    }

    protected Map<String, String> constructCiParamsForPipeline(String pipelineName, ProjectDTO projectDTO, @Nullable Map<?, ?> params, Long stageId, String stageName) {
        return StringMapBuilder.newBuilder()
                .put(MessageCodeConstants.PIPE_LINE_NAME, pipelineName)
                .put(PROJECT_ID, projectDTO.getId())
                .put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName())
                .put(ORGANIZATION_ID, projectDTO.getOrganizationId())
                .put(STAGE_ID, stageId)
                .put(STAGE_NAME, stageName)
                .putAll(params)
                .build();
    }

    @Override
    public void sendWhenIngressCreationFailure(DevopsIngressDTO devopsIngressDTO, Long creatorId, Long resourceCommandId) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsIngressDTO.getProjectId());
                    Map<String, String> params = buildResourceParams(
                            devopsIngressDTO.getId(),
                            devopsIngressDTO.getName(),
                            ObjectType.INGRESS.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    doSendWhenResourceCreationFailure(MessageCodeConstants.INGRESS_CREATION_FAILURE, devopsEnvironmentDTO.getId(), devopsIngressDTO.getName(), creatorId, resourceCommandId, params);
                    doSendWhenResourceCreationFailure(SendSettingEnum.CREATE_RESOURCE_FAILED.value(), devopsEnvironmentDTO.getId(), devopsIngressDTO.getName(), creatorId, resourceCommandId, params);
                },
                ex -> LOGGER.info("Failed to sendWhenIngressCreationFailure", ex));
    }

    @Override
    public void sendWhenCertificationCreationFailure(CertificationDTO certificationDTO, Long creatorId, Long resourceCommandId) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(certificationDTO.getProjectId());
                    Map<String, String> params = buildResourceParams(
                            certificationDTO.getId(),
                            certificationDTO.getName(),
                            ObjectType.CERTIFICATE.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    doSendWhenResourceCreationFailure(MessageCodeConstants.CERTIFICATION_CREATION_FAILURE, certificationDTO.getEnvId(), certificationDTO.getName(), creatorId, resourceCommandId, params);
                    doSendWhenResourceCreationFailure(SendSettingEnum.CREATE_RESOURCE_FAILED.value(), certificationDTO.getEnvId(), certificationDTO.getName(), creatorId, resourceCommandId, params);
                },
                ex -> LOGGER.info("Failed to sendWhenCertificationCreationFailure", ex));
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

                    Map<String, String> params = StringMapBuilder.newBuilder()
                            .put(ORGANIZATION_ID, organizationId)
                            .put("gitlabPassword", Objects.requireNonNull(password))
                            .build();

                    sendOrganizationNotices(MessageCodeConstants.GITLAB_PSW, ArrayUtil.singleAsList(constructReceiver(iamUserDTO.getId())), params, organizationId);
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", MessageCodeConstants.GITLAB_PSW, ex));
    }

    private static Map<String, String> constructParamsForEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        return StringMapBuilder.newBuilder()
                // 不是打错，模板就是这样
                .put("envId", devopsEnvironmentDTO.getId())
                .put("envCode", devopsEnvironmentDTO.getCode())
                .put(ENV_NAME, devopsEnvironmentDTO.getName())
                .put(CLUSTER_ID, devopsEnvironmentDTO.getClusterId())
                .put(ORGANIZATION_ID, organizationId)
                .build();
    }

    @Override
    public void sendWhenEnvCreate(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        doWithTryCatchAndLog(
                () -> sendNotices(SendSettingEnum.CREATE_ENV.value(), WEB_HOOK, constructParamsForEnv(devopsEnvironmentDTO, organizationId), devopsEnvironmentDTO.getProjectId()),
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.CREATE_ENV.value(), ex));
    }

    @Override
    public void sendWhenEnvEnable(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        doWithTryCatchAndLog(
                () -> sendNotices(SendSettingEnum.ENABLE_ENV.value(), WEB_HOOK, constructParamsForEnv(devopsEnvironmentDTO, organizationId), devopsEnvironmentDTO.getProjectId()),
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.ENABLE_ENV.value(), ex));
    }

    @Override
    public void sendWhenEnvDisable(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        doWithTryCatchAndLog(
                () -> sendNotices(SendSettingEnum.DISABLE_ENV.value(), WEB_HOOK, constructParamsForEnv(devopsEnvironmentDTO, organizationId), devopsEnvironmentDTO.getProjectId()),
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.DISABLE_ENV.value(), ex));
    }

    @Override
    public void sendWhenEnvDelete(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        doWithTryCatchAndLog(
                () -> sendNotices(SendSettingEnum.DELETE_ENV.value(), WEB_HOOK, constructParamsForEnv(devopsEnvironmentDTO, organizationId), devopsEnvironmentDTO.getProjectId()),
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.DELETE_ENV.value(), ex));
    }

    @Override
    public void sendWhenCreateEnvFailed(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId) {
        doWithTryCatchAndLog(
                () -> sendNotices(SendSettingEnum.CREATE_ENVFAILED.value(),
                        WEB_HOOK,
                        constructParamsForEnv(devopsEnvironmentDTO, organizationId), devopsEnvironmentDTO.getProjectId()),
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.CREATE_ENVFAILED.value(), ex));
    }

    @Override
    public void sendWhenEnvUpdatePermissions(DevopsEnvUserPayload devopsEnvUserPayload, ProjectDTO projectDTO) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvUserPayload.getDevopsEnvironmentDTO();
                    if (devopsEnvironmentDTO == null) {
                        devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(Objects.requireNonNull(devopsEnvUserPayload.getEnvId()));
                        if (devopsEnvironmentDTO == null) {
                            return;
                        }
                    }
                    Map<String, String> params = constructParamsForEnv(devopsEnvironmentDTO, projectDTO.getOrganizationId());
                    List<Long> iamUserIds = devopsEnvUserPayload.getIamUserIds();
                    List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(iamUserIds);
                    List<Receiver> userList = iamUserDTOS.stream().map(iamUserDTO -> constructReceiver(iamUserDTO.getId())).collect(Collectors.toList());
                    params.put("users", JSONObject.toJSONString(userList));
                    sendNotices(SendSettingEnum.UPDATE_ENV_PERMISSIONS.value(), WEB_HOOK, params, devopsEnvironmentDTO.getProjectId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.UPDATE_ENV_PERMISSIONS.value(), ex));
    }

    private Map<String, String> constructParamsForCluster(DevopsClusterDTO devopsClusterDTO, Long organizationId, @Nullable String message) {
        return StringMapBuilder.newBuilder()
                .put(CLUSTER_ID, devopsClusterDTO.getId())
                .put("clusterCode", devopsClusterDTO.getCode())
                .put("clusterName", devopsClusterDTO.getName())
                .put(ORGANIZATION_ID, organizationId)
                .put("msg", message)
                .build();
    }

    @Override
    public void sendWhenCreateCluster(DevopsClusterDTO devopsClusterDTO, ProjectDTO iamProject) {
        doWithTryCatchAndLog(
                () -> sendNotices(SendSettingEnum.CREATE_CLUSTER.value(), WEB_HOOK, constructParamsForCluster(devopsClusterDTO, iamProject.getOrganizationId(), null), devopsClusterDTO.getProjectId()),
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.CREATE_CLUSTER.value(), ex));
    }

    @Override
    public void sendWhenCreateClusterFail(DevopsClusterDTO devopsClusterDTO, ProjectDTO iamProject, String msg) {
        doWithTryCatchAndLog(() -> sendNotices(SendSettingEnum.CREATE_CLUSTERFAILED.value(), WEB_HOOK, constructParamsForCluster(devopsClusterDTO, iamProject.getOrganizationId(), msg), iamProject.getId()),
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.CREATE_CLUSTERFAILED.value(), ex));
    }

    @Override
    public void sendWhenPVCResource(DevopsPvcDTO devopsPvcDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, String code) {
        doWithTryCatchAndLog(() -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsPvcDTO.getProjectId());
                    Map<String, String> params = buildResourceParams(devopsPvcDTO.getId(),
                            devopsPvcDTO.getName(),
                            ObjectType.PERSISTENTVOLUMECLAIM.getType(),
                            projectDTO.getId(),
                            projectDTO.getName(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getName()
                    );
                    sendNotices(code, WEB_HOOK, params, projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", code, ex));
    }

    protected Map<String, String> buildResourceParams(Long resourceId, String resourceName, String k8sKind, Long projectId, String projectName, Long envId, String envName) {
        return StringMapBuilder.newBuilder()
                .put("resourceId", resourceId)
                .put("resourceName", resourceName)
                .put("k8sKind", k8sKind)
                .put(PROJECT_ID, projectId)
                .put(MessageCodeConstants.PROJECT_NAME, projectName)
                .put("envId", envId)
                .put(ENV_NAME, envName)
                .build();
    }

    @Override
    public void sendWhenActivateCluster(DevopsClusterDTO devopsClusterDTO) {
        doWithTryCatchAndLog(
                () -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsClusterDTO.getProjectId());
                    if (Objects.isNull(projectDTO)) {
                        return;
                    }
                    sendNotices(SendSettingEnum.ACTIVATE_CLUSTER.value(), WEB_HOOK, constructParamsForCluster(devopsClusterDTO, projectDTO.getOrganizationId(), null), projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.ACTIVATE_CLUSTER, ex));
    }

    @Override
    public void sendWhenDeleteCluster(DevopsClusterDTO devopsClusterDTO) {
        doWithTryCatchAndLog(
                () -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsClusterDTO.getProjectId());
                    if (Objects.isNull(projectDTO)) {
                        return;
                    }
                    sendNotices(SendSettingEnum.DELETE_CLUSTER.value(), WEB_HOOK, constructParamsForCluster(devopsClusterDTO, projectDTO.getOrganizationId(), null), projectDTO.getId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", SendSettingEnum.DELETE_CLUSTER.value(), ex));
    }


    @Override
    public void sendWhenResourceInstallFailed(DevopsClusterResourceDTO devopsClusterResourceDTO, String value, String type, Long clusterId, String payload) {
        doWithTryCatchAndLog(
                () -> {
                    DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
                    if (Objects.isNull(devopsClusterDTO)) {
                        return;
                    }
                    Map<String, String> params = StringMapBuilder.newBuilder()
                            .put("resourceId", devopsClusterResourceDTO.getId())
                            .put("resourceType", type)
                            .put(CLUSTER_ID, clusterId)
                            .put(ORGANIZATION_ID, devopsClusterDTO.getOrganizationId())
                            .put("msg", payload)
                            .build();
                    sendNotices(value, WEB_HOOK, params, devopsClusterDTO.getProjectId());
                },
                ex -> LOGGER.info("Error occurred when sending message {}. The exception is {}.", value, ex));
    }


    /**
     * 保证在执行逻辑时不抛出异常的包装方法
     *
     * @param actionInTry   正常处理的逻辑
     * @param actionInCatch 处理异常的逻辑
     */
    protected static void doWithTryCatchAndLog(Runnable actionInTry, Consumer<Exception> actionInCatch) {
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

    @Override
    public void sendCiPipelineAuditResultMassage(String type, Long ciPipelineId, List<Long> userIds, Long pipelineRecordId, String stageName, Long userId, Long projectId) {
        doWithTryCatchAndLog(
                () -> {
                    List<IamUserDTO> users = baseServiceClientOperator.queryUsersByUserIds(userIds);
                    CiCdPipelineDTO ciCdPipelineDTO = devopsCiPipelineService.baseQueryById(ciPipelineId);
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

                    List<Receiver> userList = new ArrayList<>();
                    users.forEach(t -> userList.add(constructReceiver(t.getId(), t.getEmail(), t.getPhone(), t.getOrganizationId())));

                    Map<String, String> params = new HashMap<>();
                    params.put(STAGE_NAME, stageName);
                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);
                    params.put("auditName", iamUserDTO.getLoginName());
                    params.put("realName", iamUserDTO.getRealName());
                    params.put("pipelineId", ciPipelineId.toString());
                    params.put("pipelineIdRecordId", pipelineRecordId.toString());
                    //加上查看详情的url
                    params.put(LINK, String.format(BASE_URL, frontUrl, projectDTO.getId(), projectDTO.getName(),
                            projectDTO.getOrganizationId(), ciPipelineId.toString(), pipelineRecordId.toString()));
                    addSpecifierList(type, projectDTO.getId(), userList);
                    sendNotices(type, userList, constructParamsForPipeline(ciCdPipelineDTO, projectDTO, params, stageName), projectDTO.getId());
                },
                ex -> LOGGER.info("Failed to sendPipelineAuditMassage.", ex)
        );
    }

    @Override
    public void sendPipelineAuditResultMassage(String type, Long pipelineId, List<Long> userIds, Long pipelineRecordId, String stageName, Long userId, Long projectId) {
        doWithTryCatchAndLog(
                () -> {
                    List<IamUserDTO> users = baseServiceClientOperator.queryUsersByUserIds(userIds);
                    PipelineDTO pipelineDTO = pipelineService.baseQueryById(pipelineId);
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

                    List<Receiver> userList = new ArrayList<>();
                    users.forEach(t -> userList.add(constructReceiver(t.getId(), t.getEmail(), t.getPhone(), t.getOrganizationId())));

                    Map<String, String> params = new HashMap<>();
                    params.put(STAGE_NAME, stageName);
                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);
                    params.put("auditName", iamUserDTO.getLoginName());
                    params.put("realName", iamUserDTO.getRealName());
                    params.put("pipelineId", pipelineId.toString());
                    params.put("pipelineIdRecordId", pipelineRecordId.toString());
                    params.put(MessageCodeConstants.LINK,
                            String.format(MessageCodeConstants.BASE_URL,
                                    frontUrl,
                                    pipelineId,
                                    projectDTO.getId(),
                                    projectDTO.getName(),
                                    projectDTO.getOrganizationId()));
                    //加上查看详情的url
//                    params.put(LINK, String.format(BASE_URL, frontUrl, projectDTO.getId(), projectDTO.getName(),
//                            projectDTO.getOrganizationId(), ciPipelineId.toString(), pipelineRecordId.toString()));
                    addSpecifierList(type, projectDTO.getId(), userList);

                    Map<String, String> newParams = StringMapBuilder.newBuilder()
                            .put(MessageCodeConstants.PIPE_LINE_NAME, pipelineDTO.getName())
                            .put(PROJECT_ID, projectDTO.getId())
                            .put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName())
                            .put(ORGANIZATION_ID, projectDTO.getOrganizationId())
                            .put(STAGE_NAME, stageName)
                            .putAll(params)
                            .build();

                    sendNotices(type, userList, newParams, projectDTO.getId());
                },
                ex -> LOGGER.info("Failed to sendPipelineAuditMassage.", ex)
        );
    }

    private Map<String, String> constructParamsForPipeline(CiCdPipelineDTO ciCdPipelineDTO, ProjectDTO projectDTO, Map<String, String> params, String stageName) {
        return StringMapBuilder.newBuilder()
                .put(MessageCodeConstants.PIPE_LINE_NAME, ciCdPipelineDTO.getName())
                .put(PROJECT_ID, projectDTO.getId())
                .put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName())
                .put(ORGANIZATION_ID, projectDTO.getOrganizationId())
                .put(STAGE_NAME, stageName)
                .putAll(params)
                .build();
    }

//    @Override
//    public void sendCdPipelineNotice(Long pipelineRecordId, String type, Long userId, String email, HashMap<String, String> params) {
//        doWithTryCatchAndLog(
//                () -> {
//                    String actualEmail = email;
//                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);
//                    if (iamUserDTO == null) {
//                        LogUtil.loggerInfoObjectNullWithId("User", userId, LOGGER);
//                        return;
//                    }
//                    if (actualEmail == null) {
//                        actualEmail = iamUserDTO.getEmail();
//
//                    }
//                    sendCdPipelineMessage(pipelineRecordId, type, ArrayUtil.singleAsList(constructReceiver(userId, actualEmail, iamUserDTO.getPhone(), iamUserDTO.getOrganizationId())), params, null, null);
//                },
//                ex -> LOGGER.info("Failed to sendPipelineNotice  with email", ex));
//    }

    @Override
    public void sendCiPipelineNotice(Long pipelineRecordId, String type, Long userId, String email, HashMap<String, String> params) {
        doWithTryCatchAndLog(
                () -> {
                    String actualEmail = email;
                    IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);
                    if (iamUserDTO == null) {
                        LogUtil.loggerInfoObjectNullWithId("User", userId, LOGGER);
                        return;
                    }
                    if (actualEmail == null) {
                        actualEmail = iamUserDTO.getEmail();

                    }
                    sendCiPipelineMessage(pipelineRecordId, type, ArrayUtil.singleAsList(constructReceiver(userId, actualEmail, iamUserDTO.getPhone(), iamUserDTO.getOrganizationId())), params, null, null);
                },
                ex -> LOGGER.info("Failed to sendPipelineNotice  with email", ex));
    }

//    @Override
//    public void sendCdPipelineNotice(Long pipelineRecordId, String type, List<Receiver> receivers, @Nullable Map<String, String> params) {
//        doWithTryCatchAndLog(
//                () -> sendCdPipelineMessage(pipelineRecordId, type, receivers, params, null, null),
//                ex -> LOGGER.info("Failed to sendPipelineNotice ", ex)
//        );
//    }

    @Override
    public void sendCiPipelineAuditMessage(Long ciPipelineId, Long ciPipelineRecordId, String stage, List<Long> userIds) {
        List<Receiver> userList = new ArrayList<>();
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
        Map<Long, IamUserDTO> userDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));

        userIds.forEach(id -> {
            IamUserDTO iamUserDTO = userDTOMap.get(id);
            if (iamUserDTO != null) {
                Receiver user = new Receiver();
                user.setEmail(iamUserDTO.getEmail());
                user.setUserId(iamUserDTO.getId());
                user.setPhone(iamUserDTO.getPhone());
                user.setTargetUserTenantId(iamUserDTO.getOrganizationId());
                userList.add(user);
            }
        });

        CiCdPipelineDTO ciCdPipelineDTO = devopsCiPipelineService.baseQueryById(ciPipelineId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(ciCdPipelineDTO.getProjectId());

        HashMap<String, String> params = new HashMap<>();
        params.put(MessageCodeConstants.PROJECT_ID, projectDTO.getId().toString());
        params.put(MessageCodeConstants.ORGANIZATION_ID, projectDTO.getOrganizationId().toString());
        params.put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName());
        params.put(MessageCodeConstants.PIPE_LINE_NAME, ciCdPipelineDTO.getName());
        params.put(MessageCodeConstants.STAGE_NAME, stage);
        params.put(MessageCodeConstants.REL_ID, ciPipelineRecordId.toString());
        params.put(MessageCodeConstants.PIPELINE_ID, ciPipelineId.toString());
        params.put(MessageCodeConstants.LINK,
                String.format(MessageCodeConstants.BASE_URL,
                        frontUrl,
                        projectDTO.getId(),
                        projectDTO.getName(),
                        projectDTO.getOrganizationId(),
                        ciPipelineId.toString(),
                        ciPipelineRecordId.toString()));

        sendNotices(MessageCodeConstants.PIPELINE_AUDIT, userList, params, projectDTO.getId());
    }

    @Override
    public void sendPipelineAuditMessage(Long pipelineId, Long pipelineRecordId, String stage, List<Long> userIds) {
        List<Receiver> userList = new ArrayList<>();
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
        Map<Long, IamUserDTO> userDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));

        userIds.forEach(id -> {
            IamUserDTO iamUserDTO = userDTOMap.get(id);
            if (iamUserDTO != null) {
                Receiver user = new Receiver();
                user.setEmail(iamUserDTO.getEmail());
                user.setUserId(iamUserDTO.getId());
                user.setPhone(iamUserDTO.getPhone());
                user.setTargetUserTenantId(iamUserDTO.getOrganizationId());
                userList.add(user);
            }
        });

        PipelineDTO pipelineDTO = pipelineService.baseQueryById(pipelineId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(pipelineDTO.getProjectId());

        HashMap<String, String> params = new HashMap<>();
        params.put(MessageCodeConstants.PROJECT_ID, projectDTO.getId().toString());
        params.put(MessageCodeConstants.ORGANIZATION_ID, projectDTO.getOrganizationId().toString());
        params.put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName());
        params.put(MessageCodeConstants.PIPE_LINE_NAME, pipelineDTO.getName());
        params.put(MessageCodeConstants.STAGE_NAME, stage);
        params.put(MessageCodeConstants.REL_ID, pipelineRecordId.toString());
        params.put(MessageCodeConstants.PIPELINE_ID, pipelineId.toString());
        params.put(MessageCodeConstants.LINK,
                String.format(MessageCodeConstants.BASE_URL,
                        frontUrl,
                        pipelineId,
                        projectDTO.getId(),
                        projectDTO.getName(),
                        projectDTO.getOrganizationId()));

        sendNotices(MessageCodeConstants.CD_PIPELINE_AUDIT, userList, params, projectDTO.getId());
    }

    @Override
    public void sendInstanceStatusUpdate(AppServiceInstanceDTO appServiceInstanceDTO, DevopsEnvCommandDTO devopsEnvCommandDTO, String currentStatus) {
        doWithTryCatchAndLog(
                () -> {
                    String code = "";
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    List<Receiver> receivers = new ArrayList<>();
                    Map<String, String> webHookParams = StringMapBuilder.newBuilder()
                            .put("createdAt", LocalDateTime.now())
                            .put(MessageCodeConstants.PROJECT_NAME, projectDTO.getName())
                            .put(ENV_NAME, devopsEnvironmentDTO.getName())
                            .put("instanceId", appServiceInstanceDTO.getId())
                            .put("instanceName", appServiceInstanceDTO.getCode())
                            .put("envId", devopsEnvironmentDTO.getId()).build();
                    switch (CommandType.valueOf(devopsEnvCommandDTO.getCommandType().toUpperCase())) {
                        case CREATE:
                            webHookParams.put(CURRENT_STATUS, currentStatus);
                            webHookParams.put("deployVersion", appServiceInstanceDTO.getAppServiceVersion());
                            //成功
                            if (InstanceStatus.FAILED != InstanceStatus.valueOf(currentStatus.toUpperCase())) {
                                code = MessageCodeConstants.CREATE_INSTANCE_SUCCESS;
                            }
                            //失败
                            if (InstanceStatus.FAILED == InstanceStatus.valueOf(currentStatus.toUpperCase())) {
                                code = MessageCodeConstants.CREATE_INSTANCE_FAIL;
                                //
                                webHookParams.put(LINK, String.format(INSTANCE_URL, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), devopsEnvironmentDTO.getId()));
                                //实例部署失败还有站内信和邮件
                                receivers = ArrayUtil.singleAsList(constructReceiver(Objects.requireNonNull(appServiceInstanceDTO.getCreatedBy())));
                                sendNotices(SendSettingEnum.CREATE_RESOURCE_FAILED.value(), receivers, webHookParams, projectDTO.getId());
                            }
                            break;
                        case UPDATE:
                            webHookParams.put(CURRENT_STATUS, currentStatus);
                            webHookParams.put("deployVersion", appServiceInstanceDTO.getAppServiceVersion());
                            //成功
                            if (InstanceStatus.FAILED != InstanceStatus.valueOf(currentStatus.toUpperCase())) {
                                code = MessageCodeConstants.UPDATE_INSTANCE_SUCCESS;
                            }
                            //失败
                            if (InstanceStatus.FAILED == InstanceStatus.valueOf(currentStatus.toUpperCase())) {
                                code = MessageCodeConstants.UPDATE_INSTANCE_FAIL;
                            }
                            break;
                        case STOP:
                            webHookParams.put(CURRENT_STATUS, currentStatus);
                            code = MessageCodeConstants.STOP_INSTANCE;
                            break;
                        case RESTART:
                            webHookParams.put(CURRENT_STATUS, currentStatus);
                            code = MessageCodeConstants.ENABLE_INSTANCE;
                            break;
                        default:
                    }
                    webHookParams.put("objectKind", code);
                    webHookParams.put("eventName", code);
                    sendNotices(code, receivers, webHookParams, projectDTO.getId());
                },
                ex -> LOGGER.info("Failed to send message WhenInstanceStatusUpdate.", ex)
        );
    }

    @Override
    public void sendApiTestWarningMessage(Set<Long> userIds, Map<String, String> params, Long projectId) {
        doWithTryCatchAndLog(
                () -> {
                    List<Receiver> receivers = new ArrayList<>();
                    userIds.forEach(userId -> receivers.add(constructReceiver(userId)));

                    sendNotices(MessageCodeConstants.PIPELINE_API_TEST_WARNING, receivers, params, projectId);
                },
                ex -> LOGGER.info("Failed to sendPipelineNotice  with email", ex));
    }

    @Override
    public void sendApiTestSuiteWarningMessage(Set<Long> userIds, Map<String, String> params, Long projectId) {
        doWithTryCatchAndLog(
                () -> {
                    List<Receiver> receivers = new ArrayList<>();
                    userIds.forEach(userId -> receivers.add(constructReceiver(userId)));

                    sendNotices(MessageCodeConstants.PIPELINE_API_SUITE_WARNING, receivers, params, projectId);
                },
                ex -> LOGGER.info("Failed to sendPipelineNotice  with email", ex));
    }

    @Override
    public void sendEnvDeploySuccessMessage(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        doWithTryCatchAndLog(
                () -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listProjectOwnerByProjectId(devopsEnvironmentDTO.getProjectId());
                    List<Receiver> receivers = constructReceivers(iamUserDTOS);

                    Map<String, String> params = new HashMap<>();
                    String link = String.format(ENV_AND_CERTIFICATION_LINK, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), devopsEnvironmentDTO.getName(), devopsEnvironmentDTO.getId());
                    params.put("projectName", projectDTO.getName());
                    params.put("projectId", projectDTO.getId().toString());
                    params.put("organizationId", projectDTO.getOrganizationId().toString());
                    params.put("searchId", devopsEnvironmentDTO.getId().toString());
                    params.put("searchName", devopsEnvironmentDTO.getName());
                    params.put("envName", devopsEnvironmentDTO.getName());
                    params.put("link", link);

                    sendNotices(MessageCodeConstants.ENV_DEPLOY_SUCCESS, receivers, params, projectDTO.getId());
                },
                ex -> LOGGER.info("Failed to send env deploy success message", ex));
    }


    @Override
    public void sendEnvDeployFailMessage(DevopsEnvironmentDTO devopsEnvironmentDTO, String errorMsg) {
        doWithTryCatchAndLog(
                () -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
                    List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listProjectOwnerByProjectId(devopsEnvironmentDTO.getProjectId());
                    List<Receiver> receivers = constructReceivers(iamUserDTOS);

                    Map<String, String> params = new HashMap<>();
                    String link = String.format(ENV_AND_CERTIFICATION_LINK, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), devopsEnvironmentDTO.getName(), devopsEnvironmentDTO.getId());
                    params.put("projectName", projectDTO.getName());
                    params.put("projectId", projectDTO.getId().toString());
                    params.put("organizationId", projectDTO.getOrganizationId().toString());
                    params.put("searchId", devopsEnvironmentDTO.getId().toString());
                    params.put("searchName", devopsEnvironmentDTO.getName());
                    params.put("envName", devopsEnvironmentDTO.getName());
                    params.put("link", link);
                    params.put("errorMsg", errorMsg);

                    sendNotices(MessageCodeConstants.ENV_DEPLOY_FAIL, receivers, params, projectDTO.getId());
                },
                ex -> LOGGER.info("Failed to send env deploy fail message", ex));
    }

    @Override
    public void sendCertificationExpireNotice(List<Receiver> receivers, Map<String, String> params, Long projectId) {
        doWithTryCatchAndLog(
                () -> {
                    sendNotices(MessageCodeConstants.CERTIFICATION_EXPIRE, receivers, params, projectId);
                },
                ex -> LOGGER.info("Failed to send env deploy fail message", ex));
    }

    private List<Receiver> constructReceivers(List<IamUserDTO> iamUserDTOS) {
        List<Receiver> receivers = new ArrayList<>();
        iamUserDTOS.forEach(iamUserDTO -> {
            receivers.add(constructReceiver(iamUserDTO.getId(), iamUserDTO.getEmail(), iamUserDTO.getPhone(), iamUserDTO.getOrganizationId()));
        });
        return receivers;
    }

    private Receiver constructReceiver(Long userId) {
        IamUserDTO user = baseServiceClientOperator.queryUserByUserId(userId);
        return constructReceiver(userId, user.getEmail(), user.getPhone(), user.getOrganizationId());
    }

    protected static MessageSender constructMessageSender(String sendSettingCode, List<Receiver> targetUsers, String receiveType, Map<String, String> params, Map<String, Object> addition, Long projectId) {
        MessageSender messageSender = new MessageSender();
        messageSender.setTenantId(0L);
        messageSender.setReceiverAddressList(targetUsers);
        messageSender.setReceiverTypeCode(receiveType);
        messageSender.setArgs(params);
        messageSender.setMessageCode(sendSettingCode);
        if (addition == null) {
            addition = new HashMap<>();
        }
        addition.putIfAbsent(MessageAdditionalType.PARAM_PROJECT_ID.getTypeName(), Objects.requireNonNull(projectId));
        messageSender.setAdditionalInformation(addition);
        return messageSender;
    }

    private static MessageSender constructOrganizationMessageSender(String sendSettingCode, List<Receiver> targetUsers, String receiveType, Map<String, String> params, Map<String, Object> addition, Long organizationId) {
        MessageSender messageSender = new MessageSender();
        messageSender.setTenantId(0L);
        messageSender.setReceiverAddressList(targetUsers);
        messageSender.setReceiverTypeCode(receiveType);
        messageSender.setArgs(params);
        messageSender.setMessageCode(sendSettingCode);
        if (addition == null) {
            addition = new HashMap<>();
        }
        addition.putIfAbsent(MessageAdditionalType.PARAM_TENANT_ID.getTypeName(), Objects.requireNonNull(organizationId));
        messageSender.setAdditionalInformation(addition);
        return messageSender;
    }

    @Override
    public void sendNotices(String sendSettingCode, List<Receiver> receivers, Map<String, String> params, Long projectId) {
        LOGGER.debug("Send Notice: code: {}, receivers: {}, params: {}, projectId: {}", sendSettingCode, receivers, params, projectId);
        doWithTryCatchAndLog(
                () -> {
                    setParamsForUserInfo(params);
                    MessageSender sender = constructMessageSender(sendSettingCode, receivers, null, params, null, projectId);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Sender: {}", JsonHelper.marshalByJackson(sender));
                    }

                    messageClient.async().sendMessage(sender);
                },
                ex -> LOGGER.info("Failed to send message with code {}", sendSettingCode));
    }

    public void sendOrganizationNotices(String sendSettingCode, List<Receiver> receivers, Map<String, String> params, Long organizationId) {
        LOGGER.debug("Send Notice: code: {}, receivers: {}, params: {}, organizationId: {}", sendSettingCode, receivers, params, organizationId);
        doWithTryCatchAndLog(
                () -> {
                    setParamsForUserInfo(params);
                    MessageSender sender = constructOrganizationMessageSender(sendSettingCode, receivers, null, params, null, organizationId);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Sender: {}", JsonHelper.marshalByJackson(sender));
                    }
                    messageClient.async().sendMessage(sender);
                },
                ex -> LOGGER.info("Failed to send message with code {}", sendSettingCode));
    }

    protected void sendNotices(String sendSettingCode, String receiveType, Map<String, String> params, Long projectId) {
        LOGGER.debug("Send Notice: code: {}, params: {}, projectId: {}", sendSettingCode, params, projectId);
        setParamsForUserInfo(params);
        MessageSender sender = constructMessageSender(sendSettingCode, null, receiveType, params, null, projectId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sender: {}", JsonHelper.marshalByJackson(sender));
        }
        messageClient.async().sendMessage(sender);
    }

    protected void setParamsForUserInfo(Map<String, String> params) {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        if (details == null) {
            params.put(LOGIN_NAME, DetailsHelper.getAnonymousDetails().getUsername());
            params.put(USER_NAME, "匿名用户");
        } else {
            params.put(LOGIN_NAME, details.getUsername());
            params.put(USER_NAME, details.getRealName());
        }
    }
}
