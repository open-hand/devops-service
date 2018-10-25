package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Map;
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
     * @return String
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "项目下创建环境")
    @PostMapping
    public ResponseEntity<String> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用信息", required = true)
            @RequestBody DevopsEnviromentDTO devopsEnviromentDTO) {
        return Optional.ofNullable(devopsEnvironmentService.create(projectId, devopsEnviromentDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.create"));
    }

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
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
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
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
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
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
     * 项目下查询单个环境的可执行shell
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @return String
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "项目下查询单个环境的可执行shell")
    @GetMapping("/{environmentId}/shell")
    public ResponseEntity<String> queryShell(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable Long environmentId,
            @ApiParam(value = "是否更新")
            @RequestParam(required = false) Boolean update) {
        return Optional.ofNullable(devopsEnvironmentService.queryShell(environmentId, update))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.shell.get"));
    }

    /**
     * 项目下启用停用环境
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @param active        是否可用
     * @return Boolean
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "项目下启用停用环境")
    @PutMapping("/{environmentId}/active")
    public ResponseEntity<Boolean> enableOrDisableEnv(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable Long environmentId,
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
            roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "项目下查询单个环境")
    @GetMapping("/{environmentId}")
    public ResponseEntity<DevopsEnvironmentUpdateDTO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable Long environmentId) {
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
            InitRoleCode.DEPLOY_ADMINISTRATOR
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
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
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
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "创建环境校验名称是否存在")
    @GetMapping(value = "/checkName")
    public ResponseEntity checkName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境名", required = true)
            @RequestParam(value = "name") String name) {
        devopsEnvironmentService.checkName(projectId, name);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 创建环境校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      应用code
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "创建环境校验编码是否存在")
    @GetMapping(value = "/checkCode")
    public ResponseEntity checkCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境编码", required = true)
            @RequestParam(value = "code") String code) {
        devopsEnvironmentService.checkCode(projectId, code);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下查询有正在运行实例的环境
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
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
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "查询环境同步状态")
    @GetMapping(value = "/{envId}/status")
    public ResponseEntity<EnvSyncStatusDTO> queryEnvSyncStatus(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "envId") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.queryEnvSyncStatus(projectId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.sync.get"));
    }

    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @CustomPageRequest
    @ApiOperation(value = "环境下查询用户权限")
    @GetMapping(value = "/{envId}/perssion")
    public ResponseEntity<Page<EnvUserPermissionDTO>> queryUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "envId") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.pageUserPermission(projectId, envId, pageRequest))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.user.permission.get"));
    }

    /**
     * 环境下为用户分配权限
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param perssionMap 用户权限map
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "环境下为用户分配权限")
    @PostMapping(value = "/{envId}/perssion")
    public ResponseEntity updateEnvUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "envId") Long envId,
            @ApiParam(value = "用户权限map")
            @RequestBody Map<String, Boolean> perssionMap) {
        devopsEnvironmentService.updateEnvUserPermission(envId, perssionMap);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
