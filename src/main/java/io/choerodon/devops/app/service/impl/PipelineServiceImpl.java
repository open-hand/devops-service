package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CommonScheduleVO;
import io.choerodon.devops.api.vo.PipelineVO;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.api.vo.cd.PipelineStageVO;
import io.choerodon.devops.app.eventhandler.cd.AbstractCdJobHandler;
import io.choerodon.devops.app.eventhandler.cd.CdJobOperator;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.asgard.ScheduleTaskDTO;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.enums.cd.PipelineTriggerTypeEnum;
import io.choerodon.devops.infra.mapper.PipelineMapper;
import io.choerodon.devops.infra.util.*;

/**
 * 流水线表(Pipeline)应用服务
 *
 * @author
 * @since 2022-11-24 15:50:13
 */
@Service
public class PipelineServiceImpl implements PipelineService {

    private static final String CRON_TRIGGER = "cron-trigger";
    private static final String SERIAL = "SERIAL";
    private static final String DEVOPS_SAVE_PIPELINE_FAILED = "devops.save.pipeline.failed";
    private static final String DEVOPS_ENABLE_PIPELINE_FAILED = "devops.enable.pipeline.failed";
    private static final String DEVOPS_DISABLE_PIPELINE_FAILED = "devops.disable.pipeline.failed";
    private static final String DEVOPS_PIPELINE_STATUS_IS_DISABLE = "devops.pipeline.status.is.disable";

    @Value("${spring.application.name}")
    private String serviceCode;
    @Autowired
    private PipelineMapper pipelineMapper;
    @Autowired
    private PipelineStageService pipelineStageService;
    @Autowired
    private PipelineVersionService pipelineVersionService;
    @Autowired
    private PipelineJobService pipelineJobService;
    @Autowired
    private PipelineRecordService pipelineRecordService;
    @Autowired
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    private PipelineJobRecordService pipelineJobRecordService;
    @Autowired
    private PipelineScheduleService pipelineScheduleService;
    @Autowired
    private CdJobOperator cdJobOperator;

