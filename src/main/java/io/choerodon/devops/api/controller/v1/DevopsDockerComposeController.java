package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DockerComposeDeployVO;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.app.service.DockerComposeService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Sheep on 2019/7/30.
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/docker_composes")
public class DevopsDockerComposeController {
    @Autowired
    private DockerComposeService dockerComposeService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "docker_compose应用部署")
    @PostMapping
    public ResponseEntity<Void> deployDockerComposeApp(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Validated DockerComposeDeployVO dockerComposeDeployVO) {
        dockerComposeService.deployDockerComposeApp(projectId, dockerComposeDeployVO);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "docker_compose应用部署")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDockerComposeApp(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "id") Long id,
            @RequestBody @Validated DockerComposeDeployVO dockerComposeDeployVO) {
        dockerComposeService.updateDockerComposeApp(projectId, id, dockerComposeDeployVO);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "docker_compose应用重新部署")
    @PutMapping("/{id}/restart")
    public ResponseEntity<Void> restartDockerComposeApp(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "id") Long id) {
        dockerComposeService.restartDockerComposeApp(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "docker_compose应用重新部署")
    @GetMapping("/{id}/containers")
    public ResponseEntity<Page<DevopsDockerInstanceVO>> pageContainers(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(dockerComposeService.pageContainers(projectId, id));
    }


}
