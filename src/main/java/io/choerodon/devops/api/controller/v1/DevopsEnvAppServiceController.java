package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.EnvironmentApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author lizongwei
 * @since 2019/7/1
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
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建环境下的服务关联")
    @PostMapping("/batch_create")
    public ResponseEntity<List<DevopsEnvApplicationVO>> batchCreate(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "关联信息", required = true)
            @RequestBody DevopsEnvAppServiceVO devopsEnvAppServiceVO) {
        validator.checkEnvIdExist(devopsEnvAppServiceVO.getEnvId());
        validator.checkAppIdsExist(devopsEnvAppServiceVO.getAppServiceIds());
        return ResponseEntity.ok(devopsEnvApplicationService.batchCreate(projectId, devopsEnvAppServiceVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除指定环境-服务关联关系")
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境Id", required = true)
            @Encrypt
            @RequestParam("env_id") Long envId,
            @Encrypt
            @ApiParam(value = "应用服务Id", required = true)
            @RequestParam("app_service_id") Long appServiceId) {
        validator.checkEnvIdAndAppIdsExist(projectId, envId, appServiceId);
        devopsEnvApplicationService.delete(envId, appServiceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 查询环境下的所有服务
     *
     * @param envId 环境id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询环境下的所有服务")
    @GetMapping("/list_by_env")
    public ResponseEntity<List<AppServiceRepVO>> listAppByEnvId(
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsEnvApplicationService.listAppByEnvId(envId));
    }

    /**
     * 查询服务在环境下的所有label
     *
     * @param envId        环境id
     * @param appServiceId 服务id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询服务在环境下的所有label")
    @GetMapping("/list_label")
    public ResponseEntity<List<Map<String, String>>> listLabelByAppAndEnvId(
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(devopsEnvApplicationService.listLabelByAppAndEnvId(envId, appServiceId));
    }


    /**
     * 查询服务在环境下的所有端口
     *
     * @param envId        环境id
     * @param appServiceId 服务id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询服务在环境下的所有port")
    @GetMapping("/list_port")
    public ResponseEntity<List<DevopsEnvPortVO>> listPortByAppAndEnvId(
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(devopsEnvApplicationService.listPortByAppAndEnvId(envId, appServiceId));
    }


    /**
     * 查询项目下可用的且没有与该环境关联的服务
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 服务列表
     * @deprecated
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询项目下可用的且没有与该环境关联的服务", hidden = true)
    @GetMapping("/non_related_app_service")
    public ResponseEntity<List<BaseApplicationServiceVO>> listNonRelatedAppService(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return new ResponseEntity<>(devopsEnvApplicationService.listNonRelatedAppService(projectId, envId), HttpStatus.OK);
    }
}
