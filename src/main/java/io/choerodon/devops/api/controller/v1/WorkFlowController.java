package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PipelineService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    @ApiOperation(value = "触发自动部署")
    @GetMapping("/auto_deploy")
    public ResponseEntity autoDeploy(
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "task_id") Long taskId) {
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
    @ApiOperation(value = "接收任务状态")
    @PutMapping("/status")
    public ResponseEntity setAppDeployStatusTask(
            @ApiParam(value = "流水线记录Id", required = true)
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "task_id") Long taskId,
            @ApiParam(value = "状态", required = true)
            @RequestParam(value = "status") Boolean status) {
        pipelineService.setAppDeployStatus(pipelineRecordId, stageRecordId, taskId, status);
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
    @GetMapping("/status")
    public ResponseEntity<String> getAppDeployStatusTask(
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "task_id") Long taskId) {
        return Optional.ofNullable(pipelineService.getAppDeployStatus(stageRecordId, taskId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.get.deploy.status"));
    }
}
