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

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.ApplicationValidator;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.gitlab.MemberVO;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.api.vo.AppUserPermissionRepDTO;
import io.choerodon.devops.api.vo.ApplicationCodeDTO;
import io.choerodon.devops.api.vo.ApplicationImportDTO;
import io.choerodon.devops.api.vo.ApplicationRepVO;
import io.choerodon.devops.api.vo.ApplicationReqVO;
import io.choerodon.devops.api.vo.ApplicationTemplateRepVO;
import io.choerodon.devops.api.vo.ApplicationUpdateVO;
import io.choerodon.devops.api.vo.CommitVO;
import io.choerodon.devops.api.vo.ProjectConfigDTO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.api.vo.SonarContentDTO;
import io.choerodon.devops.api.vo.SonarContentsDTO;
import io.choerodon.devops.api.vo.SonarTableDTO;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.api.vo.iam.entity.AppUserPermissionE;
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
import io.choerodon.devops.api.vo.iam.entity.ApplicationTemplateE;
import io.choerodon.devops.api.vo.iam.entity.DevopsAppShareE;
import io.choerodon.devops.api.vo.iam.entity.DevopsBranchE;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
=======
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.gitlab.MemberVO;
import io.choerodon.devops.api.vo.iam.entity.*;
>>>>>>> [IMP]重构后端代码
import io.choerodon.devops.api.vo.iam.entity.gitlab.CommitE;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabUserE;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.sonar.Bug;
import io.choerodon.devops.api.vo.sonar.Component;
import io.choerodon.devops.api.vo.sonar.Facet;
import io.choerodon.devops.api.vo.sonar.Projects;
import io.choerodon.devops.api.vo.sonar.Quality;
import io.choerodon.devops.api.vo.sonar.SonarAnalyses;
import io.choerodon.devops.api.vo.sonar.SonarComponent;
import io.choerodon.devops.api.vo.sonar.SonarHistroy;
import io.choerodon.devops.api.vo.sonar.SonarTables;
import io.choerodon.devops.api.vo.sonar.Vulnerability;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppImportPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppSyncPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsUserPayload;
import io.choerodon.devops.app.eventhandler.payload.IamAppPayLoad;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.app.service.GitLabService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.app.service.IamService;
import io.choerodon.devops.app.service.ProjectService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.domain.application.repository.AppShareRepository;
import io.choerodon.devops.domain.application.repository.AppUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.ApplicationTemplateRepository;
import io.choerodon.devops.domain.application.repository.DevopsGitRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.choerodon.devops.domain.application.repository.GitlabUserRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
=======
import io.choerodon.devops.api.vo.sonar.*;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.repository.*;
>>>>>>> [IMP]重构后端代码
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
<<<<<<< HEAD
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;
import io.choerodon.devops.infra.dataobject.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.ApplicationDO;
=======
import io.choerodon.devops.infra.dataobject.AppUserPermissionDTO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.ApplicationDTO;
>>>>>>> [IMP]重构后端代码
import io.choerodon.devops.infra.dto.gitlab.BranchDO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDO;
import io.choerodon.devops.infra.dto.harbor.ProjectDetail;
import io.choerodon.devops.infra.dto.harbor.User;
<<<<<<< HEAD
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.devops.infra.dataobject.AppUserPermissionDTO;
import io.choerodon.devops.infra.dataobject.UserAttrDTO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.GitPlatformType;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import io.choerodon.devops.infra.enums.Rate;
import io.choerodon.devops.infra.enums.SonarQubeType;
=======
import io.choerodon.devops.infra.dto.iam.OrganizationDO;
import io.choerodon.devops.infra.dto.iam.ProjectDO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
>>>>>>> [IMP]重构后端代码
import io.choerodon.devops.infra.mapper.AppUserPermissionMapper;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.websocket.tool.UUIDTool;
<<<<<<< HEAD
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
=======
import io.kubernetes.client.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
>>>>>>> [IMP]重构后端代码
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
<<<<<<< HEAD
=======

>>>>>>> [IMP]重构后端代码

/**
 * Created by younger on 2018/3/28.
 */
@Service
@EnableConfigurationProperties(HarborConfigurationProperties.class)
public class ApplicationServiceImpl implements ApplicationService {
    public static final String SEVERITIES = "severities";
    public static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final Pattern REPOSITORY_URL_PATTERN = Pattern.compile("^http.*\\.git");
    private static final String GITLAB_CI_FILE = ".gitlab-ci.yml";
    private static final String DOCKER_FILE_NAME = "Dockerfile";
    private static final String ISSUE = "issue";
    private static final String COVERAGE = "coverage";
    private static final String CHART_DIR = "charts";
    private static final String SONAR = "sonar";
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
    private static final String MASTER = "master";
    private static final String APPLICATION = "application";
    private static final String ERROR_UPDATE_APP = "error.application.update";
    private static final String TEST = "test-application";
    private static final String DUPLICATE = "duplicate";
    public static final String NODELETED = "nodeleted";

    private Gson gson = new Gson();
    private JSON json = new JSON();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${services.sonarqube.url:}")
    private String sonarqubeUrl;
    @Value("${services.gateway.url}")
    private String gatewayUrl;
    @Value("${services.sonarqube.username:}")
    private String userName;
    @Value("${services.sonarqube.password:}")
    private String password;


    @Autowired
    private ApplicationRepository applicationRepository;
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
    private AppShareRepository applicationMarketRepository;
    @Autowired
    private AppUserPermissionRepository appUserPermissionRepository;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private DevopsProjectConfigRepository devopsProjectConfigRepository;
    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private UserAttrMapper userAttrMapper;
    @Autowired
    private AppUserPermissionMapper appUserPermissionMapper;

    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private IamService iamService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private GitLabService gitLabService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;

    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;

    @Override
    @Saga(code = "devops-create-application",
            description = "Devops创建应用", inputSchema = "{}")
    @Transactional

    public ApplicationRepVO create(Long projectId, ApplicationReqVO applicationReqVO) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplication(applicationReqVO);
        ProjectVO projectVO = iamService.queryIamProject(projectId);
        OrganizationVO organizationVO = iamService.queryOrganizationById(projectVO.getOrganization().getId());
        // 查询创建应用所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
        MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(
                TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrVO.getGitlabUserId()));
        if (memberDTO == null || memberDTO.getAccessLevel() != AccessLevel.OWNER) {
            throw new CommonException("error.user.not.owner");
        }

        ApplicationDTO applicationDTO = getApplicationDTO(projectId, applicationReqVO);
        applicationDTO = baseCreate(applicationDTO);

        Long appId = applicationDTO.getId();
        if (appId == null) {
            throw new CommonException("error.application.create.insert");
        }
        // 如果不跳过权限检查
        List<Long> userIds = applicationReqVO.getUserIds();
        if (!applicationReqVO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> appUserPermissionRepository.baseCreate(e, appId));
        }

        IamAppPayLoad iamAppPayLoad = new IamAppPayLoad();
        iamAppPayLoad.setApplicationCategory(APPLICATION);
        iamAppPayLoad.setApplicationType(applicationReqVO.getType());
        iamAppPayLoad.setCode(applicationReqVO.getCode());
        iamAppPayLoad.setName(applicationReqVO.getName());
        iamAppPayLoad.setEnabled(true);
        iamAppPayLoad.setOrganizationId(organizationVO.getId());
        iamAppPayLoad.setProjectId(projectId);
        iamAppPayLoad.setFrom(applicationName);

<<<<<<< HEAD
        iamService.createIamApp(organization.getId(), iamAppPayLoad);
=======
        iamService.createIamApp(organizationVO.getId(), iamAppPayLoad);
