package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_PIPELINE_ENV_AUTO_DEPLOY_INSTANCE;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.CustomContextUtil;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.GitUserNameUtil;

@Service
public class DevopsCdPipelineServiceImpl implements DevopsCdPipelineService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final String CREATE_PIPELINE_FAILED = "create.pipeline.failed";
    private static final String ERROR_USER_HAVE_NO_APP_PERMISSION = "error.user.have.no.app.permission";
    private static final String ERROR_UNSUPPORTED_STEP_TYPE = "error.unsupported.step.type";
    private static final String ERROR_CI_MAVEN_REPOSITORY_TYPE = "error.ci.maven.repository.type";
    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "error.maven.settings.insert";
    private static final String UPDATE_PIPELINE_FAILED = "update.pipeline.failed";
    private static final String DISABLE_PIPELINE_FAILED = "disable.pipeline.failed";
    private static final String ENABLE_PIPELINE_FAILED = "enable.pipeline.failed";
    private static final String DELETE_PIPELINE_FAILED = "delete.pipeline.failed";

    private static final String ERROR_PIPELINE_STATUS_CHANGED = "error.pipeline.status.changed";
    private static final String ERROR_PERMISSION_MISMATCH_FOR_AUDIT = "error.permission.mismatch.for.audit";
    private static final Integer ADMIN = 1;

    private static final String STAGE_NAME = "stageName";

    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    private static final Gson gson = new Gson();

    @Autowired
    private CiCdPipelineMapper ciCdPipelineMapper;
    @Autowired
    private DevopsCdStageMapper devopsCdStageMapper;
    @Autowired
    private CiCdJobMapper ciCdJobMapper;
    @Autowired
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCdStageService devopsCdStageService;
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
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsCdAuditService devopsCdAuditService;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;
    @Autowired
    private DevopsCdAuditMapper devopsCdAuditMapper;
    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;

    @Autowired
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;
    @Autowired
    private DevopsCdStageRecordService devopsCdStageRecordService;
    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;
    @Autowired
    private DevopsCdJobService devopsCdJobService;
    @Autowired
    private DevopsCdJobRecordService devopsCdJobRecordService;
    @Autowired
    private WorkFlowServiceOperator workFlowServiceOperator;
    @Autowired
    private DevopsCdAuditRecordService devopsCdAuditRecordService;
    @Autowired
    private SendNotificationService sendNotificationService;

    @Autowired
    private AppServiceVersionService appServiceVersionService;

    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;
    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
    @Autowired
    private UserAttrService userAttrService;

    @Override
    @Transactional
    public void handleCiPipelineStatusUpdate(PipelineWebHookVO pipelineWebHookVO) {
        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(pipelineWebHookVO.getToken());
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
        Long iamUserId = GitUserNameUtil.getIamUserIdByGitlabUserName(pipelineWebHookVO.getUser().getUsername());
        CustomContextUtil.setDefaultIfNull(iamUserId);
        // 查询流水线是否有cd阶段, 没有cd阶段不做处理
        List<DevopsCdStageDTO> devopsCdStageDTOList = devopsCdStageService.queryByPipelineId(devopsCiPipelineDTO.getId());
        if (CollectionUtils.isEmpty(devopsCdStageDTOList)) {
            return;
        }
        Map<Long, DevopsCdStageDTO> devopsCdStageDTOMap = devopsCdStageDTOList.stream().collect(Collectors.toMap(DevopsCdStageDTO::getId, v -> v));
        PipelineWebHookAttributesVO pipelineAttr = pipelineWebHookVO.getObjectAttributes();

        String status = pipelineAttr.getStatus();
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryByGitlabPipelineId(pipelineAttr.getId());
        LOGGER.info("handler ci pipeline status update, current ci pipeline {} status is {}", pipelineAttr.getId(), status);
        if (PipelineStatus.PENDING.toValue().equals(status)
                || PipelineStatus.RUNNING.toValue().equals(status)) {
            // 校验CD流水线记录是否已经创建，未创建才创建记录，并将记录的初始状态设置为pending
            if (devopsCdPipelineRecordDTO == null) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>> init cd pipeline >>>>>>>>>>>>>>>>>>>>>>>>>>>>", pipelineAttr.getId(), status);
                // 1. 根据流水线id,查询job列表
                List<DevopsCdJobDTO> devopsCdJobDTOList = devopsCdJobService.listByPipelineId(devopsCiPipelineDTO.getId());

                // 2. 计算要执行的job
                List<DevopsCdJobDTO> executeJobList = calculateExecuteJobList(pipelineAttr.getRef(), devopsCdJobDTOList);
                if (CollectionUtils.isEmpty(executeJobList)) {
                    return;
                }
                Map<Long, List<DevopsCdJobDTO>> executeJobMap = executeJobList.stream().collect(Collectors.groupingBy(DevopsCdJobDTO::getStageId));

                // 3. 统计出要执行的阶段（要执行的job的所属阶段）
                Set<Long> stageIds = executeJobList.stream().map(DevopsCdJobDTO::getStageId).collect(Collectors.toSet());
                List<DevopsCdStageDTO> executeStageList = stageIds.stream().map(devopsCdStageDTOMap::get).collect(Collectors.toList());

                // 4. 如果有要执行的阶段、job，则初始化执行记录（初始化记录状态为pending）
                if (!CollectionUtils.isEmpty(executeStageList)) {
                    // 保存流水线记录
                    devopsCdPipelineRecordDTO = initPipelineRecord(devopsCiPipelineDTO, pipelineAttr.getId(), pipelineAttr.getSha(), pipelineAttr.getRef());
                    // 创建cd阶段记录
                    DevopsCdPipelineRecordDTO finalDevopsCdPipelineRecordDTO = devopsCdPipelineRecordDTO;
                    devopsCdStageDTOList.forEach(stage -> {
                        DevopsCdStageRecordDTO devopsCdStageRecordDTO = initStageRecord(finalDevopsCdPipelineRecordDTO.getId(), stage);
                        // 手动流转阶段，添加审核人员记录
                        if (TriggerTypeEnum.MANUAL.value().equals(stage.getTriggerType())) {
                            addStageAuditRecord(stage.getId(), devopsCdStageRecordDTO.getId());
                        }
                        // 保存job执行记录
                        List<DevopsCdJobDTO> devopsCdJobDTOS = executeJobMap.get(stage.getId());
                        devopsCdJobDTOS.forEach(job -> {
                            DevopsCdJobRecordDTO devopsCdJobRecordDTO = initJobRecord(devopsCdStageRecordDTO.getId(), job);
                            // 人工卡点任务，添加审核人员记录
                            if (JobTypeEnum.CD_AUDIT.value().equals(job.getType())) {
                                addJobAuditRecord(job.getId(), devopsCdJobRecordDTO.getId());
                            }
                        });
                    });
                }
                LOGGER.info(">>>>>>>>>>>>>>>>>>>> init cd pipeline {} : {} success>>>>>>>>>>>>>>>>>>>>>>>>>>>>", devopsCdPipelineRecordDTO.getId(), devopsCdPipelineRecordDTO.getPipelineName());
            }

        }

        // ci流水线执行成功， 开始执行cd流水线
        if (PipelineStatus.SUCCESS.toValue().equals(status)) {
            if (devopsCdPipelineRecordDTO == null) {
                LOGGER.info("current pipeline have no match record.", pipelineAttr.getId());
                return;
            }
            // 执行条件：cd流水线记录状态为pending
            if (PipelineStatus.CREATED.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {
                LOGGER.info("current cd pipeline status is {}", devopsCdPipelineRecordDTO.getStatus());
                LOGGER.info(">>>>>>>>>>>>>>>>>>>> exec cd pipeline start >>>>>>>>>>>>>>>>>>>>>>>>>>>>", pipelineAttr.getId(), status);
                executeCdPipeline(devopsCdPipelineRecordDTO.getId());
                LOGGER.info(">>>>>>>>>>>>>>>>>>>> exec cd pipeline success >>>>>>>>>>>>>>>>>>>>>>>>>>>>", pipelineAttr.getId(), status);
            }

        }
    }

    private DevopsCdPipelineRecordDTO initPipelineRecord(CiCdPipelineDTO devopsCiPipelineDTO, Long gitlabPipelineId, String commitSha, String ref) {
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
        devopsCdPipelineRecordDTO.setPipelineId(devopsCiPipelineDTO.getId());
        devopsCdPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.CREATED.toValue());
        devopsCdPipelineRecordDTO.setPipelineName(devopsCiPipelineDTO.getName());
        devopsCdPipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
        devopsCdPipelineRecordDTO.setProjectId(devopsCiPipelineDTO.getProjectId());
        devopsCdPipelineRecordDTO.setCommitSha(commitSha);
        devopsCdPipelineRecordDTO.setRef(ref);
        devopsCdPipelineRecordService.save(devopsCdPipelineRecordDTO);
        return devopsCdPipelineRecordService.queryById(devopsCdPipelineRecordDTO.getId());
    }

    private DevopsCdStageRecordDTO initStageRecord(Long pipelineRecordId, DevopsCdStageDTO devopsCdStageDTO) {
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
        devopsCdStageRecordDTO.setPipelineRecordId(pipelineRecordId);
        devopsCdStageRecordDTO.setStageId(devopsCdStageDTO.getId());
        devopsCdStageRecordDTO.setStatus(PipelineStatus.CREATED.toValue());
        devopsCdStageRecordDTO.setStageName(devopsCdStageDTO.getName());
        devopsCdStageRecordDTO.setSequence(devopsCdStageDTO.getSequence());
        devopsCdStageRecordDTO.setProjectId(devopsCdStageDTO.getProjectId());
        devopsCdStageRecordDTO.setTriggerType(devopsCdStageDTO.getTriggerType());
        devopsCdStageRecordService.save(devopsCdStageRecordDTO);

        return devopsCdStageRecordService.queryById(devopsCdStageRecordDTO.getId());
    }

    private DevopsCdJobRecordDTO initJobRecord(Long stageRecordId, DevopsCdJobDTO job) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
        devopsCdJobRecordDTO.setName(job.getName());
        devopsCdJobRecordDTO.setStageRecordId(stageRecordId);
        devopsCdJobRecordDTO.setType(job.getType());
        devopsCdJobRecordDTO.setStatus(PipelineStatus.CREATED.toValue());
        devopsCdJobRecordDTO.setTriggerType(job.getTriggerType());
        devopsCdJobRecordDTO.setTriggerValue(job.getTriggerValue());
        devopsCdJobRecordDTO.setMetadata(job.getMetadata());
        devopsCdJobRecordDTO.setJobId(job.getId());
        devopsCdJobRecordDTO.setDeployInfoId(job.getDeployInfoId());
        devopsCdJobRecordDTO.setProjectId(job.getProjectId());
        devopsCdJobRecordDTO.setSequence(job.getSequence());
        devopsCdJobRecordDTO.setCountersigned(job.getCountersigned());

        devopsCdJobRecordService.save(devopsCdJobRecordDTO);

        return devopsCdJobRecordService.queryById(devopsCdJobRecordDTO.getId());
    }

    private void addStageAuditRecord(Long stageId, Long stageRecordId) {
        List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, stageId, null);
        devopsCdAuditDTOS.forEach(audit -> {
            DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = new DevopsCdAuditRecordDTO();
            devopsCdAuditRecordDTO.setStageRecordId(stageRecordId);
            devopsCdAuditRecordDTO.setUserId(audit.getUserId());
            devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.NOT_AUDIT.value());
            devopsCdAuditRecordService.save(devopsCdAuditRecordDTO);
        });
    }

    private void addJobAuditRecord(Long jobId, Long jobRecordId) {
        List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, null, jobId);
        devopsCdAuditDTOS.forEach(audit -> {
            DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = new DevopsCdAuditRecordDTO();
            devopsCdAuditRecordDTO.setJobRecordId(jobRecordId);
            devopsCdAuditRecordDTO.setUserId(audit.getUserId());
            devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.NOT_AUDIT.value());
            devopsCdAuditRecordService.save(devopsCdAuditRecordDTO);
        });
    }

    private List<DevopsCdJobDTO> calculateExecuteJobList(String ref, List<DevopsCdJobDTO> devopsCdJobDTOList) {
        return devopsCdJobDTOList.stream().filter(job -> {
            String triggerType = job.getTriggerType();
            // 根据匹配规则，计算出要执行的job
            if (CiTriggerType.REFS.value().equals(triggerType)
                    && job.getTriggerValue().contains(ref)) {
                return true;
            } else if (CiTriggerType.EXACT_MATCH.value().equals(triggerType)
                    && job.getTriggerValue().equals(ref)) {
                return true;
            } else if (CiTriggerType.EXACT_EXCLUDE.value().equals(triggerType)
                    && job.getTriggerValue().equals(ref)) {
                return false;
            } else if (CiTriggerType.REGEX_MATCH.value().equals(triggerType)) {
                Pattern pattern = Pattern.compile(job.getTriggerValue());
                return pattern.matcher(ref).matches();
            }
            return false;
        }).collect(Collectors.toList());
    }

    private void sendFailedSiteMessage(Long pipelineRecordId, Long userId) {
        sendNotificationService.sendCdPipelineNotice(pipelineRecordId,
                MessageCodeConstants.PIPELINE_FAILED, userId, null, null);
    }

    private void updateFirstStage(Long pipelineRecordId) {
        LOGGER.info(">>>>>>>>>>>>>>>>>>>> update first stage status<<<<<<<<<<<<<<<<<<<<<<<<<<");
        DevopsCdStageRecordDTO devopsCdStageRecord = devopsCdStageRecordService.queryFirstByPipelineRecordId(pipelineRecordId);
        LOGGER.info("current stage is {}", devopsCdStageRecord);
        if (TriggerTypeEnum.MANUAL.value().equals(devopsCdStageRecord.getTriggerType())) {
            devopsCdStageRecordService.updateStageStatusNotAudit(pipelineRecordId, devopsCdStageRecord.getId());
        } else {
            // 更新阶段状态为执行中
            devopsCdStageRecord.setStatus(PipelineStatus.RUNNING.toValue());
            devopsCdStageRecordService.update(devopsCdStageRecord);
            // 更新第一个job的状态
            updateFirstJob(pipelineRecordId, devopsCdStageRecord);
        }
    }

    private void updateFirstJob(Long pipelineRecordId, DevopsCdStageRecordDTO devopsCdStageRecord) {
        LOGGER.info(">>>>>>>>>>>>>>>>>>>> update first job status to running <<<<<<<<<<<<<<<<<<<<<<<<<<");
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryFirstByStageRecordId(devopsCdStageRecord.getId());
        if (devopsCdJobRecordDTO == null) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>> current stage have no job <<<<<<<<<<<<<<<<<<<<<<<<<<");
            return;
        }
        LOGGER.info("current stage first job is {}", devopsCdJobRecordDTO);
        if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordDTO.getType())) {
            devopsCdJobRecordService.updateJobStatusNotAudit(pipelineRecordId, devopsCdStageRecord.getId(), devopsCdJobRecordDTO.getId());
        }
    }


    @Override
    public void triggerCdPipeline(String token, String commitSha, String ref, Long gitlabPipelineId) {
        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());

        // 没有配置流水线或流水线已经停用不处理
        if (devopsCiPipelineDTO == null || Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
            return;
        }
        // 查询流水线是否有cd阶段, 没有cd阶段不做处理
        List<DevopsCdStageDTO> devopsCdStageDTOList = devopsCdStageService.queryByPipelineId(devopsCiPipelineDTO.getId());
        if (CollectionUtils.isEmpty(devopsCdStageDTOList)) {
            return;
        }

        // 初始化pipeline、stage、job、audit记录
        Map<Long, DevopsCdStageDTO> devopsCdStageDTOMap = devopsCdStageDTOList.stream().collect(Collectors.toMap(DevopsCdStageDTO::getId, v -> v));

        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
        // 1. 根据流水线id,查询job列表
        List<DevopsCdJobDTO> devopsCdJobDTOList = devopsCdJobService.listByPipelineId(devopsCiPipelineDTO.getId());

        // 2. 计算要执行的job
        List<DevopsCdJobDTO> executeJobList = calculateExecuteJobList(ref, devopsCdJobDTOList);
        if (CollectionUtils.isEmpty(executeJobList)) {
            return;
        }
        Map<Long, List<DevopsCdJobDTO>> executeJobMap = executeJobList.stream().collect(Collectors.groupingBy(DevopsCdJobDTO::getStageId));

        // 3. 统计出要执行的阶段（要执行的job的所属阶段）
        Set<Long> stageIds = executeJobList.stream().map(DevopsCdJobDTO::getStageId).collect(Collectors.toSet());
        List<DevopsCdStageDTO> executeStageList = stageIds.stream().map(devopsCdStageDTOMap::get).collect(Collectors.toList());

        // 4. 如果有要执行的阶段、job，则初始化执行记录（初始化记录状态为pending）
        if (!CollectionUtils.isEmpty(executeStageList)) {
            // 保存流水线记录
            devopsCdPipelineRecordDTO = initPipelineRecord(devopsCiPipelineDTO, gitlabPipelineId, commitSha, ref);
            // 创建cd阶段记录
            DevopsCdPipelineRecordDTO finalDevopsCdPipelineRecordDTO = devopsCdPipelineRecordDTO;
            devopsCdStageDTOList.forEach(stage -> {
                DevopsCdStageRecordDTO devopsCdStageRecordDTO = initStageRecord(finalDevopsCdPipelineRecordDTO.getId(), stage);
                // 手动流转阶段，添加审核人员记录
                if (TriggerTypeEnum.MANUAL.value().equals(stage.getTriggerType())) {
                    addStageAuditRecord(stage.getId(), devopsCdStageRecordDTO.getId());
                }
                // 保存job执行记录
                List<DevopsCdJobDTO> devopsCdJobDTOS = executeJobMap.get(stage.getId());
                devopsCdJobDTOS.forEach(job -> {
                    DevopsCdJobRecordDTO devopsCdJobRecordDTO = initJobRecord(devopsCdStageRecordDTO.getId(), job);
                    // 人工卡点任务，添加审核人员记录
                    if (JobTypeEnum.CD_AUDIT.value().equals(job.getType())) {
                        addJobAuditRecord(job.getId(), devopsCdJobRecordDTO.getId());
                    }
                });
            });
        }
        // 执行cd流水线
        executeCdPipeline(devopsCdPipelineRecordDTO.getId());

    }

    /**
     * 执行cd流水线
     *
     * @param pipelineRecordId
     */
    private void executeCdPipeline(Long pipelineRecordId) {
        DevopsPipelineDTO devopsPipelineDTO = devopsCdPipelineRecordService.createCDWorkFlowDTO(pipelineRecordId);
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
        devopsCdPipelineRecordDTO.setBpmDefinition(gson.toJson(devopsPipelineDTO));
        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
        // 更新流水线记录信息
        devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);

        try {
            CustomUserDetails details = DetailsHelper.getUserDetails();
            // 执行流水线
            createWorkFlow(devopsCdPipelineRecordDTO.getProjectId(), devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
            // 更新流水线执行状态
            updateFirstStage(devopsCdPipelineRecordDTO.getId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            sendFailedSiteMessage(devopsCdPipelineRecordDTO.getId(), GitUserNameUtil.getUserId().longValue());
            devopsCdPipelineRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
//            devopsCdPipelineRecordDTO.setErrorInfo(e.getMessage());
            devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
        }
    }

    @Override
    public void envAutoDeploy(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
        LOGGER.info("autoDeploy:pipelineRecordId {} stageRecordId: {} jobRecordId: {}", pipelineRecordId, stageRecordId, jobRecordId);
        //获取数据
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
        CustomContextUtil.setUserContext(devopsCdJobRecordDTO.getCreatedBy());
        DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(devopsCdJobRecordDTO.getDeployInfoId());
        AppServiceVersionDTO appServiceServiceE = getDeployVersion(pipelineRecordId);

        if (appServiceServiceE == null) {
            devopsCdJobRecordService.updateStatusById(jobRecordId, PipelineStatus.SKIPPED.toValue());
            return;
        }


        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO();
        appServiceDeployVO.setDeployInfoId(devopsCdJobRecordDTO.getDeployInfoId());
        try {
            if (CommandType.CREATE.getType().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
                addCreateInfoForAppServiceDeployVO(appServiceDeployVO, appServiceServiceE, devopsCdEnvDeployInfoDTO, devopsCdJobRecordDTO);
            } else if (CommandType.UPDATE.getType().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
                AppServiceInstanceDTO instanceE = appServiceInstanceService.baseQueryByCodeAndEnv(devopsCdEnvDeployInfoDTO.getInstanceName(), devopsCdEnvDeployInfoDTO.getEnvId());
                if (instanceE == null) {
                    addCreateInfoForAppServiceDeployVO(appServiceDeployVO, appServiceServiceE, devopsCdEnvDeployInfoDTO, devopsCdJobRecordDTO);
                } else {
                    appServiceDeployVO.setAppServiceVersionId(appServiceServiceE.getId());
                    appServiceDeployVO.setEnvironmentId(devopsCdEnvDeployInfoDTO.getEnvId());
                    appServiceDeployVO.setValues(devopsDeployValueService.baseQueryById(devopsCdEnvDeployInfoDTO.getValueId()).getValue());
                    appServiceDeployVO.setAppServiceId(devopsCdEnvDeployInfoDTO.getAppServiceId());
                    appServiceDeployVO.setType(CommandType.UPDATE.getType());
                    appServiceDeployVO.setRecordId(devopsCdJobRecordDTO.getId());
                    appServiceDeployVO.setValueId(devopsCdEnvDeployInfoDTO.getValueId());
                    appServiceDeployVO.setInstanceId(instanceE.getId());
                    appServiceDeployVO.setInstanceName(instanceE.getCode());

                    AppServiceInstanceDTO preInstance = appServiceInstanceService.baseQuery(appServiceDeployVO.getInstanceId());
                    DevopsEnvCommandDTO preCommand = devopsEnvCommandService.baseQuery(preInstance.getCommandId());
                    AppServiceVersionRespVO deploydAppServiceVersion = appServiceVersionService.queryById(preCommand.getObjectVersionId());
                    if (preCommand.getObjectVersionId().equals(appServiceDeployVO.getAppServiceVersionId())) {
                        String oldValue = appServiceInstanceService.baseQueryValueByInstanceId(appServiceDeployVO.getInstanceId());
                        if (appServiceDeployVO.getValues().trim().equals(oldValue.trim())) {
                            devopsCdJobRecordService.updateStatusById(jobRecordId, PipelineStatus.SKIPPED.toValue());
                            return;
                        }
                    }

                    AppServiceDTO appServiceDTO = appServiceService.baseQuery(devopsCdEnvDeployInfoDTO.getAppServiceId());

                    // 要部署版本的commit
                    CommitDTO currentCommit = gitlabServiceClientOperator.queryCommit(appServiceDTO.getGitlabProjectId(), appServiceServiceE.getCommit(), ADMIN);
                    // 已经部署版本的commit
                    CommitDTO deploydCommit = gitlabServiceClientOperator.queryCommit(appServiceDTO.getGitlabProjectId(), deploydAppServiceVersion.getCommit(), ADMIN);
                    if (deploydCommit != null && currentCommit != null) {
                        // 计算commitDate
                        // 如果要部署的版本的commitDate落后于环境中已经部署的版本，则跳过
                        // 如果现在部署的版本落后于已经部署的版本则跳过
                        if (currentCommit.getCommittedDate().before(deploydCommit.getCommittedDate())) {
                            devopsCdJobRecordService.updateStatusById(jobRecordId, PipelineStatus.SKIPPED.toValue());
                            return;
                        }
                    }
                }
            }

            // 开始执行job
            devopsCdJobRecordDTO.setStartedDate(new Date());
            devopsCdJobRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
            devopsCdJobRecordService.update(devopsCdJobRecordDTO);

            String input = gson.toJson(appServiceDeployVO);
            producer.apply(
                    StartSagaBuilder.newBuilder()
                            .withJson(input)
                            .withSagaCode(DEVOPS_PIPELINE_ENV_AUTO_DEPLOY_INSTANCE)
                            .withRefType("env")
                            .withRefId(devopsCdEnvDeployInfoDTO.getEnvId().toString())
                            .withLevel(ResourceLevel.PROJECT)
                            .withSourceId(devopsCdJobRecordDTO.getProjectId()),
                    builder -> {

                    });
        } catch (Exception e) {
            LOGGER.error("error.create.pipeline.auto.deploy.instance", e);
            sendFailedSiteMessage(pipelineRecordId, GitUserNameUtil.getUserId().longValue());
            devopsCdStageRecordService.updateStageStatusFailed(stageRecordId);
            devopsCdJobRecordService.updateJobStatusFailed(jobRecordId);
            devopsCdPipelineRecordService.updatePipelineStatusFailed(pipelineRecordId, e.getMessage());
        }
    }

    private void addCreateInfoForAppServiceDeployVO(AppServiceDeployVO appServiceDeployVO, AppServiceVersionDTO appServiceServiceE, DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO, DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
        appServiceDeployVO.setAppServiceVersionId(appServiceServiceE.getId());
        appServiceDeployVO.setEnvironmentId(devopsCdEnvDeployInfoDTO.getEnvId());
        appServiceDeployVO.setValues(devopsDeployValueService.baseQueryById(devopsCdEnvDeployInfoDTO.getValueId()).getValue());
        appServiceDeployVO.setAppServiceId(devopsCdEnvDeployInfoDTO.getAppServiceId());
        appServiceDeployVO.setType(CommandType.CREATE.getType());
        appServiceDeployVO.setRecordId(devopsCdJobRecordDTO.getId());
        appServiceDeployVO.setValueId(devopsCdEnvDeployInfoDTO.getValueId());
        appServiceDeployVO.setInstanceName(devopsCdEnvDeployInfoDTO.getInstanceName());
    }

    @Override
    public void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, Boolean status) {
        LOGGER.info("setAppDeployStatus:pipelineRecordId: {} stageRecordId: {} taskId: {}, status: {}", pipelineRecordId, stageRecordId, jobRecordId, status);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>> Userdetails is {}", DetailsHelper.getUserDetails());
        if (DetailsHelper.getUserDetails().getUserId().equals(BaseConstants.ANONYMOUS_USER_ID)) {
            DetailsHelper.setCustomUserDetails(0L, BaseConstants.DEFAULT_LOCALE_STR);
        }
        if (Boolean.TRUE.equals(status)) {
            startNextTask(pipelineRecordId, stageRecordId, jobRecordId);
        } else {
            DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
            workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
        }
    }

    @Override
    public String getDeployStatus(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(jobRecordId);
        if (jobRecordDTO != null) {
            return jobRecordDTO.getStatus();
        }
        return PipelineStatus.FAILED.toValue();
    }

    @Override
    @Transactional
    public void auditStage(Long projectId, Long pipelineRecordId, Long stageRecordId, String result) {

        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
        // 1. 查询审核人员
        CustomUserDetails details = DetailsHelper.getUserDetails();
        List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByStageRecordId(stageRecordId);
        Map<Long, DevopsCdAuditRecordDTO> auditRecordDTOMap = devopsCdAuditRecordDTOS.stream().collect(Collectors.toMap(DevopsCdAuditRecordDTO::getUserId, v -> v));
        List<Long> userIds = devopsCdAuditRecordDTOS.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
        DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = auditRecordDTOMap.get(details.getUserId());
        // 找不到对应记录，则说明用户没有审核权限
        if (devopsCdAuditRecordDTO == null) {
            throw new CommonException(ERROR_PERMISSION_MISMATCH_FOR_AUDIT);
        }
        // 状态不是待审核，抛出错误信息
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordService.queryById(stageRecordId);
        if (!PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdStageRecordDTO.getStatus())) {
            throw new CommonException(ERROR_PIPELINE_STATUS_CHANGED);
        }
        if (AuditStatusEnum.PASSED.value().equals(result)) {
            // 审核通过
            // 1. 工作流任务审核通过
            try {
                approveWorkFlow(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey(), details.getUsername(), details.getUserId(), details.getOrganizationId());
            } catch (Exception e) {
                LOGGER.error("Approve stage failed: {}", e.getMessage());
                // 更新阶段状态为失败
                devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.FAILED.toValue());
                // 更新流水线状态为失败
                devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.FAILED.toValue());
                // 停止流水线
                workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
                // 发送失败通知
                sendNotificationService.sendCdPipelineNotice(pipelineRecordId,
                        MessageCodeConstants.PIPELINE_FAILED, details.getUserId(), null, null);
            }

            // 更新审核状态为通过
            devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.PASSED.value());
            devopsCdAuditRecordService.update(devopsCdAuditRecordDTO);

            // 1. 更新阶段状态为执行中
            devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.RUNNING.toValue());
            // 2. 更新流水线状态为执行中
            devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.RUNNING.toValue());
            // 3. 查询阶段的第一个任务
            DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryFirstByStageRecordId(stageRecordId);
            // 4. 判断阶段的第一个任务是否是人工卡点任务，人工卡点任务则更新状态为待审核
            if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordDTO.getType())) {
                devopsCdJobRecordService.updateJobStatusNotAudit(pipelineRecordId, stageRecordId, devopsCdJobRecordDTO.getId());
            }
            sendNotificationService.sendPipelineAuditMassage(MessageCodeConstants.PIPELINE_SUCCESS, userIds, devopsCdPipelineRecordDTO.getId(), devopsCdStageRecordDTO.getStageName(), devopsCdStageRecordDTO.getStageId());
        } else if (AuditStatusEnum.REFUSED.value().equals(result)) {
            // 审核不通过
            // 1. 停止流水线
            workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
            // 2. 更新后续阶段以及任务状态为终止
            List<DevopsCdStageRecordDTO> afterStageRecordList = devopsCdStageRecordService.queryByPipelineRecordId(pipelineRecordId).stream()
                    .filter(v -> v.getSequence() >= devopsCdStageRecordDTO.getSequence())
                    .collect(Collectors.toList());
            afterStageRecordList.forEach(v -> {
                devopsCdStageRecordService.updateStageStatusStop(v.getId());
            });
            // 3. 更新流水线状态为终止
            devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.STOP.toValue());
            // 4. 发送审核记录通知
            sendNotificationService.sendPipelineAuditMassage(MessageCodeConstants.PIPELINE_STOP, userIds, devopsCdPipelineRecordDTO.getId(), devopsCdStageRecordDTO.getStageName(), devopsCdStageRecordDTO.getStageId());
        } else {
            throw new CommonException(ResourceCheckConstant.ERROR_PARAM_IS_INVALID);
        }
    }

    private void approveWorkFlow(Long projectId, String businessKey, String loginName, Long userId, Long orgId) {
        Observable.create((ObservableOnSubscribe<String>) Emitter::onComplete).subscribeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(String s) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        CustomContextUtil.setUserContext(loginName, userId, orgId);
                        try {
                            workFlowServiceOperator.approveUserTask(projectId, businessKey);
                        } catch (Exception e) {
                            throw new CommonException(e);
                        }
                    }
                });
    }

    private DevopsCdJobRecordDTO getNextJob(Long stageRecordId, DevopsCdJobRecordDTO currentJob) {
        List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(stageRecordId);
        Optional<DevopsCdJobRecordDTO> optionalDevopsCdJobRecordDTO = devopsCdJobRecordDTOS.stream()
                .filter(v -> !v.getId().equals(currentJob.getId()))
                .sorted(Comparator.comparing(DevopsCdJobRecordDTO::getSequence))
                .filter(v -> v.getSequence() > currentJob.getSequence()).findFirst();
        return optionalDevopsCdJobRecordDTO.orElse(null);
    }

    private void startNextTask(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
        DevopsCdStageRecordDTO currentStage = devopsCdStageRecordService.queryById(stageRecordId);
        DevopsCdJobRecordDTO currentJob = devopsCdJobRecordService.queryById(jobRecordId);

        DevopsCdJobRecordDTO nextJob = getNextJob(stageRecordId, currentJob);
        // 如果下一个任务为人工卡点任务，更新任务状态为待审核，并通知审核人员
        if (nextJob != null && JobTypeEnum.CD_AUDIT.value().equals(nextJob.getType())) {
            devopsCdJobRecordService.updateJobStatusNotAudit(pipelineRecordId, stageRecordId, nextJob.getId());
        }
        //如果没有下一个任务，则属于阶段的最后一个任务,表明当前阶段执行成功，执行下一个阶段
        if (nextJob == null) {
            // 1. 更新当前阶段状态为success
            currentStage.setStatus(PipelineStatus.SUCCESS.toValue());
            devopsCdStageRecordService.update(currentStage);
            // 2. 执行下一个阶段
            // 2.1 如果下一个阶段为空，则说明已经执行到了最后一个阶段， 则需要更新流水线状态为success
            // 2.2 如果存在下一个阶段，则判断阶段是自动流转还是手动流转
            // 2.2.1 自动流转：判断阶段的第一个任务是不是人工卡点任务， 人工卡点任务则更新任务状态为待审核，以及通知审核人员
            // 2.2.2 手动流转：更新任务状态为待审核，通知审核人员
            List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(pipelineRecordId);
            Optional<DevopsCdStageRecordDTO> optionalNextStage = devopsCdStageRecordDTOS.stream()
                    .filter(v -> !v.getId().equals(currentStage.getId()))
                    .sorted(Comparator.comparing(DevopsCdStageRecordDTO::getSequence))
                    .filter(v -> v.getSequence() > currentStage.getSequence())
                    .findFirst();
            DevopsCdStageRecordDTO nextStage = optionalNextStage.orElse(null);
            // 存在下一个阶段
            if (nextStage != null) {
                if (TriggerTypeEnum.MANUAL.value().equals(nextStage.getTriggerType())) {
                    // 手动流转
                    devopsCdStageRecordService.updateStageStatusNotAudit(pipelineRecordId, nextStage.getId());
                } else {
                    // 自动流转
                    DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryFirstByStageRecordId(nextStage.getId());
                    if (devopsCdJobRecordDTO == null) {
                        return;
                    }
                    if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordDTO.getType())) {
                        devopsCdJobRecordService.updateJobStatusNotAudit(pipelineRecordId, nextStage.getId(), devopsCdJobRecordDTO.getId());
                    }
                }
            } else {
                // 已经是最后一个阶段了
                devopsCdPipelineRecordService.updateStatusById(devopsCdPipelineRecordDTO.getId(), PipelineStatus.SUCCESS.toValue());
                sendNotificationService.sendCdPipelineNotice(devopsCdPipelineRecordDTO.getId(), MessageCodeConstants.PIPELINE_SUCCESS, devopsCdPipelineRecordDTO.getCreatedBy(), null, null);
            }
        }
    }


    private AppServiceVersionDTO getDeployVersion(Long pipelineRecordId) {
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
        return appServiceVersionService.queryByCommitShaAndRef(devopsCdPipelineRecordDTO.getCommitSha(), devopsCdPipelineRecordDTO.getRef());
    }

    @Override
    public void createWorkFlow(Long projectId, io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO devopsPipelineDTO, String loginName, Long userId, Long orgId) {

        Observable.create((ObservableOnSubscribe<String>) Emitter::onComplete).subscribeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(String s) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        CustomContextUtil.setUserContext(loginName, userId, orgId);
                        try {
                            workFlowServiceOperator.createCiCdPipeline(projectId, devopsPipelineDTO);
                        } catch (Exception e) {
                            throw new CommonException(e);
                        }
                    }
                });

    }

    @Override
    @Transactional
    public void auditJob(Long projectId, Long pipelineRecordId, Long stageRecordId, Long jobRecordId, String result) {
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordService.queryById(stageRecordId);
        // 1. 查询审核人员
        CustomUserDetails details = DetailsHelper.getUserDetails();
        List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(jobRecordId);
        Map<Long, DevopsCdAuditRecordDTO> auditRecordDTOMap = devopsCdAuditRecordDTOS.stream().collect(Collectors.toMap(DevopsCdAuditRecordDTO::getUserId, v -> v));
        List<Long> userIds = devopsCdAuditRecordDTOS.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
        DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = auditRecordDTOMap.get(details.getUserId());
        // 找不到对应记录，则说明用户没有审核权限
        if (devopsCdAuditRecordDTO == null) {
            throw new CommonException(ERROR_PERMISSION_MISMATCH_FOR_AUDIT);
        }
        // 状态不是待审核，抛出错误信息
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
        if (!PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
            throw new CommonException(ERROR_PIPELINE_STATUS_CHANGED);
        }
        if (AuditStatusEnum.PASSED.value().equals(result)) {
            // 1. 工作流任务审核通过
            try {
                // 审核通过
                // 判断是否是或签任务
                if (devopsCdJobRecordDTO.getCountersigned() != null && devopsCdJobRecordDTO.getCountersigned() == 0) {
                    approveWorkFlow(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey(), details.getUsername(), details.getUserId(), details.getOrganizationId());
                    // 更新审核状态为通过
                    devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.PASSED.value());
                    devopsCdAuditRecordService.update(devopsCdAuditRecordDTO);

                    // 更新job状态为success
                    devopsCdJobRecordService.updateStatusById(devopsCdJobRecordDTO.getId(), PipelineStatus.SUCCESS.toValue());

                    // 发送通知
                    sendNotificationService.sendPipelineAuditMassage(MessageCodeConstants.PIPELINE_PASS, userIds, pipelineRecordId, devopsCdStageRecordDTO.getStageName(), devopsCdStageRecordDTO.getStageId());

                    // 执行下一个任务
                    startNextTask(pipelineRecordId, stageRecordId, jobRecordId);
                } else if (devopsCdJobRecordDTO.getCountersigned() != null && devopsCdJobRecordDTO.getCountersigned() == 1) {
                    approveWorkFlow(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey(), details.getUsername(), details.getUserId(), details.getOrganizationId());
                    // 更新审核状态为通过
                    devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.PASSED.value());
                    devopsCdAuditRecordService.update(devopsCdAuditRecordDTO);

                    if (devopsCdAuditRecordService.queryByJobRecordId(jobRecordId).stream()
                            .filter(v -> !v.getId().equals(devopsCdAuditRecordDTO.getId()))
                            .allMatch(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus()))) {
                        // 更新job状态为success
                        devopsCdJobRecordService.updateStatusById(devopsCdJobRecordDTO.getId(), PipelineStatus.SUCCESS.toValue());
                        // 执行下一个任务
                        startNextTask(pipelineRecordId, stageRecordId, jobRecordId);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Approve job failed: {}", e.getMessage());
                // 更新job状态为失败
                devopsCdJobRecordService.updateStatusById(jobRecordId, PipelineStatus.FAILED.toValue());
                // 更新阶段状态为失败
                devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.FAILED.toValue());
                // 更新流水线状态为失败
                devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.FAILED.toValue());
                // 停止流水线
                workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
                // 发送失败通知
                sendNotificationService.sendCdPipelineNotice(pipelineRecordId,
                        MessageCodeConstants.PIPELINE_FAILED, details.getUserId(), null, null);
            }
        } else if (AuditStatusEnum.REFUSED.value().equals(result)) {
            // 审核不通过
            // 1. 停止流水线
            workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
            // 更新当前阶段状态为stop
            devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.STOP.toValue());
            // 更新当前阶段中的后续任务状态为stop
            List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(stageRecordId);
            List<DevopsCdJobRecordDTO> stopJobRecordList = devopsCdJobRecordDTOS.stream()
                    .filter(v -> v.getSequence() >= devopsCdJobRecordDTO.getSequence())
                    .collect(Collectors.toList());
            stopJobRecordList.forEach(v -> devopsCdJobRecordService.updateStatusById(v.getId(), PipelineStatus.STOP.toValue()));
            // 2. 更新后续阶段以及任务状态为终止
            // 2.1 更新当前阶段状态为stop
            // 2.2 更新后续阶段状态为stop
            devopsCdStageRecordService.updateStatusById(devopsCdStageRecordDTO.getId(), PipelineStatus.STOP.toValue());
            List<DevopsCdStageRecordDTO> afterStageRecordList = devopsCdStageRecordService.queryByPipelineRecordId(pipelineRecordId).stream()
                    .filter(v -> v.getSequence() > devopsCdStageRecordDTO.getSequence())
                    .collect(Collectors.toList());
            afterStageRecordList.forEach(v -> devopsCdStageRecordService.updateStageStatusStop(v.getId()));
            // 3. 更新流水线状态为终止
            devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.STOP.toValue());
            // 4. 发送审核记录通知
            sendNotificationService.sendPipelineAuditMassage(MessageCodeConstants.PIPELINE_STOP, userIds, devopsCdPipelineRecordDTO.getId(), devopsCdStageRecordDTO.getStageName(), devopsCdStageRecordDTO.getStageId());
        }
    }

    @Override
    public AduitStatusChangeVO checkAuditStatus(Long projectId, Long pipelineRecordId, AuditCheckVO auditCheckVO) {
        AduitStatusChangeVO aduitStatusChangeVO = new AduitStatusChangeVO();
        aduitStatusChangeVO.setAuditStatusChanged(false);
        if ("stage".equals(auditCheckVO.getSourceType())) {
            DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordService.queryById(auditCheckVO.getSourceId());
            if (!PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdStageRecordDTO.getStatus())) {
                List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByStageRecordId(auditCheckVO.getSourceId());
                Optional<DevopsCdAuditRecordDTO> optionalDevopsCdAuditRecordDTO = devopsCdAuditRecordDTOS.stream().filter(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus())).findFirst();
                calculatAuditUserName(optionalDevopsCdAuditRecordDTO, aduitStatusChangeVO);
            }
            return aduitStatusChangeVO;
        } else if ("job".equals(auditCheckVO.getSourceType())) {
            DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(auditCheckVO.getSourceId());
            if (!PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
                List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(auditCheckVO.getSourceId());
                Optional<DevopsCdAuditRecordDTO> optionalDevopsCdAuditRecordDTO = devopsCdAuditRecordDTOS.stream().filter(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus())).findFirst();
                calculatAuditUserName(optionalDevopsCdAuditRecordDTO, aduitStatusChangeVO);
            }
            return aduitStatusChangeVO;
        } else {
            throw new CommonException(ResourceCheckConstant.ERROR_PARAM_IS_INVALID);
        }
    }

    private void calculatAuditUserName(Optional<DevopsCdAuditRecordDTO> optionalDevopsCdAuditRecordDTO, AduitStatusChangeVO aduitStatusChangeVO) {
        if (optionalDevopsCdAuditRecordDTO.isPresent()) {
            aduitStatusChangeVO.setAuditStatusChanged(true);
            Long userId = optionalDevopsCdAuditRecordDTO.get().getUserId();
            IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);
            if (iamUserDTO.getLdap()) {
                aduitStatusChangeVO.setAuditUserName(iamUserDTO.getLoginName());
            } else {
                aduitStatusChangeVO.setAuditUserName(iamUserDTO.getEmail());
            }
        }
    }


}
