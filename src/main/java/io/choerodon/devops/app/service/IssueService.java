package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CustomMergeRequestDTO;
import io.choerodon.devops.api.vo.DevopsBranchDTO;
import io.choerodon.devops.api.vo.IssueDTO;

public interface IssueService {

    IssueDTO countCommitAndMergeRequest(Long issueId);

    List<DevopsBranchDTO> getBranchsByIssueId(Long issueId);

    List<CustomMergeRequestDTO> getMergeRequestsByIssueId(Long issueId);

}
