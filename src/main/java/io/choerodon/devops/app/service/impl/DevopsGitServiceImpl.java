package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
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
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.DateUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandType;
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
    private static final String NO_COMMIT_SHA = "0000000000000000000000000000000000000000";
    private static final String REF_HEADS = "refs/heads/";
    private static final String PATH_DUPLICATED = "error.path.duplicated";
    private static final String INSTANCE_APP_ID_NOT_SAME = "The instance is not belong to the same application! \n";
    private static final String INSTANCE_NOT_FOUND = "The instances not found: ";

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    private Yaml yaml = new Yaml();
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
        //TODO 在收到环境库webhook 之后应该在env commit 记录表中插入提交记录，并且更新对应环境中git库最新提交字段
        try {
            input = objectMapper.writeValueAsString(pushWebHookDTO);
            sagaClient.startSaga("devops-sync-gitops", new StartInstanceDTO(input, "", ""));
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public void fileResourceSync(PushWebHookDTO pushWebHookDTO) {

        //TODO 在解释的第一步应该拉去最新提交，然后判断最新提交是否和tag一致，否则不进行之后操作
        Integer gitLabProjectId = pushWebHookDTO.getProjectId();
        Integer gitLabUserId = pushWebHookDTO.getUserId();


        List<String> operationFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();


        //根据token查出环境
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryByToken(pushWebHookDTO.getToken());

        //文件和hash值map映射关系
        Map<String, String> files = new HashMap<>();


        pushWebHookDTO.getCommits().parallelStream().forEach(commitDTO -> {

            Set<String> fileNames = new HashSet<>();

            //新增和修改的文件
            fileNames.addAll(commitDTO.getAdded());
            fileNames.addAll(commitDTO.getModified());


            for (String operationFile : fileNames) {
                files.put(operationFile, commitDTO.getId());
            }

            for (String deleteFile : commitDTO.getRemoved()) {
                //删除数据库中本次hook中删除的文件
                DevopsEnvFileE devopsEnvFileE = new DevopsEnvFileE();
                devopsEnvFileE.setEnvId(devopsEnvironmentE.getId());
                devopsEnvFileE.setFilePath(deleteFile);
                devopsEnvFileRepository.delete(devopsEnvFileE);
            }
            DevopsEnvCommitE devopsEnvCommitE = new DevopsEnvCommitE();
            devopsEnvCommitE.setEnvId(devopsEnvironmentE.getId());
            devopsEnvCommitE.setCommitSha(commitDTO.getId());
            devopsEnvCommitE.setCommitUser(TypeUtil.objToLong(pushWebHookDTO.getUserId()));
            devopsEnvCommitE.setCommitDate(commitDTO.getTimestamp());
            if (devopsEnvCommitRepository.queryByEnvIdAndCommit(devopsEnvironmentE.getId(), commitDTO.getId()) == null) {
                devopsEnvCommitRepository.create(devopsEnvCommitE);
            }
        });


        files.forEach((file, commit) -> {
            DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository
                    .queryByEnvAndPathAndCommit(devopsEnvironmentE.getId(), file, commit);
            if (devopsEnvFileE == null) {
                devopsEnvFileE = new DevopsEnvFileE();
                devopsEnvFileE.setCommitSha(commit);
                devopsEnvFileE.setEnvId(devopsEnvironmentE.getId());
                devopsEnvFileE.setSync(false);
                devopsEnvFileE.setFilePath(file);
                devopsEnvFileRepository.create(devopsEnvFileE);
            }
        });

        //从iam服务中查出项目和组织code
        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        //本地路径
        String path = String.format("gitops/%s/%s/%s",
                organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
        //生成环境git仓库ssh地址
        String url = String.format("git@%s:%s-%s-gitops/%s.git",
                gitlabSshUrl, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
        LOGGER.info(url);

        try {
            //从git库中查出环境最新提交
            BranchDO branch = devopsGitRepository.getBranch(gitLabProjectId, "master");
            String masterSha = branch.getCommit().getId();

            DevopsEnvCommitE devopsEnvCommitE = devopsEnvCommitRepository.queryByEnvIdAndCommit(devopsEnvironmentE.getId(), masterSha);
            devopsEnvironmentE.setGitCommit(devopsEnvCommitE.getId());

            //更新gitlab中的最新提交
            //TODO 此处不应该更新环境gitlab最新提交，环境git最新提交应该在收到webhook的时候更新，而不是在解释的时候更新，/解释完的时候会更新环境表中解释的conmit
            devopsEnvironmentRepository.update(devopsEnvironmentE);

            //更新本地库到最新提交
            handDevopsEnvGitRepository(path, url, devopsEnvironmentE.getEnvIdRsa(), masterSha);


            //获取将此次最新提交与tag作比价得到diff
            CompareResultsE compareResultsE = devopsGitRepository
                    .getCompareResults(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, masterSha);

            List<DevopsEnvFileResourceE> beforeSync = new ArrayList<>();


            compareResultsE.getDiffs().forEach(t -> {
                if (t.getNewPath().contains("yaml") || t.getNewPath().contains("yml")) {
                    if (t.getDeletedFile()) {
                        deletedFiles.add(t.getNewPath());
                    } else {
                        operationFiles.add(t.getNewPath());
                    }
                }
                //TODO new path？
                List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                        .queryByEnvIdAndPath(devopsEnvironmentE.getId(), t.getOldPath());
                if (!devopsEnvFileResourceES.isEmpty()) {
                    beforeSync.addAll(devopsEnvFileResourceES);
                }
            });

            //如果没有涉及到到yml文件的修改则跳过本次解释
            if (operationFiles.isEmpty() && deletedFiles.isEmpty()) {
                return;
            }

            List<C7nHelmRelease> c7nHelmReleases = new ArrayList<>();
            List<V1Service> v1Services = new ArrayList<>();
            List<V1beta1Ingress> v1beta1Ingresses = new ArrayList<>();

            Map<String, String> objectPath = new HashMap<>();

            //从文件中读出对象
            handleFilesToObject(operationFiles, path, c7nHelmReleases,
                    v1Services, v1beta1Ingresses,
                    objectPath, devopsEnvironmentE.getId());

            //处理对象关系

            handlerObjectRelations(
                    objectPath,
                    beforeSync,
                    c7nHelmReleases,
                    v1Services,
                    v1beta1Ingresses,
                    devopsEnvironmentE.getId(),
                    devopsEnvironmentE.getProjectE().getId()
            );


            //TODO 此时请求应考虑请求失败情况，还有删除成功了创建没有成功
            //删除tag
            devopsGitRepository.deleteTag(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, gitLabUserId);
            //创建新tag
            devopsGitRepository.createTag(gitLabProjectId, GitUtil.DEVOPS_GITOPS_TAG, masterSha, gitLabUserId);

            //向agent发送同步指令
            deployService.sendCommand(devopsEnvironmentE);
            devopsEnvironmentE.setDevopsSyncCommit(devopsEnvCommitE.getId());
            //更新环境 解释commit
            devopsEnvironmentRepository.update(devopsEnvironmentE);
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
        final String gitSuffix = "/.git";
        final String repoPath = path + gitSuffix;
        if (!file.exists()) {
            gitUtil.cloneBySsh(path, url);
            gitUtil.checkout(repoPath, commit);
        } else {
            gitUtil.checkout(repoPath, "master");
            gitUtil.pullBySsh(repoPath);
            gitUtil.checkout(repoPath, commit);
        }
    }

    private void handleFilesToObject(List<String> files,
                                     String path,
                                     List<C7nHelmRelease> c7nHelmReleases,
                                     List<V1Service> v1Services,
                                     List<V1beta1Ingress> v1beta1Ingresses,
                                     Map<String, String> objectPath,
                                     Long envId) {
        files.parallelStream().forEach(filePath -> {
            File file = new File(String.format("%s/%s", path, filePath));
            try {
                //从数据库中查出
                DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository.queryLatestByEnvAndPath(envId, filePath);
                for (Object data : yaml.loadAll(new FileInputStream(file))) {
                    JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
                    String type = jsonObject.get("kind").toString();
                    switch (type) {
                        case "C7NHelmRelease":
                            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
                            SerializableOperation<C7nHelmRelease> c7nHelmReleaseSerializableOperation
                                    = new SerializableOperation<>();
                            c7nHelmReleaseSerializableOperation.setT(c7nHelmRelease);
                            c7nHelmReleases.add(c7nHelmReleaseSerializableOperation
                                    .serializable(jsonObject.toJSONString(), filePath, objectPath, devopsEnvFileE));
                            break;
                        case "Ingress":
                            V1beta1Ingress v1beta1Ingress = new V1beta1Ingress();
                            SerializableOperation<V1beta1Ingress> v1beta1IngressSerializableOperation
                                    = new SerializableOperation<>();
                            v1beta1IngressSerializableOperation.setT(v1beta1Ingress);
                            v1beta1Ingresses.add(v1beta1IngressSerializableOperation
                                    .serializable(jsonObject.toJSONString(), filePath, objectPath, devopsEnvFileE));
                            break;
                        case "Service":
                            V1Service v1Service = new V1Service();
                            SerializableOperation<V1Service> v1ServiceSerializableOperation =
                                    new SerializableOperation<>();
                            v1ServiceSerializableOperation.setT(v1Service);
                            v1Services.add(v1ServiceSerializableOperation
                                    .serializable(jsonObject.toJSONString(), filePath, objectPath, devopsEnvFileE));
                            break;
                        default:
                            break;
                    }
                }
                //为什么
                if (devopsEnvFileE.getMessage() != null) {
                    devopsEnvFileRepository.update(devopsEnvFileE);
                }
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
            }
        });
    }


    private void handlerObjectRelations(Map<String, String> objectPath,
                                        List<DevopsEnvFileResourceE> beforeSync,
                                        List<C7nHelmRelease> c7nHelmReleases,
                                        List<V1Service> v1Services,
                                        List<V1beta1Ingress> v1beta1Ingresses,
                                        Long envId, Long projectId) {
        handlerC7nReleaseRelations(objectPath, beforeSync, c7nHelmReleases, envId, projectId);
        handlerServiceRelations(objectPath, beforeSync, v1Services, envId, projectId);
        handlerIngressRelations(objectPath, beforeSync, v1beta1Ingresses, envId, projectId);
    }

    private void handlerServiceRelations(Map<String, String> objectPath,
                                         List<DevopsEnvFileResourceE> beforeSync,
                                         List<V1Service> v1Services,
                                         Long envId, Long projectId) {
        List<String> beforeService = beforeSync.parallelStream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals("Service"))
                .map(devopsEnvFileResourceE -> {
                    DevopsServiceE devopsServiceE = devopsServiceRepository
                            .query(devopsEnvFileResourceE.getResourceId());
                    return devopsServiceE.getName();
                }).collect(Collectors.toList());

        v1Services.parallelStream()
                .filter(v1Service -> !beforeService.contains(v1Service.getMetadata().getName()))
                .forEach(v1Service -> {
                    DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository
                            .queryLatestByEnvAndPath(envId, objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
                    checkServiceName(objectPath, devopsEnvFileE, v1Service);
                    DevopsServiceE devopsServiceE = devopsServiceRepository
                            .selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                    DevopsServiceReqDTO devopsServiceReqDTO;
                    if (devopsServiceE == null) {
                        devopsServiceReqDTO = getDevopsServiceDTO(
                                v1Service,
                                envId,
                                devopsEnvFileE,
                                objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
                        if (devopsEnvFileE.getMessage() != null) {
                            devopsEnvFileRepository.update(devopsEnvFileE);
                        }
                        devopsServiceService.insertDevopsService(projectId, devopsServiceReqDTO, true);
                        devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                                devopsServiceReqDTO.getName(), envId);
                    }
                    DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                    devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                    devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
                    devopsEnvFileResourceE.setResourceId(devopsServiceE.getId());
                    devopsEnvFileResourceE.setResourceType(v1Service.getKind());
                    devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                });
        v1Services.parallelStream()
                .filter(v1Service -> beforeService.contains(v1Service.getMetadata().getName()))
                .forEach(v1Service -> {
                    DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository
                            .queryLatestByEnvAndPath(envId, objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
                    beforeService.remove(v1Service.getMetadata().getName());
                    checkServiceName(objectPath, devopsEnvFileE, v1Service);
                    DevopsServiceReqDTO devopsServiceReqDTO = getDevopsServiceDTO(
                            v1Service,
                            envId,
                            devopsEnvFileE,
                            objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
                    if (devopsEnvFileE.getMessage() != null) {
                        devopsEnvFileRepository.update(devopsEnvFileE);
                    }
                    DevopsServiceE devopsServiceE = devopsServiceRepository
                            .selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                    devopsServiceService.updateDevopsService(
                            projectId, devopsServiceE.getId(), devopsServiceReqDTO, true);
                    DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                            .queryByEnvIdAndResource(envId, devopsServiceE.getId(), v1Service.getKind());
                    updateOrCreateFileResource(objectPath,
                            envId,
                            devopsEnvFileResourceE,
                            v1Service.hashCode(), devopsServiceE.getId(), v1Service.getKind());
                });
        beforeService.parallelStream().forEach(serviceName -> {
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(serviceName, envId);
            devopsServiceService.deleteDevopsService(devopsServiceE.getId(), true);
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsServiceE.getId(), "Service");
        });
    }

    private void checkServiceName(Map<String, String> objectPath,
                                  DevopsEnvFileE devopsEnvFileE,
                                  V1Service v1Service) {
        try {
            DevopsServiceValidator.checkName(v1Service.getMetadata().getName());
        } catch (Exception e) {
            devopsEnvFileE.setMessage(e.getMessage());
            devopsEnvFileE.setFilePath(objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
            devopsEnvFileRepository.update(devopsEnvFileE);
        }
    }

    private ApplicationDeployDTO getApplicationDeployDTO(C7nHelmRelease c7nHelmRelease,
                                                         Long projectId, Long envId, String type,
                                                         DevopsEnvFileE devopsEnvFileE) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = deployMsgHandlerService.getApplication(c7nHelmRelease.getSpec().getChartName(), projectId, organization.getId());
        if (applicationE == null) {
            return null;
        }
        ApplicationVersionE applicationVersionE = applicationVersionRepository
                .queryByAppAndVersion(applicationE.getId(), c7nHelmRelease.getSpec().getChartVersion());
        if (applicationVersionE == null) {
            devopsEnvFileE.setMessage("release version not exist!");
            devopsEnvFileRepository.update(devopsEnvFileE);
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
            applicationDeployDTO.setAppInstanceId(applicationInstanceE.getId());
        }
        return applicationDeployDTO;
    }


    private void handlerC7nReleaseRelations(Map<String, String> objectPath,
                                            List<DevopsEnvFileResourceE> beforeSync,
                                            List<C7nHelmRelease> c7nHelmReleases,
                                            Long envId, Long projectId) {
        List<String> beforeC7nRelease = beforeSync.parallelStream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals("C7NHelmRelease"))
                .map(devopsEnvFileResourceE -> {
                    ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                            .selectById(devopsEnvFileResourceE.getResourceId());
                    return applicationInstanceE.getCode();
                }).collect(Collectors.toList());
        c7nHelmReleases.parallelStream()
                .filter(c7nHelmRelease -> !beforeC7nRelease.contains(c7nHelmRelease.getMetadata().getName()))
                .forEach(c7nHelmRelease -> {
                    ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                            .selectByCode(c7nHelmRelease.getMetadata().getName(), envId);
                    DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository.queryLatestByEnvAndPath(envId, objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())));
                    ApplicationDeployDTO applicationDeployDTO;
                    ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
                    if (applicationInstanceE == null) {
                        applicationDeployDTO = getApplicationDeployDTO(
                                c7nHelmRelease,
                                projectId,
                                envId,
                                "create",
                                devopsEnvFileE);
                        if (applicationDeployDTO == null) {
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
        c7nHelmReleases.parallelStream()
                .filter(c7nHelmRelease -> beforeC7nRelease.contains(c7nHelmRelease.getMetadata().getName()))
                .forEach(c7nHelmRelease -> {
                    beforeC7nRelease.remove(c7nHelmRelease.getMetadata().getName());
                    DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository.queryLatestByEnvAndPath(envId, objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())));
                    ApplicationDeployDTO applicationDeployDTO = getApplicationDeployDTO(
                            c7nHelmRelease,
                            projectId,
                            envId,
                            "update",
                            devopsEnvFileE);
                    if (applicationDeployDTO == null) {
                        return;
                    }
                    DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.queryByObject(ObjectType.INSTANCE.getType(), applicationDeployDTO.getAppInstanceId());

                    if (!devopsEnvCommandE.getCommandType().equals(CommandType.SYNC.getType())) {
                        applicationInstanceService
                                .create(applicationDeployDTO, true);
                    }
                    DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                            .queryByEnvIdAndResource(envId, applicationDeployDTO.getAppInstanceId(), c7nHelmRelease.getKind());
                    updateOrCreateFileResource(objectPath, envId,
                            devopsEnvFileResourceE,
                            c7nHelmRelease.hashCode(), applicationDeployDTO.getAppInstanceId(),
                            c7nHelmRelease.getKind());
                });
        beforeC7nRelease.parallelStream().forEach(releaseName -> {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName, envId);
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.queryByObject(ObjectType.INSTANCE.getType(), applicationInstanceE.getId());
            if (!devopsEnvCommandE.getCommandType().equals(CommandType.DELETE)) {
                applicationInstanceService.instanceDelete(applicationInstanceE.getId(), true);
            }
            devopsEnvFileResourceRepository
                    .deleteByEnvIdAndResource(envId, applicationInstanceE.getId(), "C7NHelmRelease");
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
                                         Long envId, Long projectId) {
        List<String> beforeIngress = beforeSync.parallelStream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals("Ingress"))
                .map(devopsEnvFileResourceE -> {
                    DevopsIngressDO devopsIngressDO = devopsIngressRepository
                            .getIngress(devopsEnvFileResourceE.getResourceId());
                    return devopsIngressDO.getName();
                }).collect(Collectors.toList());
        v1beta1Ingresses.parallelStream()
                .filter(v1beta1Ingress -> !beforeIngress.contains(v1beta1Ingress.getMetadata().getName()))
                .forEach(v1beta1Ingress -> {
                    DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository.queryLatestByEnvAndPath(envId, objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                    checkIngressAppVersion(devopsEnvFileE, v1beta1Ingress);
                    DevopsIngressE devopsIngressE = devopsIngressRepository
                            .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                    DevopsIngressDTO devopsIngressDTO;
                    if (devopsIngressE == null) {
                        devopsIngressDTO = getDevopsIngressDTO(
                                v1beta1Ingress,
                                envId,
                                devopsEnvFileE);
                        devopsIngressService.addIngress(devopsIngressDTO, projectId, true);
                        devopsIngressE = devopsIngressRepository
                                .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                    }
                    DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                    devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                    devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                    devopsEnvFileResourceE.setResourceId(devopsIngressE.getId());
                    devopsEnvFileResourceE.setResourceType(v1beta1Ingress.getKind());
                    devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                });
        v1beta1Ingresses.parallelStream()
                .filter(v1beta1Ingress -> beforeIngress.contains(v1beta1Ingress.getMetadata().getName()))
                .forEach(v1beta1Ingress -> {
                    beforeIngress.remove(v1beta1Ingress.getMetadata().getName());
                    DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository.queryLatestByEnvAndPath(envId, objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                    checkIngressAppVersion(devopsEnvFileE, v1beta1Ingress);
                    DevopsIngressDTO devopsIngressDTO = getDevopsIngressDTO(
                            v1beta1Ingress,
                            envId,
                            devopsEnvFileE);
                    DevopsIngressE devopsIngressE = devopsIngressRepository
                            .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                    devopsIngressService.updateIngress(devopsIngressE.getId(), devopsIngressDTO, projectId, true);
                    DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                            .queryByEnvIdAndResource(envId, devopsIngressE.getId(), v1beta1Ingress.getKind());
                    updateOrCreateFileResource(objectPath,
                            envId,
                            devopsEnvFileResourceE,
                            v1beta1Ingress.hashCode(), devopsIngressE.getId(), v1beta1Ingress.getKind());
                });
        beforeIngress.parallelStream().forEach(ingressName -> {
            DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, ingressName);
            devopsIngressService.deleteIngress(devopsIngressE.getId(), true);
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsIngressE.getId(), "Ingress");
        });

    }


    private void checkIngressAppVersion(
            DevopsEnvFileE devopsEnvFileE,
            V1beta1Ingress v1beta1Ingress) {
        try {
            DevopsIngressValidator.checkIngressName(v1beta1Ingress.getMetadata().getName());
        } catch (Exception e) {
            devopsEnvFileE.setMessage(e.getMessage());
            devopsEnvFileRepository.update(devopsEnvFileE);
            throw new CommonException(e.getMessage());
        }
    }


    private DevopsIngressDTO getDevopsIngressDTO(V1beta1Ingress v1beta1Ingress,
                                                 Long envId,
                                                 DevopsEnvFileE devopsEnvFileE) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setDomain(v1beta1Ingress.getSpec().getRules().get(0).getHost()
        );
        devopsIngressDTO.setName(v1beta1Ingress.getMetadata().getName());
        devopsIngressDTO.setEnvId(envId);
        List<String> pathCheckList = new ArrayList<>();
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = new ArrayList<>();
        List<V1beta1HTTPIngressPath> paths = v1beta1Ingress.getSpec().getRules().get(0).getHttp().getPaths();
        for (V1beta1HTTPIngressPath v1beta1HTTPIngressPath : paths) {
            try {
                DevopsIngressValidator.checkPath(v1beta1HTTPIngressPath.getPath());
                if (pathCheckList.contains(v1beta1HTTPIngressPath.getPath())) {
                    LOGGER.info(PATH_DUPLICATED);
                } else {
                    pathCheckList.add(v1beta1HTTPIngressPath.getPath());
                }
            } catch (Exception e) {
                devopsEnvFileE.setMessage(e.getMessage());
                devopsEnvFileRepository.update(devopsEnvFileE);
            }
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(
                    v1beta1HTTPIngressPath.getBackend().getServiceName(), devopsEnvironmentE.getCode());
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
            devopsIngressPathDTO.setPath(v1beta1HTTPIngressPath.getPath());

            devopsIngressPathDTO.setServiceId(devopsServiceE.getId());
            devopsIngressPathDTOS.add(devopsIngressPathDTO);
        }
        devopsIngressDTO.setPathList(devopsIngressPathDTOS);
        return devopsIngressDTO;
    }


    private DevopsServiceReqDTO getDevopsServiceDTO(V1Service v1Service,
                                                    Long envId,
                                                    DevopsEnvFileE devopsEnvFileE,
                                                    String filePath) {
        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO();
        devopsServiceReqDTO.setExternalIp(String.join(",", v1Service.getSpec().getExternalIPs()));
        devopsServiceReqDTO.setName(v1Service.getMetadata().getName());
        devopsServiceReqDTO.setEnvId(envId);
        devopsServiceReqDTO.setLabel(v1Service.getMetadata().getLabels());

        List<PortMapE> portMapList = v1Service.getSpec().getPorts().parallelStream()
                .map(t -> {
                    PortMapE portMap = new PortMapE();
                    portMap.setName(t.getName());
                    portMap.setNodePort(t.getNodePort().longValue());
                    portMap.setPort(t.getPort().longValue());
                    portMap.setProtocol(t.getProtocol());
                    portMap.setTargetPort(t.getTargetPort().getIntValue().longValue());
                    return portMap;
                }).collect(Collectors.toList());
        devopsServiceReqDTO.setPorts(portMapList);

        String instancesCode = v1Service.getMetadata().getAnnotations().get("choerodon.io/network-service-instances");
        if (!instancesCode.isEmpty()) {
            List<Long> instanceIdList = Arrays.stream(instancesCode.split("\\+")).parallel()
                    .map(t -> getInstanceId(t, envId, devopsServiceReqDTO, devopsEnvFileE, filePath))
                    .collect(Collectors.toList());
            devopsServiceReqDTO.setAppInstance(instanceIdList);
        }
        return devopsServiceReqDTO;
    }

    private Long getInstanceId(String instanceCode, Long envId, DevopsServiceReqDTO devopsServiceReqDTO,
                               DevopsEnvFileE devopsEnvFileE, String filePath) {
        try {
            ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(instanceCode, envId);
            if (devopsServiceReqDTO.getAppId() == null) {
                devopsServiceReqDTO.setAppId(instanceE.getApplicationE().getId());
            }
            String logMsg = devopsEnvFileE.getMessage();
            if (!devopsServiceReqDTO.getAppId().equals(instanceE.getApplicationE().getId())
                    && (logMsg == null || !logMsg.contains(INSTANCE_APP_ID_NOT_SAME))) {
                if (logMsg == null) {
                    devopsEnvFileE.setMessage(INSTANCE_APP_ID_NOT_SAME);
                } else {
                    devopsEnvFileE.setMessage(INSTANCE_APP_ID_NOT_SAME + logMsg);
                }
                devopsEnvFileE.setFilePath(filePath);
            }
            return instanceE.getId();
        } catch (Exception e) {
            String logMsg = devopsEnvFileE.getMessage();
            if (logMsg == null) {
                devopsEnvFileE.setMessage(INSTANCE_NOT_FOUND + instanceCode);
            } else if (!logMsg.contains(INSTANCE_NOT_FOUND)) {
                devopsEnvFileE.setMessage(logMsg + INSTANCE_NOT_FOUND + instanceCode);
            } else {
                devopsEnvFileE.setMessage(logMsg + ", " + instanceCode);
            }
            devopsEnvFileE.setFilePath(filePath);
            return null;
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
        applicationVersionE
                .setRepository("/" + orgCode + "/" + projectCode + "/");
        applicationVersionValueE.setValue(values);
        applicationVersionE.initApplicationVersionValueE(
                applicationVersionValueRepository.create(applicationVersionValueE).getId());
        return applicationVersionRepository.create(applicationVersionE);

    }
}
