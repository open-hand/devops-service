package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.dto.gitlab.MemberDTO;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.event.DevOpsAppPayload;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.domain.service.UpdateUserPermissionService;
import io.choerodon.devops.domain.service.impl.UpdateAppUserPermissionServiceImpl;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.AccessLevel;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/28.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    public static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final String MASTER = "master";
    private static final String APPLICATION = "application";
    private Gson gson = new Gson();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${services.sonarqube.url}")
    private String sonarqubeUrl;
    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private ApplicationTemplateRepository applicationTemplateRepository;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private GitlabUserRepository gitlabUserRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository;
    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private ApplicationMarketRepository applicationMarketRepository;
    @Autowired
    private AppUserPermissionRepository appUserPermissionRepository;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;

    @Override
    @Saga(code = "devops-create-gitlab-project",
            description = "devops create GitLab project", inputSchema = "{}")
    public ApplicationRepDTO create(Long projectId, ApplicationReqDTO applicationReqDTO) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplication(applicationReqDTO);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = ConvertHelper.convert(applicationReqDTO, ApplicationE.class);
        applicationE.initProjectE(projectId);
        applicationRepository.checkName(applicationE.getProjectE().getId(), applicationE.getName());
        applicationRepository.checkCode(applicationE);
        applicationE.initActive(true);
        applicationE.initSynchro(false);
        applicationE.setIsSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());

        // 查询创建应用所在的gitlab应用组
        GitlabGroupE gitlabGroupE = devopsProjectRepository.queryDevopsProject(applicationE.getProjectE().getId());
        if (gitlabGroupE == null) {
            throw new CommonException("error.group.not.sync");
        }
        GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (gitlabMemberE == null || gitlabMemberE.getAccessLevel() != AccessLevel.OWNER.toValue()) {
            throw new CommonException("error.user.not.owner");
        }
        // 创建saga payload
        DevOpsAppPayload devOpsAppPayload = new DevOpsAppPayload();
        devOpsAppPayload.setType(APPLICATION);
        devOpsAppPayload.setPath(applicationReqDTO.getCode());
        devOpsAppPayload.setOrganizationId(organization.getId());
        devOpsAppPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        devOpsAppPayload.setGroupId(TypeUtil.objToInteger(gitlabGroupE.getDevopsAppGroupId()));
        devOpsAppPayload.setUserIds(applicationReqDTO.getUserIds());
        devOpsAppPayload.setSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());
        applicationE = applicationRepository.create(applicationE);
        devOpsAppPayload.setAppId(applicationE.getId());
        Long appId = applicationE.getId();
        if (appId == null) {
            throw new CommonException("error.application.create.insert");
        }
        // 如果不跳过权限检查
        List<Long> userIds = applicationReqDTO.getUserIds();
        if (!applicationReqDTO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> appUserPermissionRepository.create(e, appId));
        }

        String input = gson.toJson(devOpsAppPayload);
        sagaClient.startSaga("devops-create-gitlab-project", new StartInstanceDTO(input, "", ""));

        return ConvertHelper.convert(applicationRepository.queryByCode(applicationE.getCode(),
                applicationE.getProjectE().getId()), ApplicationRepDTO.class);
    }

    @Override
    public ApplicationRepDTO query(Long projectId, Long applicationId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = applicationRepository.query(applicationId);
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        applicationE.initGitlabProjectEByUrl(gitlabUrl + urlSlash
                + organization.getCode() + "-" + projectE.getCode() + "/"
                + applicationE.getCode() + ".git");
        return ConvertHelper.convert(applicationE, ApplicationRepDTO.class);
    }

    @Override
    public void delete(Long projectId, Long applicationId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = applicationRepository.query(applicationId);
        UserAttrE userAttrE = userAttrRepository.queryById(DetailsHelper.getUserDetails().getUserId());
        gitlabRepository.deleteDevOpsApp(organization.getCode() + "-" + projectE.getCode(),
                applicationE.getCode(), userAttrE.getGitlabUserId().intValue());
        applicationRepository.delete(applicationId);
    }

    @Override
    public Boolean update(Long projectId, ApplicationUpdateDTO applicationUpdateDTO) {
        ApplicationE applicationE = ConvertHelper.convert(applicationUpdateDTO, ApplicationE.class);
        applicationE.setIsSkipCheckPermission(applicationUpdateDTO.getPermission());
        applicationE.initProjectE(projectId);

        Long appId = applicationUpdateDTO.getId();
        ApplicationE oldApplicationE = applicationRepository.query(appId);
        if (!oldApplicationE.getName().equals(applicationUpdateDTO.getName())) {
            applicationRepository.checkName(applicationE.getProjectE().getId(), applicationE.getName());
        }
        if (applicationRepository.update(applicationE) != 1) {
            throw new CommonException("error.application.update");
        }

        UpdateUserPermissionService updateUserPermissionService = new UpdateAppUserPermissionServiceImpl();
        // 原来跳过，现在也跳过，不更新权限表
        if (oldApplicationE.getIsSkipCheckPermission() && applicationUpdateDTO.getPermission()) {
            return true;
        }
        // 原来跳过，现在不跳过，需要更新权限表
        else if (oldApplicationE.getIsSkipCheckPermission() && !applicationUpdateDTO.getPermission()) {
            return updateUserPermissionService
                    .updateUserPermission(projectId, appId, applicationUpdateDTO.getUserIds(), 1);
        }
        // 原来不跳过，现在跳过，需要删除权限表中的所有人，然后把项目下所有项目成员加入gitlab权限
        else if (!oldApplicationE.getIsSkipCheckPermission() && applicationUpdateDTO.getPermission()) {
            appUserPermissionRepository.deleteByAppId(appId);
            return updateUserPermissionService.updateUserPermission(projectId, appId, new ArrayList<>(), 2);
        }
        // 原来不跳过，现在也不跳过，需要更新权限表
        else {
            return updateUserPermissionService
                    .updateUserPermission(projectId, appId, applicationUpdateDTO.getUserIds(), 3);
        }
    }

    @Override
    public Boolean active(Long applicationId, Boolean active) {
        if (!active) {
            applicationRepository.checkAppCanDisable(applicationId);
        }
        ApplicationE applicationE = applicationRepository.query(applicationId);
        applicationE.initActive(active);
        if (applicationRepository.update(applicationE) != 1) {
            throw new CommonException("error.application.active");
        }
        return true;
    }

    @Override
    public Page<ApplicationRepDTO> listByOptions(Long projectId, Boolean isActive, Boolean hasVersion,
                                                 String type, Boolean doPage,
                                                 PageRequest pageRequest, String params) {
        Page<ApplicationE> applicationES =
                applicationRepository.listByOptions(projectId, isActive, hasVersion, type, doPage, pageRequest, params);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        applicationES.getContent().forEach(t -> {
                    if (t.getGitlabProjectE() != null && t.getGitlabProjectE().getId() != null) {
                        t.initGitlabProjectEByUrl(
                                gitlabUrl + urlSlash + organization.getCode() + "-" + projectE.getCode() + "/" +
                                        t.getCode() + ".git");
                        getSonarUrl(projectE, organization, t);
                    }
                }
        );
        Page<ApplicationRepDTO> resultDTOPage = ConvertPageHelper.convertPage(applicationES, ApplicationRepDTO.class);
        List<ApplicationRepDTO> resulstDTOList = setApplicationRepDTOPermission(applicationES.getContent(), userAttrE,
                projectE);
        resultDTOPage.setContent(resulstDTOList);
        return resultDTOPage;
    }

    private void getSonarUrl(ProjectE projectE, Organization organization, ApplicationE t) {
        if (!sonarqubeUrl.equals("")) {
            Integer result;
            try {
                result = HttpClientUtil.getSonar(sonarqubeUrl.endsWith("/")
                        ? sonarqubeUrl
                        : String
                        .format("%s/api/project_links/search?projectKey=%s-%s:%s", sonarqubeUrl, organization.getCode(),
                                projectE.getCode(), t.getCode()));
                if (result.equals(HttpStatus.OK.value())) {
                    t.initSonarUrl(sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/"
                            + "dashboard?id=" + organization.getCode() + "-" + projectE.getCode() + ":" + t.getCode());
                }
            } catch (Exception e) {
                t.initSonarUrl(null);
            }
        }
    }

    @Override
    public Page<ApplicationRepDTO> listCodeRepository(Long projectId, PageRequest pageRequest, String params) {
        Page<ApplicationE> applicationES = applicationRepository.listCodeRepository(projectId, pageRequest, params);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        applicationES.forEach(t -> {
                    if (t.getGitlabProjectE() != null && t.getGitlabProjectE().getId() != null) {
                        t.initGitlabProjectEByUrl(gitlabUrl + urlSlash
                                + organization.getCode() + "-" + projectE.getCode() + "/" + t.getCode() + ".git");
                        getSonarUrl(projectE, organization, t);
                    }
                }
        );
        Page<ApplicationRepDTO> resultDTOPage = ConvertPageHelper.convertPage(applicationES, ApplicationRepDTO.class);
        List<ApplicationRepDTO> resultDTOList = setApplicationRepDTOPermission(applicationES.getContent(), userAttrE,
                projectE);
        resultDTOPage.setContent(resultDTOList);
        return resultDTOPage;
    }

    @Override
    public List<ApplicationRepDTO> listByActive(Long projectId) {
        List<ApplicationE> applicationEList = applicationRepository.listByActive(projectId);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        applicationEList.forEach(t -> {
                    if (t.getGitlabProjectE() != null && t.getGitlabProjectE().getId() != null) {
                        t.initGitlabProjectEByUrl(gitlabUrl + urlSlash
                                + organization.getCode() + "-" + projectE.getCode() + "/" + t.getCode() + ".git");
                        getSonarUrl(projectE, organization, t);
                    }
                }
        );
        List<ApplicationRepDTO> resultDTOList = setApplicationRepDTOPermission(applicationEList, userAttrE, projectE);
        return resultDTOList;
    }

    private List<ApplicationRepDTO> setApplicationRepDTOPermission(List<ApplicationE> applicationEList,
                                                                   UserAttrE userAttrE, ProjectE projectE) {
        List<ApplicationRepDTO> resultDTOList = ConvertHelper.convertList(applicationEList, ApplicationRepDTO.class);
        if (!iamRepository.isProjectOwner(userAttrE.getIamUserId(), projectE)) {
            List<Long> appIds = appUserPermissionRepository.listByUserId(userAttrE.getIamUserId()).stream()
                    .map(AppUserPermissionE::getAppId).collect(Collectors.toList());
            resultDTOList.forEach(e -> {
                if (appIds.contains(e.getId())) {
                    e.setPermission(true);
                }
                else {
                    e.setPermission(false);
                }
            });
        }
        else {
            resultDTOList.forEach(e -> e.setPermission(true));
        }
        return resultDTOList;
    }

    @Override
    public List<ApplicationRepDTO> listAll(Long projectId) {
        return ConvertHelper.convertList(applicationRepository.listAll(projectId), ApplicationRepDTO.class);
    }

    @Override
    public void checkName(Long projectId, String name) {
        applicationRepository.checkName(projectId, name);
    }

    @Override
    public void checkCode(Long projectId, String code) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        applicationE.initProjectE(projectId);
        applicationE.setCode(code);
        applicationRepository.checkCode(applicationE);
    }

    @Override
    public List<ApplicationTemplateRepDTO> listTemplate(Long projectId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        return ConvertHelper.convertList(applicationTemplateRepository.list(projectE.getOrganization().getId()),
                ApplicationTemplateRepDTO.class).stream()
                .filter(ApplicationTemplateRepDTO::getSynchro).collect(Collectors.toList());
    }

    @Override
    public void operationApplication(DevOpsAppPayload gitlabProjectPayload) {
        GitlabGroupE gitlabGroupE = devopsProjectRepository.queryByGitlabGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        ApplicationE applicationE = applicationRepository.queryByCode(gitlabProjectPayload.getPath(),
                gitlabGroupE.getProjectE().getId());
        ProjectE projectE = iamRepository.queryIamProject(gitlabGroupE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        GitlabProjectDO gitlabProjectDO = gitlabRepository
                .getProjectByName(organization.getCode() + "-" + projectE.getCode(), applicationE.getCode(),
                        gitlabProjectPayload.getUserId());
        Integer gitlabProjectId = gitlabProjectDO.getId();
        if (gitlabProjectId == null) {
            gitlabProjectDO = gitlabRepository.createProject(gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(),
                    gitlabProjectPayload.getUserId(), false);
        }
        gitlabProjectPayload.setGitlabProjectId(gitlabProjectDO.getId());

        // 不跳过权限检查，则为gitlab项目分配项目成员权限
        if (!gitlabProjectPayload.getSkipCheckPermission()) {
            if (!gitlabProjectPayload.getUserIds().isEmpty()) {
                List<Long> gitlabUserIds = userAttrRepository.listByUserIds(gitlabProjectPayload.getUserIds()).stream()
                        .map(UserAttrE::getGitlabUserId).collect(Collectors.toList());
                gitlabUserIds.forEach(e -> {
                    GitlabMemberE gitlabMemberE = gitlabProjectRepository
                            .getProjectMember(gitlabProjectPayload.getGitlabProjectId(), TypeUtil.objToInteger(e));
                    if (gitlabMemberE == null || gitlabMemberE.getId() == null) {
                        gitlabRepository.addMemberIntoProject(gitlabProjectPayload.getGitlabProjectId(),
                                new MemberDTO(TypeUtil.objToInteger(e), 40, ""));
                    }
                });
            }
        }
        // 跳过权限检查，项目下所有成员自动分配权限
        else {
            List<Long> gitlabUserIds = iamRepository.getAllMemberIdsWithoutOwner(projectE.getId());
            gitlabUserIds.forEach(e ->
                    gitlabRepository.addMemberIntoProject(gitlabProjectPayload.getGitlabProjectId(),
                            new MemberDTO(TypeUtil.objToInteger(e), 40, "")));
        }
        if (applicationE.getApplicationTemplateE() != null) {
            ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
                    applicationE.getApplicationTemplateE().getId());
            String applicationDir = APPLICATION + System.currentTimeMillis();
            //拉取模板
            String repoUrl = applicationTemplateE.getRepoUrl();
            String type = applicationTemplateE.getCode();
            if (applicationTemplateE.getOrganization().getId() != null) {
                repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
                repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
                type = MASTER;
            }
            Git git = gitUtil.clone(applicationDir, type, repoUrl);
            //渲染模板里面的参数
            replaceParams(applicationE, projectE, organization, applicationDir);

            UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
            String accessToken = getToken(gitlabProjectPayload, applicationDir, userAttrE);

            repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            applicationE.initGitlabProjectEByUrl(repoUrl + organization.getCode()
                    + "-" + projectE.getCode() + "/" + applicationE.getCode() + ".git");
            GitlabUserE gitlabUserE = gitlabUserRepository.getGitlabUserByUserId(gitlabProjectPayload.getUserId());

            BranchDO branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
            if (branchDO.getName() == null) {
                gitUtil.push(git, applicationDir, applicationE.getGitlabProjectE().getRepoURL(),
                        gitlabUserE.getUsername(), accessToken);
                branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
                //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                if (!branchDO.getProtected()) {
                    try {
                        gitlabRepository.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
                                AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                                gitlabProjectPayload.getUserId());
                    } catch (CommonException e) {
                        branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
                        if (!branchDO.getProtected()) {
                            throw new CommonException(e);
                        }
                    }
                }
            } else {
                if (!branchDO.getProtected()) {
                    gitlabRepository.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
                            AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                            gitlabProjectPayload.getUserId());
                }
            }
            initMasterBranch(gitlabProjectPayload, applicationE);
        }
        try {
            String token = GenerateUUID.generateUUID();
            List<Variable> variables = gitlabRepository
                    .getVariable(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId());
            if (variables.isEmpty()) {
                gitlabRepository.addVariable(gitlabProjectPayload.getGitlabProjectId(), "Token", token, false,
                        gitlabProjectPayload.getUserId());
            } else {
                token = variables.get(0).getValue();
            }
            applicationE.setToken(token);
            applicationE.initGitlabProjectE(TypeUtil.objToInteger(gitlabProjectPayload.getGitlabProjectId()));
            applicationE.initSynchro(true);
            ProjectHook projectHook = ProjectHook.allHook();
            projectHook.setEnableSslVerification(true);
            projectHook.setProjectId(gitlabProjectPayload.getGitlabProjectId());
            projectHook.setToken(token);
            String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
            uri += "devops/webhook";
            projectHook.setUrl(uri);
            List<ProjectHook> projectHooks = gitlabRepository
                    .getHooks(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId());
            if (projectHooks == null) {
                applicationE.initHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(
                        gitlabProjectPayload.getGitlabProjectId(), gitlabProjectPayload.getUserId(), projectHook)
                        .getId()));
            } else {
                applicationE.initHookId(TypeUtil.objToLong(projectHooks.get(0).getId()));
            }
            if (applicationRepository.update(applicationE) != 1) {
                throw new CommonException("error.application.update");
            }
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    @Saga(code = "devops-set-app-err",
            description = "devops set app status create err", inputSchema = "{}")
    public void setAppErrStatus(String input) {
        sagaClient.startSaga("devops-set-app-err", new StartInstanceDTO(input, "", ""));
    }

    private void initMasterBranch(DevOpsAppPayload gitlabProjectPayload, ApplicationE applicationE) {
        CommitE commitE;
        try {
            commitE = devopsGitRepository.getCommit(
                    gitlabProjectPayload.getGitlabProjectId(), MASTER, gitlabProjectPayload.getUserId());
        } catch (Exception e) {
            commitE = new CommitE();
        }
        DevopsBranchE devopsBranchE = new DevopsBranchE();
        devopsBranchE.setUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchE.setApplicationE(applicationE);
        devopsBranchE.setBranchName(MASTER);
        devopsBranchE.setCheckoutCommit(commitE.getId());
        Date date = DateUtil.changeTimeZone(
                commitE.getCommittedDate(), TimeZone.getTimeZone("GMT"), TimeZone.getDefault());
        devopsBranchE.setCheckoutDate(date);
        devopsBranchE.setLastCommitUser(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchE.setLastCommitMsg(commitE.getMessage());
        devopsBranchE.setLastCommitDate(date);
        devopsBranchE.setLastCommit(commitE.getId());
        devopsGitRepository.createDevopsBranch(devopsBranchE);
    }

    private void replaceParams(ApplicationE applicationE, ProjectE projectE, Organization organization,
                               String applicationDir) {
        try {
            File file = new File(gitUtil.getWorkingDirectory(applicationDir));
            Map<String, String> params = new HashMap<>();
            params.put("{{group.name}}", organization.getCode() + "-" + projectE.getCode());
            params.put("{{service.code}}", applicationE.getCode());
            FileUtil.replaceReturnFile(file, params);
        } catch (Exception e) {
            //删除模板
            gitUtil.deleteWorkingDirectory(applicationDir);
            throw new CommonException(e.getMessage(), e);
        }
    }

    private String getToken(DevOpsAppPayload gitlabProjectPayload, String applicationDir, UserAttrE userAttrE) {
        String accessToken = userAttrE.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitlabRepository.createToken(gitlabProjectPayload.getGitlabProjectId(),
                    applicationDir, gitlabProjectPayload.getUserId());
            userAttrE.setGitlabToken(accessToken);
            userAttrRepository.update(userAttrE);
        }
        return accessToken;
    }

    @Override
    public Boolean applicationExist(String uuid) {
        return applicationRepository.applicationExist(uuid);
    }


    @Override
    public String queryFile(String token, String type) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);
        if (applicationE == null) {
            return null;
        }
        try {
            ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            InputStream inputStream;
            if (type == null) {
                inputStream = this.getClass().getResourceAsStream("/shell/ci.sh");
            } else {
                inputStream = this.getClass().getResourceAsStream("/shell/" + type + ".sh");
            }
            Map<String, String> params = new HashMap<>();
            params.put("{{ GROUP_NAME }}", organization.getCode() + "-" + projectE.getCode());
            params.put("{{ PROJECT_NAME }}", applicationE.getCode());
            return FileUtil.replaceReturnString(inputStream, params);
        } catch (CommonException e) {
            return null;
        }
    }

    @Override
    public List<ApplicationCodeDTO> listByEnvId(Long projectId, Long envId, String status, Long appId) {
        List<ApplicationCodeDTO> applicationCodeDTOS = ConvertHelper
                .convertList(applicationRepository.listByEnvId(projectId, envId, status),
                        ApplicationCodeDTO.class);
        if (appId != null) {
            ApplicationE applicationE = applicationRepository.query(appId);
            ApplicationCodeDTO applicationCodeDTO = new ApplicationCodeDTO();
            BeanUtils.copyProperties(applicationE, applicationCodeDTO);
            ApplicationMarketE applicationMarketE = applicationMarketRepository.queryByAppId(appId);
            if (applicationMarketE != null) {
                applicationCodeDTO.setPublishLevel(applicationMarketE.getPublishLevel());
                applicationCodeDTO.setContributor(applicationMarketE.getContributor());
                applicationCodeDTO.setDescription(applicationMarketE.getDescription());
            }
            for (int i = 0; i < applicationCodeDTOS.size(); i++) {
                if (applicationCodeDTOS.get(i).getId().equals(applicationE.getId())) {
                    applicationCodeDTOS.remove(applicationCodeDTOS.get(i));
                }
            }
            applicationCodeDTOS.add(0, applicationCodeDTO);
        }
        return applicationCodeDTOS;
    }

    @Override
    public Page<ApplicationCodeDTO> pageByEnvId(Long projectId, Long envId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPage(applicationRepository.pageByEnvId(projectId, envId, pageRequest),
                ApplicationCodeDTO.class);
    }

    @Override
    public Page<ApplicationReqDTO> listByActiveAndPubAndVersion(Long projectId, PageRequest pageRequest,
                                                                String params) {
        return ConvertPageHelper.convertPage(applicationRepository
                        .listByActiveAndPubAndVersion(projectId, true, pageRequest, params),
                ApplicationReqDTO.class);
    }

    @Override
    public List<AppUserPermissionRepDTO> listAllUserPermission(Long appId) {
        return ConvertHelper.convertList(appUserPermissionRepository.listAll(appId), AppUserPermissionRepDTO.class);
    }

    @Override
    public void initMockService(SagaClient sagaClient) {
        this.sagaClient = sagaClient;
    }
}
