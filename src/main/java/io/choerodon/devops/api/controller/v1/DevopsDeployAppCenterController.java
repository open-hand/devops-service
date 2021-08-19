package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppCenterEnvDetailVO;
import io.choerodon.devops.api.vo.DevopsDeployAppCenterVO;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

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
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable) {
        return ResponseEntity.ok(devopsDeployAppCenterService.listApp(projectId, envId, name, rdupmType, operationType, pageable));
    }

    @ApiOperation("根据应用id查询应用详情")
    @GetMapping("/app_detail_env")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<AppCenterEnvDetailVO> appCenterDetail(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @RequestParam("app_center_id") Long appCenterId) {
        return Results.success(devopsDeployAppCenterService.appCenterDetail(projectId, appCenterId));
    }
}
