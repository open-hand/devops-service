package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DefaultConfigVO;
import io.choerodon.devops.api.vo.DevopsConfigRepVO;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsConfigService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author zhaotianxin
 * @since 2019/8/8 16:45
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/organization_config")
public class DevopsOrganizationConfigController {

    @Autowired
    DevopsConfigService devopsConfigService;

    @Autowired
    AppServiceService appServiceService;


    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下创建配置")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsConfigRepVO devopsConfigRepVO) {
        devopsConfigService.operateConfig(organizationId, ResourceType.ORGANIZATION.value(), devopsConfigRepVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 组织下查询配置详情
     *
     * @param organizationId 组织Id
     * @return List<DevopsConfigVO> 配置详情
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下查询配置详情")
    @GetMapping
    public ResponseEntity<DevopsConfigRepVO> query(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId) {
        return Optional.ofNullable(
                devopsConfigService.queryConfig(organizationId, ResourceType.ORGANIZATION.value()))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.organization.config.get.type"));
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下查询默认配置")
    @GetMapping("/default_config")
    public ResponseEntity<DefaultConfigVO> queryOrganizationDefaultConfig(
            @ApiParam(value = "组织Id")
            @PathVariable(value = "organization_id") Long organizationId) {
        return Optional.ofNullable(
                devopsConfigService.queryDefaultConfig(organizationId, ResourceType.ORGANIZATION.value()))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.organization.config.get"));
    }

    /**
     * 校验harbor配置信息是否正确
     *
     * @param url      harbor地址
     * @param userName harbor用户名
     * @param password harbor密码
     * @param project  harbor项目
     * @param email    harbor邮箱
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "校验harbor配置信息是否正确")
    @GetMapping(value = "/check_harbor")
    public void checkHarbor(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "harbor地址", required = true)
            @RequestParam String url,
            @ApiParam(value = "harbor用户名", required = true)
            @RequestParam String userName,
            @ApiParam(value = "harbor密码", required = true)
            @RequestParam String password,
            @ApiParam(value = "harborProject")
            @RequestParam(required = false) String project,
            @ApiParam(value = "harbor邮箱", required = true)
            @RequestParam String email) {
        appServiceService.checkHarbor(url, userName, password, project, email);
    }


    /**
     * 校验chart配置信息是否正确
     *
     * @param url chartmusume地址
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "校验chart配置信息是否正确")
    @GetMapping(value = "/check_chart")
    public void checkChart(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "chartmusume地址", required = true)
            @RequestParam String url) {
        appServiceService.checkChart(url);
    }
}
