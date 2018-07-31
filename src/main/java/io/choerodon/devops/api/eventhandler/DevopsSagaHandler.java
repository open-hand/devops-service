package io.choerodon.devops.api.eventhandler;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.saga.SagaDefinition;
import io.choerodon.core.saga.SagaTask;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.app.service.HarborService;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsEventHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private GitlabGroupService gitlabGroupService;
    @Autowired
    private HarborService harborService;
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
    public void handleGitlabGroupEvent(String msg) {
        GitlabGroupPayload gitlabGroupPayload = null;
        try {
            gitlabGroupPayload = objectMapper.readValue(msg, GitlabGroupPayload.class);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
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
    public void handleHarborEvent(String msg) {
        HarborPayload harborPayload = null;
        try {
            harborPayload = objectMapper.readValue(msg, HarborPayload.class);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
        loggerInfo(harborPayload);
        harborService.createHarbor(harborPayload);
    }

    /**
     * devops创建环境
     */
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
