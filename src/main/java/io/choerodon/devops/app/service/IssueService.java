package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;

public interface IssueService {

    Map<String, Object> countCommitAndMergeRequest(Long issueId);

    List<DevopsBranchDTO> getBranchsByIssueId(Long issueId);

    List<MergeRequestDTO> getMergeRequestsByIssueId(Long issueId);

}
