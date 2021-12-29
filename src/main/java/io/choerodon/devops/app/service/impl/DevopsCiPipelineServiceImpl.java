package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.pipeline.*;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.BranchDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.dto.maven.Repository;
import io.choerodon.devops.infra.dto.maven.RepositoryPolicy;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.sonar.CiSonarConfigType;
import io.choerodon.devops.infra.enums.sonar.SonarAuthType;
import io.choerodon.devops.infra.enums.sonar.SonarScannerType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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


    private static final Long DEFAULT_PIPELINE_ID = 0L;
    private static final String CREATE_PIPELINE_FAILED = "create.pipeline.failed";
    private static final String UPDATE_PIPELINE_FAILED = "update.pipeline.failed";
    private static final String DISABLE_PIPELINE_FAILED = "disable.pipeline.failed";
    private static final String ENABLE_PIPELINE_FAILED = "enable.pipeline.failed";
    private static final String DELETE_PIPELINE_FAILED = "delete.pipeline.failed";
    private static final String ERROR_APP_SVC_ID_IS_NULL = "error.app.svc.id.is.null";
    private static final String ERROR_PROJECT_ID_IS_NULL = "error.project.id.is.null";
    private static final String ERROR_CI_MAVEN_REPOSITORY_TYPE = "error.ci.maven.repository.type";
    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "error.maven.settings.insert";
    private static final String ERROR_UNSUPPORTED_STEP_TYPE = "error.unsupported.step.type";
    private static final String ERROR_BRANCH_PERMISSION_MISMATCH = "error.branch.permission.mismatch";
    private static final String UNKNOWN_DEPLOY_TYPE = "unknown.deploy.type";

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

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
    private final DevopsCdJobService devopsCdJobService;
    private final DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
    private final DevopsEnvironmentMapper devopsEnvironmentMapper;
    private final DevopsPipelineRecordRelService devopsPipelineRecordRelService;
    private final DevopsCdPipelineService devopsCdPipelineService;
    private final DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper;
    private final DevopsDeployValueMapper devopsDeployValueMapper;

    @Autowired
    private DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;

    @Autowired
    private DevopsCdPipelineRecordMapper devopsCdPipelineRecordMapper;

    @Autowired
    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;

    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;

    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;

    @Autowired
    @Lazy
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;
    @Autowired
    private DevopsCdHostDeployInfoService devopsCdHostDeployInfoService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    private DevopsHostAppService devopsHostAppService;
    @Autowired
    @Lazy
    private DevopsHostUserPermissionService devopsHostUserPermissionService;
    @Autowired
    private AppExternalConfigService appExternalConfigService;
    @Autowired
    private DevopsPipelineBranchRelMapper devopsPipelineBranchRelMapper;
    @Autowired
    private DevopsCiPipelineFunctionService devopsCiPipelineFunctionService;
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;
    @Autowired
    private DevopsCiStepService devopsCiStepService;
    @Autowired
    private DevopsCiPipelineVariableService devopsCiPipelineVariableService;
    @Autowired
    private DevopsCdApiTestInfoService devopsCdApiTestInfoService;


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
            CiCdPipelineMapper ciCdPipelineMapper,
            DevopsCdStageService devopsCdStageService,
            DevopsCdAuditService devopsCdAuditService,
            DevopsCdJobService devopsCdJobService,
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
        this.devopsCdJobService = devopsCdJobService;
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
            if (m.getType() != null) {
                String[] types = m.getType().split(GitOpsConstants.COMMA);
                if (types.length > 2) {
                    throw new CommonException(ERROR_CI_MAVEN_REPOSITORY_TYPE, m.getType());
                }
            }
            if (Boolean.TRUE.equals(m.getPrivateRepo())) {
                servers.add(new Server(Objects.requireNonNull(m.getName()), Objects.requireNonNull(m.getUsername()), Objects.requireNonNull(m.getPassword())));
            }
            repositories.add(new Repository(
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getUrl()),
                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.RELEASE)),
                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.SNAPSHOT))));
        });
        return MavenSettingsUtil.generateMavenSettings(servers, repositories);
    }

    /**
     * 第一次创建CI流水线时初始化仓库下的.gitlab-ci.yml文件
     *
     * @param gitlabProjectId  gitlab项目id
     * @param branch
     * @param ciFileIncludeUrl include中的链接
     */
    private void initGitlabCiFile(Integer gitlabProjectId, String branch, String ciFileIncludeUrl) {
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, branch, GitOpsConstants.GITLAB_CI_FILE_NAME);

        if (repositoryFile == null || repositoryFile.getContent() == null || repositoryFile.getFilePath() == null) {
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
                        branch);
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
                        GitUserNameUtil.getAdminId(),
                        branch);
            } catch (Exception ex) {
                throw new CommonException("error.create.or.update.gitlab.ci", ex);
            }
        }
    }

    /**
     * 第一次创建CI流水线时初始化外部仓库下的.gitlab-ci.yml文件
     *
     * @param gitlabProjectId  gitlab项目id
     * @param branch
     * @param ciFileIncludeUrl include中的链接
     */
    private void initExternalGitlabCiFile(Integer gitlabProjectId, String branch, String ciFileIncludeUrl, AppExternalConfigDTO appExternalConfigDTO) {
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getExternalWholeFile(gitlabProjectId, branch, GitOpsConstants.GITLAB_CI_FILE_NAME, appExternalConfigDTO);

        if (repositoryFile == null || repositoryFile.getContent() == null || repositoryFile.getFilePath() == null) {
            // 说明项目下还没有CI文件
            // 创建文件
            try {
                LOGGER.info("initGitlabCiFile: create .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.createExternalFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        buildIncludeYaml(ciFileIncludeUrl),
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId(),
                        branch,
                        appExternalConfigDTO);
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
                gitlabServiceClientOperator.updateExternalFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        buildIncludeYaml(ciFileIncludeUrl) + GitOpsConstants.NEW_LINE + commentedLines,
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId(),
                        branch,
                        appExternalConfigDTO);
            } catch (Exception ex) {
                throw new CommonException("error.create.or.update.gitlab.ci", ex);
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
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
        ciCdPipelineDTO.setId(null);
        ciCdPipelineDTO.setToken(GenerateUUID.generateUUID());
        ciCdPipelineDTO.setEnabled(true);
        if (ciCdPipelineMapper.insertSelective(ciCdPipelineDTO) != 1) {
            throw new CommonException(CREATE_PIPELINE_FAILED);
        }
        // 保存流水线分支关系
        saveBranchRel(ciCdPipelineVO, ciCdPipelineDTO);
        // 保存流水线函数
        saveFunction(ciCdPipelineVO, ciCdPipelineDTO);
        // 保存流水线变量
        saveCiVariable(ciCdPipelineVO, ciCdPipelineDTO);

        // 1.保存ci stage信息
        saveCiPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        // 2.保存cd stage信息
        saveCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        return ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineDTO.getId());
    }

    /**
     * 保存流水线分支关系
     *
     * @param ciCdPipelineVO
     * @param ciCdPipelineDTO
     */
    private void saveBranchRel(CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        ciCdPipelineVO.getRelatedBranches().forEach(branch -> {
            DevopsPipelineBranchRelDTO devopsPipelineBranchRelDTO = new DevopsPipelineBranchRelDTO();
            devopsPipelineBranchRelDTO.setId(null);
            devopsPipelineBranchRelDTO.setBranch(branch);
            devopsPipelineBranchRelDTO.setPipelineId(ciCdPipelineDTO.getId());
            MapperUtil.resultJudgedInsertSelective(devopsPipelineBranchRelMapper, devopsPipelineBranchRelDTO, "error.save.pipeline.branch.rel");
        });
    }

    /**
     * 保存流水线函数
     *
     * @param ciCdPipelineVO
     * @param ciCdPipelineDTO
     */
    private void saveFunction(CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOList = ciCdPipelineVO.getDevopsCiPipelineFunctionDTOList();
        if (!CollectionUtils.isEmpty(devopsCiPipelineFunctionDTOList)) {
            devopsCiPipelineFunctionDTOList.forEach(devopsCiPipelineFunctionDTO -> {
                devopsCiPipelineFunctionDTO.setId(null);
                devopsCiPipelineFunctionDTO.setDevopsPipelineId(ciCdPipelineDTO.getId());
                devopsCiPipelineFunctionService.baseCreate(devopsCiPipelineFunctionDTO);
            });
        }
    }

    /**
     * 保存流水线变量
     *
     * @param ciCdPipelineVO
     * @param ciCdPipelineDTO
     */
    private void saveCiVariable(CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        List<DevopsCiPipelineVariableDTO> devopsCiPipelineVariableDTOList = ciCdPipelineVO.getDevopsCiPipelineVariableDTOList();
        if (!CollectionUtils.isEmpty(devopsCiPipelineVariableDTOList)) {
            devopsCiPipelineVariableDTOList.forEach(devopsCiPipelineVariableDTO -> {
                devopsCiPipelineVariableDTO.setId(null);
                devopsCiPipelineVariableDTO.setDevopsPipelineId(ciCdPipelineDTO.getId());
                devopsCiPipelineVariableService.baseCreate(devopsCiPipelineVariableDTO);
            });
        }

    }

    @Override
    public String generateGitlabCiYaml(CiCdPipelineDTO ciCdPipelineDTO) {
        Long pipelineId = ciCdPipelineDTO.getId();
        GitlabCi gitlabCi = buildGitLabCiObject(ciCdPipelineDTO);

        StringBuilder gitlabCiYaml = new StringBuilder(GitlabCiUtil.gitlabCi2yaml(gitlabCi));

        List<DevopsCiJobDTO> devopsCiCustomJobDTOList = devopsCiJobService.listCustomByPipelineId(pipelineId);
        // 拼接自定义job
        if (!CollectionUtils.isEmpty(devopsCiCustomJobDTOList)) {
            for (DevopsCiJobDTO job : devopsCiCustomJobDTOList) {
                gitlabCiYaml.append(GitOpsConstants.NEW_LINE).append(job.getMetadata());
            }
        }
        return gitlabCiYaml.toString();
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
//            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
            ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                devopsCiStageDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存ci job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        // 不让数据库存加密的值
//                        decryptCiBuildMetadata(devopsCiJobVO);
//                        processCiJobVO(devopsCiJobVO);
                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                        devopsCiJobService.create(devopsCiJobDTO);

                        // 保存任务中的步骤信息
                        batchSaveStep(projectId, devopsCiJobDTO.getId(), devopsCiJobVO.getDevopsCiStepVOList());

                    });
                }
            });
            // 保存ci配置文件
