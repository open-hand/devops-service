package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.api.vo.host.DevopsJavaInstanceVO;
import io.choerodon.devops.api.vo.host.ResourceUsageInfoVO;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.mybatis.pagehelper.annotation.PageableDefault;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public ResponseEntity<String> createHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "创建主机相关参数")
            @RequestBody @Valid DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        String resp = devopsHostService.createHost(projectId, devopsHostCreateRequestVO);
        return Results.success(resp);
    }

    @ApiOperation("更新主机")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/{host_id}")
    public ResponseEntity<Void> updateHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @Encrypt @PathVariable("host_id") Long hostId,
            @ApiParam(value = "更新主机相关参数")
            @RequestBody @Valid DevopsHostUpdateRequestVO devopsHostUpdateRequestVO) {
        devopsHostService.updateHost(projectId, hostId, devopsHostUpdateRequestVO);
        return ResponseEntity.noContent().build();
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
            @SortDefault.SortDefaults({
                    @SortDefault(value = "host_status", direction = Sort.Direction.DESC),
                    @SortDefault(value = "id", direction = Sort.Direction.DESC)})
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "是否带有更新者信息")
            @RequestParam(value = "with_updater_info", required = false, defaultValue = "false")
                    Boolean withUpdaterInfo,
            @ApiParam(value = "搜索参数")
            @RequestParam(value = "search_param", required = false) String searchParam,
            @ApiParam(value = "主机状态")
            @RequestParam(value = "host_status", required = false) String hostStatus,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "do_page", required = false, defaultValue = "true") Boolean doPage) {
        return Results.success(devopsHostService.pageByOptions(projectId, pageRequest, withUpdaterInfo, searchParam, hostStatus, doPage));
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

    @ApiOperation("测试多个主机连接状态/返回结果是所有连接不通过的主机id（仅供中间件部署的主机连通测试使用）")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/multi/connection_test")
    public ResponseEntity<Set<Object>> multiTestConnection(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @RequestBody @Encrypt Set<Long> hostIds) {
        return Results.success(devopsHostService.multiTestConnection(projectId, hostIds));
    }

    @ApiOperation("通过id测试部署类型主机的连接状态/阻塞形式")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/connection_test_by_id")
    public ResponseEntity<Boolean> testConnectionByIdForDeployHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @Encrypt @RequestParam("host_id") Long hostId) {
        return Results.success(devopsHostService.testConnectionByIdForDeployHost(projectId, hostId));
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

    @ApiOperation("校验主机能否被删除")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check/delete")
    public ResponseEntity<Boolean> checkHostDelete(@ApiParam(value = "项目id", required = true)
                                                   @PathVariable("project_id") Long projectId,
                                                   @ApiParam(value = "主机id", required = true)
                                                   @Encrypt @RequestParam("host_id") Long hostId) {
        return Results.success(devopsHostService.checkHostDelete(projectId, hostId));
    }

    @ApiOperation("批量校准主机状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/batch_correct")
    public ResponseEntity<Void> batchCorrect(@ApiParam(value = "项目id", required = true)
                                             @PathVariable("project_id") Long projectId,
                                             @ApiParam(value = "主机id集合", required = true)
                                             @Encrypt @RequestBody Set<Long> hostIds) {
        devopsHostService.asyncBatchCorrectStatus(projectId, devopsHostService.batchSetStatusOperating(projectId, hostIds), DetailsHelper.getUserDetails().getUserId());
        return Results.success();
    }

    @ApiOperation("批量校准主机状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/batch_correct_with_progress")
    public ResponseEntity<String> batchCorrectWithProgress(@ApiParam(value = "项目id", required = true)
                                                           @PathVariable("project_id") Long projectId,
                                                           @ApiParam(value = "主机id集合", required = true)
                                                           @Encrypt @RequestBody Set<Long> hostIds) {
        return ResponseEntity.ok(devopsHostService.asyncBatchCorrectStatusWithProgress(projectId, devopsHostService.batchSetStatusOperating(projectId, hostIds)));
    }

    @ApiOperation("获取批量校准主机状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/checking_progress")
    public ResponseEntity<CheckingProgressVO> getCheckingProgress(@ApiParam(value = "项目id", required = true)
                                                                  @PathVariable("project_id") Long projectId,
                                                                  @RequestParam("correctKey") String correctKey) {
        return ResponseEntity.ok(devopsHostService.getCheckingProgress(projectId, correctKey));
    }

    @ApiOperation("获取批量校准主机状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/paging_with_checking_status")
    @CustomPageRequest
    public ResponseEntity<Page<DevopsHostVO>> pagingWithCheckingStatus(@ApiParam(value = "项目id", required = true)
                                                                       @PathVariable("project_id") Long projectId,
                                                                       @SortDefault.SortDefaults({
                                                                               @SortDefault(value = "jmeter_status", direction = Sort.Direction.DESC),
                                                                               @SortDefault(value = "host_status", direction = Sort.Direction.DESC),
                                                                               @SortDefault(value = "id", direction = Sort.Direction.DESC)}) PageRequest pageRequest,
                                                                       @RequestParam(value = "correctKey", required = false) String correctKey,
                                                                       @RequestParam(value = "search_param", required = false) String searchParam) {
        return ResponseEntity.ok(devopsHostService.pagingWithCheckingStatus(projectId, pageRequest, correctKey, searchParam));
    }

    @ApiOperation("获取java进程信息接口")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{host_id}/java_process")
    public ResponseEntity<List<DevopsJavaInstanceVO>> listJavaProcessInfo(@ApiParam(value = "项目id", required = true)
                                                                          @PathVariable("project_id") Long projectId,
                                                                          @Encrypt
                                                                          @ApiParam(value = "主机id", required = true)
                                                                          @PathVariable("host_id") Long hostId) {
        return ResponseEntity.ok(devopsHostService.listJavaProcessInfo(projectId, hostId));
    }

    @ApiOperation("获取docker进程信息接口")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{host_id}/docker_process")
    public ResponseEntity<List<DevopsDockerInstanceVO>> listDockerProcessInfo(@ApiParam(value = "项目id", required = true)
                                                                              @PathVariable("project_id") Long projectId,
                                                                              @Encrypt
                                                                              @ApiParam(value = "主机id", required = true)
                                                                              @PathVariable("host_id") Long hostId) {
        return ResponseEntity.ok(devopsHostService.listDockerProcessInfo(projectId, hostId));
    }

    @ApiOperation("删除java进程")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{host_id}/java_process/{instance_id}")
    public ResponseEntity<Void> deleteJavaProcess(@ApiParam(value = "项目id", required = true)
                                                  @PathVariable("project_id") Long projectId,
                                                  @ApiParam(value = "主机id", required = true)
                                                  @Encrypt
                                                  @PathVariable("host_id") Long hostId,
                                                  @ApiParam(value = "java实例id", required = true)
                                                  @Encrypt
                                                  @PathVariable("instance_id") Long instanceId) {
        devopsHostService.deleteJavaProcess(projectId, hostId, instanceId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("删除docker进程")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{host_id}/docker_process/{instance_id}")
    public ResponseEntity<Void> deleteDockerProcess(@ApiParam(value = "项目id", required = true)
                                                    @PathVariable("project_id") Long projectId,
                                                    @ApiParam(value = "主机id", required = true)
                                                    @Encrypt
                                                    @PathVariable("host_id") Long hostId,
                                                    @ApiParam(value = "docker实例id", required = true)
                                                    @Encrypt
                                                    @PathVariable("instance_id") Long instanceId) {
        devopsHostService.deleteDockerProcess(projectId, hostId, instanceId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("停止docker进程")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/{host_id}/docker_process/{instance_id}/stop")
    public ResponseEntity<Void> stopDockerProcess(@ApiParam(value = "项目id", required = true)
                                                  @PathVariable("project_id") Long projectId,
                                                  @ApiParam(value = "主机id", required = true)
                                                  @Encrypt
                                                  @PathVariable("host_id") Long hostId,
                                                  @ApiParam(value = "docker实例id", required = true)
                                                  @Encrypt
                                                  @PathVariable("instance_id") Long instanceId) {
        devopsHostService.stopDockerProcess(projectId, hostId, instanceId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("重启docker进程")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/{host_id}/docker_process/{instance_id}/restart")
    public ResponseEntity<Void> restartDockerProcess(@ApiParam(value = "项目id", required = true)
                                                     @PathVariable("project_id") Long projectId,
                                                     @ApiParam(value = "主机id", required = true)
                                                     @Encrypt
                                                     @PathVariable("host_id") Long hostId,
                                                     @ApiParam(value = "docker实例id", required = true)
                                                     @Encrypt
                                                     @PathVariable("instance_id") Long instanceId) {
        devopsHostService.restartDockerProcess(projectId, hostId, instanceId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("启动docker进程")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/{host_id}/docker_process/{instance_id}/start")
    public ResponseEntity<Void> startDockerProcess(@ApiParam(value = "项目id", required = true)
                                                   @PathVariable("project_id") Long projectId,
                                                   @ApiParam(value = "主机id", required = true)
                                                   @Encrypt
                                                   @PathVariable("host_id") Long hostId,
                                                   @ApiParam(value = "docker实例id", required = true)
                                                   @Encrypt
                                                   @PathVariable("instance_id") Long instanceId) {
        devopsHostService.startDockerProcess(projectId, hostId, instanceId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("获取主机资源使用率")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{host_id}/resource_usage_info")
    public ResponseEntity<ResourceUsageInfoVO> queryResourceUsageInfo(@ApiParam(value = "项目id", required = true)
                                                                      @PathVariable("project_id") Long projectId,
                                                                      @ApiParam(value = "主机id", required = true)
                                                                      @Encrypt
                                                                      @PathVariable("host_id") Long hostId) {
        return ResponseEntity.ok(devopsHostService.queryResourceUsageInfo(projectId, hostId));
    }

    @ApiOperation("下载创建主机脚本")
    @Permission(permissionPublic = true)
    @GetMapping("/{host_id}/download_file/{token}")
    public ResponseEntity<String> downloadCreateHostFile(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @PathVariable("host_id") Long hostId,
            @ApiParam(value = "token", required = true)
            @PathVariable("token") String token,
            HttpServletResponse res) {
        return Optional.ofNullable(devopsHostService.downloadCreateHostFile(projectId, hostId, token, res))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.host.insert"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询连接主机的命令")
    @GetMapping("/{host_id}/link_shell")
    public ResponseEntity<String> queryShell(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id", required = true)
            @PathVariable(value = "host_id") Long hostId) {
        return ResponseEntity.ok(devopsHostService.queryShell(projectId, hostId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询删除主机的命令")
    @GetMapping("/{host_id}/uninstall_shell")
    public ResponseEntity<String> queryUninstallShell(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id", required = true)
            @PathVariable(value = "host_id") Long hostId) {
        return ResponseEntity.ok(devopsHostService.queryUninstallShell(projectId, hostId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询主机的应用实例列表")
    @GetMapping("/app/{app_service_id}/instance/list")
    @CustomPageRequest
    public ResponseEntity<List<?>> queryInstanceList(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id", required = true)
            @RequestParam(value = "host_id", required = false) Long hostId,
            @ApiParam(value = "应用服务id", required = true)
            @Encrypt @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiIgnore @PageableDefault() PageRequest pageRequest) {
        return ResponseEntity.ok(devopsHostService.queryInstanceList(projectId, hostId, appServiceId, pageRequest));
    }
}
