package io.choerodon.devops.app.eventhandler;

import java.util.Collections;
import java.util.HashMap;

import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.asgard.saga.consumer.MockHttpServletRequest;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.devops.app.service.DevopsDemoEnvInitService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.app.service.OrganizationService;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.app.eventhandler.payload.OrganizationEventPayload;
import io.choerodon.devops.app.eventhandler.payload.OrganizationRegisterEventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * Handle saga events for setting up demo environment.
 *
 * @author zmf
 */
@Component
public class DemoEnvSetupSagaHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SagaHandler.class);
    private final Gson gson = new Gson();

    @Autowired
    private GitlabGroupService gitlabGroupService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private DevopsDemoEnvInitService devopsDemoEnvInitService;

    /**
     * 创建组织事件
     */
    @SagaTask(code = "register-devops-init-org",
            description = "创建组织事件",
            sagaCode = "register-org",
            maxRetryCount = 3,
            seq = 30)
    public OrganizationRegisterEventPayload handleDemoOrganizationCreateEvent(String payload) {
        logInfoPayload("register-devops-init-org", payload);
        OrganizationRegisterEventPayload registerInfo = gson.fromJson(payload, OrganizationRegisterEventPayload.class);
        OrganizationEventPayload organizationEventPayload = new OrganizationEventPayload();
        BeanUtils.copyProperties(registerInfo.getOrganization(), organizationEventPayload);
        organizationEventPayload.setUserId(registerInfo.getUser().getId());
        organizationService.create(organizationEventPayload);
        return registerInfo;
    }


    /**
     * 创建项目事件
     */
    @SagaTask(code = "register-devops-init-projcet",
            description = "devops 创建对应项目的两个gitlab组",
            sagaCode = "register-org",
            maxRetryCount = 3,
            seq = 90)
    private OrganizationRegisterEventPayload handleCreateGroupsForDemoProject(String payload) {
        logInfoPayload("register-devops-init-projcet", payload);
        OrganizationRegisterEventPayload registerInfo = gson.fromJson(payload, OrganizationRegisterEventPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        gitlabGroupPayload.setOrganizationCode(registerInfo.getOrganization().getCode());
        gitlabGroupPayload.setOrganizationName(registerInfo.getOrganization().getName());
        gitlabGroupPayload.setProjectCode(registerInfo.getProject().getCode());
        gitlabGroupPayload.setProjectId(registerInfo.getProject().getId());
        gitlabGroupPayload.setProjectName(registerInfo.getProject().getName());
        gitlabGroupPayload.setUserId(registerInfo.getUser().getId());
        gitlabGroupPayload.setUserName(registerInfo.getUser().getLoginName());

        gitlabGroupService.createGroup(gitlabGroupPayload, "");
        gitlabGroupService.createGroup(gitlabGroupPayload, "-gitops");
        return registerInfo;
    }


    /**
     * 设置Demo流程的用户上下文
     *
     */
    public static void beforeInvoke(String loginName, Long userId, Long orgId) {
        CustomUserDetails customUserDetails = new CustomUserDetails(loginName, "unknown", Collections.emptyList());
        customUserDetails.setUserId(userId);
        customUserDetails.setOrganizationId(orgId);
        customUserDetails.setLanguage("zh_CN");
        customUserDetails.setTimeZone("CCT");

        Authentication user = new UsernamePasswordAuthenticationToken("default", "N/A", Collections.emptyList());
        OAuth2Request request = new OAuth2Request(new HashMap<>(0), "", Collections.emptyList(), true,
                Collections.emptySet(), Collections.emptySet(), null, null, null);
        OAuth2Authentication authentication = new OAuth2Authentication(request, user);
        OAuth2AuthenticationDetails oAuth2AuthenticationDetails = new OAuth2AuthenticationDetails(new MockHttpServletRequest());
        oAuth2AuthenticationDetails.setDecodedDetails(customUserDetails);
        authentication.setDetails(oAuth2AuthenticationDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * log info payload
     *
     * @param sagaTaskCode the saga task's payload
     * @param payload      the payload
     */
    private void logInfoPayload(String sagaTaskCode, String payload) {
        LOGGER.info("saga task code: {}, payload: {}", sagaTaskCode, payload);
    }

    /**
     * 清空当前用户上下文
     */
    public static void afterInvoke() {
        SecurityContextHolder.clearContext();
    }

    @SagaTask(code = "register-devops-init-demo-data", description = "初始化Demo环境的项目相关数据", sagaCode = "register-org", maxRetryCount = 3, seq = 150)
    public OrganizationRegisterEventPayload initDemoProject(String payload) {
        logInfoPayload("register-devops-init-demo-data", payload);
        OrganizationRegisterEventPayload registerInfo = gson.fromJson(payload, OrganizationRegisterEventPayload.class);
        beforeInvoke(registerInfo.getUser().getLoginName(), registerInfo.getUser().getId(), registerInfo.getOrganization().getId());
        devopsDemoEnvInitService.initialDemoEnv(registerInfo);
        afterInvoke();
        return registerInfo;
    }
}
