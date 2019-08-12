package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DefaultConfigVO;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.app.service.DevopsConfigService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



/**
 * @author zhaotianxin
 * @since  2019/8/8 16:45
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/organization_config")
public class DevopsOrganizationConfigController {

    @Autowired
    DevopsConfigService devopsConfigService;

    /**
     * 组织下创建配置
     * @param organizationId 组织Id
     * @param devopsConfigVOS 配置信息
     * @return void
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下创建配置")
    @PostMapping
    public ResponseEntity<DevopsConfigVO> create(
            @ApiParam(value = "组织ID",required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody List<DevopsConfigVO> devopsConfigVOS) {
        devopsConfigService.operate(organizationId,ResourceType.ORGANIZATION.value(),devopsConfigVOS);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 组织下查询配置详情
     * @param organizationId 组织Id
     * @param type 配置类型
     * @return
     */
    @Permission(type = ResourceType.ORGANIZATION,roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下查询配置详情")
    @GetMapping
    public ResponseEntity<List<DevopsConfigVO>> query(
            @ApiParam(value = "组织Id",required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "配置类型",required = true)
            @RequestParam(value = "type") String type) {
        return Optional.ofNullable(
              devopsConfigService.queryByResourceId(organizationId, type))
              .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
              .orElseThrow(() -> new CommonException("error.devops.organization.config.get.type"));
    }

    @Permission(type = ResourceType.ORGANIZATION,roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下查询默认配置")
    @GetMapping("/default_config")
    public ResponseEntity<DefaultConfigVO> queryOrganizationDefaultConfig(
            @ApiParam(value = "组织Id")
            @PathVariable(value = "organization_id") Long organizationId) {
        return Optional.ofNullable(
                devopsConfigService.queryDefaultConfig(organizationId,ResourceType.ORGANIZATION.value()))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.organization.config.get"));
    }


}
