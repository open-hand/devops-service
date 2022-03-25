package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiPipelineScheduleVO;
import io.choerodon.devops.app.service.CiPipelineScheduleService;
import io.choerodon.devops.infra.dto.CiPipelineScheduleDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * devops_ci_pipeline_schedule(CiPipelineSchedule)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:27
 */

@RestController("ciPipelineScheduleController.v1")
@RequestMapping("/v1/projects/{project_id}/ci_pipeline_schedules")
public class CiPipelineScheduleController extends BaseController {

    @Autowired
    private CiPipelineScheduleService ciPipelineScheduleService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建定时计划")
    @PostMapping
    public ResponseEntity<CiPipelineScheduleDTO> create(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody CiPipelineScheduleVO ciPipelineScheduleVO) {
        return ResponseEntity.ok(ciPipelineScheduleService.create(ciPipelineScheduleVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询所有执行计划")
    @GetMapping
    public ResponseEntity<List<CiPipelineScheduleVO>> listByAppServiceId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam("app_service_id") Long appServiceId) {
        return ResponseEntity.ok(ciPipelineScheduleService.listByAppServiceId(projectId, appServiceId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "启用执行计划")
    @PutMapping("{id}/enable")
    public ResponseEntity<Void> enableSchedule(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable("id") Long id) {
        ciPipelineScheduleService.enableSchedule(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "停用执行计划")
    @PutMapping("{id}/disable")
    public ResponseEntity<Void> disableSchedule(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable("id") Long id) {
        ciPipelineScheduleService.disableSchedule(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除执行计划")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSchedule(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable("id") Long id) {
        ciPipelineScheduleService.deleteSchedule(projectId, id);
        return ResponseEntity.noContent().build();
    }

}

