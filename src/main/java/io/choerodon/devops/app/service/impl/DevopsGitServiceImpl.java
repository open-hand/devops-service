package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.BranchCode.DEVOPS_BRANCH_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_GITLAB_USER_SYNC_FAILED;
import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_USER_NOT_IN_GITLAB_PROJECT;
import static io.choerodon.devops.infra.constant.KubernetesConstants.METADATA;
import static io.choerodon.devops.infra.constant.KubernetesConstants.NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSONObject;
import io.kubernetes.client.openapi.models.V1Endpoints;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.PageUtils;
import io.choerodon.devops.api.vo.MergeRequestVO;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.BranchSagaPayLoad;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.agile.IssueDTO;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.exception.GitlabAccessInvalidException;
import io.choerodon.devops.infra.feign.operator.AgileServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.AsgardServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:44
 * Description:
 */
@Service
public class DevopsGitServiceImpl implements DevopsGitService {
    private static final String REF_HEADS = "refs/heads/";
    private static final String GIT_SUFFIX = "/.git";
    private static final String PROJECT = "project";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    private final Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");

    private final Map<String, ConvertK8sObjectService> userEnvSupportedResourceConverters = new HashMap<>();
    private final Map<String, ConvertK8sObjectService> systemEnvSupportedResourceConverters = new HashMap<>();
    private final Map<Class, HandlerObjectFileRelationsService> objectFileRelationHandlers = new HashMap<>();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private AgileServiceClientOperator agileServiceClientOperator;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private DevopsEnvFileService devopsEnvFileService;
    @Autowired
    private DevopsEnvCommitService devopsEnvCommitService;
    @Autowired
    private DevopsEnvFileErrorService devopsEnvFileErrorService;
    @Autowired
    private DevopsBranchService devopsBranchService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private DevopsMergeRequestService devopsMergeRequestService;
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private DevopsCustomizeResourceService devopsCustomizeResourceService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;

    @Autowired
    private List<HandlerObjectFileRelationsService> handlerObjectFileRelationsServices;
    @Autowired
    private List<ConvertK8sObjectService> convertK8sObjectServices;
    @Autowired
    private AsgardServiceClientOperator asgardServiceClientOperator;
    @Autowired
    private DevopsIssueRelService devopsIssueRelService;
    @Autowired
    @Lazy
    private DevopsProjectService devopsProjectService;
    @Autowired
    private AppExternalConfigService appExternalConfigService;
    @Autowired
    private DevopsBranchMapper devopsBranchMapper;
    @Autowired
    private DevopsIngressService devopsIngressService;

    /**
     * 初始化转换类和处理关系的类
     */
    @PostConstruct
    private void initialize() {
        Map<String, ConvertK8sObjectService> allConverters = new HashMap<>();
        convertK8sObjectServices.forEach(converter -> allConverters.put(converter.getType().getType(), converter));

        initUserEnvSupportedResources(allConverters);
        initSystemEnvSupportedResources(allConverters);
        initHandlers();
    }

    /**
     * 初始化对于用户环境支持的资源类型的转换类
     *
     * @param allConverters 所有支持的转换类
     */
    private void initUserEnvSupportedResources(Map<String, ConvertK8sObjectService> allConverters) {
        for (UserEnvSupportedResourceType type : UserEnvSupportedResourceType.values()) {
            if (allConverters.get(type.getType()) != null) {
                userEnvSupportedResourceConverters.put(type.getType(), allConverters.get(type.getType()));
            }
        }
        // 默认的处理
        userEnvSupportedResourceConverters.put(ResourceType.MISSTYPE.getType(), allConverters.get(ResourceType.CUSTOM.getType()));
    }

    /**
     * 初始化对于系统环境支持的资源类型的转换类
     *
     * @param allConverters 所有支持的转换类
     */
    private void initSystemEnvSupportedResources(Map<String, ConvertK8sObjectService> allConverters) {
        for (SystemEnvSupportedResourceType type : SystemEnvSupportedResourceType.values()) {
            if (allConverters.get(type.getType()) != null) {
                systemEnvSupportedResourceConverters.put(type.getType(), allConverters.get(type.getType()));
            }
        }
        // 默认的处理
        systemEnvSupportedResourceConverters.put(ResourceType.MISSTYPE.getType(), allConverters.get(ResourceType.MISSTYPE.getType()));
    }

    /**
     * 初始化资源类对应的处理类
     */
    private void initHandlers() {
        handlerObjectFileRelationsServices.forEach(handler -> objectFileRelationHandlers.put(handler.getTarget(), handler));
    }


    private Integer getGitlabUserId() {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (userAttrDTO == null) {
            throw new CommonException(DEVOPS_GITLAB_USER_SYNC_FAILED);
        }
        return TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
    }

    @Override
    public String queryUrl(Long projectId, Long appServiceId) {
        return appServiceService.getGitlabUrl(projectId, appServiceId);
    }

