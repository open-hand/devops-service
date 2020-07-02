package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import com.alibaba.fastjson.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.ci.Cache;
import io.choerodon.devops.infra.dto.gitlab.ci.CachePolicy;
import io.choerodon.devops.infra.dto.gitlab.ci.CiJob;
import io.choerodon.devops.infra.dto.gitlab.ci.GitlabCi;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.maven.Repository;
import io.choerodon.devops.infra.dto.maven.RepositoryPolicy;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;

@Service
public class CiCdPipelineServiceImpl implements CiCdPipelineService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final String CREATE_PIPELINE_FAILED = "create.pipeline.failed";
    private static final String ERROR_USER_HAVE_NO_APP_PERMISSION = "error.user.have.no.app.permission";
    private static final String ERROR_UNSUPPORTED_STEP_TYPE = "error.unsupported.step.type";
    private static final String ERROR_CI_MAVEN_REPOSITORY_TYPE = "error.ci.maven.repository.type";
    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "error.maven.settings.insert";
    private static final String UPDATE_PIPELINE_FAILED = "update.pipeline.failed";
    private static final String DISABLE_PIPELINE_FAILED = "disable.pipeline.failed";
    private static final String ENABLE_PIPELINE_FAILED = "enable.pipeline.failed";


    private static final String MANUAL = "manual";
    private static final String AUTO = "auto";


    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Autowired
    private CiCdPipelineMapper ciCdPipelineMapper;
    @Autowired
    private CiCdStageMapper ciCdStageMapper;
    @Autowired
    private CiCdJobMapper ciCdJobMapper;
    @Autowired
    private CiCdJobValuesMapper ciCdJobValuesMapper;

    @Autowired
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private CiCdStageService ciCdStageService;
    @Autowired
    @Lazy
    private CiCdJobService ciCdJobService;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;
    @Autowired
    private CiCdJobValuesServcie ciCdJobValuesServcie;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsCdAuditService devopsCdAuditService;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;
    @Autowired
    private DevopsCdAuditMapper devopsCdAuditMapper;


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

    @Override
    @Transactional
    public CiCdPipelineDTO create(Long projectId, CiCdPipelineVO ciCdPipelineVO) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineVO.getAppServiceId(), AppServiceEvent.CI_PIPELINE_CREATE);
        Long iamUserId = TypeUtil.objToLong(GitUserNameUtil.getUserId());
        checkUserPermission(ciCdPipelineVO.getAppServiceId(), iamUserId);
        ciCdPipelineVO.setProjectId(projectId);

        // 设置默认镜像
        if (StringUtils.isEmpty(ciCdPipelineVO.getImage())) {
            ciCdPipelineVO.setImage(defaultCiImage);
        }
        //1.保存流水线的基本信息，触发方式全为自动
        CiCdPipelineDTO ciCdPipelineDTO = ConvertUtils.convertObject(ciCdPipelineVO, CiCdPipelineDTO.class);
        ciCdPipelineDTO.setToken(GenerateUUID.generateUUID());
        if (ciCdPipelineMapper.insertSelective(ciCdPipelineDTO) != 1) {
            throw new CommonException(CREATE_PIPELINE_FAILED);
        }

        //2.保存stage信息
        ciCdPipelineVO.getCiCdStageVOS().forEach(ciCdStageVO -> {
            CiCdStageDTO ciCdStageDTO = ConvertUtils.convertObject(ciCdStageVO, CiCdStageDTO.class);
            ciCdStageDTO.setPipelineId(ciCdPipelineVO.getId());
            //保存stage的基本信息
            CiCdStageDTO savedCiCdStageDTO = ciCdStageService.create(ciCdStageDTO);
            //CD阶段保存人工审核的关系
            createUserRel(ciCdStageVO.getCdAuditUserIds(), null, savedCiCdStageDTO.getId(), null);

            // 3.保存job信息
            if (!CollectionUtils.isEmpty(ciCdStageVO.getJobList())) {
                ciCdStageVO.getJobList().forEach(ciCdJobVO -> {
                    CiCdJobDTO ciCdJobDTO = ConvertUtils.convertObject(ciCdJobVO, CiCdJobDTO.class);
                    ciCdJobDTO.setPipelineIid(ciCdPipelineVO.getId());
                    ciCdJobDTO.setStageId(savedCiCdStageDTO.getId());
                    //保存JOB的基本信息
                    ciCdJobVO.setId(ciCdJobService.create(ciCdJobDTO).getId());
                    //CD阶段下的job 需要保存审核人员的关系
                    createPipelineJob(ciCdJobVO, projectId, ciCdStageVO.getId());
                });
            }
        });

        // 4.保存ci配置文件
        saveCiContent(projectId, ciCdPipelineDTO.getId(), ciCdPipelineVO);

        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciCdPipelineDTO.getAppServiceId());
        String ciFileIncludeUrl = String.format(GitOpsConstants.CI_CONTENT_URL_TEMPLATE, gatewayUrl, projectId, ciCdPipelineDTO.getToken());
        initGitlabCiFile(appServiceDTO.getGitlabProjectId(), ciFileIncludeUrl);
        return ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineDTO.getId());

    }

    @Override
    public CiCdPipelineVO query(Long projectId, Long ciCdPipelineId) {
        // 根据pipeline_id查询数据
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineId);
        CommonExAssertUtil.assertTrue(ciCdPipelineDTO != null, "error.ci.pipeline.not.exist", ciCdPipelineId);
        List<CiCdStageDTO> devopsCiStageDTOList = ciCdStageService.listByPipelineId(ciCdPipelineId);

        List<CiCdJobDTO> devopsCiJobDTOS = ciCdJobService.listByPipelineId(ciCdPipelineId);
        // dto转vo
        CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(ciCdPipelineDTO, CiCdPipelineVO.class);
        List<CiCdStageVO> ciCdStageVOS = ConvertUtils.convertList(devopsCiStageDTOList, CiCdStageVO.class);
        fillStageUserIds(ciCdStageVOS);
        List<CiCdJobVO> ciCdJobVOS = ConvertUtils.convertList(devopsCiJobDTOS, CiCdJobVO.class);
        fillJobUserIds(ciCdJobVOS);

        // 封装对象
        Map<Long, List<CiCdJobVO>> jobMap = ciCdJobVOS.stream().collect(Collectors.groupingBy(CiCdJobVO::getCiCdStageId));

        ciCdStageVOS.forEach(ciCdStageVO -> {
            List<CiCdJobVO> ciJobVOS = jobMap.getOrDefault(ciCdStageVO.getId(), Collections.emptyList());
            ciJobVOS.sort(Comparator.comparingLong(CiCdJobVO::getId));
            ciCdStageVO.setJobList(ciJobVOS);
        });
        // stage排序
        ciCdStageVOS = ciCdStageVOS.stream().sorted(Comparator.comparing(CiCdStageVO::getSequence)).collect(Collectors.toList());
        ciCdPipelineVO.setCiCdStageVOS(ciCdStageVOS);

        return ciCdPipelineVO;
    }

    @Override
    @Transactional
    public CiCdPipelineDTO update(Long projectId, Long ciCdPipelineId, CiCdPipelineVO ciCdPipelineVO) {
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineVO.getAppServiceId(), AppServiceEvent.CI_PIPELINE_UPDATE);
        Long userId = DetailsHelper.getUserDetails().getUserId();
        checkUserPermission(ciCdPipelineVO.getAppServiceId(), userId);
        // 校验自定义任务格式
        CiCdPipelineDTO ciCdPipelineDTO = ConvertUtils.convertObject(ciCdPipelineVO, CiCdPipelineDTO.class);
        ciCdPipelineDTO.setId(ciCdPipelineId);
        if (ciCdPipelineMapper.updateByPrimaryKeySelective(ciCdPipelineDTO) != 1) {
            throw new CommonException(UPDATE_PIPELINE_FAILED);
        }
        // 更新stage
        // 查询数据库中原有stage列表,并和新的stage列表作比较。
        // 差集：要删除的记录
        // 交集：要更新的记录
        List<CiCdStageDTO> ciCdStageDTOS = ciCdStageService.listByPipelineId(ciCdPipelineId);
        Set<Long> oldStageIds = ciCdStageDTOS.stream().map(CiCdStageDTO::getId).collect(Collectors.toSet());

        Set<Long> updateIds = ciCdPipelineVO.getCiCdStageVOS().stream()
                .filter(devopsCiStageVO -> devopsCiStageVO.getId() != null)
                .map(CiCdStageVO::getId)
                .collect(Collectors.toSet());
        // 去掉要更新的记录，剩下的为要删除的记录
        oldStageIds.removeAll(updateIds);
        oldStageIds.forEach(stageId -> {
            ciCdStageService.deleteById(stageId);
            ciCdJobService.deleteByStageId(stageId);
            // 删除cd阶段审核人员关系
            if (stageIsCdAndManual(stageId)) {
                deleteAuditUserBystageId(stageId);
            }
        });

        ciCdPipelineVO.getCiCdStageVOS().forEach(ciCdStageVO -> {
            if (ciCdStageVO.getId() != null) {
                // 更新
                ciCdStageService.update(ciCdStageVO);
                //cd阶段审核人员关系
                if (stageIsCdAndManual(ciCdStageVO.getId())) {
                    updateCdAuditUser(ciCdStageVO);
                }

                ciCdJobService.deleteByStageId(ciCdStageVO.getId());

                // 保存job信息
                if (!CollectionUtils.isEmpty(ciCdStageVO.getJobList())) {
                    ciCdStageVO.getJobList().forEach(ciCdJobVO -> {
                        CiCdJobDTO ciCdJobDTO = ConvertUtils.convertObject(ciCdJobVO, CiCdJobDTO.class);
                        ciCdJobDTO.setId(null);
                        ciCdJobDTO.setStageId(ciCdStageVO.getId());
                        ciCdJobDTO.setPipelineIid(ciCdPipelineId);
                        ciCdJobService.create(ciCdJobDTO);
                        if (stageIsCdAndManual(ciCdJobVO.getCiCdStageId())) {
                            updateCdAuditUser(ciCdJobVO);
                        }
                        ciCdJobVO.setId(ciCdJobDTO.getId());
                    });
                }
            } else {
                // 新增
                ciCdStageVO.setCiCdPipelineId(ciCdPipelineId);
                CiCdStageDTO ciCdStageDTO = ConvertUtils.convertObject(ciCdStageVO, CiCdStageDTO.class);
                CiCdStageDTO savedDevopsCiStageDTO = ciCdStageService.create(ciCdStageDTO);
                // 保存job信息
                if (!CollectionUtils.isEmpty(ciCdStageVO.getJobList())) {
                    ciCdStageVO.getJobList().forEach(ciCdJobVO -> {
                        CiCdJobDTO ciCdJobDTO = ConvertUtils.convertObject(ciCdJobVO, CiCdJobDTO.class);
                        ciCdJobDTO.setStageId(savedDevopsCiStageDTO.getId());
                        ciCdJobDTO.setPipelineIid(ciCdPipelineId);
                        ciCdJobService.create(ciCdJobDTO);
                        ciCdJobVO.setId(ciCdJobDTO.getId());
                    });
                }
            }
        });
        saveCiContent(projectId, ciCdPipelineId, ciCdPipelineVO);
        return ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineId);
    }

    @Override
    @Transactional
    public CiCdPipelineDTO disablePipeline(Long projectId, Long ciCdPipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineId);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CI_PIPELINE_STATUS_UPDATE);
        if (ciCdPipelineMapper.disablePipeline(ciCdPipelineId) != 1) {
            throw new CommonException(DISABLE_PIPELINE_FAILED);
        }
        return ciCdPipelineMapper.selectByPrimaryKey(ciCdPipelineId);
    }

    @Override
    @Transactional
    public CiCdPipelineDTO enablePipeline(Long projectId, Long ciPipelineId) {
        CiCdPipelineDTO ciCdPipelineDTO = ciCdPipelineMapper.selectByPrimaryKey(ciPipelineId);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineDTO.getAppServiceId(), AppServiceEvent.CI_PIPELINE_STATUS_UPDATE);
        if (ciCdPipelineMapper.enablePipeline(ciPipelineId) != 1) {
            throw new CommonException(ENABLE_PIPELINE_FAILED);
        }
        return ciCdPipelineMapper.selectByPrimaryKey(ciPipelineId);
    }

    private void updateCdAuditUser(CiCdJobVO ciCdJobVO) {
        deleteAuditUserByJobId(ciCdJobVO.getId());
        insertAuditUserByJobId(ciCdJobVO);
    }

    private void insertAuditUserByJobId(CiCdJobVO ciCdJobVO) {
        if (!CollectionUtils.isEmpty(ciCdJobVO.getCdAuditUserIds())) {
            ciCdJobVO.getCdAuditUserIds().stream().forEach(aLong -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
                devopsCdAuditDTO.setCicdJobId(ciCdJobVO.getId());
                devopsCdAuditDTO.setUserId(aLong);
                devopsCdAuditMapper.insert(devopsCdAuditDTO);
            });
        }
    }

    private void deleteAuditUserByJobId(Long id) {
        DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
        devopsCdAuditDTO.setCicdJobId(id);
        devopsCdAuditMapper.delete(devopsCdAuditDTO);
    }

    private void updateCdAuditUser(CiCdStageVO ciCdStageVO) {
        deleteAuditUserBystageId(ciCdStageVO.getId());
        insertAuditUserBystageId(ciCdStageVO);
    }

    private void deleteAuditUserBystageId(Long stageId) {
        DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
        devopsCdAuditDTO.setCicdStageId(stageId);
        devopsCdAuditMapper.delete(devopsCdAuditDTO);
    }

    private void insertAuditUserBystageId(CiCdStageVO ciCdStageVO) {
        if (!CollectionUtils.isEmpty(ciCdStageVO.getCdAuditUserIds())) {
            ciCdStageVO.getCdAuditUserIds().stream().forEach(aLong -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
                devopsCdAuditDTO.setCicdStageId(ciCdStageVO.getId());
                devopsCdAuditDTO.setUserId(aLong);
                devopsCdAuditMapper.insert(devopsCdAuditDTO);
            });
        }
    }

    private void fillJobUserIds(List<CiCdJobVO> ciCdJobVOS) {
        DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
        ciCdJobVOS.stream().filter(ciCdJobVO -> stageIsCdAndManual(ciCdJobVO.getCiCdStageId())).forEach(e -> {
            devopsCdAuditDTO.setCicdJobId(e.getId());
            List<Long> userIds = devopsCdAuditMapper.select(devopsCdAuditDTO).stream().map(DevopsCdAuditDTO::getUserId).collect(Collectors.toList());
            e.setCdAuditUserIds(userIds);
        });

    }

    private void fillStageUserIds(List<CiCdStageVO> ciCdStageVOS) {
        DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
        ciCdStageVOS.stream().filter(v -> StageType.CD.getType().equals(v.getType())
                && DeployType.MANUAL.getType().equals(v.getTriggerType())).forEach(e -> {
            devopsCdAuditDTO.setCicdStageId(e.getId());
            List<Long> userIds = devopsCdAuditMapper.select(devopsCdAuditDTO).stream().map(DevopsCdAuditDTO::getUserId).collect(Collectors.toList());
            e.setCdAuditUserIds(userIds);
        });
    }

    /**
     * 校验用户是否拥有应用服务权限
     *
     * @param appServiceId 应用服务id
     * @param iamUserId    用户id
     */
    private void checkUserPermission(Long appServiceId, Long iamUserId) {
        if (!appServiceService.checkAppServicePermissionForUser(appServiceId, iamUserId)) {
            throw new CommonException(ERROR_USER_HAVE_NO_APP_PERMISSION);
        }

    }

    private void saveCiContent(final Long projectId, Long pipelineId, CiCdPipelineVO ciCdPipelineVO) {
        GitlabCi gitlabCi = buildGitLabCiObject(projectId, ciCdPipelineVO);
        StringBuilder gitlabCiYaml = new StringBuilder(GitlabCiUtil.gitlabCi2yaml(gitlabCi));

        // 拼接自定义job
        if (!CollectionUtils.isEmpty(ciCdPipelineVO.getCiCdStageVOS())) {
            List<CiCdJobVO> ciJobVOS = ciCdPipelineVO.getCiCdStageVOS().stream()
                    .flatMap(v -> v.getJobList().stream()).filter(job -> CiJobTypeEnum.CUSTOM.value().equalsIgnoreCase(job.getType()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ciJobVOS)) {
                for (CiCdJobVO job : ciJobVOS) {
                    gitlabCiYaml.append(GitOpsConstants.NEW_LINE).append(job.getMetadata());
                }
            }

        }

        //保存gitlab-ci配置文件
        CiCdJobValuesDTO ciCdJobValuesDTO = new CiCdJobValuesDTO();
        ciCdJobValuesDTO.setCicdJobId(pipelineId);
        ciCdJobValuesDTO.setValue(gitlabCiYaml.toString());
        ciCdJobValuesServcie.create(ciCdJobValuesDTO);
    }

    /**
     * 构建gitlab-ci对象，用于转换为gitlab-ci.yaml
     *
     * @param projectId      项目id
     * @param ciCdPipelineVO 流水线数据
     * @return 构建完的CI文件对象
     */
    private GitlabCi buildGitLabCiObject(final Long projectId, CiCdPipelineVO ciCdPipelineVO) {
        // 对阶段排序，筛选出ci的阶段
        List<String> stages = ciCdPipelineVO.getCiCdStageVOS().stream()
                .filter(e -> StageType.CI.getType().equals(e.getType()))
                .sorted(Comparator.comparing(CiCdStageVO::getSequence))
                .map(CiCdStageVO::getName)
                .collect(Collectors.toList());

        GitlabCi gitlabCi = new GitlabCi();

        // 如果用户指定了就使用用户指定的，如果没有指定就使用默认的猪齿鱼提供的镜像
        gitlabCi.setImage(StringUtils.isEmpty(ciCdPipelineVO.getImage()) ? defaultCiImage : ciCdPipelineVO.getImage());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        gitlabCi.setStages(stages);
        ciCdPipelineVO.getCiCdStageVOS().forEach(stageVO -> {
            if (!CollectionUtils.isEmpty(stageVO.getJobList())) {
                stageVO.getJobList().forEach(job -> {
                    if (CiJobTypeEnum.CUSTOM.value().equals(job.getType())) {
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
        buildBeforeScript(gitlabCi);
        return gitlabCi;
    }

    /**
     * 把配置转换为gitlab-ci配置（maven,sonarqube）
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param jobVO          生成脚本
     * @return 生成的脚本列表
     */
    private List<String> buildScript(final Long organizationId, final Long projectId, CiCdJobVO jobVO) {
        Assert.notNull(jobVO, "Job can't be null");
        Assert.notNull(organizationId, "Organization id can't be null");
        Assert.notNull(projectId, "project id can't be null");
        final Long jobId = jobVO.getId();
        Assert.notNull(jobId, "Ci job id is required.");

        if (CiJobTypeEnum.SONAR.value().equals(jobVO.getType())) {
            // sonar配置转化为gitlab-ci配置
            List<String> scripts = new ArrayList<>();
            SonarQubeConfigVO sonarQubeConfigVO = JSONObject.parseObject(jobVO.getMetadata(), SonarQubeConfigVO.class);
            if (CiSonarConfigType.DEFAULT.value().equals(sonarQubeConfigVO.getConfigType())) {
                // 查询默认的sonarqube配置
                DevopsConfigDTO sonarConfig = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
                CommonExAssertUtil.assertTrue(sonarConfig != null, "error.default.sonar.not.exist");
                scripts.add(GitlabCiUtil.getDefaultSonarCommand());
            } else if (CiSonarConfigType.CUSTOM.value().equals(sonarQubeConfigVO.getConfigType())) {
                if (Objects.isNull(sonarQubeConfigVO.getSonarUrl())) {
                    throw new CommonException("error.sonar.url.is.null");
                }
                if (SonarAuthType.USERNAME_PWD.value().equals(sonarQubeConfigVO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getUsername(), sonarQubeConfigVO.getPassword()));
                } else if (SonarAuthType.TOKEN.value().equals(sonarQubeConfigVO.getAuthType())) {
                    scripts.add(GitlabCiUtil.renderSonarCommand(sonarQubeConfigVO.getSonarUrl(), sonarQubeConfigVO.getToken()));
                }
            } else {
                throw new CommonException("error.sonar.config.type.not.supported", sonarQubeConfigVO.getConfigType());
            }

            return scripts;
        } else if (CiJobTypeEnum.BUILD.value().equals(jobVO.getType())) {
            // 将构建类型的stage中的job的每个step进行解析和转化
            CiConfigVO ciConfigVO = JSONObject.parseObject(jobVO.getMetadata(), CiConfigVO.class);
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
                                result.add(GitlabCiUtil.generateDockerScripts(
                                        config.getDockerContextDir(),
                                        config.getDockerFilePath(),
                                        config.getSkipDockerTlsVerify() == null || config.getSkipDockerTlsVerify()));
                                break;
                            case MAVEN_DEPLOY:
                                List<MavenRepoVO> repos = buildAndSaveMavenSettings(projectId, jobId, config.getSequence(), config.getMavenDeployRepoSettings());
                                result.addAll(buildMavenScripts(projectId, jobId, config, repos));
                                break;
                        }
                    });

            return result;
        } else if (CiJobTypeEnum.CHART.value().equals(jobVO.getType())) {
            // 生成chart步骤
            return ArrayUtil.singleAsList(GitlabCiUtil.generateChartBuildScripts());
        }
        return Collections.emptyList();
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
                repos.addAll(nexusMavenRepoDTOs.stream().map(CiCdPipelineServiceImpl::convertRepo).collect(Collectors.toList()));
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

    /**
     * 生成maven构建相关的脚本
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param ciConfigTemplateVO maven发布软件包阶段的信息
     * @param mavenRepoVO        仓库信息
     * @return 生成的shell脚本
     */
    private List<String> buildMavenScripts(final Long projectId, final Long jobId, CiConfigTemplateVO ciConfigTemplateVO, List<MavenRepoVO> mavenRepoVO) {
        List<String> shells = new ArrayList<>();
        // 这里这么写是为了考虑之后可能选了多个仓库, 如果是多个仓库的话, 变量替换不便
        List<String> templateShells = GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(ciConfigTemplateVO.getScript()), true, true);
        if (!CollectionUtils.isEmpty(mavenRepoVO)) {
            // 插入shell指令将配置的settings文件下载到项目目录下
            shells.add(GitlabCiUtil.downloadMavenSettings(projectId, jobId, ciConfigTemplateVO.getSequence()));

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
            for (MavenRepoVO repo : mavenRepoVO) {
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
        } else {
            shells.addAll(templateShells);
        }
        return shells;
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
     * 生成并存储maven settings到数据库
     *
     * @param projectId               项目id
     * @param jobId                   job id
     * @param sequence                这个步骤的序号
     * @param mavenDeployRepoSettings 配置信息
     * @return 为空表示没有settings配置，不为空表示有
     */
    private List<MavenRepoVO> buildAndSaveMavenSettings(Long projectId, Long jobId, Long sequence, MavenDeployRepoSettings mavenDeployRepoSettings) {
        if (CollectionUtils.isEmpty(mavenDeployRepoSettings.getNexusRepoIds())) {
            return Collections.emptyList();
        }
        List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, mavenDeployRepoSettings.getNexusRepoIds());

        if (CollectionUtils.isEmpty(nexusMavenRepoDTOs)) {
            return Collections.emptyList();
        }

        List<MavenRepoVO> mavenRepoVOS = nexusMavenRepoDTOs.stream().map(CiCdPipelineServiceImpl::convertRepo).collect(Collectors.toList());

        // settings文件内容
        String settings = buildSettings(mavenRepoVOS);
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO(jobId, sequence, settings);
        MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        return mavenRepoVOS;
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

    @Nullable
    private Cache buildJobCache(CiCdJobVO jobConfig) {
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


    /**
     * 处理job的触发方式
     *
     * @param metadata job元数据
     * @param ciJob    ci文件的job对象
     */
    private void processOnlyAndExcept(CiCdJobVO metadata, CiJob ciJob) {
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

    private void buildBeforeScript(GitlabCi gitlabCi) {
        List<String> beforeScripts = ArrayUtil.singleAsList(GitOpsConstants.CHOERODON_BEFORE_SCRIPT);
        // 如果有job启用了缓存设置, 就创建缓存目录
        if (gitlabCi.getJobs().values().stream().anyMatch(j -> j.getCache() != null)) {
            beforeScripts.add(GitlabCiUtil.generateCreateCacheDir(GitOpsConstants.CHOERODON_CI_CACHE_DIR));
        }
        gitlabCi.setBeforeScript(beforeScripts);
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

    private static String buildIncludeYaml(String ciFileIncludeUrl) {
        GitlabCi gitlabCi = new GitlabCi();
        gitlabCi.setInclude(ciFileIncludeUrl);
        return GitlabCiUtil.gitlabCi2yaml(gitlabCi);
    }

    private void createUserRel(List<Long> pipelineUserRelDTOS, Long pipelineId, Long stageId, Long jobId) {
        if (pipelineUserRelDTOS != null) {
            pipelineUserRelDTOS.forEach(t -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
                devopsCdAuditDTO.setCicdPipelineId(pipelineId);
                devopsCdAuditDTO.setCicdStageId(stageId);
                devopsCdAuditDTO.setCicdJobId(jobId);
                devopsCdAuditDTO.setUserId(t);
                devopsCdAuditService.baseCreate(devopsCdAuditDTO);
            });
        }
    }


    private void createPipelineJob(CiCdJobVO t, Long projectId, Long stageId) {
        t.setProjectId(projectId);
        t.setCiCdStageId(stageId);
        if (AUTO.equals(t.getType())) {
            //如果JOB是自动类型的
            PipelineAppServiceDeployDTO appDeployDTO = deployVoToDto(t.getPipelineAppServiceDeployVO());
            appDeployDTO.setProjectId(projectId);
            t.setAppServiceDeployId(pipelineAppDeployService.baseCreate(appDeployDTO).getId());
        }
        Long jobId = ciCdJobService.create(ConvertUtils.convertObject(t, CiCdJobDTO.class)).getId();
        if (MANUAL.equals(t.getType())) {
            //如果Job是手动类型的
            createUserRel(t.getCdAuditUserIds(), null, null, jobId);
        }
    }

    private PipelineAppServiceDeployDTO deployVoToDto(PipelineAppServiceDeployVO appServiceDeployVO) {
        PipelineAppServiceDeployDTO appServiceDeployDTO = new PipelineAppServiceDeployDTO();
        BeanUtils.copyProperties(appServiceDeployVO, appServiceDeployDTO);
        if (appServiceDeployVO.getTriggerVersion() != null && !appServiceDeployVO.getTriggerVersion().isEmpty()) {
            appServiceDeployDTO.setTriggerVersion(String.join(",", appServiceDeployVO.getTriggerVersion()));
        }
        return appServiceDeployDTO;
    }

    private Boolean stageIsCdAndManual(Long stageId) {
        CiCdStageDTO ciCdStageDTO = ciCdStageMapper.selectByPrimaryKey(stageId);
        return !Objects.isNull(ciCdStageDTO) && DeployType.MANUAL.getType().equals(ciCdStageDTO.getTriggerType());
    }

    private void updateUserRel(List<Long> relDTOList, Long pipelineId, Long stageId, Long jobId) {
        List<Long> addUserRelEList = new ArrayList<>();
        List<Long> relEList = devopsCdAuditService.baseListByOptions(pipelineId, stageId, jobId).stream().map(DevopsCdAuditDTO::getUserId).collect(Collectors.toList());
        if (relDTOList != null) {
            relDTOList.forEach(relE -> {
                if (!relEList.contains(relE)) {
                    addUserRelEList.add(relE);
                } else {
                    relEList.remove(relE);
                }
            });
            addUserRelEList.forEach(addUserId -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO(pipelineId, stageId, jobId);
                devopsCdAuditDTO.setUserId(addUserId);
                devopsCdAuditService.baseCreate(devopsCdAuditDTO);
            });
        }
        relEList.forEach(delUserId -> {
            DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO(pipelineId, stageId, jobId);
            devopsCdAuditDTO.setUserId(delUserId);
            devopsCdAuditService.baseDelete(devopsCdAuditDTO);
        });
    }
}
