package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsAppTemplateCreateVO;
import io.choerodon.devops.app.service.DevopsAppTemplateService;
import io.choerodon.devops.infra.dto.DevopsAppTemplateDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/9
 * @Modified By:
 */
@RestController
@RequestMapping(value = "/v1/app_template")
public class DevopsAppTemplateController {
    @Autowired
    private DevopsAppTemplateService devopsAppTemplateService;

    @ApiOperation("平台层查询应用模板")
    @PostMapping("/site")
    @Permission(level = ResourceLevel.SITE)
    @CustomPageRequest
    public ResponseEntity<Page<DevopsAppTemplateDTO>> queryAppTemplateOnSite(
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestBody(required = false) String params) {
        return Results.success(devopsAppTemplateService.pageAppTemplate(0L, ResourceLevel.SITE.value(), params, pageRequest));
    }

    @ApiOperation("平台层创建应用模板")
    @PostMapping("/site/create_template")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> createTemplateOnSite(
            @RequestBody DevopsAppTemplateCreateVO appTemplateCreateVO) {
        devopsAppTemplateService.createTemplate(0L, ResourceLevel.SITE.value(), appTemplateCreateVO);
        return Results.success();
    }

    @ApiOperation("平台层校验名称或者编码")
    @GetMapping("/site/check_name_or_code")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Boolean> checkNameAndCodeOnSite(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "app_template_id", required = false) Long appTemplateId) {
        DevopsAppTemplateDTO appTemplateDTO = new DevopsAppTemplateDTO(appTemplateId, 0L, ResourceLevel.SITE.value());
        appTemplateDTO.setName(name);
        appTemplateDTO.setCode(code);
        return Results.success(devopsAppTemplateService.checkNameAndCode(appTemplateDTO, type));
    }

    @ApiOperation("平台层分配权限给自己")
    @GetMapping("/site/add_permission/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> addPermissionOnSite(
            @PathVariable(value = "app_template_id", required = false) Long appTemplateId) {
        devopsAppTemplateService.addPermission(appTemplateId);
        return Results.success();
    }

    @ApiOperation("平台层分配权限给自己")
    @GetMapping("/site/enable/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> enableAppTemplateOnSite(
            @PathVariable(value = "app_template_id", required = false) Long appTemplateId) {
        devopsAppTemplateService.enableAppTemplate(appTemplateId);
        return Results.success();
    }


    @ApiOperation("平台层分配权限给自己")
    @GetMapping("/site/disable/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> disableAppTemplateOnSite(
            @PathVariable(value = "app_template_id", required = false) Long appTemplateId) {
        devopsAppTemplateService.disableAppTemplate(appTemplateId);
        return Results.success();
    }

    @ApiOperation("平台层分配权限给自己")
    @DeleteMapping("/site/enable/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> deleteAppTemplateOnSite(
            @PathVariable(value = "app_template_id", required = false) Long appTemplateId) {
        devopsAppTemplateService.deleteAppTemplate(appTemplateId);
        return Results.success();
    }


}
