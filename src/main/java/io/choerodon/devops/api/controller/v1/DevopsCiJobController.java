package io.choerodon.devops.api.controller.v1;

import io.choerodon.devops.api.vo.SonarInfoVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
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
    private DevopsCiJobService devopsCiJobService;


    public DevopsCiJobController(DevopsCiJobService devopsCiJobService) {
        this.devopsCiJobService = devopsCiJobService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @PostMapping("/sonar/connect")
    @ApiOperation("sonar的连接测试")
    public ResponseEntity<Boolean> sonarConnect(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody SonarQubeConfigVO sonarQubeConfigVO) {
        return ResponseEntity.ok(devopsCiJobService.sonarConnect(projectId, sonarQubeConfigVO));
    }
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @GetMapping("/sonar/default")
    @ApiOperation("sonar默认配置")
    public ResponseEntity<SonarInfoVO> getSonarDefault() {
        return ResponseEntity.ok(devopsCiJobService.getSonarDefault());
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询job日志")
    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/trace")
    public ResponseEntity<String> queryTrace(
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
            @PathVariable(value = "job_id") Long jobId) {
        return ResponseEntity.ok(devopsCiJobService.queryTrace(gitlabProjectId, jobId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试job")
    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/retry")
    public ResponseEntity<Void> retryJob(
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
            @PathVariable(value = "job_id") Long jobId) {
        devopsCiJobService.retryJob(projectId, gitlabProjectId, jobId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("获取job指定sequence的step的maven构建的settings文件内容")
    @Permission(permissionPublic = true)
    @GetMapping("/maven_settings")
    public ResponseEntity<String> querySettings(
            @ApiParam("猪齿鱼项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务token", required = true)
            @RequestParam(value = "token") String token,
            @ApiParam("猪齿鱼中流水线job id")
            @RequestParam(value = "job_id") Long job_id,
            @ApiParam("猪齿鱼中流水线的step的sequence")
            @RequestParam(value = "sequence") Long sequence) {
        String response = devopsCiJobService.queryMavenSettings(projectId, token, job_id, sequence);
        return response == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }


    /**
     * 查询上传的软件包的url
     *
     * @param token        应用服务token
     * @param commit       ci的commit值
     * @param ciPipelineId 流水线id
     * @param ciJobId      流水线的job id
     * @param artifactName 软件包名称
     * @return 状态码200 表示ok并返回url， 404表示未找到
     */
    @Permission(permissionPublic = true)
    @ApiOperation("查询上传的软件包的url, 状态码200 表示ok并返回url， 404表示未找到")
    @GetMapping("/artifact_url")
    public ResponseEntity<String> queryArtifactUrl(
            @ApiParam("猪齿鱼项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务token", required = true)
            @RequestParam(value = "token") String token,
            @ApiParam(value = "此次ci的commit", required = true)
            @RequestParam(value = "commit") String commit,
            @ApiParam(value = "gitlab内置的流水线id", required = true)
            @RequestParam(value = "ci_pipeline_id") Long ciPipelineId,
            @ApiParam(value = "gitlab内置的jobId", required = true)
            @RequestParam(value = "ci_job_id") Long ciJobId,
            @ApiParam(value = "创建ci流水线时的软件包名称", required = true)
            @RequestParam(value = "artifact_name") String artifactName) {
        String url = devopsCiJobService.queryArtifactUrl(token, commit, ciPipelineId, ciJobId, artifactName);
        return url == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(url);
    }

}
