package io.choerodon.devops.app.service.impl;

import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.ApprovalVO;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.app.service.WorkDesktopService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.ApprovalTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;
import io.choerodon.devops.infra.mapper.PipelineStageRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkDesktopServiceImpl implements WorkDesktopService {
    private static final String MERGE_REQUEST_CONTENT_FORMAT = "%s (%s)在应用服务“%s”中提交了合并请求";
    private static final String PIPELINE_CONTENT_FORMAT = "流水线 “%s” 目前暂停于【%s】阶段，需要您进行审核";
    private static final String ORGANIZATION_NAME_AND_PROJECT_NAME = "%s-%s";

    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper;

    @Autowired
    private PipelineStageRecordMapper pipelineStageRecordMapper;

    @Autowired
    private UserAttrService userAttrService;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    DevopsGitService devopsGitService;

    @Autowired
    private AppServiceMapper appServiceMapper;


    @Override
    public List<ApprovalVO> listApproval(Long organizationId, Long projectId) {
        Tenant tenant = baseServiceClientOperator.queryOrganizationById(organizationId);

        if (projectId != null) {
            return listApprovalVOByProject(tenant, projectId);
        } else {
            List<ApprovalVO> approvalVOList = new ArrayList<>();
            List<ProjectDTO> projectList = baseServiceClientOperator.pageProjectByOrgId(tenant.getTenantId(), 0, -1, null, null, null, null).getContent();
            projectList.forEach(projectDTO -> {
                approvalVOList.addAll(listApprovalVOByProject(tenant, projectDTO.getId()));
            });
            return approvalVOList;
        }
    }

    private List<ApprovalVO> listApprovalVOByProject(Tenant tenant, Long projectId) {
        List<ApprovalVO> approvalVOList = new ArrayList<>();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        String organizationAndProjectName = String.format(ORGANIZATION_NAME_AND_PROJECT_NAME, tenant.getTenantName(), projectDTO.getName());
        // 1.查询合并请求
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.listByActive(projectId);
        approvalVOList.addAll(listMergeRequestApproval(organizationAndProjectName, appServiceDTOList));

        // 2.查出流水线请求
        approvalVOList.addAll(listPipelineApproval(organizationAndProjectName, projectId));
        return approvalVOList;
    }

    private List<ApprovalVO> listMergeRequestApproval(String organizationAndProjectName, List<AppServiceDTO> appServiceDTOList) {
        List<ApprovalVO> approvalVOList = new ArrayList<>();
        List<Integer> gitlabProjectIds = appServiceDTOList.stream().map(AppServiceDTO::getGitlabProjectId).collect(Collectors.toList());
        Map<Integer, String> gitlabProjectAndAppNameMap = appServiceDTOList.stream().collect(Collectors.toMap(AppServiceDTO::getGitlabProjectId, AppServiceDTO::getName));
        // 查出该用户待审批的合并请求
        List<DevopsMergeRequestDTO> mergeRequestDTOList = new ArrayList<>();
        if (gitlabProjectIds.size() != 0) {
            mergeRequestDTOList = devopsMergeRequestMapper.listToBeAuditedByThisUserUnderProjectIds(gitlabProjectIds, DetailsHelper.getUserDetails() == null ? 0L : DetailsHelper.getUserDetails().getUserId());
        }
        // 根据authorId查出合并请求发起者信息
        Set<Long> authorIds = mergeRequestDTOList.stream().map(DevopsMergeRequestDTO::getAuthorId).collect(Collectors.toSet());
        List<UserAttrVO> userAttrDTOList = userAttrService.listUsersByGitlabUserIds(authorIds);
        List<Long> iamUserIds = userAttrDTOList.stream().map(UserAttrVO::getIamUserId).collect(Collectors.toList());
        Map<Long, List<IamUserDTO>> iamUserDTOMap = baseServiceClientOperator.queryUsersByUserIds(iamUserIds).stream().collect(Collectors.groupingBy(IamUserDTO::getId));
        Map<Long, List<UserAttrVO>> userAttrVO = userAttrDTOList.stream().collect(Collectors.groupingBy(UserAttrVO::getGitlabUserId));
        mergeRequestDTOList.forEach(devopsMergeRequestDTO -> {
            IamUserDTO iamUserDTO = iamUserDTOMap.get(userAttrVO.get(devopsMergeRequestDTO.getAuthorId()).get(0).getIamUserId()).get(0);
            ApprovalVO approvalVO = new ApprovalVO()
                    .setImageUrl(iamUserDTO.getImageUrl())
                    .setType(ApprovalTypeEnum.MERGE_REQUEST.getType())
                    .setOrganizationNameAndProjectName(organizationAndProjectName)
                    .setGitlabProjectId(devopsMergeRequestDTO.getGitlabProjectId().intValue())
                    .setContent(String.format(MERGE_REQUEST_CONTENT_FORMAT, iamUserDTO.getRealName(), iamUserDTO.getId(), gitlabProjectAndAppNameMap.get(devopsMergeRequestDTO.getGitlabProjectId().intValue())));
            approvalVOList.add(approvalVO);
        });

        return approvalVOList;
    }

    private List<ApprovalVO> listPipelineApproval(String organizationAndProjectName, Long projectId) {
        List<ApprovalVO> approvalVOList = new ArrayList<>();

        Long userId = DetailsHelper.getUserDetails().getUserId() == null ? 0 : DetailsHelper.getUserDetails().getUserId();
        // 查出该用户待审批的流水线阶段
        List<PipelineStageRecordDTO> pipelineStageRecordDTOList = pipelineStageRecordMapper.listToBeAuditedByProjectIds(Collections.singletonList(projectId), userId);
        List<PipelineStageRecordDTO> pipelineStageRecordDTOAuditByThisUserList = pipelineStageRecordDTOList.stream()
                .filter(pipelineStageRecordDTO -> pipelineStageRecordDTO.getAuditUser() != null && pipelineStageRecordDTO.getAuditUser().contains(String.valueOf(userId)))
                .collect(Collectors.toList());
        pipelineStageRecordDTOAuditByThisUserList.forEach(pipelineStageRecordDTO -> {
            ApprovalVO approvalVO = new ApprovalVO()
                    .setType(ApprovalTypeEnum.PIPE_LINE.getType())
                    .setOrganizationNameAndProjectName(organizationAndProjectName)
                    .setContent(String.format(PIPELINE_CONTENT_FORMAT, pipelineStageRecordDTO.getPipelineName(), pipelineStageRecordDTO.getStageName()))
                    .setPipeRecordId(pipelineStageRecordDTO.getId());
            approvalVOList.add(approvalVO);
        });
        return approvalVOList;
    }
}
