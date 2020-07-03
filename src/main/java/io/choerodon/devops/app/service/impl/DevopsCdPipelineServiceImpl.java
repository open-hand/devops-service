package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.GenerateUUID;

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


    private static final String MANUAL = "manual";
    private static final String AUTO = "auto";


    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    @Value("${services.gateway.url}")
    private String gatewayUrl;

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
                String ref = pipelineWebHookVO.getObjectAttributes().getRef();
                List<DevopsCdJobDTO> executeJobList = devopsCdJobDTOList.stream().filter(job -> {
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
                Map<Long, List<DevopsCdJobDTO>> executeJobMap = executeJobList.stream().collect(Collectors.groupingBy(DevopsCdJobDTO::getStageId));

                // 3. 统计出要执行的阶段（要执行的job的所属阶段）
                Set<Long> stageIds = executeJobList.stream().map(DevopsCdJobDTO::getStageId).collect(Collectors.toSet());
                List<DevopsCdStageDTO> executeStageList = stageIds.stream().map(devopsCdStageDTOMap::get).collect(Collectors.toList());

                // 4. 如果有要执行的阶段、job，则初始化执行记录（初始化记录状态为pending）
                if (!CollectionUtils.isEmpty(executeStageList)) {
                    // 保存流水线记录
                    devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
                    devopsCdPipelineRecordDTO.setPipelineId(devopsCiPipelineDTO.getId());
                    devopsCdPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
                    devopsCdPipelineRecordDTO.setStatus(PipelineStatus.PENDING.toValue());
                    devopsCdPipelineRecordDTO.setPipelineName(devopsCiPipelineDTO.getName());
                    devopsCdPipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
                    devopsCdPipelineRecordDTO.setProjectId(devopsCiPipelineDTO.getProjectId());

                    devopsCdPipelineRecordService.save(devopsCdPipelineRecordDTO);

                    // 创建cd阶段记录
                    DevopsCdPipelineRecordDTO finalDevopsCdPipelineRecordDTO = devopsCdPipelineRecordDTO;

                    devopsCdStageDTOList.forEach(stage -> {
                        DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
                        devopsCdStageRecordDTO.setPipelineRecordId(finalDevopsCdPipelineRecordDTO.getId());
                        devopsCdStageRecordDTO.setStageId(stage.getId());
                        devopsCdStageRecordDTO.setStatus(PipelineStatus.PENDING.toValue());
                        devopsCdStageRecordService.save(devopsCdStageRecordDTO);

                        // 人工审核阶段，添加审核人员记录
                        if (TriggerTypeEnum.MANUAL.value().equals(stage.getTriggerType())) {
                            List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, stage.getId(), null);
                            devopsCdAuditDTOS.forEach(audit -> {
                                DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = new DevopsCdAuditRecordDTO();
                                devopsCdAuditRecordDTO.setStageRecordId(devopsCdStageRecordDTO.getId());
                                devopsCdAuditRecordDTO.setUserId(audit.getUserId());
                                devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.NOT_AUDIT.value());
                            });
                        }

                        // 保存job执行记录
                        List<DevopsCdJobDTO> devopsCdJobDTOS = executeJobMap.get(stage.getId());
                        devopsCdJobDTOS.forEach(job -> {
                            DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
                            devopsCdJobRecordDTO.setName(job.getName());
                            devopsCdJobRecordDTO.setStageRecordId(devopsCdStageRecordDTO.getId());
                            devopsCdJobRecordDTO.setType(job.getType());
                            devopsCdJobRecordDTO.setStatus(PipelineStatus.PENDING.toValue() );
                            devopsCdJobRecordDTO.setTriggerType(job.getTriggerType());
                            devopsCdJobRecordDTO.setTriggerValue(job.getTriggerType());
                            devopsCdJobRecordDTO.setMetadata(job.getMetadata());

                            devopsCdJobRecordService.save(devopsCdJobRecordDTO);

                            // 人工卡点任务，添加审核人员记录
                            if (JobTypeEnum.CD_AUDIT.value().equals(job.getType())) {
                                List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, null, job.getId());
                                devopsCdAuditDTOS.forEach(audit -> {
                                    DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = new DevopsCdAuditRecordDTO();
                                    devopsCdAuditRecordDTO.setJobRecordId(devopsCdJobRecordDTO.getId());
                                    devopsCdAuditRecordDTO.setUserId(audit.getUserId());
                                    devopsCdAuditRecordDTO.setStatus(AuditStatusEnum.NOT_AUDIT.value());
                                });
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


            }
        }
    }


}
