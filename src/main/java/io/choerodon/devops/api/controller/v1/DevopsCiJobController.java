package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.SonarQubeConfigVO;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;

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

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @PostMapping("/sonar/connect")
    @ApiOperation("sonar的连接测试")
    public ResponseEntity<Boolean> sonarConnect(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody SonarQubeConfigVO sonarQubeConfigVO) {
        return ResponseEntity.ok(devopsCiJobService.sonarConnect(projectId, sonarQubeConfigVO));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询job日志")
    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/trace")
    public ResponseEntity<String> queryTrace(
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
            @PathVariable(value = "job_id") Long jobId) {
        return ResponseEntity.ok(devopsCiJobService.queryTrace(gitlabProjectId, jobId));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试job")
    @GetMapping("/gitlab_projects/{gitlab_project_id}/gitlab_jobs/{job_id}/retry")
    public ResponseEntity<JobDTO> retryJob(
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
            @PathVariable(value = "job_id") Long jobId) {
        return ResponseEntity.ok(devopsCiJobService.retryJob(gitlabProjectId, jobId));
    }

    @ApiOperation("获取job指定sequence的step的maven构建的settings文件内容")
    @Permission(permissionPublic = true)
    @GetMapping("/maven_settings")
    public ResponseEntity<String> querySettings(
            @ApiParam("猪齿鱼项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam("猪齿鱼中流水线job id")
            @RequestParam(value = "job_id") Long job_id,
            @ApiParam("猪齿鱼中流水线的step的sequence")
            @RequestParam(value = "sequence") Long sequence) {
        return ResponseEntity.ok(devopsCiJobService.queryMavenSettings(projectId, job_id, sequence));
    }
}
