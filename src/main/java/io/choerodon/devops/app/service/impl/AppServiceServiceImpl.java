package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.CUSTOM_REPO;
import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.DEFAULT_REPO;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Functions;
import com.google.gson.Gson;
import io.kubernetes.client.JSON;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.api.vo.hrdsCode.RepositoryPrivilegeViewDTO;
import io.choerodon.devops.api.vo.sonar.*;
import io.choerodon.devops.app.eventhandler.DevopsSagaHandler;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.AppServiceImportPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppImportServicePayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppServicePayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.app.task.DevopsTask;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.RoleDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.dto.repo.RdmMemberQueryDTO;
import io.choerodon.devops.infra.dto.repo.RdmMemberViewDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.exception.GitlabAccessInvalidException;
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.feign.HrdsCodeRepoClient;
import io.choerodon.devops.infra.feign.RdupmClient;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.feign.operator.AsgardServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.HrdsCodeRepoClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;


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
    private static final String SONAR_KEY = "%s-%s:%s";
    private static final Pattern REPOSITORY_URL_PATTERN = Pattern.compile("^http.*\\.git");
    private static final String ISSUE = "issue";
    private static final String COVERAGE = "coverage";
    private static final String SONAR = "sonar";
    private static final String NORMAL = "normal";
    private static final String APP_SERVICE = "appService";
    private static final String ERROR_USER_NOT_GITLAB_OWNER = "error.user.not.gitlab.owner";
    private static final String ERROR_GITLAB_USER_SYNC_FAILED = "error.gitlab.user.sync.failed";
    private static final String METRICS = "metrics";
    private static final String SONAR_NAME = "sonar_default";
    private static final String APPLICATION = "application";
    private static final String DUPLICATE = "duplicate";
    private static final String NORMAL_SERVICE = "normal_service";
    private static final String SHARE_SERVICE = "share_service";
    private static final String TEMP_MODAL = "\\?version=";
    private static final String LOGIN_NAME = "loginName";
    private static final String REAL_NAME = "realName";
    private static final String ERROR_PROJECT_APP_SVC_NUM_MAX = "error.project.app.svc.num.max";
    private static final String APPSERVICE = "app-service";
    private static final String APP = "app";

    /**
     * CI 文件模板
     */
    private static final String CI_FILE_TEMPLATE;

    @Autowired
    DevopsSagaHandler devopsSagaHandler;
    private final Gson gson = new Gson();
    private final JSON json = new JSON();
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
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
    @Autowired
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    @Lazy
    @Autowired
    private RdupmClient rdupmClient;
    @Autowired
    private HarborService harborService;
    @Autowired
    @Lazy
    private DevopsTask devopsTask;
    @Autowired
    private HrdsCodeRepoClient hrdsCodeRepoClient;
    @Autowired
    private DevopsCiCdPipelineMapper DevopsCiCdPipelineMapper;
    @Autowired
    private HrdsCodeRepoClientOperator hrdsCodeRepoClientOperator;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AsgardServiceClientOperator asgardServiceClientOperator;

    static {
        InputStream inputStream = AppServiceServiceImpl.class.getResourceAsStream("/shell/ci.sh");
        try {
            CI_FILE_TEMPLATE = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("error.load.ci.sh");
        }
    }


    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_APPLICATION_SERVICE,
            description = "Devops创建应用服务", inputSchema = "{}")
    @Transactional
    public AppServiceRepVO create(Long projectId, AppServiceReqVO appServiceReqVO) {
        appServiceReqVO.setProjectId(projectId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplicationService(appServiceReqVO.getCode());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        // 判断项目下是否还能创建应用服务
        checkEnableCreateAppSvcOrThrowE(projectId, 1);

        // 校验模板id和模板版本id是否都有值或者都为空
        boolean isTemplateNull = appServiceReqVO.getTemplateAppServiceId() == null;
        boolean isTemplateVersionNull = appServiceReqVO.getTemplateAppServiceVersionId() == null;

        if ((isTemplateNull && !isTemplateVersionNull) || (!isTemplateNull && isTemplateVersionNull)) {
            throw new CommonException("error.template.fields");
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

                throw new CommonException(ERROR_USER_NOT_GITLAB_OWNER);
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
                        .withRefType("app-service")
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
    private void checkEnableCreateAppSvcOrThrowE(Long projectId, int appSize) {
        if (Boolean.FALSE.equals(checkEnableCreateAppSvcWithSize(projectId, appSize))) {
            throw new CommonException(ERROR_PROJECT_APP_SVC_NUM_MAX);
        }
    }

    @Override
    public AppServiceRepVO query(Long projectId, Long appServiceId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        Boolean shareAppService = false;
        if (!appServiceDTO.getProjectId().equals(projectId)) {
            shareAppService = true;
        }
        AppServiceRepVO appServiceRepVO = dtoToRepVo(appServiceDTO);
        List<DevopsConfigVO> devopsConfigVOS = devopsConfigService.queryByResourceId(appServiceId, APP_SERVICE);
        if (!devopsConfigVOS.isEmpty()) {
            devopsConfigVOS.forEach(devopsConfigVO -> {
                if (devopsConfigVO.getType().equals(CHART)) {
                    appServiceRepVO.setChart(devopsConfigVO);
                }
            });
        }
        //url地址拼接
        if (appServiceDTO.getGitlabProjectId() != null && !shareAppService) {
            appServiceRepVO.setRepoUrl(concatRepoUrl(organizationDTO.getTenantNum(), projectDTO.getCode(), appServiceDTO.getCode()));
        }
        if (shareAppService) {
            ProjectDTO shareProjectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
            appServiceRepVO.setShareProjectName(shareProjectDTO.getName());
        }
        //添加harbor的配置信息
        HarborRepoDTO selectedHarborConfig = rdupmClient.queryHarborRepoConfig(projectId, appServiceId).getBody();
        if (!Objects.isNull(selectedHarborConfig) && !Objects.isNull(selectedHarborConfig.getHarborRepoConfig())) {
            selectedHarborConfig.getHarborRepoConfig().setType(selectedHarborConfig.getRepoType());
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
                                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)), PageRequestUtil.checkSortIsEmpty(pageable))),
                this::dtoToRepVoWithoutIamUserFill);
    }


    @Saga(code = SagaTopicCodeConstants.DEVOPS_APP_DELETE,
            description = "Devops删除应用服务", inputSchemaClass = DevOpsAppServicePayload.class)
    @Transactional
    @Override
    public void delete(Long projectId, Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (appServiceDTO == null) {
            return;
        }

        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        // 禁止删除未失败或者启用状态的应用服务
        if (Boolean.TRUE.equals(appServiceDTO.getActive())
                && Boolean.FALSE.equals(appServiceDTO.getFailed())) {
            throw new CommonException("error.delete.nonfailed.app.service", appServiceDTO.getName());
        }
        // 验证改应用服务在其他项目是否被生成实例
        checkAppserviceIsShareDeploy(projectId, appServiceId);
        AppServiceMsgVO checkResult = checkCanDisable(appServiceId, projectId);
        if (checkResult.getCheckResources()) {
            throw new CommonException("error.delete.application.service.due.to.share");
        }
        if (checkResult.getCheckRule()) {
            throw new CommonException("error.delete.application.service.due.to.resources");
        }
        if (checkResult.getCheckCi()) {
            throw new CommonException("error.delete.application.service.due.to.ci.pipeline");
        }

        if (devopsCiPipelineService.selectCountByAppServiceId(appServiceId) != 0) {
            throw new CommonException("error.delete.app.service.due.to.ci.pipeline", appServiceId);
        }

        appServiceDTO.setSynchro(Boolean.FALSE);
        appServiceMapper.updateByPrimaryKey(appServiceDTO);

        DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
        devOpsAppServicePayload.setAppServiceId(appServiceId);
        devOpsAppServicePayload.setIamProjectId(projectId);
        //删除应用服务后需要发送消息，这里将消息的内容封近paylod
        List<DevopsUserPermissionVO> list = pagePermissionUsers(appServiceDTO.getProjectId(), appServiceDTO.getId(), new PageRequest(0, 0), null).getContent();
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(devopsUserPermissionVO -> devopsUserPermissionVO.setCreationDate(null));
        }
        devOpsAppServicePayload.setAppServiceDTO(appServiceDTO);
        devOpsAppServicePayload.setDevopsUserPermissionVOS(list);
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
        Long organizationId = baseServiceClientOperator.queryIamProjectById(projectId).getOrganizationId();
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(organizationId);
        Set<Long> projectIds = projectDTOS.stream().filter(projectDTO -> !projectDTO.getId().equals(projectId)).map(ProjectDTO::getId).collect(toSet());
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        List<AppServiceInstanceDTO> appServiceInstanceDTOS = appServiceInstanceMapper.listByProjectIdsAndAppServiceId(projectIds, appServiceId);
        if (!CollectionUtils.isEmpty(appServiceInstanceDTOS)) {
            throw new CommonException("error.not.delete.service.by.other.project.deployment");
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
        devopsBranchService.deleteAllBaranch(appServiceId);
        gitlabCommitMapper.deleteByAppServiceId(appServiceId);
        mergeRequestMapper.deleteByProjectId(appServiceDTO.getGitlabProjectId());
        gitlabPipelineMapper.deleteByAppServiceId(appServiceId);
        // 删除应用服务的版本
        appServiceVersionService.deleteByAppServiceId(appServiceId);
        //删除gitlab project
        if (appServiceDTO.getGitlabProjectId() != null) {
            Integer gitlabProjectId = appServiceDTO.getGitlabProjectId();
            GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectById(gitlabProjectId);
            if (gitlabProjectDTO != null && gitlabProjectDTO.getId() != null) {
                UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                Integer gitlabUserId = TypeUtil.objToInt(userAttrDTO.getGitlabUserId());
                gitlabServiceClientOperator.deleteProjectById(gitlabProjectId, gitlabUserId);
            }
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

        CommonExAssertUtil.assertTrue(projectId.equals(oldAppServiceDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        AppServiceDTO appServiceDTO = ConvertUtils.convertObject(appServiceUpdateDTO, AppServiceDTO.class);
        List<DevopsConfigVO> devopsConfigVOS = new ArrayList<>();
        DevopsConfigVO chart = new DevopsConfigVO();
        if (ObjectUtils.isEmpty(appServiceUpdateDTO.getChart())) {
            chart.setCustom(false);
        } else {
            chart = appServiceUpdateDTO.getChart();
            chart.setCustom(Boolean.TRUE);
            ConfigVO configVO = chart.getConfig();
            CommonExAssertUtil.assertNotNull(configVO, "error.chart.config.null");
            boolean usernameEmpty = StringUtils.isEmpty(configVO.getUserName());
            boolean passwordEmpty = StringUtils.isEmpty(configVO.getPassword());
            if (!usernameEmpty && !passwordEmpty) {
                configVO.setUserName(configVO.getUserName());
                configVO.setPassword(configVO.getPassword());
                configVO.setPrivate(Boolean.TRUE);
            } else {
                configVO.setPrivate(Boolean.FALSE);
            }

            // 用户名和密码要么都为空, 要么都有值
            CommonExAssertUtil.assertTrue(((usernameEmpty && passwordEmpty) || (!usernameEmpty && !passwordEmpty)), "error.chart.auth.invalid");
        }
        chart.setType(CHART);
        devopsConfigVOS.add(chart);

        //处理helm仓库的配置
        devopsConfigService.operate(appServiceId, APP_SERVICE, devopsConfigVOS);
        //保存应用服务与harbor仓库的关系
        if (!Objects.isNull(appServiceUpdateDTO.getHarborRepoConfigDTO())) {
            if (DEFAULT_REPO.equals(appServiceUpdateDTO.getHarborRepoConfigDTO().getType())) {
                HarborRepoDTO beforeRepo = rdupmClient.queryHarborRepoConfig(projectId, appServiceId).getBody();
                //如果之前存在非默认仓库与应用服务的关联关系，则删除
                if (!Objects.isNull(beforeRepo) && !DEFAULT_REPO.equals(beforeRepo.getRepoType())) {
                    deleteHarborAppServiceRel(projectId, appServiceDTO.getId());
                }
            }
            if (CUSTOM_REPO.equals((appServiceUpdateDTO.getHarborRepoConfigDTO().getType()))) {
                deleteHarborAppServiceRel(projectId, appServiceDTO.getId());
                rdupmClient.saveRelationByService(projectId, appServiceDTO.getId(), appServiceUpdateDTO.getHarborRepoConfigDTO().getRepoId());
            }
        }
        if (appServiceUpdateDTO.getChart() != null) {
            DevopsConfigDTO chartConfig = devopsConfigService.queryRealConfig(appServiceId, APP_SERVICE, CHART, AUTHTYPE_PULL);
            appServiceDTO.setChartConfigId(chartConfig.getId());
        }

        if (!oldAppServiceDTO.getName().equals(appServiceUpdateDTO.getName())) {
            checkName(oldAppServiceDTO.getProjectId(), appServiceDTO.getName());
        }
        baseUpdate(appServiceDTO);
        return true;
    }

    private void deleteHarborAppServiceRel(Long projectId, Long appServcieId) {
        HarborCustomRepo harborCustomRepoVO = rdupmClient.listRelatedCustomRepoByService(projectId, appServcieId).getBody();
        if (!Objects.isNull(harborCustomRepoVO)) {
            rdupmClient.deleteRelationByService(projectId, appServcieId, harborCustomRepoVO.getId());
        }
    }


    @Override
    @Transactional
    @Saga(code = SagaTopicCodeConstants.DEVOPS_APP_SYNC_STATUS,
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
                throw new CommonException("error.disable.or.enable.application.service");
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
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
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

        return appServiceMsgVO;
    }

    private boolean appServiceIsExistsCi(Long projectId, Long appServiceId) {
        CiCdPipelineDTO devopsCiPipelineDTO = new CiCdPipelineDTO();
        devopsCiPipelineDTO.setAppServiceId(appServiceId);
        devopsCiPipelineDTO.setProjectId(projectId);
        return DevopsCiCdPipelineMapper.selectCount(devopsCiPipelineDTO) > 0;
    }

    @Override
    public Page<AppServiceRepVO> pageByOptions(Long projectId, Boolean isActive, Boolean hasVersion,
                                               Boolean appMarket,
                                               String type, Boolean doPage,
                                               PageRequest pageable, String params, Boolean checkMember) {

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId, false, false, false);
        Page<AppServiceDTO> applicationServiceDTOS = basePageByOptions(projectId, isActive, hasVersion, appMarket, type, doPage, pageable, params, checkMember);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId(), false);
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        initApplicationParams(projectDTO, organizationDTO, applicationServiceDTOS.getContent(), urlSlash);

        Page<AppServiceRepVO> destination = new Page<>();
        BeanUtils.copyProperties(applicationServiceDTOS, destination, "content");
        if (applicationServiceDTOS.getContent() != null) {
            List<AppServiceDTO> appServiceDTOList = applicationServiceDTOS.getContent();
            List<Long> userIds = appServiceDTOList.stream().map(AppServiceDTO::getCreatedBy).collect(toList());
            userIds.addAll(appServiceDTOList.stream().map(AppServiceDTO::getLastUpdatedBy).collect(toList()));
            List<Long> distinctIds = userIds.stream().distinct().collect(toList());

            Map<Long, IamUserDTO> users = baseServiceClientOperator.listUsersByIds(new ArrayList<>(distinctIds)).stream().collect(Collectors.toMap(IamUserDTO::getId, u -> u));
            List<String> refIds = applicationServiceDTOS.getContent().stream().map(appServiceDTO -> String.valueOf(appServiceDTO.getId())).collect(toList());
            List<AppServiceRepVO> appServiceRepVOS = applicationServiceDTOS.getContent().stream().map(appServiceDTO -> dtoToRepVo(appServiceDTO, users)).collect(toList());
            if (!CollectionUtils.isEmpty(refIds)) {
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
    public List<AppServiceRepVO> listByActive(Long projectId) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId, false, false, false);
        boolean projectOwner = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        List<AppServiceDTO> applicationDTOServiceList;
        if (projectOwner) {
            applicationDTOServiceList = appServiceMapper.listByActive(projectId);
        } else {
            Set<Long> appServiceIds = getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new ArrayList<>();
            }
            applicationDTOServiceList = appServiceMapper.listProjectMembersAppServiceByActive(projectId, appServiceIds, userId);
        }

        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId(), false);
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        List<Long> userIds = applicationDTOServiceList.stream().map(AppServiceDTO::getCreatedBy).collect(toList());
        userIds.addAll(applicationDTOServiceList.stream().map(AppServiceDTO::getLastUpdatedBy).collect(toList()));

        List<Long> distinctIds = userIds.stream().distinct().collect(toList());
        Map<Long, IamUserDTO> users = baseServiceClientOperator.listUsersByIds(new ArrayList<>(distinctIds)).stream().collect(toMap(IamUserDTO::getId, u -> u));

        initApplicationParams(projectDTO, organizationDTO, applicationDTOServiceList, urlSlash);

        return applicationDTOServiceList.stream().map(appServiceDTO -> dtoToRepVo(appServiceDTO, users)).collect(toList());
    }

    @Override
    public Integer countByActive(Long projectId) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        int count;
        if (projectOwnerOrRoot) {
            count = appServiceMapper.countByActive(projectId);
        } else {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
            Set<Long> appServiceIds = getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return 0;
            }
            count = appServiceMapper.countProjectMembersAppServiceByActive(projectId, appServiceIds, userId);
        }

        return count;
    }

    @Override
    public List<AppServiceRepVO> listAll(Long projectId) {
        List<AppServiceRepVO> appServiceRepVOList = ConvertUtils.convertList(baseListAll(projectId), AppServiceRepVO.class);
        appServiceRepVOList.forEach(appServiceRepVO -> {
            if (appServiceRepVO.getProjectId() != null && appServiceRepVO.getProjectId().equals(projectId)) {
                appServiceRepVO.setServiceType(NORMAL_SERVICE);
            } else {
                appServiceRepVO.setServiceType(SHARE_SERVICE);
            }
        });
        return appServiceRepVOList;
    }

    @Override
    public void checkName(Long projectId, String name) {
        if (!isNameUnique(projectId, name)) {
            throw new CommonException("error.name.exist");
        }
    }

    @Override
    public void checkCode(Long projectId, String code) {
        if (!isCodeUnique(projectId, code)) {
            throw new CommonException("error.code.exist");
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

    @Override
    public void operationApplication(DevOpsAppServicePayload devOpsAppServicePayload) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByGitlabAppGroupId(
                TypeUtil.objToInteger(devOpsAppServicePayload.getGroupId()));

        AppServiceDTO appServiceDTO = baseQueryByCode(devOpsAppServicePayload.getPath(),
                devopsProjectDTO.getIamProjectId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(devOpsAppServicePayload.getUserId()));

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsProjectDTO.getIamProjectId());
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator
                .queryProjectByName(organizationDTO.getTenantNum() + "-" + projectDTO.getCode(), appServiceDTO.getCode(),
                        devOpsAppServicePayload.getUserId());
        Integer gitlabProjectId = gitlabProjectDO.getId();
        if (gitlabProjectId == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(devOpsAppServicePayload.getGroupId(),
                    devOpsAppServicePayload.getPath(),
                    devOpsAppServicePayload.getUserId(), false);
        }
        devOpsAppServicePayload.setGitlabProjectId(gitlabProjectDO.getId());

        String applicationServiceToken = getApplicationToken(appServiceDTO.getToken(), devOpsAppServicePayload.getGitlabProjectId(), devOpsAppServicePayload.getUserId());
        appServiceDTO.setGitlabProjectId(gitlabProjectDO.getId());
        appServiceDTO.setSynchro(true);
        appServiceDTO.setFailed(false);
        setProjectHook(appServiceDTO, devOpsAppServicePayload.getGitlabProjectId(), applicationServiceToken, devOpsAppServicePayload.getUserId());

        // 为项目下的成员分配对于此gitlab项目的权限
//        operateGitlabMemberPermission(devOpsAppServicePayload);
        if (devOpsAppServicePayload.getTemplateAppServiceId() != null && devOpsAppServicePayload.getTemplateAppServiceVersionId() != null) {
            LOGGER.info("The current app service id is {} and the service code is {}", appServiceDTO.getId(), appServiceDTO.getCode());
            LOGGER.info("The template app service id is not null: {}, start to clone template repository", devOpsAppServicePayload.getTemplateAppServiceId());

            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            String newGroupName = organizationDTO.getTenantNum() + "-" + projectDTO.getCode();
            String repositoryUrl = repoUrl + newGroupName + "/" + appServiceDTO.getCode() + GIT;
            cloneAndPushCode(appServiceDTO, userAttrDTO, devOpsAppServicePayload.getTemplateAppServiceId(), devOpsAppServicePayload.getTemplateAppServiceVersionId(), repositoryUrl, newGroupName);
        }

        appServiceMapper.updateByIdSelectiveWithoutAudit(appServiceDTO);
    }


    @Override
    public void operationAppServiceImport(DevOpsAppImportServicePayload devOpsAppServiceImportPayload) {
        // 准备相关的数据
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByGitlabAppGroupId(
                TypeUtil.objToInteger(devOpsAppServiceImportPayload.getGroupId()));
        AppServiceDTO appServiceDTO = baseQueryByCode(devOpsAppServiceImportPayload.getPath(),
                devopsProjectDTO.getIamProjectId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsProjectDTO.getIamProjectId());
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectByName(
                organizationDTO.getTenantNum() + "-" + projectDTO.getCode(),
                appServiceDTO.getCode(),
                devOpsAppServiceImportPayload.getUserId());
        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(devOpsAppServiceImportPayload.getGroupId(),
                    devOpsAppServiceImportPayload.getPath(),
                    devOpsAppServiceImportPayload.getUserId(), false);
        }
        devOpsAppServiceImportPayload.setGitlabProjectId(gitlabProjectDO.getId());

        // 为项目下的成员分配对于此gitlab项目的权限
