package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.ProjectInfo;

public interface AgileRepository {

    Issue queryIssue(Long projectId, Long issueId);

    ProjectInfo queryProjectInfo(Long projectId);
}
