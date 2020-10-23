package io.choerodon.devops.api.controller.v1;

import java.util.Set;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.HostConnectionVO;
import io.choerodon.devops.api.vo.NodeDeleteCheckVO;
import io.choerodon.devops.api.vo.NodeRoleDeleteCheckVO;
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
            @RequestBody @Valid HostConnectionVO hostConnectionVO) {
        return Results.success(devopsClusterNodeService.testConnection(projectId, hostConnectionVO));
    }

    @ApiOperation("校验是否能够删除节点")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{node_id}/check_enable_delete")
    public ResponseEntity<NodeDeleteCheckVO> checkEnableDelete(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "node id")
            @PathVariable(value = "node_id") @Encrypt Long nodeId) {
        return ResponseEntity.ok(devopsClusterNodeService.checkEnableDelete(projectId, nodeId));
    }

    @ApiOperation("删除节点")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{node_id}")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "node id")
            @PathVariable(value = "node_id") @Encrypt Long nodeId) {
        devopsClusterNodeService.delete(projectId, nodeId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("校验是否能够删除节点角色")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{node_id}/check_enable_delete_role")
    public ResponseEntity<NodeRoleDeleteCheckVO> checkEnableDeleteRole(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "node id")
            @PathVariable(value = "node_id") @Encrypt Long nodeId) {
        return ResponseEntity.ok(devopsClusterNodeService.checkEnableDeleteRole(projectId, nodeId));
    }

    @ApiOperation("删除节点角色")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{node_id}/roles/{role}")
    public ResponseEntity<Void> deleteRole(
            @ApiParam(value = "项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "node id")
            @PathVariable(value = "node_id") @Encrypt Long nodeId,
            @PathVariable(value = "roles") Set<Integer> roles) {
        devopsClusterNodeService.deleteRole(projectId, nodeId, roles);
        return ResponseEntity.noContent().build();
    }

    // TODO wx 批量添加节点

    // TODO wx 删除节点

    // TODO wx 移除节点角色
}
