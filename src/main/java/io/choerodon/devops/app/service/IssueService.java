package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.dto.CommitDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;

public interface IssueService {

    Map<String, Object> getCommitsAndMergeRequests(Long issueId);

    List<CommitDTO> getCommitsByIssueId(Long issueId);

    List<MergeRequestDTO> getMergeRequestsByIssueId(Long issueId);

}