>>>>>>> [IMP] 修改AppControler重构
        return ConvertHelper.convert(queryByCode(applicationDTO.getCode(), applicationDTO.getProjectId()), ApplicationRepVO.class);
    }


    @Override
    public ApplicationRepVO query(Long projectId, Long applicationId) {
        ProjectVO projectVO = iamService.queryIamProject(projectId);
        OrganizationVO organizationVO = iamService.queryOrganizationById(projectVO.getOrganization().getId());
        ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(applicationId);
        ApplicationRepVO applicationRepVO = ConvertHelper.convert(applicationDTO, ApplicationRepVO.class);
        //url地址拼接
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        if (applicationDTO.getGitlabProjectId() != null) {
            applicationRepVO.setRepoUrl(gitlabUrl + urlSlash
                    + organizationVO.getCode() + "-" + projectVO.getCode() + "/"
                    + applicationDTO.getCode() + ".git");
        }
        if (applicationDTO.getIsSkipCheckPermission()) {
            applicationRepVO.setPermission(true);
        } else {
            applicationRepVO.setPermission(false);
        }
        return applicationRepVO;
    }

    @Override
    @Saga(code = "devops-app-delete", description = "Devops删除失败应用", inputSchema = "{}")
    public void delete(Long projectId, Long appId) {
<<<<<<< HEAD
        ProjectVO projectE = iamService.queryIamProject(projectId);
=======
        ProjectVO projectVO = iamService.queryIamProject(projectId);
>>>>>>> [IMP] 修改AppControler重构
        //删除应用权限

        appUserPermissionRepository.baseDeleteByAppId(appId);
        //删除gitlab project
        ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(appId);
        if (applicationDTO.getGitlabProjectId() != null) {
            Integer gitlabProjectId = applicationDTO.getGitlabProjectId();
            GitlabProjectDTO gitlabProjectDO = gitLabService.getProjectById(gitlabProjectId);
            if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                Integer gitlabUserId = TypeUtil.objToInt(userAttrE.getGitlabUserId());
                gitLabService.deleteProject(gitlabProjectId, gitlabUserId);
            }
        }
        //删除iam应用
        DevOpsAppSyncPayload appSyncPayload = new DevOpsAppSyncPayload();
        appSyncPayload.setProjectId(projectId);
        appSyncPayload.setOrganizationId(projectVO.getOrganization().getId());
        appSyncPayload.setCode(applicationDTO.getCode());
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode("devops-app-delete"),
                builder -> builder
                        .withPayloadAndSerialize(appSyncPayload)
                        .withRefId(String.valueOf(appId))
                        .withSourceId(projectId));
        applicationMapper.deleteByPrimaryKey(appId);
    }

    @Saga(code = "devops-update-gitlab-users",
            description = "Devops更新gitlab用户", inputSchema = "{}")
    @Override
    public Boolean update(Long projectId, ApplicationUpdateVO applicationUpdateVO) {

        ApplicationDTO applicationDTO = ConvertHelper.convert(applicationUpdateVO, ApplicationDTO.class);
        applicationDTO.setIsSkipCheckPermission(applicationUpdateVO.getIsSkipCheckPermission());
        applicationDTO.setProjectId(projectId);
        applicationDTO.setHarborConfigId(applicationUpdateVO.getHarborConfigId());
        applicationDTO.setChartConfigId(applicationUpdateVO.getChartConfigId());

        Long appId = applicationUpdateVO.getId();
        ApplicationE oldApplicationE = applicationRepository.query(appId);

        if (!oldApplicationE.getName().equals(applicationUpdateVO.getName())) {
            checkNameFromBase(applicationDTO.getProjectId(), applicationDTO.getName());
        }
        if (updateFromBase(applicationDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_APP);
        }

<<<<<<< HEAD
            if (!oldApplicationE.getName().equals(applicationUpdateVO.getName())) {
                ProjectVO projectE = iamService.queryIamProject(oldApplicationE.getProjectE().getId());
                OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
                IamAppPayLoad iamAppPayLoad = iamService.queryIamAppByCode(organization.getId(), applicationE.getCode());
                iamAppPayLoad.setName(applicationUpdateVO.getName());
                iamService.updateIamApp(organization.getId(), iamAppPayLoad.getId(), iamAppPayLoad);
            }

            // 创建gitlabUserPayload
            DevOpsUserPayload devOpsUserPayload = new DevOpsUserPayload();
            devOpsUserPayload.setIamProjectId(projectId);
            devOpsUserPayload.setAppId(appId);
            devOpsUserPayload.setGitlabProjectId(oldApplicationE.getGitlabProjectE().getId());
            devOpsUserPayload.setIamUserIds(applicationUpdateVO.getUserIds());

            if (oldApplicationE.getIsSkipCheckPermission() && applicationUpdateVO.getIsSkipCheckPermission()) {
                return false;
            } else if (oldApplicationE.getIsSkipCheckPermission() && !applicationUpdateVO.getIsSkipCheckPermission()) {
                applicationUpdateVO.getUserIds().forEach(e -> appUserPermissionRepository.create(e, appId));
                devOpsUserPayload.setOption(1);
            } else if (!oldApplicationE.getIsSkipCheckPermission() && applicationUpdateVO.getIsSkipCheckPermission()) {
                appUserPermissionRepository.deleteByAppId(appId);
                devOpsUserPayload.setOption(2);
            } else {
                appUserPermissionRepository.deleteByAppId(appId);
                applicationUpdateVO.getUserIds().forEach(e -> appUserPermissionRepository.create(e, appId));
                devOpsUserPayload.setOption(3);
            }
            producer.applyAndReturn(
                    StartSagaBuilder
                            .newBuilder()
                            .withLevel(ResourceLevel.PROJECT)
                            .withRefType("app")
                            .withSagaCode("evops-update-gitlab-users"),
                    builder -> builder
                            .withPayloadAndSerialize(devOpsUserPayload)
                            .withRefId(String.valueOf(appId))
                            .withSourceId(projectId));
            return true;
        }

        @Saga(code = "devops-update-iam-app",
                description = "Devops同步更新iam应用", inputSchema = "{}")
        private void updateIamApp (Long projectId, ApplicationE applicationE, String code){
            ProjectE projectE = iamRepository.queryIamProject(projectId);
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            DevOpsAppSyncPayload devOpsAppSyncPayload = new DevOpsAppSyncPayload();
            devOpsAppSyncPayload.setName(applicationE.getName());
            devOpsAppSyncPayload.setCode(code);
            devOpsAppSyncPayload.setProjectId(projectId);
            devOpsAppSyncPayload.setOrganizationId(organization.getId());
            devOpsAppSyncPayload.setAppId(applicationE.getId());
            String input = gson.toJson(devOpsAppSyncPayload);
            sagaClient.startSaga("devops-update-iam-app", new StartInstanceDTO(input, "app", applicationE.getId().toString(), ResourceLevel.PROJECT.value(), projectId));
=======
        if (!oldApplicationE.getName().equals(applicationUpdateVO.getName())) {
            ProjectVO projectVO = iamService.queryIamProject(oldApplicationE.getProjectE().getId());
            OrganizationVO organizationVO = iamService.queryOrganizationById(projectVO.getOrganization().getId());
            IamAppPayLoad iamAppPayLoad = iamService.queryIamAppByCode(organizationVO.getId(), applicationDTO.getCode());
            iamAppPayLoad.setName(applicationUpdateVO.getName());
            iamService.updateIamApp(organizationVO.getId(), iamAppPayLoad.getId(), iamAppPayLoad);
        }

        // 创建gitlabUserPayload
        DevOpsUserPayload devOpsUserPayload = new DevOpsUserPayload();
        devOpsUserPayload.setIamProjectId(projectId);
        devOpsUserPayload.setAppId(appId);
        devOpsUserPayload.setGitlabProjectId(oldApplicationE.getGitlabProjectE().getId());
        devOpsUserPayload.setIamUserIds(applicationUpdateVO.getUserIds());

        if (oldApplicationE.getIsSkipCheckPermission() && applicationUpdateVO.getIsSkipCheckPermission()) {
            return false;
        } else if (oldApplicationE.getIsSkipCheckPermission() && !applicationUpdateVO.getIsSkipCheckPermission()) {
            applicationUpdateVO.getUserIds().forEach(e -> appUserPermissionMapper.insert(new AppUserPermissionDTO(e, appId)));
            devOpsUserPayload.setOption(1);
        } else if (!oldApplicationE.getIsSkipCheckPermission() && applicationUpdateVO.getIsSkipCheckPermission()) {
            AppUserPermissionDTO appUserPermissionDO = new AppUserPermissionDTO();
            appUserPermissionDO.setAppId(appId);
            appUserPermissionMapper.delete(appUserPermissionDO);
            devOpsUserPayload.setOption(2);
        } else {
            AppUserPermissionDTO appUserPermissionDO = new AppUserPermissionDTO();
            appUserPermissionDO.setAppId(appId);
            appUserPermissionMapper.delete(appUserPermissionDO);
            applicationUpdateVO.getUserIds().forEach(e -> appUserPermissionMapper.insert(new AppUserPermissionDTO(e, appId)));

            devOpsUserPayload.setOption(3);
>>>>>>> [IMP] 修改AppControler重构
<<<<<<< HEAD
        @Saga(code = "devops-sync-app-active",
                description = "同步iam应用状态", inputSchema = "{}")
        @Override
        public Boolean updateActive (Long appId, Boolean active){
            ApplicationE applicationE = applicationRepository.query(appId);
            applicationE.initActive(active);
            if (applicationRepository.update(applicationE) != 1) {
                throw new CommonException("error.application.active");
            }
            ProjectVO projectE = iamService.queryIamProject(applicationE.getProjectE().getId());
            DevOpsAppSyncPayload opsAppSyncPayload = new DevOpsAppSyncPayload();
            opsAppSyncPayload.setActive(active);
            opsAppSyncPayload.setOrganizationId(projectE.getOrganization().getId());
            opsAppSyncPayload.setProjectId(applicationE.getProjectE().getId());
            opsAppSyncPayload.setCode(applicationE.getCode());
            producer.applyAndReturn(
                    StartSagaBuilder
                            .newBuilder()
                            .withLevel(ResourceLevel.PROJECT)
                            .withRefType("app")
                            .withSagaCode("devops-sync-app-active"),
                    builder -> builder
                            .withPayloadAndSerialize(opsAppSyncPayload)
                            .withRefId(String.valueOf(appId))
                            .withSourceId(applicationE.getProjectE().getId());
            return true;
        }

        @Override
        public PageInfo<ApplicationRepVO> pageByOptions (Long projectId, Boolean isActive, Boolean hasVersion,
                Boolean appMarket,
                String type, Boolean doPage,
                PageRequest pageRequest, String params){
            PageInfo<ApplicationE> applicationES =
                    applicationRepository.listByOptions(projectId, isActive, hasVersion, appMarket, type, doPage, pageRequest, params);
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            ProjectVO projectE = iamService.queryIamProject(projectId);
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

            initApplicationParams(projectE, organization, applicationES.getList(), urlSlash);

            PageInfo<ApplicationRepVO> resultDTOPage = ConvertPageHelper.convertPageInfo(applicationES, ApplicationRepVO.class);
            resultDTOPage.setList(setApplicationRepVOPermission(applicationES.getList(), userAttrE, projectE));
            return resultDTOPage;
        }


        @Override
        public PageInfo<ApplicationRepVO> pageCodeRepository (Long projectId, PageRequest pageRequest, String params){

            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            ProjectVO projectE = iamService.queryIamProject(projectId);
            Boolean isProjectOwner = iamService.isProjectOwner(userAttrE.getIamUserId(), projectE);
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());

            PageInfo<ApplicationE> applicationES = applicationRepository
                    .listCodeRepository(projectId, pageRequest, params, isProjectOwner, userAttrE.getIamUserId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
=======
    @Saga(code = "devops-sync-app-active",
            description = "同步iam应用状态", inputSchema = "{}")
    @Override
    public Boolean updateActive(Long appId, Boolean active) {
        ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(appId);
        applicationDTO.setActive(active);
        if (updateFromBase(applicationDTO) != 1) {
            throw new CommonException("error.application.active");
        }
        ProjectVO projectVO = iamService.queryIamProject(applicationDTO.getId());
        DevOpsAppSyncPayload opsAppSyncPayload = new DevOpsAppSyncPayload();
        opsAppSyncPayload.setActive(active);
        opsAppSyncPayload.setOrganizationId(projectVO.getOrganization().getId());
        opsAppSyncPayload.setProjectId(applicationDTO.getId());
        opsAppSyncPayload.setCode(applicationDTO.getCode());
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode("devops-sync-app-active"),
                builder -> builder
                        .withPayloadAndSerialize(opsAppSyncPayload)
                        .withRefId(String.valueOf(appId))
                        .withSourceId(applicationDTO.getId()));
        return true;
    }

    @Override
    public PageInfo<ApplicationRepVO> pageByOptions(Long projectId, Boolean isActive, Boolean hasVersion,
                                                    Boolean appMarket,
                                                    String type, Boolean doPage,
                                                    PageRequest pageRequest, String params) {
        PageInfo<ApplicationDTO> applicationDTOS = listByOptionsFromBase(projectId, isActive, hasVersion, appMarket, type, doPage, pageRequest, params);
        UserAttrDTO userAttrDTO = userAttrMapper.selectByPrimaryKey(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectVO projectVO = iamService.queryIamProject(projectId);
        OrganizationVO organization = iamService.queryOrganizationById(projectVO.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectVO, organization, applicationDTOS.getList(), urlSlash);

        PageInfo<ApplicationRepVO> resultDTOPage = ConvertPageHelper.convertPageInfo(applicationDTOS, ApplicationRepVO.class);
        resultDTOPage.setList(setApplicationRepVOPermission(applicationDTOS.getList(), userAttrDTO, projectVO));
        return resultDTOPage;
    }

    PageInfo<ApplicationDTO> listByOptionsFromBase(Long projectId, Boolean isActive, Boolean hasVersion,
                                                   Boolean appMarket,
                                                   String type, Boolean doPage,
                                                   PageRequest pageRequest, String params) {
        PageInfo<ApplicationDTO> applicationES = new PageInfo<>();

        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        //是否需要分页
        if (doPage != null && !doPage) {
            applicationES.setList(applicationMapper.list(projectId, isActive, hasVersion, appMarket, type,
                    (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                    mapParams.get(TypeUtil.PARAM).toString(), PageRequestUtil.checkSortIsEmpty(pageRequest)));
        } else {
            applicationES = PageHelper
                    .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.list(projectId, isActive, hasVersion, appMarket, type,
                            (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                            (String) mapParams.get(TypeUtil.PARAM), PageRequestUtil.checkSortIsEmpty(pageRequest)));
        }
        return applicationES;
    }

    @Override
    public PageInfo<ApplicationRepVO> pageCodeRepository(Long projectId, PageRequest pageRequest, String params) {

        UserAttrDTO userAttrDTO = userAttrMapper.selectByPrimaryKey(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectVO projectVO = iamService.queryIamProject(projectId);
        Boolean isProjectOwner = iamService.isProjectOwner(userAttrDTO.getIamUserId(), projectVO);
        OrganizationVO organizationVO = iamService.queryOrganizationById(projectVO.getOrganization().getId());

        Map maps = gson.fromJson(params, Map.class);
        PageInfo<ApplicationDTO> applicationES = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.listCodeRepository(projectId,
                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(maps.get(TypeUtil.PARAM)), isProjectOwner, userAttrDTO.getIamUserId()));
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectVO, organizationVO, applicationES.getList(), urlSlash);
>>>>>>> [IMP] 修改AppControler重构

            initApplicationParams(projectE, organization, applicationES.getList(), urlSlash);

<<<<<<< HEAD
            return ConvertPageHelper.convertPageInfo(applicationES, ApplicationRepVO.class);
        }

        private void initApplicationParams (ProjectVO projectE, OrganizationVO
        organization, List < ApplicationE > applicationES, String urlSlash){
            List<String> projectKeys = new ArrayList<>();
            if (!sonarqubeUrl.equals("")) {
                SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, "sonar", userName, password);
                try {
                    Response<Projects> projectsResponse = sonarClient.listProject().execute();
                    if (projectsResponse != null && projectsResponse.raw().code() == 200) {
                        projectKeys = projectsResponse.body().getComponents().stream().map(Component::getKey).collect(Collectors.toList());
                    }
                } catch (IOException e) {
                    LOGGER.info(e.getMessage(), e);
=======
    private void initApplicationParams(ProjectVO projectE, OrganizationVO organization, List<ApplicationDTO> applicationDTOS, String urlSlash) {
        List<String> projectKeys = new ArrayList<>();
        if (!sonarqubeUrl.equals("")) {
            SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, "sonar", userName, password);
            try {
                Response<Projects> projectsResponse = sonarClient.listProject().execute();
                if (projectsResponse != null && projectsResponse.raw().code() == 200) {
                    projectKeys = projectsResponse.body().getComponents().stream().map(Component::getKey).collect(Collectors.toList());
>>>>>>> [IMP] 修改AppControler重构
                }
            }

<<<<<<< HEAD
            for (ApplicationE t : applicationES) {
                if (t.getGitlabProjectE() != null && t.getGitlabProjectE().getId() != null) {
                    t.initGitlabProjectEByUrl(
                            gitlabUrl + urlSlash + organization.getCode() + "-" + projectE.getCode() + "/" +
                                    t.getCode() + ".git");
                    String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), t.getCode());
                    if (!projectKeys.isEmpty() && projectKeys.contains(key)) {
                        t.initSonarUrl(sonarqubeUrl);
                    }
                }
            }
        }

        @Override
        public List<ApplicationRepVO> listByActive (Long projectId){
            List<ApplicationE> applicationEList = applicationRepository.listByActive(projectId);
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            ProjectVO projectE = iamService.queryIamProject(projectId);
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

            initApplicationParams(projectE, organization, applicationEList, urlSlash);

            return setApplicationRepVOPermission(applicationEList, userAttrE, projectE);
        }
=======
        for (ApplicationDTO t : applicationDTOS) {
            if (t.getGitlabProjectId() != null) {
                t.setRepoUrl(
                        gitlabUrl + urlSlash + organization.getCode() + "-" + projectE.getCode() + "/" +
                                t.getCode() + ".git");
                String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), t.getCode());
                if (!projectKeys.isEmpty() && projectKeys.contains(key)) {
                    t.setSonarUrl(sonarqubeUrl);
                }
            }
        }
    }

    @Override
    public List<ApplicationRepVO> listByActive(Long projectId) {
        List<ApplicationDTO> applicationDTOList = applicationMapper.listActive(projectId);
        UserAttrDTO userAttrDTO = userAttrMapper.selectByPrimaryKey(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectVO projectVO = iamService.queryIamProject(projectId);
        OrganizationVO organizationVO = iamService.queryOrganizationById(projectVO.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectVO, organizationVO, applicationDTOList, urlSlash);

        return setApplicationRepVOPermission(applicationDTOList, userAttrDTO, projectVO);
    }

    private List<ApplicationRepVO> setApplicationRepVOPermission(List<ApplicationDTO> applicationDTOList,
                                                                 UserAttrDTO userAttrDTO, ProjectVO projectE) {
        List<ApplicationRepVO> resultDTOList = ConvertHelper.convertList(applicationDTOList, ApplicationRepVO.class);
        if (userAttrDTO == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }
        if (!iamService.isProjectOwner(userAttrDTO.getIamUserId(), projectE)) {
            AppUserPermissionDTO appUserPermissionDO = new AppUserPermissionDTO();
            appUserPermissionDO.setIamUserId(userAttrDTO.getIamUserId());
            List<Long> appIds = appUserPermissionMapper.select(appUserPermissionDO).stream()
                    .map(AppUserPermissionDTO::getAppId).collect(Collectors.toList());

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
<<<<<<< HEAD
        return resultDTOList;
    }
>>>>>>> [IMP] 修改AppControler重构

        private List<ApplicationRepVO> setApplicationRepVOPermission (List < ApplicationE > applicationEList,
                UserAttrE userAttrE, ProjectVO projectE){
            List<ApplicationRepVO> resultDTOList = ConvertHelper.convertList(applicationEList, ApplicationRepVO.class);
            if (userAttrE == null) {
                throw new CommonException("error.gitlab.user.sync.failed");
            }
            if (!iamService.isProjectOwner(userAttrE.getIamUserId(), projectE)) {
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
        public List<ApplicationRepVO> listAll (Long projectId){
            return ConvertHelper.convertList(applicationRepository.listAll(projectId), ApplicationRepVO.class);
        }

        @Override
        public void checkName (Long projectId, String name){
            applicationRepository.checkName(projectId, name);
        }

<<<<<<< HEAD
        @Override
        public void checkCode (Long projectId, String code){
            ApplicationE applicationE = ApplicationFactory.createApplicationE();
            applicationE.initProjectE(projectId);
            applicationE.setCode(code);
            applicationRepository.checkCode(applicationE);
=======
    @Override
    public List<ApplicationTemplateRepVO> listTemplate(Long projectId, Boolean isPredefined) {
        ProjectVO projectE = iamService.queryIamProject(projectId);
        List<ApplicationTemplateE> applicationTemplateES = applicationTemplateRepository.list(projectE.getOrganization().getId())
                .stream()
                .filter(ApplicationTemplateE::getSynchro).collect(Collectors.toList());
        if (isPredefined != null && isPredefined) {
            applicationTemplateES = applicationTemplateES.stream().filter(applicationTemplateE -> applicationTemplateE.getOrganization().getId() == null).collect(Collectors.toList());
>>>>>>> [IMP] 修改AppControler重构
        }

        @Override
        public List<ApplicationTemplateRepVO> listTemplate (Long projectId, Boolean isPredefined){
            ProjectVO projectE = iamService.queryIamProject(projectId);
            List<ApplicationTemplateE> applicationTemplateES = applicationTemplateRepository.list(projectE.getOrganization().getId())
                    .stream()
                    .filter(ApplicationTemplateE::getSynchro).collect(Collectors.toList());
            if (isPredefined != null && isPredefined) {
                applicationTemplateES = applicationTemplateES.stream().filter(applicationTemplateE -> applicationTemplateE.getOrganization().getId() == null).collect(Collectors.toList());
            }
            return ConvertHelper.convertList(applicationTemplateES, ApplicationTemplateRepVO.class);
        }

<<<<<<< HEAD
=======

        @Override
        public List<ApplicationRepVO> listAll (Long projectId){
            return ConvertHelper.convertList(applicationRepository.listAll(projectId), ApplicationRepVO.class);
        }

        @Override
        public void checkName (Long projectId, String name){
            applicationRepository.checkName(projectId, name);
        }

        @Override
        public void checkCode (Long projectId, String code){
            ApplicationE applicationE = new ApplicationE();
            applicationE.initProjectE(projectId);
            applicationE.setCode(code);
            applicationRepository.checkCode(applicationE);
        }

        @Override
        public List<ApplicationTemplateRepVO> listTemplate (Long projectId, Boolean isPredefined){
            ProjectVO projectE = iamService.queryIamProject(projectId);
            List<ApplicationTemplateE> applicationTemplateES = applicationTemplateRepository.baseListByOrganizationId(projectE.getOrganization().getId())
                    .stream()
                    .filter(ApplicationTemplateE::getSynchro).collect(Collectors.toList());
            if (isPredefined != null && isPredefined) {
                applicationTemplateES = applicationTemplateES.stream().filter(applicationTemplateE -> applicationTemplateE.getOrganization().getId() == null).collect(Collectors.toList());
            }
            return ConvertHelper.convertList(applicationTemplateES, ApplicationTemplateRepVO.class);
        }

>>>>>>> [IMP]重构后端代码
        /**
         * analyze location of the dockerfile in the template
         *
         * @param templateWorkDir       template work dir
         * @param applicationTemplateId application template id
         */
        private void analyzeDockerfileToMap (File templateWorkDir, Long applicationTemplateId){
            Collection<File> dockerfile = FileUtils.listFiles(templateWorkDir, filenameFilter, TrueFileFilter.INSTANCE);
            Optional<File> df = dockerfile.stream().findFirst();
            templateDockerfileMap.putIfAbsent(applicationTemplateId, df.map(f -> f.getAbsolutePath().replace(templateWorkDir.getAbsolutePath() + System.getProperty("file.separator"), "")).orElse(DOCKER_FILE_NAME));
        }

        @Override
        public void operationApplication (DevOpsAppPayload gitlabProjectPayload){
<<<<<<< HEAD
            DevopsProjectE devopsProjectE = devopsProjectRepository.queryByGitlabGroupId(
=======
            DevopsProjectVO devopsProjectE = devopsProjectRepository.queryByGitlabGroupId(
>>>>>>> [IMP]重构后端代码
                    TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
            ApplicationE applicationE = applicationRepository.queryByCode(gitlabProjectPayload.getPath(),
                    devopsProjectE.getProjectE().getId());
            ProjectVO projectE = iamService.queryIamProject(devopsProjectE.getProjectE().getId());
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
<<<<<<< HEAD
            GitlabProjectDO gitlabProjectDO = gitlabRepository
=======
            GitlabProjectDTO gitlabProjectDO = gitLabService
>>>>>>> [IMP]重构后端代码
                    .getProjectByName(organization.getCode() + "-" + projectE.getCode(), applicationE.getCode(),
                            gitlabProjectPayload.getUserId());
            Integer gitlabProjectId = gitlabProjectDO.getId();
            if (gitlabProjectId == null) {
<<<<<<< HEAD
                gitlabProjectDO = gitlabRepository.createProject(gitlabProjectPayload.getGroupId(),
=======
                gitlabProjectDO = gitLabService.createProject(gitlabProjectPayload.getGroupId(),
>>>>>>> [IMP]重构后端代码
                        gitlabProjectPayload.getPath(),
                        gitlabProjectPayload.getUserId(), false);
            }
            gitlabProjectPayload.setGitlabProjectId(gitlabProjectDO.getId());

            // 为项目下的成员分配对于此gitlab项目的权限
            operateGitlabMemberPermission(gitlabProjectPayload);

            if (applicationE.getApplicationTemplateE() != null) {
<<<<<<< HEAD
                ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
=======
                ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.baseQuery(
>>>>>>> [IMP]重构后端代码
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
<<<<<<< HEAD
                            gitlabRepository.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
=======
                            gitLabService.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
>>>>>>> [IMP]重构后端代码
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
<<<<<<< HEAD
                        gitlabRepository.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
=======
    @Override
    public void operationApplication(DevOpsAppPayload gitlabProjectPayload) {
        DevopsProjectVO devopsProjectE = devopsProjectRepository.queryByGitlabGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        ApplicationE applicationE = applicationRepository.queryByCode(gitlabProjectPayload.getPath(),
                devopsProjectE.getProjectE().getId());
        ProjectVO projectE = iamService.queryIamProject(devopsProjectE.getProjectE().getId());
        OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
        GitlabProjectDTO gitlabProjectDO = gitLabService
                .getProjectByName(organization.getCode() + "-" + projectE.getCode(), applicationE.getCode(),
                        gitlabProjectPayload.getUserId());
        Integer gitlabProjectId = gitlabProjectDO.getId();
        if (gitlabProjectId == null) {
            gitlabProjectDO = gitLabService.createProject(gitlabProjectPayload.getGroupId(),
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
=======
>>>>>>> [IMP]重构后端代码
                        gitLabService.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
>>>>>>> [IMP] 修改AppControler重构
                                AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                                gitlabProjectPayload.getUserId());
                    }
                }
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> [IMP]重构后端代码
                initBranch(gitlabProjectPayload, applicationE, MASTER);
            }
            try {
                String applicationToken = getApplicationToken(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId());
                applicationE.setToken(applicationToken);
                applicationE.initGitlabProjectE(TypeUtil.objToInteger(gitlabProjectPayload.getGitlabProjectId()));
                applicationE.initSynchro(true);
                applicationE.setFailed(false);
                // set project hook id for application
                setProjectHook(applicationE, gitlabProjectDO.getId(), applicationToken, gitlabProjectPayload.getUserId());
                // 更新并校验
                applicationRepository.updateSql(applicationE);
            } catch (Exception e) {
                throw new CommonException(e.getMessage(), e);
<<<<<<< HEAD
            }
        }

        /**
         * get application token (set a token if there is not one in gitlab)
         *
         * @param projectId gitlab project id
         * @param userId    gitlab user id
         * @return the application token that is stored in gitlab variables
         */
        private String getApplicationToken (Integer projectId, Integer userId){
            List<Variable> variables = gitlabRepository.getVariable(projectId, userId);
            if (variables.isEmpty()) {
                String token = GenerateUUID.generateUUID();
                gitlabRepository.addVariable(projectId, "Token", token, false, userId);
                return token;
            } else {
                return variables.get(0).getValue();
            }
=======
            } else {
                if (!branchDO.getProtected()) {
                    gitLabService.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
                            AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                            gitlabProjectPayload.getUserId());
                }
=======
>>>>>>> [IMP]重构后端代码
            }
        }

        /**
         * get application token (set a token if there is not one in gitlab)
         *
         * @param projectId gitlab project id
         * @param userId    gitlab user id
         * @return the application token that is stored in gitlab variables
         */
        private String getApplicationToken (Integer projectId, Integer userId){
            List<Variable> variables = gitLabService.getVariable(projectId, userId);
            if (variables.isEmpty()) {
                String token = GenerateUUID.generateUUID();
                gitLabService.addVariable(projectId, "Token", token, false, userId);
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
        private void operateGitlabMemberPermission (DevOpsAppPayload devOpsAppPayload){
            // 不跳过权限检查，则为gitlab项目分配项目成员权限
            if (!devOpsAppPayload.getSkipCheckPermission()) {
                if (!devOpsAppPayload.getUserIds().isEmpty()) {
                    List<Long> gitlabUserIds = userAttrRepository.listByUserIds(devOpsAppPayload.getUserIds()).stream()
                            .map(UserAttrE::getGitlabUserId).collect(Collectors.toList());
                    gitlabUserIds.forEach(e -> {
                        GitlabMemberE gitlabGroupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(e));
                        if (gitlabGroupMemberE != null) {
                            gitlabGroupMemberRepository.deleteMember(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(e));
                        }
                        GitlabMemberE gitlabProjectMemberE = gitlabProjectRepository.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(e));
                        if (gitlabProjectMemberE == null || gitlabProjectMemberE.getId() == null) {
                            gitLabService.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                                    new MemberVO(TypeUtil.objToInteger(e), 30, ""));
                        }
                    });
                }
            }
            // 跳过权限检查，项目下所有成员自动分配权限
            else {
                List<Long> iamUserIds = iamService.getAllMemberIdsWithoutOwner(devOpsAppPayload.getIamProjectId());
                List<Integer> gitlabUserIds = userAttrRepository.listByUserIds(iamUserIds).stream()
                        .map(UserAttrE::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());

                gitlabUserIds.forEach(e ->
                        updateGitlabMemberPermission(devOpsAppPayload, e));
            }
        }

        private void updateGitlabMemberPermission (DevOpsAppPayload devOpsAppPayload, Integer gitlabUserId){
            GitlabMemberE gitlabGroupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
            if (gitlabGroupMemberE != null) {
                gitlabGroupMemberRepository.deleteMember(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
            }
            GitlabMemberE gitlabProjectMemberE = gitlabProjectRepository.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(gitlabUserId));
            if (gitlabProjectMemberE == null || gitlabProjectMemberE.getId() == null) {
                gitLabService.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                        new MemberVO(TypeUtil.objToInteger(gitlabUserId), 30, ""));
            }
        }

<<<<<<< HEAD
    private void updateGitlabMemberPermission(DevOpsAppPayload devOpsAppPayload, Integer gitlabUserId) {
        GitlabMemberE gitlabGroupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
        if (gitlabGroupMemberE != null) {
            gitlabGroupMemberRepository.deleteMember(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
        }
        GitlabMemberE gitlabProjectMemberE = gitlabProjectRepository.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(gitlabUserId));
        if (gitlabProjectMemberE == null || gitlabProjectMemberE.getId() == null) {
            gitLabService.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                    new MemberVO(TypeUtil.objToInteger(gitlabUserId), 30, ""));
>>>>>>> [IMP] 修改AppControler重构
        }

        /**
         * 处理当前项目成员对于此gitlab应用的权限
         *
         * @param devOpsAppPayload 此次操作相关信息
         */
        private void operateGitlabMemberPermission (DevOpsAppPayload devOpsAppPayload){
            // 不跳过权限检查，则为gitlab项目分配项目成员权限
            if (!devOpsAppPayload.getSkipCheckPermission()) {
                if (!devOpsAppPayload.getUserIds().isEmpty()) {
                    List<Long> gitlabUserIds = userAttrRepository.listByUserIds(devOpsAppPayload.getUserIds()).stream()
                            .map(UserAttrE::getGitlabUserId).collect(Collectors.toList());
                    gitlabUserIds.forEach(e -> {
                        GitlabMemberE gitlabGroupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(e));
                        if (gitlabGroupMemberE != null) {
                            gitlabGroupMemberRepository.deleteMember(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(e));
                        }
                        GitlabMemberE gitlabProjectMemberE = gitlabProjectRepository.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(e));
                        if (gitlabProjectMemberE == null || gitlabProjectMemberE.getId() == null) {
                            gitlabRepository.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                                    new MemberVO(TypeUtil.objToInteger(e), 30, ""));
                        }
                    });
                }
            }
            // 跳过权限检查，项目下所有成员自动分配权限
            else {
                List<Long> iamUserIds = iamService.getAllMemberIdsWithoutOwner(devOpsAppPayload.getIamProjectId());
                List<Integer> gitlabUserIds = userAttrRepository.listByUserIds(iamUserIds).stream()
                        .map(UserAttrE::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());

                gitlabUserIds.forEach(e ->
                        updateGitlabMemberPermission(devOpsAppPayload, e));
            }
        }

<<<<<<< HEAD
        private void updateGitlabMemberPermission (DevOpsAppPayload devOpsAppPayload, Integer gitlabUserId){
            GitlabMemberE gitlabGroupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
            if (gitlabGroupMemberE != null) {
                gitlabGroupMemberRepository.deleteMember(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
            }
            GitlabMemberE gitlabProjectMemberE = gitlabProjectRepository.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(gitlabUserId));
            if (gitlabProjectMemberE == null || gitlabProjectMemberE.getId() == null) {
                gitlabRepository.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                        new MemberVO(TypeUtil.objToInteger(gitlabUserId), 30, ""));
            }
=======
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
        List<ProjectHook> projectHooks = gitLabService.getHooks(projectId, userId);
        if (projectHooks.isEmpty()) {
            applicationE.initHookId(TypeUtil.objToLong(gitLabService.createWebHook(
                    projectId, userId, projectHook)
                    .getId()));
        } else {
            applicationE.initHookId(TypeUtil.objToLong(projectHooks.get(0).getId()));
>>>>>>> [IMP] 修改AppControler重构
        }

<<<<<<< HEAD
        /**
         * 拉取模板库到本地
         *
         * @param applicationTemplateE 模板库的信息
         * @param applicationDir       本地库地址
         * @return 本地库的git实例
         */
        private Git cloneTemplate (ApplicationTemplateE applicationTemplateE, String applicationDir){
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
        private void setProjectHook (ApplicationE applicationE, Integer projectId, String token, Integer userId){
            ProjectHook projectHook = ProjectHook.allHook();
            projectHook.setEnableSslVerification(true);
            projectHook.setProjectId(projectId);
            projectHook.setToken(token);
            String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
            uri += "devops/webhook";
            projectHook.setUrl(uri);
            List<ProjectHook> projectHooks = gitlabRepository
                    .getHooks(projectId, userId);
            if (projectHooks.isEmpty()) {
                applicationE.initHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(
                        projectId, userId, projectHook)
                        .getId()));
            } else {
                applicationE.initHookId(TypeUtil.objToLong(projectHooks.get(0).getId()));
            }
        }

        @Override
        public void operationApplicationImport (DevOpsAppImportPayload devOpsAppImportPayload){
            // 准备相关的数据
            DevopsProjectE devopsProjectE = devopsProjectRepository.queryByGitlabGroupId(
                    TypeUtil.objToInteger(devOpsAppImportPayload.getGroupId()));
            ApplicationE applicationE = applicationRepository.queryByCode(devOpsAppImportPayload.getPath(),
                    devopsProjectE.getProjectE().getId());
            ProjectVO projectE = iamService.queryIamProject(devopsProjectE.getProjectE().getId());
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
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


                // 设置Application对应的gitlab项目的仓库地址
                String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
                applicationE.initGitlabProjectEByUrl(repoUrl + organization.getCode()
                        + "-" + projectE.getCode() + "/" + applicationE.getCode() + ".git");

                File templateWorkDir = new File(gitUtil.getWorkingDirectory(templateDir));
                File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));

                try {
                    List<Ref> refs = repositoryGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                    for (Ref ref : refs) {
                        String branchName;
                        if (ref.getName().equals("refs/remotes/origin/master")) {
                            continue;
                        }
                        if (ref.getName().equals("refs/heads/master")) {
                            branchName = MASTER;
                        } else {
                            branchName = ref.getName().split("/")[3];
                        }
                        repositoryGit.checkout().setName(ref.getName()).call();
                        if (!branchName.equals(MASTER)) {
                            repositoryGit.checkout().setCreateBranch(true).setName(branchName).call();
                        }
=======
    @Override
    public void operationApplicationImport(DevOpsAppImportPayload devOpsAppImportPayload) {
        // 准备相关的数据
        DevopsProjectVO devopsProjectVO = devopsProjectRepository.queryByGitlabGroupId(
                TypeUtil.objToInteger(devOpsAppImportPayload.getGroupId()));
        ApplicationDTO applicationDTO = queryByCodeFromBase(devOpsAppImportPayload.getPath(),
                devopsProjectVO.getProjectE().getId());
        ProjectVO projectE = iamService.queryIamProject(devopsProjectVO.getProjectE().getId());
        OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
        GitlabProjectDTO gitlabProjectDO = gitLabService
                .getProjectByName(organization.getCode() + "-" + projectE.getCode(), applicationDTO.getCode(),
                        devOpsAppImportPayload.getUserId());
        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitLabService.createProject(devOpsAppImportPayload.getGroupId(),
                    devOpsAppImportPayload.getPath(),
                    devOpsAppImportPayload.getUserId(), false);
        }
        devOpsAppImportPayload.setGitlabProjectId(gitlabProjectDO.getId());

        // 为项目下的成员分配对于此gitlab项目的权限
        operateGitlabMemberPermission(devOpsAppImportPayload);

        if (applicationDTO.getAppTemplateId() != null) {
            UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(TypeUtil.objToLong(devOpsAppImportPayload.getUserId()));
            ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
                    applicationDTO.getAppTemplateId());
            // 拉取模板
            String templateDir = APPLICATION + UUIDTool.genUuid();
            Git templateGit = cloneTemplate(applicationTemplateE, templateDir);
            // 渲染模板里面的参数
            replaceParams(applicationDTO, projectE, organization, templateDir);

            // clone外部代码仓库
            String applicationDir = APPLICATION + UUIDTool.genUuid();
            Git repositoryGit = gitUtil.cloneRepository(applicationDir, devOpsAppImportPayload.getRepositoryUrl(), devOpsAppImportPayload.getAccessToken());


            // 设置Application对应的gitlab项目的仓库地址
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            applicationDTO.setRepoUrl(repoUrl + organization.getCode()
                    + "-" + projectE.getCode() + "/" + applicationDTO.getCode() + ".git");

            File templateWorkDir = new File(gitUtil.getWorkingDirectory(templateDir));
            File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));

            try {
                List<Ref> refs = repositoryGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                for (Ref ref : refs) {
                    String branchName;
                    if (ref.getName().equals("refs/remotes/origin/master")) {
                        continue;
                    }
                    if (ref.getName().equals("refs/heads/master")) {
                        branchName = MASTER;
                    } else {
                        branchName = ref.getName().split("/")[3];
                    }
                    repositoryGit.checkout().setName(ref.getName()).call();
                    if (!branchName.equals(MASTER)) {
                        repositoryGit.checkout().setCreateBranch(true).setName(branchName).call();
                    }
>>>>>>> [IMP] 修改AppControler重构
=======
        /**
         * 拉取模板库到本地
         *
         * @param applicationTemplateE 模板库的信息
         * @param applicationDir       本地库地址
         * @return 本地库的git实例
         */
        private Git cloneTemplate (ApplicationTemplateE applicationTemplateE, String applicationDir){
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
        private void setProjectHook (ApplicationE applicationE, Integer projectId, String token, Integer userId){
            ProjectHook projectHook = ProjectHook.allHook();
            projectHook.setEnableSslVerification(true);
            projectHook.setProjectId(projectId);
            projectHook.setToken(token);
            String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
            uri += "devops/webhook";
            projectHook.setUrl(uri);
            List<ProjectHook> projectHooks = gitLabService.getHooks(projectId, userId);
            if (projectHooks.isEmpty()) {
                applicationE.initHookId(TypeUtil.objToLong(gitLabService.createWebHook(
                        projectId, userId, projectHook)
                        .getId()));
            } else {
                applicationE.initHookId(TypeUtil.objToLong(projectHooks.get(0).getId()));
            }
        }

        @Override
        public void operationApplicationImport (DevOpsAppImportPayload devOpsAppImportPayload){
            // 准备相关的数据
            DevopsProjectVO devopsProjectVO = devopsProjectRepository.queryByGitlabGroupId(
                    TypeUtil.objToInteger(devOpsAppImportPayload.getGroupId()));
            ApplicationDTO applicationDTO = queryByCodeFromBase(devOpsAppImportPayload.getPath(),
                    devopsProjectVO.getProjectE().getId());
            ProjectVO projectE = iamService.queryIamProject(devopsProjectVO.getProjectE().getId());
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
            GitlabProjectDTO gitlabProjectDO = gitLabService
                    .getProjectByName(organization.getCode() + "-" + projectE.getCode(), applicationDTO.getCode(),
                            devOpsAppImportPayload.getUserId());
            if (gitlabProjectDO.getId() == null) {
                gitlabProjectDO = gitLabService.createProject(devOpsAppImportPayload.getGroupId(),
                        devOpsAppImportPayload.getPath(),
                        devOpsAppImportPayload.getUserId(), false);
            }
            devOpsAppImportPayload.setGitlabProjectId(gitlabProjectDO.getId());

            // 为项目下的成员分配对于此gitlab项目的权限
            operateGitlabMemberPermission(devOpsAppImportPayload);

            if (applicationDTO.getAppTemplateId() != null) {
                UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(TypeUtil.objToLong(devOpsAppImportPayload.getUserId()));
                ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
                        applicationDTO.getAppTemplateId());

                // 拉取模板
                String templateDir = APPLICATION + UUIDTool.genUuid();
                Git templateGit = cloneTemplate(applicationTemplateE, templateDir);
                // 渲染模板里面的参数
                replaceParams(applicationDTO, projectE, organization, templateDir);
>>>>>>> [IMP]重构后端代码

                // clone外部代码仓库
                String applicationDir = APPLICATION + UUIDTool.genUuid();
                Git repositoryGit = gitUtil.cloneRepository(applicationDir, devOpsAppImportPayload.getRepositoryUrl(), devOpsAppImportPayload.getAccessToken());

<<<<<<< HEAD
                        // 将模板库中文件复制到代码库中
                        mergeTemplateToApplication(templateWorkDir, applicationWorkDir, applicationTemplateE.getId());

                        // 获取push代码所需的access token
                        String accessToken = getToken(devOpsAppImportPayload, applicationDir, userAttrE);

<<<<<<< HEAD
                        BranchDO branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), branchName);
                        if (branchDO.getName() == null) {
                            try {
                                // 提交并推代码
                                gitUtil.commitAndPush(repositoryGit, applicationE.getGitlabProjectE().getRepoURL(), accessToken, ref.getName());
                            } catch (CommonException e) {
                                releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
                                throw e;
                            }

                            branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), branchName);
                            //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                            if (branchName.equals(MASTER)) {
                                if (!branchDO.getProtected()) {
                                    try {
                                        gitlabRepository.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER, AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(), devOpsAppImportPayload.getUserId());
                                    } catch (CommonException e) {
                                        if (!devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER).getProtected()) {
                                            throw new CommonException(e);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (branchName.equals(MASTER)) {
                                if (!branchDO.getProtected()) {
                                    gitlabRepository.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER,
                                            AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                                            devOpsAppImportPayload.getUserId());
                                }
=======
                    BranchDO branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), branchName);
                    if (branchDO.getName() == null) {
                        try {
                            // 提交并推代码
                            gitUtil.commitAndPush(repositoryGit, applicationDTO.getGitlabProjectE().getRepoURL(), accessToken, ref.getName());
                        } catch (CommonException e) {
                            releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
                            throw e;
=======

                // 设置Application对应的gitlab项目的仓库地址
                String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
                applicationDTO.setRepoUrl(repoUrl + organization.getCode()
                        + "-" + projectE.getCode() + "/" + applicationDTO.getCode() + ".git");

                File templateWorkDir = new File(gitUtil.getWorkingDirectory(templateDir));
                File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));

                try {
                    List<Ref> refs = repositoryGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                    for (Ref ref : refs) {
                        String branchName;
                        if (ref.getName().equals("refs/remotes/origin/master")) {
                            continue;
                        }
                        if (ref.getName().equals("refs/heads/master")) {
                            branchName = MASTER;
                        } else {
                            branchName = ref.getName().split("/")[3];
                        }
                        repositoryGit.checkout().setName(ref.getName()).call();
                        if (!branchName.equals(MASTER)) {
                            repositoryGit.checkout().setCreateBranch(true).setName(branchName).call();
>>>>>>> [IMP]重构后端代码
                        }


                        // 将模板库中文件复制到代码库中
                        mergeTemplateToApplication(templateWorkDir, applicationWorkDir, applicationTemplateE.getId());

                        // 获取push代码所需的access token
                        String accessToken = getToken(devOpsAppImportPayload, applicationDir, userAttrE);

                        BranchDO branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), branchName);
                        if (branchDO.getName() == null) {
                            try {
                                // 提交并推代码
                                gitUtil.commitAndPush(repositoryGit, applicationDTO.getGitlabProjectE().getRepoURL(), accessToken, ref.getName());
                            } catch (CommonException e) {
                                releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
                                throw e;
                            }

                            branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), branchName);
                            //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                            if (branchName.equals(MASTER)) {
                                if (!branchDO.getProtected()) {
                                    try {
                                        gitLabService.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER, AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(), devOpsAppImportPayload.getUserId());
                                    } catch (CommonException e) {
                                        if (!devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER).getProtected()) {
                                            throw new CommonException(e);
                                        }
                                    }
                                }
                            }
