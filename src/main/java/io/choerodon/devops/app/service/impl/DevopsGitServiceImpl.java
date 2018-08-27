package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.entity.gitlab.CompareResultsE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.handler.SerializableOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.certification.CertificationSpec;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.CertificationStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
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
    public static final String INGRESS = "Ingress";
    public static final String C7NHELM_RELEASE = "C7NHelmRelease";
    public static final String SERVICE = "Service";
    public static final String CERTIFICATE = "Certificate";
    private static final String NO_COMMIT_SHA = "0000000000000000000000000000000000000000";
    private static final String REF_HEADS = "refs/heads/";
    private static final String GIT_SUFFIX = "/.git";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    private static final String ERROR_MESSAGE = "the another file already has the same object :";

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
    private SagaClient sagaClient;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private DeployService deployService;
    @Autowired
    private DevopsEnvFileRepository devopsEnvFileRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DeployMsgHandlerService deployMsgHandlerService;
    @Autowired
    private ApplicationVersionValueRepository applicationVersionValueRepository;
    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository;
    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private CertificationService certificationService;


    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }

    @Override
    public String getUrl(Long projectId, Long appId) {
        return devopsGitRepository.getGitlabUrl(projectId, appId);
    }

    @Override
    public void createTag(Long projectId, Long appId, String tag, String ref) {
        applicationRepository.checkApp(projectId, appId);
        Integer gitLabProjectId = devopsGitRepository.getGitLabId(appId);
        Integer gitLabUserId = devopsGitRepository.getGitlabUserId();
        devopsGitRepository.createTag(gitLabProjectId, tag, ref, gitLabUserId);
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
        page.setContent(branches.parallelStream().map(t -> {
            Issue issue = null;
            if (t.getIssueId() != null) {
                issue = agileRepository.queryIssue(projectId, t.getIssueId());
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
    public void deleteBranch(Long projectId, Long applicationId, String branchName) {
        Integer gitLabId = devopsGitRepository.getGitLabId(applicationId);
        devopsGitRepository.deleteBranch(gitLabId, branchName, getGitlabUserId());
        devopsGitRepository.deleteDevopsBranch(applicationId, branchName);
    }


    private BranchDTO getBranchDTO(DevopsBranchE t, String lastCommitUrl, UserE commitUserE, UserE userE,
                                   Issue issue) {
        String createUserUrl = null;
        String createUserName = null;
        String createUserRealName = null;
        Long issueId = t.getIssueId();
        if (userE != null) {
            createUserName = userE.getLoginName();
            createUserUrl = userE.getImageUrl();
            createUserRealName = userE.getRealName();
        }
        if (commitUserE == null) {
            commitUserE = new UserE();
        }
        return new BranchDTO(
                t,
                lastCommitUrl,
                t.getCreationDate(),
                createUserUrl,
                issueId,
                issue == null ? null : issue.getIssueNum(),
                issue == null ? null : issue.getSummary(),
                commitUserE.getImageUrl(),
                issue == null ? null : issue.getTypeCode(),
                commitUserE.getLoginName(),
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
        return devopsGitRepository.getTagList(applicationId, getGitlabUserId()).parallelStream()
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
    @Saga(code = "devops-sync-gitops", description = "gitops", inputSchemaClass = PushWebHookDTO.class)
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
            if (devopsEnvCommitRepository.queryByEnvIdAndCommit(devopsEnvironmentE.getId(), commitDTO.getId()) == null) {
                devopsEnvCommitRepository.create(devopsEnvCommitE);
            }

        });
        DevopsEnvCommitE devopsEnvCommitE = devopsEnvCommitRepository.queryByEnvIdAndCommit(devopsEnvironmentE.getId(), pushWebHookDTO.getCheckoutSha());
        devopsEnvironmentE.setGitCommit(devopsEnvCommitE.getId());
        devopsEnvironmentRepository.update(devopsEnvironmentE);
        try {
            input = objectMapper.writeValueAsString(pushWebHookDTO);
            sagaClient.startSaga("devops-sync-gitops", new StartInstanceDTO(input, "env", devopsEnvironmentE.getId().toString()));
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fileResourceSync(PushWebHookDTO pushWebHookDTO) {
        Integer gitLabProjectId = pushWebHookDTO.getProjectId();
        Integer gitLabUserId = pushWebHookDTO.getUserId();


        List<String> operationFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        Set<DevopsEnvFileResourceE> beforeSync = new HashSet<>();
        Set<DevopsEnvFileResourceE> beforeSyncDelete = new HashSet<>();
        String path = "";
        //根据token查出环境
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryByToken(pushWebHookDTO.getToken());
        DevopsEnvCommitE devopsEnvCommitE = devopsEnvCommitRepository.query(devopsEnvironmentE.getGitCommit());
        Boolean tagNotExist = false;
        try {
            //从iam服务中查出项目和组织code
            ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

            //本地路径
            path = String.format("gitops/%s/%s/%s",
                    organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
            //生成环境git仓库ssh地址
            String url = String.format("git@%s:%s-%s-gitops/%s.git",
                    gitlabSshUrl, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());

            //更新本地库到最新提交
            handDevopsEnvGitRepository(path, url, devopsEnvironmentE.getEnvIdRsa(), devopsEnvCommitE.getCommitSha());

            //查询devops-sync tag是否存在，存在则比较tag和最新commit的diff，不存在则识别gitops库下所有文件为新增文件
            tagNotExist = getDevopsSyncTag(pushWebHookDTO);

            if (tagNotExist) {
                operationFiles.addAll(FileUtil.getFilesPath(path));
                operationFiles.parallelStream().forEach(file -> {
                    List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                            .queryByEnvIdAndPath(devopsEnvironmentE.getId(), file);
                    if (!devopsEnvFileResourceES.isEmpty()) {
                        beforeSync.addAll(devopsEnvFileResourceES);
                    }
                });
            } else {
                handleDiffs(gitLabProjectId, operationFiles, deletedFiles, beforeSync, beforeSyncDelete, devopsEnvironmentE, devopsEnvCommitE);
            }
        } catch (CommonException e) {
            LOGGER.info(e.getTrace());
            DevopsEnvFileErrorE devopsEnvFileErrorE = new DevopsEnvFileErrorE();
            devopsEnvFileErrorE.setCommit(devopsEnvCommitE.getCommitSha());
            devopsEnvFileErrorE.setError(e.getMessage());
            devopsEnvFileErrorE.setEnvId(devopsEnvironmentE.getId());
            devopsEnvFileErrorRepository.create(devopsEnvFileErrorE);
            return;
        }
        List<C7nHelmRelease> c7nHelmReleases = new ArrayList<>();
        List<V1Service> v1Services = new ArrayList<>();
        List<V1beta1Ingress> v1beta1Ingresses = new ArrayList<>();
        List<C7nCertification> c7nCertifications = new ArrayList<>();

        try {
            //从文件中读出对象
            Map<String, String> objectPath = handleFilesToObject(operationFiles, path, c7nHelmReleases,
                    v1Services, v1beta1Ingresses,
                    devopsEnvironmentE.getId(),
                    new ArrayList<>(beforeSyncDelete),
                    c7nCertifications);

            //处理对象关系
            handlerObjectRelations(
                    objectPath,
                    new ArrayList<>(beforeSync),
                    c7nHelmReleases,
                    v1Services,
                    v1beta1Ingresses,
                    c7nCertifications,
                    devopsEnvironmentE.getId(),
                    path
            );
        } catch (CommonException e) {
            LOGGER.info(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }
        //新增解释文件记录，并删除文件错误记录
        try {
            for (String filePath : operationFiles) {
                DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository.queryByEnvAndPath(devopsEnvironmentE.getId(), filePath);
                if (devopsEnvFileE == null) {
                    devopsEnvFileE = new DevopsEnvFileE();
                    devopsEnvFileE.setDevopsCommit(getFileLatestCommit(path + GIT_SUFFIX, filePath));
                    devopsEnvFileE.setFilePath(filePath);
                    devopsEnvFileE.setEnvId(devopsEnvCommitE.getEnvId());
                    devopsEnvFileRepository.create(devopsEnvFileE);
                } else {
                    devopsEnvFileE.setDevopsCommit(getFileLatestCommit(path + GIT_SUFFIX, filePath));
                    devopsEnvFileRepository.update(devopsEnvFileE);
                }
            }

            for (String filePath : deletedFiles) {
                DevopsEnvFileE devopsEnvFileE = new DevopsEnvFileE();
                devopsEnvFileE.setEnvId(devopsEnvironmentE.getId());
                devopsEnvFileE.setFilePath(filePath);
                devopsEnvFileRepository.delete(devopsEnvFileE);
            }
            //删除tag
            handleTag(pushWebHookDTO, gitLabProjectId, gitLabUserId, devopsEnvCommitE, tagNotExist);


            //向agent发送同步指令
            deployService.sendCommand(devopsEnvironmentE);
            devopsEnvironmentE.setDevopsSyncCommit(devopsEnvCommitE.getId());
            //更新环境 解释commit
            devopsEnvironmentRepository.update(devopsEnvironmentE);
        } catch (CommonException e) {
            LOGGER.info(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            DevopsEnvFileErrorE newdevopsEnvFileErrorE = new DevopsEnvFileErrorE();
            newdevopsEnvFileErrorE.setCommit(devopsEnvCommitE.getCommitSha());
            newdevopsEnvFileErrorE.setError(e.getMessage());
            newdevopsEnvFileErrorE.setEnvId(devopsEnvironmentE.getId());
            devopsEnvFileErrorRepository.createOrUpdate(newdevopsEnvFileErrorE);
            return;
        }


        //清楚历史错误记录
        DevopsEnvFileErrorE devopsEnvFileErrorE = new DevopsEnvFileErrorE();
        devopsEnvFileErrorE.setEnvId(devopsEnvironmentE.getId());
        devopsEnvFileErrorRepository.delete(devopsEnvFileErrorE);
        // do sth to files
    }

    private void handleTag(PushWebHookDTO pushWebHookDTO, Integer gitLabProjectId, Integer gitLabUserId, DevopsEnvCommitE devopsEnvCommitE, Boolean tagNotExist) {
        if (tagNotExist) {
            devopsGitRepository.createTag(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, devopsEnvCommitE.getCommitSha(), gitLabUserId);
        } else {
            try {
                devopsGitRepository.deleteTag(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, gitLabUserId);
            } catch (CommonException e) {
                if (getDevopsSyncTag(pushWebHookDTO)) {
                    devopsGitRepository.createTag(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, devopsEnvCommitE.getCommitSha(), gitLabUserId);
                } else {
                    throw new CommonException(e.getMessage(), e);
                }
            }
            //创建新tag
            if (getDevopsSyncTag(pushWebHookDTO)) {
                devopsGitRepository.createTag(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, devopsEnvCommitE.getCommitSha(), gitLabUserId);
            }
        }
    }

    private void handleDiffs(Integer gitLabProjectId, List<String> operationFiles, List<String> deletedFiles, Set<DevopsEnvFileResourceE> beforeSync, Set<DevopsEnvFileResourceE> beforeSyncDelete, DevopsEnvironmentE devopsEnvironmentE, DevopsEnvCommitE devopsEnvCommitE) {
        //获取将此次最新提交与tag作比价得到diff
        CompareResultsE compareResultsE = devopsGitRepository
                .getCompareResults(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, devopsEnvCommitE.getCommitSha());
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

        deletedFiles.parallelStream().forEach(file -> {
            List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                    .queryByEnvIdAndPath(devopsEnvironmentE.getId(), file);
            if (!devopsEnvFileResourceES.isEmpty()) {
                beforeSyncDelete.addAll(devopsEnvFileResourceES);
            }
        });
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

            CommitE commitE = devopsGitRepository.getCommit(
                    pushWebHookDTO.getProjectId(),
                    lastCommit,
                    userId.intValue());
            String branchName = pushWebHookDTO.getRef().replaceFirst(REF_HEADS, "");
            Boolean branchExist = devopsGitRepository.queryByAppAndBranchName(appId, branchName) != null;
            if (!branchExist) {
                DevopsBranchE devopsBranchE = new DevopsBranchE();
                devopsBranchE.setUserId(userId);
                devopsBranchE.initApplicationE(appId);
                devopsBranchE.setCheckoutDate(commitE.getCommittedDate());
                devopsBranchE.setCheckoutCommit(lastCommit);
                devopsBranchE.setBranchName(branchName);

                devopsBranchE.setLastCommitUser(userId);
                devopsBranchE.setLastCommit(lastCommit);
                devopsBranchE.setLastCommitMsg(commitE.getMessage());
                Date date = DateUtil.changeTimeZone(
                        commitE.getCommittedDate(), TimeZone.getTimeZone("GMT"), TimeZone.getDefault());
                devopsBranchE.setLastCommitDate(date);
                devopsGitRepository.createDevopsBranch(devopsBranchE);
            }
        } catch (Exception e) {
            LOGGER.info("error.create.branch");
        }
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


    private Map<String, String> handleFilesToObject(List<String> files,
                                                    String path,
                                                    List<C7nHelmRelease> c7nHelmReleases,
                                                    List<V1Service> v1Services,
                                                    List<V1beta1Ingress> v1beta1Ingresses,
                                                    Long envId,
                                                    List<DevopsEnvFileResourceE> beforeSyncDelete,
                                                    List<C7nCertification> c7nCertifications) {
        Map<String, String> objectPath = new HashMap<>();

        files.stream().forEach(filePath -> {
            Yaml yaml = new Yaml();
            File file = new File(String.format("%s/%s", path, filePath));
            DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, filePath, path);
            try {
                for (Object data : yaml.loadAll(new FileInputStream(file))) {
                    JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
                    String type = jsonObject.get("kind").toString();
                    switch (type) {
                        case C7NHELM_RELEASE:
                            //反序列文件为c7nHelmRelease对象
                            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
                            SerializableOperation<C7nHelmRelease> c7nHelmReleaseSerializableOperation
                                    = new SerializableOperation<>();
                            c7nHelmReleaseSerializableOperation.setT(c7nHelmRelease);
                            C7nHelmRelease serializableC7n = c7nHelmReleaseSerializableOperation
                                    .serializable(jsonObject.toJSONString(), filePath, objectPath);

                            //校验c7nrelease对象，校验是否已存在，以及参数是否填写正确
                            checkC7nHelmRelease(path, c7nHelmReleases, envId, beforeSyncDelete, objectPath, devopsEnvFileErrorE, serializableC7n);
                            break;
                        case INGRESS:
                            //反序列文件为ingress对象
                            V1beta1Ingress v1beta1Ingress = new V1beta1Ingress();
                            SerializableOperation<V1beta1Ingress> v1beta1IngressSerializableOperation
                                    = new SerializableOperation<>();
                            v1beta1IngressSerializableOperation.setT(v1beta1Ingress);
                            V1beta1Ingress serializableIng = v1beta1IngressSerializableOperation
                                    .serializable(jsonObject.toJSONString(), filePath, objectPath);
                            //校验ingress对象，校验是否已存在，以及参数是否填写正确
                            checkIngress(path, v1beta1Ingresses, envId, beforeSyncDelete, objectPath, devopsEnvFileErrorE, serializableIng);
                            break;
                        case SERVICE:
                            //反序列文件为service对象
                            V1Service v1Service = new V1Service();
                            SerializableOperation<V1Service> v1ServiceSerializableOperation =
                                    new SerializableOperation<>();
                            v1ServiceSerializableOperation.setT(v1Service);
                            V1Service serializableSvc = v1ServiceSerializableOperation
                                    .serializable(jsonObject.toJSONString(), filePath, objectPath);
                            //校验service对象，校验是否已存在，以及参数是否填写正确
                            checkService(path, v1Services, envId, beforeSyncDelete, objectPath, devopsEnvFileErrorE, serializableSvc);
                            break;
                        case CERTIFICATE:
                            //反序列文件为Certificate对象
                            C7nCertification c7nCertification = new C7nCertification();
                            SerializableOperation<C7nCertification> c7nCertificationSerializableOperation
                                    = new SerializableOperation<>();
                            c7nCertificationSerializableOperation.setT(c7nCertification);
                            C7nCertification serializableCert = c7nCertificationSerializableOperation
                                    .serializable(jsonObject.toJSONString(), filePath, objectPath);
                            //校验Certificate对象，校验是否已存在，以及参数是否填写正确
                            checkC7nCertification(path, envId, beforeSyncDelete, c7nCertifications, objectPath, devopsEnvFileErrorE, serializableCert);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                devopsEnvFileErrorE.setError(e.getMessage());
                devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                throw new CommonException(e.getMessage(), e);
            }
        });
        return objectPath;
    }

    private void checkC7nCertification(String path, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, List<C7nCertification> c7nCertifications, Map<String, String> objectPath, DevopsEnvFileErrorE devopsEnvFileErrorE, C7nCertification serializableCert) {
        String certName = serializableCert.getMetadata().getName();
        if (certName == null) {
            throw new CommonException("The C7nCertification does not have name");
        }
        CertificationE certificationE = certificationRepository.queryByEnvAndName(envId, certName);
        if (certificationE != null) {
            Long certId = certificationE.getId();
            if (beforeSyncDelete.parallelStream()
                    .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType()
                            .equals(serializableCert.getKind()))
                    .noneMatch(devopsEnvFileResourceE ->
                            devopsEnvFileResourceE.getResourceId()
                                    .equals(certId))) {
                hasSameObject(envId, objectPath, certName, serializableCert.getKind(),
                        certId, path, serializableCert.hashCode());
            }
        }
        if (c7nCertifications.parallelStream()
                .anyMatch(certification -> certification.getMetadata().getName()
                        .equals(certName))) {
            createDevopsFileError(devopsEnvFileErrorE, ERROR_MESSAGE + certName);
        } else {
            c7nCertifications.add(serializableCert);
        }
    }

    private void checkService(String path, List<V1Service> v1Services, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, DevopsEnvFileErrorE devopsEnvFileErrorE, V1Service serializableSvc) {
        formatService(serializableSvc);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(serializableSvc.getMetadata().getName(), devopsEnvironmentE.getCode());
        if (devopsServiceE != null && beforeSyncDelete.parallelStream().filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(serializableSvc.getKind())).noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(devopsServiceE.getId()))) {
            hasSameObject(envId, objectPath, serializableSvc.getMetadata().getName(), serializableSvc.getKind(), devopsServiceE.getId(), path, serializableSvc.hashCode());
        }
        if (v1Services.parallelStream().anyMatch(v1Service1 -> v1Service1.getMetadata().getName().equals(serializableSvc.getMetadata().getName()))) {
            createDevopsFileError(devopsEnvFileErrorE, serializableSvc.getMetadata().getName());
        } else {
            v1Services.add(serializableSvc);
        }
    }

    private void checkIngress(String path, List<V1beta1Ingress> v1beta1Ingresses, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, DevopsEnvFileErrorE devopsEnvFileErrorE, V1beta1Ingress serializableIng) {
        formatIngress(serializableIng);
        DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, serializableIng.getMetadata().getName());
        if (devopsIngressE != null && beforeSyncDelete.parallelStream().filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(serializableIng.getKind())).noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(devopsIngressE.getId()))) {
            hasSameObject(envId, objectPath, serializableIng.getMetadata().getName(), serializableIng.getKind(), devopsIngressE.getId(), path, serializableIng.hashCode());
        }
        if (v1beta1Ingresses.parallelStream().anyMatch(v1beta1Ingress1 -> v1beta1Ingress1.getMetadata().getName().equals(serializableIng.getMetadata().getName()))) {
            createDevopsFileError(devopsEnvFileErrorE, serializableIng.getMetadata().getName());
        } else {
            v1beta1Ingresses.add(serializableIng);
        }
    }

    private void checkC7nHelmRelease(String path, List<C7nHelmRelease> c7nHelmReleases, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, DevopsEnvFileErrorE devopsEnvFileErrorE, C7nHelmRelease serializableC7n) {
        formatC7nRelease(serializableC7n);
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(serializableC7n.getMetadata().getName(), envId);

        if (applicationInstanceE != null && beforeSyncDelete.parallelStream().filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(serializableC7n.getKind())).noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(applicationInstanceE.getId()))) {
            hasSameObject(envId, objectPath, serializableC7n.getMetadata().getName(), serializableC7n.getKind(), applicationInstanceE.getId(), path, serializableC7n.hashCode());
        }
        if (c7nHelmReleases.parallelStream().anyMatch(c7nHelmRelease1 -> c7nHelmRelease1.getMetadata().getName().equals(serializableC7n.getMetadata().getName()))) {
            createDevopsFileError(devopsEnvFileErrorE, serializableC7n.getMetadata().getName());
        } else {
            c7nHelmReleases.add(serializableC7n);
        }
    }


    private void handlerObjectRelations(Map<String, String> objectPath,
                                        List<DevopsEnvFileResourceE> beforeSync,
                                        List<C7nHelmRelease> c7nHelmReleases,
                                        List<V1Service> v1Services,
                                        List<V1beta1Ingress> v1beta1Ingresses,
                                        List<C7nCertification> c7nCertifications,
                                        Long envId, String path) {

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        Long projectId = devopsEnvironmentE.getProjectE().getId();
        //处理c7nHelmRelease对象
        handlerC7nReleaseRelations(objectPath, beforeSync, c7nHelmReleases, envId, projectId, path);
        //处理Service对象
        handlerServiceRelations(objectPath, beforeSync, v1Services, envId, projectId, path);
        //处理Ingress对象
        handlerIngressRelations(objectPath, beforeSync, v1beta1Ingresses, envId, projectId, path);
        //处理C7nCertifications对象
        handlerC7nCertifications(objectPath, beforeSync, c7nCertifications, envId, path);
    }

    private void handlerC7nCertifications(Map<String, String> objectPath,
                                          List<DevopsEnvFileResourceE> beforeSync,
                                          List<C7nCertification> c7nCertifications, Long envId, String path) {
        beforeSync.parallelStream().filter(devopsEnvFileResourceE ->
                devopsEnvFileResourceE.getResourceType().equals("Certificate"))
                .map(devopsEnvFileResourceE -> {
                    CertificationE certificationE = certificationRepository
                            .queryById(devopsEnvFileResourceE.getResourceId());
                    if (certificationE == null) {
                        throw new CommonException("the certification in the file is not exist in devops database");
                    }
                    return certificationE.getName();
                })
                .forEach(certName -> {
                    CertificationE certificationE = certificationRepository.queryByEnvAndName(envId, certName);
                    certificationService.deleteById(certificationE.getId(), true);
                    devopsEnvFileResourceRepository
                            .deleteByEnvIdAndResource(envId, certificationE.getId(), ObjectType.CERTIFICATE.getType());
                });
        c7nCertifications.parallelStream().forEach(c7nCertification -> {
            DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(
                    envId, objectPath.get(TypeUtil.objToString(c7nCertification.hashCode())), path);
            try {
                DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(c7nCertification.hashCode())));
                devopsEnvFileResourceE.setResourceId(
                        getOrCreateCertificationId(envId, c7nCertification, c7nCertification.getMetadata().getName()));
                devopsEnvFileResourceE.setResourceType(c7nCertification.getKind());
                devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
            } catch (Exception e) {
                devopsEnvFileErrorE.setError(e.getMessage());
                devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                throw new CommonException(e.getMessage());
            }
        });
    }

    private Long getOrCreateCertificationId(Long envId, C7nCertification c7nCertification, String certName) {
        CertificationE certificationE = certificationRepository
                .queryByEnvAndName(envId, certName);
        DevopsEnvironmentE environmentE = new DevopsEnvironmentE(envId);
        if (certificationE == null) {
            certificationE = new CertificationE();
            CertificationSpec certificationSpec = c7nCertification.getSpec();
            String domain = certificationSpec.getCommonName();
            List<String> dnsDomain = certificationSpec.getDnsNames();
            List<String> domains = new ArrayList<>();
            domains.add(domain);
            if (dnsDomain != null && !dnsDomain.isEmpty()) {
                domains.addAll(dnsDomain);
            }
            certificationE.setDomains(domains);
            certificationE.setEnvironmentE(environmentE);
            certificationE.setName(certName);
            certificationE.setStatus(CertificationStatus.OPERATING.getStatus());
            certificationE = certificationRepository.create(certificationE);
        }
        return certificationE.getId();
    }

    private void handlerServiceRelations(Map<String, String> objectPath,
                                         List<DevopsEnvFileResourceE> beforeSync,
                                         List<V1Service> v1Services,
                                         Long envId, Long projectId, String path) {
        List<String> beforeService = beforeSync.parallelStream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(SERVICE))
                .map(devopsEnvFileResourceE -> {
                    DevopsServiceE devopsServiceE = devopsServiceRepository
                            .query(devopsEnvFileResourceE.getResourceId());
                    if (devopsServiceE == null) {
                        throw new CommonException("the service in the file is not exist in devops database");
                    }
                    return devopsServiceE.getName();
                }).collect(Collectors.toList());
        //比较已存在网络和新增要处理的网络,获取新增网络，更新网络，删除网络
        List<V1Service> addV1Service = new ArrayList<>();
        List<V1Service> updateV1Service = new ArrayList<>();
        v1Services.parallelStream().forEach(v1Service -> {
            if (beforeService.contains(v1Service.getMetadata().getName())) {
                updateV1Service.add(v1Service);
                beforeService.remove(v1Service.getMetadata().getName());
            } else {
                addV1Service.add(v1Service);
            }
        });
        //新增service
        addService(objectPath, envId, projectId, path, addV1Service);
        //更新service
        updateService(objectPath, envId, projectId, path, updateV1Service);
        //删除service,和文件对象关联关系
        beforeService.stream().forEach(serviceName -> {
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(serviceName, envId);
            devopsServiceService.deleteDevopsServiceByGitOps(devopsServiceE.getId());
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsServiceE.getId(), SERVICE);
        });
    }

    private void updateService(Map<String, String> objectPath, Long envId, Long projectId, String path, List<V1Service> updateV1Service) {
        updateV1Service.stream()
                .forEach(v1Service -> {
                    DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, objectPath.get(TypeUtil.objToString(v1Service.hashCode())), path);
                    try {
                        DevopsServiceE devopsServiceE = devopsServiceRepository
                                .selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                        checkServiceName(devopsEnvFileErrorE, v1Service);
                        //初始化网络参数,更新网络和网络关联关系
                        DevopsServiceReqDTO devopsServiceReqDTO = getDevopsServiceDTO(
                                v1Service,
                                envId,
                                devopsEnvFileErrorE);
                        devopsServiceService.updateDevopsServiceByGitOps(
                                projectId, devopsServiceE.getId(), devopsServiceReqDTO);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                .queryByEnvIdAndResource(envId, devopsServiceE.getId(), v1Service.getKind());
                        updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceE,
                                v1Service.hashCode(), devopsServiceE.getId(), v1Service.getKind());
                    } catch (Exception e) {
                        devopsEnvFileErrorE.setError(e.getMessage());
                        devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                        throw new CommonException(e.getMessage(), e);
                    }
                });
    }

    private void addService(Map<String, String> objectPath, Long envId, Long projectId, String path, List<V1Service> addV1Service) {
        addV1Service.stream()
                .forEach(v1Service -> {
                    DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, objectPath.get(TypeUtil.objToString(v1Service.hashCode())), path);
                    try {
                        checkServiceName(devopsEnvFileErrorE, v1Service);
                        DevopsServiceE devopsServiceE = devopsServiceRepository
                                .selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                        DevopsServiceReqDTO devopsServiceReqDTO;
                        //初始化网络参数,创建时判断网络是否存在，存在则直接创建文件对象关联关系
                        if (devopsServiceE == null) {
                            devopsServiceReqDTO = getDevopsServiceDTO(
                                    v1Service,
                                    envId,
                                    devopsEnvFileErrorE);
                            devopsServiceService.insertDevopsServiceByGitOps(projectId, devopsServiceReqDTO);
                            devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                                    devopsServiceReqDTO.getName(), envId);
                        }
                        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                        devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                        devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
                        devopsEnvFileResourceE.setResourceId(devopsServiceE.getId());
                        devopsEnvFileResourceE.setResourceType(v1Service.getKind());
                        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                    } catch (Exception e) {
                        devopsEnvFileErrorE.setError(e.getMessage());
                        devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                        throw new CommonException(e.getMessage(), e);
                    }
                });
    }

    private void checkServiceName(
            DevopsEnvFileErrorE devopsEnvFileErrorE,
            V1Service v1Service) {
        try {
            DevopsServiceValidator.checkName(v1Service.getMetadata().getName());
        } catch (Exception e) {
            devopsEnvFileErrorE.setError(e.getMessage());
            devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
            throw new CommonException(e.getMessage());
        }
    }

    private ApplicationDeployDTO getApplicationDeployDTO(C7nHelmRelease c7nHelmRelease,
                                                         Long projectId, Long envId, String type) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = deployMsgHandlerService.getApplication(c7nHelmRelease.getSpec().getChartName(), projectId, organization.getId());
        if (applicationE == null) {
            throw new CommonException("the App: " + c7nHelmRelease.getSpec().getChartName() + "not exit in the devops-service");
        }
        ApplicationVersionE applicationVersionE = applicationVersionRepository
                .queryByAppAndVersion(applicationE.getId(), c7nHelmRelease.getSpec().getChartVersion());
        if (applicationVersionE == null) {
            applicationVersionE = getApplicationVersion(applicationE.getId(), applicationE.getCode(), organization.getCode(), projectE.getCode(), c7nHelmRelease.getSpec().getChartVersion(), c7nHelmRelease.getSpec().getValues());
        }

        ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO();
        applicationDeployDTO.setEnvironmentId(envId);
        applicationDeployDTO.setType(type);
        applicationDeployDTO.setValues(c7nHelmRelease.getSpec().getValues());
        applicationDeployDTO.setAppId(applicationE.getId());
        applicationDeployDTO.setAppVerisonId(applicationVersionE.getId());
        applicationDeployDTO.setInstanceName(c7nHelmRelease.getMetadata().getName());
        if (type.equals("update")) {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                    .selectByCode(c7nHelmRelease.getMetadata().getName(), envId);
            String deployValue = applicationInstanceRepository.queryValueByInstanceId(applicationInstanceE.getId());
            if (deployValue.equals(applicationDeployDTO.getValues()) && applicationVersionE.getId().equals(applicationInstanceE.getApplicationVersionE().getId())) {
                applicationDeployDTO.setIsNotChange(true);
            }
            applicationDeployDTO.setAppInstanceId(applicationInstanceE.getId());
        }
        return applicationDeployDTO;
    }


    private void handlerC7nReleaseRelations(Map<String, String> objectPath,
                                            List<DevopsEnvFileResourceE> beforeSync,
                                            List<C7nHelmRelease> c7nHelmReleases,
                                            Long envId, Long projectId, String path) {
        List<String> beforeC7nRelease = beforeSync.parallelStream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(C7NHELM_RELEASE))
                .map(devopsEnvFileResourceE -> {
                    ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                            .selectById(devopsEnvFileResourceE.getResourceId());
                    if (applicationInstanceE == null) {
                        throw new CommonException("the applicationInstance in the file is not exist in devops database");
                    }
                    return applicationInstanceE.getCode();
                }).collect(Collectors.toList());

        //比较已存在实例和新增要处理的实例,获取新增实例，更新实例，删除实例
        List<C7nHelmRelease> addC7nHelmRelease = new ArrayList<>();
        List<C7nHelmRelease> updateC7nHelmRelease = new ArrayList<>();
        c7nHelmReleases.parallelStream().forEach(c7nHelmRelease -> {
            if (beforeC7nRelease.contains(c7nHelmRelease.getMetadata().getName())) {
                updateC7nHelmRelease.add(c7nHelmRelease);
                beforeC7nRelease.remove(c7nHelmRelease.getMetadata().getName());
            } else {
                addC7nHelmRelease.add(c7nHelmRelease);
            }
        });

        //新增instance
        addC7nHelmRelease(objectPath, envId, projectId, path, addC7nHelmRelease);
        //更新instance
        updateC7nHelmRelease(objectPath, envId, projectId, path, updateC7nHelmRelease);
        //删除instance,和文件对象关联关系
        beforeC7nRelease.stream().forEach(releaseName -> {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName, envId);
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.queryByObject(ObjectType.INSTANCE.getType(), applicationInstanceE.getId());
            if (!devopsEnvCommandE.getCommandType().equals(CommandType.DELETE.getType())) {
                applicationInstanceService.instanceDeleteByGitOps(applicationInstanceE.getId());
            }
            devopsEnvFileResourceRepository
                    .deleteByEnvIdAndResource(envId, applicationInstanceE.getId(), C7NHELM_RELEASE);
        });
    }

    private void updateC7nHelmRelease(Map<String, String> objectPath, Long envId, Long projectId, String path, List<C7nHelmRelease> updateC7nHelmRelease) {
        updateC7nHelmRelease.stream()
                .forEach(c7nHelmRelease -> {
                    DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())), path);
                    try {
                        //初始化实例参数,更新时判断实例是否真的修改，没有修改则直接更新文件关联关系
                        ApplicationDeployDTO applicationDeployDTO = getApplicationDeployDTO(
                                c7nHelmRelease,
                                projectId,
                                envId,
                                "update");
                        if (applicationDeployDTO == null) {
                            return;
                        }
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.queryByObject(ObjectType.INSTANCE.getType(), applicationDeployDTO.getAppInstanceId());

                        if (!devopsEnvCommandE.getCommandType().equals(CommandType.SYNC.getType()) && !applicationDeployDTO.getIsNotChange()) {
                            applicationInstanceService
                                    .createOrUpdateByGitOps(applicationDeployDTO);
                        }
                        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                .queryByEnvIdAndResource(envId, applicationDeployDTO.getAppInstanceId(), c7nHelmRelease.getKind());
                        updateOrCreateFileResource(objectPath, envId,
                                devopsEnvFileResourceE,
                                c7nHelmRelease.hashCode(), applicationDeployDTO.getAppInstanceId(),
                                c7nHelmRelease.getKind());
                    } catch (Exception e) {
                        devopsEnvFileErrorE.setError(e.getMessage());
                        devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                        throw new CommonException(e.getMessage(), e);
                    }
                });
    }

    private void addC7nHelmRelease(Map<String, String> objectPath, Long envId, Long projectId, String path, List<C7nHelmRelease> addC7nHelmRelease) {
        addC7nHelmRelease.stream()
                .forEach(c7nHelmRelease -> {
                    DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())), path);
                    try {
                        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                                .selectByCode(c7nHelmRelease.getMetadata().getName(), envId);
                        ApplicationDeployDTO applicationDeployDTO;

                        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
                        //初始化实例参数,创建时判断实例是否存在，存在则直接创建文件对象关联关系
                        if (applicationInstanceE == null) {
                            applicationDeployDTO = getApplicationDeployDTO(
                                    c7nHelmRelease,
                                    projectId,
                                    envId,
                                    "create");
                            if (applicationDeployDTO == null) {
                                return;
                            }
                            applicationInstanceDTO = applicationInstanceService.createOrUpdateByGitOps(applicationDeployDTO);
                        } else {
                            applicationInstanceDTO.setId(applicationInstanceE.getId());
                        }
                        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                        devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                        devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())));
                        devopsEnvFileResourceE.setResourceId(applicationInstanceDTO.getId());
                        devopsEnvFileResourceE.setResourceType(c7nHelmRelease.getKind());
                        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                    } catch (Exception e) {
                        devopsEnvFileErrorE.setError(e.getMessage());
                        devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                        throw new CommonException(e.getMessage(), e);
                    }
                });
    }

    private void updateOrCreateFileResource(Map<String, String> objectPath,
                                            Long envId,
                                            DevopsEnvFileResourceE devopsEnvFileResourceE,
                                            Integer i, Long id, String kind) {
        if (devopsEnvFileResourceE != null) {
            devopsEnvFileResourceE.setFilePath(objectPath.get(
                    TypeUtil.objToString(i)));
            devopsEnvFileResourceRepository.updateFileResource(devopsEnvFileResourceE);
        } else {
            devopsEnvFileResourceE = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
            devopsEnvFileResourceE.setFilePath(objectPath.get(
                    TypeUtil.objToString(i)));
            devopsEnvFileResourceE.setResourceId(id);
            devopsEnvFileResourceE.setResourceType(kind);
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
        }
    }


    private void handlerIngressRelations(Map<String, String> objectPath,
                                         List<DevopsEnvFileResourceE> beforeSync,
                                         List<V1beta1Ingress> v1beta1Ingresses,
                                         Long envId, Long projectId, String path) {

        List<String> beforeIngress = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(INGRESS))
                .map(devopsEnvFileResourceE -> {
                    DevopsIngressDO devopsIngressDO = devopsIngressRepository
                            .getIngress(devopsEnvFileResourceE.getResourceId());
                    if (devopsIngressDO == null) {
                        throw new CommonException("the ingress in the file is not exist in devops database");
                    }
                    return devopsIngressDO.getName();
                }).collect(Collectors.toList());
        //比较已存在域名和新增要处理的域名,获取新增域名，更新域名，删除域名
        List<V1beta1Ingress> addV1beta1Ingress = new ArrayList<>();
        List<V1beta1Ingress> updateV1beta1Ingress = new ArrayList<>();
        v1beta1Ingresses.parallelStream().forEach(v1beta1Ingress -> {
            if (beforeIngress.contains(v1beta1Ingress.getMetadata().getName())) {
                updateV1beta1Ingress.add(v1beta1Ingress);
                beforeIngress.remove(v1beta1Ingress.getMetadata().getName());
            } else {
                addV1beta1Ingress.add(v1beta1Ingress);
            }
        });
        //删除ingress,删除文件对象关联关系
        beforeIngress.stream().forEach(ingressName -> {
            DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, ingressName);
            devopsIngressService.deleteIngressByGitOps(devopsIngressE.getId());
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsIngressE.getId(), INGRESS);
        });
        //新增ingress
        addIngress(objectPath, envId, projectId, path, addV1beta1Ingress);
        //更新ingress
        updateIngress(objectPath, envId, projectId, path, updateV1beta1Ingress);
    }

    private void addIngress(Map<String, String> objectPath, Long envId, Long projectId, String path, List<V1beta1Ingress> addV1beta1Ingress) {
        addV1beta1Ingress.stream()
                .forEach(v1beta1Ingress -> {
                    DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())), path);
                    try {
                        checkIngressAppVersion(devopsEnvFileErrorE, v1beta1Ingress);
                        DevopsIngressE devopsIngressE = devopsIngressRepository
                                .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        DevopsIngressDTO devopsIngressDTO;
                        //初始化ingress对象参数,存在ingress则直接创建文件对象关联关系
                        if (devopsIngressE == null) {
                            devopsIngressDTO = getDevopsIngressDTO(
                                    v1beta1Ingress,
                                    envId);
                            if (!devopsIngressDTO.getPathList().stream()
                                    .allMatch(t ->
                                            devopsIngressRepository.checkIngressAndPath(null, devopsIngressDTO.getDomain(), t.getPath()))) {
                                throw new CommonException(GitOpsObjectError.INGRESS_DOMAIN_PATH_IS_EXIST.getError());
                            }
                            devopsIngressService.addIngressByGitOps(devopsIngressDTO, projectId);
                            devopsIngressE = devopsIngressRepository
                                    .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        }
                        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                        devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                        devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                        devopsEnvFileResourceE.setResourceId(devopsIngressE.getId());
                        devopsEnvFileResourceE.setResourceType(v1beta1Ingress.getKind());
                        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                    } catch (Exception e) {
                        devopsEnvFileErrorE.setError(e.getMessage());
                        devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                        throw new CommonException(e.getMessage(), e);
                    }
                });
    }

    private void updateIngress(Map<String, String> objectPath, Long envId, Long projectId, String path, List<V1beta1Ingress> updateV1beta1Ingress) {
        updateV1beta1Ingress.stream()
                .forEach(v1beta1Ingress -> {
                    DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())), path);
                    try {
                        DevopsIngressE devopsIngressE = devopsIngressRepository
                                .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        checkIngressAppVersion(devopsEnvFileErrorE, v1beta1Ingress);
                        //初始化ingress对象参数,更新ingress并更新文件对象关联关系
                        DevopsIngressDTO devopsIngressDTO = getDevopsIngressDTO(
                                v1beta1Ingress,
                                envId);
                        if (!devopsIngressDTO.getPathList().stream()
                                .allMatch(t ->
                                        devopsIngressRepository.checkIngressAndPath(devopsIngressE.getId(), devopsIngressDTO.getDomain(), t.getPath()))) {
                            throw new CommonException(GitOpsObjectError.INGRESS_DOMAIN_PATH_IS_EXIST.getError());
                        }
                        devopsIngressService.updateIngressByGitOps(devopsIngressE.getId(), devopsIngressDTO, projectId);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                .queryByEnvIdAndResource(envId, devopsIngressE.getId(), v1beta1Ingress.getKind());
                        updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceE,
                                v1beta1Ingress.hashCode(), devopsIngressE.getId(), v1beta1Ingress.getKind());
                    } catch (Exception e) {
                        devopsEnvFileErrorE.setError(e.getMessage());
                        devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                        throw new CommonException(e.getMessage(), e);
                    }
                });
    }


    private void checkIngressAppVersion(
            DevopsEnvFileErrorE devopsEnvFileErrorE,
            V1beta1Ingress v1beta1Ingress) {
        try {
            DevopsIngressValidator.checkIngressName(v1beta1Ingress.getMetadata().getName());
        } catch (Exception e) {
            devopsEnvFileErrorE.setError(e.getMessage());
            throw new CommonException(e.getMessage());
        }
    }


    private DevopsIngressDTO getDevopsIngressDTO(V1beta1Ingress v1beta1Ingress,
                                                 Long envId) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setDomain(v1beta1Ingress.getSpec().getRules().get(0).getHost()
        );
        devopsIngressDTO.setName(v1beta1Ingress.getMetadata().getName());
        devopsIngressDTO.setEnvId(envId);
        List<String> pathCheckList = new ArrayList<>();
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = new ArrayList<>();
        List<V1beta1HTTPIngressPath> paths = v1beta1Ingress.getSpec().getRules().get(0).getHttp().getPaths();
        if (paths == null) {
            throw new CommonException(GitOpsObjectError.INGRESS_PATH_IS_EMPTY.getError());
        }
        for (V1beta1HTTPIngressPath v1beta1HTTPIngressPath : paths) {
            String path = v1beta1HTTPIngressPath.getPath();
            try {
                DevopsIngressValidator.checkPath(path);
                if (pathCheckList.contains(path)) {
                    throw new CommonException(GitOpsObjectError.INGRESS_PATH_DUPLICATED.getError());
                } else {
                    pathCheckList.add(path);
                }
            } catch (Exception e) {
                throw new CommonException(e.getMessage());
            }
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
            V1beta1IngressBackend backend = v1beta1HTTPIngressPath.getBackend();
            String serviceName = backend.getServiceName();
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(
                    serviceName, devopsEnvironmentE.getCode());
            if (devopsServiceE == null) {
                throw new CommonException(GitOpsObjectError.SERVICE_RELEATED_INGRESS_NOT_FOUND.getError() + serviceName);
            }
            Long servicePort;
            IntOrString backendServicePort = backend.getServicePort();
            if (backendServicePort.isInteger()) {
                servicePort = backendServicePort.getIntValue().longValue();
                if (devopsServiceE.getPorts().parallelStream()
                        .map(PortMapE::getPort).noneMatch(t -> t.equals(servicePort))) {
                    throw new CommonException(GitOpsObjectError.INGRESS_PATH_PORT_NOT_BELONG_TO_SERVICE.getError(),
                            serviceName, servicePort);
                }
            } else {
                servicePort = devopsServiceE.getPorts().get(0).getPort();
            }
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
            devopsIngressPathDTO.setPath(path);
            devopsIngressPathDTO.setServicePort(servicePort);
            devopsIngressPathDTO.setServiceId(devopsServiceE.getId());
            devopsIngressPathDTOS.add(devopsIngressPathDTO);
        }
        devopsIngressDTO.setPathList(devopsIngressPathDTOS);
        return devopsIngressDTO;
    }


    private DevopsServiceReqDTO getDevopsServiceDTO(V1Service v1Service,
                                                    Long envId,
                                                    DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO();
        if (v1Service.getSpec().getExternalIPs() != null) {
            devopsServiceReqDTO.setExternalIp(String.join(",", v1Service.getSpec().getExternalIPs()));
        }
        devopsServiceReqDTO.setName(v1Service.getMetadata().getName());
        devopsServiceReqDTO.setType(v1Service.getSpec().getType());
        devopsServiceReqDTO.setEnvId(envId);

        List<PortMapE> portMapList = v1Service.getSpec().getPorts().parallelStream()
                .map(t -> {
                    PortMapE portMap = new PortMapE();
                    portMap.setName(t.getName());
                    if (t.getNodePort() != null) {
                        portMap.setNodePort(t.getNodePort().longValue());
                    }
                    portMap.setPort(t.getPort().longValue());
                    portMap.setProtocol(t.getProtocol());
                    portMap.setTargetPort(TypeUtil.objToString(t.getTargetPort()));
                    return portMap;
                }).collect(Collectors.toList());
        devopsServiceReqDTO.setPorts(portMapList);

        if (v1Service.getMetadata().getAnnotations() != null) {
            String instancesCode = v1Service.getMetadata().getAnnotations()
                    .get("choerodon.io/network-service-instances");
            if (!instancesCode.isEmpty()) {
                List<Long> instanceIdList = Arrays.stream(instancesCode.split("\\+")).parallel()
                        .map(t -> getInstanceId(t, envId, devopsServiceReqDTO, devopsEnvFileErrorE))
                        .collect(Collectors.toList());
                devopsServiceReqDTO.setAppInstance(instanceIdList);
            }
        }
        if (v1Service.getSpec().getSelector() != null) {
            devopsServiceReqDTO.setLabel(v1Service.getSpec().getSelector());
        }
        return devopsServiceReqDTO;
    }

    private Long getInstanceId(String instanceCode, Long envId, DevopsServiceReqDTO devopsServiceReqDTO,
                               DevopsEnvFileErrorE devopsEnvFileErrorE) {
        try {
            ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(instanceCode, envId);
            if (devopsServiceReqDTO.getAppId() == null) {
                devopsServiceReqDTO.setAppId(instanceE.getApplicationE().getId());
            }
            String logMsg = devopsEnvFileErrorE.getError();
            if (!devopsServiceReqDTO.getAppId().equals(instanceE.getApplicationE().getId())
                    && (logMsg == null || !logMsg.contains(GitOpsObjectError.INSTANCE_APP_ID_NOT_SAME.getError()))) {
                if (logMsg == null) {
                    devopsEnvFileErrorE.setError(GitOpsObjectError.INSTANCE_APP_ID_NOT_SAME.getError());
                } else {
                    devopsEnvFileErrorE.setError(GitOpsObjectError.INSTANCE_APP_ID_NOT_SAME.getError() + logMsg);
                }
                throw new CommonException(devopsEnvFileErrorE.getError());
            }
            return instanceE.getId();
        } catch (Exception e) {
            String logMsg = devopsEnvFileErrorE.getError();
            if (logMsg == null) {
                devopsEnvFileErrorE.setError(GitOpsObjectError.INSTANCE_RELEATED_SERVICE_NOT_FOUND.getError() + instanceCode);
            } else if (!logMsg.contains(GitOpsObjectError.INSTANCE_RELEATED_SERVICE_NOT_FOUND.getError())) {
                devopsEnvFileErrorE.setError(logMsg + GitOpsObjectError.INSTANCE_RELEATED_SERVICE_NOT_FOUND.getError() + instanceCode);
            } else {
                devopsEnvFileErrorE.setError(logMsg + ", " + instanceCode);
            }
            throw new CommonException(devopsEnvFileErrorE.getError());
        }

    }


    public ApplicationVersionE getApplicationVersion(Long appId, String appCode, String orgCode, String projectCode, String version, String values) {
        ApplicationVersionE applicationVersionE = new ApplicationVersionE();
        ApplicationVersionValueE applicationVersionValueE = new ApplicationVersionValueE();
        applicationVersionE.initApplicationEById(appId);
        String image = String.format("%s%s%s%s%s%s%s%s%s", harborConfigurationProperties.getBaseUrl(),
                System.getProperty("file.separator"),
                orgCode,
                "-",
                projectCode,
                System.getProperty("file.separator"),
                appCode,
                ":",
                version
        );
        applicationVersionE.setImage(image);
        applicationVersionE.setVersion(version);
        applicationVersionE.initApplicationVersionReadmeV("");
        applicationVersionE
                .setRepository("/" + orgCode + "/" + projectCode + "/");
        applicationVersionValueE.setValue(values);
        applicationVersionE.initApplicationVersionValueE(
                applicationVersionValueRepository.create(applicationVersionValueE).getId());
        return applicationVersionRepository.create(applicationVersionE);

    }

    public String getFileLatestCommit(String path, String filePath) {
        String[] fileName = filePath.split("/");
        return GitUtil.getLog(path, fileName[fileName.length - 1]);
    }

    public DevopsEnvFileErrorE getDevopsFileError(Long envId, String filePath, String path) {
        DevopsEnvFileErrorE devopsEnvFileErrorE = devopsEnvFileErrorRepository.queryByEnvIdAndFilePath(envId, filePath);
        if (devopsEnvFileErrorE == null) {
            devopsEnvFileErrorE = new DevopsEnvFileErrorE();
            devopsEnvFileErrorE.setFilePath(filePath);
            devopsEnvFileErrorE.setEnvId(envId);
            devopsEnvFileErrorE.setCommit(getFileLatestCommit(path + GIT_SUFFIX, filePath));
        } else {
            devopsEnvFileErrorE.setFilePath(filePath);
            devopsEnvFileErrorE.setCommit(getFileLatestCommit(path + GIT_SUFFIX, filePath));
        }
        return devopsEnvFileErrorE;
    }

    public void hasSameObject(Long envId, Map<String, String> objectPath, String objectName, String objectKind, Long objectId, String path, Integer objectHashCode) {
        DevopsEnvFileErrorE devopsEnvFileErrorE = getDevopsFileError(envId, objectPath.get(TypeUtil.objToString(objectHashCode)), path);
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, objectId, objectKind);
        if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath().equals(objectPath.get(TypeUtil.objToString(objectHashCode)))) {
            createDevopsFileError(devopsEnvFileErrorE, objectName);
        }
    }

    public void createDevopsFileError(DevopsEnvFileErrorE devopsEnvFileErrorE, String objectName) {
        devopsEnvFileErrorE.setError(GitOpsObjectError.OBJECT_EXIST.getError() + objectName);
        devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
        throw new CommonException(GitOpsObjectError.OBJECT_EXIST.getError() + objectName);
    }

    private void formatC7nRelease(C7nHelmRelease c7nHelmRelease) {
        if (c7nHelmRelease.getMetadata() == null) {
            throw new CommonException(GitOpsObjectError.RELEASE_APIVERSION_NOT_FOUND.getError());
        } else {
            if (c7nHelmRelease.getMetadata().getName() == null) {
                throw new CommonException(GitOpsObjectError.RELEASE_NAME_NOT_FOUND.getError());
            }
        }
        if (c7nHelmRelease.getSpec() == null) {
            throw new CommonException(GitOpsObjectError.RELEASE_SPEC_NOT_FOUND.getError());
        } else {
            if (c7nHelmRelease.getSpec().getChartName() == null) {
                throw new CommonException(GitOpsObjectError.RELEASE_CHART_NAME_NOT_FOUND.getError());
            }
            if (c7nHelmRelease.getSpec().getChartVersion() == null) {
                throw new CommonException(GitOpsObjectError.RELEASE_CHART_VERSION_NOT_FOUND.getError());
            }
            if (c7nHelmRelease.getSpec().getRepoUrl() == null) {
                throw new CommonException(GitOpsObjectError.RELEASE_REPOURL_NOT_FOUND.getError());
            }
        }
        if (c7nHelmRelease.getApiVersion() == null) {
            throw new CommonException(GitOpsObjectError.RELEASE_APIVERSION_NOT_FOUND.getError());
        }
    }

    private void formatService(V1Service v1Service) {
        if (v1Service.getMetadata() == null) {
            throw new CommonException(GitOpsObjectError.SERVICE_METADATA_NOT_FOUND.getError());
        } else {
            if (v1Service.getMetadata().getName() == null) {
                throw new CommonException(GitOpsObjectError.SERVICE_NAME_NOT_FOUND.getError());
            }
        }
        if (v1Service.getSpec() == null) {
            throw new CommonException(GitOpsObjectError.SERVICE_SPEC_NOT_FOUND.getError());
        } else {
            checkV1ServicePorts(v1Service);
        }
        if (v1Service.getApiVersion() == null) {
            throw new CommonException(GitOpsObjectError.SERVICE_APIVERSION_NOT_FOUND.getError());

        }
    }

    private void checkV1ServicePorts(V1Service v1Service) {
        List<V1ServicePort> v1ServicePorts = v1Service.getSpec().getPorts();
        if (v1ServicePorts.isEmpty()) {
            throw new CommonException(GitOpsObjectError.SERVICE_PORTS_NOT_FOUND.getError());
        } else {
            for (V1ServicePort v1ServicePort : v1ServicePorts) {
                if (v1ServicePort.getName() == null) {
                    throw new CommonException(GitOpsObjectError.SERVICE_PORTS_NAME_NOT_FOUND.getError());

                }
                if (v1ServicePort.getPort() == null) {
                    throw new CommonException(GitOpsObjectError.SERVICE_PORTS_PORT_NOT_FOUND.getError());

                }
                if (v1ServicePort.getTargetPort() == null) {
                    throw new CommonException(GitOpsObjectError.SERVICE_PORTS_TARGET_PORT.getError());
                }
            }
        }
        if (v1Service.getSpec().getType() == null) {
            throw new CommonException(GitOpsObjectError.SERVICE_TYPE_NOT_FOUND.getError());
        }
    }

    private void formatIngress(V1beta1Ingress v1beta1Ingress) {
        if (v1beta1Ingress == null) {
            throw new CommonException(GitOpsObjectError.INGRESS_META_DATA_NOT_FOUND.getError());
        } else {
            if (v1beta1Ingress.getMetadata().getName() == null) {
                throw new CommonException(GitOpsObjectError.INGRESS_NAME_NOT_FOUND.getError());
            }
        }
        if (v1beta1Ingress.getSpec() == null) {
            throw new CommonException(GitOpsObjectError.INGRESS_SPEC_NOT_FOUND.getError());
        } else {
            checkV1beta1IngressRules(v1beta1Ingress);
        }
        if (v1beta1Ingress.getApiVersion() == null) {
            throw new CommonException(GitOpsObjectError.INGRESS_APIVERSION_NOT_FOUND.getError());

        }

    }


    private boolean getDevopsSyncTag(PushWebHookDTO pushWebHookDTO) {
        return devopsGitRepository.getGitLabTags(pushWebHookDTO.getProjectId(), pushWebHookDTO.getUserId()).parallelStream().noneMatch(tagDO -> tagDO.getName().equals(GitUtil.DEVOPS_GITOPS_TAG));

    }


    private void checkV1beta1IngressRules(V1beta1Ingress v1beta1Ingress) {
        List<V1beta1IngressRule> v1beta1IngressRules = v1beta1Ingress.getSpec().getRules();
        if (v1beta1IngressRules.isEmpty()) {
            throw new CommonException(GitOpsObjectError.INGRESS_RULES_NOT_FOUND.getError());
        } else {
            for (V1beta1IngressRule v1beta1IngressRule : v1beta1IngressRules) {
                if (v1beta1IngressRule.getHost() == null) {
                    throw new CommonException(GitOpsObjectError.INGRESS_RULE_HOST_NOT_FOUND.getError());
                }
                if (v1beta1IngressRule.getHttp() == null) {
                    throw new CommonException(GitOpsObjectError.INGRESS_RULE_HTTP_NOT_FOUND.getError());
                } else {
                    checkV1beta1HTTPIngressPaths(v1beta1IngressRule);
                }
            }
        }
    }

    private void checkV1beta1HTTPIngressPaths(V1beta1IngressRule v1beta1IngressRule) {
        List<V1beta1HTTPIngressPath> v1beta1HTTPIngressPaths = v1beta1IngressRule.getHttp().getPaths();
        if (v1beta1HTTPIngressPaths.isEmpty()) {
            throw new CommonException(GitOpsObjectError.INGRESS_PATHS_NOT_FOUND.getError());
        } else {
            for (V1beta1HTTPIngressPath v1beta1HTTPIngressPath : v1beta1HTTPIngressPaths) {
                if (v1beta1HTTPIngressPath.getPath() == null) {
                    throw new CommonException(GitOpsObjectError.INGRESS_PATHS_PATH_NOT_FOUND.getError());
                }
                if (v1beta1HTTPIngressPath.getBackend() == null) {
                    throw new CommonException(GitOpsObjectError.INGRESS_PATHS_BACKEND_NOT_FOUND.getError());
                } else {
                    checkBackend(v1beta1HTTPIngressPath);
                }
            }
        }
    }

    private void checkBackend(V1beta1HTTPIngressPath v1beta1HTTPIngressPath) {
        if (v1beta1HTTPIngressPath.getBackend().getServiceName() == null) {
            throw new CommonException(GitOpsObjectError.INGRESS_BACKEND_SERVICE_NAME_NOT_FOUND.getError());
        }
        if (v1beta1HTTPIngressPath.getBackend().getServicePort() == null) {
            throw new CommonException(GitOpsObjectError.INGRESS_BACKEND_SERVICE_PORT_NOT_FOUND.getError());
        }
    }

    @Override
    public void initMockService(SagaClient sagaClient, ApplicationInstanceService applicationInstanceService,DevopsServiceService devopsServiceService, DevopsIngressService devopsIngressService) {
        this.sagaClient = sagaClient;
        this.devopsIngressService = devopsIngressService;
        this.devopsServiceService = devopsServiceService;
        this.applicationInstanceService = applicationInstanceService;
    }
}