//            saveCiContent(projectId, projectDTO.getOrganizationId(), ciCdPipelineDTO.getId(), ciCdPipelineVO);

            AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());
            String ciFileIncludeUrl = String.format(GitOpsConstants.CI_CONTENT_URL_TEMPLATE, gatewayUrl, projectId, ciCdPipelineDTO.getToken());
            if (appServiceDTO.getExternalConfigId() != null) {
                AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
                ciCdPipelineVO.getRelatedBranches().forEach(branch -> initExternalGitlabCiFile(appServiceDTO.getGitlabProjectId(), branch, ciFileIncludeUrl, appExternalConfigDTO));
            } else {
                ciCdPipelineVO.getRelatedBranches().forEach(branch -> initGitlabCiFile(appServiceDTO.getGitlabProjectId(), branch, ciFileIncludeUrl));
            }

        }
    }

    /**
     * 保存步骤的配置信息
     * @param projectId
     * @param jobId
     * @param devopsCiStepVOList
     */
    private void batchSaveStep(Long projectId, Long jobId, List<DevopsCiStepVO> devopsCiStepVOList) {
        devopsCiStepVOList.forEach(devopsCiStepVO -> {
            AbstractDevopsCiStepHandler devopsCiStepHandler = devopsCiStepOperator.getHandlerOrThrowE(devopsCiStepVO.getType());
            devopsCiStepHandler.save(projectId, jobId, devopsCiStepVO);
        });
    }

    @Override
    public CiCdPipelineVO query(Long projectId, Long pipelineId) {
        // 根据pipeline_id查询数据
        CiCdPipelineVO ciCdPipelineVO = getCiCdPipelineVO(pipelineId);
        //查询流水线对应的应用服务
        AppServiceDTO appServiceDTO = getAppServiceDTO(ciCdPipelineVO);
        //当前用户是否能修改流水线权限
        fillEditPipelinePermission(projectId, ciCdPipelineVO, appServiceDTO);
        //查询CI相关的阶段以及JOB
        List<DevopsCiStageVO> devopsCiStageVOS = handleCiStage(pipelineId);
        //查询CD相关的阶段以及JOB
        List<DevopsCdStageVO> devopsCdStageVOS = handleCdStage(pipelineId);
        //封装流水线
        ciCdPipelineVO.setDevopsCiStageVOS(devopsCiStageVOS);
        ciCdPipelineVO.setDevopsCdStageVOS(devopsCdStageVOS);
        return ciCdPipelineVO;
    }

    private List<DevopsCdStageVO> handleCdStage(Long pipelineId) {
        //获取Cdjob
        List<DevopsCdJobVO> devopsCdJobVOS = getDevopsCdJobVOS(pipelineId);
        Map<Long, List<DevopsCdJobVO>> cdJobMap = devopsCdJobVOS.stream().collect(Collectors.groupingBy(DevopsCdJobVO::getStageId));
        //获取CdStage
        List<DevopsCdStageVO> devopsCdStageVOS = getDevopsCdStageVOS(pipelineId, cdJobMap);
        // cd stage排序
        devopsCdStageVOS = devopsCdStageVOS.stream().sorted(Comparator.comparing(DevopsCdStageVO::getSequence)).collect(Collectors.toList());
        return devopsCdStageVOS;
    }

    private List<DevopsCdStageVO> getDevopsCdStageVOS(Long pipelineId, Map<Long, List<DevopsCdJobVO>> cdJobMap) {
        List<DevopsCdStageDTO> devopsCdStageDTOS = devopsCdStageService.queryByPipelineId(pipelineId);
        List<DevopsCdStageVO> devopsCdStageVOS = ConvertUtils.convertList(devopsCdStageDTOS, DevopsCdStageVO.class);
        devopsCdStageVOS.forEach(devopsCdStageVO -> {
            List<DevopsCdJobVO> jobMapOrDefault = cdJobMap.getOrDefault(devopsCdStageVO.getId(), Collections.emptyList());
            jobMapOrDefault.sort(Comparator.comparing(DevopsCdJobVO::getSequence));
            devopsCdStageVO.setJobList(jobMapOrDefault);
        });
        return devopsCdStageVOS;
    }

    private List<DevopsCdJobVO> getDevopsCdJobVOS(Long pipelineId) {
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobService.listByPipelineId(pipelineId);
        List<DevopsCdJobVO> devopsCdJobVOS = ConvertUtils.convertList(devopsCdJobDTOS, DevopsCdJobVO.class);
        //给cd的job加上环境名称
        if (!CollectionUtils.isEmpty(devopsCdJobVOS)) {
            for (DevopsCdJobVO devopsCdJobVO : devopsCdJobVOS) {
                //如果是自动部署添加环境名字
                if (JobTypeEnum.CD_DEPLOY.value().equals(devopsCdJobVO.getType())
                        || JobTypeEnum.CD_DEPLOYMENT.value().equals(devopsCdJobVO.getType())) {
                    handleCdDeploy(devopsCdJobVO);
                } else if (JobTypeEnum.CD_HOST.value().equals(devopsCdJobVO.getType())) {
                    handCdHost(devopsCdJobVO);
                } else if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobVO.getType())) {
                    handCdAudit(devopsCdJobVO);
                } else if (JobTypeEnum.CD_API_TEST.value().equals(devopsCdJobVO.getType())) {
                    handCdApiTest(devopsCdJobVO);
                } else if (JobTypeEnum.CD_EXTERNAL_APPROVAL.value().equals(devopsCdJobVO.getType())) {
                    handCdExternalApproval(devopsCdJobVO);
                }
            }
        }
        return devopsCdJobVOS;
    }

    private void handCdExternalApproval(DevopsCdJobVO devopsCdJobVO) {
        ExternalApprovalJobVO externalApprovalJobVO = JsonHelper.unmarshalByJackson(devopsCdJobVO.getMetadata(), ExternalApprovalJobVO.class);
        // 将主键加密，再序列化为json
        devopsCdJobVO.setExternalApprovalJobVO(externalApprovalJobVO);
    }

    private void handCdApiTest(DevopsCdJobVO devopsCdJobVO) {
        CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(devopsCdJobVO.getMetadata(), CdApiTestConfigVO.class);
        // 将主键加密，再序列化为json
        devopsCdJobVO.setMetadata(KeyDecryptHelper.encryptJson(cdApiTestConfigVO));
    }

    private void handCdAudit(DevopsCdJobVO devopsCdJobVO) {
        //如果是人工审核，返回审核人员信息
        List<Long> longs = devopsCdAuditService.baseListByOptions(null, null, devopsCdJobVO.getId()).stream().map(DevopsCdAuditDTO::getUserId).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(longs);
        devopsCdJobVO.setIamUserDTOS(iamUserDTOS);
        devopsCdJobVO.setCdAuditUserIds(longs);
    }

    private void handCdHost(DevopsCdJobVO devopsCdJobVO) {
        // 加密json中主键
        DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO = devopsCdHostDeployInfoService.queryById(devopsCdJobVO.getDeployInfoId());
        CdHostDeployConfigVO cdHostDeployConfigVO = ConvertUtils.convertObject(devopsCdHostDeployInfoDTO, CdHostDeployConfigVO.class);
        if (devopsCdHostDeployInfoDTO.getJarDeployJson() != null) {
            cdHostDeployConfigVO.setJarDeploy(JsonHelper.unmarshalByJackson(devopsCdHostDeployInfoDTO.getJarDeployJson(), CdHostDeployConfigVO.JarDeploy.class));
        }

        devopsCdJobVO.setEdit(devopsHostUserPermissionService.checkUserOwnUsePermission(devopsCdJobVO.getProjectId(), devopsCdHostDeployInfoDTO.getHostId(), DetailsHelper.getUserDetails().getUserId()));

        devopsCdJobVO.setMetadata(JsonHelper.singleQuoteWrapped(KeyDecryptHelper.encryptJson(cdHostDeployConfigVO)));
    }

    private void handleCdDeploy(DevopsCdJobVO devopsCdJobVO) {
        DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(devopsCdJobVO.getDeployInfoId());
        DevopsDeployInfoVO devopsDeployInfoVO = ConvertUtils.convertObject(devopsCdEnvDeployInfoDTO, DevopsDeployInfoVO.class);
        if (devopsCdEnvDeployInfoDTO.getAppConfigJson() != null) {
            devopsDeployInfoVO.setAppConfig(JsonHelper.unmarshalByJackson(devopsCdEnvDeployInfoDTO.getAppConfigJson(), DevopsDeployGroupAppConfigVO.class));
        }
        if (devopsCdEnvDeployInfoDTO.getContainerConfigJson() != null) {
            devopsDeployInfoVO.setContainerConfig(JsonHelper.unmarshalByJackson(devopsCdEnvDeployInfoDTO.getContainerConfigJson(), new TypeReference<List<DevopsDeployGroupContainerConfigVO>>() {
            }));
        }
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getEnvId());
        if (!Objects.isNull(devopsEnvironmentDTO)) {
            devopsCdJobVO.setEnvName(devopsEnvironmentDTO.getName());
        }
        //判断当前用户是不是有环境的权限
        //环境为项目下所有成员都有权限
        devopsCdJobVO.setEdit(isEditCdJob(devopsEnvironmentDTO, devopsCdJobVO));
        // 加密json中主键
        devopsCdJobVO.setMetadata(JsonHelper.singleQuoteWrapped(KeyDecryptHelper.encryptJson(devopsDeployInfoVO)));
    }

    private boolean isEditCdJob(DevopsEnvironmentDTO devopsEnvironmentDTO, DevopsCdJobVO devopsCdJobVO) {
        if (Objects.isNull(devopsEnvironmentDTO)) {
            return Boolean.FALSE;
        }
        if (devopsEnvironmentDTO.getSkipCheckPermission()) {
            return Boolean.TRUE;
        }
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (baseServiceClientOperator.isProjectOwner(userDetails.getUserId(), devopsCdJobVO.getProjectId())) {
            return Boolean.TRUE;
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsCdJobVO.getProjectId());
        if (baseServiceClientOperator.isOrganzationRoot(userDetails.getUserId(), projectDTO.getOrganizationId()) || userDetails.getAdmin()) {
            return Boolean.TRUE;
        }
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
        devopsEnvUserPermissionDTO.setEnvId(devopsEnvironmentDTO.getId());
        devopsEnvUserPermissionDTO.setIamUserId(userDetails.getUserId());
        List<DevopsEnvUserPermissionDTO> devopsEnvUserPermissionDTOS = devopsEnvUserPermissionMapper.select(devopsEnvUserPermissionDTO);
        if (!CollectionUtils.isEmpty(devopsEnvUserPermissionDTOS)) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private List<DevopsCiStageVO> handleCiStage(Long pipelineId) {
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(pipelineId);
        if (CollectionUtils.isEmpty(devopsCiStageDTOList)) {
            return new ArrayList<>();
        }
        //处理ci流水线Job
        Map<Long, List<DevopsCiJobVO>> ciJobMap = handleCiJob(pipelineId);
        //处理CI流水线stage
        List<DevopsCiStageVO> devopsCiStageVOS = getDevopsCiStageVOS(devopsCiStageDTOList, ciJobMap);
        //ci stage排序
        devopsCiStageVOS = ciStageSort(devopsCiStageVOS);
        return devopsCiStageVOS;
    }

    private List<DevopsCiStageVO> getDevopsCiStageVOS(List<DevopsCiStageDTO> devopsCiStageDTOList, Map<Long, List<DevopsCiJobVO>> ciJobMap) {
        List<DevopsCiStageVO> devopsCiStageVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageVO.class);
        devopsCiStageVOS.forEach(devopsCiStageVO -> {
            List<DevopsCiJobVO> ciJobVOS = ciJobMap.getOrDefault(devopsCiStageVO.getId(), Collections.emptyList());
            ciJobVOS = ciJobVOS.stream().sorted(Comparator.comparingLong(DevopsCiJobVO::getId)).collect(Collectors.toList());
            devopsCiStageVO.setJobList(ciJobVOS);
        });
        return devopsCiStageVOS;
    }

    private List<DevopsCiStageVO> ciStageSort(List<DevopsCiStageVO> devopsCiStageVOS) {
        devopsCiStageVOS = devopsCiStageVOS.stream().sorted(Comparator.comparing(DevopsCiStageVO::getSequence)).collect(Collectors.toList());
        for (DevopsCiStageVO devopsCiStageVO : devopsCiStageVOS) {
            devopsCiStageVO.setType(StageType.CI.getType());
        }
        return devopsCiStageVOS;
    }

    private Map<Long, List<DevopsCiJobVO>> handleCiJob(Long pipelineId) {
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(pipelineId);
        if (CollectionUtils.isEmpty(devopsCiJobDTOS)) {
            return new HashMap<>();
        }
        List<DevopsCiJobVO> devopsCiJobVOS = ConvertUtils.convertList(devopsCiJobDTOS, DevopsCiJobVO.class);
//        devopsCiJobVOS.forEach(this::processBeforeQueryJob);
        // 封装CI对象
        List<Long> jobIds = devopsCiJobVOS.stream().map(DevopsCiJobVO::getId).collect(Collectors.toList());
        List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobIds(jobIds);
        Map<Long, List<DevopsCiStepDTO>> jobStepMap = devopsCiStepDTOS.stream().collect(Collectors.groupingBy(DevopsCiStepDTO::getDevopsCiJobId));

        devopsCiJobVOS.forEach(devopsCiJobVO -> {
            List<DevopsCiStepDTO> ciStepDTOS = jobStepMap.get(devopsCiJobVO.getId());
            List<DevopsCiStepVO> devopsCiStepVOList = ConvertUtils.convertList(ciStepDTOS, DevopsCiStepVO.class);
            devopsCiStepVOList.forEach(ciStepVO -> {
                AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(ciStepVO.getType());
                handler.fillStepConfigInfo(ciStepVO);
            });
            devopsCiJobVO.setDevopsCiStepVOList(devopsCiStepVOList);
        });
        return devopsCiJobVOS.stream().collect(Collectors.groupingBy(DevopsCiJobVO::getCiStageId));
    }

    private void fillEditPipelinePermission(Long projectId, CiCdPipelineVO ciCdPipelineVO, AppServiceDTO appServiceDTO) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        //当前用户是否对这条流水线有修改的权限
        Set<Long> memberAppServiceIds = appServiceService.getMemberAppServiceIdsByAccessLevel(projectDTO.getOrganizationId(), projectId, DetailsHelper.getUserDetails().getUserId(), AccessLevel.DEVELOPER.value, appServiceDTO.getId());
        if (!CollectionUtils.isEmpty(memberAppServiceIds) && memberAppServiceIds.contains(appServiceDTO.getId())) {
            ciCdPipelineVO.setEdit(Boolean.TRUE);
        } else {
            ciCdPipelineVO.setEdit(Boolean.FALSE);
        }
    }

    private AppServiceDTO getAppServiceDTO(CiCdPipelineVO ciCdPipelineVO) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineVO.getAppServiceId());
        if (appServiceDTO == null) {
            throw new CommonException("error.app.service.null");
        }
        ciCdPipelineVO.setAppServiceCode(appServiceDTO.getCode());
        ciCdPipelineVO.setAppServiceType(appServiceDTO.getType());
        ciCdPipelineVO.setAppServiceName(appServiceDTO.getName());
        ciCdPipelineVO.setAppServiceName(appServiceDTO.getName());
        return appServiceDTO;
    }

    private CiCdPipelineVO getCiCdPipelineVO(Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        CommonExAssertUtil.assertTrue(ciCdPipelineDTO != null, "error.pipeline.not.exist", pipelineId);
        return ConvertUtils.convertObject(ciCdPipelineDTO, CiCdPipelineVO.class);
    }

