//package io.choerodon.devops.app.service.impl;
//
//import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_CI_PIPELINE_SUCCESS_FOR_SIMPLE_CD;
//
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//import javax.annotation.Nullable;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import org.apache.commons.lang3.StringUtils;
//import org.hzero.core.base.BaseConstants;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.client.RestTemplate;
//
//import io.choerodon.asgard.saga.annotation.Saga;
//import io.choerodon.asgard.saga.producer.StartSagaBuilder;
//import io.choerodon.asgard.saga.producer.TransactionalProducer;
//import io.choerodon.core.convertor.ApplicationContextHelper;
//import io.choerodon.core.exception.CommonException;
//import io.choerodon.core.iam.ResourceLevel;
//import io.choerodon.core.oauth.CustomUserDetails;
//import io.choerodon.core.oauth.DetailsHelper;
//import io.choerodon.devops.api.vo.*;
//import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO;
//import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
//import io.choerodon.devops.app.service.*;
//import io.choerodon.devops.infra.constant.MessageCodeConstants;
//import io.choerodon.devops.infra.constant.MiscConstants;
//import io.choerodon.devops.infra.constant.PipelineConstants;
//import io.choerodon.devops.infra.constant.ResourceCheckConstant;
//import io.choerodon.devops.infra.dto.*;
//import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
//import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
//import io.choerodon.devops.infra.dto.iam.IamUserDTO;
//import io.choerodon.devops.infra.dto.iam.ProjectDTO;
//import io.choerodon.devops.infra.dto.repo.C7nNexusRepoDTO;
//import io.choerodon.devops.infra.dto.test.ApiTestTaskRecordDTO;
//import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
//import io.choerodon.devops.infra.enums.*;
//import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
//import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
//import io.choerodon.devops.infra.enums.test.ApiTestTaskType;
//import io.choerodon.devops.infra.enums.test.ApiTestTriggerType;
//import io.choerodon.devops.infra.feign.operator.*;
//import io.choerodon.devops.infra.gitops.IamAdminIdHolder;
//import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;
//import io.choerodon.devops.infra.mapper.DevopsCiCdPipelineMapper;
//import io.choerodon.devops.infra.mapper.DevopsCiPipelineRecordMapper;
//import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
//import io.choerodon.devops.infra.util.*;
//
//@Service
//public class DevopsCdPipelineServiceImpl implements DevopsCdPipelineService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCdPipelineServiceImpl.class);
//
//    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    private static final String AUTH_HEADER = "c7n-pipeline-token";
//    private static final String STATUS_CODE = "statusCode";
//
//
//    private static final String ERROR_PIPELINE_STATUS_CHANGED = "devops.pipeline.status.changed";
//    private static final String ERROR_PERMISSION_MISMATCH_FOR_AUDIT = "devops.permission.mismatch.for.audit";
//    private static final String AUDIT_TASK_CALLBACK_URL = "/devops/v1/cd_pipeline/external_approval_task/callback";
//    private static final String PIPELINE_LINK_URL_TEMPLATE = "/#/devops/pipeline-manage?type=project&id=%s&name=%s&organizationId=%s&pipelineId=%s&pipelineIdRecordId=%s";
//
//    private static final Integer GITLAB_ADMIN_ID = 1;
//
//    private static final Gson gson = new Gson();
//
//    @Value(value = "${services.gateway.url: http://api.example.com}")
//    private String gatewayUrl;
//    @Value(value = "${services.front.url: http://app.example.com}")
//    private String frontUrl;
//
//    @Autowired
//    private AppServiceService appServiceService;
//    @Autowired
//    private DevopsCdStageService devopsCdStageService;
//    @Autowired
//    private BaseServiceClientOperator baseServiceClientOperator;
//    @Autowired
//    private GitlabServiceClientOperator gitlabServiceClientOperator;
//    @Autowired
//    private DevopsCdAuditService devopsCdAuditService;
//    @Autowired
//    private AppServiceService applicationService;
//    @Autowired
//    @Lazy
//    private DevopsCiPipelineService devopsCiPipelineService;
//
//    @Autowired
//    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;
//    @Autowired
//    private DevopsCdStageRecordService devopsCdStageRecordService;
//    @Autowired
//    private DevopsCdJobService devopsCdJobService;
//    @Autowired
//    private DevopsCdJobRecordService devopsCdJobRecordService;
//    @Autowired
//    private WorkFlowServiceOperator workFlowServiceOperator;
//    @Autowired
//    private DevopsCdAuditRecordService devopsCdAuditRecordService;
//    @Autowired
//    private SendNotificationService sendNotificationService;
//
//    @Autowired
//    private AppServiceVersionService appServiceVersionService;
//
//    @Autowired
//    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
//
//    @Autowired
//    private AppServiceInstanceService appServiceInstanceService;
//    @Autowired
//    private DevopsDeployValueService devopsDeployValueService;
//    @Autowired
//    private DevopsEnvCommandService devopsEnvCommandService;
//    @Autowired
//    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;
//    @Autowired
//    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    @Autowired
//    @Lazy
//    private DevopsCiStageService devopsCiStageService;
//    @Autowired
//    private TransactionalProducer transactionalProducer;
//
//    @Autowired
//    private DevopsPipelineRecordRelService devopsPipelineRecordRelService;
//    @Autowired
//    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
//    @Autowired
//    private TestServiceClientOperator testServiceClientoperator;
//    @Autowired
//    private DevopsEnvPodService devopsEnvPodService;
//    @Autowired
//    private DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;
//    @Autowired
//    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
//
//    @Autowired
//    private DevopsCiApiTestInfoService devopsCiApiTestInfoService;
//
//    @Autowired
//    @Qualifier("restTemplateForIp")
//    private RestTemplate restTemplateForIp;
//
//    @Autowired
//    @Lazy
//    private CiCdPipelineRecordService ciCdPipelineRecordService;
//
//    @Autowired
//    @Lazy
//    private DevopsEnvironmentMapper devopsEnvironmentMapper;
//    @Autowired
//    private DevopsDeployAppCenterService devopsDeployAppCenterService;
//    @Autowired
//    private DevopsDeployGroupService devopsDeployGroupService;
//    @Autowired
//    private CiPipelineImageService ciPipelineImageService;
//    @Autowired
//    private RdupmClientOperator rdupmClientOperator;
//    @Autowired
//    private CiPipelineMavenService ciPipelineMavenService;
//    @Autowired
//    private DevopsCdApiTestInfoService devopsCdApiTestInfoService;
//    @Autowired
//    private DevopsCiJobRecordService devopsCiJobRecordService;
//    @Autowired
//    private DevopsCiJobService devopsCiJobService;
//
////    @Override
////    @Transactional
////    public void handleCiPipelineStatusUpdate(PipelineWebHookVO pipelineWebHookVO) {
////        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(pipelineWebHookVO.getToken());
////        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
////        Long iamUserId = GitUserNameUtil.getIamUserIdByGitlabUserName(pipelineWebHookVO.getUser().getUsername());
////        CustomContextUtil.setDefaultIfNull(iamUserId);
////        PipelineWebHookAttributesVO pipelineAttr = pipelineWebHookVO.getObjectAttributes();
////        String status = pipelineAttr.getStatus();
////
////        LOGGER.info("handler ci pipeline status update, current ci pipeline {} status is {}", pipelineAttr.getId(), status);
////        // 初始化流水线记录
////        if (!PipelineStatus.SUCCESS.toValue().equals(status) && !PipelineStatus.SKIPPED.toValue().equals(status)) {
////            initPipelineRecordWithStageAndJob(appServiceDTO.getProjectId(), pipelineAttr.getId(), pipelineAttr.getSha(), pipelineAttr.getRef(), pipelineAttr.getTag(), devopsCiPipelineDTO);
////        }
////
////        // ci流水线执行成功， 开始执行cd流水线
////        if (PipelineStatus.SUCCESS.toValue().equals(status)) {
////            DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryByGitlabPipelineId(devopsCiPipelineDTO.getId(), pipelineAttr.getId());
////            if (devopsCdPipelineRecordDTO == null) {
////                LOGGER.info("current pipeline have no match ciPipelineRecordDTO.");
////                DevopsCiPipelineRecordDTO ciPipelineRecordDTO = new DevopsCiPipelineRecordDTO();
////                ciPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
////                DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(ciPipelineRecordDTO);
////                sendNotificationService.sendCiPipelineNotice(devopsCiPipelineRecordDTO.getId(), MessageCodeConstants.PIPELINE_SUCCESS, devopsCiPipelineRecordDTO.getCreatedBy(), null, new HashMap<>());
////                return;
////            }
////            // 执行条件：cd流水线记录状态为pending
////            if (PipelineStatus.CREATED.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {
////                LOGGER.info("current cd pipeline status is {}", devopsCdPipelineRecordDTO.getStatus());
////                LOGGER.info(">>>>>>>>>>>>>>>>>>>> exec cd pipeline start >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
////                executeCdPipeline(devopsCdPipelineRecordDTO.getId());
////                LOGGER.info(">>>>>>>>>>>>>>>>>>>> exec cd pipeline success >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
////            }
////
////        }
////    }
//
//    private DevopsCdPipelineRecordDTO initPipelineRecord(CiCdPipelineDTO devopsCiPipelineDTO, Long gitlabPipelineId, String commitSha, String ref) {
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
//        devopsCdPipelineRecordDTO.setPipelineId(devopsCiPipelineDTO.getId());
//        devopsCdPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
//        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.CREATED.toValue());
//        devopsCdPipelineRecordDTO.setPipelineName(devopsCiPipelineDTO.getName());
//        devopsCdPipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
//        devopsCdPipelineRecordDTO.setProjectId(devopsCiPipelineDTO.getProjectId());
//        devopsCdPipelineRecordDTO.setCommitSha(commitSha);
//        devopsCdPipelineRecordDTO.setRef(ref);
//        devopsCdPipelineRecordService.save(devopsCdPipelineRecordDTO);
//        return devopsCdPipelineRecordService.queryById(devopsCdPipelineRecordDTO.getId());
//    }
//
//    private DevopsCdStageRecordDTO initStageRecord(Long pipelineRecordId, DevopsCdStageDTO devopsCdStageDTO) {
//        DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
//        devopsCdStageRecordDTO.setPipelineRecordId(pipelineRecordId);
//        devopsCdStageRecordDTO.setStageId(devopsCdStageDTO.getId());
//        devopsCdStageRecordDTO.setStatus(PipelineStatus.CREATED.toValue());
//        devopsCdStageRecordDTO.setStageName(devopsCdStageDTO.getName());
//        devopsCdStageRecordDTO.setSequence(devopsCdStageDTO.getSequence());
//        devopsCdStageRecordDTO.setProjectId(devopsCdStageDTO.getProjectId());
//        devopsCdStageRecordService.save(devopsCdStageRecordDTO);
//
//        return devopsCdStageRecordService.queryById(devopsCdStageRecordDTO.getId());
//    }
//
//    private DevopsCdJobRecordDTO initJobRecord(Long stageRecordId, DevopsCdJobDTO job) {
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
//        devopsCdJobRecordDTO.setName(job.getName());
//        devopsCdJobRecordDTO.setStageRecordId(stageRecordId);
//        devopsCdJobRecordDTO.setType(job.getType());
//        devopsCdJobRecordDTO.setStatus(PipelineStatus.CREATED.toValue());
//        devopsCdJobRecordDTO.setTriggerType(job.getTriggerType());
//        devopsCdJobRecordDTO.setTriggerValue(job.getTriggerValue());
//        devopsCdJobRecordDTO.setMetadata(job.getMetadata());
//        devopsCdJobRecordDTO.setJobId(job.getId());
//        devopsCdJobRecordDTO.setDeployInfoId(job.getDeployInfoId());
//        devopsCdJobRecordDTO.setProjectId(job.getProjectId());
//        devopsCdJobRecordDTO.setSequence(job.getSequence());
//        devopsCdJobRecordDTO.setCountersigned(job.getCountersigned());
//
//        devopsCdJobRecordService.save(devopsCdJobRecordDTO);
//
//        return devopsCdJobRecordService.queryById(devopsCdJobRecordDTO.getId());
//    }
//
//    private void addJobAuditRecord(Long projectId, Long jobId, Long jobRecordId) {
//        List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, null, jobId);
//        devopsCdAuditDTOS.forEach(audit -> {
//            DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = new DevopsCdAuditRecordDTO();
//            devopsCdAuditRecordDTO.setJobRecordId(jobRecordId);
//            devopsCdAuditRecordDTO.setUserId(audit.getUserId());
//            devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.NOT_AUDIT.value());
//            devopsCdAuditRecordDTO.setProjectId(projectId);
//            devopsCdAuditRecordService.save(devopsCdAuditRecordDTO);
//        });
//    }
//
//    private List<DevopsCdJobDTO> calculateExecuteJobList(String ref, Boolean tag, List<DevopsCdJobDTO> devopsCdJobDTOList) {
//        return devopsCdJobDTOList.stream().filter(job -> {
//            if (StringUtils.isEmpty(job.getTriggerValue())) {
//                return true;
//            }
//            String triggerType = job.getTriggerType();
//            // 根据匹配规则，计算出要执行的job
//            if (CiTriggerType.REFS.value().equals(triggerType)) {
//                String[] matchRefs = job.getTriggerValue().split(",");
//                if (matchRefs.length > 0) {
//                    for (String matchRef : matchRefs) {
//                        if ("tag".equals(matchRef) && Boolean.TRUE.equals(tag)) {
//                            return true;
//                        }
//                        if (ref.contains(matchRef)) {
//                            return true;
//                        }
//                    }
//                }
//                return false;
//            } else if (CiTriggerType.EXACT_MATCH.value().equals(triggerType)) {
//                String[] matchRefs = job.getTriggerValue().split(",");
//                if (matchRefs.length > 0) {
//                    for (String matchRef : matchRefs) {
//                        if (ref.equals(matchRef)) {
//                            return true;
//                        }
//                    }
//                }
//                return false;
//            } else if (CiTriggerType.EXACT_EXCLUDE.value().equals(triggerType)) {
//                String[] matchRefs = job.getTriggerValue().split(",");
//                if (matchRefs.length > 0) {
//                    for (String matchRef : matchRefs) {
//                        if (ref.equals(matchRef)) {
//                            return false;
//                        }
//                    }
//                }
//                return true;
//            } else if (CiTriggerType.REGEX_MATCH.value().equals(triggerType)) {
//                try {
//                    Pattern pattern = Pattern.compile(job.getTriggerValue());
//                    return pattern.matcher(ref).matches();
//                } catch (Exception e) {
//                    LOGGER.info("parse regex failed, regex: {}, ref: {}, error: {}", job.getTriggerValue(), ref, e);
//                }
//                return false;
//
//            }
//            return false;
//        }).collect(Collectors.toList());
//    }
//
//    private void sendFailedSiteMessage(Long pipelineRecordId, Long userId) {
//        sendNotificationService.sendCdPipelineNotice(pipelineRecordId,
//                MessageCodeConstants.PIPELINE_FAILED, userId, null, new HashMap<>());
//    }
//
//    private void updateFirstStage(Long pipelineRecordId) {
//        LOGGER.info(">>>>>>>>>>>>>>>>>>>> update first stage status<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        DevopsCdStageRecordDTO devopsCdStageRecord = devopsCdStageRecordService.queryFirstByPipelineRecordId(pipelineRecordId);
//        LOGGER.info("current stage is {}", devopsCdStageRecord);
//
//        // 更新阶段状态为执行中
//        devopsCdStageRecord.setStatus(PipelineStatus.RUNNING.toValue());
//        devopsCdStageRecordService.update(devopsCdStageRecord);
//        // 更新第一个job的状态
//        updateFirstJob(pipelineRecordId, devopsCdStageRecord);
//    }
//
//    private void updateFirstJob(Long pipelineRecordId, DevopsCdStageRecordDTO devopsCdStageRecord) {
//        LOGGER.info(">>>>>>>>>>>>>>>>>>>> update first job status to running <<<<<<<<<<<<<<<<<<<<<<<<<<");
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryFirstByStageRecordId(devopsCdStageRecord.getId());
//        if (devopsCdJobRecordDTO == null) {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>>> current stage have no job <<<<<<<<<<<<<<<<<<<<<<<<<<");
//            return;
//        }
//        LOGGER.info("current stage first job is {}", devopsCdJobRecordDTO);
//        if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordDTO.getType())) {
//            devopsCdJobRecordService.updateJobStatusNotAudit(pipelineRecordId, devopsCdStageRecord.getId(), devopsCdJobRecordDTO.getId());
//        }
//    }
//
//
//    @Override
//    public void triggerCdPipeline(Long projectId, String token, String commitSha, String ref, Boolean tag, Long gitlabPipelineId) {
//        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);
//        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
//
//        // 没有配置流水线或流水线已经停用不处理
//        if (devopsCiPipelineDTO == null || Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
//            return;
//        }
//        // 查询流水线是否有cd阶段, 没有cd阶段不做处理
//        List<DevopsCdStageDTO> devopsCdStageDTOList = devopsCdStageService.queryByPipelineId(devopsCiPipelineDTO.getId());
//        if (CollectionUtils.isEmpty(devopsCdStageDTOList)) {
//            return;
//        }
//
//        // 初始化pipeline、stage、job、audit记录
//        Map<Long, DevopsCdStageDTO> devopsCdStageDTOMap = devopsCdStageDTOList.stream().collect(Collectors.toMap(DevopsCdStageDTO::getId, v -> v));
//
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
//        // 1. 根据流水线id,查询job列表
//        List<DevopsCdJobDTO> devopsCdJobDTOList = devopsCdJobService.listByPipelineId(devopsCiPipelineDTO.getId());
//
//        // 2. 计算要执行的job
//        List<DevopsCdJobDTO> executeJobList = calculateExecuteJobList(ref, tag, devopsCdJobDTOList);
//        if (CollectionUtils.isEmpty(executeJobList)) {
//            return;
//        }
//        Map<Long, List<DevopsCdJobDTO>> executeJobMap = executeJobList.stream().collect(Collectors.groupingBy(DevopsCdJobDTO::getStageId));
//
//        // 3. 统计出要执行的阶段（要执行的job的所属阶段）
//        Set<Long> stageIds = executeJobList.stream().map(DevopsCdJobDTO::getStageId).collect(Collectors.toSet());
//        List<DevopsCdStageDTO> executeStageList = stageIds.stream().map(devopsCdStageDTOMap::get).collect(Collectors.toList());
//
//        // 4. 如果有要执行的阶段、job，则初始化执行记录（初始化记录状态为pending）
//        if (!CollectionUtils.isEmpty(executeStageList)) {
//            // 保存流水线记录
//            devopsCdPipelineRecordDTO = initPipelineRecord(devopsCiPipelineDTO, gitlabPipelineId, commitSha, ref);
//            // 保存流水线关系记录
//            DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = new DevopsPipelineRecordRelDTO();
//            devopsPipelineRecordRelDTO.setPipelineId(devopsCiPipelineDTO.getId());
//            devopsPipelineRecordRelDTO.setCdPipelineRecordId(devopsCdPipelineRecordDTO.getId());
//            devopsPipelineRecordRelDTO.setCiPipelineRecordId(PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID);
//            devopsPipelineRecordRelService.save(devopsPipelineRecordRelDTO);
//            // 创建cd阶段记录
//            DevopsCdPipelineRecordDTO finalDevopsCdPipelineRecordDTO = devopsCdPipelineRecordDTO;
//            devopsCdStageDTOList.forEach(stage -> {
//                DevopsCdStageRecordDTO devopsCdStageRecordDTO = initStageRecord(finalDevopsCdPipelineRecordDTO.getId(), stage);
//
//                // 保存job执行记录
//                List<DevopsCdJobDTO> devopsCdJobDTOS = executeJobMap.get(stage.getId());
//                devopsCdJobDTOS.forEach(job -> {
//                    DevopsCdJobRecordDTO devopsCdJobRecordDTO = initJobRecord(devopsCdStageRecordDTO.getId(), job);
//                    // 人工卡点任务，添加审核人员记录
//                    if (JobTypeEnum.CD_AUDIT.value().equals(job.getType())) {
//                        addJobAuditRecord(projectId, job.getId(), devopsCdJobRecordDTO.getId());
//                    }
//                });
//            });
//        }
//        // 执行cd流水线
//        executeCdPipeline(devopsCdPipelineRecordDTO.getId());
//
//    }
//
//    /**
//     * 执行cd流水线
//     *
//     * @param pipelineRecordId
//     */
//    @Override
//    public void executeCdPipeline(Long pipelineRecordId) {
//        DevopsPipelineDTO devopsPipelineDTO = devopsCdPipelineRecordService.createCDWorkFlowDTO(pipelineRecordId);
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        devopsCdPipelineRecordDTO.setBpmDefinition(gson.toJson(devopsPipelineDTO));
//        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
//        // 更新流水线记录信息
//        devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
//        CustomUserDetails details = DetailsHelper.getUserDetails();
//        try {
//            // 执行流水线
//            createWorkFlow(devopsCdPipelineRecordDTO.getProjectId(), devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
//            // 更新流水线执行状态
//            updateFirstStage(devopsCdPipelineRecordDTO.getId());
//        } catch (Exception e) {
//            LOGGER.error("create WorkFlow error", e);
//            sendFailedSiteMessage(devopsCdPipelineRecordDTO.getId(), GitUserNameUtil.getUserId());
//            devopsCdPipelineRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
//            devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
//        }
//    }
//
//    @Override
//    @Async
//    @Transactional
//    public void envAutoDeploy(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
//        StringBuilder log = new StringBuilder();
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
//        if (PipelineStatus.CANCELED.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
//            return;
//        }
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>Begin envAutoDeploy,devopsCdJobRecordDTO: {}<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(devopsCdJobRecordDTO));
//        }
//        try {
//            if (JobTypeEnum.CD_DEPLOY.value().equals(devopsCdJobRecordDTO.getType())) {
//                ApplicationContextHelper
//                        .getContext()
//                        .getBean(DevopsCdPipelineService.class)
//                        .pipelineDeploy(pipelineRecordId, stageRecordId, jobRecordId, log);
//            } else if (JobTypeEnum.CD_DEPLOYMENT.value().equals(devopsCdJobRecordDTO.getType())) {
//                ApplicationContextHelper
//                        .getContext()
//                        .getBean(DevopsCdPipelineService.class)
//                        .pipelineDeployDeployment(pipelineRecordId, stageRecordId, jobRecordId, log);
//            }
//
//        } catch (Exception e) {
//            LOGGER.error("devops.create.pipeline.auto.deploy.instance", e);
//            log.append("Deploy app instance failed").append(System.lineSeparator());
//            log.append(LogUtil.cutOutString(LogUtil.readContentOfThrowable(e), 2500)).append(System.lineSeparator());
//            devopsCdStageRecordService.updateStageStatusFailed(stageRecordId);
//            devopsCdJobRecordService.updateJobStatusFailed(jobRecordId, log.toString());
//            devopsCdPipelineRecordService.updatePipelineStatusFailed(pipelineRecordId);
//            sendNotificationService.sendCdPipelineNotice(pipelineRecordId,
//                    MessageCodeConstants.PIPELINE_FAILED,
//                    devopsCdPipelineRecordDTO.getCreatedBy(), GitUserNameUtil.getEmail(), new HashMap<>());
//        }
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>End envAutoDeploy,devopsCdJobRecordDTO: {}<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(devopsCdJobRecordDTO));
//        }
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    @Override
//    public void pipelineDeploy(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, StringBuilder log) {
//        LOGGER.info("autoDeploy:pipelineRecordId {} stageRecordId: {} jobRecordId: {}", pipelineRecordId, stageRecordId, jobRecordId);
//        // log 用于记录部署日志
//
//        log.append("Start pipeline auto deploy task.").append(System.lineSeparator());
//        // 获取数据
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
//        devopsCdJobService.queryById(devopsCdJobRecordDTO.getJobId());
//        DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(devopsCdJobRecordDTO.getDeployInfoId());
//        Date startDate = new Date();
//        Long commandId = null;
//        Long appId = null;
//
//        // 设置用户上下文
//        log.append("Pipeline trigger user id is :").append(devopsCdJobRecordDTO.getCreatedBy()).append(System.lineSeparator());
//        CustomContextUtil.setUserContext(devopsCdJobRecordDTO.getCreatedBy());
//
//        // 1. 校验环境是否开启一键关闭自动部署
//        if (Boolean.FALSE.equals(checkEnvEnableAutoDeploy(devopsCdJobRecordDTO, devopsCdEnvDeployInfoDTO, log))) {
//            return;
//        }
//        // 2. 校验用户权限
//        if (Boolean.FALSE.equals(checkUserPermission(devopsCdEnvDeployInfoDTO, devopsCdJobRecordDTO, log))) {
//            return;
//        }
//
//        // 获取部署版本信息
//        log.append("## 3.Query Deploy version.").append(System.lineSeparator());
//        AppServiceVersionDTO appServiceVersionDTO = getDeployVersion(pipelineRecordId);
//        if (appServiceVersionDTO == null) {
//            log.append("Not Found App Version in this branch and commit, skipped.").append(System.lineSeparator());
//            devopsCdJobRecordDTO.setStatus(PipelineStatus.SKIPPED.toValue());
//            devopsCdJobRecordDTO.setLog(log.toString());
//            devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//            return;
//        } else {
//            log.append("Deploy Version is ").append(appServiceVersionDTO.getVersion()).append(System.lineSeparator());
//        }
//        log.append("## 4.Deploy app instance.").append(System.lineSeparator());
//
//        AppServiceDeployVO appServiceDeployVO = null;
//        if (DeployTypeEnum.CREATE.value().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
//            // 不存在应用则新建
//            log.append("App not exist, create it now.").append(System.lineSeparator());
//            appServiceDeployVO = new AppServiceDeployVO(appServiceVersionDTO.getAppServiceId(),
//                    appServiceVersionDTO.getId(),
//                    devopsCdEnvDeployInfoDTO.getEnvId(),
//                    devopsDeployValueService.baseQueryById(devopsCdEnvDeployInfoDTO.getValueId()).getValue(),
//                    devopsCdEnvDeployInfoDTO.getValueId(),
//                    devopsCdEnvDeployInfoDTO.getAppCode(),
//                    null,
//                    CommandType.CREATE.getType(),
//                    devopsCdEnvDeployInfoDTO.getAppName(),
//                    devopsCdEnvDeployInfoDTO.getAppCode());
//            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(devopsCdJobRecordDTO.getProjectId(), appServiceDeployVO, true);
//            commandId = appServiceInstanceVO.getCommandId();
//            appId = appServiceInstanceVO.getAppId();
//
//            devopsCdEnvDeployInfoDTO.setDeployType(DeployTypeEnum.UPDATE.value());
//            devopsCdEnvDeployInfoDTO.setAppId(appId);
//            devopsCdEnvDeployInfoService.update(devopsCdEnvDeployInfoDTO);
//        } else {
//            // 3. 如果是更新应用，先判断应用是否存在。不存在则跳过。
//            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getAppId());
//            if (devopsDeployAppCenterEnvDTO == null) {
//                log.append("App: ").append(devopsCdEnvDeployInfoDTO.getAppId()).append(" not found, is deleted? Skip this task.").append(System.lineSeparator());
//                devopsCdJobRecordDTO.setStatus(PipelineStatus.SKIPPED.toValue());
//                devopsCdJobRecordDTO.setLog(log.toString());
//                devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//                return;
//            }
//            // 存在则更新
//            AppServiceInstanceDTO preInstance = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
//            DevopsEnvCommandDTO preCommand = devopsEnvCommandService.baseQuery(preInstance.getCommandId());
//            AppServiceVersionRespVO deploydAppServiceVersion = appServiceVersionService.queryById(preCommand.getObjectVersionId());
//            log.append("App exist, upgrade it now.").append(System.lineSeparator());
//            // 如果当前部署版本和流水线生成版本相同则重启
//            if (preCommand.getObjectVersionId().equals(appServiceVersionDTO.getId())) {
//                log.append("Deploy version is same to instance version, restart it.").append(System.lineSeparator());
//
//                DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
//                cdJobRecordDTO.setStartedDate(new Date());
//                DevopsEnvCommandDTO devopsEnvCommandDTO = appServiceInstanceService.restartInstance(devopsCdJobRecordDTO.getProjectId(), preInstance.getId(), true, true);
//                log.append("Restart success.").append(System.lineSeparator());
//                // 更新job状态为success
//                cdJobRecordDTO.setCommandId(devopsEnvCommandDTO.getId());
//                cdJobRecordDTO.setFinishedDate(new Date());
//                cdJobRecordDTO.setStatus(PipelineStatus.SUCCESS.toValue());
//                cdJobRecordDTO.setLog(log.toString());
//                devopsCdJobRecordService.update(cdJobRecordDTO);
//                return;
//            }
//            AppServiceDTO appServiceDTO = appServiceService.baseQuery(preInstance.getAppServiceId());
//            // 要部署版本的commit
//            CommitDTO currentCommit = gitlabServiceClientOperator.queryCommit(appServiceDTO.getGitlabProjectId(), appServiceVersionDTO.getCommit(), GITLAB_ADMIN_ID);
//            // 已经部署版本的commit
//            CommitDTO deploydCommit = gitlabServiceClientOperator.queryCommit(appServiceDTO.getGitlabProjectId(), deploydAppServiceVersion.getCommit(), GITLAB_ADMIN_ID);
//            if (deploydCommit != null
//                    && currentCommit != null
//                    && currentCommit.getCommittedDate().before(deploydCommit.getCommittedDate())) {
//                // 计算commitDate
//                // 如果要部署的版本的commitDate落后于环境中已经部署的版本，则跳过
//                // 如果现在部署的版本落后于已经部署的版本则跳过
//                log.append("Deploy version is behind to instance current version, skipped.").append(System.lineSeparator());
//                devopsCdJobRecordService.updateStatusById(jobRecordId, PipelineStatus.SKIPPED.toValue());
//                devopsCdJobRecordService.updateLogById(jobRecordId, log);
//                return;
//            }
//            appServiceDeployVO = new AppServiceDeployVO(appServiceVersionDTO.getAppServiceId(),
//                    appServiceVersionDTO.getId(),
//                    devopsCdEnvDeployInfoDTO.getEnvId(),
//                    devopsDeployValueService.baseQueryById(devopsCdEnvDeployInfoDTO.getValueId()).getValue(),
//                    devopsCdEnvDeployInfoDTO.getValueId(),
//                    devopsCdEnvDeployInfoDTO.getAppCode(),
//                    devopsDeployAppCenterEnvDTO.getObjectId(),
//                    CommandType.UPDATE.getType(),
//                    null,
//                    null);
//            appServiceDeployVO.setInstanceId(devopsDeployAppCenterEnvDTO.getObjectId());
//            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(devopsCdJobRecordDTO.getProjectId(), appServiceDeployVO, true);
//            commandId = appServiceInstanceVO.getCommandId();
//        }
//
//        log.append("Deploy app success.").append(System.lineSeparator());
//        devopsCdJobRecordDTO.setStatus(PipelineStatus.SUCCESS.toValue());
//        devopsCdJobRecordDTO.setStartedDate(startDate);
//        devopsCdJobRecordDTO.setFinishedDate(new Date());
//        devopsCdJobRecordDTO.setCommandId(commandId);
//        devopsCdJobRecordDTO.setLog(log.toString());
//        devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//    }
//
//    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void pipelineDeployDeployment(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, StringBuilder log) {
//        LOGGER.info("autoDeploy:pipelineRecordId {} stageRecordId: {} jobRecordId: {}", pipelineRecordId, stageRecordId, jobRecordId);
//        // log 用于记录部署日志
//
//        log.append("Start pipeline auto deploy task.").append(System.lineSeparator());
//        // 获取数据
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
//        Date startDate = new Date();
//        Long commandId = null;
//        Long appId = null;
//
//        // 设置用户上下文
//        log.append("Pipeline trigger user id is :").append(devopsCdJobRecordDTO.getCreatedBy()).append(System.lineSeparator());
//        CustomContextUtil.setUserContext(devopsCdJobRecordDTO.getCreatedBy());
//        DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(devopsCdJobRecordDTO.getDeployInfoId());
//
//        // 1. 校验环境是否开启一键关闭自动部署
//        if (Boolean.FALSE.equals(checkEnvEnableAutoDeploy(devopsCdJobRecordDTO, devopsCdEnvDeployInfoDTO, log))) {
//            return;
//        }
//        // 2. 校验用户权限
//        if (Boolean.FALSE.equals(checkUserPermission(devopsCdEnvDeployInfoDTO, devopsCdJobRecordDTO, log))) {
//            return;
//        }
//
//        Long objectId = null;
//        if (DeployTypeEnum.UPDATE.value().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
//            // 3. 如果是更新应用，先判断应用是否存在。不存在则跳过。
//            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getAppId());
//            if (devopsDeployAppCenterEnvDTO == null) {
//                log.append("App: ").append(devopsCdEnvDeployInfoDTO.getAppId()).append(" not found, is deleted? Skip this task.").append(System.lineSeparator());
//                devopsCdJobRecordDTO.setStatus(PipelineStatus.SKIPPED.toValue());
//                devopsCdJobRecordDTO.setLog(log.toString());
//                devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//                return;
//            }
//            objectId = devopsDeployAppCenterEnvDTO.getObjectId();
//        }
//
//        DevopsDeployGroupAppConfigVO devopsDeployGroupAppConfigVO = JsonHelper.unmarshalByJackson(devopsCdEnvDeployInfoDTO.getAppConfigJson(), DevopsDeployGroupAppConfigVO.class);
//        List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOS = JsonHelper.unmarshalByJackson(devopsCdEnvDeployInfoDTO.getContainerConfigJson(), new TypeReference<List<DevopsDeployGroupContainerConfigVO>>() {
//        });
//        DevopsDeployGroupVO devopsDeployGroupVO = new DevopsDeployGroupVO(devopsCdEnvDeployInfoDTO.getAppName(),
//                devopsCdEnvDeployInfoDTO.getAppCode(),
//                devopsCdJobRecordDTO.getProjectId(),
//                devopsCdEnvDeployInfoDTO.getEnvId(),
//                devopsDeployGroupAppConfigVO,
//                devopsDeployGroupContainerConfigVOS,
//                objectId);
//
//
//        devopsDeployGroupContainerConfigVOS.forEach(config -> {
//            if (config.getPipelineJobName() != null) {
//                CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(devopsCdPipelineRecordDTO.getPipelineId());
//                if (RdupmTypeEnum.DOCKER.value().equals(config.getType())) {
//                    fillDockerConfig(devopsCdPipelineRecordDTO, config, ciCdPipelineVO);
//
//                } else {
//                    CiPipelineMavenDTO ciPipelineMavenDTO = ciPipelineMavenService.queryByGitlabPipelineId(ciCdPipelineVO.getAppServiceId(), devopsCdPipelineRecordDTO.getGitlabPipelineId(), config.getPipelineJobName());
//                    ProdJarInfoVO prodJarInfoVO = new ProdJarInfoVO(ciPipelineMavenDTO.getNexusRepoId(),
//                            ciPipelineMavenDTO.getGroupId(),
//                            ciPipelineMavenDTO.getArtifactId(),
//                            getMavenVersion(ciPipelineMavenDTO.getVersion()));
//
//                    if (ciPipelineMavenDTO.getNexusRepoId() == null) {
//                        prodJarInfoVO.setDownloadUrl(ciPipelineMavenDTO.calculateDownloadUrl());
//                        prodJarInfoVO.setUsername(DESEncryptUtil.decode(ciPipelineMavenDTO.getUsername()));
//                        prodJarInfoVO.setPassword(DESEncryptUtil.decode(ciPipelineMavenDTO.getPassword()));
//                    }
//                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsCdJobRecordDTO.getProjectId());
//                    C7nNexusRepoDTO c7nNexusRepoDTO = rdupmClientOperator.getMavenRepo(projectDTO.getOrganizationId(), devopsCdJobRecordDTO.getProjectId(), ciPipelineMavenDTO.getNexusRepoId());
//
//                    prodJarInfoVO.setNexusId(c7nNexusRepoDTO.getConfigId());
//
//
//                    DevopsDeployGroupJarDeployVO devopsDeployGroupJarDeployVO = new DevopsDeployGroupJarDeployVO();
//                    devopsDeployGroupJarDeployVO.setProdJarInfoVO(prodJarInfoVO);
//                    devopsDeployGroupJarDeployVO.setSourceType(AppSourceType.CURRENT_PROJECT.getValue());
//                    config.setPipelineJobName(null);
//                    config.setSourceType(AppSourceType.CURRENT_PROJECT.getValue());
//                    config.setJarDeployVO(devopsDeployGroupJarDeployVO);
//                }
//            }
//        });
//
//
//        DevopsDeployAppCenterEnvVO devopsDeployAppCenterEnvVO = devopsDeployGroupService.createOrUpdate(devopsCdJobRecordDTO.getProjectId(), devopsDeployGroupVO, devopsCdEnvDeployInfoDTO.getDeployType(), false, true);
//        commandId = devopsDeployAppCenterEnvVO.getCommandId();
//        if (DeployTypeEnum.CREATE.value().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
//            appId = devopsDeployAppCenterEnvVO.getId();
//            devopsCdEnvDeployInfoDTO.setAppId(appId);
//            devopsCdEnvDeployInfoDTO.setDeployType(DeployTypeEnum.UPDATE.value());
//            devopsCdEnvDeployInfoService.update(devopsCdEnvDeployInfoDTO);
//        }
//        if (commandId == null) {
//            devopsCdJobRecordDTO.setStatus(PipelineStatus.SKIPPED.toValue());
//            devopsCdJobRecordDTO.setLog(log.toString());
//            devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//            return;
//        }
//
//        log.append("Deploy app success.").append(System.lineSeparator());
//        devopsCdJobRecordDTO.setStatus(PipelineStatus.SUCCESS.toValue());
//        devopsCdJobRecordDTO.setStartedDate(startDate);
//        devopsCdJobRecordDTO.setFinishedDate(new Date());
//        devopsCdJobRecordDTO.setCommandId(commandId);
//        devopsCdJobRecordDTO.setLog(log.toString());
//        devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//    }
//
//    protected void fillDockerConfig(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO, DevopsDeployGroupContainerConfigVO config, CiCdPipelineVO ciCdPipelineVO) {
//        CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(ciCdPipelineVO.getAppServiceId(), devopsCdPipelineRecordDTO.getGitlabPipelineId(), config.getPipelineJobName());
//        HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigById(devopsCdPipelineRecordDTO.getProjectId(), ciPipelineImageDTO.getHarborRepoId(), ciPipelineImageDTO.getRepoType());
//
//        DevopsDeployGroupDockerDeployVO dockerDeployVO = new DevopsDeployGroupDockerDeployVO();
//        dockerDeployVO.setSourceType(AppSourceType.CURRENT_PROJECT.getValue());
//
//        int index = ciPipelineImageDTO.getImageTag().lastIndexOf(":");
//        String imageName = ciPipelineImageDTO.getImageTag().substring(0, index);
//        String tagName = ciPipelineImageDTO.getImageTag().substring(index + 1);
//
//        ProdImageInfoVO prodImageInfoVO = new ProdImageInfoVO(harborRepoDTO.getHarborRepoConfig().getRepoName(),
//                harborRepoDTO.getRepoType(),
//                harborRepoDTO.getHarborRepoConfig().getRepoId(),
//                imageName,
//                tagName,
//                Boolean.TRUE.toString().equals(harborRepoDTO.getHarborRepoConfig().getIsPrivate()),
//                ciPipelineImageDTO.getImageTag());
//        dockerDeployVO.setImageInfo(prodImageInfoVO);
//        config.setPipelineJobName(null);
//        config.setSourceType(AppSourceType.CURRENT_PROJECT.getValue());
//        config.setDockerDeployVO(dockerDeployVO);
//    }
//
//    private boolean checkEnvEnableAutoDeploy(DevopsCdJobRecordDTO devopsCdJobRecordDTO, DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO, StringBuilder log) {
//        log.append("## 1.Check Environment automatic deploy enable.").append(System.lineSeparator());
//        if (Boolean.FALSE.equals(checkAutoDeploy(devopsCdEnvDeployInfoDTO.getEnvId()))) {
//            log.append("Environment automatic deploy has been turned off!").append(System.lineSeparator());
//            devopsCdJobRecordDTO.setStatus(PipelineStatus.SKIPPED.toValue());
//            devopsCdJobRecordDTO.setLog(log.toString());
//            devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//            return false;
//        }
//        return true;
//    }
//
//    private String getMavenVersion(String version) {
//        if (version.contains(BaseConstants.Symbol.SLASH)) {
//            return version.split(BaseConstants.Symbol.SLASH)[0];
//        } else {
//            return version;
//        }
//    }
//
//    /**
//     * // 判断是否需要校验环境权限
//     * // 2.1 需要： 没有权限将任务状态改为skipped，有权限往下执行
//     * // 2.2 不需要：没有权限使用管理员账户部署，有权限则使用自己账户
//     *
//     * @param devopsCdEnvDeployInfoDTO
//     * @param devopsCdJobRecordDTO
//     * @param log
//     */
//    private boolean checkUserPermission(DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO, DevopsCdJobRecordDTO devopsCdJobRecordDTO, StringBuilder log) {
//        log.append("## 2.Check user env permission.").append(System.lineSeparator());
//        if (Boolean.FALSE.equals(devopsCdEnvDeployInfoDTO.getSkipCheckPermission())) {
//            log.append("Skip check user permission Flag is false, check user permission.").append(System.lineSeparator());
//            if (Boolean.FALSE.equals(devopsEnvUserPermissionService.checkUserEnvPermission(devopsCdEnvDeployInfoDTO.getEnvId(), devopsCdJobRecordDTO.getCreatedBy()))) {
//                log.append("User have no env Permission, skipped.").append(System.lineSeparator());
//                devopsCdJobRecordDTO.setStatus(PipelineStatus.SKIPPED.toValue());
//                devopsCdJobRecordDTO.setLog(log.toString());
//                devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//                return false;
//            } else {
//                log.append("Check user permission Passed.").append(System.lineSeparator());
//            }
//        } else {
//            log.append("Skip check user permission Flag is true, choose deploy account.").append(System.lineSeparator());
//            if (Boolean.FALSE.equals(devopsEnvUserPermissionService.checkUserEnvPermission(devopsCdEnvDeployInfoDTO.getEnvId(), devopsCdJobRecordDTO.getCreatedBy()))) {
//                log.append("User have no env Permission, use admin account to deploy.").append(System.lineSeparator());
//                CustomContextUtil.setUserContext(IamAdminIdHolder.getAdminId());
//            } else {
//                log.append("User have env Permission, use self account to deploy.").append(System.lineSeparator());
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, Boolean status) {
//        LOGGER.info("setAppDeployStatus:pipelineRecordId: {} stageRecordId: {} taskId: {}, status: {}", pipelineRecordId, stageRecordId, jobRecordId, status);
//        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>> Userdetails is {}", DetailsHelper.getUserDetails());
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        CustomContextUtil.setUserContext(devopsCdPipelineRecordDTO.getCreatedBy());
//        // 流水线取消执行则不更新相关状态
//        if (PipelineStatus.CANCELED.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {
//            LOGGER.info("Pipeline instance has canceled:pipelineRecordId: {} stageRecordId: {} taskId: {}, status: {}", pipelineRecordId, stageRecordId, jobRecordId, status);
//            return;
//        }
//
//        if (Boolean.TRUE.equals(status)
//                && PipelineStatus.RUNNING.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {
//            LOGGER.info(">>>>>>> setAppDeployStatus, start next task, pipelineStatus is :{}<<<<<<<<<<<", devopsCdPipelineRecordDTO.getStatus());
//            startNextTask(pipelineRecordId, stageRecordId, jobRecordId);
//        } else {
//            LOGGER.info(">>>>>>> setAppDeployStatus, update status to failed, pipelineStatus is :{}<<<<<<<<<<<", devopsCdPipelineRecordDTO.getStatus());
//            devopsCdJobRecordService.updateJobStatusFailed(jobRecordId, null);
//            devopsCdStageRecordService.updateStageStatusFailed(stageRecordId);
//            devopsCdPipelineRecordService.updatePipelineStatusFailed(pipelineRecordId);
//            workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
//        }
//    }
//
//    @Override
//    public String getDeployStatus(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
//        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(jobRecordId);
//
//        if (jobRecordDTO == null || PipelineStatus.FAILED.toValue().equals(jobRecordDTO.getStatus())) {
//            return PipelineStatus.FAILED.toValue();
//        }
//        return jobRecordDTO.getStatus();
//    }
//
//    private DevopsCdJobRecordDTO getNextJob(Long stageRecordId, DevopsCdJobRecordDTO currentJob) {
//        List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(stageRecordId);
//        Optional<DevopsCdJobRecordDTO> optionalDevopsCdJobRecordDTO = devopsCdJobRecordDTOS.stream()
//                .filter(v -> !v.getId().equals(currentJob.getId()))
//                .sorted(Comparator.comparing(DevopsCdJobRecordDTO::getSequence))
//                .filter(v -> v.getSequence() > currentJob.getSequence()).findFirst();
//        return optionalDevopsCdJobRecordDTO.orElse(null);
//    }
//
//    private void startNextTask(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        DevopsCdStageRecordDTO currentStage = devopsCdStageRecordService.queryById(stageRecordId);
//        DevopsCdJobRecordDTO currentJob = devopsCdJobRecordService.queryById(jobRecordId);
//
//        DevopsCdJobRecordDTO nextJob = getNextJob(stageRecordId, currentJob);
//        // 如果下一个任务为人工卡点任务，更新任务状态为待审核，并通知审核人员
//        if (nextJob != null && JobTypeEnum.CD_AUDIT.value().equals(nextJob.getType())) {
//            devopsCdJobRecordService.updateJobStatusNotAudit(pipelineRecordId, stageRecordId, nextJob.getId());
//        }
//        //如果没有下一个任务，则属于阶段的最后一个任务,表明当前阶段执行成功，执行下一个阶段
//        if (nextJob == null) {
//            // 1. 更新当前阶段状态为success
//            currentStage.setStatus(PipelineStatus.SUCCESS.toValue());
//            devopsCdStageRecordService.update(currentStage);
//            // 2. 执行下一个阶段
//            // 2.1 如果下一个阶段为空，则说明已经执行到了最后一个阶段， 则需要更新流水线状态为success
//            // 2.2 如果存在下一个阶段，则判断阶段是自动流转还是手动流转
//            // 2.2.1 自动流转：判断阶段的第一个任务是不是人工卡点任务， 人工卡点任务则更新任务状态为待审核，以及通知审核人员
//            List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(pipelineRecordId);
//            Optional<DevopsCdStageRecordDTO> optionalNextStage = devopsCdStageRecordDTOS.stream()
//                    .filter(v -> !v.getId().equals(currentStage.getId()))
//                    .sorted(Comparator.comparing(DevopsCdStageRecordDTO::getSequence))
//                    .filter(v -> v.getSequence() > currentStage.getSequence())
//                    .findFirst();
//            DevopsCdStageRecordDTO nextStage = optionalNextStage.orElse(null);
//            // 存在下一个阶段
//            if (nextStage != null) {
//                DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryFirstByStageRecordId(nextStage.getId());
//                if (devopsCdJobRecordDTO == null) {
//                    return;
//                }
//                if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordDTO.getType())) {
//                    devopsCdJobRecordService.updateJobStatusNotAudit(pipelineRecordId, nextStage.getId(), devopsCdJobRecordDTO.getId());
//                } else {
//                    devopsCdStageRecordService.updateStatusById(nextStage.getId(), PipelineStatus.RUNNING.toValue());
//                }
//            } else {
//                // 已经是最后一个阶段了
//                devopsCdPipelineRecordService.updateStatusById(devopsCdPipelineRecordDTO.getId(), PipelineStatus.SUCCESS.toValue());
//                sendNotificationService.sendCdPipelineNotice(devopsCdPipelineRecordDTO.getId(), MessageCodeConstants.PIPELINE_SUCCESS, devopsCdPipelineRecordDTO.getCreatedBy(), null, new HashMap<>());
//            }
//        }
//    }
//
//
//    private AppServiceVersionDTO getDeployVersion(Long pipelineRecordId) {
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(devopsCdPipelineRecordDTO.getPipelineId());
//        return appServiceVersionService.queryByCommitShaAndRef(ciCdPipelineVO.getAppServiceId(), devopsCdPipelineRecordDTO.getCommitSha(), devopsCdPipelineRecordDTO.getRef());
//    }
//
//    @Override
//    public void createWorkFlow(Long projectId, io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO devopsPipelineDTO, String loginName, Long userId, Long orgId) {
//        CustomContextUtil.setUserContext(loginName, userId, orgId);
//        try {
//            workFlowServiceOperator.createCiCdPipeline(projectId, devopsPipelineDTO);
//        } catch (Exception e) {
//            throw new CommonException(e);
//        }
//
//    }
//
//    @Override
//    @Transactional
//    public AuditResultVO auditJob(Long projectId, Long pipelineRecordId, Long stageRecordId, Long jobRecordId, String result) {
//        AuditResultVO auditResultVO = new AuditResultVO();
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordService.queryById(stageRecordId);
//        // 1. 查询审核人员
//        CustomUserDetails details = DetailsHelper.getUserDetails();
//        List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(jobRecordId);
//        Map<Long, DevopsCdAuditRecordDTO> auditRecordDTOMap = devopsCdAuditRecordDTOS.stream().collect(Collectors.toMap(DevopsCdAuditRecordDTO::getUserId, v -> v));
//        List<Long> userIds = devopsCdAuditRecordDTOS.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
//        DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = auditRecordDTOMap.get(details.getUserId());
//        // 找不到对应记录，则说明用户没有审核权限
//        if (devopsCdAuditRecordDTO == null) {
//            throw new CommonException(ERROR_PERMISSION_MISMATCH_FOR_AUDIT);
//        }
//        // 状态不是待审核，抛出错误信息
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
//        if (!PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
//            throw new CommonException(ERROR_PIPELINE_STATUS_CHANGED);
//        }
//        // 添加审核结果 - 是否会签
//        auditResultVO.setCountersigned(devopsCdJobRecordDTO.getCountersigned());
//
//        if (AuditStatusEnum.PASSED.value().equals(result)) {
//            // 1. 工作流任务审核通过
//            try {
//                // 审核通过
//                // 判断是否是或签任务
//                if (devopsCdJobRecordDTO.getCountersigned() != null && devopsCdJobRecordDTO.getCountersigned() == 0) {
//
//                    // 更新审核状态为通过
//                    devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.PASSED.value());
//                    devopsCdAuditRecordService.update(devopsCdAuditRecordDTO);
//
//                    // 更新job状态为success
//                    devopsCdJobRecordService.updateStatusById(devopsCdJobRecordDTO.getId(), PipelineStatus.SUCCESS.toValue());
//                    // 更新阶段状态为RUNNING
//                    devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.RUNNING.toValue());
//                    // 更新流水线状态为RUNNING
//                    devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.RUNNING.toValue());
//
//                    // 发送通知
////                    sendNotificationService.sendPipelineAuditResultMassage(MessageCodeConstants.PIPELINE_PASS, devopsCiPipelineRecordDTO.getCiPipelineId(), userIds, pipelineRecordId, devopsCdStageRecordDTO.getStageName(), devopsCdStageRecordDTO.getStageId(), details.getUserId());
//                    // 执行下一个任务
//                    startNextTask(pipelineRecordId, stageRecordId, jobRecordId);
//                    workFlowServiceOperator.approveUserTask(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey(), details.getUsername(), details.getUserId(), details.getOrganizationId());
//
//                } else if (devopsCdJobRecordDTO.getCountersigned() != null && devopsCdJobRecordDTO.getCountersigned() == 1) {
//                    // 更新审核状态为通过
//                    devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.PASSED.value());
//                    devopsCdAuditRecordService.update(devopsCdAuditRecordDTO);
//                    // 如果说是最后一个人审核，则不添加审核人员信息
//                    if (devopsCdAuditRecordDTOS.stream()
//                            .filter(v -> !v.getId().equals(devopsCdAuditRecordDTO.getId()))
//                            .allMatch(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus()))) {
//                        // 更新job状态为success
//                        devopsCdJobRecordService.updateStatusById(devopsCdJobRecordDTO.getId(), PipelineStatus.SUCCESS.toValue());
//                        // 更新阶段状态为RUNNING
//                        devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.RUNNING.toValue());
//                        // 更新流水线状态为RUNNING
//                        devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.RUNNING.toValue());
//                        // 执行下一个任务
//                        startNextTask(pipelineRecordId, stageRecordId, jobRecordId);
//                    } else {
//                        // 添加审核结果信息 - 审核人员信息
//                        addAuditUserInfo(userIds, devopsCdAuditRecordDTOS, auditResultVO);
//                    }
//                    workFlowServiceOperator.approveUserTask(devopsCdPipelineRecordDTO.getProjectId(),
//                            devopsCdPipelineRecordDTO.getBusinessKey(),
//                            details.getUsername(),
//                            details.getUserId(),
//                            details.getOrganizationId());
//                }
//            } catch (Exception e) {
//                LOGGER.error("Approve job failed: {}", e.getMessage());
//                // 更新job状态为失败
//                devopsCdJobRecordService.updateStatusById(jobRecordId, PipelineStatus.FAILED.toValue());
//                // 更新阶段状态为失败
//                devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.FAILED.toValue());
//                // 更新流水线状态为失败
//                devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.FAILED.toValue());
//                // 停止流水线
//                workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
//                // 发送失败通知
//                sendNotificationService.sendCdPipelineNotice(pipelineRecordId,
//                        MessageCodeConstants.PIPELINE_FAILED, details.getUserId(), null, new HashMap<>());
//            }
//        } else if (AuditStatusEnum.REFUSED.value().equals(result)) {
//            // 审核不通过
//            // 更新审核状态为不通过
//            devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.REFUSED.value());
//            devopsCdAuditRecordService.update(devopsCdAuditRecordDTO);
//            // 1. 停止流水线
//            workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
//            // 更新当前阶段状态为stop
//            devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.STOP.toValue());
//            // 更新当前阶段中的后续任务状态为stop
//            List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(stageRecordId);
//            List<DevopsCdJobRecordDTO> stopJobRecordList = devopsCdJobRecordDTOS.stream()
//                    .filter(v -> v.getSequence() >= devopsCdJobRecordDTO.getSequence())
//                    .collect(Collectors.toList());
//            stopJobRecordList.forEach(v -> devopsCdJobRecordService.updateStatusById(v.getId(), PipelineStatus.STOP.toValue()));
//            // 2. 更新后续阶段以及任务状态为终止
//            // 2.1 更新当前阶段状态为stop
//            // 2.2 更新后续阶段状态为stop
//            devopsCdStageRecordService.updateStatusById(devopsCdStageRecordDTO.getId(), PipelineStatus.STOP.toValue());
//            List<DevopsCdStageRecordDTO> afterStageRecordList = devopsCdStageRecordService.queryByPipelineRecordId(pipelineRecordId).stream()
//                    .filter(v -> v.getSequence() > devopsCdStageRecordDTO.getSequence())
//                    .collect(Collectors.toList());
//            afterStageRecordList.forEach(v -> devopsCdStageRecordService.updateStageStatusStop(v.getId()));
//            // 3. 更新流水线状态为终止
//            devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.STOP.toValue());
//            // 4. 发送审核记录通知
////            sendNotificationService.sendPipelineAuditResultMassage(MessageCodeConstants.PIPELINE_STOP, devopsCiPipelineRecordDTO.getCiPipelineId(), userIds, devopsCdPipelineRecordDTO.getId(), devopsCdStageRecordDTO.getStageName(), devopsCdStageRecordDTO.getStageId(), details.getUserId());
//        }
//        return auditResultVO;
//    }
//
//    private void addAuditUserInfo(List<Long> userIds, List<DevopsCdAuditRecordDTO> cdAuditRecordDTOS, AuditResultVO auditResultVO) {
//        Map<Long, IamUserDTO> userDTOMap = baseServiceClientOperator.queryUsersByUserIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
//        cdAuditRecordDTOS.forEach(v -> {
//            if (AuditStatusEnum.PASSED.value().equals(v.getStatus())) {
//                IamUserDTO iamUserDTO = userDTOMap.get(v.getUserId());
//                if (iamUserDTO != null) {
//                    auditResultVO.getAuditedUserNameList().add(iamUserDTO.getRealName());
//                }
//            } else if (AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus())) {
//                IamUserDTO iamUserDTO = userDTOMap.get(v.getUserId());
//                if (iamUserDTO != null) {
//                    auditResultVO.getNotAuditUserNameList().add(iamUserDTO.getRealName());
//                }
//            }
//        });
//
//    }
//
//    @Override
//    public AduitStatusChangeVO checkAuditStatus(Long projectId, Long pipelineRecordId, AuditCheckVO auditCheckVO) {
//        AduitStatusChangeVO aduitStatusChangeVO = new AduitStatusChangeVO();
//        aduitStatusChangeVO.setAuditStatusChanged(false);
//        if ("task".equals(auditCheckVO.getSourceType())) {
//            DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(auditCheckVO.getSourceId());
//            List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(auditCheckVO.getSourceId());
//            if (PipelineStatus.STOP.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
//                List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOList = devopsCdAuditRecordDTOS.stream().filter(v -> AuditStatusEnum.REFUSED.value().equals(v.getStatus())).collect(Collectors.toList());
//                calculatAuditUserName(devopsCdAuditRecordDTOList, aduitStatusChangeVO);
//                aduitStatusChangeVO.setCurrentStatus(PipelineStatus.STOP.toValue());
//            } else if (PipelineStatus.SUCCESS.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
//                List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOList = devopsCdAuditRecordDTOS.stream().filter(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus())).collect(Collectors.toList());
//                calculatAuditUserName(devopsCdAuditRecordDTOList, aduitStatusChangeVO);
//                aduitStatusChangeVO.setCurrentStatus(PipelineStatus.SUCCESS.toValue());
//            }
//            aduitStatusChangeVO.setCountersigned(devopsCdJobRecordDTO.getCountersigned());
//            return aduitStatusChangeVO;
//        } else {
//            throw new CommonException(ResourceCheckConstant.DEVOPS_PARAM_IS_INVALID);
//        }
//    }
//
////    @Override
////    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON,code = DEVOPS_CI_PIPELINE_SUCCESS_FOR_SIMPLE_CD, description = "ci流水线成功，执行纯cd流水线", inputSchemaClass = PipelineWebHookVO.class)
////    public void handlerCiPipelineStatusSuccess(PipelineWebHookVO pipelineWebHookVO, String token) {
////        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);
////        pipelineWebHookVO.setToken(token);
////        try {
////            String input = objectMapper.writeValueAsString(pipelineWebHookVO);
////            transactionalProducer.apply(
////                    StartSagaBuilder.newBuilder()
////                            .withRefType("app")
////                            .withRefId(appServiceDTO.getId().toString())
////                            .withSagaCode(DEVOPS_CI_PIPELINE_SUCCESS_FOR_SIMPLE_CD)
////                            .withLevel(ResourceLevel.PROJECT)
////                            .withSourceId(appServiceDTO.getProjectId())
////                            .withJson(input),
////                    builder -> {
////                    });
////        } catch (JsonProcessingException e) {
////            throw new CommonException(e.getMessage(), e);
////        }
////    }
//
////    @Override
////    public void trigerSimpleCDPipeline(PipelineWebHookVO pipelineWebHookVO) {
////        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(pipelineWebHookVO.getToken());
////        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
////
////        // 设置用户上下文
////        Long iamUserId = GitUserNameUtil.getIamUserIdByGitlabUserName(pipelineWebHookVO.getUser().getUsername());
////        CustomContextUtil.setDefaultIfNull(iamUserId);
////
////        if (devopsCiPipelineDTO == null || Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
////            LOGGER.debug("Skip null of disabled pipeline for pipeline webhook with id {} and token: {}", pipelineWebHookVO.getObjectAttributes().getId(), pipelineWebHookVO.getToken());
////            return;
////        }
////
////        // 只有纯cd流水线才触发
////        // 包含ci阶段的不触发
////        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineDTO.getId());
////        if (!CollectionUtils.isEmpty(devopsCiStageDTOList)) {
////            return;
////        }
////        // 不包含cd阶段的不触发
////        List<DevopsCdStageDTO> devopsCdStageDTOList = devopsCdStageService.queryByPipelineId(devopsCiPipelineDTO.getId());
////        if (CollectionUtils.isEmpty(devopsCdStageDTOList)) {
////            return;
////        }
////        triggerCdPipeline(appServiceDTO.getProjectId(), pipelineWebHookVO.getToken(),
////                pipelineWebHookVO.getObjectAttributes().getSha(),
////                pipelineWebHookVO.getObjectAttributes().getRef(),
////                pipelineWebHookVO.getObjectAttributes().getTag(),
////                pipelineWebHookVO.getObjectAttributes().getId());
////
////    }
//
////    @Override
////    public void initPipelineRecordWithStageAndJob(Long projectId, Long gitlabPipelineId, String commitSha, String ref, Boolean tag, CiCdPipelineDTO devopsCiPipelineDTO) {
////
////        // 查询流水线是否有cd阶段, 没有cd阶段不做处理
////        List<DevopsCdStageDTO> devopsCdStageDTOList = devopsCdStageService.queryByPipelineId(devopsCiPipelineDTO.getId());
////        if (CollectionUtils.isEmpty(devopsCdStageDTOList)) {
////            return;
////        }
////        // 校验CD流水线记录是否已经创建，未创建才创建记录，并将记录的初始状态设置为pending
////        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryByGitlabPipelineId(devopsCiPipelineDTO.getId(), gitlabPipelineId);
////        Map<Long, DevopsCdStageDTO> devopsCdStageDTOMap = devopsCdStageDTOList.stream().collect(Collectors.toMap(DevopsCdStageDTO::getId, v -> v));
////        if (devopsCdPipelineRecordDTO == null) {
////            LOGGER.info(">>>>>>>>>>>>>>>>>>>> init cd pipeline >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
////            // 1. 根据流水线id,查询job列表
////            List<DevopsCdJobDTO> devopsCdJobDTOList = devopsCdJobService.listByPipelineId(devopsCiPipelineDTO.getId());
////
////            // 2. 计算要执行的job
////            List<DevopsCdJobDTO> executeJobList = calculateExecuteJobList(ref, tag, devopsCdJobDTOList);
////            if (CollectionUtils.isEmpty(executeJobList)) {
////                return;
////            }
////            Map<Long, List<DevopsCdJobDTO>> executeJobMap = executeJobList.stream().collect(Collectors.groupingBy(DevopsCdJobDTO::getStageId));
////
////            // 3. 统计出要执行的阶段（要执行的job的所属阶段）
////            Set<Long> stageIds = executeJobList.stream().map(DevopsCdJobDTO::getStageId).collect(Collectors.toSet());
////            List<DevopsCdStageDTO> executeStageList = stageIds.stream().map(devopsCdStageDTOMap::get).collect(Collectors.toList());
////
////            // 4. 如果有要执行的阶段、job，则初始化执行记录（初始化记录状态为pending）
////            if (CollectionUtils.isEmpty(executeStageList)) {
////                LOGGER.info(">>>>>>>>>>>>>>>>>>>> pipeline has no execte stage, skipped >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
////            } else {
////                // 保存流水线记录
////                devopsCdPipelineRecordDTO = initPipelineRecord(devopsCiPipelineDTO, gitlabPipelineId, commitSha, ref);
////
////                // 保存流水线记录关系
////                DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryByGitlabPipelineId(devopsCiPipelineDTO.getId(), gitlabPipelineId);
////                DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelService.queryByPipelineIdAndCiPipelineRecordId(devopsCiPipelineDTO.getId(), devopsCiPipelineRecordDTO.getId());
////                devopsPipelineRecordRelDTO.setCdPipelineRecordId(devopsCdPipelineRecordDTO.getId());
////                devopsPipelineRecordRelService.update(devopsPipelineRecordRelDTO);
////                // 创建cd阶段记录
////                DevopsCdPipelineRecordDTO finalDevopsCdPipelineRecordDTO = devopsCdPipelineRecordDTO;
////                executeStageList.forEach(stage -> {
////                    DevopsCdStageRecordDTO devopsCdStageRecordDTO = initStageRecord(finalDevopsCdPipelineRecordDTO.getId(), stage);
////                    // 保存job执行记录
////                    List<DevopsCdJobDTO> devopsCdJobDTOS = executeJobMap.get(stage.getId());
////                    devopsCdJobDTOS.forEach(job -> {
////                        DevopsCdJobRecordDTO devopsCdJobRecordDTO = initJobRecord(devopsCdStageRecordDTO.getId(), job);
////                        // 人工卡点任务，添加审核人员记录
////                        if (JobTypeEnum.CD_AUDIT.value().equals(job.getType())) {
////                            addJobAuditRecord(projectId, job.getId(), devopsCdJobRecordDTO.getId());
////                        }
////                    });
////                });
////                LOGGER.info(">>>>>>>>>>>>>>>>>>>> init cd pipeline {} : {} success>>>>>>>>>>>>>>>>>>>>>>>>>>>>", devopsCdPipelineRecordDTO.getId(), devopsCdPipelineRecordDTO.getPipelineName());
////            }
////
////        }
////    }
//
//    @Override
//    @Transactional
//    public void executeApiTestTask(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
//        if (PipelineStatus.CANCELED.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
//            return;
//        }
//        LOGGER.info(">>>>>>>>>>>>>>>>>>>  Execute api test task. pipelineRecordId : {}, stageRecordId : {} ,jobRecordId : {} <<<<<<<<<<<<<<<<<<<<", pipelineRecordId, stageRecordId, jobRecordId);
//        if (!JobTypeEnum.CD_API_TEST.value().equals(devopsCdJobRecordDTO.getType())) {
//            throw new CommonException("devops.invalid.job.type");
//        }
//        DevopsCdApiTestInfoDTO devopsCdApiTestInfoDTO = devopsCdApiTestInfoService.queryById(devopsCdJobRecordDTO.getDeployInfoId());
//
//
//        ApiTestTaskRecordDTO taskRecordDTO;
//
//        // 更新记录状态为执行中
//        devopsCdJobRecordService.updateStatusById(devopsCdJobRecordDTO.getId(), PipelineStatus.RUNNING.toValue());
//        try {
//            if (ApiTestTaskType.TASK.getValue().equals(devopsCdApiTestInfoDTO.getTaskType())) {
//                taskRecordDTO = testServiceClientoperator
//                        .executeTask(devopsCdJobRecordDTO.getProjectId(),
//                                devopsCdApiTestInfoDTO.getApiTestTaskId(),
//                                devopsCdJobRecordDTO.getCreatedBy(),
//                                ApiTestTriggerType.PIPELINE.getValue(),
//                                jobRecordId,
//                                devopsCdApiTestInfoDTO.getApiTestConfigId());
//            } else if (ApiTestTaskType.SUITE.getValue().equals(devopsCdApiTestInfoDTO.getTaskType())) {
//                taskRecordDTO = testServiceClientoperator
//                        .executeSuite(devopsCdJobRecordDTO.getProjectId(),
//                                devopsCdApiTestInfoDTO.getApiTestSuiteId(),
//                                devopsCdJobRecordDTO.getCreatedBy(),
//                                ApiTestTriggerType.PIPELINE.getValue(),
//                                jobRecordId);
//            } else {
//                throw new CommonException("devops.task.type.invalid");
//            }
//
//            DevopsCdJobRecordDTO devopsCdJobRecordDTO1 = devopsCdJobRecordService.queryById(jobRecordId);
//            devopsCdJobRecordDTO1.setApiTestTaskRecordId(taskRecordDTO.getId());
//            devopsCdJobRecordService.update(devopsCdJobRecordDTO1);
//            LOGGER.info(">>>>>>>>>>>>>>>>>>> Execute api test task success. projectId : {}, taskId : {} <<<<<<<<<<<<<<<<<<<<", devopsCdJobRecordDTO.getProjectId(), devopsCdApiTestInfoDTO.getApiTestTaskId());
//        } catch (Exception e) {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>> Execute api test task failed. projectId : {}, taskId : {} e: {}<<<<<<<<<<<<<<<<<<<<", devopsCdJobRecordDTO.getProjectId(), devopsCdApiTestInfoDTO.getApiTestTaskId(), e);
//            // 更新记录状态为失败
//            devopsCdJobRecordService.updateStatusById(devopsCdJobRecordDTO.getId(), PipelineStatus.FAILED.toValue());
//            devopsCdStageRecordService.updateStageStatusFailed(stageRecordId);
//            devopsCdPipelineRecordService.updatePipelineStatusFailed(pipelineRecordId);
//        }
//
//
//    }
//
//    @Override
//    public String getDeployStatus(Long pipelineRecordId, String deployJobName) {
//        // 查询部署任务
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryByPipelineRecordIdAndJobName(pipelineRecordId, deployJobName);
//        // 查询部署配置
//        DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(devopsCdJobRecordDTO.getDeployInfoId());
//
//        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(devopsCdEnvDeployInfoDTO.getEnvId(), devopsCdEnvDeployInfoDTO.getAppCode());
//
//        // 查询实例
//        AppServiceInstanceDTO instanceE = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
//        // 查询当前实例运行时pod metadata
//        List<PodResourceDetailsDTO> podResourceDetailsDTOS = devopsEnvPodService.queryResourceDetailsByInstanceId(instanceE.getId());
//
//        if (CollectionUtils.isEmpty(podResourceDetailsDTOS)) {
//            return JobStatusEnum.RUNNING.value();
//        }
//        if (!podResourceDetailsDTOS.stream().allMatch(v -> Boolean.TRUE.equals(v.getReady()))) {
//            return JobStatusEnum.RUNNING.value();
//        }
//        return JobStatusEnum.SUCCESS.value();
//    }
//
////    @Override
////    @Transactional
////    public void executeExternalApprovalTask(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
////        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
////        if (PipelineStatus.CANCELED.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
////            return;
////        }
////        String callbackToken = UUIDUtils.generateUUID();
////        // 添加回调token
////        devopsCdJobRecordDTO.setCallbackToken(callbackToken);
////
////        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
////        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelService.queryByCdPipelineRecordId(pipelineRecordId);
////        CiCdPipelineRecordVO ciCdPipelineRecordVO = ciCdPipelineRecordService.queryPipelineRecordDetails(devopsCdJobRecordDTO.getProjectId(), devopsPipelineRecordRelDTO.getId());
////
////        // 装配发送内容
////        ExternalApprovalInfoVO externalApprovalInfoVO = new ExternalApprovalInfoVO();
////        externalApprovalInfoVO.setProjectId(devopsCdJobRecordDTO.getId());
////        externalApprovalInfoVO.setPipelineRecordId(pipelineRecordId);
////        externalApprovalInfoVO.setStageRecordId(stageRecordId);
////        externalApprovalInfoVO.setJobRecordId(jobRecordId);
////        externalApprovalInfoVO.setCurrentCdJob(devopsCdJobRecordDTO);
////        externalApprovalInfoVO.setPipelineRecordDetails(ciCdPipelineRecordVO);
////        externalApprovalInfoVO.setCallbackToken(callbackToken);
////
////
////        ExternalApprovalJobVO externalApprovalJobVO = JsonHelper.unmarshalByJackson(devopsCdJobRecordDTO.getMetadata(), ExternalApprovalJobVO.class);
////
////        HttpHeaders headers = new HttpHeaders();
////        MediaType type = MediaType.parseMediaType(MediaType.APPLICATION_JSON_VALUE);
////        headers.setContentType(type);
////        headers.add(AUTH_HEADER, externalApprovalJobVO.getSecretToken());
////        HttpEntity<Object> entity = new HttpEntity<>(externalApprovalInfoVO, headers);
////
////        StringBuilder log = new StringBuilder();
////
////        log.append("\u001B[0K\u001B[32;1mGeneral: \u001B[0;m").append(System.lineSeparator());
////        log.append("\u001B[36mTrigger url\u001B[0m:").append("POST: ").append(externalApprovalJobVO.getTriggerUrl()).append(System.lineSeparator());
////        log.append("\u001B[36mStatus Code\u001B[0m:").append(STATUS_CODE).append(System.lineSeparator());
////
////        log.append("\u001B[0K\u001B[32;1mRequest headers: \u001B[0;m").append(System.lineSeparator());
////        log.append(entity.getHeaders()).append(System.lineSeparator());
////        log.append("\u001B[0K\u001B[32;1mRequest body: \u001B[0;m").append(System.lineSeparator());
////        log.append(JsonHelper.marshalByJackson(entity.getBody())).append(System.lineSeparator());
////
////
////        ResponseEntity<Void> responseEntity = null;
////        try {
////            responseEntity = restTemplateForIp.exchange(externalApprovalJobVO.getTriggerUrl(), HttpMethod.POST, entity, Void.class);
////            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
////                throw new RestClientException("devops.trigger.external.approval.task");
////            }
////
////            log.append("\u001B[0K\u001B[32;1mResponse headers: \u001B[0;m").append(System.lineSeparator());
////            log.append(responseEntity.getHeaders()).append(System.lineSeparator());
////            log.append("\u001B[0K\u001B[32;1mResponse body: \u001B[0;m:").append(System.lineSeparator());
////            log.append(responseEntity.getBody()).append(System.lineSeparator());
////            String logStr = log.toString();
////            devopsCdJobRecordDTO.setLog(logStr.replace(STATUS_CODE, responseEntity.getStatusCode().toString()));
////            devopsCdJobRecordDTO.setStartedDate(new Date());
////            devopsCdJobRecordDTO.setFinishedDate(null);
////            // 更新任务状态为执行中
////            devopsCdJobRecordDTO.setStatus(PipelineStatus.RUNNING.toString());
////            devopsCdJobRecordService.update(devopsCdJobRecordDTO);
////        } catch (Exception e) {
////            LOGGER.info("devops.trigger.external.approval.task", e);
////            log.append("\u001B[0K\u001B[31;1mTrigger error msg: \u001B[0;m").append(System.lineSeparator());
////            log.append(LogUtil.cutOutString(LogUtil.readContentOfThrowable(e), 2500)).append(System.lineSeparator());
////            String logStr = log.toString();
////            if (responseEntity != null) {
////                devopsCdJobRecordDTO.setLog(logStr.replace(STATUS_CODE, responseEntity.getStatusCode().toString()));
////            } else {
////                devopsCdJobRecordDTO.setLog(logStr.replace(STATUS_CODE, "500"));
////            }
////
////            devopsCdJobRecordDTO.setStatus(PipelineStatus.FAILED.toValue());
////            devopsCdJobRecordDTO.setStartedDate(new Date());
////            devopsCdJobRecordDTO.setFinishedDate(new Date());
////            if (devopsCdJobRecordDTO.getStartedDate() != null) {
////                devopsCdJobRecordDTO.setDurationSeconds((new Date().getTime() - devopsCdJobRecordDTO.getStartedDate().getTime()) / 1000);
////            }
////            devopsCdJobRecordService.update(devopsCdJobRecordDTO);
////            devopsCdStageRecordService.updateStageStatusFailed(stageRecordId);
////            devopsCdPipelineRecordService.updatePipelineStatusFailed(pipelineRecordId);
////            workFlowServiceOperator.stopInstance(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey());
////        }
////
////
////    }
////
////    @Override
////    public void externalApprovalTaskCallback(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, String callbackToken, Boolean status) {
////        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
////        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
////        LOGGER.info("setExternalApprovalTaskStatus:pipelineRecordId: {} stageRecordId: {} taskId: {}, callbackToken: {}, status: {}.", pipelineRecordId, stageRecordId, jobRecordId, callbackToken, status);
////
////        // 如果token认证不通过则直接返回
////        if (!Objects.equals(devopsCdJobRecordDTO.getCallbackToken(), callbackToken)) {
////            LOGGER.info("setExternalApprovalTaskStatus:pipelineRecordId: {} stageRecordId: {} taskId: {}, callbackToken: {}, status: {}.callbackToken is invalid. ", pipelineRecordId, stageRecordId, jobRecordId, callbackToken, status);
////            return;
////        }
////
////        // 状态不是待审核，抛出错误信息
////        if (!PipelineStatus.RUNNING.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
////            LOGGER.info("setExternalApprovalTaskStatus:pipelineRecordId: {} stageRecordId: {} taskId: {}, callbackToken: {}, status: {}.job status is invalid", pipelineRecordId, stageRecordId, jobRecordId, callbackToken, status);
////            throw new CommonException(ERROR_PIPELINE_STATUS_CHANGED);
////        }
////
////        // 记录回调日志
////        StringBuilder logStB = new StringBuilder(devopsCdJobRecordDTO.getLog());
////        logStB.append("\u001B[0K\u001B[32;1mCallBack Info: \u001B[0;m").append(System.lineSeparator());
////        String url = gatewayUrl + AUDIT_TASK_CALLBACK_URL;
////        logStB.append("\u001B[36mTrigger url\u001B[0m:").append("PUT: ").append(url).append(System.lineSeparator());
////        logStB.append("\u001B[36mpipeline_record_id\u001B[0m:").append(pipelineRecordId).append(System.lineSeparator());
////        logStB.append("\u001B[36mstage_record_id\u001B[0m:").append(stageRecordId).append(System.lineSeparator());
////        logStB.append("\u001B[36mjob_record_id\u001B[0m:").append(jobRecordId).append(System.lineSeparator());
////        logStB.append("\u001B[36mcallback_token\u001B[0m:").append(callbackToken).append(System.lineSeparator());
////        logStB.append("\u001B[36mapproval_status\u001B[0m:").append(status).append(System.lineSeparator());
////
////        devopsCdJobRecordService.updateLogById(jobRecordId, logStB);
////        if (Boolean.TRUE.equals(status)) {
////            try {
////                workFlowServiceOperator.approveUserTask(devopsCdPipelineRecordDTO.getProjectId(), devopsCdPipelineRecordDTO.getBusinessKey(), MiscConstants.WORKFLOW_ADMIN_NAME, MiscConstants.WORKFLOW_ADMIN_ID, MiscConstants.WORKFLOW_ADMIN_ORG_ID);
////
////                devopsCdJobRecordService.updateJobStatusSuccess(jobRecordId);
////                setAppDeployStatus(pipelineRecordId, stageRecordId, jobRecordId, true);
////            } catch (Exception e) {
////                setAppDeployStatus(pipelineRecordId, stageRecordId, jobRecordId, false);
////            }
////        } else {
////            setAppDeployStatus(pipelineRecordId, stageRecordId, jobRecordId, false);
////        }
////
////    }
////
////    @Override
////    public String queryCallbackUrl() {
////        return gatewayUrl + AUDIT_TASK_CALLBACK_URL + "?pipeline_record_id=${xxx}&stage_record_id=${xxx}&job_record_id=${xxx}&callback_token=${xxx}$approval_status=${xxx}";
////    }
//
////    private void handlerJobSuccess(DevopsCdJobRecordDTO devopsCdJobRecordDTO, DevopsCdStageRecordDTO devopsCdStageRecordDTO, DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO) {
////        try {
////            workFlowServiceOperator.approveUserTask(devopsCdPipelineRecordDTO.getProjectId(),
////                    devopsCdPipelineRecordDTO.getBusinessKey(),
////                    MiscConstants.WORKFLOW_ADMIN_NAME,
////                    MiscConstants.WORKFLOW_ADMIN_ID,
////                    MiscConstants.WORKFLOW_ADMIN_ORG_ID);
////            devopsCdJobRecordService.updateJobStatusSuccess(devopsCdJobRecordDTO.getId());
////            setAppDeployStatus(devopsCdPipelineRecordDTO.getId(),
////                    devopsCdStageRecordDTO.getId(),
////                    devopsCdJobRecordDTO.getId(),
////                    true);
////        } catch (Exception e) {
////            setAppDeployStatus(devopsCdPipelineRecordDTO.getId(),
////                    devopsCdStageRecordDTO.getId(),
////                    devopsCdJobRecordDTO.getId(),
////                    false);
////        }
////    }
//
//
//    private void calculatAuditUserName(List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOList, AduitStatusChangeVO aduitStatusChangeVO) {
//
//        if (!CollectionUtils.isEmpty(devopsCdAuditRecordDTOList)) {
//            aduitStatusChangeVO.setAuditStatusChanged(true);
//            List<Long> userIds = devopsCdAuditRecordDTOList.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
//
//            List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
//            List<String> userNameList = new ArrayList<>();
//            iamUserDTOS.forEach(iamUserDTO -> {
//                if (Boolean.TRUE.equals(iamUserDTO.getLdap())) {
//                    userNameList.add(iamUserDTO.getLoginName());
//                } else {
//                    userNameList.add(iamUserDTO.getEmail());
//                }
//            });
//            aduitStatusChangeVO.setAuditUserName(StringUtils.join(userNameList, ","));
//        }
//    }
//
//    @Override
//    public PipelineInstanceReferenceVO queryPipelineReference(Long projectId, Long instanceId) {
//        return devopsCiCdPipelineMapper.queryPipelineReference(instanceId);
//    }
//
//    @Nullable
//    @Override
//    public PipelineInstanceReferenceVO queryPipelineReferenceEnvApp(Long projectId, Long appId) {
//        return devopsCiCdPipelineMapper.queryPipelineReferenceEnvApp(appId);
//    }
//
//    @Nullable
//    @Override
//    public PipelineInstanceReferenceVO queryPipelineReferenceHostApp(Long projectId, Long appId) {
//        return devopsCiCdPipelineMapper.queryPipelineReferenceHostApp(appId);
//    }
//
//    @Override
//    @Transactional
//    public void hostDeployStatusUpdate(Long commandId, Long jobRecordId, Boolean status, String error) {
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
//        DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordService.queryById(devopsCdJobRecordDTO.getStageRecordId());
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(devopsCdStageRecordDTO.getPipelineRecordId());
//        Long pipelineRecordId = devopsCdPipelineRecordDTO.getId();
//        Long stageRecordId = devopsCdStageRecordDTO.getId();
//
//        // 状态不是待审核，抛出错误信息
//        if (!PipelineStatus.RUNNING.toValue().equals(devopsCdJobRecordDTO.getStatus())) {
//            throw new CommonException(ERROR_PIPELINE_STATUS_CHANGED);
//        }
//        if (Boolean.TRUE.equals(status)) {
//            try {
//                workFlowServiceOperator.approveUserTask(devopsCdPipelineRecordDTO.getProjectId(),
//                        devopsCdPipelineRecordDTO.getBusinessKey(),
//                        MiscConstants.WORKFLOW_ADMIN_NAME,
//                        MiscConstants.WORKFLOW_ADMIN_ID,
//                        MiscConstants.WORKFLOW_ADMIN_ORG_ID);
//                devopsCdJobRecordService.updateJobStatusSuccess(jobRecordId);
//                setAppDeployStatus(pipelineRecordId, stageRecordId, jobRecordId, true);
//            } catch (Exception e) {
//                setAppDeployStatus(pipelineRecordId, stageRecordId, jobRecordId, false);
//            }
//        } else {
//            devopsCdJobRecordService.updateJobStatusFailed(jobRecordId, error);
//            setAppDeployStatus(pipelineRecordId, stageRecordId, jobRecordId, false);
//        }
//    }
//
//    private Boolean checkAutoDeploy(Long envId) {
//        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(envId);
//        if (environmentDTO == null) {
//            throw new CommonException("devops.get.environment");
//        }
//        return environmentDTO.getAutoDeploy();
//    }
//}
