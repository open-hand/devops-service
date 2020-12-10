package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCdPipelineService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/3 17:24
 */
@RestController
@RequestMapping("/v1/cd_pipeline")
public class DevopsCdPipelineController {

    @Autowired
    private DevopsCdPipelineService devopsCdPipelineService;
    @Autowired
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;

    /**
     * 主机模式镜像部署接口
     */
    @Permission(permissionWithin = true)
    @ApiOperation(value = "主机模式部署接口")
    @PostMapping(value = "/cd_host_deploy")
    public ResponseEntity<Void> cdHostDeploy(
            @Encrypt
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @Encrypt
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @Encrypt
            @RequestParam(value = "job_record_id") Long jobRecordId) {
        devopsCdPipelineRecordService.cdHostDeploy(pipelineRecordId, stageRecordId, jobRecordId);
        return Results.success();
    }


    /**
     * 触发环境自动部署
     */
    @Permission(permissionWithin = true)
    @ApiOperation(value = "环境部署")
    @PostMapping(value = "/env_auto_deploy")
    public ResponseEntity<Void> envAutoDeploy(
            @Encrypt
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @Encrypt
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @Encrypt
            @RequestParam(value = "job_record_id") Long jobRecordId) {
        devopsCdPipelineService.envAutoDeploy(pipelineRecordId, stageRecordId, jobRecordId);
        return Results.success();
    }

    /**
     * 接收任务状态
     *
     * @param pipelineRecordId 流水线记录Id
     * @param stageRecordId    阶段记录Id
     * @param jobRecordId      任务Id
     */
    @Permission(permissionWithin = true)
    @ApiOperation(value = "接收任务状态")
    @PutMapping("/auto_deploy/status")
    public ResponseEntity<Void> setAppDeployStatus(
            @Encrypt
            @ApiParam(value = "流水线记录Id", required = true)
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @Encrypt
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @Encrypt
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "job_record_id") Long jobRecordId,
            @ApiParam(value = "状态", required = true)
            @RequestParam(value = "status") Boolean status) {
        devopsCdPipelineService.setAppDeployStatus(pipelineRecordId, stageRecordId, jobRecordId, status);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 接收任务状态
     *
     * @param pipelineRecordId 流水线记录Id
     * @param stageRecordId    阶段记录Id
     * @param jobRecordId      任务Id
     */
    @Permission(permissionWithin = true)
    @ApiOperation(value = "查询任务状态")
    @GetMapping("/job/status")
    public ResponseEntity<String> getJobStatus(
            @Encrypt
            @ApiParam(value = "流水线记录Id", required = true)
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @Encrypt
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @Encrypt
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "job_record_id") Long jobRecordId) {
        return Results.success(devopsCdPipelineService.getDeployStatus(pipelineRecordId, stageRecordId, jobRecordId));
    }

    /**
     * 执行API测试任务
     */
    @Permission(permissionWithin = true)
    @ApiOperation(value = "执行api测试")
    @PostMapping(value = "/execute_api_test_task")
    public ResponseEntity<Void> executeApiTestTask(
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @RequestParam(value = "job_record_id") Long jobRecordId) {
        devopsCdPipelineService.executeApiTestTask(pipelineRecordId, stageRecordId, jobRecordId);
        return Results.success();
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "查询部署任务状态")
    @GetMapping(value = "/deploy_status")
    public ResponseEntity<String> getDeployStatus(
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @RequestParam(value = "deploy_job_name") String deployJobName) {
        return ResponseEntity.ok(devopsCdPipelineService.getDeployStatus(pipelineRecordId, deployJobName));
    }


    @Permission(permissionWithin = true)
    @ApiOperation(value = "执行外部卡点任务")
    @PostMapping(value = "/execute_external_approval_task")
    public ResponseEntity<Void> executeExternalApprovalTask(
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @RequestParam(value = "job_record_id") Long jobRecordId) {
        devopsCdPipelineService.executeExternalApprovalTask(pipelineRecordId, stageRecordId, jobRecordId);
        return ResponseEntity.noContent().build();
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "接收外部卡点任务状态")
    @PutMapping("/external_approval_task/status")
    public ResponseEntity<Void> setExternalApprovalTaskStatus(
            @ApiParam(value = "流水线记录Id", required = true)
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "job_record_id") Long jobRecordId,
            @ApiParam(value = "状态", required = true)
            @RequestParam(value = "status") Boolean status) {
        devopsCdPipelineService.setExternalApprovalTaskStatus(pipelineRecordId, stageRecordId, jobRecordId, status);
        return ResponseEntity.noContent().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "外部卡点任务执行回调接口")
    @PutMapping("/external_approval_task/callback")
    public ResponseEntity<Void> externalApprovalTaskCallback(
            @ApiParam(value = "流水线记录Id", required = true)
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "job_record_id") Long jobRecordId,
            @ApiParam(value = "外部卡点任务回调认证token", required = true)
            @RequestParam(value = "callback_token") String callbackToken,
            @ApiParam(value = "状态", required = true)
            @RequestParam(value = "approval_status") Boolean status) {
        devopsCdPipelineService.externalApprovalTaskCallback(pipelineRecordId, stageRecordId, jobRecordId, callbackToken, status);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询外部卡点任务执行回调接口URL")
    @GetMapping("/external_approval_task/callback_url")
    public ResponseEntity<String> queryCallbackUrl() {
        return ResponseEntity.ok(devopsCdPipelineService.queryCallbackUrl());
    }
}
