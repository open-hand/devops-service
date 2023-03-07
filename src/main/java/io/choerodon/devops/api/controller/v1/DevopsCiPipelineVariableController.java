package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsCiPipelineVariableService;
import io.choerodon.devops.infra.dto.DevopsCiPipelineVariableDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/22 10:59
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/pipeline_variables")
public class DevopsCiPipelineVariableController {

    @Autowired
    private DevopsCiPipelineVariableService devopsCiPipelineVariableService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据流水线id查询环境变量信息")
    @GetMapping
    public ResponseEntity<List<DevopsCiPipelineVariableDTO>> listByPipelineId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @RequestParam(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiPipelineVariableService.listByPipelineId(pipelineId));
    }
}