    @Autowired
    private TransactionalProducer transactionalProducer;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineDTO pipelineDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineMapper, pipelineDTO, DEVOPS_SAVE_PIPELINE_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineDTO pipelineDTO) {
        pipelineMapper.updateByPrimaryKeySelective(pipelineDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDeleteById(Long id) {
        pipelineMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PipelineDTO baseQueryById(Long id) {
        return pipelineMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_PIPELINE_TIME_TASK,
            description = "创建流水线定时执行计划",
            inputSchema = "{}")
    public PipelineDTO create(Long projectId, PipelineVO pipelineVO) {

        PipelineDTO pipelineDTO = ConvertUtils.convertObject(pipelineVO, PipelineDTO.class);

        // 初始化令牌
        if (StringUtils.isEmpty(pipelineVO.getToken())) {
            pipelineDTO.setToken(GenerateUUID.generateUUID());
        }
        baseCreate(pipelineDTO);
        Long pipelineId = pipelineDTO.getId();

        // 保存流水线定时执行配置
        List<ScheduleTaskDTO> scheduleTaskDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(pipelineVO.getPipelineScheduleList())) {
            pipelineVO.getPipelineScheduleList().forEach(pipelineScheduleVO -> {
                PipelineScheduleDTO pipelineScheduleDTO = pipelineScheduleService.create(pipelineId, pipelineScheduleVO);
                constructScheduleTaskDTO(projectId, pipelineId, scheduleTaskDTOList, pipelineScheduleVO, pipelineScheduleDTO);
            });
        }

        savePipelineVersion(projectId, pipelineVO, pipelineDTO);

        // 发送saga创建定时任务
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withRefType("devops-pipeline")
                        .withRefId(pipelineId.toString())
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_PIPELINE_TIME_TASK)
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withPayloadAndSerialize(scheduleTaskDTOList),
                builder -> {
                });
        return pipelineDTO;
    }

    private void constructScheduleTaskDTO(Long projectId,
                                          Long pipelineId,
                                          List<ScheduleTaskDTO> scheduleTaskDTOList,
                                          CommonScheduleVO pipelineScheduleVO,
                                          PipelineScheduleDTO pipelineScheduleDTO) {
        ScheduleTaskDTO scheduleTaskDTO = new ScheduleTaskDTO();
        scheduleTaskDTO.setName("DevopsPipelineTrigger-" + pipelineId + "-" + pipelineScheduleDTO.getName());
        scheduleTaskDTO.setCronExpression(ScheduleUtil.calculateCron(pipelineScheduleVO));
        scheduleTaskDTO.setTriggerType(CRON_TRIGGER);
        scheduleTaskDTO.setExecuteStrategy(SERIAL);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(new Date());
        scheduleTaskDTO.setStartTimeStr(dateString);
        ScheduleTaskDTO.NotifyUser notifyUser = new ScheduleTaskDTO.NotifyUser();
        notifyUser.setAdministrator(true);
        notifyUser.setAssigner(false);
        notifyUser.setCreator(false);
        scheduleTaskDTO.setNotifyUser(notifyUser);

        Map<String, Object> params = new HashMap<>();
        params.put(MiscConstants.PROJECT_ID, projectId);
        params.put(MiscConstants.PIPELINE_ID, pipelineId);
        params.put(MiscConstants.SCHEDULE_TOKEN, pipelineScheduleDTO.getToken());
        params.put(MiscConstants.USER_ID, pipelineScheduleDTO.getCreatedBy());
        scheduleTaskDTO.setParams(params);
        scheduleTaskDTO.setMethodCode("pipelineScheduleTrigger");
        scheduleTaskDTO.setServiceCode(serviceCode);
        scheduleTaskDTOList.add(scheduleTaskDTO);
    }

    private void savePipelineVersion(Long projectId, PipelineVO pipelineVO, PipelineDTO pipelineDTO) {
        PipelineVersionDTO pipelineVersionDTO = pipelineVersionService.createByPipelineId(pipelineDTO.getId());
        pipelineDTO.setEffectVersionId(pipelineVersionDTO.getId());
        List<PipelineStageVO> stageList = pipelineVO.getStageList();
        if (CollectionUtils.isEmpty(stageList)) {
            throw new CommonException("devops.pipeline.stage.is.empty");
        }
        stageList.forEach(stage -> {
            pipelineStageService.saveStage(projectId, pipelineDTO.getId(), pipelineVersionDTO.getId(), stage);
        });
        baseUpdate(pipelineDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (Boolean.FALSE.equals(pipelineDTO.getEnable())) {
            pipelineDTO.setEnable(true);
            MapperUtil.resultJudgedInsertSelective(pipelineMapper, pipelineDTO, DEVOPS_ENABLE_PIPELINE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (Boolean.TRUE.equals(pipelineDTO.getEnable())) {
            pipelineDTO.setEnable(false);
            MapperUtil.resultJudgedInsertSelective(pipelineMapper, pipelineDTO, DEVOPS_DISABLE_PIPELINE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        if (pipelineDTO == null) {
            return;
        }
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        // 1. 删除流水线定义
        // 删除任务、任务配置
        pipelineJobService.deleteByPipelineId(id);
        // 删除阶段
        pipelineStageService.deleteByPipelineId(id);
        //删除版本
        pipelineVersionService.deleteByPipelineId(id);
        // 删除流水线
        baseDeleteById(id);

        // 2. 删除流水线执行记录
        // 删除任务记录
        pipelineJobRecordService.deleteByPipelineId(id);
        // 删除阶段记录
        pipelineStageRecordService.deleteByPipelineId(id);
        // 删除流水线记录
        pipelineRecordService.deleteByPipelineId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long projectId, Long id, PipelineVO pipelineVO) {
        //
        PipelineDTO pipelineDTO = ConvertUtils.convertObject(pipelineVO, PipelineDTO.class);

        savePipelineVersion(projectId, pipelineVO, pipelineDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineRecordDTO execute(Long projectId,
                                     Long id,
                                     PipelineTriggerTypeEnum triggerType,
                                     Map<String, Object> params) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        if (Boolean.FALSE.equals(pipelineDTO.getEnable())) {
            throw new CommonException(DEVOPS_PIPELINE_STATUS_IS_DISABLE);
        }
        Long effectVersionId = pipelineDTO.getEffectVersionId();

        // 初始化流水线记录
        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO();
        pipelineRecordDTO.setTriggerType(triggerType.value());
        pipelineRecordDTO.setStatus(PipelineStatusEnum.CREATED.value());
        pipelineRecordDTO.setPipelineId(id);
        pipelineRecordService.baseCreate(pipelineRecordDTO);

        Long pipelineRecordId = pipelineRecordDTO.getId();

        //初始化阶段
        PipelineStageRecordDTO firstStageRecordDTO = null;
        List<PipelineJobRecordDTO> firstJobRecordList = new ArrayList<>();
        List<PipelineStageDTO> pipelineStageDTOS = pipelineStageService.listByVersionId(effectVersionId);
        List<PipelineStageDTO> sortedPipelineStage = pipelineStageDTOS
                .stream()
                .sorted(Comparator.comparing(PipelineStageDTO::getSequence).reversed()).collect(Collectors.toList());

        Long nextStageRecordId = null;
        for (int i = 0; i < sortedPipelineStage.size(); i++) {
            PipelineStageDTO stage = sortedPipelineStage.get(i);
            Long stageId = stage.getId();
            PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO(id,
                    stageId,
                    pipelineRecordId,
                    stage.getSequence(),
                    nextStageRecordId,
                    PipelineStatusEnum.CREATED.value());
            pipelineStageRecordService.baseCreate(pipelineStageRecordDTO);

            Long stageRecordId = pipelineStageRecordDTO.getId();
            // 初始化任务记录
            List<PipelineJobDTO> pipelineJobDTOS = pipelineJobService.listByStageId(stageId);
            for (PipelineJobDTO job : pipelineJobDTOS) {
                Long jobId = job.getId();
                PipelineJobRecordDTO pipelineJobRecordDTO = new PipelineJobRecordDTO(id,
                        jobId,
                        stageRecordId,
                        PipelineStatusEnum.CREATED.value(),
                        job.getType());
                pipelineJobRecordService.baseCreate(pipelineJobRecordDTO);
                // 记录流水线的第一个阶段信息
                if (i == sortedPipelineStage.size() - 1) {
                    firstStageRecordDTO = pipelineStageRecordDTO;
                    firstJobRecordList.add(pipelineJobRecordDTO);
                }

                AbstractCdJobHandler handler = cdJobOperator.getHandlerOrThrowE(job.getType());
                handler.initAdditionalRecordInfo(id, job, pipelineJobRecordDTO);
            }
            // 用于设置阶段间流转顺序
            nextStageRecordId = pipelineStageRecordDTO.getId();
        }
        // 启动流水线
        pipelineRecordService.startNextStage(pipelineRecordDTO, firstStageRecordDTO, firstJobRecordList);

        return pipelineRecordDTO;
    }

    @Override
    public PipelineVO query(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        PipelineVO pipelineVO = ConvertUtils.convertObject(pipelineDTO, PipelineVO.class);
        Long versionId = pipelineDTO.getEffectVersionId();

        // 查询阶段信息
        List<PipelineStageDTO> pipelineStageDTOS = pipelineStageService.listByVersionId(versionId);
        List<PipelineStageVO> pipelineStageVOS = ConvertUtils.convertList(pipelineStageDTOS, PipelineStageVO.class);

        List<PipelineJobDTO> pipelineJobDTOS = pipelineJobService.listByVersionId(versionId);
        List<PipelineJobVO> pipelineJobVOS = ConvertUtils.convertList(pipelineJobDTOS, PipelineJobVO.class);
        Map<Long, List<PipelineJobVO>> jobMap = pipelineJobVOS.stream().collect(Collectors.groupingBy(PipelineJobVO::getStageId));

        pipelineStageVOS.forEach(stage -> {
            List<PipelineJobVO> jobVOList = jobMap.get(stage.getId());
            jobVOList.forEach(job -> {
                AbstractCdJobHandler handler = cdJobOperator.getHandlerOrThrowE(job.getType());
                handler.fillJobConfigInfo(job);
            });
            stage.setJobList(jobVOList);
        });
        List<PipelineStageVO> sortedStageList = pipelineStageVOS.stream().sorted(Comparator.comparing(PipelineStageVO::getSequence)).collect(Collectors.toList());
        pipelineVO.setStageList(sortedStageList);
        return pipelineVO;
    }
}

