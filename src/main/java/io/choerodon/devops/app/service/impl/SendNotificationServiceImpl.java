package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.SendNotificationService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.LogUtil;

/**
 * @author zmf
 * @since 12/5/19
 */
@Service
public class SendNotificationServiceImpl implements SendNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendNotificationServiceImpl.class);

    @Value("${sendMessages:false}")
    private boolean sendMessages;

    @Autowired
    private NotifyClient notifyClient;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

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
            LogUtil.loggerInfoObjectNullWithId("Project", appServiceId, LOGGER);
            return;
        }
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        if (organizationDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("Organization", appServiceId, LOGGER);
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

        NoticeSendDTO noticeSendDTO = new NoticeSendDTO();
        noticeSendDTO.setSourceId(sourceId);
        noticeSendDTO.setCode(sendSettingCode);
        noticeSendDTO.setTargetUsers(targetUsers);
        noticeSendDTO.setParams(params);

        notifyClient.sendMessage(noticeSendDTO);
    }

    @Override
    public void sendWhenAppServiceFailure(Long appServiceId) {
        if (!sendMessages) {
            return;
        }
        doWithTryCatchAndLog(
                () -> {
                    // TODO by zmf
                    sendNoticeAboutAppService(appServiceId, null, app -> {
                        NoticeSendDTO.User targetUser = new NoticeSendDTO.User();
                        targetUser.setId(app.getCreatedBy());
                        return ArrayUtil.singleAsList(targetUser);
                    });
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
                                    .map(p -> {
                                        NoticeSendDTO.User user = new NoticeSendDTO.User();
                                        user.setId(p.getIamUserId());
                                        return user;
                                    })
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
                                    .map(p -> {
                                        NoticeSendDTO.User user = new NoticeSendDTO.User();
                                        user.setId(p.getIamUserId());
                                        return user;
                                    })
                                    .collect(Collectors.toList()));
                },
                ex -> LOGGER.info("Error occurred when sending message about app-service-disable. The exception is {}.", ex));
    }


    @Override
    public void sendWhenCDFailure(String gitlabUrl, String organizationCode, String projectCode, String projectName, String appServiceCode, String appServiceName) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
    }

    @Override
    public void sendWhenMergeRequestAuditEvent(String gitlabUrl, String organizationCode, String projectCode, String projectName, String appServiceCode, String appServiceName, String realName, Long mergeRequestId) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
    }

    @Override
    public void sendWhenMergeRequestClosed(String gitlabUrl, String organizationCode, String projectCode, String projectName, String appServiceCode, String appServiceName, String realName, Long mergeRequestId) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
    }

    @Override
    public void sendWhenMergeRequestPassed(String gitlabUrl, String organizationCode, String projectCode, String projectName, String appServiceCode, String appServiceName, String realName, Long mergeRequestId) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
    }

    @Override
    public void sendWhenInstanceCreationFailure(String projectName, String envName, String resourceName) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
    }

    @Override
    public void sendWhenServiceCreationFailure(String projectName, String envName, String resourceName) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
    }

    @Override
    public void sendWhenIngressCreationFailure(String projectName, String envName, String resourceName) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
    }

    @Override
    public void sendWhenCertificationCreationFailure(String projectName, String envName, String resourceName) {
        if (!sendMessages) {
            return;
        }
        // TODO by zmf
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
}