<<<<<<< HEAD
                        }
                    } else {
                        if (branchName.equals(MASTER)) {
                            if (!branchDO.getProtected()) {
                                gitLabService.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER,
                                        AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                                        devOpsAppImportPayload.getUserId());
>>>>>>> [IMP] 修改AppControler重构
                            }
                        }
                        initBranch(devOpsAppImportPayload, applicationE, branchName);
                    }
<<<<<<< HEAD
                } catch (GitAPIException e) {
                    e.printStackTrace();
=======
                    initBranch(devOpsAppImportPayload, applicationDTO, branchName);
>>>>>>> [IMP] 修改AppControler重构
                }

                releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
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


<<<<<<< HEAD
        /**
         * 释放资源
         */
        private void releaseResources (File templateWorkDir, File applicationWorkDir, Git templateGit, Git repositoryGit)
        {
            if (templateGit != null) {
                templateGit.close();
            }
            if (repositoryGit != null) {
                repositoryGit.close();
            }
            FileUtil.deleteDirectory(templateWorkDir);
            FileUtil.deleteDirectory(applicationWorkDir);
        }

        /**
         * 将模板库中的chart包，dockerfile，gitlab-ci文件复制到导入的代码仓库中
         * 复制文件前会判断文件是否存在，如果存在则不复制
         *
         * @param templateWorkDir       模板库工作目录
         * @param applicationWorkDir    应用库工作目录
         * @param applicationTemplateId application template id
         */
        private void mergeTemplateToApplication (File templateWorkDir, File applicationWorkDir, Long
        applicationTemplateId){
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
=======
        try {
            // 设置appliation的属性
            String applicationToken = getApplicationToken(gitlabProjectDO.getId(), devOpsAppImportPayload.getUserId());
            applicationDTO.initGitlabProjectE(TypeUtil.objToInteger(devOpsAppImportPayload.getGitlabProjectId()));
            applicationDTO.setToken(applicationToken);
            applicationDTO.setSynchro(true);
=======
                        } else {
                            if (branchName.equals(MASTER)) {
                                if (!branchDO.getProtected()) {
                                    gitLabService.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER,
                                            AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                                            devOpsAppImportPayload.getUserId());
                                }
                            }
                        }
                        initBranch(devOpsAppImportPayload, applicationDTO, branchName);
                    }
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }

                releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
            }


            try {
                // 设置appliation的属性
                String applicationToken = getApplicationToken(gitlabProjectDO.getId(), devOpsAppImportPayload.getUserId());
                applicationDTO.initGitlabProjectE(TypeUtil.objToInteger(devOpsAppImportPayload.getGitlabProjectId()));
                applicationDTO.setToken(applicationToken);
                applicationDTO.setSynchro(true);

                // set project hook id for application
                setProjectHook(applicationDTO, gitlabProjectDO.getId(), applicationToken, devOpsAppImportPayload.getUserId());

                // 更新并校验
                if (applicationRepository.update(applicationDTO) != 1) {
                    throw new CommonException(ERROR_UPDATE_APP);
                }
            } catch (Exception e) {
                throw new CommonException(e.getMessage(), e);
            }
        }
