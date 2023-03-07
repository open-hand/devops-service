package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsCiJobLogVO;
import io.choerodon.devops.api.vo.SonarInfoVO;
import io.choerodon.devops.api.vo.SonarQubeConfigVO;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:29
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_jobs")
public class DevopsCiJobController {
    private final DevopsCiJobService devopsCiJobService;


    public DevopsCiJobController(DevopsCiJobService devopsCiJobService) {
        this.devopsCiJobService = devopsCiJobService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/sonar/connect")
    @ApiOperation("sonar的连接测试")
    public ResponseEntity<Boolean> sonarConnect(
            @ApiParam("项目 id")
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody SonarQubeConfigVO sonarQubeConfigVO) {
        return ResponseEntity.ok(devopsCiJobService.sonarConnect(projectId, sonarQubeConfigVO));
    }

    /**
     * 返回应用服务对应的sonar配置（给质量管理团队使用）
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/sonar/config")
    @ApiOperation("返回应用服务对应的sonar配置（给质量管理团队使用）")
    public ResponseEntity<SonarInfoVO> getSonarConfig(
            @ApiParam("项目 id")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam("应用服务 id")
            @RequestParam(name = "appServiceId", required = false) Long appServiceId,
            @ApiParam("应用服务编码")
            @RequestParam(name = "appServiceCode", required = false) String code) {
        return ResponseEntity.ok(devopsCiJobService.getSonarConfig(projectId, appServiceId, code));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询job日志")
    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/trace")
    public ResponseEntity<DevopsCiJobLogVO> queryTrace(
            @ApiParam("项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam("gitlab project id")
            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
            @ApiParam("流水线任务 id")
            @PathVariable(value = "job_id") Long jobId,
            @Encrypt
            @ApiParam("应用服务 id")
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(devopsCiJobService.queryTrace(gitlabProjectId, jobId, appServiceId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重试job")
    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/retry")
    public ResponseEntity<Void> retryJob(
            @ApiParam("项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam("gitlab project id")
            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
            @ApiParam("流水线任务 id")
            @PathVariable(value = "job_id") Long jobId,
            @Encrypt
            @ApiParam("应用服务 id")
            @RequestParam(value = "app_service_id") Long appServiceId) {
        devopsCiJobService.retryJob(projectId, gitlabProjectId, jobId, appServiceId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "执行 manul状态 job")
    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/play")
    public ResponseEntity<Void> playJob(
            @ApiParam("项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam("gitlab project id")
            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
            @ApiParam("流水线任务 id")
            @PathVariable(value = "job_id") Long jobId,
            @Encrypt
            @ApiParam("应用服务 id")
            @RequestParam(value = "app_service_id") Long appServiceId) {
        devopsCiJobService.playJob(projectId, gitlabProjectId, jobId, appServiceId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("获取job指定sequence的step的maven构建的settings文件内容")
    @Permission(permissionPublic = true)
    @GetMapping("/maven_settings/{id}")
    public ResponseEntity<String> querySettingsById(
            @ApiParam("猪齿鱼项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务token", required = true)
            @RequestParam(value = "token") String token,
            @ApiParam("猪齿鱼中流水线job id")
            @PathVariable(value = "id") Long id) {
        String response = devopsCiJobService.queryMavenSettings(projectId, token, id);
        return response == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }
}
