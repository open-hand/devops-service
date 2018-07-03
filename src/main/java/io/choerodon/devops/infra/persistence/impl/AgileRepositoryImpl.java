package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.repository.AgileRepository;
import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.ProjectInfo;
import io.choerodon.devops.infra.feign.AgileServiceClient;

@Component
public class AgileRepositoryImpl implements AgileRepository {

    @Autowired
    private AgileServiceClient agileServiceClient;

    @Override
    public Issue queryIssue(Long projectId, Long issueId) {
        return agileServiceClient.queryIssue(projectId, issueId).getBody();
    }

    @Override
    public ProjectInfo queryProjectInfo(Long projectId) {
        return agileServiceClient.queryProjectInfo(projectId).getBody();
    }
}
