package io.choerodon.devops.infra.feign;

import io.choerodon.devops.infra.dto.agile.IssueDTO;
import io.choerodon.devops.infra.dto.agile.ProjectInfoDTO;
import io.choerodon.devops.infra.feign.fallback.AgileServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "agile-service", fallback = AgileServiceClientFallback.class)
public interface AgileServiceClient {

    @GetMapping(value = "/v1/projects/{project_id}/issues/{issueId}")
    ResponseEntity<IssueDTO> queryIssue(
            @PathVariable("project_id") Long projectId,
            @PathVariable("issueId") Long issueId,
            @RequestParam("organizationId") Long organizationId);

    @GetMapping(value = "/v1/projects/{project_id}/project_info")
    ResponseEntity<ProjectInfoDTO> queryProjectInfo(
            @PathVariable("project_id") Long projectId);
}
