package io.choerodon.devops.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.ProjectInfo;
import io.choerodon.devops.infra.feign.fallback.AgileServiceClientFallback;

@FeignClient(value = "agile-service", fallback = AgileServiceClientFallback.class)
public interface AgileServiceClient {

    @GetMapping(value = "/v1/projects/{project_id}/issues/{issueId}")
    ResponseEntity<Issue> queryIssue(
            @PathVariable("project_id") Long projectId,
            @PathVariable("issueId") Long issueId,
            @RequestParam("organizationId") Long organizationId);

    @GetMapping(value = "/v1/projects/{project_id}/project_info")
    ResponseEntity<ProjectInfo> queryProjectInfo(
            @PathVariable("project_id") Long projectId);
}
