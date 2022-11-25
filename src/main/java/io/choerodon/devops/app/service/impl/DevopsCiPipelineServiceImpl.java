package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_YAML_FORMAT_INVALID;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.*;
import static io.choerodon.devops.infra.constant.PipelineConstants.*;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.*;
import static io.choerodon.devops.infra.enums.CiJobTypeEnum.API_TEST;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.ImmutableProjectInfoVO;
import io.choerodon.devops.api.vo.pipeline.*;
import io.choerodon.devops.app.eventhandler.pipeline.job.AbstractJobHandler;
import io.choerodon.devops.app.eventhandler.pipeline.job.JobOperator;
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
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.sonar.SonarScannerType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
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
    private static final String DEVOPS_CREATE_PIPELINE_FAILED = "devops.create.pipeline.failed";
    private static final String DEVOPS_UPDATE_PIPELINE_FAILED = "devops.update.pipeline.failed";
    private static final String DEVOPS_DISABLE_PIPELINE_FAILED = "devops.disable.pipeline.failed";
    private static final String DEVOPS_ENABLE_PIPELINE_FAILED = "devops.enable.pipeline.failed";
    private static final String DEVOPSDELETE_PIPELINE_FAILED = "devopsdelete.pipeline.failed";
    private static final String DEVOPS_BRANCH_PERMISSION_MISMATCH = "devops.branch.permission.mismatch";

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    @Value("${services.test.runner-image}")
    private String testRunnerImage;
    @Autowired
    private DevopsCiSonarConfigService devopsCiSonarConfigService;

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
    private final DevopsProjectService devopsProjectService;
    private final BaseServiceClientOperator baseServiceClientOperator;
    private final CheckGitlabAccessLevelService checkGitlabAccessLevelService;
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
    private DevopsImageScanResultService devopsImageScanResultService;
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
    @Autowired
    private CiDockerAuthConfigService ciDockerAuthConfigService;
    @Autowired
    private CiPipelineScheduleService ciPipelineScheduleService;
    @Autowired
    private DevopsCiPipelineChartService devopsCiPipelineChartService;
    @Autowired
    private CiPipelineMavenService ciPipelineMavenService;
    @Autowired
    private CiPipelineImageService ciPipelineImageService;
    @Autowired
    private CiPipelineAppVersionService ciPipelineAppVersionService;
    @Autowired
    private DevopsHostService devopsHostService;
    @Autowired
    private JobOperator jobOperator;
    @Autowired
    @Lazy
    private CiAuditRecordService ciAuditRecordService;
    @Autowired
    @Lazy
    private CiAuditUserRecordService ciAuditUserRecordService;


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
            DevopsProjectService devopsProjectService,
            BaseServiceClientOperator baseServiceClientOperator,
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
            DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper
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
        this.baseServiceClientOperator = baseServiceClientOperator;
        this.devopsProjectService = devopsProjectService;
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
        this.devopsCiJobMapper = devopsCiJobMapper;
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
                throw new CommonException(DEVOPS_CREATE_OR_UPDATE_GITLAB_CI, ex, branch);
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
                throw new CommonException(DEVOPS_CREATE_OR_UPDATE_GITLAB_CI, ex, branch);
            }
        }
    }

    private void initGitlabCiFileIfEmpty(Integer gitlabProjectId, String branch, String ciFileIncludeUrl) {
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
                throw new CommonException(DEVOPS_CREATE_OR_UPDATE_GITLAB_CI, ex, branch);
            }

        }
    }

    private void initExternalGitlabCiFileIfEmpty(Integer gitlabProjectId, String branch, String ciFileIncludeUrl, AppExternalConfigDTO appExternalConfigDTO) {
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
                throw new CommonException(DEVOPS_CREATE_OR_UPDATE_GITLAB_CI, ex, branch);
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
                throw new CommonException(DEVOPS_CREATE_OR_UPDATE_GITLAB_CI, ex, branch);
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
                throw new CommonException(DEVOPS_CREATE_OR_UPDATE_GITLAB_CI, ex, branch);
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
            throw new CommonException(DEVOPS_CREATE_PIPELINE_FAILED);
        }
        // 保存流水线分支关系
        saveBranchRel(ciCdPipelineVO, ciCdPipelineDTO);
        // 保存流水线函数
        saveFunction(ciCdPipelineVO, ciCdPipelineDTO);
        // 保存流水线变量
        saveCiVariable(ciCdPipelineVO, ciCdPipelineDTO);
        // 保存Docker认证配置
        saveCiDockerAuthConfig(ciCdPipelineVO, ciCdPipelineDTO);

        // 1.保存ci stage信息
        saveCiPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
        // 2.保存cd stage信息