>>>>>>> [IMP]重构后端代码


<<<<<<< HEAD
            // 更新并校验
            if (applicationRepository.update(applicationDTO) != 1) {
                throw new CommonException(ERROR_UPDATE_APP);
>>>>>>> [IMP] 修改AppControler重构
            }
        }

        @Override
        @Saga(code = "devops-create-app-fail",
                description = "Devops设置application状态为创建失败(devops set app status create err)", inputSchema = "{}")
        public void setAppErrStatus (String input, Long projectId){
            sagaClient.startSaga("devops-create-app-fail", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));
        }

        private void initBranch (DevOpsAppPayload gitlabProjectPayload, ApplicationE applicationE, String branchName){
            CommitE commitE;
            try {
                commitE = devopsGitRepository.getCommit(
                        gitlabProjectPayload.getGitlabProjectId(), branchName, gitlabProjectPayload.getUserId());
            } catch (Exception e) {
                commitE = new CommitE();
            }
            DevopsBranchE devopsBranchE = new DevopsBranchE();
            devopsBranchE.setUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
            devopsBranchE.setApplicationE(applicationE);
            devopsBranchE.setBranchName(branchName);
            devopsBranchE.setCheckoutCommit(commitE.getId());
            devopsBranchE.setCheckoutDate(commitE.getCommittedDate());
            devopsBranchE.setLastCommitUser(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
            devopsBranchE.setLastCommitMsg(commitE.getMessage());
            devopsBranchE.setLastCommitDate(commitE.getCommittedDate());
            devopsBranchE.setLastCommit(commitE.getId());
            devopsGitRepository.createDevopsBranch(devopsBranchE);
        }

        private void replaceParams (ApplicationE applicationE, ProjectVO projectE, OrganizationVO organization,
                String applicationDir){
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

        private String getToken (DevOpsAppPayload gitlabProjectPayload, String applicationDir, UserAttrE userAttrE){
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
        public String queryFile (String token, String type){
            ApplicationE applicationE = applicationRepository.queryByToken(token);
            if (applicationE == null) {
                return null;
            }
            try {
                ProjectVO projectE = iamService.queryIamProject(applicationE.getProjectE().getId());
                OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
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

<<<<<<< HEAD
        @Override
        public List<ApplicationCodeDTO> listByEnvId (Long projectId, Long envId, String status, Long appId){
            List<ApplicationCodeDTO> applicationCodeDTOS = ConvertHelper
                    .convertList(applicationRepository.listByEnvId(projectId, envId, status),
                            ApplicationCodeDTO.class);
            if (appId != null) {
                ApplicationE applicationE = applicationRepository.query(appId);
                ApplicationCodeDTO applicationCodeDTO = new ApplicationCodeDTO();
                BeanUtils.copyProperties(applicationE, applicationCodeDTO);
                DevopsAppShareE applicationMarketE = applicationMarketRepository.queryByAppId(appId);
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
        public PageInfo<ApplicationCodeDTO> pageByIds (Long projectId, Long envId, Long appId, PageRequest pageRequest){
            return ConvertPageHelper.convertPageInfo(applicationRepository.pageByEnvId(projectId, envId, appId, pageRequest),
                    ApplicationCodeDTO.class);
=======
    private void initBranch(DevOpsAppPayload gitlabProjectPayload, ApplicationDTO applicationDTO, String branchName) {
        CommitVO commitVO=new CommitVO();
        try {
//            BeanUtils.copyProperties(
//                    gitlabServiceClient.getCommit(gitLabProjectId, commit, userId).getBody(),
//                    commitE);
            commitE = devopsGitRepository.getCommit(
                    gitlabProjectPayload.getGitlabProjectId(), branchName, gitlabProjectPayload.getUserId());
        } catch (Exception e) {
            commitVO = new CommitE();
        }
        DevopsBranchE devopsBranchE = new DevopsBranchE();
        devopsBranchE.setUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchE.setApplicationE(applicationDTO);
        devopsBranchE.setBranchName(branchName);
        devopsBranchE.setCheckoutCommit(commitVO.getId());
        devopsBranchE.setCheckoutDate(commitVO.getCommittedDate());
        devopsBranchE.setLastCommitUser(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchE.setLastCommitMsg(commitVO.getMessage());
        devopsBranchE.setLastCommitDate(commitVO.getCommittedDate());
        devopsBranchE.setLastCommit(commitVO.getId());
        devopsGitRepository.createDevopsBranch(devopsBranchE);
    }

    private void replaceParams(ApplicationDTO applicationDTO, ProjectVO projectVO, OrganizationVO organizationVO,
                               String applicationDir) {
        try {
            File file = new File(gitUtil.getWorkingDirectory(applicationDir));
            Map<String, String> params = new HashMap<>();
            params.put("{{group.name}}", organizationVO.getCode() + "-" + projectVO.getCode());
            params.put("{{service.code}}", applicationDTO.getCode());
            FileUtil.replaceReturnFile(file, params);
        } catch (Exception e) {
            //删除模板
            gitUtil.deleteWorkingDirectory(applicationDir);
            throw new CommonException(e.getMessage(), e);
>>>>>>> [IMP] 修改AppControler重构
        }

<<<<<<< HEAD
        @Override
        public PageInfo<ApplicationReqVO> pageByActiveAndPubAndVersion (Long projectId, PageRequest pageRequest,
                String params){
            return ConvertPageHelper.convertPageInfo(applicationRepository
                            .listByActiveAndPubAndVersion(projectId, true, pageRequest, params),
                    ApplicationReqVO.class);
=======
    private String getToken(DevOpsAppPayload gitlabProjectPayload, String applicationDir, UserAttrE userAttrE) {
        String accessToken = userAttrE.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitLabService.createToken(gitlabProjectPayload.getGitlabProjectId(),
                    applicationDir, gitlabProjectPayload.getUserId());
            userAttrE.setGitlabToken(accessToken);
            userAttrRepository.update(userAttrE);
>>>>>>> [IMP] 修改AppControler重构
=======
        /**
         * 释放资源
         */
        private void releaseResources (File templateWorkDir, File applicationWorkDir, Git templateGit, Git repositoryGit)
        {
            if (templateGit != null) {
                templateGit.close();
            }
            if (repositoryGit != null) {
                repositoryGit.close();
            }
            FileUtil.deleteDirectory(templateWorkDir);
            FileUtil.deleteDirectory(applicationWorkDir);
        }

        /**
         * 将模板库中的chart包，dockerfile，gitlab-ci文件复制到导入的代码仓库中
         * 复制文件前会判断文件是否存在，如果存在则不复制
         *
         * @param templateWorkDir       模板库工作目录
         * @param applicationWorkDir    应用库工作目录
         * @param applicationTemplateId application template id
         */
        private void mergeTemplateToApplication (File templateWorkDir, File applicationWorkDir, Long
        applicationTemplateId){
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
        @Saga(code = "devops-create-app-fail",
                description = "Devops设置application状态为创建失败(devops set app status create err)", inputSchema = "{}")
        public void setAppErrStatus (String input, Long projectId){
            sagaClient.startSaga("devops-create-app-fail", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));
        }

        private void initBranch (DevOpsAppPayload gitlabProjectPayload, ApplicationDTO applicationDTO, String branchName)
        {
            CommitVO commitVO = new CommitVO();
            try {
//            BeanUtils.copyProperties(
//                    gitlabServiceClient.getCommit(gitLabProjectId, commit, userId).getBody(),
//                    commitE);
                commitE = devopsGitRepository.getCommit(
                        gitlabProjectPayload.getGitlabProjectId(), branchName, gitlabProjectPayload.getUserId());
            } catch (Exception e) {
                commitVO = new CommitE();
            }
            DevopsBranchE devopsBranchE = new DevopsBranchE();
            devopsBranchE.setUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
            devopsBranchE.setApplicationE(applicationDTO);
            devopsBranchE.setBranchName(branchName);
            devopsBranchE.setCheckoutCommit(commitVO.getId());
            devopsBranchE.setCheckoutDate(commitVO.getCommittedDate());
            devopsBranchE.setLastCommitUser(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
            devopsBranchE.setLastCommitMsg(commitVO.getMessage());
            devopsBranchE.setLastCommitDate(commitVO.getCommittedDate());
            devopsBranchE.setLastCommit(commitVO.getId());
            devopsGitRepository.createDevopsBranch(devopsBranchE);
        }


        private void replaceParams (ApplicationDTO applicationDTO, ProjectVO projectVO, OrganizationVO organizationVO,
                                    String applicationDir){
            try {
                File file = new File(gitUtil.getWorkingDirectory(applicationDir));
                Map<String, String> params = new HashMap<>();
                params.put("{{group.name}}", organizationVO.getCode() + "-" + projectVO.getCode());
                params.put("{{service.code}}", applicationDTO.getCode());
                FileUtil.replaceReturnFile(file, params);
            } catch (Exception e) {
                //删除模板
                gitUtil.deleteWorkingDirectory(applicationDir);
                throw new CommonException(e.getMessage(), e);
            }
        }

        private String getToken (DevOpsAppPayload gitlabProjectPayload, String applicationDir, UserAttrE userAttrE){
            String accessToken = userAttrE.getGitlabToken();
            if (accessToken == null) {
                accessToken = gitLabService.createToken(gitlabProjectPayload.getGitlabProjectId(),
                        applicationDir, gitlabProjectPayload.getUserId());
                userAttrE.setGitlabToken(accessToken);
                userAttrRepository.update(userAttrE);
            }
            return accessToken;
>>>>>>> [IMP]重构后端代码
        }

<<<<<<< HEAD
        @Override
        public List<AppUserPermissionRepDTO> listAllUserPermission (Long appId){
            List<Long> userIds = appUserPermissionRepository.listAll(appId).stream().map(AppUserPermissionE::getIamUserId)
                    .collect(Collectors.toList());
            List<UserE> userEList = iamService.listUsersByIds(userIds);
            List<AppUserPermissionRepDTO> resultList = new ArrayList<>();
            userEList.forEach(
                    e -> resultList.add(new AppUserPermissionRepDTO(e.getId(), e.getLoginName(), e.getRealName())));
            return resultList;
=======

        @Override
        public String queryFile (String token, String type){
            ApplicationE applicationE = applicationRepository.queryByToken(token);
            if (applicationE == null) {
                return null;
            }
            try {
                ProjectVO projectE = iamService.queryIamProject(applicationE.getProjectE().getId());
                OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
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
<<<<<<< HEAD
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
>>>>>>> [IMP] 修改AppControler重构
        }

        @Override
        public Boolean validateRepositoryUrlAndToken (GitPlatformType gitPlatformType, String repositoryUrl, String
        accessToken){
            if (!REPOSITORY_URL_PATTERN.matcher(repositoryUrl).matches()) {
                return Boolean.FALSE;
            }
<<<<<<< HEAD
=======
            applicationCodeDTOS.add(0, applicationCodeDTO);
=======
        }

        @Override
        public List<ApplicationCodeDTO> listByEnvId (Long projectId, Long envId, String status, Long appId){
            List<ApplicationCodeDTO> applicationCodeDTOS = ConvertHelper
                    .convertList(applicationRepository.listByEnvId(projectId, envId, status),
                            ApplicationCodeDTO.class);
            if (appId != null) {
                ApplicationE applicationE = applicationRepository.query(appId);
                ApplicationCodeDTO applicationCodeDTO = new ApplicationCodeDTO();
                BeanUtils.copyProperties(applicationE, applicationCodeDTO);
                DevopsAppShareE applicationMarketE = applicationMarketRepository.baseQueryByAppId(appId);
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
>>>>>>> [IMP]重构后端代码
        }

        @Override
        public PageInfo<ApplicationCodeDTO> pageByIds (Long projectId, Long envId, Long appId, PageRequest pageRequest){
            return ConvertPageHelper.convertPageInfo(applicationRepository.pageByEnvId(projectId, envId, appId, pageRequest),
                    ApplicationCodeDTO.class);
        }

<<<<<<< HEAD
    @Override
    public List<AppUserPermissionRepDTO> listAllUserPermission(Long appId) {
        List<Long> userIds = appUserPermissionRepository.listAll(appId).stream().map(AppUserPermissionE::getIamUserId)
                .collect(Collectors.toList());
        List<UserE> userEList = iamService.listUsersByIds(userIds);
        List<AppUserPermissionRepDTO> resultList = new ArrayList<>();
        userEList.forEach(
                e -> resultList.add(new AppUserPermissionRepDTO(e.getId(), e.getLoginName(), e.getRealName())));
        return resultList;
    }
>>>>>>> [IMP] 修改AppControler重构

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
        private void checkRepositoryUrlAndToken (GitPlatformType gitPlatformType, String repositoryUrl, String
        accessToken){
            Boolean validationResult = validateRepositoryUrlAndToken(gitPlatformType, repositoryUrl, accessToken);
            if (Boolean.FALSE.equals(validationResult)) {
                throw new CommonException("error.repository.token.invalid");
            } else if (validationResult == null) {
                throw new CommonException("error.repository.empty");
            }
        }

        @Override
        @Saga(code = "devops-import-gitlab-project", description = "Devops从外部代码平台导入到gitlab项目", inputSchema = "{}")
        public ApplicationRepVO importApp (Long projectId, ApplicationImportDTO applicationImportDTO){
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

            ProjectVO projectE = iamService.queryIamProject(projectId);
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());

<<<<<<< HEAD
            ApplicationE applicationE = fromImportDtoToEntity(applicationImportDTO);
=======
        ProjectVO projectE = iamService.queryIamProject(projectId);
        OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
>>>>>>> [IMP] 修改AppControler重构

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
            Long appId = applicationE.getId();

            IamAppPayLoad iamAppPayLoad = new IamAppPayLoad();
            iamAppPayLoad.setApplicationCategory(APPLICATION);
            iamAppPayLoad.setApplicationType(applicationImportDTO.getType());
            iamAppPayLoad.setCode(applicationImportDTO.getCode());
            iamAppPayLoad.setName(applicationImportDTO.getName());
            iamAppPayLoad.setEnabled(true);
            iamAppPayLoad.setOrganizationId(organization.getId());
            iamAppPayLoad.setProjectId(projectId);
            iamAppPayLoad.setFrom(applicationName);
            //iam创建应用
            iamService.createIamApp(organization.getId(), iamAppPayLoad);

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

<<<<<<< HEAD
            String input = gson.toJson(devOpsAppImportPayload);
            sagaClient.startSaga("devops-import-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));
=======
        // 查询创建应用所在的gitlab应用组
        DevopsProjectVO devopsProjectE = devopsProjectRepository.queryDevopsProject(applicationE.getProjectE().getId());
        GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
>>>>>>> [IMP] 修改AppControler重构

            return ConvertHelper.convert(applicationRepository.query(appId), ApplicationRepVO.class);
        }

<<<<<<< HEAD
        @Override
        public ApplicationRepVO queryByCode (Long projectId, String code){
            return ConvertHelper.convert(applicationRepository.queryByCode(code, projectId), ApplicationRepVO.class);
=======
        // 创建应用
        applicationE = applicationRepository.create(applicationE);
        Long appId = applicationE.getId();
=======
        @Override
        public PageInfo<ApplicationReqVO> pageByActiveAndPubAndVersion (Long projectId, PageRequest pageRequest,
                String params){
            return ConvertPageHelper.convertPageInfo(applicationRepository
                            .listByActiveAndPubAndVersion(projectId, true, pageRequest, params),
                    ApplicationReqVO.class);
        }

        @Override
        public List<AppUserPermissionRepDTO> listAllUserPermission (Long appId){
            List<Long> userIds = appUserPermissionRepository.baseListByAppId(appId).stream().map(AppUserPermissionE::getIamUserId)
                    .collect(Collectors.toList());
            List<UserE> userEList = iamService.listUsersByIds(userIds);
            List<AppUserPermissionRepDTO> resultList = new ArrayList<>();
            userEList.forEach(
                    e -> resultList.add(new AppUserPermissionRepDTO(e.getId(), e.getLoginName(), e.getRealName())));
            return resultList;
        }

        @Override
        public Boolean validateRepositoryUrlAndToken (GitPlatformType gitPlatformType, String repositoryUrl, String
        accessToken){
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
        private void checkRepositoryUrlAndToken (GitPlatformType gitPlatformType, String repositoryUrl, String
        accessToken){
            Boolean validationResult = validateRepositoryUrlAndToken(gitPlatformType, repositoryUrl, accessToken);
            if (Boolean.FALSE.equals(validationResult)) {
                throw new CommonException("error.repository.token.invalid");
            } else if (validationResult == null) {
                throw new CommonException("error.repository.empty");
            }
        }

        @Override
        @Saga(code = "devops-import-gitlab-project", description = "Devops从外部代码平台导入到gitlab项目", inputSchema = "{}")
        public ApplicationRepVO importApp (Long projectId, ApplicationImportDTO applicationImportDTO){
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

            ProjectVO projectE = iamService.queryIamProject(projectId);
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());

            ApplicationE applicationE = fromImportDtoToEntity(applicationImportDTO);

            applicationE.initProjectE(projectId);


            applicationE.initActive(true);
            applicationE.initSynchro(false);
            applicationE.setIsSkipCheckPermission(applicationImportDTO.getIsSkipCheckPermission());
            applicationE.initHarborConfig(applicationImportDTO.getHarborConfigId());
            applicationE.initChartConfig(applicationImportDTO.getChartConfigId());

            // 查询创建应用所在的gitlab应用组
            DevopsProjectVO devopsProjectE = devopsProjectRepository.queryDevopsProject(applicationE.getProjectE().getId());
            GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                    TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));

            // 校验用户的gitlab权限
            if (gitlabMemberE == null || gitlabMemberE.getAccessLevel() != AccessLevel.OWNER.toValue()) {
                throw new CommonException("error.user.not.owner");
            }

            // 创建应用
            applicationE = applicationRepository.create(applicationE);
            Long appId = applicationE.getId();

            IamAppPayLoad iamAppPayLoad = new IamAppPayLoad();
            iamAppPayLoad.setApplicationCategory(APPLICATION);
            iamAppPayLoad.setApplicationType(applicationImportDTO.getType());
            iamAppPayLoad.setCode(applicationImportDTO.getCode());
            iamAppPayLoad.setName(applicationImportDTO.getName());
            iamAppPayLoad.setEnabled(true);
            iamAppPayLoad.setOrganizationId(organization.getId());
            iamAppPayLoad.setProjectId(projectId);
            iamAppPayLoad.setFrom(applicationName);
            //iam创建应用
            iamService.createIamApp(organization.getId(), iamAppPayLoad);

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
                userIds.forEach(e -> appUserPermissionRepository.baseCreate(e, appId));
            }
>>>>>>> [IMP]重构后端代码

            String input = gson.toJson(devOpsAppImportPayload);
            sagaClient.startSaga("devops-import-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));

<<<<<<< HEAD
        // 如果不跳过权限检查
        List<Long> userIds = applicationImportDTO.getUserIds();
        if (!applicationImportDTO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> appUserPermissionRepository.create(e, appId));
>>>>>>> [IMP] 修改AppControler重构
        }

=======
            return ConvertHelper.convert(applicationRepository.query(appId), ApplicationRepVO.class);
        }

        @Override
        public ApplicationRepVO queryByCode (Long projectId, String code){
            return ConvertHelper.convert(applicationRepository.queryByCode(code, projectId), ApplicationRepVO.class);
        }

>>>>>>> [IMP]重构后端代码

        @Override
        @Saga(code = "devops-create-gitlab-project",
                description = "Devops创建gitlab项目", inputSchema = "{}")
        public void createIamApplication (IamAppPayLoad iamAppPayLoad){

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
<<<<<<< HEAD
                    userIds = appUserPermissionRepository.listAll(applicationE.getId()).stream().map(AppUserPermissionE::getIamUserId).collect(Collectors.toList());
                }
            }

            //创建iam入口过来的应用直接用管理员去gitlab创建对应的project,避免没有对应项目的权限导致创建失败
            Long gitlabUserId = 1L;
            if (applicationName.equals(iamAppPayLoad.getFrom())) {
                UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                gitlabUserId = userAttrE.getGitlabUserId();
=======
                    userIds = appUserPermissionRepository.baseListByAppId(applicationE.getId()).stream().map(AppUserPermissionE::getIamUserId).collect(Collectors.toList());
                }
            }

            //创建iam入口过来的应用直接用管理员去gitlab创建对应的project,避免没有对应项目的权限导致创建失败
            Long gitlabUserId = 1L;
            if (applicationName.equals(iamAppPayLoad.getFrom())) {
                UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                gitlabUserId = userAttrE.getGitlabUserId();
            }

            DevopsProjectVO devopsProjectE = devopsProjectRepository.queryDevopsProject(iamAppPayLoad.getProjectId());

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
            if (applicationE.getGitlabProjectE() == null) {
                String input = gson.toJson(devOpsAppPayload);
                sagaClient.startSaga("devops-create-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), iamAppPayLoad.getProjectId()));
>>>>>>> [IMP]重构后端代码
            }

<<<<<<< HEAD
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
            if (applicationE.getGitlabProjectE() == null) {
                String input = gson.toJson(devOpsAppPayload);
                sagaClient.startSaga("devops-create-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), iamAppPayLoad.getProjectId()));
            }
        }

<<<<<<< HEAD
=======
>>>>>>> [IMP]重构后端代码
        @Override
        public void updateIamApplication (IamAppPayLoad iamAppPayLoad){
            ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
            applicationE.setName(iamAppPayLoad.getName());
            applicationRepository.update(applicationE);
<<<<<<< HEAD
=======
        DevopsProjectVO devopsProjectE = devopsProjectRepository.queryDevopsProject(iamAppPayLoad.getProjectId());

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
        if (applicationE.getGitlabProjectE() == null) {
            String input = gson.toJson(devOpsAppPayload);
            sagaClient.startSaga("devops-create-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), iamAppPayLoad.getProjectId()));
>>>>>>> [IMP] 修改AppControler重构
        }

<<<<<<< HEAD
        @Override
        public void deleteIamApplication (IamAppPayLoad iamAppPayLoad){
            ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
            if (applicationE.getGitlabProjectE() != null) {
                gitlabRepository.deleteProject(applicationE.getGitlabProjectE().getId(), 1);
            }
            applicationRepository.delete(applicationE.getId());
=======
    @Override
    public void updateIamApplication(IamAppPayLoad iamAppPayLoad) {
        ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        applicationE.setName(iamAppPayLoad.getName());
        applicationRepository.update(applicationE);
    }

    @Override
    public void deleteIamApplication(IamAppPayLoad iamAppPayLoad) {
        ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        if (applicationE.getGitlabProjectE() != null) {
            gitLabService.deleteProject(applicationE.getGitlabProjectE().getId(), 1);
>>>>>>> [IMP] 修改AppControler重构
=======
        }

        @Override
        public void deleteIamApplication (IamAppPayLoad iamAppPayLoad){
            ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
            if (applicationE.getGitlabProjectE() != null) {
                gitLabService.deleteProject(applicationE.getGitlabProjectE().getId(), 1);
            }
            applicationRepository.delete(applicationE.getId());
>>>>>>> [IMP]重构后端代码
        }

        @Override
        public Boolean checkHarbor (String url, String userName, String password, String project, String email){
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
        public Boolean checkChart (String url){
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


        private ApplicationE fromImportDtoToEntity (ApplicationImportDTO applicationImportDTO){
<<<<<<< HEAD
            ApplicationE applicationE = ApplicationFactory.createApplicationE();
=======
            ApplicationE applicationE = new ApplicationE();
>>>>>>> [IMP]重构后端代码
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
        public SonarContentsDTO getSonarContent (Long projectId, Long appId){

<<<<<<< HEAD
<<<<<<< HEAD
            //没有使用sonarqube直接返回空对象
            if (sonarqubeUrl.equals("")) {
=======
        //没有使用sonarqube直接返回空对象
        if (sonarqubeUrl.equals("")) {
            return new SonarContentsDTO();
        }
        SonarContentsDTO sonarContentsDTO = new SonarContentsDTO();
        List<SonarContentDTO> sonarContentDTOS = new ArrayList<>();
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectVO projectE = iamService.queryIamProject(projectId);
        OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());


        //初始化sonarClient
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), applicationE.getCode());
        sonarqubeUrl = sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/";
        try {

            //初始化查询参数
            Map<String, String> queryContentMap = new HashMap<>();
            queryContentMap.put("additionalFields", "metrics,periods");
            queryContentMap.put("componentKey", key);
            queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,new_bugs,new_vulnerabilities,sqale_index,code_smells,new_technical_debt,new_code_smells,coverage,tests,new_coverage,duplicated_lines_density,duplicated_blocks,new_duplicated_lines_density,ncloc,ncloc_language_distribution");

            //根据project-key查询sonarqube项目内容
            Response<SonarComponent> sonarComponentResponse = sonarClient.getSonarComponet(queryContentMap).execute();
            if (sonarComponentResponse.raw().code() != 200) {
                if (sonarComponentResponse.raw().code() == 404) {
                    return new SonarContentsDTO();
                }
                if (sonarComponentResponse.raw().code() == 401) {
                    throw new CommonException("error.sonarqube.user");
                }
                throw new CommonException(sonarComponentResponse.errorBody().string());
            }
            if (sonarComponentResponse.body() == null) {
>>>>>>> [IMP] 修改AppControler重构
=======
            //没有使用sonarqube直接返回空对象
            if (sonarqubeUrl.equals("")) {
>>>>>>> [IMP]重构后端代码
                return new SonarContentsDTO();
            }
            SonarContentsDTO sonarContentsDTO = new SonarContentsDTO();
            List<SonarContentDTO> sonarContentDTOS = new ArrayList<>();
            ApplicationE applicationE = applicationRepository.query(appId);
            ProjectVO projectE = iamService.queryIamProject(projectId);
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());


            //初始化sonarClient
            SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
            String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), applicationE.getCode());
            sonarqubeUrl = sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/";
            try {

                //初始化查询参数
                Map<String, String> queryContentMap = new HashMap<>();
                queryContentMap.put("additionalFields", "metrics,periods");
                queryContentMap.put("componentKey", key);
                queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,new_bugs,new_vulnerabilities,sqale_index,code_smells,new_technical_debt,new_code_smells,coverage,tests,new_coverage,duplicated_lines_density,duplicated_blocks,new_duplicated_lines_density,ncloc,ncloc_language_distribution");

                //根据project-key查询sonarqube项目内容
                Response<SonarComponent> sonarComponentResponse = sonarClient.getSonarComponet(queryContentMap).execute();
                if (sonarComponentResponse.raw().code() != 200) {
                    if (sonarComponentResponse.raw().code() == 404) {
                        return new SonarContentsDTO();
                    }
                    if (sonarComponentResponse.raw().code() == 401) {
                        throw new CommonException("error.sonarqube.user");
                    }
                    throw new CommonException(sonarComponentResponse.errorBody().string());
                }
                if (sonarComponentResponse.body() == null) {
                    return new SonarContentsDTO();
                }
                if (sonarComponentResponse.body().getPeriods() != null && sonarComponentResponse.body().getPeriods().size() > 0) {
                    sonarContentsDTO.setDate(sonarComponentResponse.body().getPeriods().get(0).getDate());
                    sonarContentsDTO.setMode(sonarComponentResponse.body().getPeriods().get(0).getMode());
                    sonarContentsDTO.setParameter(sonarComponentResponse.body().getPeriods().get(0).getParameter());
                } else {
                    Map<String, String> analyseMap = new HashMap<>();
                    analyseMap.put("project", key);
                    analyseMap.put("ps", "3");

                    //查询上一次的分析时间
                    Response<SonarAnalyses> sonarAnalyses = sonarClient.getAnalyses(analyseMap).execute();
                    if (sonarAnalyses.raw().code() == 200 && sonarAnalyses.body().getAnalyses() != null && sonarAnalyses.body().getAnalyses().size() > 0) {
                        sonarContentsDTO.setDate(sonarAnalyses.body().getAnalyses().get(0).getDate());
                    }
                }

                //分类型对sonarqube project查询返回的结果进行处理
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
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> [IMP]重构后端代码
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
<<<<<<< HEAD
=======
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
        if (sonarqubeUrl.equals("")) {
            return new SonarTableDTO();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(endTime);
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = c.getTime();
        SonarTableDTO sonarTableDTO = new SonarTableDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectVO projectE = iamService.queryIamProject(projectId);
        OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
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
                    if (sonarTablesResponse.raw().code() == 401) {
                        throw new CommonException("error.sonarqube.user");
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
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            bugs.add(sonarHistroy.getValue());
                            dates.add(sonarHistroy.getDate());
                        });
                        sonarTableDTO.setDates(dates);
                        sonarTableDTO.setBugs(bugs);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.CODE_SMELLS.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            codeSmells.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setCodeSmells(codeSmells);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.VULNERABILITIES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            vulnerabilities.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setVulnerabilities(vulnerabilities);
>>>>>>> [IMP] 修改AppControler重构
=======
>>>>>>> [IMP]重构后端代码
                    }
                });
                sonarContentsDTO.setSonarContents(sonarContentDTOS);
            } catch (IOException e) {
                throw new CommonException(e);
            }
            return sonarContentsDTO;
        }

        @Override
        public SonarTableDTO getSonarTable (Long projectId, Long appId, String type, Date startTime, Date endTime){
            if (sonarqubeUrl.equals("")) {
                return new SonarTableDTO();
            }
            Calendar c = Calendar.getInstance();
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, 1);
            Date tomorrow = c.getTime();
            SonarTableDTO sonarTableDTO = new SonarTableDTO();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
            ApplicationE applicationE = applicationRepository.query(appId);
            ProjectVO projectE = iamService.queryIamProject(projectId);
            OrganizationVO organization = iamService.queryOrganizationById(projectE.getOrganization().getId());
            SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
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
                        if (sonarTablesResponse.raw().code() == 401) {
                            throw new CommonException("error.sonarqube.user");
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
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
                            ).forEach(sonarHistroy -> {
                                bugs.add(sonarHistroy.getValue());
                                dates.add(sonarHistroy.getDate());
                            });
                            sonarTableDTO.setDates(dates);
                            sonarTableDTO.setBugs(bugs);
                        }
                        if (sonarTableMeasure.getMetric().equals(SonarQubeType.CODE_SMELLS.getType())) {
                            sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
                            ).forEach(sonarHistroy -> {
                                codeSmells.add(sonarHistroy.getValue());
                            });
                            sonarTableDTO.setCodeSmells(codeSmells);
                        }
                        if (sonarTableMeasure.getMetric().equals(SonarQubeType.VULNERABILITIES.getType())) {
                            sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
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
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
                            ).forEach(sonarHistroy -> {
                                coverage.add(sonarHistroy.getValue());
                            });
                            sonarTableDTO.setCoverage(coverage);
                        }
                        if (sonarTableMeasure.getMetric().equals(SonarQubeType.LINES_TO_COVER.getType())) {
                            sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
                            ).forEach(sonarHistroy -> {
                                linesToCover.add(sonarHistroy.getValue());
                                dates.add(sonarHistroy.getDate());
                            });
                            sonarTableDTO.setDates(dates);
                            sonarTableDTO.setLinesToCover(linesToCover);
                        }

                        if (sonarTableMeasure.getMetric().equals(SonarQubeType.UNCOVERED_LINES.getType())) {
                            sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
                            ).forEach(sonarHistroy -> {
                                unCoverLines.add(sonarHistroy.getValue());
                            });
                        }
                    });
                    for (int i = 0; i < linesToCover.size(); i++) {
                        coverLines.add(TypeUtil.objToString(TypeUtil.objToLong(linesToCover.get(i)) - TypeUtil.objToLong(unCoverLines.get(i))));
                    }
                    sonarTableDTO.setCoverLines(coverLines);
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
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
                            ).forEach(sonarHistroy -> {
                                nclocs.add(sonarHistroy.getValue());
                                dates.add(sonarHistroy.getDate());
                            });
                            sonarTableDTO.setNclocs(nclocs);
                            sonarTableDTO.setDates(dates);
                        }
                        if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES.getType())) {
                            sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
                            ).forEach(sonarHistroy ->
                                    duplicatedLines.add(sonarHistroy.getValue())
                            );
                            sonarTableDTO.setDuplicatedLines(duplicatedLines);
                        }
                        if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES_DENSITY.getType())) {
                            sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                    getHistory(startTime, tomorrow, sdf, sonarHistroy)
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


<<<<<<< HEAD
        public String getGitlabUrl (Long projectId, Long appId){
            ApplicationE applicationE = applicationRepository.query(appId);
            if (applicationE.getGitlabProjectE() != null && applicationE.getGitlabProjectE().getId() != null) {
                ProjectE projectE = iamRepository.queryIamProject(projectId);
                Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
                String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
                return gitlabUrl + urlSlash
                        + organization.getCode() + "-" + projectE.getCode() + "/"
                        + applicationE.getCode();
            }
            return "";
        }

        private Integer getGitLabId (Long applicationId){
            ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(applicationId);
            if (applicationDO != null) {
                return applicationDO.getGitlabProjectId();
            } else {
                throw new CommonException("error.application.select");
            }
=======
        public void baseCheckApp (Long projectId, Long appId){
            ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(appId);
            if (applicationDTO == null || !applicationDTO.getProjectId().equals(projectId)) {
                throw new CommonException("error.app.project.notMatch");
            }
        }


        public int baseUpdate (ApplicationDTO applicationDTO){
            ApplicationDTO oldApplicationDTO = applicationMapper.selectByPrimaryKey(applicationDTO.getId());
            if (applicationDTO.getFailed() != null && !applicationDTO.getFailed()) {
                applicationMapper.updateAppToSuccess(applicationDTO.getId());
            }
            applicationDTO.setObjectVersionNumber(oldApplicationDTO.getObjectVersionNumber());
            return applicationMapper.updateByPrimaryKeySelective(applicationDTO);
>>>>>>> [IMP]重构后端代码
        }

        public void updateApplicationStatus (ApplicationDTO applicationDTO){
            applicationMapper.updateApplicationStatus(applicationDTO.getId(), applicationDTO.getToken(),
                    applicationDTO.getGitlabProjectId(), applicationDTO.getHookId(), applicationDTO.getSynchro());
        }

<<<<<<< HEAD
        private boolean getHistory (Date startTime, Date endTime, SimpleDateFormat sdf, SonarHistroy sonarHistroy){
            try {
                return sdf.parse(sonarHistroy.getDate()).compareTo(startTime) >= 0 && sdf.parse(sonarHistroy.getDate()).compareTo(endTime) <= 0;
            } catch (ParseException e) {
                throw new CommonException(e);
            }
=======
        public ApplicationDTO baseQuery (Long applicationId){
            return applicationMapper.selectByPrimaryKey(applicationId);
        }

        public PageInfo<ApplicationDTO> basePageByOptions (Long projectId, Boolean isActive, Boolean hasVersion, Boolean
        appMarket,
                String type, Boolean doPage, PageRequest pageRequest, String params){
            PageInfo<ApplicationDTO> applicationDTOPageInfo = new PageInfo<>();

            Map<String, Object> mapParams = TypeUtil.castMapParams(params);
            //是否需要分页
            if (doPage != null && !doPage) {
                applicationDTOPageInfo.setList(applicationMapper.list(projectId, isActive, hasVersion, appMarket, type,
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        mapParams.get(TypeUtil.PARAM).toString(), PageRequestUtil.checkSortIsEmpty(pageRequest)));
            } else {
                applicationDTOPageInfo = PageHelper
                        .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.list(projectId, isActive, hasVersion, appMarket, type,
                                (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                                (String) mapParams.get(TypeUtil.PARAM), PageRequestUtil.checkSortIsEmpty(pageRequest)));
            }
            return applicationDTOPageInfo;
>>>>>>> [IMP]重构后端代码
        }

        public PageInfo<ApplicationDTO> basePageCodeRepository (Long projectId, PageRequest pageRequest, String params,
                Boolean isProjectOwner, Long userId){
            Map maps = gson.fromJson(params, Map.class);
            return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.listCodeRepository(projectId,
                    TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(maps.get(TypeUtil.PARAM)), isProjectOwner, userId));
        }


        public ApplicationDTO baseQueryByCode (String code, Long projectId){
            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setProjectId(projectId);
            applicationDTO.setCode(code);
            return applicationMapper.selectOne(applicationDTO);
        }

        public ApplicationDTO baseQueryByCodeWithNullProject (String code){
            return applicationMapper.queryByCodeWithNoProject(code);
        }

        public List<ApplicationDTO> baseListByEnvId (Long projectId, Long envId, String status){
            return applicationMapper.listByEnvId(projectId, envId, null, status);
        }

<<<<<<< HEAD
=======
        public PageInfo<ApplicationDTO> basePageByEnvId (Long projectId, Long envId, Long appId, PageRequest pageRequest)
        {
            return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.listByEnvId(projectId, envId, appId, NODELETED));

        }

        public List<ApplicationDTO> baseListByActive (Long projectId){
            return applicationMapper.listByActive(projectId);
        }

        public List<ApplicationDTO> baseListDeployedApp (Long projectId){
            return applicationMapper.listDeployedApp(projectId);
        }


        public PageInfo<ApplicationDTO> basePageByActiveAndPubAndHasVersion (Long projectId, Boolean isActive,
                PageRequest pageRequest, String params){
            Map<String, Object> searchParam = null;
            String param = null;
            if (!StringUtils.isEmpty(params)) {
                Map<String, Object> searchParamMap = json.deserialize(params, Map.class);
                searchParam = TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM));
                param = TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM));
            }
            Map<String, Object> finalSearchParam = searchParam;
            String finalParam = param;

            return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper
                    .basePageByActiveAndPubAndHasVersion(projectId, isActive, finalSearchParam, finalParam));
        }

        public ApplicationDTO baseQueryByToken (String token){
            return applicationMapper.queryByToken(token);
        }

        public void baseCheckAppCanDisable (Long applicationId){
            if (applicationMapper.checkAppCanDisable(applicationId) == 0) {
                throw new CommonException("error.app.publishedOrDeployed");
            }
        }

        public List<ApplicationDTO> baseListByCode (String code){
            return applicationMapper.listByCode(code);
        }

        public List<ApplicationDTO> baseListByGitLabProjectIds (List < Long > gitLabProjectIds) {
            return applicationMapper.listByGitLabProjectIds(gitLabProjectIds);
        }

        public void baseDelete (Long appId){
            applicationMapper.deleteByPrimaryKey(appId);
        }

        public List<ApplicationDTO> baseListByProjectIdAndSkipCheck (Long projectId){
            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setProjectId(projectId);
            applicationDTO.setIsSkipCheckPermission(true);
            return applicationMapper.select(applicationDTO);
        }


        public List<ApplicationDTO> baseListByProjectId (Long projectId){
            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setProjectId(projectId);
            return applicationMapper.select(applicationDTO);
        }

        public void baseUpdateHarborConfig (Long projectId, Long newConfigId, Long oldConfigId,boolean harborPrivate){
            applicationMapper.updateHarborConfig(projectId, newConfigId, oldConfigId, harborPrivate);
        }


        public String getGitlabUrl (Long projectId, Long appId){
            ApplicationDTO applicationDTO = baseQuery(appId);
            if (applicationDTO.getGitlabProjectId() != null) {
                ProjectDO projectDO = iamServiceClientOperator.queryIamProject(projectId);
                OrganizationDO organizationDO = iamServiceClientOperator.queryOrganizationById(projectDO.getOrganizationId());
                String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
                return gitlabUrl + urlSlash
                        + organizationDO.getCode() + "-" + projectDO.getCode() + "/"
                        + applicationDTO.getCode();
            }
            return "";
        }

        private Integer getGitLabId (Long applicationId){
            ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(applicationId);
            if (applicationDTO != null) {
                return applicationDTO.getGitlabProjectId();
            } else {
                throw new CommonException("error.application.select");
            }
        }


        private boolean getHistory (Date startTime, Date endTime, SimpleDateFormat sdf, SonarHistroy sonarHistroy){
            try {
                return sdf.parse(sonarHistroy.getDate()).compareTo(startTime) >= 0 && sdf.parse(sonarHistroy.getDate()).compareTo(endTime) <= 0;
            } catch (ParseException e) {
                throw new CommonException(e);
            }
        }


