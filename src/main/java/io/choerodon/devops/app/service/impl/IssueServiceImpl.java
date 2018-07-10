package io.choerodon.devops.app.service.impl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.CommitDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;
import io.choerodon.devops.app.service.IssueService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.gitlab.MergeRequestE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO;
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

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DevopsMergeRequestRepository devopsMergeRequestRepository;

    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;


    @Override
    public Map<String, Object> countCommitAndMergeRequest(Long issueId) {
        List<CommitDTO> commitDTOS = getAllCommit(issueId);
        List<MergeRequestDTO> mergeRequestDTOS = getMergeRequestsByIssueId(issueId);
        Optional<CommitDTO> commitDTO = commitDTOS.parallelStream().max(
                (CommitDTO o1, CommitDTO o2) ->
                        o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        Optional<MergeRequestDTO> mergeRequestDTO = mergeRequestDTOS.parallelStream().max(
                (MergeRequestDTO o1, MergeRequestDTO o2) ->
                        o1.getUpdatedAt().compareTo(o2.getUpdatedAt())
        );
        Map<String, Object> result = new HashMap<>();
        result.put("totalCommit", commitDTOS.size());
        result.put("totalMergeRequest", mergeRequestDTOS.isEmpty() ? 0 : mergeRequestDTOS.size());
        if (commitDTO.isPresent()) {
            result.put("commitUpdateTime", commitDTO.get().getCreatedAt());
        } else {
            result.put("commitUpdateTime", null);
        }
        if (mergeRequestDTO.isPresent()) {
            result.put("mergeRequestUpdateTime", mergeRequestDTO.get().getUpdatedAt());
        } else {
            result.put("mergeRequestUpdateTime", null);
        }
        return result;
    }


    private List<CommitDTO> getAllCommit(Long issueId) {
        List<DevopsBranchDTO> devopsBranchDTOS = getBranchsByIssueId(issueId);
        List<CommitDTO> commitDTOS = new ArrayList<>();
        if (devopsBranchDTOS != null && !devopsBranchDTOS.isEmpty()) {
            devopsBranchDTOS.forEach(devopsBranchDTO ->
                    commitDTOS.addAll(devopsBranchDTO.getCommits()));
        }
        return commitDTOS;
    }

    @Override
    public List<DevopsBranchDTO> getBranchsByIssueId(Long issueId) {
        List<DevopsBranchDO> devopsBranchDOs = devopsBranchRepository.getDevopsBranchsByIssueId(issueId);
        List<DevopsBranchDTO> devopsBranchDTOS = new ArrayList<>();
        devopsBranchDOs.forEach(devopsBranchDO -> {
            Integer gitLabProjectId = devopsGitRepository.getGitLabId(devopsBranchDO.getAppId());
            String sha = devopsBranchDO.getCheckoutCommit();
            Integer gitlabUserId = devopsGitRepository.getGitlabUserId();
            ResponseEntity<CommitDO> commitResult = gitlabServiceClient.getCommit(gitLabProjectId, sha, gitlabUserId);
            List<CommitDO> commitDOs = gitlabServiceClient
                    .getCommits(gitLabProjectId,
                            devopsBranchDO.getBranchName(),
                            commitResult.getBody().getCreatedAt()).getBody();
            commitDOs.add(commitResult.getBody());
            DevopsBranchDTO devopsBranchDTO = ConvertHelper.convert(devopsBranchDO, DevopsBranchDTO.class);
            devopsBranchDTO.setCommits(ConvertHelper.convertList(commitDOs, CommitDTO.class));
            ApplicationE app = applicationRepository.query(devopsBranchDO.getAppId());
            devopsBranchDTO.setAppName(app.getName());
            devopsBranchDTOS.add(devopsBranchDTO);
        });
        return devopsBranchDTOS;
    }

    @Override
    public List<MergeRequestDTO> getMergeRequestsByIssueId(Long issueId) {
        List<DevopsBranchDO> devopsBranchDOs = devopsBranchRepository.getDevopsBranchsByIssueId(issueId);
        List<MergeRequestDTO> mergeRequests = new ArrayList<>();
        devopsBranchDOs.forEach(devopsBranchDO -> {
            Integer gitLabProjectId = devopsGitRepository.getGitLabId(devopsBranchDO.getAppId());
            List<MergeRequestE> mergeRequestEs = devopsMergeRequestRepository.getBySourceBranch(
                    devopsBranchDO.getBranchName(), gitLabProjectId * 1L);
            List<MergeRequestDTO> mergeRequestDTOS = ConvertHelper.convertList(mergeRequestEs, MergeRequestDTO.class);
            mergeRequests.addAll(mergeRequestDTOS);
        });
        return mergeRequests;
    }
}
