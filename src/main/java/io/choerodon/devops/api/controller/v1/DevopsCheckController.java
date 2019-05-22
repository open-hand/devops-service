package io.choerodon.devops.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "平滑升级")
    @GetMapping
    public ResponseEntity<String> checkLog(
            @ApiParam(value = "version")
            @RequestParam(value = "version") String version) {
        devopsCheckLogService.checkLog(version);
        return new ResponseEntity<>(System.currentTimeMillis() + "", HttpStatus.OK);
    }


}
