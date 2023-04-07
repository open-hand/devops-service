package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.api.vo.host.DevopsJavaInstanceVO;
import io.choerodon.devops.api.vo.host.ResourceUsageInfoVO;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
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
        return Results.success(devopsHostService.createHost(projectId, devopsHostCreateRequestVO));
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
                    @SortDefault(value = "host_status", direction = Sort.Direction.ASC),
                    @SortDefault(value = "id", direction = Sort.Direction.DESC)})
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "是否带有创建者信息")
            @RequestParam(value = "with_creator_info", required = false, defaultValue = "false")
            Boolean withCreatorInfo,
            @ApiParam(value = "搜索参数")
            @RequestParam(value = "search_param", required = false) String searchParam,
            @ApiParam(value = "主机状态")
            @RequestParam(value = "host_status", required = false) String hostStatus,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "do_page", required = false, defaultValue = "true") Boolean doPage) {
        return Results.success(devopsHostService.pageByOptions(projectId, pageRequest, withCreatorInfo, searchParam, hostStatus, doPage));
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

    @ApiOperation("校验主机能否被删除（是否关联流水线）")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check/delete")
    public ResponseEntity<Boolean> checkHostDelete(@ApiParam(value = "项目id", required = true)
                                                   @PathVariable("project_id") Long projectId,
                                                   @ApiParam(value = "主机id", required = true)
                                                   @Encrypt @RequestParam("host_id") Long hostId) {
        return Results.success(devopsHostService.checkHostDelete(projectId, hostId));
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
            @PathVariable("token") String token) {
        return ResponseEntity.ok(devopsHostService.downloadCreateHostFile(projectId, hostId, token));
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
        return ResponseEntity.ok(devopsHostService.queryShell(projectId, hostId, false, ""));
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
    @ApiOperation(value = "主机连接")
    @PostMapping("/{host_id}/connection_host")
    public ResponseEntity<Void> connectHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @Encrypt @PathVariable("host_id") Long hostId,
            @RequestBody @Valid DevopsHostConnectionVO devopsHostConnectionVO) {
        devopsHostService.connectHost(projectId, hostId, devopsHostConnectionVO);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "主机连接")
    @GetMapping("/{host_id}/connection_host")
    public ResponseEntity<Map<Object, Object>> queryConnectHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @Encrypt @PathVariable("host_id") Long hostId) {
        return ResponseEntity.ok(devopsHostService.queryConnectHost(projectId, hostId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "主机连接测试")
    @PostMapping("/{host_id}/connection_host_test")
    public ResponseEntity<Map<String, String>> testConnectHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "主机id", required = true)
            @Encrypt
            @PathVariable("host_id") Long hostId,
            @RequestBody @Valid DevopsHostConnectionVO devopsHostConnectionVO) {
        return ResponseEntity.ok(devopsHostService.testConnectHost(projectId, hostId, devopsHostConnectionVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "断开连接")
    @GetMapping("/disconnection")
    public ResponseEntity<String> disconnectionHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(devopsHostService.disconnectionHost());
    }

    /**
     * 分页查询主机下用户权限
     *
     * @param projectId 项目id
     * @param pageable  分页参数
     * @param hostId    主机id
     * @param params    搜索参数
     * @return page
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @CustomPageRequest
    @ApiOperation(value = "分页查询主机下用户权限")
    @PostMapping(value = "/{host_id}/permission/page_by_options")
    public ResponseEntity<Page<DevopsHostUserPermissionVO>> pageHostUserPermissions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "主机id", required = true)
            @PathVariable(value = "host_id") Long hostId,
            @ApiParam(value = "分页参数", required = true)
            @ApiIgnore @SortDefault(value = "creationDate", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsHostService.pageUserPermissionByHostId(projectId, pageable, params, hostId));
    }


    /**
     * 列出项目下所有与该主机未分配权限的项目成员
     *
     * @param projectId 项目ID
     * @param hostId    主机ID
     * @param params    搜索参数
     * @return 所有与该主机未分配权限的项目成员
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "列出项目下所有与该主机未分配权限的项目成员")
    @PostMapping(value = "/{host_id}/permission/list_non_related")
    public ResponseEntity<Page<DevopsUserVO>> listAllNonRelatedMembers(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "主机id", required = true)
            @PathVariable(value = "host_id") Long hostId,
            @ApiParam(value = "分页参数", required = true)
            @ApiIgnore PageRequest pageable,
            @Encrypt
            @ApiParam(value = "指定用户id")
            @RequestParam(value = "iamUserId", required = false) Long selectedIamUserId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsHostService.pageNonRelatedMembers(projectId, hostId, selectedIamUserId, pageable, params));
    }

    /**
     * 删除该用户在该主机下的权限
     *
     * @param projectId 项目id
     * @param hostId    主机id
     * @param userId    用户id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除该用户在该主机下的权限")
    @DeleteMapping(value = "/{host_id}/permission")
    public ResponseEntity<DevopsHostUserPermissionDeleteResultVO> deletePermissionOfUser(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "主机id", required = true)
            @PathVariable(value = "host_id") Long hostId,
            @Encrypt
            @ApiParam(value = "用户id", required = true)
            @RequestParam(value = "user_id") Long userId) {
        return ResponseEntity.ok(devopsHostService.deletePermissionOfUser(projectId, hostId, userId));
    }

    /**
     * 主机下为用户批量分配权限
     *
     * @param hostId                           主机id
     * @param devopsHostUserPermissionUpdateVO 权限分配信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "主机下为用户分配权限")
    @PostMapping(value = "/{host_id}/batch_update_permission")
    public ResponseEntity<Void> batchUpdateHostUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "主机id", required = true)
            @PathVariable(value = "host_id") Long hostId,
            @ApiParam(value = "权限信息")
            @RequestBody @Valid DevopsHostUserPermissionUpdateVO devopsHostUserPermissionUpdateVO) {
        devopsHostService.batchUpdateHostUserPermission(projectId, devopsHostUserPermissionUpdateVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重启主机agent")
    @PostMapping(value = "/{host_id}/restart")
    public ResponseEntity<Void> restartHostAgent(@ApiParam(value = "项目id", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @Encrypt
                                                 @ApiParam(value = "主机id", required = true)
                                                 @PathVariable(value = "host_id") Long hostId) {
        devopsHostService.restartAgent(projectId, hostId);
        return ResponseEntity.noContent().build();
    }
}
