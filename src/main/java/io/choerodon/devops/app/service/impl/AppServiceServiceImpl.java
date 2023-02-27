package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.CUSTOM_REPO;
import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.DEFAULT_REPO;
import static io.choerodon.devops.infra.constant.ExceptionConstants.AppServiceCode.*;
import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_USER_NOT_GITLAB_OWNER;
import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_CODE_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_NAME_EXIST;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.hzero.boot.file.FileClient;
import org.hzero.core.base.BaseConstants;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.api.vo.hrds.MemberPrivilegeViewDTO;
import io.choerodon.devops.api.vo.hrds.RepositoryPrivilegeViewDTO;
import io.choerodon.devops.api.vo.iam.ImmutableProjectInfoVO;
import io.choerodon.devops.api.vo.iam.ResourceVO;
import io.choerodon.devops.api.vo.market.MarketCategoryVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.api.vo.market.MarketSourceCodeVO;
import io.choerodon.devops.api.vo.open.OpenAppServiceReqVO;
import io.choerodon.devops.api.vo.sonar.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.AppServiceImportPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppImportServicePayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppServicePayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.PipelineConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.dto.repo.RdmMemberQueryDTO;
import io.choerodon.devops.infra.dto.repo.RdmMemberViewDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.ApplicationCenterEnum;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.feign.HrdsCodeRepoClient;
import io.choerodon.devops.infra.feign.RdupmClient;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.feign.operator.*;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


/**
 * Created by younger on 2018/3/28.
 */
@Service
public class AppServiceServiceImpl implements AppServiceService {
    public static final String SEVERITIES = "severities";
    public static final Logger LOGGER = LoggerFactory.getLogger(AppServiceServiceImpl.class);
    public static final String NODELETED = "nodeleted";
    private static final String AUTHTYPE_PUSH = "push";
    private static final String AUTHTYPE_PULL = "pull";
    private static final String CHART = "chart";
    private static final String GIT = ".git";
    protected static final String SONAR_KEY = "%s-%s:%s";
    private static final String PRIVATE_TOKEN_FORMAT = "private-token:%s";
    private static final String PRIVATE_TOKEN_ID_FORMAT = "private-token-id:%s";
    private static final Pattern REPOSITORY_URL_PATTERN = Pattern.compile("^http.*");
    private static final String ISSUE = "issue";
    private static final String COVERAGE = "coverage";
    private static final String SONAR = "sonar";
    private static final String NORMAL = "normal";
    private static final String APP_SERVICE = "appService";
    private static final String METRICS = "metrics";
    private static final String SONAR_NAME = "sonar_default";
    private static final String APPLICATION = "application";
    private static final String DUPLICATE = "duplicate";
    private static final String NORMAL_SERVICE = "normal_service";
    private static final String SHARE_SERVICE = "share_service";
    private static final String TEMP_MODAL = "\\?version=";
    private static final String LOGIN_NAME = "loginName";
    private static final String REAL_NAME = "realName";
    private static final String APPSERVICE = "app-service";
    private static final String APP = "app";
    public static final String SOURCE_CODE_BUCKET_NAME = "market-source-code-bucket";
    public static final String GITLAB_VARIABLE_TOKEN = "Token";
    public static final String GITLAB_VARIABLE_TRIVY_INSECURE = "TRIVY_INSECURE";
    public static final String CHOERODON_URL = "CHOERODON_URL";
    private static final Long ONE_GB_TO_B = 1073741824L;

    /**
     * CI 文件模板
     */
    private static final String CI_FILE_TEMPLATE;

    private final Gson gson = new Gson();
    @Value("${services.gitlab.url}")
    protected String gitlabUrl;
    @Value("${services.gitlab.proxy-url:}")
    private String gitlabProxyUrl;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;
    @Value("${services.sonarqube.url:}")
    private String sonarqubeUrl;
    @Value("${services.gateway.url}")
    private String gatewayUrl;
    @Value("${services.sonarqube.username:}")
    private String userName;
    @Value("${services.sonarqube.password:}")
    private String password;
    @Value("${services.sonarqube.analysis-user-token:}")
    private String analysisUserToken;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private AppServiceVersionMapper appServiceVersionMapper;
    @Autowired
    private UserAttrMapper userAttrMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private DevopsBranchService devopsBranchService;
    @Autowired
    private DevopsEnvAppServiceMapper devopsEnvAppServiceMapper;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;
    @Autowired
    private DevopsGitlabCommitMapper gitlabCommitMapper;
    @Autowired
    private DevopsGitlabPipelineMapper gitlabPipelineMapper;
    @Autowired
    private DevopsMergeRequestMapper mergeRequestMapper;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Lazy
    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;
    @Lazy
    @Autowired
    private RdupmClient rdupmClient;
    @Autowired
    private RducmClientOperator rducmClientOperator;
    @Autowired
    private HarborService harborService;
    @Autowired
    private HrdsCodeRepoClient hrdsCodeRepoClient;
    @Autowired
    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
    @Autowired
    private HrdsCodeRepoClientOperator hrdsCodeRepoClientOperator;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AsgardServiceClientOperator asgardServiceClientOperator;
    @Autowired
    private AppServiceUtils appServiceUtils;
    @Autowired
    private FileClient fileClient;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private DevopsAppTemplateMapper devopsAppTemplateMapper;
    @Autowired
    private DevopsAppTemplateService devopsAppTemplateService;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AppExternalConfigService appExternalConfigService;
    @Autowired
    private ExternalGitUtil externalGitUtil;
    @Autowired
    private DevopsProjectMapper devopsProjectMapper;
    @Autowired
    private DevopsCiPipelineFunctionService devopsCiPipelineFunctionService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsAppServiceHelmRelService devopsAppServiceHelmRelService;
    @Lazy
    @Autowired
    private DevopsCiSonarQualityGateService devopsCiSonarQualityGateService;

    @Autowired
    @Lazy
    private PipelineService pipelineService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;

