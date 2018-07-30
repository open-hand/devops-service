package io.choerodon.devops.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.saga.SagaClient;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.saga.Saga;
import io.choerodon.devops.app.service.ProjectService;
import io.choerodon.devops.domain.application.event.GitlabGroupPayload;
import io.choerodon.devops.domain.application.event.HarborPayload;
import io.choerodon.devops.domain.application.event.ProjectEvent;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.event.producer.execute.EventProducerTemplate;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/2
 * Time: 11:00
 * Description:
 */
@Component
public class ProjectServiceImpl implements ProjectService {
    private static final String DEVOPS_SERVICE = "devops-service";
    private static final String GROUP_EVENT = "GitlabGroup";
    private static final String HARBOR_EVENT = "Harbor";

    private final EventProducerTemplate eventProducerTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private SagaClient sagaClient;

    @Value("${spring.application.name}")
    private String applicationName;
    private DevopsProjectRepository devopsProjectRepository;

    @Autowired
    public ProjectServiceImpl(EventProducerTemplate eventProducerTemplate,
                              DevopsProjectRepository devopsProjectRepository) {
        this.eventProducerTemplate = eventProducerTemplate;
        this.devopsProjectRepository = devopsProjectRepository;
    }

    @Override
    public void createProject(ProjectEvent projectEvent) {
        // create project in db
        DevopsProjectDO devopsProject = new DevopsProjectDO(projectEvent.getProjectId());
        devopsProjectRepository.createProject(devopsProject);

        createGitlabGroupEvent(projectEvent);
        createHarborEvent(projectEvent);
    }

    @Saga(code = "devops-create-gitlab-group", description = "创建gitlab group", inputSchema = "{}")
    private void createGitlabGroupEvent(ProjectEvent projectEvent) {
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectEvent, gitlabGroupPayload);
//        Exception exception = eventProducerTemplate.execute(GROUP_EVENT, DEVOPS_SERVICE, gitlabGroupPayload,
//                (String uuid) -> {
//                    DevopsProjectDO devopsProject = new DevopsProjectDO(projectEvent.getProjectId());
//                    devopsProject.setGitlabUuid(uuid);
//                    devopsProjectRepository.createProject(devopsProject);
//                });
//        if (exception != null) {
//            throw new CommonException(exception.getMessage());
//        }

        try {
            String input = objectMapper.writeValueAsString(gitlabGroupPayload);
            sagaClient.startSaga(
                    "devops-create-gitlab-group",
                    new StartInstanceDTO(input, projectEvent.getUserId(), "", ""));
        } catch (JsonProcessingException e) {
            throw new CommonException("error.SagaProducer.devops.createGitLabGroup");
        }
    }

    @Saga(code = "devops-create-harbor-project", description = "创建 harbor project", inputSchema = "{}")
    private void createHarborEvent(ProjectEvent projectEvent) {
        HarborPayload harborPayload = new HarborPayload(
                projectEvent.getProjectId(),
                projectEvent.getOrganizationCode() + "-" + projectEvent.getProjectCode()
        );
//        Exception exception = eventProducerTemplate.execute(HARBOR_EVENT, DEVOPS_SERVICE, harborPayload,
//                (String uuid) -> {
//                    DevopsProjectDO devopsProject = new DevopsProjectDO(projectEvent.getProjectId());
//                    devopsProject.setHarborUuid(uuid);
//                    devopsProjectRepository.updateProjectAttr(devopsProject);
//                });
//        if (exception != null) {
//            throw new CommonException(exception.getMessage());
//        }
        try {
            String input = objectMapper.writeValueAsString(harborPayload);
            sagaClient.startSaga(
                    "devops-create-harbor-project",
                    new StartInstanceDTO(input, projectEvent.getUserId(), "", ""));
        } catch (JsonProcessingException e) {
            throw new CommonException("error.SagaProducer.devops.createHarbor");
        }
    }

    @Override
    public Boolean groupExist(String uuid) {
        return devopsProjectRepository.checkGroupExist(uuid);
    }

    @Override
    public Boolean harborExist(String uuid) {
        return devopsProjectRepository.checkHarborExist(uuid);
    }

    @Override
    public Boolean memberExist(String uuid) {
        return devopsProjectRepository.checkMemberExist(uuid);
    }
}
