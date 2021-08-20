package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsDeployGroupVO;
import io.choerodon.devops.app.service.DevopsDeployGroupService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:38
 **/
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/devops_deploy_group")
public class DevopsDeployGroupController {

    @Autowired
    private DevopsDeployGroupService devopsDeployGroupService;

    @ApiOperation("查询应用和容器配置信息")
    @GetMapping("/config_detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<DevopsDeployGroupVO> appConfigDetail(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @RequestParam("devops_config_group_id") Long devopsConfigGroupId) {
        return Results.success(devopsDeployGroupService.appConfigDetail(projectId, devopsConfigGroupId));
    }
}
