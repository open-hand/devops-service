package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.CustomMergeRequestDTO;
import io.choerodon.devops.api.vo.DevopsBranchDTO;
import io.choerodon.devops.api.vo.IssueDTO;
import io.choerodon.devops.app.service.IssueService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据issueId获取issue关联的commit列表")
    @GetMapping("/commit/list")
    public ResponseEntity<List<DevopsBranchDTO>> getCommitsByIssueId(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "issueID")
            @PathVariable(value = "issue_id") Long issueId) {

        return Optional.ofNullable(issueService.getBranchsByIssueId(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.commit.get"));
    }

    /**
     * 根据issueId获取issue关联的mergerequest
     *
     * @param issueId issueID
     * @return 返回mergerequest列表
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据issueId获取issue关联的mergerequest列表")
    @GetMapping("/merge_request/list")
    public ResponseEntity<List<CustomMergeRequestDTO>> getMergeRequestsByIssueId(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "issueID")
            @PathVariable(value = "issue_id") Long issueId) {

        return Optional.ofNullable(issueService.getMergeRequestsByIssueId(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.mergerequest.get"));
    }

    /**
     * 根据issueId获取issue关联的mergerequest和commit数量
     *
     * @param issueId issueID
     * @return 返回mergerequest和commit数量
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据issueId获取issue关联的mergerequest和commit数量")
    @GetMapping("/commit_and_merge_request/count")
    public ResponseEntity<IssueDTO> countCommitAndMergeRequest(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "issueID")
            @PathVariable(value = "issue_id") Long issueId) {
        return Optional.ofNullable(issueService.countCommitAndMergeRequest(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.commit.mergerequest.count"));
    }
}