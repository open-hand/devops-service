package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.ClusterNodeInfoService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/clusters")
public class DevopsClusterController {
    private static final String ERROR_CLUSTER_QUERY = "error.cluster.query";

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
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "项目下创建集群")
    @PostMapping
    public ResponseEntity<String> create(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群信息", required = true)
            @RequestBody DevopsClusterReqVO devopsClusterReqVO) {
        return Optional.ofNullable(devopsClusterService.createCluster(projectId, devopsClusterReqVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.cluster.insert"));
    }

    /**
     * 更新集群
     *
     * @param projectId          项目Id
     * @param devopsClusterReqVO 集群对象
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "更新集群")
    @PutMapping
    public void update(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Id")
            @RequestParam Long clusterId,
            @ApiParam(value = "集群对象")
            @RequestBody DevopsClusterReqVO devopsClusterReqVO) {
        devopsClusterService.updateCluster(clusterId, devopsClusterReqVO);
    }

    /**
     * 查询单个集群信息
     *
     * @param projectId 项目Id
     * @param clusterId 集群Id
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "查询单个集群信息")
    @GetMapping("/{cluster_id}")
    public ResponseEntity<DevopsClusterRepVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cluster_id") Long clusterId) {
        return Optional.ofNullable(devopsClusterService.query(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_CLUSTER_QUERY));
    }

    /**
     * 根据code查询集群
     *
     * @param projectId 项目Id
     * @param code      集群Code
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "根据code查询集群")
    @GetMapping("/query_by_code")
    public ResponseEntity<DevopsClusterRepVO> queryByCode(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Code")
            @RequestParam String code) {
        return Optional.ofNullable(devopsClusterService.queryByCode(projectId, code))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_CLUSTER_QUERY));
    }

    /**
     * 校验集群名唯一性
     *
     * @param projectId 项目id
     * @param name      集群name
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "校验集群名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群name", required = true)
            @RequestParam String name) {
        devopsClusterService.checkName(projectId, name);
    }

    /**
     * 校验集群编码唯一性
     *
     * @param projectId 项目id
     * @param code      集群code
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "校验集群名唯一性")
    @GetMapping(value = "/check_code")
    public void checkCode(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群code", required = true)
            @RequestParam String code) {
        devopsClusterService.checkCode(projectId, code);
    }

    /**
     * 分页查询项目列表
     *
     * @param projectId 项目id
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "分页查询项目列表")
    @CustomPageRequest
    @PostMapping("/page_projects")
    public ResponseEntity<PageInfo<ProjectReqVO>> pageProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "集群Id")
            @RequestParam(required = false) Long clusterId,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody String[] params) {
        return Optional.ofNullable(devopsClusterService.pageProjects(projectId, clusterId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }

    /**
     * 查询集群下已有权限的项目列表
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "查询集群下已有权限的项目列表")
    @GetMapping("/list_cluster_projects/{cluster_id}")
    public ResponseEntity<List<ProjectReqVO>> listClusterProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cluster_id") Long clusterId) {
        return Optional.ofNullable(devopsClusterService.listClusterProjects(projectId, clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }


    /**
     * 查询shell脚本
     *
     * @param projectId 项目ID
     * @param clusterId 集群Id
     * @return String
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "查询shell脚本")
    @CustomPageRequest
    @GetMapping("/query_shell/{cluster_id}")
    public ResponseEntity<String> queryShell(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId) {
        return Optional.ofNullable(devopsClusterService.queryShell(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_CLUSTER_QUERY));
    }

    /**
     * 集群列表查询
     *
     * @param projectId 项目ID
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "集群列表查询")
    @CustomPageRequest
    @PostMapping("/page_cluster")
    public ResponseEntity<PageInfo<ClusterWithNodesVO>> pageCluster(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "是否需要分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsClusterService.pageClusters(projectId, doPage, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_CLUSTER_QUERY));
    }


    /**
     * 删除集群
     *
     * @param projectId 项目ID
     * @param clusterId 集群Id
     * @return String
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "删除集群")
    @DeleteMapping("/{cluster_id}")
    public void deleteCluster(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cluster_id") Long clusterId) {
        devopsClusterService.deleteCluster(clusterId);
    }

    /**
     * 查询集群下是否关联已连接环境
     *
     * @param projectId 项目ID
     * @param clusterId 集群Id
     * @return String
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "查询集群下是否关联已连接环境")
    @GetMapping("/{cluster_id}/check_connect_envs")
    public ResponseEntity<Boolean> checkConnectEnvs(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cluster_id") Long clusterId) {
        return Optional.ofNullable(devopsClusterService.checkConnectEnvs(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.connect.env.query"));
    }

    /**
     * 分页查询节点下的Pod
     *
     * @param projectId   项目id
     * @param clusterId   集群id
     * @param nodeName    节点名称
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "分页查询节点下的Pod")
    @CustomPageRequest
    @PostMapping(value = "/page_node_pods")
    public ResponseEntity<PageInfo<DevopsClusterPodVO>> pageQueryPodsByNodeName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @ApiParam(value = "节点名称", required = true)
            @RequestParam(value = "node_name") String nodeName,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数", required = false)
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsClusterService.pagePodsByNodeName(clusterId, nodeName, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.node.pod.query", nodeName));
    }

    /**
     * 分页查询集群下的节点
     *
     * @param projectId   项目ID
     * @param clusterId   集群id
     * @param pageRequest 分页参数
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "分页查询集群下的节点")
    @CustomPageRequest
    @GetMapping("/page_nodes")
    public ResponseEntity<PageInfo<ClusterNodeInfoVO>> listClusterNodes(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest) {
        return new ResponseEntity<>(clusterNodeInfoService.pageClusterNodeInfo(clusterId, projectId, pageRequest), HttpStatus.OK);
    }


    /**
     * 根据集群id和节点名查询节点状态信息
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param nodeName  节点名称
     * @return node information
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "根据集群id和节点名查询节点状态信息")
    @GetMapping(value = "/nodes")
    public ResponseEntity<ClusterNodeInfoVO> queryNodeInfo(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @ApiParam(value = "节点名称", required = true)
            @RequestParam(value = "node_name") String nodeName) {
        return new ResponseEntity<>(clusterNodeInfoService.queryNodeInfo(projectId, clusterId, nodeName), HttpStatus.OK);
    }


}
