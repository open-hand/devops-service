package io.choerodon.devops.api.eventhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.event.EventPayload;
import io.choerodon.core.saga.SagaDefinition;
import io.choerodon.core.saga.SagaTask;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.event.ProjectEvent;
import io.choerodon.event.consumer.annotation.EventListener;

/**
 * Creator: Runge
 * Date: 2018/7/27
 * Time: 10:06
 * Description: External saga msg
 */
@Component
public class SagaHandler {

    private static final String DEVOPS_SERVICE = "devops-service";
    private static final String IAM_SERVICE = "iam-service";
    private static final String TEMPLATE = "template";
    private static final String ORG_SERVICE = "organization-service";
    private static final String APPLICATION = "application";

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsEventHandler.class);

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


    private void loggerInfo(Object o) {
        LOGGER.info("data: {}", o);
    }

    /**
     * 创建项目saga
     */
    @SagaTask(code = "devopsCreateProject",
            description = "devops创建项目",
            sagaCode = "iam-create-project",
            concurrentLimitNum = 2,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE,
            seq = 2)
    public void handleProjectCreateEvent(EventPayload<ProjectEvent> payload) {
        ProjectEvent projectEvent = payload.getData();
        loggerInfo(projectEvent);
        projectService.createProject(projectEvent);
    }
}
