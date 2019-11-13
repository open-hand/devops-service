package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.kubernetes.client.models.V1Endpoints;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.BranchSagaPayLoad;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.agile.IssueDTO;
import io.choerodon.devops.infra.dto.gitlab.BranchDTO;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.gitlab.CompareResultDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.feign.operator.AgileServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;
import io.choerodon.devops.infra.message.ResourceBundleHandler;
import io.choerodon.devops.infra.util.*;

import static io.choerodon.devops.infra.constant.KubernetesConstants.METADATA;
import static io.choerodon.devops.infra.constant.KubernetesConstants.NAME;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:44
 * Description:
 */
@Service
public class DevopsGitServiceImpl implements DevopsGitService {
    private static final String NO_COMMIT_SHA = "0000000000000000000000000000000000000000";
    private static final String REF_HEADS = "refs/heads/";
    private static final String GIT_SUFFIX = "/.git";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    private Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");

    private Map<String, ConvertK8sObjectService> userEnvSupportedResourceConverters = new HashMap<>();
    private Map<String, ConvertK8sObjectService> systemEnvSupportedResourceConverters = new HashMap<>();
    private Map<Class, HandlerObjectFileRelationsService> objectFileRelationHandlers = new HashMap<>();

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
    private MessageSource messageSource;

    @Autowired
    private List<HandlerObjectFileRelationsService> handlerObjectFileRelationsServices;
    @Autowired
    private List<ConvertK8sObjectService> convertK8sObjectServices;

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
        return TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
    }

    @Override
    public String queryUrl(Long projectId, Long appServiceId) {
        return appServiceService.getGitlabUrl(projectId, appServiceId);
    }

    @Override
    public void createTag(Long projectId, Long appServiceId, String tag, String ref, String msg, String releaseNotes) {
        appServiceService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO applicationDTO = appServiceService.baseQuery(appServiceId);
        gitlabServiceClientOperator.createTag(applicationDTO.getGitlabProjectId(), tag, ref, msg, releaseNotes, getGitlabUserId());
    }

    @Override
    public TagVO updateTag(Long projectId, Long appServiceId, String tag, String releaseNotes) {
        appServiceService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO applicationDTO = appServiceService.baseQuery(appServiceId);
        return ConvertUtils.convertObject(gitlabServiceClientOperator.updateTag(applicationDTO.getGitlabProjectId(), tag, releaseNotes, getGitlabUserId()), TagVO.class);
    }

    @Override
    public void deleteTag(Long projectId, Long appServiceId, String tag) {
        appServiceService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO applicationDTO = appServiceService.baseQuery(appServiceId);
        gitlabServiceClientOperator.deleteTag(applicationDTO.getGitlabProjectId(), tag, getGitlabUserId());
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_BRANCH,
            description = "devops创建分支", inputSchema = "{}")
    public void createBranch(Long projectId, Long appServiceId, DevopsBranchVO devopsBranchVO) {

        DevopsBranchDTO devopsBranchDTO = ConvertUtils.convertObject(devopsBranchVO, DevopsBranchDTO.class);

        checkBranchName(projectId, appServiceId, devopsBranchVO.getBranchName());

        Long gitLabUser = TypeUtil.objToLong(getGitlabUserId());
        devopsBranchDTO.setUserId(gitLabUser);
        devopsBranchDTO.setAppServiceId(appServiceId);
        devopsBranchDTO.setStatus(CommandStatus.OPERATING.getStatus());
        AppServiceDTO applicationDTO = appServiceService.baseQuery(appServiceId);
        devopsBranchDTO = devopsBranchService.baseCreate(devopsBranchDTO);
        Long devopsBranchId = devopsBranchDTO.getId();

        BranchSagaPayLoad branchSagaPayLoad = new BranchSagaPayLoad(TypeUtil.objToLong(applicationDTO.getGitlabProjectId()), devopsBranchId, devopsBranchVO.getBranchName(), devopsBranchVO.getOriginBranch());

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("project")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_BRANCH),
                builder -> builder
                        .withPayloadAndSerialize(branchSagaPayLoad)
                        .withRefId(projectId.toString()));
    }

    @Override
    public void createBranchBySaga(BranchSagaPayLoad branchSagaPayLoad) {
        try {
            BranchDTO branchDTO = gitlabServiceClientOperator.queryBranch(TypeUtil.objToInteger(branchSagaPayLoad.getGitlabProjectId()), branchSagaPayLoad.getBranchName());
            if (branchDTO.getName() == null) {
                //创建gitlab分支，并处理返回值
                branchDTO = gitlabServiceClientOperator.createBranch(
                        TypeUtil.objToInteger(branchSagaPayLoad.getGitlabProjectId()),
                        branchSagaPayLoad.getBranchName(),
                        branchSagaPayLoad.getOriginBranch(),
                        getGitlabUserId());
            }

            if (branchDTO.getCommit() == null) {
                throw new CommonException("error.branch.exist");
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
            devopsBranchDTOCreate.setLastCommitMsg(commitDTO.getMessage());
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
    public PageInfo<BranchVO> pageBranchByOptions(Long projectId, Pageable pageable, Long appServiceId, String params) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceDTO applicationDTO = appServiceService.baseQuery(appServiceId);
        // 查询用户是否在该gitlab project下
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (!baseServiceClientOperator.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectId)) {
            MemberDTO memberDTO = gitlabServiceClientOperator.getProjectMember(applicationDTO.getGitlabProjectId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO == null) {
                throw new CommonException("error.user.not.in.gitlab.project");
            }
        }

        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organizationDTO.getCode(), projectDTO.getCode(), applicationDTO.getCode());
        PageInfo<DevopsBranchDTO> devopsBranchDTOPageInfo =
                devopsBranchService.basePageBranch(appServiceId, pageable, params);
        PageInfo<BranchVO> devopsBranchVOPageInfo = ConvertUtils.convertPage(devopsBranchDTOPageInfo, BranchVO.class);

        devopsBranchVOPageInfo.setList(devopsBranchDTOPageInfo.getList().stream().map(t -> {
            IssueDTO issueDTO = null;
            if (t.getIssueId() != null) {
                issueDTO = agileServiceClientOperator.queryIssue(projectId, t.getIssueId(), organizationDTO.getId());
            }
            IamUserDTO userDTO = baseServiceClientOperator.queryUserByUserId(
                    userAttrService.queryUserIdByGitlabUserId(t.getUserId()));
            IamUserDTO commitUserDTO = baseServiceClientOperator.queryUserByUserId(
                    userAttrService.queryUserIdByGitlabUserId(t.getLastCommitUser()));
            String commitUrl = String.format("%s/commit/%s?view=parallel", path, t.getLastCommit());
            return getBranchVO(t, commitUrl, commitUserDTO, userDTO, issueDTO);
        }).collect(Collectors.toList()));
        return devopsBranchVOPageInfo;
    }

    @Override
    public DevopsBranchVO queryBranch(Long projectId, Long applicationId, String branchName) {
        DevopsBranchDTO branchDTO = devopsBranchService.baseQueryByAppAndBranchName(applicationId, branchName);
        DevopsBranchVO devopsBranchVO = ConvertUtils.convertObject(branchDTO, DevopsBranchVO.class);
        if (devopsBranchVO.getIssueId() != null) {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
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
    public void updateBranchIssue(Long projectId, Long appServiceId, DevopsBranchUpdateVO devopsBranchUpdateVO) {
        devopsBranchService.updateBranchIssue(ConvertUtils.convertObject(devopsBranchUpdateVO, DevopsBranchDTO.class));
    }

    @Override
    public void deleteBranch(Long appServiceId, String branchName) {
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        List<BranchDTO> branchDTOS = gitlabServiceClientOperator.listBranch(appServiceDTO.getGitlabProjectId(),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        // 不能删除仓库下最后一个分支
        if (branchDTOS.size() <= 1) {
            throw new CommonException("error.delete.the.only.branch");
        }
        Optional<BranchDTO> branchDTO = branchDTOS
                .stream().filter(e -> branchName.equals(e.getName())).findFirst();
        branchDTO.ifPresent(e -> {
            if (Boolean.TRUE.equals(e.getProtected())) {
                // 不能删除保护分支
                throw new CommonException("error.delete.protected.branch");
            } else {
                gitlabServiceClientOperator.deleteBranch(appServiceDTO.getGitlabProjectId(), branchName,
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        });
        devopsBranchService.baseDelete(appServiceId, branchName);
    }


    @Override
    public MergeRequestTotalVO listMergeRequest(Long projectId, Long appServiceId, String state, Pageable pageable) {
        appServiceService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        if (appServiceDTO.getGitlabProjectId() == null) {
            throw new CommonException("error.gitlabProjectId.not.exists");
        }

        PageInfo<DevopsMergeRequestDTO> devopsMergeRequestDTOPageInfo = devopsMergeRequestService
                .basePageByOptions(appServiceDTO.getGitlabProjectId(), state, pageable);

        List<MergeRequestVO> pageContent = new ArrayList<>();
        List<DevopsMergeRequestDTO> devopsMergeRequestDTOS = devopsMergeRequestDTOPageInfo.getList();

        //设置每个合并请求下关联的commit
        if (devopsMergeRequestDTOS != null && !devopsMergeRequestDTOS.isEmpty()) {
            devopsMergeRequestDTOS.forEach(content -> {
                MergeRequestVO mergeRequestVO = devopsMergeRequestToMergeRequest(content);
                if (mergeRequestVO != null) {
                    pageContent.add(mergeRequestVO);
                }
            });
        }
        PageInfo<MergeRequestVO> mergeRequestVOPageInfo = ConvertUtils.convertPage(devopsMergeRequestDTOPageInfo, MergeRequestVO.class);
        mergeRequestVOPageInfo.setList(pageContent);

        //查询某个应用代码仓库各种状态合并请求的数量
        DevopsMergeRequestDTO devopsMergeRequestDTO = devopsMergeRequestService.baseCountMergeRequest(appServiceDTO.getGitlabProjectId());

        MergeRequestTotalVO mergeRequestTotalVO = new MergeRequestTotalVO();
        mergeRequestTotalVO.setMergeRequestVOPageInfo(mergeRequestVOPageInfo);
        mergeRequestTotalVO.setTotalCount(devopsMergeRequestDTO.getTotal());
        mergeRequestTotalVO.setCloseCount(devopsMergeRequestDTO.getClosed());
        mergeRequestTotalVO.setMergeCount(devopsMergeRequestDTO.getMerged());
        mergeRequestTotalVO.setOpenCount(devopsMergeRequestDTO.getOpened());

        return mergeRequestTotalVO;
    }

    @Override
    public PageInfo<TagVO> pageTagsByOptions(Long projectId, Long applicationId, String params, Integer page, Integer size) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        AppServiceDTO applicationDTO = appServiceService.baseQuery(applicationId);
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organizationDTO.getCode(), projectDTO.getCode(), applicationDTO.getCode());
        return ConvertUtils.convertPage(gitlabServiceClientOperator.pageTag(projectDTO, applicationDTO.getGitlabProjectId(), path, page, params, size, getGitlabUserId()), TagVO.class);
    }

    @Override
    public List<TagVO> listTags(Long projectId, Long applicationId) {
        AppServiceDTO applicationDTO = appServiceService.baseQuery(applicationId);
        return ConvertUtils.convertList(gitlabServiceClientOperator.listTags(applicationDTO.getGitlabProjectId(), getGitlabUserId()), TagVO.class);
    }

    @Override
    public Boolean checkTag(Long projectId, Long applicationId, String tagName) {
        AppServiceDTO applicationDTO = appServiceService.baseQuery(applicationId);
        return gitlabServiceClientOperator.listTags(applicationDTO.getGitlabProjectId(), getGitlabUserId()).stream()
                .noneMatch(t -> tagName.equals(t.getName()));
    }


    @Override
    public void branchSync(PushWebHookVO pushWebHookVO, String token) {
        AppServiceDTO applicationDTO = appServiceService.baseQueryByToken(token);
        if (NO_COMMIT_SHA.equals(pushWebHookVO.getBefore())) {
            createBranchSync(pushWebHookVO, applicationDTO.getId());
            devopsGitlabCommitService.create(pushWebHookVO, token);
        } else if (NO_COMMIT_SHA.equals(pushWebHookVO.getAfter())) {
            deleteBranchSync(pushWebHookVO, applicationDTO.getId());
        } else {
            commitBranchSync(pushWebHookVO, applicationDTO.getId());
            devopsGitlabCommitService.create(pushWebHookVO, token);

        }
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_SYNC_GITOPS, description = "devops同步gitops库相关操作", inputSchemaClass = PushWebHookVO.class)
    public void fileResourceSyncSaga(PushWebHookVO pushWebHookVO, String token) {
        LOGGER.info("````````````````````````````` {}", pushWebHookVO.getCheckoutSha());

        Long userId = userAttrService.baseQueryUserIdByGitlabUserId(TypeUtil.objToLong(pushWebHookVO.getUserId()));
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);

        if (iamUserDTO != null) {
            CustomContextUtil.setUserContext(iamUserDTO.getLoginName(), userId, iamUserDTO.getOrganizationId());
        }

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
            if (devopsEnvCommitService
                    .baseQueryByEnvIdAndCommit(devopsEnvironmentDTO.getId(), commitDTO.getId()) == null) {
                devopsEnvCommitService.baseCreate(devopsEnvCommitDTO);
            }
        });
        DevopsEnvCommitDTO devopsEnvCommitDTO = devopsEnvCommitService
                .baseQueryByEnvIdAndCommit(devopsEnvironmentDTO.getId(), pushWebHookVO.getCheckoutSha());
        devopsEnvironmentDTO.setSagaSyncCommit(devopsEnvCommitDTO.getId());
        devopsEnvironmentService.baseUpdateSagaSyncEnvCommit(devopsEnvironmentDTO);
        LOGGER.info("update devopsCommit successfully: {}", pushWebHookVO.getCheckoutSha());

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_SYNC_GITOPS),
                builder -> builder
                        .withPayloadAndSerialize(pushWebHookVO)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void fileResourceSync(PushWebHookVO pushWebHookVO) {
        final Integer gitLabProjectId = pushWebHookVO.getProjectId();
        Integer gitLabUserId = pushWebHookVO.getUserId();
        Long userId = userAttrService.baseQueryUserIdByGitlabUserId(TypeUtil.objToLong(gitLabUserId));
        if (userId == null) {
            gitLabUserId = 1;
        }

        List<String> operationFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        Set<DevopsEnvFileResourceDTO> beforeSync = new HashSet<>();
        Set<DevopsEnvFileResourceDTO> beforeSyncDelete = new HashSet<>();
        //根据token查出环境

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.queryByTokenWithClusterCode(pushWebHookVO.getToken());
        DevopsEnvCommitDTO devopsEnvCommitDTO = devopsEnvCommitService.baseQuery(devopsEnvironmentDTO.getSagaSyncCommit());
        boolean tagNotExist;
        Map<String, String> objectPath;
        //从iam服务中查出项目和组织code
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //本地路径
        final String path = GitOpsUtil.getLocalPathToStoreEnv(organizationDTO.getCode(),
                projectDTO.getCode(), devopsEnvironmentDTO.getClusterCode(), devopsEnvironmentDTO.getCode());
        //生成环境git仓库ssh地址
        final String url = GitUtil.getGitlabSshUrl(
                pattern, gitlabSshUrl, organizationDTO.getCode(), projectDTO.getCode(),
                devopsEnvironmentDTO.getCode(),
                EnvironmentType.forValue(devopsEnvironmentDTO.getType()),
                devopsEnvironmentDTO.getClusterCode());

        LOGGER.info("The gitOps Repository ssh url: {}", url);

        final Long envId = devopsEnvironmentDTO.getId();

        final Long projectId = devopsEnvironmentDTO.getProjectId();

        try {
            //更新本地库到最新提交
            Git git = handDevopsEnvGitRepository(path, url, devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvCommitDTO.getCommitSha());
            LOGGER.info("更新gitops库成功");
            //查询devops-sync tag是否存在，存在则比较tag和最新commit的diff，不存在则识别gitops库下所有文件为新增文件
            tagNotExist = getDevopsSyncTag(pushWebHookVO);

            if (tagNotExist) {
                operationFiles.addAll(FileUtil.getFilesPath(path));
                operationFiles.forEach(file -> {
                    List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService
                            .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), file);
                    if (!devopsEnvFileResourceDTOS.isEmpty()) {
                        beforeSync.addAll(devopsEnvFileResourceDTOS);
                    }
                });
            } else {
                handleDiffs(gitLabProjectId, operationFiles, deletedFiles, beforeSync, beforeSyncDelete,
                        devopsEnvironmentDTO, devopsEnvCommitDTO);
            }

            Map<Class, List> resourceKindMap = initResourceKindContainer();

            //从文件中读出对象,序列化为K8S对象
            objectPath = convertFileToK8sObjects(operationFiles, path,
                    EnvironmentType.forValue(devopsEnvironmentDTO.getType()),
                    resourceKindMap, devopsEnvironmentDTO.getId(),
                    new ArrayList<>(beforeSyncDelete));

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
            //处理文件
            handleFiles(operationFiles, deletedFiles, devopsEnvironmentDTO, devopsEnvCommitDTO, path);

            //删除tag

            handleTag(git,devopsEnvironmentDTO.getEnvIdRsa(),pushWebHookVO, devopsEnvCommitDTO, tagNotExist);

            devopsEnvironmentDTO.setDevopsSyncCommit(devopsEnvCommitDTO.getId());
            //更新环境 解释commit
            devopsEnvironmentService.baseUpdateDevopsSyncEnvCommit(devopsEnvironmentDTO);
            //向agent发送同步指令
            agentCommandService.sendCommand(devopsEnvironmentDTO);
            LOGGER.info("发送gitops同步成功指令成功");
        } catch (CommonException e) {
            String filePath = "";
            String errorCode = "";
            if (e instanceof GitOpsExplainException) {
                filePath = ((GitOpsExplainException) e).getFilePath();
                errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e)
                        .getErrorCode();
            }
            DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = getDevopsFileError(envId, filePath, path);
            String error;
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception parameters: {}", Arrays.toString(e.getParameters()));
                }
                error = messageSource.getMessage(e.getMessage(), e.getParameters(), GitOpsUtil.locale());
            } catch (Exception e1) {
                LOGGER.debug("Exception occurred when read message from message source. The original message is {}. The exception is : {}",e.getMessage(), e1);
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
    public void checkBranchName(Long projectId, Long applicationId, String branchName) {
        AppServiceDTO applicationDTO = appServiceService.baseQuery(applicationId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        List<BranchDTO> branchDTOS = gitlabServiceClientOperator.listBranch(applicationDTO.getGitlabProjectId(),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        Optional<BranchDTO> branchEOptional = branchDTOS
                .stream().filter(e -> branchName.equals(e.getName())).findFirst();
        branchEOptional.ifPresent(e -> {
            throw new CommonException("error.branch.exist");
        });
    }

    private void handleFiles(List<String> operationFiles, List<String> deletedFiles,
                             DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsEnvCommitDTO devopsEnvCommitDTO, String path) {
        //新增解释文件记录
        for (String filePath : operationFiles) {
            DevopsEnvFileDTO devopsEnvFileDTO = devopsEnvFileService
                    .baseQueryByEnvAndPath(devopsEnvironmentDTO.getId(), filePath);
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

    private void handleTag(Git git, String sshKey, PushWebHookVO pushWebHookVO,
                           DevopsEnvCommitDTO devopsEnvCommitDTO, Boolean tagNotExist) {
        if (tagNotExist) {
            GitUtil.createTag(git, sshKey, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
        } else {
            GitUtil.pushTag(git, sshKey, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
            if (getDevopsSyncTag(pushWebHookVO)) {
                GitUtil.createTag(git, sshKey, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
            }
        }
    }


    private void handleDiffs(Integer gitLabProjectId, List<String> operationFiles, List<String> deletedFiles,
                             Set<DevopsEnvFileResourceDTO> beforeSync, Set<DevopsEnvFileResourceDTO> beforeSyncDelete,
                             DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsEnvCommitDTO devopsEnvCommitDTO) {
        //获取将此次最新提交与tag作比价得到diff
        CompareResultDTO compareResultDTO = gitlabServiceClientOperator
                .queryCompareResult(gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha());
        compareResultDTO.getDiffs().forEach(t -> {
            if (t.getNewPath().contains("yaml") || t.getNewPath().contains("yml")) {
                if (t.getDeletedFile()) {
                    // t.getNewPath() 而不是t.getOldPath()，这里能用是因为删除的文件的两个的值一致
                    deletedFiles.add(t.getNewPath());
                } else if (t.getRenamedFile()) {
                    deletedFiles.add(t.getOldPath());
                    operationFiles.add(t.getNewPath());
                } else {
                    operationFiles.add(t.getNewPath());
                }
            }

            List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), t.getOldPath());
            if (!devopsEnvFileResourceDTOS.isEmpty()) {
                beforeSync.addAll(devopsEnvFileResourceDTOS);
            }
        });

        deletedFiles.forEach(file -> {
            List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), file);
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
    private Map<String, String> convertFileToK8sObjects(List<String> files,
                                                        String path,
                                                        EnvironmentType environmentType,
                                                        Map<Class, List> resourceContainer,
                                                        Long envId,
                                                        List<DevopsEnvFileResourceDTO> beforeSyncDelete) {
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
                if (ResourceType.PERSISTENT_VOLUME_CLAIM.getType().equals(type)
                        && isPvcTreatedAsCustomizeResourceBefore(envId, getPersistentVolumeClaimName(jsonObject, filePath))) {
                    // 0.20版本之前被作为自定义资源解析的PVC仍然作为自定义资源看待
                    currentHandler = converters.get(ResourceType.MISSTYPE.getType());
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
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_NAME_NOT_FOUND.getError(), filePath);
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
            Optional<CommitVO> lastCommitOptional
                    = pushWebHookVO.getCommits().stream().filter(t -> lastCommit.equals(t.getId())).findFirst();
            CommitVO lastCommitDTO = new CommitVO();
            if (lastCommitOptional.isPresent()) {
                lastCommitDTO = lastCommitOptional.get();
            }

            devopsBranchDTO.setLastCommit(lastCommit);
            devopsBranchDTO.setLastCommitDate(lastCommitDTO.getTimestamp());
            devopsBranchDTO.setLastCommitMsg(lastCommitDTO.getMessage());
            devopsBranchDTO.setLastCommitUser(pushWebHookVO.getUserId().longValue());
            devopsBranchService.baseUpdateBranchLastCommit(devopsBranchDTO);
        } catch (Exception e) {
            LOGGER.info("error.update.branch");
        }

    }


    private MergeRequestVO devopsMergeRequestToMergeRequest(DevopsMergeRequestDTO
                                                                    devopsMergeRequestDTO) {
        MergeRequestVO mergeRequestVO = ConvertUtils.convertObject(devopsMergeRequestDTO, MergeRequestVO.class);
        mergeRequestVO.setProjectId(devopsMergeRequestDTO.getGitlabProjectId().intValue());
        mergeRequestVO.setId(devopsMergeRequestDTO.getId().intValue());
        mergeRequestVO.setIid(devopsMergeRequestDTO.getGitlabMergeRequestId().intValue());
        Long authorUserId = userAttrService
                .queryUserIdByGitlabUserId(devopsMergeRequestDTO.getAuthorId());
        Long assigneeId = userAttrService
                .queryUserIdByGitlabUserId(devopsMergeRequestDTO.getAssigneeId());
        List<CommitDTO> commitDTOS;
        try {
            commitDTOS = gitlabServiceClientOperator.listCommits(
                    devopsMergeRequestDTO.getGitlabProjectId().intValue(),
                    devopsMergeRequestDTO.getGitlabMergeRequestId().intValue(), getGitlabUserId());
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
            authorVO.setUsername(authorUser.getLdap() ? authorUser.getLoginName() : authorUser.getEmail());
            authorVO.setName(authorUser.getRealName());
            authorVO.setId(authorUser.getId() == null ? null : authorUser.getId().intValue());
            authorVO.setWebUrl(authorUser.getImageUrl());
            mergeRequestVO.setAuthor(authorVO);
        }
        IamUserDTO assigneeUser = baseServiceClientOperator.queryUserByUserId(assigneeId);
        if (assigneeUser != null) {
            AssigneeVO assigneeVO = new AssigneeVO();
            if (assigneeUser.getLdap()) {
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


    private BranchVO getBranchVO(DevopsBranchDTO devopsBranchDTO, String lastCommitUrl, IamUserDTO
            commitUserDTO, IamUserDTO userDTO,
                                 IssueDTO issue) {
        String createUserUrl = null;
        String createUserName = null;
        String createUserRealName = null;
        if (userDTO != null) {
            if (userDTO.getLdap()) {
                createUserName = userDTO.getLoginName();
            } else {
                createUserName = userDTO.getEmail();
            }
            createUserUrl = userDTO.getImageUrl();
            createUserRealName = userDTO.getRealName();
        }
        return new BranchVO(
                devopsBranchDTO,
                lastCommitUrl,
                createUserUrl,
                issue,
                commitUserDTO,
                createUserName,
                createUserRealName,
                devopsBranchDTO.getStatus(),
                devopsBranchDTO.getErrorMessage());
    }

    private void deleteBranchSync(PushWebHookVO pushWebHookVO, Long appServiceId) {
        try {
            String branchName = pushWebHookVO.getRef().replaceFirst(REF_HEADS, "");
            devopsBranchService.baseDelete(appServiceId, branchName);
        } catch (Exception e) {
            LOGGER.info("error.devops.branch.delete");
        }
    }

    private void createBranchSync(PushWebHookVO pushWebHookVO, Long appServiceId) {
        try {
            String lastCommit = pushWebHookVO.getAfter();
            Long userId = pushWebHookVO.getUserId().longValue();

            CommitDTO commitDTO = gitlabServiceClientOperator.queryCommit(

                    pushWebHookVO.getProjectId(),
                    lastCommit,
                    userId.intValue());
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
                devopsBranchDTO.setLastCommitMsg(commitDTO.getMessage());

                devopsBranchDTO.setLastCommitDate(commitDTO.getCommittedDate());
                devopsBranchService.baseCreate(devopsBranchDTO);


            }
        } catch (Exception e) {
            LOGGER.info("error.create.branch");
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

        return gitlabServiceClientOperator.listTags(pushWebHookVO.getProjectId(), pushWebHookVO.getUserId())
                .stream().noneMatch(tagDO -> tagDO.getName().equals(GitUtil.DEV_OPS_SYNC_TAG));

    }

    private Git handDevopsEnvGitRepository(String path, String url, String envIdRsa, String commit) {
        File file = new File(path);
        if (!file.exists()) {
            return gitUtil.cloneBySsh(path, url, envIdRsa);
        } else {
            if (file.isDirectory() && file.listFiles().length > 0) {
                String localPath = String.format("%s%s", path, GIT_SUFFIX);
                return GitUtil.pullBySsh(localPath, envIdRsa);
            } else {
               return gitUtil.cloneBySsh(path, url, envIdRsa);
            }
        }
    }


    @Override
    public BranchDTO baseQueryBranch(Integer gitLabProjectId, String branchName) {
        return gitlabServiceClientOperator.queryBranch(gitLabProjectId, branchName);
    }


}
