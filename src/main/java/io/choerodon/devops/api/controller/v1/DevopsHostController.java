package io.choerodon.devops.api.controller.v1;

import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsHostCreateRequestVO;
import io.choerodon.devops.api.vo.DevopsHostUpdateRequestVO;
import io.choerodon.devops.api.vo.DevopsHostVO;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author zmf
 * @since 2020/9/15
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/hosts")
public class DevopsHostController {
    @Autowired
    private DevopsHostService devopsHostService;

    @ApiOperation("创建主机")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<DevopsHostVO> createHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "创建主机相关参数")
            @RequestBody @Valid DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        return new ResponseEntity<>(devopsHostService.createHost(projectId, devopsHostCreateRequestVO), HttpStatus.OK);
    }

    @ApiOperation("更新主机")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/{host_id}")
    public ResponseEntity<DevopsHostVO> updateHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @Encrypt @PathVariable("host_id") Long hostId,
            @ApiParam(value = "更新主机相关参数")
            @RequestBody @Valid DevopsHostUpdateRequestVO devopsHostUpdateRequestVO) {
        return new ResponseEntity<>(devopsHostService.updateHost(projectId, hostId, devopsHostUpdateRequestVO), HttpStatus.OK);
    }

    @ApiOperation("查询单个主机")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{host_id}")
    public ResponseEntity<DevopsHostVO> queryHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @Encrypt @PathVariable("host_id") Long hostId) {
        return Results.success(devopsHostService.queryHost(projectId, hostId));
    }

    @ApiOperation("删除主机")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{host_id}")
    public ResponseEntity<Void> deleteHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @Encrypt @PathVariable("host_id") Long hostId) {
        devopsHostService.deleteHost(projectId, hostId);
        return Results.success();
    }
}
