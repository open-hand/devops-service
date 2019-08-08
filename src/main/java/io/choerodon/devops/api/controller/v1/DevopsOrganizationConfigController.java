package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
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
     * @param devopsConfigVO 配置信息
     * @return ResponseEntity<DevopsConfigVO>
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下创建配置")
    @PostMapping
    public ResponseEntity<DevopsConfigVO> create(
            @ApiParam(value = "组织ID",required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsConfigVO devopsConfigVO) {

        return Optional.ofNullable(devopsConfigService.create(organizationId,devopsConfigVO))
                 .map(target -> new ResponseEntity<>(target,HttpStatus.OK))
                 .orElseThrow(() -> new CommonException("error.devops.organization.config.create"));
    }

    /**
     * 组织下更新配置信息
     * @param organizationId 组织Id
     * @param devopsConfigVO 配置信息
     * @return ResponseEntity<DevopsConfigVO>
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下更新配置信息")
    @PutMapping
    public ResponseEntity<DevopsConfigVO> update(
            @ApiParam(value = "组织ID",required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsConfigVO devopsConfigVO) {

        return Optional.ofNullable(devopsConfigService.update(organizationId, devopsConfigVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.organization.config.update"));
    }
}
