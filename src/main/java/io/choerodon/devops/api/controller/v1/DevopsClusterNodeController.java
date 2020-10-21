package io.choerodon.devops.api.controller.v1;

import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestResultVO;
import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestVO;
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
    public ResponseEntity<DevopsClusterNodeConnectionTestResultVO> testConnection(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @RequestBody @Valid DevopsClusterNodeConnectionTestVO devopsClusterNodeConnectionTestVO) {
        return Results.success(devopsClusterNodeService.testConnection(projectId, devopsClusterNodeConnectionTestVO));
    }

//    @ApiOperation("校验是否能够删除节点")
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @PostMapping("/{node_id}/check_enable_delete")
//    public ResponseEntity<DevopsClusterNodeConnectionTestResultVO> checkEnableDelete(
//            @ApiParam(value = "项目id", required = true)
//            @PathVariable("project_id") Long projectId,
//            @PathVariable @Valid DevopsClusterNodeConnectionTestVO devopsClusterNodeConnectionTestVO) {
//        return Results.success(devopsClusterNodeService.checkEnableDelete(projectId, devopsClusterNodeConnectionTestVO));
//    }

    // TODO wx 批量添加节点

    // TODO wx 删除节点

    // TODO wx 移除节点角色
}
