package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
     * 启动cd流水线
     *
     * @param token
     * @return
     */
    @Permission(permissionPublic = true)
    @ApiOperation(value = "创建应用服务版本")
    @PostMapping
    public ResponseEntity<Void> triggerCdPipeline(@RequestParam(value = "token") String token,
                                                  @RequestParam(value = "commit") String commit) {
        devopsCdPipelineService.triggerCdPipeline(token, commit);
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
    @ApiOperation(value = "主机模式镜像部署接口")
    @PostMapping(value = "/cd_host_image")
    public ResponseEntity<Boolean> cdHostImageDeploy(@RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
                                                     @RequestParam(value = "stage_record_id") Long stageRecordId,
                                                     @RequestParam(value = "job_record_id") Long jobRecordId) {
        return Results.success(devopsCdPipelineRecordService.cdHostImageDeploy(pipelineRecordId, stageRecordId, jobRecordId));
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
    @ApiOperation(value = "主机模式jar部署接口")
    @PostMapping(value = "/cd_host_jar")
    public ResponseEntity<Boolean> cdHostJarDeploy(@RequestParam(value = "pipeline_record_id") Long pipelineRecordId,
                                                   @RequestParam(value = "stage_record_id") Long stageRecordId,
                                                   @RequestParam(value = "job_record_id") Long jobRecordId) {
        return Results.success(devopsCdPipelineRecordService.cdHostJarDeploy(pipelineRecordId, stageRecordId, jobRecordId));
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


}