//    private void processBeforeQueryJob(DevopsCiJobVO devopsCiJobVO) {
//        if (JobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
//            // 反序列化
//            CiConfigVO ciConfigVO = JSONObject.parseObject(devopsCiJobVO.getMetadata(), CiConfigVO.class);
//            if (!CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
//                // 将script字段加密
//                ciConfigVO.getConfig().stream().filter(e -> !Objects.isNull(e.getScript())).forEach(c -> c.setScript(Base64Util.getBase64EncodedString(c.getScript())));
//                // 序列化
//                devopsCiJobVO.setMetadata(JsonHelper.singleQuoteWrapped(JSONObject.toJSONString(ciConfigVO)));
//            }
//        } else if (JobTypeEnum.CUSTOM.value().equals(devopsCiJobVO.getType())) {
//            // 加密自定义任务的元数据
//            devopsCiJobVO.setMetadata(Base64Util.getBase64EncodedString(devopsCiJobVO.getMetadata()));
//        }
//    }

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
    public Page<CiCdPipelineVO> listByProjectIdAndAppName(Long projectId, String searchParam, PageRequest pageRequest) {
        if (projectId == null) {
            throw new CommonException(ERROR_PROJECT_ID_IS_NULL);
        }
        // 应用有权限的应用服务
        Long userId = DetailsHelper.getUserDetails().getUserId();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        boolean projectOwner = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        Set<Long> appServiceIds;
        if (projectOwner) {
            appServiceIds = appServiceMapper.listByActive(projectId, null).stream().map(AppServiceDTO::getId).collect(Collectors.toSet());
        } else {
            //如果是项目成员，需要developer及以上的权限
            appServiceIds = appServiceService.getMemberAppServiceIds(projectDTO.getOrganizationId(), projectId, userId);
            // 添加外部应用服务
            appServiceIds.addAll(appServiceService.listExternalAppIdByProjectId(projectId));
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new Page<>();
            }
        }

        // 查询流水线
        Page<CiCdPipelineVO> pipelinePage = PageHelper.doPage(pageRequest, () -> ciCdPipelineMapper.queryByProjectIdAndName(projectId, appServiceIds, searchParam));

        if (CollectionUtils.isEmpty(pipelinePage.getContent())) {
            return pipelinePage;
        }
        pipelinePage.getContent().forEach(pipelineVO -> {
            // 查询每条流水线，最新的一条执行记录
            PipelineCompositeRecordVO pipelineCompositeRecordVO = devopsPipelineRecordRelService.queryLatestedPipelineRecord(pipelineVO.getId());
            if (pipelineCompositeRecordVO != null) {
                // 判断是否存在记录
                pipelineVO.setHasRecords(true);
                //计算流水线上一次执行的状态和时间
                String latestExecuteStatus = calculateExecuteStatus(pipelineCompositeRecordVO);
                pipelineVO.setLatestExecuteStatus(latestExecuteStatus);
                pipelineVO.setLatestExecuteDate(pipelineCompositeRecordVO.getCreationDate());
            } else {
                pipelineVO.setLatestExecuteStatus(PipelineStatus.SKIPPED.toValue());
                pipelineVO.setLatestExecuteDate(pipelineVO.getCreationDate());
            }

        });
        return pipelinePage;
    }

    private String calculateExecuteStatus(PipelineCompositeRecordVO pipelineCompositeRecordVO) {
        if (pipelineCompositeRecordVO.getCiStatus() != null && pipelineCompositeRecordVO.getCdStatus() != null) {
            return PipelineStatus.SUCCESS.toValue().equals(pipelineCompositeRecordVO.getCiStatus())
                    ? pipelineCompositeRecordVO.getCdStatus() : pipelineCompositeRecordVO.getCiStatus();
        }
        if (pipelineCompositeRecordVO.getCiStatus() != null && pipelineCompositeRecordVO.getCdStatus() == null) {
            return pipelineCompositeRecordVO.getCiStatus();
        }
        if (pipelineCompositeRecordVO.getCiStatus() == null && pipelineCompositeRecordVO.getCdStatus() != null) {
            return pipelineCompositeRecordVO.getCdStatus();
        }
        return null;
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
        List<DevopsCiStageDTO> devopsCiStageDTOS = devopsCiStageService.listByPipelineId(pipelineId);
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

        // 删除content file
        devopsCiContentService.deleteByPipelineId(pipelineId);

        // 删除流水线定义函数
        devopsCiPipelineFunctionService.deleteByPipelineId(pipelineId);


        // 删除.gitlab-ci.yaml文件
        List<DevopsPipelineBranchRelDTO> devopsPipelineBranchRelDTOS = listPipelineBranchRel(pipelineId);
        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());

        if (!CollectionUtils.isEmpty(devopsCiStageDTOS)) {
            devopsPipelineBranchRelDTOS.forEach(devopsPipelineBranchRelDTO -> {
                if (appExternalConfigDTO == null) {
                    deleteGitlabCiFile(appServiceDTO.getGitlabProjectId(), devopsPipelineBranchRelDTO.getBranch());
                } else {
                    deleteExternalGitlabCiFile(appServiceDTO.getGitlabProjectId(), devopsPipelineBranchRelDTO.getBranch(), appExternalConfigDTO);
                }
            });
        }


        // 删除流水线分支关系
        DevopsPipelineBranchRelDTO devopsPipelineBranchRelDTO = new DevopsPipelineBranchRelDTO();
        devopsPipelineBranchRelDTO.setPipelineId(pipelineId);

        devopsPipelineBranchRelMapper.delete(devopsPipelineBranchRelDTO);
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
    @Transactional
    public void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref, Map<String, String> variables) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());

        //触发ci流水线
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());
        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        // 外置仓库不校验
        if (appExternalConfigDTO == null) {
            checkUserBranchPushPermission(projectId, userAttrDTO.getGitlabUserId(), gitlabProjectId, ref);
        }
        Pipeline pipeline = gitlabServiceClientOperator.createPipeline(gitlabProjectId.intValue(),
                userAttrDTO.getGitlabUserId().intValue(),
                ref,
                appExternalConfigDTO,
                variables);
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
            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId.intValue(),
                    pipeline.getId(),
                    userAttrDTO.getGitlabUserId().intValue(),
                    appExternalConfigDTO);
            devopsCiJobRecordService.create(devopsCiPipelineRecordDTO.getId(), gitlabProjectId, jobDTOS, userAttrDTO.getIamUserId(), appServiceDTO.getId());
        } catch (Exception e) {
            LOGGER.info("save pipeline Records failed.", e);
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
    public void checkUserBranchMergePermission(Long projectId, Long gitlabUserId, Long gitlabProjectId, String ref) {
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
        if (Boolean.TRUE.equals(branchDTO.getProtected())) {
            if (Boolean.FALSE.equals(branchDTO.getDevelopersCanMerge()) && memberDTO.getAccessLevel() < AccessLevel.MASTER.toValue()) {
                throw new CommonException(ERROR_BRANCH_PERMISSION_MISMATCH, ref);
            }
            if (Boolean.TRUE.equals(branchDTO.getDevelopersCanMerge()) && memberDTO.getAccessLevel() < AccessLevel.DEVELOPER.toValue()) {
                throw new CommonException(ERROR_BRANCH_PERMISSION_MISMATCH, ref);
            }
        }

    }

    @Override
    public int selectCountByAppServiceId(Long appServiceId) {
        CiCdPipelineDTO ciCdPipelineDTO = new CiCdPipelineDTO();
        ciCdPipelineDTO.setAppServiceId(Objects.requireNonNull(appServiceId));
        return ciCdPipelineMapper.selectCount(ciCdPipelineDTO);
    }

    @Override
    public List<CiCdPipelineDTO> devopsPipline(Long projectId) {
        List<CiCdPipelineDTO> ciCdPipelineDTOS = devopsCiCdPipelineMapper.selectPipelineByProjectId(projectId);
        if (CollectionUtils.isEmpty(ciCdPipelineDTOS)) {
            return new ArrayList<>();
        }
        ciCdPipelineDTOS.forEach(cdPipelineDTO -> {
            //关联应用服务
            AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(cdPipelineDTO.getAppServiceId());
            if (!Objects.isNull(appServiceDTO)) {
                cdPipelineDTO.setName(cdPipelineDTO.getName() + "(" + appServiceDTO.getName() + ")");
            }
        });
        return ciCdPipelineDTOS;
    }

    @Override
    public PipelineFrequencyVO listPipelineTrigger(Long pipelineId, Date startTime, Date endTime) {
        PipelineFrequencyVO pipelineFrequencyVO = new PipelineFrequencyVO();
        if (pipelineId == null) {
            return pipelineFrequencyVO;
        }
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        if (Objects.isNull(ciCdPipelineDTO)) {
            return pipelineFrequencyVO;
        }
        List<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS = devopsPipelineRecordRelMapper.listByPipelineId(pipelineId, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime()));
        if (CollectionUtils.isEmpty(devopsPipelineRecordRelDTOS)) {
            return pipelineFrequencyVO;
        }
        //按照创建时间分组
        Map<String, List<DevopsPipelineRecordRelDTO>> stringListMap = devopsPipelineRecordRelDTOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getCreationDate().getTime()).toString()));
        //将创建时间排序
        List<String> creationDates = new ArrayList<>(devopsPipelineRecordRelDTOS.stream().map(deployDO -> new java.sql.Date(deployDO.getCreationDate().getTime()).toString()).collect(Collectors.toSet()));
        List<Long> pipelineFrequencies = new ArrayList<>();
        List<Long> pipelineSuccessFrequency = new ArrayList<>();
        List<Long> pipelineFailFrequency = new ArrayList<>();
        creationDates.forEach(date -> {
            //每天流水线的触发次数
            List<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS1 = stringListMap.get(date);
            pipelineFrequencies.add(Long.valueOf(devopsPipelineRecordRelDTOS1.size()));
            fillPipelineFrequencyVO(pipelineSuccessFrequency, pipelineFailFrequency, devopsPipelineRecordRelDTOS1);
        });
        pipelineFrequencyVO.setCreateDates(creationDates);
        pipelineFrequencyVO.setPipelineFrequencys(pipelineFrequencies);
        pipelineFrequencyVO.setPipelineSuccessFrequency(pipelineSuccessFrequency);
        pipelineFrequencyVO.setPipelineFailFrequency(pipelineFailFrequency);
        return pipelineFrequencyVO;
    }

    @Override
    public Page<CiCdPipelineRecordVO> pagePipelineTrigger(Long pipelineId, Date startTime, Date endTime, PageRequest pageRequest) {
        Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS = PageHelper.doPage(pageRequest, () -> devopsPipelineRecordRelMapper.listByPipelineId(pipelineId, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime())));
        if (CollectionUtils.isEmpty(devopsPipelineRecordRelDTOS.getContent())) {
            return new Page<>();
        }
        return handPipelineRecord(devopsPipelineRecordRelDTOS);
    }

    private Page<CiCdPipelineRecordVO> handPipelineRecord(Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS) {
        Page<CiCdPipelineRecordVO> cdPipelineRecordVOS = ConvertUtils.convertPage(devopsPipelineRecordRelDTOS, this::dtoToVo);
        cdPipelineRecordVOS.getContent().forEach(ciCdPipelineRecordVO -> {
            CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineRecordVO.getPipelineId());
            if (Objects.isNull(ciCdPipelineDTO)) {
                return;
            }
            //流水线名称
            ciCdPipelineRecordVO.setPipelineName(ciCdPipelineDTO.getName());
            ciCdPipelineRecordVO.setViewId(CiCdPipelineUtils.handleId(ciCdPipelineRecordVO.getDevopsPipelineRecordRelId()));
            //关联应用服务
            AppServiceDTO appServiceDTO = new AppServiceDTO();
            appServiceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineDTO.getAppServiceId());
            ciCdPipelineRecordVO.setAppServiceName(appServiceDTO.getName());
            //触发者
            ciCdPipelineRecordVO.setIamUserDTO(baseServiceClientOperator.queryUserByUserId(ciCdPipelineRecordVO.getCreatedBy()));
            //阶段集合
            DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryByCiPipelineRecordId(ciCdPipelineRecordVO.getCiRecordId());
            DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryByCdPipelineRecordId(ciCdPipelineRecordVO.getCdRecordId());
            List<StageRecordVO> stageRecordVOS = getStageRecordVOS(devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
            ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
            //状态
            CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
            //执行耗时
            ciCdPipelineRecordVO.setDurationSeconds(getPipelineExecuteTime(ciCdPipelineRecordVO.getDevopsPipelineRecordRelId()));

        });
        return cdPipelineRecordVOS;
    }

    private List<StageRecordVO> getStageRecordVOS(DevopsCiPipelineRecordVO devopsCiPipelineRecordVO, DevopsCdPipelineRecordVO devopsCdPipelineRecordVO) {
        List<StageRecordVO> stageRecordVOS = new ArrayList<>();
        if (!Objects.isNull(devopsCiPipelineRecordVO)) {
            stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());
        }
        if (!Objects.isNull(devopsCdPipelineRecordVO)) {
            stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
        }
        return stageRecordVOS;
    }


    @Override
    public ExecuteTimeVO pipelineExecuteTime(List<Long> pipelineIds, Date startTime, Date endTime) {
        ExecuteTimeVO executeTimeVO = new ExecuteTimeVO();
        if (CollectionUtils.isEmpty(pipelineIds)) {
            return executeTimeVO;
        }
        List<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS = devopsPipelineRecordRelMapper.listByPipelineIds(pipelineIds, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime()));

        if (CollectionUtils.isEmpty(devopsPipelineRecordRelDTOS)) {
            return executeTimeVO;
        }
        //有流水线执行时间的时间集合
        List<Date> creationDates = new ArrayList<>();
        //所有流水线执行详情的集合
        List<PipelineExecuteVO> pipelineExecuteVOS = new ArrayList<>();
        //按照流水线id进行分组
        Map<Long, List<DevopsPipelineRecordRelDTO>> longDevopsPipelineRecordRelDTOMap = devopsPipelineRecordRelDTOS.stream().collect(Collectors.groupingBy(DevopsPipelineRecordRelDTO::getPipelineId));
        for (Map.Entry<Long, List<DevopsPipelineRecordRelDTO>> longDevopsPipelineRecordRelDTOEntry : longDevopsPipelineRecordRelDTOMap.entrySet()) {
            Long pipelineId = longDevopsPipelineRecordRelDTOEntry.getKey();
            CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
            PipelineExecuteVO pipelineExecuteVO = new PipelineExecuteVO();
            if (Objects.isNull(ciCdPipelineDTO)) {
                continue;
            }
            pipelineExecuteVO.setPipelineName(ciCdPipelineDTO.getName());
            //具体每条流水线每个时间的执行时长
            //一条流水线跑出的所有记录
            List<DevopsPipelineRecordRelDTO> pipelineRecordRelDTOS = longDevopsPipelineRecordRelDTOEntry.getValue();
            List<ExecuteDetailVO> executeDetailVOS = new ArrayList<>();
            pipelineRecordRelDTOS.forEach(devopsPipelineRecordRelDTO -> {
                ExecuteDetailVO executeDetailVO = new ExecuteDetailVO();
                executeDetailVO.setExecuteDate(devopsPipelineRecordRelDTO.getCreationDate());
                executeDetailVO.setExecuteTime(secondsToMinute(getPipelineExecuteTime(devopsPipelineRecordRelDTO.getId())));
                executeDetailVOS.add(executeDetailVO);
            });
            pipelineExecuteVO.setExecuteDetailVOS(executeDetailVOS);
            pipelineExecuteVOS.add(pipelineExecuteVO);
        }
        executeTimeVO.setCreationDates(creationDates);
        executeTimeVO.setPipelineExecuteVOS(pipelineExecuteVOS);
        return executeTimeVO;
    }

    @Override
    public Page<CiCdPipelineRecordVO> pagePipelineExecuteTime(List<Long> pipelineIds, Date startTime, Date endTime, PageRequest pageRequest) {
        Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS = PageHelper.doPage(pageRequest, () -> devopsPipelineRecordRelMapper.listByPipelineIds(pipelineIds, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime())));
        if (CollectionUtils.isEmpty(devopsPipelineRecordRelDTOS.getContent())) {
            return new Page<>();
        }
        return handPipelineRecord(devopsPipelineRecordRelDTOS);
    }

    @Override
    public Map<String, String> runnerGuide(Long projectId) {

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId, false, false, false);
        Tenant tenant = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        // name: orgName-projectName + suffix
        String groupName = GitOpsUtil.renderGroupName(tenant.getTenantNum(),
                projectDTO.getCode(), "");
        String processedGitlabUrl = "";
        if (gitlabUrl.endsWith("/")) {
            processedGitlabUrl = gitlabUrl.substring(0, gitlabUrl.length() - 1);
        } else {
            processedGitlabUrl = gitlabUrl;
        }

        Map<String, String> params = new HashMap<>();
        params.put("gitlab-group-url", String.format("%s/%s", processedGitlabUrl, groupName));
        params.put("gateway", gatewayUrl);
        params.put("gitlab-url", gitlabUrl);

        return params;
    }

    @Override
    public List<DevopsPipelineBranchRelDTO> listPipelineBranchRel(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);

        DevopsPipelineBranchRelDTO devopsPipelineBranchRelDTO = new DevopsPipelineBranchRelDTO();
        devopsPipelineBranchRelDTO.setPipelineId(pipelineId);
        return devopsPipelineBranchRelMapper.select(devopsPipelineBranchRelDTO);
    }

    @Override
    public List<PipelineInstanceReferenceVO> listTaskReferencePipelineInfo(Long projectId, Set<Long> taskIds) {
        List<PipelineInstanceReferenceVO> pipelineInstanceReferenceVOList = new ArrayList<>();
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobService.listByProjectIdAndType(projectId, JobTypeEnum.CD_API_TEST);
        if (CollectionUtils.isEmpty(devopsCdJobDTOS)) {
            return pipelineInstanceReferenceVOList;
        }

        // 收集与测试任务关联的任务列表，并记录对应关系
        for (DevopsCdJobDTO devopsCdJobDTO : devopsCdJobDTOS) {
            CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(devopsCdJobDTO.getMetadata(), CdApiTestConfigVO.class);
            if (taskIds.contains(cdApiTestConfigVO.getApiTestTaskId())) {
                PipelineInstanceReferenceVO pipelineInstanceReferenceVO = new PipelineInstanceReferenceVO();
                pipelineInstanceReferenceVO.setJobId(devopsCdJobDTO.getId());
                pipelineInstanceReferenceVO.setTaskId(cdApiTestConfigVO.getApiTestTaskId());
                pipelineInstanceReferenceVOList.add(pipelineInstanceReferenceVO);
            }
        }
        if (CollectionUtils.isEmpty(pipelineInstanceReferenceVOList)) {
            return pipelineInstanceReferenceVOList;
        }
        Set<Long> jobIds = pipelineInstanceReferenceVOList.stream().map(PipelineInstanceReferenceVO::getJobId).collect(Collectors.toSet());
        List<DevopsCdJobVO> devopsCdJobVOS = devopsCdJobService.listByIdsWithNames(jobIds);
        Map<Long, DevopsCdJobVO> cdJobVOMap = devopsCdJobVOS.stream().collect(Collectors.toMap(DevopsCdJobVO::getId, Function.identity()));
        pipelineInstanceReferenceVOList.forEach(pipelineInstanceReferenceVO -> {
            DevopsCdJobVO devopsCdJobVO = cdJobVOMap.get(pipelineInstanceReferenceVO.getJobId());
            pipelineInstanceReferenceVO.setPipelineName(devopsCdJobVO.getPipelineName());
            pipelineInstanceReferenceVO.setStageName(devopsCdJobVO.getStageName());
            pipelineInstanceReferenceVO.setJobName(devopsCdJobVO.getName());
        });

        return pipelineInstanceReferenceVOList;
    }

    @Override
    public List<DevopsCiPipelineFunctionDTO> listFunctionsByDevopsPipelineId(Long projectId, Long pipelineId, Boolean includeDefault) {
        List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOList = new ArrayList<>();

        if (Boolean.TRUE.equals(includeDefault) && !pipelineId.equals(DEFAULT_PIPELINE_ID)) {
            devopsCiPipelineFunctionDTOList.addAll(devopsCiPipelineFunctionService.listFunctionsByDevopsPipelineId(DEFAULT_PIPELINE_ID));
        }
        List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOS = devopsCiPipelineFunctionService.listFunctionsByDevopsPipelineId(pipelineId);
        devopsCiPipelineFunctionDTOList.addAll(devopsCiPipelineFunctionDTOS);
        return devopsCiPipelineFunctionDTOList;
    }

    private CiCdPipelineRecordVO dtoToVo(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
        ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(devopsPipelineRecordRelDTO.getId());
        ciCdPipelineRecordVO.setCiRecordId(devopsPipelineRecordRelDTO.getCiPipelineRecordId());
        ciCdPipelineRecordVO.setCdRecordId(devopsPipelineRecordRelDTO.getCdPipelineRecordId());
        ciCdPipelineRecordVO.setCreatedDate(devopsPipelineRecordRelDTO.getCreationDate());
        ciCdPipelineRecordVO.setCreatedBy(devopsPipelineRecordRelDTO.getCreatedBy());
        ciCdPipelineRecordVO.setPipelineId(devopsPipelineRecordRelDTO.getPipelineId());
        return ciCdPipelineRecordVO;
    }

    private <T> void calculateStageStatus(DevopsCiStageRecordVO stageRecord, Map<String, List<T>> statusMap) {
        if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.CREATED.value()))) {
            stageRecord.setStatus(JobStatusEnum.CREATED.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.PENDING.value()))) {
            stageRecord.setStatus(JobStatusEnum.PENDING.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.RUNNING.value()))) {
            stageRecord.setStatus(JobStatusEnum.RUNNING.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.FAILED.value()))) {
            stageRecord.setStatus(JobStatusEnum.FAILED.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.SUCCESS.value()))) {
            stageRecord.setStatus(JobStatusEnum.SUCCESS.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.CANCELED.value()))) {
            stageRecord.setStatus(JobStatusEnum.CANCELED.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.SKIPPED.value()))) {
            stageRecord.setStatus(JobStatusEnum.SKIPPED.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.MANUAL.value()))) {
            stageRecord.setStatus(JobStatusEnum.MANUAL.value());
        }
    }

    private List<DevopsCiJobRecordDTO> filterJobs(List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS) {
        List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(devopsCiJobRecordDTOS)) {
            return devopsCiJobRecordDTOList;
        }
        Map<String, List<DevopsCiJobRecordDTO>> jobMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getName));
        jobMap.forEach((k, v) -> {
            if (v.size() > 1) {
                Optional<DevopsCiJobRecordDTO> ciJobRecordDTO = v.stream().max(Comparator.comparing(DevopsCiJobRecordDTO::getId));
                devopsCiJobRecordDTOList.add(ciJobRecordDTO.get());
            } else if (v.size() == 1) {
                devopsCiJobRecordDTOList.add(v.get(0));
            }
        });
        return devopsCiJobRecordDTOList;
    }


    private Long getPipelineExecuteTime(Long relId) {
        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelMapper.selectByPrimaryKey(relId);
        if (Objects.isNull(devopsPipelineRecordRelDTO)) {
            return 0L;
        }
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(devopsPipelineRecordRelDTO.getCiPipelineRecordId());
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(devopsPipelineRecordRelDTO.getCdPipelineRecordId());
        Long totalSeconds = 0L;
        //取每个ci流水线记录里面最新的job记录
        if (!Objects.isNull(devopsCiPipelineRecordDTO)) {
            DevopsCiJobRecordDTO devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
            devopsCiJobRecordDTO.setCiPipelineRecordId(devopsCiPipelineRecordDTO.getId());
            List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS = devopsCiJobRecordMapper.select(devopsCiJobRecordDTO);
            //流水线中job的名称是唯一的，所以这里按照job的名字进行分组
            Map<String, List<DevopsCiJobRecordDTO>> stringListMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getName));
            for (Map.Entry<String, List<DevopsCiJobRecordDTO>> stringListEntry : stringListMap.entrySet()) {
                //取每个分组里面最新的一条记录计算时间
                List<DevopsCiJobRecordDTO> ciJobRecordDTOS = stringListEntry.getValue().stream().sorted(Comparator.comparing(DevopsCiJobRecordDTO::getId).reversed()).collect(Collectors.toList());
                Long durationSeconds = ciJobRecordDTOS.get(0).getDurationSeconds();
                totalSeconds += Optional.ofNullable(durationSeconds).orElseGet(() -> 0L);
            }
        }
        if (!Objects.isNull(devopsCdPipelineRecordDTO)) {
            //cd阶段就先查询stage 记录
            DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
            devopsCdStageRecordDTO.setPipelineRecordId(devopsCdPipelineRecordDTO.getId());
            // 找到stage 记录
            List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordMapper.select(devopsCdStageRecordDTO);
            if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
                for (DevopsCdStageRecordDTO cdStageRecordDTO : devopsCdStageRecordDTOS) {
                    //再查询stage下所有的job 记录
                    DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
                    devopsCdJobRecordDTO.setStageRecordId(cdStageRecordDTO.getId());
                    List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordMapper.select(devopsCdJobRecordDTO);
                    //job记录可以重试产生，所以再按照job名字分组计算最新的job记录
                    if (!CollectionUtils.isEmpty(devopsCdJobRecordDTOS)) {
                        Map<String, List<DevopsCdJobRecordDTO>> stringListMap = devopsCdJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCdJobRecordDTO::getName));
                        for (Map.Entry<String, List<DevopsCdJobRecordDTO>> stringListEntry : stringListMap.entrySet()) {
                            List<DevopsCdJobRecordDTO> value = stringListEntry.getValue();
                            List<DevopsCdJobRecordDTO> cdJobRecordDTOS = value.stream().sorted(Comparator.comparing(DevopsCdJobRecordDTO::getId).reversed()).collect(Collectors.toList());
                            //最新的一条cd Job的记录
                            DevopsCdJobRecordDTO cdJobRecordDTO = cdJobRecordDTOS.get(0);
                            totalSeconds += Optional.ofNullable(cdJobRecordDTO.getDurationSeconds()).orElseGet(() -> 0L);
                        }
                    }
                }
            }
        }
        return totalSeconds;
    }

    private String secondsToMinute(float totalSeconds) {
        //秒转换为分
        float num = totalSeconds / (60);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }

    private void fillPipelineFrequencyVO(List<Long> pipelineSuccessFrequency, List<Long> pipelineFailFrequency, List<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS1) {
        final Long[] successCount = {0L};
        final Long[] failCount = {0L};
        devopsPipelineRecordRelDTOS1.forEach(devopsPipelineRecordRelDTO -> {
            List<DevopsPipelineRecordVO> devopsPipelineRecordVOS = new ArrayList<>();
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(devopsPipelineRecordRelDTO.getCiPipelineRecordId());
            DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(devopsPipelineRecordRelDTO.getCdPipelineRecordId());
            if (!Objects.isNull(devopsCdPipelineRecordDTO)) {
                devopsPipelineRecordVOS.add(ConvertUtils.convertObject(devopsCdPipelineRecordDTO, DevopsCdPipelineRecordVO.class));
            }
            if (!Objects.isNull(devopsCiPipelineRecordDTO)) {
                devopsPipelineRecordVOS.add(ConvertUtils.convertObject(devopsCiPipelineRecordDTO, DevopsCiPipelineRecordVO.class));
            }
            if (!CollectionUtils.isEmpty(devopsPipelineRecordVOS)) {
                //包含失败就是失败 整条流水线失败
                List<DevopsPipelineRecordVO> failPipelineRecordVOS = devopsPipelineRecordVOS.stream().filter(devopsPipelineRecordVO -> StringUtils.equalsIgnoreCase(devopsPipelineRecordVO.getStatus(), PipelineStatus.FAILED.toValue())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(failPipelineRecordVOS)) {
                    failCount[0] += 1;
                }
                // 不包含非成功的  即所有状态都成功
                List<DevopsPipelineRecordVO> successPipelineRecordVOS = devopsPipelineRecordVOS.stream().filter(devopsPipelineRecordVO -> !StringUtils.equalsIgnoreCase(devopsPipelineRecordVO.getStatus(), PipelineStatus.SUCCESS.toValue())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(successPipelineRecordVOS)) {
                    successCount[0] += 1;
                }
            }
        });

        pipelineSuccessFrequency.add(successCount[0]);
        pipelineFailFrequency.add(failCount[0]);
    }

    private void deleteGitlabCiFile(Integer gitlabProjectId, String branch) {
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, branch, GitOpsConstants.GITLAB_CI_FILE_NAME);
        if (repositoryFile != null && repositoryFile.getFilePath() != null && repositoryFile.getContent() != null) {
            try {
                LOGGER.info("deleteGitlabCiFile: delete .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.deleteFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId(),
                        branch);
            } catch (Exception e) {
                throw new CommonException("error.delete.gitlab-ci.file", e);
            }

        }
    }

    private void deleteExternalGitlabCiFile(Integer gitlabProjectId, String branch, AppExternalConfigDTO appExternalConfigDTO) {
        RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(gitlabProjectId, branch, GitOpsConstants.GITLAB_CI_FILE_NAME);
        if (repositoryFile != null) {
            try {
                LOGGER.info("deleteGitlabCiFile: delete .gitlab-ci.yaml for gitlab project with id {}", gitlabProjectId);
                gitlabServiceClientOperator.deleteExternalFile(
                        gitlabProjectId,
                        GitOpsConstants.GITLAB_CI_FILE_NAME,
                        GitOpsConstants.CI_FILE_COMMIT_MESSAGE,
                        GitUserNameUtil.getAdminId(),
                        branch,
                        appExternalConfigDTO);
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
        checkResourceId(pipelineId, ciCdPipelineVO);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineVO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_UPDATE);
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineVO.getAppServiceId());
        // 校验自定义任务格式
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        // 没有指定基础镜像，则使用默认镜像
        if (StringUtils.isEmpty(ciCdPipelineVO.getImage())) {
            ciCdPipelineDTO.setImage(defaultCiImage);
        } else {
            ciCdPipelineDTO.setImage(ciCdPipelineVO.getImage());
        }
        ciCdPipelineDTO.setName(ciCdPipelineVO.getName());
        ciCdPipelineDTO.setVersionName(ciCdPipelineVO.getVersionName());
        ciCdPipelineDTO.setObjectVersionNumber(ciCdPipelineVO.getObjectVersionNumber());
        ciCdPipelineDTO.setVersionName(ciCdPipelineVO.getVersionName());
        if (ciCdPipelineMapper.updateByPrimaryKey(ciCdPipelineDTO) != 1) {
            throw new CommonException(UPDATE_PIPELINE_FAILED);
        }


        boolean initCiFileFlag = false;
        // 如果有ci阶段，需要判断是否是新增的ci阶段。如果是新增的需要初始化gitlab-ci.yaml
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            // 校验之前是否是纯cd流水线
            List<DevopsCiStageDTO> devopsCiStageDTOS = devopsCiStageService.listByPipelineId(pipelineId);
            // 之前不存在ci stage则证明之前是纯cd流水线
            initCiFileFlag = CollectionUtils.isEmpty(devopsCiStageDTOS);
        }

        // 更新流水线函数
        // 先删除之前的函数
        devopsCiPipelineFunctionService.deleteByPipelineId(pipelineId);
        // 保存新的函数
        List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOList = ciCdPipelineVO.getDevopsCiPipelineFunctionDTOList();
        if (!CollectionUtils.isEmpty(devopsCiPipelineFunctionDTOList)) {
            devopsCiPipelineFunctionDTOList.forEach(devopsCiPipelineFunctionDTO -> {
                devopsCiPipelineFunctionDTO.setId(null);
                devopsCiPipelineFunctionDTO.setDevopsPipelineId(pipelineId);
                devopsCiPipelineFunctionService.baseCreate(devopsCiPipelineFunctionDTO);
            });
        }
        // 更新流水线变量
        // 先删除之前的旧数据（考虑到数据不多，性能影响不大，没有必要做对比更新）
        devopsCiPipelineVariableService.deleteByPipelineId(pipelineId);
        saveCiVariable(ciCdPipelineVO, ciCdPipelineDTO);

        //更新CI流水线
        updateCiPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO, initCiFileFlag);
        //更新CD流水线
        updateCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    private void checkResourceId(Long pipelineId, CiCdPipelineVO ciCdPipelineVO) {
        if (ciCdPipelineVO.getId() != null && !pipelineId.equals(ciCdPipelineVO.getId())) {
            throw new CommonException("error.pipeline.id.invalid");
        }
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
                if (devopsCiStageVO.getCiPipelineId() != null && !devopsCiStageVO.getCiPipelineId().equals(pipelineId)) {
                    throw new CommonException("error.stage.pipeline.id.invalid");
                }
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        if (devopsCiJobVO.getCiPipelineId() != null && !devopsCiJobVO.getCiPipelineId().equals(pipelineId)) {
                            throw new CommonException("error.job.pipeline.id.invalid");
                        }
                    });
                }
            });
        }
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

    private void updateCiPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO, boolean initCiFileFlag) {
        // 更新stage
        // 查询数据库中原有stage列表,并和新的stage列表作比较。
        // 差集：要删除的记录
        // 交集：要更新的记录
        List<DevopsCiStageDTO> devopsCiStageDTOS = devopsCiStageService.listByPipelineId(ciCdPipelineDTO.getId());
        Set<Long> oldStageIds = devopsCiStageDTOS.stream().map(DevopsCiStageDTO::getId).collect(Collectors.toSet());

        Set<Long> updateIds = ciCdPipelineVO.getDevopsCiStageVOS().stream()
                .map(DevopsCiStageVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 去掉要更新的记录，剩下的为要删除的记录
        oldStageIds.removeAll(updateIds);
        oldStageIds.forEach(stageId -> {
            devopsCiStageService.deleteById(stageId);
            devopsCiJobService.deleteByStageIdCascade(stageId);
        });

        ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
            if (devopsCiStageVO.getId() != null) {
                // 更新
                devopsCiStageService.update(devopsCiStageVO);
                devopsCiJobService.deleteByStageIdCascade(devopsCiStageVO.getId());
                // 保存job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
//                        decryptCiBuildMetadata(devopsCiJobVO);
//                        processCiJobVO(devopsCiJobVO);
                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setId(null);
                        devopsCiJobDTO.setCiStageId(devopsCiStageVO.getId());
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobService.create(devopsCiJobDTO);

                        batchSaveStep(projectId, devopsCiJobDTO.getId(), devopsCiJobVO.getDevopsCiStepVOList());
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
//                        decryptCiBuildMetadata(devopsCiJobVO);
//                        processCiJobVO(devopsCiJobVO);

                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                        devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                        devopsCiJobService.create(devopsCiJobDTO);

                        batchSaveStep(projectId, devopsCiJobDTO.getId(), devopsCiJobVO.getDevopsCiStepVOList());
                    });
                }
            }
        });
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
//        saveCiContent(projectId, projectDTO.getOrganizationId(), ciCdPipelineDTO.getId(), ciCdPipelineVO);
//
        // 新增ci阶段，需要初始化gitlab-ci.yaml
        if (initCiFileFlag) {
            AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());
            String ciFileIncludeUrl = String.format(GitOpsConstants.CI_CONTENT_URL_TEMPLATE, gatewayUrl, projectId, ciCdPipelineDTO.getToken());
            List<DevopsPipelineBranchRelDTO> devopsPipelineBranchRelDTOS = listPipelineBranchRel(ciCdPipelineDTO.getId());
            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());

            devopsPipelineBranchRelDTOS.forEach(devopsPipelineBranchRelDTO -> {
                if (appExternalConfigDTO == null) {
                    initGitlabCiFile(appServiceDTO.getGitlabProjectId(), devopsPipelineBranchRelDTO.getBranch(), ciFileIncludeUrl);
                } else {
                    initExternalGitlabCiFile(appServiceDTO.getGitlabProjectId(), devopsPipelineBranchRelDTO.getBranch(), ciFileIncludeUrl, appExternalConfigDTO);
                }
            });

        }
    }

