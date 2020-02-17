package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.app.service.PolarisScanningService;
import io.choerodon.devops.infra.dto.DevopsPolarisRecordDTO;

/**
 * @author zmf
 * @since 2/12/20
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/polaris")
public class PolarisScanningController {
    @Autowired
    private PolarisScanningService polarisScanningService;

    @ApiOperation("获取扫描的环境报告")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping("/envs/{env_id}")
    public ResponseEntity<Object> queryEnvPolarisResult(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的环境的id")
            @PathVariable("env_id") Long envId) {
        return null;
    }

    @ApiOperation("扫描环境")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER})
    @PostMapping("/envs/{env_id}")
    public ResponseEntity<DevopsPolarisRecordDTO> scanEnv(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的环境的id")
            @PathVariable("env_id") Long envId) {
        return new ResponseEntity<>(polarisScanningService.scanEnv(envId), HttpStatus.OK);
    }

    @ApiOperation("获取扫描的集群报告")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping("/clusters/{cluster_id}")
    public ResponseEntity<Object> clusterPolarisResult(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的集群的id")
            @PathVariable("cluster_id") Long clusterId) {
        return null;
    }

    @ApiOperation("扫描集群")
    @Permission(roles = {InitRoleCode.PROJECT_OWNER})
    @PostMapping("/clusters/{cluster_id}")
    public ResponseEntity<DevopsPolarisRecordDTO> scanCluster(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("需要扫描的集群的id")
            @PathVariable("cluster_id") Long clusterId) {
        return new ResponseEntity<>(polarisScanningService.scanCluster(clusterId), HttpStatus.OK);
    }
}
