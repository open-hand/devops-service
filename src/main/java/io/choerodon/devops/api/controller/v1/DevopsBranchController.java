package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.app.service.DevopsBranchService;

@RestController
@RequestMapping("/v1/projects/{project_id}/branch")
public class DevopsBranchController {

    @Autowired
    private DevopsBranchService devopsBranchService;

    @GetMapping("/issue/check_rel_exist")
    public ResponseEntity<Boolean> checkIssueBranchRelExist(@ApiParam("项目id")
                                                            @PathVariable("project_id") Long projectId,
                                                            @ApiParam("issueId")
                                                            @RequestParam("issue_id") Long issueId) {
        return Results.success(devopsBranchService.checkIssueBranchRelExist(projectId, issueId));
    }

    @ApiOperation("复制工作项与分支关联关系")
    @PostMapping("/issue/copy_rel")
    public ResponseEntity<Void> copyIssueBranchRel(@ApiParam("项目id")
                                                   @PathVariable("project_id") Long projectId,
                                                   @ApiParam("oldIssueId")
                                                   @RequestParam("old_issue_id") Long oldIssueId,
                                                   @ApiParam("newIssueId")
                                                   @RequestParam("new_issue_id") Long newIssueId) {
        devopsBranchService.copyIssueBranchRel(projectId, oldIssueId, newIssueId);
        return Results.success();
    }
}
