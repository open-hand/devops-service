package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.AppServiceShareRuleService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:30 2019/6/28
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/organizations/app_service_share")
public class OrgAppShareController {
    @Autowired
    private AppServiceShareRuleService appServiceShareRuleService;

    /**
     * 根据版本Id获取values和chart
     *
     * @param versionId 版本Id
     * @return Long
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "根据版本Id获取values和chart")
    @GetMapping(value = "/values")
    public ResponseEntity<AppServiceVersionAndValueVO> getValuesAndChart(
            @ApiParam(value = "应用Id")
            @RequestParam(value = "version_id") Long versionId) {
        return Optional.ofNullable(
                appServiceShareRuleService.getValuesAndChart(versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.values.chart"));
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "token校验")
    @PostMapping(value = "/check_token")
    public ResponseEntity<AccessTokenCheckResultVO> checkToken(
            @ApiParam(value = "token")
            @RequestBody AccessTokenVO tokenDTO) {
        return Optional.ofNullable(
                appServiceShareRuleService.checkToken(tokenDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.check.access.token"));
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "token保存")
    @PostMapping(value = "/save_token")
    public ResponseEntity saveToken(
            @ApiParam(value = "token")
            @RequestBody AccessTokenVO tokenDTO) {
        appServiceShareRuleService.saveToken(tokenDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

