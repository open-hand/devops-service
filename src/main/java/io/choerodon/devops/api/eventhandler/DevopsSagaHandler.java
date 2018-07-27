package io.choerodon.devops.api.eventhandler;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.event.EventPayload;
import io.choerodon.core.saga.SagaDefinition;
import io.choerodon.core.saga.SagaTask;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.event.GitlabGroupPayload;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.devops.domain.application.event.HarborPayload;

/**
 * Creator: Runge
 * Date: 2018/7/27
 * Time: 10:06
 * Description: Saga msg by DevOps self
 */
@Component
public class DevopsSagaHandler {

    private static final String DEVOPS_SERVICE = "devops-service";
    private static final String IAM_SERVICE = "iam-service";
    private static final String TEMPLATE = "template";
    private static final String ORG_SERVICE = "organization-service";
    private static final String APPLICATION = "application";

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsEventHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private ProjectService projectService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationTemplateService applicationTemplateService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private GitlabGroupService gitlabGroupService;
    @Autowired
    private HarborService harborService;
    @Autowired
    private GitlabUserService gitlabUserService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    private void loggerInfo(Object o) {
        LOGGER.info("data: {}", o);
    }

    /**
     * 创建组事件
     */
    @SagaTask(code = "devopsCreateGitLabGroup",
            description = "devops 创建 GitLab Group",
            sagaCode = "devops-create-gitlab-group",
            concurrentLimitNum = 2,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE,
            seq = 2)
    public void handleGitlabGroupEvent(EventPayload<GitlabGroupPayload> payload) {
        GitlabGroupPayload gitlabGroupPayload = payload.getData();
        loggerInfo(gitlabGroupPayload);
        gitlabGroupService.createGroup(gitlabGroupPayload, "");
        gitlabGroupService.createGroup(gitlabGroupPayload, "-gitops");
    }

    /**
     * 创建harbor项目事件
     */
    @SagaTask(code = "devopsCreateHarbor",
            description = "devops 创建 Harbor",
            sagaCode = "devops-create-harbor-project",
            concurrentLimitNum = 2,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE,
            seq = 2)
    public void handleHarborEvent(EventPayload<HarborPayload> payload) {
        HarborPayload harborPayload = payload.getData();
        loggerInfo(harborPayload);
        harborService.createHarbor(harborPayload);
    }

    @SagaTask(code = "devopsCreateEnv",
            description = "devops创建环境",
            sagaCode = "asgard-create-env",
            seq = 1)
    public void devopsCreateUser(String data) {
        GitlabProjectPayload gitlabProjectPayload = null;
        try {
            gitlabProjectPayload = objectMapper.readValue(data, GitlabProjectPayload.class);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
        devopsEnvironmentService.handleCreateEnvSaga(gitlabProjectPayload);

    }


}