//    private void processCiJobVO(DevopsCiJobVO devopsCiJobVO) {
//        // 不让数据库存加密的值
//        if (JobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
//            // 将构建类型的stage中的job的每个step进行解析和转化
//            CiConfigVO ciConfigVO = JSONObject.parseObject(devopsCiJobVO.getMetadata(), CiConfigVO.class);
//            if (!CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
//                ciConfigVO.getConfig().forEach(c -> {
//                    if (!org.springframework.util.ObjectUtils.isEmpty(c.getScript())) {
//                        c.setScript(Base64Util.getBase64DecodedString(c.getScript()));
//                    }
//                });
//            }
//            devopsCiJobVO.setConfigVO(ciConfigVO);
//            devopsCiJobVO.setMetadata(JSONObject.toJSONString(ciConfigVO));
//        }
//    }

//    private void saveCiContent(final Long projectId, final Long organizationId, Long pipelineId, CiCdPipelineVO ciCdPipelineVO) {
//        GitlabCi gitlabCi = buildGitLabCiObject(projectId, organizationId, ciCdPipelineVO);
//        StringBuilder gitlabCiYaml = new StringBuilder(GitlabCiUtil.gitlabCi2yaml(gitlabCi));
//
//        // 拼接自定义job
//        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
//            List<DevopsCiJobVO> ciJobVOS = ciCdPipelineVO.getDevopsCiStageVOS().stream()
//                    .flatMap(v -> v.getJobList().stream()).filter(job -> JobTypeEnum.CUSTOM.value().equalsIgnoreCase(job.getType()))
//                    .collect(Collectors.toList());
//            if (!CollectionUtils.isEmpty(ciJobVOS)) {
//                for (DevopsCiJobVO job : ciJobVOS) {
//                    gitlabCiYaml.append(GitOpsConstants.NEW_LINE).append(job.getMetadata());
//                }
//            }
//
//        }
//
//        //保存gitlab-ci配置文件
//        DevopsCiContentDTO devopsCiContentDTO = new DevopsCiContentDTO();
//        devopsCiContentDTO.setCiPipelineId(pipelineId);
//        devopsCiContentDTO.setCiContentFile(gitlabCiYaml.toString());
//        devopsCiContentService.create(devopsCiContentDTO);
//    }

    /**
     * 构建gitlab-ci对象，用于转换为gitlab-ci.yaml
     *
     * @param CiCdPipelineDTO 流水线数据
     * @return 构建完的CI文件对象
     */
    private GitlabCi buildGitLabCiObject(CiCdPipelineDTO ciCdPipelineDTO) {

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(ciCdPipelineDTO.getProjectId());
        Long projectId = projectDTO.getId();
        Long organizationId = projectDTO.getOrganizationId();
        Long pipelineId = ciCdPipelineDTO.getId();

        List<DevopsCiStageDTO> devopsCiStageDTOS = devopsCiStageService.listByPipelineId(pipelineId);
        // 对阶段排序
        List<String> stages = devopsCiStageDTOS.stream()
                .sorted(Comparator.comparing(DevopsCiStageDTO::getSequence))
                .map(DevopsCiStageDTO::getName)
                .collect(Collectors.toList());

        GitlabCi gitlabCi = new GitlabCi();

        // 设置流水线变量
        List<DevopsCiPipelineVariableDTO> devopsCiPipelineVariableDTOS = devopsCiPipelineVariableService.listByPipelineId(pipelineId);
        if (!CollectionUtils.isEmpty(devopsCiPipelineVariableDTOS)) {
            Map<String, String> variables = devopsCiPipelineVariableDTOS.stream().collect(Collectors.toMap(DevopsCiPipelineVariableDTO::getVariableKey, DevopsCiPipelineVariableDTO::getVariableValue));
            gitlabCi.setVariables(variables);
        }

        // 如果用户指定了就使用用户指定的，如果没有指定就使用默认的猪齿鱼提供的镜像
        gitlabCi.setImage(StringUtils.isEmpty(ciCdPipelineDTO.getImage()) ? defaultCiImage : ciCdPipelineDTO.getImage());

        gitlabCi.setStages(stages);
        devopsCiStageDTOS.forEach(stageVO -> {
            List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByStageId(stageVO.getId());
            if (!CollectionUtils.isEmpty(devopsCiJobDTOS)) {
                devopsCiJobDTOS.forEach(job -> {
                    if (CiJobTypeEnum.CUSTOM.value().equals(job.getType())) {
                        return;
                    }
                    CiJob ciJob = new CiJob();
                    if (!StringUtils.isEmpty(job.getImage())) {
                        ciJob.setImage(job.getImage());
                    }
                    ciJob.setStage(stageVO.getName());
                    ciJob.setParallel(job.getParallel());

                    //增加services
                    List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobId(job.getId());
                    if (devopsCiStepDTOS.stream().anyMatch(v -> DevopsCiStepTypeEnum.DOCKER_BUILD.value().equals(v.getType()))) {
                        CiJobServices ciJobServices = new CiJobServices();
                        ciJobServices.setName(defaultCiImage);
                        ciJobServices.setAlias("kaniko");
                        ciJob.setServices(ArrayUtil.singleAsList(ciJobServices));
                    }

                    ciJob.setCache(buildJobCache(job));
                    processOnlyAndExcept(job, ciJob);

                    ciJob.setScript(buildScript(Objects.requireNonNull(organizationId), projectId, job));

                    gitlabCi.addJob(job.getName(), ciJob);
                });
            }
        });
        buildBeforeScript(gitlabCi, ciCdPipelineDTO.getVersionName());
        return gitlabCi;
    }

