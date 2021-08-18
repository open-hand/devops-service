package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppCenterDetailVO;
import io.choerodon.devops.app.service.AppCenterService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_center")
public class AppCenterController {
    @Autowired
    private AppCenterService appCenterService;

    @ApiOperation("根据应用id查询应用详情")
    @GetMapping("/app_detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<AppCenterDetailVO> appCenterDetail(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用中心 应用Id")
            @RequestParam("app_center_id") Long appCenterId) {
        return Results.success(appCenterService.appCenterDetail(projectId, appCenterId));
    }
}