//        operateGitlabMemberPermission(devOpsAppServiceImportPayload);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(TypeUtil.objToLong(devOpsAppServiceImportPayload.getUserId()));


        // clone外部代码仓库
        String applicationDir = APPLICATION + GenerateUUID.generateUUID();

        if (devOpsAppServiceImportPayload.getTemplate() != null && devOpsAppServiceImportPayload.getTemplate()) {
            String[] tempUrl = devOpsAppServiceImportPayload.getRepositoryUrl().split(TEMP_MODAL);
            if (tempUrl.length < 2) {
                throw new CommonException("error.temp.git.url");
            }
            String templateVersion = tempUrl[1];
            String repositoryUrl = tempUrl[0];
            gitUtil.cloneAppMarket(applicationDir, templateVersion, repositoryUrl, devOpsAppServiceImportPayload.getAccessToken());
            File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));
            replaceParams(appServiceDTO.getCode(), organizationDTO.getTenantNum() + "-" + projectDTO.getCode(), applicationDir, null, null, true);
            Git newGit = gitUtil.initGit(applicationWorkDir);
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getTenantNum()
                    + "-" + projectDTO.getCode() + "/" + appServiceDTO.getCode() + ".git");
            String accessToken = getToken(devOpsAppServiceImportPayload.getGitlabProjectId(), applicationDir, userAttrDTO);
            try {
                gitUtil.commitAndPushForMaster(newGit, appServiceDTO.getRepoUrl(), templateVersion, accessToken);
            } catch (Exception e) {
                releaseResources(applicationWorkDir, newGit);
                throw e;
            }
            releaseResources(applicationWorkDir, newGit);
        } else {
            Git repositoryGit = gitUtil.cloneRepository(applicationDir, devOpsAppServiceImportPayload.getRepositoryUrl(), devOpsAppServiceImportPayload.getAccessToken());
            // 设置Application对应的gitlab项目的仓库地址
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            appServiceDTO.setRepoUrl(repoUrl + organizationDTO.getTenantNum()
                    + "-" + projectDTO.getCode() + "/" + appServiceDTO.getCode() + ".git");

            File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));

            String protectedBranchName = null;

            try {
                List<Ref> refs = repositoryGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                // 获取push代码所需的access token
                String accessToken = getToken(devOpsAppServiceImportPayload.getGitlabProjectId(), applicationDir, userAttrDTO);

                for (Ref ref : refs) {
                    String branchName;
                    if (ref.getName().contains(Constants.R_HEADS)) {
                        branchName = ref.getName().split("/")[2];
                        // 当前的本地的 refs/heads/ 内的引用是保护分支的名称，大部分保护分支是master，不排除develop等其他分支的可能
                        protectedBranchName = branchName;
                    } else {
                        branchName = ref.getName().split("/")[3];
                    }

                    // 跳过对活跃本地分支A: /refs/heads/A 和 /refs/remotes/origin/A 之间的第二次重复的推送
                    if (branchName.equals(protectedBranchName) && ref.getName().contains(Constants.R_REMOTES)) {
                        continue;
                    }

                    if (ref.getName().contains(Constants.R_REMOTES)) {
                        repositoryGit.checkout().setCreateBranch(true).setName(branchName).setStartPoint(ref.getName()).call();
                    }


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
        try {
            // 设置application的属性
            String applicationServiceToken = getApplicationToken(appServiceDTO.getToken(), gitlabProjectDO.getId(), devOpsAppServiceImportPayload.getUserId());
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
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_APP_FAIL,
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
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
//            DevopsConfigDTO harborConfigDTO = devopsConfigService.queryRealConfig(appServiceDTO.getId(), APP_SERVICE, HARBOR, AUTHTYPE_PUSH);
            DevopsConfigDTO harborConfigDTO = harborService.queryRepoConfigToDevopsConfig(projectDTO.getId(), appServiceDTO.getId(), AUTHTYPE_PUSH);
            ConfigVO harborProjectConfig = gson.fromJson(harborConfigDTO.getConfig(), ConfigVO.class);
            Map<String, String> params = new HashMap<>();
            String groupName = organizationDTO.getTenantNum() + "-" + projectDTO.getCode();
            if (harborProjectConfig.getProject() != null) {
                groupName = harborProjectConfig.getProject();
            }
            String dockerUrl = harborProjectConfig.getUrl().replace("http://", "").replace("https://", "");
            dockerUrl = dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;
            DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, SONAR_NAME);
            if (sonarConfig != null) {
                params.put("{{ SONAR_LOGIN }}", sonarConfig.getConfig());
                params.put("{{ SONAR_URL }}", sonarqubeUrl);
            } else {
                params.put("{{ SONAR_LOGIN }}", "");
                params.put("{{ SONAR_URL }}", "");
            }
            params.put("{{ SONAR_PROJECT_KEY }}", organizationDTO.getTenantNum() + "-" + projectDTO.getCode() + ":" + appServiceDTO.getCode());
            params.put("{{ GROUP_NAME }}", groupName);
            params.put("{{ PROJECT_NAME }}", appServiceDTO.getCode());
            params.put("{{ PRO_CODE }}", projectDTO.getCode());
            params.put("{{ ORG_CODE }}", organizationDTO.getTenantNum());
            params.put("{{ DOCKER_REGISTRY }}", dockerUrl);
            params.put("{{ DOCKER_USERNAME }}", "'" + harborProjectConfig.getUserName() + "'");
            params.put("{{ DOCKER_PASSWORD }}", harborProjectConfig.getPassword());
            params.put("{{ HARBOR_CONFIG_ID }}", harborConfigDTO.getId().toString());
            params.put("{{ REPO_TYPE }}", harborConfigDTO.getType());
            return FileUtil.replaceReturnString(CI_FILE_TEMPLATE, params);
        } catch (CommonException e) {
//            LOGGER.warn("Error query ci.sh for app-service with token {} , the ex is ", token, e);
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
    public Page<AppServiceReqVO> pageByActiveAndPubAndVersion(Long projectId, PageRequest pageable,
                                                              String params) {
        return ConvertUtils.convertPage(basePageByActiveAndPubAndHasVersion(projectId, true, pageable, params), AppServiceReqVO.class);
    }

//    @Override
//    public List<AppServiceUserPermissionRespVO> listAllUserPermission(Long appServiceId) {
//        List<Long> userIds = appServiceUserPermissionService.baseListByAppId(appServiceId).stream().map(AppServiceUserRelDTO::getIamUserId)
//                .collect(Collectors.toList());
//        List<IamUserDTO> userEList = baseServiceClientOperator.listUsersByIds(userIds);
//        List<AppServiceUserPermissionRespVO> resultList = new ArrayList<>();
//        userEList.forEach(
//                e -> resultList.add(new AppServiceUserPermissionRespVO(e.getId(), e.getLoginName(), e.getRealName())));
//        return resultList;
//    }

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
    @Saga(code = SagaTopicCodeConstants.DEVOPS_IMPORT_GITLAB_PROJECT,
            description = "Devops从外部代码平台导入到gitlab项目", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public AppServiceRepVO importApp(Long projectId, AppServiceImportVO appServiceImportVO, Boolean isTemplate) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        checkEnableCreateAppSvcOrThrowE(projectId, 1);

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
        if (isTemplate == null || !isTemplate) {
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

                throw new CommonException(ERROR_USER_NOT_GITLAB_OWNER);
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
        devOpsAppImportServicePayload.setTemplate(isTemplate);

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
    public Boolean checkChart(String url, @Nullable String username, @Nullable String password) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(url);
        configurationProperties.setType(CHART);
        if (username != null && password != null) {
            configurationProperties.setUsername(username);
            configurationProperties.setPassword(password);
        }
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        try {
            // 获取健康检查信息是不需要认证信息的
            Call<Object> getHealth = chartClient.getHealth();
            getHealth.execute();
        } catch (Exception e) {
            throw new CommonException("error.chart.not.available");
        }

        // 验证用户名密码
        Response<Void> response = null;
        try {
            // 获取首页html需要认证信息
            Call<Void> getHomePage = chartClient.getHomePage();
            response = getHomePage.execute();
        } catch (Exception ex) {
            throw new CommonException("error.chart.authentication.failed");
        }
        if (response != null && !response.isSuccessful()) {
            throw new CommonException("error.chart.authentication.failed");
        }

        return true;
    }

    @Override
    public SonarContentsVO getSonarContent(Long projectId, Long appServiceId) {
        try {
            checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, AppServiceEvent.SONAR_LIST);
        } catch (GitlabAccessInvalidException e) {
            return null;
        }
        //没有使用sonarqube直接返回空对象
        if (sonarqubeUrl.equals("")) {
            return new SonarContentsVO();
        }
        SonarContentsVO sonarContentsVO = new SonarContentsVO();
        List<SonarContentVO> sonarContentVOS = new ArrayList<>();
        AppServiceDTO appServiceDTO = baseQuery(appServiceId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());


        //初始化sonarClient
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = String.format(SONAR_KEY, organization.getTenantNum(), projectDTO.getCode(), appServiceDTO.getCode());
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
            queryContentMap.put("componentKey", key);
            queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,new_bugs,new_vulnerabilities,sqale_index,code_smells,new_technical_debt,new_code_smells,coverage,tests,new_coverage,duplicated_lines_density,duplicated_blocks,new_duplicated_lines_density,ncloc,ncloc_language_distribution");

            //根据project-key查询sonarqube项目内容
            Response<SonarComponent> sonarComponentResponse = sonarClient.getSonarComponent(queryContentMap).execute();
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
                        Quality quality = gson.fromJson(measure.getValue(), Quality.class);
                        sonarContentsVO.setStatus(quality.getLevel());
                        break;
                    default:
                        break;
                }
            });
            sonarContentsVO.setSonarContents(sonarContentVOS);
            cacheSonarContents(projectId, appServiceId, sonarContentsVO);
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return sonarContentsVO;
    }

    private void cacheSonarContents(Long projectId, Long appServiceId, SonarContentsVO sonarContentsVO) {
        redisTemplate.opsForValue().set(SONAR + ":" + projectId + ":" + appServiceId, JsonHelper.marshalByJackson(sonarContentsVO));
    }

    public String getTimestampTimeV17(String str) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
        Date date = null;
        try {
            date = dateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
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
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = String.format(SONAR_KEY, organizationDTO.getTenantNum(), projectDTO.getCode(), applicationDTO.getCode());
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
                        throw new CommonException("error.sonarqube.user");
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
        Long organizationId = baseServiceClientOperator.queryIamProjectById(projectId).getOrganizationId();
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
    public Page<DevopsUserPermissionVO> combineOwnerAndMember(List<DevopsUserPermissionVO> allProjectMembers, List<DevopsUserPermissionVO> allProjectOwners, PageRequest pageable) {
        List<DevopsUserPermissionVO> userPermissionVOS = new ArrayList<>(allProjectOwners);
        userPermissionVOS.addAll(allProjectMembers);
        if (userPermissionVOS.isEmpty()) {
            return ConvertUtils.convertPage(new Page<>(), DevopsUserPermissionVO.class);
        } else {
            List<DevopsUserPermissionVO> resultPermissionVOs = new ArrayList<>();
            Map<Long, List<DevopsUserPermissionVO>> maps = userPermissionVOS.stream().collect(Collectors.groupingBy(DevopsUserPermissionVO::getIamUserId));
            for (Map.Entry<Long, List<DevopsUserPermissionVO>> entry : maps.entrySet()) {
                DevopsUserPermissionVO userPermissionVO = entry.getValue().get(0);
                if (entry.getValue().size() > 1) {
                    List<RoleDTO> roleDTOS = new ArrayList<>();
                    entry.getValue().forEach(v -> roleDTOS.addAll(v.getRoles()));
                    userPermissionVO.setRoles(roleDTOS);
                }
                resultPermissionVOs.add(userPermissionVO);
            }
            resultPermissionVOs = PageRequestUtil.sortUserPermission(resultPermissionVOs, new Sort(Sort.Direction.DESC, "creationDate"));
            return PageInfoUtil.createPageFromList(new ArrayList<>(resultPermissionVOs), pageable);
        }
    }

    @Override
    public DevopsUserPermissionVO iamUserTOUserPermissionVO(IamUserDTO iamUserDTO, Boolean isGitlabProjectOwner) {
        DevopsUserPermissionVO devopsUserPermissionVO = new DevopsUserPermissionVO();
        devopsUserPermissionVO.setIamUserId(iamUserDTO.getId());
        if (iamUserDTO.getLdap()) {
            devopsUserPermissionVO.setLoginName(iamUserDTO.getLoginName());
        } else {
            devopsUserPermissionVO.setLoginName(iamUserDTO.getEmail());
        }
        devopsUserPermissionVO.setRealName(iamUserDTO.getRealName());
        devopsUserPermissionVO.setRoles(iamUserDTO.getRoles());
        devopsUserPermissionVO.setCreationDate(iamUserDTO.getCreationDate());
        devopsUserPermissionVO.setGitlabProjectOwner(isGitlabProjectOwner);
        return devopsUserPermissionVO;
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
    @Saga(code = SagaTopicCodeConstants.DEVOPS_IMPORT_INTERNAL_APPLICATION_SERVICE,
            description = "Devops创建应用服务", inputSchema = "{}")
    public void importAppServiceInternal(Long projectId, List<ApplicationImportInternalVO> importInternalVOS) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        checkEnableCreateAppSvcOrThrowE(projectId, importInternalVOS.size());
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

                    throw new CommonException(ERROR_USER_NOT_GITLAB_OWNER);
                }
            }

            AppServiceImportPayload appServiceImportPayload = new AppServiceImportPayload();
            appServiceImportPayload.setAppServiceId(appServiceDTO.getId());
            appServiceImportPayload.setGitlabGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()));
            appServiceImportPayload.setIamUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            appServiceImportPayload.setVersionId(importInternalVO.getVersionId());
            appServiceImportPayload.setOrgCode(organizationDTO.getTenantNum());
            appServiceImportPayload.setProjectId(projectId);
            appServiceImportPayload.setProCode(projectDTO.getCode());
            appServiceImportPayload.setOldAppServiceId(importInternalVO.getAppServiceId());
            importPayloadList.add(appServiceImportPayload);
        });

        importPayloadList.forEach(payload -> {
            producer.apply(
                    StartSagaBuilder
                            .newBuilder()
                            .withLevel(ResourceLevel.PROJECT)
                            .withRefType("app")
                            .withSagaCode(SagaTopicCodeConstants.DEVOPS_IMPORT_INTERNAL_APPLICATION_SERVICE)
                            .withPayloadAndSerialize(payload)
                            .withRefId(String.valueOf(payload.getAppServiceId()))
                            .withSourceId(projectId),
                    builder -> {
                    });
        });
    }

    @Override
    public void importAppServiceGitlab(AppServiceImportPayload appServiceImportPayload) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceImportPayload.getAppServiceId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(appServiceImportPayload.getIamUserId());

        String newGroupName = appServiceImportPayload.getOrgCode() + "-" + appServiceImportPayload.getProCode();
        GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectByName(
                newGroupName,
                appServiceDTO.getCode(),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        //创建gitlab 应用
        if (gitlabProjectDTO.getId() == null) {
            gitlabProjectDTO = gitlabServiceClientOperator.createProject(
                    appServiceImportPayload.getGitlabGroupId(),
                    appServiceDTO.getCode(),
                    TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), false);
        }

        appServiceDTO.setGitlabProjectId(gitlabProjectDTO.getId());
        String applicationServiceToken = getApplicationToken(appServiceDTO.getToken(), appServiceDTO.getGitlabProjectId(), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
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
        ProjectDTO oldProjectDTO = baseServiceClientOperator.queryIamProjectById(oldAppServiceDTO.getProjectId());
        Tenant oldOrganizationDTO = baseServiceClientOperator.queryOrganizationById(oldProjectDTO.getOrganizationId());
        oldGroup = oldOrganizationDTO.getTenantNum() + "-" + oldProjectDTO.getCode();
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
            throw new CommonException("error.app.service.update");
        }
        return appServiceMapper.selectByPrimaryKey(applicationDTO.getId());
    }

    @Override
    public AppServiceDTO baseQuery(Long appServiceId) {
        return appServiceMapper.selectByPrimaryKey(appServiceId);
    }

    @Override
    public Page<AppServiceDTO> basePageByOptions(Long projectId, Boolean isActive, Boolean hasVersion, Boolean
            appMarket, String type, Boolean doPage, PageRequest pageable, String params, Boolean checkMember) {

        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        Long userId = DetailsHelper.getUserDetails().getUserId();

//        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        List<AppServiceDTO> list;
//        if (projectOwnerOrRoot) {
//            //是否需要分页
//            if (doPage == null || doPage) {
                return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                        () -> appServiceMapper.list(projectId, isActive, hasVersion, type,
                                TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)), PageRequestUtil.checkSortIsEmpty(pageable)));