//    /**
//     * 构建gitlab-ci对象，用于转换为gitlab-ci.yaml
//     *
//     * @param projectId      项目id
//     * @param ciCdPipelineVO 流水线数据
//     * @return 构建完的CI文件对象
//     */
//    private GitlabCi buildGitLabCiObject(final Long projectId, final Long organizationId, CiCdPipelineVO ciCdPipelineVO) {
//        // 对阶段排序
//        List<String> stages = ciCdPipelineVO.getDevopsCiStageVOS().stream()
//                .sorted(Comparator.comparing(DevopsCiStageVO::getSequence))
//                .map(DevopsCiStageVO::getName)
//                .collect(Collectors.toList());
//
//        GitlabCi gitlabCi = new GitlabCi();
//
//        // 如果用户指定了就使用用户指定的，如果没有指定就使用默认的猪齿鱼提供的镜像
//        gitlabCi.setImage(ObjectUtils.isEmpty(ciCdPipelineVO.getImage()) ? defaultCiImage : ciCdPipelineVO.getImage());
//
//        gitlabCi.setStages(stages);
//        ciCdPipelineVO.getDevopsCiStageVOS().forEach(stageVO -> {
//            if (!CollectionUtils.isEmpty(stageVO.getJobList())) {
//                stageVO.getJobList().forEach(job -> {
//                    if (CiJobTypeEnum.CUSTOM.value().equals(job.getType())) {
//                        return;
//                    }
//                    CiJob ciJob = new CiJob();
//                    if (StringUtils.isNoneBlank(job.getImage())) {
//                        ciJob.setImage(job.getImage());
//                    }
//                    ciJob.setStage(stageVO.getName());
//                    ciJob.setParallel(job.getParallel());
//                    //增加afterScript
////                    ciJob.setAfterScript(buildAfterScript(job));
//                    //增加services
//                    CiJobServices ciJobServices = buildServices(job);
//                    ciJob.setServices(Objects.isNull(ciJobServices) ? null : ArrayUtil.singleAsList(ciJobServices));
//                    ciJob.setScript(buildScript(Objects.requireNonNull(organizationId), projectId, job));
//                    ciJob.setCache(buildJobCache(job));
//                    processOnlyAndExcept(job, ciJob);
//                    gitlabCi.addJob(job.getName(), ciJob);
//                });
//            }
//        });
//        buildBeforeScript(gitlabCi, ciCdPipelineVO.getVersionName());
//        return gitlabCi;
//    }

