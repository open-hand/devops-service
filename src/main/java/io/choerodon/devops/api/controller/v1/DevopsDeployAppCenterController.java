package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-18 15:25
 **/
@RestController
@RequestMapping("/v1/projects/{project_id}/deploy_app_center")

public class DevopsDeployAppCenterController {

    @Autowired
    DevopsDeployAppCenterService devopsDeployAppCenterService;

    /**
     * 根据环境id分页查询所有应用，不传环境id表示查出所有有权限环境下的应用
     *
     * @param envId 环境id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据环境id分页查询所有应用，不传环境id表示查出所有有权限环境下的应用")
    @GetMapping("/page_by_env")
    public ResponseEntity<Page<DevopsDeployAppCenterVO>> listApp(
            @PathVariable("project_id") Long projectId,
            @Encrypt @RequestParam(value = "envId", required = false) Long envId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rdupmType", required = false) String rdupmType,
            @RequestParam(value = "operationType", required = false) String operationType,
            @RequestParam(value = "params", required = false) String params,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable) {
        return ResponseEntity.ok(devopsDeployAppCenterService.listApp(projectId, envId, name, rdupmType, operationType, params, pageable));
    }

    @ApiOperation("根据应用id查询chart——应用详情")
    @GetMapping("/{app_center_id}/chart_detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<AppCenterEnvDetailVO> chartAppDetail(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @PathVariable("app_center_id") Long appCenterId) {
        return Results.success(devopsDeployAppCenterService.chartAppDetail(projectId, appCenterId));
    }

    @ApiOperation("根据应用id查询chart——应用事件")
    @GetMapping("/{app_center_id}/chart_event")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<List<InstanceEventVO>> chartAppEvent(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @RequestParam("app_center_id") Long appCenterId) {
        return Results.success(devopsDeployAppCenterService.chartAppEvent(projectId, appCenterId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("根据应用id查询chart——Pod详情")
    @CustomPageRequest
    @PostMapping(value = "/{app_center_id}/chart_pods_page")
    public ResponseEntity<Page<DevopsEnvPodVO>> chartAppPodsPage(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @RequestParam("app_center_id") Long appCenterId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Results.success(devopsDeployAppCenterService.chartAppPodsPage(
                projectId, appCenterId, pageRequest, searchParam));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping(value = "/{app_center_id}/chart_resources")
    public ResponseEntity<DevopsEnvResourceVO> chartRelease(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @RequestParam("app_center_id") Long appCenterId) {
        return Results.success(devopsDeployAppCenterService.chartAppRelease(projectId, appCenterId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据应用id查询chart——资源配置")
    @CustomPageRequest
    @PostMapping(value = "/{app_center_id}/chart_service")
    public ResponseEntity<Page<DevopsServiceVO>> chartService(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @RequestParam("app_center_id") Long appCenterId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Results.success(devopsDeployAppCenterService.chartService(projectId, appCenterId, pageRequest, searchParam));
    }
}
