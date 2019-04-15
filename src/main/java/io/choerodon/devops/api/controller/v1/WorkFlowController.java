package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:16 2019/4/15
 * Description:
 */
@RestController
@RequestMapping(value = "/workflow")
public class WorkFlowController {
    @Autowired
    private PipelineService pipelineService;

    /**
     * 触发自动部署
     *
     * @param stageRecordId 阶段记录Id
     * @param taskId        任务Id
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "触发自动部署")
    @GetMapping("/auto_deploy")
    public ResponseEntity autoDeploy(
            @RequestParam Long stageRecordId,
            @ApiParam(value = "taskId", required = true)
            @RequestParam Long taskId) {
        pipelineService.autoDeploy(stageRecordId, taskId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 接收任务状态
     *
     * @param pipelineRecordId 流水线记录Id
     * @param stageRecordId    阶段记录Id
     * @param taskId           任务Id
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "接收任务状态")
    @GetMapping("/status")
    public ResponseEntity setAppDeployStatus(
            @ApiParam(value = "pipeline_record_id", required = true)
            @RequestParam Long pipelineRecordId,
            @ApiParam(value = "stage_record_id", required = true)
            @RequestParam Long stageRecordId,
            @ApiParam(value = "task_id", required = true)
            @RequestParam Long taskId) {
        pipelineService.setAppDeployStatus(pipelineRecordId, stageRecordId, taskId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 检测部署任务生成实例状态
     *
     * @param taskId
     * @param stageRecordId
     * @return
     */
    @ApiOperation(value = "检测部署任务生成实例状态")
    @GetMapping("/app_deploy/status")
    public ResponseEntity<String> getAppDeployStatus(
            @ApiParam(value = "stage_record_id", required = true)
            @RequestParam Long stageRecordId,
            @ApiParam(value = "taskId", required = true)
            @RequestParam Long taskId) {
        return Optional.ofNullable(pipelineService.getAppDeployStatus(stageRecordId, taskId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.get.deploy.status"));
    }
}
