package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.CiCdPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.CiCdPipelineVO;

import io.choerodon.devops.app.service.CiCdPipelineService;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineDTO;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/projects/{project_id}/cicd_pipelines")
public class CiCdPipelineController {

    @Autowired
    private CiCdPipelineService ciCdPipelineService;

    @PostMapping
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建ci流水线")
    public ResponseEntity<CiCdPipelineDTO> create(@ApiParam(value = "项目Id", required = true)
                                                  @PathVariable(value = "project_id") Long projectId,
                                                  @RequestBody CiCdPipelineVO ciCdPipelineVO) {
        CiCdPipelineAdditionalValidator.additionalCheckPipeline(ciCdPipelineVO);
        return ResponseEntity.ok(ciCdPipelineService.create(projectId, ciCdPipelineVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询ci流水线配置")
    @GetMapping("/{cicd_pipeline_id}")
    public ResponseEntity<CiCdPipelineVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "cicd_pipeline_id") Long ciCdPipelineId) {
        return ResponseEntity.ok(ciCdPipelineService.query(projectId, ciCdPipelineId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下更新ci流水线")
    @PutMapping("/{cicd_pipeline_id}")
    public ResponseEntity<CiCdPipelineDTO> update(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "cicd_pipeline_id") Long ciCdPipelineId,
            @RequestBody @Valid CiCdPipelineVO ciCdPipelineVO) {
        CiCdPipelineAdditionalValidator.additionalCheckPipeline(ciCdPipelineVO);
        return ResponseEntity.ok(ciCdPipelineService.update(projectId, ciCdPipelineId, ciCdPipelineVO));
    }

}
