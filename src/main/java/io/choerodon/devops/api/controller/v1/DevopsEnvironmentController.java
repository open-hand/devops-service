package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.mvc.EncryptDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Created by younger on 2018/4/9.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/envs")
public class DevopsEnvironmentController {

    private static final String ERROR_ENVIRONMENT_GET = "error.environment.get";
    private static final String ERROR_ENVIRONMENT_QUERY = "error.environment.query";

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    /**
     * 项目下创建环境
     *
     * @param projectId              项目id
     * @param devopsEnvironmentReqVO 环境信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建环境")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境信息", required = true)
            @EncryptDTO @RequestBody @Valid DevopsEnvironmentReqVO devopsEnvironmentReqVO) {
        devopsEnvironmentService.create(projectId, devopsEnvironmentReqVO);
        return new ResponseEntity(HttpStatus.OK);
    }


    /**
     * 实例视图查询环境及其下服务及实例
     *
     * @param projectId 项目id
     * @return 实例视图树形目录所需的数据
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询环境")
    @GetMapping(value = "/list_by_active")
    public ResponseEntity<List<DevopsEnvironmentRepVO>> listByActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam(value = "active") Boolean active) {
        return Optional.ofNullable(devopsEnvironmentService.listByProjectIdAndActive(projectId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_GET));
    }

    // TODO 该接口未找到使用的地方，下次发版时去掉

    /**
     * 项目下环境流水线查询环境
     *
     * @param projectId 项目id
     * @param active    是否启用
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下环境流水线查询环境")
    @GetMapping("/list_by_groups")
    public ResponseEntity<List<DevopsEnvGroupEnvsVO>> listByProjectIdAndActiveWithGroup(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam Boolean active) {
        return Optional.ofNullable(devopsEnvironmentService.listDevopsEnvGroupEnvs(projectId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_GET));
    }

    /**
     * 项目下启用停用环境
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @param active        是否可用
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下启用停用环境")
    @PutMapping("/{environment_id}/active")
    public ResponseEntity<Boolean> updateActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "environment_id") Long environmentId,
            @ApiParam(value = "是否启用", required = true)
            @RequestParam(value = "active") Boolean active) {
        return Optional.ofNullable(devopsEnvironmentService.updateActive(projectId, environmentId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.active"));
    }

    /**
     * 项目下查询单个环境
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @return DevopsEnvironmentUpdateDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询单个环境")
    @GetMapping("/{environment_id}")
    public ResponseEntity<DevopsEnvironmentUpdateVO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "environment_id") Long environmentId) {
        return new ResponseEntity<>(devopsEnvironmentService.query(environmentId), HttpStatus.OK);
    }


    /**
     * 实例视图查询单个环境信息
     *
     * @param projectId     项目id
     * @param environmentId 环境id
     * @return 单个环境信息及其集群和GitOps处理情况
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例视图查询单个环境信息")
    @GetMapping("/{environment_id}/info")
    public ResponseEntity<DevopsEnvironmentInfoVO> queryEnvInfo(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "environment_id") Long environmentId) {
        return new ResponseEntity<>(devopsEnvironmentService.queryInfoById(projectId, environmentId), HttpStatus.OK);
    }


    /**
     * 查询环境下相关资源的数量
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 环境下相关资源的数量
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询环境下相关资源的数量")
    @GetMapping("/{env_id}/resource_count")
    public ResponseEntity<DevopsEnvResourceCountVO> queryEnvResourceCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.queryEnvResourceCount(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_QUERY));
    }


    /**
     * 项目下更新环境
     *
     * @param projectId                  项目id
     * @param devopsEnvironmentUpdateDTO 环境信息
     * @return DevopsEnvironmentUpdateDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新环境")
    @PutMapping
    public ResponseEntity<DevopsEnvironmentUpdateVO> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境信息", required = true)
            @EncryptDTO @RequestBody DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO) {
        return Optional.ofNullable(devopsEnvironmentService.update(devopsEnvironmentUpdateDTO, projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.update"));
    }


    /**
     * 创建环境校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      服务code
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建环境校验编码是否存在")
    @GetMapping(value = "/check_code")
    public ResponseEntity<Boolean> checkCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
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
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询有正在运行实例的环境")
    @GetMapping(value = "/list_by_instance")
    public ResponseEntity<List<DevopsEnvironmentRepVO>> listByProjectId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id")
            @Encrypt(AppServiceDTO.ENCRYPT_KEY) @RequestParam(value = "app_service_id", required = false) Long appServiceId) {
        return Optional.ofNullable(devopsEnvironmentService.listByProjectId(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.environment.running.get"));
    }


    /**
     * 查询环境同步状态
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return EnvSyncStatusDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询环境同步状态")
    @GetMapping(value = "/{env_id}/status")
    public ResponseEntity<EnvSyncStatusVO> queryEnvSyncStatus(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "项目id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.queryEnvSyncStatus(projectId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElse(new ResponseEntity<>(new EnvSyncStatusVO(), HttpStatus.OK));
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
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @CustomPageRequest
    @ApiOperation(value = "分页查询环境下用户权限")
    @PostMapping(value = "/{env_id}/permission/page_by_options")
    public ResponseEntity<Page<DevopsUserPermissionVO>> pageEnvUserPermissions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "分页参数", required = true)
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsEnvironmentService
                .pageUserPermissionByEnvId(projectId, pageable, params, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.user.permission.get"));
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
    public ResponseEntity<Page<DevopsEnvUserVO>> listAllNonRelatedMembers(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "分页参数", required = true)
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "指定用户id")
            @RequestParam(value = "iamUserId", required = false) Long selectedIamUserId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsEnvironmentService.listNonRelatedMembers(projectId, envId, selectedIamUserId, pageable, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.env.non.related.users"));
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
    public ResponseEntity deletePermissionOfUser(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "用户id", required = true)
            @RequestParam(value = "user_id") Long userId) {
        devopsEnvironmentService.deletePermissionOfUser(projectId, envId, userId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
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
    public ResponseEntity<List<DevopsEnvUserVO>> listAllUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.listAllUserPermission(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.user.permission.get"));
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
    public ResponseEntity updateEnvUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "有权限的用户ids")
            @EncryptDTO @RequestBody @Valid DevopsEnvPermissionUpdateVO devopsEnvPermissionUpdateVO) {
        devopsEnvironmentService.updateEnvUserPermission(devopsEnvPermissionUpdateVO);
        return new ResponseEntity(HttpStatus.OK);
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
    public ResponseEntity deleteDeactivatedEnvironment(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
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
        return Optional.ofNullable(devopsEnvironmentService.listDevopsCluster(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.cluster.query"));
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
        return Optional.ofNullable(devopsEnvironmentService.queryByCode(projectId, code))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_QUERY));
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
            @ApiParam(value = "环境编码", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        devopsEnvironmentService.retryGitOps(envId);
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
        return Optional.ofNullable(devopsEnvironmentService.listEnvTreeMenu(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_GET));
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
            @ApiParam(value = "分组id")
            @Encrypt(DevopsEnvGroupDTO.ENCRYPT_KEY) @RequestParam(value = "group_id", required = false) Long groupId) {
        return Optional.ofNullable(devopsEnvironmentService.listByGroup(projectId, groupId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_GET));
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
            @ApiParam(value = "环境id")
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.deleteCheck(projectId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_GET));
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
            @ApiParam(value = "环境id")
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.disableCheck(projectId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_GET));
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
            @RequestParam(value = "object_id", required = false) Long objectId,
            @ApiParam(value = "环境id")
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @PathVariable(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvironmentService.checkExist(projectId, envId, objectId, type))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_ENVIRONMENT_GET));
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
            @ApiParam(value = "集群id，传此值时表示查询集群下的环境，不传则查询项目下环境", required = false)
            @Encrypt(DevopsClusterDTO.ENCRYPT_KEY) @RequestParam(value = "cluster_id", required = false) Long clusterId) {
        return new ResponseEntity<>(devopsEnvironmentService.countEnvByOption(projectId, clusterId, isFailed), HttpStatus.OK);
    }
}
