package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.GitOpsConstants.DEFAULT_PIPELINE_RECORD_SIZE;
import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.PipelineConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.BranchDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.maven.Repository;
import io.choerodon.devops.infra.dto.maven.RepositoryPolicy;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.sonar.CiSonarConfigType;
import io.choerodon.devops.infra.enums.sonar.SonarAuthType;
import io.choerodon.devops.infra.enums.sonar.SonarScannerType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/2 18:00
 */
@Service
public class DevopsCiPipelineServiceImpl implements DevopsCiPipelineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCiPipelineServiceImpl.class);

    private static final String CREATE_PIPELINE_FAILED = "create.pipeline.failed";
    private static final String UPDATE_PIPELINE_FAILED = "update.pipeline.failed";
    private static final String DISABLE_PIPELINE_FAILED = "disable.pipeline.failed";
    private static final String ENABLE_PIPELINE_FAILED = "enable.pipeline.failed";
    private static final String DELETE_PIPELINE_FAILED = "delete.pipeline.failed";
    private static final String QUERY_PIPELINE_FAILED = "error.pipeline.query";
    private static final String ERROR_USER_HAVE_NO_APP_PERMISSION = "error.user.have.no.app.permission";
    private static final String ERROR_APP_SVC_ID_IS_NULL = "error.app.svc.id.is.null";
    private static final String ERROR_PROJECT_ID_IS_NULL = "error.project.id.is.null";
    private static final String ERROR_CI_MAVEN_REPOSITORY_TYPE = "error.ci.maven.repository.type";
    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "error.maven.settings.insert";
    private static final String ERROR_UNSUPPORTED_STEP_TYPE = "error.unsupported.step.type";
    private static final String ERROR_BRANCH_PERMISSION_MISMATCH = "error.branch.permission.mismatch";

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private ObjectMapper objectMapper = new ObjectMapper();

    private final DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
    private final DevopsCiJobMapper devopsCiJobMapper;
    private final DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    private final DevopsCiStageService devopsCiStageService;
    private final DevopsCiJobService devopsCiJobService;
    private final DevopsCiContentService devopsCiContentService;
    private final GitlabServiceClientOperator gitlabServiceClientOperator;
    private final UserAttrService userAttrService;
    private final AppServiceService appServiceService;
    private final DevopsCiJobRecordService devopsCiJobRecordService;
    private final DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;
    private final DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;
    private final DevopsProjectService devopsProjectService;
    private final BaseServiceClientOperator baseServiceClientOperator;
    private final RdupmClientOperator rdupmClientOperator;
    private final CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    private final DevopsConfigService devopsConfigService;
    private final PermissionHelper permissionHelper;
    private final AppServiceMapper appServiceMapper;
    private final CiCdPipelineMapper ciCdPipelineMapper;
    private final DevopsCdStageService devopsCdStageService;
    private final DevopsCdAuditService devopsCdAuditService;
    private final PipelineAppDeployService pipelineAppDeployService;
    private final DevopsCdJobService devopsCdJobService;
    private final DevopsCdPipelineRecordService devopsCdPipelineRecordService;
    private final DevopsCdJobRecordService devopsCDJobRecordService;
    private final DevopsCdStageRecordService devopsCdStageRecordService;
    private final DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
    private final DevopsEnvironmentMapper devopsEnvironmentMapper;
    private final DevopsPipelineRecordRelService devopsPipelineRecordRelService;
    private final DevopsCdPipelineService devopsCdPipelineService;
    private final DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper;
    private final DevopsDeployValueMapper devopsDeployValueMapper;

    public DevopsCiPipelineServiceImpl(
            @Lazy DevopsCiCdPipelineMapper devopsCiCdPipelineMapper,
            // 这里的懒加载是为了避免循环依赖
            @Lazy DevopsCiPipelineRecordService devopsCiPipelineRecordService,
            DevopsCiStageService devopsCiStageService,
            @Lazy DevopsCiJobMapper devopsCiJobMapper,
            @Lazy DevopsCiJobService devopsCiJobService,
            DevopsCiContentService devopsCiContentService,
            @Lazy GitlabServiceClientOperator gitlabServiceClientOperator,
            UserAttrService userAttrService,
            CheckGitlabAccessLevelService checkGitlabAccessLevelService,
            @Lazy AppServiceService appServiceService,
            DevopsCiJobRecordService devopsCiJobRecordService,
            DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper,
            DevopsProjectService devopsProjectService,
            BaseServiceClientOperator baseServiceClientOperator,
            RdupmClientOperator rdupmClientOperator,
            DevopsConfigService devopsConfigService,
            PermissionHelper permissionHelper,
            AppServiceMapper appServiceMapper,
            DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper,
            CiCdPipelineMapper ciCdPipelineMapper,
            DevopsCdStageService devopsCdStageService,
            DevopsCdAuditService devopsCdAuditService,
            PipelineAppDeployService pipelineAppDeployService,
            DevopsCdJobService devopsCdJobService,
            DevopsCdPipelineRecordService devopsCdPipelineRecordService,
            DevopsCdJobRecordService devopsCDJobRecordService,
            DevopsCdStageRecordService devopsCdStageRecordService,
            DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService,
            DevopsEnvironmentMapper devopsEnvironmentMapper,
            @Lazy DevopsPipelineRecordRelService devopsPipelineRecordRelService,
            @Lazy DevopsCdPipelineService devopsCdPipelineService,
            DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper,
            DevopsDeployValueMapper devopsDeployValueMapper
    ) {
        this.devopsCiCdPipelineMapper = devopsCiCdPipelineMapper;
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
        this.devopsCiStageService = devopsCiStageService;
        this.devopsCiJobService = devopsCiJobService;
        this.devopsCiContentService = devopsCiContentService;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.userAttrService = userAttrService;
        this.appServiceService = appServiceService;
        this.devopsCiJobRecordService = devopsCiJobRecordService;
        this.devopsCiMavenSettingsMapper = devopsCiMavenSettingsMapper;
        this.devopsCiPipelineRecordMapper = devopsCiPipelineRecordMapper;
        this.baseServiceClientOperator = baseServiceClientOperator;
        this.devopsProjectService = devopsProjectService;
        this.rdupmClientOperator = rdupmClientOperator;
        this.devopsConfigService = devopsConfigService;
        this.checkGitlabAccessLevelService = checkGitlabAccessLevelService;
        this.permissionHelper = permissionHelper;
        this.appServiceMapper = appServiceMapper;
        this.ciCdPipelineMapper = ciCdPipelineMapper;
        this.devopsCdStageService = devopsCdStageService;
        this.devopsCdAuditService = devopsCdAuditService;
        this.pipelineAppDeployService = pipelineAppDeployService;
        this.devopsCdJobService = devopsCdJobService;
        this.devopsCdPipelineRecordService = devopsCdPipelineRecordService;
        this.devopsCDJobRecordService = devopsCDJobRecordService;
        this.devopsCdStageRecordService = devopsCdStageRecordService;
        this.devopsCdEnvDeployInfoService = devopsCdEnvDeployInfoService;
        this.devopsEnvironmentMapper = devopsEnvironmentMapper;
        this.devopsPipelineRecordRelService = devopsPipelineRecordRelService;
        this.devopsCdPipelineService = devopsCdPipelineService;
        this.devopsPipelineRecordRelMapper = devopsPipelineRecordRelMapper;
        this.devopsDeployValueMapper = devopsDeployValueMapper;
        this.devopsCiJobMapper = devopsCiJobMapper;
    }

    private static String buildSettings(List<MavenRepoVO> mavenRepoList) {
        List<Server> servers = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();

        mavenRepoList.forEach(m -> {
            String[] types = Objects.requireNonNull(m.getType()).split(GitOpsConstants.COMMA);
            if (types.length > 2) {
                throw new CommonException(ERROR_CI_MAVEN_REPOSITORY_TYPE, m.getType());
            }
            if (Boolean.TRUE.equals(m.getPrivateRepo())) {
                servers.add(new Server(Objects.requireNonNull(m.getName()), Objects.requireNonNull(m.getUsername()), Objects.requireNonNull(m.getPassword())));
            }
            repositories.add(new Repository(
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getUrl()),
                    new RepositoryPolicy(m.getType().contains(GitOpsConstants.RELEASE)),
                    new RepositoryPolicy(m.getType().contains(GitOpsConstants.SNAPSHOT))));
        });
        return MavenSettingsUtil.generateMavenSettings(servers, repositories);
    }

    /**
     * 第一次创建CI流水线时初始化仓库下的.gitlab-ci.yml文件
     *
     * @param gitlabProjectId  gitlab项目id
     * @param ciFileIncludeUrl include中的链接
     */
    private void initGitlabCiFile(Integer gitlabProjectId, String ciFileIncludeUrl) {
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, GitOpsConstants.MASTER, GitOpsConstants.GITLAB_CI_FILE_NAME);

        if (repositoryFile == null) {
            // 说明项目下还没有CI文件
            // 创建文件
            try {
                LOGGER.info("initGitlabCiFile: create .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.createFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        buildIncludeYaml(ciFileIncludeUrl),
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId(),
                        GitOpsConstants.MASTER);
            } catch (Exception ex) {
                throw new CommonException("error.create.or.update.gitlab.ci", ex);
            }

        } else {
            // 将原先的配置文件内容注释并放在原本文件中
            String originFileContent = new String(Base64.getDecoder().decode(repositoryFile.getContent().getBytes()), StandardCharsets.UTF_8);
            // 注释后的内容
            String commentedLines = GitlabCiUtil.commentLines(originFileContent);
            try {
                // 更新文件
                LOGGER.info("initGitlabCiFile: update .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.updateFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        buildIncludeYaml(ciFileIncludeUrl) + GitOpsConstants.NEW_LINE + commentedLines,
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId());
            } catch (Exception ex) {
                throw new CommonException("error.create.or.update.gitlab.ci", ex);
            }
        }
    }

    @Override
    @Transactional
    public CiCdPipelineDTO create(Long projectId, CiCdPipelineVO ciCdPipelineVO) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineVO.getAppServiceId(), AppServiceEvent.CI_PIPELINE_CREATE);
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineVO.getAppServiceId());
        ciCdPipelineVO.setProjectId(projectId);
        checkNonCiPipelineBefore(ciCdPipelineVO.getAppServiceId());

        // 设置默认镜像
        if (StringUtils.isEmpty(ciCdPipelineVO.getImage())) {
            ciCdPipelineVO.setImage(defaultCiImage);
        }
        // 创建CICD流水线
        CiCdPipelineDTO ciCdPipelineDTO = ConvertUtils.convertObject(ciCdPipelineVO, CiCdPipelineDTO.class);
        ciCdPipelineDTO.setToken(GenerateUUID.generateUUID());
        if (ciCdPipelineMapper.insertSelective(ciCdPipelineDTO) != 1) {
            throw new CommonException(CREATE_PIPELINE_FAILED);
        }

        // 1.保存ci stage信息
        saveCiPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        // 2.保存cd stage信息
        saveCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        return ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineDTO.getId());
    }

    private void saveCdPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        //2.保存cd stage 的信息
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCdStageVOS())) {
            ciCdPipelineVO.getDevopsCdStageVOS().forEach(devopsCdStageVO -> {
                DevopsCdStageDTO devopsCdStageDTO = ConvertUtils.convertObject(devopsCdStageVO, DevopsCdStageDTO.class);
                devopsCdStageDTO.setPipelineId(ciCdPipelineDTO.getId());
                devopsCdStageDTO.setProjectId(projectId);
                devopsCdStageService.create(devopsCdStageDTO);
                // 保存cd job信息
                if (!CollectionUtils.isEmpty(devopsCdStageVO.getJobList())) {
                    devopsCdStageVO.getJobList().forEach(devopsCdJobVO -> {
                        // 添加人工卡点的任务类型时才 保存审核人员信息
                        createCdJob(devopsCdJobVO, projectId, devopsCdStageDTO.getId(), ciCdPipelineDTO.getId());
                    });
                }
            });
        }
    }

    private void saveCiPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                devopsCiStageDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存ci job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        // 不让数据库存加密的值
                        decryptCiBuildMetadata(devopsCiJobVO);
                        processCiJobVO(devopsCiJobVO);
                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                        devopsCiJobVO.setId(devopsCiJobService.create(devopsCiJobDTO).getId());
                    });
                }
            });
            // 保存ci配置文件
            saveCiContent(projectId, ciCdPipelineDTO.getId(), ciCdPipelineVO);

            AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());
            String ciFileIncludeUrl = String.format(GitOpsConstants.CI_CONTENT_URL_TEMPLATE, gatewayUrl, projectId, ciCdPipelineDTO.getToken());
            initGitlabCiFile(appServiceDTO.getGitlabProjectId(), ciFileIncludeUrl);
        }
    }

    private void getDockerTagName(DevopsCiJobVO devopsCiJobVO, List<CiDockerTagNameVO> dockerTagNames) {
        // 自定义的chart和docker tag名
        // 0.1 获取到docker任务Id 和规则
        if (devopsCiJobVO.getType().equals(JobTypeEnum.BUILD.value())) {
            if (devopsCiJobVO.getConfigJobTypes().contains(CiJobScriptTypeEnum.DOCKER.getType())) {
                List<CiConfigTemplateVO> configVOS = devopsCiJobVO.getConfigVO().getConfig();
                Optional<CiConfigTemplateVO> optional = configVOS.stream().filter(t -> t.getType().equals(CiJobScriptTypeEnum.DOCKER.getType())).findFirst();
                if (optional.isPresent()) {
                    CiConfigTemplateVO templateVO = optional.get();
                    if (templateVO.getCustomDockerTagName() != null && templateVO.getCustomDockerTagName()) {
                        if (StringUtils.isEmpty(templateVO.getDockerTagName())) {
                            throw new CommonException("error.docker.tag.name.empty");
                        }
                        CiDockerTagNameVO ciDockerTagNameVO = new CiDockerTagNameVO(devopsCiJobVO.getId(), devopsCiJobVO.getName(), templateVO.getDockerTagName());
                        dockerTagNames.add(ciDockerTagNameVO);
                    }
                }
            }
        }
    }

    private void setChartVersionName(DevopsCiJobVO devopsCiJobVO, List<CiDockerTagNameVO> dockerTagNames) {
        // chart任务 自定chart版本名
        // 前端可能不会拿到最新的chartVersionName
        // 更新metadata信息
        if (devopsCiJobVO.getType().equals(JobTypeEnum.CHART.value())) {
            List<CiConfigTemplateVO> configVOS = devopsCiJobVO.getConfigVO().getConfig();
            if (!CollectionUtils.isEmpty(configVOS)) {
                CiConfigTemplateVO ciConfigTemplateVO = configVOS.get(0);
                if (ciConfigTemplateVO.getCustomChartVersionName() != null && ciConfigTemplateVO.getCustomChartVersionName()) {
                    if (StringUtils.isEmpty(ciConfigTemplateVO.getDockerJobName()) && ciConfigTemplateVO.getDockerJobId() == null) {
                        throw new CommonException("error.chart.docker.job.empty");
                    }
                    CiDockerTagNameVO ciDockerTagNameVO = null;
                    // 创建根据名称判断 更新根据id
                    if (devopsCiJobVO.getId() != null) {
                        for (CiDockerTagNameVO t : dockerTagNames) {
                            if (t.getDockerJobId().equals(devopsCiJobVO.getId())) {
                                ciDockerTagNameVO = t;
                                break;
                            }
                        }
                    } else {
                        for (CiDockerTagNameVO t : dockerTagNames) {
                            if (t.getDockerJobName().equals(devopsCiJobVO.getName())) {
                                ciDockerTagNameVO = t;
                                break;
                            }
                        }
                    }

                    if (ciDockerTagNameVO == null) {
                        // 找不到docker任务 chart自定义名称设置为默认
                        ciConfigTemplateVO.setCustomChartVersionName(false);
                    } else {
                        ciConfigTemplateVO.setDockerJobId(ciDockerTagNameVO.getDockerJobId());
                        ciConfigTemplateVO.setDockerJobName(ciDockerTagNameVO.getDockerJobName());
                        ciConfigTemplateVO.setChartVersionName(ciDockerTagNameVO.getDockerTagName());
                    }

                    configVOS.clear();
                    configVOS.add(ciConfigTemplateVO);
                    CiConfigVO ciConfigVO = new CiConfigVO(configVOS);
                    devopsCiJobVO.setConfigVO(ciConfigVO);
                    String metadata = gson.toJson(ciConfigVO);
                    devopsCiJobVO.setMetadata(metadata.replace("\"", "'"));
                }
            }
        }
    }

    @Override
    public CiCdPipelineVO query(Long projectId, Long pipelineId) {
        // 根据pipeline_id查询数据
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        CommonExAssertUtil.assertTrue(ciCdPipelineDTO != null, "error.pipeline.not.exist", pipelineId);
        CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(ciCdPipelineDTO, CiCdPipelineVO.class);
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineVO.getAppServiceId());
        if (!Objects.isNull(appServiceDTO)) {
            ciCdPipelineVO.setAppServiceCode(appServiceDTO.getCode());
            ciCdPipelineVO.setAppServiceType(appServiceDTO.getType());
        }
        //查询CI相关的阶段以及JOB
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(pipelineId);
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(pipelineId);

        List<DevopsCiStageVO> devopsCiStageVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageVO.class);
        List<DevopsCiJobVO> devopsCiJobVOS = ConvertUtils.convertList(devopsCiJobDTOS, DevopsCiJobVO.class);
        devopsCiJobVOS.forEach(this::processBeforeQueryJob);

        // 封装CI对象
        devopsCiJobVOS.forEach(devopsCiJobVO -> {
            if (JobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
                CiConfigVO ciConfigVO = gson.fromJson(devopsCiJobVO.getMetadata(), CiConfigVO.class);
                devopsCiJobVO.setConfigJobTypes(ciConfigVO.getConfig().stream().map(io.choerodon.devops.api.vo.CiConfigTemplateVO::getType).collect(Collectors.toList()));
            }
        });
        Map<Long, List<DevopsCiJobVO>> ciJobMap = devopsCiJobVOS.stream().collect(Collectors.groupingBy(DevopsCiJobVO::getCiStageId));
        devopsCiStageVOS.forEach(devopsCiStageVO -> {
            List<DevopsCiJobVO> ciJobVOS = ciJobMap.getOrDefault(devopsCiStageVO.getId(), Collections.emptyList());
            ciJobVOS = ciJobVOS.stream().peek(job -> {
                if (JobTypeEnum.BUILD.value().equals(job.getType())) {
                    // 将json string中字段进行加密
                    CiConfigVO ciConfigVO = JsonHelper.unmarshalByJackson(job.getMetadata(), CiConfigVO.class);
                    // 返回给前端要用单引号而不是双引号的字符串
                    job.setMetadata(JsonHelper.singleQuoteWrapped(KeyDecryptHelper.encryptJson(ciConfigVO)));
                }
            }).sorted(Comparator.comparingLong(DevopsCiJobVO::getId)).collect(Collectors.toList());
            devopsCiStageVO.setJobList(ciJobVOS);
        });
        // ci stage排序
        devopsCiStageVOS = devopsCiStageVOS.stream().sorted(Comparator.comparing(DevopsCiStageVO::getSequence)).collect(Collectors.toList());
        for (DevopsCiStageVO devopsCiStageVO : devopsCiStageVOS) {
            devopsCiStageVO.setType(StageType.CI.getType());
        }
        ciCdPipelineVO.setDevopsCiStageVOS(devopsCiStageVOS);

        //查询CD相关的阶段以及JOB
        List<DevopsCdStageDTO> devopsCdStageDTOS = devopsCdStageService.queryByPipelineId(pipelineId);
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobService.listByPipelineId(pipelineId);
        List<DevopsCdStageVO> devopsCdStageVOS = ConvertUtils.convertList(devopsCdStageDTOS, DevopsCdStageVO.class);
        List<DevopsCdJobVO> devopsCdJobVOS = ConvertUtils.convertList(devopsCdJobDTOS, DevopsCdJobVO.class);
        //给cd的job加上环境名称
        if (!CollectionUtils.isEmpty(devopsCdJobVOS)) {
            for (DevopsCdJobVO devopsCdJobVO : devopsCdJobVOS) {
                //如果是自动部署添加环境名字
                if (JobTypeEnum.CD_DEPLOY.value().equals(devopsCdJobVO.getType())) {
                    Long deployInfoId = devopsCdJobVO.getDeployInfoId();
                    DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(deployInfoId);
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getEnvId());
                    if (!Objects.isNull(devopsEnvironmentDTO)) {
                        devopsCdJobVO.setEnvName(devopsEnvironmentDTO.getName());
                    }
                    DevopsCdEnvDeployInfoVO devopsCdEnvDeployInfoVO = ConvertUtils.convertObject(devopsCdEnvDeployInfoDTO, DevopsCdEnvDeployInfoVO.class);
                    //根据value id 返回values
                    DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueMapper.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getValueId());
                    devopsCdEnvDeployInfoVO.setValue(Base64Util.getBase64EncodedString(devopsDeployValueDTO.getValue()));
                    // 加密json中主键
                    devopsCdJobVO.setMetadata(JsonHelper.singleQuoteWrapped(KeyDecryptHelper.encryptJson(devopsCdEnvDeployInfoVO)));
                } else if (JobTypeEnum.CD_HOST.value().equals(devopsCdJobVO.getType())) {
                    // 加密json中主键
                    CdHostDeployConfigVO cdHostDeployConfigVO = JsonHelper.unmarshalByJackson(devopsCdJobVO.getMetadata(), CdHostDeployConfigVO.class);
                    devopsCdJobVO.setMetadata(JsonHelper.singleQuoteWrapped(KeyDecryptHelper.encryptJson(cdHostDeployConfigVO)));
                } else if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobVO.getType())) {
                    //如果是人工审核，返回审核人员信息
                    List<Long> longs = devopsCdAuditService.baseListByOptions(null, null, devopsCdJobVO.getId()).stream().map(DevopsCdAuditDTO::getUserId).collect(Collectors.toList());
                    List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(longs);
                    devopsCdJobVO.setIamUserDTOS(iamUserDTOS);
                    devopsCdJobVO.setCdAuditUserIds(longs);
                }
            }
        }
        // 封装CD对象
        Map<Long, List<DevopsCdJobVO>> cdJobMap = devopsCdJobVOS.stream().collect(Collectors.groupingBy(DevopsCdJobVO::getStageId));
        devopsCdStageVOS.forEach(devopsCdStageVO -> {
            List<DevopsCdJobVO> jobMapOrDefault = cdJobMap.getOrDefault(devopsCdStageVO.getId(), Collections.emptyList());
            jobMapOrDefault.sort(Comparator.comparing(DevopsCdJobVO::getId));
            devopsCdStageVO.setJobList(jobMapOrDefault);
        });
        // cd stage排序
        devopsCdStageVOS = devopsCdStageVOS.stream().sorted(Comparator.comparing(DevopsCdStageVO::getSequence)).collect(Collectors.toList());
        ciCdPipelineVO.setDevopsCdStageVOS(devopsCdStageVOS);
        return ciCdPipelineVO;
    }

    private void processBeforeQueryJob(DevopsCiJobVO devopsCiJobVO) {
        if (JobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
            // 反序列化
            CiConfigVO ciConfigVO = JSONObject.parseObject(devopsCiJobVO.getMetadata(), CiConfigVO.class);
            if (!CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
                // 将script字段加密
                ciConfigVO.getConfig().stream().filter(e -> !Objects.isNull(e.getScript())).forEach(c -> c.setScript(Base64Util.getBase64EncodedString(c.getScript())));
                // 序列化
                devopsCiJobVO.setMetadata(JsonHelper.singleQuoteWrapped(JSONObject.toJSONString(ciConfigVO)));
            }
        } else if (JobTypeEnum.CUSTOM.value().equals(devopsCiJobVO.getType())) {
            // 加密自定义任务的元数据
            devopsCiJobVO.setMetadata(Base64Util.getBase64EncodedString(devopsCiJobVO.getMetadata()));
        }
    }

    @Override
    public CiCdPipelineDTO queryByAppSvcId(Long id) {
        if (id == null) {
            throw new CommonException(ERROR_APP_SVC_ID_IS_NULL);
        }
        CiCdPipelineDTO ciCdPipelineDTO = new CiCdPipelineDTO();
        ciCdPipelineDTO.setAppServiceId(id);
        return ciCdPipelineMapper.selectOne(ciCdPipelineDTO);
    }

    @Override
    public List<CiCdPipelineVO> listByProjectIdAndAppName(Long projectId, String name) {
        if (projectId == null) {
            throw new CommonException(ERROR_PROJECT_ID_IS_NULL);
        }
        // 应用有权限的应用服务
        Long userId = DetailsHelper.getUserDetails().getUserId();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        boolean projectOwner = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        Set<Long> appServiceIds;
        if (projectOwner) {
            appServiceIds = appServiceMapper.listByActive(projectId).stream().map(AppServiceDTO::getId).collect(Collectors.toSet());
        } else {
            appServiceIds = appServiceService.getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new ArrayList<>();
            }
        }
        // 查询流水线
        List<CiCdPipelineVO> ciCdPipelineVOS = ciCdPipelineMapper.queryByProjectIdAndName(projectId, appServiceIds, name);
        // 封装流水线记录
        PageRequest cicdPipelineRel = new PageRequest(GitOpsConstants.FIRST_PAGE_INDEX, DEFAULT_PIPELINE_RECORD_SIZE, new Sort(new Sort.Order(Sort.Direction.DESC, DevopsPipelineRecordRelDTO.FIELD_ID)));
        //每条流水线默认展示5条记录
        ciCdPipelineVOS.forEach(ciCdPipelineVO -> {
            List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS = new ArrayList<>();
            // 查询cicd关系表
            Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOPage = devopsPipelineRecordRelService.pagingPipelineRel(ciCdPipelineVO.getId(), cicdPipelineRel);
            if (!Objects.isNull(devopsPipelineRecordRelDTOPage)) {
                ciCdPipelineVO.setHasMoreRecords(devopsPipelineRecordRelDTOPage.getTotalElements() > DEFAULT_PIPELINE_RECORD_SIZE);
            }

            if (!Objects.isNull(devopsPipelineRecordRelDTOPage) && !CollectionUtils.isEmpty(devopsPipelineRecordRelDTOPage.getContent())) {
                devopsPipelineRecordRelDTOPage.getContent().forEach(devopsPipelineRecordRelDTO -> {
                    CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
                    //查询ci记录
                    DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryByCiPipelineRecordId(devopsPipelineRecordRelDTO.getCiPipelineRecordId());
                    //查询cd记录
                    DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryByCdPipelineRecordId(devopsPipelineRecordRelDTO.getCdPipelineRecordId());
                    //cicd
                    if (devopsCiPipelineRecordVO != null && devopsCdPipelineRecordVO != null) {
                        List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                        ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(devopsPipelineRecordRelDTO.getId());
                        ciCdPipelineRecordVO.setCiStatus(devopsCiPipelineRecordVO.getStatus());
                        ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
                        ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
                        ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
                        stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());

                        ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
                        ciCdPipelineRecordVO.setCdStatus(devopsCdPipelineRecordVO.getStatus());
                        stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
                        ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());

                        //计算ciCdPipelineRecordVO的状态
                        CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
                        ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
                        ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);

                        //cicd 如果是cicd并且此记录是第一条则跳过
                        if (!CollectionUtils.isEmpty(ciCdPipelineRecordVOS) && isFirstRecord(devopsPipelineRecordRelDTO)) {
                            CiCdPipelineUtils.recordListSort(ciCdPipelineRecordVOS);
                            ciCdPipelineRecordVOS.get(ciCdPipelineRecordVOS.size() - 1).setStageRecordVOS(null);
                        }
                    }
                    //纯ci
                    if (devopsCiPipelineRecordVO != null && devopsCdPipelineRecordVO == null) {
                        List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                        ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(devopsPipelineRecordRelDTO.getId());
                        //收集这条ci流水线的所有stage
                        ciCdPipelineRecordVO.setCiStatus(devopsCiPipelineRecordVO.getStatus());
                        ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
                        ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
                        ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());

                        stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());
                        ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
                        CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
                        ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
                    }
                    //纯cd
                    if (devopsCiPipelineRecordVO == null && devopsCdPipelineRecordVO != null) {
                        List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                        ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(devopsPipelineRecordRelDTO.getId());
                        ciCdPipelineRecordVO.setCdStatus(devopsCdPipelineRecordVO.getStatus());
                        ciCdPipelineRecordVO.setCreatedDate(devopsCdPipelineRecordVO.getCreatedDate());
                        ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
                        ciCdPipelineRecordVO.setGitlabPipelineId(Objects.isNull(devopsCdPipelineRecordVO.getGitlabPipelineId()) ? null : devopsCdPipelineRecordVO.getGitlabPipelineId());
                        ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
                        stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
                        ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
                        CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
                        ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
                    }
                });
            }
            ciCdPipelineVO.setCiCdPipelineRecordVOS(ciCdPipelineRecordVOS);
            //将piplineRecord记录排序
            CiCdPipelineUtils.recordListSort(ciCdPipelineVO.getCiCdPipelineRecordVOS());
            //计算流水线上一次执行的状态和时间
            if (ciCdPipelineVO.getCiCdPipelineRecordVOS().size() > 0) {
                CiCdPipelineRecordVO lastCiCdPipelineRecordVO = ciCdPipelineVO.getCiCdPipelineRecordVOS().get(0);
                ciCdPipelineVO.setLatestExecuteStatus(lastCiCdPipelineRecordVO.getStatus());
                ciCdPipelineVO.setLatestExecuteDate(lastCiCdPipelineRecordVO.getCreatedDate());
                //填充显示编号
                CiCdPipelineUtils.fillViewId(ciCdPipelineVO.getCiCdPipelineRecordVOS());
            }
        });
        return ciCdPipelineVOS;
    }

    private boolean isFirstRecord(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        DevopsPipelineRecordRelDTO recordRelDTO = new DevopsPipelineRecordRelDTO();
        recordRelDTO.setPipelineId(devopsPipelineRecordRelDTO.getPipelineId());
        List<DevopsPipelineRecordRelDTO> select = devopsPipelineRecordRelMapper.select(recordRelDTO);
        if (select.size() == 1) {
            return true;
        }
        List<DevopsPipelineRecordRelVO> devopsPipelineRecordRelVOS = ConvertUtils.convertList(select, this::relDtoToRelVO);
        CiCdPipelineUtils.recordListSort(devopsPipelineRecordRelVOS);
        return devopsPipelineRecordRelDTO.getId().compareTo(devopsPipelineRecordRelVOS.get(devopsPipelineRecordRelVOS.size() - 1).getId()) == 0 ? true : false;

    }

    private DevopsPipelineRecordRelVO relDtoToRelVO(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        DevopsPipelineRecordRelVO devopsPipelineRecordRelVO = new DevopsPipelineRecordRelVO();
        BeanUtils.copyProperties(devopsPipelineRecordRelDTO, devopsPipelineRecordRelVO);
        devopsPipelineRecordRelVO.setCreatedDate(devopsPipelineRecordRelDTO.getCreationDate());
        return devopsPipelineRecordRelVO;
    }

    /**
     * 校验应用服务之前并不存在流水线
     *
     * @param appServiceId 应用服务id
     */
    private void checkNonCiPipelineBefore(Long appServiceId) {
        if (countByAppServiceId(appServiceId) > 0) {
            throw new CommonException("error.ci.pipeline.exists.for.app.service");
        }
    }

    private int countByAppServiceId(Long appServiceId) {
        CiCdPipelineDTO devopsCiPipelineDTO = new CiCdPipelineDTO();
        devopsCiPipelineDTO.setAppServiceId(Objects.requireNonNull(appServiceId));
        return ciCdPipelineMapper.selectCount(devopsCiPipelineDTO);
    }

    @Override
    public CiCdPipelineVO queryById(Long ciPipelineId) {
        return devopsCiCdPipelineMapper.queryById(ciPipelineId);
    }

    @Override
    @Transactional
    public CiCdPipelineDTO disablePipeline(Long projectId, Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineDTO.getAppServiceId());
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_STATUS_UPDATE);
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        if (ciCdPipelineMapper.disablePipeline(pipelineId) != 1) {
            throw new CommonException(DISABLE_PIPELINE_FAILED);
        }
        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    @Override
    @Transactional
    public void deletePipeline(Long projectId, Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineDTO.getAppServiceId());
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_DELETE);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());
        // 删除流水线
        if (ciCdPipelineMapper.deleteByPrimaryKey(pipelineId) != 1) {
            throw new CommonException(DELETE_PIPELINE_FAILED);
        }
        // 删除stage
        devopsCiStageService.deleteByPipelineId(pipelineId);
        devopsCdStageService.deleteByPipelineId(pipelineId);

        // 删除job
        devopsCiJobService.deleteByPipelineId(pipelineId);
        devopsCdJobService.deleteByPipelineId(pipelineId);

        // 删除 ci job记录
        devopsCiJobRecordService.deleteByGitlabProjectId(appServiceDTO.getGitlabProjectId().longValue());

        // 删除pipeline记录
        devopsCiPipelineRecordService.deleteByGitlabProjectId(appServiceDTO.getGitlabProjectId().longValue());
        //删除 cd  pipeline记录 stage记录 以及Job记录
        devopsCdPipelineRecordService.deleteByPipelineId(pipelineId);

        // 删除content file
        devopsCiContentService.deleteByPipelineId(pipelineId);

        // 删除.gitlab-ci.yaml文件
        deleteGitlabCiFile(appServiceDTO.getGitlabProjectId());
    }

    @Override
    @Transactional
    public CiCdPipelineDTO enablePipeline(Long projectId, Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineDTO.getAppServiceId());
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_STATUS_UPDATE);
        if (ciCdPipelineMapper.enablePipeline(pipelineId) != 1) {
            throw new CommonException(ENABLE_PIPELINE_FAILED);
        }
        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    @Override
    public void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineDTO.getAppServiceId());
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_NEW_PERFORM);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        checkUserBranchPushPermission(projectId, userAttrDTO.getGitlabUserId(), gitlabProjectId, ref);
        //触发ci流水线
        Pipeline pipeline = gitlabServiceClientOperator.createPipeline(gitlabProjectId.intValue(), userAttrDTO.getGitlabUserId().intValue(), ref);
        // 保存执行记录
        try {
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.create(pipelineId, gitlabProjectId, pipeline);
            // 保存流水线记录关系
            DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = new DevopsPipelineRecordRelDTO();
            devopsPipelineRecordRelDTO.setPipelineId(ciCdPipelineDTO.getId());
            devopsPipelineRecordRelDTO.setCiPipelineRecordId(devopsCiPipelineRecordDTO.getId());
            devopsPipelineRecordRelDTO.setCdPipelineRecordId(PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID);
            devopsPipelineRecordRelService.save(devopsPipelineRecordRelDTO);
            // 初始化cd流水线记录
            devopsCdPipelineService.initPipelineRecordWithStageAndJob(projectId, pipeline.getId().longValue(), pipeline.getSha(), pipeline.getRef(), pipeline.getTag(), ciCdPipelineDTO);
            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId.intValue(), pipeline.getId(), userAttrDTO.getGitlabUserId().intValue());
            devopsCiJobRecordService.create(devopsCiPipelineRecordDTO.getId(), gitlabProjectId, jobDTOS, userAttrDTO.getIamUserId());
        } catch (Exception e) {
            LOGGER.info("save pipeline Records failed， ciPipelineId {}.", pipelineId);
        }
    }

    @Override
    public void checkUserBranchPushPermission(Long projectId, Long gitlabUserId, Long gitlabProjectId, String ref) {
        BranchDTO branchDTO = gitlabServiceClientOperator.getBranch(gitlabProjectId.intValue(), ref);
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        GitLabUserDTO gitLabUserDTO = gitlabServiceClientOperator.queryUserById(gitlabUserId.intValue());
        // 管理员跳过权限校验
        if (Boolean.TRUE.equals(gitLabUserDTO.getIsAdmin())) {
            return;
        }
        MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(devopsProjectDTO.getDevopsAppGroupId().intValue(), gitlabUserId.intValue());
        if (memberDTO == null || memberDTO.getId() == null) {
            memberDTO = gitlabServiceClientOperator.getMember(gitlabProjectId, gitlabUserId);
        }
        if (Boolean.TRUE.equals(branchDTO.getProtected())
                && Boolean.FALSE.equals(branchDTO.getDevelopersCanMerge())
                && Boolean.FALSE.equals(branchDTO.getDevelopersCanPush())
                && memberDTO.getAccessLevel() <= AccessLevel.DEVELOPER.toValue()) {
            throw new CommonException(ERROR_BRANCH_PERMISSION_MISMATCH, ref);
        }
    }

    @Override
    public int selectCountByAppServiceId(Long appServiceId) {
        CiCdPipelineDTO ciCdPipelineDTO = new CiCdPipelineDTO();
        ciCdPipelineDTO.setAppServiceId(Objects.requireNonNull(appServiceId));
        return ciCdPipelineMapper.selectCount(ciCdPipelineDTO);
    }

    private void deleteGitlabCiFile(Integer gitlabProjectId) {
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, GitOpsConstants.MASTER, GitOpsConstants.GITLAB_CI_FILE_NAME);
        if (repositoryFile != null) {
            try {
                LOGGER.info("deleteGitlabCiFile: delete .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.deleteFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId());
            } catch (Exception e) {
                throw new CommonException("error.delete.gitlab-ci.file", e);
            }

        }
    }

    private static String buildIncludeYaml(String ciFileIncludeUrl) {
        GitlabCi gitlabCi = new GitlabCi();
        gitlabCi.setInclude(ciFileIncludeUrl);
        return GitlabCiUtil.gitlabCi2yaml(gitlabCi);
    }

    @Override
    @Transactional
    public CiCdPipelineDTO update(Long projectId, Long pipelineId, CiCdPipelineVO ciCdPipelineVO) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineVO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_UPDATE);
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineVO.getAppServiceId());
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineVO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        // 校验自定义任务格式
        CiCdPipelineDTO ciCdPipelineDTO = ConvertUtils.convertObject(ciCdPipelineVO, CiCdPipelineDTO.class);
        ciCdPipelineDTO.setId(pipelineId);
        ciCdPipelineMapper.updateByPrimaryKeySelective(ciCdPipelineDTO);
        //更新CI流水线
        updateCiPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        //更新CD流水线
        updateCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    private void updateCdPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        //cd stage 先删除，后新增
        List<DevopsCdStageDTO> cdStageDTOS = devopsCdStageService.queryByPipelineId(ciCdPipelineDTO.getId());
        cdStageDTOS.forEach(devopsCdStageDTO -> {
            //删除cd的阶段
            devopsCdStageService.deleteById(devopsCdStageDTO.getId());
            devopsCdJobService.deleteByStageId(devopsCdStageDTO.getId());
        });
        //新增
        saveCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
    }

    private void updateCiPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        // 更新stage
        // 查询数据库中原有stage列表,并和新的stage列表作比较。
        // 差集：要删除的记录
        // 交集：要更新的记录
        List<DevopsCiStageDTO> devopsCiStageDTOS = devopsCiStageService.listByPipelineId(ciCdPipelineDTO.getId());
        Set<Long> oldStageIds = devopsCiStageDTOS.stream().map(DevopsCiStageDTO::getId).collect(Collectors.toSet());

        Set<Long> updateIds = ciCdPipelineVO.getDevopsCiStageVOS().stream()
                .filter(devopsCiStageVO -> devopsCiStageVO.getId() != null)
                .map(DevopsCiStageVO::getId)
                .collect(Collectors.toSet());
        // 去掉要更新的记录，剩下的为要删除的记录
        oldStageIds.removeAll(updateIds);
        oldStageIds.forEach(stageId -> {
            devopsCiStageService.deleteById(stageId);
            devopsCiJobService.deleteByStageId(stageId);
        });

        ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
            if (devopsCiStageVO.getId() != null) {
                // 更新
                devopsCiStageService.update(devopsCiStageVO);
                devopsCiJobService.deleteByStageId(devopsCiStageVO.getId());
                // 保存job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        decryptCiBuildMetadata(devopsCiJobVO);
                        processCiJobVO(devopsCiJobVO);
                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setId(null);
                        devopsCiJobDTO.setCiStageId(devopsCiStageVO.getId());
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobService.create(devopsCiJobDTO);
                        devopsCiJobVO.setId(devopsCiJobDTO.getId());
                    });
                }
            } else {
                // 新增
                devopsCiStageVO.setCiPipelineId(ciCdPipelineDTO.getId());
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        decryptCiBuildMetadata(devopsCiJobVO);
                        processCiJobVO(devopsCiJobVO);

                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobService.create(devopsCiJobDTO);
                        devopsCiJobVO.setId(devopsCiJobDTO.getId());
                    });
                }
            }
        });
        saveCiContent(projectId, ciCdPipelineDTO.getId(), ciCdPipelineVO);
    }

    private void processCiJobVO(DevopsCiJobVO devopsCiJobVO) {
        // 不让数据库存加密的值
        if (JobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
            // 将构建类型的stage中的job的每个step进行解析和转化
            CiConfigVO ciConfigVO = JSONObject.parseObject(devopsCiJobVO.getMetadata(), CiConfigVO.class);
            if (!CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
                ciConfigVO.getConfig().forEach(c -> {
                    if (!org.springframework.util.StringUtils.isEmpty(c.getScript())) {
                        c.setScript(Base64Util.getBase64DecodedString(c.getScript()));
                    }
                });
            }
            devopsCiJobVO.setConfigVO(ciConfigVO);
            devopsCiJobVO.setMetadata(JSONObject.toJSONString(ciConfigVO));
        }
    }

    private void saveCiContent(final Long projectId, Long pipelineId, CiCdPipelineVO ciCdPipelineVO) {
        GitlabCi gitlabCi = buildGitLabCiObject(projectId, ciCdPipelineVO);
        StringBuilder gitlabCiYaml = new StringBuilder(GitlabCiUtil.gitlabCi2yaml(gitlabCi));

        // 拼接自定义job
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            List<DevopsCiJobVO> ciJobVOS = ciCdPipelineVO.getDevopsCiStageVOS().stream()
                    .flatMap(v -> v.getJobList().stream()).filter(job -> JobTypeEnum.CUSTOM.value().equalsIgnoreCase(job.getType()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ciJobVOS)) {
                for (DevopsCiJobVO job : ciJobVOS) {
                    gitlabCiYaml.append(GitOpsConstants.NEW_LINE).append(job.getMetadata());
                }
            }

        }

        //保存gitlab-ci配置文件
        DevopsCiContentDTO devopsCiContentDTO = new DevopsCiContentDTO();
        devopsCiContentDTO.setCiPipelineId(pipelineId);
        devopsCiContentDTO.setCiContentFile(gitlabCiYaml.toString());
        devopsCiContentService.create(devopsCiContentDTO);
    }

    /**
     * 构建gitlab-ci对象，用于转换为gitlab-ci.yaml
     *
     * @param projectId      项目id
     * @param ciCdPipelineVO 流水线数据
     * @return 构建完的CI文件对象
     */
    private GitlabCi buildGitLabCiObject(final Long projectId, CiCdPipelineVO ciCdPipelineVO) {
        // 对阶段排序
        List<String> stages = ciCdPipelineVO.getDevopsCiStageVOS().stream()
                .sorted(Comparator.comparing(DevopsCiStageVO::getSequence))
                .map(DevopsCiStageVO::getName)
                .collect(Collectors.toList());

        GitlabCi gitlabCi = new GitlabCi();

        // 如果用户指定了就使用用户指定的，如果没有指定就使用默认的猪齿鱼提供的镜像
        gitlabCi.setImage(StringUtils.isEmpty(ciCdPipelineVO.getImage()) ? defaultCiImage : ciCdPipelineVO.getImage());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        gitlabCi.setStages(stages);
        ciCdPipelineVO.getDevopsCiStageVOS().forEach(stageVO -> {
            if (!CollectionUtils.isEmpty(stageVO.getJobList())) {
                stageVO.getJobList().forEach(job -> {
                    if (JobTypeEnum.CUSTOM.value().equals(job.getType())) {
                        return;
                    }
                    CiJob ciJob = new CiJob();
                    if (!StringUtils.isEmpty(job.getImage())) {
                        ciJob.setImage(job.getImage());
                    }
                    ciJob.setStage(stageVO.getName());
                    ciJob.setScript(buildScript(Objects.requireNonNull(projectDTO.getOrganizationId()), projectId, job));
                    ciJob.setCache(buildJobCache(job));
                    processOnlyAndExcept(job, ciJob);
                    gitlabCi.addJob(job.getName(), ciJob);
                });
            }
        });
        buildBeforeScript(gitlabCi, ciCdPipelineVO.getVersionName());
        return gitlabCi;
    }

    /**
     * 处理job的触发方式
     *
     * @param metadata job元数据
     * @param ciJob    ci文件的job对象
     */
    private void processOnlyAndExcept(DevopsCiJobVO metadata, CiJob ciJob) {
        if (StringUtils.isNotBlank(metadata.getTriggerType())
                && StringUtils.isNotBlank(metadata.getTriggerValue())) {
            CiTriggerType ciTriggerType = CiTriggerType.forValue(metadata.getTriggerType());
            if (ciTriggerType != null) {
                String triggerValue = metadata.getTriggerValue();
                switch (ciTriggerType) {
                    case REFS:
                        GitlabCiUtil.processTriggerRefs(ciJob, triggerValue);
                        break;
                    case EXACT_MATCH:
                        GitlabCiUtil.processExactMatch(ciJob, triggerValue);
                        break;
                    case REGEX_MATCH:
                        GitlabCiUtil.processRegexMatch(ciJob, triggerValue);
                        break;
                    case EXACT_EXCLUDE:
                        GitlabCiUtil.processExactExclude(ciJob, triggerValue);
                        break;
                }
            }
        }
    }

    private static MavenRepoVO convertRepo(NexusMavenRepoDTO nexusMavenRepoDTO) {
        MavenRepoVO mavenRepoVO = new MavenRepoVO();
        mavenRepoVO.setName(nexusMavenRepoDTO.getName());
        mavenRepoVO.setPrivateRepo(Boolean.TRUE);
        if ("MIXED".equals(nexusMavenRepoDTO.getVersionPolicy())) {
            mavenRepoVO.setType(GitOpsConstants.SNAPSHOT + "," + GitOpsConstants.RELEASE);
        } else {
            mavenRepoVO.setType(nexusMavenRepoDTO.getVersionPolicy().toLowerCase());
        }
        mavenRepoVO.setUrl(nexusMavenRepoDTO.getUrl());
        mavenRepoVO.setUsername(nexusMavenRepoDTO.getNeUserId());
        mavenRepoVO.setPassword(nexusMavenRepoDTO.getNeUserPassword());
        return mavenRepoVO;
    }

    /**
     * 生成maven构建相关的脚本
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param ciConfigTemplateVO maven构建阶段的信息
     * @param hasSettings        这个阶段是否有配置settings
     * @return 生成的shell脚本
     */
    private List<String> buildMavenScripts(final Long projectId, final Long jobId, CiConfigTemplateVO ciConfigTemplateVO, boolean hasSettings) {
        List<String> shells = GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(ciConfigTemplateVO.getScript()), true, true);
        if (hasSettings) {
            // 插入shell指令将配置的settings文件下载到项目目录下
            shells.add(0, GitlabCiUtil.downloadMavenSettings(projectId, jobId, ciConfigTemplateVO.getSequence()));
        }
        return shells;
    }

    /**
     * 把配置转换为gitlab-ci配置（maven,sonarqube）
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param jobVO          生成脚本
     * @return 生成的脚本列表
     */
    private List<String> buildScript(final Long organizationId, final Long projectId, DevopsCiJobVO jobVO) {
        Assert.notNull(jobVO, "Job can't be null");
        Assert.notNull(organizationId, "Organization id can't be null");
        Assert.notNull(projectId, "project id can't be null");
        final Long jobId = jobVO.getId();
        Assert.notNull(jobId, "Ci job id is required.");

        if (JobTypeEnum.SONAR.value().equals(jobVO.getType())) {
            return calculateSonarScript(jobVO);
        } else if (JobTypeEnum.BUILD.value().equals(jobVO.getType())) {
            // 将构建类型的stage中的job的每个step进行解析和转化
            CiConfigVO ciConfigVO = jobVO.getConfigVO();
            if (ciConfigVO == null || CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
                return Collections.emptyList();
            }

            List<Long> existedSequences = new ArrayList<>();
            // 校验前端传入的sequence不为null且不重复
            ciConfigVO.getConfig().forEach(config -> DevopsCiPipelineAdditionalValidator.validConfigSequence(config.getSequence(), config.getName(), existedSequences));

            // 最后生成的所有script集合
            List<String> result = new ArrayList<>();

            // 同一个job中的所有step要按照sequence顺序来
            // 将每一个step都转为一个List<String>并将所有的list合并为一个
            ciConfigVO.getConfig()
                    .stream()
                    .sorted(Comparator.comparingLong(CiConfigTemplateVO::getSequence))
                    .forEach(config -> {
                        CiJobScriptTypeEnum type = CiJobScriptTypeEnum.forType(config.getType().toLowerCase());
                        if (type == null) {
                            throw new CommonException(ERROR_UNSUPPORTED_STEP_TYPE, config.getType());
                        }

                        switch (type) {
                            // GO和NPM是一样处理
                            case NPM:
                                result.addAll(GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(config.getScript()), true, true));
                                break;
                            case MAVEN:
                                // 处理settings文件
                                DevopsCiPipelineAdditionalValidator.validateMavenStep(config);
                                boolean hasSettings = buildAndSaveMavenSettings(projectId, jobId, config);
                                result.addAll(buildMavenScripts(projectId, jobId, config, hasSettings));
                                break;
                            case DOCKER:
                                // 不填skipDockerTlsVerify参数或者填TRUE都是跳过证书校验
                                // TODO 修复 目前后端这个参数的含义是是否跳过证书校验, 前端的含义是是否进行证书校验
                                Boolean doTlsVerify = config.getSkipDockerTlsVerify();
                                result.addAll(GitlabCiUtil.generateDockerScripts(
                                        config.getDockerContextDir(),
                                        config.getDockerFilePath(),
                                        doTlsVerify == null || !doTlsVerify));
                                break;
                            // 上传JAR包阶段是没有选择项目依赖的, 同样也可以复用maven deploy的逻辑
                            case UPLOAD_JAR:
                            case MAVEN_DEPLOY:
                                List<MavenRepoVO> targetRepos = new ArrayList<>();
                                boolean hasMavenSettings = buildAndSaveJarDeployMavenSettings(projectId, jobId, config, targetRepos);
                                result.addAll(buildMavenJarDeployScripts(projectId, jobId, hasMavenSettings, config, targetRepos));
                                break;
                        }
                    });

            return result;
        } else if (JobTypeEnum.CHART.value().equals(jobVO.getType())) {
            // 生成chart步骤
            return ArrayUtil.singleAsList(GitlabCiUtil.generateChartBuildScripts());
        }
        return Collections.emptyList();
    }

    /**
     * 计算sonar脚本
     *
     * @param jobVO
     * @return
     */
    private List<String> calculateSonarScript(DevopsCiJobVO jobVO) {
        // sonar配置转化为gitlab-ci配置
        List<String> scripts = new ArrayList<>();
        SonarQubeConfigVO sonarQubeConfigVO = JSONObject.parseObject(jobVO.getMetadata(), SonarQubeConfigVO.class);
        if (SonarScannerType.SONAR_SCANNER.value().equals(sonarQubeConfigVO.getScannerType())) {
            if (CiSonarConfigType.DEFAULT.value().equals(sonarQubeConfigVO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "error.default.sonar.not.exist");
                scripts.add(GitlabCiUtil.getDefaultSonarScannerCommand(sonarQubeConfigVO.getSources()));
            } else if (CiSonarConfigType.CUSTOM.value().equals(sonarQubeConfigVO.getConfigType())) {
                if (Objects.isNull(sonarQubeConfigVO.getSonarUrl())) {
                    throw new CommonException("error.sonar.url.is.null");
                }
                if (SonarAuthType.USERNAME_PWD.value().equals(sonarQubeConfigVO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarScannerCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getUsername(), sonarQubeConfigVO.getPassword(), sonarQubeConfigVO.getSources()));
                } else if (SonarAuthType.TOKEN.value().equals(sonarQubeConfigVO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarScannerCommandForToken(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getToken(), sonarQubeConfigVO.getSources()));
                }
            } else {
                throw new CommonException("error.sonar.config.type.not.supported", sonarQubeConfigVO.getConfigType());
            }
        } else if (SonarScannerType.SONAR_MAVEN.value().equals(sonarQubeConfigVO.getScannerType())) {
            if (CiSonarConfigType.DEFAULT.value().equals(sonarQubeConfigVO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "error.default.sonar.not.exist");
                scripts.add(GitlabCiUtil.getDefaultSonarCommand(sonarQubeConfigVO.getSkipTests()));
            } else if (CiSonarConfigType.CUSTOM.value().equals(sonarQubeConfigVO.getConfigType())) {
                if (Objects.isNull(sonarQubeConfigVO.getSonarUrl())) {
                    throw new CommonException("error.sonar.url.is.null");
                }
                if (SonarAuthType.USERNAME_PWD.value().equals(sonarQubeConfigVO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getUsername(), sonarQubeConfigVO.getPassword(), sonarQubeConfigVO.getSkipTests()));
                } else if (SonarAuthType.TOKEN.value().equals(sonarQubeConfigVO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommandForToken(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getToken(), sonarQubeConfigVO.getSkipTests()));
                }
            } else {
                throw new CommonException("error.sonar.config.type.not.supported", sonarQubeConfigVO.getConfigType());
            }
        } else {
            throw new CommonException(ResourceCheckConstant.ERROR_SONAR_SCANNER_TYPE_INVALID);
        }
        return scripts;
    }

    /**
     * 生成并存储maven settings到数据库
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param ciConfigTemplateVO 配置信息
     * @return true表示有settings配置，false表示没有
     */
    private boolean buildAndSaveMavenSettings(Long projectId, Long jobId, CiConfigTemplateVO ciConfigTemplateVO) {
        // settings文件内容
        String settings;
        final List<MavenRepoVO> repos = new ArrayList<>();

        // 是否有手动填写仓库表单
        final boolean hasManualRepos = !CollectionUtils.isEmpty(ciConfigTemplateVO.getRepos());
        // 是否有选择已有的maven仓库
        final boolean hasNexusRepos = !CollectionUtils.isEmpty(ciConfigTemplateVO.getNexusMavenRepoIds());

        if (!StringUtils.isEmpty(ciConfigTemplateVO.getMavenSettings())) {
            // 使用用户提供的xml内容，不进行内容的校验
            settings = Base64Util.getBase64DecodedString(ciConfigTemplateVO.getMavenSettings());
        } else if (hasManualRepos || hasNexusRepos) {
            if (hasNexusRepos) {
                // 用户选择的已有的maven仓库
                List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, ciConfigTemplateVO.getNexusMavenRepoIds());
                repos.addAll(nexusMavenRepoDTOs.stream().map(DevopsCiPipelineServiceImpl::convertRepo).collect(Collectors.toList()));
            }

            if (hasManualRepos) {
                // 由用户填写的表单构建xml文件内容
                repos.addAll(ciConfigTemplateVO.getRepos());
            }

            // 构建settings文件
            settings = buildSettings(repos);
        } else {
            // 没有填关于settings的信息
            return false;
        }

        // 这里存储的ci setting文件内容是解密后的
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO(jobId, ciConfigTemplateVO.getSequence(), settings);
        MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        return true;
    }

    @Nullable
    private Cache buildJobCache(DevopsCiJobVO jobConfig) {
        boolean isToUpload = Boolean.TRUE.equals(jobConfig.getToUpload());
        boolean isToDownload = Boolean.TRUE.equals(jobConfig.getToDownload());
        if (isToUpload && isToDownload) {
            return constructCache(CachePolicy.PULL_PUSH.getValue());
        } else if (isToDownload) {
            return constructCache(CachePolicy.PULL.getValue());
        } else if (isToUpload) {
            return constructCache(CachePolicy.PUSH.getValue());
        } else {
            return null;
        }
    }

    private Cache constructCache(String policy) {
        Cache cache = new Cache();
        cache.setKey(GitOpsConstants.GITLAB_CI_DEFAULT_CACHE_KEY);
        cache.setPaths(Collections.singletonList(GitOpsConstants.CHOERODON_CI_CACHE_DIR));
        cache.setPolicy(policy);
        return cache;
    }

    private void buildBeforeScript(GitlabCi gitlabCi, String versionName) {
        List<String> beforeScripts = ArrayUtil.singleAsList(GitOpsConstants.CHOERODON_BEFORE_SCRIPT);
        if (!StringUtils.isEmpty(versionName)) {
            beforeScripts.add(String.format("CI_COMMIT_TAG=%s", versionName));
        }
        // 如果有job启用了缓存设置, 就创建缓存目录
        // 如果全部都是自定义任务, 这个map是空的
        if (!CollectionUtils.isEmpty(gitlabCi.getJobs())) {
            if (gitlabCi.getJobs().values().stream().anyMatch(j -> j.getCache() != null)) {
                beforeScripts.add(GitlabCiUtil.generateCreateCacheDir(GitOpsConstants.CHOERODON_CI_CACHE_DIR));
            }
        }
        gitlabCi.setBeforeScript(beforeScripts);
    }

    /**
     * 生成jar包发布相关的脚本
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param hasSettings        是否有settings配置
     * @param ciConfigTemplateVO maven发布软件包阶段的信息
     * @param targetMavenRepoVO  目标制品库仓库信息
     * @return 生成的shell脚本
     */
    private List<String> buildMavenJarDeployScripts(final Long projectId, final Long jobId, final boolean hasSettings, CiConfigTemplateVO ciConfigTemplateVO, List<MavenRepoVO> targetMavenRepoVO) {
        List<String> shells = new ArrayList<>();
        // 这里这么写是为了考虑之后可能选了多个仓库, 如果是多个仓库的话, 变量替换不便
        // TODO 重构逻辑
        List<String> templateShells = GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(ciConfigTemplateVO.getScript()), true, true);
        // 如果有settings配置, 填入获取settings的指令
        if (hasSettings) {
            shells.add(GitlabCiUtil.downloadMavenSettings(projectId, jobId, ciConfigTemplateVO.getSequence()));
        }
        // 根据目标仓库信息, 渲染发布jar包的指令
        if (!CollectionUtils.isEmpty(targetMavenRepoVO)) {
            // 插入shell指令将配置的settings文件下载到项目目录下

            // 包含repoId锚点的字符串在templateShells中的索引号
            int repoIdIndex = -1;
            // 包含repoUrl锚点的字符串在templateShells中的索引号
            int repoUrlIndex = -1;
            // 寻找包含这两个锚点的字符串位置
            for (int i = 0; i < templateShells.size(); i++) {
                if (repoIdIndex == -1) {
                    if (templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_ID)) {
                        repoIdIndex = i;
                    }
                }
                if (repoUrlIndex == -1) {
                    if (templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_URL)) {
                        repoUrlIndex = i;
                    }
                }
                if (repoIdIndex != -1 && repoUrlIndex != -1) {
                    // 没必要再找了
                    break;
                }
            }

            // 为每一个仓库都从模板的脚本中加一份生成的命令
            for (MavenRepoVO repo : targetMavenRepoVO) {
                // 将预定的变量(仓库名和地址)替换为settings.xml文件指定的
                List<String> commands = new ArrayList<>(templateShells);
                if (repoIdIndex != -1) {
                    commands.set(repoIdIndex, commands.get(repoIdIndex).replace(GitOpsConstants.CHOERODON_MAVEN_REPO_ID, repo.getName()));
                }
                if (repoUrlIndex != -1) {
                    commands.set(repoUrlIndex, commands.get(repoUrlIndex).replace(GitOpsConstants.CHOERODON_MAVEN_REPO_URL, repo.getUrl()));
                }
                shells.addAll(commands);
            }

            // 只生成一个jar包元数据上传指令用于CD阶段
            shells.add(GitlabCiUtil.saveJarMetadata((Long) ciConfigTemplateVO.getMavenDeployRepoSettings().getNexusRepoIds()));
        } else {
            // 如果没有目标仓库信息, 则认为用户是自己填入好了maven发布jar的指令, 不需要渲染
            shells.addAll(templateShells);
        }
        return shells;
    }

    /**
     * 生成并存储maven settings到数据库
     *
     * @param projectId           项目id
     * @param jobId               job id
     * @param ciConfigTemplateVO  配置
     * @param targetRepoContainer 用来存放解析出的目标仓库信息
     * @return 返回true表示有settings信息
     */
    private boolean buildAndSaveJarDeployMavenSettings(Long projectId, Long jobId, CiConfigTemplateVO ciConfigTemplateVO, List<MavenRepoVO> targetRepoContainer) {
        MavenDeployRepoSettings mavenDeployRepoSettings = ciConfigTemplateVO.getMavenDeployRepoSettings();
        Long sequence = ciConfigTemplateVO.getSequence();
        Set<Long> dependencyRepoIds = ciConfigTemplateVO.getNexusMavenRepoIds();
        List<MavenRepoVO> dependencyRepos = ciConfigTemplateVO.getRepos();

        boolean targetRepoEmpty = mavenDeployRepoSettings.getNexusRepoIds() == null;
        boolean dependencyRepoIdsEmpty = CollectionUtils.isEmpty(dependencyRepoIds);
        boolean dependencyRepoEmpty = CollectionUtils.isEmpty(dependencyRepos);

        // 如果都为空, 不生成settings文件
        if (targetRepoEmpty && dependencyRepoIdsEmpty && dependencyRepoEmpty) {
            return false;
        }

        // 查询制品库
        List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, ArrayUtil.singleAsSet(mavenDeployRepoSettings.getNexusRepoIds()));

        // 如果填入的仓库信息和制品库查出的结果都为空, 不生成settings文件
        if (CollectionUtils.isEmpty(nexusMavenRepoDTOs) && dependencyRepoEmpty) {
            return false;
        }

        // 转化制品库信息, 并将目标仓库信息取出, 放入targetRepoContainer
        List<MavenRepoVO> mavenRepoVOS = nexusMavenRepoDTOs.stream().map(r -> {
            MavenRepoVO result = convertRepo(r);
            // 目标仓库不为空, 并且目标仓库包含
            if (!targetRepoEmpty && mavenDeployRepoSettings.getNexusRepoIds().equals(r.getRepositoryId())) {
                targetRepoContainer.add(result);
            }
            return result;
        }).collect(Collectors.toList());

        // 将手动输入的仓库信息也放入列表
        if (!dependencyRepoEmpty) {
            mavenRepoVOS.addAll(dependencyRepos);
        }

        // 生成settings文件内容
        String settings = buildSettings(mavenRepoVOS);
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO(jobId, sequence, settings);
        MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        return true;
    }

    private void createUserRel(List<Long> cdAuditUserIds, Long projectId, Long pipelineId, Long jobId) {
        if (!CollectionUtils.isEmpty(cdAuditUserIds)) {
            cdAuditUserIds.forEach(t -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO(projectId, pipelineId, jobId);
                devopsCdAuditDTO.setUserId(t);
                devopsCdAuditService.baseCreate(devopsCdAuditDTO);
            });
        }
    }

    private void createCdJob(DevopsCdJobVO t, Long projectId, Long stageId, Long pipelineId) {
        t.setProjectId(projectId);
        t.setStageId(stageId);
        t.setPipelineId(pipelineId);
        DevopsCdJobDTO devopsCdJobDTO = ConvertUtils.convertObject(t, DevopsCdJobDTO.class);
        // 环境部署需要保存部署配置信息
        if (JobTypeEnum.CD_DEPLOY.value().equals(t.getType())) {
            // 使用能够解密主键加密的json工具解密
            DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = KeyDecryptHelper.decryptJson(devopsCdJobDTO.getMetadata(), DevopsCdEnvDeployInfoDTO.class);
            // 使用不进行主键加密的json工具再将json写入类, 用于在数据库存非加密数据
            devopsCdJobDTO.setMetadata(JsonHelper.marshalByJackson(devopsCdEnvDeployInfoDTO));
            // 将从Audit-domain中继承的这个字段设置为空， 不然会将一些不需要的字段也序列化到输出到json
            devopsCdEnvDeployInfoDTO.set_innerMap(null);
            devopsCdEnvDeployInfoDTO.setProjectId(projectId);
            // 删除 前端传输的metadata中 多余数据
            updateExtraInfoToNull(devopsCdEnvDeployInfoDTO);
            devopsCdEnvDeployInfoService.save(devopsCdEnvDeployInfoDTO);
            devopsCdJobDTO.setDeployInfoId(devopsCdEnvDeployInfoDTO.getId());
        } else if (JobTypeEnum.CD_HOST.value().equals(t.getType())) {
            // 使用能够解密主键加密的json工具解密
            CdHostDeployConfigVO cdHostDeployConfigVO = KeyDecryptHelper.decryptJson(devopsCdJobDTO.getMetadata(), CdHostDeployConfigVO.class);
            checkCdHostJobName(pipelineId, cdHostDeployConfigVO, t.getName());
            // 使用不进行主键加密的json工具再将json写入类, 用于在数据库存非加密数据
            devopsCdJobDTO.setMetadata(JsonHelper.marshalByJackson(cdHostDeployConfigVO));
        } else if (JobTypeEnum.CD_AUDIT.value().equals(t.getType())) {
            // 如果审核任务，审核人员只有一个人，则默认设置为或签
            if (CollectionUtils.isEmpty(t.getCdAuditUserIds())) {
                throw new CommonException(ResourceCheckConstant.ERROR_PARAM_IS_INVALID);
            }
            if (t.getCdAuditUserIds().size() == 1) {
                devopsCdJobDTO.setCountersigned(1);
            }
        }

        Long jobId = devopsCdJobService.create(devopsCdJobDTO).getId();
        if (JobTypeEnum.CD_AUDIT.value().equals(t.getType())) {
            createUserRel(t.getCdAuditUserIds(), projectId, pipelineId, jobId);
        }

    }

    /**
     * 主机部署 关联ci任务
     * 对于创建或更新根据任务名称获取id
     *
     * @param pipelineId
     * @param ciJobName
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    public Long getCiJobId(Long pipelineId, String ciJobName) {
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(pipelineId);
        devopsCiJobDTO.setName(ciJobName);
        List<DevopsCiJobDTO> ciJobDTOList = devopsCiJobMapper.select(devopsCiJobDTO);
        if (CollectionUtils.isEmpty(ciJobDTOList)) {
            throw new CommonException("error.get.ci.job.id");
        }
        return ciJobDTOList.get(0).getId();
    }

    private void updateExtraInfoToNull(DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO) {
        devopsCdEnvDeployInfoDTO.setId(null);
        devopsCdEnvDeployInfoDTO.setCreatedBy(null);
        devopsCdEnvDeployInfoDTO.setCreationDate(null);
        devopsCdEnvDeployInfoDTO.setLastUpdatedBy(null);
        devopsCdEnvDeployInfoDTO.setLastUpdateDate(null);
    }

    private PipelineAppServiceDeployDTO deployVoToDto(PipelineAppServiceDeployVO appServiceDeployVO) {
        PipelineAppServiceDeployDTO appServiceDeployDTO = new PipelineAppServiceDeployDTO();
        BeanUtils.copyProperties(appServiceDeployVO, appServiceDeployDTO);
        if (appServiceDeployVO.getTriggerVersion() != null && !appServiceDeployVO.getTriggerVersion().isEmpty()) {
            appServiceDeployDTO.setTriggerVersion(String.join(",", appServiceDeployVO.getTriggerVersion()));
        }
        return appServiceDeployDTO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    public void checkCdHostJobName(Long ciPipelineId, CdHostDeployConfigVO deployConfigVO, String cdHostName) {
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        if (deployConfigVO.getImageDeploy() != null
                && deployConfigVO.getImageDeploy().getDeploySource().equals(HostDeploySource.PIPELINE_DEPLOY.getValue())) {
            devopsCiJobDTO.setName(deployConfigVO.getImageDeploy().getPipelineTask());
        }
        if (deployConfigVO.getJarDeploy() != null
                && deployConfigVO.getJarDeploy().getDeploySource().equals(HostDeploySource.PIPELINE_DEPLOY.getValue())) {
            devopsCiJobDTO.setName(deployConfigVO.getJarDeploy().getPipelineTask());
        }
        if (!StringUtils.isEmpty(devopsCiJobDTO.getName())) {
            if (CollectionUtils.isEmpty(devopsCiJobMapper.select(devopsCiJobDTO))) {
                throw new CommonException("error.cd.host.job.union.ci.job", devopsCiJobDTO.getName(), cdHostName);
            }
        }
    }

    /**
     * 将job中的metadata字段解密
     *
     * @param devopsCiJobVO job数据
     */
    private void decryptCiBuildMetadata(DevopsCiJobVO devopsCiJobVO) {
        if (JobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
            // 解密json字符串中的加密的主键
            devopsCiJobVO.setMetadata(JsonHelper.marshalByJackson(KeyDecryptHelper.decryptJson(devopsCiJobVO.getMetadata(), CiConfigVO.class)));
        }
    }
}
