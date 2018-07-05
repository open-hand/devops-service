package io.choerodon.devops.app.service.impl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.CommitDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;
import io.choerodon.devops.app.service.IssueService;
import io.choerodon.devops.domain.application.repository.DevopsGitRepository;
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;

@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private DevopsBranchMapper devopsBranchMapper;


    @Autowired
    private GitlabServiceClient gitlabServiceClient;

    @Autowired
    private DevopsGitRepository devopsGitRepository;


    @Override
    public Map<String, Object> getCommitsAndMergeRequests(Long issueId) {
        List<CommitDTO> commitDTOS = getCommitsByIssueId(issueId);
        List<MergeRequestDTO> mergeRequestDTOS = getMergeRequestsByIssueId(issueId);
        CommitDTO commitDTO = commitDTOS.parallelStream().max(new Comparator<CommitDTO>() {
            @Override
            public int compare(CommitDTO o1, CommitDTO o2) {
                return 0;
            }
        }).get();
        MergeRequestDTO mergeRequestDTO = mergeRequestDTOS.parallelStream().max(new Comparator<MergeRequestDTO>() {
            @Override
            public int compare(MergeRequestDTO o1, MergeRequestDTO o2) {
                return o1.getUpdatedAt().compareTo(o2.getUpdatedAt());
            }
        }).get();
        Map<String, Object> result = new HashMap<>();
        result.put("totalCommit", commitDTOS.size());
        result.put("commitUpdateTime", null);
        result.put("totalMergeRequest", mergeRequestDTOS.size());
        result.put("mergeRequestUpdateTime", mergeRequestDTO.getUpdatedAt());
        return result;
    }

    @Override
    public List<CommitDTO> getCommitsByIssueId(Long issueId) {

        return null;
    }

    @Override
    public List<MergeRequestDTO> getMergeRequestsByIssueId(Long issueId) {
        DevopsBranchDO queryDevopsBranchDO = new DevopsBranchDO();
        queryDevopsBranchDO.setIssueId(issueId);
        List<DevopsBranchDO> devopsBranchDOs = devopsBranchMapper.select(queryDevopsBranchDO);
        List<MergeRequestDO> mergeRequests = new ArrayList<>();
        devopsBranchDOs.stream().forEach(devopsBranchDO -> {
            Integer gitLabProjectId = devopsGitRepository.getGitLabId(devopsBranchDO.getAppId());
            mergeRequests.addAll(gitlabServiceClient.getMergeRequestList(gitLabProjectId).getBody());
        });
        return ConvertHelper.convertList(mergeRequests, MergeRequestDTO.class);
    }
}
