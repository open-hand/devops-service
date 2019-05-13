package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.dto.gitlab.MemberDTO;
import io.choerodon.devops.api.dto.gitlab.VariableDTO;
import io.choerodon.devops.api.dto.sonar.*;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.event.DevOpsAppImportPayload;
import io.choerodon.devops.domain.application.event.DevOpsAppPayload;
import io.choerodon.devops.domain.application.event.DevOpsUserPayload;
import io.choerodon.devops.domain.application.event.IamAppPayLoad;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
import io.choerodon.devops.infra.dataobject.harbor.ProjectDetail;
import io.choerodon.devops.infra.dataobject.harbor.User;
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.tool.UUIDTool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by younger on 2018/3/28.
 */
@Service
@EnableConfigurationProperties(HarborConfigurationProperties.class)
public class ApplicationServiceImpl implements ApplicationService {
    public static final String SEVERITIES = "severities";
    private static final Pattern REPOSITORY_URL_PATTERN = Pattern.compile("^http.*\\.git");
    private static final String GITLAB_CI_FILE = ".gitlab-ci.yml";
    private static final String DOCKER_FILE_NAME = "Dockerfile";
    public static final String SONAR = "sonar";
    private static final String ISSUE = "issue";
    private static final String COVERAGE = "coverage";
    private static final String CHART_DIR = "charts";
    private static final ConcurrentMap<Long, String> templateDockerfileMap = new ConcurrentHashMap<>();

    private static final IOFileFilter filenameFilter = new IOFileFilter() {
        @Override
        public boolean accept(File file) {
            return accept(null, file.getName());
        }

        @Override
        public boolean accept(File dir, String name) {
            return DOCKER_FILE_NAME.equals(name);
        }
    };

    public static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final String MASTER = "master";
    private static final String APPLICATION = "application";
    private static final String ERROR_UPDATE_APP = "error.application.update";
    private static final String TEST = "test-application";
    private static final String DOCKER_REGISTRY = "DOCKER_REGISTRY";
    private static final String DOCKER_PROJECT = "DOCKER_PROJECT";
    private static final String DOCKER_USERNAME = "DOCKER_USERNAME";
    private static final String DOCKER_CODE = "DOCKER_PASSWORD";
    private static final String CHART_REGISTRY = "CHART_REGISTRY";
    private static final String DUPLICATE = "duplicate";

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
    @Autowired
    private DevopsProjectConfigRepository devopsProjectConfigRepository;

    @Override
    @Saga(code = "devops-create-application",
            description = "Devops创建应用", inputSchema = "{}")
    public ApplicationRepDTO create(Long projectId, ApplicationReqDTO applicationReqDTO) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplication(applicationReqDTO);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        // 查询创建应用所在的gitlab应用组
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(projectId);
        GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (gitlabMemberE == null || gitlabMemberE.getAccessLevel() != AccessLevel.OWNER.toValue()) {
            throw new CommonException("error.user.not.owner");
        }

        ApplicationE applicationE = getApplicationE(projectId, applicationReqDTO);
        applicationE = applicationRepository.create(applicationE);

