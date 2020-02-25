package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.PolarisScanningService;

/**
 * @author zmf
 * @since 2/12/20
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/polaris")
public class PolarisScanningController {
    @Autowired
    private PolarisScanningService polarisScanningService;

    @ApiOperation("查询扫描纪录")
    @GetMapping("/records")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER}, type = ResourceType.PROJECT)
    public ResponseEntity<DevopsPolarisRecordRespVO> queryRecordByScopeAndScopeId(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("扫描的范围 env/cluster")
            @RequestParam("scope") String scope,
            @ApiParam("对应scope的envId或者clusterId")
            @RequestParam("scope_id") Long scopeId) {
        return new ResponseEntity<>(polarisScanningService.queryRecordByScopeAndScopeId(projectId, scope, scopeId), HttpStatus.OK);
    }

    @ApiOperation("获取扫描的环境报告")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER}, type = ResourceType.PROJECT)
    @GetMapping("/envs/{env_id}")
    @ResponseBody
    public ResponseEntity<String> queryEnvPolarisResult(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的环境的id")
            @PathVariable("env_id") Long envId) {
        return new ResponseEntity<>(polarisScanningService.queryEnvPolarisResult(projectId, envId), HttpStatus.OK);
    }

    @ApiOperation("扫描环境")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER}, type = ResourceType.PROJECT)
    @PostMapping("/envs/{env_id}")
    public ResponseEntity<DevopsPolarisRecordVO> scanEnv(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的环境的id")
            @PathVariable("env_id") Long envId) {
        return new ResponseEntity<>(polarisScanningService.scanEnv(projectId, envId), HttpStatus.OK);
    }

    @ApiOperation("获取扫描的集群概览报告")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER}, type = ResourceType.PROJECT)
    @GetMapping("/clusters/{cluster_id}/summary")
    public ResponseEntity<DevopsPolarisSummaryVO> clusterPolarisSummary(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的集群的id")
            @PathVariable("cluster_id") Long clusterId) {
        return new ResponseEntity<>(polarisScanningService.clusterPolarisSummary(projectId, clusterId), HttpStatus.OK);
    }

    @ApiOperation("获取扫描的集群环境详情报告")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER}, type = ResourceType.PROJECT)
    @GetMapping("/clusters/{cluster_id}/env_detail")
    public ResponseEntity<ClusterPolarisEnvDetailsVO> clusterPolarisEnvDetail(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的集群的id")
            @PathVariable("cluster_id") Long clusterId) {
        return new ResponseEntity<>(polarisScanningService.clusterPolarisEnvDetail(projectId, clusterId), HttpStatus.OK);
    }

    @ApiOperation("扫描集群")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER}, type = ResourceType.PROJECT)
    @PostMapping("/clusters/{cluster_id}")
    public ResponseEntity<DevopsPolarisRecordVO> scanCluster(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的集群的id")
            @PathVariable("cluster_id") Long clusterId) {
        return new ResponseEntity<>(polarisScanningService.scanCluster(projectId, clusterId), HttpStatus.OK);
    }
}
