package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.DevopsEnviromentDTO;
import io.choerodon.devops.api.dto.DevopsEnviromentRepDTO;
import io.choerodon.devops.api.dto.DevopsEnvironmentUpdateDTO;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by younger on 2018/4/9.
 */
@RestController
@RequestMapping(value = "/v1/project/{projectId}/envs")
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下创建环境")
    @PostMapping
    public ResponseEntity<String> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下查询存在网络环境")
    @GetMapping(value = "/deployed")
    public ResponseEntity<List<DevopsEnviromentRepDTO>> listByProjectIdDeployed(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId) {
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下查询环境")
    @GetMapping
    public ResponseEntity<List<DevopsEnviromentRepDTO>> listByProjectIdAndActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam Boolean active) {
        return Optional.ofNullable(devopsEnvironmentService.listByProjectIdAndActive(projectId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.get"));
    }

    /**
     * 项目下查询单个环境的可执行shell
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @param environmentId 是否更新
     * @return String
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下查询单个环境的可执行shell")
    @GetMapping("/{environmentId}/shell")
    public ResponseEntity<String> queryShell(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下启用停用环境")
    @PutMapping("/{environmentId}/active")
    public ResponseEntity<Boolean> queryByEnvIdAndActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable Long environmentId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam Boolean active) {
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下查询单个环境")
    @GetMapping("/{environmentId}")
    public ResponseEntity<DevopsEnvironmentUpdateDTO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下更新环境")
    @PutMapping
    public ResponseEntity<DevopsEnvironmentUpdateDTO> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下环境流水线排序")
    @PutMapping("/sort")
    public ResponseEntity<List<DevopsEnviromentRepDTO>> sort(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "创建环境校验名称是否存在")
    @GetMapping(value = "/checkName")
    public ResponseEntity checkName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "环境名", required = true)
            @RequestParam String name) {
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "创建环境校验编码是否存在")
    @GetMapping(value = "/checkCode")
    public ResponseEntity checkCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "环境编码", required = true)
            @RequestParam String code) {
        devopsEnvironmentService.checkCode(projectId, code);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下查询有正在运行实例的环境
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下查询有正在运行实例的环境")
    @GetMapping(value = "/instance")
    public ResponseEntity<List<DevopsEnviromentRepDTO>> listByProjectId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId) {
        return Optional.ofNullable(devopsEnvironmentService.listByProjectId(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.get"));
    }
}
