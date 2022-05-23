package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditCheckVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.app.service.DevopsCdPipelineService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/7 23:42
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/pipeline_records/{pipeline_record_id}")
public class DevopsCdPipelineAuditController {

    @Autowired
    private DevopsCdPipelineService devopsCdPipelineService;

    /**
     * 审核人工卡点任务
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "审核人工卡点任务")
    @PostMapping("/stage_records/{stage_record_id}/job_records/{job_record_id}/audit")
    public ResponseEntity<AuditResultVO> auditJob(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "流水线记录Id", required = true)
            @PathVariable(value = "pipeline_record_id") Long pipelineRecordId,
            @Encrypt
            @ApiParam(value = "阶段记录Id", required = true)
            @PathVariable(value = "stage_record_id") Long stageRecordId,
            @Encrypt
            @ApiParam(value = "任务记录Id", required = true)
            @PathVariable(value = "job_record_id") Long jobRecordId,
            @ApiParam(value = "审核结果，拒绝：refused、通过passed", required = true)
            @RequestParam(value = "result") String result) {
        return ResponseEntity.ok(devopsCdPipelineService.auditJob(projectId, pipelineRecordId, stageRecordId, jobRecordId, result));
    }

    /**
     * 校验stage、job审核状态是否改变
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验stage、job审核状态是否改变")
    @PostMapping("check_audit_status")
    public ResponseEntity<AduitStatusChangeVO> checkAuditStatus(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "流水线记录Id", required = true)
            @PathVariable(value = "pipeline_record_id") Long pipelineRecordId,
            @RequestBody AuditCheckVO auditCheckVO) {
        return ResponseEntity.ok(devopsCdPipelineService.checkAuditStatus(projectId, pipelineRecordId, auditCheckVO));
    }
}
