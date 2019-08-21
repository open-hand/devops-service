package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.C7nCertification;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.app.eventhandler.DemoEnvSetupSagaHandler;
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
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.feign.operator.AgileServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.message.ResourceBundleHandler;
import io.choerodon.devops.infra.util.*;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.yaml.snakeyaml.Yaml;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:44
 * Description:
 */
@Service
public class DevopsGitServiceImpl implements DevopsGitService {
    public static final String CHOERODON_IO_RESOURCE = "choerodon.io/resource";
    public static final String METADATA = "metadata";
    public static final String CUSTOM = "custom";
    public static final String LABELS = "labels";
    public static final String KIND = "kind";
    public static final String NAME = "name";
    private static final String SERVICE = "Service";
    private static final String INGRESS = "Ingress";
    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    private static final String CERTIFICATE = "Certificate";
    private static final String CONFIGMAP = "ConfigMap";
    private static final String ENDPOINTS = "Endpoints";
    private static final String SECRET = "Secret";
    private static final String NO_COMMIT_SHA = "0000000000000000000000000000000000000000";
    private static final String REF_HEADS = "refs/heads/";
    private static final String GIT_SUFFIX = "/.git";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    private Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private AppServiceService applicationService;
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
    @Qualifier("handlerC7nReleaseRelationsServiceImpl")
    private HandlerObjectFileRelationsService handlerC7nReleaseRelationsService;
    @Autowired
    @Qualifier("handlerServiceRelationsServiceImpl")
    private HandlerObjectFileRelationsService handlerServiceRelationsService;
    @Autowired
    @Qualifier("handlerIngressRelationsServiceImpl")
    private HandlerObjectFileRelationsService handlerIngressRelationsService;
    @Autowired
    @Qualifier("handlerC7nCertificationServiceImpl")
    private HandlerObjectFileRelationsService handlerC7nCertificationRelationsService;
    @Autowired
    @Qualifier("handlerConfigMapRelationsServiceImpl")
    private HandlerObjectFileRelationsService handlerConfigMapRelationsService;
    @Autowired
    @Qualifier("handlerC7nSecretServiceImpl")
    private HandlerObjectFileRelationsService handlerC7nSecretRelationsService;
    @Autowired
    @Qualifier("handlerCustomResourceServiceImpl")
    private HandlerObjectFileRelationsService handlerCustomResourceService;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;

    private Integer getGitlabUserId() {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
    }

    @Override
    public String queryUrl(Long projectId, Long appServiceId) {
        return applicationService.getGitlabUrl(projectId, appServiceId);
    }