//            } else {
//                list = appServiceMapper.list(projectId, isActive, hasVersion, type,
//                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
//                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)), PageRequestUtil.checkSortIsEmpty(pageable));
//            }
//        } else {
//            // 是否需要进行项目成员gitlab角色校验
//            Set<Long> appServiceIds;
//            if (checkMember) {
//                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
//                appServiceIds = getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
//                if (CollectionUtils.isEmpty(appServiceIds)) {
//                    return new Page<>();
//                }
//            } else {
//                appServiceIds = null;
//            }
//            //是否需要分页
//            if (doPage == null || doPage) {
//                return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
//                        () -> appServiceMapper.listProjectMembersAppService(projectId, appServiceIds, isActive, hasVersion, type,
//                                TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
//                                TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)), pageable.getSort() == null, userId));
//            } else {
//                list = appServiceMapper.listProjectMembersAppService(projectId, appServiceIds, isActive, hasVersion, type,
//                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
//                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)), pageable.getSort() == null, userId);
//            }
//        }
//
//        return PageInfoUtil.listAsPage(list);
    }

    @Override
    public AppServiceDTO baseQueryByCode(String code, Long projectId) {
        AppServiceDTO applicationDTO = new AppServiceDTO();
        applicationDTO.setProjectId(projectId);
        applicationDTO.setCode(code);
        return appServiceMapper.selectOne(applicationDTO);
    }

    @Override
    public AppServiceDTO baseQueryByCodeWithNullProject(String code) {
        return appServiceMapper.queryByCodeWithNoProject(code);
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
    public Page<AppServiceDTO> basePageByActiveAndPubAndHasVersion(Long projectId, Boolean isActive,
                                                                   PageRequest pageable, String params) {
        Map<String, Object> searchParam = null;
        List<String> paramList = null;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> searchParamMap = json.deserialize(params, Map.class);
            searchParam = TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM));
            paramList = TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS));
        }
        final Map<String, Object> finalSearchParam = searchParam;
        final List<String> finalParam = paramList;

        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> appServiceMapper
                .basePageByActiveAndPubAndHasVersion(projectId, isActive, finalSearchParam, finalParam));
    }

    @Override
    public AppServiceDTO baseQueryByToken(String token) {
        return appServiceMapper.queryByToken(Objects.requireNonNull(token));
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
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
            return gitlabUrl + urlSlash
                    + organizationDTO.getTenantNum() + "-" + projectDTO.getCode() + "/"
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
        facets.stream().filter(facet -> facet.getProperty().equals(SEVERITIES)).forEach(facet -> {
            facet.getValues().forEach(value -> {
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

    @Override
    public AppServiceDTO baseCreate(AppServiceDTO appServiceDTO) {
        if (appServiceMapper.insert(appServiceDTO) != 1) {
            throw new CommonException("error.application.create.insert");
        }
        return appServiceDTO;
    }

    @Override
    public Page<AppServiceGroupInfoVO> pageAppServiceByMode(Long projectId, Boolean share, Long searchProjectId, String param, PageRequest pageable) {

        List<AppServiceGroupInfoVO> appServiceGroupInfoVOS = new ArrayList<>();
        List<AppServiceDTO> appServiceDTOList = new ArrayList<>();
        List<ProjectDTO> projectDTOS = new ArrayList<>();
        if (Boolean.TRUE.equals(share)) {
            Long organizationId = baseServiceClientOperator.queryIamProjectById(projectId).getOrganizationId();
            List<Long> projectIds = new ArrayList<>();
            if (ObjectUtils.isEmpty(searchProjectId)) {
                projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(organizationId);
                projectIds = projectDTOS.stream().filter(ProjectDTO::getEnabled)
                        .filter(v -> !projectId.equals(v.getId()))
                        .map(ProjectDTO::getId).collect(Collectors.toList());
            } else {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(searchProjectId);
                projectIds.add(searchProjectId);
                projectDTOS.add(projectDTO);
            }
            if (ObjectUtils.isEmpty(projectDTOS)) return new Page<>();
            //查询组织共享和共享项目的应用服务
            if (projectIds.isEmpty()) {
                return new Page<>();
            }
            List<AppServiceDTO> organizationAppServices = appServiceMapper.queryOrganizationShareApps(projectIds, param, projectId);
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
    public List<AppServiceGroupVO> listAllAppServices(Long projectId, String type, String param, Boolean deployOnly, String serviceType) {
        List<AppServiceDTO> list = new ArrayList<>();
        List<String> params = new ArrayList<>();
        List<AppServiceGroupVO> appServiceGroupList = new ArrayList<>();
        if (param != null && !param.isEmpty()) {
            params.add(param);
        }
        switch (type) {
            case NORMAL_SERVICE: {
                list.addAll(appServiceMapper.list(projectId, Boolean.TRUE, true, serviceType, null, params, ""));
                AppServiceGroupVO appServiceGroupVO = new AppServiceGroupVO();
                appServiceGroupVO.setAppServiceList(ConvertUtils.convertList(list, this::dtoToGroupInfoVO));
                appServiceGroupList.add(appServiceGroupVO);
                break;
            }
            case SHARE_SERVICE: {
                Long organizationId = baseServiceClientOperator.queryIamProjectById(projectId).getOrganizationId();
                List<Long> appServiceIds = new ArrayList<>();
                baseServiceClientOperator.listIamProjectByOrgId(organizationId)
                        .forEach(pro ->
                                baseListByProjectId(pro.getId()).forEach(appServiceDTO -> appServiceIds.add(appServiceDTO.getId()))
                        );
                list.addAll(appServiceMapper.listShareApplicationService(appServiceIds, projectId, serviceType, params));
                Map<Long, List<AppServiceGroupInfoVO>> map = list.stream()
                        .map(this::dtoToGroupInfoVO)
                        .filter(v -> !projectId.equals(v.getId()))
                        .collect(Collectors.groupingBy(AppServiceGroupInfoVO::getProjectId));

                for (Map.Entry<Long, List<AppServiceGroupInfoVO>> entry : map.entrySet()) {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(entry.getKey());
                    AppServiceGroupVO appServiceGroupVO = new AppServiceGroupVO();
                    appServiceGroupVO.setName(projectDTO.getName());
                    appServiceGroupVO.setCode(projectDTO.getCode());
                    appServiceGroupVO.setId(projectDTO.getId());
                    appServiceGroupVO.setAppServiceList(entry.getValue());
                    appServiceGroupList.add(appServiceGroupVO);
                }
                break;
            }
            default: {
                throw new CommonException("error.list.deploy.app.service.type");
            }
        }
        return appServiceGroupList;
    }

    private List<AppServiceDTO> baseListAll(Long projectId) {
        return appServiceMapper.listAll(projectId);
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
            throw new CommonException("error.repository.token.invalid");
        } else if (validationResult == null) {
            throw new CommonException("error.repository.empty");
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
            params.put(oldGroupName, newGroupName);
            params.put(oldServiceCode, newServiceCode);
            FileUtil.replaceReturnFile(file, params);
        } catch (Exception e) {
            //删除模板
            gitUtil.deleteWorkingDirectory(applicationDir);
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public String checkAppServiceType(Long projectId, AppServiceDTO appServiceDTO) {
        String type = null;
        if (!appServiceDTO.getProjectId().equals(projectId)) {
            type = AppServiceType.SHARE_SERVICE.getType();
        } else if (appServiceDTO.getProjectId().equals(projectId)) {
            type = AppServiceType.NORMAL_SERVICE.getType();
        }
        return type;
    }


    @Override
    public String getToken(Integer gitlabProjectId, String applicationDir, UserAttrDTO userAttrDTO) {
        String accessToken = userAttrDTO.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitlabServiceClientOperator.createProjectToken(gitlabProjectId,
                    applicationDir, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
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
            rep.setRepoUrl(concatRepoUrl(org.getTenantNum(), project.getCode(), rep.getCode()));
            rep.setSshRepositoryUrl(GitUtil.getAppServiceSshUrl(gitlabSshUrl, org.getTenantNum(), project.getCode(), rep.getCode()));
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
            return ConvertUtils.convertPage(basePageByOptions(projectId, null, null, null, null, doPage, pageable, null, false), AppServiceVO.class);
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
    public List<ProjectVO> listProjectByShare(Long projectId, Boolean share) {
        Page<AppServiceGroupInfoVO> appServiceGroupInfoVOPageInfo = pageAppServiceByMode(projectId, Boolean.TRUE, null, null, new PageRequest(0, 0));
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

    private Boolean checkEnableCreateAppSvcWithSize(Long projectId, int appSize) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId, false, false, false);
        if (baseServiceClientOperator.checkOrganizationIsRegistered(projectDTO.getOrganizationId())) {
            ResourceLimitVO resourceLimitVO = baseServiceClientOperator.queryResourceLimit();
            AppServiceDTO example = new AppServiceDTO();
            example.setProjectId(projectId);
            int num = appServiceMapper.selectCount(example);
            return num + appSize <= resourceLimitVO.getAppSvcMaxNumber();
        }
        return true;
    }

    @Override
    public Boolean checkEnableCreateAppSvc(Long projectId) {
        return checkEnableCreateAppSvcWithSize(projectId, 1);
    }

    @Override
    public List<AppServiceSimpleVO> pageAppServiceToCreateCiPipeline(Long projectId, PageRequest pageRequest, @Nullable String params) {
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
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
            Set<Long> appServiceIds = getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new ArrayList<>();
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
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
        Tenant tenant = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
        return String.format(GitOpsConstants.REPO_URL_TEMPLATE_WITHOUT_SUFFIX, repoUrl, tenant.getTenantNum(), projectDTO.getCode(), appServiceDTO.getCode());
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
    private String getApplicationToken(String token, Integer projectId, Integer userId) {
        List<CiVariableVO> variables = gitlabServiceClientOperator.listAppServiceVariable(projectId, userId);
        if (variables.isEmpty()) {
            gitlabServiceClientOperator.createProjectVariable(projectId, "Token", token, false, userId);
            return token;
        } else {
            return variables.get(0).getValue();
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
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook";
        projectHookDTO.setUrl(uri);
        List<ProjectHookDTO> projectHookDTOS = gitlabServiceClientOperator.listProjectHook(projectId, userId);
        if (projectHookDTOS.isEmpty()) {
            appServiceDTO.setHookId(TypeUtil.objToLong(gitlabServiceClientOperator.createWebHook(
                    projectId, userId, projectHookDTO)
                    .getId()));
        } else {
            appServiceDTO.setHookId(TypeUtil.objToLong(projectHookDTOS.get(0).getId()));
        }
    }

    @Override
    public Set<Long> getMemberAppServiceIds(Long organizationId, Long projectId, Long userId) {
        List<RepositoryPrivilegeViewDTO> viewDTOList = hrdsCodeRepoClient.listRepositoriesByPrivilege(organizationId, projectId, Collections.singleton(userId)).getBody();
        if (CollectionUtils.isEmpty(viewDTOList)) {
            return null;
        }
        return viewDTOList.get(0).getAppServiceIds();
    }

    @Override
    public void baseCheckApp(Long projectId, Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (appServiceDTO == null || !projectId.equals(appServiceDTO.getProjectId())) {
            throw new CommonException("error.app.project.notMatch");
        }
    }

    private void initApplicationParams(ProjectDTO projectDTO, Tenant organizationDTO, List<AppServiceDTO> applicationDTOS, String urlSlash) {
        for (AppServiceDTO t : applicationDTOS) {
            if (t.getGitlabProjectId() != null) {
                t.setSshRepositoryUrl(GitUtil.getAppServiceSshUrl(gitlabSshUrl, organizationDTO.getTenantNum(), projectDTO.getCode(), t.getCode()));
                t.setRepoUrl(
                        gitlabUrl + urlSlash + organizationDTO.getTenantNum() + "-" + projectDTO.getCode() + "/"
                                + t.getCode() + ".git");
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
        }
        if (updateUser != null) {
            appServiceRepVO.setUpdateUserName(updateUser.getRealName());
            appServiceRepVO.setUpdateLoginName(updateUser.getLoginName());
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
    public void fixAppServiceVersion() {
        devopsTask.fixAppServiceVersion(null);
    }

    @Override
    public SonarContentsVO getSonarContentFromCache(Long projectId, Long appServiceId) {
        String jsonBody = redisTemplate.opsForValue().get(SONAR + ":" + projectId + ":" + appServiceId);
        if (StringUtils.isEmpty(jsonBody)) {
            return getSonarContent(projectId, appServiceId);
        } else {
            return JsonHelper.unmarshalByJackson(jsonBody, SonarContentsVO.class);
        }
    }
}
