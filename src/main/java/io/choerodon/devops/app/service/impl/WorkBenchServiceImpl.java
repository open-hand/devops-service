package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_USER_GET;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.ApprovalVO;
import io.choerodon.devops.api.vo.CommitFormRecordVO;
import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.api.vo.dashboard.ProjectDashboardCfgVO;
import io.choerodon.devops.api.vo.dashboard.ProjectMeasureVO;
import io.choerodon.devops.api.vo.dashboard.SearchVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.ApprovalTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class WorkBenchServiceImpl implements WorkBenchService {
    private static final String MERGE_REQUEST_CONTENT_FORMAT = "%s (%s)在应用服务“%s”中提交了合并请求";
    private static final String PIPELINE_CONTENT_FORMAT = "流水线 “%s” 目前暂停于【%s】阶段，需要您进行审核";

    private static final String CD_PIPELINE_CONTENT_FORMAT = "部署流程 “%s” 目前暂停于【%s】阶段，需要您进行审核";
    private static final String MERGE_REQUEST_URL = "%s/%s-%s/%s/merge_requests/%d";
    @Autowired
    DevopsGitService devopsGitService;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private DevopsBranchMapper devopsBranchMapper;
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private CiAuditRecordMapper ciAuditRecordMapper;
    @Autowired
    private PipelineAuditRecordMapper pipelineAuditRecordMapper;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private ProjectDashboardCfgService projectDashboardCfgService;
    @Autowired
    private SonarAnalyseRecordService sonarAnalyseRecordService;
    @Autowired
    private VulnScanRecordService vulnScanRecordService;
    @Autowired
    private PolarisScanningService polarisScanningService;


    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public List<LatestAppServiceVO> listLatestAppService(Long organizationId, Long projectId) {
        Object[] orgAndProjectInfo = getOrgAndProjectInfo(organizationId, projectId);
        Tenant tenant = (Tenant) orgAndProjectInfo[0];
        List<ProjectDTO> projectDTOList = (List<ProjectDTO>) orgAndProjectInfo[1];
        if (CollectionUtils.isEmpty(projectDTOList)) {
            return new ArrayList<>();
        } else {
            return listLatestUserAppServiceDTO(tenant, projectDTOList);
        }
    }

    @Override
    public List<ApprovalVO> listApproval(Long organizationId, Long projectId) {
        Object[] orgAndProjectInfo = getOrgAndProjectInfo(organizationId, projectId);
        Tenant tenant = (Tenant) orgAndProjectInfo[0];
        List<ProjectDTO> projectDTOList = (List<ProjectDTO>) orgAndProjectInfo[1];
        if (CollectionUtils.isEmpty(projectDTOList)) {
            return new ArrayList<>();
        } else {
            return listApprovalVOByProject(tenant, projectDTOList);
        }
    }


    @Override
    public Page<CommitFormRecordVO> listLatestCommits(Long organizationId, Long projectId, PageRequest pageRequest) {
        Object[] orgAndProjectInfo = getOrgAndProjectInfo(organizationId, projectId);
        List<ProjectDTO> projectDTOList = (List<ProjectDTO>) orgAndProjectInfo[1];
        if (CollectionUtils.isEmpty(projectDTOList)) {
            return new Page<>();
        } else {
            return listLatestCommits(projectDTOList, pageRequest);
        }
    }

    @Override
    public Page<ProjectMeasureVO> listProjectMeasure(Long organizationId, PageRequest pageRequest, SearchVO searchVO) {
        Page<ProjectMeasureVO> projectPage = baseServiceClientOperator.pagingManagedProjects(organizationId, pageRequest, searchVO);
        if (CollectionUtils.isEmpty(projectPage.getContent())) {
            return new Page<>();
        }
        List<ProjectMeasureVO> projects = projectPage.getContent();
        List<Long> pids = projects.stream().map(ProjectMeasureVO::getId).collect(Collectors.toList());

        // 查询项目的代码得分
        Map<Long, Double> codeScoreMap = sonarAnalyseRecordService.listProjectScores(pids);
        Map<Long, Double> vulnScoreMap = vulnScanRecordService.listProjectScores(pids);
        Map<Long, Double> k8sScoreMap = polarisScanningService.listProjectScores(pids);

        ProjectDashboardCfgVO projectDashboardCfgVO = projectDashboardCfgService.queryByOrganizationId(organizationId);

        for (ProjectMeasureVO projectMeasureVO : projects) {
            long pid = projectMeasureVO.getId();
            Double codeScore = codeScoreMap.get(pid);
            Double vulnScore = vulnScoreMap.get(pid);
            Double k8sScore = k8sScoreMap.get(pid);
            projectMeasureVO.setCodeScore(String.format("%.2f", codeScore));
            projectMeasureVO.setVulnScore(String.format("%.2f", vulnScore));
            projectMeasureVO.setK8sScore(String.format("%.2f", k8sScore));
            double totalWeight = 0;
            if (codeScore != null) {
                totalWeight += projectDashboardCfgVO.getCodeWeight();
            }
            if (vulnScore != null) {
                totalWeight += projectDashboardCfgVO.getVulnWeight();
            }
            if (k8sScore != null) {
                totalWeight += projectDashboardCfgVO.getK8sWeight();
            }
            double score = 0;
            if (codeScore != null) {
                score += codeScore * projectDashboardCfgVO.getCodeWeight() / totalWeight;
            }
            if (vulnScore != null) {
                score += vulnScore * projectDashboardCfgVO.getVulnWeight() / totalWeight;
            }
            if (k8sScore != null) {
                score += k8sScore * projectDashboardCfgVO.getK8sWeight() / totalWeight;
            }
            projectMeasureVO.setScore(String.format("%.2f", score));
        }

        return projectPage;
    }


    /**
     * 数组第一个元素组织信息，第二个元素项目信息
     *
     * @param organizationId
     * @param projectId
     * @return
     */
    private Object[] getOrgAndProjectInfo(Long organizationId, Long projectId) {
        Tenant tenant = baseServiceClientOperator.queryOrganizationById(organizationId);
        List<ProjectDTO> projectDTOList;
        Long userId = DetailsHelper.getUserDetails().getUserId();
        if (projectId == null) {
            projectDTOList = baseServiceClientOperator.listOwnedProjects(tenant.getTenantId(), userId);
        } else {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
            CommonExAssertUtil.assertNotNull(projectDTO, "devops.project.query");
            projectDTOList = Collections.singletonList(projectDTO);
        }
        return new Object[]{tenant, projectDTOList};
    }

    private List<ApprovalVO> listApprovalVOByProject(Tenant tenant, List<ProjectDTO> projectDTOList) {
        List<ApprovalVO> approvalVOList = new ArrayList<>();
        List<Long> projectIds = projectDTOList.stream().map(ProjectDTO::getId).collect(Collectors.toList());
        Map<Long, ProjectDTO> projectMap = projectDTOList.stream().collect(Collectors.toMap(ProjectDTO::getId, v -> v));
        // 1.查询合并请求
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.listByActiveAndProjects(projectIds);
        approvalVOList.addAll(listMergeRequestApproval(tenant, projectMap, appServiceDTOList));

        // 2.查出流水线请求
        approvalVOList.addAll(listPipelineApproval(projectMap, projectIds));

        // 3. 查询出持续部署待审核列表
        approvalVOList.addAll(listCdPipelineApproval(projectMap, projectIds));
        return approvalVOList;
    }

    private List<ApprovalVO> listMergeRequestApproval(Tenant tenant, Map<Long, ProjectDTO> projectMap, List<AppServiceDTO> appServiceDTOList) {
        List<ApprovalVO> approvalVOList = new ArrayList<>();
        // 不统计外部仓库
        appServiceDTOList = appServiceDTOList.stream()
                .filter(v -> v.getExternalConfigId() == null)
                .collect(Collectors.toList());
        List<Integer> gitlabProjectIds = appServiceDTOList.stream()
                .map(AppServiceDTO::getGitlabProjectId)
                .collect(Collectors.toList());
        Map<Integer, AppServiceDTO> gitlabProjectAndAppMap = appServiceDTOList.stream().collect(Collectors.toMap(AppServiceDTO::getGitlabProjectId, v -> v));
        // 查出该用户待审批的合并请求
        List<DevopsMergeRequestDTO> mergeRequestDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(gitlabProjectIds)) {
            mergeRequestDTOList = devopsMergeRequestMapper.listToBeAuditedByThisUserUnderProjectIds(gitlabProjectIds, DetailsHelper.getUserDetails() == null ? 0L : DetailsHelper.getUserDetails().getUserId());
            if (CollectionUtils.isEmpty(mergeRequestDTOList)) {
                return approvalVOList;
            }
        }
        // 根据authorId查出合并请求发起者信息
        Set<Long> authorIds = mergeRequestDTOList.stream().map(DevopsMergeRequestDTO::getAuthorId).collect(Collectors.toSet());
        List<UserAttrVO> userAttrDTOList = userAttrService.listUsersByGitlabUserIds(authorIds);
        List<Long> iamUserIds = userAttrDTOList.stream().map(UserAttrVO::getIamUserId).collect(Collectors.toList());
        Map<Long, IamUserDTO> iamUserDTOMap = baseServiceClientOperator.queryUsersByUserIds(iamUserIds).stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
        Map<Long, UserAttrVO> userAttrVO = userAttrDTOList.stream().collect(Collectors.toMap(UserAttrVO::getGitlabUserId, v -> v));
        mergeRequestDTOList.forEach(devopsMergeRequestDTO -> {
            IamUserDTO iamUserDTO = iamUserDTOMap.get(userAttrVO.get(devopsMergeRequestDTO.getAuthorId()).getIamUserId());
            ApprovalVO approvalVO = new ApprovalVO()
                    .setImageUrl(iamUserDTO.getImageUrl())
                    .setType(ApprovalTypeEnum.MERGE_REQUEST.getType())
                    .setMergeRequestUrl(String.format(MERGE_REQUEST_URL, gitlabUrl, tenant.getTenantNum(), projectMap.get(devopsMergeRequestDTO.getProjectId()).getDevopsComponentCode(), gitlabProjectAndAppMap.get(devopsMergeRequestDTO.getGitlabProjectId().intValue()).getCode(), devopsMergeRequestDTO.getGitlabMergeRequestId()))
                    .setProjectId(devopsMergeRequestDTO.getProjectId())
                    .setProjectName(projectMap.get(devopsMergeRequestDTO.getProjectId()).getName())
                    .setGitlabProjectId(devopsMergeRequestDTO.getGitlabProjectId().intValue())
                    .setContent(String.format(MERGE_REQUEST_CONTENT_FORMAT, iamUserDTO.getRealName(), iamUserDTO.getLoginName(), gitlabProjectAndAppMap.get(devopsMergeRequestDTO.getGitlabProjectId().intValue()).getName()));
            approvalVOList.add(approvalVO);
        });

        return approvalVOList;
    }

    private List<ApprovalVO> listPipelineApproval(Map<Long, ProjectDTO> projectNameMap, List<Long> projectIds) {
        Long userId = DetailsHelper.getUserDetails().getUserId() == null ? 0 : DetailsHelper.getUserDetails().getUserId();
        CommonExAssertUtil.assertNotNull(userId, DEVOPS_USER_GET);

        // 查处该用户待审批的流水线阶段(新流水线)
        List<ApprovalVO> approvalVOList = ciAuditRecordMapper.listApprovalInfoByProjectIdsAndUserId(userId, projectIds);
        if (!CollectionUtils.isEmpty(approvalVOList)) {
            approvalVOList.forEach(approvalVO -> {
                approvalVO.setType(ApprovalTypeEnum.CI_PIPELINE.getType());
                approvalVO.setProjectName(projectNameMap.get(approvalVO.getProjectId()).getName());
                approvalVO.setContent(String.format(PIPELINE_CONTENT_FORMAT, approvalVO.getPipelineName(), approvalVO.getStageName()));
            });
        }
        return approvalVOList;
    }

    private List<ApprovalVO> listCdPipelineApproval(Map<Long, ProjectDTO> projectNameMap, List<Long> projectIds) {
        Long userId = DetailsHelper.getUserDetails().getUserId() == null ? 0 : DetailsHelper.getUserDetails().getUserId();
        CommonExAssertUtil.assertNotNull(userId, DEVOPS_USER_GET);

        // 查处该用户待审批的流水线阶段(新流水线)
        List<ApprovalVO> approvalVOList = pipelineAuditRecordMapper.listApprovalInfoByProjectIdsAndUserId(userId, projectIds);
        if (!CollectionUtils.isEmpty(approvalVOList)) {
            approvalVOList.forEach(approvalVO -> {
                approvalVO.setType(ApprovalTypeEnum.CD_PIPELINE.getType());
                approvalVO.setProjectName(projectNameMap.get(approvalVO.getProjectId()).getName());
                approvalVO.setContent(String.format(CD_PIPELINE_CONTENT_FORMAT, approvalVO.getPipelineName(), approvalVO.getStageName()));
            });
        }
        return approvalVOList;
    }

    private List<LatestAppServiceVO> listLatestUserAppServiceDTO(Tenant tenant, List<ProjectDTO> projectDTOList) {
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        List<Long> projectIds = projectDTOList.stream().map(ProjectDTO::getId).collect(Collectors.toList());
        Map<Long, ProjectDTO> projectDTOMap = projectDTOList.stream().collect(Collectors.toMap(ProjectDTO::getId, v -> v));
        Long userId = DetailsHelper.getUserDetails().getUserId() == null ? 0 : DetailsHelper.getUserDetails().getUserId();
        List<LatestAppServiceVO> latestAppServiceVOList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -7);
        Date time = calendar.getTime();
        latestAppServiceVOList.addAll(appServiceMapper.listLatestUseAppServiceIdAndDate(projectIds, userId, time));
        latestAppServiceVOList.addAll(devopsBranchMapper.listLatestUseAppServiceIdAndDate(projectIds, userId, time));
        latestAppServiceVOList.addAll(devopsGitlabCommitMapper.listLatestUseAppServiceIdAndDate(projectIds, userId, time));
        latestAppServiceVOList.addAll(devopsMergeRequestMapper.listLatestUseAppServiceIdAndDate(projectIds, userId, time));

        if (CollectionUtils.isEmpty(latestAppServiceVOList)) {
            return latestAppServiceVOList;
        }

        // 去掉重复的appService,只保留最近使用的
        List<LatestAppServiceVO> latestTenAppServiceList = latestAppServiceVOList.stream().sorted(Comparator.comparing(LatestAppServiceVO::getLastUpdateDate).reversed())
                .filter(distinctByKey(LatestAppServiceVO::getId)).limit(10)
                .collect(Collectors.toList());

        Set<Long> appServiceIds = latestTenAppServiceList.stream().map(LatestAppServiceVO::getId).collect(Collectors.toSet());
        Map<Long, AppServiceDTO> appServiceDTOMap = appServiceMapper.listAppServiceByIds(appServiceIds, null, null).stream().collect(Collectors.toMap(AppServiceDTO::getId, v -> v));

        latestTenAppServiceList.forEach(latestAppServiceVO -> {
            AppServiceDTO appServiceDTO = appServiceDTOMap.get(latestAppServiceVO.getId());
            ProjectDTO projectDTO = projectDTOMap.get(appServiceDTO.getProjectId());
            latestAppServiceVO.setProjectName(projectDTO.getName())
                    .setProjectId(appServiceDTO.getProjectId())
                    .setCode(appServiceDTO.getCode())
                    .setRepoUrl(gitlabUrl + urlSlash + tenant.getTenantNum() + "-" + projectDTO.getDevopsComponentCode() + "/"
                            + appServiceDTO.getCode() + ".git")
                    .setName(appServiceDTO.getName());
        });
        return latestTenAppServiceList;
    }

    private Page<CommitFormRecordVO> listLatestCommits(List<ProjectDTO> projectDTOList, PageRequest pageRequest) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -7);
        Date time = calendar.getTime();
        return devopsGitlabCommitService.listUserRecentCommits(projectDTOList, pageRequest, time);
    }
}
