package io.choerodon.devops.infra.feign;

import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.feign.fallback.WorkFlowServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    ResponseEntity approveUserTask(
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam("processInstanceId") String processInstanceId);
}