//        saveCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);

        // 3. 为gitlab-ci文件添加include指令
        // 生成gitlab-ci.yaml文件，（避免第一次执行没有保存mavenSettings文件）
        devopsCiContentService.queryLatestContent(ciCdPipelineDTO.getToken());

        initGitlabCi(projectId, ciCdPipelineVO, ciCdPipelineDTO);

        return ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineDTO.getId());
    }

    private void saveCiDockerAuthConfig(CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        List<CiDockerAuthConfigDTO> ciDockerAuthConfigDTOList = ciCdPipelineVO.getCiDockerAuthConfigDTOList();
        if (!CollectionUtils.isEmpty(ciDockerAuthConfigDTOList)) {
            ciDockerAuthConfigDTOList.forEach(v -> {
                if (StringUtils.isEmpty(v.getDomain())
                        || StringUtils.isEmpty(v.getUsername())
                        || StringUtils.isEmpty(v.getPassword())) {
                    throw new CommonException(DEVOPS_DOCKER_AUTH_CONFIG_INVALID);
                }
                v.setId(null);
                v.setDevopsPipelineId(ciCdPipelineDTO.getId());
                ciDockerAuthConfigService.baseCreate(v);
            });
        }
    }

    private void initGitlabCi(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineVO.getAppServiceId());
        String ciFileIncludeUrl = String.format(GitOpsConstants.CI_CONTENT_URL_TEMPLATE, gatewayUrl, projectId, ciCdPipelineDTO.getToken());
        boolean forceFlushCiFile = !CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS());
        if (appServiceDTO.getExternalConfigId() != null) {
            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
            ciCdPipelineVO.getRelatedBranches().forEach(branch -> {
                if (forceFlushCiFile) {
                    initExternalGitlabCiFile(appServiceDTO.getGitlabProjectId(), branch, ciFileIncludeUrl, appExternalConfigDTO);
                } else {
                    initExternalGitlabCiFileIfEmpty(appServiceDTO.getGitlabProjectId(), branch, ciFileIncludeUrl, appExternalConfigDTO);
                }
            });
        } else {
            ciCdPipelineVO.getRelatedBranches().forEach(branch -> {
                if (forceFlushCiFile) {
                    initGitlabCiFile(appServiceDTO.getGitlabProjectId(), branch, ciFileIncludeUrl);
                } else {
                    initGitlabCiFileIfEmpty(appServiceDTO.getGitlabProjectId(), branch, ciFileIncludeUrl);
                }
            });

        }

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
            MapperUtil.resultJudgedInsertSelective(devopsPipelineBranchRelMapper, devopsPipelineBranchRelDTO, DEVOPS_SAVE_PIPELINE_BRANCH_REL);
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

        List<DevopsCiJobVO> devopsCiCustomJobDTOList = devopsCiJobService.listCustomByPipelineId(pipelineId);
        // 拼接自定义job
        if (!CollectionUtils.isEmpty(devopsCiCustomJobDTOList)) {
            for (DevopsCiJobVO job : devopsCiCustomJobDTOList) {
                gitlabCiYaml.append(GitOpsConstants.NEW_LINE).append(replaceStageName(job));
            }
        }
        return gitlabCiYaml.toString();
    }

    private String replaceStageName(DevopsCiJobVO job) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        Object data = yaml.load(job.getScript());
        JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
        try {

            Iterator<Object> iterator = jsonObject.values().iterator();
            Map<String, Object> value = (Map<String, Object>) iterator.next();
            value.replace("stage", job.getStageName());
            return yaml.dump(jsonObject);

        } catch (Exception e) {
            throw new CommonException(DEVOPS_YAML_FORMAT_INVALID, e);
        }
    }

