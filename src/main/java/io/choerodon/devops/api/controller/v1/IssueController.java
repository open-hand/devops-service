package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CustomMergeRequestVO;
import io.choerodon.devops.api.vo.DevopsBranchVO;
import io.choerodon.devops.api.vo.IssueVO;
import io.choerodon.devops.app.service.IssueService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/project/{project_id}/issue/{issue_id}")
public class IssueController {

    @Autowired
    private IssueService issueService;


    /**
     * 根据issueId获取issue关联的commit
     *
     * @param issueId issueID
     * @return commit列表
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据issueId获取issue关联的commit列表")
    @GetMapping("/commit/list")
    public ResponseEntity<List<DevopsBranchVO>> getCommitsByIssueId(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "issueID")
            @PathVariable(value = "issue_id") Long issueId) {

        return Optional.ofNullable(issueService.getBranchsByIssueId(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.commit.get"));
    }

    /**
     * 根据issueId获取issue关联的mergeRequest
     *
     * @param issueId issueID
     * @return 返回mergeRequest列表
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据issueId获取issue关联的mergeRequest列表")
    @GetMapping("/merge_request/list")
    public ResponseEntity<List<CustomMergeRequestVO>> getMergeRequestsByIssueId(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "issueID")
            @PathVariable(value = "issue_id") Long issueId) {

        return Optional.ofNullable(issueService.getMergeRequestsByIssueId(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.mergerequest.get"));
    }

    /**
     * 根据issueId获取issue关联的mergeRequest和commit数量
     *
     * @param issueId issueID
     * @return 返回mergeRequest和commit数量
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据issueId获取issue关联的mergerequest和commit数量")
    @GetMapping("/commit_and_merge_request/count")
    public ResponseEntity<IssueVO> countCommitAndMergeRequest(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "issueID")
            @PathVariable(value = "issue_id") Long issueId) {
        return Optional.ofNullable(issueService.countCommitAndMergeRequest(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.commit.mergerequest.count"));
    }
}