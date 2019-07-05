package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.devops.domain.application.repository.AgileRepository;
import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.ProjectInfo;
import io.choerodon.devops.infra.feign.AgileServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgileRepositoryImpl implements AgileRepository {

    @Autowired
    private AgileServiceClient agileServiceClient;

    @Override
    public Issue queryIssue(Long projectId, Long issueId, Long organizationId) {
        try {
            return agileServiceClient.queryIssue(projectId, issueId, organizationId).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ProjectInfo queryProjectInfo(Long projectId) {
        return agileServiceClient.queryProjectInfo(projectId).getBody();
    }

    @Override
    public void initAgileServiceClient(AgileServiceClient agileServiceClient) {
        this.agileServiceClient = agileServiceClient;
    }
}
