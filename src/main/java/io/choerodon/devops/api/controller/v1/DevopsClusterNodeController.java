package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ClusterHostConnectionVO;
import io.choerodon.devops.api.vo.DevopsClusterNodeVO;
import io.choerodon.devops.api.vo.NodeOperatingResultVO;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author lihao
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/nodes")
public class DevopsClusterNodeController {

    @Autowired
    private DevopsClusterNodeService devopsClusterNodeService;

    @ApiOperation("测试节点连通性")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/connection_test")
    public ResponseEntity<Boolean> testConnection(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @RequestBody ClusterHostConnectionVO clusterHostConnectionVO) {
        return Results.success(devopsClusterNodeService.testConnection(projectId, clusterHostConnectionVO));
    }

    @ApiOperation("校验是否能够删除节点角色")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{node_id}/roles/{role}/check_enable_delete")
    public ResponseEntity<Boolean> checkEnableDeleteRole(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "node id")
            @PathVariable(value = "node_id") @Encrypt Long nodeId,
            @ApiParam(value = "role")
            @PathVariable(value = "role") Integer role) {
        return ResponseEntity.ok(devopsClusterNodeService.checkEnableDeleteRole(projectId, nodeId, role));
    }

    @ApiOperation("删除节点")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{node_id}")
    public ResponseEntity<Long> delete(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "node id")
            @PathVariable(value = "node_id") @Encrypt Long nodeId) {
        return ResponseEntity.ok(devopsClusterNodeService.delete(projectId, nodeId));
    }

    @ApiOperation("删除节点角色")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{node_id}/roles/{role}")
    public ResponseEntity<Long> deleteRole(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "node id")
            @PathVariable(value = "node_id") @Encrypt Long nodeId,
            @PathVariable(value = "role") Integer role) {
        return ResponseEntity.ok(devopsClusterNodeService.deleteRole(projectId, nodeId, role));
    }

    @ApiOperation("添加节点")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<Void> addNode(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "集群id")
            @RequestParam("cluster_id") @Encrypt Long clusterId,
            @RequestBody DevopsClusterNodeVO nodeVO) {
        devopsClusterNodeService.addNode(projectId, clusterId, nodeVO);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("校验操作结果")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check_operating_result")
    public ResponseEntity<NodeOperatingResultVO> checkOperatingResult(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "操作记录id")
            @RequestParam(value = "operationRecordId") Long operationRecordId) {
        return ResponseEntity.ok(devopsClusterNodeService.checkOperatingResult(projectId,operationRecordId));
    }

}
