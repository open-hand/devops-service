package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_GITLAB_CI_PIPELINE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_YAML_FORMAT_INVALID;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_GITLAB_PIPELINE_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.PipelineConstants.DEVOPS_UPDATE_CI_JOB_RECORD;
import static org.hzero.core.base.BaseConstants.Symbol.SLASH;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.pipeline.*;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.api.vo.sonar.QualityGateResult;
import io.choerodon.devops.api.vo.test.ApiTestTaskRecordVO;
import io.choerodon.devops.app.eventhandler.pipeline.job.AbstractJobHandler;
import io.choerodon.devops.app.eventhandler.pipeline.job.JobOperator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.GitlabPipelineDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.Pipeline;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.repo.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.*;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.RdupmClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.feign.operator.TestServiceClientOperator;
import io.choerodon.devops.infra.gitops.IamAdminIdHolder;
import io.choerodon.devops.infra.handler.CiPipelineSyncHandler;
import io.choerodon.devops.infra.handler.HostConnectionHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 9:26
 */
@Service
public class DevopsCiPipelineRecordServiceImpl implements DevopsCiPipelineRecordService {


    protected static final String DEVOPS_DEPLOY_FAILED = "devops.deploy.failed";

    protected static final String CUSTOM_REPO = "CUSTOM_REPO";
    protected static final String CREATE = "create";

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCiPipelineRecordServiceImpl.class);

    private static final String DOWNLOAD_JAR_URL = "%s%s/%s/repository/";

    protected static final String HOST = "host";

    private final DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;
    private final DevopsCiJobRecordService devopsCiJobRecordService;
    private final DevopsCiStageService devopsCiStageService;
    private final DevopsCiJobService devopsCiJobService;
    private final DevopsCiJobRecordMapper devopsCiJobRecordMapper;
    private final DevopsCiPipelineService devopsCiPipelineService;
    private final AppServiceService applicationService;
    private final TransactionalProducer transactionalProducer;
    private final UserAttrService userAttrService;
    private final BaseServiceClientOperator baseServiceClientOperator;
    private final GitlabServiceClientOperator gitlabServiceClientOperator;
    private final DevopsGitlabCommitService devopsGitlabCommitService;
    private final CiPipelineSyncHandler ciPipelineSyncHandler;
    private final CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    private final AppServiceMapper appServiceMapper;
    private SendNotificationService sendNotificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCiPipelineChartService devopsCiPipelineChartService;
    @Autowired
    private RdupmClient rdupmClient;
    @Autowired
    private DevopsImageScanResultMapper devopsImageScanResultMapper;
    @Autowired
    private AppExternalConfigService appExternalConfigService;
    @Autowired
    private CiPipelineImageService ciPipelineImageService;
    @Autowired
    private CiPipelineMavenService ciPipelineMavenService;
    @Autowired
    private DevopsCiPipelineSonarService devopsCiPipelineSonarService;
    @Autowired
    private SonarAnalyseMeasureService sonarAnalyseMeasureService;
    @Autowired
    private DevopsCiSonarQualityGateService devopsCiSonarQualityGateService;
    @Autowired
    private DevopsCiUnitTestReportService devopsCiUnitTestReportService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    DevopsHostAppService devopsHostAppService;
    @Autowired
    private DevopsCiApiTestInfoService devopsCiApiTestInfoService;
    @Autowired
    private TestServiceClientOperator testServiceClientOperator;

    @Value("${services.gateway.url}")
    private String api;

    @Value("${devops.proxy.uriPrefix}")
    private String proxy;

    @Value("${nexus.proxy.urlDisplay:false}")
    private Boolean urlDisplay;

    @Autowired
    private JobOperator jobOperator;
    @Autowired
    private CiAuditRecordService ciAuditRecordService;
    @Autowired
    private CiAuditUserRecordService ciAuditUserRecordService;
    @Autowired
    protected DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    protected DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    protected AppServiceInstanceService appServiceInstanceService;
    @Autowired
    protected DevopsDeploymentService devopsDeploymentService;

    @Autowired
    protected DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;

    @Autowired
    protected TestServiceClientOperator testServiceClientoperator;

    @Autowired
    protected DevopsHostMapper devopsHostMapper;

    @Autowired
    protected SshUtil sshUtil;

    @Autowired
    protected DevopsHostCommandService devopsHostCommandService;

    @Autowired
    protected KeySocketSendHelper webSocketHelper;
    @Autowired
    protected DevopsHostAppMapper devopsHostAppMapper;

    @Autowired
    protected DevopsHostAppInstanceService devopsHostAppInstanceService;
    @Autowired
    protected HostConnectionHandler hostConnectionHandler;
    @Autowired
    protected DevopsHostService devopsHostService;
    @Autowired
    protected DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    protected DockerComposeService dockerComposeService;
    @Autowired
    protected DockerComposeValueService dockerComposeValueService;

    @Autowired
    private DevopsCiHostDeployInfoService devopsCiHostDeployInfoService;
    @Autowired
    private DevopsDockerInstanceMapper devopsDockerInstanceMapper;
    @Autowired
    private CiPipelineVlunScanRecordRelService ciPipelineVlunScanRecordRelService;


    // @lazy解决循环依赖
    public DevopsCiPipelineRecordServiceImpl(DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper,
                                             DevopsCiJobRecordService devopsCiJobRecordService,
                                             DevopsCiStageService devopsCiStageService,
                                             @Lazy DevopsCiJobService devopsCiJobService,
                                             DevopsCiJobRecordMapper devopsCiJobRecordMapper,
                                             @Lazy DevopsCiPipelineService devopsCiPipelineService,
                                             AppServiceService applicationService,
                                             TransactionalProducer transactionalProducer,
                                             UserAttrService userAttrService,
                                             AppServiceMapper appServiceMapper,
                                             CheckGitlabAccessLevelService checkGitlabAccessLevelService,
                                             BaseServiceClientOperator baseServiceClientOperator,
                                             GitlabServiceClientOperator gitlabServiceClientOperator,
                                             @Lazy CiPipelineSyncHandler ciPipelineSyncHandler,
                                             DevopsGitlabCommitService devopsGitlabCommitService,
                                             SendNotificationService sendNotificationService
    ) {
        this.devopsCiPipelineRecordMapper = devopsCiPipelineRecordMapper;
        this.devopsCiJobRecordService = devopsCiJobRecordService;
        this.devopsCiStageService = devopsCiStageService;
        this.devopsCiJobService = devopsCiJobService;
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
        this.devopsCiPipelineService = devopsCiPipelineService;
        this.applicationService = applicationService;
        this.transactionalProducer = transactionalProducer;
        this.userAttrService = userAttrService;
        this.baseServiceClientOperator = baseServiceClientOperator;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.devopsGitlabCommitService = devopsGitlabCommitService;
        this.ciPipelineSyncHandler = ciPipelineSyncHandler;
        this.checkGitlabAccessLevelService = checkGitlabAccessLevelService;
        this.appServiceMapper = appServiceMapper;
        this.sendNotificationService = sendNotificationService;
    }

    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = DEVOPS_GITLAB_CI_PIPELINE, description = "gitlab ci pipeline创建到数据库", inputSchemaClass = PipelineWebHookVO.class)
    public void create(PipelineWebHookVO pipelineWebHookVO, String token) {
        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
        if (devopsCiPipelineDTO == null || Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
            LOGGER.debug("Skip null of disabled pipeline for pipeline webhook with id {} and token: {}", pipelineWebHookVO.getObjectAttributes().getId(), token);
            return;
        }
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineDTO.getId());
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(devopsCiPipelineDTO.getId());
        Map<Long, DevopsCiStageDTO> stageMap = devopsCiStageDTOList.stream().collect(Collectors.toMap(DevopsCiStageDTO::getId, v -> v));
        Map<String, DevopsCiJobDTO> jobMap = devopsCiJobDTOS.stream().collect(Collectors.toMap(DevopsCiJobDTO::getName, v -> v));
        // 检验是否是手动修改gitlab-ci.yaml文件生成的流水线记录
        for (CiJobWebHookVO job : pipelineWebHookVO.getBuilds()) {
            DevopsCiJobDTO devopsCiJobDTO = CiCdPipelineUtils.judgeAndGetJob(job.getName(), jobMap);
            if (devopsCiJobDTO == null) {
                LOGGER.debug("Job Mismatch {} Skip the pipeline webhook...", job.getName());
                saveOrUpdateRecord(pipelineWebHookVO, appServiceDTO, devopsCiPipelineDTO);
                return;
            }
            DevopsCiStageDTO devopsCiStageDTO = stageMap.get(devopsCiJobDTO.getCiStageId());
            if (devopsCiStageDTO == null || !devopsCiStageDTO.getName().equals(job.getStage())) {
                LOGGER.debug("the stage name of the job {} mismatch...", job.getStage());
                saveOrUpdateRecord(pipelineWebHookVO, appServiceDTO, devopsCiPipelineDTO);
                return;
            } else {
                job.setType(devopsCiJobDTO.getType());
                job.setGroupType(devopsCiJobDTO.getGroupType());
                AbstractJobHandler handler = jobOperator.getHandler(devopsCiJobDTO.getType());
                if (handler != null) {
                    handler.fillJobAdditionalInfo(devopsCiJobDTO, job);
                }
            }
        }
        pipelineWebHookVO.setToken(token);
        try {
            String input = objectMapper.writeValueAsString(pipelineWebHookVO);
            transactionalProducer.apply(
                    StartSagaBuilder.newBuilder()
                            .withRefType("app")
                            .withRefId(appServiceDTO.getId().toString())
                            .withSagaCode(DEVOPS_GITLAB_CI_PIPELINE)
                            .withLevel(ResourceLevel.PROJECT)
                            .withSourceId(appServiceDTO.getProjectId())
                            .withJson(input),
                    builder -> {
                    });
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private void saveOrUpdateRecord(PipelineWebHookVO pipelineWebHookVO, AppServiceDTO appServiceDTO, CiCdPipelineDTO devopsCiPipelineDTO) {
        DevopsCiPipelineRecordDTO recordDTO = new DevopsCiPipelineRecordDTO();
        recordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
        recordDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(recordDTO);
        Long iamUserId;
        if (appServiceDTO.getExternalConfigId() == null) {
            iamUserId = userAttrService.getIamUserIdByGitlabUserName(pipelineWebHookVO.getUser().getUsername());
        } else {
            // 外置仓库默认使用admin账户执行
            iamUserId = IamAdminIdHolder.getAdminId();
        }
        //pipeline不存在则创建,存在则更新状态和阶段信息
        if (devopsCiPipelineRecordDTO == null) {
            LOGGER.debug("Start to create pipeline with gitlab pipeline id {}...", pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO = new DevopsCiPipelineRecordDTO();
            devopsCiPipelineRecordDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
            devopsCiPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setTriggerUserId(iamUserId);
            devopsCiPipelineRecordDTO.setCommitSha(pipelineWebHookVO.getObjectAttributes().getSha());
            devopsCiPipelineRecordDTO.setCreatedDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            devopsCiPipelineRecordDTO.setFinishedDate(pipelineWebHookVO.getObjectAttributes().getFinishedAt());
            devopsCiPipelineRecordDTO.setDurationSeconds(pipelineWebHookVO.getObjectAttributes().getDuration());
            devopsCiPipelineRecordDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsCiPipelineRecordDTO.setGitlabProjectId(pipelineWebHookVO.getProject().getId());
            devopsCiPipelineRecordDTO.setSource(pipelineWebHookVO.getObjectAttributes().getSource());
            devopsCiPipelineRecordDTO.setGitlabTriggerRef(pipelineWebHookVO.getObjectAttributes().getRef());
            devopsCiPipelineRecordMapper.insertSelective(devopsCiPipelineRecordDTO);
        } else {
            LOGGER.debug("Start to update pipeline with gitlab pipeline id {}...", pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setTriggerUserId(iamUserId);
            devopsCiPipelineRecordDTO.setCommitSha(pipelineWebHookVO.getObjectAttributes().getSha());
            devopsCiPipelineRecordDTO.setCreatedDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            devopsCiPipelineRecordDTO.setFinishedDate(pipelineWebHookVO.getObjectAttributes().getFinishedAt());
            devopsCiPipelineRecordDTO.setDurationSeconds(pipelineWebHookVO.getObjectAttributes().getDuration());
            devopsCiPipelineRecordDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsCiPipelineRecordDTO.setSource(pipelineWebHookVO.getObjectAttributes().getSource());
            devopsCiPipelineRecordMapper.updateByPrimaryKeySelective(devopsCiPipelineRecordDTO);
        }
    }

    @Override
    public void handleCreate(PipelineWebHookVO pipelineWebHookVO) {
        LOGGER.debug("Start to handle pipeline with gitlab pipeline id {}...", pipelineWebHookVO.getObjectAttributes().getId());
        AppServiceDTO applicationDTO = applicationService.baseQueryByToken(pipelineWebHookVO.getToken());
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(applicationDTO.getId());

        DevopsCiPipelineRecordDTO recordDTO = new DevopsCiPipelineRecordDTO();
        recordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
        recordDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(recordDTO);
        Long iamUserId;
        if (applicationDTO.getExternalConfigId() == null) {
            iamUserId = userAttrService.getIamUserIdByGitlabUserName(pipelineWebHookVO.getUser().getUsername());
        } else {
            iamUserId = applicationDTO.getCreatedBy();
        }

        CustomContextUtil.setDefaultIfNull(iamUserId);

        //pipeline不存在则创建,存在则更新状态和阶段信息
        if (devopsCiPipelineRecordDTO == null) {
            LOGGER.debug("Start to create pipeline with gitlab pipeline id {}...", pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO = new DevopsCiPipelineRecordDTO();
            devopsCiPipelineRecordDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
            devopsCiPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setTriggerUserId(iamUserId);
            devopsCiPipelineRecordDTO.setCommitSha(pipelineWebHookVO.getObjectAttributes().getSha());
            devopsCiPipelineRecordDTO.setCreatedDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            devopsCiPipelineRecordDTO.setFinishedDate(pipelineWebHookVO.getObjectAttributes().getFinishedAt());
            devopsCiPipelineRecordDTO.setDurationSeconds(pipelineWebHookVO.getObjectAttributes().getDuration());
            devopsCiPipelineRecordDTO.setQueuedDuration(pipelineWebHookVO.getObjectAttributes().getQueuedDuration());
            devopsCiPipelineRecordDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsCiPipelineRecordDTO.setGitlabProjectId(pipelineWebHookVO.getProject().getId());
            devopsCiPipelineRecordDTO.setSource(pipelineWebHookVO.getObjectAttributes().getSource());
            devopsCiPipelineRecordDTO.setGitlabTriggerRef(pipelineWebHookVO.getObjectAttributes().getRef());
            devopsCiPipelineRecordMapper.insertSelective(devopsCiPipelineRecordDTO);
            // 保存job执行记录
            Long pipelineRecordId = devopsCiPipelineRecordDTO.getId();
            saveJobRecords(pipelineWebHookVO,
                    pipelineRecordId,
                    devopsCiPipelineDTO.getId(),
                    applicationDTO.getId());

        } else {
            LOGGER.debug("Start to update pipeline with gitlab pipeline id {}...", pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setTriggerUserId(iamUserId);
            devopsCiPipelineRecordDTO.setCommitSha(pipelineWebHookVO.getObjectAttributes().getSha());
            devopsCiPipelineRecordDTO.setCreatedDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            devopsCiPipelineRecordDTO.setFinishedDate(pipelineWebHookVO.getObjectAttributes().getFinishedAt());
            devopsCiPipelineRecordDTO.setDurationSeconds(pipelineWebHookVO.getObjectAttributes().getDuration());
            devopsCiPipelineRecordDTO.setQueuedDuration(pipelineWebHookVO.getObjectAttributes().getQueuedDuration());
            devopsCiPipelineRecordDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsCiPipelineRecordDTO.setSource(pipelineWebHookVO.getObjectAttributes().getSource());
            devopsCiPipelineRecordMapper.updateByPrimaryKeySelective(devopsCiPipelineRecordDTO);
            // 更新job状态
            // 保存job执行记录
            Long pipelineRecordId = devopsCiPipelineRecordDTO.getId();
            saveJobRecords(pipelineWebHookVO,
                    pipelineRecordId,
                    devopsCiPipelineDTO.getId(),
                    applicationDTO.getId());
        }
        if (pipelineWebHookVO.getObjectAttributes().getStatus().equals(JobStatusEnum.FAILED.value())) {
            sendNotificationService.sendCiPipelineNotice(devopsCiPipelineRecordDTO.getId(),
                    MessageCodeConstants.PIPELINE_FAILED,
                    devopsCiPipelineRecordDTO.getCreatedBy(),
                    null,
                    new HashMap<>());
        } else if (pipelineWebHookVO.getObjectAttributes().getStatus().equals(JobStatusEnum.SUCCESS.value())) {
            sendNotificationService.sendCiPipelineNotice(devopsCiPipelineRecordDTO.getId(),
                    MessageCodeConstants.PIPELINE_SUCCESS,
                    devopsCiPipelineRecordDTO.getCreatedBy(),
                    null,
                    new HashMap<>());

        }

    }

    private void saveJobRecords(PipelineWebHookVO pipelineWebHookVO, Long pipelineRecordId, Long ciPipelineId, Long appServiceId) {
        pipelineWebHookVO.getBuilds().forEach(ciJobWebHookVO -> {
            DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceId, ciJobWebHookVO.getId());
            boolean statusChangedFlag = true;
            if (devopsCiJobRecordDTO == null) {
                LOGGER.debug("Start to create job with gitlab job id {}...", ciJobWebHookVO.getId());
                devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
                devopsCiJobRecordDTO.setGitlabJobId(ciJobWebHookVO.getId());
                devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                devopsCiJobRecordDTO.setDurationSeconds(ciJobWebHookVO.getDuration());
                devopsCiJobRecordDTO.setStage(ciJobWebHookVO.getStage());
                devopsCiJobRecordDTO.setType(ciJobWebHookVO.getType());
                devopsCiJobRecordDTO.setGroupType(ciJobWebHookVO.getGroupType());
                devopsCiJobRecordDTO.setName(ciJobWebHookVO.getName());
                devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus());
                devopsCiJobRecordDTO.setTriggerUserId(userAttrService.getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                devopsCiJobRecordDTO.setGitlabProjectId(pipelineWebHookVO.getProject().getId());
                devopsCiJobRecordDTO.setAppServiceId(appServiceId);
                devopsCiJobRecordMapper.insertSelective(devopsCiJobRecordDTO);
            } else {
                LOGGER.debug("Start to update job with gitlab job id {}...", ciJobWebHookVO.getId());
                statusChangedFlag = !devopsCiJobRecordDTO.getStatus().equals(ciJobWebHookVO.getStatus());
                devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                devopsCiJobRecordDTO.setDurationSeconds(ciJobWebHookVO.getDuration());
                devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus());
                devopsCiJobRecordDTO.setTriggerUserId(userAttrService.getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiJobRecordMapper, devopsCiJobRecordDTO, DEVOPS_UPDATE_CI_JOB_RECORD, ciJobWebHookVO.getId());
            }
            AbstractJobHandler handler = jobOperator.getHandler(ciJobWebHookVO.getType());
            if (handler != null) {
                handler.saveAdditionalRecordInfo(ciPipelineId, devopsCiJobRecordDTO, pipelineWebHookVO.getObjectAttributes().getId(), ciJobWebHookVO);
            }
            //如果当前任务状态为manual且任务类型为audit则发送审核邮件
            // 存在的问题，同一阶段内存在多个人工卡点任务时，当某个审核任务通过时，其他处于manual状态的任务都会再收到一次审核通知
            if (io.choerodon.devops.infra.dto.gitlab.ci.PipelineStatus.MANUAL.toValue().equals(ciJobWebHookVO.getStatus())) {
                // 重试人工审核任务时，自动执行
                if (ciAuditRecordService.queryAuditRecordIsFinish(devopsCiJobRecordDTO.getAppServiceId(),
                        pipelineWebHookVO.getObjectAttributes().getId(),
                        devopsCiJobRecordDTO.getName())) {
                    AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
                    AppExternalConfigDTO appExternalConfigDTO = null;
                    if (appServiceDTO.getExternalConfigId() != null) {
                        appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
                    }
                    gitlabServiceClientOperator.playJob(TypeUtil.objToInteger(pipelineWebHookVO.getProject().getId()),
                            TypeUtil.objToInteger(ciJobWebHookVO.getId()),
                            null,
                            appExternalConfigDTO);
                } else if (statusChangedFlag) {
                    ciAuditRecordService.sendJobAuditMessage(devopsCiJobRecordDTO.getAppServiceId(),
                            ciPipelineId,
                            pipelineRecordId,
                            pipelineWebHookVO.getObjectAttributes().getId(),
                            devopsCiJobRecordDTO.getName(),
                            ciJobWebHookVO.getStage());
                }
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Async(GitOpsConstants.PIPELINE_EXECUTOR)
    @Override
    public void asyncPipelineUpdate(Long pipelineRecordId, Integer gitlabPipelineId) {
        syncPipelineUpdate(pipelineRecordId, gitlabPipelineId);
    }

    @Override
    public void syncPipelineUpdate(Long pipelineRecordId, Integer gitlabPipelineId) {
        LOGGER.info("Start to update pipeline asynchronously...record id {}, gitlab pipeline id {}", pipelineRecordId, gitlabPipelineId);
        Assert.notNull(pipelineRecordId, PipelineCheckConstant.DEVOPS_PIPELINE_RECORD_ID_IS_NULL);

        AppServiceDTO appServiceDTO = devopsCiPipelineRecordMapper.queryGitlabProjectIdByRecordId(pipelineRecordId);
        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());

        Integer gitlabProjectId = appServiceDTO.getGitlabProjectId();
        GitlabPipelineDTO pipelineDTO = gitlabServiceClientOperator.queryPipeline(TypeUtil.objToInteger(gitlabProjectId),
                TypeUtil.objToInteger(gitlabPipelineId),
                null,
                appExternalConfigDTO);
        if (pipelineDTO != null) {
            List<JobDTO> jobDTOList = gitlabServiceClientOperator.listJobs(gitlabProjectId,
                    gitlabPipelineId,
                    null,
                    appExternalConfigDTO);

            Long gitlabPipelineIdLong = TypeUtil.objToLong(gitlabPipelineId);
            handUpdate(appServiceDTO, pipelineRecordId, gitlabPipelineIdLong, pipelineDTO, jobDTOList);
        } else {
            devopsCiPipelineRecordMapper.updateStatusByGitlabPipelineId(pipelineRecordId, io.choerodon.devops.infra.dto.gitlab.ci.PipelineStatus.CANCELED.toValue());
        }

    }

    private void handUpdate(AppServiceDTO appServiceDTO, Long pipelineRecordId, Long gitlabPipelineId, GitlabPipelineDTO gitlabPipelineDTO, List<JobDTO> jobs) {
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());

        DevopsCiPipelineRecordDTO recordDTO = new DevopsCiPipelineRecordDTO();
        recordDTO.setGitlabPipelineId(gitlabPipelineId);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
        CustomContextUtil.setDefault();

        LOGGER.debug("Start to update pipeline with gitlab pipeline id {}...", gitlabPipelineId);
        devopsCiPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsCiPipelineRecordDTO.setDurationSeconds(TypeUtil.objToLong(gitlabPipelineDTO.getDuration()));
        devopsCiPipelineRecordDTO.setStatus(gitlabPipelineDTO.getStatus());
        devopsCiPipelineRecordMapper.updateByPrimaryKeySelective(devopsCiPipelineRecordDTO);


        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineDTO.getId());
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(devopsCiPipelineDTO.getId());
        Map<Long, DevopsCiStageDTO> stageMap = devopsCiStageDTOList.stream().collect(Collectors.toMap(DevopsCiStageDTO::getId, v -> v));
        Map<String, DevopsCiJobDTO> jobMap = devopsCiJobDTOS.stream().collect(Collectors.toMap(DevopsCiJobDTO::getName, v -> v));

        // 检验是否是手动修改gitlab-ci.yaml文件生成的流水线记录
        // 如果不符合流水线设置， 提前退出， 只同步流水线的状态， stage的跳过
        Map<Integer, String> jobType = new HashMap<>();
        for (JobDTO job : jobs) {
            DevopsCiJobDTO devopsCiJobDTO = CiCdPipelineUtils.judgeAndGetJob(job.getName(), jobMap);
            if (devopsCiJobDTO == null) {
                LOGGER.debug("Job Mismatch {} Skip the pipeline webhook...", job.getName());
                return;
            } else {
                DevopsCiStageDTO devopsCiStageDTO = stageMap.get(devopsCiJobDTO.getCiStageId());
                if (devopsCiStageDTO == null || !devopsCiStageDTO.getName().equals(job.getStage())) {
                    LOGGER.debug("the stage name of the job {} mismatch...", job.getStage());
                    return;
                } else {
                    jobType.put(job.getId(), devopsCiJobDTO.getType());
                }
            }
        }

        // 更新job状态
        // 保存job执行记录
        saveJobRecords(TypeUtil.objToLong(appServiceDTO.getGitlabProjectId()), pipelineRecordId, jobs, jobType, appServiceDTO.getId());
    }

    private void saveJobRecords(Long gitlabProjectId, Long pipelineRecordId, List<JobDTO> jobs, Map<Integer, String> jobType, Long appServiceId) {
        jobs.forEach(ciJobWebHookVO -> {
            Long jobId = TypeUtil.objToLong(ciJobWebHookVO.getId());
            DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceId, jobId);
            if (devopsCiJobRecordDTO == null) {
                LOGGER.debug("Start to create job with gitlab job id {}...", ciJobWebHookVO.getId());
                devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
                devopsCiJobRecordDTO.setGitlabJobId(jobId);
                devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                devopsCiJobRecordDTO.setStage(ciJobWebHookVO.getStage());
                devopsCiJobRecordDTO.setType(jobType.get(ciJobWebHookVO.getId()));
                devopsCiJobRecordDTO.setName(ciJobWebHookVO.getName());
                devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus().toValue());
                devopsCiJobRecordDTO.setGitlabProjectId(gitlabProjectId);
                devopsCiJobRecordDTO.setAppServiceId(appServiceId);
                devopsCiJobRecordDTO.setTriggerUserId(userAttrService.getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                devopsCiJobRecordMapper.insertSelective(devopsCiJobRecordDTO);
            } else {
                LOGGER.debug("Start to update job with gitlab job id {}...", ciJobWebHookVO.getId());
                devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus().toValue());
                devopsCiJobRecordDTO.setTriggerUserId(userAttrService.getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiJobRecordMapper, devopsCiJobRecordDTO, DEVOPS_UPDATE_CI_JOB_RECORD, ciJobWebHookVO.getId());
            }
        });
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

    @Override
    public DevopsCiPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long ciPipelineRecordId) {
        if (ciPipelineRecordId == null || ciPipelineRecordId == 0L) {
            return new DevopsCiPipelineRecordVO();
        }
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(ciPipelineRecordId);

        if (Objects.isNull(devopsCiPipelineRecordDTO)) {
            return new DevopsCiPipelineRecordVO();
        }
        Long devopsPipelineId = devopsCiPipelineRecordDTO.getCiPipelineId();
        Long pipelineRecordDTOId = devopsCiPipelineRecordDTO.getId();
        Long gitlabPipelineId = devopsCiPipelineRecordDTO.getGitlabPipelineId();
        Long userId = DetailsHelper.getUserDetails().getUserId();


        // 如果流水线状态为running则尝试同步记录状态
        if (PipelineStatus.RUNNING.toValue().equals(devopsCiPipelineRecordDTO.getStatus())) {
            if (!ciPipelineSyncHandler.inFetchPeriod(gitlabPipelineId.intValue())) {
                syncPipelineUpdate(devopsCiPipelineRecordDTO.getId(), gitlabPipelineId.intValue());
            }
        }

        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = ConvertUtils.convertObject(devopsCiPipelineRecordDTO, DevopsCiPipelineRecordVO.class);
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(devopsCiPipelineRecordDTO.getTriggerUserId());
        if (!Objects.isNull(iamUserDTO)) {
            devopsCiPipelineRecordVO.setIamUserDTO(iamUserDTO);
            devopsCiPipelineRecordVO.setUsername(iamUserDTO.getRealName());
        }
        devopsCiPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordDTO.getCreationDate());

        // 添加提交信息
        CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(devopsPipelineId);
        Long appServiceId = ciCdPipelineVO.getAppServiceId();

        devopsCiPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
        addCommitInfo(appServiceId, devopsCiPipelineRecordVO, devopsCiPipelineRecordDTO);

        // 查询流水线记录下的job记录
        DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
        recordDTO.setCiPipelineRecordId(pipelineRecordDTOId);
        List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS = devopsCiJobRecordMapper.select(recordDTO);

        if (CollectionUtils.isEmpty(devopsCiJobRecordDTOS)
                && !PipelineStatus.SKIPPED.toValue().equals(devopsCiPipelineRecordDTO.getStatus())) {

            AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
            GitlabPipelineDTO pipelineDTO = gitlabServiceClientOperator.queryPipeline(TypeUtil.objToInteger(appServiceDTO.getGitlabProjectId()),
                    TypeUtil.objToInteger(gitlabPipelineId),
                    null,
                    appServiceDTO.getAppExternalConfigDTO());
            devopsCiPipelineRecordVO.setUnrelatedFlag(true);
            devopsCiPipelineRecordVO.setGitlabPipelineUrl(pipelineDTO.getWebUrl());
        }

        Map<String, List<DevopsCiJobRecordDTO>> jobRecordMap = devopsCiJobRecordDTOS.stream()
                .sorted(Comparator.comparing(DevopsCiJobRecordDTO::getId))
                .collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStage));

        List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = new ArrayList<>();
        List<DevopsCiPipelineAuditVO> pipelineAuditInfo = new ArrayList<>();
        for (Map.Entry<String, List<DevopsCiJobRecordDTO>> entry : jobRecordMap.entrySet()) {
            String k = entry.getKey();
            List<DevopsCiJobRecordDTO> value = entry.getValue();
            DevopsCiStageRecordVO devopsCiStageRecordVO = new DevopsCiStageRecordVO();
            devopsCiStageRecordVO.setName(k);
            devopsCiStageRecordVO.setType(StageType.CI.getType());
            value.stream().min(Comparator.comparing(DevopsCiJobRecordDTO::getGitlabJobId)).ifPresent(i -> devopsCiStageRecordVO.setSequence(i.getGitlabJobId()));
            // 只返回job的最新记录
            List<DevopsCiJobRecordDTO> latestedsCiJobRecordDTOS = filterJobs(value);
            calculateStageStatus(devopsCiStageRecordVO, latestedsCiJobRecordDTOS);
            List<DevopsCiJobRecordVO> latestedsCiJobRecordVOS = ConvertUtils.convertList(latestedsCiJobRecordDTOS, DevopsCiJobRecordVO.class);
            for (DevopsCiJobRecordVO devopsCiJobRecordVO : latestedsCiJobRecordVOS) {// 添加chart版本信息
                fillChartInfo(appServiceId, gitlabPipelineId, devopsCiJobRecordVO);
                // 添加Sonar扫描信息
                fillSonarInfo(appServiceId, gitlabPipelineId, devopsCiJobRecordVO);

                //如果是构建类型 填充jar下载地址，镜像地址，扫描结果
                fillJarInfo(projectId, appServiceId, gitlabPipelineId, devopsCiJobRecordVO);
                fillDockerInfo(appServiceId, gitlabPipelineId, devopsCiJobRecordVO);
                //是否本次流水线有镜像的扫描结果 有则展示
                fillImageScanInfo(appServiceId, gitlabPipelineId, devopsCiJobRecordVO);
                fillUnitTestInfo(appServiceId, gitlabPipelineId, devopsCiJobRecordVO);
                // 添加人工审核记录信息
                fillAuditInfo(appServiceId, gitlabPipelineId, devopsCiJobRecordVO);
                // 添加当前用户流水线待审核任务列表
                addPipelineAuditInfo(gitlabPipelineId, userId, appServiceId, pipelineAuditInfo, devopsCiJobRecordVO);
                // 添加chart部署记录信息
                addChartDeployInfo(devopsCiJobRecordVO);
                // 添加部署组部署记录信息
                addDeploymentDeployInfo(devopsCiJobRecordVO);
                // 添加主机部署记录信息
                addHostDeployInfo(devopsCiJobRecordVO);
                // 添加api测试执行信息
                addApiTestInfo(devopsCiJobRecordVO);
                // 添加漏洞扫描记录信息
                addVulnInfo(appServiceId, gitlabPipelineId, devopsCiJobRecordVO);

            }
            devopsCiStageRecordVO.setDurationSeconds(calculateStageDuration(latestedsCiJobRecordVOS));
            // 按照 id正序排序
            latestedsCiJobRecordVOS.sort(Comparator.comparingLong(DevopsCiJobRecordVO::getId));
            devopsCiStageRecordVO.setJobRecordVOList(latestedsCiJobRecordVOS);

            devopsCiStageRecordVOS.add(devopsCiStageRecordVO);
        }
        // stage排序
        devopsCiStageRecordVOS = devopsCiStageRecordVOS.stream().sorted(Comparator.comparing(DevopsCiStageRecordVO::getSequence)).filter(v -> v.getStatus() != null).collect(Collectors.toList());
        devopsCiPipelineRecordVO.setStageRecordVOS(devopsCiStageRecordVOS);
        if (!CollectionUtils.isEmpty(pipelineAuditInfo)) {
            devopsCiPipelineRecordVO.setPipelineAuditInfo(pipelineAuditInfo.stream().sorted(Comparator.comparing(DevopsCiPipelineAuditVO::getJobRecordId)).collect(Collectors.toList()));
        }
        devopsCiPipelineRecordVO.setViewId(CiCdPipelineUtils.handleId(devopsCiPipelineRecordVO.getId()));
        return devopsCiPipelineRecordVO;
    }

    private void addVulnInfo(Long appServiceId, Long gitlabPipelineId, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        devopsCiJobRecordVO.setVulnSacnRecordInfo(ciPipelineVlunScanRecordRelService.queryScanRecordInfo(appServiceId, gitlabPipelineId, devopsCiJobRecordVO.getName()));
    }

    private void addApiTestInfo(DevopsCiJobRecordVO devopsCiJobRecordVO) {
        try {
            DevopsCiApiTestInfoDTO ciApiTestInfoDTO = devopsCiApiTestInfoService.selectById(devopsCiJobRecordVO.getConfigId());
            if (ciApiTestInfoDTO != null) {
                ApiTestTaskRecordVO apiTestTaskRecordVO = testServiceClientOperator.queryById(ciApiTestInfoDTO.getProjectId(), devopsCiJobRecordVO.getApiTestTaskRecordId());
                apiTestTaskRecordVO.setDeployJobName(devopsCiJobRecordVO.getName());
                apiTestTaskRecordVO.setPerformThreshold(ciApiTestInfoDTO.getPerformThreshold());
                apiTestTaskRecordVO.setTaskType(ciApiTestInfoDTO.getTaskType());
                devopsCiJobRecordVO.setApiTestTaskRecordVO(apiTestTaskRecordVO);
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed to query api test task record..., the ex code is {}", ex.getMessage());
        }
    }

    private void addHostDeployInfo(DevopsCiJobRecordVO devopsCiJobRecordVO) {
        if (CiJobTypeEnum.HOST_DEPLOY.value().equals(devopsCiJobRecordVO.getType())
                && io.choerodon.devops.infra.dto.gitlab.ci.PipelineStatus.SUCCESS.toValue().equals(devopsCiJobRecordVO.getStatus())) {
            Long commandId = devopsCiJobRecordVO.getCommandId();
            if (commandId != null) {
                DeployRecordVO deployRecordVO = devopsDeployRecordService.queryHostDeployRecordByCommandId(commandId);
                DeployInfo deployInfo = new DeployInfo();
                deployInfo.setAppId(deployRecordVO.getAppId());
                deployInfo.setAppName(deployRecordVO.getAppName());
                deployInfo.setHostName(deployRecordVO.getDeployPayloadName());
                deployInfo.setDeployType(HOST);
                deployInfo.setDeployTypeId(deployRecordVO.getDeployPayloadId());
                deployInfo.setOperationType(deployRecordVO.getDeployObjectType());
                deployInfo.setRdupmType(deployRecordVO.getDeployObjectType());
                DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(deployRecordVO.getAppId());
                if (!ObjectUtils.isEmpty(devopsHostAppDTO)) {
                    deployInfo.setOperationType(devopsHostAppDTO.getOperationType());
                }
                devopsCiJobRecordVO.setDeployInfo(deployInfo);
            }
        }

    }

    private void addDeploymentDeployInfo(DevopsCiJobRecordVO devopsCiJobRecordVO) {
        if (CiJobTypeEnum.DEPLOYMENT_DEPLOY.value().equals(devopsCiJobRecordVO.getType())
                && io.choerodon.devops.infra.dto.gitlab.ci.PipelineStatus.SUCCESS.toValue().equals(devopsCiJobRecordVO.getStatus())) {
            Long commandId = devopsCiJobRecordVO.getCommandId();
            if (commandId != null) {
                DeployRecordVO deployRecordVO = devopsDeployRecordService.queryEnvDeployRecordByCommandId(commandId);
                if (deployRecordVO != null) {
                    DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(deployRecordVO.getAppId());
                    DeployInfo deployInfo = new DeployInfo();
                    deployInfo.setEnvId(deployRecordVO.getEnvId());
                    deployInfo.setEnvName(deployRecordVO.getDeployPayloadName());
                    deployInfo.setAppId(deployRecordVO.getAppId());
                    deployInfo.setAppName(deployRecordVO.getAppName());
                    if (!ObjectUtils.isEmpty(devopsDeployAppCenterEnvDTO)) {
                        deployInfo.setOperationType(devopsDeployAppCenterEnvDTO.getOperationType());
                        deployInfo.setRdupmType(devopsDeployAppCenterEnvDTO.getRdupmType());
                        deployInfo.setDeployType(MiscConstants.ENV);
                        deployInfo.setDeployTypeId(devopsDeployAppCenterEnvDTO.getEnvId());
                        if (RdupmTypeEnum.DEPLOYMENT.value().equals(devopsDeployAppCenterEnvDTO.getRdupmType())) {
                            DevopsDeploymentDTO deploymentDTO = devopsDeploymentService.selectByPrimaryKey(devopsDeployAppCenterEnvDTO.getObjectId());
                            if (!ObjectUtils.isEmpty(deploymentDTO)) {
                                deployInfo.setStatus(deploymentDTO.getStatus());
                            }
                        }
                    }
                    devopsCiJobRecordVO.setDeployInfo(deployInfo);
                }
            }
        }


    }

    private void addChartDeployInfo(DevopsCiJobRecordVO devopsCiJobRecordVO) {
        if (CiJobTypeEnum.CHART_DEPLOY.value().equals(devopsCiJobRecordVO.getType())
                && io.choerodon.devops.infra.dto.gitlab.ci.PipelineStatus.SUCCESS.toValue().equals(devopsCiJobRecordVO.getStatus())) {
            Long commandId = devopsCiJobRecordVO.getCommandId();
            if (commandId != null) {
                DeployRecordVO deployRecordVO = devopsDeployRecordService.queryEnvDeployRecordByCommandId(commandId);
                if (deployRecordVO != null) {
                    DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(deployRecordVO.getAppId());
                    DeployInfo deployInfo = new DeployInfo();
                    deployInfo.setAppServiceName(deployRecordVO.getDeployObjectName());
                    deployInfo.setAppServiceVersion(deployRecordVO.getDeployObjectVersion());
                    deployInfo.setEnvId(deployRecordVO.getEnvId());
                    deployInfo.setEnvName(deployRecordVO.getDeployPayloadName());
                    deployInfo.setAppId(deployRecordVO.getAppId());
                    deployInfo.setAppName(deployRecordVO.getAppName());
                    if (!ObjectUtils.isEmpty(devopsDeployAppCenterEnvDTO)) {
                        deployInfo.setRdupmType(devopsDeployAppCenterEnvDTO.getRdupmType());
                        deployInfo.setChartSource(devopsDeployAppCenterEnvDTO.getChartSource());
                        deployInfo.setOperationType(devopsDeployAppCenterEnvDTO.getOperationType());
                        deployInfo.setDeployType(MiscConstants.ENV);
                        deployInfo.setDeployTypeId(devopsDeployAppCenterEnvDTO.getEnvId());
                        if (RdupmTypeEnum.CHART.value().equals(devopsDeployAppCenterEnvDTO.getRdupmType())) {
                            AppServiceInstanceInfoVO appServiceInstanceInfoVO = appServiceInstanceService.queryInfoById(devopsDeployAppCenterEnvDTO.getProjectId(), devopsDeployAppCenterEnvDTO.getObjectId());
                            if (!ObjectUtils.isEmpty(appServiceInstanceInfoVO)) {
                                deployInfo.setAppServiceId(appServiceInstanceInfoVO.getAppServiceId());
                                deployInfo.setStatus(appServiceInstanceInfoVO.getStatus());
                                deployInfo.setPodCount(appServiceInstanceInfoVO.getPodCount());
                                deployInfo.setPodRunningCount(appServiceInstanceInfoVO.getPodRunningCount());
                            }
                        }
                    }
                    devopsCiJobRecordVO.setDeployInfo(deployInfo);
                }
            }
        }
    }

    private void addPipelineAuditInfo(Long gitlabPipelineId, Long userId, Long appServiceId, List<DevopsCiPipelineAuditVO> pipelineAuditInfo, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        if (CiJobTypeEnum.AUDIT.value().equals(devopsCiJobRecordVO.getType())
                && io.choerodon.devops.infra.dto.gitlab.ci.PipelineStatus.MANUAL.toValue().equals(devopsCiJobRecordVO.getStatus())) {
            CiAuditRecordDTO ciAuditRecordDTO = ciAuditRecordService.queryByUniqueOption(appServiceId, gitlabPipelineId, devopsCiJobRecordVO.getName());
            if (ciAuditRecordDTO != null) {
                List<CiAuditUserRecordDTO> auditUserRecordDTOList = ciAuditUserRecordService.listByAuditRecordId(ciAuditRecordDTO.getId());
                if (!CollectionUtils.isEmpty(auditUserRecordDTOList)) {
                    if (auditUserRecordDTOList.stream().anyMatch(r -> r.getUserId().equals(userId) && AuditStatusEnum.NOT_AUDIT.value().equals(r.getStatus()))) {
                        DevopsCiPipelineAuditVO devopsCiPipelineAuditVO = new DevopsCiPipelineAuditVO(devopsCiJobRecordVO.getName(), devopsCiJobRecordVO.getId(), true);
                        pipelineAuditInfo.add(devopsCiPipelineAuditVO);
                    }
                }
            }
        }
    }

    private void fillAuditInfo(Long appServiceId, Long gitlabPipelineId, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        CiAuditRecordDTO ciAuditRecordDTO = ciAuditRecordService.queryByUniqueOption(appServiceId, gitlabPipelineId, devopsCiJobRecordVO.getName());
        if (ciAuditRecordDTO != null) {
            List<CiAuditUserRecordDTO> auditUserRecordDTOList = ciAuditUserRecordService.listByAuditRecordId(ciAuditRecordDTO.getId());
            if (!CollectionUtils.isEmpty(auditUserRecordDTOList)) {

                List<Long> uids = auditUserRecordDTOList.stream().map(CiAuditUserRecordDTO::getUserId).collect(Collectors.toList());
                List<IamUserDTO> allIamUserDTOS = baseServiceClientOperator.listUsersByIds(uids);
                List<Long> reviewedUids = auditUserRecordDTOList.stream()
                        .filter(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus()) || AuditStatusEnum.REFUSED.value().equals(v.getStatus()))
                        .map(CiAuditUserRecordDTO::getUserId).collect(Collectors.toList());
                List<IamUserDTO> reviewedUsers = allIamUserDTOS.stream().filter(u -> reviewedUids.contains(u.getId())).collect(Collectors.toList());
                Audit audit = new Audit(allIamUserDTOS, reviewedUsers, devopsCiJobRecordVO.getStatus(), ciAuditRecordDTO.getCountersigned());
                if (PipelineStatus.MANUAL.toValue().equals(devopsCiJobRecordVO.getStatus())) {
                    Long userId = DetailsHelper.getUserDetails().getUserId();
                    audit.setCanAuditFlag(auditUserRecordDTOList.stream().anyMatch(r -> r.getUserId().equals(userId) && AuditStatusEnum.NOT_AUDIT.value().equals(r.getStatus())));
                }
                devopsCiJobRecordVO.setAudit(audit);
            }
        }
    }

    /**
     * 填充单元测试信息
     *
     * @param appServiceId
     * @param gitlabPipelineId
     * @param devopsCiJobRecordVO
     */
    private void fillUnitTestInfo(Long appServiceId, Long gitlabPipelineId, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        List<DevopsCiUnitTestReportDTO> devopsCiUnitTestReportDTOS = devopsCiUnitTestReportService.listByJobName(appServiceId,
                gitlabPipelineId,
                devopsCiJobRecordVO.getName());

        List<DevopsCiUnitTestReportVO> devopsCiUnitTestReportVOS = devopsCiUnitTestReportDTOS.stream().map(v -> {
            DevopsCiUnitTestReportVO devopsCiUnitTestReportVO = ConvertUtils.convertObject(v, DevopsCiUnitTestReportVO.class);
            double successRate = 0;
            if (devopsCiUnitTestReportVO.getTests() != 0) {
                successRate = FractionUtil.fraction((devopsCiUnitTestReportVO.getPasses() * 1.0d / devopsCiUnitTestReportVO.getTests()), 2) * 100;
            }
            devopsCiUnitTestReportVO.setSuccessRate(successRate);
            return devopsCiUnitTestReportVO;
        }).collect(Collectors.toList());
        devopsCiJobRecordVO.setDevopsCiUnitTestReportInfoList(devopsCiUnitTestReportVOS);
    }

    private void fillImageScanInfo(Long appServiceId, Long gitlabPipelineId, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        DevopsImageScanResultDTO devopsImageScanResultDTO = new DevopsImageScanResultDTO();
        devopsImageScanResultDTO.setAppServiceId(appServiceId);
        devopsImageScanResultDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsImageScanResultDTO.setJobName(devopsCiJobRecordVO.getName());
        if (devopsImageScanResultMapper.selectCount(devopsImageScanResultDTO) > 0) {
            devopsCiJobRecordVO.setImageScan(Boolean.TRUE);
        } else {
            devopsCiJobRecordVO.setImageScan(Boolean.FALSE);
        }
    }

    private void fillChartInfo(Long appServiceId, Long gitlabPipelineId, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        DevopsCiPipelineChartDTO devopsCiPipelineChartDTO = devopsCiPipelineChartService.queryByPipelineIdAndJobName(appServiceId,
                gitlabPipelineId,
                devopsCiJobRecordVO.getName());
        if (devopsCiPipelineChartDTO != null) {
            devopsCiJobRecordVO.setPipelineChartInfo(new PipelineChartInfo(devopsCiPipelineChartDTO.getChartVersion()));
        }
    }


    private void fillSonarInfo(Long appServiceId, Long gitlabPipelineId, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO = devopsCiPipelineSonarService.queryByPipelineId(appServiceId, gitlabPipelineId, devopsCiJobRecordVO.getName());
        if (devopsCiPipelineSonarDTO != null && devopsCiPipelineSonarDTO.getRecordId() != null) {

            List<SonarAnalyseMeasureDTO> sonarAnalyseMeasureDTOS = sonarAnalyseMeasureService.listByRecordId(devopsCiPipelineSonarDTO.getRecordId());

            List<SonarContentVO> sonarContents = new ArrayList<>();
            DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = null;
            for (SonarAnalyseMeasureDTO sonarAnalyseMeasureDTO : sonarAnalyseMeasureDTOS) {
                if (SonarQubeType.BUGS.getType().equals(sonarAnalyseMeasureDTO.getMetric())
                        || SonarQubeType.VULNERABILITIES.getType().equals(sonarAnalyseMeasureDTO.getMetric())
                        || SonarQubeType.CODE_SMELLS.getType().equals(sonarAnalyseMeasureDTO.getMetric())) {
                    sonarContents.add(new SonarContentVO(sonarAnalyseMeasureDTO.getMetric(), sonarAnalyseMeasureDTO.getMetricValue()));
                }
                if (SonarQubeType.SQALE_INDEX.getType().equals(sonarAnalyseMeasureDTO.getMetric())) {
                    sonarContents.add(new SonarContentVO(sonarAnalyseMeasureDTO.getMetric(), SonarUtil.caculateSqaleIndex(Long.parseLong(sonarAnalyseMeasureDTO.getMetricValue()))));
                }
                if (SonarQubeType.QUALITY_GATE_DETAILS.getType().equals(sonarAnalyseMeasureDTO.getMetric())) {
                    QualityGateResult qualityGateResult = JsonHelper.unmarshalByJackson(sonarAnalyseMeasureDTO.getMetricValue(), QualityGateResult.class);
                    devopsCiSonarQualityGateVO = devopsCiSonarQualityGateService.buildFromSonarResult(qualityGateResult);
                }
            }

            devopsCiJobRecordVO.setPipelineSonarInfo(new PipelineSonarInfo(devopsCiPipelineSonarDTO.getScannerType(), sonarContents, devopsCiSonarQualityGateVO));
//            SonarContentsVO sonarContentsVO = null;
//            try {
//                sonarContentsVO = applicationService.getSonarContentFromCache(projectId, appServiceId);
//                if (!Objects.isNull(sonarContentsVO) && !CollectionUtils.isEmpty(sonarContentsVO.getSonarContents())) {
//                    List<SonarContentVO> sonarContents = sonarContentsVO.getSonarContents();
//                    List<SonarContentVO> sonarContentVOS = sonarContents.stream().filter(sonarContentVO -> SonarQubeType.BUGS.getType().equals(sonarContentVO.getKey())
//                            || SonarQubeType.CODE_SMELLS.getType().equals(sonarContentVO.getKey())
//                            || SonarQubeType.VULNERABILITIES.getType().equals(sonarContentVO.getKey())
//                            || SonarQubeType.SQALE_INDEX.getType().equals(sonarContentVO.getKey())).collect(Collectors.toList());
//
//                    sonarContents.forEach(v -> {
//                        if (SonarQubeType.COVERAGE.getType().equals(v.getKey())) {
//                            devopsCiJobRecordVO.setCodeCoverage(v.getValue());
//                        }
//                    });
//                    devopsCiJobRecordVO.setPipelineSonarInfo(new PipelineSonarInfo(devopsCiPipelineSonarDTO.getScannerType(), sonarContentVOS, sonarContentsVO.getDevopsCiSonarQualityGateVO()));
//                }
//            } catch (Exception e) {
//                LOGGER.error("Fill sonar info failed", e);
//            }

        }
    }

    private void fillDockerInfo(Long appServiceId, Long gitlabPipelineId, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        CiPipelineImageDTO pipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(appServiceId,
                gitlabPipelineId,
                devopsCiJobRecordVO.getName());
        if (pipelineImageDTO == null) {
            return;
        }
        devopsCiJobRecordVO.setPipelineImageInfo(new PipelineImageInfoVO(pipelineImageDTO.getImageTag(),
                "docker pull " + pipelineImageDTO.getImageTag()));
    }

    private void fillJarInfo(Long projectId, Long appServiceId, Long gitlabPipelineId, DevopsCiJobRecordVO devopsCiJobRecordVO) {
        CiPipelineMavenDTO pipelineMavenDTO = ciPipelineMavenService.queryByGitlabPipelineId(appServiceId,
                gitlabPipelineId,
                devopsCiJobRecordVO.getName());
        if (Objects.isNull(pipelineMavenDTO)) {
            return;
        }
        //返回代理地址的仓库和用户名密码
        //如果在一个job里面多次发布，那么取seq最大的 最后的一次发布的结果。
        //这里不是devopsCiJobDTO的MavenSettings 而是devopsCiJobDTORecord的MavenSettings
        // 将maven的setting文件转换为java对象
        String downloadUrl = null;
        Server server = null;
        if (pipelineMavenDTO.getNexusRepoId() != null) {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
            List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, ArrayUtil.singleAsSet(pipelineMavenDTO.getNexusRepoId()));

            C7nNexusRepoDTO c7nNexusRepoDTO = rdupmClient.getMavenRepo(projectDTO.getOrganizationId(), projectDTO.getId(), pipelineMavenDTO.getNexusRepoId()).getBody();
            if (!Objects.isNull(c7nNexusRepoDTO)) {

                if (!CollectionUtils.isEmpty(nexusMavenRepoDTOs)) {
                    NexusMavenRepoDTO nexusMavenRepoDTO = nexusMavenRepoDTOs.get(0);
                    server = new Server(nexusMavenRepoDTO.getName(), nexusMavenRepoDTO.getNePullUserId(), nexusMavenRepoDTO.getNePullUserPassword());
                }
                //http://api/rdupm/v1/nexus/proxy/1/repository/lilly-snapshot/io/choerodon/springboot/0.0.1-SNAPSHOT/springboot-0.0.1-20210203.071047-5.jar
                //http://nex/repository/lilly-snapshot/io/choerodon/springboot/0.0.1-SNAPSHOT/springboot-0.0.1-20210203.071047-5.jar
                //区分RELEASE 和 SNAPSHOT
                if (urlDisplay) {
                    downloadUrl = String.format(DOWNLOAD_JAR_URL, api, proxy, c7nNexusRepoDTO.getConfigId());
                } else {
                    if (!StringUtils.isEmpty(c7nNexusRepoDTO.getInternalUrl())) {
                        downloadUrl = c7nNexusRepoDTO.getInternalUrl().split(c7nNexusRepoDTO.getNeRepositoryName())[0];
                    }
                }

                if (pipelineMavenDTO.getVersion().contains("SNAPSHOT")) {
                    downloadUrl += c7nNexusRepoDTO.getNeRepositoryName() + BaseConstants.Symbol.SLASH +
                            pipelineMavenDTO.getGroupId().replace(BaseConstants.Symbol.POINT, BaseConstants.Symbol.SLASH) +
                            BaseConstants.Symbol.SLASH + pipelineMavenDTO.getArtifactId() + BaseConstants.Symbol.SLASH + pipelineMavenDTO.getVersion() + ".jar";
                } else if (pipelineMavenDTO.getVersion().contains("RELEASE")) {
                    downloadUrl = getReleaseUrl(pipelineMavenDTO, c7nNexusRepoDTO, downloadUrl);
                } else {
                    // 通过update version函数后还有这种version:2021.3.3-143906-master ，
                    downloadUrl = getReleaseUrl(pipelineMavenDTO, c7nNexusRepoDTO, downloadUrl);
                }

            } else {
                LOGGER.error("devops.query.repo.nexus.is.null");
            }
        } else {
            downloadUrl = pipelineMavenDTO.calculateDownloadUrl();
            server = new Server(null, DESEncryptUtil.decode(pipelineMavenDTO.getUsername()), DESEncryptUtil.decode(pipelineMavenDTO.getPassword()));
        }

        PipelineJarInfoVO pipelineJarInfoVO = new PipelineJarInfoVO();
        pipelineJarInfoVO.setDownloadUrl(downloadUrl);
        pipelineJarInfoVO.setGroupId(pipelineMavenDTO.getGroupId());
        pipelineJarInfoVO.setArtifactId(pipelineMavenDTO.getArtifactId());
        pipelineJarInfoVO.setVersion(pipelineMavenDTO.getVersion());
        pipelineJarInfoVO.setServer(server);
        devopsCiJobRecordVO.setPipelineJarInfo(pipelineJarInfoVO);

    }


    private String getReleaseUrl(CiPipelineMavenDTO pipelineMavenDTO, C7nNexusRepoDTO c7nNexusRepoDTO, String downloadUrl) {
        downloadUrl += c7nNexusRepoDTO.getNeRepositoryName() + BaseConstants.Symbol.SLASH +
                pipelineMavenDTO.getGroupId().replace(BaseConstants.Symbol.POINT, BaseConstants.Symbol.SLASH) +
                BaseConstants.Symbol.SLASH + pipelineMavenDTO.getArtifactId() +
                BaseConstants.Symbol.SLASH + pipelineMavenDTO.getVersion() +
                BaseConstants.Symbol.SLASH + pipelineMavenDTO.getArtifactId() + BaseConstants.Symbol.MIDDLE_LINE + pipelineMavenDTO.getVersion() + ".jar";
        return downloadUrl;
    }

    /**
     * 添加提交信息
     */
    protected void addCommitInfo(Long appServiceId, DevopsCiPipelineRecordVO devopsCiPipelineRecordVO, DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO) {
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(devopsCiPipelineRecordDTO.getCommitSha(), devopsCiPipelineRecordDTO.getGitlabTriggerRef());

        CustomCommitVO customCommitVO = new CustomCommitVO();
        devopsCiPipelineRecordVO.setCommit(customCommitVO);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        String gitlabProjectUrl;
        if (appServiceDTO.getExternalConfigId() != null) {
            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
            gitlabProjectUrl = appExternalConfigDTO.getRepositoryUrl();
        } else {
            GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectById(appServiceDTO.getGitlabProjectId());
            gitlabProjectUrl = gitlabProjectDTO.getWebUrl();
        }

        customCommitVO.setGitlabProjectUrl(gitlabProjectUrl);

        // 可能因为GitLab webhook 失败, commit信息查不出
        if (devopsGitlabCommitDTO == null) {
            return;
        }
        IamUserDTO commitUser = null;
        if (devopsGitlabCommitDTO.getUserId() != null) {
            commitUser = baseServiceClientOperator.queryUserByUserId(devopsGitlabCommitDTO.getUserId());
        }

        customCommitVO.setRef(devopsCiPipelineRecordDTO.getGitlabTriggerRef());
        customCommitVO.setCommitSha(devopsCiPipelineRecordDTO.getCommitSha());
        customCommitVO.setCommitContent(devopsGitlabCommitDTO.getCommitContent());
        customCommitVO.setCommitUrl(devopsGitlabCommitDTO.getUrl());

        if (commitUser != null) {
            customCommitVO.setUserHeadUrl(commitUser.getImageUrl());
            customCommitVO.setUserName(Boolean.TRUE.equals(commitUser.getLdap()) ? commitUser.getLoginName() : commitUser.getEmail());
        }
    }

    private Long calculateStageDuration(List<DevopsCiJobRecordVO> devopsCiJobRecordVOS) {
        Optional<DevopsCiJobRecordVO> max = devopsCiJobRecordVOS.stream().filter(v -> v.getDurationSeconds() != null).max(Comparator.comparingInt(v -> v.getDurationSeconds().intValue()));
        return max.orElse(new DevopsCiJobRecordVO()).getDurationSeconds();
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(DEVOPS_PIPELINE_ID_IS_NULL);
        }
        DevopsCiPipelineRecordDTO pipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        pipelineRecordDTO.setCiPipelineId(ciPipelineId);
        devopsCiPipelineRecordMapper.delete(pipelineRecordDTO);
    }

    @Override
    public List<DevopsCiPipelineRecordDTO> queryByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(DEVOPS_PIPELINE_ID_IS_NULL);
        }
        DevopsCiPipelineRecordDTO pipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        pipelineRecordDTO.setCiPipelineId(ciPipelineId);
        return devopsCiPipelineRecordMapper.select(pipelineRecordDTO);
    }

    @Override
    public DevopsCiPipelineRecordDTO create(Long ciPipelineId, Long gitlabProjectId, Pipeline pipeline) {
        DevopsCiPipelineRecordDTO pipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        pipelineRecordDTO.setCiPipelineId(ciPipelineId);
        pipelineRecordDTO.setGitlabProjectId(gitlabProjectId);
        pipelineRecordDTO.setGitlabPipelineId(TypeUtil.objToLong(pipeline.getId()));
        pipelineRecordDTO.setCreatedDate(pipeline.getCreatedAt());
        pipelineRecordDTO.setFinishedDate(pipeline.getFinished_at());
        pipelineRecordDTO.setDurationSeconds(TypeUtil.objToLong(pipeline.getDuration()));
        pipelineRecordDTO.setStatus(pipeline.getStatus());
        pipelineRecordDTO.setTriggerUserId(DetailsHelper.getUserDetails().getUserId());
        pipelineRecordDTO.setGitlabTriggerRef(pipeline.getRef());
        pipelineRecordDTO.setSource("api");
        pipelineRecordDTO.setCommitSha(pipeline.getSha());
        devopsCiPipelineRecordMapper.insertSelective(pipelineRecordDTO);
        return devopsCiPipelineRecordMapper.selectByPrimaryKey(pipelineRecordDTO.getId());
    }

    @Override
    public DevopsCiPipelineRecordDTO queryById(Long ciPipelineRecordId) {
        return devopsCiPipelineRecordMapper.selectByPrimaryKey(ciPipelineRecordId);
    }

    @Override
    public DevopsCiPipelineRecordDTO queryByIdWithPipelineName(Long ciPipelineRecordId) {
        return devopsCiPipelineRecordMapper.queryByIdWithPipelineName(ciPipelineRecordId);
    }

    @Override
    public DevopsCiPipelineRecordDTO queryByGitlabPipelineId(Long devopsPipelineId, Long gitlabPipelineId) {
        Assert.notNull(gitlabPipelineId, DEVOPS_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(devopsPipelineId, DEVOPS_PIPELINE_ID_IS_NULL);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        devopsCiPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsCiPipelineRecordDTO.setCiPipelineId(devopsPipelineId);
        return devopsCiPipelineRecordMapper.selectOne(devopsCiPipelineRecordDTO);
    }

    @Override
    public List<DevopsCiPipelineRecordDTO> queryNotSynchronizedRecord(Long statusUpdatePeriodMilliSeconds) {
        return devopsCiPipelineRecordMapper.queryNotSynchronizedRecord(new Date(System.currentTimeMillis() - statusUpdatePeriodMilliSeconds));
    }

    /**
     * 校验用户是否有分支权限
     */
    private void checkUserBranchPushPermission(Long projectId, Long gitlabPipelineId, Long gitlabProjectId, Long gitlabUserId) {
        DevopsCiPipelineRecordDTO recordDTO = new DevopsCiPipelineRecordDTO();
        recordDTO.setGitlabPipelineId(gitlabPipelineId);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(recordDTO);
        devopsCiPipelineService.checkUserBranchPushPermission(projectId, gitlabUserId, gitlabProjectId, devopsCiPipelineRecordDTO.getGitlabTriggerRef());
    }

    /**
     * 更新pipeline status
     */
    private DevopsCiPipelineRecordDTO updatePipelineStatus(Long gitlabPipelineId, String status) {
        DevopsCiPipelineRecordDTO recordDTO = new DevopsCiPipelineRecordDTO();
        recordDTO.setGitlabPipelineId(gitlabPipelineId);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(recordDTO);
        devopsCiPipelineRecordDTO.setStatus(status);
        devopsCiPipelineRecordMapper.updateByPrimaryKeySelective(devopsCiPipelineRecordDTO);
        return devopsCiPipelineRecordDTO;
    }

    private void updateOrInsertJobRecord(Long ciPipelineRecordId, Long gitlabProjectId, List<JobDTO> jobDTOS, Long iamUserId, Long appServiceId) {
        jobDTOS.forEach(jobDTO -> {
            DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
            recordDTO.setGitlabJobId(TypeUtil.objToLong(jobDTO.getId()));
            DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordMapper.selectOne(recordDTO);
            // job记录存在则更新，不存在则插入
            if (devopsCiJobRecordDTO == null) {
                devopsCiJobRecordService.create(ciPipelineRecordId, gitlabProjectId, jobDTO, iamUserId, appServiceId);
            } else {
                devopsCiJobRecordDTO.setGitlabJobId(jobDTO.getId().longValue());
                devopsCiJobRecordDTO.setTriggerUserId(iamUserId);
                devopsCiJobRecordDTO.setStatus(jobDTO.getStatus().toValue());
                devopsCiJobRecordMapper.updateByPrimaryKeySelective(devopsCiJobRecordDTO);
            }
        });

    }


    private void calculateStageStatus(DevopsCiStageRecordVO stageRecord, List<DevopsCiJobRecordDTO> ciJobRecordDTOS) {

        if (ciJobRecordDTOS.stream().anyMatch(v -> JobStatusEnum.FAILED.value().equals(v.getStatus()))) {
            stageRecord.setStatus(JobStatusEnum.FAILED.value());
            return;
        }
        if (ciJobRecordDTOS.stream().anyMatch(v -> JobStatusEnum.CREATED.value().equals(v.getStatus()))) {
            stageRecord.setStatus(JobStatusEnum.CREATED.value());
            return;
        }
        if (ciJobRecordDTOS.stream().anyMatch(v -> JobStatusEnum.RUNNING.value().equals(v.getStatus()))) {
            stageRecord.setStatus(JobStatusEnum.RUNNING.value());
            return;
        }

        if (ciJobRecordDTOS.stream().anyMatch(v -> JobStatusEnum.CANCELED.value().equals(v.getStatus()))) {
            stageRecord.setStatus(JobStatusEnum.CANCELED.value());
            return;
        }
        if (ciJobRecordDTOS.stream().anyMatch(v -> JobStatusEnum.MANUAL.value().equals(v.getStatus()))) {
            stageRecord.setStatus(JobStatusEnum.MANUAL.value());
            return;
        }

        if (ciJobRecordDTOS.stream().anyMatch(v -> JobStatusEnum.SKIPPED.value().equals(v.getStatus()))) {
            stageRecord.setStatus(JobStatusEnum.SKIPPED.value());
            return;
        }

        if (ciJobRecordDTOS.stream().allMatch(v -> JobStatusEnum.PENDING.value().equals(v.getStatus()))) {
            stageRecord.setStatus(JobStatusEnum.PENDING.value());
            return;
        }
        if (ciJobRecordDTOS.stream().allMatch(v -> JobStatusEnum.SUCCESS.value().equals(v.getStatus()))) {
            stageRecord.setStatus(JobStatusEnum.SUCCESS.value());
            return;
        }
        stageRecord.setStatus(JobStatusEnum.RUNNING.value());
    }


    @Override
    public DevopsCiPipelineRecordVO queryByCiPipelineRecordId(Long ciPipelineRecordId) {
        if (ciPipelineRecordId == null || ciPipelineRecordId == 0L) {
            return null;
        }
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(ciPipelineRecordId);
        if (Objects.isNull(devopsCiPipelineRecordDTO)) {
            return null;
        }
        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = new DevopsCiPipelineRecordVO();
        BeanUtils.copyProperties(devopsCiPipelineRecordDTO, devopsCiPipelineRecordVO);
        devopsCiPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordDTO.getCreatedDate());

        ciPipelineSyncHandler.syncPipeline(devopsCiPipelineRecordVO.getStatus(), devopsCiPipelineRecordVO.getLastUpdateDate(), devopsCiPipelineRecordVO.getId(), TypeUtil.objToInteger(devopsCiPipelineRecordVO.getGitlabPipelineId()));
        // 查询流水线记录下的job记录
        DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
        recordDTO.setCiPipelineRecordId(devopsCiPipelineRecordVO.getId());
        List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS = devopsCiJobRecordMapper.select(recordDTO);


        Map<String, List<DevopsCiJobRecordDTO>> jobRecordMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStage));

        List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = new ArrayList<>();
        for (Map.Entry<String, List<DevopsCiJobRecordDTO>> entry : jobRecordMap.entrySet()) {
            String k = entry.getKey();
            List<DevopsCiJobRecordDTO> value = entry.getValue();
            DevopsCiStageRecordVO devopsCiStageRecordVO = new DevopsCiStageRecordVO();
            devopsCiStageRecordVO.setName(k);
            value.stream().min(Comparator.comparing(DevopsCiJobRecordDTO::getGitlabJobId)).ifPresent(i -> devopsCiStageRecordVO.setSequence(i.getGitlabJobId()));
            // 只返回job的最新记录
            List<DevopsCiJobRecordDTO> latestedsCiJobRecordDTOS = filterJobs(value);
            List<DevopsCiJobRecordVO> latestedsCiJobRecordVOS = ConvertUtils.convertList(latestedsCiJobRecordDTOS, DevopsCiJobRecordVO.class);
            calculateStageStatus(devopsCiStageRecordVO, latestedsCiJobRecordDTOS);
            devopsCiStageRecordVO.setDurationSeconds(calculateStageDuration(latestedsCiJobRecordVOS));
            devopsCiStageRecordVOS.add(devopsCiStageRecordVO);
        }

        // stage排序
        devopsCiStageRecordVOS = devopsCiStageRecordVOS.stream().sorted(Comparator.comparing(DevopsCiStageRecordVO::getSequence)).filter(v -> v.getStatus() != null).collect(Collectors.toList());
        devopsCiPipelineRecordVO.setStageRecordVOS(devopsCiStageRecordVOS);
        return devopsCiPipelineRecordVO;
    }

    @Override
    public DevopsCiPipelineRecordDTO queryByAppServiceIdAndGitlabPipelineId(Long appServiceId, Long gitlabPipelineId) {
        return devopsCiPipelineRecordMapper.queryByAppServiceIdAndGitlabPipelineId(appServiceId, gitlabPipelineId);
    }

    @Override
    public List<CiPipelineRecordVO> listByPipelineId(Long pipelineId) {
        return devopsCiPipelineRecordMapper.listByCiPipelineId(pipelineId);
    }

    @Override
    public void fillAdditionalInfo(CiPipelineRecordVO recordVO) {
        Long ciPipelineRecordId = recordVO.getId();
        Long gitlabPipelineId = recordVO.getGitlabPipelineId();
        Long userId = DetailsHelper.getUserDetails().getUserId();

//        ciPipelineSyncHandler.syncPipeline(devopsCiPipelineRecordVO.getStatus(), devopsCiPipelineRecordVO.getLastUpdateDate(), devopsCiPipelineRecordVO.getId(), TypeUtil.objToInteger(devopsCiPipelineRecordVO.getGitlabPipelineId()));
        // 查询流水线记录下的job记录

        List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS = devopsCiJobRecordService.listByCiPipelineRecordId(ciPipelineRecordId);

        Map<String, List<DevopsCiJobRecordDTO>> jobRecordMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStage));

        List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = new ArrayList<>();
        for (Map.Entry<String, List<DevopsCiJobRecordDTO>> entry : jobRecordMap.entrySet()) {
            String k = entry.getKey();
            List<DevopsCiJobRecordDTO> value = entry.getValue();
            DevopsCiStageRecordVO devopsCiStageRecordVO = new DevopsCiStageRecordVO();
            devopsCiStageRecordVO.setName(k);
            value.stream().min(Comparator.comparing(DevopsCiJobRecordDTO::getGitlabJobId)).ifPresent(i -> devopsCiStageRecordVO.setSequence(i.getGitlabJobId()));
            // 只返回job的最新记录
            List<DevopsCiJobRecordDTO> latestedsCiJobRecordDTOS = filterJobs(value);
            List<DevopsCiJobRecordVO> latestedsCiJobRecordVOS = ConvertUtils.convertList(latestedsCiJobRecordDTOS, DevopsCiJobRecordVO.class);
            calculateStageStatus(devopsCiStageRecordVO, latestedsCiJobRecordDTOS);
            devopsCiStageRecordVO.setDurationSeconds(calculateStageDuration(latestedsCiJobRecordVOS));
            devopsCiStageRecordVOS.add(devopsCiStageRecordVO);
        }
        List<DevopsCiPipelineAuditVO> pipelineAuditInfo = new ArrayList<>();
        devopsCiJobRecordDTOS.forEach(devopsCiJobRecordVO -> {
            if (CiJobTypeEnum.AUDIT.value().equals(devopsCiJobRecordVO.getType())
                    && io.choerodon.devops.infra.dto.gitlab.ci.PipelineStatus.MANUAL.toValue().equals(devopsCiJobRecordVO.getStatus())) {

                CiAuditRecordDTO ciAuditRecordDTO = ciAuditRecordService.queryByUniqueOption(devopsCiJobRecordVO.getAppServiceId(), gitlabPipelineId, devopsCiJobRecordVO.getName());
                if (ciAuditRecordDTO != null) {
                    List<CiAuditUserRecordDTO> auditUserRecordDTOList = ciAuditUserRecordService.listByAuditRecordId(ciAuditRecordDTO.getId());
                    if (!CollectionUtils.isEmpty(auditUserRecordDTOList)) {
                        if (auditUserRecordDTOList.stream().anyMatch(r -> r.getUserId().equals(userId) && AuditStatusEnum.NOT_AUDIT.value().equals(r.getStatus()))) {
                            DevopsCiPipelineAuditVO devopsCiPipelineAuditVO = new DevopsCiPipelineAuditVO(devopsCiJobRecordVO.getName(), devopsCiJobRecordVO.getId(), true);
                            pipelineAuditInfo.add(devopsCiPipelineAuditVO);
                        }
                    }
                }
            }
        });
        if (!CollectionUtils.isEmpty(pipelineAuditInfo)) {
            recordVO.setPipelineAuditInfo(pipelineAuditInfo.stream().sorted(Comparator.comparing(DevopsCiPipelineAuditVO::getJobRecordId)).collect(Collectors.toList()));
        }
        // stage排序
        devopsCiStageRecordVOS = devopsCiStageRecordVOS.stream().sorted(Comparator.comparing(DevopsCiStageRecordVO::getSequence)).filter(v -> v.getStatus() != null).collect(Collectors.toList());
        recordVO.setStageRecordVOS(devopsCiStageRecordVOS);
    }

    @Override
    public DevopsCiPipelineRecordDTO queryLatestedPipelineRecord(Long pipelineId) {
        return devopsCiJobRecordMapper.queryLatestedPipelineRecord(pipelineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryPipeline(Long projectId, Long id) {
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = queryById(id);

        Long gitlabPipelineId = devopsCiPipelineRecordDTO.getGitlabPipelineId();
        CiCdPipelineDTO ciCdPipelineDTO = devopsCiPipelineService.baseQueryById(devopsCiPipelineRecordDTO.getCiPipelineId());

        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineDTO.getAppServiceId());

        Integer gitlabProjectId = appServiceDTO.getGitlabProjectId();

        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceDTO.getId(), AppServiceEvent.CI_PIPELINE_RETRY);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        checkUserBranchPushPermission(projectId, gitlabPipelineId, TypeUtil.objToLong(gitlabProjectId), userAttrDTO.getGitlabUserId());
        // 重试pipeline
        Pipeline pipeline = gitlabServiceClientOperator.retryPipeline(gitlabProjectId,
                gitlabPipelineId.intValue(),
                userAttrDTO.getGitlabUserId().intValue(),
                appExternalConfigDTO);

        try {
            // 更新pipeline status
            updatePipelineStatus(gitlabPipelineId, pipeline.getStatus());
            // 更新job status

            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId,
                    gitlabPipelineId.intValue(),
                    userAttrDTO.getGitlabUserId().intValue(),
                    appExternalConfigDTO);
            updateOrInsertJobRecord(devopsCiPipelineRecordDTO.getId(),
                    TypeUtil.objToLong(gitlabProjectId),
                    jobDTOS, userAttrDTO.getIamUserId(),
                    appServiceDTO.getId());
        } catch (Exception e) {
            LOGGER.info("update pipeline Records failed， gitlabPipelineId {}.", gitlabPipelineId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPipeline(Long projectId, Long id) {
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = queryById(id);
        Long gitlabPipelineId = devopsCiPipelineRecordDTO.getGitlabPipelineId();

        CiCdPipelineDTO ciCdPipelineDTO = devopsCiPipelineService.baseQueryById(devopsCiPipelineRecordDTO.getCiPipelineId());

        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineDTO.getAppServiceId());

        Integer gitlabProjectId = appServiceDTO.getGitlabProjectId();
        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());

        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceDTO.getId(), AppServiceEvent.CI_PIPELINE_CANCEL);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        checkUserBranchPushPermission(projectId, gitlabPipelineId, TypeUtil.objToLong(gitlabProjectId), userAttrDTO.getGitlabUserId());

        gitlabServiceClientOperator.cancelPipeline(gitlabProjectId,
                gitlabPipelineId.intValue(),
                userAttrDTO.getGitlabUserId().intValue(),
                appExternalConfigDTO);

        try {
            // 更新pipeline status
            updatePipelineStatus(gitlabPipelineId, PipelineStatus.CANCELED.toValue());
            // 更新job status

            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId, gitlabPipelineId.intValue(), userAttrDTO.getGitlabUserId().intValue(), appExternalConfigDTO);
            updateOrInsertJobRecord(devopsCiPipelineRecordDTO.getId(), TypeUtil.objToLong(gitlabProjectId), jobDTOS, userAttrDTO.getIamUserId(), appServiceDTO.getId());
        } catch (Exception e) {
            LOGGER.info("update pipeline Records failed， gitlabPipelineId {}.", gitlabPipelineId);
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long ciPipelineDeployImage(Long projectId, Long gitlabPipelineId, DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO, StringBuilder log) {
        log.append("开始执行镜像部署任务").append(System.lineSeparator());
        String deployVersion = null;
        String deployObjectName = null;
        String image = null;
        Long appServiceId = null;
        String repoName = null;
        Long repoId = null;
        String userName = null;
        String password = null;
        String repoType = null;
        String tag = null;

        Long hostId = devopsCiHostDeployInfoDTO.getHostId();

        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);

        List<Long> updatedClusterList = hostConnectionHandler.getUpdatedHostList();
        log.append("1. 检查主机连接状态...").append(System.lineSeparator());
        if (Boolean.FALSE.equals(updatedClusterList.contains(hostId))) {
            log.append("主机：").append(devopsHostDTO.getName()).append("未连接，请检查主机中agent状态是否正常").append(System.lineSeparator());
            throw new CommonException(DEVOPS_DEPLOY_FAILED);
        }
        log.append("主机连接状态检查通过").append(System.lineSeparator());
        DockerDeployDTO dockerDeployDTO = new DockerDeployDTO();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        DevopsCiHostDeployInfoVO.ImageDeploy imageDeploy = JsonHelper.unmarshalByJackson(devopsCiHostDeployInfoDTO.getDeployJson(), DevopsCiHostDeployInfoVO.ImageDeploy.class);

        log.append("2. 获取部署镜像信息...").append(System.lineSeparator());
        CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(appServiceId, gitlabPipelineId, imageDeploy.getPipelineTask());
        if (ciPipelineImageDTO == null) {
            log.append("获取部署镜像信息失败，请检查关联的构建任务是否执行成功").append(System.lineSeparator());
            throw new CommonException(DEVOPS_DEPLOY_FAILED);
        }
        HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigById(projectId, ciPipelineImageDTO.getHarborRepoId(), ciPipelineImageDTO.getRepoType());

        // 设置拉取账户
        if (ciPipelineImageDTO.getRepoType().equals(CUSTOM_REPO)) {
            dockerDeployDTO.setDockerPullAccountDTO(new DockerPullAccountDTO(
                    harborRepoDTO.getHarborRepoConfig().getRepoUrl(),
                    harborRepoDTO.getHarborRepoConfig().getLoginName(),
                    harborRepoDTO.getHarborRepoConfig().getPassword()));
            userName = harborRepoDTO.getHarborRepoConfig().getLoginName();
            password = harborRepoDTO.getHarborRepoConfig().getPassword();
            repoType = harborRepoDTO.getRepoType();
            repoId = harborRepoDTO.getHarborRepoConfig().getRepoId();
        } else {
            dockerDeployDTO.setDockerPullAccountDTO(new DockerPullAccountDTO(
                    harborRepoDTO.getHarborRepoConfig().getRepoUrl(),
                    harborRepoDTO.getPullRobot().getName(),
                    harborRepoDTO.getPullRobot().getToken()));
            repoId = harborRepoDTO.getHarborRepoConfig().getRepoId();
            repoName = harborRepoDTO.getHarborRepoConfig().getRepoName();
            repoType = harborRepoDTO.getRepoType();
        }

        // 添加应用服务名用于部署记录  iamgeTag:172.23.xx.xx:30003/dev-25-test-25-4/go:2021.5.17-155211-master
        String imageTag = ciPipelineImageDTO.getImageTag();
        int indexOf = imageTag.lastIndexOf(":");
        String imageVersion = imageTag.substring(indexOf + 1);
        String repoImageName = imageTag.substring(0, indexOf);
        tag = imageVersion;
        image = ciPipelineImageDTO.getImageTag();
        deployVersion = imageVersion;
        deployObjectName = repoImageName.substring(repoImageName.lastIndexOf("/") + 1);

        log.append("镜像: ").append(imageTag).append(System.lineSeparator());
        log.append("容器名称: ").append(imageDeploy.getContainerName()).append(System.lineSeparator());
        // 1. 更新状态 记录镜像信息
        log.append("3. 开始部署...").append(System.lineSeparator());
        DevopsHostAppDTO devopsHostAppDTO = getDevopsHostAppDTO(projectId, hostId, devopsCiHostDeployInfoDTO.getDeployType(), devopsCiHostDeployInfoDTO.getAppName(), devopsCiHostDeployInfoDTO.getAppCode(), devopsCiHostDeployInfoDTO.getWorkDir());
        // 2.保存记录
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.queryByHostIdAndName(hostId, imageDeploy.getContainerName());
        log.append("部署模式：").append(devopsDockerInstanceDTO == null ? "新建应用" : "更新应用").append(System.lineSeparator());
        if (devopsDockerInstanceDTO == null) {
            // 新建实例
            devopsDockerInstanceDTO = new DevopsDockerInstanceDTO(hostId,
                    imageDeploy.getContainerName(),
                    image,
                    DockerInstanceStatusEnum.OPERATING.value(),
                    AppSourceType.CURRENT_PROJECT.getValue(), null);
            devopsDockerInstanceDTO.setDockerCommand(devopsCiHostDeployInfoDTO.getDockerCommand());
            devopsDockerInstanceDTO.setRepoId(repoId);
            devopsDockerInstanceDTO.setRepoName(repoName);
            devopsDockerInstanceDTO.setAppId(devopsHostAppDTO.getId());
            devopsDockerInstanceDTO.setImageName(deployObjectName);
            devopsDockerInstanceDTO.setPassWord(password);
            devopsDockerInstanceDTO.setUserName(userName);
            devopsDockerInstanceDTO.setRepoType(repoType);
            devopsDockerInstanceDTO.setTag(tag);
            MapperUtil.resultJudgedInsertSelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, DevopsHostConstants.ERROR_SAVE_DOCKER_INSTANCE_FAILED);
            // 保存应用实例关系
            // 保存appId
            devopsCiHostDeployInfoDTO.setAppId(devopsHostAppDTO.getId());
            devopsCiHostDeployInfoDTO.setDeployType(DeployTypeEnum.UPDATE.value());
            devopsCiHostDeployInfoService.baseUpdate(devopsCiHostDeployInfoDTO);
        } else {
            dockerDeployDTO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        }

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsDockerInstanceDTO.getId());
        devopsHostCommandDTO.setCiPipelineRecordId(gitlabPipelineId);
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        dockerDeployDTO.setInstanceId(devopsDockerInstanceDTO.getId().toString());

        dockerDeployDTO.setImage(image);
        dockerDeployDTO.setContainerName(imageDeploy.getContainerName());
        dockerDeployDTO.setCmd(HostDeployUtil.getDockerRunCmd(dockerDeployDTO, Base64Util.decodeBuffer(devopsCiHostDeployInfoDTO.getDockerCommand())));
        dockerDeployDTO.setInstanceId(String.valueOf(devopsDockerInstanceDTO.getId()));
        dockerDeployDTO.setAppCode(devopsHostAppDTO.getCode());
        dockerDeployDTO.setVersion(devopsHostAppDTO.getVersion());
        dockerDeployDTO.setWorkDir(devopsHostAppDTO.getWorkDir());

        // 3. 保存部署记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.AUTO,
                devopsHostCommandDTO.getId(),
                DeployModeEnum.HOST,
                devopsHostDTO.getId(),
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.DOCKER,
                deployObjectName,
                deployVersion,
                devopsHostAppDTO.getName(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getId(),
                new DeploySourceVO(AppSourceType.CURRENT_PROJECT, projectDTO.getName()));

        // 4. 发送部署指令给agent
        log.append("发送部署指令给agent.").append(System.lineSeparator());
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_DOCKER.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerDeployDTO));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.DOCKER_INSTANCE, hostId, devopsDockerInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
        log.append("发送成功.").append(System.lineSeparator());
        return devopsHostCommandDTO.getId();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long ciPipelineDeployDockerCompose(Long projectId, AppServiceDTO appServiceDTO, Long gitlabPipelineId, DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO, StringBuilder log) {
        log.append("开始执行镜像部署任务").append(System.lineSeparator());
        Long hostId = devopsCiHostDeployInfoDTO.getHostId();

        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);

        List<Long> updatedClusterList = hostConnectionHandler.getUpdatedHostList();
        log.append("1. 检查主机连接状态...").append(System.lineSeparator());
        if (Boolean.FALSE.equals(updatedClusterList.contains(hostId))) {
            log.append("主机：").append(devopsHostDTO.getName()).append("未连接，请检查主机中agent状态是否正常").append(System.lineSeparator());
            throw new CommonException(DEVOPS_DEPLOY_FAILED);
        }
        log.append("主机连接状态检查通过").append(System.lineSeparator());

        Long appServiceId = appServiceDTO.getId();

        Long appId = devopsCiHostDeployInfoDTO.getAppId();
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(appId);

        // 1. 查询关联构建任务生成的镜像
        log.append("2. 获取部署镜像信息...").append(System.lineSeparator());
        CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(appServiceId,
                gitlabPipelineId,
                devopsCiHostDeployInfoDTO.getImageJobName());
        if (ciPipelineImageDTO == null) {
            log.append("获取部署镜像信息失败，请检查关联的构建任务是否执行成功").append(System.lineSeparator());
            throw new CommonException("devops.deploy.images.not.exist");
        }
        log.append("镜像：").append(ciPipelineImageDTO.getImageTag()).append(System.lineSeparator());

        // 2. 通过应用服务编码匹配service，替换镜像
        log.append("[info] Start replace docker-compose.yaml").append(System.lineSeparator());
        log.append("3. 开始替换docker-compose.yaml文件").append(System.lineSeparator());
        String value = replaceValue(appServiceDTO.getCode(),
                ciPipelineImageDTO.getImageTag(),
                dockerComposeValueService
                        .baseQuery(devopsHostAppDTO.getEffectValueId())
                        .getValue());

        log.append("替换后的 docker-compose.yaml 文件: ").append(System.lineSeparator());
        log.append(value).append(System.lineSeparator());

        DockerComposeValueDTO dockerComposeValueDTO = new DockerComposeValueDTO();
        dockerComposeValueDTO.setValue(value);

        DockerComposeDeployVO dockerComposeDeployVO = new DockerComposeDeployVO();
        dockerComposeDeployVO.setRunCommand(Base64Util.decodeBuffer(devopsCiHostDeployInfoDTO.getRunCommand()));
        dockerComposeDeployVO.setDockerComposeValueDTO(dockerComposeValueDTO);
        dockerComposeDeployVO.setAppName(devopsHostAppDTO.getName());
        dockerComposeDeployVO.setAppCode(devopsHostAppDTO.getCode());
        dockerComposeDeployVO.setVersion(devopsHostAppDTO.getVersion());

        // 3. 更新docker-compose应用
        log.append("4. 开始部署...").append(System.lineSeparator());
        log.append("发送部署指令给agent.").append(System.lineSeparator());
        DevopsHostCommandDTO devopsHostCommandDTO = dockerComposeService.updateDockerComposeApp(projectId, appId, null, gitlabPipelineId, dockerComposeDeployVO, true);
        log.append("发送成功").append(System.lineSeparator());
        return devopsHostCommandDTO.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long ciPipelineDeployJar(Long projectId, AppServiceDTO appServiceDTO, Long gitlabPipelineId, DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO, StringBuilder log) {
        log.append("开始执行jar部署任务...").append(System.lineSeparator());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        Long hostId = devopsCiHostDeployInfoDTO.getHostId();

        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);

        List<Long> updatedClusterList = hostConnectionHandler.getUpdatedHostList();
        log.append("1. 检查主机连接状态...").append(System.lineSeparator());
        if (Boolean.FALSE.equals(updatedClusterList.contains(hostId))) {
            log.append("主机：").append(devopsHostDTO.getName()).append("未连接，请检查主机中agent状态是否正常").append(System.lineSeparator());
            throw new CommonException(DEVOPS_DEPLOY_FAILED);
        }
        log.append("主机连接状态检查通过").append(System.lineSeparator());
        DevopsCiHostDeployInfoVO.JarDeploy jarDeploy = JsonHelper.unmarshalByJackson(devopsCiHostDeployInfoDTO.getDeployJson(), DevopsCiHostDeployInfoVO.JarDeploy.class);

        // 0.1 从制品库获取仓库信息
        log.append("2. 获取部署jar包信息...").append(System.lineSeparator());
        Long nexusRepoId;
        String groupId;
        String artifactId;
        String version = null;
        String repoUrl;
        String artifactType;
        String downloadUrl = null;
        String username = null;
        String password = null;

        CiPipelineMavenDTO ciPipelineMavenDTO = ciPipelineMavenService.queryByGitlabPipelineId(appServiceDTO.getId(),
                gitlabPipelineId,
                jarDeploy.getPipelineTask());
        nexusRepoId = ciPipelineMavenDTO.getNexusRepoId();
        groupId = ciPipelineMavenDTO.getGroupId();
        artifactId = ciPipelineMavenDTO.getArtifactId();
        artifactType = ciPipelineMavenDTO.getArtifactType();

        log.append("根据坐标获取jar包信息：").append(System.lineSeparator());
        log.append("groupId：").append(groupId).append(System.lineSeparator());
        log.append("artifactId：").append(artifactId).append(System.lineSeparator());
        log.append("version：").append(getMavenVersion(ciPipelineMavenDTO.getVersion())).append(System.lineSeparator());
        log.append("packaging：").append(artifactType).append(System.lineSeparator());
        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO(username, password, downloadUrl);
        JarDeployVO jarDeployVO = null;
        if (nexusRepoId != null) {
            List<NexusMavenRepoDTO> mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
            if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
                log.append("获取制品仓库信息失败，请检查关联制品库是否正常").append(System.lineSeparator());
                throw new CommonException(DEVOPS_DEPLOY_FAILED);
            }
            C7nNexusRepoDTO c7nNexusRepoDTO = rdupmClientOperator.getMavenRepo(projectDTO.getOrganizationId(), projectId, nexusRepoId);

            ProdJarInfoVO prodJarInfoVO = new ProdJarInfoVO(c7nNexusRepoDTO.getConfigId(),
                    nexusRepoId,
                    groupId,
                    artifactId,
                    ciPipelineMavenDTO.getVersion());
            repoUrl = mavenRepoDTOList.get(0).getUrl();
            username = mavenRepoDTOList.get(0).getNePullUserId();
            password = mavenRepoDTOList.get(0).getNePullUserPassword();

            jarDeployVO = new JarDeployVO(AppSourceType.CURRENT_PROJECT.getValue(),
                    devopsCiHostDeployInfoDTO.getAppName(),
                    devopsCiHostDeployInfoDTO.getAppCode(),
                    devopsCiHostDeployInfoDTO.getPreCommand(),
                    devopsCiHostDeployInfoDTO.getRunCommand(),
                    devopsCiHostDeployInfoDTO.getPostCommand(),
                    devopsCiHostDeployInfoDTO.getKillCommand(),
                    devopsCiHostDeployInfoDTO.getHealthProb(),
                    prodJarInfoVO,
                    devopsCiHostDeployInfoDTO.getDeployType());
        } else {
            jarDeployVO = new JarDeployVO(AppSourceType.CUSTOM_JAR.getValue(),
                    devopsCiHostDeployInfoDTO.getAppName(),
                    devopsCiHostDeployInfoDTO.getAppCode(),
                    devopsCiHostDeployInfoDTO.getPreCommand(),
                    devopsCiHostDeployInfoDTO.getRunCommand(),
                    devopsCiHostDeployInfoDTO.getPostCommand(),
                    devopsCiHostDeployInfoDTO.getKillCommand(),
                    devopsCiHostDeployInfoDTO.getHealthProb(),
                    jarPullInfoDTO,
                    devopsCiHostDeployInfoDTO.getDeployType());
            repoUrl = ciPipelineMavenDTO.getMavenRepoUrl();
            username = DESEncryptUtil.decode(ciPipelineMavenDTO.getUsername());
            password = DESEncryptUtil.decode(ciPipelineMavenDTO.getPassword());

        }
        version = ciPipelineMavenDTO.getVersion();
        downloadUrl = MavenUtil.calculateDownloadUrl(repoUrl,
                ciPipelineMavenDTO.getGroupId(),
                ciPipelineMavenDTO.getArtifactId(),
                ciPipelineMavenDTO.getVersion(),
                ciPipelineMavenDTO.getArtifactType());

        // 2.保存记录
        log.append("3. 开始部署...").append(System.lineSeparator());
        DevopsHostAppDTO devopsHostAppDTO;
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO;
        log.append("部署模式：").append(DeployTypeEnum.CREATE.value().equals(devopsCiHostDeployInfoDTO.getDeployType()) ? "新建应用" : "更新应用").append(System.lineSeparator());
        if (DeployTypeEnum.CREATE.value().equals(devopsCiHostDeployInfoDTO.getDeployType())) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    jarDeployVO.getAppName(),
                    jarDeployVO.getAppCode(),
                    RdupmTypeEnum.JAR.value(),
                    OperationTypeEnum.PIPELINE_DEPLOY.value(),
                    devopsCiHostDeployInfoDTO.getWorkDir());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);
            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    hostId,
                    devopsHostAppDTO.getId(),
                    jarDeployVO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
                    jarDeployVO.getSourceType(),
                    devopsHostAppService.calculateSourceConfig(jarDeployVO),
                    jarDeployVO.getPreCommand(),
                    jarDeployVO.getRunCommand(),
                    jarDeployVO.getPostCommand(),
                    jarDeployVO.getKillCommand(),
                    jarDeployVO.getHealthProb());
            devopsHostAppInstanceDTO.setGroupId(groupId);
            devopsHostAppInstanceDTO.setArtifactId(artifactId);
            devopsHostAppInstanceDTO.setVersion(version);

            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);

            // 保存appId
            devopsCiHostDeployInfoDTO.setAppId(devopsHostAppDTO.getId());
            devopsCiHostDeployInfoDTO.setDeployType(DeployTypeEnum.UPDATE.value());
            devopsCiHostDeployInfoService.baseUpdate(devopsCiHostDeployInfoDTO);

        } else {
            devopsHostAppDTO = devopsHostAppService.baseQuery(devopsCiHostDeployInfoDTO.getAppId());
            if (devopsHostAppDTO == null) {
                log.append("应用：").append("'").append(devopsCiHostDeployInfoDTO.getAppName()).append("'").append("不存在,请检查应用是否已删除").append(System.lineSeparator());
                throw new CommonException(DEVOPS_DEPLOY_FAILED);
            }
            devopsHostAppDTO.setName(jarDeployVO.getAppName());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);

            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
            devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);

            devopsHostAppInstanceDTO.setPreCommand(jarDeployVO.getPreCommand());
            devopsHostAppInstanceDTO.setRunCommand(jarDeployVO.getRunCommand());
            devopsHostAppInstanceDTO.setPostCommand(jarDeployVO.getPostCommand());
            devopsHostAppInstanceDTO.setKillCommand(jarDeployVO.getKillCommand());
            devopsHostAppInstanceDTO.setHealthProb(jarDeployVO.getHealthProb());
            devopsHostAppInstanceDTO.setSourceType(jarDeployVO.getSourceType());
            devopsHostAppInstanceDTO.setSourceConfig(devopsHostAppService.calculateSourceConfig(jarDeployVO));
            devopsHostAppInstanceDTO.setVersion(version);
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        }

        Map<String, String> params = new HashMap<>();
        String workDir = ObjectUtils.isEmpty(devopsHostAppDTO.getWorkDir()) ? HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId(), devopsHostAppDTO.getCode(), devopsHostAppDTO.getVersion()) : devopsHostAppDTO.getWorkDir();
        String appFileName = artifactId + "." + artifactType;
        String appFile = workDir + SLASH + appFileName;
        params.put("{{ WORK_DIR }}", workDir);
        params.put("{{ APP_FILE_NAME }}", appFileName);
        params.put("{{ APP_FILE }}", appFile);

        InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions(
                jarDeployVO.getAppCode(),
                devopsHostAppInstanceDTO.getId().toString(),
                HostDeployUtil.getDownloadCommand(username,
                        password,
                        downloadUrl,
                        appFile),
                ObjectUtils.isEmpty(jarDeployVO.getPreCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getPreCommand())),
                ObjectUtils.isEmpty(jarDeployVO.getRunCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getRunCommand())),
                ObjectUtils.isEmpty(jarDeployVO.getPostCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getPostCommand())),
                ObjectUtils.isEmpty(jarDeployVO.getKillCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getKillCommand())),
                ObjectUtils.isEmpty(jarDeployVO.getHealthProb()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getHealthProb())),
                jarDeployVO.getOperation(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getVersion(),
                devopsHostAppDTO.getWorkDir());
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.OPERATE_INSTANCE.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setCiPipelineRecordId(gitlabPipelineId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 保存执行记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.AUTO,
                devopsHostCommandDTO.getId(),
                DeployModeEnum.HOST,
                hostId,
                devopsHostDTO != null ? devopsHostDTO.getName() : null,
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.JAR,
                artifactId,
                version,
                devopsCiHostDeployInfoDTO.getAppName(),
                devopsCiHostDeployInfoDTO.getAppCode(),
                devopsHostAppDTO.getId(),
                new DeploySourceVO(AppSourceType.CURRENT_PROJECT, projectDTO.getName()));

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.OPERATE_INSTANCE.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
        log.append("发送部署指令给agent...").append(System.lineSeparator());
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
        log.append("发送成功").append(System.lineSeparator());
        return devopsHostCommandDTO.getId();
    }

    @Override
    public Long ciPipelineCustomDeploy(Long projectId, Long gitlabPipelineId, DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO, StringBuilder log) {
        log.append("开始执行其他制品部署任务...").append(System.lineSeparator());

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        Long hostId = devopsCiHostDeployInfoDTO.getHostId();

        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);

        List<Long> updatedClusterList = hostConnectionHandler.getUpdatedHostList();
        log.append("1. 检查主机连接状态...").append(System.lineSeparator());
        if (Boolean.FALSE.equals(updatedClusterList.contains(hostId))) {
            log.append("主机：").append(devopsHostDTO.getName()).append("未连接，请检查主机中agent状态是否正常").append(System.lineSeparator());
            throw new CommonException(DEVOPS_DEPLOY_FAILED);
        }
        log.append("主机连接状态检查通过").append(System.lineSeparator());

        DevopsHostAppDTO devopsHostAppDTO;
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO;
        log.append("2. 开始部署...").append(System.lineSeparator());
        log.append("部署模式：").append(DeployTypeEnum.CREATE.value().equals(devopsCiHostDeployInfoDTO.getDeployType()) ? "新建应用" : "更新应用").append(System.lineSeparator());
        if (DeployTypeEnum.CREATE.value().equals(devopsCiHostDeployInfoDTO.getDeployType())) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    devopsCiHostDeployInfoDTO.getAppName(),
                    devopsCiHostDeployInfoDTO.getAppCode(),
                    RdupmTypeEnum.OTHER.value(),
                    OperationTypeEnum.PIPELINE_DEPLOY.value(),
                    devopsCiHostDeployInfoDTO.getWorkDir());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);
            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    hostId,
                    devopsHostAppDTO.getId(),
                    devopsCiHostDeployInfoDTO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
                    null,
                    null,
                    devopsCiHostDeployInfoDTO.getPreCommand(),
                    devopsCiHostDeployInfoDTO.getRunCommand(),
                    devopsCiHostDeployInfoDTO.getPostCommand(),
                    devopsCiHostDeployInfoDTO.getKillCommand(),
                    devopsCiHostDeployInfoDTO.getHealthProb());

            devopsCiHostDeployInfoDTO.setAppId(devopsHostAppDTO.getId());
            devopsCiHostDeployInfoDTO.setDeployType(DeployTypeEnum.UPDATE.value());
            devopsCiHostDeployInfoService.baseUpdate(devopsCiHostDeployInfoDTO);

            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
        } else {
            devopsHostAppDTO = devopsHostAppService.baseQuery(devopsCiHostDeployInfoDTO.getAppId());
            if (devopsHostAppDTO == null) {
                log.append("应用：").append("'").append(devopsCiHostDeployInfoDTO.getAppName()).append("'")
                        .append("不存在,请检查应用是否已删除").append(System.lineSeparator());
                throw new CommonException(DEVOPS_DEPLOY_FAILED);
            }
            devopsHostAppDTO.setName(devopsCiHostDeployInfoDTO.getAppName());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);

            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
            devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);

            devopsHostAppInstanceDTO.setPreCommand(devopsCiHostDeployInfoDTO.getPreCommand());
            devopsHostAppInstanceDTO.setRunCommand(devopsCiHostDeployInfoDTO.getRunCommand());
            devopsHostAppInstanceDTO.setPostCommand(devopsCiHostDeployInfoDTO.getPostCommand());
            devopsHostAppInstanceDTO.setKillCommand(devopsCiHostDeployInfoDTO.getKillCommand());
            devopsHostAppInstanceDTO.setHealthProb(devopsCiHostDeployInfoDTO.getHealthProb());
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        }

        Map<String, String> params = new HashMap<>();
        String workDir = ObjectUtils.isEmpty(devopsHostAppDTO.getWorkDir()) ? HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId(), devopsHostAppDTO.getCode(), devopsHostAppDTO.getVersion()) : devopsHostAppDTO.getWorkDir();
        params.put("{{ WORK_DIR }}", workDir);
        params.put("{{ APP_FILE_NAME }}", "");
        params.put("{{ APP_FILE }}", "");

        InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions(
                devopsCiHostDeployInfoDTO.getAppCode(),
                devopsHostAppInstanceDTO.getId().toString(),
                null,
                ObjectUtils.isEmpty(devopsCiHostDeployInfoDTO.getPreCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(devopsCiHostDeployInfoDTO.getPreCommand())),
                ObjectUtils.isEmpty(devopsCiHostDeployInfoDTO.getRunCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(devopsCiHostDeployInfoDTO.getRunCommand())),
                ObjectUtils.isEmpty(devopsCiHostDeployInfoDTO.getPostCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(devopsCiHostDeployInfoDTO.getPostCommand())),
                ObjectUtils.isEmpty(devopsCiHostDeployInfoDTO.getKillCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(devopsCiHostDeployInfoDTO.getKillCommand())),
                ObjectUtils.isEmpty(devopsCiHostDeployInfoDTO.getHealthProb()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(devopsCiHostDeployInfoDTO.getHealthProb())),
                devopsCiHostDeployInfoDTO.getDeployType(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getVersion(),
                devopsHostAppDTO.getWorkDir());

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.OPERATE_INSTANCE.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setCiPipelineRecordId(gitlabPipelineId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 保存执行记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.AUTO,
                devopsHostCommandDTO.getId(),
                DeployModeEnum.HOST,
                hostId,
                devopsHostDTO != null ? devopsHostDTO.getName() : null,
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.OTHER,
                devopsCiHostDeployInfoDTO.getAppName(),
                null,
                devopsCiHostDeployInfoDTO.getAppName(),
                devopsCiHostDeployInfoDTO.getAppCode(),
                devopsHostAppDTO.getId(),
                new DeploySourceVO(AppSourceType.CURRENT_PROJECT, projectDTO.getName()));

        // 3. 发送部署指令给agent
        log.append("发送部署指令给agent...").append(System.lineSeparator());
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.OPERATE_INSTANCE.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
        log.append("发送成功").append(System.lineSeparator());
        return devopsHostCommandDTO.getId();
    }

    @Override
    public List<DevopsCiPipelineRecordDTO> listByPipelineId(Long pipelineId, java.sql.Date startTime, java.sql.Date endTime) {
        return devopsCiPipelineRecordMapper.listByPipelineId(pipelineId, startTime, endTime);
    }

    @Override
    public Page<CiPipelineRecordVO> pagingPipelineRecord(Long projectId, Long pipelineId, PageRequest pageable) {
        Page<CiPipelineRecordVO> recordPage = PageHelper.doPage(pageable, () -> devopsCiPipelineRecordMapper.listByCiPipelineId(pipelineId));
        List<CiPipelineRecordVO> content = recordPage.getContent();
        content.forEach(recordVO -> {
            fillAdditionalInfo(recordVO);
            recordVO.setViewId(CiCdPipelineUtils.handleId(recordVO.getId()));
            ciPipelineSyncHandler.syncPipeline(recordVO.getStatus(), recordVO.getLastUpdateDate(), recordVO.getId(), TypeUtil.objToInteger(recordVO.getGitlabPipelineId()));

            // 填充前端需要的字段
            recordVO.setCiRecordId(recordVO.getId());
            recordVO.setPipelineId(recordVO.getCiPipelineId());
        });
        // 填充用户信息
        UserDTOFillUtil.fillUserInfo(content, "triggerUserId", "iamUserDTO");
        return recordPage;
    }

    private String replaceValue(String code, String imageTag, String value) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        Object data = yaml.load(value);
        JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
        try {
            Map<String, Object> services = (Map<String, Object>) jsonObject.get("services");

            Map<String, Object> service = (Map<String, Object>) services.get(code);
            if (!CollectionUtils.isEmpty(service)) {
                service.replace("image", imageTag);
            }
            return yaml.dump(jsonObject);
        } catch (Exception e) {
            throw new CommonException(DEVOPS_YAML_FORMAT_INVALID, e);
        }
    }

    private DevopsHostAppDTO getDevopsHostAppDTO(Long projectId, Long hostId, String deployType, String appName, String appCode, String workDir) {
        if (org.apache.commons.lang3.StringUtils.equals(CREATE, deployType)) {
            DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO();
            devopsHostAppDTO.setRdupmType(RdupmTypeEnum.DOCKER.value());
            devopsHostAppDTO.setProjectId(projectId);
            devopsHostAppDTO.setHostId(hostId);
            devopsHostAppDTO.setName(appName);
            devopsHostAppDTO.setCode(appCode);
            devopsHostAppDTO.setWorkDir(workDir);
            devopsHostAppDTO.setOperationType(OperationTypeEnum.CREATE_APP.value());
            devopsHostAppMapper.insertSelective(devopsHostAppDTO);

            return devopsHostAppMapper.selectByPrimaryKey(devopsHostAppDTO.getId());
        } else {
            //查询主机应用实例
            DevopsHostAppDTO record = new DevopsHostAppDTO();
            record.setRdupmType(RdupmTypeEnum.DOCKER.value());
            record.setProjectId(projectId);
            record.setHostId(hostId);
            record.setName(appName);
            record.setCode(appCode);
            return devopsHostAppMapper.selectOne(record);
        }
    }

    protected String getMavenVersion(String version) {
        if (version.contains(SLASH)) {
            return version.split(SLASH)[0];
        } else {
            return version;
        }
    }
}
