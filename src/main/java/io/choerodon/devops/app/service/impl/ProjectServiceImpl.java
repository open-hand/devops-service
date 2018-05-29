package io.choerodon.devops.app.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
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
        createGitlabGroupEvent(projectEvent);
        createHarborEvent(projectEvent);
    }

    private void createGitlabGroupEvent(ProjectEvent projectEvent) {
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        BeanUtils.copyProperties(projectEvent, gitlabGroupPayload);
        Exception exception = eventProducerTemplate.execute(GROUP_EVENT, DEVOPS_SERVICE, gitlabGroupPayload,
                (String uuid) -> {
                    DevopsProjectDO devopsProject = new DevopsProjectDO(projectEvent.getProjectId());
                    devopsProject.setGitlabUuid(uuid);
                    devopsProjectRepository.createProject(devopsProject);
                });
        if (exception != null) {
            throw new CommonException(exception.getMessage());
        }
    }

    private void createHarborEvent(ProjectEvent projectEvent) {
        HarborPayload harborPayload = new HarborPayload(
                projectEvent.getProjectId(),
                projectEvent.getOrganizationCode() + "-" + projectEvent.getProjectCode()
        );
        Exception exception = eventProducerTemplate.execute(HARBOR_EVENT, DEVOPS_SERVICE, harborPayload,
                (String uuid) -> {
                    DevopsProjectDO devopsProject = new DevopsProjectDO(projectEvent.getProjectId());
                    devopsProject.setHarborUuid(uuid);
                    devopsProjectRepository.updateProjectAttr(devopsProject);
                });
        if (exception != null) {
            throw new CommonException(exception.getMessage());
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
