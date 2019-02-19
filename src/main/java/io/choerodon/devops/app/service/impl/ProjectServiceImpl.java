package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.ProjectService;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
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

    @Value("${spring.application.name}")
    private String applicationName;

    private DevopsProjectRepository devopsProjectRepository;

    @Autowired
    public ProjectServiceImpl(DevopsProjectRepository devopsProjectRepository) {
        this.devopsProjectRepository = devopsProjectRepository;
    }

    @Override
    public void createProject(ProjectEvent projectEvent) {
        // create project in db
        DevopsProjectDO devopsProject = new DevopsProjectDO(projectEvent.getProjectId());
        devopsProjectRepository.createProject(devopsProject);
    }

    @Override
    public boolean queryProjectGitlabGroupReady(Long projectId) {
        GitlabGroupE gitlabGroupE =  devopsProjectRepository.queryDevopsProject(projectId);
        return gitlabGroupE.getDevopsAppGroupId() != null ;
    }
}
