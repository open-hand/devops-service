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

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/2
 * Time: 11:00
 * Description:
 */
@Component
public class ProjectServiceImpl implements ProjectService {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.application.name}")
    private String applicationName;

    private DevopsProjectRepository devopsProjectRepository;
    private final SagaClient sagaClient;

    @Autowired
    public ProjectServiceImpl(DevopsProjectRepository devopsProjectRepository, SagaClient sagaClient) {
        this.devopsProjectRepository = devopsProjectRepository;
        this.sagaClient = sagaClient;
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
