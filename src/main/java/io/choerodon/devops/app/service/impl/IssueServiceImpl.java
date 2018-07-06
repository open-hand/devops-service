package io.choerodon.devops.app.service.impl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.CommitDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;
import io.choerodon.devops.app.service.IssueService;
import io.choerodon.devops.domain.application.repository.DevopsBranchRepository;
import io.choerodon.devops.domain.application.repository.DevopsGitRepository;
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Creator: chenwei
 * Date: 18-7-5
 * Time: 下午3:48
 * Description:
 */
@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private DevopsBranchRepository devopsBranchRepository;

    @Autowired
    private GitlabServiceClient gitlabServiceClient;

    @Autowired
    private DevopsGitRepository devopsGitRepository;


    @Override
    public Map<String, Object> countCommitAndMergeRequest(Long issueId) {
        List<CommitDTO> commitDTOS = getCommitsByIssueId(issueId);
        List<MergeRequestDTO> mergeRequestDTOS = getMergeRequestsByIssueId(issueId);
        CommitDTO commitDTO = null;
        if (commitDTOS != null && !commitDTOS.isEmpty()) {
            commitDTO = commitDTOS.parallelStream().max(new Comparator<CommitDTO>() {
                @Override
                public int compare(CommitDTO o1, CommitDTO o2) {
                    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                }
            }).get();
        }
        MergeRequestDTO mergeRequestDTO = null;
        if (mergeRequestDTOS != null && !mergeRequestDTOS.isEmpty()) {
            mergeRequestDTO = mergeRequestDTOS.parallelStream().max(new Comparator<MergeRequestDTO>() {
                @Override
                public int compare(MergeRequestDTO o1, MergeRequestDTO o2) {
                    return o1.getUpdatedAt().compareTo(o2.getUpdatedAt());
                }
            }).get();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalCommit", commitDTOS.size());
        result.put("commitUpdateTime", commitDTO == null ? null : commitDTO.getCreatedAt());
        result.put("totalMergeRequest", mergeRequestDTOS.size());
        result.put("mergeRequestUpdateTime", mergeRequestDTO == null ? null : mergeRequestDTO.getUpdatedAt());
        return result;
    }

    @Override
    public List<CommitDTO> getCommitsByIssueId(Long issueId) {
        List<DevopsBranchDO> devopsBranchDOs = devopsBranchRepository.getDevopsBranchsByIssueId(issueId);
        List<CommitDTO> commitDTOS = new ArrayList<>();
        devopsBranchDOs.stream().forEach(devopsBranchDO -> {
            Integer gitLabProjectId = devopsGitRepository.getGitLabId(devopsBranchDO.getAppId());
            String sha = devopsBranchDO.getCommit();
            Integer gitlabUserId = devopsGitRepository.getGitlabUserId();
            ResponseEntity<CommitDO> commitResult = gitlabServiceClient.getCommit(gitLabProjectId, sha, gitlabUserId);
            // commitResult.getBody().getParentIds();
            List<CommitDO> commitDOs = gitlabServiceClient
                    .getCommits(gitLabProjectId,
                            devopsBranchDO.getBranchName(),
                            commitResult.getBody().getCreatedAt()).getBody();
            commitDOs.add(commitResult.getBody());
            commitDTOS.addAll(ConvertHelper.convertList(commitDOs, CommitDTO.class));
        });
        return commitDTOS;
    }

    @Override
    public List<MergeRequestDTO> getMergeRequestsByIssueId(Long issueId) {
        List<DevopsBranchDO> devopsBranchDOs = devopsBranchRepository.getDevopsBranchsByIssueId(issueId);
        List<MergeRequestDO> mergeRequests = new ArrayList<>();
        StringBuffer appIds = new StringBuffer();
        devopsBranchDOs.stream().filter(devopsBranchDO -> {
            boolean flag = appIds.toString().contains(devopsBranchDO.getAppId().toString());
            appIds.append("_").append(devopsBranchDO.getAppId());
            return !flag;
        }).forEach(devopsBranchDO -> {
            Integer gitLabProjectId = devopsGitRepository.getGitLabId(devopsBranchDO.getAppId());
            mergeRequests.addAll(gitlabServiceClient.getMergeRequestList(gitLabProjectId).getBody());
        });
        return ConvertHelper.convertList(mergeRequests, MergeRequestDTO.class);
    }
}
