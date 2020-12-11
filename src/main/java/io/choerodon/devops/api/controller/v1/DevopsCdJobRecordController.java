package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.PodEventVO;
import io.choerodon.devops.app.service.DevopsCdJobRecordService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/7 16:57
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/pipeline_records/{pipeline_record_id}/stage_records/{stage_record_id}/job_records")
public class DevopsCdJobRecordController {

    @Autowired
    private DevopsCdJobRecordService devopsCdJobRecordService;

    /**
     * 重试cd_job
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重试cd_job")
    @PutMapping("/{job_record_id}")
    public ResponseEntity<Void> retryCdJob(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "pipeline_record_id") Long pipelineRecordId,
            @Encrypt
            @PathVariable(value = "stage_record_id") Long stageRecordId,
            @Encrypt
            @PathVariable(value = "job_record_id") Long jobRecordId) {
        devopsCdJobRecordService.retryCdJob(projectId, pipelineRecordId, stageRecordId, jobRecordId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 查看部署任务日志
     *
     * @param projectId
     * @param pipelineRecordId
     * @param stageRecordId
     * @param jobRecordId
     * @return
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查看部署任务日志")
    @GetMapping("/{job_record_id}/logs")
    public ResponseEntity<List<PodEventVO>> queryDeployJobLogs(@PathVariable(value = "project_id") Long projectId,
                                                               @Encrypt
                                                               @PathVariable(value = "pipeline_record_id") Long pipelineRecordId,
                                                               @Encrypt
                                                               @PathVariable(value = "stage_record_id") Long stageRecordId,
                                                               @Encrypt
                                                               @PathVariable(value = "job_record_id") Long jobRecordId) {
        return ResponseEntity.ok(devopsCdJobRecordService.queryDeployJobLogs(projectId, pipelineRecordId, stageRecordId, jobRecordId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查看主机部署日志")
    @GetMapping("/log/{job_record_id}")
    public ResponseEntity<String> getHostLog(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "pipeline_record_id") Long pipelineRecordId,
            @Encrypt
            @PathVariable(value = "stage_record_id") Long stageRecordId,
            @Encrypt
            @PathVariable(value = "job_record_id") Long jobRecordId) {
        return Results.success(devopsCdJobRecordService.getHostLogById(jobRecordId));
    }
}
