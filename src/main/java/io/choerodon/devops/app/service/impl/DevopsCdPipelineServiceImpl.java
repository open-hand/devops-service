package io.choerodon.devops.app.service.impl;

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
import org.hzero.boot.message.entity.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.dto.*;
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

    @Override
    @Transactional
    public void handleCiPipelineStatusUpdate(PipelineWebHookVO pipelineWebHookVO) {
        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(pipelineWebHookVO.getToken());
        DevopsCiPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());

        // 查询流水线是否有cd阶段, 没有cd阶段不做处理
        List<DevopsCdStageDTO> devopsCdStageDTOList = devopsCdStageService.queryByPipelineId(devopsCiPipelineDTO.getId());
        if (CollectionUtils.isEmpty(devopsCdStageDTOList)) {
            return;
        }
        Map<Long, DevopsCdStageDTO> devopsCdStageDTOMap = devopsCdStageDTOList.stream().collect(Collectors.toMap(DevopsCdStageDTO::getId, v -> v));


        String status = pipelineWebHookVO.getObjectAttributes().getStatus();
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryByGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());

        if (PipelineStatus.PENDING.toValue().equals(status)
                || PipelineStatus.RUNNING.toValue().equals(status)) {
            // 校验CD流水线记录是否已经创建，未创建才创建记录，并将记录的初始状态设置为pending
            if (devopsCdPipelineRecordDTO == null) {
                // 1. 根据流水线id,查询job列表
                List<DevopsCdJobDTO> devopsCdJobDTOList = devopsCdJobService.listByPipelineId(devopsCiPipelineDTO.getId());

                // 2. 计算要执行的job
                List<DevopsCdJobDTO> executeJobList = calculateExecuteJobList(pipelineWebHookVO.getObjectAttributes().getRef(), devopsCdJobDTOList);
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
                    devopsCdPipelineRecordDTO = initPipelineRecord(devopsCiPipelineDTO, pipelineWebHookVO.getObjectAttributes().getId());
                    // 创建cd阶段记录
                    DevopsCdPipelineRecordDTO finalDevopsCdPipelineRecordDTO = devopsCdPipelineRecordDTO;
                    devopsCdStageDTOList.forEach(stage -> {
                        DevopsCdStageRecordDTO devopsCdStageRecordDTO = initStageRecord(finalDevopsCdPipelineRecordDTO.getId(), stage.getId());
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
            }
        }

        // ci流水线执行成功， 开始执行cd流水线
        if (PipelineStatus.SUCCESS.toValue().equals(status)) {
            // 执行条件：cd流水线记录状态为pending
            if (devopsCdPipelineRecordDTO  != null && PipelineStatus.PENDING.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {

                DevopsPipelineDTO devopsPipelineDTO = devopsCdPipelineRecordService.createCDWorkFlowDTO(devopsCdPipelineRecordDTO.getId());
                devopsCdPipelineRecordDTO.setBpmDefinition(gson.toJson(devopsPipelineDTO));

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
                    devopsCdPipelineRecordDTO.setErrorInfo(e.getMessage());
                    devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
                }

            }
        }
    }

    private DevopsCdPipelineRecordDTO initPipelineRecord(DevopsCiPipelineDTO devopsCiPipelineDTO, Long gitlabPipelineId) {
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
        devopsCdPipelineRecordDTO.setPipelineId(devopsCiPipelineDTO.getId());
        devopsCdPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.PENDING.toValue());
        devopsCdPipelineRecordDTO.setPipelineName(devopsCiPipelineDTO.getName());
        devopsCdPipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
        devopsCdPipelineRecordDTO.setProjectId(devopsCiPipelineDTO.getProjectId());
        devopsCdPipelineRecordService.save(devopsCdPipelineRecordDTO);

        return devopsCdPipelineRecordService.queryById(devopsCdPipelineRecordDTO.getId());
    }

    private DevopsCdStageRecordDTO initStageRecord(Long pipelineRecordId, Long stageId) {
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
        devopsCdStageRecordDTO.setPipelineRecordId(pipelineRecordId);
        devopsCdStageRecordDTO.setStageId(stageId);
        devopsCdStageRecordDTO.setStatus(PipelineStatus.PENDING.toValue());
        devopsCdStageRecordService.save(devopsCdStageRecordDTO);

        return devopsCdStageRecordService.queryById(devopsCdStageRecordDTO.getId());
    }

    private DevopsCdJobRecordDTO initJobRecord(Long stageRecordId, DevopsCdJobDTO job) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
        devopsCdJobRecordDTO.setName(job.getName());
        devopsCdJobRecordDTO.setStageRecordId(stageRecordId);
        devopsCdJobRecordDTO.setType(job.getType());
        devopsCdJobRecordDTO.setStatus(PipelineStatus.PENDING.toValue() );
        devopsCdJobRecordDTO.setTriggerType(job.getTriggerType());
        devopsCdJobRecordDTO.setTriggerValue(job.getTriggerType());
        devopsCdJobRecordDTO.setMetadata(job.getMetadata());

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
        });
    }

    private void addJobAuditRecord(Long jobId, Long jobRecordId) {
        List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, null, jobId);
        devopsCdAuditDTOS.forEach(audit -> {
            DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = new DevopsCdAuditRecordDTO();
            devopsCdAuditRecordDTO.setJobRecordId(jobRecordId);
            devopsCdAuditRecordDTO.setUserId(audit.getUserId());
            devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.NOT_AUDIT.value());
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
        sendNotificationService.sendPipelineNotice(pipelineRecordId,
                MessageCodeConstants.PIPELINE_FAILED, userId, null, null);
    }

    private void updateFirstStage(Long pipelineRecordId) {
        DevopsCdStageRecordDTO devopsCdStageRecord = devopsCdStageRecordService.queryFirstByPipelineRecordId(pipelineRecordId);
        if (TriggerTypeEnum.MANUAL.equals(devopsCdStageRecord.getTriggerType())) {
            // 更新阶段状态为待审核
            devopsCdStageRecord.setStatus(PipelineStatus.NOT_AUDIT.toValue());
            devopsCdStageRecordService.update(devopsCdStageRecord);
            // 给审核人员发送审核通知
            sendStageAuditMessage(devopsCdStageRecord);
        } else {
            // 更新阶段状态为执行中
            devopsCdStageRecord.setStatus(PipelineStatus.RUNNING.toValue());
            devopsCdStageRecordService.update(devopsCdStageRecord);
            // 更新第一个job的状态
            updateFirstJob(pipelineRecordId, devopsCdStageRecord);
        }
    }

    private void updateFirstJob(Long pipelineRecordId, DevopsCdStageRecordDTO devopsCdStageRecord) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryFirstByStageRecordId(devopsCdStageRecord.getId());
        if (devopsCdJobRecordDTO == null) {
            return;
        }
        if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordDTO.getType())) {
            // 更新job状态为待审核
            devopsCdJobRecordDTO.setStatus(PipelineStatus.NOT_AUDIT.toValue());
            devopsCdJobRecordService.update(devopsCdJobRecordDTO);
            // 给审核人员发送审核通知
            sendJobAuditMessage(pipelineRecordId, devopsCdJobRecordDTO);
        } else {
            // 更新job状态为running
            // 更新job状态为待审核
            devopsCdJobRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
            devopsCdJobRecordService.update(devopsCdJobRecordDTO);
        }
    }

    private void sendStageAuditMessage(DevopsCdStageRecordDTO devopsCdStageRecord) {
        // 查询审核人员
        List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByStageRecordId(devopsCdStageRecord.getId());
        if (CollectionUtils.isEmpty(devopsCdAuditRecordDTOS)) {
            return;
        }
        // 发送审核通知
        List<Receiver> userList = new ArrayList<>();
        List<Long> userIds = devopsCdAuditRecordDTOS.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
        Map<Long, IamUserDTO> userDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));

        userIds.forEach(id -> {
            IamUserDTO iamUserDTO = userDTOMap.get(id);
            if (iamUserDTO != null) {
                Receiver user = new Receiver();
                user.setEmail(iamUserDTO.getEmail());
                user.setUserId(iamUserDTO.getId());
                user.setPhone(iamUserDTO.getPhone());
                user.setTargetUserTenantId(iamUserDTO.getOrganizationId());
                userList.add(user);
            }
        });
        HashMap<String, String> params = new HashMap<>();
        params.put(STAGE_NAME, devopsCdStageRecord.getStageName());
        sendNotificationService.sendPipelineNotice(devopsCdStageRecord.getPipelineRecordId(), MessageCodeConstants.PIPELINE_AUDIT, userList, params);
    }

    private void sendJobAuditMessage(Long pipelineRecordId, DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
        // 查询审核人员
        List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(devopsCdJobRecordDTO.getId());
        if (CollectionUtils.isEmpty(devopsCdAuditRecordDTOS)) {
            return;
        }
        // 发送审核通知
        List<Receiver> userList = new ArrayList<>();
        List<Long> userIds = devopsCdAuditRecordDTOS.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
        Map<Long, IamUserDTO> userDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));

        userIds.forEach(id -> {
            IamUserDTO iamUserDTO = userDTOMap.get(id);
            if (iamUserDTO != null) {
                Receiver user = new Receiver();
                user.setEmail(iamUserDTO.getEmail());
                user.setUserId(iamUserDTO.getId());
                user.setPhone(iamUserDTO.getPhone());
                user.setTargetUserTenantId(iamUserDTO.getOrganizationId());
                userList.add(user);
            }
        });
        HashMap<String, String> params = new HashMap<>();
        params.put(STAGE_NAME, devopsCdJobRecordDTO.getName());
        sendNotificationService.sendPipelineNotice(pipelineRecordId, MessageCodeConstants.PIPELINE_AUDIT, userList, params);
    }

    @Override
    public void triggerCdPipeline(String token, String commit) {
//        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);
//        DevopsCiPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
//        if (devopsCiPipelineDTO != null) {
//            List<DevopsCdStageDTO> devopsCdStageDTOList = devopsCdStageService.queryByPipelineId(devopsCiPipelineDTO.getId());
//            if (CollectionUtils.isEmpty(devopsCdStageDTOList)) {
//                return;
//            }
//            Map<Long, DevopsCdStageDTO> devopsCdStageDTOMap = devopsCdStageDTOList.stream().collect(Collectors.toMap(DevopsCdStageDTO::getId, v -> v));
//
//            // 1. 根据流水线id,查询job列表
//            List<DevopsCdJobDTO> devopsCdJobDTOList = devopsCdJobService.listByPipelineId(devopsCiPipelineDTO.getId());
//
//            // 2. 计算要执行的job
//
//            String ref = pipelineWebHookVO.getObjectAttributes().getRef();
//            List<DevopsCdJobDTO> executeJobList = devopsCdJobDTOList.stream().filter(job -> {
//                String triggerType = job.getTriggerType();
//                // 根据匹配规则，计算出要执行的job
//                if (CiTriggerType.REFS.value().equals(triggerType)
//                        && job.getTriggerValue().contains(ref)) {
//                    return true;
//                } else if (CiTriggerType.EXACT_MATCH.value().equals(triggerType)
//                        && job.getTriggerValue().equals(ref)) {
//                    return true;
//                } else if (CiTriggerType.EXACT_EXCLUDE.value().equals(triggerType)
//                        && job.getTriggerValue().equals(ref)) {
//                    return false;
//                } else if (CiTriggerType.REGEX_MATCH.value().equals(triggerType)) {
//                    Pattern pattern = Pattern.compile(job.getTriggerValue());
//                    return pattern.matcher(ref).matches();
//                }
//                return false;
//            }).collect(Collectors.toList());
//            Map<Long, List<DevopsCdJobDTO>> executeJobMap = executeJobList.stream().collect(Collectors.groupingBy(DevopsCdJobDTO::getStageId));
//
//            // 3. 统计出要执行的阶段（要执行的job的所属阶段）
//            Set<Long> stageIds = executeJobList.stream().map(DevopsCdJobDTO::getStageId).collect(Collectors.toSet());
//            List<DevopsCdStageDTO> executeStageList = stageIds.stream().map(devopsCdStageDTOMap::get).collect(Collectors.toList());
//
//            // 4. 如果有要执行的阶段、job，则初始化执行记录（初始化记录状态为pending）
//            if (!CollectionUtils.isEmpty(executeStageList)) {
//                // 保存流水线记录
//                DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
//                devopsCdPipelineRecordDTO.setPipelineId(devopsCiPipelineDTO.getId());
////                devopsCdPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
//                devopsCdPipelineRecordDTO.setStatus(PipelineStatus.PENDING.toValue());
//                devopsCdPipelineRecordDTO.setPipelineName(devopsCiPipelineDTO.getName());
//                devopsCdPipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
//                devopsCdPipelineRecordDTO.setProjectId(devopsCiPipelineDTO.getProjectId());
//
//                devopsCdPipelineRecordService.save(devopsCdPipelineRecordDTO);
//
//                // 创建cd阶段记录
//                DevopsCdPipelineRecordDTO finalDevopsCdPipelineRecordDTO = devopsCdPipelineRecordDTO;
//
//                devopsCdStageDTOList.forEach(stage -> {
//                    DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
//                    devopsCdStageRecordDTO.setPipelineRecordId(finalDevopsCdPipelineRecordDTO.getId());
//                    devopsCdStageRecordDTO.setStageId(stage.getId());
//                    devopsCdStageRecordDTO.setStatus(PipelineStatus.PENDING.toValue());
//                    devopsCdStageRecordService.save(devopsCdStageRecordDTO);
//
//                    // 人工审核阶段，添加审核人员记录
//                    if (TriggerTypeEnum.MANUAL.value().equals(stage.getTriggerType())) {
//                        List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, stage.getId(), null);
//                        devopsCdAuditDTOS.forEach(audit -> {
//                            DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = new DevopsCdAuditRecordDTO();
//                            devopsCdAuditRecordDTO.setStageRecordId(devopsCdStageRecordDTO.getId());
//                            devopsCdAuditRecordDTO.setUserId(audit.getUserId());
//                            devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.NOT_AUDIT.value());
//                        });
//                    }
//
//                    // 保存job执行记录
//                    List<DevopsCdJobDTO> devopsCdJobDTOS = executeJobMap.get(stage.getId());
//                    devopsCdJobDTOS.forEach(job -> {
//                        DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
//                        devopsCdJobRecordDTO.setName(job.getName());
//                        devopsCdJobRecordDTO.setStageRecordId(devopsCdStageRecordDTO.getId());
//                        devopsCdJobRecordDTO.setType(job.getType());
//                        devopsCdJobRecordDTO.setStatus(PipelineStatus.PENDING.toValue() );
//                        devopsCdJobRecordDTO.setTriggerType(job.getTriggerType());
//                        devopsCdJobRecordDTO.setTriggerValue(job.getTriggerType());
//                        devopsCdJobRecordDTO.setMetadata(job.getMetadata());
//
//                        devopsCdJobRecordService.save(devopsCdJobRecordDTO);
//
//                        // 人工卡点任务，添加审核人员记录
//                        if (JobTypeEnum.CD_AUDIT.value().equals(job.getType())) {
//                            List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, null, job.getId());
//                            devopsCdAuditDTOS.forEach(audit -> {
//                                DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = new DevopsCdAuditRecordDTO();
//                                devopsCdAuditRecordDTO.setJobRecordId(devopsCdJobRecordDTO.getId());
//                                devopsCdAuditRecordDTO.setUserId(audit.getUserId());
//                                devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.NOT_AUDIT.value());
//                            });
//                        }
//                    });
//                });
//            }

//        }

    }

    private void createWorkFlow(Long projectId, io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO devopsPipelineDTO, String loginName, Long userId, Long orgId) {

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
}
