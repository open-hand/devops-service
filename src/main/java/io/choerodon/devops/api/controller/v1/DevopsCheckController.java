package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
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
     * @param version   版本
     */
    @Permission(level = ResourceLevel.SITE,
            roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "平滑升级")
    @GetMapping
    public ResponseEntity<String> checkLog(
            @ApiParam(value = "version")
            @RequestParam(value = "version") String version) {
        devopsCheckLogService.checkLog(version);
        return new ResponseEntity<>(System.currentTimeMillis() + "", HttpStatus.OK);
    }

    /**
     * 平滑升级指定环境Id
     *
     * @param version   版本
     * @param envId     环境id
     */
    @Permission(level = ResourceLevel.SITE,
            roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "平滑升级")
    @GetMapping(value = "/v1/upgrade/env")
    public ResponseEntity<String> checkLogByEnv(
            @ApiParam(value = "version")
            @RequestParam(value = "version") String version,
            @ApiParam(value = "envId")
            @RequestParam(value = "envId") Long envId) {
        devopsCheckLogService.checkLogByEnv(version, envId);
        return new ResponseEntity<>(System.currentTimeMillis() + "", HttpStatus.OK);
    }

}
