package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1HTTPIngressPath;
import io.kubernetes.client.models.V1beta1Ingress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.saga.SagaClient;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.saga.Saga;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.entity.gitlab.CompareResultsE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.handler.SerializableChain;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.DateUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
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
    private static final String NO_COMMIT_SHA = "0000000000000000000000000000000000000000";
    private static final String REF_HEADS = "refs/heads/";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    private static final String PATH_DUPLICATED = "error.path.duplicated";
    private ObjectMapper objectMapper = new ObjectMapper();
    @Value("${services.gitlab.url}")
    private String gitlabUrl;


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
    private DevopsEnvFileLogRepository devopsEnvFileLogRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsServiceService devopsServiceService;


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
    @Saga(code = "devops-sync-gitops", description = "gitops", inputSchema = "{}")
    public void fileResourceSyncSaga(PushWebHookDTO pushWebHookDTO, String token) {
        pushWebHookDTO.setToken(token);
        String input;
        try {
            input = objectMapper.writeValueAsString(pushWebHookDTO);
            sagaClient.startSaga("devops-sync-gitops", new StartInstanceDTO(input, "", ""));
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public void fileResourceSync(PushWebHookDTO pushWebHookDTO) {
        Integer gitLabProjectId = pushWebHookDTO.getProjectId();
        Integer gitLabUserId = pushWebHookDTO.getUserId();
        List<String> operationFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryByToken(pushWebHookDTO.getToken());
        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String path = String.format("gitops/%s/%s/%s",
                organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
        String url = "git@" + gitlabUrl + ":" + projectE.getCode() + "/" + devopsEnvironmentE.getCode() + ".git";
        try {
            BranchDO branch = devopsGitRepository.getBranch(gitLabProjectId, "master");
            String masterSha = branch.getCommit().getId();
            handDevopsEnvGitRepository(path, url, devopsEnvironmentE.getEnvIdRsa(), masterSha);
            CompareResultsE compareResultsE = devopsGitRepository
                    .getCompareResults(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, masterSha);
            List<DevopsEnvFileResourceE> beforeSync = new ArrayList<>();
            compareResultsE.getDiffs().forEach(t -> {
                if (t.getDeletedFile()) {
                    if (t.getNewPath().contains("yaml") || t.getNewPath().contains("yml")) {
                        deletedFiles.add(t.getNewPath());
                    }
                } else {
                    if (t.getNewPath().contains("yaml") || t.getNewPath().contains("yml")) {
                        operationFiles.add(t.getNewPath());

                    }
                }
                DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndPath(devopsEnvironmentE.getId(), t.getNewPath());
                if (devopsEnvFileResourceE != null) {
                    beforeSync.add(devopsEnvFileResourceE);
                }
            });
            if (operationFiles.isEmpty() && deletedFiles.isEmpty()) {
                return;
            }

            List<C7nHelmRelease> c7nHelmReleases = new ArrayList<>();
            List<V1Service> v1Services = new ArrayList<>();
            List<V1beta1Ingress> v1beta1Ingresses = new ArrayList<>();
            Map<String, String> objectPath = new HashMap<>();
            DevopsEnvFileLogE devopsEnvFileLogE = new DevopsEnvFileLogE();
            devopsEnvFileLogE.setCommitSha(masterSha);
            devopsEnvFileLogE.setEnvId(devopsEnvironmentE.getId());
            handleFilesToObject(operationFiles, path, c7nHelmReleases,
                    v1Services, v1beta1Ingresses,
                    objectPath, devopsEnvFileLogE);
            if (devopsEnvFileLogE.getMessage() != null) {
                devopsEnvFileLogRepository.create(devopsEnvFileLogE);
                return;
            }
            handlerObJectReleations(objectPath, beforeSync, c7nHelmReleases, v1Services, v1beta1Ingresses, devopsEnvironmentE.getId(), devopsEnvironmentE.getProjectE().getId(), devopsEnvFileLogE);
            if (devopsEnvFileLogE.getMessage() != null) {
                devopsEnvFileLogRepository.create(devopsEnvFileLogE);
                return;
            }
            devopsGitRepository.deleteTag(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, gitLabUserId);
            devopsGitRepository.createTag(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, masterSha, gitLabUserId);
            deployService.sendCommand(devopsEnvironmentE);
        } catch (Exception e) {
            LOGGER.info("File Resource Sync File Changes Fail!");
        }

        // do sth to files
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
        if (!file.exists()) {
            gitUtil.cloneBySsh(path, url);
            gitUtil.checkout(path + "/.git", commit);
        } else {
            gitUtil.checkout(path + "/.git", "master");
            gitUtil.pullBySsh(path + "/.git");
            gitUtil.checkout(path + "/.git", commit);
        }
    }

    private void handleFilesToObject(List<String> files,
                                     String path,
                                     List<C7nHelmRelease> c7nHelmReleases,
                                     List<V1Service> v1Services,
                                     List<V1beta1Ingress> v1beta1Ingresses,
                                     Map<String, String> objectPath,
                                     DevopsEnvFileLogE devopsEnvFileLogE) {
        files.parallelStream().forEach(filePath -> {
            File file = new File(String.format("%s/%s", path, filePath));
            io.choerodon.devops.domain.application.handler.SerializableChain serializableChain = new SerializableChain();
            serializableChain.createChain();
            serializableChain.handler(file, filePath, objectPath, c7nHelmReleases,
                    v1Services, v1beta1Ingresses, devopsEnvFileLogE);
        });
    }


    private void handlerObJectReleations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<C7nHelmRelease> c7nHelmReleases, List<V1Service> v1Services, List<V1beta1Ingress> v1beta1Ingresses, Long envId, Long projectId, DevopsEnvFileLogE devopsEnvFileLogE) {
        handlerC7nReleaseRelations(objectPath, beforeSync, c7nHelmReleases, envId, projectId, devopsEnvFileLogE);
        handlerIngressRelations(objectPath, beforeSync, v1beta1Ingresses, envId, projectId, devopsEnvFileLogE);
    }

    public ApplicationDeployDTO getApplicationDeployDTO(C7nHelmRelease c7nHelmRelease, Long projectId, Long envId, String type, DevopsEnvFileLogE devopsEnvFileLogE, String filePath) {
        ApplicationE applicationE = applicationRepository.queryByCode(c7nHelmRelease.getSpec().getChartName(), projectId);
        ApplicationVersionE applicationVersionE = applicationVersionRepository.queryByAppAndVersion(applicationE.getId(), c7nHelmRelease.getSpec().getChartVersion());
        if (applicationVersionE == null) {
            devopsEnvFileLogE.setMessage("release version not exist!");
            devopsEnvFileLogE.setFilePath(filePath);
            return null;
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
            applicationDeployDTO.setAppInstanceId(applicationInstanceE.getId());
        }
        return applicationDeployDTO;
    }


    public void handlerC7nReleaseRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<C7nHelmRelease> c7nHelmReleases, Long envId, Long projectId, DevopsEnvFileLogE devopsEnvFileLogE) {
        List<String> beforeC7nRelease = beforeSync.parallelStream().filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals("C7NHelmRelease")).map(devopsEnvFileResourceE -> {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(devopsEnvFileResourceE.getResourceId());
            return applicationInstanceE.getCode();
        }).collect(Collectors.toList());
        c7nHelmReleases.parallelStream().filter(c7nHelmRelease -> !beforeC7nRelease.contains(c7nHelmRelease.getMetadata().getName())).forEach(c7nHelmRelease -> {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(c7nHelmRelease.getMetadata().getName(), envId);
            ApplicationDeployDTO applicationDeployDTO;
            ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
            if (applicationInstanceE == null) {
                applicationDeployDTO = getApplicationDeployDTO(c7nHelmRelease, projectId, envId, "create", devopsEnvFileLogE, objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())));
                if (devopsEnvFileLogE.getMessage() != null) {
                    return;
                }
                applicationInstanceDTO = applicationInstanceService.create(applicationDeployDTO, true);
            } else {
                applicationInstanceDTO.setId(applicationInstanceE.getId());
            }
            DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
            devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())));
            devopsEnvFileResourceE.setResourceId(applicationInstanceDTO.getId());
            devopsEnvFileResourceE.setResourceType(c7nHelmRelease.getKind());
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
        });
        c7nHelmReleases.parallelStream().filter(c7nHelmRelease -> beforeC7nRelease.contains(c7nHelmRelease.getMetadata().getName())).forEach(c7nHelmRelease -> {
            beforeC7nRelease.remove(c7nHelmRelease.getMetadata().getName());
            ApplicationDeployDTO applicationDeployDTO = getApplicationDeployDTO(c7nHelmRelease, projectId, envId, "update", devopsEnvFileLogE, objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())));
            if (devopsEnvFileLogE.getMessage() != null) {
                return;
            }
            ApplicationInstanceDTO applicationInstanceDTO = applicationInstanceService.create(applicationDeployDTO, true);
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, applicationInstanceDTO.getId(), c7nHelmRelease.getKind());
            if (devopsEnvFileResourceE != null) {
                devopsEnvFileResourceE.setFilePath(objectPath.get(c7nHelmRelease.hashCode()));
                devopsEnvFileResourceRepository.updateFileResource(devopsEnvFileResourceE);
            } else {
                devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())));
                devopsEnvFileResourceE.setResourceId(applicationInstanceDTO.getId());
                devopsEnvFileResourceE.setResourceType(c7nHelmRelease.getKind());
                devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
            }
        });
        beforeC7nRelease.parallelStream().forEach(releaseName -> {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName, envId);
            applicationInstanceService.instanceDelete(applicationInstanceE.getId(), true);
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, applicationInstanceE.getId(), "C7NHelmRelease");
        });
    }


    public void handlerIngressRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<V1beta1Ingress> v1beta1Ingresses, Long envId, Long projectId, DevopsEnvFileLogE devopsEnvFileLogE) {
        List<String> beforeIngress = beforeSync.parallelStream().filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals("Ingress")).map(devopsEnvFileResourceE -> {
            DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(devopsEnvFileResourceE.getResourceId());
            return devopsIngressDO.getName();
        }).collect(Collectors.toList());
        v1beta1Ingresses.parallelStream().filter(v1beta1Ingress -> !beforeIngress.contains(v1beta1Ingress.getMetadata().getName())).forEach(v1beta1Ingress -> {
            try {
                DevopsIngressValidator.checkAppVersion(v1beta1Ingress.getMetadata().getName());
            } catch (Exception e) {
                devopsEnvFileLogE.setMessage(e.getMessage());
                devopsEnvFileLogE.setFilePath(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                return;
            }
            DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
            DevopsIngressDTO devopsIngressDTO;
            if (devopsIngressE == null) {
                devopsIngressDTO = getDevopsIngressDTO(v1beta1Ingress, envId, devopsEnvFileLogE, objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                if (devopsEnvFileLogE.getMessage() != null) {
                    return;
                }
                devopsIngressService.addIngress(devopsIngressDTO, projectId, true);
                devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
            }
            DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
            devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
            devopsEnvFileResourceE.setResourceId(devopsIngressE.getId());
            devopsEnvFileResourceE.setResourceType(v1beta1Ingress.getKind());
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
        });
        v1beta1Ingresses.parallelStream().filter(v1beta1Ingress -> beforeIngress.contains(v1beta1Ingress.getMetadata().getName())).forEach(v1beta1Ingress -> {
            beforeIngress.remove(v1beta1Ingress.getMetadata().getName());
            try {
                DevopsIngressValidator.checkAppVersion(v1beta1Ingress.getMetadata().getName());
            } catch (Exception e) {
                devopsEnvFileLogE.setMessage(e.getMessage());
                devopsEnvFileLogE.setFilePath(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                return;
            }
            DevopsIngressDTO devopsIngressDTO = getDevopsIngressDTO(v1beta1Ingress, envId, devopsEnvFileLogE, objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
            if (devopsEnvFileLogE.getMessage() != null) {
                return;
            }
            DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
            devopsIngressService.updateIngress(devopsIngressE.getId(), devopsIngressDTO, projectId, true);
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, devopsIngressE.getId(), v1beta1Ingress.getKind());
            if (devopsEnvFileResourceE != null) {
                devopsEnvFileResourceE.setFilePath(objectPath.get(v1beta1Ingress.hashCode()));
                devopsEnvFileResourceRepository.updateFileResource(devopsEnvFileResourceE);
            } else {
                devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                devopsEnvFileResourceE.setResourceId(devopsIngressE.getId());
                devopsEnvFileResourceE.setResourceType(v1beta1Ingress.getKind());
                devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
            }
        });
        beforeIngress.parallelStream().forEach(ingressName -> {
            DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, ingressName);
            devopsIngressService.deleteIngress(devopsIngressE.getId(), true);
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsIngressE.getId(), "Ingress");
        });

    }


    public DevopsIngressDTO getDevopsIngressDTO(V1beta1Ingress v1beta1Ingress, Long envId, DevopsEnvFileLogE devopsEnvFileLogE, String filePath) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setDomain(v1beta1Ingress.getSpec().getRules().get(0).getHost()
        );
        devopsIngressDTO.setName(v1beta1Ingress.getMetadata().getName());
        devopsIngressDTO.setEnvId(envId);
        List<String> pathCheckList = new ArrayList<>();
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = new ArrayList<>();
        for (V1beta1HTTPIngressPath v1beta1HTTPIngressPath : v1beta1Ingress.getSpec().getRules().get(0).getHttp().getPaths()) {
            try {
                DevopsIngressValidator.checkPath(v1beta1HTTPIngressPath.getPath());
                if (pathCheckList.contains(v1beta1HTTPIngressPath.getPath())) {
                    throw new CommonException(PATH_DUPLICATED);
                } else {
                    pathCheckList.add(v1beta1HTTPIngressPath.getPath());
                }
            } catch (Exception e) {
                devopsEnvFileLogE.setMessage(e.getMessage());
                devopsEnvFileLogE.setFilePath(filePath);
                return null;
            }
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(v1beta1HTTPIngressPath.getBackend().getServiceName(), devopsEnvironmentE.getCode());
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
            devopsIngressPathDTO.setPath(v1beta1HTTPIngressPath.getPath());

            devopsIngressPathDTO.setServiceId(devopsServiceE.getId());
            devopsIngressPathDTOS.add(devopsIngressPathDTO);
        }
        devopsIngressDTO.setPathList(devopsIngressPathDTOS);
        return devopsIngressDTO;
    }
}