>>>>>>> [IMP]重构后端代码
        private void getRate (SonarContentDTO sonarContentDTO, List < Facet > facets){
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
<<<<<<< HEAD
        }


        private Map<String, String> getQueryMap (String key, String type, Boolean newAdd){
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


        private ApplicationDTO getApplicationDTO (Long projectId, ApplicationReqVO applicationReqDTO){
            ApplicationDTO applicationDTO = ConvertHelper.convert(applicationReqDTO, ApplicationDTO.class);
            checkNameFromBase(projectId, applicationDTO.getName());
            checkCodeFromBase(applicationDTO);
            applicationDTO.setActive(true);
            applicationDTO.setSynchro(false);
            applicationDTO.setIsSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());
            applicationDTO.setHarborConfigId(applicationReqDTO.getHarborConfigId());
            applicationDTO.setChartConfigId(applicationReqDTO.getChartConfigId());
            return applicationDTO;
        }

        private void checkNameFromBase (Long projectId, String appName){
            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setProjectId(projectId);
            applicationDTO.setName(appName);
            if (applicationMapper.selectOne(applicationDTO) != null) {
                throw new CommonException("error.name.exist");
            }
=======
>>>>>>> [IMP]重构后端代码
        }

        private void checkCodeFromBase (ApplicationDTO applicationDTO){
            if (!applicationMapper.select(applicationDTO).isEmpty()) {
                throw new CommonException("error.code.exist");
            }
        }

