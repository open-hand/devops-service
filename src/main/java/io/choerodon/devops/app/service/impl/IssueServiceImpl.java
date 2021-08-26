package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.util.CiCdPipelineUtils.handleId;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CustomMergeRequestVO;
import io.choerodon.devops.api.vo.DevopsBranchVO;
import io.choerodon.devops.api.vo.IssueVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

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
    private AppServiceService applicationService;
    @Autowired
    private DevopsMergeRequestService devopsMergeRequestService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsIssueRelService devopsIssueRelService;


    @Override
    public IssueVO countCommitAndMergeRequest(Long issueId) {
        List<DevopsBranchVO> devopsBranchVOS = getBranchesByIssueId(issueId);
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
        IssueVO issueVO = new IssueVO();
        if (!customMergeRequestVOS.isEmpty()) {
            for (CustomMergeRequestVO mergeRequestTmp : customMergeRequestVOS) {
                if ("opened".equals(mergeRequestTmp.getState())) {
                    issueVO.setMergeRequestStatus("opened");
                    break;
                }
            }
        }
        issueVO.setBranchCount(devopsBranchVOS.size());
        issueVO.setTotalCommit(devopsGitlabCommitVOS.size());
        issueVO.setTotalMergeRequest(customMergeRequestVOS.size());
        devopsGitlabCommitE.ifPresent(gitlabCommitDTO ->
                issueVO.setCommitUpdateTime(gitlabCommitDTO.getCommitDate()));
        customMergeRequestDTO.ifPresent(customMergeRequestVO1 ->
                issueVO.setMergeRequestUpdateTime(customMergeRequestVO1.getUpdatedAt()));
        return issueVO;
    }

    @Override
    public List<DevopsBranchVO> getBranchesByIssueId(Long issueId) {

        // 这一步操作只能够查出已存在的分支
        List<DevopsBranchDTO> devopsBranchDTOS = devopsBranchService.baseListDevopsBranchesByIssueId(issueId);
        Map<Long, List<String>> appServiceIdDevopsBranchNameMap = devopsBranchDTOS.stream().collect(Collectors.groupingBy(DevopsBranchDTO::getAppServiceId, Collectors.mapping(DevopsBranchDTO::getBranchName, Collectors.toList())));

        // 这一步操作是根据commit查出所有分支信息
        List<DevopsBranchDTO> devopsCommitRelatedBranchDTOS = devopsGitlabCommitService.baseListDevopsBranchesByIssueId(issueId);

        Set<Long> commitRelatedBranchIds = devopsCommitRelatedBranchDTOS.stream().map(DevopsBranchDTO::getId).collect(Collectors.toSet());

        // 已经被删除的branchId
        List<Long> deletedBranchIds = devopsBranchService.listDeletedBranchIds(commitRelatedBranchIds);

        // 仍与issue存在关联关系的branchId
        List<Long> relatedBranchIds = devopsIssueRelService.listRelatedBranchIds(commitRelatedBranchIds);

        // 这一步操作将已删除的分支信息也添加到devopsBranchDTOS中
        devopsCommitRelatedBranchDTOS.forEach(d -> {
            // 通过commit查出来的分支，要满足分支已被删除但是分支与问题关联关系仍存在
            if (d.getId() != null && deletedBranchIds.contains(d.getId()) && relatedBranchIds.contains(d.getId())) {
                List<String> branchNames = appServiceIdDevopsBranchNameMap.get(d.getAppServiceId());
                if (CollectionUtils.isEmpty(branchNames) || !branchNames.contains(d.getBranchName())) {
                    devopsBranchDTOS.add(d);
                    if (branchNames == null) {
                        branchNames = new ArrayList<>();
                        appServiceIdDevopsBranchNameMap.put(d.getAppServiceId(), branchNames);
                    }
                    branchNames.add(d.getBranchName());
                }
            }
        });

        List<DevopsBranchVO> devopsBranchVOS = new ArrayList<>();

        Set<Long> projectIds = new HashSet<>();

        devopsBranchDTOS.forEach(devopsBranchDO -> {
            Integer gitLabProjectId = getGitLabId(devopsBranchDO.getAppServiceId());

            List<DevopsGitlabCommitDTO> devopsGitlabCommitES = devopsGitlabCommitService.baseListByAppIdAndBranch(devopsBranchDO.getAppServiceId(), devopsBranchDO.getBranchName(), devopsBranchDO.getCheckoutDate());

            devopsGitlabCommitES = devopsGitlabCommitES.stream().filter(devopsGitlabCommitE ->
                            !devopsGitlabCommitE.getCommitSha().equals(devopsBranchDO.getCheckoutCommit()))
                    .collect(Collectors.toList());
            DevopsBranchVO devopsBranchVO = ConvertUtils.convertObject(devopsBranchDO, DevopsBranchVO.class);
            devopsBranchVO.setCommits(devopsGitlabCommitES);
            AppServiceDTO applicationDTO = applicationService.baseQuery(devopsBranchDO.getAppServiceId());
            projectIds.add(applicationDTO.getProjectId());
            devopsBranchVO.setProjectId(applicationDTO.getProjectId());
            devopsBranchVO.setAppServiceName(applicationDTO.getName());
            List<DevopsMergeRequestDTO> mergeRequests = devopsMergeRequestService.baseListBySourceBranch(
                    devopsBranchDO.getBranchName(), (long) gitLabProjectId);
            devopsBranchVO.setMergeRequests(addAuthorNameAndAssigneeName(
                    mergeRequests, devopsBranchDO.getAppServiceId()));
            devopsBranchVOS.add(devopsBranchVO);
        });

        Map<Long, List<ProjectDTO>> projectMap = baseServiceClientOperator.queryProjectsByIds(projectIds).stream().collect(Collectors.groupingBy(ProjectDTO::getId));
        devopsBranchVOS.forEach(b -> {
            ProjectDTO projectDTO = projectMap.get(b.getProjectId()).get(0);
            b.setProjectName(projectDTO.getName());
            b.setProjectCode(projectDTO.getCode());
        });

        return devopsBranchVOS;
    }

    private List<CustomMergeRequestVO> addAuthorNameAndAssigneeName(List<DevopsMergeRequestDTO> devopsMergeRequestDTOS,
                                                                    Long applicationId) {
        List<CustomMergeRequestVO> mergeRequests = new ArrayList<>();
        List<Long> authorIds = new ArrayList<>();
        List<Long> assigneeIds = new ArrayList<>();
        devopsMergeRequestDTOS.stream().forEach(devopsMergeRequestE -> {
            authorIds.add(userAttrService
                    .queryUserIdByGitlabUserId(devopsMergeRequestE.getAuthorId()));
            assigneeIds.add(userAttrService
                    .queryUserIdByGitlabUserId(devopsMergeRequestE.getAssigneeId()));
        });
        List<IamUserDTO> authors = baseServiceClientOperator.listUsersByIds(authorIds);
        List<IamUserDTO> assignees = baseServiceClientOperator.listUsersByIds(assigneeIds);

        devopsMergeRequestDTOS.forEach(devopsMergeRequestE -> {
            Long authorId = userAttrService
                    .queryUserIdByGitlabUserId(devopsMergeRequestE.getAuthorId());
            Long assigneeId = userAttrService
                    .queryUserIdByGitlabUserId(devopsMergeRequestE.getAssigneeId());
            CustomMergeRequestVO customMergeRequestVO = new CustomMergeRequestVO();
            customMergeRequestVO.setApplicationId(applicationId);
            customMergeRequestVO.setViewId(handleId(devopsMergeRequestE.getId()));
            if (authorId != null) {
                authors.stream().filter(userE -> userE.getId().equals(authorId)).forEach(authorUser -> {
                    customMergeRequestVO.setAuthorName(authorUser.getLoginName() + authorUser.getRealName());
                    customMergeRequestVO.setImageUrl(authorUser.getImageUrl());
                });
            }
            if (assigneeId != null) {
                assignees.stream()
                        .filter(userE -> userE.getId().equals(assigneeId))
                        .forEach(assigneeUser -> {
                            customMergeRequestVO.setAssigneeName(assigneeUser.getLoginName() + assigneeUser.getRealName());
                            customMergeRequestVO.setAssigneeImageUrl(assigneeUser.getImageUrl());
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
            Integer gitLabProjectId = getGitLabId(devopsBranchDO.getAppServiceId());
            List<DevopsMergeRequestDTO> devopsMergeRequestDTOS = devopsMergeRequestService.baseListBySourceBranch(
                    devopsBranchDO.getBranchName(), (long) gitLabProjectId);
            mergeRequests.addAll(addAuthorNameAndAssigneeName(
                    devopsMergeRequestDTOS, devopsBranchDO.getAppServiceId()));
        });
        return mergeRequests;
    }

    private Integer getGitLabId(Long applicationId) {
        AppServiceDTO applicationDO = appServiceMapper.selectByPrimaryKey(applicationId);
        if (applicationDO != null) {
            return applicationDO.getGitlabProjectId();
        } else {
            throw new CommonException("error.application.select");
        }
    }
}
