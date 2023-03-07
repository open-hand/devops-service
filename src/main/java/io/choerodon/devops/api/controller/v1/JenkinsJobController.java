package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.jenkins.JenkinsBuildInfo;
import io.choerodon.devops.api.vo.jenkins.JenkinsJobVO;
import io.choerodon.devops.api.vo.jenkins.PropertyVO;
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

}
