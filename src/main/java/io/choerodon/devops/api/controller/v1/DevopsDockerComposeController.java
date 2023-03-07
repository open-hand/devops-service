package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.CreateGroup;
import io.choerodon.devops.api.validator.UpdateGroup;
import io.choerodon.devops.api.vo.DockerComposeDeployVO;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.app.service.DockerComposeService;
import io.choerodon.devops.infra.dto.DockerComposeValueDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Sheep on 2019/7/30.
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/docker_composes")
@Api(value = "docker compose应用部署相关接口")
public class DevopsDockerComposeController {
    @Autowired
    private DockerComposeService dockerComposeService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "部署docker_compose应用")
    @PostMapping
    public ResponseEntity<Void> deployDockerComposeApp(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Validated(CreateGroup.class) DockerComposeDeployVO dockerComposeDeployVO) {
        dockerComposeService.deployDockerComposeApp(projectId, dockerComposeDeployVO);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新docker_compose应用")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDockerComposeApp(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "docker_compose应用Id", required = true)
            @PathVariable(value = "id") Long id,
            @RequestBody @Validated(UpdateGroup.class) DockerComposeDeployVO dockerComposeDeployVO) {
        dockerComposeService.updateDockerComposeApp(projectId, id, null, null, dockerComposeDeployVO, false);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重新部署docker_compose应用")
    @PutMapping("/{id}/restart")
    public ResponseEntity<Void> restartDockerComposeApp(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "docker_compose应用Id", required = true)
            @PathVariable(value = "id") Long id) {
        dockerComposeService.restartDockerComposeApp(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询docker-compose应用包含的容器列表")
    @GetMapping("/{id}/containers")
    public ResponseEntity<Page<DevopsDockerInstanceVO>> pageContainers(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "docker_compose应用Id", required = true)
            @PathVariable(value = "id") Long id,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "搜索参数，docker_compose应用名称")
            @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "搜索参数，docker_compose应用名称")
            @RequestParam(value = "param", required = false) String param) {
        return ResponseEntity.ok(dockerComposeService.pageContainers(projectId, id, pageable, name, param));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "停用容器")
    @PutMapping("/{id}/containers/{instance_id}/stop")
    public ResponseEntity<Page<DevopsDockerInstanceVO>> stopContainer(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "docker_compose应用Id", required = true)
            @PathVariable(value = "id") Long id,
            @Encrypt
            @ApiParam(value = "docker容器实例id", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        dockerComposeService.stopContainer(projectId, id, instanceId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "启用容器")
    @PutMapping("/{id}/containers/{instance_id}/start")
    public ResponseEntity<Page<DevopsDockerInstanceVO>> startContainer(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "docker_compose应用Id", required = true)
            @PathVariable(value = "id") Long id,
            @Encrypt
            @ApiParam(value = "docker容器实例id", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        dockerComposeService.startContainer(projectId, id, instanceId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重启容器")
    @PutMapping("/{id}/containers/{instance_id}/restart")
    public ResponseEntity<Page<DevopsDockerInstanceVO>> restartContainer(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "docker_compose应用Id", required = true)
            @PathVariable(value = "id") Long id,
            @Encrypt
            @ApiParam(value = "docker容器实例id", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        dockerComposeService.restartContainer(projectId, id, instanceId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除容器")
    @PutMapping("/{id}/containers/{instance_id}/remove")
    public ResponseEntity<Page<DevopsDockerInstanceVO>> removeContainer(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "docker_compose应用Id", required = true)
            @PathVariable(value = "id") Long id,
            @Encrypt
            @ApiParam(value = "docker容器实例id", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        dockerComposeService.removeContainer(projectId, id, instanceId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询部署配置列表")
    @GetMapping("/{id}/value_records")
    public ResponseEntity<Page<DockerComposeValueDTO>> listValueRecords(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "docker_compose应用Id", required = true)
            @PathVariable(value = "id") Long id,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "搜索参数，部署备注名称")
            @RequestParam(value = "search_param", required = false) String searchParam) {
        return ResponseEntity.ok(dockerComposeService.listValueRecords(projectId, id, pageable, searchParam));
    }


}
