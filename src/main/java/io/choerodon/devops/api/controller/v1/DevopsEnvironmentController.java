package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by younger on 2018/4/9.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/envs")
public class DevopsEnvironmentController {

    private DevopsEnvironmentService devopsEnvironmentService;

    public DevopsEnvironmentController(DevopsEnvironmentService devopsEnvironmentService) {
        this.devopsEnvironmentService = devopsEnvironmentService;
    }

    /**
     * 项目下创建环境
     *
     * @param projectId           项目id
     * @param devopsEnviromentDTO 环境信息
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建环境")
    @PostMapping
    public void create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用信息", required = true)
            @RequestBody DevopsEnviromentDTO devopsEnviromentDTO) {
        devopsEnvironmentService.create(projectId, devopsEnviromentDTO);
    }

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询存在网络环境")
    @GetMapping(value = "/deployed")
    public ResponseEntity<List<DevopsEnviromentRepDTO>> listByProjectIdDeployed(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(devopsEnvironmentService.listDeployed(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.service.environment.get"));
    }

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @param active    是否启用
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询环境")
    @GetMapping
    public ResponseEntity<List<DevopsEnviromentRepDTO>> listByProjectIdAndActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam(value = "active") Boolean active) {
        return Optional.ofNullable(devopsEnvironmentService.listByProjectIdAndActive(projectId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.get"));
    }


    /**
     * 项目下环境流水线查询环境
     *
     * @param projectId 项目id
     * @param active    是否启用
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下环境流水线查询环境")
    @GetMapping("/groups")
    public ResponseEntity<List<DevopsEnvGroupEnvsDTO>> listByProjectIdAndActiveWithGroup(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam Boolean active) {
        return Optional.ofNullable(devopsEnvironmentService.listDevopsEnvGroupEnvs(projectId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.get"));
    }



    /**
     * 项目下启用停用环境
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @param active        是否可用
     * @return Boolean
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下启用停用环境")
    @PutMapping("/{environment_id}/active")
    public ResponseEntity<Boolean> enableOrDisableEnv(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "environment_id") Long environmentId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam(value = "active") Boolean active) {
        return Optional.ofNullable(devopsEnvironmentService.activeEnvironment(projectId, environmentId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.active"));
    }

    /**
     * 项目下查询单个环境
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @return DevopsEnvironmentUpdateDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询单个环境")
    @GetMapping("/{environment_id}")
    public ResponseEntity<DevopsEnvironmentUpdateDTO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "environment_id") Long environmentId) {
        return Optional.ofNullable(devopsEnvironmentService.query(environmentId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.query"));
    }

    /**
     * 项目下更新环境
     *
     * @param projectId                  项目id
     * @param devopsEnvironmentUpdateDTO 环境信息
     * @return DevopsEnvironmentUpdateDTO
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {
            InitRoleCode.PROJECT_OWNER
    })
    @ApiOperation(value = "项目下更新环境")
    @PutMapping
    public ResponseEntity<DevopsEnvironmentUpdateDTO> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境信息", required = true)
            @RequestBody DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO) {
        return Optional.ofNullable(devopsEnvironmentService.update(devopsEnvironmentUpdateDTO, projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.update"));
    }

    /**
     * 项目下环境流水线排序
     *
     * @param projectId      项目id
     * @param environmentIds 环境列表
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下环境流水线排序")
    @PutMapping("/sort")
    public ResponseEntity<DevopsEnvGroupEnvsDTO> sort(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境列表", required = true)
            @RequestBody Long[] environmentIds) {
        return Optional.ofNullable(devopsEnvironmentService.sort(environmentIds))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.sort"));
    }

    /**
     * 创建环境校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      环境名
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建环境校验名称是否存在")
    @GetMapping(value = "/checkName")
    public ResponseEntity checkName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Id")
            @RequestParam(required = true) Long clusterId,
            @ApiParam(value = "环境名", required = true)
            @RequestParam(value = "name") String name) {
        devopsEnvironmentService.checkName(projectId, clusterId, name);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 创建环境校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      应用code
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建环境校验编码是否存在")
    @GetMapping(value = "/checkCode")
    public ResponseEntity checkCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Id")
            @RequestParam(required = true) Long clusterId,
            @ApiParam(value = "环境编码", required = true)
            @RequestParam(value = "code") String code) {
        devopsEnvironmentService.checkCode(projectId, clusterId, code);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下查询有正在运行实例的环境
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询有正在运行实例的环境")
    @GetMapping(value = "/instance")
    public ResponseEntity<List<DevopsEnviromentRepDTO>> listByProjectId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id")
            @RequestParam(required = false) Long appId) {
        return Optional.ofNullable(devopsEnvironmentService.listByProjectId(projectId, appId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.running.get"));
    }


    /**
     * 查询环境同步状态
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return EnvSyncStatusDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询环境同步状态")
    @GetMapping(value = "/{env_id}/status")
    public ResponseEntity<EnvSyncStatusDTO> queryEnvSyncStatus(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.queryEnvSyncStatus(projectId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.sync.get"));
    }

    /**
     * 分页查询项目下用户权限
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param envId       环境id
     * @return page
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @CustomPageRequest
    @ApiOperation(value = "分页查询项目下用户权限")
    @PostMapping(value = "/list")
    public ResponseEntity<Page<DevopsEnvUserPermissionDTO>> listUserPermissionByEnvId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params,
            @ApiParam(value = "环境id")
            @RequestParam(value = "env_id") String envId) {
        return Optional.ofNullable(devopsEnvironmentService
                .listUserPermissionByEnvId(projectId, pageRequest, params, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.user.permission.get"));
    }

    /**
     * 获取环境下所有用户权限
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return list
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "获取环境下所有用户权限")
    @GetMapping(value = "/{env_id}/list_all")
    public ResponseEntity<List<DevopsEnvUserPermissionDTO>> listAllUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.listAllUserPermission(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.user.permission.get"));
    }

    /**
     * 环境下为用户分配权限
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param userIds   有权限的用户ids
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "环境下为用户分配权限")
    @PostMapping(value = "/{env_id}/permission")
    public ResponseEntity<Boolean> updateEnvUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "有权限的用户ids")
            @RequestBody List<Long> userIds) {
        return Optional.ofNullable(devopsEnvironmentService.updateEnvUserPermission(projectId, envId, userIds))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.user.permission.update"));
    }

    /**
     * 删除已停用的环境
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return Boolean
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除已停用的环境")
    @DeleteMapping(value = "/{env_id}")
    public ResponseEntity deleteDeactivatedEnvironment(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId) {
        devopsEnvironmentService.deleteDeactivatedEnvironment(envId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下查询集群信息
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询集群信息")
    @GetMapping(value = "/clusters")
    public ResponseEntity<List<DevopsClusterRepDTO>> listDevopsClusters(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(devopsEnvironmentService.listDevopsCluster(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.cluster.query"));
    }
}