    @Override
    public void createTag(Long projectId, Long appServiceId, String tag, String ref, String msg, String releaseNotes) {
        applicationService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceId);
        gitlabServiceClientOperator.createTag(applicationDTO.getGitlabProjectId(), tag, ref, msg, releaseNotes, getGitlabUserId());
    }

    @Override
    public TagVO updateTag(Long projectId, Long appServiceId, String tag, String releaseNotes) {
        applicationService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceId);
        return ConvertUtils.convertObject(gitlabServiceClientOperator.updateTag(applicationDTO.getGitlabProjectId(), tag, releaseNotes, getGitlabUserId()), TagVO.class);
    }

    @Override
    public void deleteTag(Long projectId, Long appServiceId, String tag) {
        applicationService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceId);
        gitlabServiceClientOperator.deleteTag(applicationDTO.getGitlabProjectId(), tag, getGitlabUserId());
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_BRANCH,
            description = "devops创建分支", inputSchema = "{}")
    public void createBranch(Long projectId, Long applicationId, DevopsBranchVO devopsBranchVO) {

        DevopsBranchDTO devopsBranchDTO = ConvertUtils.convertObject(devopsBranchVO, DevopsBranchDTO.class);

        checkBranchName(projectId, applicationId, devopsBranchVO.getBranchName());

        Long gitLabUser = TypeUtil.objToLong(getGitlabUserId());
        devopsBranchDTO.setUserId(gitLabUser);
        devopsBranchDTO.setAppServiceId(applicationId);
        devopsBranchDTO.setStatus(CommandStatus.OPERATING.getStatus());
        AppServiceDTO applicationDTO = applicationService.baseQuery(applicationId);
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
            Long commitUserId = null;
            if (commitDTO.getCommitterName().equals("root")) {
                UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserName("admin");
                if (userAttrDTO == null) {
                    userAttrDTO = userAttrService.baseQueryByGitlabUserName("admin1");
                }
                commitUserId = userAttrDTO.getGitlabUserId();
            }
            devopsBranchDTOCreate.setLastCommitUser(commitUserId);
            devopsBranchService.baseUpdateBranch(devopsBranchDTOCreate);
        } catch (Exception e) {
            DevopsBranchDTO devopsBranchDTOCreate = devopsBranchService.baseQuery(branchSagaPayLoad.getDevopsBranchId());
            devopsBranchDTOCreate.setStatus(CommandStatus.FAILED.getStatus());
            devopsBranchDTOCreate.setErrorMessage(e.getMessage());
            devopsBranchService.baseUpdateBranch(devopsBranchDTOCreate);
        }
    }

    @Override
    public PageInfo<BranchVO> pageBranchByOptions(Long projectId, PageRequest pageRequest, Long appServiceId, String params) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceId);
        // 查询用户是否在该gitlab project下
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (!baseServiceClientOperator.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO)) {
            MemberDTO memberDTO = gitlabServiceClientOperator.getProjectMember(applicationDTO.getGitlabProjectId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO == null) {
                throw new CommonException("error.user.not.in.project");
            }
        }

        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organizationDTO.getCode(), projectDTO.getCode(), applicationDTO.getCode());
        PageInfo<DevopsBranchDTO> devopsBranchDTOPageInfo =
                devopsBranchService.basePageBranch(appServiceId, pageRequest, params);
        PageInfo<BranchVO> devopsBranchVOPageInfo = ConvertUtils.convertPage(devopsBranchDTOPageInfo, BranchVO.class);

        devopsBranchVOPageInfo.setList(devopsBranchDTOPageInfo.getList().stream().map(t -> {
            IssueDTO issueDTO = null;
            if (t.getIssueId() != null) {
                issueDTO = agileServiceClientOperator.queryIssue(projectId, t.getIssueId(), organizationDTO.getId());
            }
            IamUserDTO userDTO = baseServiceClientOperator.queryUserByUserId(
                    userAttrService.getUserIdByGitlabUserId(t.getUserId()));
            IamUserDTO commitUserDTO = baseServiceClientOperator.queryUserByUserId(
                    userAttrService.getUserIdByGitlabUserId(t.getLastCommitUser()));
            String commitUrl = String.format("%s/commit/%s?view=parallel", path, t.getLastCommit());
            return getBranchVO(t, commitUrl, commitUserDTO, userDTO, issueDTO);
        }).collect(Collectors.toList()));
        return devopsBranchVOPageInfo;
    }

    @Override
    public DevopsBranchVO queryBranch(Long projectId, Long applicationId, String branchName) {
        return ConvertUtils.convertObject(devopsBranchService.baseQueryByAppAndBranchName(applicationId, branchName), DevopsBranchVO.class);
    }

    @Override
    public void updateBranchIssue(Long projectId, Long applicationId, DevopsBranchVO devopsBranchVO) {
        DevopsBranchDTO devopsBranchDTO = ConvertUtils.convertObject(devopsBranchVO, DevopsBranchDTO.class);
        devopsBranchService.baseUpdateBranchIssue(applicationId, devopsBranchDTO);
    }

    @Override
    public void deleteBranch(Long applicationId, String branchName) {
        AppServiceDTO applicationDTO = applicationService.baseQuery(applicationId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        List<BranchDTO> branchDTOS = gitlabServiceClientOperator.listBranch(applicationDTO.getGitlabProjectId(),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        Optional<BranchDTO> branchDTO = branchDTOS
                .stream().filter(e -> branchName.equals(e.getName())).findFirst();
        branchDTO.ifPresent(e -> gitlabServiceClientOperator.deleteBranch(applicationDTO.getGitlabProjectId(), branchName,
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
        devopsBranchService.baseDelete(applicationId, branchName);
    }


    @Override
    public MergeRequestTotalVO listMergeRequest(Long projectId, Long appServiceId, String state, PageRequest pageRequest) {
        applicationService.baseCheckApp(projectId, appServiceId);
        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceId);
        if (appServiceDTO.getGitlabProjectId() == null) {
            throw new CommonException("error.gitlabProjectId.not.exists");
        }

        //查询某个应用代码仓库各种状态合并请求的数量
        DevopsMergeRequestDTO devopsMergeRequestDTO = devopsMergeRequestService.baseCountMergeRequest(appServiceDTO.getGitlabProjectId());

        PageInfo<DevopsMergeRequestDTO> devopsMergeRequestDTOPageInfo = devopsMergeRequestService
                .basePageByOptions(appServiceDTO.getGitlabProjectId(), state, pageRequest);

        List<MergeRequestVO> pageContent = new ArrayList<>();
        List<DevopsMergeRequestDTO> devopsMergeRequestDTOS = devopsMergeRequestDTOPageInfo.getList();

        //设置每个合并请求下关联的commit
        if (devopsMergeRequestDTOS != null && !devopsMergeRequestDTOS.isEmpty()) {
            devopsMergeRequestDTOS.forEach(content -> {
                MergeRequestVO mergeRequestVO = devopsMergeRequestToMergeRequest(content);
                pageContent.add(mergeRequestVO);
            });
        }
        PageInfo<MergeRequestVO> mergeRequestVOPageInfo = ConvertUtils.convertPage(devopsMergeRequestDTOPageInfo, MergeRequestVO.class);
        mergeRequestVOPageInfo.setList(pageContent);

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
        AppServiceDTO applicationDTO = applicationService.baseQuery(applicationId);
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organizationDTO.getCode(), projectDTO.getCode(), applicationDTO.getCode());
        return ConvertUtils.convertPage(gitlabServiceClientOperator.pageTag(applicationDTO.getGitlabProjectId(), path, page, params, size, getGitlabUserId()), TagVO.class);
    }

    @Override
    public List<TagVO> listTags(Long projectId, Long applicationId) {
        AppServiceDTO applicationDTO = applicationService.baseQuery(applicationId);
        return ConvertUtils.convertList(gitlabServiceClientOperator.listTag(applicationDTO.getGitlabProjectId(), getGitlabUserId()), TagVO.class);
    }

    @Override
    public Boolean checkTag(Long projectId, Long applicationId, String tagName) {
        AppServiceDTO applicationDTO = applicationService.baseQuery(applicationId);
        return gitlabServiceClientOperator.listTag(applicationDTO.getGitlabProjectId(), getGitlabUserId()).stream()
                .noneMatch(t -> tagName.equals(t.getName()));
    }


    @Override
    public void branchSync(PushWebHookVO pushWebHookVO, String token) {
        AppServiceDTO applicationDTO = applicationService.baseQueryByToken(token);
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
        LOGGER.info(String.format("````````````````````````````` %s", pushWebHookVO.getCheckoutSha()));

        Long userId = userAttrService.baseQueryUserIdByGitlabUserId(TypeUtil.objToLong(pushWebHookVO.getUserId()));
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);

        DemoEnvSetupSagaHandler.beforeInvoke(iamUserDTO.getLoginName(),userId, iamUserDTO.getOrganizationId());


        pushWebHookVO.setToken(token);
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByToken(pushWebHookVO.getToken());
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
        LOGGER.info(String.format("update devopsCommit successfully: %s", pushWebHookVO.getCheckoutSha()));

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

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByToken(pushWebHookVO.getToken());
        DevopsEnvCommitDTO devopsEnvCommitDTO = devopsEnvCommitService.baseQuery(devopsEnvironmentDTO.getSagaSyncCommit());
        boolean tagNotExist;
        Map<String, String> objectPath;
        //从iam服务中查出项目和组织code
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //本地路径
        final String path = String.format("gitops/%s/%s/%s",
                organizationDTO.getCode(), projectDTO.getCode(), devopsEnvironmentDTO.getCode());
        //生成环境git仓库ssh地址
        final String url = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organizationDTO.getCode(), projectDTO.getCode(),
                devopsEnvironmentDTO.getCode());

        LOGGER.info("The gitOps Repository ssh url: {}", url);

        final Long envId = devopsEnvironmentDTO.getId();

        final Long projectId = devopsEnvironmentDTO.getProjectId();

        try {
            //更新本地库到最新提交
            handDevopsEnvGitRepository(path, url, devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvCommitDTO.getCommitSha());
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
            List<C7nHelmRelease> c7nHelmReleases = new ArrayList<>();
            List<V1Service> v1Services = new ArrayList<>();
            List<V1beta1Ingress> v1beta1Ingresses = new ArrayList<>();
            List<C7nCertification> c7nCertifications = new ArrayList<>();
            List<V1ConfigMap> v1ConfigMaps = new ArrayList<>();
            List<V1Secret> v1Secrets = new ArrayList<>();
            List<V1Endpoints> v1Endpoints = new ArrayList<>();
            List<DevopsCustomizeResourceDTO> devopsCustomizeResourceDTOS = new ArrayList<>();


            //从文件中读出对象,序列化为K8S对象
            objectPath = convertFileToK8sObjects(operationFiles, path, c7nHelmReleases, v1Services, v1beta1Ingresses,
                    v1ConfigMaps, v1Secrets, v1Endpoints, devopsCustomizeResourceDTOS, devopsEnvironmentDTO.getId(), new ArrayList<>(beforeSyncDelete),
                    c7nCertifications);

            LOGGER.info("序列化k8s对象成功！");

            List<DevopsEnvFileResourceDTO> beforeSyncFileResource = new ArrayList<>(beforeSync);

            //将k8s对象初始化为实例，网络，域名，证书，秘钥对象,处理对象文件关系
            handlerC7nReleaseRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, c7nHelmReleases, null, envId, projectId, path,
                            userId);

            handlerServiceRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, v1Services, v1Endpoints, envId, projectId, path, userId);

            handlerIngressRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, v1beta1Ingresses, null, envId, projectId, path,
                            userId);

            handlerC7nCertificationRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, c7nCertifications, null, envId, projectId, path,
                            userId);

            handlerConfigMapRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, v1ConfigMaps, null, envId, projectId, path, userId);

            handlerC7nSecretRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, v1Secrets, null, envId, projectId, path, userId);


            handlerCustomResourceService.handlerRelations(objectPath, beforeSyncFileResource, devopsCustomizeResourceDTOS, null, envId, projectId, path, userId);
            LOGGER.info("k8s对象转换平台对象成功！");
            //处理文件
            handleFiles(operationFiles, deletedFiles, devopsEnvironmentDTO, devopsEnvCommitDTO, path);

            //删除tag

            handleTag(pushWebHookVO, gitLabProjectId, gitLabUserId, devopsEnvCommitDTO, tagNotExist);

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
                error = ResourceBundleHandler.getInstance().getValue(e.getMessage());
            } catch (Exception e1) {
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

    @Override
    public void checkBranchName(Long projectId, Long applicationId, String branchName) {
        AppServiceDTO applicationDTO = applicationService.baseQuery(applicationId);
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

    private void handleTag(PushWebHookVO pushWebHookVO, Integer gitLabProjectId, Integer gitLabUserId,

                           DevopsEnvCommitDTO devopsEnvCommitDTO, Boolean tagNotExist) {
        if (tagNotExist) {
            gitlabServiceClientOperator.createTag(
                    gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha(),
                    "", "", gitLabUserId);
        } else {
            try {
                gitlabServiceClientOperator.deleteTag(gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, gitLabUserId);
            } catch (CommonException e) {
                if (getDevopsSyncTag(pushWebHookVO)) {
                    gitlabServiceClientOperator.createTag(
                            gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha(),
                            "", "", gitLabUserId);
                }
            }

            //创建新tag
            if (getDevopsSyncTag(pushWebHookVO)) {
                gitlabServiceClientOperator.createTag(
                        gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitDTO.getCommitSha(),
                        "", "", gitLabUserId);
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

    private Map<String, String> convertFileToK8sObjects(List<String> files,
                                                        String path,
                                                        List<C7nHelmRelease> c7nHelmReleases,
                                                        List<V1Service> v1Services,
                                                        List<V1beta1Ingress> v1beta1Ingresses,
                                                        List<V1ConfigMap> configMaps,
                                                        List<V1Secret> secrets,
                                                        List<V1Endpoints> v1Endpoints,
                                                        List<DevopsCustomizeResourceDTO> devopsCustomizeResourceDTOS,
                                                        Long envId,
                                                        List<DevopsEnvFileResourceDTO> beforeSyncDelete,
                                                        List<C7nCertification> c7nCertifications) {
        Map<String, String> objectPath = new HashMap<>();

        files.forEach(filePath -> {
            Yaml yaml = new Yaml();
            File file = new File(String.format("%s/%s", path, filePath));
            try {
                for (Object data : yaml.loadAll(new FileInputStream(file))) {
                    JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
                    String type = jsonObject.get("kind").toString();
                    switch (type) {
                        case C7NHELM_RELEASE:
                            //反序列文件为c7nHelmRelease对象,
                            ConvertK8sObjectService<C7nHelmRelease> convertC7nHelmRelease = new ConvertC7nHelmReleaseServiceImpl();
                            convertC7nHelmRelease.setT(new C7nHelmRelease());
                            C7nHelmRelease c7nHelmRelease = convertC7nHelmRelease
                                    .serializableObject(jsonObject.toJSONString(), filePath, objectPath);
                            //校验参数校验参数是否合法
                            convertC7nHelmRelease.checkParameters(c7nHelmRelease, objectPath);
                            //校验对象是否在其它文件中已经定义
                            convertC7nHelmRelease
                                    .checkIfExist(c7nHelmReleases, envId, beforeSyncDelete, objectPath, c7nHelmRelease);
                            break;
                        case INGRESS:
                            //反序列文件为V1beta1ingress对象,
                            ConvertK8sObjectService<V1beta1Ingress> convertV1beta1Ingress = new ConvertV1beta1IngressServiceImpl();
                            convertV1beta1Ingress.setT(new V1beta1Ingress());
                            V1beta1Ingress v1beta1Ingress = convertV1beta1Ingress
                                    .serializableObject(jsonObject.toJSONString(), filePath, objectPath);
                            //校验参数校验参数是否合法
                            convertV1beta1Ingress.checkParameters(v1beta1Ingress, objectPath);
                            //校验对象是否在其它文件中已经定义
                            convertV1beta1Ingress.checkIfExist(v1beta1Ingresses, envId, beforeSyncDelete, objectPath,
                                    v1beta1Ingress);
                            break;
                        case SERVICE:
                            //反序列文件为V1service对象,
                            ConvertK8sObjectService<V1Service> convertV1Service = new ConvertV1ServiceServiceImpl();
                            convertV1Service.setT(new V1Service());
                            V1Service v1Service = convertV1Service
                                    .serializableObject(jsonObject.toJSONString(), filePath, objectPath);
                            //校验参数校验参数是否合法
                            convertV1Service.checkParameters(v1Service, objectPath);
                            //校验对象是否在其它文件中已经定义
                            convertV1Service.checkIfExist(v1Services, envId, beforeSyncDelete, objectPath, v1Service);
                            break;
                        case CERTIFICATE:
                            //反序列文件为C7nCertification对象,
                            ConvertK8sObjectService<C7nCertification> convertC7nCertification = new ConvertC7nCertificationServiceImpl();
                            convertC7nCertification.setT(new C7nCertification());
                            C7nCertification c7nCertification = convertC7nCertification
                                    .serializableObject(jsonObject.toJSONString(), filePath, objectPath);
                            //校验参数校验参数是否合法
                            convertC7nCertification.checkParameters(c7nCertification, objectPath);
                            //校验对象是否在其它文件中已经定义
                            convertC7nCertification.checkIfExist(c7nCertifications, envId, beforeSyncDelete, objectPath,
                                    c7nCertification);
                            break;
                        case CONFIGMAP:
                            //反序列文件为ConfigMap对象,
                            ConvertK8sObjectService<V1ConfigMap> convertConfigMap = new ConvertV1ConfigMapServiceImpl();
                            convertConfigMap.setT(new V1ConfigMap());
                            V1ConfigMap v1ConfigMap = convertConfigMap
                                    .serializableObject(jsonObject.toJSONString(), filePath, objectPath);
                            //校验参数校验参数是否合法
                            convertConfigMap.checkParameters(v1ConfigMap, objectPath);
                            //校验对象是否在其它文件中已经定义
                            convertConfigMap.checkIfExist(configMaps, envId, beforeSyncDelete, objectPath,
                                    v1ConfigMap);
                            break;
                        case SECRET:
                            // 反序列文件为C7nSecret对象
                            ConvertK8sObjectService<V1Secret> convertSecret = new ConvertC7nSecretServiceImpl();
                            convertSecret.setT(new V1Secret());
                            V1Secret v1Secret = convertSecret
                                    .serializableObject(jsonObject.toJSONString(), filePath, objectPath);
                            // 校验参数校验参数是否合法
                            convertSecret.checkParameters(v1Secret, objectPath);
                            // 校验对象是否在其它文件中已经定义
                            convertSecret.checkIfExist(secrets, envId, beforeSyncDelete, objectPath, v1Secret);
                            break;
                        case ENDPOINTS:
                            // 反序列文件为V1EndPoints对象
                            ConvertK8sObjectService<V1Endpoints> convertEndPoints = new ConvertV1EndPointsServiceImpl();
                            convertEndPoints.setT(new V1Endpoints());
                            V1Endpoints v1Endpoints1 = convertEndPoints
                                    .serializableObject(jsonObject.toJSONString(), filePath, objectPath);
                            // 校验参数校验参数是否合法
                            convertEndPoints.checkParameters(v1Endpoints1, objectPath);
                            v1Endpoints.add(v1Endpoints1);
                            break;
                        default:
                            //初始化自定义资源对象
                            DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = getDevopsCustomizeResourceDTO(envId, filePath, (Map<String, Object>) data);
                            objectPath.put(TypeUtil.objToString(devopsCustomizeResourceDTO.hashCode()), filePath);
                            ConvertK8sObjectService<DevopsCustomizeResourceDTO> convertCustomResourceDTO = new ConvertDevopsCustomResourceImpl();
                            // 校验对象是否在其它文件中已经定义
                            convertCustomResourceDTO.checkIfExist(devopsCustomizeResourceDTOS, envId, beforeSyncDelete, objectPath, devopsCustomizeResourceDTO);
                            // 校验参数校验参数是否合法
                            convertCustomResourceDTO.checkParameters(devopsCustomizeResourceDTO, objectPath);
                            break;
                    }
                }
            } catch (FileNotFoundException e) {
                throw new CommonException(e.getMessage(), e);
            }
        });
        return objectPath;
    }

    private DevopsCustomizeResourceDTO getDevopsCustomizeResourceDTO(Long envId, String
            filePath, Map<String, Object> data) {
        DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = new DevopsCustomizeResourceDTO();

        devopsCustomizeResourceDTO.setEnvId(envId);
        devopsCustomizeResourceDTO.setFilePath(filePath);
        Map<String, Object> datas = data;
        if (datas.get(KIND) == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_KIND_NOT_FOUND.getError(), filePath);
        }
        devopsCustomizeResourceDTO.setK8sKind(datas.get(KIND).toString());
        LinkedHashMap metadata = (LinkedHashMap) datas.get(METADATA);

        if (metadata == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_METADATA_NOT_FOUND.getError(), filePath);
        }
        if (metadata.get(NAME) == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_NAME_NOT_FOUND.getError(), filePath);
        }
        devopsCustomizeResourceDTO.setName(metadata.get(NAME).toString());

        //添加自定义资源标签
        LinkedHashMap labels = (LinkedHashMap) metadata.get(LABELS);

        if (labels == null) {
            labels = new LinkedHashMap();
        }
        labels.put(CHOERODON_IO_RESOURCE, CUSTOM);
        metadata.put(LABELS, labels);
        datas.put(METADATA, metadata);

        devopsCustomizeResourceDTO.setResourceContent(FileUtil.getYaml().dump(datas));
        return devopsCustomizeResourceDTO;
    }

    private void commitBranchSync(PushWebHookVO pushWebHookVO, Long appServiceId) {
        try {
            String branchName = pushWebHookVO.getRef().replaceFirst(REF_HEADS, "");

            DevopsBranchDTO devopsBranchDTO = devopsBranchService.baseQueryByAppAndBranchName(appServiceId, branchName);
            if (devopsBranchDTO == null) {
                createBranchSync(pushWebHookVO, appServiceId);
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
                .getUserIdByGitlabUserId(devopsMergeRequestDTO.getAuthorId());
        Long assigneeId = userAttrService
                .getUserIdByGitlabUserId(devopsMergeRequestDTO.getAssigneeId());
        List<CommitDTO> commitDTOS;
        try {
            commitDTOS = gitlabServiceClientOperator.listCommits(
                    devopsMergeRequestDTO.getGitlabProjectId().intValue(),
                    devopsMergeRequestDTO.getGitlabMergeRequestId().intValue(), getGitlabUserId());
            mergeRequestVO.setCommits(ConvertUtils.convertList(commitDTOS, CommitVO.class));
        } catch (FeignException e) {
            LOGGER.info(e.getMessage());
        }
        IamUserDTO authorUser = baseServiceClientOperator.queryUserByUserId(authorUserId);
        if (authorUser != null) {
            AuthorVO authorVO = new AuthorVO();
            authorVO.setUsername(authorUser.getLoginName());
            authorVO.setName(authorUser.getRealName());
            authorVO.setId(authorUser.getId() == null ? null : authorUser.getId().intValue());
            authorVO.setWebUrl(authorUser.getImageUrl());
            mergeRequestVO.setAuthor(authorVO);
        }
        IamUserDTO assigneeUser = baseServiceClientOperator.queryUserByUserId(assigneeId);
        if (assigneeUser != null) {
            AssigneeVO assigneeVO = new AssigneeVO();
            assigneeVO.setUsername(assigneeUser.getLoginName());
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
            createUserName = userDTO.getLoginName();
            createUserUrl = userDTO.getImageUrl();
            createUserRealName = userDTO.getRealName();
        }
        if (commitUserDTO == null) {
            commitUserDTO = new IamUserDTO();
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

            boolean branchExist = devopsBranchService.baseQueryByAppAndBranchName(appServiceId, branchName) != null;
            if (!branchExist) {
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

        return gitlabServiceClientOperator.listTag(pushWebHookVO.getProjectId(), pushWebHookVO.getUserId())
                .stream().noneMatch(tagDO -> tagDO.getName().equals(GitUtil.DEV_OPS_SYNC_TAG));

    }

    private void handDevopsEnvGitRepository(String path, String url, String envIdRsa, String commit) {
        File file = new File(path);
        gitUtil.setSshKey(envIdRsa);
        final String repoPath = path + GIT_SUFFIX;
        if (!file.exists()) {
            gitUtil.cloneBySsh(path, url);
            gitUtil.checkout(repoPath, commit);
        } else {
            gitUtil.checkout(repoPath, "master");
            gitUtil.pullBySsh(repoPath);
            gitUtil.checkout(repoPath, commit);
        }
    }


    @Override
    public BranchDTO baseQueryBranch(Integer gitLabProjectId, String branchName) {
        return gitlabServiceClientOperator.queryBranch(gitLabProjectId, branchName);
    }


}
