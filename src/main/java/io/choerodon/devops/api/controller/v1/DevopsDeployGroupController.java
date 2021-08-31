package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsDeployGroupVO;
import io.choerodon.devops.app.service.DevopsDeployGroupService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:38
 **/
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/devops_deploy_group")
public class DevopsDeployGroupController {

    @Autowired
    private DevopsDeployGroupService devopsDeployGroupService;

    @ApiOperation("创建或更新部署组应用")
    @PostMapping("create_or_update")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> createOrUpdate(
            @PathVariable("project_id") Long projectId,
            @ApiParam("操作类型")
            @RequestParam("type") String type,
            @ApiParam(value = "部署组信息")
            @RequestBody DevopsDeployGroupVO devopsDeployGroupVO
    ) {
        devopsDeployGroupService.createOrUpdate(projectId, devopsDeployGroupVO, type, false);
        return Results.success();
    }

    @ApiOperation("更新部署组容器配置")
    @PostMapping("/update_container")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> updateContainer(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "部署组信息")
            @RequestBody DevopsDeployGroupVO devopsDeployGroupVO
    ) {
        devopsDeployGroupService.updateContainer(projectId, devopsDeployGroupVO);
        return Results.success();
    }
}
