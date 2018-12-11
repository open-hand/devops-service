package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1Ingress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.BranchE;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.entity.gitlab.CompareResultsE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.handler.ResourceBundleHandler;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.*;
import io.choerodon.devops.domain.service.ConvertK8sObjectService;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.domain.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.domain.service.impl.*;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:44
 * Description:
 */
@Component
public class DevopsGitServiceImpl implements DevopsGitService {
    private static final String INGRESS = "Ingress";
    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    public static final String SERVICE = "Service";
    private static final String CERTIFICATE = "Certificate";
    private static final String CONFIGMAP = "ConfigMap";
    private static final String SECRET = "Secret";
    private static final String NO_COMMIT_SHA = "0000000000000000000000000000000000000000";
    private static final String REF_HEADS = "refs/heads/";
    private static final String GIT_SUFFIX = "/.git";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    private Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private AgileRepository agileRepository;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DeployService deployService;
    @Autowired
    private DevopsEnvFileRepository devopsEnvFileRepository;
    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository;
    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository;
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
    private HandlerObjectFileRelationsService handlerConfigMapRelationsServiceImpl;
    @Autowired
    @Qualifier("handlerC7nSecretServiceImpl")
    private HandlerObjectFileRelationsService handlerC7nSecretRelationsService;

    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }

    @Override
    public String getUrl(Long projectId, Long appId) {
        return devopsGitRepository.getGitlabUrl(projectId, appId);
    }

    @Override
    public void createTag(Long projectId, Long appId, String tag, String ref, String msg, String releaseNotes) {
        applicationRepository.checkApp(projectId, appId);
        Integer gitLabProjectId = devopsGitRepository.getGitLabId(appId);
        Integer gitLabUserId = devopsGitRepository.getGitlabUserId();
        devopsGitRepository.createTag(gitLabProjectId, tag, ref, msg, releaseNotes, gitLabUserId);
    }

    @Override
    public TagDO updateTagRelease(Long projectId, Long appId, String tag, String releaseNotes) {
        applicationRepository.checkApp(projectId, appId);
        Integer gitLabProjectId = devopsGitRepository.getGitLabId(appId);
        Integer gitLabUserId = devopsGitRepository.getGitlabUserId();
        return devopsGitRepository.updateTag(gitLabProjectId, tag, releaseNotes, gitLabUserId);
    }

    @Override
    public void deleteTag(Long projectId, Long appId, String tag) {
        applicationRepository.checkApp(projectId, appId);
        Integer gitLabProjectId = devopsGitRepository.getGitLabId(appId);
        Integer gitLabUserId = devopsGitRepository.getGitlabUserId();
        devopsGitRepository.deleteTag(gitLabProjectId, tag, gitLabUserId);
    }

    @Override
    public void createBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchE devopsBranchE = ConvertHelper.convert(devopsBranchDTO, DevopsBranchE.class);
        devopsBranchE.initApplicationE(applicationId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        BranchDO branchDO = devopsGitRepository.createBranch(
                TypeUtil.objToInteger(applicationE.getGitlabProjectE().getId()),
                devopsBranchDTO.getBranchName(),
                devopsBranchDTO.getOriginBranch(),
                getGitlabUserId());
        if (branchDO.getCommit() == null) {
            throw new CommonException("error.branch.exist");
        }
        CommitE commitE = branchDO.getCommit();
        Date checkoutDate = commitE.getCommittedDate();
        String checkoutSha = commitE.getId();
        Long gitLabUser = TypeUtil.objToLong(getGitlabUserId());
        devopsBranchE.setCheckoutDate(checkoutDate);
        devopsBranchE.setCheckoutCommit(checkoutSha);
        devopsBranchE.setUserId(gitLabUser);

        devopsBranchE.setLastCommitDate(checkoutDate);
        devopsBranchE.setLastCommit(checkoutSha);
        devopsBranchE.setLastCommitMsg(commitE.getMessage());
        devopsBranchE.setLastCommitUser(gitLabUser);
        devopsGitRepository.createDevopsBranch(devopsBranchE);
    }

    @Override
    public Page<BranchDTO> listBranches(Long projectId, PageRequest pageRequest, Long applicationId, String params) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organization.getCode(), projectE.getCode(), applicationE.getCode());
        Page<DevopsBranchE> branches =
                devopsGitRepository.listBranches(applicationId, pageRequest, params);
        Page<BranchDTO> page = new Page<>();
        BeanUtils.copyProperties(branches, page);
        page.setContent(branches.stream().map(t -> {
            Issue issue = null;
            if (t.getIssueId() != null) {
                issue = agileRepository.queryIssue(projectId, t.getIssueId(), organization.getId());
            }
            UserE userE = iamRepository.queryUserByUserId(
                    devopsGitRepository.getUserIdByGitlabUserId(t.getUserId()));
            UserE commitUserE = iamRepository.queryUserByUserId(
                    devopsGitRepository.getUserIdByGitlabUserId(t.getLastCommitUser()));
            String commitUrl = String.format("%s/commit/%s?view=parallel", path, t.getLastCommit());
            return getBranchDTO(t, commitUrl, commitUserE, userE, issue);
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public DevopsBranchDTO queryBranch(Long projectId, Long applicationId, String branchName) {
        return ConvertHelper.convert(devopsGitRepository
                .queryByAppAndBranchName(applicationId, branchName), DevopsBranchDTO.class);
    }

    @Override
    public void updateBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchE devopsBranchE = ConvertHelper.convert(devopsBranchDTO, DevopsBranchE.class);
        devopsGitRepository.updateBranchIssue(applicationId, devopsBranchE);
    }

    @Override
    public void deleteBranch(Long applicationId, String branchName) {
        ApplicationE applicationE = applicationRepository.query(applicationId);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        List<BranchE> branchEList = gitlabProjectRepository.listBranches(applicationE.getGitlabProjectE().getId(),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        Optional<BranchE> branchEOptional = branchEList
                .stream().filter(e -> branchName.equals(e.getName())).findFirst();
        branchEOptional.ifPresent(
                e -> gitlabProjectRepository.deleteBranch(applicationE.getGitlabProjectE().getId(), branchName,
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId())));
        devopsGitRepository.deleteDevopsBranch(applicationId, branchName);
    }

    private BranchDTO getBranchDTO(DevopsBranchE devopsBranchE, String lastCommitUrl, UserE commitUserE, UserE userE,
                                   Issue issue) {
        String createUserUrl = null;
        String createUserName = null;
        String createUserRealName = null;
        if (userE != null) {
            createUserName = userE.getLoginName();
            createUserUrl = userE.getImageUrl();
            createUserRealName = userE.getRealName();
        }
        if (commitUserE == null) {
            commitUserE = new UserE();
        }
        return new BranchDTO(
                devopsBranchE,
                lastCommitUrl,
                createUserUrl,
                issue,
                commitUserE,
                createUserName,
                createUserRealName);
    }

    @Override
    public Map<String, Object> getMergeRequestList(Long projectId, Long applicationId, String state, PageRequest pageRequest) {
        applicationRepository.checkApp(projectId, applicationId);
        Integer gitLabProjectId = devopsGitRepository.getGitLabId(applicationId);
        if (gitLabProjectId == null) {
            throw new CommonException("error.gitlabProjectId.not.exists");
        }
        return devopsGitRepository.getMergeRequestList(projectId, gitLabProjectId, state, pageRequest);
    }

    @Override
    public Page<TagDTO> getTags(Long projectId, Long applicationId, String params, Integer page, Integer size) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organization.getCode(), projectE.getCode(), applicationE.getCode());
        return devopsGitRepository.getTags(applicationId, path, page, params, size, getGitlabUserId());
    }

    @Override
    public List<TagDO> getTags(Long projectId, Long applicationId) {
        return devopsGitRepository.getTagList(applicationId, getGitlabUserId());
    }

    @Override
    public Boolean checkTag(Long projectId, Long applicationId, String tagName) {
        return devopsGitRepository.getTagList(applicationId, getGitlabUserId()).stream()
                .noneMatch(t -> tagName.equals(t.getName()));
    }

    @Override
    public void branchSync(PushWebHookDTO pushWebHookDTO, String token) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);

        if (NO_COMMIT_SHA.equals(pushWebHookDTO.getBefore())) {
            createBranchSync(pushWebHookDTO, applicationE.getId());
        } else if (NO_COMMIT_SHA.equals(pushWebHookDTO.getAfter())) {
            deleteBranchSync(pushWebHookDTO, applicationE.getId());
        } else {
            commitBranchSync(pushWebHookDTO, applicationE.getId());
        }
    }

    @Override
    @Saga(code = "devops-sync-gitops", description = "devops同步gitops库相关操作", inputSchemaClass = PushWebHookDTO.class)
    public void fileResourceSyncSaga(PushWebHookDTO pushWebHookDTO, String token) {
        pushWebHookDTO.setToken(token);
        String input;
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryByToken(pushWebHookDTO.getToken());
        pushWebHookDTO.getCommits().forEach(commitDTO -> {
            DevopsEnvCommitE devopsEnvCommitE = new DevopsEnvCommitE();
            devopsEnvCommitE.setEnvId(devopsEnvironmentE.getId());
            devopsEnvCommitE.setCommitSha(commitDTO.getId());
            devopsEnvCommitE.setCommitUser(TypeUtil.objToLong(pushWebHookDTO.getUserId()));
            devopsEnvCommitE.setCommitDate(commitDTO.getTimestamp());
            if (devopsEnvCommitRepository
                    .queryByEnvIdAndCommit(devopsEnvironmentE.getId(), commitDTO.getId()) == null) {
                devopsEnvCommitRepository.create(devopsEnvCommitE);
            }
        });
        DevopsEnvCommitE devopsEnvCommitE = devopsEnvCommitRepository
                .queryByEnvIdAndCommit(devopsEnvironmentE.getId(), pushWebHookDTO.getCheckoutSha());
        devopsEnvironmentE.setSagaSyncCommit(devopsEnvCommitE.getId());
        devopsEnvironmentRepository.updateEnvCommit(devopsEnvironmentE);
        try {
            input = objectMapper.writeValueAsString(pushWebHookDTO);
            sagaClient.startSaga("devops-sync-gitops",
                    new StartInstanceDTO(input, "env", devopsEnvironmentE.getId().toString()));
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fileResourceSync(PushWebHookDTO pushWebHookDTO) {
        final Integer gitLabProjectId = pushWebHookDTO.getProjectId();
        final Integer gitLabUserId = pushWebHookDTO.getUserId();
        Long userId = userAttrRepository.queryUserIdByGitlabUserId(TypeUtil.objToLong(gitLabUserId));
        if (userId == null) {
            return;
        }

        List<String> operationFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        Set<DevopsEnvFileResourceE> beforeSync = new HashSet<>();
        Set<DevopsEnvFileResourceE> beforeSyncDelete = new HashSet<>();
        //根据token查出环境
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryByToken(pushWebHookDTO.getToken());
        DevopsEnvCommitE devopsEnvCommitE = devopsEnvCommitRepository.query(devopsEnvironmentE.getSagaSyncCommit());
        boolean tagNotExist;
        Map<String, String> objectPath;
        //从iam服务中查出项目和组织code
        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        //本地路径
        final String path = String.format("gitops/%s/%s/%s",
                organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
        //生成环境git仓库ssh地址
        final String url = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organization.getCode(), projectE.getCode(),
                devopsEnvironmentE.getCode());

        LOGGER.info("The gitOps Repository ssh url:" + url);

        final Long envId = devopsEnvironmentE.getId();

        final Long projectId = devopsEnvironmentE.getProjectE().getId();

        try {
            //更新本地库到最新提交
            handDevopsEnvGitRepository(path, url, devopsEnvironmentE.getEnvIdRsa(), devopsEnvCommitE.getCommitSha());

            //查询devops-sync tag是否存在，存在则比较tag和最新commit的diff，不存在则识别gitops库下所有文件为新增文件
            tagNotExist = getDevopsSyncTag(pushWebHookDTO);

            if (tagNotExist) {
                operationFiles.addAll(FileUtil.getFilesPath(path));
                operationFiles.forEach(file -> {
                    List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                            .queryByEnvIdAndPath(devopsEnvironmentE.getId(), file);
                    if (!devopsEnvFileResourceES.isEmpty()) {
                        beforeSync.addAll(devopsEnvFileResourceES);
                    }
                });
            } else {
                handleDiffs(gitLabProjectId, operationFiles, deletedFiles, beforeSync, beforeSyncDelete,
                        devopsEnvironmentE, devopsEnvCommitE);
            }
            List<C7nHelmRelease> c7nHelmReleases = new ArrayList<>();
            List<V1Service> v1Services = new ArrayList<>();
            List<V1beta1Ingress> v1beta1Ingresses = new ArrayList<>();
            List<C7nCertification> c7nCertifications = new ArrayList<>();
            List<V1ConfigMap> v1ConfigMaps = new ArrayList<>();
            List<C7nSecret> c7nSecrets = new ArrayList<>();

            //从文件中读出对象,序列化为K8S对象
            objectPath = convertFileToK8sObjects(operationFiles, path, c7nHelmReleases, v1Services, v1beta1Ingresses,
                    v1ConfigMaps, c7nSecrets, devopsEnvironmentE.getId(), new ArrayList<>(beforeSyncDelete),
                    c7nCertifications);
            List<DevopsEnvFileResourceE> beforeSyncFileResource = new ArrayList<>(beforeSync);
            //将k8s对象初始化为实例，网络，域名，证书，秘钥对象,处理对象文件关系
            handlerC7nReleaseRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, c7nHelmReleases, envId, projectId, path,
                            userId);
            handlerServiceRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, v1Services, envId, projectId, path, userId);
            handlerIngressRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, v1beta1Ingresses, envId, projectId, path,
                            userId);
            handlerC7nCertificationRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, c7nCertifications, envId, projectId, path,
                            userId);
            handlerConfigMapRelationsServiceImpl
                    .handlerRelations(objectPath, beforeSyncFileResource, v1ConfigMaps, envId, projectId, path, userId);
            handlerC7nSecretRelationsService
                    .handlerRelations(objectPath, beforeSyncFileResource, c7nSecrets, envId, projectId, path, userId);

            //处理文件
            handleFiles(operationFiles, deletedFiles, devopsEnvironmentE, devopsEnvCommitE, path);

            //删除tag
            handleTag(pushWebHookDTO, gitLabProjectId, gitLabUserId, devopsEnvCommitE, tagNotExist);

            devopsEnvironmentE.setDevopsSyncCommit(devopsEnvCommitE.getId());
            //更新环境 解释commit
            devopsEnvironmentRepository.updateEnvCommit(devopsEnvironmentE);
            //向agent发送同步指令
            deployService.sendCommand(devopsEnvironmentE);
        } catch (CommonException e) {
            String filePath = "";
            String errorCode = "";
            if (e instanceof GitOpsExplainException) {
                filePath = ((GitOpsExplainException) e).getFilePath();
                errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e)
                        .getErrorCode();
            }
            DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, filePath, path);
            String error;
            try {
                error = ResourceBundleHandler.getInstance().getValue(e.getMessage());
            } catch (Exception e1) {
                error = e.getMessage();
            }
            devopsEnvFileErrorE.setError(error + ":" + errorCode);
            devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
            LOGGER.info(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }

        //删除文件错误记录
        DevopsEnvFileErrorE devopsEnvFileErrorE = new DevopsEnvFileErrorE();
        devopsEnvFileErrorE.setEnvId(devopsEnvironmentE.getId());
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE);
        // do sth to files
    }

    @Override
    public void checkName(Long projectId, Long applicationId, String branchName) {
        ApplicationE applicationE = applicationRepository.query(applicationId);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        List<BranchE> branchEList = gitlabProjectRepository.listBranches(applicationE.getGitlabProjectE().getId(),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        Optional<BranchE> branchEOptional = branchEList
                .stream().filter(e -> branchName.equals(e.getName())).findFirst();
        branchEOptional.ifPresent(e -> {
            throw new CommonException("error.branch.exist");
        });
    }

    private void handleFiles(List<String> operationFiles, List<String> deletedFiles,
                             DevopsEnvironmentE devopsEnvironmentE, DevopsEnvCommitE devopsEnvCommitE, String path) {
        //新增解释文件记录
        for (String filePath : operationFiles) {
            DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository
                    .queryByEnvAndPath(devopsEnvironmentE.getId(), filePath);
            if (devopsEnvFileE == null) {
                devopsEnvFileE = new DevopsEnvFileE();
                devopsEnvFileE.setDevopsCommit(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvFileE.setFilePath(filePath);
                devopsEnvFileE.setEnvId(devopsEnvCommitE.getEnvId());
                devopsEnvFileRepository.create(devopsEnvFileE);
            } else {
                devopsEnvFileE.setDevopsCommit(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvFileRepository.update(devopsEnvFileE);
            }
        }

        for (String filePath : deletedFiles) {
            DevopsEnvFileE devopsEnvFileE = new DevopsEnvFileE();
            devopsEnvFileE.setEnvId(devopsEnvironmentE.getId());
            devopsEnvFileE.setFilePath(filePath);
            devopsEnvFileRepository.delete(devopsEnvFileE);
        }
    }

    private void handleTag(PushWebHookDTO pushWebHookDTO, Integer gitLabProjectId, Integer gitLabUserId,
                           DevopsEnvCommitE devopsEnvCommitE, Boolean tagNotExist) {
        if (tagNotExist) {
            devopsGitRepository.createTag(
                    gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitE.getCommitSha(),
                    "", "", gitLabUserId);
        } else {
            try {
                devopsGitRepository.deleteTag(gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, gitLabUserId);
            } catch (CommonException e) {
                if (getDevopsSyncTag(pushWebHookDTO)) {
                    devopsGitRepository.createTag(
                            gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitE.getCommitSha(),
                            "", "", gitLabUserId);
                } else {
                    throw new GitOpsExplainException(e.getMessage(), e);
                }
            }
            //创建新tag
            if (getDevopsSyncTag(pushWebHookDTO)) {
                devopsGitRepository.createTag(
                        gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitE.getCommitSha(),
                        "", "", gitLabUserId);
            }
        }
    }

    private void handleDiffs(Integer gitLabProjectId, List<String> operationFiles, List<String> deletedFiles,
                             Set<DevopsEnvFileResourceE> beforeSync, Set<DevopsEnvFileResourceE> beforeSyncDelete,
                             DevopsEnvironmentE devopsEnvironmentE, DevopsEnvCommitE devopsEnvCommitE) {
        //获取将此次最新提交与tag作比价得到diff
        CompareResultsE compareResultsE = devopsGitRepository
                .getCompareResults(gitLabProjectId, GitUtil.DEV_OPS_SYNC_TAG, devopsEnvCommitE.getCommitSha());
        compareResultsE.getDiffs().forEach(t -> {
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

            List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                    .queryByEnvIdAndPath(devopsEnvironmentE.getId(), t.getOldPath());
            if (!devopsEnvFileResourceES.isEmpty()) {
                beforeSync.addAll(devopsEnvFileResourceES);
            }
        });

        deletedFiles.forEach(file -> {
            List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                    .queryByEnvIdAndPath(devopsEnvironmentE.getId(), file);
            if (!devopsEnvFileResourceES.isEmpty()) {
                beforeSyncDelete.addAll(devopsEnvFileResourceES);
            }
        });
    }

    private Map<String, String> convertFileToK8sObjects(List<String> files,
                                                        String path,
                                                        List<C7nHelmRelease> c7nHelmReleases,
                                                        List<V1Service> v1Services,
                                                        List<V1beta1Ingress> v1beta1Ingresses,
                                                        List<V1ConfigMap> configMaps,
                                                        List<C7nSecret> c7nSecrets,
                                                        Long envId,
                                                        List<DevopsEnvFileResourceE> beforeSyncDelete,
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
                            ConvertK8sObjectService<C7nSecret> convertC7nSecret = new ConvertC7nSecretServiceImpl();
                            convertC7nSecret.setT(new C7nSecret());
                            C7nSecret c7nSecret = convertC7nSecret
                                    .serializableObject(jsonObject.toJSONString(), filePath, objectPath);
                            // 校验参数校验参数是否合法
                            convertC7nSecret.checkParameters(c7nSecret, objectPath);
                            // 校验对象是否在其它文件中已经定义
                            convertC7nSecret.checkIfExist(c7nSecrets, envId, beforeSyncDelete, objectPath, c7nSecret);
                            break;
                        default:
                            break;
                    }
                }
            } catch (FileNotFoundException e) {
                throw new CommonException(e.getMessage(), e);
            }
        });
        return objectPath;
    }

    private void commitBranchSync(PushWebHookDTO pushWebHookDTO, Long appId) {
        try {
            String branchName = pushWebHookDTO.getRef().replaceFirst(REF_HEADS, "");
            DevopsBranchE branchE = devopsGitRepository.queryByAppAndBranchName(appId, branchName);
            String lastCommit = pushWebHookDTO.getAfter();
            Optional<CommitDTO> lastCommitOptional
                    = pushWebHookDTO.getCommits().stream().filter(t -> lastCommit.equals(t.getId())).findFirst();
            CommitDTO lastCommitDTO = new CommitDTO();
            if (lastCommitOptional.isPresent()) {
                lastCommitDTO = lastCommitOptional.get();
            }
            branchE.setLastCommit(lastCommit);
            branchE.setLastCommitDate(lastCommitDTO.getTimestamp());
            branchE.setLastCommitMsg(lastCommitDTO.getMessage());
            branchE.setLastCommitUser(pushWebHookDTO.getUserId().longValue());
            devopsGitRepository.updateBranchLastCommit(branchE);
        } catch (Exception e) {
            LOGGER.info("error.update.branch");
        }

    }

    private void deleteBranchSync(PushWebHookDTO pushWebHookDTO, Long appId) {
        try {
            String branchName = pushWebHookDTO.getRef().replaceFirst(REF_HEADS, "");
            devopsGitRepository.deleteDevopsBranch(appId, branchName);
        } catch (Exception e) {
            LOGGER.info("error.devops.branch.delete");
        }
    }

    private void createBranchSync(PushWebHookDTO pushWebHookDTO, Long appId) {
        try {
            String lastCommit = pushWebHookDTO.getAfter();
            Long userId = pushWebHookDTO.getUserId().longValue();

            // 从gitlab-service获取commit数据，所以需要处理gitlab中获取的时间
            CommitE commitE = devopsGitRepository.getCommit(
                    pushWebHookDTO.getProjectId(),
                    lastCommit,
                    userId.intValue());
            String branchName = pushWebHookDTO.getRef().replaceFirst(REF_HEADS, "");
            boolean branchExist = devopsGitRepository.queryByAppAndBranchName(appId, branchName) != null;
            if (!branchExist) {
                DevopsBranchE devopsBranchE = new DevopsBranchE();
                devopsBranchE.setUserId(userId);
                devopsBranchE.initApplicationE(appId);

                // 处理时间
                Date commitDate = DateUtil.convertUTC2Local(commitE.getCommittedDate());

                devopsBranchE.setCheckoutDate(commitDate);
                devopsBranchE.setCheckoutCommit(lastCommit);
                devopsBranchE.setBranchName(branchName);

                devopsBranchE.setLastCommitUser(userId);
                devopsBranchE.setLastCommit(lastCommit);
                devopsBranchE.setLastCommitMsg(commitE.getMessage());

                devopsBranchE.setLastCommitDate(commitDate);
                devopsGitRepository.createDevopsBranch(devopsBranchE);
            }
        } catch (Exception e) {
            LOGGER.info("error.create.branch");
        }
    }

    private DevopsEnvFileErrorE getDevopsFileError(Long envId, String filePath, String path) {
        DevopsEnvFileErrorE devopsEnvFileErrorE = devopsEnvFileErrorRepository.queryByEnvIdAndFilePath(envId, filePath);
        if (devopsEnvFileErrorE == null) {
            devopsEnvFileErrorE = new DevopsEnvFileErrorE();
            devopsEnvFileErrorE.setFilePath(filePath);
            devopsEnvFileErrorE.setEnvId(envId);
            devopsEnvFileErrorE.setCommit(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
        } else {
            devopsEnvFileErrorE.setFilePath(filePath);
            devopsEnvFileErrorE.setCommit(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
        }
        return devopsEnvFileErrorE;
    }

    private boolean getDevopsSyncTag(PushWebHookDTO pushWebHookDTO) {
        return devopsGitRepository.getGitLabTags(pushWebHookDTO.getProjectId(), pushWebHookDTO.getUserId())
                .stream().noneMatch(tagDO -> tagDO.getName().equals(GitUtil.DEV_OPS_SYNC_TAG));

    }

    private void handDevopsEnvGitRepository(String path, String url, String envIdRsa, String commit) {
        File file = new File(path);
        GitUtil gitUtil = new GitUtil(envIdRsa);
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
    public void initMockService(SagaClient sagaClient) {
        this.sagaClient = sagaClient;
    }
}
