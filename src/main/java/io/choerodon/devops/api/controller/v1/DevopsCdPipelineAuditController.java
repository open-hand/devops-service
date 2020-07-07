package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
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
     * 审核手动流转阶段
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "审核手动流转阶段")
    @PostMapping("/stage_records/{stage_id}/audit")
    public ResponseEntity<Void> auditStage(@PathVariable(value = "project_id") Long projectId,
                                           @PathVariable(value = "pipeline_record_id") Long pipelineRecordId,
                                           @PathVariable(value = "stage_id") Long stageId,
                                           @PathVariable(value = "result") String result) {
        devopsCdPipelineService.auditStage(projectId, pipelineRecordId, stageId, result);
        return Results.success();
    }
}
