package io.choerodon.devops.infra.feign.fallback;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.feign.AgileServiceClient;

@Component
public class AgileServiceClientFallback implements AgileServiceClient {
    @Override
    public ResponseEntity<String> queryIssue(Long projectId, Long issueId, Long organizationId) {
        throw new CommonException("error.issue.get");
    }

    @Override
    public ResponseEntity<String> queryIssues(Long projectId, List<Long> issueIds) {
        throw new CommonException("error.issue.get");
    }

    @Override
    public ResponseEntity<String> getActiveSprint(Long projectId, Long organizationId) {
        throw new CommonException("error.active.sprint.get");
    }
}
