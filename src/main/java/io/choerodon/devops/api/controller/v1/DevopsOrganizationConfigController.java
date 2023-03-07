package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.DefaultConfigVO;
import io.choerodon.devops.api.vo.DevopsConfigRepVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.swagger.annotation.Permission;


/**
 * @author zhaotianxin
 * @since 2019/8/8 16:45
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/organization_config")
public class DevopsOrganizationConfigController {

    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private AppServiceService appServiceService;

    //组织下创建配置harbor的逻辑不要了
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下创建配置")
    @PostMapping
    public ResponseEntity<Void> create(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsConfigRepVO devopsConfigRepVO) {
        devopsConfigService.operateConfig(organizationId, ResourceLevel.ORGANIZATION.value(), devopsConfigRepVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 组织下查询配置详情
     *
     * @param organizationId 组织Id
     * @return List<DevopsConfigVO> 配置详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下查询配置详情")
    @GetMapping
    public ResponseEntity<DevopsConfigRepVO> query(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId) {
        return ResponseEntity.ok(devopsConfigService.queryConfig(organizationId, ResourceLevel.ORGANIZATION.value()));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下查询默认配置")
    @GetMapping("/default_config")
    public ResponseEntity<DefaultConfigVO> queryOrganizationDefaultConfig(
            @ApiParam(value = "组织Id")
            @PathVariable(value = "organization_id") Long organizationId) {
        return ResponseEntity.ok(devopsConfigService.queryDefaultConfig(organizationId, ResourceLevel.ORGANIZATION.value()));
    }

    /**
     * 校验chart配置信息是否正确
     *
     * @param configVO chartMuseum信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "校验chart配置信息是否正确")
    @PostMapping(value = "/check_chart")
    public ResponseEntity<Boolean> checkChart(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "chartMuseum信息", required = true)
            @RequestBody ConfigVO configVO) {
        return ResponseEntity.ok(appServiceService.checkChartOnOrganization(configVO.getUrl(), configVO.getUserName(), configVO.getPassword()));
    }
}
