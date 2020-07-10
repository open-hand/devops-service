package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.app.service.DevopsCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCdPipelineService;
import io.choerodon.devops.infra.util.CustomContextUtil;
import io.choerodon.devops.infra.util.GitUserNameUtil;
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
     * 启动cd流水线
     *
     * @param token
     * @return
     */
    @Permission(permissionPublic = true)
    @ApiOperation(value = "启动cd流水线")
    @PostMapping("/trigger_cd_pipeline")
    public ResponseEntity<Void> triggerCdPipeline(@RequestParam(value = "token") String token,
                                                  @RequestParam(value = "commit") String commit,
                                                  @RequestParam(value = "ref") String ref,
                                                  @RequestParam(value = "gitlab_user_name") String gitlabUserName,
                                                  @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId) {
        // 设置用户上下文
        Long iamUserId = GitUserNameUtil.getIamUserIdByGitlabUserName(gitlabUserName);
        CustomContextUtil.setDefaultIfNull(iamUserId);

        devopsCdPipelineService.triggerCdPipeline(token, commit, ref, gitlabPipelineId);
        return Results.success();
    }

    /**
     * 主机模式镜像部署接口
     *
     * @param pipelineRecordId
     * @param stageRecordId
     * @param jobRecordId
     * @return
     */
    @Permission(permissionWithin = true)
    @ApiOperation(value = "主机模式部署接口")
    @PostMapping(value = "/cd_host_deploy")
    public ResponseEntity<Boolean> cdHostDeploy(@RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
                                                @RequestParam(value = "stage_record_id") Long stageRecordId,
                                                @RequestParam(value = "job_record_id") Long jobRecordId) {
        return Results.success(devopsCdPipelineRecordService.cdHostDeploy(pipelineRecordId, stageRecordId, jobRecordId));
    }


    /**
     * 触发环境自动部署
     *
     * @param pipelineRecordId
     * @param stageRecordId
     * @param jobRecordId
     * @return
     */
    @Permission(permissionWithin = true)
    @ApiOperation(value = "环境部署")
    @PostMapping(value = "/env_auto_deploy")
    public ResponseEntity<Void> envAutoDeploy(@RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
                                              @RequestParam(value = "stage_record_id") Long stageRecordId,
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
    public ResponseEntity setAppDeployStatus(
            @ApiParam(value = "流水线记录Id", required = true)
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "job_record_id") Long jobRecordId,
            @ApiParam(value = "状态", required = true)
            @RequestParam(value = "status") Boolean status) {
        devopsCdPipelineService.setAppDeployStatus(pipelineRecordId, stageRecordId, jobRecordId, status);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
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
            @ApiParam(value = "流水线记录Id", required = true)
            @RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
            @ApiParam(value = "阶段记录Id", required = true)
            @RequestParam(value = "stage_record_id") Long stageRecordId,
            @ApiParam(value = "任务Id", required = true)
            @RequestParam(value = "job_record_id") Long jobRecordId) {
        return Results.success(devopsCdPipelineService.getDeployStatus(pipelineRecordId, stageRecordId, jobRecordId));
    }

}