    static {
        try (InputStream inputStream = AppServiceServiceImpl.class.getResourceAsStream("/shell/ci.sh")) {
            CI_FILE_TEMPLATE = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException(DEVOPS_LOAD_CI_SH);
        }
    }


    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_CREATE_APPLICATION_SERVICE,
            description = "Devops创建应用服务", inputSchema = "{}")
    @Transactional
    public AppServiceRepVO create(Long projectId, AppServiceReqVO appServiceReqVO) {
        appServiceReqVO.setProjectId(projectId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplicationService(appServiceReqVO.getCode());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        // 判断项目下是否还能创建应用服务
        appServiceUtils.checkEnableCreateAppSvcOrThrowE(projectDTO.getOrganizationId(), projectId, 1);

        // 校验模板id和模板版本id是否都有值或者都为空
        boolean isTemplateNull = appServiceReqVO.getTemplateAppServiceId() == null;
        boolean isTemplateVersionNull = appServiceReqVO.getTemplateAppServiceVersionId() == null;

        if ((isTemplateNull && !isTemplateVersionNull) || (!isTemplateNull && isTemplateVersionNull)) {
            throw new CommonException(DEVOPS_TEMPLATE_FIELDS);
        }

        // 查询创建应用服务所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);

        boolean isGitlabRoot = false;

        if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
            // 如果这边表存了gitlabAdmin这个字段,那么gitlabUserId就不会为空,所以不判断此字段为空
            isGitlabRoot = gitlabServiceClientOperator.isGitlabAdmin(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        if (!isGitlabRoot) {
            MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.value)) {

                throw new CommonException(DEVOPS_USER_NOT_GITLAB_OWNER);
            }
        }

        AppServiceDTO appServiceDTO = getApplicationServiceDTO(projectId, appServiceReqVO);
        appServiceDTO = baseCreate(appServiceDTO);

        //创建saga payload
        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
        devOpsAppServicePayload.setPath(appServiceDTO.getCode());
        devOpsAppServicePayload.setOrganizationId(projectDTO.getOrganizationId());
        devOpsAppServicePayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        devOpsAppServicePayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
        devOpsAppServicePayload.setAppServiceId(appServiceDTO.getId());
        devOpsAppServicePayload.setIamProjectId(projectId);
        devOpsAppServicePayload.setTemplateAppServiceId(appServiceReqVO.getTemplateAppServiceId());
        devOpsAppServicePayload.setTemplateAppServiceVersionId(appServiceReqVO.getTemplateAppServiceVersionId());
        devOpsAppServicePayload.setAppServiceDTO(appServiceDTO);
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType(APPSERVICE)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_APPLICATION_SERVICE)
                        .withPayloadAndSerialize(devOpsAppServicePayload)
                        .withRefId(String.valueOf(appServiceDTO.getId()))
                        .withSourceId(projectId),
                builder -> {
                });
        return ConvertUtils.convertObject(baseQueryByCode(appServiceDTO.getCode(), appServiceDTO.getProjectId()), AppServiceRepVO.class);
    }

    /**
     * 判断项目下是否还能创建应用服务
     *
     * @param projectId 项目id
     */


    @Override
    public AppServiceRepVO query(Long projectId, Long appServiceId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        Boolean shareAppService = false;
        if (!appServiceDTO.getProjectId().equals(projectId)) {
            shareAppService = true;
        }
        AppServiceRepVO appServiceRepVO = dtoToRepVo(appServiceDTO);
        DevopsAppServiceHelmRelDTO devopsAppServiceHelmRelDTO = devopsAppServiceHelmRelService.queryByAppServiceId(appServiceId);
        if (devopsAppServiceHelmRelDTO != null) {
            appServiceRepVO.setHelmConfigId(devopsAppServiceHelmRelDTO.getHelmConfigId());
        }
        //url地址拼接
        if (appServiceDTO.getGitlabProjectId() != null && !shareAppService) {
            appServiceRepVO.setRepoUrl(concatRepoUrl(organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), appServiceDTO.getCode()));
        }
        if (shareAppService) {
            ProjectDTO shareProjectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
            appServiceRepVO.setShareProjectName(shareProjectDTO.getName());
        }
        //添加harbor的配置信息
        HarborRepoDTO selectedHarborConfig = rdupmClient.queryHarborRepoConfig(projectId, appServiceId).getBody();
        if (!Objects.isNull(selectedHarborConfig) && !Objects.isNull(selectedHarborConfig.getHarborRepoConfig())) {
            selectedHarborConfig.getHarborRepoConfig().setType(selectedHarborConfig.getRepoType());
            selectedHarborConfig.getHarborRepoConfig().setLoginName(null);
            selectedHarborConfig.getHarborRepoConfig().setPassword(null);
            appServiceRepVO.setHarborRepoConfigDTO(selectedHarborConfig.getHarborRepoConfig());
        }
        return appServiceRepVO;
    }

    private String concatRepoUrl(String orgCode, String projectCode, String appServiceCode) {
        //url地址拼接
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        return gitlabUrl + urlSlash
                + orgCode + "-" + projectCode + "/"
                + appServiceCode + ".git";
    }

    @Override
    public Page<AppServiceRepVO> internalListAllInProject(Long projectId, String params, PageRequest pageable) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        return ConvertUtils.convertPage(
                PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                        () -> appServiceMapper.list(projectId, null, null, null,
                                TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)), PageRequestUtil.checkSortIsEmpty(pageable), true, null)),
                this::dtoToRepVoWithoutIamUserFill);
    }


    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_APP_DELETE,
            description = "Devops删除应用服务", inputSchemaClass = DevOpsAppServicePayload.class)
    @Transactional
    @Override
    public void delete(Long projectId, Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (appServiceDTO == null) {
            return;
        }

        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        // 禁止删除未失败或者启用状态的应用服务
        if (Boolean.TRUE.equals(appServiceDTO.getActive())
                && Boolean.FALSE.equals(appServiceDTO.getFailed())) {
            throw new CommonException(DEVOPS_DELETE_NONFAILED_APP_SERVICE, appServiceDTO.getName());
        }
        // 验证改应用服务在其他项目是否被生成实例
        checkAppserviceIsShareDeploy(projectId, appServiceId);
        AppServiceMsgVO checkResult = checkCanDisable(appServiceId, projectId);
        if (checkResult.getCheckResources()) {
            throw new CommonException(DEVOPS_DELETE_APPLICATION_SERVICE_DUE_TO_SHARE);
        }
        if (checkResult.getCheckRule()) {
            throw new CommonException(DEVOPS_DELETE_APPLICATION_SERVICE_DUE_TO_RESOURCES);
        }
        if (checkResult.getCheckCi()) {
            throw new CommonException(DEVOPS_DELETE_APPLICATION_SERVICE_DUE_TO_CI_PIPELINE);
        }

        if (devopsCiPipelineService.selectCountByAppServiceId(appServiceId) != 0) {
            throw new CommonException(DEVOPS_DELETE_APP_SERVICE_DUE_TO_CI_PIPELINE, appServiceId);
        }

        appServiceDTO.setSynchro(Boolean.FALSE);
        appServiceMapper.updateByPrimaryKey(appServiceDTO);

        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
        devOpsAppServicePayload.setAppServiceId(appServiceId);
        devOpsAppServicePayload.setIamProjectId(projectId);
        //删除应用服务后需要发送消息，这里将消息的内容封近paylod, 外部应用不需要
        if (appServiceDTO.getExternalConfigId() == null && appServiceDTO.getGitlabProjectId() != null) {
            List<MemberDTO> memberDTOS = gitlabServiceClientOperator.listMemberByProject(appServiceDTO.getGitlabProjectId(), null);
            devOpsAppServicePayload.setMemberDTOS(memberDTOS);
        }

        devOpsAppServicePayload.setAppServiceDTO(appServiceDTO);
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withRefId(TypeUtil.objToString(appServiceId))
                        .withSourceId(projectId)
                        .withPayloadAndSerialize(devOpsAppServicePayload)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_APP_DELETE),
                builder -> {
                });
    }

    private void checkAppserviceIsShareDeploy(Long projectId, Long appServiceId) {
        Long organizationId = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId).getOrganizationId();
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(organizationId);
        Set<Long> projectIds = projectDTOS.stream().filter(projectDTO -> !projectDTO.getId().equals(projectId)).map(ProjectDTO::getId).collect(toSet());
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        List<AppServiceInstanceDTO> appServiceInstanceDTOS = appServiceInstanceMapper.listByProjectIdsAndAppServiceId(projectIds, appServiceId);
        if (!CollectionUtils.isEmpty(appServiceInstanceDTOS)) {
            throw new CommonException(DEVOPS_NOT_DELETE_SERVICE_BY_OTHER_PROJECT_DEPLOYMENT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteAppServiceSage(Long projectId, Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (appServiceDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("AppService", appServiceId, LOGGER);
            return;
        }
        // 删除应用服务的分支,合并请求，pipeline,commit
        devopsBranchService.deleteAllBranch(appServiceId);
        gitlabCommitMapper.deleteByAppServiceId(appServiceId);
        mergeRequestMapper.deleteByProjectId(appServiceDTO.getGitlabProjectId());
        gitlabPipelineMapper.deleteByAppServiceId(appServiceId);
        // 删除应用服务的版本
        appServiceVersionService.deleteByAppServiceId(appServiceId);
        //删除gitlab project,外部仓库不删除
        if (appServiceDTO.getExternalConfigId() == null) {
            if (appServiceDTO.getGitlabProjectId() != null) {
                Integer gitlabProjectId = appServiceDTO.getGitlabProjectId();
                // 确保只有这个应用服务关联了仓库（大多数情况是这样，可能有一些特殊的脏数据）
                if (selectCountByGitlabProjectId(gitlabProjectId) == 1) {
                    GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectById(gitlabProjectId);
                    if (gitlabProjectDTO != null && gitlabProjectDTO.getId() != null) {
                        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                        Integer gitlabUserId = TypeUtil.objToInt(userAttrDTO.getGitlabUserId());
                        gitlabServiceClientOperator.deleteProjectById(gitlabProjectId, gitlabUserId);
                        LOGGER.info("Successfully delete gitlab project {} for app service with id {}", gitlabProjectId, appServiceDTO.getId());
                    }
                } else {
                    LOGGER.warn("The gitlab project id {} is associated with other app service, so skip...", gitlabProjectId);
                }
            } else {
                // 可能应用服务创建完成后被回滚了数据库没有存下仓库id
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
                Tenant tenant = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                Integer gitlabUserId = TypeUtil.objToInt(userAttrDTO.getGitlabUserId());
                GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectByName(tenant.getTenantNum() + "-" + projectDTO.getDevopsComponentCode(), appServiceDTO.getCode(), gitlabUserId, false);
                if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                    // 一般情况下，这个关于count的if条件是true，不正常的数据才会false
                    if (selectCountByGitlabProjectId(gitlabProjectDO.getId()) == 0) {
                        gitlabServiceClientOperator.deleteProjectById(gitlabProjectDO.getId(), gitlabUserId);
                        LOGGER.info("Successfully delete gitlab project {} for app service with id {}", gitlabProjectDO.getId(), appServiceDTO.getId());
                    } else {
                        LOGGER.warn("The gitlab project id {} is associated with other app service, so skip...", gitlabProjectDO.getId());
                    }
                }
            }
        } else {
            Integer gitlabProjectId = appServiceDTO.getGitlabProjectId();
            // 删除webhook
            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
            List<ProjectHookDTO> projectHookDTOS = gitlabServiceClientOperator.listExternalWebHook(gitlabProjectId, appExternalConfigDTO);
            if (!CollectionUtils.isEmpty(projectHookDTOS)) {
                projectHookDTOS.forEach(projectHookDTO -> {
                    if (getWebhookUrl().equals(projectHookDTO.getUrl())) {
                        gitlabServiceClientOperator.deleteExternalWebHook(gitlabProjectId,
                                projectHookDTO.getId(),
                                appExternalConfigDTO);
                    }
                });
            }
            // 删除token
            List<Variable> variables = gitlabServiceClientOperator.listExternalProjectVariable(gitlabProjectId, appExternalConfigDTO);
            if (!CollectionUtils.isEmpty(variables)) {
                variables.forEach(variable -> {
                    if (GITLAB_VARIABLE_TOKEN.equalsIgnoreCase(variable.getKey())) {
                        gitlabServiceClientOperator.deleteExternalProjectVariable(gitlabProjectId, variable.getKey(), appExternalConfigDTO);
                    }
                });
            }

            // 外部仓库还需要删除认证配置
            appExternalConfigService.baseDelete(appServiceDTO.getExternalConfigId());
        }

        //删除应用服务与自定义harbor仓库的关联关系
        HarborCustomRepo harborCustomRepo = rdupmClient.listRelatedCustomRepoByService(projectId, appServiceId).getBody();
        if (!Objects.isNull(harborCustomRepo)) {
            rdupmClient.deleteRelationByService(projectId, appServiceId, harborCustomRepo.getId());
        }
        appServiceMapper.deleteByPrimaryKey(appServiceId);
    }

    @Override
    @Transactional
    public Boolean update(Long projectId, AppServiceUpdateDTO appServiceUpdateDTO) {
        Long appServiceId = appServiceUpdateDTO.getId();
        AppServiceDTO oldAppServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);

        if (oldAppServiceDTO == null) {
            return false;
        }

        CommonExAssertUtil.assertTrue(projectId.equals(oldAppServiceDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        // 更新跟pom相关的两个字段
        appServiceMapper.updatePomFields(appServiceUpdateDTO.getId(), appServiceUpdateDTO.getGroupId(), appServiceUpdateDTO.getArtifactId());

        AppServiceDTO appServiceDTO = ConvertUtils.convertObject(appServiceUpdateDTO, AppServiceDTO.class);

        //处理helm仓库的配置
        devopsAppServiceHelmRelService.handleRel(appServiceUpdateDTO.getId(), appServiceUpdateDTO.getHelmConfigId());

        //保存应用服务与harbor仓库的关系
        if (!Objects.isNull(appServiceUpdateDTO.getHarborRepoConfigDTO())) {
            if (DEFAULT_REPO.equals(appServiceUpdateDTO.getHarborRepoConfigDTO().getType())) {
                deleteHarborAppServiceRel(projectId, appServiceDTO.getId());
            }
            if (CUSTOM_REPO.equals((appServiceUpdateDTO.getHarborRepoConfigDTO().getType()))) {
                deleteHarborAppServiceRel(projectId, appServiceDTO.getId());
                rdupmClient.saveRelationByService(projectId, appServiceDTO.getId(), appServiceUpdateDTO.getHarborRepoConfigDTO().getRepoId());
            }
        }
//        if (appServiceUpdateDTO.getChart() != null) {
//            DevopsConfigDTO chartConfig = devopsConfigService.queryRealConfig(appServiceId, APP_SERVICE, CHART, AUTHTYPE_PULL);
//            appServiceDTO.setChartConfigId(chartConfig.getId());
//        }

        if (!oldAppServiceDTO.getName().equals(appServiceUpdateDTO.getName())) {
            checkName(oldAppServiceDTO.getProjectId(), appServiceDTO.getName());
        }
        baseUpdate(appServiceDTO);
        return true;
    }

    private void deleteHarborAppServiceRel(Long projectId, Long appServcieId) {
        //删除应用服务关联的所有自定义仓库
        rdupmClient.deleteAllRelationByService(projectId, appServcieId);
    }


    @Override
    @Transactional
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_APP_SYNC_STATUS,
            description = "同步应用服务状态", inputSchemaClass = DevOpsAppServicePayload.class)
    public Boolean updateActive(Long projectId, Long appServiceId, final Boolean active) {
        AppServiceDTO appServiceDTO = permissionHelper.checkAppServiceBelongToProject(projectId, appServiceId);

        // 为空则默认true
        Boolean toUpdateValue = Boolean.FALSE.equals(active) ? Boolean.FALSE : Boolean.TRUE;

        // 如果原先的值和更新的值相等，则不更新
        if (toUpdateValue.equals(appServiceDTO.getActive())) {
            return false;
        }

        // 如果不相等，且将停用应用服务，检查该应用服务是否可以被停用
        if (!toUpdateValue) {
            AppServiceMsgVO appServiceMsgVO = checkCanDisable(appServiceId, projectId);
            if (!appServiceMsgVO.getCheckResources() && !appServiceMsgVO.getCheckRule() && !appServiceMsgVO.getCheckCi()) {
                // 如果能停用，删除其和他所属项目下的环境之间的关联关系
                devopsEnvAppServiceMapper.deleteRelevanceInProject(appServiceId, projectId);
            } else {
                throw new CommonException(DEVOPS_DISABLE_OR_ENABLE_APPLICATION_SERVICE);
            }
        }

        appServiceDTO.setActive(toUpdateValue);
        baseUpdate(appServiceDTO);

        // 发送启停用消息
        if (toUpdateValue) {
            sendNotificationService.sendWhenAppServiceEnabled(appServiceId);
        } else {
            sendNotificationService.sendWhenAppServiceDisabled(appServiceId);
        }

        //创建saga payload
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        // 查询创建应用服务所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
        devOpsAppServicePayload.setOrganizationId(projectDTO.getOrganizationId());
        devOpsAppServicePayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        devOpsAppServicePayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
        devOpsAppServicePayload.setAppServiceId(appServiceDTO.getId());
        devOpsAppServicePayload.setIamProjectId(projectId);
        devOpsAppServicePayload.setAppServiceDTO(appServiceDTO);
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(appServiceId)
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_APP_SYNC_STATUS),
                builder -> builder
                        .withPayloadAndSerialize(devOpsAppServicePayload)
                        .withRefId(String.valueOf(appServiceId))
                        .withSourceId(projectId));
        return true;
    }

    /**
     * 检查当前应用服务是否还有相关的资源和共享规则
     * 不能停用则会抛出异常 {@link CommonException}
     *
     * @param appServiceId 服务id
     * @param projectId    项目id
     */
    private AppServiceMsgVO checkCanDisable(Long appServiceId, Long projectId) {
        int nonDeleteInstancesCount = appServiceInstanceMapper.countNonDeletedInstances(appServiceId, projectId);
        AppServiceMsgVO appServiceMsgVO = new AppServiceMsgVO(false, false);
        if (nonDeleteInstancesCount > 0) {
            appServiceMsgVO.setCheckResources(true);
        }

        int shareRulesCount = appServiceShareRuleMapper.countShareRulesByAppServiceId(appServiceId);
        if (shareRulesCount > 0) {
            appServiceMsgVO.setCheckRule(true);
        }

        if (devopsEnvAppServiceMapper.countRelatedSecret(appServiceId, null, projectId) != 0
                || devopsEnvAppServiceMapper.countRelatedService(appServiceId, null, projectId) != 0
                || devopsEnvAppServiceMapper.countRelatedConfigMap(appServiceId, null, projectId) != 0) {
            appServiceMsgVO.setCheckResources(true);
        }
        appServiceMsgVO.setCheckCi(appServiceIsExistsCi(projectId, appServiceId));

        appServiceMsgVO.setCheckCd(pipelineService.countAppServicePipelineReference(projectId, appServiceId) > 0);
        return appServiceMsgVO;
    }

    private boolean appServiceIsExistsCi(Long projectId, Long appServiceId) {
        CiCdPipelineDTO devopsCiPipelineDTO = new CiCdPipelineDTO();
        devopsCiPipelineDTO.setAppServiceId(appServiceId);
        devopsCiPipelineDTO.setProjectId(projectId);
        return devopsCiCdPipelineMapper.selectCount(devopsCiPipelineDTO) > 0;
    }

    @Override
    public Page<AppServiceRepVO> pageByOptions(Long projectId, Boolean isActive, Boolean hasVersion,
                                               String type, Boolean doPage,
                                               PageRequest pageable,
                                               String params,
                                               Boolean checkMember,
                                               Boolean includeExternal,
                                               Boolean excludeFailed) {

        Page<AppServiceDTO> applicationServiceDTOS = basePageByOptions(projectId, isActive, hasVersion, type, doPage, pageable, params, checkMember, includeExternal, excludeFailed);
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        initApplicationParams(projectId, applicationServiceDTOS.getContent(), urlSlash);

        Page<AppServiceRepVO> destination = new Page<>();
        BeanUtils.copyProperties(applicationServiceDTOS, destination, "content");
        if (!ObjectUtils.isEmpty(applicationServiceDTOS.getContent())) {
            List<AppServiceDTO> appServiceDTOList = applicationServiceDTOS.getContent();
            List<Long> userIds = appServiceDTOList.stream().map(AppServiceDTO::getCreatedBy).collect(toList());
            userIds.addAll(appServiceDTOList.stream().map(AppServiceDTO::getLastUpdatedBy).collect(toList()));
            List<Long> distinctIds = userIds.stream().distinct().collect(toList());
            List<IamUserDTO> userResult = baseServiceClientOperator.listUsersByIds(distinctIds);

            Map<Long, IamUserDTO> users = userResult.stream().collect(Collectors.toMap(IamUserDTO::getId, u -> u));
            // 收集失败的应用服务的id
            List<String> refIds = applicationServiceDTOS.getContent().stream().filter(app -> Boolean.TRUE.equals(app.getFailed())).map(appServiceDTO -> String.valueOf(appServiceDTO.getId())).collect(toList());
            List<AppServiceRepVO> appServiceRepVOS = applicationServiceDTOS.getContent().stream().map(appServiceDTO -> dtoToRepVo(appServiceDTO, users)).collect(toList());
            if (!CollectionUtils.isEmpty(refIds) && (excludeFailed == null || !excludeFailed)) {
                Map<String, SagaInstanceDetails> stringSagaInstanceDetailsMap = SagaInstanceUtils.listToMap(asgardServiceClientOperator.queryByRefTypeAndRefIds(APPSERVICE, refIds, SagaTopicCodeConstants.DEVOPS_CREATE_APPLICATION_SERVICE));
                Map<String, SagaInstanceDetails> sagaInstanceDetailsMapImport = SagaInstanceUtils.listToMap(asgardServiceClientOperator.queryByRefTypeAndRefIds(APP, refIds, SagaTopicCodeConstants.DEVOPS_IMPORT_GITLAB_PROJECT));
                appServiceRepVOS.forEach(appServiceRepVO -> {
                    Long createSagaId = SagaInstanceUtils.fillInstanceId(stringSagaInstanceDetailsMap, String.valueOf(appServiceRepVO.getId()));
                    Long importSagaId = SagaInstanceUtils.fillInstanceId(sagaInstanceDetailsMapImport, String.valueOf(appServiceRepVO.getId()));
                    if (!Objects.isNull(createSagaId)) {
                        appServiceRepVO.setSagaInstanceId(createSagaId);
                    } else {
                        appServiceRepVO.setSagaInstanceId(importSagaId);
                    }
                });
            }
            destination.setContent(appServiceRepVOS);
        } else {
            destination.setContent(new ArrayList<>());
        }
        return destination;
    }

    @Override
    public Page<AppServiceRepVO> pageInternalByOptionsWithAccessLevel(Long projectId,
                                                                      PageRequest pageable,
                                                                      String params) {
        Page<AppServiceRepVO> appServiceRepVOS = pageByOptions(projectId, true, null, null, true, pageable, params, true, false, true);
        Set<Long> appServiceIds = appServiceRepVOS.getContent().stream().map(AppServiceRepVO::getId).collect(toSet());
        Map<Long, MemberPrivilegeViewDTO> memberPrivilegeViewDTOMap = hrdsCodeRepoClientOperator.selfPrivilege(null, projectId, appServiceIds).stream().collect(toMap(MemberPrivilegeViewDTO::getRepositoryId, Function.identity()));
        appServiceRepVOS.getContent().forEach(appServiceRepVO -> {
            appServiceRepVO.setAccessLevel(memberPrivilegeViewDTOMap.get(appServiceRepVO.getId()).getAccessLevel());
        });
        return appServiceRepVOS;
    }

    @Override
    public List<AppServiceRepVO> listByActive(Long projectId) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        ImmutableProjectInfoVO immutableProjectInfoVO = baseServiceClientOperator.queryImmutableProjectInfo(projectId);
        boolean projectOwner = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        List<AppServiceDTO> applicationDTOServiceList;
        if (projectOwner) {
            applicationDTOServiceList = appServiceMapper.listByActive(projectId, null);
        } else {
            Set<Long> appServiceIds = getMemberAppServiceIds(immutableProjectInfoVO.getTenantId(), projectId, userId);
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new ArrayList<>();
            }
            applicationDTOServiceList = appServiceMapper.listProjectMembersAppServiceByActive(projectId, appServiceIds, userId, null);
        }

        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        List<Long> userIds = applicationDTOServiceList.stream().map(AppServiceDTO::getCreatedBy).collect(toList());
        userIds.addAll(applicationDTOServiceList.stream().map(AppServiceDTO::getLastUpdatedBy).collect(toList()));

        List<Long> distinctIds = userIds.stream().distinct().collect(toList());
        Map<Long, IamUserDTO> users = baseServiceClientOperator.listUsersByIds(new ArrayList<>(distinctIds)).stream().collect(toMap(IamUserDTO::getId, u -> u));

        initApplicationParamsWithProxyUrl(projectId, applicationDTOServiceList, urlSlash);


        return applicationDTOServiceList.stream().map(appServiceDTO -> dtoToRepVo(appServiceDTO, users)).collect(toList());
    }

    protected void initApplicationParamsWithProxyUrl(Long projectId, List<AppServiceDTO> applicationDTOS, String urlSlash) {
        ImmutableProjectInfoVO info = baseServiceClientOperator.queryImmutableProjectInfo(projectId);
        for (AppServiceDTO t : applicationDTOS) {
            initApplicationParamsWithProxyUrl(info, t, urlSlash);
        }
    }

    @Override
    public Integer countByActive(Long projectId) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        int count;
        if (projectOwnerOrRoot) {
            count = appServiceMapper.countByActive(projectId);
        } else {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
            Set<Long> appServiceIds = getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return 0;
            }
            count = appServiceMapper.countProjectMembersAppServiceByActive(projectId, appServiceIds, userId);
        }

        return count;
    }

    @Override
    public List<AppServiceRepVO> listAll(Long projectId, Long envId) {
        // 查询本项目或其它项目共享的应用
        List<AppServiceRepVO> appServiceRepVOList = ConvertUtils.convertList(baseListAll(projectId, envId, null), AppServiceRepVO.class);
        appServiceRepVOList.forEach(appServiceRepVO -> {
            if (appServiceRepVO.getProjectId() != null && appServiceRepVO.getProjectId().equals(projectId)) {
                appServiceRepVO.setServiceType(NORMAL_SERVICE);
            } else {
                appServiceRepVO.setServiceType(SHARE_SERVICE);
            }
        });

        // 查询应用市场的应用
        List<AppServiceInstanceVO> appServiceInstanceVOS = appServiceInstanceService.listMarketInstance(envId);
        if (!CollectionUtils.isEmpty(appServiceInstanceVOS)) {
            Set<Long> marketServiceIds = appServiceInstanceVOS.stream().map(AppServiceInstanceVO::getAppServiceId).collect(toSet());
            List<MarketServiceVO> marketServiceVOS = marketServiceClientOperator.queryMarketServiceByIds(projectId, marketServiceIds);
            marketServiceVOS.forEach(marketServiceVO ->
                    {
                        AppServiceRepVO appServiceRepVO = new AppServiceRepVO();
                        appServiceRepVO.setId(marketServiceVO.getId());
                        appServiceRepVO.setServiceType(AppServiceType.MARKET_SERVICE.getType());
                        appServiceRepVO.setType(AppSourceType.MARKET.getValue());
                        appServiceRepVO.setName(marketServiceVO.getMarketServiceName());
                        appServiceRepVO.setCode(marketServiceVO.getMarketServiceCode());
                        appServiceRepVOList.add(appServiceRepVO);
                    }
            );
        }
        return appServiceRepVOList;
    }

    @Override
    public void checkName(Long projectId, String name) {
        if (!isNameUnique(projectId, name)) {
            throw new CommonException(DEVOPS_NAME_EXIST);
        }
    }

    @Override
    public void checkCode(Long projectId, String code) {
        if (!isCodeUnique(projectId, code)) {
            throw new CommonException(DEVOPS_CODE_EXIST);
        }
    }

    @Override
    public boolean isCodeUnique(Long projectId, String code) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setProjectId(projectId);
        appServiceDTO.setCode(code);
        return appServiceMapper.selectCount(appServiceDTO) == 0;
    }

    @Override
    public boolean isNameUnique(Long projectId, String name) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setProjectId(projectId);
        appServiceDTO.setName(name);
        return appServiceMapper.selectCount(appServiceDTO) == 0;
    }

    @Override
    public AppServiceBatchCheckVO checkCodeByProjectId(Long projectId, AppServiceBatchCheckVO appServiceBatchCheckVO) {
        AppServiceBatchCheckVO batchCheckVO = new AppServiceBatchCheckVO();
        batchCheckVO.setListCode(
                appServiceBatchCheckVO.getListCode().stream().filter(code -> {
                    AppServiceDTO appServiceDTO = new AppServiceDTO();
                    appServiceDTO.setProjectId(projectId);
                    appServiceDTO.setCode(code);
                    List<AppServiceDTO> list = appServiceMapper.select(appServiceDTO);
                    return list != null && !list.isEmpty();
                }).collect(Collectors.toList()));
        batchCheckVO.setListName(appServiceBatchCheckVO.getListName().stream().filter(name -> {
            AppServiceDTO appServiceDTO = new AppServiceDTO();
            appServiceDTO.setProjectId(projectId);
            appServiceDTO.setName(name);
            List<AppServiceDTO> list = appServiceMapper.select(appServiceDTO);
            return list != null && !list.isEmpty();
        }).collect(Collectors.toList()));
        return batchCheckVO;
    }

    /**
     * 保证创建应用服务前，这个 gitlabProjectId 没有人用
     *
     * @param gitlabProjectId gitlab项目id
     */
    private void checkGitlabProjectIdNotUsedBefore(Integer gitlabProjectId) {
        CommonExAssertUtil.assertTrue(selectCountByGitlabProjectId(gitlabProjectId) == 0, DEVOPS_GITLAB_PROJECT_ID_ASSOCIATED_WITH_OTHER_APP_SERVICE);
    }

    private int selectCountByGitlabProjectId(Integer gitlabProjectId) {
        AppServiceDTO condition = new AppServiceDTO();
        condition.setGitlabProjectId(gitlabProjectId);
        return appServiceMapper.selectCount(condition);
    }

    @Override
    public void operationApplication(DevOpsAppServicePayload devOpsAppServicePayload) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByGitlabAppGroupId(
                TypeUtil.objToInteger(devOpsAppServicePayload.getGroupId()));

        AppServiceDTO appServiceDTO = baseQueryByCode(devOpsAppServicePayload.getPath(),
                devopsProjectDTO.getIamProjectId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(devOpsAppServicePayload.getUserId()));

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsProjectDTO.getIamProjectId());
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator
                .queryProjectByName(organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode(), appServiceDTO.getCode(),
                        devOpsAppServicePayload.getUserId(), false);
        Integer gitlabProjectId = gitlabProjectDO.getId();
        if (gitlabProjectId == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(devOpsAppServicePayload.getGroupId(),
                    devOpsAppServicePayload.getPath(),
                    devOpsAppServicePayload.getUserId(), false);
        } else {
            checkGitlabProjectIdNotUsedBefore(gitlabProjectId);
        }
        devOpsAppServicePayload.setGitlabProjectId(gitlabProjectDO.getId());
        String applicationServiceToken = appServiceDTO.getToken();
        getApplicationToken(applicationServiceToken, devOpsAppServicePayload.getGitlabProjectId(), devOpsAppServicePayload.getUserId());
        appServiceDTO.setGitlabProjectId(gitlabProjectDO.getId());
        appServiceDTO.setSynchro(true);
        appServiceDTO.setFailed(false);
        setProjectHook(appServiceDTO, devOpsAppServicePayload.getGitlabProjectId(), applicationServiceToken, devOpsAppServicePayload.getUserId());

        if (devOpsAppServicePayload.getTemplateAppServiceId() != null && devOpsAppServicePayload.getTemplateAppServiceVersionId() != null) {
            LOGGER.info("The current app service id is {} and the service code is {}", appServiceDTO.getId(), appServiceDTO.getCode());
            LOGGER.info("The template app service id is not null: {}, start to clone template repository", devOpsAppServicePayload.getTemplateAppServiceId());

            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            String newGroupName = organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode();
            String repositoryUrl = repoUrl + newGroupName + "/" + appServiceDTO.getCode() + GIT;
            cloneAndPushCode(appServiceDTO, userAttrDTO, devOpsAppServicePayload.getTemplateAppServiceId(), devOpsAppServicePayload.getTemplateAppServiceVersionId(), repositoryUrl, newGroupName);
        }

        appServiceMapper.updateByIdSelectiveWithoutAudit(appServiceDTO);
    }

    @Override
    public void operationExternalApplication(DevOpsAppServicePayload devOpsAppServicePayload) {

        AppServiceDTO appServiceDTO = baseQuery(devOpsAppServicePayload.getAppServiceId());

        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());

        // 2. 设置token等变量（创建或更新）
        List<CiVariableVO> variables = new ArrayList<>();
        variables.add(new CiVariableVO(GITLAB_VARIABLE_TOKEN, appServiceDTO.getToken()));
        variables.add(new CiVariableVO(GITLAB_VARIABLE_TRIVY_INSECURE, "true"));
        variables.add(new CiVariableVO(CHOERODON_URL, gatewayUrl));
        gitlabServiceClientOperator.batchSaveExternalProjectVariable(appServiceDTO.getGitlabProjectId(), appExternalConfigDTO, variables);

        // 3. 添加webhook
        setExternalProjectHook(appServiceDTO, appServiceDTO.getGitlabProjectId(), appServiceDTO.getToken(), appExternalConfigDTO);
        // 4. 创建应用服务
        appServiceDTO.setSynchro(true);
        appServiceDTO.setFailed(false);
        appServiceDTO.setActive(true);
        baseUpdate(appServiceDTO);
    }

    public void setExternalProjectHook(AppServiceDTO appServiceDTO, Integer projectId, String token, AppExternalConfigDTO appExternalConfigDTO) {
        ProjectHookDTO projectHookDTO = new ProjectHookDTO(false,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false);
        projectHookDTO.setEnableSslVerification(true);
        projectHookDTO.setProjectId(projectId);
        projectHookDTO.setToken(token);
        String uri = getWebhookUrl();
        projectHookDTO.setUrl(uri);

        appServiceDTO.setHookId(TypeUtil.objToLong(gitlabServiceClientOperator.createExternalWebHook(
                        projectId, appExternalConfigDTO, projectHookDTO)
                .getId()));
    }

    @NotNull
    private String getWebhookUrl() {
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook";
        return uri;
    }


    @Override
    public void operationAppServiceImport(DevOpsAppImportServicePayload devOpsAppServiceImportPayload) {
        // 准备相关的数据
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByGitlabAppGroupId(
                TypeUtil.objToInteger(devOpsAppServiceImportPayload.getGroupId()));
        AppServiceDTO appServiceDTO = baseQueryByCode(devOpsAppServiceImportPayload.getPath(),
                devopsProjectDTO.getIamProjectId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsProjectDTO.getIamProjectId());
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectByName(
                organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode(),
                appServiceDTO.getCode(),
                devOpsAppServiceImportPayload.getUserId(),
                false);
        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(devOpsAppServiceImportPayload.getGroupId(),
                    devOpsAppServiceImportPayload.getPath(),
                    devOpsAppServiceImportPayload.getUserId(), false);
        } else {
            checkGitlabProjectIdNotUsedBefore(gitlabProjectDO.getId());
        }
        devOpsAppServiceImportPayload.setGitlabProjectId(gitlabProjectDO.getId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(devOpsAppServiceImportPayload.getUserId()));


        // clone外部代码仓库
        String applicationDir = APPLICATION + GenerateUUID.generateUUID();

        // 更改默认仓库的ci文件为这个，避免导入应用时跑ci，导入完成后改回默认
        gitlabServiceClientOperator.updateProjectCiConfigPath(gitlabProjectDO.getId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), GitOpsConstants.TEMP_CI_CONFIG_PATH);

        if (devOpsAppServiceImportPayload.getTemplate() != null && devOpsAppServiceImportPayload.getTemplate()
                && StringUtils.hasText(devOpsAppServiceImportPayload.getRepositoryUrl())) {
            String[] tempUrl = devOpsAppServiceImportPayload.getRepositoryUrl().split(TEMP_MODAL);
            if (tempUrl.length < 2) {
                throw new CommonException(DEVOPS_TEMP_GIT_URL);
            }
            String templateVersion = tempUrl[1];
            String repositoryUrl = tempUrl[0];
            externalGitUtil.cloneAppMarket(applicationDir, templateVersion, repositoryUrl, devOpsAppServiceImportPayload.getAccessToken());
            File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));
            replaceParams(appServiceDTO.getCode(), organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode(), applicationDir, null, null, true);
            Git newGit = gitUtil.initGit(applicationWorkDir);
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getTenantNum()
                    + "-" + projectDTO.getDevopsComponentCode() + "/" + appServiceDTO.getCode() + ".git");
            String accessToken = getToken(devOpsAppServiceImportPayload.getGitlabProjectId(), applicationDir, userAttrDTO);
            try {
                gitUtil.commitAndPushForMaster(newGit, appServiceDTO.getRepoUrl(), templateVersion, accessToken);
            } catch (Exception e) {
                releaseResources(applicationWorkDir, newGit);
                throw e;
            }
            releaseResources(applicationWorkDir, newGit);
            // 保存devops_branch信息
            initBranch(devOpsAppServiceImportPayload, appServiceDTO, GitOpsConstants.MASTER);
        } else if (devOpsAppServiceImportPayload.getTemplate() != null && devOpsAppServiceImportPayload.getTemplate()
                && devOpsAppServiceImportPayload.getDevopsAppTemplateId() != null) {
            String applicationWorkPath = gitUtil.getWorkingDirectory(applicationDir);
            File applicationWorkDir = new File(applicationWorkPath);
            Git git;
            DevopsAppTemplateDTO appTemplateDTO = devopsAppTemplateMapper.selectByPrimaryKey(devOpsAppServiceImportPayload.getDevopsAppTemplateId());
            String oldAppServiceCode = appTemplateDTO.getCode();
            if (appTemplateDTO.getType().equals("P")) {
                // 预定义模块旧服务code是{{service.code}} 会覆盖新服务code 处理为空
                oldAppServiceCode = null;
                ClassPathResource cpr = new ClassPathResource(String.format("/app-template/%s", appTemplateDTO.getCode()) + ".zip");
                File zipFile = null;
                try {
                    InputStream inputStream = cpr.getInputStream();
                    zipFile = new File(applicationWorkPath + ".zip");
                    FileUtils.copyInputStreamToFile(cpr.getInputStream(), zipFile);
                    inputStream.close();
                    FileUtil.unpack(zipFile, applicationWorkDir);
                } catch (IOException e) {
                    throw new CommonException(e.getMessage());
                } finally {
                    FileUtil.deleteFile(zipFile);
                }
                git = gitUtil.initGit(applicationWorkDir);
            } else {
                UserAttrDTO gitlabAdminDTO = userAttrService.queryGitlabAdminByIamId();
                String pullToken = getToken(devOpsAppServiceImportPayload.getGitlabProjectId(), applicationDir, gitlabAdminDTO);
                git = gitUtil.cloneRepository(applicationWorkDir, appTemplateDTO.getGitlabUrl(), pullToken);
            }
            replaceParams(appServiceDTO.getCode(), organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode(), applicationWorkPath, oldAppServiceCode, devopsAppTemplateService.getTemplateGroupPath(appTemplateDTO.getId()), false);
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getTenantNum()
                    + "-" + projectDTO.getDevopsComponentCode() + "/" + appServiceDTO.getCode() + ".git");
            String accessToken = getToken(devOpsAppServiceImportPayload.getGitlabProjectId(), applicationDir, userAttrDTO);
            try {
                gitUtil.commitAndPushForMaster(git, appServiceDTO.getRepoUrl(), "init app from template", accessToken);
            } catch (Exception e) {
                throw new CommonException(DEVOPS_INIT_APP_FROM_TEMPLATE_FAILED, e);
            } finally {
                releaseResources(applicationWorkDir, git);
            }
            // 保存devops_branch信息
            initBranch(devOpsAppServiceImportPayload, appServiceDTO, GitOpsConstants.MASTER);
        } else {
            Git repositoryGit = externalGitUtil.cloneRepository(applicationDir, devOpsAppServiceImportPayload.getRepositoryUrl(), devOpsAppServiceImportPayload.getAccessToken(), devOpsAppServiceImportPayload.getUsername(), devOpsAppServiceImportPayload.getPassword());
            // 设置Application对应的gitlab项目的仓库地址
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getTenantNum()
                    + "-" + projectDTO.getDevopsComponentCode() + "/" + appServiceDTO.getCode() + ".git");

            File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));

            String protectedBranchName = null;

            try {
                List<Ref> refs = repositoryGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                // 获取push代码所需的access token
                String accessToken = getToken(devOpsAppServiceImportPayload.getGitlabProjectId(), applicationDir, userAttrDTO);

                for (Ref ref : refs) {
                    String branchName;
                    if (ref.getName().contains(Constants.R_HEADS)) {
                        branchName = ref.getName().replace("refs/heads/", "");
                        // 当前的本地的 refs/heads/ 内的引用是保护分支的名称，大部分保护分支是master，不排除develop等其他分支的可能
                        protectedBranchName = branchName;
                    } else {
                        branchName = ref.getName().replace("refs/remotes/origin/", "");
                    }

                    // 跳过对活跃本地分支A: /refs/heads/A 和 /refs/remotes/origin/A 之间的第二次重复的推送
                    if (branchName.equals(protectedBranchName) && ref.getName().contains(Constants.R_REMOTES)) {
                        continue;
                    }

                    if (ref.getName().contains(Constants.R_REMOTES)) {
                        repositoryGit.checkout().setCreateBranch(true).setName(branchName).setStartPoint(ref.getName()).call();
                    }

                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>import branch name is {} <<<<<<<<<<<<<<<<<<<<<", branchName);

                    BranchDTO branchDTO = gitlabServiceClientOperator.queryBranch(gitlabProjectDO.getId(), branchName);
                    if (branchDTO.getName() == null) {
                        try {
                            // 提交并推代码
                            gitUtil.push(repositoryGit, appServiceDTO.getRepoUrl(), accessToken, branchName);
                        } catch (CommonException e) {
                            releaseResources(applicationWorkDir, repositoryGit);
                            throw e;
                        }
                    }
                    initBranch(devOpsAppServiceImportPayload, appServiceDTO, branchName);
                }

                BranchDTO branchDTO = gitlabServiceClientOperator.queryBranch(gitlabProjectDO.getId(), protectedBranchName);
                //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                if (!branchDTO.getProtected()) {
                    try {
                        gitlabServiceClientOperator.createProtectBranch(devOpsAppServiceImportPayload.getGitlabProjectId(), protectedBranchName, AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(), devOpsAppServiceImportPayload.getUserId());
                    } catch (CommonException e) {
                        // 出现异常时重试一次
                        if (!gitlabServiceClientOperator.queryBranch(gitlabProjectDO.getId(), protectedBranchName).getProtected()) {
                            gitlabServiceClientOperator.createProtectBranch(devOpsAppServiceImportPayload.getGitlabProjectId(), protectedBranchName, AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(), devOpsAppServiceImportPayload.getUserId());
                        }
                    }
                }

                // 将所有的tag推送到远程
                List<Ref> tags = repositoryGit.tagList().call();
                for (Ref tag : tags) {
                    gitUtil.pushLocalTag(repositoryGit, appServiceDTO.getRepoUrl(), accessToken, tag.getName());
                }

            } catch (GitAPIException e) {
                LOGGER.error("Failed to import external application.");
                LOGGER.error("GitAPIException: ", e);
            }

            releaseResources(applicationWorkDir, repositoryGit);
        }

        // 将ci文件位置改回默认
        gitlabServiceClientOperator.updateProjectCiConfigPath(gitlabProjectDO.getId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), GitOpsConstants.DEFAULT_CI_CONFIG_PATH);

        try {
            // 设置application的属性
            String applicationServiceToken = appServiceDTO.getToken();
            getApplicationToken(applicationServiceToken, gitlabProjectDO.getId(), devOpsAppServiceImportPayload.getUserId());
            appServiceDTO.setGitlabProjectId(TypeUtil.objToInteger(devOpsAppServiceImportPayload.getGitlabProjectId()));
            appServiceDTO.setSynchro(true);
            appServiceDTO.setFailed(false);

            // set project hook id for application
            setProjectHook(appServiceDTO, gitlabProjectDO.getId(), applicationServiceToken, devOpsAppServiceImportPayload.getUserId());

            appServiceMapper.updateByIdSelectiveWithoutAudit(appServiceDTO);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_CREATE_APP_FAIL,
            description = "Devops设置application状态为创建失败(devops set app status create err)", inputSchema = "{}")
    public void setAppErrStatus(String input, Long projectId, Long appServiceId) {
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_APP_FAIL),
                builder -> builder
                        .withJson(input)
                        .withRefId(String.valueOf(appServiceId))
                        .withSourceId(projectId));
    }

    @Override
    public String queryFile(String token) {
        AppServiceDTO appServiceDTO = baseQueryByToken(token);
        if (appServiceDTO == null) {
            return null;
        }
        try {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            DevopsConfigDTO harborConfigDTO = harborService.queryRepoConfigToDevopsConfig(projectDTO.getId(), appServiceDTO.getId(), AUTHTYPE_PUSH);
            ConfigVO harborProjectConfig = gson.fromJson(harborConfigDTO.getConfig(), ConfigVO.class);
            Map<String, String> params = new HashMap<>();
            String groupName = organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode();
            params.put("{{ SONAR_GROUP_NAME }}", groupName);
            if (harborProjectConfig.getProject() != null) {
                groupName = harborProjectConfig.getProject();
            }
            String dockerUrl = harborProjectConfig.getUrl().replace("http://", "").replace("https://", "");
            dockerUrl = dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;
            DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, SONAR_NAME);
            if (sonarConfig != null) {
                params.put("{{ SONAR_LOGIN }}", StringUtils.hasText(analysisUserToken) ? analysisUserToken : sonarConfig.getConfig());
                params.put("{{ SONAR_URL }}", sonarqubeUrl);
            } else {
                params.put("{{ SONAR_LOGIN }}", "");
                params.put("{{ SONAR_URL }}", "");
            }
            params.put("{{ SONAR_PROJECT_KEY }}", organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode() + ":" + appServiceDTO.getCode());
            params.put("{{ GROUP_NAME }}", groupName);
            params.put("{{ PROJECT_NAME }}", appServiceDTO.getCode());
            params.put("{{ PRO_CODE }}", projectDTO.getDevopsComponentCode());
            params.put("{{ ORG_CODE }}", organizationDTO.getTenantNum());
            params.put("{{ DOCKER_REGISTRY }}", dockerUrl);
            params.put("{{ DOCKER_USERNAME }}", "'" + harborProjectConfig.getUserName() + "'");
            params.put("{{ DOCKER_PASSWORD }}", "'" + harborProjectConfig.getPassword() + "'");
            params.put("{{ HARBOR_CONFIG_ID }}", harborConfigDTO.getId().toString());
            params.put("{{ REPO_TYPE }}", harborConfigDTO.getType());
            params.put("{{ CHOERODON_URL }}", gatewayUrl);
            String ciStr = FileUtil.replaceReturnString(CI_FILE_TEMPLATE, params);
            StringBuilder stringBuilder = new StringBuilder(ciStr);

            // 查询应用服务关联的流水线, 添加自定义函数
            CiCdPipelineDTO ciCdPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
            if (ciCdPipelineDTO != null) {
                List<DevopsCiPipelineFunctionDTO> functionDTOS = new ArrayList<>();
                List<DevopsCiPipelineFunctionDTO> defaultCiPipelineFunctionDTOS = devopsCiPipelineFunctionService.listFunctionsByDevopsPipelineId(PipelineConstants.DEFAULT_CI_PIPELINE_FUNCTION_ID);
                List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOS = devopsCiPipelineFunctionService.listFunctionsByDevopsPipelineId(ciCdPipelineDTO.getId());
                functionDTOS.addAll(defaultCiPipelineFunctionDTOS);
                functionDTOS.addAll(devopsCiPipelineFunctionDTOS);
                stringBuilder.append(System.lineSeparator());
                if (!CollectionUtils.isEmpty(functionDTOS)) {
                    functionDTOS.forEach(functionDTO -> stringBuilder.append(functionDTO.getScript()).append(System.lineSeparator()));
                }
            }
            return stringBuilder.toString();
        } catch (CommonException e) {
            throw new DevopsCiInvalidException(e.getCode(), e, e.getParameters());
        }
    }

    @Override
    public List<AppServiceCodeVO> listByEnvId(Long projectId, Long envId, String status, Long appServiceId) {
        List<AppServiceCodeVO> applicationCodeVOS = ConvertUtils
                .convertList(baseListByEnvId(projectId, envId, status),
                        AppServiceCodeVO.class);
        if (appServiceId != null) {
            AppServiceDTO appServiceDTO = baseQuery(appServiceId);
            AppServiceCodeVO applicationCodeVO = new AppServiceCodeVO();
            BeanUtils.copyProperties(appServiceDTO, applicationCodeVO);
            for (int i = 0; i < applicationCodeVOS.size(); i++) {
                if (applicationCodeVOS.get(i).getId().equals(appServiceDTO.getId())) {
                    applicationCodeVOS.remove(applicationCodeVOS.get(i));
                }
            }
            applicationCodeVOS.add(0, applicationCodeVO);
        }
        return applicationCodeVOS;
    }

    @Override
    public Page<AppServiceCodeVO> pageByIds(Long projectId, Long envId, Long appServiceId, PageRequest pageable) {
        return ConvertUtils.convertPage(basePageByEnvId(projectId, envId, appServiceId, pageable),
                AppServiceCodeVO.class);
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
    public Boolean validateRepositoryUrlAndUsernameAndPassword(String repositoryUrl, String username, String password) {
        if (!REPOSITORY_URL_PATTERN.matcher(repositoryUrl).matches()) {
            return Boolean.FALSE;
        }
        return GitUtil.validRepositoryUrl(repositoryUrl, username, password);
    }

    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_IMPORT_GITLAB_PROJECT,
            description = "Devops从外部代码平台导入到gitlab项目", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public AppServiceRepVO importApp(Long projectId, AppServiceImportVO appServiceImportVO, Boolean isTemplate) {
        return saveAppService(projectId, appServiceImportVO, isTemplate, false);
    }

    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_IMPORT_GITLAB_PROJECT,
            description = "Devops从外部代码平台导入到gitlab项目", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public AppServiceRepVO importFromGeneralGit(Long projectId, AppServiceImportVO appServiceImportVO) {
        return saveAppService(projectId, appServiceImportVO, null, false);
    }

    private AppServiceRepVO saveAppService(Long projectId, AppServiceImportVO appServiceImportVO, Boolean isTemplate, Boolean importFromGeneralGit) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        appServiceUtils.checkEnableCreateAppSvcOrThrowE(projectDTO.getOrganizationId(), projectId, 1);
        // 获取当前操作的用户的信息
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验application信息的格式
        ApplicationValidator.checkApplicationService(appServiceImportVO.getCode());
        // 校验名称唯一性
        checkName(projectId, appServiceImportVO.getName());

        // 校验code唯一性
        checkCode(projectId, appServiceImportVO.getCode());

        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setProjectId(projectId);
        appServiceDTO.setName(appServiceImportVO.getName());
        appServiceDTO.setCode(appServiceImportVO.getCode());

        // 校验repository（和token） 地址是否有效
        if (importFromGeneralGit) {
            checkRepositoryUrlAndUsernameAndPassword(appServiceImportVO.getRepositoryUrl(), appServiceImportVO.getUsername(), appServiceImportVO.getPassword());
        } else if (isTemplate == null || !isTemplate) {
            GitPlatformType gitPlatformType = GitPlatformType.from(appServiceImportVO.getPlatformType());
            checkRepositoryUrlAndToken(gitPlatformType, appServiceImportVO.getRepositoryUrl(), appServiceImportVO.getAccessToken());
        }

        appServiceDTO = fromImportVoToDto(appServiceImportVO);
        appServiceDTO.setToken(GenerateUUID.generateUUID());
        appServiceDTO.setProjectId(projectId);
        appServiceDTO.setActive(true);
        appServiceDTO.setSynchro(false);

        // 查询创建应用所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(appServiceDTO.getProjectId());

        boolean isGitlabRoot = false;

        if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
            // 如果这边表存了gitlabAdmin这个字段,那么gitlabUserId就不会为空,所以不判断此字段为空
            isGitlabRoot = gitlabServiceClientOperator.isGitlabAdmin(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        if (!isGitlabRoot) {
            MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));

            // 校验用户的gitlab权限
            if (memberDTO == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.toValue())) {
                throw new CommonException(DEVOPS_USER_NOT_GITLAB_OWNER);
            }
        }

        // 创建应用服务
        appServiceDTO = baseCreate(appServiceDTO);
        Long appServiceId = appServiceDTO.getId();

        //创建saga payload
        DevOpsAppImportServicePayload devOpsAppImportServicePayload = new DevOpsAppImportServicePayload();
        devOpsAppImportServicePayload.setPath(appServiceDTO.getCode());
        devOpsAppImportServicePayload.setOrganizationId(projectDTO.getOrganizationId());
        devOpsAppImportServicePayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        devOpsAppImportServicePayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
        devOpsAppImportServicePayload.setUserIds(Collections.emptyList());
        devOpsAppImportServicePayload.setAppServiceId(appServiceDTO.getId());
        devOpsAppImportServicePayload.setIamProjectId(projectId);
        devOpsAppImportServicePayload.setRepositoryUrl(appServiceImportVO.getRepositoryUrl());
        devOpsAppImportServicePayload.setAccessToken(appServiceImportVO.getAccessToken());
        devOpsAppImportServicePayload.setDevopsAppTemplateId(appServiceImportVO.getDevopsAppTemplateId());
        devOpsAppImportServicePayload.setTemplate(isTemplate);
        devOpsAppImportServicePayload.setUsername(appServiceImportVO.getUsername());
        devOpsAppImportServicePayload.setPassword(appServiceImportVO.getPassword());
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_IMPORT_GITLAB_PROJECT),
                builder -> builder
                        .withPayloadAndSerialize(devOpsAppImportServicePayload)
                        .withRefId(String.valueOf(appServiceId))
                        .withSourceId(projectId));

        return ConvertUtils.convertObject(baseQuery(appServiceId), AppServiceRepVO.class);
    }

    @Override
    public AppServiceRepVO queryByCode(Long projectId, String code) {
        return ConvertUtils.convertObject(baseQueryByCode(code, projectId), AppServiceRepVO.class);
    }

    @Override
    public Boolean checkChartOnOrganization(String url, @Nullable String username, @Nullable String password) {
        url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(url);
        configurationProperties.setType(CHART);
        if (username != null && password != null) {
            configurationProperties.setUsername(username);
            configurationProperties.setPassword(password);
        }
        ChartClient chartClient = null;

        try {
            Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
            chartClient = retrofit.create(ChartClient.class);
            // 获取健康检查信息是不需要认证信息的
            Call<Object> getHealth = chartClient.getHealth();
            getHealth.execute();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new CommonException(DEVOPS_CHART_URL_BASE, e.getMessage());
            } else {
                throw new CommonException(DEVOPS_CHART_NOT_AVAILABLE, e.getMessage());
            }
        }

        // 验证用户名密码
        Response<Void> response = null;
        try {
            // 获取首页html需要认证信息
            Call<Void> getHomePage = chartClient.getHomePage();
            response = getHomePage.execute();
        } catch (Exception ex) {
            throw new CommonException(DEVOPS_CHART_AUTHENTICATION_FAILED);
        }
        if (response != null && !response.isSuccessful()) {
            throw new CommonException(DEVOPS_CHART_AUTHENTICATION_FAILED);
        }

        return true;
    }

    @Override
    public CheckInfoVO checkChart(Long projectId, String url, @Nullable String username, @Nullable String password) {
        CheckInfoVO checkInfoVO = new CheckInfoVO();
        url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        URL processedUrl;
        try {
            processedUrl = new URL(url);
        } catch (Exception e) {
            checkInfoVO.setSuccess(false);
            checkInfoVO.setErrMsg("helm仓库地址不正确");
            return checkInfoVO;
        }
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(processedUrl.getProtocol() + "://" + processedUrl.getHost());
        configurationProperties.setType(CHART);
        if (username != null && password != null) {
            configurationProperties.setUsername(username);
            configurationProperties.setPassword(password);
        }
        ChartClient chartClient = null;

        Response<String> result;
        try {
            String[] params = processedUrl.getPath().split("/");
            if (params.length != 3) {
                checkInfoVO.setSuccess(false);
                checkInfoVO.setErrMsg("helm仓库地址无效，应该类似：http://localhost:8080/org1/repoa");
                return checkInfoVO;
            }
            Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties, new RetrofitHandler.StringConverter());
            chartClient = retrofit.create(ChartClient.class);
            Call<String> getIndex = chartClient.getIndex(params[1], params[2]);
            result = getIndex.execute();
        } catch (Exception ex) {
            checkInfoVO.setSuccess(false);
            checkInfoVO.setErrMsg("无法访问helm仓库:" + ex.getMessage());
            return checkInfoVO;
        }
        if (result != null && result.isSuccessful()) {
            checkInfoVO.setSuccess(true);
            return checkInfoVO;
        }
        if (result != null && (result.code() > 400 && result.code() < 500)) {
            checkInfoVO.setSuccess(false);
            checkInfoVO.setErrMsg("账号或密码不正确");
            return checkInfoVO;
        }
        checkInfoVO.setSuccess(false);
        checkInfoVO.setErrMsg("测试连接失败");
        return checkInfoVO;
    }

    @Override
    public SonarContentsVO getSonarContent(Long projectId, Long appServiceId) {
        //没有使用sonarqube直接返回空对象
        if (sonarqubeUrl.equals("")) {
            return new SonarContentsVO();
        }
        SonarContentsVO sonarContentsVO = new SonarContentsVO();
        List<SonarContentVO> sonarContentVOS = new ArrayList<>();
        AppServiceDTO appServiceDTO = baseQuery(appServiceId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());


        //初始化sonarClient
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = getSonarKey(appServiceDTO.getCode(), projectDTO.getDevopsComponentCode(), organization.getTenantNum());
        sonarqubeUrl = sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/";

        //校验sonarqube地址是否正确
        try {
            sonarClient.getUser().execute();
        } catch (IOException e) {
            return new SonarContentsVO();
        }

        try {
            //初始化查询参数
            Map<String, String> queryContentMap = new HashMap<>();
            queryContentMap.put("additionalFields", "metrics,periods");
            queryContentMap.put("component", key);
            queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,new_bugs,new_vulnerabilities,sqale_index,code_smells,new_technical_debt,new_code_smells,coverage,tests,new_coverage,duplicated_lines_density,duplicated_blocks,new_duplicated_lines_density,ncloc,ncloc_language_distribution");

            //根据project-key查询sonarqube项目内容
            Response<SonarComponent> sonarComponentResponse = sonarClient.getSonarComponent(queryContentMap).execute();
            if (sonarComponentResponse.raw().code() != 200) {
                if (sonarComponentResponse.raw().code() == 404) {
                    return new SonarContentsVO();
                }
                if (sonarComponentResponse.raw().code() == 401) {
                    throw new CommonException(DEVOPS_SONARQUBE_USER);
                }
                throw new CommonException(sonarComponentResponse.errorBody().string());
            }
            if (sonarComponentResponse.body() == null) {
                return new SonarContentsVO();
            }
            if (sonarComponentResponse.body().getPeriods() != null && !sonarComponentResponse.body().getPeriods().isEmpty()) {
                sonarContentsVO.setDate(sonarComponentResponse.body().getPeriods().get(0).getDate());
                sonarContentsVO.setMode(sonarComponentResponse.body().getPeriods().get(0).getMode());
                sonarContentsVO.setParameter(sonarComponentResponse.body().getPeriods().get(0).getParameter());
            } else {
                Map<String, String> analyseMap = new HashMap<>();
                analyseMap.put("project", key);
                analyseMap.put("ps", "3");

                //查询上一次的分析时间
                Response<SonarAnalyses> sonarAnalyses = sonarClient.getAnalyses(analyseMap).execute();
                if (sonarAnalyses.raw().code() == 200 && sonarAnalyses.body().getAnalyses() != null && !sonarAnalyses.body().getAnalyses().isEmpty()) {
                    sonarContentsVO.setDate(sonarAnalyses.body().getAnalyses().get(0).getDate());
                }
            }
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("component", key);
            Response<SonarComponent> sonarAnalysisDate = sonarClient.getSonarAnalysisDate(queryMap).execute();
            if (sonarAnalysisDate.raw().code() == 200 && sonarAnalysisDate.body() != null) {
                sonarContentsVO.setDate(sonarAnalysisDate.body().getAnalysisDate());
            }
            sonarContentsVO.setDate(getTimestampTimeV17(sonarContentsVO.getDate()));

            //分类型对sonarqube project查询返回的结果进行处理
            sonarComponentResponse.body().getComponent().getMeasures().forEach(measure -> {
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
                                BigDecimal codeSmellDecimal = BigDecimal.valueOf(result);
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
                        double newResult = TypeUtil.objToLong(measure.getPeriods().get(0).getValue()) / 1000.0;
                        if (newResult > 0) {
                            if (TypeUtil.objToLong(measure.getPeriods().get(0).getValue()) % 1000 == 0) {
                                newCodeSmells.setValue(String.format("%sK", newResult));
                            } else {
                                BigDecimal codeSmellDecimal = BigDecimal.valueOf(newResult);
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
                            BigDecimal b = BigDecimal.valueOf(TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()));
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
                                BigDecimal nclocDecimal = BigDecimal.valueOf(nclocResult);
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
                        QualityGateResult qualityGateResult = gson.fromJson(measure.getValue(), QualityGateResult.class);
                        String sonarProjectKey = getSonarKey(appServiceDTO.getCode(), projectDTO.getDevopsComponentCode(), organization.getTenantNum());
                        Boolean sonarQualityExists = devopsCiSonarQualityGateService.qualityGateExistsByName(sonarProjectKey);
                        if (Boolean.TRUE.equals(sonarQualityExists)) {
                            sonarContentsVO.setDevopsCiSonarQualityGateVO(devopsCiSonarQualityGateService.buildFromSonarResult(qualityGateResult));
                        }
                        sonarContentsVO.setStatus(qualityGateResult.getLevel());
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

    public static String getSonarKey(String appServiceCode, String projectDevopsComponentCode, String organiztionCode) {
        return String.format(SONAR_KEY, organiztionCode, projectDevopsComponentCode, appServiceCode);
    }



    public String getTimestampTimeV17(String str) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(str)) {
            return "";
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
        Date date = null;
        try {
            date = dateFormat.parse(str);
        } catch (ParseException e) {
            LOGGER.error("parse error", e);
            return str;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        GregorianCalendar ca = new GregorianCalendar(TimeZone.getTimeZone("GMT 00:00"));
        ca.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getDefault());
        return format.format(ca.getTime());
    }

    @Override
    public SonarTableVO getSonarTable(Long projectId, Long appServiceId, String type, Date startTime, Date endTime) {
        if (sonarqubeUrl.equals("")) {
            return new SonarTableVO();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(endTime);
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = c.getTime();
        SonarTableVO sonarTableVO = new SonarTableVO();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
        AppServiceDTO applicationDTO = baseQuery(appServiceId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = getSonarKey(applicationDTO.getCode(), projectDTO.getDevopsComponentCode(), organizationDTO.getTenantNum());
        sonarqubeUrl = sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/";
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("component", key);
        queryMap.put("ps", "1000");
        if (ISSUE.equals(type)) {
            queryMap.put(METRICS, "bugs,code_smells,vulnerabilities");
            try {
                Response<SonarTables> sonarTablesResponse = sonarClient.getSonarTables(queryMap).execute();
                if (sonarTablesResponse.raw().code() != 200) {
                    if (sonarTablesResponse.raw().code() == 404) {
                        return new SonarTableVO();
                    }
                    if (sonarTablesResponse.raw().code() == 401) {
                        throw new CommonException(DEVOPS_SONARQUBE_USER);
                    }
                    throw new CommonException(sonarTablesResponse.errorBody().string());
                }
                List<String> bugs = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                List<String> codeSmells = new ArrayList<>();
                List<String> vulnerabilities = new ArrayList<>();
                sonarTablesResponse.body().getMeasures().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.BUGS.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistory ->
                                getHistory(startTime, tomorrow, sdf, sonarHistory)
                        ).forEach(sonarHistory -> {
                            bugs.add(sonarHistory.getValue());
                            dates.add(sonarHistory.getDate());
                        });
                        sonarTableVO.setDates(dates);
                        sonarTableVO.setBugs(bugs);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.CODE_SMELLS.getType())) {
                        sonarTableMeasure.getHistory()
                                .stream()
                                .filter(sonarHistory -> getHistory(startTime, tomorrow, sdf, sonarHistory))
                                .forEach(sonarHistory -> codeSmells.add(sonarHistory.getValue()));
                        sonarTableVO.setCodeSmells(codeSmells);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.VULNERABILITIES.getType())) {
                        sonarTableMeasure.getHistory()
                                .stream()
                                .filter(sonarHistory -> getHistory(startTime, tomorrow, sdf, sonarHistory))
                                .forEach(sonarHistory -> vulnerabilities.add(sonarHistory.getValue()));
                        sonarTableVO.setVulnerabilities(vulnerabilities);
                    }
                });
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }
        if (COVERAGE.equals(type)) {
            queryMap.put(METRICS, "lines_to_cover,uncovered_lines,coverage");
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
                sonarTablesResponse.body().getMeasures().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.COVERAGE.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistory ->
                                getHistory(startTime, tomorrow, sdf, sonarHistory)
                        ).forEach(sonarHistory -> coverage.add(sonarHistory.getValue()));
                        sonarTableVO.setCoverage(coverage);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.LINES_TO_COVER.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistory ->
                                getHistory(startTime, tomorrow, sdf, sonarHistory)
                        ).forEach(sonarHistory -> {
                            linesToCover.add(sonarHistory.getValue());
                            dates.add(sonarHistory.getDate());
                        });
                        sonarTableVO.setDates(dates);
                        sonarTableVO.setLinesToCover(linesToCover);
                    }

                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.UNCOVERED_LINES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistory ->
                                getHistory(startTime, tomorrow, sdf, sonarHistory)
                        ).forEach(sonarHistory -> unCoverLines.add(sonarHistory.getValue()));
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
            queryMap.put(METRICS, "ncloc,duplicated_lines,duplicated_lines_density");
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
                sonarTablesResponse.body().getMeasures().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.NCLOC.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistory ->
                                getHistory(startTime, tomorrow, sdf, sonarHistory)
                        ).forEach(sonarHistory -> {
                            nclocs.add(sonarHistory.getValue());
                            dates.add(sonarHistory.getDate());
                        });
                        sonarTableVO.setNclocs(nclocs);
                        sonarTableVO.setDates(dates);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistory ->
                                getHistory(startTime, tomorrow, sdf, sonarHistory)
                        ).forEach(sonarHistory ->
                                duplicatedLines.add(sonarHistory.getValue())
                        );
                        sonarTableVO.setDuplicatedLines(duplicatedLines);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES_DENSITY.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistory ->
                                getHistory(startTime, tomorrow, sdf, sonarHistory)
                        ).forEach(sonarHistory -> duplicatedLinesRate.add(sonarHistory.getValue()));
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
    public Page<AppServiceRepVO> pageShareAppService(Long projectId, boolean doPage, PageRequest pageable, String searchParam) {
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(searchParam);
        Long organizationId = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId).getOrganizationId();
        List<Long> appServiceIds = new ArrayList<>();
        baseServiceClientOperator.listIamProjectByOrgId(organizationId).stream()
                .filter(projectDTO -> !projectId.equals(projectDTO.getId()))
                .forEach(proId ->
                        baseListAll(proId.getId()).forEach(appServiceDTO -> appServiceIds.add(appServiceDTO.getId()))
                );
        Page<AppServiceDTO> applicationServiceDTOPageInfo = new Page<>();
        if (doPage) {
            applicationServiceDTOPageInfo = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> appServiceMapper.listShareApplicationService(appServiceIds, projectId, null, TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
        } else {
            applicationServiceDTOPageInfo.setContent(appServiceMapper.listShareApplicationService(appServiceIds, projectId, null, TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
        }
        return ConvertUtils.convertPage(applicationServiceDTOPageInfo, AppServiceRepVO.class);
    }

    @Override
    public Page<DevopsUserPermissionVO> pagePermissionUsers(Long projectId, Long appServiceId, PageRequest pageable, String
            searchParam) {
        Map<String, Object> searchParamMap;
        RdmMemberQueryDTO queryDTO = new RdmMemberQueryDTO();
        // 处理搜索参数
        if (!org.springframework.util.StringUtils.isEmpty(searchParam)) {
            Map maps = gson.fromJson(searchParam, Map.class);
            searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            List<String> list = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
            if (!CollectionUtils.isEmpty(list)) {
                String[] arrayParams = new String[list.size()];
                list.toArray(arrayParams);
                queryDTO.setParams(arrayParams[0]);
            }
            if (!CollectionUtils.isEmpty(searchParamMap)) {
                if (searchParamMap.get(LOGIN_NAME) != null) {
                    String loginName = TypeUtil.objToString(searchParamMap.get(LOGIN_NAME));
                    queryDTO.setLoginName(loginName);

                }
                if (searchParamMap.get(REAL_NAME) != null) {
                    String realName = TypeUtil.objToString(searchParamMap.get(REAL_NAME));
                    queryDTO.setRealName(realName);
                }
            }
        }
        queryDTO.setRepositoryIds(Collections.singleton(appServiceId));
        Page<RdmMemberViewDTO> permissionVOPage = PageInfoUtil.createPageFromList(new ArrayList<>(hrdsCodeRepoClientOperator.listMembers(null, projectId, queryDTO)), pageable);
        return ConvertUtils.convertPage(permissionVOPage, this::remDTOToPermissionVO);
    }

    @Override
    public List<ProjectVO> listProjects(Long organizationId, Long projectId, String params) {
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(organizationId, null, null, params).stream()
                .filter(ProjectDTO::getEnabled)
                .filter(v -> !projectId.equals(v.getId())).collect(Collectors.toList());
        List<ProjectVO> projectVOS = ConvertUtils.convertList(projectDTOS, ProjectVO.class);
        if (projectVOS == null) {
            return new ArrayList<>();
        }
        return projectVOS;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_IMPORT_INTERNAL_APPLICATION_SERVICE,
            description = "Devops创建应用服务", inputSchema = "{}")
    public void importAppServiceInternal(Long projectId, List<ApplicationImportInternalVO> importInternalVOS) {
        List<AppServiceImportPayload> importPayloadList = createAppService(projectId, importInternalVOS);
        importPayloadList.forEach(payload -> producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_IMPORT_INTERNAL_APPLICATION_SERVICE)
                        .withPayloadAndSerialize(payload)
                        .withRefId(String.valueOf(payload.getAppServiceId()))
                        .withSourceId(projectId),
                builder -> {
                }));
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AppServiceImportPayload> createAppService(Long projectId, List<ApplicationImportInternalVO> importInternalVOS) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        appServiceUtils.checkEnableCreateAppSvcOrThrowE(projectId, importInternalVOS.size());
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        List<AppServiceImportPayload> importPayloadList = new ArrayList<>();
        importInternalVOS.forEach(importInternalVO -> {
            AppServiceDTO appServiceDTO = new AppServiceDTO();
            appServiceDTO.setProjectId(projectId);
            if (importInternalVO.getAppCode() != null) {
                // 校验application信息的格式
                ApplicationValidator.checkApplicationService(importInternalVO.getAppCode());

                // 校验名称唯一性
                checkName(projectId, importInternalVO.getAppName());

                // 校验code唯一性
                checkCode(projectId, importInternalVO.getAppCode());

                appServiceDTO.setCode(importInternalVO.getAppCode());
                appServiceDTO.setName(importInternalVO.getAppName());
            } else {
                AppServiceDTO oldAppService = baseQuery(importInternalVO.getAppServiceId());
                appServiceDTO.setCode(oldAppService.getCode());
                appServiceDTO.setName(oldAppService.getName());
            }

            appServiceDTO.setToken(GenerateUUID.generateUUID());
            appServiceDTO.setProjectId(projectId);
            appServiceDTO.setActive(true);
            appServiceDTO.setSynchro(false);
            appServiceDTO.setType(NORMAL);
            appServiceDTO = baseCreate(appServiceDTO);

            // 查询创建应用所在的gitlab应用组
            DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);

            boolean isGitlabRoot = false;

            if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
                // 如果这边表存了gitlabAdmin这个字段,那么gitlabUserId就不会为空,所以不判断此字段为空
                isGitlabRoot = gitlabServiceClientOperator.isGitlabAdmin(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }

            if (!isGitlabRoot) {
                // 查询创建应用所在的gitlab应用组 用户权限
                MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(
                        TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));

                if (memberDTO == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.value)) {

                    throw new CommonException(DEVOPS_USER_NOT_GITLAB_OWNER);
                }
            }

            AppServiceImportPayload appServiceImportPayload = new AppServiceImportPayload();
            appServiceImportPayload.setAppServiceId(appServiceDTO.getId());
            appServiceImportPayload.setGitlabGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
            appServiceImportPayload.setIamUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            appServiceImportPayload.setVersionId(importInternalVO.getVersionId());
            appServiceImportPayload.setOrgCode(organizationDTO.getTenantNum());
            appServiceImportPayload.setProjectId(projectId);
            appServiceImportPayload.setProCode(projectDTO.getDevopsComponentCode());
            appServiceImportPayload.setOldAppServiceId(importInternalVO.getAppServiceId());
            appServiceImportPayload.setDeployObjectId(importInternalVO.getDeployObjectId());
            importPayloadList.add(appServiceImportPayload);
        });
        return importPayloadList;
    }

    @Override
    public void importMarketAppServiceGitlab(AppServiceImportPayload appServiceImportPayload) {
        // TODO: 2021/3/3  方法待抽取
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceImportPayload.getAppServiceId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(appServiceImportPayload.getIamUserId());

        String newGroupName = appServiceImportPayload.getOrgCode() + "-" + appServiceImportPayload.getProCode();
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectByName(
                newGroupName,
                appServiceDTO.getCode(),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()),
                true);
        //创建gitlab 应用 一个空的库
        if (gitlabProjectDTO.getId() == null) {
            gitlabProjectDTO = gitlabServiceClientOperator.createProject(
                    appServiceImportPayload.getGitlabGroupId(),
                    appServiceDTO.getCode(),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), false);
        } else {
            checkGitlabProjectIdNotUsedBefore(gitlabProjectDTO.getId());
        }

        appServiceDTO.setGitlabProjectId(gitlabProjectDTO.getId());
        String applicationServiceToken = appServiceDTO.getToken();
        getApplicationToken(applicationServiceToken, appServiceDTO.getGitlabProjectId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        appServiceDTO.setSynchro(true);
        appServiceDTO.setFailed(false);
        setProjectHook(appServiceDTO, appServiceDTO.getGitlabProjectId(), applicationServiceToken, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));

        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String repositoryUrl = repoUrl + appServiceImportPayload.getOrgCode() + "-" + appServiceImportPayload.getProCode() + "/" + appServiceDTO.getCode() + GIT;
        //将文件服务器上的源码下载下来推上去
        downloadSourceCodeAndPush(appServiceDTO, userAttrDTO, appServiceImportPayload, repositoryUrl, newGroupName);
        appServiceMapper.updateByIdSelectiveWithoutAudit(appServiceDTO);
    }

    @Override
    public List<ResourceVO> listResourceByIds(Long organizationId, List<Long> projectIds) {
        List<ResourceVO> resourceVOList;
        if (CollectionUtils.isEmpty(projectIds)) {
            return new ArrayList<>();
        } else {
            resourceVOList = new ArrayList<>();
            Tenant tenant = baseServiceClientOperator.queryOrganizationById(organizationId);
            if (tenant == null) {
                return new ArrayList<>();
            }

            projectIds.forEach(t -> {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(t);
                if (projectDTO == null) {
                    return;
                }
                ResourceVO resourceVO = appServiceMapper.queryResourceById(t);
                if (resourceVO == null) {
                    resourceVO = new ResourceVO();
                    resourceVO.setCurrentAppService(0L);
                    resourceVO.setCurrentEnv(0L);
                    resourceVO.setCurrentCluster(0L);
                    resourceVO.setProjectId(t);
                    resourceVO.setCurrentGitlabCapacity(String.valueOf(0));
                } else {
                    GroupDTO groupDTO = queryGroupWithStatistics(tenant.getTenantNum(), projectDTO);
                    if (groupDTO != null && groupDTO.getStatistics() != null) {
                        if (groupDTO.getStatistics().getStorageSize() == 0) {
                            resourceVO.setCurrentGitlabCapacity(String.valueOf(0));
                        } else if (groupDTO.getStatistics().getStorageSize() < ONE_GB_TO_B && groupDTO.getStatistics().getStorageSize() > 0) {
                            resourceVO.setCurrentGitlabCapacity(String.format("%.2f", groupDTO.getStatistics().getStorageSize() / new BigDecimal(1024).pow(2).doubleValue()) + "MB");

                        } else if (groupDTO.getStatistics().getStorageSize() >= ONE_GB_TO_B) {
                            resourceVO.setCurrentGitlabCapacity(String.format("%.2f", groupDTO.getStatistics().getStorageSize() / new BigDecimal(1024).pow(3).doubleValue()) + "GB");
                        }
                    }
                }
                resourceVOList.add(resourceVO);
            });
            return resourceVOList;
        }
    }

    @Nullable
    private GroupDTO queryGroupWithStatistics(String tenantCode, ProjectDTO projectDTO) {
        //查询内置仓库的所有仓库的使用量
        DevopsProjectDTO record = new DevopsProjectDTO();
        record.setIamProjectId(projectDTO.getId());
        DevopsProjectDTO devopsProjectDTO = devopsProjectMapper.selectOne(record);
        //创建失败的项目和非devops类型的项目不统计
        if (devopsProjectDTO == null || devopsProjectDTO.getDevopsAppGroupId() == null) {
            return null;
        }
        UserAttrDTO userRecord = new UserAttrDTO();
        userRecord.setIamUserId(DetailsHelper.getUserDetails().getUserId());
        UserAttrDTO userAttrDTO = userAttrMapper.selectOne(userRecord);
        if (userAttrDTO == null || userAttrDTO.getGitlabUserId() == null) {
            return null;
        }
        String path = tenantCode + BaseConstants.Symbol.MIDDLE_LINE + projectDTO.getDevopsComponentCode();
        List<GroupDTO> groupDTOS = gitlabServiceClientOperator.queryGroupWithStatisticsByName(path, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), Boolean.TRUE);
        if (!CollectionUtils.isEmpty(groupDTOS)) {
            List<GroupDTO> projectGroups = groupDTOS.stream().filter(groupDTO -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(groupDTO.getPath(), path)).collect(toList());
            if (!CollectionUtils.isEmpty(projectGroups)) {
                return projectGroups.get(0);
            }

        }
        return null;
    }

    @Override
    public List<AppServiceSimpleVO> listByProjectIdAndCode(List<AppServiceSimpleVO> appServiceList) {
        // 1. 第一次过滤
        List<Long> projectIds = appServiceList.stream().map(AppServiceSimpleVO::getProjectId).collect(toList());
        List<String> appCodes = appServiceList.stream().map(AppServiceSimpleVO::getAppServiceCode).filter(Objects::nonNull).collect(toList());

        List<AppServiceSimpleVO> appServiceSimpleVOList = appServiceMapper.listByProjectIdsAndCodes(projectIds, appCodes);
        if (CollectionUtils.isEmpty(appCodes)) {
            return appServiceSimpleVOList;
        }
        // 2.第二次筛选
        Map<Long, List<String>> collectMap = appServiceList.stream()
                .collect(groupingBy(AppServiceSimpleVO::getProjectId, mapping(AppServiceSimpleVO::getAppServiceCode, toList())));
        if (!CollectionUtils.isEmpty(appServiceSimpleVOList)) {
            return appServiceSimpleVOList.stream().filter(v -> {
                List<String> codes = collectMap.get(v.getProjectId());
                if (CollectionUtils.isEmpty(codes) || !codes.contains(v.getAppServiceCode())) {
                    return false;
                } else {
                    return true;
                }
            }).collect(toList());
        }
        return new ArrayList<>();
    }

    @Override
    public Long countAppCountByOptions(Long projectId) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setProjectId(projectId);
        int selectCount = appServiceMapper.selectCount(appServiceDTO);
        return Long.valueOf(selectCount);
    }

    @Override
    public Page<AppServiceRepVO> applicationCenter(Long projectId, Long envId, String type, String params, PageRequest pageRequest) {
        //查询应用服务  env为0查询所有环境关联的应用服务，
        Page<AppServiceRepVO> appServiceRepVOS = PageHelper.doPageAndSort(pageRequest, () -> appServiceMapper.queryApplicationCenter(projectId, envId, type, params));
        List<AppServiceRepVO> appServiceRepVOSContent = appServiceRepVOS.getContent();
        if (CollectionUtils.isEmpty(appServiceRepVOSContent)) {
            return appServiceRepVOS;
        }
        // 处理应用服务的来源 仓库地址 版本等等的信息
        handAppServices(projectId, appServiceRepVOS);
        return appServiceRepVOS;
    }

    private void handAppServices(Long projectId, Page<AppServiceRepVO> appServiceRepVOS) {
        //筛出应用市场的服务，查询市场服务  应用市场类型的marketServiceId
        Map<Long, MarketServiceVO> longMarketServiceVOMap = queryMarketDeployObj(projectId, appServiceRepVOS);
        //处理最新版本,仓库地址
        //项目的来源处理：来源是这样的  如果是项目发布的  那么显示组织/项目   如果是中间件之类的就显示平台预置的
        Map<Long, MarketServiceVO> finalLongMarketServiceVOMap = longMarketServiceVOMap;
        appServiceRepVOS.getContent().forEach(appServiceRepVO -> {
            if (isMarketOrHzero(appServiceRepVO)) {
                //根据市场服务id查询已发布部署对象
                handMarketAppService(finalLongMarketServiceVOMap, appServiceRepVO);
            } else {
                //共享与本项目
                handNormalAppService(appServiceRepVO);
            }
        });
    }

    private boolean isMarketOrHzero(AppServiceRepVO appServiceRepVO) {
        return org.apache.commons.lang3.StringUtils.equalsIgnoreCase(appServiceRepVO.getSource(), ApplicationCenterEnum.MARKET.value())
                || org.apache.commons.lang3.StringUtils.equalsIgnoreCase(appServiceRepVO.getSource(), AppSourceType.HZERO.getValue());
    }

    private void handNormalAppService(AppServiceRepVO appServiceRepVO) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceRepVO.getId());
        if (!Objects.isNull(appServiceDTO)) {
            ImmutableProjectInfoVO info = baseServiceClientOperator.queryImmutableProjectInfo(appServiceDTO.getProjectId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
            if (!Objects.isNull(info)) {
                initApplicationParams(info, appServiceDTO, urlSlash);
                appServiceRepVO.setRepoUrl(appServiceDTO.getRepoUrl());
            }
            //判断是不是共享的应用服务 如果是要返回项目的来源
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(appServiceRepVO.getEnvId());
            if (!Objects.isNull(devopsEnvironmentDTO)
                    && !devopsEnvironmentDTO.getProjectId().equals(appServiceDTO.getProjectId())) {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
                appServiceRepVO.setShareProjectName(projectDTO.getName());
            }
            if (StringUtils.isEmpty(appServiceRepVO.getServiceName())) {
                appServiceRepVO.setServiceName(appServiceDTO.getName());
            }
            if (StringUtils.isEmpty(appServiceRepVO.getServiceCode())) {
                appServiceRepVO.setServiceCode(appServiceDTO.getCode());
            }
        }

        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
        appServiceVersionDTO.setAppServiceId(appServiceRepVO.getId());
        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.select(appServiceVersionDTO);
        if (!CollectionUtils.isEmpty(appServiceVersionDTOS)) {
            List<AppServiceVersionDTO> serviceVersionDTOS = appServiceVersionDTOS.stream().sorted(comparing(AppServiceVersionDTO::getId).reversed()).collect(toList());
            appServiceRepVO.setLatestVersion(serviceVersionDTOS.get(0).getVersion());
        }
    }

    private void handMarketAppService(Map<Long, MarketServiceVO> finalLongMarketServiceVOMap, AppServiceRepVO appServiceRepVO) {
        MarketServiceVO serviceVO = finalLongMarketServiceVOMap.get(appServiceRepVO.getId());

        if (!Objects.isNull(serviceVO)) {
            appServiceRepVO.setServiceName(serviceVO.getMarketServiceName());
            appServiceRepVO.setServiceCode(serviceVO.getMarketServiceCode());
            //看看是不是预置的
            List<MarketCategoryVO> marketCategoryVOS = serviceVO.getMarketCategoryVOS().stream().filter(MarketCategoryVO::getBuiltIn).collect(toList());
            appServiceRepVO.setBuiltIn(Boolean.FALSE);
            if (!CollectionUtils.isEmpty(marketCategoryVOS)) {
                appServiceRepVO.setBuiltIn(Boolean.TRUE);
            }
            //过滤出已发布的最新的版本
            if (!CollectionUtils.isEmpty(serviceVO.getMarketServiceDeployObjectVOS())) {
                MarketServiceDeployObjectVO marketServiceDeployObjectVO = serviceVO.getMarketServiceDeployObjectVOS().stream().sorted(comparing(MarketServiceDeployObjectVO::getId).reversed()).collect(toList()).get(0);
                appServiceRepVO.setLatestVersion(marketServiceDeployObjectVO.getMarketServiceVersion());
                if (!appServiceRepVO.getBuiltIn()) {
                    //如果是项目发布的  那么显示组织/项目
                    appServiceMapper.selectByPrimaryKey(appServiceRepVO.getId());
                    appServiceRepVO.setSourceView(serviceVO.getSourceName());
                }
                //还需要版本的id,
                appServiceRepVO.setMarketServiceDeployObjectVO(marketServiceDeployObjectVO);
                if (Objects.isNull(appServiceRepVO.getServiceName())) {
                    appServiceRepVO.setServiceName(serviceVO.getMarketServiceName());
                }
            }

        }
    }

    private Map<Long, MarketServiceVO> queryMarketDeployObj(Long projectId, Page<AppServiceRepVO> appServiceRepVOS) {
        List<AppServiceRepVO> serviceRepVOS = appServiceRepVOS.getContent().stream().filter(appServiceRepVO -> isMarketOrHzero(appServiceRepVO)).collect(toList());
        Map<Long, MarketServiceVO> longMarketServiceVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(serviceRepVOS)) {
            Set<Long> marketServiceIds = serviceRepVOS.stream().map(AppServiceRepVO::getId).collect(toSet());
            List<MarketServiceVO> marketServiceVOs = marketServiceClientOperator.queryMarketServiceAndDeployObjAndCategoryByMarketServiceId(projectId, marketServiceIds);
            if (!CollectionUtils.isEmpty(marketServiceVOs)) {
                longMarketServiceVOMap = marketServiceVOs.stream().collect(toMap(MarketServiceVO::getId, Function.identity()));
            }
        }
        return longMarketServiceVOMap;
    }

    @Override
    public List<DevopsEnvironmentRepVO> listEnvByAppServiceId(Long projectId, Long appServiceId) {
        DevopsEnvAppServiceDTO devopsEnvAppServiceDTO = new DevopsEnvAppServiceDTO();
        devopsEnvAppServiceDTO.setAppServiceId(appServiceId);
        List<DevopsEnvAppServiceDTO> devopsEnvAppServiceDTOS = devopsEnvAppServiceMapper.select(devopsEnvAppServiceDTO);
        if (!CollectionUtils.isEmpty(devopsEnvAppServiceDTOS)) {
            Set<Long> envIds = devopsEnvAppServiceDTOS.stream().map(DevopsEnvAppServiceDTO::getEnvId).collect(toSet());
            List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(envIds));
            return ConvertUtils.convertList(devopsEnvironmentDTOS, DevopsEnvironmentRepVO.class);
        }
        return new ArrayList<>();
    }

    @Override
    public Boolean checkDeleteEnvApp(Long appServiceId, Long envId) {
        return devopsEnvApplicationService.checkCanDelete(appServiceId, envId);
    }


    private void downloadSourceCodeAndPush(AppServiceDTO appServiceDTO, UserAttrDTO userAttrDTO, AppServiceImportPayload appServiceImportPayload, String repositoryUrl, String newGroupName) {
        // TODO: 2021/3/3  方法待抽取
        // 获取push代码所需的access token
        String applicationDir = APPLICATION + System.currentTimeMillis();
        String pushToken = getToken(appServiceDTO.getGitlabProjectId(), applicationDir, userAttrDTO);

        //拿到部署对象id 查询文件的地址 project并没有使用
        Long deployObjectId = appServiceImportPayload.getDeployObjectId();
        List<MarketServiceDeployObjectVO> marketServiceDeployObjectVOS = marketServiceClientOperator.listDeployObjectsByIds(0L, Stream.of(deployObjectId).collect(toSet()));
        if (CollectionUtils.isEmpty(marketServiceDeployObjectVOS)) {
            LOGGER.info("deploy object is null ,id is :{}", deployObjectId);
            return;
        }
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceDeployObjectVOS.get(0);
        if (StringUtils.isEmpty(marketServiceDeployObjectVO.getMarketSourceCode())) {
            throw new CommonException(DEVOPS_SOURCE_CODE_VO_IS_NULL, JsonHelper.marshalByJackson(marketServiceDeployObjectVO));
        }
        MarketSourceCodeVO marketSourceCodeVO = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getMarketSourceCode(), MarketSourceCodeVO.class);
        if (StringUtils.isEmpty(marketSourceCodeVO.getMarketSourceCodeUrl())) {
            throw new CommonException(DEVOPS_SOURCE_CODE_URL_IS_NULL);
        }
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(marketSourceCodeVO.getDevopsAppServiceVersionId());

        try {
            InputStream inputStream = fileClient.downloadFile(0L, SOURCE_CODE_BUCKET_NAME, marketSourceCodeVO.getMarketSourceCodeUrl());
            //获取一个临时的工作目录
            FileUtil.createDirectory(applicationDir);
            //解压源码到applicationDir这个目录，源码的文件名字
            FileUtil.unTar(inputStream, applicationDir);
            //处理文件路径 applicationDir=application1615476300950
            //源码目录  application1615476300950\eureka-demo-4221c90325bb438179c43c3886d6cc5a57250e43-4221c90325bb438179c43c3886d6cc5a57250e43  =》application1615476300950\newcode
            //目前这个git目录应该是application1615476300950\new-code
            //todo  处理chart里面的服务名字？
            reFileName(applicationDir, appServiceDTO.getCode());
            Git git = gitUtil.initGit(new File(applicationDir + File.separator + appServiceDTO.getCode()));
            //push 到远程仓库
            GitLabUserDTO gitLabUserDTO = gitlabServiceClientOperator.queryUserById(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            gitUtil.push(git, applicationDir, "The template version:" + appServiceVersionDTO.getVersion(), repositoryUrl, gitLabUserDTO.getUsername(), pushToken);
            LOGGER.info(">>>>>>>>>>>>The address of the remote git is:{}>>>>>>>>>>>>>", repositoryUrl);
        } catch (Exception e) {
            LOGGER.error("push source code git ", e);
        } finally {
            FileUtil.deleteDirectory(new File(applicationDir));
        }
    }

    private void reFileName(String applicationDir, String newCode) {
        //找到applicationDir子文件夹
        File file = new File(applicationDir);
        File[] listFiles = file.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            return;
        }
        //应该就只有一个源码的首目录
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                //修改他的名字为new code
                boolean renameTo = listFile.renameTo(new File(applicationDir + File.separator + newCode));
                if (!renameTo) {
                    LOGGER.error(DEVOPS_RENAME_FAIL);
                }
            }
        }
    }

    @Override
    public void importAppServiceGitlab(AppServiceImportPayload appServiceImportPayload) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceImportPayload.getAppServiceId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(appServiceImportPayload.getIamUserId());

        String newGroupName = appServiceImportPayload.getOrgCode() + "-" + appServiceImportPayload.getProCode();
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectByName(
                newGroupName,
                appServiceDTO.getCode(),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()),
                true);
        //创建gitlab 应用
        if (gitlabProjectDTO.getId() == null) {
            gitlabProjectDTO = gitlabServiceClientOperator.createProject(
                    appServiceImportPayload.getGitlabGroupId(),
                    appServiceDTO.getCode(),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), false);
        } else {
            checkGitlabProjectIdNotUsedBefore(gitlabProjectDTO.getId());
        }

        appServiceDTO.setGitlabProjectId(gitlabProjectDTO.getId());
        String applicationServiceToken = appServiceDTO.getToken();
        getApplicationToken(applicationServiceToken, appServiceDTO.getGitlabProjectId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        appServiceDTO.setSynchro(true);
        appServiceDTO.setFailed(false);
        setProjectHook(appServiceDTO, appServiceDTO.getGitlabProjectId(), applicationServiceToken, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));

        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String repositoryUrl = repoUrl + appServiceImportPayload.getOrgCode() + "-" + appServiceImportPayload.getProCode() + "/" + appServiceDTO.getCode() + GIT;
        cloneAndPushCode(appServiceDTO, userAttrDTO, appServiceImportPayload.getOldAppServiceId(), appServiceImportPayload.getVersionId(), repositoryUrl, newGroupName);

        appServiceMapper.updateByIdSelectiveWithoutAudit(appServiceDTO);
    }

    private void cloneAndPushCode(AppServiceDTO appServiceDTO, UserAttrDTO userAttrDTO, Long oldAppServiceId, Long oldAppServiceVersionId, String repositoryUrl, String newGroupName) {
        AppServiceDTO oldAppServiceDTO = appServiceMapper.selectByPrimaryKey(oldAppServiceId);
        AppServiceVersionDTO oldAppServiceVersionDTO = appServiceVersionService.baseQuery(oldAppServiceVersionId);
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        String oldGroup;
        ProjectDTO oldProjectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(oldAppServiceDTO.getProjectId());
        Tenant oldOrganizationDTO = baseServiceClientOperator.queryOrganizationById(oldProjectDTO.getOrganizationId());
        oldGroup = oldOrganizationDTO.getTenantNum() + "-" + oldProjectDTO.getDevopsComponentCode();
        //拉取代码
        // 获取push代码所需的access token
        String applicationDir = APPLICATION + System.currentTimeMillis();
        String pushToken = getToken(appServiceDTO.getGitlabProjectId(), applicationDir, userAttrDTO);

        //获取admin的token
        String pullToken = gitlabServiceClientOperator.getAdminToken();
        String oldRepository = repoUrl + oldGroup + "/" + oldAppServiceDTO.getCode() + GIT;
        String workingDirectory = gitUtil.cloneAppMarket(applicationDir, oldAppServiceVersionDTO.getCommit(), oldRepository, pullToken);
        replaceParams(appServiceDTO.getCode(), newGroupName, applicationDir, oldAppServiceDTO.getCode(), oldGroup, true);
        Git git = gitUtil.initGit(new File(workingDirectory));
        //push 到远程仓库
        GitLabUserDTO gitLabUserDTO = gitlabServiceClientOperator.queryUserById(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        gitUtil.push(git, applicationDir, "The template version:" + oldAppServiceVersionDTO.getVersion(), repositoryUrl, gitLabUserDTO.getUsername(), pushToken);
    }

    @Override
    public AppServiceDTO baseUpdate(AppServiceDTO applicationDTO) {
        AppServiceDTO oldAppServiceDTO = appServiceMapper.selectByPrimaryKey(applicationDTO.getId());
        applicationDTO.setObjectVersionNumber(oldAppServiceDTO.getObjectVersionNumber());
        if (appServiceMapper.updateByPrimaryKeySelective(applicationDTO) != 1) {
            throw new CommonException(DEVOPS_APP_SERVICE_UPDATE);
        }
        return appServiceMapper.selectByPrimaryKey(applicationDTO.getId());
    }

    @Override
    public AppServiceDTO baseQuery(Long appServiceId) {
        return appServiceMapper.selectByPrimaryKey(appServiceId);
    }

    @Override
    public Page<AppServiceDTO> basePageByOptions(Long projectId,
                                                 Boolean isActive,
                                                 Boolean hasVersion,
                                                 String type,
                                                 Boolean doPage,
                                                 PageRequest pageable,
                                                 String params,
                                                 Boolean checkMember,
                                                 Boolean includeExternal,
                                                 Boolean excludeFailed) {

        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        Long userId = DetailsHelper.getUserDetails().getUserId();

        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        List<AppServiceDTO> list;
        if (projectOwnerOrRoot) {
            //是否需要分页
            if (doPage == null || doPage) {
                return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                        () -> appServiceMapper.list(projectId, isActive, hasVersion, type,
                                TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                                PageRequestUtil.checkSortIsEmpty(pageable),
                                includeExternal,
                                excludeFailed)
                );
            } else {
                list = appServiceMapper.list(projectId, isActive, hasVersion, type,
                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                        PageRequestUtil.checkSortIsEmpty(pageable),
                        includeExternal,
                        excludeFailed);
            }
        } else {
            // 是否需要进行项目成员gitlab角色校验
            Set<Long> appServiceIds;
            if (checkMember) {
                Long tenantId = baseServiceClientOperator.queryImmutableProjectInfo(projectId).getTenantId();
                appServiceIds = getMemberAppServiceIds(tenantId, projectId, userId);
                appServiceIds.addAll(listExternalAppIdByProjectId(projectId));
                if (CollectionUtils.isEmpty(appServiceIds)) {
                    return new Page<>();
                }
            } else {
                appServiceIds = null;
            }
            //是否需要分页
            if (doPage == null || doPage) {
                return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                        () -> appServiceMapper.listProjectMembersAppService(projectId, appServiceIds, isActive, hasVersion, type,
                                TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                                pageable.getSort() == null,
                                userId,
                                includeExternal,
                                excludeFailed));
            } else {
                list = appServiceMapper.listProjectMembersAppService(projectId, appServiceIds, isActive, hasVersion, type,
                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)),
                        pageable.getSort() == null,
                        userId,
                        includeExternal,
                        excludeFailed);
            }
        }

        return PageInfoUtil.listAsPage(list);
    }

    @Override
    public AppServiceDTO baseQueryByCode(String code, Long projectId) {
        AppServiceDTO applicationDTO = new AppServiceDTO();
        applicationDTO.setProjectId(projectId);
        applicationDTO.setCode(code);
        return appServiceMapper.selectOne(applicationDTO);
    }

    @Override
    public List<AppServiceDTO> baseListByEnvId(Long projectId, Long envId, String status) {
        return appServiceMapper.listByEnvId(projectId, envId, null, status);
    }

    @Override
    public Page<AppServiceDTO> basePageByEnvId(Long projectId, Long envId, Long appServiceId, PageRequest pageable) {
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> appServiceMapper.listByEnvId(projectId, envId, appServiceId, NODELETED));

    }

    @Override
    public AppServiceDTO baseQueryByToken(String token) {
        return appServiceMapper.queryByToken(Objects.requireNonNull(token));
    }

    @Override
    public AppServiceDTO queryByTokenOrThrowE(String token) {
        AppServiceDTO appServiceDTO = baseQueryByToken(token);
        if (appServiceDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_TOKEN_INVALID);
        }
        return appServiceDTO;
    }

    @Override
    public void baseDelete(Long appServiceId) {
        appServiceMapper.deleteByPrimaryKey(appServiceId);
    }

    @Override
    public List<AppServiceDTO> baseListByProjectId(Long projectId) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setProjectId(projectId);
        return appServiceMapper.select(appServiceDTO);
    }

    @Override
    public String getGitlabUrl(Long projectId, Long appServiceId) {
        AppServiceDTO appServiceDTO = baseQuery(appServiceId);
        if (appServiceDTO.getGitlabProjectId() != null) {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
            return gitlabUrl + urlSlash
                    + organizationDTO.getTenantNum() + "-" + projectDTO.getDevopsComponentCode() + "/"
                    + appServiceDTO.getCode();
        }
        return "";
    }


    private boolean getHistory(Date startTime, Date endTime, SimpleDateFormat sdf, SonarHistroy sonarHistory) {
        try {
            return sdf.parse(sonarHistory.getDate()).compareTo(startTime) >= 0 && sdf.parse(sonarHistory.getDate()).compareTo(endTime) <= 0;
        } catch (ParseException e) {
            throw new CommonException(e);
        }
    }

    private void getRate(SonarContentVO sonarContentVO, List<Facet> facets) {
        sonarContentVO.setRate("A");
        facets.stream().filter(facet -> facet.getProperty().equals(SEVERITIES)).forEach(facet -> facet.getValues().forEach(value -> {
            if (value.getVal().equals(Rate.MINOR.getRate()) && value.getCount() >= 1
                    && sonarContentVO.getRate().equals("A")) {
                sonarContentVO.setRate("B");
            }
            if (value.getVal().equals(Rate.MAJOR.getRate())
                    && value.getCount() >= 1
                    && !sonarContentVO.getRate().equals("D")
                    && !sonarContentVO.getRate().equals("E")) {
                sonarContentVO.setRate("C");
            }
            if (value.getVal().equals(Rate.CRITICAL.getRate())
                    && value.getCount() >= 1
                    && !sonarContentVO.getRate().equals("E")) {
                sonarContentVO.setRate("D");
            }
            if (value.getVal().equals(Rate.BLOCKER.getRate()) && value.getCount() >= 1) {
                sonarContentVO.setRate("E");
            }
        }));
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
    public AppServiceDTO getApplicationServiceDTO(Long projectId, AppServiceReqVO appServiceReqVO) {
        AppServiceDTO appServiceDTO = ConvertUtils.convertObject(appServiceReqVO, AppServiceDTO.class);
        checkName(projectId, appServiceDTO.getName());
        checkCode(projectId, appServiceDTO.getCode());
        appServiceDTO.setToken(GenerateUUID.generateUUID());
        appServiceDTO.setActive(true);
        appServiceDTO.setSynchro(false);
        appServiceDTO.setProjectId(projectId);
        appServiceDTO.setHarborConfigId(appServiceReqVO.getHarborConfigId());
        appServiceDTO.setChartConfigId(appServiceReqVO.getChartConfigId());
        return appServiceDTO;
    }

    protected AppServiceDTO getExternalApplicationServiceDTO(Long projectId, Integer gitlabProjectId, ExternalAppServiceVO appServiceReqVO) {
        AppServiceDTO appServiceDTO = ConvertUtils.convertObject(appServiceReqVO, AppServiceDTO.class);
        checkName(projectId, appServiceDTO.getName());
        checkCode(projectId, appServiceDTO.getCode());
        appServiceDTO.setToken(GenerateUUID.generateUUID());
        appServiceDTO.setActive(true);
        appServiceDTO.setGitlabProjectId(gitlabProjectId);
        appServiceDTO.setSynchro(false);
        appServiceDTO.setProjectId(projectId);
        return appServiceDTO;
    }

    @Override
    public AppServiceDTO baseCreate(AppServiceDTO appServiceDTO) {
        if (appServiceMapper.insertSelective(appServiceDTO) != 1) {
            throw new CommonException(DEVOPS_APPLICATION_CREATE_INSERT);
        }
        return appServiceDTO;
    }

    @Override
    public Page<AppServiceGroupInfoVO> pageAppServiceByMode(Long projectId, Boolean share, Long searchProjectId, String param, Boolean includeExternal, PageRequest pageable) {

        List<AppServiceGroupInfoVO> appServiceGroupInfoVOS = new ArrayList<>();
        List<AppServiceDTO> appServiceDTOList = new ArrayList<>();
        List<ProjectDTO> projectDTOS = new ArrayList<>();
        if (Boolean.TRUE.equals(share)) {
            Long organizationId = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId).getOrganizationId();
            List<Long> projectIds = new ArrayList<>();
            if (ObjectUtils.isEmpty(searchProjectId)) {
                projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(organizationId);
                projectIds = projectDTOS.stream().filter(ProjectDTO::getEnabled)
                        .filter(v -> !projectId.equals(v.getId()))
                        .map(ProjectDTO::getId).collect(Collectors.toList());
            } else {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(searchProjectId);
                projectIds.add(searchProjectId);
                projectDTOS.add(projectDTO);
            }
            if (ObjectUtils.isEmpty(projectDTOS)) return new Page<>();
            //查询组织共享和共享项目的应用服务
            if (projectIds.isEmpty()) {
                return new Page<>();
            }
            List<AppServiceDTO> organizationAppServices = appServiceMapper.queryOrganizationShareApps(projectIds, param, projectId, includeExternal);
            if (organizationAppServices.isEmpty()) return new Page<>();

            // 去重
            appServiceDTOList = organizationAppServices.stream().collect(collectingAndThen(
                    toCollection(() -> new TreeSet<>(comparing(AppServiceDTO::getId))), ArrayList::new));

        }
        Map<Long, ProjectDTO> projectDTOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(projectDTOS)) {
            projectDTOMap = projectDTOS.stream().collect(Collectors.toMap(ProjectDTO::getId, Function.identity()));
        }
        Map<Long, ProjectDTO> finalProjectDTOMap = projectDTOMap;
        appServiceDTOList.forEach(appServiceDTO -> {
            AppServiceGroupInfoVO appServiceGroupInfoVO = dtoToGroupInfoVO(appServiceDTO);
            if (share) {
                AppServiceVersionDTO appServiceVersionDTO = appServiceVersionMapper.queryByShareVersion(appServiceDTO.getId(), projectId);
                ProjectDTO projectDTO = finalProjectDTOMap.get(appServiceDTO.getProjectId());
                appServiceGroupInfoVO.setProjectName(projectDTO.getName());
                appServiceGroupInfoVO.setShare(true);
                if (ObjectUtils.isEmpty(appServiceVersionDTO)) return;
                appServiceGroupInfoVO.setVersionId(appServiceVersionDTO.getId());
            }
            appServiceGroupInfoVOS.add(appServiceGroupInfoVO);
        });
        return PageInfoUtil.createPageFromList(appServiceGroupInfoVOS, pageable);
    }

    @Override
    public List<AppServiceGroupVO> listAllAppServices(Long projectId, String type, String param, String serviceType, Long appServiceId, Boolean includeExternal) {
        List<String> params = new ArrayList<>();
        if (!ObjectUtils.isEmpty(param)) {
            params.add(param);
        }
        if (appServiceId == null) {
            switch (type) {
                case NORMAL_SERVICE: {
                    List<AppServiceDTO> list = appServiceMapper.list(projectId, Boolean.TRUE, true, serviceType, null, params, "", includeExternal, null);
                    AppServiceGroupVO appServiceGroupVO = new AppServiceGroupVO();
                    appServiceGroupVO.setAppServiceList(ConvertUtils.convertList(list, this::dtoToGroupInfoVO));
                    return ArrayUtil.singleAsList(appServiceGroupVO);
                }
                case SHARE_SERVICE: {
                    return listAllAppServicesHavingVersion(projectId, params, serviceType, includeExternal);
                }
                default: {
                    throw new CommonException(DEVOPS_LIST_DEPLOY_APP_SERVICE_TYPE);
                }
            }
        } else {
            AppServiceDTO appServiceSearchDTO = new AppServiceDTO();
            appServiceSearchDTO.setId(appServiceId);
            AppServiceDTO appServiceDTO = appServiceMapper.selectOne(appServiceSearchDTO);
            if (appServiceDTO == null) {
                // 如果appServiceDTO为null，表示该应用不在本项目下或其它项目共享应用，则最后来源是市场应用
                MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(projectId, appServiceId);
                if (marketServiceVO == null) {
                    // 市场应用也不存在，返回空
                    return new ArrayList<>();
                }
                appServiceDTO = new AppServiceDTO();
                appServiceDTO.setName(marketServiceVO.getMarketServiceName());
            }
            AppServiceGroupVO appServiceGroupVO = new AppServiceGroupVO();
            appServiceGroupVO.setAppServiceList(ConvertUtils.convertList(Collections.singletonList(appServiceDTO), this::dtoToGroupInfoVO));
            return ArrayUtil.singleAsList(appServiceGroupVO);
        }
    }

    private List<AppServiceGroupVO> listAllAppServicesHavingVersion(Long projectId, List<String> params, String serviceType, Boolean includeExternal) {
        List<AppServiceGroupVO> appServiceGroupList = new ArrayList<>();
        Long organizationId = baseServiceClientOperator.queryImmutableProjectInfo(projectId).getTenantId();

        // 查询当前组织下的所有项目id
        Set<Long> ids = baseServiceClientOperator.listProjectIdsInOrg(organizationId);
        // 移除当前项目
        ids.remove(projectId);
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<AppServiceDTO> list = appServiceMapper.listShareAppServiceHavingVersion(ids, projectId, serviceType, params, includeExternal);

        // 将应用服务按照项目分组
        Map<Long, List<AppServiceGroupInfoVO>> map = list.stream()
                .map(this::dtoToGroupInfoVO)
                .collect(Collectors.groupingBy(AppServiceGroupInfoVO::getProjectId));

        // 批量查询项目信息
        Map<Long, ProjectDTO> projectMap = baseServiceClientOperator.queryProjectsByIds(map.keySet()).stream().collect(Collectors.toMap(ProjectDTO::getId, Function.identity()));

        // 填充项目的信息
        for (Map.Entry<Long, List<AppServiceGroupInfoVO>> entry : map.entrySet()) {
            ProjectDTO projectDTO = projectMap.get(entry.getKey());
            AppServiceGroupVO appServiceGroupVO = new AppServiceGroupVO();
            appServiceGroupVO.setName(projectDTO.getName());
            appServiceGroupVO.setCode(projectDTO.getDevopsComponentCode());
            appServiceGroupVO.setId(projectDTO.getId());
            appServiceGroupVO.setAppServiceList(entry.getValue());
            appServiceGroupList.add(appServiceGroupVO);
        }
        return appServiceGroupList;
    }

    private List<AppServiceDTO> baseListAll(Long projectId) {
        return appServiceMapper.listAll(projectId, null, null);
    }

    private List<AppServiceDTO> baseListAll(Long projectId, Long envId, String appServiceName) {
        return appServiceMapper.listAll(projectId, envId, appServiceName);
    }

    private AppServiceDTO fromImportVoToDto(AppServiceImportVO appServiceImportVO) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        BeanUtils.copyProperties(appServiceImportVO, appServiceDTO);
        appServiceDTO.setHarborConfigId(appServiceImportVO.getHarborConfigId());
        appServiceDTO.setChartConfigId(appServiceImportVO.getChartConfigId());
        return appServiceDTO;
    }

    private AppServiceGroupInfoVO dtoToGroupInfoVO(AppServiceDTO appServiceDTO) {
        AppServiceGroupInfoVO appServiceGroupInfoVO = new AppServiceGroupInfoVO();
        BeanUtils.copyProperties(appServiceDTO, appServiceGroupInfoVO);
        return appServiceGroupInfoVO;
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
            throw new CommonException(DEVOPS_REPOSITORY_TOKEN_INVALID);
        } else if (validationResult == null) {
            throw new CommonException(DEVOPS_REPOSITORY_EMPTY);
        }
    }

    private void checkRepositoryUrlAndUsernameAndPassword(String repositoryUrl, String userName, String password) {
        Boolean validationResult = validateRepositoryUrlAndUsernameAndPassword(repositoryUrl, userName, password);
        if (Boolean.FALSE.equals(validationResult)) {
            throw new CommonException(DEVOPS_REPOSITORY_ACCOUNT_INVALID);
        } else if (validationResult == null) {
            throw new CommonException(DEVOPS_REPOSITORY_EMPTY);
        }
    }

    private void initBranch(DevOpsAppServicePayload devOpsAppServicePayload, AppServiceDTO appServiceDTO, String branchName) {
        CommitDTO commitDTO = gitlabServiceClientOperator.queryCommit(devOpsAppServicePayload.getGitlabProjectId(), branchName, devOpsAppServicePayload.getUserId());
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        devopsBranchDTO.setUserId(TypeUtil.objToLong(devOpsAppServicePayload.getUserId()));
        devopsBranchDTO.setAppServiceId(appServiceDTO.getId());
        devopsBranchDTO.setBranchName(branchName);
        devopsBranchDTO.setCheckoutCommit(commitDTO.getId());
        devopsBranchDTO.setCheckoutDate(commitDTO.getCommittedDate());
        devopsBranchDTO.setLastCommitUser(TypeUtil.objToLong(devOpsAppServicePayload.getUserId()));
        devopsBranchDTO.setLastCommitMsg(LogUtil.cutOutString(commitDTO.getMessage(), MiscConstants.DEVOPS_BRANCH_LAST_COMMIT_MESSAGE_MAX_LENGTH));
        devopsBranchDTO.setLastCommitDate(commitDTO.getCommittedDate());
        devopsBranchDTO.setLastCommit(commitDTO.getId());
        devopsBranchService.baseCreate(devopsBranchDTO);
    }

    @Override
    public void replaceParams(String newServiceCode,
                              String newGroupName,
                              String applicationDir,
                              String oldServiceCode,
                              String oldGroupName,
                              Boolean isGetWorkingDirectory) {
        try {
            File file = isGetWorkingDirectory ? new File(gitUtil.getWorkingDirectory(applicationDir)) : new File(applicationDir);
            Map<String, String> params = new HashMap<>();
            params.put("{{group.name}}", newGroupName);
            params.put("{{service.code}}", newServiceCode);
            params.put("the-oldService-name", oldServiceCode);
            if (!ObjectUtils.isEmpty(oldGroupName)) {
                params.put(oldGroupName, newGroupName);
            }
            if (!ObjectUtils.isEmpty(oldServiceCode)) {
                params.put(oldServiceCode, newServiceCode);
            }
            FileUtil.replaceReturnFile(file, params);
        } catch (Exception e) {
            //删除模板
            gitUtil.deleteWorkingDirectory(applicationDir);
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public String checkAppServiceType(Long projectId, @Nullable Long appServiceProjectId, String source) {
        String type = null;
        if (appServiceProjectId == null) {
            if (AppSourceType.MIDDLEWARE.getValue().equals(source)) {
                return AppServiceType.MIDDLEWARE_SERVICE.getType();
            } else {
                return AppServiceType.MARKET_SERVICE.getType();
            }
        } else if (!appServiceProjectId.equals(projectId)) {
            type = AppServiceType.SHARE_SERVICE.getType();
        } else {
            type = AppServiceType.NORMAL_SERVICE.getType();
        }
        return type;
    }


    @Override
    public String getToken(Integer gitlabProjectId, String applicationDir, UserAttrDTO userAttrDTO) {
        String accessToken = userAttrDTO.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitlabServiceClientOperator.createProjectToken(gitlabProjectId,
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()),
                    applicationDir);
            userAttrDTO.setGitlabToken(accessToken);
            userAttrService.baseUpdate(userAttrDTO);
        }
        return accessToken;
    }

    @Override
    public AppServiceDTO queryByGitlabProjectId(Long gitlabProjectId) {
        AppServiceDTO appServiceDTO = new AppServiceDTO();
        appServiceDTO.setGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId));
        return appServiceMapper.selectOne(appServiceDTO);
    }

    @Override
    public Page<AppServiceVO> listAppByProjectId(Long projectId, Boolean doPage, PageRequest pageable, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.pageServiceByProjectId(projectId,
                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS))).stream()
                .filter(appServiceDTO -> (appServiceDTO.getActive() != null && appServiceDTO.getActive()) && (appServiceDTO.getSynchro() != null && appServiceDTO.getSynchro()) && (appServiceDTO.getFailed() == null || !appServiceDTO.getFailed()))
                .collect(toList());
        List<AppServiceVO> list = ConvertUtils.convertList(appServiceDTOList, AppServiceVO.class);
        if (doPage) {
            return PageInfoUtil.createPageFromList(list, pageable);
        } else {
            return PageInfoUtil.listAsPage(list);
        }

    }

    @Override
    public Page<AppServiceVO> listAppServiceByIds(Long projectId, Set<Long> ids, Boolean doPage, boolean withVersions, PageRequest pageable, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.listAppServiceByIds(ids,
                TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)));
        List<AppServiceVersionDTO> appServiceVersionDTOS;

        if (withVersions) {
            // 需要版本信息不查询
            if (!ObjectUtils.isEmpty(projectId)) {
                appServiceVersionDTOS = appServiceVersionService.listAppServiceVersionByIdsAndProjectId(ids, projectId, null);
            } else {
                appServiceVersionDTOS = new ArrayList<>(appServiceVersionService.listServiceVersionByAppServiceIds(ids, null, null, null));
            }
        } else {
            // 不需要版本信息就不查询
            appServiceVersionDTOS = new ArrayList<>();
        }
        Map<Long, List<AppServiceVersionDTO>> appVersionMap = appServiceVersionDTOS.stream().collect(Collectors.groupingBy(AppServiceVersionDTO::getAppServiceId));
        List<AppServiceVO> collect = appServiceDTOList.stream()
                .filter(v -> !CollectionUtils.isEmpty(appVersionMap.get(v.getId())))
                .map(appServiceDTO -> dtoTOVo(appServiceDTO, appVersionMap))
                .collect(Collectors.toList());
        List<AppServiceVO> appServiceVOS = appServiceDTOList.stream()
                .filter(v -> CollectionUtils.isEmpty(appVersionMap.get(v.getId())))
                .map(appServiceDTO -> dtoTOVo(appServiceDTO, appVersionMap))
                .collect(Collectors.toList());
        collect.addAll(appServiceVOS);
        if (doPage == null || doPage) {
            return PageInfoUtil.createPageFromList(collect, pageable);
        } else {
            return PageInfoUtil.listAsPage(collect);
        }
    }

    @Override
    public Page<AppServiceRepVO> listAppServiceByIds(Set<Long> ids, Boolean doPage, PageRequest pageable, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.listAppServiceByIds(ids,
                TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)));

        // 查询相关的组织和项目数据
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.queryProjectsByIds(appServiceDTOList.stream().map(AppServiceDTO::getProjectId).collect(toSet()));
        List<Tenant> organizationDTOS = baseServiceClientOperator.listOrganizationByIds(projectDTOS.stream().map(ProjectDTO::getOrganizationId).collect(toSet()));
        Map<Long, ProjectDTO> projectDTOMap = projectDTOS.stream().collect(Collectors.toMap(ProjectDTO::getId, Functions.identity()));
        Map<Long, Tenant> orgMap = organizationDTOS.stream().collect(Collectors.toMap(Tenant::getTenantId, Functions.identity()));

        // 设置仓库地址和ssh地址
        List<AppServiceRepVO> collect = appServiceDTOList.stream().map(app -> {
            AppServiceRepVO rep = ConvertUtils.convertObject(app, AppServiceRepVO.class);
            rep.setGitlabProjectId(TypeUtil.objToLong(app.getGitlabProjectId()));
            ProjectDTO project = projectDTOMap.get(rep.getProjectId());
            Tenant org = orgMap.get(project.getOrganizationId());
            rep.setRepoUrl(concatRepoUrl(org.getTenantNum(), project.getDevopsComponentCode(), rep.getCode()));
            rep.setSshRepositoryUrl(GitUtil.getAppServiceSshUrl(gitlabSshUrl, org.getTenantNum(), project.getDevopsComponentCode(), rep.getCode()));
            return rep;
        }).collect(toList());


        if (doPage == null || doPage) {
            return PageInfoUtil.createPageFromList(collect, pageable);
        } else {
            return PageInfoUtil.listAsPage(collect);
        }
    }

    @Override
    public Page<AppServiceVO> listByIdsOrPage(Long projectId, @Nullable Set<Long> ids, @Nullable Boolean doPage, PageRequest pageable) {
        // 如果没指定应用服务id，按照普通分页处理
        if (CollectionUtils.isEmpty(ids)) {
            return ConvertUtils.convertPage(basePageByOptions(projectId, null, null, null, doPage, pageable, null, false, true, null), AppServiceVO.class);
        } else {
            // 指定应用服务id，从这些id中根据参数决定是否分页
            // 如果不分页
            if (Boolean.FALSE.equals(doPage)) {
                return PageInfoUtil.listAsPage(ConvertUtils.convertList(appServiceMapper.listAppServiceByIds(ids, null, null), AppServiceVO.class));
            } else {
                // 如果分页
                return ConvertUtils.convertPage(PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> appServiceMapper.listAppServiceByIds(ids, null, null)), AppServiceVO.class);
            }
        }
    }

    @Override
    public List<ProjectVO> listProjectByShare(Long projectId, Boolean share, Boolean includeExternal) {
        Page<AppServiceGroupInfoVO> appServiceGroupInfoVOPageInfo = pageAppServiceByMode(projectId, Boolean.TRUE, null, null, includeExternal, new PageRequest(0, 0));
        List<AppServiceGroupInfoVO> list = appServiceGroupInfoVOPageInfo.getContent();
        List<ProjectVO> projectVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        list.forEach(v -> {
            ProjectVO projectVO = new ProjectVO();
            if (share) {
                projectVO.setId(v.getProjectId());
            } else {
                projectVO.setId(v.getMktAppId());
            }
            projectVO.setName(v.getProjectName());
            projectVOS.add(projectVO);
        });

        // 去重
        return projectVOS.stream().collect(collectingAndThen(
                toCollection(() -> new TreeSet<>(comparing(ProjectVO::getId))), ArrayList::new));
    }

    @Override
    public List<AppServiceVO> listServiceByVersionIds(Set<Long> ids) {
        List<AppServiceDTO> appServiceDTOList = appServiceMapper.listServiceByVersionIds(ids);
        return ConvertUtils.convertList(appServiceDTOList, AppServiceVO.class);
    }

    @Override
    public List<AppServiceTemplateVO> listServiceTemplates() {
        List<AppServiceTemplateVO> serviceTemplateVOS = new ArrayList<>();
        AppServiceTemplate.getTemplatePath().forEach((k, v) -> {
            AppServiceTemplateVO appServiceTemplateVO = new AppServiceTemplateVO(k, v);
            serviceTemplateVOS.add(appServiceTemplateVO);
        });
        return serviceTemplateVOS;
    }

    @Override
    public AppServiceMsgVO checkAppService(Long projectId, Long appServiceId) {
        return checkCanDisable(appServiceId, projectId);
    }

    @Override
    public List<AppServiceSimpleVO> listAppServiceHavingVersions(Long projectId) {
        return ConvertUtils.convertList(appServiceMapper.queryAppServicesHavingVersions(projectId), app -> new AppServiceSimpleVO(app.getId(), app.getName(), app.getCode()));
    }

    private AppServiceVO dtoTOVo(AppServiceDTO appServiceDTO, Map<Long, List<AppServiceVersionDTO>> appVerisonMap) {
        AppServiceVO appServiceVO = new AppServiceVO();
        BeanUtils.copyProperties(appServiceDTO, appServiceVO);
        if (!appVerisonMap.isEmpty()) {
            List<AppServiceVersionVO> appServiceVersionVOS = ConvertUtils.convertList(appVerisonMap.get(appServiceVO.getId()),
                    AppServiceVersionVO.class);
            if (!CollectionUtils.isEmpty(appServiceVersionVOS) && appServiceVersionVOS.size() > 10) {
                appServiceVO.setAllAppServiceVersions(appServiceVersionVOS.subList(0, 10));
            } else {
                appServiceVO.setAllAppServiceVersions(appServiceVersionVOS);
            }
        }
        if (appServiceDTO.getFailed() != null && appServiceDTO.getFailed()) {
            appServiceVO.setStatus(AppServiceStatus.FAILED.getStatus());
        } else if (appServiceDTO.getActive() != null && !appServiceDTO.getActive()) {
            appServiceVO.setStatus(AppServiceStatus.DISABLE.getStatus());
        } else if ((appServiceDTO.getActive() != null && appServiceDTO.getActive()) && (appServiceDTO.getSynchro() != null && appServiceDTO.getSynchro()) && (appServiceDTO.getFailed() == null || !appServiceDTO.getFailed())) {
            appServiceVO.setStatus(AppServiceStatus.ENABLE.getStatus());
        } else if (appServiceDTO.getSynchro() != null && !appServiceDTO.getSynchro() && (!appServiceDTO.getFailed() || appServiceDTO.getFailed() == null)) {
            appServiceVO.setStatus(AppServiceStatus.ESTABLISH.getStatus());
        }
        return appServiceVO;
    }

    @Override
    public Map<Long, Integer> countByProjectId(List<Long> projectIds) {
        Map<Long, Integer> map = new HashMap<>();
        if (CollectionUtils.isEmpty(projectIds)) {
            return map;
        }
        List<ProjectAppSvcCountVO> projectAppSvcCountVOList = appServiceMapper.countByProjectIds(projectIds);
        Map<Long, Integer> appSvcNumMap = projectAppSvcCountVOList.stream().collect(toMap(ProjectAppSvcCountVO::getProjectId, ProjectAppSvcCountVO::getAppSvcNum));
        projectIds.forEach(id -> {
            if (appSvcNumMap.get(id) == null) {
                map.put(id, 0);
            } else {
                map.put(id, appSvcNumMap.get(id));
            }
        });
        return map;
    }


    @Override
    public Boolean checkEnableCreateAppSvc(Long projectId) {
        return appServiceUtils.checkEnableCreateAppSvcWithSize(projectId, 1);
    }

    @Override
    public Page<AppServiceSimpleVO> pageAppServiceToCreateCiPipeline(Long projectId, PageRequest pageRequest, @Nullable String params) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        // 校验用户是否同步
        userAttrService.checkUserSync(userAttrDTO, userId);

        // 查询参数
        Map<String, Object> paramsMap = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamMap = TypeUtil.cast(paramsMap.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(paramsMap.get(TypeUtil.PARAMS));

        // 判断权限
        if (permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId)) {
            return ConvertUtils.convertPage(PageHelper.doPageAndSort(pageRequest, () -> appServiceMapper.listAppServiceToCreatePipelineForOwner(projectId, searchParamMap, paramList)), AppServiceServiceImpl::dto2SimpleVo);
        } else {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
            Set<Long> appServiceIds = getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
            appServiceIds.addAll(listExternalAppIdByProjectId(projectId));
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new Page<>();
            }
            return ConvertUtils.convertPage(PageHelper.doPageAndSort(pageRequest, () -> appServiceMapper.listAppServiceToCreatePipelineForMember(projectId, userId, appServiceIds, searchParamMap, paramList)), AppServiceServiceImpl::dto2SimpleVo);
        }
    }

    private static AppServiceSimpleVO dto2SimpleVo(AppServiceDTO app) {
        return new AppServiceSimpleVO(app.getId(), app.getName(), app.getCode(), app.getType());
    }

    @Override
    public String calculateGitlabProjectUrlWithSuffix(Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(Objects.requireNonNull(appServiceId));
        if (appServiceDTO == null) {
            return null;
        }
        if (appServiceDTO.getExternalConfigId() != null) {
            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
            return appExternalConfigDTO.getRepositoryUrl();
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
        Tenant tenant = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        return String.format(GitOpsConstants.REPO_URL_TEMPLATE_WITHOUT_SUFFIX, repoUrl, tenant.getTenantNum(), projectDTO.getDevopsComponentCode(), appServiceDTO.getCode());
    }

    /**
     * 释放资源
     */
    private void releaseResources(File applicationWorkDir, Git repositoryGit) {
        if (repositoryGit != null) {
            repositoryGit.close();
        }
        FileUtil.deleteDirectory(applicationWorkDir);
    }


    /**
     * get application token (set a token if there is not one in gitlab)
     *
     * @param projectId gitlab project id
     * @param userId    gitlab user id
     * @return the application token that is stored in gitlab variables
     */
    private void getApplicationToken(String token, Integer projectId, Integer userId) {
        List<CiVariableVO> variables = gitlabServiceClientOperator.listAppServiceVariable(projectId, userId);
        if (CollectionUtils.isEmpty(variables)) {
            gitlabServiceClientOperator.createProjectVariable(projectId, GITLAB_VARIABLE_TOKEN, token, false, userId);
            //添加跳过证书扫描的变量
            gitlabServiceClientOperator.createProjectVariable(projectId, GITLAB_VARIABLE_TRIVY_INSECURE, "true", false, userId);
        } else {
            List<CiVariableVO> variableList = new ArrayList<>();
            CiVariableVO ciVariableVO = new CiVariableVO();
            ciVariableVO.setKey(GITLAB_VARIABLE_TOKEN);
            ciVariableVO.setValue(token);

            CiVariableVO ciVariableVO2 = new CiVariableVO();
            ciVariableVO2.setKey(GITLAB_VARIABLE_TRIVY_INSECURE);
            ciVariableVO2.setValue("true");

            variableList.add(ciVariableVO);
            variableList.add(ciVariableVO2);

            gitlabServiceClientOperator.batchSaveProjectVariable(projectId, userId, variableList);
        }
    }

    /**
     * set project hook id for application
     *
     * @param appServiceDTO the application entity
     * @param projectId     the gitlab project id
     * @param token         the token for project hook
     * @param userId        the gitlab user id
     */
    @Override
    public void setProjectHook(AppServiceDTO appServiceDTO, Integer projectId, String token, Integer userId) {
        ProjectHookDTO projectHookDTO = ProjectHookDTO.allHook();
        projectHookDTO.setEnableSslVerification(true);
        projectHookDTO.setProjectId(projectId);
        projectHookDTO.setToken(token);
        String uri = getWebhookUrl();
        projectHookDTO.setUrl(uri);
        List<ProjectHookDTO> projectHookDTOS = gitlabServiceClientOperator.listProjectHook(projectId, userId);
        String finalUri = uri;
        Optional<ProjectHookDTO> first = projectHookDTOS.stream().filter(v -> v.getUrl().equals(finalUri)).findFirst();
        if (first.isPresent()) {
            // 存在则更新
            ProjectHookDTO projectHookDTO1 = first.get();
            Integer hookId = projectHookDTO1.getId();
            projectHookDTO1.setEnableSslVerification(true);
            projectHookDTO1.setProjectId(projectId);
            projectHookDTO1.setToken(token);
            gitlabServiceClientOperator.updateWebHook(projectId, userId, hookId, projectHookDTO1);
            appServiceDTO.setHookId(TypeUtil.objToLong(hookId));
        } else {
            // 不存在则新建
            appServiceDTO.setHookId(TypeUtil.objToLong(gitlabServiceClientOperator.createWebHook(projectId, userId, projectHookDTO).getId()));
        }
    }

    @Override
    public Set<Long> getMemberAppServiceIds(Long organizationId, Long projectId, Long userId) {
        List<RepositoryPrivilegeViewDTO> viewDTOList = hrdsCodeRepoClient.listRepositoriesByPrivilege(organizationId, projectId, Collections.singleton(userId)).getBody();
        if (CollectionUtils.isEmpty(viewDTOList)) {
            return new HashSet<>();
        }
        return viewDTOList.get(0).getAppServiceIds() == null ? new HashSet<>() : viewDTOList.get(0).getAppServiceIds();
    }

    @Override
    public Set<Long> getMemberAppServiceIdsByAccessLevel(Long organizationId, Long projectId, Long userId, Integer accessLevel, Long appId) {
        List<RepositoryPrivilegeViewDTO> viewDTOList = hrdsCodeRepoClient.listRepositoriesByAccessLevel(organizationId, projectId, accessLevel, appId, Collections.singleton(userId)).getBody();
        if (CollectionUtils.isEmpty(viewDTOList)) {
            return new HashSet<>();
        }
        return viewDTOList.get(0).getAppServiceIds();
    }

    @Override
    @Transactional
    public void batchTransfer(Long projectId, List<AppServiceTransferVO> appServiceTransferVOList) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        appServiceUtils.checkEnableCreateAppSvcOrThrowE(projectId, appServiceTransferVOList.size());

        // 校验是否能迁移应用服务
        appServiceTransferVOList.forEach(v -> {
            AppServiceDTO appServiceDTO = new AppServiceDTO();
            appServiceDTO.setGitlabProjectId(v.getGitlabProjectId());
            List<AppServiceDTO> appServiceDTOList = appServiceMapper.select(appServiceDTO);
            if (!CollectionUtils.isEmpty(appServiceDTOList)
                    && appServiceDTOList.stream().anyMatch(app -> "none".equals(app.getExternalGitlabUrl()))) {
                throw new CommonException(DEVOPS_APP_IS_ALREADY_BIND);
            }
        });

        // 权限校验
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        boolean isGitlabRoot = false;
        if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
            // 如果这边表存了gitlabAdmin这个字段,那么gitlabUserId就不会为空,所以不判断此字段为空
            isGitlabRoot = gitlabServiceClientOperator.isGitlabAdmin(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
        if (!isGitlabRoot) {
            // 查询创建应用所在的gitlab应用组 用户权限
            MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));

            if (memberDTO == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.value)) {
                throw new CommonException(DEVOPS_USER_NOT_GITLAB_OWNER);
            }
        }

        appServiceTransferVOList.forEach(appServiceTransferVO -> {
            ApplicationValidator.checkApplicationService(appServiceTransferVO.getCode());
            // 校验名称唯一性
            checkName(projectId, appServiceTransferVO.getName());
            // 校验code唯一性
            checkCode(projectId, appServiceTransferVO.getCode());
            AppServiceDTO appServiceDTO = new AppServiceDTO();
            appServiceDTO.setProjectId(projectId);
            appServiceDTO.setCode(appServiceTransferVO.getCode());
            appServiceDTO.setName(appServiceTransferVO.getName());
            appServiceDTO.setToken(GenerateUUID.generateUUID());
            appServiceDTO.setProjectId(projectId);
            appServiceDTO.setActive(true);
            appServiceDTO.setSynchro(false);
            appServiceDTO.setType(appServiceTransferVO.getType());
            appServiceTransferVO.setAppServiceId(baseCreate(appServiceDTO).getId());
        });

        appServiceTransferVOList.forEach(appServiceTransferVO -> transferAppService(projectId,
                devopsProjectDTO.getDevopsAppGroupId(),
                appServiceTransferVO));
    }


    @Override
    public void createAppServiceForTransfer(AppServiceTransferVO appServiceTransferVO) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());
        Integer userId = TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());


        AppServiceDTO appServiceDTO = baseQuery(appServiceTransferVO.getAppServiceId());
        String token = appServiceDTO.getToken();
        // 0. 如果修改了服务编码，则先修改原仓库编码
        GitlabProjectDTO oldProjectDTO = gitlabServiceClientOperator.queryProjectById(appServiceTransferVO.getGitlabProjectId());
        if (!Objects.equals(oldProjectDTO.getName(), appServiceTransferVO.getCode())) {
            gitlabServiceClientOperator.updateNameAndPath(userId, appServiceTransferVO.getGitlabProjectId(), appServiceTransferVO.getCode());
        }

        // 1. 迁移gitlab代码库
        if (!oldProjectDTO.getNamespace().getId().equals(appServiceTransferVO.getGitlabGroupId())) {
            gitlabServiceClientOperator.transferProject(appServiceTransferVO.getGitlabProjectId(),
                    appServiceTransferVO.getGitlabGroupId(),
                    userId);
        }

        // 2. 设置token等变量（创建或更新）
        List<CiVariableVO> variables = new ArrayList<>();
        variables.add(new CiVariableVO(GITLAB_VARIABLE_TOKEN, token));
        variables.add(new CiVariableVO(GITLAB_VARIABLE_TRIVY_INSECURE, "true"));
        gitlabServiceClientOperator.batchSaveProjectVariable(appServiceTransferVO.getGitlabProjectId(), userId, variables);

        // 3. 添加webhook
        setProjectHook(appServiceDTO, appServiceTransferVO.getGitlabProjectId(), token, userId);
        // 4. 创建应用服务
        appServiceDTO.setGitlabProjectId(appServiceTransferVO.getGitlabProjectId());
        appServiceDTO.setSynchro(true);
        appServiceDTO.setFailed(false);
        appServiceDTO.setActive(true);
        baseUpdate(appServiceDTO);
    }

    @Override
    public List<CheckAppServiceCodeAndNameVO> checkNameAndCode(Long projectId, List<CheckAppServiceCodeAndNameVO> codeAndNameVOList) {
        codeAndNameVOList.forEach(t -> {
            if (!StringUtils.isEmpty(t.getServiceCode())) {
                t.setCodeEnabledFlag(isCodeUnique(projectId, t.getServiceCode()));
            }
            if (!StringUtils.isEmpty(t.getServiceName())) {
                t.setNameEnabledFlag(isNameUnique(projectId, t.getServiceName()));
            }
        });
        return codeAndNameVOList;
    }

    @Override
    @Transactional
    public OpenAppServiceReqVO openCreateAppService(Long projectId, OpenAppServiceReqVO openAppServiceReqVO) {
        openAppServiceReqVO.setProjectId(projectId);
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(openAppServiceReqVO.getEmail());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserDTO.getId());
        userAttrService.checkUserSync(userAttrDTO, iamUserDTO.getId());

        ApplicationValidator.checkApplicationService(openAppServiceReqVO.getCode());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        // 判断项目下是否还能创建应用服务
        appServiceUtils.checkEnableCreateAppSvcOrThrowE(projectId, 1);

        // 查询创建应用服务所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);

        boolean isGitlabRoot = false;
        if (Boolean.TRUE.equals(userAttrDTO.getGitlabAdmin())) {
            // 如果这边表存了gitlabAdmin这个字段,那么gitlabUserId就不会为空,所以不判断此字段为空
            isGitlabRoot = gitlabServiceClientOperator.isGitlabAdmin(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

        if (!isGitlabRoot) {
            MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            if (memberDTO == null || !memberDTO.getAccessLevel().equals(AccessLevel.OWNER.value)) {
                throw new CommonException(DEVOPS_USER_NOT_GITLAB_OWNER);
            }
        }
        AppServiceReqVO appServiceReqVO = new AppServiceReqVO();
        BeanUtils.copyProperties(openAppServiceReqVO, appServiceReqVO);
        AppServiceDTO appServiceDTO = getApplicationServiceDTO(projectId, appServiceReqVO);
        appServiceDTO = baseCreate(appServiceDTO);

        //创建saga payload
        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
        devOpsAppServicePayload.setPath(appServiceDTO.getCode());
        devOpsAppServicePayload.setOrganizationId(projectDTO.getOrganizationId());
        devOpsAppServicePayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        devOpsAppServicePayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
        devOpsAppServicePayload.setAppServiceId(appServiceDTO.getId());
        devOpsAppServicePayload.setIamProjectId(projectId);
        devOpsAppServicePayload.setAppServiceDTO(appServiceDTO);
        devOpsAppServicePayload.setOpenAppService(true);
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType(APPSERVICE)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_APPLICATION_SERVICE)
                        .withPayloadAndSerialize(devOpsAppServicePayload)
                        .withRefId(String.valueOf(appServiceDTO.getId()))
                        .withSourceId(projectId),
                builder -> {
                });
        return ConvertUtils.convertObject(baseQueryByCode(appServiceDTO.getCode(), appServiceDTO.getProjectId()), OpenAppServiceReqVO.class);
    }

    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_TRANSFER_APP_SERVICE,
            description = "迁移应用服务",
            inputSchemaClass = AppServiceTransferVO.class)
    public void transferAppService(Long projectId, Long gitlabGroupId, AppServiceTransferVO appServiceTransferVO) {
        appServiceTransferVO.setProjectId(projectId);
        appServiceTransferVO.setGitlabGroupId(TypeUtil.objToInteger(gitlabGroupId));

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withRefId(TypeUtil.objToString(appServiceTransferVO.getGitlabProjectId()))
                        .withSourceId(projectId)
                        .withPayloadAndSerialize(appServiceTransferVO)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_TRANSFER_APP_SERVICE),
                builder -> {
                });
    }

    @Override
    public void baseCheckApp(Long projectId, Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (appServiceDTO == null || !projectId.equals(appServiceDTO.getProjectId())) {
            throw new CommonException(DEVOPS_APP_PROJECT_NOT_MATCH);
        }
    }

    private void initApplicationParams(Long projectId, List<AppServiceDTO> applicationDTOS, String urlSlash) {
        ImmutableProjectInfoVO info = baseServiceClientOperator.queryImmutableProjectInfo(projectId);
        for (AppServiceDTO t : applicationDTOS) {
            initApplicationParams(info, t, urlSlash);
        }
    }

    private void initApplicationParams(ImmutableProjectInfoVO info, AppServiceDTO appService, String urlSlash) {
        if (appService.getExternalConfigId() == null) {
            if (appService.getGitlabProjectId() != null) {
                String devopsComponentCode = info.getDevopsComponentCode();
                String tenantCode = info.getTenantNum();
                appService.setSshRepositoryUrl(GitUtil.getAppServiceSshUrl(gitlabSshUrl, tenantCode, devopsComponentCode, appService.getCode()));
                appService.setRepoUrl(
                        gitlabUrl + urlSlash + tenantCode + "-" + devopsComponentCode + "/"
                                + appService.getCode() + ".git");
            }
        } else {
            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithoutPasswordAndToken(appService.getExternalConfigId());
            appService.setRepoUrl(appExternalConfigDTO.getRepositoryUrl());
            appService.setAppExternalConfigDTO(appExternalConfigDTO);
        }

    }

    private void initApplicationParamsWithProxyUrl(ImmutableProjectInfoVO info, AppServiceDTO appService, String urlSlash) {
        if (appService.getGitlabProjectId() != null) {
            String devopsComponentCode = info.getDevopsComponentCode();
            String tenantCode = info.getTenantNum();
            if (appService.getExternalConfigId() == null) {
                appService.setSshRepositoryUrl(GitUtil.getAppServiceSshUrl(gitlabSshUrl, tenantCode, devopsComponentCode, appService.getCode()));
                if (org.apache.commons.lang3.StringUtils.isNoneBlank(gitlabProxyUrl)) {
                    appService.setRepoUrl(
                            gitlabProxyUrl + urlSlash + tenantCode + "-" + devopsComponentCode + "/"
                                    + appService.getCode() + ".git");
                } else {
                    appService.setRepoUrl(
                            gitlabUrl + urlSlash + tenantCode + "-" + devopsComponentCode + "/"
                                    + appService.getCode() + ".git");
                }

            } else {
                AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appService.getExternalConfigId());
                appService.setRepoUrl(appExternalConfigDTO.getRepositoryUrl());
                appService.setAppExternalConfigDTO(appExternalConfigDTO);
            }

        }
    }

    private AppServiceRepVO dtoToRepVoWithoutIamUserFill(AppServiceDTO appServiceDTO) {
        AppServiceRepVO appServiceRepVO = new AppServiceRepVO();
        BeanUtils.copyProperties(appServiceDTO, appServiceRepVO);
        appServiceRepVO.setFail(appServiceDTO.getFailed());
        appServiceRepVO.setGitlabProjectId(TypeUtil.objToLong(appServiceDTO.getGitlabProjectId()));
        return appServiceRepVO;
    }

    private AppServiceRepVO dtoToRepVo(AppServiceDTO appServiceDTO) {
        AppServiceRepVO appServiceRepVO = new AppServiceRepVO();
        BeanUtils.copyProperties(appServiceDTO, appServiceRepVO);
        appServiceRepVO.setFail(appServiceDTO.getFailed());
        IamUserDTO createUser = baseServiceClientOperator.queryUserByUserId(appServiceDTO.getCreatedBy());
        IamUserDTO updateUser = baseServiceClientOperator.queryUserByUserId(appServiceDTO.getLastUpdatedBy());
        if (createUser != null) {
            appServiceRepVO.setCreateUserName(createUser.getRealName());
            appServiceRepVO.setCreateLoginName(createUser.getLoginName());
        }
        if (updateUser != null) {
            appServiceRepVO.setUpdateUserName(updateUser.getRealName());
            appServiceRepVO.setUpdateLoginName(updateUser.getLoginName());
        }
        appServiceRepVO.setGitlabProjectId(TypeUtil.objToLong(appServiceDTO.getGitlabProjectId()));
        return appServiceRepVO;
    }

    public AppServiceRepVO dtoToRepVo(AppServiceDTO appServiceDTO, Map<Long, IamUserDTO> users) {
        AppServiceRepVO appServiceRepVO = new AppServiceRepVO();
        BeanUtils.copyProperties(appServiceDTO, appServiceRepVO);
        appServiceRepVO.setFail(appServiceDTO.getFailed());
        IamUserDTO createUser = users.get(appServiceDTO.getCreatedBy());
        IamUserDTO updateUser = users.get(appServiceDTO.getLastUpdatedBy());
        if (createUser != null) {
            appServiceRepVO.setCreateUserName(createUser.getRealName());
            appServiceRepVO.setCreateLoginName(createUser.getLoginName());
            appServiceRepVO.setCreateUserImage(createUser.getImageUrl());
        }
        if (updateUser != null) {
            appServiceRepVO.setUpdateUserName(updateUser.getRealName());
            appServiceRepVO.setUpdateLoginName(updateUser.getLoginName());
            appServiceRepVO.setUpdateUserImage(updateUser.getImageUrl());
        }
        appServiceRepVO.setGitlabProjectId(TypeUtil.objToLong(appServiceDTO.getGitlabProjectId()));
        return appServiceRepVO;
    }

    private DevopsUserPermissionVO remDTOToPermissionVO(RdmMemberViewDTO rdmMemberViewDTO) {
        DevopsUserPermissionVO devopsUserPermissionVO = new DevopsUserPermissionVO();
        BeanUtils.copyProperties(rdmMemberViewDTO.getUser(), devopsUserPermissionVO);
        devopsUserPermissionVO.setIamUserId(rdmMemberViewDTO.getUser().getUserId());
        return devopsUserPermissionVO;
    }

    @Override
    public AppServiceRepVO queryOtherProjectAppServiceWithRepositoryInfo(Long projectId, Long appServiceId) {

        AppServiceDTO appServiceDTO = appServiceMapper.selectWithEmptyRepositoryByPrimaryKey(appServiceId);

        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        initApplicationParams(baseServiceClientOperator.queryImmutableProjectInfo(projectId), appServiceDTO, urlSlash);

        AppServiceRepVO appServiceRepVO = new AppServiceRepVO();
        BeanUtils.copyProperties(appServiceDTO, appServiceRepVO);
        appServiceRepVO.setFail(appServiceDTO.getFailed());

        return appServiceRepVO;
    }

    @Override
    public Page<AppServiceUnderOrgVO> listAppServiceUnderOrg(Long projectId, Long appServiceId, String searchParam, PageRequest pageRequest) {
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        UserAppServiceIdsVO userAppServiceIdsVO = rducmClientOperator.getAppServiceIds(projectDTO.getOrganizationId(), userDetails.getUserId());
        // 待查询的appService列表
        List<Long> appServiceIds = userAppServiceIdsVO.getAppServiceIds();
        // 列举出当前项目下的应用服务id
        List<Long> appServiceIdsBelongToCurrentProject = appServiceMapper.listAllAppServiceIds(projectId);
        // 移除当前项目下的所有应用服务
        appServiceIds.removeAll(appServiceIdsBelongToCurrentProject);
        // 如果在移除当前项目下的所有应用服务后，应用服务列表为空表示其他项目下没有应用服务权限，返回空列表
        if (CollectionUtils.isEmpty(appServiceIds)) {
            return new Page<>();
        }
        // 如果appServiceId存在，添加到查询列表中
        if (appServiceId != null && !appServiceIds.contains(appServiceId)) {
            appServiceIds.add(appServiceId);
        }

        List<ProjectDTO> projectDTOS = baseServiceClientOperator.listOwnedProjects(projectDTO.getOrganizationId(), userDetails.getUserId());

        List<AppServiceDTO> appServiceDTOS = appServiceMapper.listAppServiceByIdsWithParam(userAppServiceIdsVO.getAppServiceIds(), searchParam);

        List<AppServiceUnderOrgVO> appServiceUnderOrgVOS = new ArrayList<>();

        Map<Long, List<AppServiceDTO>> appServiceGroupProjectId = appServiceDTOS.stream().collect(groupingBy(AppServiceDTO::getProjectId));
        Map<Long, String> projectIdAndNameMap = projectDTOS.stream().collect(Collectors.toMap(ProjectDTO::getId, ProjectDTO::getName));

        // 如果appServiceId存在，始终添加到第一页
        if (appServiceId != null) {
            AppServiceDTO appServiceDTO = appServiceDTOS.stream().filter(a -> a.getId().equals(appServiceId)).collect(toList()).get(0);
            List<AppServiceDTO> appServiceDTOSUnderSameProject = appServiceGroupProjectId.get(appServiceDTO.getProjectId());
            List<AppServiceDTO> appServiceDTOToReturn = appServiceDTOSUnderSameProject.stream().filter(a -> !a.getId().equals(appServiceId)).collect(toList());
            appServiceDTOToReturn.add(0, appServiceDTO);
            String projectName = projectIdAndNameMap.get(appServiceDTO.getProjectId());
            addAppServiceUnderOrgVO(appServiceDTO.getProjectId(), projectName, appServiceDTOToReturn, appServiceUnderOrgVOS);
            appServiceGroupProjectId.remove(appServiceDTO.getProjectId());
        }

        appServiceGroupProjectId.forEach((k, v) -> addAppServiceUnderOrgVO(k, projectIdAndNameMap.get(k), v, appServiceUnderOrgVOS));

        return PageInfoUtil.createPageFromList(appServiceUnderOrgVOS, pageRequest);
    }

    private void addAppServiceUnderOrgVO(Long projectId, String projectName, List<AppServiceDTO> appServiceDTOS, List<AppServiceUnderOrgVO> appServiceUnderOrgVOS) {
        if (StringUtils.isEmpty(projectName)) {
            return;
        }
        int size = appServiceDTOS.size();
        List<AppServiceVO> appServiceVOSTOReturn;
        if (size > 5) {
            appServiceVOSTOReturn = ConvertUtils.convertList(appServiceDTOS.subList(0, 5), AppServiceVO.class);
        } else {
            appServiceVOSTOReturn = ConvertUtils.convertList(appServiceDTOS, AppServiceVO.class);
        }
        AppServiceUnderOrgVO appServiceUnderOrgVO = new AppServiceUnderOrgVO();
        appServiceUnderOrgVO.setProjectId(projectId);
        appServiceUnderOrgVO.setProjectName(projectName);
        appServiceUnderOrgVO.setAppServices(appServiceVOSTOReturn);
        appServiceUnderOrgVOS.add(appServiceUnderOrgVO);
    }

    @Override
    public SonarContentsVO getSonarContentFromCache(Long projectId, Long appServiceId) {
        String jsonBody = redisTemplate.opsForValue().get(SONAR + ":" + projectId + ":" + appServiceId);
        if (StringUtils.isEmpty(jsonBody)) {
            SonarContentsVO sonarContent = getSonarContent(projectId, appServiceId);
            if (!StringUtils.isEmpty(sonarContent)) {
                redisTemplate.opsForValue().set(SONAR + ":" + projectId + ":" + appServiceId, JsonHelper.marshalByJackson(sonarContent), 1, TimeUnit.HOURS);
            }
            return sonarContent;
        } else {
            return JsonHelper.unmarshalByJackson(jsonBody, SonarContentsVO.class);
        }
    }

    @Override
    public List<AppServiceDTO> baseListByIds(Set<Long> appServiceIds) {
        if (CollectionUtils.isEmpty(appServiceIds)) {
            return Collections.emptyList();
        }
        return appServiceMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(appServiceIds));
    }

    @Override
    public String getPrivateToken(Long projectId, String serviceCode, String email) {
        AppServiceDTO appServiceDTO = baseQueryByCode(serviceCode, projectId);
        if (appServiceDTO.getGitlabProjectId() == null) {
            throw new CommonException(DEVOPS_APP_SERVICE_SYNC);
        }
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(email);
        if (!permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, iamUserDTO.getId())) {
            throw new CommonException(DEVOPS_USER_NOT_GITLAB_PROJECT_OWNER);
        }
        String key = String.format(PRIVATE_TOKEN_FORMAT, appServiceDTO.getGitlabProjectId());
        String privateToken = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(privateToken)) {
            String tokenIdKey = String.format(PRIVATE_TOKEN_ID_FORMAT, appServiceDTO.getGitlabProjectId());
            String tokenIdStr = stringRedisTemplate.opsForValue().get(tokenIdKey);
            UserAttrDTO userAttrDTO = userAttrService.baseQueryById(iamUserDTO.getId());
            if (!StringUtils.isEmpty(tokenIdStr)) {
                gitlabServiceClientOperator.revokeImpersonationToken(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), TypeUtil.objToInteger(tokenIdStr));
            }
            ImpersonationTokenDTO tokenDTO = gitlabServiceClientOperator.createPrivateToken(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "c7nToken", DateUtil.subOrAddDay(new Date(), 7));
            if (tokenDTO == null) {
                throw new CommonException(DEVOPS_CREATE_PRIVATE_TOKEN);
            }
            privateToken = tokenDTO.getToken();
            stringRedisTemplate.opsForValue().set(key, privateToken, 7, TimeUnit.DAYS);
            stringRedisTemplate.opsForValue().set(tokenIdKey, tokenDTO.getId().toString());
        }
        return privateToken;
    }

    @Override
    public String getSshUrl(Long projectId, String orgCode, String projectCode, String serviceCode) {
        return String.format("ssh://git@%s/%s-%s/%s.git",
                gitlabSshUrl, orgCode, projectCode, serviceCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppServiceDTO createExternalApp(Long projectId, ExternalAppServiceVO externalAppServiceVO) {
        externalAppServiceVO.setProjectId(projectId);
        externalAppServiceVO.setType(ApplicationType.NORMAL.getType());

        AppExternalConfigDTO externalConfigDTO = externalAppServiceVO.getAppExternalConfigDTO();
        externalConfigDTO.setRepositoryUrl(externalConfigDTO.getRepositoryUrl().replace(".git", ""));

        ApplicationValidator.checkApplicationService(externalAppServiceVO.getCode());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        // 判断项目下是否还能创建应用服务
        appServiceUtils.checkEnableCreateAppSvcOrThrowE(projectDTO.getOrganizationId(), projectId, 1);

        // 校验账户权限
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryExternalProjectByCode(externalConfigDTO);

        // 保存外部仓库配置
        AppExternalConfigDTO appExternalConfigDTO = ConvertUtils.convertObject(externalConfigDTO, AppExternalConfigDTO.class);
        appExternalConfigDTO.setProjectId(projectId);
        appExternalConfigService.baseSave(appExternalConfigDTO);

        AppServiceDTO appServiceDTO = getExternalApplicationServiceDTO(projectId,
                gitlabProjectDTO.getId(),
                externalAppServiceVO);
        GitlabRepositoryInfo gitlabRepositoryInfo = GitUtil.calaulateRepositoryInfo(externalConfigDTO.getRepositoryUrl());
        appServiceDTO.setExternalGitlabUrl(gitlabRepositoryInfo.getGitlabUrl());
        appServiceDTO.setExternalConfigId(appExternalConfigDTO.getId());
        appServiceDTO = baseCreate(appServiceDTO);

        //创建saga payload
        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
        devOpsAppServicePayload.setPath(appServiceDTO.getCode());
        devOpsAppServicePayload.setOrganizationId(projectDTO.getOrganizationId());
        devOpsAppServicePayload.setAppServiceId(appServiceDTO.getId());
        devOpsAppServicePayload.setIamProjectId(projectId);
        devOpsAppServicePayload.setAppServiceDTO(appServiceDTO);
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType(APPSERVICE)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_EXTERNAL_APPLICATION_SERVICE)
                        .withPayloadAndSerialize(devOpsAppServicePayload)
                        .withRefId(String.valueOf(appServiceDTO.getId()))
                        .withSourceId(projectId),
                builder -> {
                });
        return appServiceDTO;
    }

    @Override
    public Boolean isExternalGitlabUrlUnique(String externalGitlabUrl) {
        if (externalGitlabUrl.endsWith(".git")) {
            externalGitlabUrl = externalGitlabUrl.substring(0, externalGitlabUrl.indexOf(".git"));
        }

        return appExternalConfigService.checkRepositoryUrlUnique(externalGitlabUrl);
    }

    @Override
    public Boolean testConnection(AppExternalConfigDTO appExternalConfigDTO) {
        boolean flag = true;
        try {
            // 校验账户权限
            appExternalConfigDTO.setRepositoryUrl(appExternalConfigDTO.getRepositoryUrl().replace(".git", ""));
            GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryExternalProjectByCode(appExternalConfigDTO);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>gitlabProjectDTO is {}<<<<<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(gitlabProjectDTO));
            }
            if (gitlabProjectDTO == null || gitlabProjectDTO.getId() == null) {
                flag = false;
            }
        } catch (Exception e) {
            LOGGER.error("devops.query.gitlab.group", e);
            flag = false;
        }
        return flag;
    }

    @Override
    public Set<Long> listExternalAppIdByProjectId(Long projectId) {
        return appServiceMapper.listAllExternalAppServiceIds(projectId);
    }

    @Override
    public List<AppServiceDTO> queryAppByProjectIds(List<Long> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        return appServiceMapper.listByActiveAndProjects(projectIds);
    }

    @Override
    public Page<AppServiceVO> pageByActive(Long projectId, Long targetProjectId, Long targetAppServiceId, PageRequest pageRequest, String param) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        ImmutableProjectInfoVO immutableProjectInfoVO = baseServiceClientOperator.queryImmutableProjectInfo(targetProjectId);
        boolean projectOwner = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(targetProjectId, userId);
        Page<AppServiceDTO> appServiceDTOPage;
        if (projectOwner) {
            appServiceDTOPage = PageHelper.doPage(pageRequest, () -> appServiceMapper.listByActiveOrderByTargetAppServiceId(targetProjectId, targetAppServiceId, param));
        } else {
            Set<Long> appServiceIds = getMemberAppServiceIds(immutableProjectInfoVO.getTenantId(), targetProjectId, userId);
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new Page<>();
            }
            appServiceDTOPage = PageHelper.doPage(pageRequest, () -> appServiceMapper.listProjectMembersAppServiceByActiveOrderByTargetAppServiceId(targetProjectId, targetAppServiceId, appServiceIds, userId, param));
        }
        return ConvertUtils.convertPage(appServiceDTOPage, AppServiceVO.class);
    }

    @Override
    public Set<Long> listAllIdsByProjectId(Long projectId) {
        return appServiceMapper.listAllIdsByProjectId(projectId);
    }

    @Override
    public HarborRepoConfigDTO queryRepoConfigById(Long projectId, Long appServiceId) {
        HarborRepoDTO selectedHarborConfig = rdupmClient.queryHarborRepoConfig(projectId, appServiceId).getBody();
        if (selectedHarborConfig == null) {
            return null;
        }
        HarborRepoConfigDTO harborRepoConfig = selectedHarborConfig.getHarborRepoConfig();
        harborRepoConfig.setType(selectedHarborConfig.getRepoType());
        return harborRepoConfig;
    }

    @Override
    public List<Long> listProjectIdsByAppIds(List<Long> appIds) {
        if (ObjectUtils.isEmpty(appIds)) {
            return new ArrayList<>();
        }
        return appServiceMapper.listProjectIdsByAppIds(appIds);
    }

    @Override
    public ImageRepoInfoVO queryRepoConfigByCode(Long projectId, String code, String repoType, String repoCode) {
        ImageRepoInfoVO imageRepoInfoVO = null;
        AppServiceRepVO appServiceRepVO = queryByCode(projectId, code);
        if (appServiceRepVO == null) {
            throw new CommonException(DEVOPS_APP_SERVICE_NOT_EXIST);
        }
        CommonExAssertUtil.assertTrue((projectId.equals(appServiceRepVO.getProjectId())), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigByCode(projectId, repoType, repoCode);
        String dockerRegistry;
        String groupName;
        String dockerUsername;
        String dockerPassword;
        if (DevopsRegistryRepoType.CUSTOM_REPO.getType().equals(repoType)) {
            dockerRegistry = harborRepoDTO.getHarborRepoConfig().getRepoUrl();
            groupName = harborRepoDTO.getHarborRepoConfig().getRepoName();
            dockerUsername = harborRepoDTO.getHarborRepoConfig().getLoginName();
            dockerPassword = harborRepoDTO.getHarborRepoConfig().getPassword();

        } else {
            dockerRegistry = harborRepoDTO.getHarborRepoConfig().getRepoUrl();
            groupName = harborRepoDTO.getHarborRepoConfig().getRepoName();
            dockerUsername = harborRepoDTO.getPushRobot().getName();
            dockerPassword = harborRepoDTO.getPushRobot().getToken();
        }
        imageRepoInfoVO = new ImageRepoInfoVO();
        imageRepoInfoVO.setDockerRegistry(trimPrefix(dockerRegistry));
        imageRepoInfoVO.setGroupName(groupName);
        imageRepoInfoVO.setDockerUsername(dockerUsername);
        imageRepoInfoVO.setDockerPassword(dockerPassword);
        imageRepoInfoVO.setRepoType(repoType);
        imageRepoInfoVO.setRepoCode(repoCode);
        return imageRepoInfoVO;
    }

    private String trimPrefix(String dockerRegistry) {
        String dockerUrl = dockerRegistry.replace("http://", "").replace("https://", "");
        return dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;
    }

}
