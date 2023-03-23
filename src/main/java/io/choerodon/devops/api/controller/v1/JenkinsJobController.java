package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.jenkins.*;
import io.choerodon.devops.app.service.JenkinsJobService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 9:22
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/jenkins_jobs")
public class JenkinsJobController {

    @Autowired
    private JenkinsJobService jenkinsJobService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下所有JenkinsJob")
    @GetMapping("/all")
    public ResponseEntity<List<JenkinsJobVO>> listAll(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(jenkinsJobService.listAll(projectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询Job的执行参数")
    @GetMapping("/{name}/property")
    public ResponseEntity<List<PropertyVO>> listProperty(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        return ResponseEntity.ok(jenkinsJobService.listProperty(projectId, serverId, folder, name));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询多分支流水线中的分支")
    @PostMapping("/{name}/branchs")
    public ResponseEntity<List<String>> listBranch(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        return ResponseEntity.ok(jenkinsJobService.listBranch(projectId, serverId, folder, name));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "全新执行")
    @PostMapping("/{name}/build")
    public ResponseEntity<Void> build(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder,
            @RequestBody List<PropertyVO> properties) {
        jenkinsJobService.build(projectId, serverId, folder, name, properties);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "构建历史")
    @GetMapping("/{name}/build_history")
    public ResponseEntity<List<JenkinsBuildInfo>> listBuildHistory(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        return ResponseEntity.ok(jenkinsJobService.listBuildHistory(projectId, serverId, folder, name));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "停止构建")
    @PostMapping("/{name}/build/{build_id}/stop")
    public ResponseEntity<Void> stopBuild(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        jenkinsJobService.stopBuild(projectId, serverId, folder, name, buildId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重新执行")
    @PostMapping("/{name}/build/{build_id}/restart")
    public ResponseEntity<Void> retryBuild(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        jenkinsJobService.retryBuild(projectId, serverId, folder, name, buildId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "审核通过")
    @PutMapping("/{name}/build/{build_id}/audit_pass")
    public ResponseEntity<Void> auditPass(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder,
            @RequestParam(value = "inputId") String inputId,
            @RequestBody List<PropertyVO> properties) {
        jenkinsJobService.auditPass(projectId, serverId, folder, name, buildId, inputId, properties);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "审核拒绝")
    @PutMapping("/{name}/build/{build_id}/audit_refuse")
    public ResponseEntity<Void> auditRefuse(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder,
            @RequestParam(value = "inputId") String inputId) {
        jenkinsJobService.auditRefuse(projectId, serverId, folder, name, buildId, inputId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查看构建记录详情")
    @GetMapping("/{name}/build/{build_id}/info")
    public ResponseEntity<JenkinsBuildInfo> queryBuildInfo(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        return ResponseEntity.ok(jenkinsJobService.queryBuildInfo(projectId, serverId, folder, name, buildId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查看完整执行日志")
    @GetMapping("/{name}/build/{build_id}/log")
    public ResponseEntity<String> queryLog(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        return ResponseEntity.ok(jenkinsJobService.queryLog(projectId, serverId, folder, name, buildId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查看流水线阶段列表")
    @GetMapping("/{name}/build/{build_id}/stages")
    public ResponseEntity<List<JenkinsStageVO>> listStage(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        return ResponseEntity.ok(jenkinsJobService.listStage(projectId, serverId, folder, name, buildId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查看流水线阶段下的步骤列表")
    @GetMapping("/{name}/build/{build_id}/stages/{stage_id}/nodes")
    public ResponseEntity<List<JenkinsNodeVO>> listNode(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @PathVariable(value = "stage_id") Integer stageId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        return ResponseEntity.ok(jenkinsJobService.listNode(projectId, serverId, folder, name, buildId, stageId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查看流水线阶段下的步骤日志")
    @GetMapping("/{name}/build/{build_id}/stages/{stage_id}/nodes/{node_id}/log")
    public ResponseEntity<String> queryNodeLog(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable String name,
            @PathVariable(value = "build_id") Integer buildId,
            @PathVariable(value = "stage_id") Integer stageId,
            @PathVariable(value = "node_id") Integer nodeId,
            @Encrypt
            @RequestParam(value = "server_id") Long serverId,
            @RequestParam(value = "folder") String folder) {
        return ResponseEntity.ok(jenkinsJobService.queryNodeLog(projectId, serverId, folder, name, buildId, stageId, nodeId));
    }

}
