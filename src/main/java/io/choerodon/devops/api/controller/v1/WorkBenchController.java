package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ApprovalVO;
import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.app.service.WorkBenchService;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 个人工作台
 *
 * @author lihao
 */
@RestController
@RequestMapping("/v1/organizations/{organization_id}/work_bench")
public class WorkBenchController {

    @Autowired
    WorkBenchService workBenchService;

    @Permission(permissionLogin = true, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/approval")
    @ApiOperation("查询个人待审核事件")
    public ResponseEntity<List<ApprovalVO>> listApproval(
            @ApiParam(value = "组织id", required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "项目id")
            @RequestParam(value = "project_id", required = false) Long projectId) {
        return Optional.ofNullable(workBenchService.listApproval(organizationId, projectId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.approval"));
    }

    @Permission(permissionLogin = true, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/latest_app_service")
    @ApiOperation("查看最近操作过的应用服务")
    public ResponseEntity<List<LatestAppServiceVO>> listLatestAppService(
            @ApiParam(value = "组织id", required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "项目id")
            @RequestParam(value = "project_id", required = false) Long projectId) {
        return Optional.ofNullable(workBenchService.listLatestAppService(organizationId, projectId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.latest.app.service"));
    }
}
