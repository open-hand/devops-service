package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import io.choerodon.devops.api.dto.CommitDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;
import io.choerodon.devops.app.service.IssueService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/issue/{issueId}")
public class IssueController {

    @Autowired
    private IssueService issueService;


    /**
     *根据issueId获取issue关联的commit
     * @param issueId issueID
     * @return commit列表
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "根据issueId获取issue关联的commit列表")
    @GetMapping("/commit/list")
    public ResponseEntity<List<CommitDTO>> getCommitsByIssueId(
            @ApiParam(value = "issueID")
            @PathVariable(value = "issueId") Long issueId) {

        return Optional.ofNullable(issueService.getCommitsByIssueId(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.commit.get"));
    }

    /**
     * 根据issueId获取issue关联的mergerequest
     * @param issueId issueID
     * @return 返回mergerequest列表
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "根据issueId获取issue关联的mergerequest列表")
    @GetMapping("/merge_request/list")
    public ResponseEntity<List<MergeRequestDTO>> getMergeRequestsByIssueId(
            @ApiParam(value = "issueID")
            @PathVariable(value = "issueId") Long issueId) {

        return Optional.ofNullable(issueService.getMergeRequestsByIssueId(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.mergerequest.get"));
    }

    /**
     * 根据issueId获取issue关联的mergerequest和commit数量
     * @param issueId issueID
     * @return 返回mergerequest和commit数量
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "根据issueId获取issue关联的mergerequest和commit数量")
    @GetMapping("/Commit_and_mergeRequest/count")
    public ResponseEntity<Map<String, Object>> countCommitAndMergeRequest(
            @ApiParam(value = "issueID")
            @PathVariable(value = "issueId") Long issueId) {
        return Optional.ofNullable(issueService.countCommitAndMergeRequest(issueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.issue.commit.mergerequest.count"));
    }

}
