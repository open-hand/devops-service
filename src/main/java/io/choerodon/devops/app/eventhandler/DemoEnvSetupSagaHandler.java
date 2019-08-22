package io.choerodon.devops.app.eventhandler;

import java.util.Collections;
import java.util.HashMap;

import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.asgard.saga.consumer.MockHttpServletRequest;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.ApplicationEventPayload;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.app.eventhandler.payload.OrganizationRegisterEventPayload;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.app.service.DevopsDemoEnvInitService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.infra.util.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private DevopsDemoEnvInitService devopsDemoEnvInitService;
    @Autowired
    private GitlabGroupService gitlabGroupService;
    @Autowired
    private ApplicationService applicationService;

    /**
     * saga原有的用户上下文
     */
    private ThreadLocal<Authentication> preAuthentication = new ThreadLocal<>();


    /**
     * 创建项目事件
     */
    @SagaTask(code = SagaTaskCodeConstants.REGISTER_DEVOPS_INIT_PROJCET,
            description = "创建对应项目的两个gitlab组",
            sagaCode = SagaTopicCodeConstants.REGISTER_ORG,
            maxRetryCount = 3,
            seq = 90)
    private String handleCreateGroupsForDemoProject(String payload) {
        logInfoPayload(SagaTaskCodeConstants.REGISTER_DEVOPS_INIT_PROJCET, payload);
        OrganizationRegisterEventPayload registerInfo = gson.fromJson(payload, OrganizationRegisterEventPayload.class);
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        gitlabGroupPayload.setOrganizationCode(registerInfo.getOrganization().getCode());
        gitlabGroupPayload.setOrganizationName(registerInfo.getOrganization().getName());
        gitlabGroupPayload.setProjectCode(registerInfo.getProject().getCode());
        gitlabGroupPayload.setProjectId(registerInfo.getProject().getId());
        gitlabGroupPayload.setProjectName(registerInfo.getProject().getName());
        gitlabGroupPayload.setUserId(registerInfo.getUser().getId());
        gitlabGroupPayload.setUserName(registerInfo.getUser().getLoginName());

        // 创建环境gitlab组
        gitlabGroupService.createEnvGroup(gitlabGroupPayload);

        // 创建应用gitlab组
        ApplicationEventPayload applicationEventPayload = ConvertUtils.convertObject(registerInfo.getApplication(), ApplicationEventPayload.class);
        applicationEventPayload.setOrganizationCode(registerInfo.getOrganization().getCode());
        applicationEventPayload.setOrganizationName(registerInfo.getOrganization().getName());
        applicationEventPayload.setProjectId(registerInfo.getProject().getId());
        applicationEventPayload.setUserId(registerInfo.getUser().getId());

        applicationService.handleApplicationCreation(applicationEventPayload);
        return payload;
    }


    /**
     * 设置组织注册流程的用户上下文
     */
    private void beforeInvoke(String loginName, Long userId, Long orgId) {
        preAuthentication.set(SecurityContextHolder.getContext().getAuthentication());

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
     * 重置为saga原有的用户上下文
     */
    private void afterInvoke() {
        SecurityContextHolder.getContext().setAuthentication(preAuthentication.get());
        preAuthentication.remove();
    }

    /**
     * 初始化Demo环境的项目相关数据
     */
    @SagaTask(code = SagaTaskCodeConstants.REGISTER_DEVOPS_INIT_DEMO_DATA,
            description = "初始化Demo环境的项目相关数据",
            sagaCode = SagaTopicCodeConstants.REGISTER_ORG,
            maxRetryCount = 3, seq = 150)
    public OrganizationRegisterEventPayload initDemoProject(String payload) {
        logInfoPayload(SagaTaskCodeConstants.REGISTER_DEVOPS_INIT_DEMO_DATA, payload);
        OrganizationRegisterEventPayload registerInfo = gson.fromJson(payload, OrganizationRegisterEventPayload.class);
        beforeInvoke(registerInfo.getUser().getLoginName(), registerInfo.getUser().getId(), registerInfo.getOrganization().getId());
        devopsDemoEnvInitService.initialDemoEnv(registerInfo);
        afterInvoke();
        return registerInfo;
    }
}
