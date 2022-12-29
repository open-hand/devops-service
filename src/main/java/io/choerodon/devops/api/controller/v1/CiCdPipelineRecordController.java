package io.choerodon.devops.api.controller.v1;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.dto.DevopsPipelineRecordRelDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/projects/{project_id}/cicd_pipelines_record")
public class CiCdPipelineRecordController {
@Autowired
private DevopsCiPipelineRecordService devopsCiPipelineRecordService;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询指定流水线记录详情")
    @GetMapping("/details")
    public ResponseEntity<DevopsCiPipelineRecordVO> queryPipelineRecordDetails(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线记录id", required = true)
            @RequestParam(value = "id") Long id) {
        return ResponseEntity.ok(devopsCiPipelineRecordService.queryPipelineRecordDetails(projectId, id));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重试整条流水线")
    @GetMapping("/retry")
    public ResponseEntity<Void> retryPipeline(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线记录id", required = true)
            @RequestParam(value = "id") Long id,
            @ApiParam(value = "流水线ID", required = true)
            @RequestParam("gitlab_project_id") Long gitlabProjectId) {
        devopsCiPipelineRecordService.retryPipeline(projectId, id, gitlabProjectId);
        return Results.success();
    }

    /**
     * Cancel jobs in a pipeline
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "取消流水线")
    @GetMapping(value = "/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线记录id", required = true)
            @RequestParam(value = "id") Long id,
            @ApiParam(value = "流水线ID", required = true)
            @RequestParam("gitlab_project_id") Long gitlabProjectId) {
        devopsCiPipelineRecordService.cancelPipeline(projectId, id, gitlabProjectId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询流水线执行记录")
    @GetMapping("/{pipeline_id}")
    public ResponseEntity<Page<CiPipelineRecordVO>> pagingPipelineRecord(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "pipeline_id") Long pipelineId,
            @ApiIgnore
            @SortDefault(value = DevopsPipelineRecordRelDTO.FIELD_ID, direction = Sort.Direction.DESC) PageRequest pageable) {
        return ResponseEntity.ok(devopsCiPipelineRecordService.pagingPipelineRecord(projectId, pipelineId, pageable));
    }
}
