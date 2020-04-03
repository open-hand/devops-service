package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsCiPipelineVO;
import io.choerodon.devops.api.vo.DevopsClusterReqVO;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.infra.dto.DevopsCiPipelineDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

/**
 * 〈功能简述〉
 * 〈CI流水线Controller〉
 *
 * @author wanghao
 * @Date 2020/4/2 17:57
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_pipelines")
public class DevopsCiPipelineController {

    private DevopsCiPipelineService devopsCiPipelineService;

    public DevopsCiPipelineController(DevopsCiPipelineService devopsCiPipelineService) {
        this.devopsCiPipelineService = devopsCiPipelineService;
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建ci流水线")
    @PostMapping
    public ResponseEntity<DevopsCiPipelineDTO> create(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Valid DevopsCiPipelineVO devopsCiPipelineVO) {
        return ResponseEntity.ok(devopsCiPipelineService.create(projectId, devopsCiPipelineVO));
    }
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新ci流水线")
    @PostMapping("/{ci_pipeline_id}")
    public ResponseEntity<DevopsCiPipelineDTO> update(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "ci_pipeline_id") Long ciPipelineId,
            @RequestBody @Valid DevopsCiPipelineVO devopsCiPipelineVO) {
        return ResponseEntity.ok(devopsCiPipelineService.update(projectId, ciPipelineId, devopsCiPipelineVO));
    }
}
