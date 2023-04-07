package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import groovy.lang.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.api.vo.cd.PipelineScheduleVO;
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
import io.choerodon.devops.infra.enums.cd.ScheduleTaskOperationTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.PipelineMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
    private static final String DEVOPS_PIPELINE_NOT_DISABLE = "devops.pipeline.not.disable";
    private static final String DEVOPS_PIPELINE_STATUS_IS_DISABLE = "devops.pipeline.status.is.disable";
    private static final String DEVOPS_PIPELINE_TOKEN_IS_NULL = "devops.pipeline.token.is.null";
    private static final String DEVOPS_PIPELINE_NOT_FOUND = "devops.pipeline.not.found";

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
    @Lazy
    private PipelineAuditCfgService pipelineAuditCfgService;
    @Autowired
    @Lazy
    private PipelineChartDeployCfgService pipelineChartDeployCfgService;
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
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private PipelinePersonalTokenService pipelinePersonalTokenService;


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
    public void updateEffectVersionId(Long id, Long effectVersionId) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        pipelineDTO.setEffectVersionId(effectVersionId);
        baseUpdate(pipelineDTO);
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
    public PipelineDTO baseQueryByToken(String token) {
        Assert.notNull(token, DEVOPS_PIPELINE_TOKEN_IS_NULL);
        PipelineDTO pipelineDTO = new PipelineDTO();
        pipelineDTO.setToken(token);

        return pipelineMapper.selectOne(pipelineDTO);
    }

    @Override
    public PipelineDTO queryByTokenOrThrowE(String token) {
        PipelineDTO pipelineDTO = baseQueryByToken(token);
        if (pipelineDTO == null) {
            throw new CommonException(DEVOPS_PIPELINE_NOT_FOUND);
        }
        return pipelineDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_CREATE_PIPELINE_TIME_TASK,
            description = "创建流水线定时执行计划",
            inputSchema = "{}")
    public PipelineDTO create(Long projectId, PipelineVO pipelineVO) {

        PipelineDTO pipelineDTO = ConvertUtils.convertObject(pipelineVO, PipelineDTO.class);
        pipelineDTO.setProjectId(projectId);
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
                constructScheduleTaskDTO(projectId,
                        pipelineId,
                        scheduleTaskDTOList,
                        pipelineScheduleVO,
                        pipelineScheduleDTO,
                        ScheduleTaskOperationTypeEnum.CREATE.value());
            });
        }

        savePipelineVersion(projectId, pipelineVO, pipelineDTO);

        // 发送saga创建定时任务
        if (!CollectionUtils.isEmpty(scheduleTaskDTOList)) {
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
        }
        return pipelineDTO;
    }

    private void constructScheduleTaskDTO(Long projectId,
                                          Long pipelineId,
                                          List<ScheduleTaskDTO> scheduleTaskDTOList,
                                          CommonScheduleVO pipelineScheduleVO,
                                          PipelineScheduleDTO pipelineScheduleDTO,
                                          String operationType) {
        ScheduleTaskDTO scheduleTaskDTO = new ScheduleTaskDTO();
        scheduleTaskDTO.setName("DevopsPipelineTrigger-" + pipelineId + "-" + pipelineScheduleDTO.getName());
        scheduleTaskDTO.setCronExpression(ScheduleUtil.calculateQuartzCron(pipelineScheduleVO));
        scheduleTaskDTO.setTriggerType(CRON_TRIGGER);
        scheduleTaskDTO.setProjectId(projectId);
        scheduleTaskDTO.setOperationType(operationType);
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
        scheduleTaskDTO.setMethodCode(MiscConstants.PIPELINE_SCHEDULE_TRIGGER);
        scheduleTaskDTO.setServiceCode(serviceCode);
        scheduleTaskDTOList.add(scheduleTaskDTO);
    }

    private void savePipelineVersion(Long projectId, PipelineVO pipelineVO, PipelineDTO pipelineDTO) {
        PipelineVersionDTO pipelineVersionDTO = pipelineVersionService.createByPipelineId(pipelineDTO.getId());
        Long versionId = pipelineVersionDTO.getId();
        List<PipelineStageVO> stageList = pipelineVO.getStageList();
        if (CollectionUtils.isEmpty(stageList)) {
            throw new CommonException("devops.pipeline.stage.is.empty");
        }
        stageList.forEach(stage -> pipelineStageService.saveStage(projectId, pipelineDTO.getId(), versionId, stage));
        updateEffectVersionId(pipelineDTO.getId(), versionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (Boolean.FALSE.equals(pipelineDTO.getEnable())) {
            pipelineDTO.setEnable(true);
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineMapper, pipelineDTO, DEVOPS_ENABLE_PIPELINE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (Boolean.TRUE.equals(pipelineDTO.getEnable())) {
            pipelineDTO.setEnable(false);
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineMapper, pipelineDTO, DEVOPS_DISABLE_PIPELINE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        if (pipelineDTO == null) {
            return;
        }
        if (Boolean.TRUE.equals(pipelineDTO.getEnable())) {
            throw new CommonException(DEVOPS_PIPELINE_NOT_DISABLE);
        }
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        // 1. 删除流水线定义
        // 删除任务、任务配置
        pipelineJobService.deleteByPipelineId(id);
        pipelineAuditCfgService.deleteConfigByPipelineId(id);
        pipelineChartDeployCfgService.deleteConfigByPipelineId(id);
        // 删除阶段
        pipelineStageService.deleteByPipelineId(id);
        //删除版本
        pipelineVersionService.deleteByPipelineId(id);
        // 删除定时设置
        List<ScheduleTaskDTO> scheduleTaskDTOList = new ArrayList<>();
        List<PipelineScheduleDTO> pipelineScheduleDTOS = pipelineScheduleService.listByPipelineId(id);
        if (!CollectionUtils.isEmpty(pipelineScheduleDTOS)) {
            pipelineScheduleDTOS.forEach(pipelineScheduleDTO -> {
                CommonScheduleVO commonScheduleVO = ConvertUtils.convertObject(pipelineScheduleDTO, CommonScheduleVO.class);
                constructScheduleTaskDTO(projectId,
                        id,
                        scheduleTaskDTOList,
                        commonScheduleVO,
                        pipelineScheduleDTO,
                        ScheduleTaskOperationTypeEnum.CREATE.value());
            });
        }
        pipelineScheduleService.deleteByPipelineId(id);

        // 删除流水线
        baseDeleteById(id);

        // 2. 删除流水线执行记录
        // 删除任务记录
        pipelineJobRecordService.deleteByPipelineId(id);
        // 删除阶段记录
        pipelineStageRecordService.deleteByPipelineId(id);
        // 删除流水线记录
        pipelineRecordService.deleteByPipelineId(id);

        if (!CollectionUtils.isEmpty(scheduleTaskDTOList)) {
            transactionalProducer.apply(
                    StartSagaBuilder.newBuilder()
                            .withRefType("devops-pipeline")
                            .withRefId(id.toString())
                            .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_PIPELINE_TIME_TASK)
                            .withLevel(ResourceLevel.PROJECT)
                            .withSourceId(projectId)
                            .withPayloadAndSerialize(scheduleTaskDTOList),
                    builder -> {
                    });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long projectId, Long id, PipelineVO pipelineVO) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        pipelineDTO.setName(pipelineVO.getName());
        pipelineDTO.setToken(pipelineVO.getToken());
        pipelineDTO.setAppVersionTriggerEnable(pipelineVO.getAppVersionTriggerEnable());
        baseUpdate(pipelineDTO);

        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        // 更新或创建定时计划
        List<PipelineScheduleDTO> pipelineScheduleDTOS = pipelineScheduleService.listByPipelineId(id);
        List<PipelineScheduleVO> pipelineScheduleVOS = pipelineVO.getPipelineScheduleList();
        Map<String, PipelineScheduleDTO> pipelineScheduleDTOMap = null;
        if (CollectionUtils.isEmpty(pipelineScheduleDTOS)) {
            pipelineScheduleDTOMap = new HashMap<>();
        } else {
            pipelineScheduleDTOMap = pipelineScheduleDTOS
                    .stream()
                    .collect(Collectors.toMap(PipelineScheduleDTO::getName, Function.identity()));
        }

        List<ScheduleTaskDTO> scheduleTaskDTOList = new ArrayList<>();
        // 项目成员只能新建定时计划，无法更新、删除其他用户的定时计划
        Boolean scheduleEdit;
        if (Boolean.TRUE.equals(userDetails.getAdmin())) {
            scheduleEdit = true;
        } else {
            scheduleEdit = baseServiceClientOperator.checkIsOrgOrProjectGitlabOwner(userDetails.getUserId(), projectId);
        }
        if (Boolean.TRUE.equals(scheduleEdit)) {
            Map<String, PipelineScheduleVO> pipelineScheduleVOMap = pipelineScheduleVOS.stream().collect(Collectors.toMap(PipelineScheduleVO::getName, Function.identity()));
            for (PipelineScheduleVO pipelineScheduleVO : pipelineScheduleVOS) {// 不存在则新建
                if (pipelineScheduleDTOMap.get(pipelineScheduleVO.getName()) == null) {
                    PipelineScheduleDTO pipelineScheduleDTO = pipelineScheduleService.create(id, pipelineScheduleVO);
                    constructScheduleTaskDTO(projectId,
                            id,
                            scheduleTaskDTOList,
                            pipelineScheduleVO,
                            pipelineScheduleDTO,
                            ScheduleTaskOperationTypeEnum.CREATE.value());
                } else {
                    // 存在则更新
                    PipelineScheduleDTO oldPipelineScheduleDTO = pipelineScheduleDTOMap.get(pipelineScheduleVO.getName());
                    oldPipelineScheduleDTO.setTriggerType(pipelineScheduleVO.getTriggerType());
                    oldPipelineScheduleDTO.setWeekNumber(pipelineScheduleVO.getWeekNumber());
                    oldPipelineScheduleDTO.setStartHourOfDay(pipelineScheduleVO.getStartHourOfDay());
                    oldPipelineScheduleDTO.setEndHourOfDay(pipelineScheduleVO.getEndHourOfDay());
                    oldPipelineScheduleDTO.setPeriod(pipelineScheduleVO.getPeriod());
                    oldPipelineScheduleDTO.setExecuteTime(pipelineScheduleVO.getExecuteTime());
                    oldPipelineScheduleDTO.setToken(GenerateUUID.generateUUID());
                    pipelineScheduleService.baseUpdate(oldPipelineScheduleDTO);

                    constructScheduleTaskDTO(projectId,
                            id,
                            scheduleTaskDTOList,
                            pipelineScheduleVO,
                            oldPipelineScheduleDTO,
                            ScheduleTaskOperationTypeEnum.UPDATE.value());
                }
            }
            // 计算要删除的执行计划
            pipelineScheduleDTOS.forEach(pipelineScheduleDTO -> {
                if (pipelineScheduleVOMap.get(pipelineScheduleDTO.getName()) == null) {
                    ScheduleTaskDTO scheduleTaskDTO = new ScheduleTaskDTO();
                    scheduleTaskDTO.setProjectId(projectId);
                    scheduleTaskDTO.setName("DevopsPipelineTrigger-" + id + "-" + pipelineScheduleDTO.getName());
                    scheduleTaskDTO.setOperationType(ScheduleTaskOperationTypeEnum.DELETE.value());
                    pipelineScheduleService.deleteById(pipelineScheduleDTO.getId());
                    scheduleTaskDTOList.add(scheduleTaskDTO);
                }
            });
        } else {
            // 没有编辑权限，只能新建定时计划
            if (!CollectionUtils.isEmpty(pipelineScheduleVOS)) {
                for (PipelineScheduleVO pipelineScheduleVO : pipelineScheduleVOS) {
                    if (pipelineScheduleDTOMap.get(pipelineScheduleVO.getName()) == null) {
                        PipelineScheduleDTO pipelineScheduleDTO = pipelineScheduleService.create(id, pipelineScheduleVO);
                        constructScheduleTaskDTO(projectId,
                                id,
                                scheduleTaskDTOList,
                                pipelineScheduleVO,
                                pipelineScheduleDTO,
                                ScheduleTaskOperationTypeEnum.CREATE.value());
                    }
                }
            }
        }

        savePipelineVersion(projectId, pipelineVO, pipelineDTO);
        if (!CollectionUtils.isEmpty(scheduleTaskDTOList)) {
            transactionalProducer.apply(
                    StartSagaBuilder.newBuilder()
                            .withRefType("devops-pipeline")
                            .withRefId(id.toString())
                            .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_PIPELINE_TIME_TASK)
                            .withLevel(ResourceLevel.PROJECT)
                            .withSourceId(projectId)
                            .withPayloadAndSerialize(scheduleTaskDTOList),
                    builder -> {
                    });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineRecordDTO executeByManual(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        return execute(id, PipelineTriggerTypeEnum.MANUAL, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineRecordDTO execute(Long id,
                                     PipelineTriggerTypeEnum triggerType,
                                     Map<String, Object> params) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        if (Boolean.FALSE.equals(pipelineDTO.getEnable())) {
            throw new CommonException(DEVOPS_PIPELINE_STATUS_IS_DISABLE);
        }
        Long effectVersionId = pipelineDTO.getEffectVersionId();
        Long projectId = pipelineDTO.getProjectId();

        // 初始化流水线记录
        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO();
        pipelineRecordDTO.setTriggerType(triggerType.value());
        pipelineRecordDTO.setStatus(PipelineStatusEnum.CREATED.value());
        pipelineRecordDTO.setPipelineId(id);
        pipelineRecordDTO.setStartedDate(new Date());
        pipelineRecordDTO.setName(pipelineDTO.getName());
        if (PipelineTriggerTypeEnum.APP_VERSION.equals(triggerType)) {
            pipelineRecordDTO.setAppServiceId((Long) params.get(MiscConstants.APP_SERVICE_ID));
            pipelineRecordDTO.setAppServiceVersionId((Long) params.get(MiscConstants.APP_VERSION_ID));
        }
        pipelineRecordService.baseCreate(pipelineRecordDTO);

        Long pipelineRecordId = pipelineRecordDTO.getId();

        //初始化阶段
        Long firstStageRecordId = null;
        List<PipelineStageDTO> pipelineStageDTOS = pipelineStageService.listByVersionId(effectVersionId);
        List<PipelineStageDTO> sortedPipelineStage = pipelineStageDTOS
                .stream()
                .sorted(Comparator.comparing(PipelineStageDTO::getSequence).reversed()).collect(Collectors.toList());

        Long nextStageRecordId = null;
        for (int i = 0; i < sortedPipelineStage.size(); i++) {
            PipelineStageDTO stage = sortedPipelineStage.get(i);
            Long stageId = stage.getId();

            List<PipelineJobDTO> pipelineJobDTOS = pipelineJobService.listByStageId(stageId);
            List<PipelineJobDTO> enabledJobs = pipelineJobDTOS.stream().filter(v -> Boolean.TRUE.equals(v.getEnabled())).collect(Collectors.toList());
            // 阶段下不包含启用的任务，则跳过
            if (CollectionUtils.isEmpty(enabledJobs)) {
                continue;
            }
            PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO(id,
                    stage.getName(),
                    stageId,
                    pipelineRecordId,
                    stage.getSequence(),
                    nextStageRecordId,
                    PipelineStatusEnum.CREATED.value());
            pipelineStageRecordService.baseCreate(pipelineStageRecordDTO);

            Long stageRecordId = pipelineStageRecordDTO.getId();
            // 初始化任务记录
            for (PipelineJobDTO job : enabledJobs) {
                Long jobId = job.getId();
                PipelineJobRecordDTO pipelineJobRecordDTO = new PipelineJobRecordDTO(projectId,
                        id,
                        jobId,
                        job.getName(),
                        pipelineRecordId,
                        stageRecordId,
                        PipelineStatusEnum.CREATED.value(),
                        job.getType());
                pipelineJobRecordService.baseCreate(pipelineJobRecordDTO);
                // 记录流水线的第一个阶段信息
                if (i == sortedPipelineStage.size() - 1) {
                    firstStageRecordId = stageRecordId;
                }

                AbstractCdJobHandler handler = cdJobOperator.getHandlerOrThrowE(job.getType());
                handler.initAdditionalRecordInfo(id, job, pipelineJobRecordDTO);
            }
            // 用于设置阶段间流转顺序
            nextStageRecordId = pipelineStageRecordDTO.getId();
        }
        // 启动流水线
        pipelineRecordService.startNextStage(firstStageRecordId);

        return pipelineRecordDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineRecordDTO executeByToken(Long projectId, String token, String personalToken) {
        PipelineDTO pipelineDTO = queryByTokenOrThrowE(token);
        // 设置上下文
        PipelinePersonalTokenDTO pipelinePersonalTokenDTO = pipelinePersonalTokenService.queryByTokenOrThrowE(personalToken);
        CustomContextUtil.setUserContext(pipelinePersonalTokenDTO.getUserId());
        return execute(pipelineDTO.getId(), PipelineTriggerTypeEnum.API, null);
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
                handler.fillJobAdditionalInfo(job);
            });
            stage.setJobList(jobVOList);
        });
        List<PipelineStageVO> sortedStageList = pipelineStageVOS
                .stream()
                .sorted(Comparator.comparing(PipelineStageVO::getSequence))
                .collect(Collectors.toList());
        pipelineVO.setStageList(sortedStageList);

        pipelineVO.setPipelineScheduleList(pipelineScheduleService.listVOByPipelineId(id));
        return pipelineVO;
    }

    @Override
    public Page<PipelineHomeVO> paging(Long projectId, PageRequest pageRequest, SearchVO searchVO) {
        Page<PipelineHomeVO> pipelineVOS = PageHelper.doPage(pageRequest, () -> pipelineMapper.pagingByProjectIdAndOptions(projectId, searchVO));
        if (pipelineVOS.isEmpty()) {
            return new Page<>();
        }
        UserDTOFillUtil.fillUserInfo(pipelineVOS.getContent(), "latestPipelineRecordId", "trigger");
        pipelineVOS.getContent().forEach(pipelineHomeVO -> {
            // 添加阶段信息
            if (pipelineHomeVO.getLatestPipelineRecordId() != null) {
                List<PipelineStageRecordDTO> pipelineStageRecordDTOS = pipelineStageRecordService.listByPipelineRecordId(pipelineHomeVO.getLatestPipelineRecordId());
                List<PipelineStageRecordDTO> sortedStageRecord = pipelineStageRecordDTOS.stream().sorted(Comparator.comparing(PipelineStageRecordDTO::getSequence)).collect(Collectors.toList());
                pipelineHomeVO.setStageRecordList(sortedStageRecord);
            }

        });
        return pipelineVOS;
    }

    @Override
    public Boolean checkName(Long projectId, Long id, String name) {
        return pipelineMapper.checkName(projectId, id, name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerByAppVersion(Long appServiceId, Long appVersionId) {
        Set<Long> pipelineDTOS = pipelineMapper.listAppAssociatedPipeline(appServiceId);
        if (CollectionUtils.isEmpty(pipelineDTOS)) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put(MiscConstants.APP_SERVICE_ID, appServiceId);
        params.put(MiscConstants.APP_VERSION_ID, appVersionId);
        pipelineDTOS.forEach(pipelindId -> execute(pipelindId, PipelineTriggerTypeEnum.APP_VERSION, params));

    }

    @Override
    public List<PipelineInstanceReferenceVO> listDeployValuePipelineReference(Long projectId, Long valueId) {
        return pipelineMapper.listDeployValuePipelineReference(projectId, valueId);
    }

    @Override
    public List<PipelineInstanceReferenceVO> listChartEnvReferencePipelineInfo(Long projectId, Long envId) {
        return pipelineMapper.listChartEnvReferencePipelineInfo(projectId, envId);
    }

    @Override
    public List<PipelineInstanceReferenceVO> listAppPipelineReference(Long projectId, Long appId) {
        return pipelineMapper.listAppPipelineReference(projectId, appId);
    }

    @Override
    public int countAppServicePipelineReference(Long projectId, Long appServiceId) {
        return pipelineMapper.countAppServicePipelineReference(projectId, appServiceId);
    }
}

