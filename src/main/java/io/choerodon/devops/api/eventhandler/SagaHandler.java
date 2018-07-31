package io.choerodon.devops.api.eventhandler;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.saga.SagaTask;
import io.choerodon.devops.app.service.ProjectService;
import io.choerodon.devops.domain.application.event.ProjectEvent;

/**
 * Creator: Runge
 * Date: 2018/7/27
 * Time: 10:06
 * Description: External saga msg
 */
@Component
public class SagaHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsEventHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProjectService projectService;

    private void loggerInfo(Object o) {
        LOGGER.info("data: {}", o);
    }

    /**
     * 创建项目saga
     */
    @SagaTask(code = "devopsCreateProject",
            description = "devops创建项目",
            sagaCode = "iam-create-project",
            seq = 2)
    public void handleProjectCreateEvent(String msg) throws IOException {
        ProjectEvent projectEvent = objectMapper.readValue(msg, ProjectEvent.class);
        loggerInfo(projectEvent);
        projectService.createProject(projectEvent);
    }
}
