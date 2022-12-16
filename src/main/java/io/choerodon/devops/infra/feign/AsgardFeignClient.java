package io.choerodon.devops.infra.feign;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.SagaInstanceDetails;
import io.choerodon.devops.infra.dto.asgard.QuartzTaskDTO;
import io.choerodon.devops.infra.dto.asgard.ScheduleTaskDTO;
import io.choerodon.devops.infra.feign.fallback.AsgardFeignClientFallback;


/**
 * @author dengyouquan
 **/
@FeignClient(value = "zknow-asgard",
        fallback = AsgardFeignClientFallback.class)
public interface AsgardFeignClient {

    @PostMapping("/v1/schedules/tasks/internal")
    ResponseEntity<QuartzTaskDTO> createByServiceCodeAndMethodCode(@RequestParam("source_level") String sourceLevel,
                                                                   @RequestParam("source_id") Long sourceId,
                                                                   @RequestBody @Valid ScheduleTaskDTO dto);

    @ApiOperation(value = "根据name查询定时任务")
    @GetMapping("/v1/schedules/tasks/internal")
    ResponseEntity<QuartzTaskDTO> queryByName(@RequestParam("task_name") String taskName);

    @DeleteMapping("/v1/schedules/tasks/delete/job")
    ResponseEntity<QuartzTaskDTO> deleteByIds(@RequestBody List<Long> ids);

    @GetMapping("/v1/sagas/instances/ref/business/instance")
    ResponseEntity<List<SagaInstanceDetails>> queryByRefTypeAndRefIds(@RequestParam(value = "refType") String refType,
                                                                      @RequestParam(value = "refIds") List<String> refIds,
                                                                      @RequestParam(value = "sagaCode") String sagaCode);

    @PutMapping("/v1/sagas/projects/{project_id}/tasks/instances/{instance_id}/retry")
    ResponseEntity<Void> retry(@PathVariable("project_id") Long projectId,
                               @PathVariable("instance_id") Long instanceId);
}