package io.choerodon.devops.api.controller.v1;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.app.service.CiCdPipelineRecordService;
import io.choerodon.devops.infra.dto.DevopsPipelineRecordRelDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/projects/{project_id}/cicd_pipelines_record")
public class CiCdPipelineRecordController {

    @Autowired
    private CiCdPipelineRecordService ciCdPipelineRecordService;


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询指定流水线记录详情")
    @GetMapping("/details")
    public ResponseEntity<CiCdPipelineRecordVO> queryPipelineRecordDetails(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "ci与cd记录关系表", required = true)
            @RequestParam(value = "record_rel_id") Long recordRelId) {
        return ResponseEntity.ok(ciCdPipelineRecordService.queryPipelineRecordDetails(projectId, recordRelId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试整条流水线")
    @GetMapping("/retry")
    public ResponseEntity<Void> retryPipeline(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "cd流水线记录id", required = true)
            @RequestParam(value = "record_rel_id") Long pipelineRecordRelId,
            @ApiParam(value = "流水线ID", required = true)
            @RequestParam("gitlab_project_id") Long gitlabProjectId) {
        ciCdPipelineRecordService.retryPipeline(projectId, pipelineRecordRelId, gitlabProjectId);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试cd任务")
    @GetMapping("/retry_cd_task")
    public ResponseEntity<Void> retryPipelineCdTask(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "cd流水线记录id", required = true)
            @RequestParam(value = "cd_pipeline_record_id") Long cdPipelineRecordId) {
        ciCdPipelineRecordService.retryCdPipeline(projectId, cdPipelineRecordId, true);
        return Results.success();
    }

    /**
     * Cancel jobs in a pipeline
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "取消流水线")
    @GetMapping(value = "/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "cd流水线记录id", required = true)
            @RequestParam(value = "record_rel_id") Long pipelineRecordRelId,
            @ApiParam(value = "流水线ID", required = true)
            @RequestParam("gitlab_project_id") Long gitlabProjectId) {
        ciCdPipelineRecordService.cancel(projectId, pipelineRecordRelId, gitlabProjectId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询流水线执行记录")
    @GetMapping("/{pipeline_id}")
    public ResponseEntity<Page<CiCdPipelineRecordVO>> pagingPipelineRecord(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "pipeline_id") Long pipelineId,
            @ApiIgnore
            @SortDefault(value = DevopsPipelineRecordRelDTO.FIELD_ID, direction = Sort.Direction.DESC) PageRequest pageable) {
        return ResponseEntity.ok(ciCdPipelineRecordService.pagingPipelineRecord(projectId, pipelineId, pageable));
    }
}
