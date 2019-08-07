package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.validator.EnvironmentApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/env/app_services")
public class DevopsEnvAppServiceController {

    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;

    @Autowired
    private EnvironmentApplicationValidator validator;

    /**
     * 创建环境下的服务关联
     *
     * @param devopsEnvAppServiceVO 环境和服务的关联关系
     * @return ApplicationRepDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建环境下的服务关联")
    @PostMapping("/batch_create")
    public ResponseEntity<List<DevopsEnvApplicationVO>> batch_create(
            @ApiParam(value = "关联信息", required = true)
            @RequestBody DevopsEnvAppServiceVO devopsEnvAppServiceVO) {
        validator.checkEnvIdExist(devopsEnvAppServiceVO.getEnvId());
        validator.checkAppIdsExist(devopsEnvAppServiceVO.getAppServiceIds());
        return Optional.ofNullable(devopsEnvApplicationService.batchCreate(devopsEnvAppServiceVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.create"));
    }

    /**
     * 查询环境下的所有服务
     *
     * @param envId 环境id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询环境下的所有服务")
    @GetMapping("/list_by_env")
    public ResponseEntity<List<AppServiceRepVO>> listAppByEnvId(
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvApplicationService.listAppByEnvId(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.query"));
    }

    /**
     * 查询服务在环境下的所有label
     *
     * @param envId        环境id
     * @param appServiceId 服务id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询服务在环境下的所有label")
    @GetMapping("/list_label")
    public ResponseEntity<List<DevopsEnvLabelVO>> listLabelByAppAndEnvId(
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "服务id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(devopsEnvApplicationService.listLabelByAppAndEnvId(envId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.label.query"));
    }


    /**
     * 查询服务在环境下的所有端口
     *
     * @param envId        环境id
     * @param appServiceId 服务id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询服务在环境下的所有port")
    @GetMapping("/list_port")
    public ResponseEntity<List<DevopsEnvPortVO>> listPortByAppAndEnvId(
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "服务id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(devopsEnvApplicationService.listPortByAppAndEnvId(envId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.port.query"));
    }


    /**
     * 查询项目下可用的且没有与该环境关联的服务
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 服务列表
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询项目下可用的且没有与该环境关联的服务")
    @GetMapping("/non_related_app_service")
    public ResponseEntity<List<BaseApplicationVO>> listNonRelatedAppService(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvApplicationService.listNonRelatedAppService(projectId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.query"));
    }
}
