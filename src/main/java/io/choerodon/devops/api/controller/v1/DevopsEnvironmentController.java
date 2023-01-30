package io.choerodon.devops.api.controller.v1;

import java.util.List;
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
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by younger on 2018/4/9.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/envs")
public class DevopsEnvironmentController {

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    /**
     * 项目下创建环境
     *
     * @param projectId              项目id
     * @param devopsEnvironmentReqVO 环境信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下创建环境")
    @PostMapping
    public ResponseEntity<Void> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境信息", required = true)
            @RequestBody @Valid DevopsEnvironmentReqVO devopsEnvironmentReqVO) {
        devopsEnvironmentService.create(projectId, devopsEnvironmentReqVO);
        return ResponseEntity.ok().build();
    }


    /**
     * 实例视图查询环境及其下服务及实例
     *
     * @param projectId 项目id
     * @return 实例视图树形目录所需的数据
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "实例视图查询环境及其下服务及实例")
    @GetMapping(value = "/ins_tree_menu")
    public ResponseEntity<List<DevopsEnvironmentViewVO>> listEnvTree(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(devopsEnvironmentService.listInstanceEnvTree(projectId), HttpStatus.OK);
    }

    /**
     * 资源视图查询项目下环境及其下各种资源的基本信息
     *
     * @param projectId 项目id
     * @return 资源视图树形目录所需目录
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "资源视图查询项目下环境及其下各种资源的基本信息")
    @GetMapping(value = "/resource_tree_menu")
    public ResponseEntity<List<DevopsResourceEnvOverviewVO>> listResourceEnvTree(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(devopsEnvironmentService.listResourceEnvTree(projectId), HttpStatus.OK);
    }

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @param active    是否启用
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下查询环境")
    @GetMapping(value = "/list_by_active")
    public ResponseEntity<List<DevopsEnvironmentRepVO>> listByActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam(value = "active") Boolean active) {
        return ResponseEntity.ok(devopsEnvironmentService.listByProjectIdAndActive(projectId, active));
    }

    /**
     * 项目下启用停用环境
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @param active        是否可用
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下启用停用环境")
    @PutMapping("/{environment_id}/active")
    public ResponseEntity<Boolean> updateActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "environment_id") Long environmentId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam(value = "active") Boolean active) {
        return ResponseEntity.ok(devopsEnvironmentService.updateActive(projectId, environmentId, active));
    }

    /**
     * 项目下查询单个环境
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @return DevopsEnvironmentUpdateDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下查询单个环境")
    @GetMapping("/{environment_id}")
    public ResponseEntity<DevopsEnvironmentUpdateVO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "environment_id") Long environmentId) {
        return new ResponseEntity<>(devopsEnvironmentService.query(environmentId), HttpStatus.OK);
    }


    /**
     * 实例视图查询单个环境信息
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @return 单个环境信息及其集群和GitOps处理情况
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "实例视图查询单个环境信息")
    @GetMapping("/{environment_id}/info")
    public ResponseEntity<DevopsEnvironmentInfoVO> queryEnvInfo(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "environment_id") Long environmentId) {
        return new ResponseEntity<>(devopsEnvironmentService.queryInfoById(projectId, environmentId), HttpStatus.OK);
    }


    /**
     * 查询环境下相关资源的数量
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 环境下相关资源的数量
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询环境下相关资源的数量")
    @GetMapping("/{env_id}/resource_count")
    public ResponseEntity<DevopsEnvResourceCountVO> queryEnvResourceCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsEnvironmentService.queryEnvResourceCount(envId));
    }


    /**
     * 项目下更新环境
     *
     * @param projectId                  项目id
     * @param devopsEnvironmentUpdateDTO 环境信息
     * @return DevopsEnvironmentUpdateDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下更新环境")
    @PutMapping
    public ResponseEntity<DevopsEnvironmentUpdateVO> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境信息", required = true)
            @RequestBody DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO) {
        return ResponseEntity.ok(devopsEnvironmentService.update(devopsEnvironmentUpdateDTO, projectId));
    }


    /**
     * 创建环境校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      服务code
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建环境校验编码是否存在")
    @GetMapping(value = "/check_code")
    public ResponseEntity<Boolean> checkCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @ApiParam(value = "环境编码", required = true)
            @RequestParam(value = "code") String code) {
        return ResponseEntity.ok(devopsEnvironmentService.isCodeValid(projectId, clusterId, code));
    }

    /**
     * 项目下查询有正在运行实例的环境
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下查询有正在运行实例的环境")
    @GetMapping(value = "/list_by_instance")
    public ResponseEntity<List<DevopsEnvironmentRepVO>> listByProjectId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId) {
        return ResponseEntity.ok(devopsEnvironmentService.listByProjectId(projectId, appServiceId));
    }


    /**
     * 查询环境同步状态
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return EnvSyncStatusDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询环境同步状态")
    @GetMapping(value = "/{env_id}/status")
    public ResponseEntity<EnvSyncStatusVO> queryEnvSyncStatus(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsEnvironmentService.queryEnvSyncStatus(projectId, envId));
    }


    /**
     * 分页查询环境下用户权限
     *
     * @param projectId 项目id
     * @param pageable  分页参数
     * @param envId     环境id
     * @param params    搜索参数
     * @return page
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    @ApiOperation(value = "分页查询环境下用户权限")
    @PostMapping(value = "/{env_id}/permission/page_by_options")
    public ResponseEntity<Page<DevopsUserPermissionVO>> pageEnvUserPermissions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "分页参数", required = true)
            @ApiIgnore @SortDefault(value = "creationDate", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsEnvironmentService
                .pageUserPermissionByEnvId(projectId, pageable, params, envId));
    }


    /**
     * 列出项目下所有与该环境未分配权限的项目成员
     *
     * @param projectId 项目ID
     * @param envId     环境ID
     * @param params    搜索参数
     * @return 所有与该环境未分配权限的项目成员
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "列出项目下所有与该环境未分配权限的项目成员")
    @PostMapping(value = "/{env_id}/permission/list_non_related")
    public ResponseEntity<Page<DevopsUserVO>> listAllNonRelatedMembers(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "分页参数", required = true)
            @ApiIgnore PageRequest pageable,
            @Encrypt
            @ApiParam(value = "指定用户id")
            @RequestParam(value = "iamUserId", required = false) Long selectedIamUserId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsEnvironmentService.listNonRelatedMembers(projectId, envId, selectedIamUserId, pageable, params));
    }

    /**
     * 删除该用户在该环境下的权限
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param userId    用户id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除该用户在该环境下的权限")
    @DeleteMapping(value = "/{env_id}/permission")
    public ResponseEntity<Void> deletePermissionOfUser(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "用户id", required = true)
            @RequestParam(value = "user_id") Long userId) {
        devopsEnvironmentService.deletePermissionOfUser(projectId, envId, userId);
        return ResponseEntity.noContent().build();
    }


    /**
     * 获取环境下所有用户权限（获取所有有环境权限的项目下项目成员）
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return baseList
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "获取环境下所有用户权限")
    @GetMapping(value = "/{env_id}/list_all")
    public ResponseEntity<List<DevopsUserVO>> listAllUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsEnvironmentService.listAllUserPermission(envId));
    }

    /**
     * 环境下为用户分配权限
     *
     * @param envId                       环境id
     * @param devopsEnvPermissionUpdateVO 权限分配信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "环境下为用户分配权限")
    @PostMapping(value = "/{env_id}/permission")
    public ResponseEntity<Void> updateEnvUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "有权限的用户ids")
            @RequestBody @Valid DevopsEnvPermissionUpdateVO devopsEnvPermissionUpdateVO) {
        devopsEnvironmentService.updateEnvUserPermission(projectId, devopsEnvPermissionUpdateVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除已停用的环境
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除未连接/已停用/失败的环境")
    @DeleteMapping(value = "/{env_id}")
    public ResponseEntity<Void> deleteDeactivatedEnvironment(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId) {
        devopsEnvironmentService.deleteDeactivatedOrFailedEnvironment(projectId, envId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下查询集群信息
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询集群信息")
    @GetMapping(value = "/list_clusters")
    public ResponseEntity<List<DevopsClusterRepVO>> listDevopsClusters(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsEnvironmentService.listDevopsCluster(projectId));
    }


    /**
     * 根据环境编码查询环境
     *
     * @param projectId 项目ID
     * @param code      环境code
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据环境编码查询环境")
    @GetMapping(value = "/query_by_code")
    public ResponseEntity<DevopsEnvironmentRepVO> queryByCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境编码", required = true)
            @RequestParam(value = "code") String code) {
        return ResponseEntity.ok(devopsEnvironmentService.queryByCode(projectId, code));
    }


    /**
     * 重试gitOps
     *
     * @param projectId 项目ID
     * @param envId     环境Id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试gitOps解析流程")
    @GetMapping(value = "/{env_id}/retry")
    public void retryByGitOps(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId) {
        devopsEnvironmentService.retryGitOps(projectId, envId);
    }

    /**
     * 项目下环境配置树形目录
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "环境树形目录")
    @GetMapping(value = "/env_tree_menu")
    public ResponseEntity<List<DevopsEnvGroupEnvsVO>> listEnvTreeMenu(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsEnvironmentService.listEnvTreeMenu(projectId));
    }

    /**
     * 项目下根据分组查看环境详情
     *
     * @param projectId 项目id
     * @param groupId   分组id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询环境")
    @GetMapping(value = "/list_by_group")
    public ResponseEntity<List<DevopsEnvironmentRepVO>> listByGroup(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "分组id")
            @RequestParam(value = "group_id", required = false) Long groupId) {
        return ResponseEntity.ok(devopsEnvironmentService.listByGroup(projectId, groupId));
    }

    /**
     * 查询指定环境是否可删除
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return true表示可以删除
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询指定环境是否可删除")
    @GetMapping(value = "/{env_id}/delete_check")
    public ResponseEntity<Boolean> deleteCheck(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id")
            @PathVariable(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsEnvironmentService.deleteCheck(projectId, envId));
    }

    /**
     * 查询指定环境是否可停用
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return true表示可以停用
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询指定环境是否可停用")
    @GetMapping(value = "/{env_id}/disable_check")
    public ResponseEntity<Boolean> disableCheck(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id")
            @PathVariable(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsEnvironmentService.disableCheck(projectId, envId));
    }

    /**
     * 检查资源是否存在
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param objectId  其他对象id
     * @param type      其他对象类型
     * @return boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查资源是否存在")
    @GetMapping(value = "/{env_id}/check")
    public ResponseEntity<EnvironmentMsgVO> checkExist(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "对象类型,对象类型为空时，表示查询环境是否存在")
            @RequestParam(value = "type", required = false) String type,
            @ApiParam(value = "对象id")
            @Encrypt
            @RequestParam(value = "object_id", required = false) Long objectId,
            @Encrypt
            @ApiParam(value = "环境id")
            @PathVariable(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsEnvironmentService.checkExist(projectId, envId, objectId, type));
    }

    /**
     * 获取环境的数量
     *
     * @param isFailed  是否失败
     * @param clusterId 集群id
     * @return 环境数量
     */
    @ApiOperation("查询环境的数量")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER}, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/count_by_options")
    public ResponseEntity<Long> countEnvByOptions(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "环境是否失败", required = false)
            @RequestParam(value = "is_failed", required = false) Boolean isFailed,
            @Encrypt
            @ApiParam(value = "集群id，传此值时表示查询集群下的环境，不传则查询项目下环境", required = false)
            @RequestParam(value = "cluster_id", required = false) Long clusterId) {
        return new ResponseEntity<>(devopsEnvironmentService.countEnvByOption(projectId, clusterId, isFailed), HttpStatus.OK);
    }

    @ApiOperation("一键开启所有自动部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{env_id}/open_auto_deploy")
    public ResponseEntity<Void> openAutoDeploy(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("环境id")
            @Encrypt
            @PathVariable("env_id") Long envId) {
        devopsEnvironmentService.updateAutoDeploy(projectId, envId, true);
        return Results.success();
    }

    @ApiOperation("一键关闭所有自动部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{env_id}/close_auto_deploy")
    public ResponseEntity<Void> closeAutoDeploy(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("环境id")
            @Encrypt
            @PathVariable("env_id") Long envId) {
        devopsEnvironmentService.updateAutoDeploy(projectId, envId, false);
        return Results.success();
    }

    @ApiOperation("查询一键部署状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{env_id}/query_auto_deploy")
    public ResponseEntity<EnvAutoDeployVO> queryAutoDeploy(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("环境id")
            @Encrypt
            @PathVariable("env_id") Long envId) {
        return Results.success(devopsEnvironmentService.queryAutoDeploy(projectId, envId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "检查是否还能创建环境")
    @GetMapping("/check_enable_create")
    public ResponseEntity<Boolean> checkEnableCreateEnv(@PathVariable(name = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsEnvironmentService.checkEnableCreateEnv(projectId));
    }

    @ApiOperation("开启确认values生效策略")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/enable_check_values_policy")
    public ResponseEntity<Void> enableCheckValuesPolicy(@ApiParam("项目id")
                                                        @PathVariable("project_id") Long projectId,
                                                        @ApiParam("环境id")
                                                        @Encrypt
                                                        @PathVariable("env_id") Long envId) {
        devopsEnvironmentService.updateCheckValuesPolicy(projectId, envId, true);
        return Results.success();
    }

    @ApiOperation("关闭确认values生效策略")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/disable_check_values_policy")
    public ResponseEntity<Void> disableCheckValuesPolicy(@ApiParam("项目id")
                                                         @PathVariable("project_id") Long projectId,
                                                         @ApiParam("环境id")
                                                         @Encrypt
                                                         @PathVariable("env_id") Long envId) {
        devopsEnvironmentService.updateCheckValuesPolicy(projectId, envId, false);
        return Results.success();
    }
}
