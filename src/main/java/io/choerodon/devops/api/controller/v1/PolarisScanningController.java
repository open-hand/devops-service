package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ClusterPolarisEnvDetailsVO;
import io.choerodon.devops.api.vo.DevopsPolarisRecordRespVO;
import io.choerodon.devops.api.vo.DevopsPolarisRecordVO;
import io.choerodon.devops.api.vo.DevopsPolarisSummaryVO;
import io.choerodon.devops.app.service.PolarisScanningService;
import io.choerodon.swagger.annotation.Permission;

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
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER}, level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<DevopsPolarisRecordRespVO> queryRecordByScopeAndScopeId(
            @Encrypt
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("扫描的范围 env/cluster")
            @RequestParam("scope") String scope,
            @Encrypt
            @ApiParam("对应scope的envId或者clusterId")
            @RequestParam("scope_id") Long scopeId) {
        return new ResponseEntity<>(polarisScanningService.queryRecordByScopeAndScopeId(projectId, scope, scopeId), HttpStatus.OK);
    }

    @ApiOperation("获取扫描的环境报告")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER}, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/envs/{env_id}")
    @ResponseBody
    public ResponseEntity<String> queryEnvPolarisResult(
            @Encrypt
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @ApiParam("需要扫描的环境的id")
            @PathVariable("env_id") Long envId) {
        return new ResponseEntity<>(polarisScanningService.queryEnvPolarisResult(projectId, envId), HttpStatus.OK);
    }

    @ApiOperation("扫描环境")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER}, level = ResourceLevel.ORGANIZATION)
    @PostMapping("/envs/{env_id}")
    public ResponseEntity<DevopsPolarisRecordVO> scanEnv(
            @Encrypt
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @ApiParam("需要扫描的环境的id")
            @PathVariable("env_id") Long envId) {
        return new ResponseEntity<>(polarisScanningService.scanEnv(projectId, envId), HttpStatus.OK);
    }

    @ApiOperation("获取扫描的集群概览报告")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER}, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/clusters/{cluster_id}/summary")
    public ResponseEntity<DevopsPolarisSummaryVO> clusterPolarisSummary(
            @Encrypt
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @ApiParam("需要扫描的集群的id")
            @PathVariable("cluster_id") Long clusterId) {
        return new ResponseEntity<>(polarisScanningService.clusterPolarisSummary(projectId, clusterId), HttpStatus.OK);
    }

    @ApiOperation("获取扫描的集群环境详情报告")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER}, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/clusters/{cluster_id}/env_detail")
    public ResponseEntity<ClusterPolarisEnvDetailsVO> clusterPolarisEnvDetail(
            @Encrypt
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @ApiParam("需要扫描的集群的id")
            @PathVariable("cluster_id") Long clusterId) {
        return new ResponseEntity<>(polarisScanningService.clusterPolarisEnvDetail(projectId, clusterId), HttpStatus.OK);
    }

    @ApiOperation("扫描集群")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER}, level = ResourceLevel.ORGANIZATION)
    @PostMapping("/clusters/{cluster_id}")
    public ResponseEntity<DevopsPolarisRecordVO> scanCluster(
            @Encrypt
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @ApiParam("需要扫描的集群的id")
            @PathVariable("cluster_id") Long clusterId) {
        return new ResponseEntity<>(polarisScanningService.scanCluster(projectId, clusterId), HttpStatus.OK);
    }
}
