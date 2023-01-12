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
     * 校验名称唯一
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param name      名称
     * @return boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验名称唯一")
    @GetMapping("/check_name")
    public ResponseEntity<Boolean> checkNameUnique(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用名称", required = true)
            @RequestParam(value = "name") String name,
            @ApiParam(value = "环境id", required = true)
            @Encrypt @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "制品类型", required = true)
            @RequestParam(value = "rdupm_type", required = false) String rdupmType,
            @ApiParam(value = "实例id", required = true)
            @Encrypt @RequestParam(value = "object_id", required = false) Long objectId) {
        return ResponseEntity.ok(devopsDeployAppCenterService.checkNameUnique(envId, rdupmType, objectId, name));
    }

    @ApiOperation("查询引用了容器应用作为替换对象的流水线信息")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{app_id}/pipeline_reference")
    public ResponseEntity<List<PipelineInstanceReferenceVO>> queryPipelineReference(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用ID", required = true)
            @PathVariable(value = "app_id") Long appId) {
        return ResponseEntity.ok().body(devopsDeployAppCenterService.queryPipelineReference(projectId, appId));
    }

    /**
     * 校验code唯一
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param code      code
     * @return boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验code唯一")
    @GetMapping("/check_code")
    public ResponseEntity<Boolean> checkCodeUnique(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用编码", required = true)
            @RequestParam(value = "code") String code,
            @ApiParam(value = "环境id", required = true)
            @Encrypt @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "制品类型", required = true)
            @RequestParam(value = "rdupm_type", required = false) String rdupmType,
            @ApiParam(value = "实例id", required = true)
            @Encrypt @RequestParam(value = "object_id", required = false) Long objectId) {
        return ResponseEntity.ok(devopsDeployAppCenterService.checkCodeUnique(envId, rdupmType, objectId, code));
    }

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
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @Encrypt @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "应用名称", required = true)
            @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "制品类型", required = true)
            @RequestParam(value = "rdupm_type", required = false) String rdupmType,
            @ApiParam(value = "操作类型", required = true)
            @RequestParam(value = "operation_type", required = false) String operationType,
            @ApiParam(value = "搜索参数", required = true)
            @RequestParam(value = "params", required = false) String params,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable) {
        return ResponseEntity.ok(devopsDeployAppCenterService.listApp(projectId, envId, name, rdupmType, operationType, params, pageable));
    }

    @ApiOperation("根据应用id查询环境部署——应用详情（chart包和部署组）")
    @GetMapping("/{app_center_id}/env_detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<AppCenterEnvDetailVO> envAppDetail(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @PathVariable("app_center_id") Long appCenterId) {
        return Results.success(devopsDeployAppCenterService.envAppDetail(projectId, appCenterId));
    }

    @ApiOperation("根据应用id查询环境部署（chart包和部署组）——应用事件")
    @GetMapping("/{app_center_id}/env_event")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<List<InstanceEventVO>> envAppEvent(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @PathVariable("app_center_id") Long appCenterId) {
        return Results.success(devopsDeployAppCenterService.envAppEvent(projectId, appCenterId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("根据应用id查询环境部署（chart包和部署组）——Pod详情")
    @CustomPageRequest
    @PostMapping(value = "/{app_center_id}/env_pods_page")
    public ResponseEntity<Page<DevopsEnvPodVO>> envAppPodsPage(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @PathVariable("app_center_id") Long appCenterId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Results.success(devopsDeployAppCenterService.envAppPodsPage(
                projectId, appCenterId, pageRequest, searchParam));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("根据应用id查询环境部署（chart包和部署组）——运行详情")
    @GetMapping(value = "/{app_center_id}/env_resources")
    public ResponseEntity<DevopsEnvResourceVO> envAppRelease(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @PathVariable("app_center_id") Long appCenterId) {
        return Results.success(devopsDeployAppCenterService.envAppRelease(projectId, appCenterId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据应用id查询chart——资源配置")
    @CustomPageRequest
    @PostMapping(value = "/{app_center_id}/env_chart_service")
    public ResponseEntity<Page<DevopsServiceVO>> envChartService(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @Encrypt
            @PathVariable("app_center_id") Long appCenterId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Results.success(devopsDeployAppCenterService.envChartService(projectId, appCenterId, pageRequest, searchParam));
    }

    /**
     * 根据projectId和envId查询deployment的应用列表
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据projectId和envId查询deployment的应用列表")
    @GetMapping(value = "/deployment")
    public ResponseEntity<Page<DevopsDeployAppCenterVO>> pageFromDeployment(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable) {
        return ResponseEntity.ok(devopsDeployAppCenterService.pageByProjectIdAndEnvId(projectId, envId, pageable));
    }

    /**
     * 根据projectId和envId查询chart的应用列表
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据projectId和envId查询deployment的应用列表")
    @GetMapping(value = "/chart")
    public ResponseEntity<Page<DevopsDeployAppCenterVO>> pageFromChart(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "应用服务ID", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable) {
        return ResponseEntity.ok(devopsDeployAppCenterService.pageByProjectIdAndEnvIdAndAppId(projectId, envId, appServiceId, pageable));
    }

    /**
     * 根据环境id分页查询所有chart应用
     *
     * @param envId 环境id
     * @return page
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据环境id分页查询所有chart应用")
    @GetMapping("/page_chart")
    public ResponseEntity<Page<DevopsDeployAppCenterVO>> pageChart(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @Encrypt @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "应用名称", required = true)
            @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "操作类型", required = true)
            @RequestParam(value = "operation_type", required = false) String operationType,
            @ApiParam(value = "搜索参数", required = true)
            @RequestParam(value = "params", required = false) String params,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable) {
        return ResponseEntity.ok(devopsDeployAppCenterService.pageChart(projectId, envId, name, operationType, params, pageable));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "开启chart应用监控")
    @PutMapping("/{app_id}/metric/enable")
    public ResponseEntity<Void> enableMetric(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用id")
            @Encrypt @PathVariable(value = "app_id") Long appId) {
        devopsDeployAppCenterService.enableMetric(projectId, appId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "关闭chart应用监控")
    @PutMapping("/{app_id}/metric/disable")
    public ResponseEntity<Void> disableMetric(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用id")
            @Encrypt @PathVariable(value = "app_id") Long appId) {
        devopsDeployAppCenterService.disableMetric(projectId, appId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "chart应用监控 - 停机次数折线图所需坐标信息")
    @PostMapping("/{app_id}/metric/exception_times_chart_info")
    public ResponseEntity<ExceptionTimesVO> queryExceptionTimesChartInfo(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用id")
            @Encrypt @PathVariable(value = "app_id") Long appId,
            @RequestBody DateQueryVO dateQueryVO) {
        return ResponseEntity.ok(devopsDeployAppCenterService.queryExceptionTimesChartInfo(projectId,
                appId,
                dateQueryVO.getStartTime(),
                dateQueryVO.getEndTime()));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "chart应用监控 - 异常持续时长散点图坐标信息")
    @PostMapping("/{app_id}/metric/exception_duration_chart_info")
    public ResponseEntity<ExceptionDurationVO> queryExceptionDurationChartInfo(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用id")
            @Encrypt @PathVariable(value = "app_id") Long appId,
            @RequestBody DateQueryVO dateQueryVO) {
        return ResponseEntity.ok(devopsDeployAppCenterService.queryExceptionDurationChartInfo(projectId,
                appId,
                dateQueryVO.getStartTime(),
                dateQueryVO.getEndTime()));
    }
}
