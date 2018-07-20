package io.choerodon.devops.infra.feign.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.ProjectInfo;
import io.choerodon.devops.infra.feign.AgileServiceClient;

public class AgileServiceClientFallback implements AgileServiceClient {
    @Override
    public ResponseEntity<Issue> queryIssue(Long projectId, Long issueId) {
        return new ResponseEntity("error.issue.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProjectInfo> queryProjectInfo(Long projectId) {
        return new ResponseEntity("error.projectinfo.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