//    private List<String> buildAfterScript(DevopsCiJobVO jobVO) {
//        List<String> afterScript = new ArrayList<>();
//        if (isContainDokcerBuild(jobVO)) {
//            afterScript.add("rm -rf /${CI_PROJECT_NAMESPACE}-${CI_PROJECT_NAME}-${CI_COMMIT_SHA}/${PROJECT_NAME}.tar");
//            return afterScript;
//        } else {
//            return null;
//        }
//    }


//    private CiJobServices buildServices(DevopsCiJobVO jobVO) {
//        CiJobServices ciJobServices = new CiJobServices();
//        if (isContainDokcerBuild(jobVO)) {
//            ciJobServices.setName(defaultCiImage);
//            ciJobServices.setAlias("kaniko");
//            return ciJobServices;
//        } else {
//            return null;
//        }
//    }

//    private boolean isContainDokcerBuild(DevopsCiJobVO jobVO) {
//        if (Objects.isNull(jobVO)) {
//            return false;
//        }
//        if (JobTypeEnum.BUILD.value().equals(jobVO.getType())) {
//            CiConfigVO ciConfigVO = jobVO.getConfigVO();
//            if (ciConfigVO == null || CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
//                return false;
//            }
//            if (!CollectionUtils.isEmpty(ciConfigVO.getConfig().stream().filter(ciConfigTemplateVO -> StringUtils.equalsIgnoreCase(ciConfigTemplateVO.getType().trim(), CiJobScriptTypeEnum.DOCKER.getType())).collect(Collectors.toList()))) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * 处理job的触发方式
     *
     * @param devopsCiJobDTO job元数据
     * @param ciJob          ci文件的job对象
     */
    private void processOnlyAndExcept(DevopsCiJobDTO devopsCiJobDTO, CiJob ciJob) {
        if (StringUtils.isNotBlank(devopsCiJobDTO.getTriggerType())
                && StringUtils.isNotBlank(devopsCiJobDTO.getTriggerValue())) {
            CiTriggerType ciTriggerType = CiTriggerType.forValue(devopsCiJobDTO.getTriggerType());
            if (ciTriggerType != null) {
                String triggerValue = devopsCiJobDTO.getTriggerValue();
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
            // group 类型的仓库没有版本类型
            mavenRepoVO.setType(nexusMavenRepoDTO.getVersionPolicy() == null ? null : nexusMavenRepoDTO.getVersionPolicy().toLowerCase());
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
     * @param devopsCiJobDTO          生成脚本
     * @return 生成的脚本列表
     */
    private List<String> buildScript(final Long organizationId, final Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        Assert.notNull(devopsCiJobDTO, "Job can't be null");
        Assert.notNull(organizationId, "Organization id can't be null");
        Assert.notNull(projectId, "project id can't be null");
        final Long jobId = devopsCiJobDTO.getId();
        Assert.notNull(jobId, "Ci job id is required.");

        List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobId(jobId);

        if (CollectionUtils.isEmpty(devopsCiStepDTOS)) {
            return null;
        }
        // 最后生成的所有script集合
        List<String> result = new ArrayList<>();
        devopsCiStepDTOS
                .stream()
                .sorted(Comparator.comparingLong(DevopsCiStepDTO::getSequence))
                .forEach(devopsCiStepDTO -> {
                    AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandlerOrThrowE(devopsCiStepDTO.getType());
                    result.addAll(handler.buildGitlabCiScript(devopsCiStepDTO));
                });
        return result;
    }

//    /**
//     * 把配置转换为gitlab-ci配置（maven,sonarqube）
//     *
//     * @param organizationId 组织id
//     * @param projectId      项目id
//     * @param jobVO          生成脚本
//     * @return 生成的脚本列表
//     */
//    private List<String> buildScript(final Long organizationId, final Long projectId, DevopsCiJobVO jobVO) {
//        Assert.notNull(jobVO, "Job can't be null");
//        Assert.notNull(organizationId, "Organization id can't be null");
//        Assert.notNull(projectId, "project id can't be null");
//        final Long jobId = jobVO.getId();
//        Assert.notNull(jobId, "Ci job id is required.");
//
//        if (JobTypeEnum.SONAR.value().equals(jobVO.getType())) {
//            return calculateSonarScript(jobVO);
//        } else if (JobTypeEnum.BUILD.value().equals(jobVO.getType())) {
//            // 将构建类型的stage中的job的每个step进行解析和转化
//            CiConfigVO ciConfigVO = jobVO.getConfigVO();
//            if (ciConfigVO == null || CollectionUtils.isEmpty(ciConfigVO.getConfig())) {
//                return Collections.emptyList();
//            }
//
//            List<Long> existedSequences = new ArrayList<>();
//            // 校验前端传入的sequence不为null且不重复
//            ciConfigVO.getConfig().forEach(config -> DevopsCiPipelineAdditionalValidator.validConfigSequence(config.getSequence(), config.getName(), existedSequences));
//
//            // 最后生成的所有script集合
//            List<String> result = new ArrayList<>();
//
//            // 同一个job中的所有step要按照sequence顺序来
//            // 将每一个step都转为一个List<String>并将所有的list合并为一个
//            ciConfigVO.getConfig()
//                    .stream()
//                    .sorted(Comparator.comparingLong(CiConfigTemplateVO::getSequence))
//                    .forEach(config -> {
//                        CiJobScriptTypeEnum type = CiJobScriptTypeEnum.forType(config.getType().toLowerCase());
//                        if (type == null) {
//                            throw new CommonException(ERROR_UNSUPPORTED_STEP_TYPE, config.getType());
//                        }
//
//                        switch (type) {
//                            // GO和NPM是一样处理
//                            case NPM:
//                                result.addAll(GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(config.getScript()), true, true));
//                                break;
//                            case MAVEN:
//                                // 处理settings文件
//                                DevopsCiPipelineAdditionalValidator.validateMavenStep(config);
//                                boolean hasSettings = buildAndSaveMavenSettings(projectId, jobId, config);
//                                result.addAll(buildMavenScripts(projectId, jobId, config, hasSettings));
//                                break;
//                            case DOCKER:
//                                // 不填skipDockerTlsVerify参数或者填TRUE都是跳过证书校验
//                                // TODO 修复 目前后端这个参数的含义是是否跳过证书校验, 前端的含义是是否进行证书校验
//                                Boolean doTlsVerify = config.getSkipDockerTlsVerify();
//                                //是否开启镜像扫描 默认是关闭镜像扫描的
//                                Boolean imageScan = config.getImageScan();
//                                result.addAll(GitlabCiUtil.generateDockerScripts(
//                                        config.getDockerContextDir(),
//                                        config.getDockerFilePath(),
//                                        doTlsVerify == null || !doTlsVerify,
//                                        Objects.isNull(imageScan) ? false : imageScan, jobVO.getId()));
//                                break;
//                            // 上传JAR包阶段是没有选择项目依赖的, 同样也可以复用maven deploy的逻辑
//                            case UPLOAD_JAR:
//                            case MAVEN_DEPLOY:
//                                List<MavenRepoVO> targetRepos = new ArrayList<>();
//                                boolean hasMavenSettings = buildAndSaveJarDeployMavenSettings(projectId, jobId, config, targetRepos);
//                                result.addAll(buildMavenJarDeployScripts(projectId, jobId, hasMavenSettings, config, targetRepos));
//                                break;
//                            default:
//                        }
//                    });
//
//            return result;
//        } else if (JobTypeEnum.CHART.value().equals(jobVO.getType())) {
//            // 生成chart步骤
//            return ArrayUtil.singleAsList(GitlabCiUtil.generateChartBuildScripts());
//        }
//        return Collections.emptyList();
//    }

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
    @Nullable
    private Cache buildJobCache(DevopsCiJobDTO jobConfig) {
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
        if (!CollectionUtils.isEmpty(gitlabCi.getJobs())
                && gitlabCi.getJobs().values().stream().anyMatch(j -> j.getCache() != null)) {
            beforeScripts.add(GitlabCiUtil.generateCreateCacheDir(GitOpsConstants.CHOERODON_CI_CACHE_DIR));
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
                if (repoIdIndex == -1 && templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_ID)) {
                    repoIdIndex = i;
                }
                if (repoUrlIndex == -1 && templateShells.get(i).contains(GitOpsConstants.CHOERODON_MAVEN_REPO_URL)) {
                    repoUrlIndex = i;
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
            //加上jobId  与sequence，用于查询jar包的时间戳
            shells.add(GitlabCiUtil.saveJarMetadata(ciConfigTemplateVO.getMavenDeployRepoSettings().getNexusRepoIds(), jobId, ciConfigTemplateVO.getSequence()));
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
            // 去重
            Set<Long> userIds = new HashSet<>(cdAuditUserIds);
            userIds.forEach(t -> {
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
        if (JobTypeEnum.CD_DEPLOY.value().equals(t.getType())
                || JobTypeEnum.CD_DEPLOYMENT.value().equals(t.getType())) {
            // 使用能够解密主键加密的json工具解密
            DevopsDeployInfoVO devopsDeployInfoVO = KeyDecryptHelper.decryptJson(devopsCdJobDTO.getMetadata(), DevopsDeployInfoVO.class);
            DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = ConvertUtils.convertObject(devopsDeployInfoVO, DevopsCdEnvDeployInfoDTO.class);
            if (devopsDeployInfoVO.getAppConfig() != null) {
                devopsCdEnvDeployInfoDTO.setAppConfigJson(JsonHelper.marshalByJackson(devopsDeployInfoVO.getAppConfig()));
            }
            if (devopsDeployInfoVO.getContainerConfig() != null) {
                devopsCdEnvDeployInfoDTO.setContainerConfigJson(JsonHelper.marshalByJackson(devopsDeployInfoVO.getContainerConfig()));
            }

            String rdupmType = JobTypeEnum.CD_DEPLOY.value().equals(t.getType()) ? RdupmTypeEnum.CHART.value() : RdupmTypeEnum.DEPLOYMENT.value();
            if (DeployTypeEnum.CREATE.value().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
                // 校验应用编码和应用名称
                devopsDeployAppCenterService.checkNameAndCodeUniqueAndThrow(devopsCdEnvDeployInfoDTO.getEnvId(), rdupmType, null, devopsCdEnvDeployInfoDTO.getAppName(), devopsCdEnvDeployInfoDTO.getAppCode());
            }

            // 使用不进行主键加密的json工具再将json写入类, 用于在数据库存非加密数据
            devopsCdJobDTO.setMetadata(JsonHelper.marshalByJackson(devopsDeployInfoVO));
            devopsCdEnvDeployInfoService.save(devopsCdEnvDeployInfoDTO);
            devopsCdJobDTO.setDeployInfoId(devopsCdEnvDeployInfoDTO.getId());

        } else if (JobTypeEnum.CD_HOST.value().equals(t.getType())) {
            // 使用能够解密主键加密的json工具解密
            CdHostDeployConfigVO cdHostDeployConfigVO = KeyDecryptHelper.decryptJson(devopsCdJobDTO.getMetadata(), CdHostDeployConfigVO.class);
            checkCdHostJobName(pipelineId, cdHostDeployConfigVO, t.getName(), devopsCdJobDTO);
            // 使用不进行主键加密的json工具再将json写入类, 用于在数据库存非加密数据
            DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO = ConvertUtils.convertObject(cdHostDeployConfigVO, DevopsCdHostDeployInfoDTO.class);
            if (cdHostDeployConfigVO.getJarDeploy() != null) {
                devopsCdHostDeployInfoDTO.setJarDeployJson(JsonHelper.marshalByJackson(cdHostDeployConfigVO.getJarDeploy()));
            }

            if (DeployTypeEnum.CREATE.value().equals(devopsCdHostDeployInfoDTO.getDeployType())) {
                // 校验应用编码和应用名称
                devopsHostAppService.checkNameAndCodeUniqueAndThrow(projectId, null, devopsCdHostDeployInfoDTO.getAppName(), devopsCdHostDeployInfoDTO.getAppCode());
            }

            devopsCdJobDTO.setDeployInfoId(devopsCdHostDeployInfoService.baseCreate(devopsCdHostDeployInfoDTO).getId());
            devopsCdJobDTO.setMetadata(JsonHelper.marshalByJackson(cdHostDeployConfigVO));

        } else if (JobTypeEnum.CD_AUDIT.value().equals(t.getType())) {
            // 如果审核任务，审核人员只有一个人，则默认设置为或签
            if (CollectionUtils.isEmpty(t.getCdAuditUserIds())) {
                throw new CommonException(ResourceCheckConstant.ERROR_PARAM_IS_INVALID);
            }
            if (t.getCdAuditUserIds().size() == 1) {
                devopsCdJobDTO.setCountersigned(1);
            }
        } else if (JobTypeEnum.CD_API_TEST.value().equals(t.getType())) {
            // 使用能够解密主键加密的json工具解密
            CdApiTestConfigVO cdApiTestConfigVO = KeyDecryptHelper.decryptJson(devopsCdJobDTO.getMetadata(), CdApiTestConfigVO.class);
            // 使用不进行主键加密的json工具再将json写入类, 用于在数据库存非加密数据
            devopsCdJobDTO.setMetadata(JsonHelper.marshalByJackson(cdApiTestConfigVO));

            DevopsCdApiTestInfoDTO devopsCdApiTestInfoDTO = ConvertUtils.convertObject(cdApiTestConfigVO, DevopsCdApiTestInfoDTO.class);

            WarningSettingVO warningSettingVO = cdApiTestConfigVO.getWarningSettingVO();
            if (warningSettingVO != null) {
                devopsCdApiTestInfoDTO.setEnableWarningSetting(warningSettingVO.getEnableWarningSetting());
                devopsCdApiTestInfoDTO.setBlockAfterJob(warningSettingVO.getBlockAfterJob());
                devopsCdApiTestInfoDTO.setSendEmail(warningSettingVO.getSendEmail());
                devopsCdApiTestInfoDTO.setPerformThreshold(warningSettingVO.getPerformThreshold());

                if (!CollectionUtils.isEmpty(warningSettingVO.getNotifyUserIds())) {
                    devopsCdApiTestInfoDTO.setNotifyUserIds(JsonHelper.marshalByJackson(warningSettingVO.getNotifyUserIds()));
                }
            }

            devopsCdApiTestInfoService.baseCreate(devopsCdApiTestInfoDTO);
            devopsCdJobDTO.setDeployInfoId(devopsCdApiTestInfoDTO.getId());
        } else if (JobTypeEnum.CD_EXTERNAL_APPROVAL.value().equals(t.getType())) {
            // 后续如果需要对外部卡点任务处理逻辑可以写这里
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

    public void checkCdHostJobName(Long ciPipelineId, CdHostDeployConfigVO deployConfigVO, String cdHostName, DevopsCdJobDTO devopsCdJobDTO) {
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
//        if (deployConfigVO.getImageDeploy() != null
//                && deployConfigVO.getImageDeploy().getDeploySource().equals(HostDeploySource.PIPELINE_DEPLOY.getValue())) {
//            devopsCiJobDTO.setName(deployConfigVO.getImageDeploy().getPipelineTask());
//        }
        if (deployConfigVO.getJarDeploy() != null
                && deployConfigVO.getJarDeploy().getDeploySource().equals(HostDeploySource.PIPELINE_DEPLOY.getValue())) {
            devopsCiJobDTO.setName(deployConfigVO.getJarDeploy().getPipelineTask());
        }
        if (!StringUtils.isEmpty(devopsCiJobDTO.getName())) {
            DevopsCiJobDTO ciJobDTO = devopsCiJobMapper.selectOne(devopsCiJobDTO);
            if (ciJobDTO == null) {
                throw new CommonException("error.cd.host.job.union.ci.job", devopsCiJobDTO.getName(), cdHostName);
            }
            if (!ciJobDTO.getTriggerType().equals(devopsCdJobDTO.getTriggerType())) {
                throw new CommonException("error.ci.cd.trigger.type.invalid");
            }
        }
    }

//    /**
//     * 将job中的metadata字段解密
//     *
//     * @param devopsCiJobVO job数据
//     */
//    private void decryptCiBuildMetadata(DevopsCiJobVO devopsCiJobVO) {
//        if (JobTypeEnum.BUILD.value().equals(devopsCiJobVO.getType())) {
//            // 解密json字符串中的加密的主键
//            devopsCiJobVO.setMetadata(JsonHelper.marshalByJackson(KeyDecryptHelper.decryptJson(devopsCiJobVO.getMetadata(), CiConfigVO.class)));
//        }
//    }
}