//    private void saveCdPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
//        //2.保存cd stage 的信息
//        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCdStageVOS())) {
//            ciCdPipelineVO.getDevopsCdStageVOS().forEach(devopsCdStageVO -> {
//                DevopsCdStageDTO devopsCdStageDTO = ConvertUtils.convertObject(devopsCdStageVO, DevopsCdStageDTO.class);
//                devopsCdStageDTO.setPipelineId(ciCdPipelineDTO.getId());
//                devopsCdStageDTO.setProjectId(projectId);
//                devopsCdStageService.create(devopsCdStageDTO);
//                // 保存cd job信息
//                if (!CollectionUtils.isEmpty(devopsCdStageVO.getJobList())) {
//                    devopsCdStageVO.getJobList().forEach(devopsCdJobVO -> {
//                        // 添加人工卡点的任务类型时才 保存审核人员信息
//                        createCdJob(devopsCdJobVO, projectId, devopsCdStageDTO.getId(), ciCdPipelineDTO.getId());
//                    });
//                }
//            });
//        }
//    }

    private void saveCiPipeline(Long projectId, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineDTO ciCdPipelineDTO) {
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                devopsCiStageDTO.setCiPipelineId(ciCdPipelineDTO.getId());
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存ci job信息
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        // 保存任务信息
                        AbstractJobHandler handler = jobOperator.getHandlerOrThrowE(devopsCiJobVO.getType());
                        DevopsCiJobDTO devopsCiJobDTO = handler.saveJobInfo(projectId, ciCdPipelineDTO.getId(), savedDevopsCiStageDTO.getId(), devopsCiJobVO);

                        // 保存任务中的步骤信息
                        batchSaveStep(projectId, devopsCiJobDTO.getId(), devopsCiJobVO.getDevopsCiStepVOList());

                    });
                }
            });
        }
    }

    /**
     * 保存步骤的配置信息
     *
     * @param projectId
     * @param jobId
     * @param devopsCiStepVOList
     */
    private void batchSaveStep(Long projectId, Long jobId, List<DevopsCiStepVO> devopsCiStepVOList) {
        if (CollectionUtils.isEmpty(devopsCiStepVOList)) {
            return;
        }
        devopsCiStepVOList.forEach(devopsCiStepVO -> {
            AbstractDevopsCiStepHandler devopsCiStepHandler = devopsCiStepOperator.getHandlerOrThrowE(devopsCiStepVO.getType());
            if (Boolean.FALSE.equals(devopsCiStepHandler.isComplete(devopsCiStepVO))) {
                throw new CommonException(DEVOPS_STEP_NOT_COMPLETE, devopsCiStepVO.getName());
            }
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
//        //查询CD相关的阶段以及JOB
//        List<DevopsCdStageVO> devopsCdStageVOS = handleCdStage(pipelineId);
        //封装流水线
        ciCdPipelineVO.setDevopsCiStageVOS(devopsCiStageVOS);
//        ciCdPipelineVO.setDevopsCdStageVOS(devopsCdStageVOS);

        // 添加是否开启执行计划
        List<CiPipelineScheduleVO> ciPipelineScheduleVOS = ciPipelineScheduleService.listByAppServiceId(projectId, appServiceDTO.getId());
        if (!CollectionUtils.isEmpty(ciPipelineScheduleVOS)
                && ciPipelineScheduleVOS.stream().anyMatch(v -> Boolean.TRUE.equals(v.getActive()))) {
            ciCdPipelineVO.setEnableSchedule(true);
        } else {
            ciCdPipelineVO.setEnableSchedule(false);
        }
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

    protected void handCdHost(DevopsCdJobVO devopsCdJobVO) {
        // 加密json中主键
        DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO = devopsCdHostDeployInfoService.queryById(devopsCdJobVO.getDeployInfoId());
        CdHostDeployConfigVO cdHostDeployConfigVO = ConvertUtils.convertObject(devopsCdHostDeployInfoDTO, CdHostDeployConfigVO.class);
        if (devopsCdHostDeployInfoDTO.getDeployJson() != null && !StringUtils.equals(cdHostDeployConfigVO.getHostDeployType(), RdupmTypeEnum.DOCKER.value())) {
            cdHostDeployConfigVO.setJarDeploy(JsonHelper.unmarshalByJackson(devopsCdHostDeployInfoDTO.getDeployJson(), CdHostDeployConfigVO.JarDeploy.class));
        }
        if (devopsCdHostDeployInfoDTO.getDeployJson() != null && StringUtils.equals(cdHostDeployConfigVO.getHostDeployType(), RdupmTypeEnum.DOCKER.value())) {
            cdHostDeployConfigVO.setImageDeploy(JsonHelper.unmarshalByJackson(devopsCdHostDeployInfoDTO.getDeployJson(), CdHostDeployConfigVO.ImageDeploy.class));
            cdHostDeployConfigVO.setDockerCommand(cdHostDeployConfigVO.getDockerCommand());
        }
        devopsCdJobVO.setEdit(devopsHostUserPermissionService.checkUserOwnUsePermission(devopsCdJobVO.getProjectId(), devopsCdHostDeployInfoDTO.getHostId(), DetailsHelper.getUserDetails().getUserId()));
        cdHostDeployConfigVO.setDevopsHostDTO(devopsHostService.baseQuery(devopsCdHostDeployInfoDTO.getHostId()));

        devopsCdJobVO.setMetadata(JsonHelper.singleQuoteWrapped(KeyDecryptHelper.encryptJson(cdHostDeployConfigVO)));
    }

    protected void handleCdDeploy(DevopsCdJobVO devopsCdJobVO) {
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
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsCdJobVO.getProjectId());
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
        // 封装CI对象
        List<Long> jobIds = devopsCiJobVOS.stream().map(DevopsCiJobVO::getId).collect(Collectors.toList());
        List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobIds(jobIds);
        Map<Long, List<DevopsCiStepDTO>> jobStepMap = devopsCiStepDTOS.stream().collect(Collectors.groupingBy(DevopsCiStepDTO::getDevopsCiJobId));

        devopsCiJobVOS.forEach(devopsCiJobVO -> {
            AbstractJobHandler jobHandler = jobOperator.getHandler(devopsCiJobVO.getType());
            jobHandler.fillJobConfigInfo(devopsCiJobVO);
            jobHandler.fillJobAdditionalInfo(devopsCiJobVO);

            List<DevopsCiStepDTO> ciStepDTOS = jobStepMap.get(devopsCiJobVO.getId());
            if (!CollectionUtils.isEmpty(ciStepDTOS)) {
                List<DevopsCiStepVO> devopsCiStepVOList = ConvertUtils.convertList(ciStepDTOS, DevopsCiStepVO.class);
                devopsCiStepVOList.forEach(ciStepVO -> {
                    AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(ciStepVO.getType());
                    handler.fillStepConfigInfo(ciStepVO);
                });
                devopsCiJobVO.setDevopsCiStepVOList(devopsCiStepVOList);
            }

        });
        return devopsCiJobVOS.stream().collect(Collectors.groupingBy(DevopsCiJobVO::getCiStageId));
    }

    protected void fillEditPipelinePermission(Long projectId, CiCdPipelineVO ciCdPipelineVO, AppServiceDTO appServiceDTO) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
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
            throw new CommonException(ExceptionConstants.AppServiceCode.DEVOPS_APP_SERVICE_NOT_EXIST);
        }
        ciCdPipelineVO.setAppServiceCode(appServiceDTO.getCode());
        ciCdPipelineVO.setAppServiceType(appServiceDTO.getType());
        ciCdPipelineVO.setAppServiceName(appServiceDTO.getName());
        return appServiceDTO;
    }

    private CiCdPipelineVO getCiCdPipelineVO(Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = baseQueryById(pipelineId);
        CommonExAssertUtil.assertTrue(ciCdPipelineDTO != null, DEVOPS_PIPELINE_NOT_EXIST, pipelineId);
        return ConvertUtils.convertObject(ciCdPipelineDTO, CiCdPipelineVO.class);
    }

    @Override
    public CiCdPipelineDTO queryByAppSvcId(Long id) {
        if (id == null) {
            throw new CommonException(ResourceCheckConstant.DEVOPS_APP_SERVICE_ID_IS_NULL);
        }
        CiCdPipelineDTO ciCdPipelineDTO = new CiCdPipelineDTO();
        ciCdPipelineDTO.setAppServiceId(id);
        return ciCdPipelineMapper.selectOne(ciCdPipelineDTO);
    }

    @Override
    public Page<CiCdPipelineVO> listByProjectIdAndAppName(Long projectId, String searchParam, PageRequest pageRequest, Boolean enableFlag, String status) {
        if (projectId == null) {
            throw new CommonException(DEVOPS_PROJECT_ID_IS_NULL);
        }
        // 应用有权限的应用服务
        Long userId = DetailsHelper.getUserDetails().getUserId();
        boolean projectOwner = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);
        Set<Long> appServiceIds;
        if (projectOwner) {
            appServiceIds = appServiceMapper.listByActive(projectId, null).stream().map(AppServiceDTO::getId).collect(Collectors.toSet());
        } else {
            //如果是项目成员，需要developer及以上的权限
            ImmutableProjectInfoVO projectDTO = baseServiceClientOperator.queryImmutableProjectInfo(projectId);
            appServiceIds = appServiceService.getMemberAppServiceIds(projectDTO.getTenantId(), projectId, userId);
            // 添加外部应用服务
            appServiceIds.addAll(appServiceService.listExternalAppIdByProjectId(projectId));
            if (CollectionUtils.isEmpty(appServiceIds)) {
                return new Page<>();
            }
        }

        // 查询流水线
        Page<CiCdPipelineVO> pipelinePage = PageHelper.doPage(pageRequest, () -> ciCdPipelineMapper.queryByProjectIdAndName(projectId, appServiceIds, searchParam, enableFlag, status));

