package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/upgrade")
public class DevopsCheckController {
    @Autowired
    private DevopsCheckLogService devopsCheckLogService;

    /**
     * 平滑升级
     *
     * @param version 版本
     */
    @Permission(permissionLogin = true)
    @ApiOperation(value = "用于平滑升级(迁移数据等操作,可以多次调用)")
    @GetMapping
    public ResponseEntity<String> checkLog(
            @ApiParam(value = "version")
            @RequestParam(value = "version") String version) {
        devopsCheckLogService.checkLog(version);
        return new ResponseEntity<>(System.currentTimeMillis() + "", HttpStatus.OK);
    }
}