        Long appId = applicationE.getId();
        if (appId == null) {
            throw new CommonException("error.application.create.insert");
        }
        // 如果不跳过权限检查
        List<Long> userIds = applicationReqDTO.getUserIds();
        if (!applicationReqDTO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> appUserPermissionRepository.create(e, appId));
        }

        IamAppPayLoad iamAppPayLoad = new IamAppPayLoad();
        iamAppPayLoad.setApplicationCategory(APPLICATION);
        iamAppPayLoad.setApplicationType(applicationReqDTO.getType());
        iamAppPayLoad.setCode(applicationReqDTO.getCode());
        iamAppPayLoad.setName(applicationReqDTO.getName());
        iamAppPayLoad.setEnabled(true);
        iamAppPayLoad.setOrganizationId(organization.getId());
        iamAppPayLoad.setProjectId(projectId);
        iamAppPayLoad.setFrom(applicationName);

        iamRepository.createIamApp(organization.getId(), iamAppPayLoad);
        return ConvertHelper.convert(applicationRepository.queryByCode(applicationE.getCode(),
                applicationE.getProjectE().getId()), ApplicationRepDTO.class);
    }

    private ApplicationE getApplicationE(Long projectId, ApplicationReqDTO applicationReqDTO) {

        ApplicationE applicationE = ConvertHelper.convert(applicationReqDTO, ApplicationE.class);
        applicationE.initProjectE(projectId);
        applicationRepository.checkName(applicationE.getProjectE().getId(), applicationE.getName());
        applicationRepository.checkCode(applicationE);
        applicationE.initActive(true);
        applicationE.initSynchro(false);
        applicationE.setIsSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());
        applicationE.initHarborConfig(applicationReqDTO.getHarborConfigId());
        applicationE.initChartConfig(applicationReqDTO.getChartConfigId());
        return applicationE;
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
        ApplicationRepDTO applicationRepDTO = ConvertHelper.convert(applicationE, ApplicationRepDTO.class);
        if (applicationE.getIsSkipCheckPermission()) {
            applicationRepDTO.setPermission(true);
        } else {
            applicationRepDTO.setPermission(false);
        }
        return applicationRepDTO;
    }

    @Override
    @Saga(code = "devops-delete-application",
            description = "Devops删除创建失败应用", inputSchema = "{}")
    public void delete(Long projectId, Long applicationId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = applicationRepository.query(applicationId);
        UserAttrE userAttrE = userAttrRepository.queryById(DetailsHelper.getUserDetails().getUserId());
        gitlabRepository.deleteDevOpsApp(organization.getCode() + "-" + projectE.getCode(),
                applicationE.getCode(), userAttrE.getGitlabUserId().intValue());
        IamAppPayLoad iamAppPayLoad = new IamAppPayLoad();
        iamAppPayLoad.setProjectId(projectId);
        iamAppPayLoad.setOrganizationId(organization.getId());
        iamAppPayLoad.setOrganizationId(organization.getId());
        iamAppPayLoad.setCode(applicationE.getCode());
        iamAppPayLoad.setName(applicationE.getName());
        String input = gson.toJson(iamAppPayLoad);
        sagaClient.startSaga("devops-delete-application", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));
        applicationRepository.delete(applicationId);
    }

    @Saga(code = "devops-update-gitlab-users",
            description = "Devops更新gitlab用户", inputSchema = "{}")
    @Override
    public Boolean update(Long projectId, ApplicationUpdateDTO applicationUpdateDTO) {

        ApplicationE applicationE = ConvertHelper.convert(applicationUpdateDTO, ApplicationE.class);
        applicationE.setIsSkipCheckPermission(applicationUpdateDTO.getIsSkipCheckPermission());
        applicationE.initProjectE(projectId);
        applicationE.initHarborConfig(applicationUpdateDTO.getHarborConfigId());
        applicationE.initChartConfig(applicationUpdateDTO.getChartConfigId());

        Long appId = applicationUpdateDTO.getId();
        ApplicationE oldApplicationE = applicationRepository.query(appId);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabRepository.batchAddVariable(oldApplicationE.getGitlabProjectE().getId(), TypeUtil.objToInteger(userAttrE.getGitlabUserId()),
                setVariableDTO(applicationUpdateDTO.getHarborConfigId(), applicationUpdateDTO.getChartConfigId()));
        if (!oldApplicationE.getName().equals(applicationUpdateDTO.getName())) {
            applicationRepository.checkName(applicationE.getProjectE().getId(), applicationE.getName());
        }
        if (applicationRepository.update(applicationE) != 1) {
            throw new CommonException(ERROR_UPDATE_APP);
        }

        if (!oldApplicationE.getName().equals(applicationUpdateDTO.getName())) {
            ProjectE projectE = iamRepository.queryIamProject(oldApplicationE.getProjectE().getId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            IamAppPayLoad iamAppPayLoad = iamRepository.queryIamAppByCode(organization.getId(), applicationE.getCode());
            iamAppPayLoad.setName(applicationUpdateDTO.getName());
            iamRepository.updateIamApp(organization.getId(), iamAppPayLoad.getId(), iamAppPayLoad);
        }

        // 创建gitlabUserPayload
        DevOpsUserPayload devOpsUserPayload = new DevOpsUserPayload();
        devOpsUserPayload.setIamProjectId(projectId);
        devOpsUserPayload.setAppId(appId);
        devOpsUserPayload.setGitlabProjectId(oldApplicationE.getGitlabProjectE().getId());
        devOpsUserPayload.setIamUserIds(applicationUpdateDTO.getUserIds());

        if (oldApplicationE.getIsSkipCheckPermission() && applicationUpdateDTO.getIsSkipCheckPermission()) {
            return false;
        } else if (oldApplicationE.getIsSkipCheckPermission() && !applicationUpdateDTO.getIsSkipCheckPermission()) {
            applicationUpdateDTO.getUserIds().forEach(e -> appUserPermissionRepository.create(e, appId));
            devOpsUserPayload.setOption(1);
        } else if (!oldApplicationE.getIsSkipCheckPermission() && applicationUpdateDTO.getIsSkipCheckPermission()) {
            appUserPermissionRepository.deleteByAppId(appId);
            devOpsUserPayload.setOption(2);
        } else {
            appUserPermissionRepository.deleteByAppId(appId);
            applicationUpdateDTO.getUserIds().forEach(e -> appUserPermissionRepository.create(e, appId));
            devOpsUserPayload.setOption(3);
        }
        String input = gson.toJson(devOpsUserPayload);
        sagaClient.startSaga("devops-update-gitlab-users", new StartInstanceDTO(input, "app", appId.toString(), ResourceLevel.PROJECT.value(), projectId));

        return true;
    }


    @Override
    public Boolean active(Long applicationId, Boolean active) {
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
        resultDTOPage.setContent(setApplicationRepDTOPermission(applicationES.getContent(), userAttrE, projectE));
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

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Boolean isProjectOwner = iamRepository.isProjectOwner(userAttrE.getIamUserId(), projectE);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        Page<ApplicationE> applicationES = applicationRepository
                .listCodeRepository(projectId, pageRequest, params, isProjectOwner, userAttrE.getIamUserId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        applicationES.forEach(t -> {
                    if (t.getGitlabProjectE() != null && t.getGitlabProjectE().getId() != null) {
                        t.initGitlabProjectEByUrl(gitlabUrl + urlSlash
                                + organization.getCode() + "-" + projectE.getCode() + "/" + t.getCode() + ".git");
                        getSonarUrl(projectE, organization, t);
                    }
                }
        );
        return ConvertPageHelper.convertPage(applicationES, ApplicationRepDTO.class);
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
        return setApplicationRepDTOPermission(applicationEList, userAttrE, projectE);
    }

    private List<ApplicationRepDTO> setApplicationRepDTOPermission(List<ApplicationE> applicationEList,
                                                                   UserAttrE userAttrE, ProjectE projectE) {
        List<ApplicationRepDTO> resultDTOList = ConvertHelper.convertList(applicationEList, ApplicationRepDTO.class);
        if (userAttrE == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }
        if (!iamRepository.isProjectOwner(userAttrE.getIamUserId(), projectE)) {
            List<Long> appIds = appUserPermissionRepository.listByUserId(userAttrE.getIamUserId()).stream()
                    .map(AppUserPermissionE::getAppId).collect(Collectors.toList());
            resultDTOList.stream().filter(e -> e != null && !e.getPermission()).forEach(e -> {
                if (appIds.contains(e.getId())) {
                    e.setPermission(true);
                }
            });
        } else {
            resultDTOList.stream().filter(Objects::nonNull).forEach(e -> e.setPermission(true));
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
    public List<ApplicationTemplateRepDTO> listTemplate(Long projectId, Boolean isPredefined) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        List<ApplicationTemplateE> applicationTemplateES = applicationTemplateRepository.list(projectE.getOrganization().getId())
                .stream()
                .filter(ApplicationTemplateE::getSynchro).collect(Collectors.toList());
        if (isPredefined != null && isPredefined) {
            applicationTemplateES = applicationTemplateES.stream().filter(applicationTemplateE -> applicationTemplateE.getOrganization().getId() == null).collect(Collectors.toList());
        }
        return ConvertHelper.convertList(applicationTemplateES, ApplicationTemplateRepDTO.class);
    }

    /**
     * analyze location of the dockerfile in the template
     *
     * @param templateWorkDir       template work dir
     * @param applicationTemplateId application template id
     */
    private void analyzeDockerfileToMap(File templateWorkDir, Long applicationTemplateId) {
        Collection<File> dockerfile = FileUtils.listFiles(templateWorkDir, filenameFilter, TrueFileFilter.INSTANCE);
        Optional<File> df = dockerfile.stream().findFirst();
        templateDockerfileMap.putIfAbsent(applicationTemplateId, df.map(f -> f.getAbsolutePath().replace(templateWorkDir.getAbsolutePath() + System.getProperty("file.separator"), "")).orElse(DOCKER_FILE_NAME));
    }

    @Override
    public void operationApplication(DevOpsAppPayload gitlabProjectPayload) {
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryByGitlabGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        ApplicationE applicationE = applicationRepository.queryByCode(gitlabProjectPayload.getPath(),
                devopsProjectE.getProjectE().getId());
        ProjectE projectE = iamRepository.queryIamProject(devopsProjectE.getProjectE().getId());
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

        // 为项目下的成员分配对于此gitlab项目的权限
        operateGitlabMemberPermission(gitlabProjectPayload);

        if (applicationE.getApplicationTemplateE() != null) {
            ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
                    applicationE.getApplicationTemplateE().getId());
            //拉取模板
            String applicationDir = APPLICATION + System.currentTimeMillis();
            Git git = cloneTemplate(applicationTemplateE, applicationDir);
            //渲染模板里面的参数
            replaceParams(applicationE, projectE, organization, applicationDir);

            UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));

            // 获取push代码所需的access token
            String accessToken = getToken(gitlabProjectPayload, applicationDir, userAttrE);

            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
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
            String applicationToken = getApplicationToken(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId());
            applicationE.setToken(applicationToken);
            applicationE.initGitlabProjectE(TypeUtil.objToInteger(gitlabProjectPayload.getGitlabProjectId()));
            applicationE.initSynchro(true);
            applicationE.setFailed(false);
            // set project hook id for application
            setProjectHook(applicationE, gitlabProjectDO.getId(), applicationToken, gitlabProjectPayload.getUserId());
//            gitlabRepository.batchAddVariable(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId(),
//                    setVariableDTO(applicationE.getHarborConfigE().getId(), applicationE.getChartConfigE().getId()));
            // 更新并校验
            applicationRepository.updateSql(applicationE);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    /**
     * get application token (set a token if there is not one in gitlab)
     *
     * @param projectId gitlab project id
     * @param userId    gitlab user id
     * @return the application token that is stored in gitlab variables
     */
    private String getApplicationToken(Integer projectId, Integer userId) {
        List<Variable> variables = gitlabRepository.getVariable(projectId, userId);
        if (variables.isEmpty()) {
            String token = GenerateUUID.generateUUID();
            gitlabRepository.addVariable(projectId, "Token", token, false, userId);
            return token;
        } else {
            return variables.get(0).getValue();
        }
    }

    /**
     * 处理当前项目成员对于此gitlab应用的权限
     *
     * @param devOpsAppPayload 此次操作相关信息
     */
    private void operateGitlabMemberPermission(DevOpsAppPayload devOpsAppPayload) {
        // 不跳过权限检查，则为gitlab项目分配项目成员权限
        if (!devOpsAppPayload.getSkipCheckPermission()) {
            if (!devOpsAppPayload.getUserIds().isEmpty()) {
                List<Long> gitlabUserIds = userAttrRepository.listByUserIds(devOpsAppPayload.getUserIds()).stream()
                        .map(UserAttrE::getGitlabUserId).collect(Collectors.toList());
                gitlabUserIds.forEach(e -> {
                    GitlabMemberE gitlabMemberE = gitlabProjectRepository
                            .getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(e));
                    if (gitlabMemberE == null || gitlabMemberE.getId() == null) {
                        gitlabRepository.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                                new MemberDTO(TypeUtil.objToInteger(e), 30, ""));
                    }
                });
            }
        }
        // 跳过权限检查，项目下所有成员自动分配权限
        else {
            List<Long> iamUserIds = iamRepository.getAllMemberIdsWithoutOwner(devOpsAppPayload.getIamProjectId());
            List<Integer> gitlabUserIds = userAttrRepository.listByUserIds(iamUserIds).stream()
                    .map(UserAttrE::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());

            gitlabUserIds.forEach(e -> {
                        GitlabMemberE gitlabMemberE = gitlabProjectRepository.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(e));
                        if (gitlabMemberE == null || gitlabMemberE.getId() == null) {
                            gitlabRepository.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                                    new MemberDTO(TypeUtil.objToInteger(e), 30, ""));
                        }
                    }
            );
        }
    }

    /**
     * 拉取模板库到本地
     *
     * @param applicationTemplateE 模板库的信息
     * @param applicationDir       本地库地址
     * @return 本地库的git实例
     */
    private Git cloneTemplate(ApplicationTemplateE applicationTemplateE, String applicationDir) {
        String repoUrl = applicationTemplateE.getRepoUrl();
        String type = applicationTemplateE.getCode();
        if (applicationTemplateE.getOrganization().getId() != null) {
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
            repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
            type = MASTER;
        }
        return gitUtil.clone(applicationDir, type, repoUrl);
    }

    /**
     * set project hook id for application
     *
     * @param applicationE the application entity
     * @param projectId    the gitlab project id
     * @param token        the token for project hook
     * @param userId       the gitlab user id
     */
    private void setProjectHook(ApplicationE applicationE, Integer projectId, String token, Integer userId) {
        ProjectHook projectHook = ProjectHook.allHook();
        projectHook.setEnableSslVerification(true);
        projectHook.setProjectId(projectId);
        projectHook.setToken(token);
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook";
        projectHook.setUrl(uri);
        List<ProjectHook> projectHooks = gitlabRepository
                .getHooks(projectId, userId);
        if (projectHooks == null) {
            applicationE.initHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(
                    projectId, userId, projectHook)
                    .getId()));
        } else {
            applicationE.initHookId(TypeUtil.objToLong(projectHooks.get(0).getId()));
        }
    }

    @Override
    public void operationApplicationImport(DevOpsAppImportPayload devOpsAppImportPayload) {
        // 准备相关的数据
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryByGitlabGroupId(
                TypeUtil.objToInteger(devOpsAppImportPayload.getGroupId()));
        ApplicationE applicationE = applicationRepository.queryByCode(devOpsAppImportPayload.getPath(),
                devopsProjectE.getProjectE().getId());
        ProjectE projectE = iamRepository.queryIamProject(devopsProjectE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        GitlabProjectDO gitlabProjectDO = gitlabRepository
                .getProjectByName(organization.getCode() + "-" + projectE.getCode(), applicationE.getCode(),
                        devOpsAppImportPayload.getUserId());
        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabRepository.createProject(devOpsAppImportPayload.getGroupId(),
                    devOpsAppImportPayload.getPath(),
                    devOpsAppImportPayload.getUserId(), false);
        }
        devOpsAppImportPayload.setGitlabProjectId(gitlabProjectDO.getId());

        // 为项目下的成员分配对于此gitlab项目的权限
        operateGitlabMemberPermission(devOpsAppImportPayload);

        if (applicationE.getApplicationTemplateE() != null) {
            UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(TypeUtil.objToLong(devOpsAppImportPayload.getUserId()));
            ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
                    applicationE.getApplicationTemplateE().getId());
            // 拉取模板
            String templateDir = APPLICATION + UUIDTool.genUuid();
            Git templateGit = cloneTemplate(applicationTemplateE, templateDir);
            // 渲染模板里面的参数
            replaceParams(applicationE, projectE, organization, templateDir);

            // clone外部代码仓库
            String applicationDir = APPLICATION + UUIDTool.genUuid();
            Git repositoryGit = gitUtil.cloneRepository(applicationDir, devOpsAppImportPayload.getRepositoryUrl(), devOpsAppImportPayload.getAccessToken());

            // 将模板库中文件复制到代码库中
            File templateWorkDir = new File(gitUtil.getWorkingDirectory(templateDir));
            File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));
            mergeTemplateToApplication(templateWorkDir, applicationWorkDir, applicationTemplateE.getId());

            // 获取push代码所需的access token
            String accessToken = getToken(devOpsAppImportPayload, applicationDir, userAttrE);

            // 设置Application对应的gitlab项目的仓库地址
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            applicationE.initGitlabProjectEByUrl(repoUrl + organization.getCode()
                    + "-" + projectE.getCode() + "/" + applicationE.getCode() + ".git");

            BranchDO branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
            if (branchDO.getName() == null) {
                try {
                    // 提交并推代码
                    gitUtil.commitAndPush(repositoryGit, applicationE.getGitlabProjectE().getRepoURL(), accessToken);
                } catch (CommonException e) {
                    releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
                    throw e;
                } finally {
                    releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
                }

                branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
                //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                if (!branchDO.getProtected()) {
                    try {
                        gitlabRepository.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER, AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(), devOpsAppImportPayload.getUserId());
                    } catch (CommonException e) {
                        if (!devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER).getProtected()) {
                            throw new CommonException(e);
                        }
                    }
                }
            } else {
                if (!branchDO.getProtected()) {
                    gitlabRepository.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER,
                            AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                            devOpsAppImportPayload.getUserId());
                }
            }
            initMasterBranch(devOpsAppImportPayload, applicationE);
        }
        try {
            // 设置appliation的属性
            String applicationToken = getApplicationToken(gitlabProjectDO.getId(), devOpsAppImportPayload.getUserId());
            applicationE.initGitlabProjectE(TypeUtil.objToInteger(devOpsAppImportPayload.getGitlabProjectId()));
            applicationE.setToken(applicationToken);
            applicationE.setSynchro(true);

            // set project hook id for application
            setProjectHook(applicationE, gitlabProjectDO.getId(), applicationToken, devOpsAppImportPayload.getUserId());

            // 更新并校验
            if (applicationRepository.update(applicationE) != 1) {
                throw new CommonException(ERROR_UPDATE_APP);
            }
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


    /**
     * 释放资源
     */
    private void releaseResources(File templateWorkDir, File applicationWorkDir, Git templateGit, Git repositoryGit) {
        FileUtil.deleteDirectory(templateWorkDir);
        FileUtil.deleteDirectory(applicationWorkDir);
        if (templateGit != null) {
            templateGit.close();
        }
        if (repositoryGit != null) {
            repositoryGit.close();
        }
    }

    /**
     * 将模板库中的chart包，dockerfile，gitlab-ci文件复制到导入的代码仓库中
     * 复制文件前会判断文件是否存在，如果存在则不复制
     *
     * @param templateWorkDir       模板库工作目录
     * @param applicationWorkDir    应用库工作目录
     * @param applicationTemplateId application template id
     */
    private void mergeTemplateToApplication(File templateWorkDir, File applicationWorkDir, Long applicationTemplateId) {
        // ci 文件
        File appGitlabCiFile = new File(applicationWorkDir, GITLAB_CI_FILE);
        File templateGitlabCiFile = new File(templateWorkDir, GITLAB_CI_FILE);
        if (!appGitlabCiFile.exists() && templateGitlabCiFile.exists()) {
            FileUtil.copyFile(templateGitlabCiFile, appGitlabCiFile);
        }

        // Dockerfile 文件
        if (!templateDockerfileMap.containsKey(applicationTemplateId)) {
            analyzeDockerfileToMap(templateWorkDir, applicationTemplateId);
        }
        File appDockerFile = new File(applicationWorkDir, templateDockerfileMap.get(applicationTemplateId));
        File templateDockerFile = new File(templateWorkDir, templateDockerfileMap.get(applicationTemplateId));
        if (!appDockerFile.exists() && templateDockerFile.exists()) {
            FileUtil.copyFile(templateDockerFile, appDockerFile);
        }

        // chart文件夹
        File appChartDir = new File(applicationWorkDir, CHART_DIR);
        File templateChartDir = new File(templateWorkDir, CHART_DIR);
        if (!appChartDir.exists() && templateChartDir.exists()) {
            FileUtil.copyDir(templateChartDir, appChartDir);
        }
    }

    @Override
    @Saga(code = "devops-set-app-err",
            description = "Devops设置application状态为创建失败(devops set app status create err)", inputSchema = "{}")
    public void setAppErrStatus(String input, Long projectId) {
        sagaClient.startSaga("devops-set-app-err", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));
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
        devopsBranchE.setCheckoutDate(commitE.getCommittedDate());
        devopsBranchE.setLastCommitUser(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchE.setLastCommitMsg(commitE.getMessage());
        devopsBranchE.setLastCommitDate(commitE.getCommittedDate());
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
            ProjectConfigDTO harborProjectConfig;
            ProjectConfigDTO chartProjectConfig;
            if (applicationE.getHarborConfigE() != null) {
                harborProjectConfig = devopsProjectConfigRepository.queryByPrimaryKey(applicationE.getHarborConfigE().getId()).getConfig();
            } else {
                harborProjectConfig = devopsProjectConfigRepository.queryByIdAndType(null, ProjectConfigType.HARBOR.getType()).get(0).getConfig();
            }
            if (applicationE.getChartConfigE() != null) {
                chartProjectConfig = devopsProjectConfigRepository.queryByPrimaryKey(applicationE.getChartConfigE().getId()).getConfig();
            } else {
                chartProjectConfig = devopsProjectConfigRepository.queryByIdAndType(null, ProjectConfigType.CHART.getType()).get(0).getConfig();
            }
            if (type == null) {
                inputStream = this.getClass().getResourceAsStream("/shell/ci.sh");
            } else {
                inputStream = this.getClass().getResourceAsStream("/shell/" + type + ".sh");
            }
            Map<String, String> params = new HashMap<>();
            String groupName = organization.getCode() + "-" + projectE.getCode();
            if (harborProjectConfig.getProject() != null) {
                groupName = harborProjectConfig.getProject();
            }
            String dockerUrl = harborProjectConfig.getUrl().replace("http://", "").replace("https://", "");
            dockerUrl = dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;

            params.put("{{ GROUP_NAME }}", groupName);
            params.put("{{ PROJECT_NAME }}", applicationE.getCode());
            params.put("{{ PRO_CODE }}", projectE.getCode());
            params.put("{{ ORG_CODE }}", organization.getCode());
            params.put("{{ DOCKER_REGISTRY }}", dockerUrl);
            params.put("{{ DOCKER_USERNAME }}", harborProjectConfig.getUserName());
            params.put("{{ DOCKER_PASSWORD }}", harborProjectConfig.getPassword());
            params.put("{{ CHART_REGISTRY }}", chartProjectConfig.getUrl().endsWith("/") ? chartProjectConfig.getUrl().substring(0, chartProjectConfig.getUrl().length() - 1) : chartProjectConfig.getUrl());
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
    public Page<ApplicationCodeDTO> pageByEnvId(Long projectId, Long envId, Long appId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPage(applicationRepository.pageByEnvId(projectId, envId, appId, pageRequest),
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
        List<Long> userIds = appUserPermissionRepository.listAll(appId).stream().map(AppUserPermissionE::getIamUserId)
                .collect(Collectors.toList());
        List<UserE> userEList = iamRepository.listUsersByIds(userIds);
        List<AppUserPermissionRepDTO> resultList = new ArrayList<>();
        userEList.forEach(
                e -> resultList.add(new AppUserPermissionRepDTO(e.getId(), e.getLoginName(), e.getRealName())));
        return resultList;
    }

    @Override
    public Boolean validateRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String accessToken) {
        if (!REPOSITORY_URL_PATTERN.matcher(repositoryUrl).matches()) {
            return Boolean.FALSE;
        }

        // 当不存在access_token时，默认将仓库识别为公开的
        return GitUtil.validRepositoryUrl(repositoryUrl, accessToken);
    }

    /**
     * ensure the repository url and access token are valid.
     *
     * @param gitPlatformType git platform type
     * @param repositoryUrl   repository url
     * @param accessToken     access token (Nullable)
     */
    private void checkRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String accessToken) {
        Boolean validationResult = validateRepositoryUrlAndToken(gitPlatformType, repositoryUrl, accessToken);
        if (Boolean.FALSE.equals(validationResult)) {
            throw new CommonException("error.repository.token.invalid");
        } else if (validationResult == null) {
            throw new CommonException("error.repository.empty");
        }
    }

    @Override
    @Saga(code = "devops-import-gitlab-project", description = "Devops从外部代码平台导入到gitlab项目", inputSchema = "{}")
    public ApplicationRepDTO importApplicationFromGitPlatform(Long projectId, ApplicationImportDTO applicationImportDTO) {
        // 获取当前操作的用户的信息
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验application信息的格式
        ApplicationValidator.checkApplication(applicationImportDTO);

        // 校验名称唯一性
        applicationRepository.checkName(projectId, applicationImportDTO.getName());

        // 校验code唯一性
        applicationRepository.checkCode(projectId, applicationImportDTO.getCode());

        // 校验repository（和token） 地址是否有效
        GitPlatformType gitPlatformType = GitPlatformType.from(applicationImportDTO.getPlatformType());
        checkRepositoryUrlAndToken(gitPlatformType, applicationImportDTO.getRepositoryUrl(), applicationImportDTO.getAccessToken());

        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        ApplicationE applicationE = fromImportDtoToEntity(applicationImportDTO);

        applicationE.initProjectE(projectId);


        applicationE.initActive(true);
        applicationE.initSynchro(false);
        applicationE.setIsSkipCheckPermission(applicationImportDTO.getIsSkipCheckPermission());
        applicationE.initHarborConfig(applicationImportDTO.getHarborConfigId());
        applicationE.initChartConfig(applicationImportDTO.getChartConfigId());

        // 查询创建应用所在的gitlab应用组
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(applicationE.getProjectE().getId());
        GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));

        // 校验用户的gitlab权限
        if (gitlabMemberE == null || gitlabMemberE.getAccessLevel() != AccessLevel.OWNER.toValue()) {
            throw new CommonException("error.user.not.owner");
        }

        // 创建应用
        applicationE = applicationRepository.create(applicationE);

        // 校验创建成功与否
        Long appId = applicationE.getId();
        if (appId == null) {
            throw new CommonException("error.application.create.insert");
        }

        // 创建saga payload
        DevOpsAppImportPayload devOpsAppImportPayload = new DevOpsAppImportPayload();
        devOpsAppImportPayload.setType(APPLICATION);
        devOpsAppImportPayload.setPath(applicationImportDTO.getCode());
        devOpsAppImportPayload.setOrganizationId(organization.getId());
        devOpsAppImportPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        devOpsAppImportPayload.setGroupId(TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()));
        devOpsAppImportPayload.setUserIds(applicationImportDTO.getUserIds());
        devOpsAppImportPayload.setSkipCheckPermission(applicationImportDTO.getIsSkipCheckPermission());
        devOpsAppImportPayload.setAppId(appId);
        devOpsAppImportPayload.setIamProjectId(projectId);
        devOpsAppImportPayload.setPlatformType(gitPlatformType);
        devOpsAppImportPayload.setRepositoryUrl(applicationImportDTO.getRepositoryUrl());
        devOpsAppImportPayload.setAccessToken(applicationImportDTO.getAccessToken());
        devOpsAppImportPayload.setGitlabUserId(userAttrE.getGitlabUserId());

        // 如果不跳过权限检查
        List<Long> userIds = applicationImportDTO.getUserIds();
        if (!applicationImportDTO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> appUserPermissionRepository.create(e, appId));
        }

        String input = gson.toJson(devOpsAppImportPayload);
        sagaClient.startSaga("devops-import-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));

        return ConvertHelper.convert(applicationRepository.query(appId), ApplicationRepDTO.class);
    }

    @Override
    public ApplicationRepDTO queryByCode(Long projectId, String code) {
        return ConvertHelper.convert(applicationRepository.queryByCode(code, projectId), ApplicationRepDTO.class);
    }


    @Override
    @Saga(code = "devops-create-gitlab-project",
            description = "Devops创建gitlab项目", inputSchema = "{}")
    public void createIamApplication(IamAppPayLoad iamAppPayLoad) {

        List<Long> userIds = new ArrayList<>();
        ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        if (applicationE == null) {
            applicationE = new ApplicationE();
            applicationE.setIsSkipCheckPermission(true);
            applicationE.setName(iamAppPayLoad.getName());
            applicationE.setCode(iamAppPayLoad.getCode());
            applicationE.initActive(true);
            applicationE.initSynchro(false);
            applicationE.initProjectE(iamAppPayLoad.getProjectId());
            applicationE.setType("normal");
            if (iamAppPayLoad.getApplicationType().equals(TEST)) {
                applicationE.setType("test");
            }
            applicationE = applicationRepository.create(applicationE);
        } else {
            //创建iam入口过来的应用直接跳过权限校验，从devops入口过来的应用选择了特定用户权限，需要给特定用户分配该用户权限
            if (!applicationE.getIsSkipCheckPermission()) {
                userIds = appUserPermissionRepository.listAll(applicationE.getId()).stream().map(AppUserPermissionE::getIamUserId).collect(Collectors.toList());
            }
        }

        //创建iam入口过来的应用直接用管理员去gitlab创建对应的project,避免没有对应项目的权限导致创建失败
        Long gitlabUserId = 1L;
        if (applicationName.equals(iamAppPayLoad.getFrom())) {
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            gitlabUserId = userAttrE.getGitlabUserId();
        }

        DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(iamAppPayLoad.getProjectId());

        //创建saga payload
        DevOpsAppPayload devOpsAppPayload = new DevOpsAppPayload();
        devOpsAppPayload.setType(APPLICATION);
        devOpsAppPayload.setPath(iamAppPayLoad.getCode());
        devOpsAppPayload.setOrganizationId(iamAppPayLoad.getOrganizationId());
        devOpsAppPayload.setUserId(TypeUtil.objToInteger(gitlabUserId));
        devOpsAppPayload.setGroupId(TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()));
        devOpsAppPayload.setUserIds(userIds);
        devOpsAppPayload.setSkipCheckPermission(applicationE.getIsSkipCheckPermission());
        devOpsAppPayload.setAppId(applicationE.getId());
        devOpsAppPayload.setIamProjectId(iamAppPayLoad.getProjectId());
        //0.14.0-0.15.0的时候，同步已有的app到iam，此时app已经存在gitlab project,不需要再创建
        if (applicationE.getGitlabProjectE().getId() == null) {
            String input = gson.toJson(devOpsAppPayload);
            sagaClient.startSaga("devops-create-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), iamAppPayLoad.getProjectId()));
        }
    }

    @Override
    public void updateIamApplication(IamAppPayLoad iamAppPayLoad) {
        ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        applicationE.setName(iamAppPayLoad.getName());
        applicationRepository.update(applicationE);
    }

    @Override
    public Boolean checkHarborIsUsable(String url, String userName, String password, String project, String email) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(url);
        configurationProperties.setUsername(userName);
        configurationProperties.setPassword(password);
        configurationProperties.setInsecureSkipTlsVerify(false);
        configurationProperties.setProject(project);
        configurationProperties.setType("harbor");
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        Call<User> getUser = harborClient.getCurrentUser();
        Response<User> userResponse = null;
        try {
            userResponse = getUser.execute();
            if (userResponse.raw().code() != 200) {
                if (userResponse.raw().code() == 401) {
                    throw new CommonException("error.harbor.user.password");
                } else {
                    throw new CommonException(userResponse.errorBody().string());
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
        //校验用户的邮箱是否匹配
        if (!email.equals(userResponse.body().getEmail())) {
            throw new CommonException("error.user.email.not.equal");
        }

        //如果传入了project,校验用户是否有project的权限
        Call<List<ProjectDetail>> listProject = harborClient.listProject(project);
        Response<List<ProjectDetail>> projectResponse = null;
        try {
            projectResponse = listProject.execute();
            if (projectResponse.body() == null) {
                throw new CommonException("error.harbor.project.permission");
            } else {
                if (project != null) {
                    List<ProjectDetail> projects = (projectResponse.body()).stream().filter(a -> (a.getName().equals(configurationProperties.getProject()))).collect(Collectors.toList());
                    if (projects.isEmpty()) {
                        throw new CommonException("error.harbor.project.permission");
                    }
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return true;
    }


    @Override
    public Boolean checkChartIsUsable(String url) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(url);
        configurationProperties.setType("chart");
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        chartClient.getHealth();
        Call<Object> getHealth = chartClient.getHealth();
        try {
            getHealth.execute();
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return true;
    }


    private ApplicationE fromImportDtoToEntity(ApplicationImportDTO applicationImportDTO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        applicationE.initProjectE(applicationImportDTO.getProjectId());
        BeanUtils.copyProperties(applicationImportDTO, applicationE);
        if (applicationImportDTO.getApplicationTemplateId() != null) {
            applicationE.initApplicationTemplateE(applicationImportDTO.getApplicationTemplateId());
        }
        applicationE.initHarborConfig(applicationImportDTO.getHarborConfigId());
        applicationE.initChartConfig(applicationImportDTO.getChartConfigId());
        return applicationE;
    }

    @Override
    public List<VariableDTO> setVariableDTO(Long harborConfigId, Long chartConfigId) {
        List<VariableDTO> variableDTOS = new ArrayList<>();
        DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.queryByPrimaryKey(harborConfigId);
        if (devopsProjectConfigE != null) {
            String dockerUrl = devopsProjectConfigE.getConfig().getUrl().replace("https://", "");
            dockerUrl = dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;
            variableDTOS.add(new VariableDTO(DOCKER_REGISTRY, dockerUrl, false));
            variableDTOS.add(new VariableDTO(DOCKER_PROJECT, devopsProjectConfigE.getConfig().getProject(), false));
            variableDTOS.add(new VariableDTO(DOCKER_USERNAME, devopsProjectConfigE.getConfig().getUserName(), false));
            variableDTOS.add(new VariableDTO(DOCKER_CODE, devopsProjectConfigE.getConfig().getPassword(), false));
        }
        devopsProjectConfigE = devopsProjectConfigRepository.queryByPrimaryKey(chartConfigId);
        if (devopsProjectConfigE != null) {
            String chartUrl = devopsProjectConfigE.getConfig().getUrl();
            chartUrl = chartUrl.endsWith("/") ? chartUrl.substring(0, chartUrl.length() - 1) : chartUrl;
            variableDTOS.add(new VariableDTO(CHART_REGISTRY, chartUrl, false));
        }
        return variableDTOS;
    }

    @Override
    public SonarContentsDTO getSonarContent(Long projectId, Long appId) {
        SonarContentsDTO sonarContentsDTO = new SonarContentsDTO();
        List<SonarContentDTO> sonarContentDTOS = new ArrayList<>();
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        SonarClient sonarClient = getSonarClient();
        String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), applicationE.getCode());
        sonarqubeUrl = sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/";
        try {
            Map<String, String> queryContentMap = new HashMap<>();
            queryContentMap.put("additionalFields", "metrics,periods");
            queryContentMap.put("componentKey", key);
            queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,new_bugs,new_vulnerabilities,sqale_index,code_smells,new_technical_debt,new_code_smells,coverage,tests,new_coverage,duplicated_lines_density,duplicated_blocks,new_duplicated_lines_density,ncloc,ncloc_language_distribution");
            Response<SonarComponent> sonarComponentResponse = sonarClient.getSonarComponet(queryContentMap).execute();
            if (sonarComponentResponse.raw().code() != 200) {
                if (sonarComponentResponse.raw().code() == 404) {
                    return new SonarContentsDTO();
                }
                throw new CommonException(sonarComponentResponse.errorBody().string());
            }
            if (sonarComponentResponse.body() == null) {
                return new SonarContentsDTO();
            }
            sonarContentsDTO.setDate(sonarComponentResponse.body().getPeriods().get(0).getDate());
            sonarContentsDTO.setMode(sonarComponentResponse.body().getPeriods().get(0).getMode());
            sonarContentsDTO.setParameter(sonarComponentResponse.body().getPeriods().get(0).getParameter());
            sonarComponentResponse.body().getComponent().getMeasures().stream().forEach(measure -> {
                SonarQubeType sonarQubeType = SonarQubeType.forValue(String.valueOf(measure.getMetric()));
                switch (sonarQubeType) {
                    case BUGS:
                        SonarContentDTO bug = new SonarContentDTO();
                        bug.setKey(measure.getMetric());
                        bug.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        bug.setUrl(String.format("%sproject/issues?id=%s&resolved=false&types=BUG", sonarqubeUrl, key));
                        try {
                            Map<String, String> queryBugMap = getQueryMap(key, "BUG", false);
                            Response<Bug> bugResponse = sonarClient.getBugs(queryBugMap).execute();
                            if (bugResponse.raw().code() != 200) {
                                throw new CommonException(bugResponse.errorBody().string());
                            }
                            List<Facet> facets = bugResponse.body().getFacets();
                            getRate(bug, facets);
                        } catch (IOException e) {
                            throw new CommonException(e);
                        }
                        sonarContentDTOS.add(bug);
                        break;
                    case VULNERABILITIES:
                        SonarContentDTO vulnerabilities = new SonarContentDTO();
                        vulnerabilities.setKey(measure.getMetric());
                        vulnerabilities.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        vulnerabilities.setUrl(String.format("%sproject/issues?id=%s&resolved=false&types=VULNERABILITY", sonarqubeUrl, key));
                        try {
                            Map<String, String> queryVulnerabilitiesMap = getQueryMap(key, "VULNERABILITY", false);
                            Response<Vulnerability> vulnerabilityResponse = sonarClient.getVulnerability(queryVulnerabilitiesMap).execute();
                            if (vulnerabilityResponse.raw().code() != 200) {
                                throw new CommonException(vulnerabilityResponse.errorBody().string());
                            }
                            List<Facet> facets = vulnerabilityResponse.body().getFacets();
                            getRate(vulnerabilities, facets);
                        } catch (IOException e) {
                            throw new CommonException(e);
                        }
                        sonarContentDTOS.add(vulnerabilities);
                        break;
                    case NEW_BUGS:
                        SonarContentDTO newBug = new SonarContentDTO();
                        newBug.setKey(measure.getMetric());
                        newBug.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        newBug.setUrl(String.format("%sproject/issues?id=%s&resolved=false&sinceLeakPeriod=true&types=BUG", sonarqubeUrl, key));
                        try {
                            Map<String, String> queryNewBugMap = getQueryMap(key, "BUG", true);

                            Response<Bug> newBugResponse = sonarClient.getNewBugs(queryNewBugMap).execute();
                            if (newBugResponse.raw().code() != 200) {
                                throw new CommonException(newBugResponse.errorBody().string());
                            }
                            List<Facet> facets = newBugResponse.body().getFacets();
                            getRate(newBug, facets);
                        } catch (IOException e) {
                            throw new CommonException(e);
                        }
                        sonarContentDTOS.add(newBug);
                        break;
                    case NEW_VULNERABILITIES:
                        SonarContentDTO newVulnerabilities = new SonarContentDTO();
                        newVulnerabilities.setKey(measure.getMetric());
                        newVulnerabilities.setValue(measure.getPeriods().get(0).getValue());
                        newVulnerabilities.setUrl(String.format("%sproject/issues?id=%s&resolved=false&sinceLeakPeriod=true&types=VULNERABILITY", sonarqubeUrl, key));
                        try {
                            Map<String, String> queryNewVulnerabilitiesMap = getQueryMap(key, "VULNERABILITY", true);
                            Response<Vulnerability> newVulnerabilityResponse = sonarClient.getNewVulnerability(queryNewVulnerabilitiesMap).execute();
                            if (newVulnerabilityResponse.raw().code() != 200) {
                                throw new CommonException(newVulnerabilityResponse.errorBody().string());
                            }
                            List<Facet> facets = newVulnerabilityResponse.body().getFacets();
                            getRate(newVulnerabilities, facets);
                        } catch (IOException e) {
                            throw new CommonException(e);
                        }
                        sonarContentDTOS.add(newVulnerabilities);
                        break;
                    case SQALE_INDEX:
                        SonarContentDTO debt = new SonarContentDTO();
                        debt.setKey(measure.getMetric());
                        debt.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        double day = measure.getValue() == null ? 0 : TypeUtil.objTodouble(measure.getValue()) / 480;
                        double hour = measure.getValue() == null ? 0 : TypeUtil.objTodouble(measure.getValue()) / 60;
                        if (day >= 1) {
                            debt.setValue(String.format("%sd", Math.round(day)));
                        } else if (hour >= 1) {
                            debt.setValue(String.format("%sh", Math.round(hour)));
                        } else {
                            debt.setValue(String.format("%s%s", Math.round(TypeUtil.objTodouble(measure.getValue() == null ? 0 : measure.getValue())), measure.getValue() == null ? "" : "min"));
                        }
                        debt.setUrl(String.format("%sproject/issues?facetMode=effort&id=%s&resolved=false&types=CODE_SMELL", sonarqubeUrl, key));
                        sonarContentDTOS.add(debt);
                        break;
                    case CODE_SMELLS:
                        SonarContentDTO codeSmells = new SonarContentDTO();
                        codeSmells.setKey(measure.getMetric());
                        double result = measure.getValue() == null ? 0 : TypeUtil.objToLong(measure.getValue()) / 1000;
                        if (result > 0) {
                            if (TypeUtil.objToLong(measure.getValue()) % 1000 == 0) {
                                codeSmells.setValue(String.format("%sK", result));
                            } else {
                                BigDecimal codeSmellDecimal = new BigDecimal(result);
                                codeSmells.setValue(String.format("%sK", codeSmellDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                            }
                        } else {
                            codeSmells.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        }
                        codeSmells.setUrl(String.format("%sproject/issues?id=%s&resolved=false&types=CODE_SMELL", sonarqubeUrl, key));
                        sonarContentDTOS.add(codeSmells);
                        break;
                    case NEW_TECHNICAL_DEBT:
                        SonarContentDTO newDebt = new SonarContentDTO();
                        newDebt.setKey(measure.getMetric());
                        double newDay = TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()) / 480;
                        double newHour = TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()) / 60;
                        if (newDay >= 1) {
                            newDebt.setValue(String.format("%sd", Math.round(newDay)));
                        } else if (newHour >= 1) {
                            newDebt.setValue(String.format("%sh", Math.round(newHour)));
                        } else {
                            newDebt.setValue(String.format("%s%s", measure.getPeriods().get(0).getValue(), measure.getPeriods().get(0).getValue().equals("0") ? "" : "min"));
                        }
                        newDebt.setUrl(String.format("%sproject/issues?facetMode=effort&id=%s&resolved=false&sinceLeakPeriod=true&types=CODE_SMELL", sonarqubeUrl, key));
                        sonarContentDTOS.add(newDebt);
                        break;
                    case NEW_CODE_SMELLS:
                        SonarContentDTO newCodeSmells = new SonarContentDTO();
                        newCodeSmells.setKey(measure.getMetric());
                        double newResult = TypeUtil.objToLong(measure.getPeriods().get(0).getValue()) / 1000;
                        if (newResult > 0) {
                            if (TypeUtil.objToLong(measure.getPeriods().get(0).getValue()) % 1000 == 0) {
                                newCodeSmells.setValue(String.format("%sK", newResult));
                            } else {
                                BigDecimal codeSmellDecimal = new BigDecimal(newResult);
                                newCodeSmells.setValue(String.format("%sK", codeSmellDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                            }
                        } else {
                            newCodeSmells.setValue(measure.getPeriods().get(0).getValue());
                        }
                        newCodeSmells.setUrl(String.format("%sproject/issues?id=%s&resolved=false&sinceLeakPeriod=true&types=CODE_SMELL", sonarqubeUrl, key));
                        sonarContentDTOS.add(newCodeSmells);
                        break;
                    case COVERAGE:
                        SonarContentDTO coverage = new SonarContentDTO();
                        coverage.setKey(measure.getMetric());
                        coverage.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        coverage.setUrl(String.format("%scomponent_measures?id=%s&metric=coverage", sonarqubeUrl, key));
                        sonarContentDTOS.add(coverage);
                        break;
                    case NEW_COVERAGE:
                        SonarContentDTO newCoverage = new SonarContentDTO();
                        newCoverage.setKey(measure.getMetric());
                        BigDecimal codeSmellDecimal = new BigDecimal(measure.getPeriods().get(0).getValue());
                        newCoverage.setValue(String.format("%s", codeSmellDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                        newCoverage.setUrl(String.format("%scomponent_measures?id=%s&metric=new_coverage", sonarqubeUrl, key));
                        sonarContentDTOS.add(newCoverage);
                        break;
                    case DUPLICATED_LINES_DENSITY:
                        SonarContentDTO duplicated = new SonarContentDTO();
                        duplicated.setKey(measure.getMetric());
                        duplicated.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        duplicated.setUrl(String.format("%scomponent_measures?id=%s&metric=duplicated_lines_density", sonarqubeUrl, key));
                        if (TypeUtil.objTodouble(measure.getValue()) >= 0 && TypeUtil.objTodouble(measure.getValue()) < 3) {
                            duplicated.setRate("A");
                        } else if (TypeUtil.objTodouble(measure.getValue()) >= 3 && TypeUtil.objTodouble(measure.getValue()) < 10) {
                            duplicated.setRate("B");
                        } else if (TypeUtil.objTodouble(measure.getValue()) >= 10 && TypeUtil.objTodouble(measure.getValue()) < 20) {
                            duplicated.setRate("C");
                        } else {
                            duplicated.setRate("D");
                        }
                        sonarContentDTOS.add(duplicated);
                        break;
                    case DUPLICATED_BLOCKS:
                        SonarContentDTO duplicatedBlocks = new SonarContentDTO();
                        duplicatedBlocks.setKey(measure.getMetric());
                        duplicatedBlocks.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        duplicatedBlocks.setUrl(String.format("%scomponent_measures?id=%s&metric=duplicated_blocks", sonarqubeUrl, key));
                        sonarContentDTOS.add(duplicatedBlocks);
                        break;
                    case NEW_DUPLICATED_LINES_DENSITY:
                        SonarContentDTO newDuplicated = new SonarContentDTO();
                        newDuplicated.setKey(measure.getMetric());
                        if (TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()) == 0) {
                            newDuplicated.setValue("0");
                        } else {
                            BigDecimal b = new BigDecimal(TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()));
                            newDuplicated.setValue(TypeUtil.objToString(b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                        }
                        newDuplicated.setUrl(String.format("%scomponent_measures?id=%s&metric=new_duplicated_lines_density", sonarqubeUrl, key));
                        sonarContentDTOS.add(newDuplicated);
                        break;
                    case NCLOC:
                        SonarContentDTO ncloc = new SonarContentDTO();
                        ncloc.setKey(measure.getMetric());
                        double nclocResult = TypeUtil.objTodouble(measure.getValue()) / 1000;
                        if (nclocResult >= 0) {
                            if (TypeUtil.objToLong(measure.getValue()) % 1000 == 0) {
                                ncloc.setValue(String.format("%sK", nclocResult));
                            } else {
                                BigDecimal nclocDecimal = new BigDecimal(nclocResult);
                                ncloc.setValue(String.format("%sK", nclocDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                            }
                        } else {
                            ncloc.setValue(measure.getValue());
                        }
                        if (TypeUtil.objToLong(measure.getValue()) > 0 && TypeUtil.objToLong(measure.getValue()) < 1000) {
                            ncloc.setRate("XS");
                        } else if (TypeUtil.objToLong(measure.getValue()) >= 1000 && TypeUtil.objToLong(measure.getValue()) < 10000) {
                            ncloc.setRate("S");
                        } else if (TypeUtil.objToLong(measure.getValue()) >= 10000 && TypeUtil.objToLong(measure.getValue()) < 100000) {
                            ncloc.setRate("M");
                        } else if (TypeUtil.objToLong(measure.getValue()) >= 100000 && TypeUtil.objToLong(measure.getValue()) < 500000) {
                            ncloc.setRate("L");
                        } else {
                            ncloc.setRate("XL");
                        }
                        sonarContentDTOS.add(ncloc);
                        break;
                    case TESTS:
                        SonarContentDTO test = new SonarContentDTO();
                        test.setKey(measure.getMetric());
                        test.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        test.setUrl(String.format("%scomponent_measures?id=%s&metric=tests", sonarqubeUrl, key));
                        sonarContentDTOS.add(test);
                        break;
                    case NCLOC_LANGUAGE_DISTRIBUTION:
                        SonarContentDTO nclocLanguage = new SonarContentDTO();
                        nclocLanguage.setKey(measure.getMetric());
                        nclocLanguage.setValue(measure.getValue());
                        sonarContentDTOS.add(nclocLanguage);
                        break;
                    case QUALITY_GATE_DETAILS:
                        Quality quality = gson.fromJson(measure.getValue(), Quality.class);
                        sonarContentsDTO.setStatus(quality.getLevel());
                        break;
                    default:
                        break;
                }
            });
            sonarContentsDTO.setSonarContents(sonarContentDTOS);
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return sonarContentsDTO;
    }

    @Override
    public SonarTableDTO getSonarTable(Long projectId, Long appId, String type, Date startTime, Date endTime) {
        SonarTableDTO sonarTableDTO = new SonarTableDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        SonarClient sonarClient = getSonarClient();
        String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), applicationE.getCode());
        sonarqubeUrl = sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/";
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("component", key);
        queryMap.put("ps", "1000");
        if (ISSUE.equals(type)) {
            queryMap.put("metrics", "bugs,code_smells,vulnerabilities");
            try {
                Response<SonarTables> sonarTablesResponse = sonarClient.getSonarTables(queryMap).execute();
                if (sonarTablesResponse.raw().code() != 200) {
                    if (sonarTablesResponse.raw().code() == 404) {
                        return new SonarTableDTO();
                    }
                    throw new CommonException(sonarTablesResponse.errorBody().string());
                }
                List<String> bugs = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                List<String> codeSmells = new ArrayList<>();
                List<String> vulnerabilities = new ArrayList<>();
                sonarTablesResponse.body().getMeasures().stream().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.BUGS.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            bugs.add(sonarHistroy.getValue());
                            dates.add(sonarHistroy.getDate());
                        });
                        sonarTableDTO.setDates(dates);
                        sonarTableDTO.setBugs(bugs);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.CODE_SMELLS.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            codeSmells.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setCodeSmells(codeSmells);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.VULNERABILITIES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            vulnerabilities.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setVulnerabilities(vulnerabilities);
                    }
                });
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }
        if (COVERAGE.equals(type)) {
            queryMap.put("metrics", "lines_to_cover,uncovered_lines,coverage");
            try {
                Response<SonarTables> sonarTablesResponse = sonarClient.getSonarTables(queryMap).execute();
                if (sonarTablesResponse.raw().code() != 200) {
                    if (sonarTablesResponse.raw().code() == 404) {
                        return new SonarTableDTO();
                    }
                    throw new CommonException(sonarTablesResponse.errorBody().string());
                }
                List<String> linesToCover = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                List<String> unCoverLines = new ArrayList<>();
                List<String> coverLines = new ArrayList<>();
                List<String> coverage = new ArrayList<>();
                sonarTablesResponse.body().getMeasures().stream().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.COVERAGE.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            coverage.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setCoverage(coverage);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.LINES_TO_COVER.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            linesToCover.add(sonarHistroy.getValue());
                            dates.add(sonarHistroy.getDate());
                        });
                        sonarTableDTO.setDates(dates);
                        sonarTableDTO.setLinesToCover(linesToCover);
                    }

                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.UNCOVERED_LINES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            unCoverLines.add(sonarHistroy.getValue());
                        });
                    }
                    for (int i = 0; i < linesToCover.size(); i++) {
                        coverLines.add(TypeUtil.objToString(TypeUtil.objToLong(linesToCover.get(i)) - TypeUtil.objToLong(unCoverLines.get(i))));
                    }
                    sonarTableDTO.setCoverLines(coverLines);
                });

            } catch (IOException e) {
                throw new CommonException(e);
            }
        }
        if (DUPLICATE.equals(type)) {
            queryMap.put("metrics", "ncloc,duplicated_lines,duplicated_lines_density");
            try {
                Response<SonarTables> sonarTablesResponse = sonarClient.getSonarTables(queryMap).execute();
                if (sonarTablesResponse.raw().code() != 200) {
                    if (sonarTablesResponse.raw().code() == 404) {
                        return new SonarTableDTO();
                    }
                    throw new CommonException(sonarTablesResponse.errorBody().string());
                }
                List<String> nclocs = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                List<String> duplicatedLines = new ArrayList<>();
                List<String> duplicatedLinesRate = new ArrayList<>();
                sonarTablesResponse.body().getMeasures().stream().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.NCLOC.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            nclocs.add(sonarHistroy.getValue());
                            dates.add(sonarHistroy.getDate());
                        });
                        sonarTableDTO.setNclocs(nclocs);
                        sonarTableDTO.setDates(dates);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy ->
                                duplicatedLines.add(sonarHistroy.getValue())
                        );
                        sonarTableDTO.setDuplicatedLines(duplicatedLines);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES_DENSITY.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, endTime, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            duplicatedLinesRate.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setDuplicatedLinesRate(duplicatedLinesRate);
                    }
                });
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }
        return sonarTableDTO;
    }

    private boolean getHistory(Date startTime, Date endTime, SimpleDateFormat sdf, SonarHistroy sonarHistroy) {
        try {
            return sdf.parse(sonarHistroy.getDate()).compareTo(startTime) >= 0 && sdf.parse(sonarHistroy.getDate()).compareTo(endTime) <= 0;
        } catch (ParseException e) {
            throw new CommonException(e);
        }
    }


    private void getRate(SonarContentDTO sonarContentDTO, List<Facet> facets) {
        sonarContentDTO.setRate("A");
        facets.stream().filter(facet -> facet.getProperty().equals(SEVERITIES)).forEach(facet -> {
            facet.getValues().stream().forEach(value -> {
                if (value.getVal().equals(Rate.MINOR.getRate()) && value.getCount() >= 1) {
                    if (sonarContentDTO.getRate().equals("A")) {
                        sonarContentDTO.setRate("B");
                    }
                }
                if (value.getVal().equals(Rate.MAJOR.getRate()) && value.getCount() >= 1) {
                    if (!sonarContentDTO.getRate().equals("D") && !sonarContentDTO.getRate().equals("E")) {
                        sonarContentDTO.setRate("C");
                    }
                }
                if (value.getVal().equals(Rate.CRITICAL.getRate()) && value.getCount() >= 1) {
                    if (!sonarContentDTO.getRate().equals("E")) {
                        sonarContentDTO.setRate("D");
                    }
                }
                if (value.getVal().equals(Rate.BLOCKER.getRate()) && value.getCount() >= 1) {
                    sonarContentDTO.setRate("E");
                }
            });
        });
    }


    public Map<String, String> getQueryMap(String key, String type, Boolean newAdd) {
        Map<String, String> map = new HashMap<>();
        map.put("componentKeys", key);
        map.put("s", "FILE_LINE");
        map.put("resolved", "false");
        map.put("types", type);
        if (newAdd) {
            map.put("sinceLeakPeriod", "true");
        }
        map.put("ps", "100");
        map.put("facets", "severities,types");
        map.put("additionalFields", "_all");
        return map;
    }


    private SonarClient getSonarClient() {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(sonarqubeUrl);
        configurationProperties.setType(SONAR);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        return retrofit.create(SonarClient.class);
    }
}