    @Override
    public void createTag(Long projectId, Long appServiceId, String tag, String ref, String msg, String releaseNotes) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, AppServiceEvent.TAG_CREATE);
        appServiceService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO applicationDTO = permissionHelper.checkAppServiceBelongToProject(projectId, appServiceId);
        gitlabServiceClientOperator.createTag(applicationDTO.getGitlabProjectId(), tag, ref, msg, releaseNotes, getGitlabUserId());
    }

    @Override
    public void updateTag(Long projectId, Long appServiceId, String tag, String releaseNotes) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, AppServiceEvent.TAG_UPDATE);
        appServiceService.baseCheckApp(projectId, appServiceId);
        permissionHelper.checkAppServiceBelongToProject(projectId, appServiceId);
        AppServiceDTO applicationDTO = permissionHelper.checkAppServiceBelongToProject(projectId, appServiceId);
        Release release = gitlabServiceClientOperator.queryRelease(applicationDTO.getGitlabProjectId(), tag, getGitlabUserId());
        if (release == null || release.getTagName() == null) {
            ReleaseParams releaseParams = new ReleaseParams();
            releaseParams.setTagName(tag);
            releaseParams.setDescription(releaseNotes);
            gitlabServiceClientOperator.createRelease(applicationDTO.getGitlabProjectId(), tag, releaseNotes, getGitlabUserId());
        } else {
            gitlabServiceClientOperator.updateRelease(applicationDTO.getGitlabProjectId(), tag, releaseNotes, getGitlabUserId());
        }

    }

    @Override
    public void deleteTag(Long projectId, Long appServiceId, String tag) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, AppServiceEvent.TAG_DELETE);
        appServiceService.baseCheckApp(projectId, appServiceId);
        permissionHelper.checkAppServiceBelongToProject(projectId, appServiceId);
        AppServiceDTO applicationDTO = permissionHelper.checkAppServiceBelongToProject(projectId, appServiceId);
        gitlabServiceClientOperator.deleteTag(applicationDTO.getGitlabProjectId(), tag, getGitlabUserId());
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_BRANCH, description = "devops创建分支", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public void createBranch(Long projectId, Long appServiceId, DevopsBranchVO devopsBranchVO) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, AppServiceEvent.BRANCH_CREATE);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        DevopsBranchDTO devopsBranchDTO = ConvertUtils.convertObject(devopsBranchVO, DevopsBranchDTO.class);
        Long gitLabUser = TypeUtil.objToLong(getGitlabUserId());
        devopsBranchDTO.setUserId(gitLabUser);
        devopsBranchDTO.setAppServiceId(appServiceId);
        devopsBranchDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsBranchDTO = devopsBranchService.baseCreate(devopsBranchDTO);
        Long devopsBranchId = devopsBranchDTO.getId();
        if (devopsBranchVO.getIssueId() != null) {
            devopsIssueRelService.addRelation(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), devopsBranchId, devopsBranchId, projectId, appServiceDTO.getCode(), devopsBranchVO.getIssueId());
        }


        BranchSagaPayLoad branchSagaPayLoad = new BranchSagaPayLoad(TypeUtil.objToLong(appServiceDTO.getGitlabProjectId()), devopsBranchId, devopsBranchVO.getBranchName(), devopsBranchVO.getOriginBranch());
        producer.apply(StartSagaBuilder.newBuilder().withLevel(ResourceLevel.PROJECT).withSourceId(projectId).withRefType(PROJECT).withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_BRANCH), builder -> builder.withPayloadAndSerialize(branchSagaPayLoad).withRefId(String.valueOf(devopsBranchId)));
    }

    @Override
    public void createBranchBySaga(BranchSagaPayLoad branchSagaPayLoad) {
        try {
            BranchDTO branchDTO = gitlabServiceClientOperator.queryBranch(TypeUtil.objToInteger(branchSagaPayLoad.getGitlabProjectId()), branchSagaPayLoad.getBranchName());
            if (branchDTO.getName() == null) {
                //创建gitlab分支，并处理返回值
                branchDTO = gitlabServiceClientOperator.createBranch(TypeUtil.objToInteger(branchSagaPayLoad.getGitlabProjectId()), branchSagaPayLoad.getBranchName(), branchSagaPayLoad.getOriginBranch(), getGitlabUserId());
            }

            if (branchDTO.getCommit() == null) {
                throw new CommonException(DEVOPS_BRANCH_EXIST);
            }
            CommitDTO commitDTO = branchDTO.getCommit();
            Date checkoutDate = commitDTO.getCommittedDate();
            String checkoutSha = commitDTO.getId();
            DevopsBranchDTO devopsBranchDTOCreate = devopsBranchService.baseQuery(branchSagaPayLoad.getDevopsBranchId());
            devopsBranchDTOCreate.setStatus(CommandStatus.SUCCESS.getStatus());
            devopsBranchDTOCreate.setCheckoutDate(checkoutDate);
            devopsBranchDTOCreate.setCheckoutCommit(checkoutSha);

            devopsBranchDTOCreate.setLastCommitDate(checkoutDate);
            devopsBranchDTOCreate.setLastCommit(checkoutSha);
            devopsBranchDTOCreate.setLastCommitMsg(LogUtil.cutOutString(commitDTO.getMessage(), MiscConstants.DEVOPS_BRANCH_LAST_COMMIT_MESSAGE_MAX_LENGTH));
            UserAttrDTO userAttrDTO;
            if (commitDTO.getCommitterName().equals("root")) {
                userAttrDTO = userAttrService.baseQueryByGitlabUserName("admin");
                if (userAttrDTO == null) {
                    userAttrDTO = userAttrService.baseQueryByGitlabUserName("admin1");
                }
            } else {
                userAttrDTO = userAttrService.baseQueryByGitlabUserName(commitDTO.getCommitterName());
            }
            devopsBranchDTOCreate.setLastCommitUser(userAttrDTO == null ? null : userAttrDTO.getGitlabUserId());
            devopsBranchService.baseUpdateBranch(devopsBranchDTOCreate);
        } catch (Exception e) {
            DevopsBranchDTO devopsBranchDTOCreate = devopsBranchService.baseQuery(branchSagaPayLoad.getDevopsBranchId());
            devopsBranchDTOCreate.setStatus(CommandStatus.FAILED.getStatus());
            devopsBranchDTOCreate.setErrorMessage(e.getMessage());
            devopsBranchService.baseUpdateBranch(devopsBranchDTOCreate);
        }
    }

    @Override
    public Page<BranchVO> pageBranchByOptions(Long projectId, PageRequest pageable, Long appServiceId, String params, Long currentProjectId) {
        AppServiceDTO applicationDTO = appServiceService.baseQuery(appServiceId);

        if (applicationDTO == null) {
            return new Page<>();
        }
        // 外部应用服务直接从gitlab查询
        if (applicationDTO.getExternalConfigId() != null) {
            return listExternalBranch(pageable, params, applicationDTO);
        }

        try {
            checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, AppServiceEvent.BRANCH_LIST);
        } catch (GitlabAccessInvalidException e) {
            return new Page<>();
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        // 查询用户是否在该gitlab project下
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (userAttrDTO == null) {
            throw new CommonException(DEVOPS_GITLAB_USER_SYNC_FAILED);
        }
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);

        if (!permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId)) {
            MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(devopsProjectDTO.getDevopsAppGroupId().intValue(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO == null || memberDTO.getId() == null) {
                memberDTO = gitlabServiceClientOperator.getMember(Long.valueOf(applicationDTO.getGitlabProjectId()), userAttrDTO.getGitlabUserId());
            }
            if (memberDTO == null) {
                throw new CommonException(DEVOPS_USER_NOT_IN_GITLAB_PROJECT);
            }
        }

        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s", gitlabUrl, urlSlash, organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), applicationDTO.getCode());
        Page<DevopsBranchDTO> devopsBranchDTOPageInfo = devopsBranchService.basePageBranch(appServiceId, pageable, params, null);
        Page<BranchVO> devopsBranchVOPageInfo = ConvertUtils.convertPage(devopsBranchDTOPageInfo, BranchVO.class);

        if (CollectionUtils.isEmpty(devopsBranchVOPageInfo.getContent())) {
            return devopsBranchVOPageInfo;
        }
        Set<Long> branchCreaterGitlabUserIds = devopsBranchDTOPageInfo.getContent().stream().map(DevopsBranchDTO::getUserId).collect(Collectors.toSet());
        Set<Long> lastCommitGitlabUserIds = devopsBranchDTOPageInfo.getContent().stream().map(DevopsBranchDTO::getLastCommitUser).collect(Collectors.toSet());

        List<UserAttrVO> creater = userAttrService.listUsersByGitlabUserIds(branchCreaterGitlabUserIds);
        List<Long> createIamUserIds = creater.stream().map(UserAttrVO::getIamUserId).collect(Collectors.toList());
        Map<Long, Long> createrIamUserIdAndGitlabUserIdMap = creater.stream().filter(userAttrVO -> userAttrVO.getIamUserId() != null && userAttrVO.getGitlabUserId() != null).collect(Collectors.toMap(UserAttrVO::getGitlabUserId, UserAttrVO::getIamUserId));


        List<UserAttrVO> lastCommitUser = userAttrService.listUsersByGitlabUserIds(lastCommitGitlabUserIds);
        List<Long> lastCommitIamUserIds = lastCommitUser.stream().map(UserAttrVO::getIamUserId).collect(Collectors.toList());
        Map<Long, Long> lastCommitIamUserIdAndGitlabUserIdMap = lastCommitUser.stream().filter(userAttrVO -> userAttrVO.getIamUserId() != null && userAttrVO.getGitlabUserId() != null).collect(Collectors.toMap(UserAttrVO::getGitlabUserId, UserAttrVO::getIamUserId));

        Map<Long, List<Long>> mappedIssueIds = devopsIssueRelService.listMappedIssueIdsByObjectTypeAndObjectId(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), devopsBranchDTOPageInfo.getContent().stream().map(DevopsBranchDTO::getId).collect(Collectors.toSet()));

        List<Long> issuedIds = new ArrayList<>();
        mappedIssueIds.forEach((k, v) -> issuedIds.addAll(v));

        Map<Long, IamUserDTO> iamUserDTOMap = baseServiceClientOperator.listUsersByIds(createIamUserIds).stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
        Map<Long, IamUserDTO> lastCommitUserDTOMap = baseServiceClientOperator.listUsersByIds(lastCommitIamUserIds).stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
        Map<Long, IssueDTO> issues = null;
        // 读取敏捷问题列表可能会失败，但是不希望影响查询分支逻辑，所以捕获异常
        try {
            if (!CollectionUtils.isEmpty(issuedIds)) {
                // 敏捷需要提供接口 根据ids查询Issue  返回 内容要带projectId
                issues = agileServiceClientOperator.listIssueByIdsWithProjectId(issuedIds).stream().collect(Collectors.toMap(IssueDTO::getIssueId, v -> v));
            }
        } catch (Exception e) {
            LOGGER.error("query agile issue failed:{}", e.getMessage());
        }

        Map<Long, IssueDTO> finalIssues = issues;
        Map<Long, ProjectDTO> projectDTOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(issues)) {
            Set<Long> projectIds = issues.values().stream().map(IssueDTO::getProjectId).collect(Collectors.toSet());
            projectDTOMap = baseServiceClientOperator.queryProjectsByIds(projectIds).stream().collect(Collectors.toMap(ProjectDTO::getId, Function.identity()));
        }

        List<String> refIds = devopsBranchDTOPageInfo.getContent().stream().map(devopsBranchDTO -> String.valueOf(devopsBranchDTO.getId())).collect(Collectors.toList());
        Map<String, SagaInstanceDetails> stringSagaInstanceDetailsMap = SagaInstanceUtils.listToMap(asgardServiceClientOperator.queryByRefTypeAndRefIds(PROJECT, refIds, SagaTopicCodeConstants.DEVOPS_CREATE_BRANCH));
        Map<Long, ProjectDTO> finalProjectDTOMap = projectDTOMap;
        devopsBranchVOPageInfo.setContent(devopsBranchDTOPageInfo.getContent().stream().map(t -> {
            List<IssueDTO> issueDTOList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(finalIssues) && !CollectionUtils.isEmpty(mappedIssueIds.get(t.getId()))) {
                mappedIssueIds.get(t.getId()).forEach(i -> {
                    IssueDTO issueDTO = finalIssues.get(i);
                    if (issueDTO != null) {
                        ProjectDTO dto = finalProjectDTOMap.get(issueDTO.getProjectId());
                        if (!Objects.isNull(dto)) {
                            if (dto.getId().longValue() == projectId.longValue()) {
                                issueDTO.setProjectName("(本项目)" + dto.getName());
                                issueDTO.setProjectId(dto.getId());
                                issueDTO.setProjectCode(dto.getCode());
                            } else {
                                issueDTO.setProjectName(dto.getName());
                                issueDTO.setProjectId(dto.getId());
                                issueDTO.setProjectCode(dto.getCode());
                            }
                        }
                        issueDTOList.add(issueDTO);
                    }
                });
            }

            Long createUserId = createrIamUserIdAndGitlabUserIdMap.get(t.getUserId());
            IamUserDTO userDTO = createUserId == null ? null : iamUserDTOMap.get(createUserId);

            Long lastCommitUserId = lastCommitIamUserIdAndGitlabUserIdMap.get(t.getLastCommitUser());
            IamUserDTO commitUserDTO = lastCommitUserId == null ? null : lastCommitUserDTOMap.get(lastCommitUserId);
            String commitUrl = String.format("%s/commit/%s?view=parallel", path, t.getLastCommit());
            return getBranchVO(t, commitUrl, commitUserDTO, userDTO, issueDTOList, SagaInstanceUtils.fillInstanceId(stringSagaInstanceDetailsMap, String.valueOf(t.getId())));
        }).collect(Collectors.toList()));
        return devopsBranchVOPageInfo;
    }

    @Override
    public DevopsBranchVO queryBranch(Long projectId, Long applicationId, String branchName) {
        DevopsBranchDTO branchDTO = devopsBranchService.baseQueryByAppAndBranchName(applicationId, branchName);
        DevopsBranchVO devopsBranchVO = ConvertUtils.convertObject(branchDTO, DevopsBranchVO.class);
        if (devopsBranchVO.getIssueId() != null) {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
            IssueDTO issueDTO = agileServiceClientOperator.queryIssue(projectId, devopsBranchVO.getIssueId(), projectDTO.getOrganizationId());
            if (issueDTO == null || issueDTO.getIssueId() == null) {
                devopsBranchVO.setIssueId(null);
                branchDTO.setIssueId(null);
                devopsBranchService.baseUpdateBranch(branchDTO);
            }
        }
        return devopsBranchVO;
    }

    @Override
    public void updateBranchIssue(Long projectId, Long appServiceId, DevopsBranchUpdateVO devopsBranchUpdateVO, boolean onyInsert) {
        AppServiceDTO appServiceDTO = permissionHelper.checkAppServiceBelongToProject(projectId, devopsBranchUpdateVO.getAppServiceId());
        devopsBranchService.updateBranchIssue(projectId, appServiceDTO, ConvertUtils.convertObject(devopsBranchUpdateVO, DevopsBranchDTO.class), onyInsert);
    }

    @Override
    public void deleteBranch(Long projectId, Long appServiceId, String branchName) {
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        checkGitlabAccessLevelService.checkGitlabPermission(appServiceDTO.getProjectId(), appServiceId, AppServiceEvent.BRANCH_DELETE);
        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        List<BranchDTO> branchDTOS = gitlabServiceClientOperator.listBranch(appServiceDTO.getGitlabProjectId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        // 不能删除仓库下最后一个分支
        if (branchDTOS.size() <= 1) {
            throw new CommonException("devops.delete.the.only.branch");
        }
        Optional<BranchDTO> branchDTO = branchDTOS.stream().filter(e -> branchName.equals(e.getName())).findFirst();
        branchDTO.ifPresent(e -> {
            if (Boolean.TRUE.equals(e.getProtected())) {
                // 不能删除保护分支
                throw new CommonException("devops.delete.protected.branch");
            } else {
                gitlabServiceClientOperator.deleteBranch(appServiceDTO.getGitlabProjectId(), branchName, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        });
        devopsBranchService.baseDelete(appServiceId, branchName);
    }

    @Override
    public MergeRequestTotalVO listMergeRequest(Long projectId, Long appServiceId, String state, PageRequest pageable) {
        permissionHelper.checkAppServiceBelongToProject(projectId, appServiceId);
        try {
            checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, AppServiceEvent.MERGE_REQUEST_LIST);
        } catch (GitlabAccessInvalidException e) {
            return null;
        }
        appServiceService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        if (appServiceDTO.getGitlabProjectId() == null) {
            throw new CommonException("devops.gitlabProjectId.not.exists");
        }

        // 由于gitlab不会同步被删掉的合并请求，所以每一次查询前先删除已经不存在的合并请求
        syncMergeRequest(appServiceDTO.getGitlabProjectId());

        Page<DevopsMergeRequestDTO> devopsMergeRequestDTOPageInfo = devopsMergeRequestService.basePageByOptions(appServiceDTO.getGitlabProjectId(), state, pageable);

        List<MergeRequestVO> pageContent = new ArrayList<>();
        List<DevopsMergeRequestDTO> devopsMergeRequestDTOS = devopsMergeRequestDTOPageInfo.getContent();

        //设置每个合并请求下关联的commit
        if (devopsMergeRequestDTOS != null && !devopsMergeRequestDTOS.isEmpty()) {
            devopsMergeRequestDTOS.forEach(content -> {
                MergeRequestVO mergeRequestVO = devopsMergeRequestToMergeRequest(content);
                if (mergeRequestVO != null) {
                    pageContent.add(mergeRequestVO);
                }
            });
        }
        Page<MergeRequestVO> mergeRequestVOPageInfo = ConvertUtils.convertPage(devopsMergeRequestDTOPageInfo, MergeRequestVO.class);
        mergeRequestVOPageInfo.setContent(pageContent);

        //查询某个应用代码仓库各种状态合并请求的数量
        DevopsMergeRequestDTO devopsMergeRequestDTO = devopsMergeRequestService.baseCountMergeRequest(appServiceDTO.getGitlabProjectId());

        MergeRequestTotalVO mergeRequestTotalVO = new MergeRequestTotalVO();
        mergeRequestTotalVO.setMergeRequestVOPageInfo(mergeRequestVOPageInfo);
        mergeRequestTotalVO.setTotalCount(devopsMergeRequestDTO.getTotal());
        mergeRequestTotalVO.setCloseCount(devopsMergeRequestDTO.getClosed());
        mergeRequestTotalVO.setMergeCount(devopsMergeRequestDTO.getMerged());
        mergeRequestTotalVO.setOpenCount(devopsMergeRequestDTO.getOpened());
        mergeRequestTotalVO.setAuditCount(devopsMergeRequestDTO.getAuditCount());

        return mergeRequestTotalVO;
    }

    private void syncMergeRequest(Integer gitlabProjectId) {
        List<Long> ids = gitlabServiceClientOperator.listMergeRequestIds(gitlabProjectId);
        if (!CollectionUtils.isEmpty(ids)) {
            devopsMergeRequestMapper.deleteByGitlabProjectIdAndMergeRequestIdNotInIds(gitlabProjectId, ids);
        } else {
            devopsMergeRequestMapper.deleteByProjectId(gitlabProjectId);
        }
    }

    @Override
    public Page<TagVO> pageTagsByOptions(Long projectId, Long applicationId, String params, Integer page, Integer size, Boolean checkMember) {
        AppServiceDTO applicationDTO = appServiceService.baseQuery(applicationId);
        if (applicationDTO.getExternalConfigId() != null) {
            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(applicationDTO.getExternalConfigId());
            List<TagDTO> tagDTOS = gitlabServiceClientOperator.listExternalTags(applicationDTO.getGitlabProjectId(), appExternalConfigDTO);
            if (tagDTOS == null) {
                return new Page<>();
            }
            List<TagVO> tagVOS = ConvertUtils.convertList(tagDTOS, TagVO.class);
            if (params != null) {
                Map<String, Object> maps = TypeUtil.castMapParams(params);
                Map<String, Object> searchParam = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
                Object tagName = searchParam.get("tagName");
                tagVOS = tagVOS.stream().filter(tag -> tag.getName().contains(tagName.toString())).collect(Collectors.toList());
            }
            return PageUtils.createPageFromList(tagVOS, new PageRequest(page, size));
        }

        if (Boolean.TRUE.equals(checkMember)) {
            try {
                checkGitlabAccessLevelService.checkGitlabPermission(projectId, applicationId, AppServiceEvent.TAG_LIST);
            } catch (GitlabAccessInvalidException e) {
                return new Page<>();
            }
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s", gitlabUrl, urlSlash, organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), applicationDTO.getCode());
        return ConvertUtils.convertPage(gitlabServiceClientOperator.pageTag(projectDTO, applicationDTO.getGitlabProjectId(), path, page, params, size, getGitlabUserId(), checkMember), TagVO.class);
    }

    @Override
    public List<TagVO> listTags(Long projectId, Long applicationId) {
        try {
            checkGitlabAccessLevelService.checkGitlabPermission(projectId, applicationId, AppServiceEvent.TAG_LIST);
        } catch (GitlabAccessInvalidException e) {
            return new ArrayList<>();
        }
        AppServiceDTO applicationDTO = appServiceService.baseQuery(applicationId);
        return ConvertUtils.convertList(gitlabServiceClientOperator.listTags(applicationDTO.getGitlabProjectId(), getGitlabUserId()), TagVO.class);
    }

    @Override
    public Boolean checkTag(Long projectId, Long applicationId, String tagName) {
        AppServiceDTO applicationDTO = appServiceService.baseQuery(applicationId);
        return gitlabServiceClientOperator.listTags(applicationDTO.getGitlabProjectId(), getGitlabUserId()).stream().noneMatch(t -> tagName.equals(t.getName()));
    }

    @Override
    public void branchSync(PushWebHookVO pushWebHookVO, String token) {
        AppServiceDTO applicationDTO = appServiceService.baseQueryByToken(token);
        // 创建分支操作
        if (GitOpsConstants.NO_COMMIT_SHA.equals(pushWebHookVO.getBefore())) {
            createBranchSync(pushWebHookVO, applicationDTO.getId());
            devopsGitlabCommitService.create(pushWebHookVO, token, GitOpsConstants.CREATE);
        } else if (GitOpsConstants.NO_COMMIT_SHA.equals(pushWebHookVO.getAfter())) {
            // 删除分支操作
            deleteBranchSync(pushWebHookVO, applicationDTO.getId());
        } else {
            // 某一分支提交代码操作
            commitBranchSync(pushWebHookVO, applicationDTO.getId());
            devopsGitlabCommitService.create(pushWebHookVO, token, GitOpsConstants.COMMIT);

        }
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_SYNC_GITOPS, description = "devops同步gitops库相关操作", inputSchemaClass = PushWebHookVO.class)
    public void fileResourceSyncSaga(PushWebHookVO pushWebHookVO, String token) {
        LOGGER.info("````````````````````````````` {}", pushWebHookVO.getCheckoutSha());

        Long userId = userAttrService.baseQueryUserIdByGitlabUserId(TypeUtil.objToLong(pushWebHookVO.getUserId()));
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);

        // 设置用户上下文
        CustomContextUtil.setDefaultIfNull(iamUserDTO);

        pushWebHookVO.setToken(token);
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByToken(pushWebHookVO.getToken());

        if (devopsEnvironmentDTO == null) {
            LOGGER.error("Environment is unexpectedly null. The token is {}. The gitlab projectId is {}.", token, pushWebHookVO.getProjectId());
            return;
        }

        pushWebHookVO.getCommits().forEach(commitDTO -> {
            DevopsEnvCommitDTO devopsEnvCommitDTO = new DevopsEnvCommitDTO();
            devopsEnvCommitDTO.setEnvId(devopsEnvironmentDTO.getId());
            devopsEnvCommitDTO.setCommitSha(commitDTO.getId());
            devopsEnvCommitDTO.setCommitUser(TypeUtil.objToLong(pushWebHookVO.getUserId()));
            devopsEnvCommitDTO.setCommitDate(commitDTO.getTimestamp());
            if (devopsEnvCommitService.baseQueryByEnvIdAndCommit(devopsEnvironmentDTO.getId(), commitDTO.getId()) == null) {
                devopsEnvCommitService.baseCreate(devopsEnvCommitDTO);
            }
        });
        DevopsEnvCommitDTO devopsEnvCommitDTO = devopsEnvCommitService.baseQueryByEnvIdAndCommit(devopsEnvironmentDTO.getId(), pushWebHookVO.getCheckoutSha());
        devopsEnvironmentDTO.setSagaSyncCommit(devopsEnvCommitDTO.getId());
        devopsEnvironmentService.baseUpdateSagaSyncEnvCommit(devopsEnvironmentDTO);
        LOGGER.info("update devopsCommit successfully: {}", pushWebHookVO.getCheckoutSha());

        producer.apply(StartSagaBuilder.newBuilder().withLevel(ResourceLevel.PROJECT).withSourceId(devopsEnvironmentDTO.getProjectId()).withRefType("env").withSagaCode(SagaTopicCodeConstants.DEVOPS_SYNC_GITOPS), builder -> builder.withPayloadAndSerialize(pushWebHookVO).withRefId(devopsEnvironmentDTO.getId().toString()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void fileResourceSync(PushWebHookVO pushWebHookVO) {
        if (Boolean.TRUE.equals(pushWebHookVO.getHasErrors())) {
            LOGGER.debug("Skip GitOps due to previous devops. push webhook is {}", pushWebHookVO);
            return;
        }

        LOGGER.info("Starting GitOps: context user id is: {}", DetailsHelper.getUserDetails() == null ? null : DetailsHelper.getUserDetails().getUserId());
        final Integer gitLabProjectId = pushWebHookVO.getProjectId();
        final Integer gitLabUserId = pushWebHookVO.getUserId();
        final Long userId = userAttrService.baseQueryUserIdByGitlabUserId(TypeUtil.objToLong(gitLabUserId));

        final List<String> operationFiles = new ArrayList<>();
        final List<String> deletedFiles = new ArrayList<>();
        final Set<DevopsEnvFileResourceDTO> beforeSync = new HashSet<>();
        final Set<DevopsEnvFileResourceDTO> beforeSyncDelete = new HashSet<>();
        //根据token查出环境

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.queryByTokenWithClusterCode(pushWebHookVO.getToken());
        DevopsEnvCommitDTO devopsEnvCommitDTO = devopsEnvCommitService.baseQuery(devopsEnvironmentDTO.getSagaSyncCommit());
        boolean tagNotExist;
        Map<String, String> objectPath;
        //从iam服务中查出项目和组织code
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //本地路径
        final String path = GitOpsUtil.getLocalPathToStoreEnv(organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), devopsEnvironmentDTO.getClusterCode(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId());
        //生成环境git仓库ssh地址
        final String url = GitUtil.getGitlabSshUrl(pattern, gitUtil.getSshUrl(), organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), devopsEnvironmentDTO.getCode(), EnvironmentType.forValue(devopsEnvironmentDTO.getType()), devopsEnvironmentDTO.getClusterCode());

        LOGGER.info("The gitOps Repository ssh url: {}", url);

        final Long envId = devopsEnvironmentDTO.getId();

        final Long projectId = devopsEnvironmentDTO.getProjectId();

        try {
            //更新本地库到最新提交
            Git git = handDevopsEnvGitRepository(path, url, devopsEnvironmentDTO.getEnvIdRsa());
            LOGGER.info("更新gitops库成功");
            //查询devops-sync tag是否存在，存在则比较tag和最新commit的diff，不存在则识别gitops库下所有文件为新增文件
            tagNotExist = getDevopsSyncTag(pushWebHookVO);

            if (tagNotExist) {
                operationFiles.addAll(FileUtil.getFilesPath(path));
                operationFiles.forEach(file -> {
                    List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService.baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), file);
                    if (!devopsEnvFileResourceDTOS.isEmpty()) {
                        beforeSync.addAll(devopsEnvFileResourceDTOS);
                    }
                });
            } else {
                handleDiffs(gitLabProjectId, operationFiles, deletedFiles, beforeSync, beforeSyncDelete, devopsEnvironmentDTO, devopsEnvCommitDTO);
            }

            Map<Class, List> resourceKindMap = initResourceKindContainer();

            //从文件中读出对象,序列化为K8S对象
            objectPath = convertFileToK8sObjects(operationFiles, path, EnvironmentType.forValue(devopsEnvironmentDTO.getType()), resourceKindMap, devopsEnvironmentDTO.getId(), new ArrayList<>(beforeSyncDelete));

            LOGGER.info("序列化k8s对象成功！");

            List<DevopsEnvFileResourceDTO> beforeSyncFileResource = new ArrayList<>(beforeSync);

            //将k8s对象初始化为实例，网络，域名，证书，秘钥对象,处理对象文件关系
            resourceKindMap.computeIfAbsent(V1Endpoints.class, key -> new ArrayList());
            resourceKindMap.forEach((k, v) -> {
                HandlerObjectFileRelationsService handler = objectFileRelationHandlers.get(k);
                if (handler == null) {
                    if (k != V1Endpoints.class) {
                        LOGGER.info("Handler is unexpectedly null. The resource kind is {}", k);
                    }
                    return;
                }
                handler.handlerRelations(objectPath, beforeSyncFileResource, v, resourceKindMap.get(V1Endpoints.class), envId, projectId, path, userId);
            });
            LOGGER.info("k8s对象转换平台对象成功！");
            // 处理文件
            handleFiles(operationFiles, deletedFiles, devopsEnvironmentDTO, devopsEnvCommitDTO, path);

            // 更新远程仓库的DevOps相关的tag
            handleTag(git, devopsEnvironmentDTO.getEnvIdRsa(), pushWebHookVO, devopsEnvCommitDTO, tagNotExist);

            devopsEnvironmentDTO.setDevopsSyncCommit(devopsEnvCommitDTO.getId());
            //更新环境 解释commit
            devopsEnvironmentService.baseUpdateDevopsSyncEnvCommit(devopsEnvironmentDTO);
            //向agent发送同步指令
            agentCommandService.sendCommand(devopsEnvironmentDTO);
            LOGGER.info("发送GitOps同步成功指令成功");
        } catch (CommonException e) {
            String filePath = "";
            String errorCode = "";
            if (e instanceof GitOpsExplainException) {
                filePath = ((GitOpsExplainException) e).getFilePath();
                errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
            }
            DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = getDevopsFileError(envId, filePath, path);
            String error;
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception parameters: {}", Arrays.toString(e.getParameters()));
                }
                error = messageSource.getMessage(e.getMessage(), e.getParameters(), GitOpsUtil.locale());
            } catch (Exception e1) {
                LOGGER.debug("Exception occurred when read message from message source. The original message is {}. The exception is : {}", e.getMessage(), e1);
                error = e.getMessage();
            }
            devopsEnvFileErrorDTO.setError(error + ":" + errorCode);
            devopsEnvFileErrorService.baseCreateOrUpdate(devopsEnvFileErrorDTO);
            LOGGER.info(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }

        //删除文件错误记录
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDTO.setEnvId(devopsEnvironmentDTO.getId());
        devopsEnvFileErrorService.baseDelete(devopsEnvFileErrorDTO);
        // do sth to files
    }

    private Map<Class, List> initResourceKindContainer() {
        Map<Class, List> map = new HashMap<>();
        handlerObjectFileRelationsServices.forEach(handler -> map.computeIfAbsent(handler.getTarget(), k -> new ArrayList()));
        return map;
    }

    @Override
    public Boolean isBranchNameUnique(Long projectId, Long applicationId, String branchName) {
        AppServiceDTO applicationDTO = appServiceService.baseQuery(applicationId);
        BranchDTO branchDTO = gitlabServiceClientOperator.queryBranch(applicationDTO.getGitlabProjectId(), branchName);
        return branchDTO == null || ObjectUtils.isEmpty(branchDTO.getName());
    }

    private void handleFiles(List<String> operationFiles, List<String> deletedFiles, DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsEnvCommitDTO devopsEnvCommitDTO, String path) {
        //新增解释文件记录
        for (String filePath : operationFiles) {
            DevopsEnvFileDTO devopsEnvFileDTO = devopsEnvFileService.baseQueryByEnvAndPath(devopsEnvironmentDTO.getId(), filePath);
            if (devopsEnvFileDTO == null) {
                devopsEnvFileDTO = new DevopsEnvFileDTO();
                devopsEnvFileDTO.setDevopsCommit(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvFileDTO.setFilePath(filePath);
                devopsEnvFileDTO.setEnvId(devopsEnvCommitDTO.getEnvId());
                devopsEnvFileService.baseCreate(devopsEnvFileDTO);
            } else {
                devopsEnvFileDTO.setDevopsCommit(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvFileService.baseUpdate(devopsEnvFileDTO);
            }
        }

        for (String filePath : deletedFiles) {
            DevopsEnvFileDTO devopsEnvFileDTO = new DevopsEnvFileDTO();
            devopsEnvFileDTO.setEnvId(devopsEnvironmentDTO.getId());
            devopsEnvFileDTO.setFilePath(filePath);
            devopsEnvFileService.baseDelete(devopsEnvFileDTO);
        }
    }

    private void handleTag(Git git, String sshKey, PushWebHookVO pushWebHookVO, DevopsEnvCommitDTO devopsEnvCommitDTO, Boolean tagNotExist) {
        if (Boolean.TRUE.equals(tagNotExist)) {
            GitUtil.createTagAndPush(git, sshKey, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
            if (getDevopsSyncTag(pushWebHookVO)) {
                GitUtil.createTagAndPush(git, sshKey, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
            }
        } else {
            GitUtil.pushTag(git, sshKey, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
            if (getDevopsSyncTag(pushWebHookVO)) {
                GitUtil.createTagAndPush(git, sshKey, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
            }
        }
    }

    private void handleDiffs(Integer gitLabProjectId, List<String> operationFiles, List<String> deletedFiles, Set<DevopsEnvFileResourceDTO> beforeSync, Set<DevopsEnvFileResourceDTO> beforeSyncDelete, DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsEnvCommitDTO devopsEnvCommitDTO) {
        //获取将此次最新提交与tag作比价得到diff
        CompareResultDTO compareResultDTO = gitlabServiceClientOperator.queryCompareResult(gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
        compareResultDTO.getDiffs().forEach(t -> {
            if (t.getNewPath().contains("yaml") || t.getNewPath().contains("yml")) {
                if (Boolean.TRUE.equals(t.getDeletedFile())) {
                    // t.getNewPath() 而不是t.getOldPath()，这里能用是因为删除的文件的两个的值一致
                    deletedFiles.add(t.getNewPath());
                } else if (Boolean.TRUE.equals(t.getRenamedFile())) {
                    deletedFiles.add(t.getOldPath());
                    operationFiles.add(t.getNewPath());
                } else {
                    operationFiles.add(t.getNewPath());
                }
            }

            List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService.baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), t.getOldPath());
            if (!devopsEnvFileResourceDTOS.isEmpty()) {
                beforeSync.addAll(devopsEnvFileResourceDTOS);
            }
        });

        deletedFiles.forEach(file -> {
            List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService.baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), file);
            if (!devopsEnvFileResourceDTOS.isEmpty()) {
                beforeSyncDelete.addAll(devopsEnvFileResourceDTOS);
            }
        });
    }

    /**
     * 将涉及的文件内的对象进行反序列化处理，获取后续处理所需要的元数据
     *
     * @param files             对应之前diff操作的operationFiles
     * @param path              环境库的本地目录
     * @param environmentType   环境的类型 user/system
     * @param resourceContainer 用于放置解析出的资源的容器
     * @param envId             环境ID
     * @param beforeSyncDelete  删除的文件的 资源文件关联关系
     * @return 对象hashcode和对象所处文件名对映射
     */
    private Map<String, String> convertFileToK8sObjects(List<String> files, String path, EnvironmentType environmentType, Map<Class, List> resourceContainer, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete) {
        Map<String, String> objectPath = new HashMap<>();
        Yaml yaml = new Yaml();
        final Map<String, ConvertK8sObjectService> converters = EnvironmentType.USER == environmentType ? userEnvSupportedResourceConverters : systemEnvSupportedResourceConverters;

        files.forEach(filePath -> {
            File file = new File(String.format("%s/%s", path, filePath));
            Iterable<Object> allParts;
            try {
                allParts = yaml.loadAll(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new CommonException(e.getMessage(), e);
            }
            while (allParts.iterator().hasNext()) {
                Object data;
                try {
                    // 真正对数据进行解析是在next方法中调用
                    // next方法会调用 org.yaml.snakeyaml.constructor.BaseConstructor.getData()
                    data = allParts.iterator().next();
                } catch (Exception e) {
                    // 捕获Yaml解析出错，如： "---{}\n---"
                    // 返回友好的方式
                    throw new GitOpsExplainException(GitOpsObjectError.FILE_NOT_YAML.getError(), filePath);
                }

                if (data == null) {
                    // 跳过只有"---"而没有内容的对象，例如"---\n---\n"
                    continue;
                }

                if (!(data instanceof Map)) {
                    // 不是yaml格式的文件部分，报错 如： "---{}\n---" 或者 "---\naaa\n
                    throw new GitOpsExplainException(GitOpsObjectError.FILE_NOT_YAML.getError(), filePath);
                }

                JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
                if (jsonObject.get("kind") == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_KIND_NOT_FOUND.getError(), filePath);
                }


                // 之前都是对数据进行校验的阶段
                String type = jsonObject.get("kind").toString();

                // 处理当前资源的处理逻辑
                ConvertK8sObjectService currentHandler;
                if (ResourceType.PERSISTENT_VOLUME_CLAIM.getType().equals(type) && isPvcTreatedAsCustomizeResourceBefore(envId, getPersistentVolumeClaimName(jsonObject, filePath))) {
                    // 0.20版本之前被作为自定义资源解析的PVC仍然作为自定义资源看待
                    currentHandler = converters.get(ResourceType.MISSTYPE.getType());
                } else if (ResourceType.INGRESS.getType().equals(type)) {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
                    boolean operateForOldTypeIngress = devopsIngressService.operateForOldTypeIngress(devopsEnvironmentDTO.getClusterId());
                    if (operateForOldTypeIngress) {
                        currentHandler = converters.get(ResourceType.V1BETA1_INGRESS.getType());
                    } else {
                        currentHandler = converters.get(ResourceType.V1_INGRESS.getType());
                    }
                } else {
                    currentHandler = converters.get(type);
                    if (currentHandler == null) {
                        // 准备默认处理方式，用户环境默认处理方式是作为自定义资源处理，
                        // 系统环境的默认处理方式是抛出异常以表示不支持
                        currentHandler = converters.get(ResourceType.MISSTYPE.getType());
                    }
                }

                Object resource = currentHandler.serializableObject(jsonObject.toJSONString(), filePath, objectPath, envId);
                resourceContainer.computeIfAbsent(resource.getClass(), t -> new ArrayList<>());

                // 校验参数
                currentHandler.checkParameters(resource, objectPath);

                // 校验资源是否已经存在
                currentHandler.checkIfExist(resourceContainer.get(resource.getClass()), envId, beforeSyncDelete, objectPath, resource);
            }
        });
        return objectPath;
    }

    private static String getPersistentVolumeClaimName(JSONObject jsonObject, String filePath) {
        String name;
        try {
            name = jsonObject.getJSONObject(METADATA).getString(NAME);
        } catch (Exception e) {
            throw new GitOpsExplainException(GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_NAME_NOT_FOUND.getError(), filePath);
        }
        return name;
    }

    /**
     * 判断PVC在之前0.20版本之前是否被解析为自定义资源
     *
     * @param envId        环境id
     * @param resourceName 资源名称
     * @return true表明是作为自定义资源
     */
    private boolean isPvcTreatedAsCustomizeResourceBefore(Long envId, String resourceName) {
        return devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, ResourceType.PERSISTENT_VOLUME_CLAIM.getType(), resourceName) != null;
    }

    private void commitBranchSync(PushWebHookVO pushWebHookVO, Long appServiceId) {
        try {
            String branchName = pushWebHookVO.getRef().replaceFirst(REF_HEADS, "");

            DevopsBranchDTO devopsBranchDTO = devopsBranchService.baseQueryByAppAndBranchName(appServiceId, branchName);
            if (devopsBranchDTO == null) {
                createBranchSync(pushWebHookVO, appServiceId);
                return;
            }


            String lastCommit = pushWebHookVO.getAfter();
            Optional<CommitVO> lastCommitOptional = pushWebHookVO.getCommits().stream().filter(t -> lastCommit.equals(t.getId())).findFirst();
            CommitVO lastCommitDTO = new CommitVO();
            if (lastCommitOptional.isPresent()) {
                lastCommitDTO = lastCommitOptional.get();
            }

            devopsBranchDTO.setLastCommit(lastCommit);
            devopsBranchDTO.setLastCommitDate(lastCommitDTO.getTimestamp());
            devopsBranchDTO.setLastCommitMsg(LogUtil.cutOutString(lastCommitDTO.getMessage(), MiscConstants.DEVOPS_BRANCH_LAST_COMMIT_MESSAGE_MAX_LENGTH));
            devopsBranchDTO.setLastCommitUser(pushWebHookVO.getUserId().longValue());
            devopsBranchService.baseUpdateBranchLastCommit(devopsBranchDTO);
        } catch (Exception e) {
            LOGGER.info("devops.update.branch");
        }

    }


    private MergeRequestVO devopsMergeRequestToMergeRequest(DevopsMergeRequestDTO devopsMergeRequestDTO) {
        MergeRequestVO mergeRequestVO = ConvertUtils.convertObject(devopsMergeRequestDTO, MergeRequestVO.class);
        mergeRequestVO.setProjectId(devopsMergeRequestDTO.getGitlabProjectId().intValue());
        mergeRequestVO.setId(devopsMergeRequestDTO.getId().intValue());
        mergeRequestVO.setIid(devopsMergeRequestDTO.getGitlabMergeRequestId().intValue());
        Long authorUserId = userAttrService.queryUserIdByGitlabUserId(devopsMergeRequestDTO.getAuthorId());
        Long assigneeId = userAttrService.queryUserIdByGitlabUserId(devopsMergeRequestDTO.getAssigneeId());
        List<CommitDTO> commitDTOS;
        try {
            commitDTOS = gitlabServiceClientOperator.listCommits(devopsMergeRequestDTO.getGitlabProjectId().intValue(), devopsMergeRequestDTO.getGitlabMergeRequestId().intValue(), getGitlabUserId());
            if (commitDTOS == null) {
                LOGGER.info("MergeRequest not exist");
                devopsMergeRequestMapper.delete(devopsMergeRequestDTO);
                return null;
            }
            mergeRequestVO.setCommits(ConvertUtils.convertList(commitDTOS, CommitVO.class));
        } catch (FeignException e) {
            LOGGER.info(e.getMessage());
        }
        IamUserDTO authorUser = baseServiceClientOperator.queryUserByUserId(authorUserId);
        if (authorUser != null) {
            AuthorVO authorVO = new AuthorVO();
            authorVO.setUsername(Boolean.TRUE.equals(authorUser.getLdap()) ? authorUser.getLoginName() : authorUser.getEmail());
            authorVO.setName(authorUser.getRealName());
            authorVO.setId(authorUser.getId() == null ? null : authorUser.getId().intValue());
            authorVO.setWebUrl(authorUser.getImageUrl());
            mergeRequestVO.setAuthor(authorVO);
        }
        IamUserDTO assigneeUser = baseServiceClientOperator.queryUserByUserId(assigneeId);
        if (assigneeUser != null) {
            AssigneeVO assigneeVO = new AssigneeVO();
            if (Boolean.TRUE.equals(assigneeUser.getLdap())) {
                assigneeVO.setUsername(assigneeUser.getLdap() ? assigneeUser.getLoginName() : assigneeUser.getEmail());
            } else {
                assigneeVO.setUsername(assigneeUser.getEmail());
            }
            assigneeVO.setName(assigneeUser.getRealName());
            assigneeVO.setId(assigneeId.intValue());
            assigneeVO.setWebUrl(assigneeUser.getImageUrl());
            mergeRequestVO.setAssignee(assigneeVO);
        }
        return mergeRequestVO;
    }


    private BranchVO getBranchVO(DevopsBranchDTO devopsBranchDTO, String lastCommitUrl, IamUserDTO commitUserDTO, IamUserDTO userDTO, List<IssueDTO> issues, Long sagaInstanceId) {
        String createUserUrl = null;
        String createUserName = null;
        String createUserRealName = null;
        if (userDTO != null) {
            if (Boolean.TRUE.equals(userDTO.getLdap())) {
                createUserName = userDTO.getLoginName();
            } else {
                createUserName = userDTO.getEmail();
            }
            createUserUrl = userDTO.getImageUrl();
            createUserRealName = userDTO.getRealName();
        }
        return new BranchVO(devopsBranchDTO, lastCommitUrl, createUserUrl, issues, commitUserDTO, createUserName, createUserRealName, devopsBranchDTO.getStatus(), devopsBranchDTO.getErrorMessage(), sagaInstanceId);
    }

    private void deleteBranchSync(PushWebHookVO pushWebHookVO, Long appServiceId) {
        try {
            String branchName = pushWebHookVO.getRef().replaceFirst(REF_HEADS, "");
            devopsBranchService.baseDelete(appServiceId, branchName);
        } catch (Exception e) {
            LOGGER.info("devops.devops.branch.delete", e);
        }
    }

    private void createBranchSync(PushWebHookVO pushWebHookVO, Long appServiceId) {
        try {
            String lastCommit = pushWebHookVO.getAfter();
            Long userId = pushWebHookVO.getUserId().longValue();

            CommitDTO commitDTO = gitlabServiceClientOperator.queryCommit(

                    pushWebHookVO.getProjectId(), lastCommit, userId.intValue());
            String branchName = pushWebHookVO.getRef().replaceFirst(REF_HEADS, "");

            if (devopsBranchService.baseQueryByAppAndBranchName(appServiceId, branchName) == null) {
                DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
                devopsBranchDTO.setUserId(userId);
                devopsBranchDTO.setAppServiceId(appServiceId);

                devopsBranchDTO.setCheckoutDate(commitDTO.getCommittedDate());
                devopsBranchDTO.setCheckoutCommit(lastCommit);
                devopsBranchDTO.setBranchName(branchName);

                devopsBranchDTO.setLastCommitUser(userId);
                devopsBranchDTO.setLastCommit(lastCommit);
                devopsBranchDTO.setLastCommitMsg(LogUtil.cutOutString(commitDTO.getMessage(), MiscConstants.DEVOPS_BRANCH_LAST_COMMIT_MESSAGE_MAX_LENGTH));

                devopsBranchDTO.setLastCommitDate(commitDTO.getCommittedDate());
                devopsBranchService.baseCreate(devopsBranchDTO);


            }
        } catch (Exception e) {
            LOGGER.info("devops.create.branch");
        }
    }

    private DevopsEnvFileErrorDTO getDevopsFileError(Long envId, String filePath, String path) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = devopsEnvFileErrorService.baseQueryByEnvIdAndFilePath(envId, filePath);
        if (devopsEnvFileErrorDTO == null) {
            devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO();
            devopsEnvFileErrorDTO.setFilePath(filePath);
            devopsEnvFileErrorDTO.setEnvId(envId);
            devopsEnvFileErrorDTO.setCommit(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
        } else {
            devopsEnvFileErrorDTO.setFilePath(filePath);
            devopsEnvFileErrorDTO.setCommit(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
        }
        return devopsEnvFileErrorDTO;
    }

    private boolean getDevopsSyncTag(PushWebHookVO pushWebHookVO) {

        return gitlabServiceClientOperator.listTags(pushWebHookVO.getProjectId(), pushWebHookVO.getUserId()).stream().noneMatch(tagDO -> tagDO.getName().equals(GitUtil.DEV_OPS_SYNC_TAG));

    }

    private Git handDevopsEnvGitRepository(String path, String url, String envIdRsa) {
        synchronized (path.intern()) {
            File file = new File(path);
            if (!file.exists()) {
                return gitUtil.cloneBySsh(path, url, envIdRsa);
            } else {
                if (file.isDirectory() && file.listFiles().length > 0) {
                    try {
                        String localPath = String.format("%s%s", path, GIT_SUFFIX);
                        return gitUtil.pullBySsh(localPath, envIdRsa);
                    } catch (Exception e) {
                        // 有时本地文件和远端gitops库文件冲突可能导致pull 代码库失败，所以添加以下补偿逻辑
                        if (e instanceof CheckoutConflictException) {
                            // 删除本地gitops文件，然后重新clone
                            FileUtil.deleteDirectory(file);
                            return gitUtil.cloneBySsh(path, url, envIdRsa);
                        } else {
                            throw new CommonException("devops.git.pull", e);
                        }
                    }
                } else {
                    return gitUtil.cloneBySsh(path, url, envIdRsa);
                }
            }
        }
    }

    @Override
    public Page<BranchVO> pageBranchFilteredByIssueId(Long projectId, PageRequest pageable, Long appServiceId, String params, Long issueId) {
        Page<DevopsBranchDTO> branchDTOPage = devopsBranchService.basePageBranch(appServiceId, pageable, params, issueId);
        return ConvertUtils.convertPage(branchDTOPage, BranchVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAssociation(Long projectId, Long appServiceId, Long branchId, Long issueId) {
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        DevopsBranchDTO devopsBranchDTO = devopsBranchService.baseQueryByAppAndBranchIdWithIssueId(appServiceId, branchId);
        Set<Long> branchIdToRemove = new HashSet<>();
        branchIdToRemove.add(branchId);
        // 不等于null，代表分支为删除
        if (devopsBranchDTO != null) {
            CommonExAssertUtil.assertTrue(devopsBranchDTO.getIssueIds().contains(issueId), "devops.branch.issue.mismatch");
            String branchName = devopsBranchDTO.getBranchName();
            // 这里的操作是查出之前被删除的同名分支id
            List<DevopsGitlabCommitDTO> devopsGitlabCommitDTOS = devopsGitlabCommitService.baseListByAppIdAndBranch(appServiceId, branchName, null);
            if (!CollectionUtils.isEmpty(devopsGitlabCommitDTOS)) {
                Set<Long> commitIds = devopsGitlabCommitDTOS.stream().map(DevopsGitlabCommitDTO::getId).collect(Collectors.toSet());
                branchIdToRemove.addAll(devopsIssueRelService.listBranchIdsByCommitIds(commitIds));
            }
        } else {
            // 此情况是分支已被删除，需要用另一种方式获取分支名称
            List<Long> relatedCommitIds = devopsIssueRelService.listCommitRelationByBranchId(branchId);
            // 因为所有的commit都属于同一个分支，因此默认取第1个commitId来查询分支名称
            if (CollectionUtils.isEmpty(relatedCommitIds)) {
                return;
            }
            DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.selectByPrimaryKey(relatedCommitIds.get(0));
            // 这里的操作是查出之前被删除的同名分支id
            List<DevopsGitlabCommitDTO> devopsGitlabCommitDTOS = devopsGitlabCommitService.baseListByAppIdAndBranch(appServiceId, devopsGitlabCommitDTO.getRef(), null);
            if (!CollectionUtils.isEmpty(devopsGitlabCommitDTOS)) {
                Set<Long> commitIds = devopsGitlabCommitDTOS.stream().map(DevopsGitlabCommitDTO::getId).collect(Collectors.toSet());
                branchIdToRemove.addAll(devopsIssueRelService.listBranchIdsByCommitIds(commitIds));
            }
        }

        branchIdToRemove.forEach(id -> {
            // 移除分支关联关系
            devopsIssueRelService.deleteRelationByObjectAndObjectIdAndIssueId(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), id, issueId);
            // 移除分支对应的提交关联关系
            devopsIssueRelService.deleteCommitRelationByBranchId(id, issueId);
        });

        // 查出剩下的和敏捷问题有关联的分支
        Set<DevopsIssueRelDTO> remainBranchIssueRelation = devopsIssueRelService.listRelationByIssueIdAndProjectIdAndObjectType(projectId, DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), issueId);
        List<DevopsBranchVO> devopsBranchVOS = remainBranchIssueRelation.stream().map(r -> {
            DevopsBranchVO devopsBranchVO = new DevopsBranchVO();
            devopsBranchVO.setAppServiceCode(r.getAppServiceCode());
            devopsBranchVO.setProjectId(r.getProjectId());
            devopsBranchVO.setBranchId(r.getBranchId());
            return devopsBranchVO;
        }).collect(Collectors.toList());
        IssueIdAndBranchIdsVO issueIdAndBranchIdsVO = new IssueIdAndBranchIdsVO();
        issueIdAndBranchIdsVO.setIssueId(issueId);
        issueIdAndBranchIdsVO.setBranches(new ArrayList<>(devopsBranchVOS));

        agileServiceClientOperator.deleteTagByBranch(projectId, issueIdAndBranchIdsVO);
    }

    @Override
    public List<IssueIdAndBranchIdsVO> getIssueIdsBetweenTags(Long projectId, Long appServiceId, String from, String to) {
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        Set<String> commitSha;

        if (StringUtils.isEmpty(to)) {
            return new ArrayList<>();
        }

        if (!StringUtils.isEmpty(from)) {
            CompareResultDTO diffs = gitlabServiceClientOperator.queryCompareResult(appServiceDTO.getGitlabProjectId(), from, to);
            commitSha = diffs.getCommits().stream().map(CommitDTO::getId).collect(Collectors.toSet());
        } else {
            List<CommitDTO> commits = gitlabServiceClientOperator.getCommits(appServiceDTO.getGitlabProjectId(), to, "2000-01-01 00:00:00 CST");
            commitSha = commits.stream().map(CommitDTO::getId).collect(Collectors.toSet());
        }

        if (!CollectionUtils.isEmpty(commitSha)) {
            Set<Long> commitIds = devopsGitlabCommitService.listIdsByCommitSha(commitSha);
            Map<Long, List<Long>> mappedIssueIds = devopsIssueRelService.listMappedIssueIdsByObjectTypeAndObjectId(DevopsIssueRelObjectTypeEnum.COMMIT.getValue(), commitIds);
            Set<Long> issueIds = new HashSet<>();
            mappedIssueIds.forEach((k, v) -> issueIds.addAll(v));
            return devopsIssueRelService.listBranchInfoByIssueIds(issueIds);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<GroupDTO> listOwnedGroupExpectCurrent(Long projectId, String search) {

        if (org.apache.commons.lang3.StringUtils.isEmpty(search)) {
            return new ArrayList<>();
        }
        // 查询当前group的id
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);

        // 查询是owner权限的group列表
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());

        List<GroupDTO> groupDTOS = gitlabServiceClientOperator.listGroupsWithParam(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), true, search, Collections.singletonList(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId())));

        if (CollectionUtils.isEmpty(groupDTOS)) {
            return new ArrayList<>();
        }
        Set<Integer> groupIds = groupDTOS.stream().map(GroupDTO::getId).collect(Collectors.toSet());

        List<DevopsProjectDTO> devopsProjectDTOS = devopsProjectService.listExistGroup(groupIds);
        if (!CollectionUtils.isEmpty(devopsProjectDTOS)) {
            Map<Long, GroupDTO> groupDTOMap = groupDTOS.stream().distinct().collect(Collectors.toMap(v -> TypeUtil.objToLong(v.getId()), Function.identity()));
            devopsProjectDTOS.forEach(devopsProjectDTO1 -> {

                if (groupDTOMap.get(devopsProjectDTO1.getDevopsAppGroupId()) != null) {
                    groupDTOMap.get(devopsProjectDTO1.getDevopsAppGroupId()).setBindFlag(true);
                }
                if (groupDTOMap.get(devopsProjectDTO1.getDevopsEnvGroupId()) != null) {
                    groupDTOMap.get(devopsProjectDTO1.getDevopsEnvGroupId()).setBindFlag(true);
                }
                if (groupDTOMap.get(devopsProjectDTO1.getDevopsClusterEnvGroupId()) != null) {
                    groupDTOMap.get(devopsProjectDTO1.getDevopsClusterEnvGroupId()).setBindFlag(true);
                }
            });
        }

        return groupDTOS;
    }

    @Override
    public Page<GitlabProjectDTO> listOwnedProjectByGroupId(Long projectId, Integer gitlabGroupId, String search, PageRequest pageRequest) {

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());

        List<GitlabProjectDTO> gitlabProjectDTOS = gitlabServiceClientOperator.listProject(gitlabGroupId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), true, search, pageRequest.getPage(), pageRequest.getSize(), null);

        int totalElements = 0;
        if (gitlabProjectDTOS.size() < pageRequest.getSize()) {
            totalElements = (pageRequest.getPage() * pageRequest.getSize()) + gitlabProjectDTOS.size();
        } else {
            List<GitlabProjectDTO> nextProjects = gitlabServiceClientOperator.listProject(gitlabGroupId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), true, search, pageRequest.getPage() + 1, pageRequest.getSize(), null);
            if (CollectionUtils.isEmpty(nextProjects)) {
                totalElements = ((pageRequest.getPage() + 1) * pageRequest.getSize());
            } else {
                totalElements = ((pageRequest.getPage() + 1) * pageRequest.getSize()) + 1;
            }
        }


        Page<GitlabProjectDTO> pageFromList = new Page<>();
        pageFromList.setTotalElements(totalElements);
        pageFromList.setContent(gitlabProjectDTOS);
        pageFromList.setSize(pageRequest.getSize());
        pageFromList.setNumber(pageRequest.getPage());
        return pageFromList;
    }

    @Override
    public Page<BranchVO> pageBranchBasicInfoByOptions(Long projectId, PageRequest pageable, Long appServiceId, String params) {
        AppServiceDTO applicationDTO = appServiceService.baseQuery(appServiceId);

        if (applicationDTO == null) {
            return new Page<>();
        }
        // 外部应用服务直接从gitlab查询
        if (applicationDTO.getExternalConfigId() != null) {
            return listExternalBranch(pageable, params, applicationDTO);
        }

        try {
            checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, AppServiceEvent.BRANCH_LIST);
        } catch (GitlabAccessInvalidException e) {
            return new Page<>();
        }

        // 查询用户是否在该gitlab project下
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (userAttrDTO == null) {
            throw new CommonException(DEVOPS_GITLAB_USER_SYNC_FAILED);
        }
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);

        if (!permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId)) {
            MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(devopsProjectDTO.getDevopsAppGroupId().intValue(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO == null || memberDTO.getId() == null) {
                memberDTO = gitlabServiceClientOperator.getMember(Long.valueOf(applicationDTO.getGitlabProjectId()), userAttrDTO.getGitlabUserId());
            }
            if (memberDTO == null) {
                throw new CommonException(DEVOPS_USER_NOT_IN_GITLAB_PROJECT);
            }
        }
        Page<DevopsBranchDTO> devopsBranchDTOPageInfo = devopsBranchService.basePageBranch(appServiceId, pageable, params, null);

        return ConvertUtils.convertPage(devopsBranchDTOPageInfo, BranchVO.class);
    }

    @Override
    public Integer syncBranch(Long projectId, Long appServiceId, Boolean sync) {
        Integer syncCount = 0;
        // 查询所有分支
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        checkGitlabAccessLevelService.checkGitlabPermission(appServiceDTO.getProjectId(), appServiceId, AppServiceEvent.BRANCH_SYNC);
        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        List<BranchDTO> branchDTOS = gitlabServiceClientOperator.listBranch(appServiceDTO.getGitlabProjectId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (CollectionUtils.isEmpty(branchDTOS)) {
            return syncCount;
        }
        List<String> gitlabBranches = branchDTOS.stream().map(BranchDTO::getName).collect(Collectors.toList());
        // 查询c7n已经存在的分支
        DevopsBranchDTO queryBranchDTO = new DevopsBranchDTO();
        queryBranchDTO.setAppServiceId(appServiceId);
        List<String> devopsBranches = devopsBranchMapper.select(queryBranchDTO).stream().map(DevopsBranchDTO::getBranchName).collect(Collectors.toList());
        // 创建分支
        for (BranchDTO branchDTO : branchDTOS) {
            if (!devopsBranches.contains(branchDTO.getName())) {
                syncCount = syncCount + 1;
                if (sync) {
                    CommitDTO commitDTO = branchDTO.getCommit();
                    DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
                    devopsBranchDTO.setBranchName(branchDTO.getName());
                    devopsBranchDTO.setUserId(userAttrDTO.getGitlabUserId());
                    devopsBranchDTO.setAppServiceId(appServiceId);

                    devopsBranchDTO.setCheckoutDate(commitDTO.getCommittedDate());
                    devopsBranchDTO.setCheckoutCommit(commitDTO.getId());

                    devopsBranchDTO.setLastCommitUser(userAttrDTO.getGitlabUserId());
                    devopsBranchDTO.setLastCommit(commitDTO.getId());
                    devopsBranchDTO.setLastCommitMsg(LogUtil.cutOutString(commitDTO.getMessage(), MiscConstants.DEVOPS_BRANCH_LAST_COMMIT_MESSAGE_MAX_LENGTH));
                    devopsBranchDTO.setLastCommitDate(commitDTO.getCommittedDate());

                    devopsBranchService.baseCreate(devopsBranchDTO);
                }
            }
        }
        // 删除不存在分支
        for (String t : devopsBranches) {
            if (!gitlabBranches.contains(t)) {
                syncCount = syncCount + 1;
                if (sync) {
                    devopsBranchService.baseDelete(appServiceId, t);
                }
            }
        }
        return syncCount;
    }

    @Override
    public Integer syncOpenMergeRequest(Long projectId, Long appServiceId, Boolean sync) {
        int syncCount = 0;
        // 查询所有开放的合并请求
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        checkGitlabAccessLevelService.checkGitlabPermission(appServiceDTO.getProjectId(), appServiceId, AppServiceEvent.MERGE_REQUEST_LIST);
        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        List<MergeRequestDTO> mergeRequestDTOS = gitlabServiceClientOperator.listMergeRequest(appServiceDTO.getGitlabProjectId(), MergeRequestState.OPENED.getValue());
        if (CollectionUtils.isEmpty(mergeRequestDTOS)) {
            return syncCount;
        }
        List<Integer> openMergeRequestIds = mergeRequestDTOS.stream().map(MergeRequestDTO::getIid).collect(Collectors.toList());
        // 查询c7n已经存在的开放合并请求
        DevopsMergeRequestDTO devopsMergeRequestDTO = new DevopsMergeRequestDTO();
        devopsMergeRequestDTO.setGitlabProjectId(appServiceDTO.getGitlabProjectId().longValue());
        List<Long> devopsOpenMergeRequestIds = devopsMergeRequestMapper.select(devopsMergeRequestDTO).stream().map(DevopsMergeRequestDTO::getGitlabMergeRequestId).collect(Collectors.toList());
        // 创建合并请求
        for (MergeRequestDTO mergeRequestDTO : mergeRequestDTOS) {
            if (!devopsOpenMergeRequestIds.contains(mergeRequestDTO.getIid().longValue())) {
                syncCount = syncCount + 1;
                if (sync) {
                    DevopsMergeRequestDTO devopsMergeRequestDTOToInsert = new DevopsMergeRequestDTO();
                    devopsMergeRequestDTOToInsert.setGitlabProjectId(mergeRequestDTO.getProjectId().longValue());
                    devopsMergeRequestDTOToInsert.setGitlabMergeRequestId(mergeRequestDTO.getIid().longValue());
                    devopsMergeRequestDTOToInsert.setSourceBranch(mergeRequestDTO.getSourceBranch());
                    devopsMergeRequestDTOToInsert.setTargetBranch(mergeRequestDTO.getTargetBranch());
                    devopsMergeRequestDTOToInsert.setAuthorId(mergeRequestDTO.getAuthor().getId().longValue());
                    if (mergeRequestDTO.getAssignee() != null) {
                        devopsMergeRequestDTOToInsert.setAssigneeId(mergeRequestDTO.getAssignee().getId().longValue());
                    }
                    devopsMergeRequestDTOToInsert.setState(mergeRequestDTO.getState());
                    devopsMergeRequestDTOToInsert.setTitle(mergeRequestDTO.getTitle());
                    MapperUtil.resultJudgedInsertSelective(devopsMergeRequestMapper, devopsMergeRequestDTOToInsert, "devops.save.merge.request");
                }
            }
        }
        // 删除不存在分支
        for (Long t : devopsOpenMergeRequestIds) {
            if (!openMergeRequestIds.contains(t.intValue())) {
                syncCount = syncCount + 1;
                if (sync) {
                    devopsMergeRequestMapper.deleteByGitlabProjectIdAndMergeRequestId(appServiceDTO.getGitlabProjectId(), t);
                }
            }
        }
        return syncCount;
    }


    @NotNull
    private Page<BranchVO> listExternalBranch(PageRequest pageable, String params, AppServiceDTO applicationDTO) {
        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(applicationDTO.getExternalConfigId());
        List<BranchDTO> branchDTOS = gitlabServiceClientOperator.listExternalBranch(applicationDTO.getGitlabProjectId(), appExternalConfigDTO);
        if (branchDTOS == null) {
            return new Page<>();
        }
        List<BranchVO> branchVOS = branchDTOS.stream().map(branchDTO -> {
            BranchVO branchVO = new BranchVO();
            branchVO.setBranchName(branchDTO.getName());
            return branchVO;
        }).collect(Collectors.toList());
        if (params != null) {
            Map<String, Object> maps = TypeUtil.castMapParams(params);
            Map<String, Object> searchParam = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            if (!CollectionUtils.isEmpty(searchParam)) {
                Object branchName = searchParam.get("branchName");
                if (branchName != null) {
                    branchVOS = branchVOS.stream().filter(branchVO -> branchVO.getBranchName().contains(branchName.toString())).collect(Collectors.toList());
                }
            }

        }
        return PageUtils.createPageFromList(branchVOS, pageable);
    }
}