//        if (CollectionUtils.isEmpty(pipelinePage.getContent())) {
//            return pipelinePage;
//        }
//        pipelinePage.getContent().forEach(pipelineVO -> {
//            // 查询每条流水线，最新的一条执行记录
//            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryLatestedPipelineRecord(pipelineVO.getId());
////            PipelineCompositeRecordVO pipelineCompositeRecordVO = devopsPipelineRecordRelService.queryLatestedPipelineRecord(pipelineVO.getId());
//            if (devopsCiPipelineRecordDTO != null) {
//                // 判断是否存在记录
//                pipelineVO.setHasRecords(true);
//                //计算流水线上一次执行的状态和时间
////                String latestExecuteStatus = calculateExecuteStatus(pipelineCompositeRecordVO);
//                pipelineVO.setLatestExecuteStatus(devopsCiPipelineRecordDTO.getStatus());
//                pipelineVO.setLatestExecuteDate(devopsCiPipelineRecordDTO.getCreationDate());
//            } else {
////                pipelineVO.setLatestExecuteStatus(PipelineStatus.SKIPPED.toValue());
////                pipelineVO.setLatestExecuteDate(pipelineVO.getCreationDate());
//            }
//
//        });
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
            throw new CommonException(DEVOPS_CI_PIPELINE_EXISTS_FOR_APP_SERVICE);
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
    public CiCdPipelineDTO baseQueryById(Long id) {
        return devopsCiCdPipelineMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public CiCdPipelineDTO disablePipeline(Long projectId, Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineDTO.getAppServiceId());
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_STATUS_UPDATE);
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        if (ciCdPipelineMapper.disablePipeline(pipelineId) != 1) {
            throw new CommonException(DEVOPS_DISABLE_PIPELINE_FAILED);
        }
        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    @Override
    @Transactional
    public void deletePipeline(Long projectId, Long pipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
        if (ciCdPipelineDTO == null) {
            return;
        }
        permissionHelper.checkAppServiceBelongToProject(projectId, ciCdPipelineDTO.getAppServiceId());
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_DELETE);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());

        // 删除流水线
        if (ciCdPipelineMapper.deleteByPrimaryKey(pipelineId) != 1) {
            throw new CommonException(DEVOPSDELETE_PIPELINE_FAILED);
        }
        // 删除stage
        List<DevopsCiStageDTO> devopsCiStageDTOS = devopsCiStageService.listByPipelineId(pipelineId);
        devopsCiStageService.deleteByPipelineId(pipelineId);