<<<<<<< HEAD
        private ApplicationDTO createFromBase (ApplicationDTO applicationDTO){
            if (applicationMapper.insert(applicationDTO) != 1) {
                throw new CommonException("error.application.create.insert");
            }
            return applicationDTO;
        }

<<<<<<< HEAD
        private ApplicationDTO queryByCode (String code, Long projectId){
            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setProjectId(projectId);
            applicationDTO.setCode(code);
            return applicationMapper.selectOne(applicationDTO);
        }
    }
=======
    private ApplicationDTO getApplicationDTO(Long projectId, ApplicationReqVO applicationReqDTO) {
        ApplicationDTO applicationDTO = ConvertHelper.convert(applicationReqDTO, ApplicationDTO.class);
        checkNameFromBase(projectId, applicationDTO.getName());
        checkCodeFromBase(applicationDTO);
        applicationDTO.setActive(true);
        applicationDTO.setSynchro(false);
        applicationDTO.setIsSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());
        applicationDTO.setHarborConfigId(applicationReqDTO.getHarborConfigId());
        applicationDTO.setChartConfigId(applicationReqDTO.getChartConfigId());
        return applicationDTO;
    }

    private void checkNameFromBase(Long projectId, String appName) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setProjectId(projectId);
        applicationDTO.setName(appName);
        if (applicationMapper.selectOne(applicationDTO) != null) {
            throw new CommonException("error.name.exist");
=======
        private Map<String, String> getQueryMap (String key, String type, Boolean newAdd){
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


        private ApplicationDTO getApplicationDTO (Long projectId, ApplicationReqVO applicationReqDTO){
            ApplicationDTO applicationDTO = ConvertHelper.convert(applicationReqDTO, ApplicationDTO.class);
            baseCheckName(projectId, applicationDTO.getName());
            baseCheckCode(applicationDTO);
            applicationDTO.setActive(true);
            applicationDTO.setSynchro(false);
            applicationDTO.setIsSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());
            applicationDTO.setHarborConfigId(applicationReqDTO.getHarborConfigId());
            applicationDTO.setChartConfigId(applicationReqDTO.getChartConfigId());
            return applicationDTO;
        }

        private void baseCheckName (Long projectId, String appName){
            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setProjectId(projectId);
            applicationDTO.setName(appName);
            if (applicationMapper.selectOne(applicationDTO) != null) {
                throw new CommonException("error.name.exist");
            }
>>>>>>> [IMP]重构后端代码
        }

        private void baseCheckCode (ApplicationDTO applicationDTO){
            if (!applicationMapper.select(applicationDTO).isEmpty()) {
                throw new CommonException("error.code.exist");
            }
        }

        private ApplicationDTO baseCreate (ApplicationDTO applicationDTO){
            if (applicationMapper.insert(applicationDTO) != 1) {
                throw new CommonException("error.application.create.insert");
            }
            return applicationDTO;
        }
<<<<<<<HEAD

        private ApplicationDTO queryByCode (String code, Long projectId){
            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setProjectId(projectId);
            applicationDTO.setCode(code);
            return applicationMapper.selectOne(applicationDTO);
        }

        private int updateFromBase (ApplicationDTO applicationDTO){
            if (applicationDTO.getFailed() != null && !applicationDTO.getFailed()) {
                applicationMapper.updateAppToSuccess(applicationDTO.getId());
            }
            applicationDTO.setObjectVersionNumber(applicationDTO.getObjectVersionNumber());
            return applicationMapper.updateByPrimaryKeySelective(applicationDTO);
        }

        private ApplicationDTO queryByCodeFromBase (String code, Long projectId){
            ApplicationDTO applicationDO = new ApplicationDTO();
            applicationDO.setProjectId(projectId);
            applicationDO.setCode(code);
            return applicationMapper.selectOne(applicationDO);
        }
=======
>>>>>>> [IMP]重构后端代码
    }
<<<<<<< HEAD
}
>>>>>>> [IMP] 修改AppControler重构
=======
>>>>>>> [IMP]重构后端代码
