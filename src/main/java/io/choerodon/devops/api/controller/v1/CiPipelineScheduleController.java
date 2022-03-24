package io.choerodon.devops.api.controller.v1;

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

}

