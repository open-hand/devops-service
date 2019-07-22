package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CustomMergeRequestVO;
import io.choerodon.devops.api.vo.DevopsBranchVO;
import io.choerodon.devops.api.vo.IssueDTO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.api.vo.iam.entity.DevopsMergeRequestE;
import io.choerodon.devops.infra.dto.ApplicationDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.ApplicationMapper;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creator: chenwei
 * Date: 18-7-5
 * Time: 下午3:48
 * Description:
 */
@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private DevopsBranchService devopsBranchService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private DevopsMergeRequestService devopsMergeRequestService;
    @Autowired
    private IamService iamService;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private ApplicationMapper applicationMapper;


    @Override
    public IssueDTO countCommitAndMergeRequest(Long issueId) {
        List<DevopsBranchVO> devopsBranchVOS = getBranchsByIssueId(issueId);
        List<DevopsGitlabCommitDTO> devopsGitlabCommitVOS = new ArrayList<>();
        List<CustomMergeRequestVO> customMergeRequestVOS = new ArrayList<>();
        devopsBranchVOS.forEach(devopsBranchDTO -> {
            devopsGitlabCommitVOS.addAll(devopsBranchDTO.getCommits());
            customMergeRequestVOS.addAll(devopsBranchDTO.getMergeRequests());
        });
        Optional<DevopsGitlabCommitDTO> devopsGitlabCommitE = devopsGitlabCommitVOS.stream().max(
                Comparator.comparing(DevopsGitlabCommitDTO::getCommitDate));
        Optional<CustomMergeRequestVO> customMergeRequestDTO = customMergeRequestVOS.stream().max(
                Comparator.comparing(CustomMergeRequestVO::getUpdatedAt)
        );
        IssueDTO issueDTO = new IssueDTO();
        if (!customMergeRequestVOS.isEmpty()) {
            for (CustomMergeRequestVO mergeRequestTmp : customMergeRequestVOS) {
                if ("opened".equals(mergeRequestTmp.getState())) {
                    issueDTO.setMergeRequestStatus("opened");
                    break;
                }
            }
        }
        issueDTO.setBranchCount(devopsBranchVOS.size());
        issueDTO.setTotalCommit(devopsGitlabCommitVOS.size());
        issueDTO.setTotalMergeRequest(customMergeRequestVOS.size());
        devopsGitlabCommitE.ifPresent(gitlabCommitDTO ->
                issueDTO.setCommitUpdateTime(gitlabCommitDTO.getCommitDate()));
        customMergeRequestDTO.ifPresent(customMergeRequestVO1 ->
                issueDTO.setMergeRequestUpdateTime(customMergeRequestVO1.getUpdatedAt()));
        return issueDTO;
    }

    @Override
    public List<DevopsBranchVO> getBranchsByIssueId(Long issueId) {
        List<DevopsBranchDTO> devopsBranchDTOS = devopsBranchService.baseListDevopsBranchesByIssueId(issueId);
        List<DevopsBranchVO> devopsBranchVOS = new ArrayList<>();
        devopsBranchDTOS.forEach(devopsBranchDO -> {
            Integer gitLabProjectId = getGitLabId(devopsBranchDO.getAppId());

            List<DevopsGitlabCommitDTO> devopsGitlabCommitES = devopsGitlabCommitService.baseListByAppIdAndBranch(devopsBranchDO.getAppId(), devopsBranchDO.getBranchName(), devopsBranchDO.getCheckoutDate());

            devopsGitlabCommitES = devopsGitlabCommitES.stream().filter(devopsGitlabCommitE ->
                    !devopsGitlabCommitE.getCommitSha().equals(devopsBranchDO.getCheckoutCommit()))
                    .collect(Collectors.toList());
            DevopsBranchVO devopsBranchVO = ConvertHelper.convert(devopsBranchDO, DevopsBranchVO.class);
            devopsBranchVO.setCommits(devopsGitlabCommitES);
            ApplicationDTO applicationDTO = applicationService.baseQuery(devopsBranchDO.getAppId());
            devopsBranchVO.setAppName(applicationDTO.getName());
            List<DevopsMergeRequestDTO> mergeRequests = devopsMergeRequestService.baseListBySourceBranch(
                    devopsBranchDO.getBranchName(), (long) gitLabProjectId);
            devopsBranchVO.setMergeRequests(addAuthorNameAndAssigneeName(
                    mergeRequests, devopsBranchDO.getAppId()));
            devopsBranchVOS.add(devopsBranchVO);
        });
        return devopsBranchVOS;
    }

    private List<CustomMergeRequestVO> addAuthorNameAndAssigneeName(List<DevopsMergeRequestDTO> devopsMergeRequestDTOS,
                                                                    Long applicationId) {
        List<CustomMergeRequestVO> mergeRequests = new ArrayList<>();
        List<Long> authorIds = new ArrayList<>();
        List<Long> assigneeIds = new ArrayList<>();
        devopsMergeRequestDTOS.stream().forEach(devopsMergeRequestE -> {
            authorIds.add(devopsMergeRequestE.getAuthorId());
            assigneeIds.add(devopsMergeRequestE.getAssigneeId());
        });
        List<IamUserDTO> authors = iamService.listUsersByIds(authorIds);
        List<IamUserDTO> assignees = iamService.listUsersByIds(assigneeIds);

        devopsMergeRequestDTOS.forEach(devopsMergeRequestE -> {
            Long authorId = devopsMergeRequestE.getAuthorId();
            Long assigneeId = devopsMergeRequestE.getAssigneeId();
            CustomMergeRequestVO customMergeRequestVO = new CustomMergeRequestVO();
            customMergeRequestVO.setApplicationId(applicationId);
            if (authorId != null) {
                authors.stream().filter(userE -> userE.getId().equals(authorId)).forEach(authorUser -> {
                    customMergeRequestVO.setAuthorName(authorUser.getLoginName() + authorUser.getRealName());
                    customMergeRequestVO.setImageUrl(authorUser.getImageUrl());
                });
            }
            if (assigneeId != null) {
                assignees.stream().filter(userE -> userE.getId().equals(assigneeId)).forEach(assigneeUser -> {
                    customMergeRequestVO.setAssigneeName(assigneeUser.getLoginName());
                });
            }
            BeanUtils.copyProperties(devopsMergeRequestE, customMergeRequestVO);
            mergeRequests.add(customMergeRequestVO);
        });
        return mergeRequests;
    }

    @Override
    public List<CustomMergeRequestVO> getMergeRequestsByIssueId(Long issueId) {
        List<DevopsBranchDTO> devopsBranchDTOS = devopsBranchService.baseListDevopsBranchesByIssueId(issueId);
        List<CustomMergeRequestVO> mergeRequests = new ArrayList<>();
        devopsBranchDTOS.forEach(devopsBranchDO -> {
            Integer gitLabProjectId = getGitLabId(devopsBranchDO.getAppId());
            List<DevopsMergeRequestDTO> devopsMergeRequestDTOS = devopsMergeRequestService.baseListBySourceBranch(
                    devopsBranchDO.getBranchName(), (long) gitLabProjectId);
            mergeRequests.addAll(addAuthorNameAndAssigneeName(
                    devopsMergeRequestDTOS, devopsBranchDO.getAppId()));
        });
        return mergeRequests;
    }

    private Integer getGitLabId(Long applicationId) {
        ApplicationDTO applicationDO = applicationMapper.selectByPrimaryKey(applicationId);
        if (applicationDO != null) {
            return applicationDO.getGitlabProjectId();
        } else {
            throw new CommonException("error.application.select");
        }
    }
}
