package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.CommitDTO;
import io.choerodon.devops.api.dto.CustomMergeRequestDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.api.dto.IssueDTO;
import io.choerodon.devops.app.service.IssueService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO;

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
    private DevopsGitRepository devopsGitRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DevopsMergeRequestRepository devopsMergeRequestRepository;

    @Autowired
    private IamRepository iamRepository;


    @Override
    public IssueDTO countCommitAndMergeRequest(Long issueId) {
        List<DevopsBranchDTO> devopsBranchDTOs = getBranchsByIssueId(issueId);
        List<CommitDTO> commitDTOS = new ArrayList<>();
        List<CustomMergeRequestDTO> customMergeRequestDTOS = new ArrayList<>();
        devopsBranchDTOs.forEach(devopsBranchDTO -> {
            commitDTOS.addAll(devopsBranchDTO.getCommits());
            customMergeRequestDTOS.addAll(devopsBranchDTO.getMergeRequests());
        });
        Optional<CommitDTO> commitDTO = commitDTOS.parallelStream().max(
                Comparator.comparing(CommitDTO::getCreatedAt));
        Optional<CustomMergeRequestDTO> customMergeRequestDTO = customMergeRequestDTOS.parallelStream().max(
                Comparator.comparing(CustomMergeRequestDTO::getUpdatedAt)
        );
        IssueDTO issueDTO = new IssueDTO();
        issueDTO.setMergeRequestStatus(null);
        issueDTO.setMergeRequestUpdateTime(null);
        issueDTO.setCommitUpdateTime(null);
        if (!customMergeRequestDTOS.isEmpty()) {
            for (CustomMergeRequestDTO mergeRequestTmp : customMergeRequestDTOS) {
                if ("opened".equals(mergeRequestTmp.getState())) {
                    issueDTO.setMergeRequestStatus("opened");
                    break;
                }
            }
        }
        issueDTO.setBranchCount(devopsBranchDTOs.size());
        issueDTO.setTotalCommit(commitDTOS.size());
        issueDTO.setTotalMergeRequest(customMergeRequestDTOS.size());
        commitDTO.ifPresent(commitDTO1 ->
                issueDTO.setCommitUpdateTime(commitDTO1.getCreatedAt()));
        customMergeRequestDTO.ifPresent(customMergeRequestDTO1 ->
                issueDTO.setMergeRequestUpdateTime(customMergeRequestDTO1.getUpdatedAt()));
        return issueDTO;
    }

    @Override
    public List<DevopsBranchDTO> getBranchsByIssueId(Long issueId) {
        List<DevopsBranchDO> devopsBranchDOs = devopsBranchRepository.getDevopsBranchsByIssueId(issueId);
        List<DevopsBranchDTO> devopsBranchDTOS = new ArrayList<>();
        devopsBranchDOs.forEach(devopsBranchDO -> {
            Integer gitLabProjectId = devopsGitRepository.getGitLabId(devopsBranchDO.getAppId());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
            String sinceDate = simpleDateFormat.format(devopsBranchDO.getCheckoutDate());
            List<CommitDO> commitDOs = devopsGitRepository
                    .getCommits(gitLabProjectId, devopsBranchDO.getBranchName(), sinceDate);
            commitDOs = commitDOs.parallelStream().filter(commitDO ->
                    !commitDO.getId().equals(devopsBranchDO.getCheckoutCommit()))
                    .collect(Collectors.toList());
            DevopsBranchDTO devopsBranchDTO = ConvertHelper.convert(devopsBranchDO, DevopsBranchDTO.class);
            devopsBranchDTO.setCommits(ConvertHelper.convertList(commitDOs, CommitDTO.class));
            ApplicationE app = applicationRepository.query(devopsBranchDO.getAppId());
            devopsBranchDTO.setAppName(app.getName());
            List<DevopsMergeRequestE> mergeRequests = devopsMergeRequestRepository.getBySourceBranch(
                    devopsBranchDO.getBranchName(), (long) gitLabProjectId);
            devopsBranchDTO.setMergeRequests(addAuthorNameAndAssigneeName(
                    mergeRequests, devopsBranchDO.getAppId()));
            devopsBranchDTOS.add(devopsBranchDTO);
        });
        return devopsBranchDTOS;
    }


    private List<CustomMergeRequestDTO> addAuthorNameAndAssigneeName(List<DevopsMergeRequestE> devopsMergeRequestES,
                                                                     Long applicationId) {
        List<CustomMergeRequestDTO> mergeRequests = new ArrayList<>();
        devopsMergeRequestES.forEach(devopsMergeRequestE -> {
            Long authorId = devopsMergeRequestE.getAuthorId();
            Long assigneeId = devopsMergeRequestE.getAssigneeId();
            CustomMergeRequestDTO customMergeRequestDTO = new CustomMergeRequestDTO();
            customMergeRequestDTO.setApplicationId(applicationId);
            if (authorId != null) {
                UserE authorUser = iamRepository.queryUserByUserId(
                        devopsGitRepository.getUserIdByGitlabUserId(authorId));
                customMergeRequestDTO.setAuthorName(authorUser.getLoginName() + authorUser.getRealName());
                customMergeRequestDTO.setImageUrl(authorUser.getImageUrl());
            }
            if (assigneeId != null) {
                UserE assigneeUser = iamRepository.queryUserByUserId(
                        devopsGitRepository.getUserIdByGitlabUserId(assigneeId));
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
            mergeRequests.addAll(addAuthorNameAndAssigneeName(
                    devopsMergeRequestES, devopsBranchDO.getAppId()));
        });
        return mergeRequests;
    }
}
