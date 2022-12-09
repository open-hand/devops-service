package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.app.service.PipelineJobRecordService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线执行记录(PipelineRecord)表控制层
 *
 * @author
 * @since 2022-11-23 16:43:02
 */

@RestController
@RequestMapping("/v1/projects/{project_id}/pipeline_job_records")
public class PipelineJobRecordController {


    @Autowired
    private PipelineJobRecordService pipelineJobRecordService;

    /**
     * 审核人工卡点任务
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "审核人工卡点任务")
    @PostMapping("/{id}/audit")
    public ResponseEntity<AuditResultVO> auditJob(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "任务记录Id", required = true)
            @PathVariable(value = "id") Long id,
            @ApiParam(value = "审核结果，拒绝：refused、通过passed", required = true)
            @RequestParam(value = "result") String result) {
        return ResponseEntity.ok(pipelineJobRecordService.auditJob(projectId, id, result));
    }

    /**
     * 校验stage、job审核状态是否改变
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验stage、job审核状态是否改变")
    @PostMapping("/{id}/check_audit_status")
    public ResponseEntity<AduitStatusChangeVO> checkAuditStatus(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "任务记录Id", required = true)
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(pipelineJobRecordService.checkAuditStatus(projectId, id));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查看任务日志")
    @GetMapping("/{id}/log")
    public ResponseEntity<String> queryLog(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "任务记录Id", required = true)
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(pipelineJobRecordService.queryLog(projectId, id));
    }
}