//        devopsCdStageService.deleteByPipelineId(pipelineId);

        // 删除job
        devopsCiJobService.deleteByPipelineId(pipelineId);
//        devopsCdJobService.deleteByPipelineId(pipelineId);

        //删除任务配置

        // 删除 ci job记录
        devopsCiJobRecordService.deleteByAppServiceId(appServiceDTO.getId());
        // 删除审核记录
        ciAuditUserRecordService.deleteByCiPipelineId(pipelineId);
        ciAuditRecordService.deleteByCiPipelineId(pipelineId);

        // 删除流水线相关产物
        devopsCiPipelineChartService.deleteByAppServiceId(appServiceDTO.getId());
        ciPipelineImageService.deleteByAppServiceId(appServiceDTO.getId());
        ciPipelineMavenService.deleteByAppServiceId(appServiceDTO.getId());
        ciPipelineAppVersionService.deleteByAppServiceId(appServiceDTO.getId());

        // 删除pipeline记录
        devopsCiPipelineRecordService.deleteByPipelineId(pipelineId);

        // 删除镜像扫描数据
        devopsImageScanResultService.deleteByAppServiceId(appServiceDTO.getId());
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
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_STATUS_UPDATE);
        if (ciCdPipelineMapper.enablePipeline(pipelineId) != 1) {
            throw new CommonException(DEVOPS_ENABLE_PIPELINE_FAILED);
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
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = createCiPipelineRecord(pipelineId, gitlabProjectId, pipeline);
            // 保存流水线记录关系
//            DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = new DevopsPipelineRecordRelDTO();
//            devopsPipelineRecordRelDTO.setPipelineId(ciCdPipelineDTO.getId());
//            devopsPipelineRecordRelDTO.setCiPipelineRecordId(devopsCiPipelineRecordDTO.getId());
//            devopsPipelineRecordRelDTO.setCdPipelineRecordId(PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID);
//            devopsPipelineRecordRelService.save(devopsPipelineRecordRelDTO);
            // 初始化cd流水线记录
//            devopsCdPipelineService.initPipelineRecordWithStageAndJob(projectId, pipeline.getId().longValue(), pipeline.getSha(), pipeline.getRef(), pipeline.getTag(), ciCdPipelineDTO);
            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId.intValue(),
                    pipeline.getId(),
                    userAttrDTO.getGitlabUserId().intValue(),
                    appExternalConfigDTO);
            devopsCiJobRecordService.create(devopsCiPipelineRecordDTO.getId(),
                    gitlabProjectId,
                    jobDTOS,
                    userAttrDTO.getIamUserId(),
                    appServiceDTO.getId());
        } catch (Exception e) {
            LOGGER.info("save pipeline Records failed.", e);
        }
    }

    private DevopsCiPipelineRecordDTO createCiPipelineRecord(Long pipelineId, Long gitlabProjectId, Pipeline pipeline) {
        DevopsCiPipelineRecordDTO recordDTO = new DevopsCiPipelineRecordDTO();
        recordDTO.setGitlabPipelineId(pipeline.getId().longValue());
        recordDTO.setCiPipelineId(pipelineId);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(recordDTO);
        if (devopsCiPipelineRecordDTO == null) {
            return devopsCiPipelineRecordService.create(pipelineId, gitlabProjectId, pipeline);
        }
        return devopsCiPipelineRecordDTO;
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
            throw new CommonException(DEVOPS_BRANCH_PERMISSION_MISMATCH, ref);
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
                throw new CommonException(DEVOPS_BRANCH_PERMISSION_MISMATCH, ref);
            }
            if (Boolean.TRUE.equals(branchDTO.getDevelopersCanMerge()) && memberDTO.getAccessLevel() < AccessLevel.DEVELOPER.toValue()) {
                throw new CommonException(DEVOPS_BRANCH_PERMISSION_MISMATCH, ref);
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
            stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOS());
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

        ImmutableProjectInfoVO immutableProjectInfoVO = baseServiceClientOperator.queryImmutableProjectInfo(projectId);
        Tenant tenant = baseServiceClientOperator.queryOrganizationById(immutableProjectInfoVO.getTenantId());

        // name: orgName-projectName + suffix
        String groupName = GitOpsUtil.renderGroupName(tenant.getTenantNum(),
                immutableProjectInfoVO.getDevopsComponentCode(), "");
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
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        DevopsPipelineBranchRelDTO devopsPipelineBranchRelDTO = new DevopsPipelineBranchRelDTO();
        devopsPipelineBranchRelDTO.setPipelineId(pipelineId);
        return devopsPipelineBranchRelMapper.select(devopsPipelineBranchRelDTO);
    }

    @Override
    public List<PipelineInstanceReferenceVO> listTaskReferencePipelineInfo(Long projectId, Set<Long> taskIds) {
        List<PipelineInstanceReferenceVO> pipelineInstanceReferenceVOS = devopsCiJobService.listApiTestTaskReferencePipelineInfo(projectId, taskIds);
        if (CollectionUtils.isEmpty(pipelineInstanceReferenceVOS)) {
            return new ArrayList<>();
        }
        return pipelineInstanceReferenceVOS;
//        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByProjectIdAndType(projectId, API_TEST);
//        if (CollectionUtils.isEmpty(devopsCiJobDTOS)) {
//            return pipelineInstanceReferenceVOList;
//        }
//
//        // 收集与测试任务关联的任务列表，并记录对应关系
//        for (DevopsCiJobDTO devopsCiJobDTO : devopsCiJobDTOS) {
//            CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(devopsCiJobDTO.getMetadata(), CdApiTestConfigVO.class);
//            if (taskIds.contains(cdApiTestConfigVO.getApiTestTaskId())) {
//                PipelineInstanceReferenceVO pipelineInstanceReferenceVO = new PipelineInstanceReferenceVO();
//                pipelineInstanceReferenceVO.setJobId(devopsCiJobDTO.getId());
//                pipelineInstanceReferenceVO.setTaskId(cdApiTestConfigVO.getApiTestTaskId());
//                pipelineInstanceReferenceVOList.add(pipelineInstanceReferenceVO);
//            }
//        }
//        if (CollectionUtils.isEmpty(pipelineInstanceReferenceVOList)) {
//            return pipelineInstanceReferenceVOList;
//        }
//        Set<Long> jobIds = pipelineInstanceReferenceVOList.stream().map(PipelineInstanceReferenceVO::getJobId).collect(Collectors.toSet());
//        List<DevopsCdJobVO> devopsCdJobVOS = devopsCdJobService.listByIdsWithNames(jobIds);
//        Map<Long, DevopsCdJobVO> cdJobVOMap = devopsCdJobVOS.stream().collect(Collectors.toMap(DevopsCdJobVO::getId, Function.identity()));
//        pipelineInstanceReferenceVOList.forEach(pipelineInstanceReferenceVO -> {
//            DevopsCdJobVO devopsCdJobVO = cdJobVOMap.get(pipelineInstanceReferenceVO.getJobId());
//            pipelineInstanceReferenceVO.setPipelineName(devopsCdJobVO.getPipelineName());
//            pipelineInstanceReferenceVO.setStageName(devopsCdJobVO.getStageName());
//            pipelineInstanceReferenceVO.setJobName(devopsCdJobVO.getName());
//        });
//
//        return pipelineInstanceReferenceVOList;
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

    @Override
    public List<String> listPipelineNameReferenceByConfigId(Long projectId, Long taskConfigId) {
        return ciCdPipelineMapper.listPipelineNameByTaskConfigId(taskConfigId);
    }

    @Override
    public Boolean doesApiTestSuiteRelatedWithPipeline(Long projectId, Long suiteId) {
        return devopsCiJobService.doesApiTestSuiteRelatedWithPipeline(projectId, suiteId);
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
                throw new CommonException(DEVOPS_DELETE_GITLAB_CI_FILE, e);
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
                throw new CommonException(DEVOPS_DELETE_GITLAB_CI_FILE, e);
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
        CommonExAssertUtil.assertTrue(projectId.equals(ciCdPipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
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
            throw new CommonException(DEVOPS_UPDATE_PIPELINE_FAILED);
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

        // 更新流水线变量
        // 先删除之前的旧数据（考虑到数据不多，性能影响不大，没有必要做对比更新）
        ciDockerAuthConfigService.deleteByPipelineId(pipelineId);
        saveCiDockerAuthConfig(ciCdPipelineVO, ciCdPipelineDTO);


        //更新CI流水线
        updateCiPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO, initCiFileFlag);
        //更新CD流水线
//        updateCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);

        // 生成gitlab-ci.yaml文件，（避免第一次执行没有保存mavenSettings文件）
        devopsCiContentService.queryLatestContent(ciCdPipelineDTO.getToken());

        return ciCdPipelineMapper.selectByPrimaryKey(pipelineId);
    }

    private void checkResourceId(Long pipelineId, CiCdPipelineVO ciCdPipelineVO) {
        if (ciCdPipelineVO.getId() != null && !pipelineId.equals(ciCdPipelineVO.getId())) {
            throw new CommonException(DEVOPS_PIPELINE_ID_INVALID);
        }
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getDevopsCiStageVOS())) {
            ciCdPipelineVO.getDevopsCiStageVOS().forEach(devopsCiStageVO -> {
                if (devopsCiStageVO.getCiPipelineId() != null && !devopsCiStageVO.getCiPipelineId().equals(pipelineId)) {
                    throw new CommonException(DEVOPS_STAGE_PIPELINE_ID_INVALID);
                }
                if (!CollectionUtils.isEmpty(devopsCiStageVO.getJobList())) {
                    devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                        if (devopsCiJobVO.getCiPipelineId() != null && !devopsCiJobVO.getCiPipelineId().equals(pipelineId)) {
                            throw new CommonException(DEVOPS_JOB_PIPELINE_ID_INVALID);
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
//        saveCdPipeline(projectId, ciCdPipelineVO, ciCdPipelineDTO);
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
//                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
//                        devopsCiJobDTO.setId(null);
//                        devopsCiJobDTO.setCiStageId(devopsCiStageVO.getId());
//                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
//                        devopsCiJobService.create(devopsCiJobDTO);

                        // 保存任务信息
                        AbstractJobHandler handler = jobOperator.getHandlerOrThrowE(devopsCiJobVO.getType());
                        DevopsCiJobDTO devopsCiJobDTO = handler.saveJobInfo(projectId, ciCdPipelineDTO.getId(), devopsCiStageVO.getId(), devopsCiJobVO);

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
//                        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
//                        devopsCiJobDTO.setCiStageId(savedDevopsCiStageDTO.getId());
//                        devopsCiJobDTO.setCiPipelineId(ciCdPipelineDTO.getId());
//                        devopsCiJobService.create(devopsCiJobDTO);

                        // 保存任务信息
                        AbstractJobHandler handler = jobOperator.getHandlerOrThrowE(devopsCiJobVO.getType());
                        DevopsCiJobDTO devopsCiJobDTO = handler.saveJobInfo(projectId, ciCdPipelineDTO.getId(), savedDevopsCiStageDTO.getId(), devopsCiJobVO);

                        batchSaveStep(projectId, devopsCiJobDTO.getId(), devopsCiJobVO.getDevopsCiStepVOList());
                    });
                }
            }
        });

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


    /**
     * 构建gitlab-ci对象，用于转换为gitlab-ci.yaml
     *
     * @return 构建完的CI文件对象
     */
    private GitlabCi buildGitLabCiObject(CiCdPipelineDTO ciCdPipelineDTO) {

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(ciCdPipelineDTO.getProjectId());
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
        List<CiDockerAuthConfigDTO> ciDockerAuthConfigDTOS = ciDockerAuthConfigService.listByPipelineId(pipelineId);
        if (!CollectionUtils.isEmpty(ciDockerAuthConfigDTOS)) {
            Map<String, Object> auth = new HashMap<>();
            ciDockerAuthConfigDTOS.forEach(ciDockerAuthConfigDTO -> {
                Map<String, String> config = new HashMap<>();
                String authStr = ciDockerAuthConfigDTO.getUsername() + ":" + ciDockerAuthConfigDTO.getPassword();
                config.put("auth", Base64Util.getBase64EncodedString(authStr));
                auth.put(ciDockerAuthConfigDTO.getDomain(), config);
            });
            Map<String, Object> auths = new HashMap<>();
            auths.put("auths", auth);
            Map<String, String> variables = gitlabCi.getVariables();
            if (CollectionUtils.isEmpty(variables)) {
                variables = new HashMap<>();
            }
            variables.put("DOCKER_AUTH_CONFIG", JsonHelper.marshalByJackson(auths));
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
                    if (devopsCiStepDTOS.stream().filter(v -> DevopsCiStepTypeEnum.SONAR.value().equals(v.getType())).anyMatch(s -> {
                        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = devopsCiSonarConfigService.queryByStepId(s.getId());
                        return SonarScannerType.SONAR_MAVEN.value().equals(devopsCiSonarConfigDTO.getScannerType());
                    })) {
                        CiJobServices ciJobServices = new CiJobServices();
                        ciJobServices.setName(defaultCiImage);
                        ciJobServices.setAlias("kaniko");
                        ciJob.setServices(ArrayUtil.singleAsList(ciJobServices));
                    }
                    if (job.getType().equals(API_TEST.value())) {
                        ciJob.setImage(testRunnerImage);
                        if (job.getStartIn() != null) {
                            ciJob.setStartIn(String.format("%s minutes", job.getStartIn().toString()));
                            ciJob.setWhen("delayed");
                        }
                    }
                    if (StringUtils.isNoneBlank(job.getTags())) {
                        ciJob.setTags(Arrays.asList(job.getTags().split(",")));
                    }
                    ciJob.setCache(buildJobCache(job));
                    processOnlyAndExcept(job, ciJob);

                    AbstractJobHandler handler = jobOperator.getHandler(job.getType());
                    handler.setCiJobConfig(job, ciJob);
                    ciJob.setScript(handler.buildScript(Objects.requireNonNull(organizationId), projectId, job));

                    gitlabCi.addJob(job.getName(), ciJob);
                });
            }
        });
        buildBeforeScript(gitlabCi, ciCdPipelineDTO.getVersionName());
        return gitlabCi;
    }

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
     * 把配置转换为gitlab-ci配置（maven,sonarqube）
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param devopsCiJobDTO 生成脚本
     * @return 生成的脚本列表
     */
    private List<String> buildScript(final Long organizationId, final Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        Assert.notNull(devopsCiJobDTO, "Job can't be null");
        Assert.notNull(organizationId, DEVOPS_ORGANIZATION_ID_IS_NULL);
        Assert.notNull(projectId, DEVOPS_PROJECT_ID_IS_NULL);
        final Long jobId = devopsCiJobDTO.getId();
        Assert.notNull(jobId, DEVOPS_JOB_ID_IS_NULL);

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
            } else {
                DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getAppId());
                devopsCdEnvDeployInfoDTO.setAppCode(devopsDeployAppCenterEnvDTO.getCode());
                devopsCdEnvDeployInfoDTO.setAppName(devopsDeployAppCenterEnvDTO.getName());
            }
            // 使用不进行主键加密的json工具再将json写入类, 用于在数据库存非加密数据
            devopsCdJobDTO.setMetadata(JsonHelper.marshalByJackson(devopsDeployInfoVO));
            devopsCdEnvDeployInfoService.save(devopsCdEnvDeployInfoDTO);
            devopsCdJobDTO.setDeployInfoId(devopsCdEnvDeployInfoDTO.getId());

        } else if (JobTypeEnum.CD_HOST.value().equals(t.getType())) {
            handlerHostInfo(t, projectId, pipelineId, devopsCdJobDTO);

        } else if (JobTypeEnum.CD_AUDIT.value().equals(t.getType())) {
            // 如果审核任务，审核人员只有一个人，则默认设置为或签
            if (CollectionUtils.isEmpty(t.getCdAuditUserIds())) {
                throw new CommonException(ResourceCheckConstant.DEVOPS_PARAM_IS_INVALID);
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

    protected void handlerHostInfo(DevopsCdJobVO t, Long projectId, Long pipelineId, DevopsCdJobDTO devopsCdJobDTO) {
        // 使用能够解密主键加密的json工具解密
        CdHostDeployConfigVO cdHostDeployConfigVO = KeyDecryptHelper.decryptJson(devopsCdJobDTO.getMetadata(), CdHostDeployConfigVO.class);
        checkCdHostJobName(pipelineId, cdHostDeployConfigVO, t.getName(), devopsCdJobDTO);
        additionalCheck(cdHostDeployConfigVO);
        // 使用不进行主键加密的json工具再将json写入类, 用于在数据库存非加密数据
        DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO = ConvertUtils.convertObject(cdHostDeployConfigVO, DevopsCdHostDeployInfoDTO.class);
        if (cdHostDeployConfigVO.getJarDeploy() != null && !StringUtils.equals(cdHostDeployConfigVO.getHostDeployType(), RdupmTypeEnum.DOCKER.value())) {
            devopsCdHostDeployInfoDTO.setDeployJson(JsonHelper.marshalByJackson(cdHostDeployConfigVO.getJarDeploy()));
        }
        if (cdHostDeployConfigVO.getImageDeploy() != null && StringUtils.equals(cdHostDeployConfigVO.getHostDeployType(), RdupmTypeEnum.DOCKER.value())) {
            devopsCdHostDeployInfoDTO.setDeployJson(JsonHelper.marshalByJackson(cdHostDeployConfigVO.getImageDeploy()));
            devopsCdHostDeployInfoDTO.setDockerCommand(cdHostDeployConfigVO.getDockerCommand());
            devopsCdHostDeployInfoDTO.setKillCommand(null);
            devopsCdHostDeployInfoDTO.setPreCommand(null);
            devopsCdHostDeployInfoDTO.setRunCommand(null);
            devopsCdHostDeployInfoDTO.setPostCommand(null);
        }

        if (DeployTypeEnum.CREATE.value().equals(devopsCdHostDeployInfoDTO.getDeployType())) {
            // 校验应用编码和应用名称
            devopsHostAppService.checkNameAndCodeUniqueAndThrow(projectId, null, devopsCdHostDeployInfoDTO.getAppName(), devopsCdHostDeployInfoDTO.getAppCode());
            devopsCdHostDeployInfoDTO.setAppId(null);
        } else {
            if (devopsCdHostDeployInfoDTO.getAppId() != null) {
                DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(devopsCdHostDeployInfoDTO.getAppId());
                devopsCdHostDeployInfoDTO.setHostId(devopsHostAppDTO.getHostId());
            }
        }
        devopsCdJobDTO.setDeployInfoId(devopsCdHostDeployInfoService.baseCreate(devopsCdHostDeployInfoDTO).getId());
        devopsCdJobDTO.setMetadata(JsonHelper.marshalByJackson(cdHostDeployConfigVO));
    }

    /**
     * 供子类拓展
     *
     * @param cdHostDeployConfigVO
     */
    protected void additionalCheck(CdHostDeployConfigVO cdHostDeployConfigVO) {
        // 创建主机应用，必须输入主机id
        if (DeployTypeEnum.CREATE.value().equals(cdHostDeployConfigVO.getDeployType()) && cdHostDeployConfigVO.getHostId() == null) {
            throw new CommonException(DEVOPS_HOST_ID_IS_NULL);
        }
    }

    public void checkCdHostJobName(Long ciPipelineId, CdHostDeployConfigVO deployConfigVO, String cdHostName, DevopsCdJobDTO devopsCdJobDTO) {
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        if (deployConfigVO.getJarDeploy() != null
                && deployConfigVO.getJarDeploy().getDeploySource().equals(HostDeploySource.PIPELINE_DEPLOY.getValue())) {
            devopsCiJobDTO.setName(deployConfigVO.getJarDeploy().getPipelineTask());
        }
        if (!StringUtils.isEmpty(devopsCiJobDTO.getName())) {
            DevopsCiJobDTO ciJobDTO = devopsCiJobMapper.selectOne(devopsCiJobDTO);
            if (ciJobDTO == null) {
                throw new CommonException(DEVOPS_CD_HOST_JOB_UNION_CI_JOB, devopsCiJobDTO.getName(), cdHostName);
            }
            if (!ciJobDTO.getTriggerType().equals(devopsCdJobDTO.getTriggerType())) {
                throw new CommonException(DEVOPS_CI_CD_TRIGGER_TYPE_INVALID);
            }
        }
    }
}
