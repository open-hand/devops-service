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
import io.choerodon.devops.api.vo.DevopsAppTemplateCreateVO;
import io.choerodon.devops.app.service.DevopsAppTemplateService;
import io.choerodon.devops.infra.dto.DevopsAppTemplateDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
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
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
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
            @ApiParam("name or code")
            @RequestParam(value = "value") String value,
            @ApiParam("type")
            @RequestParam(value = "type") String type,
            @Encrypt
            @ApiParam("模板id")
            @RequestParam(value = "app_template_id", required = false) Long appTemplateId) {
        DevopsAppTemplateDTO appTemplateDTO = new DevopsAppTemplateDTO(appTemplateId, 0L, ResourceLevel.SITE.value());
        appTemplateDTO.setName(value);
        appTemplateDTO.setCode(value);
        return Results.success(devopsAppTemplateService.checkNameAndCode(appTemplateDTO, type));
    }

    @ApiOperation("平台层分配权限给自己")
    @GetMapping("/site/add_permission/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> addPermissionOnSite(
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        devopsAppTemplateService.addPermission(appTemplateId);
        return Results.success();
    }

    @ApiOperation("平台层启用模板")
    @GetMapping("/site/enable/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> enableAppTemplateOnSite(
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        devopsAppTemplateService.enableAppTemplate(appTemplateId);
        return Results.success();
    }


    @ApiOperation("平台层停用模板")
    @GetMapping("/site/disable/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> disableAppTemplateOnSite(
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        devopsAppTemplateService.disableAppTemplate(appTemplateId);
        return Results.success();
    }

    @ApiOperation("平台层删除模板")
    @DeleteMapping("/site/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> deleteAppTemplateOnSite(
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        devopsAppTemplateService.deleteAppTemplate(0L, ResourceLevel.SITE.value(), appTemplateId);
        return Results.success();
    }

    @ApiOperation("平台层查询已有应用模板")
    @GetMapping("/site/list")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<List<DevopsAppTemplateDTO>> listAppTemplateOnSite(
            @ApiParam("搜索参数")
            @RequestParam(required = false) String param) {
        return Results.success(devopsAppTemplateService.listAppTemplate(0L, ResourceLevel.SITE.value(), ResourceLevel.SITE.value(), param));
    }

    @ApiOperation("平台层查询应用模板详情")
    @GetMapping("/site/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<DevopsAppTemplateDTO> detailsAppTemplateOnSite(
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        return Results.success(devopsAppTemplateService.queryAppTemplateById(appTemplateId));
    }

    @ApiOperation("平台层修改应用模板名称")
    @PutMapping("/site/{app_template_id}")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> updateAppTemplateOnSite(
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId,
            @ApiParam("模板名称")
            @RequestParam(value = "name") String name) {
        devopsAppTemplateService.updateAppTemplate(appTemplateId, name);
        return Results.success();
    }

    @ApiOperation("组织层查询应用模板")
    @PostMapping("/organization/{organization_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    public ResponseEntity<Page<DevopsAppTemplateDTO>> queryAppTemplateOnTenant(
            @ApiParam(value = "分页参数")
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @RequestBody(required = false) String params) {
        return Results.success(devopsAppTemplateService.pageAppTemplate(organizationId, ResourceLevel.ORGANIZATION.value(), params, pageRequest));
    }

    @ApiOperation("组织层创建应用模板")
    @PostMapping("/organization/{organization_id}/create_template")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> createTemplateOnTenant(
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @RequestBody DevopsAppTemplateCreateVO appTemplateCreateVO) {
        devopsAppTemplateService.createTemplate(organizationId, ResourceLevel.ORGANIZATION.value(), appTemplateCreateVO);
        return Results.success();
    }

    @ApiOperation("组织层校验名称或者编码")
    @GetMapping("/organization/{organization_id}/check_name_or_code")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Boolean> checkNameAndCodeOnTenant(
            @ApiParam("name or code")
            @RequestParam(value = "value") String value,
            @ApiParam("type")
            @RequestParam(value = "type") String type,
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @Encrypt
            @ApiParam("模板id")
            @RequestParam(value = "app_template_id", required = false) Long appTemplateId) {
        DevopsAppTemplateDTO appTemplateDTO = new DevopsAppTemplateDTO(appTemplateId, organizationId, ResourceLevel.ORGANIZATION.value());
        appTemplateDTO.setName(value);
        appTemplateDTO.setCode(value);
        return Results.success(devopsAppTemplateService.checkNameAndCode(appTemplateDTO, type));
    }

    @ApiOperation("组织层分配权限给自己")
    @GetMapping("/organization/{organization_id}/add_permission/{app_template_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> addPermissionOnTenant(
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        devopsAppTemplateService.addPermission(appTemplateId);
        return Results.success();
    }

    @ApiOperation("组织层启用模板")
    @GetMapping("/organization/{organization_id}/enable/{app_template_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> enableAppTemplateOnTenant(
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        devopsAppTemplateService.enableAppTemplate(appTemplateId);
        return Results.success();
    }


    @ApiOperation("组织层停用模板")
    @GetMapping("/organization/{organization_id}/disable/{app_template_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> disableAppTemplateOnTenant(
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        devopsAppTemplateService.disableAppTemplate(appTemplateId);
        return Results.success();
    }

    @ApiOperation("组织层删除模板")
    @DeleteMapping("/organization/{organization_id}/{app_template_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> deleteAppTemplateOnTenant(
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        devopsAppTemplateService.deleteAppTemplate(organizationId, ResourceLevel.ORGANIZATION.value(), appTemplateId);
        return Results.success();
    }

    @ApiOperation("组织层查询已有应用模板")
    @GetMapping("/organization/{organization_id}/list")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<List<DevopsAppTemplateDTO>> listAppTemplateOnTenant(
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam("选择查询平台层模板/组织层模板：site/organization")
            @RequestParam(value = "selectedLevel") String selectedLevel,
            @ApiParam("搜索参数")
            @RequestParam(required = false) String param) {
        return Results.success(devopsAppTemplateService.listAppTemplate(organizationId, ResourceLevel.ORGANIZATION.value(), selectedLevel, param));
    }

    @ApiOperation("组织层查询应用模板详情")
    @GetMapping("/organization/{organization_id}/{app_template_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<DevopsAppTemplateDTO> detailsAppTemplateOnTenant(
            @ApiParam("租户id")
            @PathVariable(value = "organization_id") Long organizationId,
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId) {
        return Results.success(devopsAppTemplateService.queryAppTemplateById(appTemplateId));
    }

    @ApiOperation("组织层修改应用模板名称")
    @PutMapping("/organization/{organization_id}/{app_template_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> updateAppTemplateOnTenant(
            @Encrypt
            @ApiParam("模板id")
            @PathVariable(value = "app_template_id") Long appTemplateId,
            @ApiParam("模板名称")
            @RequestParam(value = "name") String name) {
        devopsAppTemplateService.updateAppTemplate(appTemplateId, name);
        return Results.success();
    }

    @ApiOperation("项目层查询已有应用模板")
    @GetMapping("/project/{project_id}/list")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<List<DevopsAppTemplateDTO>> listAppTemplateOnProject(
            @ApiParam("项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam("选择查询平台层模板/组织层模板：site/organization")
            @RequestParam(value = "selectedLevel") String selectedLevel,
            @ApiParam("搜索参数")
            @RequestParam(required = false) String param) {
        return Results.success(devopsAppTemplateService.listAppTemplate(projectId, ResourceLevel.PROJECT.value(), selectedLevel, param));
    }

}
