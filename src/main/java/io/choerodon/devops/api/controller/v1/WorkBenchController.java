package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ApprovalVO;
import io.choerodon.devops.api.vo.CommitFormRecordVO;
import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.api.vo.dashboard.ProjectMeasureVO;
import io.choerodon.devops.app.service.WorkBenchService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;

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
        return ResponseEntity.ok(workBenchService.listApproval(organizationId, projectId));
    }

    @Permission(permissionLogin = true, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/latest_app_service")
    @ApiOperation("查看最近操作过的应用服务")
    public ResponseEntity<List<LatestAppServiceVO>> listLatestAppService(
            @ApiParam(value = "组织id", required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "项目id")
            @RequestParam(value = "project_id", required = false) Long projectId) {
        return ResponseEntity.ok(workBenchService.listLatestAppService(organizationId, projectId));
    }

    @Permission(permissionLogin = true, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/latest_commit")
    @ApiOperation("查询最近代码提交提交记录")
    public ResponseEntity<Page<CommitFormRecordVO>> listLatestCommit(
            @ApiParam(value = "组织id", required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiParam(value = "项目id")
            @RequestParam(value = "project_id", required = false) Long projectId,
            @ApiIgnore PageRequest pageRequest) {
        return ResponseEntity.ok(workBenchService.listLatestCommits(organizationId, projectId, pageRequest));
    }

    @Permission(permissionLogin = true, level = ResourceLevel.ORGANIZATION)
    @GetMapping("/project_measure")
    @ApiOperation("项目质量评分")
    public ResponseEntity<Page<ProjectMeasureVO>> listProjectMeasure(
            @ApiParam(value = "组织id", required = true)
            @PathVariable("organization_id") Long organizationId,
            @ApiIgnore PageRequest pageRequest) {
        return ResponseEntity.ok(workBenchService.listProjectMeasure(organizationId, pageRequest));
    }
}
