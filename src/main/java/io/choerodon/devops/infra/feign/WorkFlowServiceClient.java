package io.choerodon.devops.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.feign.fallback.WorkFlowServiceClientFallback;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:38 2019/4/9
 * Description:
 */
@FeignClient(value = "workflow-service", fallback = WorkFlowServiceClientFallback.class)
public interface WorkFlowServiceClient {

    @PostMapping(value = "/v1/projects/{project_id}/process_instances")
    ResponseEntity<String> create(
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody DevopsPipelineDTO devopsPipelineDTO);


    @PutMapping(value = "/v1/projects/{project_id}/process_instances")
    ResponseEntity<Boolean> approveUserTask(
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "business_key") String businessKey);

    @GetMapping(value = "/v1/projects/{project_id}/process_instances")
    ResponseEntity stopInstance(
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "business_key") String businessKey);
}
