package io.choerodon.devops.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import io.choerodon.devops.app.service.ClusterNodeInfoService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/clusters")
public class DevopsClusterController {
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private ClusterNodeInfoService clusterNodeInfoService;

    /**
     * 项目下创建集群
     *
     * @param projectId          项目Id
     * @param devopsClusterReqVO 集群信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建集群")
    @PostMapping("/create")
    public ResponseEntity<String> create(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群信息", required = true)
            @RequestBody @Valid DevopsClusterReqVO devopsClusterReqVO) throws Exception {
        return ResponseEntity.ok(devopsClusterService.createCluster(projectId, devopsClusterReqVO));
    }

    /**
     * 获得节点检测进度
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "获得节点检测进度")
    @GetMapping("/check_progress")
    public ResponseEntity<DevopsNodeCheckResultVO> checkProgress(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(value = "key") String key) {
        return ResponseEntity.ok(devopsClusterService.checkProgress(projectId, key));
    }

    /**
     * 重试创建集群
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下重试创建集群")
    @PostMapping("/retry_create")
    public ResponseEntity<Void> retryCreate(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(value = "cluster_id") @Encrypt Long clusterId) {
        devopsClusterService.retryInstallK8s(projectId, clusterId);
        return ResponseEntity.ok().build();
    }

    /**
     * 项目下激活集群
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目连接集群")
    @PostMapping("/activate")
    public ResponseEntity<String> enable(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群信息", required = true)
            @RequestBody @Valid DevopsClusterReqVO devopsClusterReqVO) {
        return ResponseEntity.ok(devopsClusterService.activateCluster(projectId, devopsClusterReqVO));
    }

    /**
     * 更新集群
     *
     * @param projectId             项目Id
     * @param devopsClusterUpdateVO 集群对象
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "更新集群")
    @PutMapping("/{cluster_id}")
    public void update(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id")
            @PathVariable("cluster_id") Long clusterId,
            @ApiParam(value = "集群对象")
            @RequestBody @Valid DevopsClusterUpdateVO devopsClusterUpdateVO) {
        devopsClusterService.updateCluster(projectId, clusterId, devopsClusterUpdateVO);
    }

    /**
     * 查询单个集群信息
     *
     * @param projectId 项目Id
     * @param clusterId 集群Id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询单个集群信息")
    @GetMapping("/{cluster_id}")
    public ResponseEntity<DevopsClusterRepVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cluster_id") Long clusterId) {
        return ResponseEntity.ok(devopsClusterService.query(clusterId));
    }

    /**
     * 根据code查询集群
     *
     * @param projectId 项目Id
     * @param code      集群Code
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据code查询集群")
    @GetMapping("/query_by_code")
    public ResponseEntity<DevopsClusterRepVO> queryByCode(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Code")
            @RequestParam String code) {
        return ResponseEntity.ok(devopsClusterService.queryByCode(projectId, code));
    }

    /**
     * 校验集群名唯一性
     *
     * @param projectId 项目id
     * @param name      集群name
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验集群名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群name", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(devopsClusterService.isNameUnique(projectId, name));
    }

    /**
     * 校验集群编码唯一性
     *
     * @param projectId 项目id
     * @param code      集群code
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验集群code唯一性")
    @GetMapping(value = "/check_code")
    public ResponseEntity<Boolean> checkCode(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群code", required = true)
            @RequestParam String code) {
        return ResponseEntity.ok(devopsClusterService.isCodeUnique(projectId, code));
    }

    /**
     * 分页查询集群下已有权限的项目列表
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param pageable  分页参数
     * @param params    查询参数
     * @return page
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询集群下已有权限的项目列表")
    @PostMapping("/{cluster_id}/permission/page_related")
    public ResponseEntity<Page<ProjectReqVO>> pageRelatedProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cluster_id") Long clusterId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsClusterService.pageRelatedProjects(projectId, clusterId, pageable, params));
    }

    /**
     * 列出组织下所有项目中在数据库中没有权限关联关系的项目(不论当前数据库中是否跳过权限检查)
     *
     * @param projectId 项目ID
     * @param clusterId 集群ID
     * @param params    搜索参数
     * @return 列出组织下所有项目中在数据库中没有权限关联关系的项目
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询组织下所有与该集群未分配权限的项目")
    @PostMapping(value = "/{cluster_id}/permission/list_non_related")
    public ResponseEntity<Page<ProjectReqVO>> listAllNonRelatedProjects(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @Encrypt
            @ApiParam(value = "指定项目id")
            @RequestParam(value = "id", required = false) Long selectedProjectId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsClusterService.listNonRelatedProjects(projectId, clusterId, selectedProjectId, pageable, params));
    }

    /**
     * 集群下为项目分配权限
     *
     * @param clusterId                       集群id
     * @param devopsClusterPermissionUpdateVO 权限分配信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "集群下为项目分配权限")
    @PostMapping(value = "/{cluster_id}/permission")
    public ResponseEntity<Void> assignPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId,
            @ApiParam(value = "权限分配信息")
            @RequestBody @Valid DevopsClusterPermissionUpdateVO devopsClusterPermissionUpdateVO) {
        devopsClusterService.assignPermission(projectId, devopsClusterPermissionUpdateVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除集群下该项目的权限
     *
     * @param projectId       项目id
     * @param clusterId       集群id
     * @param projectToDelete 要删除权限的项目id
     * @return NO_CONTENT
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除集群下该项目的权限")
    @DeleteMapping(value = "/{cluster_id}/permission")
    public ResponseEntity<Void> deletePermissionOfProject(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId,
            @ApiParam(value = "要删除权限的项目id", required = true)
            @RequestParam(value = "delete_project_id") Long projectToDelete) {
        devopsClusterService.deletePermissionOfProject(projectId, clusterId, projectToDelete);
        return ResponseEntity.noContent().build();
    }


    /**
     * 查询激活集群的命令
     *
     * @param projectId 项目ID
     * @param clusterId 集群Id
     * @return String
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询激活集群的命令")
    @CustomPageRequest
    @GetMapping("/query_shell/{cluster_id}")
    public ResponseEntity<String> queryShell(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId) {
        return ResponseEntity.ok(devopsClusterService.queryShell(clusterId));
    }

    /**
     * 查询项目下的集群以及所有节点名称
     *
     * @param projectId 项目ID
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下所有集群以及所有的节点名称(树形目录)")
    @GetMapping("/tree_menu")
    public ResponseEntity<List<DevopsClusterBasicInfoVO>> queryClustersAndNodes(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsClusterService.queryClustersAndNodes(projectId));
    }

    /**
     * 分页查询集群列表
     *
     * @param projectId 项目ID
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询集群列表")
    @CustomPageRequest
    @PostMapping("/page_cluster")
    public ResponseEntity<Page<ClusterWithNodesVO>> pageCluster(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "是否需要分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsClusterService.pageClusters(projectId, doPage, pageable, params));
    }


    /**
     * 删除集群
     *
     * @param projectId 项目ID
     * @param clusterId 集群Id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除集群")
    @DeleteMapping("/{cluster_id}")
    public void deleteCluster(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cluster_id") Long clusterId) {
        devopsClusterService.deleteCluster(projectId, clusterId);
    }

    /**
     * 查询集群下是否关联已连接环境
     *
     * @param projectId 项目ID
     * @param clusterId 集群Id
     * @return String
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询集群下是否关联已连接环境或者存在PV")
    @GetMapping("/{cluster_id}/check_connect_envs_and_pv")
    public ResponseEntity<ClusterMsgVO> checkConnectEnvsAndPV(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cluster_id") Long clusterId) {
        return ResponseEntity.ok(devopsClusterService.checkConnectEnvsAndPV(clusterId));
    }

    /**
     * 分页查询节点下的Pod
     *
     * @param projectId   项目id
     * @param clusterId   集群id
     * @param nodeName    节点名称
     * @param pageable    分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询节点下的Pod")
    @CustomPageRequest
    @PostMapping(value = "/page_node_pods")
    public ResponseEntity<Page<DevopsEnvPodVO>> pageQueryPodsByNodeName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @ApiParam(value = "节点名称", required = true)
            @RequestParam(value = "node_name") String nodeName,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数", required = false)
            @RequestBody(required = false) String searchParam) {
        return ResponseEntity.ok(devopsClusterService.pagePodsByNodeName(clusterId, nodeName, pageable, searchParam));
    }

    /**
     * 分页查询集群下的节点
     *
     * @param projectId 项目ID
     * @param clusterId 集群id
     * @param pageable  分页参数
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询集群下的节点")
    @CustomPageRequest
    @GetMapping("/page_nodes")
    public ResponseEntity<Page<ClusterNodeInfoVO>> listClusterNodes(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable) {
        return new ResponseEntity<>(clusterNodeInfoService.pageClusterNodeInfo(clusterId, projectId, pageable), HttpStatus.OK);
    }


    /**
     * 根据集群id和节点名查询节点状态信息
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param nodeName  节点名称
     * @return node information
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据集群id和节点名查询节点状态信息")
    @GetMapping(value = "/nodes")
    public ResponseEntity<ClusterNodeInfoVO> queryNodeInfo(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @ApiParam(value = "节点名称", required = true)
            @RequestParam(value = "node_name") String nodeName) {
        return new ResponseEntity<>(clusterNodeInfoService.queryNodeInfo(projectId, clusterId, nodeName), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "检查是否还能创建集群")
    @GetMapping("/check_enable_create")
    public ResponseEntity<Boolean> checkEnableCreateCluster(@PathVariable(name = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsClusterService.checkEnableCreateCluster(projectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(("查询指定集群下的节点名称"))
    @GetMapping("/{cluster_id}/node_names")
    public ResponseEntity<List<String>> getNodeNames(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @Encrypt
            @PathVariable(value = "cluster_id") Long clusterId) {
        return new ResponseEntity<>(clusterNodeInfoService.queryNodeName(projectId, clusterId), HttpStatus.OK);
    }


    /**
     * 获取集群的数量
     *
     * @return 环境数量
     */
    @ApiOperation("获取集群的数量")
    @Permission(permissionWithin = true)
    @GetMapping("/count_by_options")
    public ResponseEntity<Long> countClusterByOptions(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId) {
        return new ResponseEntity<>(devopsClusterService.countClusterByOptions(projectId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "断开连接")
    @GetMapping("/{cluster_id}/disconnection")
    public ResponseEntity<String> disconnectionHost(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @Encrypt
            @PathVariable(value = "cluster_id") Long clusterId) {
        return ResponseEntity.ok(devopsClusterService.disconnectionHost(clusterId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重启集群agent")
    @PostMapping(value = "/{cluster_id}/restart")
    public ResponseEntity<Void> restartClusterAgent(@ApiParam(value = "项目id", required = true)
                                                    @PathVariable(value = "project_id") Long projectId,
                                                    @Encrypt
                                                    @ApiParam(value = "集群id", required = true)
                                                    @PathVariable(value = "cluster_id") Long clusterId) {
        devopsClusterService.restartAgent(projectId, clusterId);
        return ResponseEntity.noContent().build();
    }
}
