package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.dto.CustomMergeRequestDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;

public interface IssueService {

    Map<String, Object> countCommitAndMergeRequest(Long issueId);

    List<DevopsBranchDTO> getBranchsByIssueId(Long issueId);

    List<CustomMergeRequestDTO> getMergeRequestsByIssueId(Long issueId);

}
