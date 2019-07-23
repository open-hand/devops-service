package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CustomMergeRequestVO;
import io.choerodon.devops.api.vo.DevopsBranchVO;
import io.choerodon.devops.api.vo.IssueVO;

public interface IssueService {

    IssueVO countCommitAndMergeRequest(Long issueId);

    List<DevopsBranchVO> getBranchsByIssueId(Long issueId);

    List<CustomMergeRequestVO> getMergeRequestsByIssueId(Long issueId);

}
