package io.choerodon.devops.api.controller.v1;

import java.util.Set;
import javax.validation.Valid;

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
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
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
        DevopsHostVO resp = devopsHostService.createHost(projectId, devopsHostCreateRequestVO);
        devopsHostService.asyncBatchCorrectStatus(projectId, ArrayUtil.singleAsSet(resp.getId()));
        return Results.success(resp);
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
        DevopsHostVO resp = devopsHostService.updateHost(projectId, hostId, devopsHostUpdateRequestVO);
        devopsHostService.asyncBatchCorrectStatus(projectId, ArrayUtil.singleAsSet(resp.getId()));
        return Results.success(resp);
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

    @ApiOperation("分页查询主机")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/page_by_options")
    @CustomPageRequest
    public ResponseEntity<Page<DevopsHostVO>> pageByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "是否带有更新者信息", required = false)
            @RequestParam(value = "with_updater_info", required = false, defaultValue = "false")
                    Boolean withUpdaterInfo,
            @ApiParam(value = "查询参数", required = false)
            @RequestBody(required = false) String options) {
        return Results.success(devopsHostService.pageByOptions(projectId, pageRequest, withUpdaterInfo, options));
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

    @ApiOperation("测试主机连接状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/connection_test")
    public ResponseEntity<DevopsHostConnectionTestResultVO> testConnection(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @RequestBody @Valid DevopsHostConnectionTestVO devopsHostConnectionTestVO) {
        return Results.success(devopsHostService.testConnection(projectId, devopsHostConnectionTestVO));
    }

    @ApiOperation("校验名称唯一性")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check/name_unique")
    public ResponseEntity<Boolean> checkName(@ApiParam(value = "项目id", required = true)
                                             @PathVariable("project_id") Long projectId,
                                             @ApiParam(value = "主机名称", required = true)
                                             @RequestParam("name") String name) {
        return Results.success(devopsHostService.isNameUnique(projectId, name));
    }

    @ApiOperation("校验ip和ssh端口唯一性")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check/ssh_unique")
    public ResponseEntity<Boolean> checkIpSshUnique(@ApiParam(value = "项目id", required = true)
                                                    @PathVariable("project_id") Long projectId,
                                                    @ApiParam(value = "主机ip", required = true)
                                                    @RequestParam("ip") String ip,
                                                    @ApiParam(value = "ssh端口", required = true)
                                                    @RequestParam("ssh_port") Integer sshPort) {
        return Results.success(devopsHostService.isSshIpPortUnique(projectId, ip, sshPort));
    }

    @ApiOperation("校验ip和jmeter端口唯一性")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check/jmeter_unique")
    public ResponseEntity<Boolean> checkIpJmeterPortUnique(@ApiParam(value = "项目id", required = true)
                                                           @PathVariable("project_id") Long projectId,
                                                           @ApiParam(value = "主机ip", required = true)
                                                           @RequestParam("ip") String ip,
                                                           @ApiParam(value = "ssh端口", required = true)
                                                           @RequestParam("jmeter_port") Integer jmeterPort) {
        return Results.success(devopsHostService.isIpJmeterPortUnique(projectId, ip, jmeterPort));
    }

    @ApiOperation("批量校准主机状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/batch_correct")
    public ResponseEntity<Void> batchCorrect(@ApiParam(value = "项目id", required = true)
                                             @PathVariable("project_id") Long projectId,
                                             @ApiParam(value = "主机id集合", required = true)
                                             @Encrypt @RequestBody Set<Long> hostIds) {
        devopsHostService.batchSetStatusOperating(projectId, hostIds);
        devopsHostService.asyncBatchCorrectStatus(projectId, hostIds);
        return Results.success();
    }
}
