package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.kubernetes.client.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
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

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.sonar.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.harbor.ProjectDetail;
import io.choerodon.devops.infra.dto.harbor.User;
import io.choerodon.devops.infra.dto.iam.IamAppDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.AppShareClient;
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.devops.infra.mapper.ApplicationUserPermissionMapper;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.websocket.tool.UUIDTool;


/**
 * Created by younger on 2018/3/28.
 */
@Service
@EnableConfigurationProperties(HarborConfigurationProperties.class)
public class ApplicationServiceImpl implements ApplicationService {
    public static final String SEVERITIES = "severities";
    public static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    public static final String NODELETED = "nodeleted";
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
    private GitUtil gitUtil;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private UserAttrMapper userAttrMapper;
    @Autowired
    private ApplicationUserPermissionMapper applicationUserPermissionMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private IamService iamService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private GitlabUserService gitlabUserService;
    @Autowired
    private ApplicationTemplateService applicationTemplateService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ApplicationUserPermissionService applicationUserPermissionService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsProjectConfigService devopsProjectConfigService;
    @Autowired
    private ApplicationShareService applicationShareService;
    @Autowired
    private DevopsBranchService devopsBranchService;
    @Autowired
    private MarketConnectInfoService marketConnectInfoService;


    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_APPLICATION,
            description = "Devops创建应用", inputSchema = "{}")
    @Transactional
    public ApplicationRepVO create(Long projectId, ApplicationReqVO applicationReqVO) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplication(applicationReqVO);
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        // 查询创建应用所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = projectService.queryById(projectId);
        MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(
                TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrVO.getGitlabUserId()));
        if (memberDTO == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.value)) {
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
            userIds.forEach(e -> applicationUserPermissionService.baseCreate(e, appId));
        }

        IamAppDTO iamAppDTO = new IamAppDTO();
        iamAppDTO.setApplicationCategory(APPLICATION);
        iamAppDTO.setApplicationType(applicationReqVO.getType());
        iamAppDTO.setCode(applicationReqVO.getCode());
        iamAppDTO.setName(applicationReqVO.getName());
        iamAppDTO.setEnabled(true);
        iamAppDTO.setOrganizationId(organizationDTO.getId());
        iamAppDTO.setProjectId(projectId);
        iamAppDTO.setFrom(applicationName);

        iamService.createIamApp(organizationDTO.getId(), iamAppDTO);
        return ConvertUtils.convertObject(baseQueryByCode(applicationDTO.getCode(), applicationDTO.getProjectId()), ApplicationRepVO.class);
    }

    @Override
    public ApplicationRepVO query(Long projectId, Long applicationId) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(applicationId);
        ApplicationRepVO applicationRepVO = ConvertUtils.convertObject(applicationDTO, ApplicationRepVO.class);
        //url地址拼接
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        if (applicationDTO.getGitlabProjectId() != null) {
            applicationRepVO.setRepoUrl(gitlabUrl + urlSlash
                    + organizationDTO.getCode() + "-" + projectDTO.getCode() + "/"
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
    @Saga(code = SagaTopicCodeConstants.DEVOPS_APP_DELETE, description = "Devops删除失败应用", inputSchema = "{}")
    @Transactional
    public void delete(Long projectId, Long appId) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        //删除应用权限

        applicationUserPermissionService.baseDeleteByAppId(appId);
        //删除gitlab project
        ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(appId);
        if (applicationDTO.getGitlabProjectId() != null) {
            Integer gitlabProjectId = applicationDTO.getGitlabProjectId();
            GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectById(gitlabProjectId);
            if (gitlabProjectDTO != null && gitlabProjectDTO.getId() != null) {
                UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                Integer gitlabUserId = TypeUtil.objToInt(userAttrDTO.getGitlabUserId());
                gitlabServiceClientOperator.deleteProjectById(gitlabProjectId, gitlabUserId);
            }
        }
        //删除iam应用
        DevOpsAppSyncPayload appSyncPayload = new DevOpsAppSyncPayload();
        appSyncPayload.setProjectId(projectId);
        appSyncPayload.setOrganizationId(projectDTO.getOrganizationId());
        appSyncPayload.setCode(applicationDTO.getCode());
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_APP_DELETE),
                builder -> builder
                        .withPayloadAndSerialize(appSyncPayload)
                        .withRefId(String.valueOf(appId))
                        .withSourceId(projectId));
        applicationMapper.deleteByPrimaryKey(appId);
    }

    @Saga(code = SagaTopicCodeConstants.DEVOPS_UPDATE_GITLAB_USERS,
            description = "Devops更新gitlab用户", inputSchema = "{}")
    @Override
    @Transactional
    public Boolean update(Long projectId, ApplicationUpdateVO applicationUpdateVO) {

        ApplicationDTO applicationDTO = ConvertUtils.convertObject(applicationUpdateVO, ApplicationDTO.class);
        applicationDTO.setIsSkipCheckPermission(applicationUpdateVO.getIsSkipCheckPermission());
        applicationDTO.setProjectId(projectId);
        applicationDTO.setHarborConfigId(applicationUpdateVO.getHarborConfigId());
        applicationDTO.setChartConfigId(applicationUpdateVO.getChartConfigId());

        Long appId = applicationUpdateVO.getId();
        ApplicationDTO oldApplicationDTO = applicationMapper.selectByPrimaryKey(appId);

        if (!oldApplicationDTO.getName().equals(applicationUpdateVO.getName())) {
            baseCheckName(applicationDTO.getProjectId(), applicationDTO.getName());
        }
        if (baseUpdate(applicationDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_APP);
        }

        if (!oldApplicationDTO.getName().equals(applicationUpdateVO.getName())) {
            ProjectDTO projectDTO = iamService.queryIamProject(oldApplicationDTO.getProjectId());
            OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
            IamAppDTO iamAppDTO = iamService.queryIamAppByCode(organizationDTO.getId(), applicationDTO.getCode());
            iamAppDTO.setName(applicationUpdateVO.getName());
            iamService.updateIamApp(organizationDTO.getId(), iamAppDTO.getId(), iamAppDTO);
        }

        // 创建gitlabUserPayload
        DevOpsUserPayload devOpsUserPayload = new DevOpsUserPayload();
        devOpsUserPayload.setIamProjectId(projectId);
        devOpsUserPayload.setAppId(appId);
        devOpsUserPayload.setGitlabProjectId(oldApplicationDTO.getGitlabProjectId());
        devOpsUserPayload.setIamUserIds(applicationUpdateVO.getUserIds());

        if (oldApplicationDTO.getIsSkipCheckPermission() && applicationUpdateVO.getIsSkipCheckPermission()) {
            return false;
        } else if (oldApplicationDTO.getIsSkipCheckPermission() && !applicationUpdateVO.getIsSkipCheckPermission()) {
            applicationUpdateVO.getUserIds().forEach(e -> applicationUserPermissionService.baseCreate(e, appId));
            devOpsUserPayload.setOption(1);
        } else if (!oldApplicationDTO.getIsSkipCheckPermission() && applicationUpdateVO.getIsSkipCheckPermission()) {
            applicationUserPermissionService.baseDeleteByAppId(appId);
            devOpsUserPayload.setOption(2);
        } else {
            applicationUserPermissionService.baseDeleteByAppId(appId);
            applicationUpdateVO.getUserIds().forEach(e -> applicationUserPermissionService.baseCreate(e, appId));
            devOpsUserPayload.setOption(3);
        }
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_UPDATE_GITLAB_USERS),
                builder -> builder
                        .withPayloadAndSerialize(devOpsUserPayload)
                        .withRefId(String.valueOf(appId))
                        .withSourceId(projectId));
        return true;
    }


    @Saga(code = SagaTopicCodeConstants.DEVOPS_SYNC_APP_ACTIVE,
            description = "同步iam应用状态", inputSchema = "{}")
    @Override
    @Transactional
    public Boolean updateActive(Long appId, Boolean active) {
        ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(appId);
        applicationDTO.setActive(active);
        if (baseUpdate(applicationDTO) != 1) {
            throw new CommonException("error.application.active");
        }
        ProjectDTO projectDTO = iamService.queryIamProject(applicationDTO.getId());
        DevOpsAppSyncPayload opsAppSyncPayload = new DevOpsAppSyncPayload();
        opsAppSyncPayload.setActive(active);
        opsAppSyncPayload.setOrganizationId(projectDTO.getOrganizationId());
        opsAppSyncPayload.setProjectId(applicationDTO.getId());
        opsAppSyncPayload.setCode(applicationDTO.getCode());
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_SYNC_APP_ACTIVE),
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
        PageInfo<ApplicationDTO> applicationDTOS = basePageByOptions(projectId, isActive, hasVersion, appMarket, type, doPage, pageRequest, params);
        UserAttrDTO userAttrDTO = userAttrMapper.selectByPrimaryKey(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectDTO, organizationDTO, applicationDTOS.getList(), urlSlash);

        PageInfo<ApplicationRepVO> resultDTOPage = ConvertUtils.convertPage(applicationDTOS, this::dtoToRepVo);
        resultDTOPage.setList(setApplicationRepVOPermission(applicationDTOS.getList(), userAttrDTO, projectDTO));
        return resultDTOPage;
    }

    @Override
    public PageInfo<ApplicationRepVO> pageByOptionsAppMarket(Long projectId, Boolean isActive, Boolean hasVersion,
                                                             Boolean appMarket,
                                                             String type, Boolean doPage,
                                                             PageRequest pageRequest, String params) {
        PageInfo<ApplicationDTO> applicationDTOS = basePageByOptions(projectId, isActive, hasVersion, appMarket, type, doPage, pageRequest, params);
        return ConvertUtils.convertPage(applicationDTOS, this::dtoToRepVo);
    }

    @Override
    public PageInfo<ApplicationRepVO> pageCodeRepository(Long projectId, PageRequest pageRequest, String params) {

        UserAttrDTO userAttrDTO = userAttrMapper.selectByPrimaryKey(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        Boolean isProjectOwner = iamService.isProjectOwner(userAttrDTO.getIamUserId(), projectDTO);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());

        Map maps = gson.fromJson(params, Map.class);
        PageInfo<ApplicationDTO> applicationDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.listCodeRepository(projectId,
                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(maps.get(TypeUtil.PARAM)), isProjectOwner, userAttrDTO.getIamUserId()));
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectDTO, organizationDTO, applicationDTOPageInfo.getList(), urlSlash);

        return ConvertUtils.convertPage(applicationDTOPageInfo, ApplicationRepVO.class);
    }

    @Override
    public List<ApplicationRepVO> listByActive(Long projectId) {
        List<ApplicationDTO> applicationDTOList = baseListByActive(projectId);
        UserAttrDTO userAttrDTO = userAttrMapper.selectByPrimaryKey(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectDTO, organizationDTO, applicationDTOList, urlSlash);

        return setApplicationRepVOPermission(applicationDTOList, userAttrDTO, projectDTO);
    }

    @Override
    public List<ApplicationRepVO> listAll(Long projectId) {
        return ConvertUtils.convertList(baseListAll(projectId), ApplicationRepVO.class);
    }

    @Override
    public void checkName(Long projectId, String name) {
        baseCheckName(projectId, name);
    }

    @Override
    public void checkCode(Long projectId, String code) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setProjectId(projectId);
        applicationDTO.setCode(code);
        baseCheckCode(applicationDTO);
    }

    @Override
    public List<ApplicationTemplateRespVO> listTemplate(Long projectId, Boolean isPredefined) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        List<ApplicationTemplateDTO> applicationTemplateDTOS = applicationTemplateService.baseListByOrganizationId(projectDTO.getOrganizationId())
                .stream()
                .filter(ApplicationTemplateDTO::getSynchro).collect(Collectors.toList());
        if (isPredefined != null && isPredefined) {
            applicationTemplateDTOS = applicationTemplateDTOS.stream().filter(applicationTemplateDTO -> applicationTemplateDTO.getOrganizationId() == null).collect(Collectors.toList());
        }
        return ConvertUtils.convertList(applicationTemplateDTOS, ApplicationTemplateRespVO.class);
    }

    @Override
    public void operationApplication(DevOpsAppPayload gitlabProjectPayload) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByGitlabAppGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        ApplicationDTO applicationDTO = baseQueryByCode(gitlabProjectPayload.getPath(),
                devopsProjectDTO.getIamProjectId());
        ProjectDTO projectDTO = iamService.queryIamProject(devopsProjectDTO.getIamProjectId());
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator
                .queryProjectByName(organizationDTO.getCode() + "-" + projectDTO.getCode(), applicationDTO.getCode(),
                        gitlabProjectPayload.getUserId());
        Integer gitlabProjectId = gitlabProjectDO.getId();
        if (gitlabProjectId == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(),
                    gitlabProjectPayload.getUserId(), false);
        }
        gitlabProjectPayload.setGitlabProjectId(gitlabProjectDO.getId());

        // 为项目下的成员分配对于此gitlab项目的权限

        operateGitlabMemberPermission(gitlabProjectPayload);

        if (applicationDTO.getAppTemplateId() != null) {
            ApplicationTemplateDTO applicationTemplateDTO = applicationTemplateService.baseQuery(applicationDTO.getAppTemplateId());
            //拉取模板
            String applicationDir = APPLICATION + System.currentTimeMillis();
            Git git = cloneTemplate(applicationTemplateDTO, applicationDir);
            //渲染模板里面的参数
            replaceParams(applicationDTO, projectDTO, organizationDTO, applicationDir);

            UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));

            // 获取push代码所需的access token
            String accessToken = getToken(gitlabProjectPayload, applicationDir, userAttrDTO);

            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            applicationDTO.setGitlabProjectUrl(repoUrl + organizationDTO.getCode()
                    + "-" + projectDTO.getCode() + "/" + applicationDTO.getCode() + ".git");
            GitLabUserDTO gitlabUserDTO = gitlabUserService.getGitlabUserByUserId(gitlabProjectPayload.getUserId());

            BranchDTO branchDTO = devopsGitService.baseQueryBranch(gitlabProjectDO.getId(), MASTER);
            if (branchDTO.getName() == null) {
                gitUtil.push(git, applicationDir, applicationDTO.getGitlabProjectUrl(),
                        gitlabUserDTO.getUsername(), accessToken);
                branchDTO = devopsGitService.baseQueryBranch(gitlabProjectDO.getId(), MASTER);
                //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                if (!branchDTO.getProtected()) {
                    try {
                        gitlabServiceClientOperator.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
                                AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                                gitlabProjectPayload.getUserId());
                    } catch (CommonException e) {
                        branchDTO = devopsGitService.baseQueryBranch(gitlabProjectDO.getId(), MASTER);
                        if (!branchDTO.getProtected()) {
                            throw new CommonException(e);
                        }
                    }
                }
            } else {
                if (!branchDTO.getProtected()) {
                    gitlabServiceClientOperator.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
                            AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                            gitlabProjectPayload.getUserId());
                }
            }
            initBranch(gitlabProjectPayload, applicationDTO, MASTER);
        }
        try {
            String applicationToken = getApplicationToken(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId());
            applicationDTO.setToken(applicationToken);
            applicationDTO.setGitlabProjectId(TypeUtil.objToInteger(gitlabProjectPayload.getGitlabProjectId()));
            applicationDTO.setSynchro(true);
            applicationDTO.setFailed(false);
            // set project hook id for application
            setProjectHook(applicationDTO, gitlabProjectDO.getId(), applicationToken, gitlabProjectPayload.getUserId());
            // 更新并校验
            baseUpdate(applicationDTO);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public void operationApplicationImport(DevOpsAppImportPayload devOpsAppImportPayload) {
        // 准备相关的数据
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByGitlabAppGroupId(
                TypeUtil.objToInteger(devOpsAppImportPayload.getGroupId()));
        ApplicationDTO applicationDTO = baseQueryByCode(devOpsAppImportPayload.getPath(),
                devopsProjectDTO.getIamProjectId());
        ProjectDTO projectDTO = iamService.queryIamProject(devopsProjectDTO.getIamProjectId());
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator
                .queryProjectByName(organizationDTO.getCode() + "-" + projectDTO.getCode(), applicationDTO.getCode(),
                        devOpsAppImportPayload.getUserId());
        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(devOpsAppImportPayload.getGroupId(),
                    devOpsAppImportPayload.getPath(),
                    devOpsAppImportPayload.getUserId(), false);
        }
        devOpsAppImportPayload.setGitlabProjectId(gitlabProjectDO.getId());

        // 为项目下的成员分配对于此gitlab项目的权限
        operateGitlabMemberPermission(devOpsAppImportPayload);

        if (applicationDTO.getAppTemplateId() != null) {
            UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(devOpsAppImportPayload.getUserId()));
            ApplicationTemplateDTO applicationTemplateDTO = applicationTemplateService.baseQuery(
                    applicationDTO.getAppTemplateId());

            // 拉取模板
            String templateDir = APPLICATION + UUIDTool.genUuid();
            Git templateGit = cloneTemplate(applicationTemplateDTO, templateDir);
            // 渲染模板里面的参数
            replaceParams(applicationDTO, projectDTO, organizationDTO, templateDir);

            // clone外部代码仓库
            String applicationDir = APPLICATION + UUIDTool.genUuid();
            Git repositoryGit = gitUtil.cloneRepository(applicationDir, devOpsAppImportPayload.getRepositoryUrl(), devOpsAppImportPayload.getAccessToken());


            // 设置Application对应的gitlab项目的仓库地址
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            applicationDTO.setRepoUrl(repoUrl + organizationDTO.getCode()
                    + "-" + projectDTO.getCode() + "/" + applicationDTO.getCode() + ".git");

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


                    // 将模板库中文件复制到代码库中
                    mergeTemplateToApplication(templateWorkDir, applicationWorkDir, applicationTemplateDTO.getId());

                    // 获取push代码所需的access token
                    String accessToken = getToken(devOpsAppImportPayload, applicationDir, userAttrDTO);

                    BranchDTO branchDTO = gitlabServiceClientOperator.queryBranch(gitlabProjectDO.getId(), branchName);
                    if (branchDTO.getName() == null) {
                        try {
                            // 提交并推代码
                            gitUtil.commitAndPush(repositoryGit, applicationDTO.getGitlabProjectUrl(), accessToken, ref.getName());
                        } catch (CommonException e) {
                            releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
                            throw e;
                        }

                        branchDTO = gitlabServiceClientOperator.queryBranch(gitlabProjectDO.getId(), branchName);
                        //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                        if (branchName.equals(MASTER)) {
                            if (!branchDTO.getProtected()) {
                                try {
                                    gitlabServiceClientOperator.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER, AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(), devOpsAppImportPayload.getUserId());
                                } catch (CommonException e) {
                                    if (!gitlabServiceClientOperator.queryBranch(gitlabProjectDO.getId(), MASTER).getProtected()) {
                                        throw new CommonException(e);
                                    }
                                }
                            }
                        }
                    } else {
                        if (branchName.equals(MASTER)) {
                            if (!branchDTO.getProtected()) {
                                gitlabServiceClientOperator.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER,
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
            applicationDTO.setGitlabProjectId(TypeUtil.objToInteger(devOpsAppImportPayload.getGitlabProjectId()));
            applicationDTO.setToken(applicationToken);
            applicationDTO.setSynchro(true);

            // set project hook id for application
            setProjectHook(applicationDTO, gitlabProjectDO.getId(), applicationToken, devOpsAppImportPayload.getUserId());

            // 更新并校验
            if (baseUpdate(applicationDTO) != 1) {
                throw new CommonException(ERROR_UPDATE_APP);
            }
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_APP_FAIL,
            description = "Devops设置application状态为创建失败(devops set app status create err)", inputSchema = "{}")
    public void setAppErrStatus(String input, Long projectId) {
        GitlabProjectEventVO gitlabProjectEventVO = JSONObject.parseObject(input, GitlabProjectEventVO.class);
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_APP_FAIL),
                builder -> builder
                        .withPayloadAndSerialize(gitlabProjectEventVO)
                        .withRefId("")
                        .withSourceId(projectId));
    }

    @Override
    public String queryFile(String token, String type) {
        ApplicationDTO applicationDTO = baseQueryByToken(token);
        if (applicationDTO == null) {
            return null;
        }
        try {
            ProjectDTO projectDTO = iamService.queryIamProject(applicationDTO.getProjectId());
            OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
            InputStream inputStream;
            ProjectConfigVO harborProjectConfig;
            ProjectConfigVO chartProjectConfig;
            if (applicationDTO.getHarborConfigId() != null) {
                harborProjectConfig = gson.fromJson(devopsProjectConfigService.baseQuery(applicationDTO.getHarborConfigId()).getConfig(), ProjectConfigVO.class);
            } else {
                harborProjectConfig = gson.fromJson(devopsProjectConfigService.baseListByIdAndType(null, ProjectConfigType.HARBOR.getType()).get(0).getConfig(), ProjectConfigVO.class);
            }
            if (applicationDTO.getChartConfigId() != null) {
                chartProjectConfig = gson.fromJson(devopsProjectConfigService.baseQuery(applicationDTO.getChartConfigId()).getConfig(), ProjectConfigVO.class);
            } else {
                chartProjectConfig = gson.fromJson(devopsProjectConfigService.baseListByIdAndType(null, ProjectConfigType.CHART.getType()).get(0).getConfig(), ProjectConfigVO.class);
            }
            if (type == null) {
                inputStream = this.getClass().getResourceAsStream("/shell/ci.sh");
            } else {
                inputStream = this.getClass().getResourceAsStream("/shell/" + type + ".sh");
            }
            Map<String, String> params = new HashMap<>();
            String groupName = organizationDTO.getCode() + "-" + projectDTO.getCode();
            if (harborProjectConfig.getProject() != null) {
                groupName = harborProjectConfig.getProject();
            }
            String dockerUrl = harborProjectConfig.getUrl().replace("http://", "").replace("https://", "");
            dockerUrl = dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;

            params.put("{{ GROUP_NAME }}", groupName);
            params.put("{{ PROJECT_NAME }}", applicationDTO.getCode());
            params.put("{{ PRO_CODE }}", projectDTO.getCode());
            params.put("{{ ORG_CODE }}", organizationDTO.getCode());
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
    public List<ApplicationCodeVO> listByEnvId(Long projectId, Long envId, String status, Long appId) {
        List<ApplicationCodeVO> applicationCodeVOS = ConvertUtils
                .convertList(baseListByEnvId(projectId, envId, status),
                        ApplicationCodeVO.class);
        if (appId != null) {
            ApplicationDTO applicationDTO = baseQuery(appId);
            ApplicationCodeVO applicationCodeVO = new ApplicationCodeVO();
            BeanUtils.copyProperties(applicationDTO, applicationCodeVO);
            ApplicationShareDTO applicationShareDTO = applicationShareService.baseQueryByAppId(appId);
            if (applicationShareDTO != null) {
                applicationCodeVO.setPublishLevel(applicationShareDTO.getPublishLevel());
                applicationCodeVO.setContributor(applicationShareDTO.getContributor());
                applicationCodeVO.setDescription(applicationShareDTO.getDescription());
            }
            for (int i = 0; i < applicationCodeVOS.size(); i++) {
                if (applicationCodeVOS.get(i).getId().equals(applicationDTO.getId())) {
                    applicationCodeVOS.remove(applicationCodeVOS.get(i));
                }
            }
            applicationCodeVOS.add(0, applicationCodeVO);
        }
        return applicationCodeVOS;
    }

    @Override
    public PageInfo<ApplicationCodeVO> pageByIds(Long projectId, Long envId, Long appId, PageRequest pageRequest) {
        return ConvertUtils.convertPage(basePageByEnvId(projectId, envId, appId, pageRequest),
                ApplicationCodeVO.class);
    }

    @Override
    public PageInfo<ApplicationReqVO> pageByActiveAndPubAndVersion(Long projectId, PageRequest pageRequest,
                                                                   String params) {
        return ConvertUtils.convertPage(basePageByActiveAndPubAndHasVersion(projectId, true, pageRequest, params), ApplicationReqVO.class);
    }

    @Override
    public List<AppUserPermissionRespVO> listAllUserPermission(Long appId) {
        List<Long> userIds = applicationUserPermissionService.baseListByAppId(appId).stream().map(ApplicationUserPermissionDTO::getIamUserId)
                .collect(Collectors.toList());
        List<IamUserDTO> userEList = iamService.listUsersByIds(userIds);
        List<AppUserPermissionRespVO> resultList = new ArrayList<>();
        userEList.forEach(
                e -> resultList.add(new AppUserPermissionRespVO(e.getId(), e.getLoginName(), e.getRealName())));
        return resultList;
    }

    @Override
    public Boolean validateRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String
            accessToken) {
        if (!REPOSITORY_URL_PATTERN.matcher(repositoryUrl).matches()) {
            return Boolean.FALSE;
        }

        // 当不存在access_token时，默认将仓库识别为公开的
        return GitUtil.validRepositoryUrl(repositoryUrl, accessToken);
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_IMPORT_GITLAB_PROJECT, description = "Devops从外部代码平台导入到gitlab项目", inputSchema = "{}")
    public ApplicationRepVO importApp(Long projectId, ApplicationImportVO applicationImportVO) {
        // 获取当前操作的用户的信息
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验application信息的格式
        ApplicationValidator.checkApplication(applicationImportVO);

        // 校验名称唯一性
        baseCheckName(projectId, applicationImportVO.getName());

        // 校验code唯一性
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setProjectId(projectId);
        applicationDTO.setCode(applicationImportVO.getCode());
        baseCheckCode(applicationDTO);

        // 校验repository（和token） 地址是否有效
        GitPlatformType gitPlatformType = GitPlatformType.from(applicationImportVO.getPlatformType());
        checkRepositoryUrlAndToken(gitPlatformType, applicationImportVO.getRepositoryUrl(), applicationImportVO.getAccessToken());

        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());

        applicationDTO = fromImportDtoToEntity(applicationImportVO);

        applicationDTO.setProjectId(projectId);
        applicationDTO.setActive(true);
        applicationDTO.setSynchro(false);
        applicationDTO.setIsSkipCheckPermission(applicationImportVO.getIsSkipCheckPermission());
        applicationDTO.setHarborConfigId(applicationImportVO.getHarborConfigId());
        applicationDTO.setChartConfigId(applicationImportVO.getChartConfigId());

        // 查询创建应用所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(applicationDTO.getProjectId());
        MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(
                TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));

        // 校验用户的gitlab权限
        if (memberDTO == null || memberDTO.getAccessLevel().equals(AccessLevel.OWNER.toValue())) {
            throw new CommonException("error.user.not.owner");
        }

        // 创建应用
        applicationDTO = baseCreate(applicationDTO);
        Long appId = applicationDTO.getId();

        IamAppDTO iamAppDTO = new IamAppDTO();
        iamAppDTO.setApplicationCategory(APPLICATION);
        iamAppDTO.setApplicationType(applicationImportVO.getType());
        iamAppDTO.setCode(applicationImportVO.getCode());
        iamAppDTO.setName(applicationImportVO.getName());
        iamAppDTO.setEnabled(true);
        iamAppDTO.setOrganizationId(organizationDTO.getId());
        iamAppDTO.setProjectId(projectId);
        iamAppDTO.setFrom(applicationName);
        //iam创建应用
        iamService.createIamApp(organizationDTO.getId(), iamAppDTO);

        // 创建saga payload
        DevOpsAppImportPayload devOpsAppImportPayload = new DevOpsAppImportPayload();
        devOpsAppImportPayload.setType(APPLICATION);
        devOpsAppImportPayload.setPath(applicationImportVO.getCode());
        devOpsAppImportPayload.setOrganizationId(organizationDTO.getId());
        devOpsAppImportPayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        devOpsAppImportPayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
        devOpsAppImportPayload.setUserIds(applicationImportVO.getUserIds());
        devOpsAppImportPayload.setSkipCheckPermission(applicationImportVO.getIsSkipCheckPermission());
        devOpsAppImportPayload.setAppId(appId);
        devOpsAppImportPayload.setIamProjectId(projectId);
        devOpsAppImportPayload.setPlatformType(gitPlatformType);
        devOpsAppImportPayload.setRepositoryUrl(applicationImportVO.getRepositoryUrl());
        devOpsAppImportPayload.setAccessToken(applicationImportVO.getAccessToken());
        devOpsAppImportPayload.setGitlabUserId(userAttrDTO.getGitlabUserId());

        // 如果不跳过权限检查
        List<Long> userIds = applicationImportVO.getUserIds();
        if (!applicationImportVO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> applicationUserPermissionService.baseCreate(e, appId));
        }

        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_IMPORT_GITLAB_PROJECT),
                builder -> builder
                        .withPayloadAndSerialize(devOpsAppImportPayload)
                        .withRefId("")
                        .withSourceId(projectId));
        return ConvertUtils.convertObject(baseQuery(appId), ApplicationRepVO.class);
    }

    @Override
    public ApplicationRepVO queryByCode(Long projectId, String code) {
        return ConvertUtils.convertObject(baseQueryByCode(code, projectId), ApplicationRepVO.class);
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_GITLAB_PROJECT,
            description = "Devops创建gitlab项目", inputSchema = "{}")
    public void createIamApplication(IamAppPayLoad iamAppPayLoad) {

        List<Long> userIds = new ArrayList<>();
        ApplicationDTO applicationDTO = baseQueryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        if (applicationDTO == null) {
            applicationDTO = new ApplicationDTO();
            applicationDTO.setIsSkipCheckPermission(true);
            applicationDTO.setName(iamAppPayLoad.getName());
            applicationDTO.setCode(iamAppPayLoad.getCode());
            applicationDTO.setActive(true);
            applicationDTO.setSynchro(false);
            applicationDTO.setProjectId(iamAppPayLoad.getProjectId());
            applicationDTO.setType("normal");
            if (iamAppPayLoad.getApplicationType().equals(TEST)) {
                applicationDTO.setType("test");
            }
            applicationDTO = baseCreate(applicationDTO);
        } else {
            //创建iam入口过来的应用直接跳过权限校验，从devops入口过来的应用选择了特定用户权限，需要给特定用户分配该用户权限
            if (!applicationDTO.getIsSkipCheckPermission()) {
                userIds = applicationUserPermissionService.baseListByAppId(applicationDTO.getId()).stream().map(ApplicationUserPermissionDTO::getIamUserId).collect(Collectors.toList());
            }
        }

        //创建iam入口过来的应用直接用管理员去gitlab创建对应的project,避免没有对应项目的权限导致创建失败
        Long gitlabUserId = 1L;
        if (applicationName.equals(iamAppPayLoad.getFrom())) {
            UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            gitlabUserId = userAttrDTO.getGitlabUserId();
        }

        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(iamAppPayLoad.getProjectId());

        //创建saga payload
        DevOpsAppPayload devOpsAppPayload = new DevOpsAppPayload();
        devOpsAppPayload.setType(APPLICATION);
        devOpsAppPayload.setPath(iamAppPayLoad.getCode());
        devOpsAppPayload.setOrganizationId(iamAppPayLoad.getOrganizationId());
        devOpsAppPayload.setUserId(TypeUtil.objToInteger(gitlabUserId));
        devOpsAppPayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
        devOpsAppPayload.setUserIds(userIds);
        devOpsAppPayload.setSkipCheckPermission(applicationDTO.getIsSkipCheckPermission());
        devOpsAppPayload.setAppId(applicationDTO.getId());
        devOpsAppPayload.setIamProjectId(iamAppPayLoad.getProjectId());
        //0.14.0-0.15.0的时候，同步已有的app到iam，此时app已经存在gitlab project,不需要再创建
        if (applicationDTO.getGitlabProjectId() == null) {
            producer.applyAndReturn(
                    StartSagaBuilder
                            .newBuilder()
                            .withLevel(ResourceLevel.PROJECT)
                            .withRefType("")
                            .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_GITLAB_PROJECT),
                    builder -> builder
                            .withPayloadAndSerialize(devOpsAppPayload)
                            .withRefId("")
                            .withSourceId(iamAppPayLoad.getProjectId()));
        }
    }

    @Override
    public void updateIamApplication(IamAppPayLoad iamAppPayLoad) {
        ApplicationDTO applicationDTO = baseQueryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        applicationDTO.setName(iamAppPayLoad.getName());
        baseUpdate(applicationDTO);
    }

    @Override
    public void deleteIamApplication(IamAppPayLoad iamAppPayLoad) {
        ApplicationDTO applicationDTO = baseQueryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        if (applicationDTO.getGitlabProjectId() != null) {
            gitlabServiceClientOperator.deleteProjectById(applicationDTO.getGitlabProjectId(), 1);
        }
        baseDelete(applicationDTO.getId());
    }

    @Override
    public Boolean checkHarbor(String url, String userName, String password, String project, String email) {
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
    public Boolean checkChart(String url) {
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

    @Override
    public SonarContentsVO getSonarContent(Long projectId, Long appId) {

        //没有使用sonarqube直接返回空对象
        if (sonarqubeUrl.equals("")) {
            return new SonarContentsVO();
        }
        SonarContentsVO sonarContentsVO = new SonarContentsVO();
        List<SonarContentVO> sonarContentVOS = new ArrayList<>();
        ApplicationDTO applicationDTO = baseQuery(appId);
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organization = iamService.queryOrganizationById(projectDTO.getOrganizationId());


        //初始化sonarClient
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = String.format("%s-%s:%s", organization.getCode(), projectDTO.getCode(), applicationDTO.getCode());
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
                    return new SonarContentsVO();
                }
                if (sonarComponentResponse.raw().code() == 401) {
                    throw new CommonException("error.sonarqube.user");
                }
                throw new CommonException(sonarComponentResponse.errorBody().string());
            }
            if (sonarComponentResponse.body() == null) {
                return new SonarContentsVO();
            }
            if (sonarComponentResponse.body().getPeriods() != null && sonarComponentResponse.body().getPeriods().size() > 0) {
                sonarContentsVO.setDate(sonarComponentResponse.body().getPeriods().get(0).getDate());
                sonarContentsVO.setMode(sonarComponentResponse.body().getPeriods().get(0).getMode());
                sonarContentsVO.setParameter(sonarComponentResponse.body().getPeriods().get(0).getParameter());
            } else {
                Map<String, String> analyseMap = new HashMap<>();
                analyseMap.put("project", key);
                analyseMap.put("ps", "3");

                //查询上一次的分析时间
                Response<SonarAnalyses> sonarAnalyses = sonarClient.getAnalyses(analyseMap).execute();
                if (sonarAnalyses.raw().code() == 200 && sonarAnalyses.body().getAnalyses() != null && sonarAnalyses.body().getAnalyses().size() > 0) {
                    sonarContentsVO.setDate(sonarAnalyses.body().getAnalyses().get(0).getDate());
                }
            }

            //分类型对sonarqube project查询返回的结果进行处理
            sonarComponentResponse.body().getComponent().getMeasures().stream().forEach(measure -> {
                SonarQubeType sonarQubeType = SonarQubeType.forValue(String.valueOf(measure.getMetric()));
                switch (sonarQubeType) {
                    case BUGS:
                        SonarContentVO bug = new SonarContentVO();
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
                        sonarContentVOS.add(bug);
                        break;
                    case VULNERABILITIES:
                        SonarContentVO vulnerabilities = new SonarContentVO();
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
                        sonarContentVOS.add(vulnerabilities);
                        break;
                    case NEW_BUGS:
                        SonarContentVO newBug = new SonarContentVO();
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
                        sonarContentVOS.add(newBug);
                        break;
                    case NEW_VULNERABILITIES:
                        SonarContentVO newVulnerabilities = new SonarContentVO();
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
                        sonarContentVOS.add(newVulnerabilities);
                        break;
                    case SQALE_INDEX:
                        SonarContentVO debt = new SonarContentVO();
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
                        sonarContentVOS.add(debt);
                        break;
                    case CODE_SMELLS:
                        SonarContentVO codeSmells = new SonarContentVO();
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
                        sonarContentVOS.add(codeSmells);
                        break;
                    case NEW_TECHNICAL_DEBT:
                        SonarContentVO newDebt = new SonarContentVO();
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
                        sonarContentVOS.add(newDebt);
                        break;
                    case NEW_CODE_SMELLS:
                        SonarContentVO newCodeSmells = new SonarContentVO();
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
                        sonarContentVOS.add(newCodeSmells);
                        break;
                    case COVERAGE:
                        SonarContentVO coverage = new SonarContentVO();
                        coverage.setKey(measure.getMetric());
                        coverage.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        coverage.setUrl(String.format("%scomponent_measures?id=%s&metric=coverage", sonarqubeUrl, key));
                        sonarContentVOS.add(coverage);
                        break;
                    case NEW_COVERAGE:
                        SonarContentVO newCoverage = new SonarContentVO();
                        newCoverage.setKey(measure.getMetric());
                        BigDecimal codeSmellDecimal = new BigDecimal(measure.getPeriods().get(0).getValue());
                        newCoverage.setValue(String.format("%s", codeSmellDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                        newCoverage.setUrl(String.format("%scomponent_measures?id=%s&metric=new_coverage", sonarqubeUrl, key));
                        sonarContentVOS.add(newCoverage);
                        break;
                    case DUPLICATED_LINES_DENSITY:
                        SonarContentVO duplicated = new SonarContentVO();
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
                        sonarContentVOS.add(duplicated);
                        break;
                    case DUPLICATED_BLOCKS:
                        SonarContentVO duplicatedBlocks = new SonarContentVO();
                        duplicatedBlocks.setKey(measure.getMetric());
                        duplicatedBlocks.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        duplicatedBlocks.setUrl(String.format("%scomponent_measures?id=%s&metric=duplicated_blocks", sonarqubeUrl, key));
                        sonarContentVOS.add(duplicatedBlocks);
                        break;
                    case NEW_DUPLICATED_LINES_DENSITY:
                        SonarContentVO newDuplicated = new SonarContentVO();
                        newDuplicated.setKey(measure.getMetric());
                        if (TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()) == 0) {
                            newDuplicated.setValue("0");
                        } else {
                            BigDecimal b = new BigDecimal(TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()));
                            newDuplicated.setValue(TypeUtil.objToString(b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                        }
                        newDuplicated.setUrl(String.format("%scomponent_measures?id=%s&metric=new_duplicated_lines_density", sonarqubeUrl, key));
                        sonarContentVOS.add(newDuplicated);
                        break;
                    case NCLOC:
                        SonarContentVO ncloc = new SonarContentVO();
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
                        sonarContentVOS.add(ncloc);
                        break;
                    case TESTS:
                        SonarContentVO test = new SonarContentVO();
                        test.setKey(measure.getMetric());
                        test.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        test.setUrl(String.format("%scomponent_measures?id=%s&metric=tests", sonarqubeUrl, key));
                        sonarContentVOS.add(test);
                        break;
                    case NCLOC_LANGUAGE_DISTRIBUTION:
                        SonarContentVO nclocLanguage = new SonarContentVO();
                        nclocLanguage.setKey(measure.getMetric());
                        nclocLanguage.setValue(measure.getValue());
                        sonarContentVOS.add(nclocLanguage);
                        break;
                    case QUALITY_GATE_DETAILS:
                        Quality quality = gson.fromJson(measure.getValue(), Quality.class);
                        sonarContentsVO.setStatus(quality.getLevel());
                        break;
                    default:
                        break;
                }
            });
            sonarContentsVO.setSonarContents(sonarContentVOS);
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return sonarContentsVO;
    }

    @Override
    public SonarTableVO getSonarTable(Long projectId, Long appId, String type, Date startTime, Date endTime) {
        if (sonarqubeUrl.equals("")) {
            return new SonarTableVO();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(endTime);
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = c.getTime();
        SonarTableVO sonarTableVO = new SonarTableVO();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
        ApplicationDTO applicationDTO = baseQuery(appId);
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = String.format("%s-%s:%s", organizationDTO.getCode(), projectDTO.getCode(), applicationDTO.getCode());
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
                        return new SonarTableVO();
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
                        sonarTableVO.setDates(dates);
                        sonarTableVO.setBugs(bugs);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.CODE_SMELLS.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            codeSmells.add(sonarHistroy.getValue());
                        });
                        sonarTableVO.setCodeSmells(codeSmells);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.VULNERABILITIES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            vulnerabilities.add(sonarHistroy.getValue());
                        });
                        sonarTableVO.setVulnerabilities(vulnerabilities);
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
                        return new SonarTableVO();
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
                        sonarTableVO.setCoverage(coverage);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.LINES_TO_COVER.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            linesToCover.add(sonarHistroy.getValue());
                            dates.add(sonarHistroy.getDate());
                        });
                        sonarTableVO.setDates(dates);
                        sonarTableVO.setLinesToCover(linesToCover);
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
                sonarTableVO.setCoverLines(coverLines);
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
                        return new SonarTableVO();
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
                        sonarTableVO.setNclocs(nclocs);
                        sonarTableVO.setDates(dates);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy ->
                                duplicatedLines.add(sonarHistroy.getValue())
                        );
                        sonarTableVO.setDuplicatedLines(duplicatedLines);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES_DENSITY.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            duplicatedLinesRate.add(sonarHistroy.getValue());
                        });
                        sonarTableVO.setDuplicatedLinesRate(duplicatedLinesRate);
                    }
                });
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }
        return sonarTableVO;
    }


    @Override
    public PageInfo<RemoteApplicationVO> pageRemoteApps(Long projectId, PageRequest pageRequest, String params) {
        DevopsMarketConnectInfoDTO marketConnectInfoDO = marketConnectInfoService.baseQuery();
        if (marketConnectInfoDO == null) {
            throw new CommonException("not.exist.remote token");
        }
        AppShareClient shareClient = RetrofitHandler.getAppShareClient(marketConnectInfoDO.getSaasMarketUrl());
        Map<String, Object> map = new HashMap<>();
        map.put("page", pageRequest.getPage());
        map.put("size", pageRequest.getSize());
        map.put("sort", PageRequestUtil.getOrderByStr(pageRequest));
        map.put("access_token", marketConnectInfoDO.getAccessToken());
        Response<PageInfo<RemoteApplicationVO>> pageInfoResponse = null;
        try {
            map.put("params", URLEncoder.encode(params, "UTF-8"));
            pageInfoResponse = shareClient.getAppShares(map).execute();
            if (!pageInfoResponse.isSuccessful()) {
                throw new CommonException("error.get.app.shares");
            }
        } catch (IOException e) {
            throw new CommonException("error.get.app.shares");
        }
        return pageInfoResponse.body();
    }

    @Override
    public void baseCheckApp(Long projectId, Long appId) {
        ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(appId);
        if (applicationDTO == null || !applicationDTO.getProjectId().equals(projectId)) {
            throw new CommonException("error.app.project.notMatch");
        }
    }

    @Override
    public int baseUpdate(ApplicationDTO applicationDTO) {
        ApplicationDTO oldApplicationDTO = applicationMapper.selectByPrimaryKey(applicationDTO.getId());
        if (applicationDTO.getFailed() != null && !applicationDTO.getFailed()) {
            applicationMapper.updateAppToSuccess(applicationDTO.getId());
        }
        applicationDTO.setObjectVersionNumber(oldApplicationDTO.getObjectVersionNumber());
        return applicationMapper.updateByPrimaryKeySelective(applicationDTO);
    }

    @Override
    public void updateApplicationStatus(ApplicationDTO applicationDTO) {
        applicationMapper.updateApplicationStatus(applicationDTO.getId(), applicationDTO.getToken(),
                applicationDTO.getGitlabProjectId(), applicationDTO.getHookId(), applicationDTO.getSynchro());
    }

    @Override
    public ApplicationDTO baseQuery(Long applicationId) {
        return applicationMapper.selectByPrimaryKey(applicationId);
    }

    @Override
    public PageInfo<ApplicationDTO> basePageByOptions(Long projectId, Boolean isActive, Boolean hasVersion, Boolean
            appMarket, String type, Boolean doPage, PageRequest pageRequest, String params) {
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
    }

    @Override
    public PageInfo<ApplicationDTO> basePageCodeRepository(Long projectId, PageRequest pageRequest, String params,
                                                           Boolean isProjectOwner, Long userId) {
        Map maps = gson.fromJson(params, Map.class);
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.listCodeRepository(projectId,
                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(maps.get(TypeUtil.PARAM)), isProjectOwner, userId));
    }

    @Override
    public ApplicationDTO baseQueryByCode(String code, Long projectId) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setProjectId(projectId);
        applicationDTO.setCode(code);
        return applicationMapper.selectOne(applicationDTO);
    }

    @Override
    public ApplicationDTO baseQueryByCodeWithNullProject(String code) {
        return applicationMapper.queryByCodeWithNoProject(code);
    }

    @Override
    public List<ApplicationDTO> baseListByEnvId(Long projectId, Long envId, String status) {
        return applicationMapper.listByEnvId(projectId, envId, null, status);
    }

    @Override
    public PageInfo<ApplicationDTO> basePageByEnvId(Long projectId, Long envId, Long appId, PageRequest pageRequest) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.listByEnvId(projectId, envId, appId, NODELETED));

    }

    @Override
    public List<ApplicationDTO> baseListByActive(Long projectId) {
        return applicationMapper.listByActive(projectId);
    }

    @Override
    public List<ApplicationDTO> baseListDeployedApp(Long projectId) {
        return applicationMapper.listDeployedApp(projectId);
    }

    @Override
    public PageInfo<ApplicationDTO> basePageByActiveAndPubAndHasVersion(Long projectId, Boolean isActive,
                                                                        PageRequest pageRequest, String params) {
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

    @Override
    public ApplicationDTO baseQueryByToken(String token) {
        return applicationMapper.queryByToken(token);
    }

    @Override
    public void baseCheckAppCanDisable(Long applicationId) {
        if (applicationMapper.checkAppCanDisable(applicationId) == 0) {
            throw new CommonException("error.app.publishedOrDeployed");
        }
    }

    @Override
    public List<ApplicationDTO> baseListByCode(String code) {
        return applicationMapper.listByCode(code);
    }

    @Override
    public List<ApplicationDTO> baseListByGitLabProjectIds(List<Long> gitLabProjectIds) {
        return applicationMapper.listByGitLabProjectIds(gitLabProjectIds);
    }

    @Override
    public void baseDelete(Long appId) {
        applicationMapper.deleteByPrimaryKey(appId);
    }

    @Override
    public List<ApplicationDTO> baseListByProjectIdAndSkipCheck(Long projectId) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setProjectId(projectId);
        applicationDTO.setIsSkipCheckPermission(true);
        return applicationMapper.select(applicationDTO);
    }

    @Override
    public List<ApplicationDTO> baseListByProjectId(Long projectId) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setProjectId(projectId);
        return applicationMapper.select(applicationDTO);
    }

    @Override
    public void baseUpdateHarborConfig(Long projectId, Long newConfigId, Long oldConfigId, boolean harborPrivate) {
        applicationMapper.updateHarborConfig(projectId, newConfigId, oldConfigId, harborPrivate);
    }

    @Override
    public String getGitlabUrl(Long projectId, Long appId) {
        ApplicationDTO applicationDTO = baseQuery(appId);
        if (applicationDTO.getGitlabProjectId() != null) {
            ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
            OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
            return gitlabUrl + urlSlash
                    + organizationDTO.getCode() + "-" + projectDTO.getCode() + "/"
                    + applicationDTO.getCode();
        }
        return "";
    }

    private Integer getGitLabId(Long applicationId) {
        ApplicationDTO applicationDTO = applicationMapper.selectByPrimaryKey(applicationId);
        if (applicationDTO != null) {
            return applicationDTO.getGitlabProjectId();
        } else {
            throw new CommonException("error.application.select");
        }
    }

    private boolean getHistory(Date startTime, Date endTime, SimpleDateFormat sdf, SonarHistroy sonarHistroy) {
        try {
            return sdf.parse(sonarHistroy.getDate()).compareTo(startTime) >= 0 && sdf.parse(sonarHistroy.getDate()).compareTo(endTime) <= 0;
        } catch (ParseException e) {
            throw new CommonException(e);
        }
    }

    private void getRate(SonarContentVO sonarContentVO, List<Facet> facets) {
        sonarContentVO.setRate("A");
        facets.stream().filter(facet -> facet.getProperty().equals(SEVERITIES)).forEach(facet -> {
            facet.getValues().stream().forEach(value -> {
                if (value.getVal().equals(Rate.MINOR.getRate()) && value.getCount() >= 1) {
                    if (sonarContentVO.getRate().equals("A")) {
                        sonarContentVO.setRate("B");
                    }
                }
                if (value.getVal().equals(Rate.MAJOR.getRate()) && value.getCount() >= 1) {
                    if (!sonarContentVO.getRate().equals("D") && !sonarContentVO.getRate().equals("E")) {
                        sonarContentVO.setRate("C");
                    }
                }
                if (value.getVal().equals(Rate.CRITICAL.getRate()) && value.getCount() >= 1) {
                    if (!sonarContentVO.getRate().equals("E")) {
                        sonarContentVO.setRate("D");
                    }
                }
                if (value.getVal().equals(Rate.BLOCKER.getRate()) && value.getCount() >= 1) {
                    sonarContentVO.setRate("E");
                }
            });
        });
    }

    private Map<String, String> getQueryMap(String key, String type, Boolean newAdd) {
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

    @Override
    public ApplicationDTO getApplicationDTO(Long projectId, ApplicationReqVO applicationReqDTO) {
        ApplicationDTO applicationDTO = ConvertUtils.convertObject(applicationReqDTO, ApplicationDTO.class);
        baseCheckName(projectId, applicationDTO.getName());
        baseCheckCode(applicationDTO);
        applicationDTO.setActive(true);
        applicationDTO.setSynchro(false);
        applicationDTO.setIsSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());
        applicationDTO.setHarborConfigId(applicationReqDTO.getHarborConfigId());
        applicationDTO.setChartConfigId(applicationReqDTO.getChartConfigId());
        return applicationDTO;
    }

    @Override
    public void baseCheckName(Long projectId, String appName) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setProjectId(projectId);
        applicationDTO.setName(appName);
        if (applicationMapper.selectOne(applicationDTO) != null) {
            throw new CommonException("error.name.exist");
        }
    }

    @Override
    public void baseCheckCode(ApplicationDTO applicationDTO) {
        if (!applicationMapper.select(applicationDTO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public ApplicationDTO baseCreate(ApplicationDTO applicationDTO) {
        if (applicationMapper.insert(applicationDTO) != 1) {
            throw new CommonException("error.application.create.insert");
        }
        return applicationDTO;
    }

    private List<ApplicationDTO> baseListAll(Long projectId) {
        return applicationMapper.listAll(projectId);
    }

    private ApplicationDTO fromImportDtoToEntity(ApplicationImportVO applicationImportVO) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setProjectId(applicationImportVO.getProjectId());
        BeanUtils.copyProperties(applicationImportVO, applicationDTO);
        if (applicationImportVO.getApplicationTemplateId() != null) {
            applicationDTO.setAppTemplateId(applicationImportVO.getApplicationTemplateId());
        }
        applicationDTO.setHarborConfigId(applicationImportVO.getHarborConfigId());
        applicationDTO.setChartConfigId(applicationImportVO.getChartConfigId());
        return applicationDTO;
    }

    /**
     * ensure the repository url and access token are valid.
     *
     * @param gitPlatformType git platform type
     * @param repositoryUrl   repository url
     * @param accessToken     access token (Nullable)
     */
    private void checkRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String
            accessToken) {
        Boolean validationResult = validateRepositoryUrlAndToken(gitPlatformType, repositoryUrl, accessToken);
        if (Boolean.FALSE.equals(validationResult)) {
            throw new CommonException("error.repository.token.invalid");
        } else if (validationResult == null) {
            throw new CommonException("error.repository.empty");
        }
    }

    private void initBranch(DevOpsAppPayload gitlabProjectPayload, ApplicationDTO applicationDTO, String branchName) {
        CommitDTO commitDTO = gitlabServiceClientOperator.queryCommit(gitlabProjectPayload.getGitlabProjectId(), branchName, gitlabProjectPayload.getUserId());
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        devopsBranchDTO.setUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchDTO.setAppId(applicationDTO.getId());
        devopsBranchDTO.setBranchName(branchName);
        devopsBranchDTO.setCheckoutCommit(commitDTO.getId());
        devopsBranchDTO.setCheckoutDate(commitDTO.getCommittedDate());
        devopsBranchDTO.setLastCommitUser(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchDTO.setLastCommitMsg(commitDTO.getMessage());
        devopsBranchDTO.setLastCommitDate(commitDTO.getCommittedDate());
        devopsBranchDTO.setLastCommit(commitDTO.getId());
        devopsBranchService.baseCreate(devopsBranchDTO);
    }

    private void replaceParams(ApplicationDTO applicationDTO,
                               ProjectDTO projectDTO,
                               OrganizationDTO organizationDTO,
                               String applicationDir) {
        try {
            File file = new File(gitUtil.getWorkingDirectory(applicationDir));
            Map<String, String> params = new HashMap<>();
            params.put("{{group.name}}", organizationDTO.getCode() + "-" + projectDTO.getCode());
            params.put("{{service.code}}", applicationDTO.getCode());
            FileUtil.replaceReturnFile(file, params);
        } catch (Exception e) {
            //删除模板
            gitUtil.deleteWorkingDirectory(applicationDir);
            throw new CommonException(e.getMessage(), e);
        }
    }


    private String getToken(DevOpsAppPayload gitlabProjectPayload, String applicationDir, UserAttrDTO userAttrDTO) {
        String accessToken = userAttrDTO.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitlabServiceClientOperator.createProjectToken(gitlabProjectPayload.getGitlabProjectId(),
                    applicationDir, gitlabProjectPayload.getUserId());
            userAttrDTO.setGitlabToken(accessToken);
            userAttrService.baseUpdate(userAttrDTO);
        }
        return accessToken;
    }

    /**
     * 释放资源
     */
    private void releaseResources(File templateWorkDir, File applicationWorkDir, Git templateGit, Git repositoryGit) {
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
    private void mergeTemplateToApplication(File templateWorkDir, File applicationWorkDir, Long
            applicationTemplateId) {
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

    /**
     * get application token (set a token if there is not one in gitlab)
     *
     * @param projectId gitlab project id
     * @param userId    gitlab user id
     * @return the application token that is stored in gitlab variables
     */
    private String getApplicationToken(Integer projectId, Integer userId) {
        List<VariableDTO> variables = gitlabServiceClientOperator.listVariable(projectId, userId);
        if (variables.isEmpty()) {
            String token = GenerateUUID.generateUUID();
            gitlabServiceClientOperator.createVariable(projectId, "Token", token, false, userId);
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
                List<Long> gitlabUserIds = userAttrService.baseListByUserIds(devOpsAppPayload.getUserIds()).stream()
                        .map(UserAttrDTO::getGitlabUserId).collect(Collectors.toList());
                gitlabUserIds.forEach(e -> {
                    MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(e));
                    if (memberDTO != null) {
                        gitlabGroupMemberService.delete(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(e));
                    }
                    MemberDTO projectMemberDTO = gitlabGroupMemberService.queryByUserId(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(e));
                    if (projectMemberDTO == null || projectMemberDTO.getUserId() == null) {
                        gitlabServiceClientOperator.createProjectMember(devOpsAppPayload.getGitlabProjectId(),
                                new MemberDTO(TypeUtil.objToInteger(e), 30, ""));
                    }
                });
            }
        }
        // 跳过权限检查，项目下所有成员自动分配权限
        else {
            List<Long> iamUserIds = iamService.getAllMemberIdsWithoutOwner(devOpsAppPayload.getIamProjectId());
            List<Integer> gitlabUserIds = userAttrService.baseListByUserIds(iamUserIds).stream()
                    .map(UserAttrDTO::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());

            gitlabUserIds.forEach(e ->
                    updateGitlabMemberPermission(devOpsAppPayload, e));
        }
    }

    private void updateGitlabMemberPermission(DevOpsAppPayload devOpsAppPayload, Integer gitlabUserId) {
        MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
        if (memberDTO != null) {
            gitlabGroupMemberService.delete(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
        }
        MemberDTO gitlabMemberDTO = gitlabServiceClientOperator.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(gitlabUserId));
        if (gitlabMemberDTO == null || gitlabMemberDTO.getUserId() == null) {
            gitlabServiceClientOperator.createProjectMember(devOpsAppPayload.getGitlabProjectId(),
                    new MemberDTO(TypeUtil.objToInteger(gitlabUserId), 30, ""));
        }
    }

    /**
     * 拉取模板库到本地
     *
     * @param applicationTemplateDTO 模板库的信息
     * @param applicationDir         本地库地址
     * @return 本地库的git实例
     */
    private Git cloneTemplate(ApplicationTemplateDTO applicationTemplateDTO, String applicationDir) {
        String repoUrl = applicationTemplateDTO.getRepoUrl();
        String type = applicationTemplateDTO.getCode();
        if (applicationTemplateDTO.getOrganizationId() != null) {
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
            repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
            type = MASTER;
        }
        return gitUtil.clone(applicationDir, type, repoUrl);
    }

    /**
     * set project hook id for application
     *
     * @param applicationDTO the application entity
     * @param projectId      the gitlab project id
     * @param token          the token for project hook
     * @param userId         the gitlab user id
     */
    private void setProjectHook(ApplicationDTO applicationDTO, Integer projectId, String token, Integer userId) {
        ProjectHookDTO projectHookDTO = ProjectHookDTO.allHook();
        projectHookDTO.setEnableSslVerification(true);
        projectHookDTO.setProjectId(projectId);
        projectHookDTO.setToken(token);
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook";
        projectHookDTO.setUrl(uri);
        List<ProjectHookDTO> projectHookDTOS = gitlabServiceClientOperator.listProjectHook(projectId, userId);
        if (projectHookDTOS.isEmpty()) {
            applicationDTO.setHookId(TypeUtil.objToLong(gitlabServiceClientOperator.createWebHook(
                    projectId, userId, projectHookDTO)
                    .getId()));
        } else {
            applicationDTO.setHookId(TypeUtil.objToLong(projectHookDTOS.get(0).getId()));
        }
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

    private List<ApplicationRepVO> setApplicationRepVOPermission(List<ApplicationDTO> applicationDTOList,
                                                                 UserAttrDTO userAttrDTO, ProjectDTO projectDTO) {
        List<ApplicationRepVO> resultDTOList = ConvertUtils.convertList(applicationDTOList, this::dtoToRepVo);
        if (userAttrDTO == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }
        if (!iamService.isProjectOwner(userAttrDTO.getIamUserId(), projectDTO)) {
            ApplicationUserPermissionDTO appUserPermissionDO = new ApplicationUserPermissionDTO();
            appUserPermissionDO.setIamUserId(userAttrDTO.getIamUserId());
            List<Long> appIds = applicationUserPermissionMapper.select(appUserPermissionDO).stream()
                    .map(ApplicationUserPermissionDTO::getAppId).collect(Collectors.toList());

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

    private void initApplicationParams(ProjectDTO projectDTO, OrganizationDTO organizationDTO, List<ApplicationDTO> applicationDTOS, String urlSlash) {
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
            }
        }

        for (ApplicationDTO t : applicationDTOS) {
            if (t.getGitlabProjectId() != null) {
                t.setRepoUrl(
                        gitlabUrl + urlSlash + organizationDTO.getCode() + "-" + projectDTO.getCode() + "/" +
                                t.getCode() + ".git");
                String key = String.format("%s-%s:%s", organizationDTO.getCode(), projectDTO.getCode(), t.getCode());
                if (!projectKeys.isEmpty() && projectKeys.contains(key)) {
                    t.setSonarUrl(sonarqubeUrl);
                }
            }
        }
    }

    private ApplicationRepVO dtoToRepVo(ApplicationDTO applicationDTO) {
        ApplicationRepVO applicationRepVO = new ApplicationRepVO();
        BeanUtils.copyProperties(applicationDTO, applicationRepVO);
        applicationRepVO.setApplicationTemplateId(applicationDTO.getAppTemplateId());
        applicationRepVO.setGitlabProjectId(TypeUtil.objToLong(applicationDTO.getGitlabProjectId()));
        return applicationRepVO;
    }
}
