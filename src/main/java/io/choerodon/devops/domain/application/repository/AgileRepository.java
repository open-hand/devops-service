package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.ProjectInfo;
import io.choerodon.devops.infra.feign.AgileServiceClient;

public interface AgileRepository {

    Issue queryIssue(Long projectId, Long issueId, Long organizationId);

    ProjectInfo queryProjectInfo(Long projectId);

    void initAgileServiceClient(AgileServiceClient agileServiceClient);
}
