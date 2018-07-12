package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.CommitDTO;
import io.choerodon.devops.api.dto.CustomMergeRequestDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.app.service.IssueService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
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
    private GitlabUserRepository gitlabUserRepository;

    @Autowired
    private DevopsMergeRequestRepository devopsMergeRequestRepository;

    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;

    @Autowired
    private IamRepository iamRepository;


    @Override
    public Map<String, Object> countCommitAndMergeRequest(Long issueId) {
        List<DevopsBranchDTO> devopsBranchDTOs = getBranchsByIssueId(issueId);
        List<CommitDTO> commitDTOS = new ArrayList<>();
        List<CustomMergeRequestDTO> customMergeRequestDTOS = new ArrayList<>();
        devopsBranchDTOs.forEach(devopsBranchDTO -> {
            commitDTOS.addAll(devopsBranchDTO.getCommits());
            customMergeRequestDTOS.addAll(devopsBranchDTO.getMergeRequests());
        });
        Optional<CommitDTO> commitDTO = commitDTOS.parallelStream().max(
                (CommitDTO o1, CommitDTO o2) ->
                        o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        Optional<CustomMergeRequestDTO> customMergeRequestDTO = customMergeRequestDTOS.parallelStream().max(
                (CustomMergeRequestDTO o1, CustomMergeRequestDTO o2) ->
                        o1.getUpdatedAt().compareTo(o2.getUpdatedAt())
        );
        Map<String, Object> result = new HashMap<>();
        result.put("mergeRequestStatus", null);
        result.put("mergeRequestUpdateTime", null);
        result.put("commitUpdateTime", null);
        if (!customMergeRequestDTOS.isEmpty()) {
            for (CustomMergeRequestDTO mergeRequestTmp : customMergeRequestDTOS) {
                if ("opened".equals(mergeRequestTmp.getState())) {
                    result.put("mergeRequestStatus", "opened");
                    break;
                }
            }
        }
        result.put("totalCommit", commitDTOS.size());
        result.put("totalMergeRequest", customMergeRequestDTOS.size());
        commitDTO.ifPresent(commitDTO1 ->
                result.put("commitUpdateTime", commitDTO1.getCreatedAt()));
        customMergeRequestDTO.ifPresent(customMergeRequestDTO1 ->
                result.put("mergeRequestUpdateTime", customMergeRequestDTO1.getUpdatedAt()));
        return result;
    }

    @Override
    public List<DevopsBranchDTO> getBranchsByIssueId(Long issueId) {
        List<DevopsBranchDO> devopsBranchDOs = devopsBranchRepository.getDevopsBranchsByIssueId(issueId);
        List<DevopsBranchDTO> devopsBranchDTOS = new ArrayList<>();
        devopsBranchDOs.forEach(devopsBranchDO -> {
            Integer gitLabProjectId = devopsGitRepository.getGitLabId(devopsBranchDO.getAppId());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
            String sinceDate = simpleDateFormat.format(devopsBranchDO.getCheckoutDate());
            List<CommitDO> commitDOs = gitlabServiceClient
                    .getCommits(gitLabProjectId, devopsBranchDO.getBranchName(), sinceDate).getBody();
            commitDOs = commitDOs.parallelStream().filter(commitDO ->
                    !commitDO.getId().equals(devopsBranchDO.getCheckoutCommit()))
                    .collect(Collectors.toList());
            DevopsBranchDTO devopsBranchDTO = ConvertHelper.convert(devopsBranchDO, DevopsBranchDTO.class);
            devopsBranchDTO.setCommits(ConvertHelper.convertList(commitDOs, CommitDTO.class));
            ApplicationE app = applicationRepository.query(devopsBranchDO.getAppId());
            devopsBranchDTO.setAppName(app.getName());
            List<DevopsMergeRequestE> mergeRequests = devopsMergeRequestRepository.getBySourceBranch(
                    devopsBranchDO.getBranchName(), (long) gitLabProjectId);
            Long projectId = applicationRepository.query(devopsBranchDO.getAppId()).getProjectE().getId();
            devopsBranchDTO.setMergeRequests(addAuthorNameAndAssigneeName(projectId,
                    mergeRequests, devopsBranchDO.getAppId()));
            devopsBranchDTOS.add(devopsBranchDTO);
        });
        return devopsBranchDTOS;
    }


    private List<CustomMergeRequestDTO> addAuthorNameAndAssigneeName(Long projectId,
                                                                     List<DevopsMergeRequestE> devopsMergeRequestES,
                                                                     Long applicationId) {
        List<CustomMergeRequestDTO> mergeRequests = new ArrayList<>();
        devopsMergeRequestES.forEach(devopsMergeRequestE -> {
            Long authorId = devopsMergeRequestE.getAuthorId();
            Long assigneeId = devopsMergeRequestE.getAssigneeId();
            CustomMergeRequestDTO customMergeRequestDTO = new CustomMergeRequestDTO();
            customMergeRequestDTO.setApplicationId(applicationId);
            if (authorId != null) {
                UserE authorUser = iamRepository.queryByProjectAndId(projectId, devopsGitRepository
                        .getUserIdByGitlabUserId(authorId));
                customMergeRequestDTO.setAuthorName(authorUser.getLoginName());
                customMergeRequestDTO.setImageUrl(authorUser.getImageUrl());
            }
            if (assigneeId != null) {
                UserE assigneeUser = iamRepository.queryByProjectAndId(projectId, devopsGitRepository
                        .getUserIdByGitlabUserId(assigneeId));
                customMergeRequestDTO.setAssigneeName(assigneeUser.getLoginName());
            }
            BeanUtils.copyProperties(devopsMergeRequestE, customMergeRequestDTO);
            mergeRequests.add(customMergeRequestDTO);
        });
        return mergeRequests;
    }

    @Override
    public List<CustomMergeRequestDTO> getMergeRequestsByIssueId(Long issueId) {
        List<DevopsBranchDO> devopsBranchDOs = devopsBranchRepository.getDevopsBranchsByIssueId(issueId);
        List<CustomMergeRequestDTO> mergeRequests = new ArrayList<>();
        devopsBranchDOs.forEach(devopsBranchDO -> {
            Integer gitLabProjectId = devopsGitRepository.getGitLabId(devopsBranchDO.getAppId());
            List<DevopsMergeRequestE> devopsMergeRequestES = devopsMergeRequestRepository.getBySourceBranch(
                    devopsBranchDO.getBranchName(), (long) gitLabProjectId);
            Long projectId = applicationRepository.query(devopsBranchDO.getAppId()).getProjectE().getId();
            mergeRequests.addAll(addAuthorNameAndAssigneeName(projectId,
                    devopsMergeRequestES, devopsBranchDO.getAppId()));
        });
        return mergeRequests;
    }
}
