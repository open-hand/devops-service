package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_PIPELINE_AUTO_DEPLOY_INSTANCE;
import static java.util.Comparator.comparing;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.eventhandler.DemoEnvSetupSagaHandler;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineTaskDTO;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.PipelineNoticeType;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.mapper.PipelineMapper;
import io.choerodon.devops.infra.mapper.PipelineRecordMapper;
import io.choerodon.devops.infra.util.*;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/4/3
 * Description:
 */
@Service
public class PipelineServiceImpl implements PipelineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineServiceImpl.class);
    private static final String MANUAL = "manual";
    private static final String AUTO = "auto";
    private static final String STAGE = "stage";
    private static final String TASK = "task";

    private static final Gson gson = new Gson();
    @Autowired
    private PipelineUserRecordRelationshipService pipelineUserRecordRelationshipService;
    @Autowired
    private PipelineRecordService pipelineRecordService;
    @Autowired
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    private PipelineStageService pipelineStageService;
    @Autowired
    private IamService iamService;
    @Autowired
    private PipelineTaskService pipelineTaskService;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private PipelineTaskRecordService pipelineTaskRecordService;
    @Autowired
    private WorkFlowServiceOperator workFlowServiceOperator;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private NotifyClient notifyClient;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private PipelineUserRelationshipService userRelationshipService;
    @Autowired
    private PipelineMapper pipelineMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;

    @Override
    public PageInfo<PipelineVO> pageByOptions(Long projectId, Boolean creator, Boolean executor, List<String> envIds, PageRequest pageRequest, String params) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        Map<String, Object> classifyParam = new HashMap<>();
        classifyParam.put("creator", creator);
        classifyParam.put("executor", executor);
        classifyParam.put("userId", DetailsHelper.getUserDetails().getUserId());
        classifyParam.put("envIds", envIds);
        PageInfo<PipelineVO> pipelineDTOS = ConvertUtils.convertPage(baseListByOptions(projectId, pageRequest, params, classifyParam), PipelineVO.class);
        PageInfo<PipelineVO> page = new PageInfo<>();
        BeanUtils.copyProperties(pipelineDTOS, page);
        page.setList(pipelineDTOS.getList().stream().peek(t -> {
            IamUserDTO iamUserDTO = iamService.queryUserByUserId(t.getCreatedBy());
            t.setCreateUserName(iamUserDTO.getLoginName());
            t.setCreateUserUrl(iamUserDTO.getImageUrl());
            t.setCreateUserRealName(iamUserDTO.getRealName());
            List<Long> pipelineEnvIds = getAllAppDeploy(t.getId()).stream().map(PipelineAppServiceDeployDTO::getEnvId).collect(Collectors.toList());
            t.setEdit(checkPipelineEnvPermission(projectDTO, pipelineEnvIds));
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public PageInfo<PipelineRecordVO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params, Boolean pendingcheck, Boolean executed, Boolean reviewed) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        Map<String, Object> classifyParam = new HashMap<>();
        classifyParam.put("executed", executed);
        classifyParam.put("reviewed", reviewed);
        classifyParam.put("pendingcheck", pendingcheck);
        classifyParam.put("userId", DetailsHelper.getUserDetails().getUserId());
        PageInfo<PipelineRecordVO> pageRecordDTOS = ConvertUtils.convertPage(
                pipelineRecordService.basePageByOptions(projectId, pipelineId, pageRequest, params, classifyParam), PipelineRecordVO.class);
        pageRecordDTOS.setList(pageRecordDTOS.getList().stream().peek(t -> {
            t.setExecute(false);
            t.setStageDTOList(ConvertUtils.convertList(pipelineStageRecordService.baseListByRecordAndStageId(projectId, t.getId()), PipelineStageRecordVO.class));
            if (t.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                for (int i = 0; i < t.getStageDTOList().size(); i++) {
                    if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                        List<PipelineTaskRecordDTO> list = pipelineTaskRecordService.baseQueryByStageRecordId(t.getStageDTOList().get(i).getId(), null);
                        if (list != null && list.size() > 0) {
                            Optional<PipelineTaskRecordDTO> taskRecordE = list.stream().filter(task -> task.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())).findFirst();
                            t.setStageName(t.getStageDTOList().get(i).getStageName());
                            t.setTaskRecordId(taskRecordE.get().getId());
                            t.setStageRecordId(t.getStageDTOList().get(i).getId());
                            t.setType(TASK);
                            t.setExecute(checkTaskTriggerPermission(taskRecordE.get().getId()));
                            break;
                        }
                    } else if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                        t.setType(STAGE);
                        t.setStageName(t.getStageDTOList().get(i - 1).getStageName());
                        t.setStageRecordId(t.getStageDTOList().get(i).getId());
                        t.setExecute(checkRecordTriggerPermission(null, t.getStageDTOList().get(i - 1).getId()));
                        break;
                    }
                }
            } else if (t.getStatus().equals(WorkFlowStatus.STOP.toValue())) {
                t.setType(STAGE);
                for (int i = 0; i < t.getStageDTOList().size(); i++) {
                    if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.STOP.toValue())) {
                        List<PipelineTaskRecordDTO> recordEList = pipelineTaskRecordService.baseQueryByStageRecordId(t.getStageDTOList().get(i).getId(), null);
                        Optional<PipelineTaskRecordDTO> optional = recordEList.stream().filter(recordE -> recordE.getStatus().equals(WorkFlowStatus.STOP.toValue())).findFirst();
                        if (optional.isPresent()) {
                            t.setType(TASK);
                            t.setTaskRecordId(optional.get().getId());
                        }
                        break;
                    } else if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                        t.setStageRecordId(t.getStageDTOList().get(i).getId());
                        break;
                    }
                }
            } else if (t.getStatus().equals(WorkFlowStatus.FAILED.toValue())) {
                if (t.getTriggerType().equals(AUTO) || checkRecordTriggerPermission(t.getId(), null)) {
                    List<Long> pipelineEnvIds = pipelineTaskRecordService.baseQueryAllAutoTaskRecord(t.getId()).stream().map(PipelineTaskRecordDTO::getEnvId).collect(Collectors.toList());
                    t.setExecute(checkPipelineEnvPermission(projectDTO, pipelineEnvIds));
                }
            }
        }).collect(Collectors.toList()));
        return pageRecordDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineReqVO create(Long projectId, PipelineReqVO pipelineReqVO) {
        PipelineDTO pipelineE = ConvertUtils.convertObject(pipelineReqVO, PipelineDTO.class);
        pipelineE.setProjectId(projectId);
        checkName(projectId, pipelineReqVO.getName());
        pipelineE = baseCreate(projectId, pipelineE);
        createUserRel(pipelineReqVO.getPipelineUserRelDTOS(), pipelineE.getId(), null, null);

        Long pipelineId = pipelineE.getId();
        List<PipelineStageDTO> pipelineStageES = ConvertUtils.convertList(pipelineReqVO.getPipelineStageVOS(), PipelineStageDTO.class)
                .stream().map(t -> {
                    t.setPipelineId(pipelineId);
                    t.setProjectId(projectId);
                    return pipelineStageService.baseCreate(t);
                }).collect(Collectors.toList());
        for (int i = 0; i < pipelineStageES.size(); i++) {
            Long stageId = pipelineStageES.get(i).getId();
            createUserRel(pipelineReqVO.getPipelineStageVOS().get(i).getStageUserRelDTOS(), null, stageId, null);
            List<PipelineTaskVO> taskDTOList = pipelineReqVO.getPipelineStageVOS().get(i).getPipelineTaskVOS();
            if (taskDTOList != null && taskDTOList.size() > 0) {
                taskDTOList.forEach(t -> createPipelineTask(t, projectId, stageId));
            }
        }
        return pipelineReqVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineReqVO update(Long projectId, PipelineReqVO pipelineReqVO) {
        pipelineReqVO.setProjectId(projectId);
        PipelineDTO pipelineE = ConvertUtils.convertObject(pipelineReqVO, PipelineDTO.class);
        pipelineE = baseUpdate(projectId, pipelineE);
        updateUserRel(pipelineReqVO.getPipelineUserRelDTOS(), pipelineE.getId(), null, null);
        Long pipelineId = pipelineE.getId();
        removeStages(pipelineReqVO.getPipelineStageVOS(), pipelineId);

        for (int i = 0; i < pipelineReqVO.getPipelineStageVOS().size(); i++) {
            PipelineStageDTO pipelineStageDTO = createOrUpdateStage(pipelineReqVO.getPipelineStageVOS().get(i), pipelineId, projectId);
            List<PipelineTaskVO> taskDTOList = pipelineReqVO.getPipelineStageVOS().get(i).getPipelineTaskVOS();
            if (taskDTOList != null) {
                removeTasks(taskDTOList, pipelineStageDTO.getId());
                taskDTOList.stream().filter(Objects::nonNull).forEach(t -> {
                    createOrUpdateTask(t, pipelineStageDTO.getId(), projectId);
                });
            }
        }
        pipelineRecordService.baseUpdateWithEdited(pipelineId);
        return pipelineReqVO;
    }

    @Override
    public PipelineVO updateIsEnabled(Long projectId, Long pipelineId, Integer isEnabled) {
        return ConvertUtils.convertObject(baseUpdateWithEnabled(pipelineId, isEnabled), PipelineVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId, Long pipelineId) {
        pipelineRecordService.baseQueryByPipelineId(pipelineId).forEach(t -> {
            t.setStatus(WorkFlowStatus.DELETED.toValue());
            pipelineRecordService.baseUpdate(t);
        });
        userRelationshipService.baseListByOptions(pipelineId, null, null).forEach(t -> userRelationshipService.baseDelete(t));
        pipelineStageService.baseListByPipelineId(pipelineId).forEach(stage -> {
            pipelineTaskService.baseQueryTaskByStageId(stage.getId()).forEach(task -> {
                if (task.getAppDeployId() != null) {
                    pipelineAppDeployService.baseDeleteById(task.getAppDeployId());
                }
                pipelineTaskService.baseDeleteTaskById(task.getId());
                userRelationshipService.baseListByOptions(null, null, task.getId()).forEach(t -> userRelationshipService.baseDelete(t));
            });
            pipelineStageService.baseDelete(stage.getId());
            userRelationshipService.baseListByOptions(null, stage.getId(), null).forEach(t -> userRelationshipService.baseDelete(t));
        });
        baseDelete(pipelineId);
    }

    @Override
    public PipelineReqVO queryById(Long projectId, Long pipelineId) {
        PipelineReqVO pipelineReqVO = ConvertUtils.convertObject(baseQueryById(pipelineId), PipelineReqVO.class);
        pipelineReqVO.setPipelineUserRelDTOS(userRelationshipService.baseListByOptions(pipelineId, null, null).stream().map(PipelineUserRelationshipDTO::getUserId).collect(Collectors.toList()));
        List<PipelineStageVO> pipelineStageES = ConvertUtils.convertList(pipelineStageService.baseListByPipelineId(pipelineId), PipelineStageVO.class);
        pipelineStageES = pipelineStageES.stream()
                .peek(stage -> {
                    List<PipelineTaskVO> pipelineTaskVOS = ConvertUtils.convertList(pipelineTaskService.baseQueryTaskByStageId(stage.getId()), PipelineTaskVO.class);
                    pipelineTaskVOS = pipelineTaskVOS.stream().peek(task -> {
                        if (task.getAppDeployId() != null) {
                            task.setPipelineAppServiceDeployVO(ConvertUtils.convertObject(baseQueryById(task.getAppDeployId()), PipelineAppServiceDeployVO.class));
                        } else {
                            task.setTaskUserRelDTOS(userRelationshipService.baseListByOptions(null, null, task.getId()).stream().map(PipelineUserRelationshipDTO::getUserId).collect(Collectors.toList()));
                        }
                    }).collect(Collectors.toList());
                    stage.setPipelineTaskVOS(pipelineTaskVOS);
                    stage.setStageUserRelDTOS(userRelationshipService.baseListByOptions(null, stage.getId(), null).stream().map(PipelineUserRelationshipDTO::getUserId).collect(Collectors.toList()));
                }).collect(Collectors.toList());
        pipelineReqVO.setPipelineStageVOS(pipelineStageES);
        return pipelineReqVO;
    }

    @Override
    public void execute(Long projectId, Long pipelineId) {
        PipelineDTO pipelineDTO = baseQueryById(pipelineId);
        if (AUTO.equals(pipelineDTO.getTriggerType()) || !checkTriggerPermission(pipelineId)) {
            throw new CommonException("error.permission.trigger.pipeline");
        }
        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO(pipelineId, pipelineDTO.getTriggerType(), projectId, WorkFlowStatus.RUNNING.toValue(), pipelineDTO.getName());
        pipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
        if (pipelineDTO.getTriggerType().equals(MANUAL)) {
            List<PipelineUserRelationshipDTO> taskRelEList = userRelationshipService.baseListByOptions(pipelineId, null, null);
            pipelineRecordDTO.setAuditUser(StringUtils.join(taskRelEList.stream().map(PipelineUserRelationshipDTO::getUserId).toArray(), ","));
        }


        pipelineRecordDTO = pipelineRecordService.baseCreate(pipelineRecordDTO);

        //插入部署记录
        createDeployRecord(pipelineRecordDTO);


        PipelineUserRecordRelationshipDTO userRecordRelationshipDTO = new PipelineUserRecordRelationshipDTO();
        userRecordRelationshipDTO.setPipelineRecordId(pipelineRecordDTO.getId());
        userRecordRelationshipDTO.setUserId(DetailsHelper.getUserDetails().getUserId());
        pipelineUserRecordRelationshipService.baseCreate(userRecordRelationshipDTO);
        io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO devopsPipelineDTO = createWorkFlowDTO(pipelineRecordDTO.getId(), pipelineId, pipelineRecordDTO.getBusinessKey());
        pipelineRecordDTO.setBpmDefinition(gson.toJson(devopsPipelineDTO));
        pipelineRecordService.baseUpdate(pipelineRecordDTO);
        try {
            CustomUserDetails details = DetailsHelper.getUserDetails();
            createWorkFlow(projectId, devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
            updateFirstStage(pipelineRecordDTO.getId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            sendFailedSiteMessage(pipelineRecordDTO.getId(), GitUserNameUtil.getUserId().longValue());
            pipelineRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
            pipelineRecordDTO.setErrorInfo(e.getMessage());
            pipelineRecordService.baseUpdate(pipelineRecordDTO);
        }
    }

    @Override
    public void batchExecute(Long projectId, Long[] pipelineIds) {
        for (Long pipelineId : pipelineIds) {
            execute(projectId, pipelineId);
        }
    }

    @Override
    @Saga(code = DEVOPS_PIPELINE_AUTO_DEPLOY_INSTANCE,
            description = "创建流水线自动部署实例", inputSchema = "{}")
    public void autoDeploy(Long stageRecordId, Long taskRecordId) {
        LOGGER.info("autoDeploy:stageRecordId: {} taskId: {}", stageRecordId, taskRecordId);
        //获取数据
        PipelineTaskRecordDTO taskRecordDTO = pipelineTaskRecordService.baseQueryRecordById(taskRecordId);
        Long pipelineRecordId = pipelineStageRecordService.baseQueryById(stageRecordId).getPipelineRecordId();
        CustomContextUtil.setUserId(taskRecordDTO.getCreatedBy());
        AppServiceVersionDTO appServiceServiceE = getDeployVersion(pipelineRecordId, stageRecordId, taskRecordDTO);
        //保存记录
        taskRecordDTO.setStatus(WorkFlowStatus.RUNNING.toValue());
        taskRecordDTO.setName(taskRecordDTO.getName());
        taskRecordDTO.setVersionId(appServiceServiceE.getId());
        taskRecordDTO = pipelineTaskRecordService.baseCreateOrUpdateRecord(taskRecordDTO);
        try {
            AppServiceInstanceDTO instanceE = appServiceInstanceService.baseQueryByCodeAndEnv(taskRecordDTO.getInstanceName(), taskRecordDTO.getEnvId());
            Long instanceId = instanceE == null ? null : instanceE.getId();
            String type = instanceId == null ? CommandType.CREATE.getType() : CommandType.UPDATE.getType();
            AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO(appServiceServiceE.getId(), taskRecordDTO.getEnvId(),
                    devopsDeployValueService.baseQueryById(taskRecordDTO.getValueId()).getValue(), taskRecordDTO.getAppServiceId(), type, instanceId,
                    taskRecordDTO.getInstanceName(), taskRecordDTO.getId(), taskRecordDTO.getValueId());
            if (type.equals(CommandType.UPDATE.getType())) {
                AppServiceInstanceDTO oldapplicationServiceInstanceDTO = appServiceInstanceService.baseQuery(appServiceDeployVO.getInstanceId());
                DevopsEnvCommandDTO olddevopsEnvCommandDTO = devopsEnvCommandService.baseQuery(oldapplicationServiceInstanceDTO.getCommandId());
                if (olddevopsEnvCommandDTO.getObjectVersionId().equals(appServiceDeployVO.getAppServiceVersionId())) {
                    String oldValue = appServiceInstanceService.baseQueryValueByInstanceId(appServiceDeployVO.getInstanceId());
                    if (appServiceDeployVO.getValues().trim().equals(oldValue.trim())) {
                        appServiceDeployVO.setIsNotChange(true);
                    }
                }
            }

            String input = gson.toJson(appServiceDeployVO);
            producer.apply(
                    StartSagaBuilder.newBuilder()
                            .withJson(input)
                            .withRefType("env")
                            .withRefId(taskRecordDTO.getEnvId().toString())
                            .withLevel(ResourceLevel.PROJECT)
                            .withSourceId(taskRecordDTO.getProjectId()),
                    builder -> {
                    });
        } catch (Exception e) {
            sendFailedSiteMessage(pipelineRecordId, GitUserNameUtil.getUserId().longValue());
            PipelineStageRecordDTO stageRecordDTO = pipelineStageRecordService.baseQueryById(stageRecordId);
            long time = System.currentTimeMillis() - TypeUtil.objToLong(stageRecordDTO.getExecutionTime());
            stageRecordDTO.setExecutionTime(Long.toString(time));
            pipelineStageRecordService.baseUpdate(stageRecordDTO);
            setPipelineFailed(pipelineRecordId, stageRecordId, taskRecordDTO, e.getMessage());
            throw new CommonException("error.create.pipeline.auto.deploy.instance", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PipelineUserVO> audit(Long projectId, PipelineUserRecordRelationshipVO recordRelDTO) {
        List<PipelineUserVO> userDTOS = new ArrayList<>();
        String status;
        PipelineRecordDTO pipelineRecordE = pipelineRecordService.baseQueryById(recordRelDTO.getPipelineRecordId());
        PipelineStageRecordDTO stageRecordDTO = pipelineStageRecordService.baseQueryById(recordRelDTO.getStageRecordId());
        String auditUser = "";
        if ("task".equals(recordRelDTO.getType())) {
            auditUser = pipelineTaskRecordService.baseQueryRecordById(recordRelDTO.getTaskRecordId()).getAuditUser();
        } else {
            Optional<PipelineStageRecordDTO> optional = pipelineStageRecordService.baseListByRecordAndStageId(recordRelDTO.getPipelineRecordId(), null).stream().filter(t -> t.getId() < recordRelDTO.getStageRecordId()).findFirst();
            auditUser = optional.isPresent() ? optional.get().getAuditUser() : auditUser;
        }
        status = getAuditResult(recordRelDTO, pipelineRecordE, auditUser, stageRecordDTO.getStageName());
        PipelineUserRecordRelationshipDTO userRelE = new PipelineUserRecordRelationshipDTO();
        userRelE.setUserId(DetailsHelper.getUserDetails().getUserId());
        switch (recordRelDTO.getType()) {
            case TASK: {
                userRelE.setTaskRecordId(recordRelDTO.getTaskRecordId());
                pipelineUserRecordRelationshipService.baseCreate(userRelE);
                PipelineTaskRecordDTO taskRecordDTO = pipelineTaskRecordService.baseQueryRecordById(recordRelDTO.getTaskRecordId());
                if (status.equals(WorkFlowStatus.SUCCESS.toValue())) {
                    //判断会签是否全部通过
                    if (taskRecordDTO.getIsCountersigned() == 1) {
                        if (!checkCouAllApprove(userDTOS, taskRecordDTO.getTaskId(), recordRelDTO.getTaskRecordId())) {
                            break;
                        }
                    } else {
                        sendAuditSiteMassage(PipelineNoticeType.PIPELINEPASS.toValue(), auditUser, recordRelDTO.getPipelineRecordId(), stageRecordDTO.getStageName());
                    }
                    updateStatus(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId(), WorkFlowStatus.RUNNING.toValue(), null);
                    startNextTask(taskRecordDTO.getId(), recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                } else {
                    Long time = System.currentTimeMillis() - TypeUtil.objToLong(stageRecordDTO.getExecutionTime());
                    stageRecordDTO.setStatus(status);
                    stageRecordDTO.setExecutionTime(time.toString());
                    pipelineStageRecordService.baseCreateOrUpdate(stageRecordDTO);
                    updateStatus(recordRelDTO.getPipelineRecordId(), null, status, null);
                }
                taskRecordDTO.setStatus(status);
                pipelineTaskRecordService.baseCreateOrUpdateRecord(taskRecordDTO);
                break;
            }
            case STAGE: {
                userRelE.setStageRecordId(recordRelDTO.getStageRecordId());
                pipelineUserRecordRelationshipService.baseCreate(userRelE);
                if (status.equals(WorkFlowStatus.RUNNING.toValue())) {
                    updateStatus(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId(), status, null);
                    if (!isEmptyStage(recordRelDTO.getStageRecordId())) {
                        List<PipelineTaskRecordDTO> taskRecordEList = pipelineTaskRecordService.baseQueryByStageRecordId(recordRelDTO.getStageRecordId(), null);
                        PipelineTaskRecordDTO taskRecordDTO = taskRecordEList.get(0);
                        if (MANUAL.equals(taskRecordDTO.getTaskType())) {
                            startNextTask(taskRecordDTO, recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                        }
                    } else {
                        startEmptyStage(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                    }
                    sendAuditSiteMassage(PipelineNoticeType.PIPELINEPASS.toValue(), auditUser, recordRelDTO.getPipelineRecordId(), stageRecordDTO.getStageName());
                } else {
                    updateStatus(recordRelDTO.getPipelineRecordId(), null, status, null);
                }
                break;
            }
            default: {
                break;
            }
        }
        return userDTOS;
    }

    @Override
    public CheckAuditVO checkAudit(Long projectId, PipelineUserRecordRelationshipVO recordRelDTO) {
        CheckAuditVO auditDTO = new CheckAuditVO();
        switch (recordRelDTO.getType()) {
            case TASK: {
                PipelineTaskRecordDTO pipelineTaskRecordDTO = pipelineTaskRecordService.baseQueryRecordById(recordRelDTO.getTaskRecordId());
                if (!pipelineTaskRecordDTO.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                    if (pipelineTaskRecordDTO.getIsCountersigned() == 1) {
                        auditDTO.setIsCountersigned(1);
                    } else {
                        auditDTO.setIsCountersigned(0);
                    }
                    auditDTO.setUserName(iamService.queryUserByUserId(
                            pipelineUserRecordRelationshipService.baseListByOptions(null, null, pipelineTaskRecordDTO.getId()).get(0).getUserId())
                            .getRealName());
                }
                break;
            }
            case STAGE: {
                PipelineStageRecordDTO stageRecordDTO = pipelineStageRecordService.baseQueryById(recordRelDTO.getStageRecordId());
                if (!stageRecordDTO.getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                    auditDTO.setIsCountersigned(0);
                    auditDTO.setUserName(iamService.queryUserByUserId(
                            pipelineUserRecordRelationshipService.baseListByOptions(null, stageRecordDTO.getId(), null).get(0).getUserId())
                            .getRealName());
                }
                break;
            }
            default:
                break;
        }
        return auditDTO;
    }

    /**
     * 检测是否满足部署条件
     *
     * @param pipelineId
     * @return
     */
    @Override
    public PipelineCheckDeployVO checkDeploy(Long projectId, Long pipelineId) {
        PipelineDTO pipelineE = baseQueryById(pipelineId);
        if (pipelineE.getIsEnabled() == 0) {
            throw new CommonException("error.pipeline.check.deploy");
        }
        Long userId = pipelineE.getTriggerType().equals(AUTO) ? pipelineE.getLastUpdatedBy() : TypeUtil.objToLong(GitUserNameUtil.getUserId());
        PipelineCheckDeployVO checkDeployDTO = new PipelineCheckDeployVO();
        checkDeployDTO.setPermission(true);
        checkDeployDTO.setVersions(true);
        List<PipelineAppServiceDeployDTO> allAppDeploys = getAllAppDeploy(pipelineId);
        if (allAppDeploys.isEmpty()) {
            return checkDeployDTO;
        }
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        if (!iamService.isProjectOwner(userId, projectDTO)) {
            List<Long> envIds = devopsEnvUserPermissionService
                    .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                    .filter(DevopsEnvUserPermissionDTO::getPermitted)
                    .map(DevopsEnvUserPermissionDTO::getEnvId).collect(Collectors.toList());
            for (PipelineAppServiceDeployDTO appDeployDTO : allAppDeploys) {
                if (!envIds.contains(appDeployDTO.getEnvId())) {
                    checkDeployDTO.setPermission(false);
                    checkDeployDTO.setEnvName(appDeployDTO.getEnvName());
                    return checkDeployDTO;
                }
            }
        }
        for (PipelineAppServiceDeployDTO appDeployDTO : allAppDeploys) {
            if (appDeployDTO.getCreationDate().getTime() > appServiceVersionService.baseQueryNewestVersion(appDeployDTO.getAppServiceId()).getCreationDate().getTime()) {
                checkDeployDTO.setVersions(false);
                break;
            } else {
                if ((appDeployDTO.getTriggerVersion() != null) && !appDeployDTO.getTriggerVersion().isEmpty()) {
                    List<String> list = Arrays.asList(appDeployDTO.getTriggerVersion().split(","));
                    List<AppServiceVersionDTO> versionES = appServiceVersionService.baseListByAppServiceId(appDeployDTO.getAppServiceId())
                            .stream()
                            .filter(versionE -> versionE.getCreationDate().getTime() > appDeployDTO.getCreationDate().getTime())
                            .collect(Collectors.toList());

                    int i = 0;
                    for (AppServiceVersionDTO versionE : versionES) {
                        Optional<String> branch = list.stream().filter(t -> versionE.getVersion().contains(t)).findFirst();
                        if (!branch.isPresent()) {
                            i++;
                            if (i == versionES.size()) {
                                checkDeployDTO.setVersions(false);
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return checkDeployDTO;
    }

    /**
     * 准备workflow创建实例所需数据
     * 为此workflow下所有stage创建记录
     */
    @Override
    public io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO createWorkFlowDTO(Long pipelineRecordId, Long pipelineId, String businessKey) {
        io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO devopsPipelineDTO = new io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO();
        devopsPipelineDTO.setPipelineRecordId(pipelineRecordId);
        devopsPipelineDTO.setBusinessKey(businessKey);
        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
        List<PipelineStageDTO> stageES = pipelineStageService.baseListByPipelineId(pipelineId);
        for (int i = 0; i < stageES.size(); i++) {
            PipelineStageDTO stageE = stageES.get(i);
            List<PipelineUserRelationshipDTO> stageRelEList = userRelationshipService.baseListByOptions(null, stageE.getId(), null);
            PipelineStageRecordDTO recordE = createStageRecord(stageE, pipelineRecordId, stageRelEList);
            DevopsPipelineStageDTO devopsPipelineStageDTO = new DevopsPipelineStageDTO();
            devopsPipelineStageDTO.setStageRecordId(recordE.getId());
            devopsPipelineStageDTO.setParallel(stageE.getIsParallel() != null && stageE.getIsParallel() == 1);
            if (i != stageES.size() - 1) {
                devopsPipelineStageDTO.setNextStageTriggerType(stageE.getTriggerType());
            }
            devopsPipelineStageDTO.setMultiAssign(stageRelEList.size() > 1);
            devopsPipelineStageDTO.setUsernames(stageRelEList.stream()
                    .map(userRelE -> userRelE.getUserId().toString())
                    .collect(Collectors.toList()));

            List<DevopsPipelineTaskDTO> devopsPipelineTaskDTOS = new ArrayList<>();
            Long stageRecordId = recordE.getId();
            pipelineTaskService.baseQueryTaskByStageId(stageE.getId()).forEach(taskE -> {
                List<PipelineUserRelationshipDTO> taskUserRels = userRelationshipService.baseListByOptions(null, null, taskE.getId());
                PipelineTaskRecordDTO taskRecordDTO = createTaskRecordDTO(taskE, stageRecordId, taskUserRels);
                DevopsPipelineTaskDTO devopsPipelineTaskDTO = new DevopsPipelineTaskDTO();
                devopsPipelineTaskDTO.setTaskRecordId(taskRecordDTO.getId());
                devopsPipelineTaskDTO.setTaskName(taskE.getName());
                devopsPipelineTaskDTO.setTaskType(taskE.getType());
                devopsPipelineTaskDTO.setMultiAssign(taskUserRels.size() > 1);
                devopsPipelineTaskDTO.setUsernames(taskUserRels.stream().map(userRelE -> userRelE.getUserId().toString()).collect(Collectors.toList()));
                devopsPipelineTaskDTO.setTaskRecordId(taskRecordDTO.getId());
                if (taskE.getIsCountersigned() != null) {
                    devopsPipelineTaskDTO.setSign(taskE.getIsCountersigned().longValue());
                }
                devopsPipelineTaskDTOS.add(devopsPipelineTaskDTO);

            });
            devopsPipelineStageDTO.setTasks(devopsPipelineTaskDTOS);
            devopsPipelineStageDTOS.add(devopsPipelineStageDTO);
        }
        pipelineStageService.baseListByPipelineId(pipelineId).forEach(t -> {


        });
        devopsPipelineDTO.setStages(devopsPipelineStageDTOS);
        return devopsPipelineDTO;
    }

    @Override
    public String getAppDeployStatus(Long stageRecordId, Long taskRecordId) {
        PipelineTaskRecordDTO taskRecordE = pipelineTaskRecordService.baseQueryRecordById(taskRecordId);
        if (taskRecordE != null) {
            return taskRecordE.getStatus();
        }
        return WorkFlowStatus.FAILED.toValue();
    }

    @Override
    public void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long taskRecordId, Boolean status) {
        LOGGER.info("setAppDeployStatus:pipelineRecordId: {} stageRecordId: {} taskId: {}", pipelineRecordId, stageRecordId, taskRecordId);
        PipelineRecordDTO pipelineRecordE = pipelineRecordService.baseQueryById(pipelineRecordId);
        PipelineStageRecordDTO stageRecordDTO = pipelineStageRecordService.baseQueryById(stageRecordId);
        if (status) {
            if (stageRecordDTO.getIsParallel() == 1) {
                List<PipelineTaskRecordDTO> taskRecordEList = pipelineTaskRecordService.baseQueryByStageRecordId(stageRecordId, null);
                List<PipelineTaskRecordDTO> taskSuccessRecordList = taskRecordEList.stream().filter(t -> t.getStatus().equals(WorkFlowStatus.SUCCESS.toValue())).collect(Collectors.toList());
                if (taskRecordEList.size() == taskSuccessRecordList.size() && !pipelineRecordE.getStatus().equals(WorkFlowStatus.FAILED.toValue())) {
                    startNextTask(taskRecordId, pipelineRecordId, stageRecordId);
                }
            } else {
                startNextTask(taskRecordId, pipelineRecordId, stageRecordId);
            }
        } else {
            if (stageRecordDTO.getIsParallel() == 1) {
                List<PipelineTaskRecordDTO> taskRecordEList = pipelineTaskRecordService.baseQueryByStageRecordId(stageRecordId, null);
                List<PipelineTaskRecordDTO> taskSuccessRecordList = taskRecordEList.stream().filter(t -> t.getStatus().equals(WorkFlowStatus.SUCCESS.toValue())).collect(Collectors.toList());
                List<PipelineTaskRecordDTO> taskFailedRecordList = taskRecordEList.stream().filter(t -> t.getStatus().equals(WorkFlowStatus.FAILED.toValue())).collect(Collectors.toList());
                if (taskRecordEList.size() == (taskSuccessRecordList.size() + taskFailedRecordList.size())) {
                    workFlowServiceOperator.stopInstance(pipelineRecordE.getProjectId(), pipelineRecordE.getBusinessKey());
                }
            } else {
                workFlowServiceOperator.stopInstance(pipelineRecordE.getProjectId(), pipelineRecordE.getBusinessKey());
            }
        }
    }

    @Override
    public PipelineRecordReqVO getRecordById(Long projectId, Long pipelineRecordId) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        PipelineRecordReqVO recordReqDTO = new PipelineRecordReqVO();
        PipelineRecordDTO pipelineRecordE = pipelineRecordService.baseQueryById(pipelineRecordId);
        BeanUtils.copyProperties(pipelineRecordE, recordReqDTO);
        IamUserDTO iamUserDTO = getTriggerUser(pipelineRecordId, null);
        if (iamUserDTO != null) {
            recordReqDTO.setUserDTO(iamUserDTO);
        }
        List<PipelineStageRecordVO> recordDTOList = ConvertUtils.convertList(pipelineStageRecordService.baseListByRecordAndStageId(pipelineRecordId, null), PipelineStageRecordVO.class);
        for (int i = 0; i < recordDTOList.size(); i++) {
            PipelineStageRecordVO stageRecordDTO = recordDTOList.get(i);
            if (stageRecordDTO.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue()) || stageRecordDTO.getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue()) || stageRecordDTO.getStatus().equals(WorkFlowStatus.RUNNING.toValue())) {
                recordDTOList.get(i).setExecutionTime(null);
            }
            if (stageRecordDTO.getTriggerType().equals(MANUAL)) {
                if (getNextStage(stageRecordDTO.getId()) != null) {
                    stageRecordDTO.setUserDTOS(getStageAuditUsers(recordDTOList.get(i).getStageId(), recordDTOList.get(i + 1).getId()));
                    if (recordDTOList.get(i).getStatus().equals(WorkFlowStatus.SUCCESS.toValue()) && recordDTOList.get(i + 1).getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                        recordReqDTO.setType(STAGE);
                        stageRecordDTO.setIndex(true);
                        recordReqDTO.setStageRecordId(recordDTOList.get(i + 1).getId());
                        recordReqDTO.setStageName(stageRecordDTO.getStageName());
                        recordReqDTO.setExecute(checkRecordTriggerPermission(null, stageRecordDTO.getId()));
                    }
                }
            }
            List<PipelineTaskRecordVO> taskRecordDTOS = pipelineTaskRecordService.baseQueryByStageRecordId(stageRecordDTO.getId(), null).stream().map(r -> {
                PipelineTaskRecordVO taskRecordDTO = ConvertUtils.convertObject(r, PipelineTaskRecordVO.class);
                if (taskRecordDTO.getTaskType().equals(MANUAL)) {
                    taskRecordDTO.setUserDTOList(getTaskAuditUsers(r.getAuditUser(), r.getId()));
                    if (r.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                        recordReqDTO.setType(TASK);
                        recordReqDTO.setStageRecordId(r.getStageRecordId());
                        recordReqDTO.setTaskRecordId(r.getId());
                        recordReqDTO.setStageName(pipelineStageRecordService.baseQueryById(r.getStageRecordId()).getStageName());
                        recordReqDTO.setExecute(checkTaskTriggerPermission(r.getId()));
                    }
                } else {
                    taskRecordDTO.setEnvPermission(getTaskEnvPermission(projectDTO));
                }
                return taskRecordDTO;
            }).collect(Collectors.toList());
            stageRecordDTO.setTaskRecordDTOS(taskRecordDTOS);
        }
        if (pipelineRecordE.getStatus().equals(WorkFlowStatus.FAILED.toValue())) {
            List<Long> pipelineEnvIds = pipelineTaskRecordService.baseQueryAllAutoTaskRecord(pipelineRecordId)
                    .stream()
                    .map(PipelineTaskRecordDTO::getEnvId).collect(Collectors.toList());
            if (checkRecordTriggerPermission(pipelineRecordE.getId(), null) && checkPipelineEnvPermission(projectDTO, pipelineEnvIds)) {
                recordReqDTO.setExecute(true);
            }
        }
        recordReqDTO.setStageRecordDTOS(recordDTOList);
        return recordReqDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(Long projectId, Long pipelineRecordId) {
        PipelineRecordDTO pipelineRecordE = pipelineRecordService.baseQueryById(pipelineRecordId);
        String bpmDefinition = pipelineRecordE.getBpmDefinition();
        io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO devopsPipelineDTO = gson.fromJson(bpmDefinition, io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO.class);
        String uuid = GenerateUUID.generateUUID();
        devopsPipelineDTO.setBusinessKey(uuid);
        CustomUserDetails details = DetailsHelper.getUserDetails();
        createWorkFlow(projectId, devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
        //清空之前数据
        pipelineRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        pipelineRecordE.setBusinessKey(uuid);
        pipelineRecordE.setErrorInfo("");
        pipelineRecordService.baseUpdate(pipelineRecordE);
        pipelineStageRecordService.baseListByRecordAndStageId(pipelineRecordId, null).forEach(t -> {

            t.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
            t.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis()));
            pipelineStageRecordService.baseUpdate(t);
            pipelineTaskRecordService.baseQueryByStageRecordId(t.getId(), null).forEach(taskRecordE -> {
                taskRecordE.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
                pipelineTaskRecordService.baseCreateOrUpdateRecord(taskRecordE);
                if (taskRecordE.getTaskType().equals(MANUAL)) {
                    pipelineUserRecordRelationshipService.baseDelete(pipelineRecordId, t.getId(), taskRecordE.getId());
                }
            });
        });
        //更新第一阶段
        if (pipelineRecordE.getTriggerType().equals(MANUAL)) {
            updateFirstStage(pipelineRecordId);
        }
    }

    @Override
    public List<PipelineRecordListVO> queryByPipelineId(Long pipelineId) {
        return pipelineRecordService.baseQueryByPipelineId(pipelineId).stream()
                .sorted(comparing(PipelineRecordDTO::getId).reversed())
                .map(t -> new PipelineRecordListVO(t.getId(), t.getCreationDate())).collect(Collectors.toList());
    }

    @Override
    public void checkName(Long projectId, String name) {
        baseCheckName(projectId, name);
    }

    @Override
    public List<PipelineVO> listPipelineDTO(Long projectId) {
        return ConvertUtils.convertList(baseQueryByProjectId(projectId), PipelineVO.class);
    }

    @Override
    public List<UserVO> listAllUsers(Long projectId) {
        return ConvertUtils.convertList(iamService.getAllMember(projectId), UserVO.class);
    }

    @Override
    public void updateStatus(Long pipelineRecordId, Long stageRecordId, String status, String errorInfo) {
        if (pipelineRecordId != null) {
            PipelineRecordDTO pipelineRecordE = new PipelineRecordDTO();
            pipelineRecordE.setId(pipelineRecordId);
            pipelineRecordE.setStatus(status);
            pipelineRecordE.setErrorInfo(errorInfo);
            pipelineRecordService.baseUpdate(pipelineRecordE);
        }
        if (stageRecordId != null) {
            PipelineStageRecordDTO stageRecordE = new PipelineStageRecordDTO();
            stageRecordE.setId(stageRecordId);
            stageRecordE.setStatus(status);
            pipelineStageRecordService.baseCreateOrUpdate(stageRecordE);
        }
    }

    /**
     * 执行自动部署流水线
     */
    @Override
    public void executeAutoDeploy(Long pipelineId) {
        PipelineDTO pipelineDTO = baseQueryById(pipelineId);
        CustomContextUtil.setUserId(pipelineDTO.getCreatedBy());
        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO(pipelineId, pipelineDTO.getTriggerType(), pipelineDTO.getProjectId(), WorkFlowStatus.RUNNING.toValue(), pipelineDTO.getName());
        String uuid = GenerateUUID.generateUUID();
        pipelineRecordDTO.setBusinessKey(uuid);
        pipelineRecordDTO = pipelineRecordService.baseCreate(pipelineRecordDTO);


        //插入部署记录
        createDeployRecord(pipelineRecordDTO);

        DevopsPipelineDTO devopsPipelineDTO = createWorkFlowDTO(pipelineRecordDTO.getId(), pipelineId, uuid);
        pipelineRecordDTO.setBpmDefinition(gson.toJson(devopsPipelineDTO));
        pipelineRecordDTO = pipelineRecordService.baseUpdate(pipelineRecordDTO);
        try {
            CustomUserDetails details = DetailsHelper.getUserDetails();
            createWorkFlow(pipelineDTO.getProjectId(), devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
            List<PipelineStageRecordDTO> stageRecordES = pipelineStageRecordService.baseListByRecordAndStageId(pipelineRecordDTO.getId(), null);
            if (stageRecordES != null && stageRecordES.size() > 0) {
                PipelineStageRecordDTO stageRecordE = stageRecordES.get(0);
                stageRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
                stageRecordE.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis()));
                pipelineStageRecordService.baseCreateOrUpdate(stageRecordE);
            }
        } catch (Exception e) {
            pipelineRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
            pipelineRecordService.baseUpdate(pipelineRecordDTO);
            throw new CommonException(e);
        }
    }

    private void createDeployRecord(PipelineRecordDTO pipelineRecordDTO) {


        List<PipelineRecordDTO> pipelineRecordDTOS = pipelineRecordMapper.listAllPipelineRecordAndEnv(pipelineRecordDTO.getId());

        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(pipelineRecordDTOS.get(0).getProjectId(), "auto", pipelineRecordDTO.getId(), pipelineRecordDTOS.get(0).getEnv(), pipelineRecordDTO.getCreationDate());
        devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void failed(Long projectId, Long recordId) {
        PipelineRecordDTO recordE = pipelineRecordService.baseQueryById(recordId);
        if (!recordE.getStatus().equals(WorkFlowStatus.RUNNING.toValue())) {
            throw new CommonException("error.pipeline.record.status");
        }
        List<PipelineStageRecordDTO> stageRecordES = pipelineStageRecordService.baseListByRecordAndStageId(recordId, null);
        for (PipelineStageRecordDTO stageRecordE : stageRecordES) {
            if (stageRecordE.getStatus().equals(WorkFlowStatus.RUNNING.toValue()) || stageRecordE.getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                updateStatus(recordId, null, WorkFlowStatus.FAILED.toValue(), "Force failure");
                stageRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
                stageRecordE.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis() - TypeUtil.objToLong(stageRecordE.getExecutionTime())));
                pipelineStageRecordService.baseCreateOrUpdate(stageRecordE);
                Optional<PipelineTaskRecordDTO> optional = pipelineTaskRecordService.
                        baseQueryByStageRecordId(stageRecordE.getId(), null).stream().filter(t -> t.getStatus().equals(WorkFlowStatus.RUNNING.toValue())).findFirst();
                if (optional.isPresent()) {
                    PipelineTaskRecordDTO taskRecordE = optional.get();
                    taskRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
                    pipelineTaskRecordService.baseCreateOrUpdateRecord(taskRecordE);
                }
                break;
            }
        }
        sendFailedSiteMessage(recordId, recordE.getCreatedBy());
    }

    @Override
    public void sendSiteMessage(Long pipelineRecordId, String type, List<NoticeSendDTO.User> users, Map<String, Object> params) {
        NotifyVO notifyVO = new NotifyVO();
        notifyVO.setTargetUsers(users);
        notifyVO.setSourceId(pipelineRecordId);
        notifyVO.setCode(type);
        notifyVO.setCustomizedSendingTypes(Collections.singletonList("siteMessage"));
        PipelineRecordDTO recordE = pipelineRecordService.baseQueryById(pipelineRecordId);
        params.put("pipelineId", recordE.getPipelineId().toString());
        params.put("pipelineName", recordE.getPipelineName());
        params.put("pipelineRecordId", pipelineRecordId.toString());
        params.put("projectId", recordE.getProjectId().toString());
        ProjectDTO projectDTO = iamService.queryIamProject(recordE.getProjectId());
        params.put("projectName", projectDTO.getName());
        params.put("organizationId", projectDTO.getOrganizationId().toString());
        notifyVO.setParams(params);
        notifyClient.sendMessage(notifyVO);
    }

    private void sendFailedSiteMessage(Long pipelineRecordId, Long userId) {
        IamUserDTO iamUserDTO = iamService.queryUserByUserId(userId);
        NoticeSendDTO.User user = new NoticeSendDTO.User();
        user.setEmail(iamUserDTO.getEmail());
        user.setId(iamUserDTO.getId());
        sendSiteMessage(pipelineRecordId, PipelineNoticeType.PIPELINEFAILED.toValue(), Collections.singletonList(user), new HashMap<>());
    }

    private void sendAuditSiteMassage(String type, String auditUser, Long pipelineRecordId, String stageName) {
        List<String> userIds = new ArrayList<>();
        if (auditUser != null && !auditUser.isEmpty()) {
            userIds = Arrays.asList(auditUser.split(","));
            List arrList = new ArrayList(userIds);
            arrList.remove(TypeUtil.objToString(GitUserNameUtil.getUserId()));
            userIds = arrList;
        }
        List<NoticeSendDTO.User> userList = new ArrayList<>();
        userIds.forEach(t -> {
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            IamUserDTO iamUserDTO = iamService.queryUserByUserId(TypeUtil.objToLong(t));
            user.setEmail(iamUserDTO.getEmail());
            user.setId(iamUserDTO.getId());
            userList.add(user);
        });
        Map<String, Object> params = new HashMap<>();
        params.put("stageName", stageName);
        IamUserDTO iamUserDTO = iamService.queryUserByUserId(GitUserNameUtil.getUserId().longValue());
        params.put("auditName", iamUserDTO.getLoginName());
        params.put("realName", iamUserDTO.getRealName());
        sendSiteMessage(pipelineRecordId, type, userList, params);
    }

    /**
     * 校验会签任务是否全部审核过
     *
     * @return
     */
    private Boolean checkCouAllApprove(List<PipelineUserVO> userDTOS, Long taskId, Long taskRecordId) {
        List<Long> userList = userRelationshipService.baseListByOptions(null, null, taskId)
                .stream().map(PipelineUserRelationshipDTO::getUserId).collect(Collectors.toList());
        List<Long> userRecordList = pipelineUserRecordRelationshipService.baseListByOptions(null, null, taskRecordId)
                .stream().map(PipelineUserRecordRelationshipDTO::getUserId).collect(Collectors.toList());
        //是否全部同意
        if (userList.size() != userRecordList.size()) {
            List<Long> userListUnExe = new ArrayList<>(userList);
            userList.forEach(u -> {
                if (userRecordList.contains(u)) {
                    userListUnExe.remove(u);
                }
            });
            userRecordList.forEach(u -> {
                PipelineUserVO userDTO = ConvertUtils.convertObject(iamService.queryUserByUserId(u), PipelineUserVO.class);
                userDTO.setAudit(true);
                userDTOS.add(userDTO);
            });
            userListUnExe.forEach(u -> {
                PipelineUserVO userDTO = ConvertUtils.convertObject(iamService.queryUserByUserId(u), PipelineUserVO.class);
                userDTO.setAudit(false);
                userDTOS.add(userDTO);
            });
            return false;
        }
        return true;
    }

    private AppServiceVersionDTO getDeployVersion(Long pipelineRecordId, Long stageRecordId, PipelineTaskRecordDTO taskRecordDTO) {
        List<AppServiceVersionDTO> versionES = appServiceVersionService.baseListByAppServiceId(taskRecordDTO.getAppServiceId());
        Integer index = -1;
        for (int i = 0; i < versionES.size(); i++) {
            AppServiceVersionDTO versionE = versionES.get(i);
            if (taskRecordDTO.getTriggerVersion() == null || taskRecordDTO.getTriggerVersion().isEmpty()) {
                index = i;
                break;
            } else {
                List<String> list = Arrays.asList(taskRecordDTO.getTriggerVersion().split(","));
                Optional<String> branch = list.stream().filter(t -> versionE.getVersion().contains(t)).findFirst();
                if (branch.isPresent() && !branch.get().isEmpty()) {
                    index = i;
                    break;
                }
            }
        }
        if (index == -1) {
            setPipelineFailed(pipelineRecordId, stageRecordId, taskRecordDTO, "No version can trigger deploy");
            throw new CommonException("error.version.can.trigger.deploy");
        }
        return versionES.get(index);
    }

    private void removeStages(List<PipelineStageVO> stageDTOList, Long pipelineId) {
        List<Long> newStageIds = ConvertUtils.convertList(stageDTOList, PipelineStageDTO.class)
                .stream().filter(t -> t.getId() != null)
                .map(PipelineStageDTO::getId).collect(Collectors.toList());
        pipelineStageService.baseListByPipelineId(pipelineId).forEach(t -> {
            if (!newStageIds.contains(t.getId())) {
                pipelineStageService.baseDelete(t.getId());
                updateUserRel(null, null, t.getId(), null);
                pipelineTaskService.baseQueryTaskByStageId(t.getId()).forEach(taskE -> {
                    pipelineTaskRecordService.baseDeleteRecordById(taskE.getId());
                    updateUserRel(null, null, null, taskE.getId());
                });
            }
        });
    }

    private void removeTasks(List<PipelineTaskVO> taskDTOList, Long stageId) {
        List<Long> newTaskIds = taskDTOList.stream()
                .filter(t -> t.getId() != null)
                .map(PipelineTaskVO::getId)
                .collect(Collectors.toList());
        pipelineTaskService.baseQueryTaskByStageId(stageId).forEach(t -> {
            if (!newTaskIds.contains(t.getId())) {
                pipelineTaskService.baseDeleteTaskById(t.getId());
                if (t.getType().equals(MANUAL)) {
                    updateUserRel(null, null, null, t.getId());
                } else {
                    pipelineAppDeployService.baseDeleteById(t.getAppDeployId());
                }
            }
        });
    }

    private PipelineStageDTO createOrUpdateStage(PipelineStageVO stageDTO, Long pipelineId, Long projectId) {
        PipelineStageDTO stageE = ConvertUtils.convertObject(stageDTO, PipelineStageDTO.class);
        if (stageE.getId() != null) {
            pipelineStageService.baseUpdate(stageE);
        } else {
            stageE.setPipelineId(pipelineId);
            stageE.setProjectId(projectId);
            stageE = pipelineStageService.baseCreate(stageE);
            createUserRel(stageDTO.getStageUserRelDTOS(), null, stageE.getId(), null);
        }
        updateUserRel(stageDTO.getStageUserRelDTOS(), null, stageE.getId(), null);
        return stageE;
    }

    private void createOrUpdateTask(PipelineTaskVO taskDTO, Long stageId, Long projectId) {
        if (taskDTO.getId() != null) {
            if (AUTO.equals(taskDTO.getType())) {
                taskDTO.setAppDeployId(pipelineAppDeployService.baseUpdate(ConvertUtils.convertObject(taskDTO.getPipelineAppServiceDeployVO(), PipelineAppServiceDeployDTO.class)).getId());
            }
            Long taskId = pipelineTaskService.baseUpdateTask(ConvertUtils.convertObject(taskDTO, PipelineTaskDTO.class)).getId();
            if (MANUAL.equals(taskDTO.getType())) {
                updateUserRel(taskDTO.getTaskUserRelDTOS(), null, null, taskId);
            }
        } else {
            createPipelineTask(taskDTO, projectId, stageId);
        }
    }

    private Boolean getTaskEnvPermission(ProjectDTO projectDTO) {
        Boolean envPermission = true;
        if (!iamService.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO)) {
            List<Long> envIds = devopsEnvUserPermissionService
                    .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                    .filter(DevopsEnvUserPermissionDTO::getPermitted)
                    .map(DevopsEnvUserPermissionDTO::getEnvId).collect(Collectors.toList());
            envPermission = envIds.contains(DetailsHelper.getUserDetails().getUserId());
        }
        return envPermission;
    }

    private List<PipelineUserVO> getTaskAuditUsers(String auditUser, Long taskRecordId) {
        List<PipelineUserVO> userDTOS = new ArrayList<>();
        //获取已经审核人员
        List<Long> userIds = pipelineUserRecordRelationshipService.baseListByOptions(null, null, taskRecordId)
                .stream().map(PipelineUserRecordRelationshipDTO::getUserId).collect(Collectors.toList());
        userIds.forEach(userId -> {
            PipelineUserVO pipelineUserVO = ConvertUtils.convertObject(iamService.queryUserByUserId(userId), PipelineUserVO.class);
            pipelineUserVO.setAudit(true);
            userDTOS.add(pipelineUserVO);
        });
        //获取指定审核人员
        if (auditUser != null) {
            List<String> auditUserIds = Arrays.asList(auditUser.split(","));
            auditUserIds.forEach(userId -> {
                PipelineUserVO userDTO = ConvertUtils.convertObject(iamService.queryUserByUserId(TypeUtil.objToLong(userId)), PipelineUserVO.class);
                userDTO.setAudit(false);
                userDTOS.add(userDTO);
            });
        }
        return userDTOS;
    }

    private List<PipelineUserVO> getStageAuditUsers(Long stageRecordId, Long lastStageRecordId) {
        List<PipelineUserVO> userDTOS = new ArrayList<>();
        List<Long> userIds = pipelineUserRecordRelationshipService.baseListByOptions(null, lastStageRecordId, null)
                .stream().map(PipelineUserRecordRelationshipDTO::getUserId).collect(Collectors.toList());
        Boolean audit = !userIds.isEmpty();
        if (userIds.isEmpty()) {
            userIds = userRelationshipService.baseListByOptions(null, stageRecordId, null)
                    .stream().map(PipelineUserRelationshipDTO::getUserId).collect(Collectors.toList());
        }
        userIds.forEach(u -> {
            PipelineUserVO userDTO = ConvertUtils.convertObject(iamService.queryUserByUserId(u), PipelineUserVO.class);
            userDTO.setAudit(audit);
            userDTOS.add(userDTO);
        });
        return userDTOS;
    }

    private PipelineStageRecordDTO createStageRecord(PipelineStageDTO stageE, Long pipelineRecordId, List<PipelineUserRelationshipDTO> userRelationshipDTOS) {
        PipelineStageRecordDTO recordE = new PipelineStageRecordDTO();
        BeanUtils.copyProperties(stageE, recordE);
        recordE.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
        recordE.setStageId(stageE.getId());
        recordE.setPipelineRecordId(pipelineRecordId);
        recordE.setId(null);
        if (stageE.getTriggerType().equals(MANUAL)) {
            recordE.setAuditUser(StringUtils.join(userRelationshipDTOS.stream().map(PipelineUserRelationshipDTO::getUserId).toArray(), ","));
        }
        return pipelineStageRecordService.baseCreateOrUpdate(recordE);
    }

    private PipelineTaskRecordDTO createTaskRecordDTO(PipelineTaskDTO taskE, Long stageRecordId, List<PipelineUserRelationshipDTO> taskUserRels) {
        //创建task记录
        PipelineTaskRecordDTO taskRecordE = new PipelineTaskRecordDTO();
        BeanUtils.copyProperties(taskE, taskRecordE);
        taskRecordE.setTaskId(taskE.getId());
        taskRecordE.setTaskType(taskE.getType());
        taskRecordE.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
        taskRecordE.setStageRecordId(stageRecordId);
        if (taskE.getAppDeployId() != null) {
            PipelineAppServiceDeployDTO appDeployE = pipelineAppDeployService.baseQueryById(taskE.getAppDeployId());
            BeanUtils.copyProperties(appDeployE, taskRecordE);
            if (appDeployE.getInstanceName() == null) {
                taskRecordE.setInstanceName(appServiceInstanceService.baseQuery(appDeployE.getInstanceId()).getCode());
            }
            taskRecordE.setInstanceId(null);
            taskRecordE.setValueId(appDeployE.getValueId());
        }
        taskRecordE.setAuditUser(StringUtils.join(taskUserRels.stream().map(PipelineUserRelationshipDTO::getUserId).toArray(), ","));
        taskRecordE.setId(null);
        return pipelineTaskRecordService.baseCreateOrUpdateRecord(taskRecordE);
    }

    private PipelineTaskRecordDTO getFirstTask(Long pipelineRecordId) {
        return pipelineTaskRecordService.baseQueryByStageRecordId(pipelineStageRecordService.baseListByRecordAndStageId(pipelineRecordId, null).get(0).getId(), null).get(0);
    }

    private List<PipelineAppServiceDeployDTO> getAllAppDeploy(Long pipelineId) {
        List<PipelineAppServiceDeployDTO> appDeployEList = new ArrayList<>();
        pipelineStageService.baseListByPipelineId(pipelineId).forEach(stageE -> {
            pipelineTaskService.baseQueryTaskByStageId(stageE.getId()).forEach(taskE -> {
                if (taskE.getAppDeployId() != null) {
                    PipelineAppServiceDeployDTO appDeployE = pipelineAppDeployService.baseQueryById(taskE.getAppDeployId());
                    appDeployEList.add(appDeployE);
                }
            });
        });
        return appDeployEList;
    }

    private void updateFirstStage(Long pipelineRecordId) {
        PipelineStageRecordDTO stageRecordE = pipelineStageRecordService.baseListByRecordAndStageId(pipelineRecordId, null).get(0);
        stageRecordE.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis()));
        stageRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        pipelineStageRecordService.baseCreateOrUpdate(stageRecordE);
        if (isEmptyStage(stageRecordE.getId())) {
            startEmptyStage(pipelineRecordId, stageRecordE.getId());
        } else {
            PipelineTaskRecordDTO taskRecordE = getFirstTask(pipelineRecordId);
            if (taskRecordE.getTaskType().equals(MANUAL)) {
                startNextTask(taskRecordE, pipelineRecordId, stageRecordE.getId());
            }
        }
    }

    private void createPipelineTask(PipelineTaskVO t, Long projectId, Long stageId) {
        t.setProjectId(projectId);
        t.setStageId(stageId);
        if (AUTO.equals(t.getType())) {
            PipelineAppServiceDeployDTO appDeployDTO = ConvertUtils.convertObject(t.getPipelineAppServiceDeployVO(), PipelineAppServiceDeployDTO.class);
            appDeployDTO.setProjectId(projectId);
            t.setAppDeployId(pipelineAppDeployService.baseCreate(appDeployDTO).getId());
        }
        Long taskId = pipelineTaskService.baseCreateTask(ConvertUtils.convertObject(t, PipelineTaskDTO.class)).getId();
        if (MANUAL.equals(t.getType())) {
            createUserRel(t.getTaskUserRelDTOS(), null, null, taskId);
        }
    }

    private IamUserDTO getTriggerUser(Long pipelineRecordId, Long stageRecordId) {
        List<PipelineUserRecordRelationshipDTO> taskUserRecordRelES = pipelineUserRecordRelationshipService.baseListByOptions(pipelineRecordId, stageRecordId, null);
        if (taskUserRecordRelES != null && taskUserRecordRelES.size() > 0) {
            Long triggerUserId = taskUserRecordRelES.get(0).getUserId();
            return iamService.queryUserByUserId(triggerUserId);
        }
        return null;
    }

    private void startNextTask(Long taskRecordId, Long pipelineRecordId, Long stageRecordId) {
        PipelineTaskRecordDTO taskRecordE = pipelineTaskRecordService.baseQueryRecordById(taskRecordId);
        PipelineTaskRecordDTO nextTaskRecord = getNextTask(taskRecordId);
        PipelineStageRecordDTO stageRecordDTO = pipelineStageRecordService.baseQueryById(stageRecordId);
        //属于阶段的最后一个任务
        if (nextTaskRecord == null) {
            Long time = System.currentTimeMillis() - TypeUtil.objToLong(stageRecordDTO.getExecutionTime());
            stageRecordDTO.setStatus(WorkFlowStatus.SUCCESS.toValue());
            stageRecordDTO.setExecutionTime(time.toString());
            pipelineStageRecordService.baseCreateOrUpdate(stageRecordDTO);
            //属于pipeline最后一个任务
            PipelineStageRecordDTO nextStageRecord = getNextStage(taskRecordE.getStageRecordId());
            PipelineRecordDTO recordE = pipelineRecordService.baseQueryById(pipelineRecordId);
            if (nextStageRecord == null) {
                LOGGER.info("任务成功了");
                recordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
                pipelineRecordService.baseUpdate(recordE);
                IamUserDTO iamUserDTO = iamService.queryUserByUserId(recordE.getCreatedBy());
                NoticeSendDTO.User user = new NoticeSendDTO.User();
                user.setEmail(iamUserDTO.getEmail());
                user.setId(iamUserDTO.getId());
                sendSiteMessage(recordE.getId(), PipelineNoticeType.PIPELINESUCCESS.toValue(), Collections.singletonList(user), new HashMap<>());
            } else {
                //更新下一个阶段状态
                startNextStageRecord(stageRecordId, recordE);
            }
        } else {
            startNextTask(nextTaskRecord, pipelineRecordId, stageRecordId);
        }
    }

    /**
     * 开始下一个阶段
     *
     * @param stageRecordId 阶段记录Id
     * @param recordE
     */
    private void startNextStageRecord(Long stageRecordId, PipelineRecordDTO recordE) {
        PipelineStageRecordDTO nextStageRecordDTO = getNextStage(stageRecordId);
        nextStageRecordDTO.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis()));
        pipelineStageRecordService.baseCreateOrUpdate(nextStageRecordDTO);
        if (pipelineStageRecordService.baseQueryById(stageRecordId).getTriggerType().equals(AUTO)) {
            if (!isEmptyStage(nextStageRecordDTO.getId())) {
                nextStageRecordDTO.setStatus(WorkFlowStatus.RUNNING.toValue());
                List<PipelineTaskRecordDTO> list = pipelineTaskRecordService.baseQueryByStageRecordId(nextStageRecordDTO.getId(), null);
                if (list != null && list.size() > 0) {
                    if (list.get(0).getTaskType().equals(MANUAL)) {
                        startNextTask(list.get(0), recordE.getId(), nextStageRecordDTO.getId());
                    }
                }
            } else {
                startEmptyStage(recordE.getId(), nextStageRecordDTO.getId());
            }
        } else {
            List<NoticeSendDTO.User> userList = new ArrayList<>();
            String auditUser = pipelineStageRecordService.baseQueryById(stageRecordId).getAuditUser();
            if (auditUser != null && !auditUser.isEmpty()) {
                List<String> userIds = Arrays.asList(auditUser.split(","));
                userIds.forEach(t -> {
                    IamUserDTO userDTO = iamService.queryUserByUserId(TypeUtil.objToLong(t));
                    NoticeSendDTO.User user = new NoticeSendDTO.User();
                    user.setEmail(userDTO.getEmail());
                    user.setId(userDTO.getId());
                    userList.add(user);
                });
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("stageName", pipelineStageRecordService.baseQueryById(stageRecordId).getStageName());
            sendSiteMessage(recordE.getId(), PipelineNoticeType.PIPELINEAUDIT.toValue(), userList, params);
            updateStatus(recordE.getId(), null, WorkFlowStatus.PENDINGCHECK.toValue(), null);
        }
    }

    private Boolean isEmptyStage(Long stageRecordId) {
        List<PipelineTaskRecordDTO> taskRecordEList = pipelineTaskRecordService.baseQueryByStageRecordId(stageRecordId, null);
        return taskRecordEList == null || taskRecordEList.isEmpty();
    }

    private void startEmptyStage(Long pipelineRecordId, Long stageRecordId) {
        PipelineRecordDTO pipelineRecordDTO = pipelineRecordService.baseQueryById(pipelineRecordId);
        PipelineStageRecordDTO stageRecordE = pipelineStageRecordService.baseQueryById(stageRecordId);
        stageRecordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
        Long time = 0L;
        stageRecordE.setExecutionTime(time.toString());
        pipelineStageRecordService.baseCreateOrUpdate(stageRecordE);
        PipelineStageRecordDTO nextStageRecordE = getNextStage(stageRecordE.getId());
        if (nextStageRecordE != null) {
            startNextStageRecord(nextStageRecordE.getId(), pipelineRecordDTO);
        } else {
            updateStatus(pipelineRecordId, null, WorkFlowStatus.SUCCESS.toValue(), null);
            IamUserDTO iamUserDTO = iamService.queryUserByUserId(pipelineRecordDTO.getCreatedBy());
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            user.setEmail(iamUserDTO.getEmail());
            user.setId(iamUserDTO.getId());
            sendSiteMessage(pipelineRecordId, PipelineNoticeType.PIPELINESUCCESS.toValue(), Collections.singletonList(user), new HashMap<>());
        }
    }

    private void startNextTask(PipelineTaskRecordDTO pipelineTaskRecordDTO, Long pipelineRecordId, Long stageRecordId) {
        if (!pipelineTaskRecordDTO.getTaskType().equals(AUTO)) {
            pipelineTaskRecordDTO.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
            pipelineTaskRecordService.baseCreateOrUpdateRecord(pipelineTaskRecordDTO);
            updateStatus(pipelineRecordId, stageRecordId, WorkFlowStatus.PENDINGCHECK.toValue(), null);
            List<NoticeSendDTO.User> userList = new ArrayList<>();
            String auditUser = pipelineTaskRecordDTO.getAuditUser();
            if (auditUser != null && !auditUser.isEmpty()) {
                List<String> userIds = Arrays.asList(auditUser.split(","));
                userIds.forEach(t -> {
                    IamUserDTO iamUserDTO = iamService.queryUserByUserId(TypeUtil.objToLong(t));
                    NoticeSendDTO.User user = new NoticeSendDTO.User();
                    user.setEmail(iamUserDTO.getEmail());
                    user.setId(iamUserDTO.getId());
                    userList.add(user);
                });
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("stageName", pipelineStageRecordService.baseQueryById(stageRecordId).getStageName());
            sendSiteMessage(pipelineRecordId, PipelineNoticeType.PIPELINEAUDIT.toValue(), userList, params);
        }
    }

    private PipelineStageRecordDTO getNextStage(Long stageRecordId) {
        List<PipelineStageRecordDTO> list = pipelineStageRecordService.baseListByRecordAndStageId(pipelineStageRecordService.baseQueryById(stageRecordId).getPipelineRecordId(), null);
        return list.stream().filter(t -> t.getId() > stageRecordId).findFirst().orElse(null);
    }

    private PipelineTaskRecordDTO getNextTask(Long taskRecordId) {
        List<PipelineTaskRecordDTO> list = pipelineTaskRecordService.baseQueryByStageRecordId(pipelineTaskRecordService.baseQueryRecordById(taskRecordId).getStageRecordId(), null);
        return list.stream().filter(t -> t.getId() > taskRecordId).findFirst().orElse(null);
    }

    private Boolean checkTriggerPermission(Long pipelineId) {
        List<Long> userIds = userRelationshipService.baseListByOptions(pipelineId, null, null)
                .stream()
                .map(PipelineUserRelationshipDTO::getUserId)
                .collect(Collectors.toList());
        return userIds.contains(DetailsHelper.getUserDetails().getUserId());
    }

    private Boolean checkRecordTriggerPermission(Long pipelineRecordId, Long stageRecordId) {
        String auditUser = null;
        if (pipelineRecordId != null) {
            PipelineRecordDTO pipelineRecordE = pipelineRecordService.baseQueryById(pipelineRecordId);
            if (pipelineRecordE.getTriggerType().equals(AUTO)) {
                return true;
            }
            auditUser = pipelineRecordE.getAuditUser();
        }
        if (stageRecordId != null) {
            auditUser = pipelineStageRecordService.baseQueryById(stageRecordId).getAuditUser();
        }
        List<String> userIds = new ArrayList<>();
        if (auditUser != null && !auditUser.isEmpty()) {
            userIds = Arrays.asList(auditUser.split(","));
        }
        return userIds.contains(TypeUtil.objToString(DetailsHelper.getUserDetails().getUserId()));
    }

    private Boolean checkTaskTriggerPermission(Long taskRecordId) {
        PipelineTaskRecordDTO pipelineTaskRecordDTO = pipelineTaskRecordService.baseQueryRecordById(taskRecordId);
        List<String> userIds = new ArrayList<>();
        if (pipelineTaskRecordDTO.getAuditUser() != null && !pipelineTaskRecordDTO.getAuditUser().isEmpty()) {
            userIds = Arrays.asList(pipelineTaskRecordDTO.getAuditUser().split(","));
        }
        //未执行
        List<String> userIdsUnExe = new ArrayList<>(userIds);
        if (pipelineTaskRecordDTO.getIsCountersigned() == 1) {
            List<Long> userIdRecords = pipelineUserRecordRelationshipService.baseListByOptions(null, null, taskRecordId)
                    .stream()
                    .map(PipelineUserRecordRelationshipDTO::getUserId)
                    .collect(Collectors.toList());
            //移除已经执行
            userIds.forEach(t -> {
                if (userIdRecords.contains(TypeUtil.objToLong(t))) {
                    userIdsUnExe.remove(t);
                }
            });
        }
        return userIdsUnExe.contains(TypeUtil.objToString(DetailsHelper.getUserDetails().getUserId()));
    }

    private void createUserRel(List<Long> pipelineUserRelDTOS, Long pipelineId, Long stageId, Long taskId) {
        if (pipelineUserRelDTOS != null) {
            pipelineUserRelDTOS.forEach(t -> {
                PipelineUserRelationshipDTO userRelationshipDTO = new PipelineUserRelationshipDTO(pipelineId, stageId, taskId);
                userRelationshipDTO.setUserId(t);
                userRelationshipService.baseCreate(userRelationshipDTO);
            });
        }
    }

    private void updateUserRel(List<Long> relDTOList, Long pipelineId, Long stageId, Long taskId) {
        List<Long> addUserRelEList = new ArrayList<>();
        List<Long> relEList = userRelationshipService.baseListByOptions(pipelineId, stageId, taskId).stream().map(PipelineUserRelationshipDTO::getUserId).collect(Collectors.toList());
        if (relDTOList != null) {
            relDTOList.forEach(relE -> {
                if (!relEList.contains(relE)) {
                    addUserRelEList.add(relE);
                } else {
                    relEList.remove(relE);
                }
            });
            addUserRelEList.forEach(addUserId -> {
                PipelineUserRelationshipDTO delUserRelationshipDTO = new PipelineUserRelationshipDTO(pipelineId, stageId, taskId);
                delUserRelationshipDTO.setUserId(addUserId);
                userRelationshipService.baseCreate(delUserRelationshipDTO);
            });
        }
        relEList.forEach(delUserId -> {
            PipelineUserRelationshipDTO addUserRelationshipDTO = new PipelineUserRelationshipDTO(pipelineId, stageId, taskId);
            addUserRelationshipDTO.setUserId(delUserId);
            userRelationshipService.baseDelete(addUserRelationshipDTO);
        });
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
                        DemoEnvSetupSagaHandler.beforeInvoke(loginName, userId, orgId);
                        try {
                            workFlowServiceOperator.create(projectId, devopsPipelineDTO);
                        } catch (Exception e) {
                            throw new CommonException(e);
                        }
                    }
                });

    }

    private void approveWorkFlow(Long projectId, String businessKey, String loginName, Long userId, Long orgId) {
        Observable.create((ObservableOnSubscribe<String>) dtoObservableEmitter -> {
            dtoObservableEmitter.onComplete();
        }).subscribeOn(Schedulers.io())
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
                        DemoEnvSetupSagaHandler.beforeInvoke(loginName, userId, orgId);
                        try {
                            workFlowServiceOperator.approveUserTask(projectId, businessKey);
                        } catch (Exception e) {
                            throw new CommonException(e);
                        }
                    }
                });
    }

    private Boolean checkPipelineEnvPermission(ProjectDTO projectDTO, List<Long> pipelineEnvIds) {
        Boolean index = true;
        if (!iamService.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO)) {
            List<Long> envIds = devopsEnvUserPermissionService
                    .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                    .filter(DevopsEnvUserPermissionDTO::getPermitted)
                    .map(DevopsEnvUserPermissionDTO::getEnvId).collect(Collectors.toList());
            for (Long pipelineEnvId : pipelineEnvIds) {
                if (!envIds.contains(pipelineEnvId)) {
                    index = false;
                    break;
                }
            }
        }
        return index;
    }

    private void setPipelineFailed(Long pipelineRecordId, Long stageRecordId, PipelineTaskRecordDTO taskRecordDTO, String errorInfo) {
        taskRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
        pipelineTaskRecordService.baseCreateOrUpdateRecord(taskRecordDTO);
        updateStatus(pipelineRecordId, stageRecordId, WorkFlowStatus.FAILED.toValue(), errorInfo);
    }

    private String getAuditResult(PipelineUserRecordRelationshipVO recordRelDTO, PipelineRecordDTO pipelineRecordDTO, String auditUser, String stageName) {
        Boolean result = true;
        String status;
        if (recordRelDTO.getIsApprove()) {
            try {
                CustomUserDetails details = DetailsHelper.getUserDetails();
                approveWorkFlow(pipelineRecordDTO.getProjectId(), pipelineRecordService.baseQueryById(recordRelDTO.getPipelineRecordId()).getBusinessKey(), details.getUsername(), details.getUserId(), details.getOrganizationId());
            } catch (Exception e) {
                result = false;
                sendFailedSiteMessage(recordRelDTO.getPipelineRecordId(), pipelineRecordDTO.getCreatedBy());
            }
            status = result ? WorkFlowStatus.SUCCESS.toValue() : WorkFlowStatus.FAILED.toValue();
            if (STAGE.equals(recordRelDTO.getType())) {
                status = result ? WorkFlowStatus.RUNNING.toValue() : WorkFlowStatus.FAILED.toValue();
            }
        } else {
            status = WorkFlowStatus.STOP.toValue();
            auditUser = auditUser.contains(pipelineRecordDTO.getCreatedBy().toString()) ? auditUser : auditUser + "," + pipelineRecordDTO.getCreatedBy();
            sendAuditSiteMassage(PipelineNoticeType.PIPELINESTOP.toValue(), auditUser, recordRelDTO.getPipelineRecordId(), stageName);
        }
        return status;
    }

    @Override
    public PageInfo<PipelineDTO> baseListByOptions(Long projectId, PageRequest pageRequest, String params, Map<String, Object> classifyParam) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                pipelineMapper.listByOptions(projectId, searchParamMap, paramList, PageRequestUtil.checkSortIsEmpty(pageRequest), classifyParam));
    }

    @Override
    public PipelineDTO baseCreate(Long projectId, PipelineDTO devopsPipelineDTO) {
        devopsPipelineDTO.setIsEnabled(1);
        if (pipelineMapper.insert(devopsPipelineDTO) != 1) {
            throw new CommonException("error.insert.pipeline");
        }
        return devopsPipelineDTO;
    }

    @Override
    public PipelineDTO baseUpdate(Long projectId, PipelineDTO devopsPipelineDTO) {
        devopsPipelineDTO.setIsEnabled(1);
        if (pipelineMapper.updateByPrimaryKey(devopsPipelineDTO) != 1) {
            throw new CommonException("error.update.pipeline");
        }
        return devopsPipelineDTO;
    }

    @Override
    public PipelineDTO baseUpdateWithEnabled(Long pipelineId, Integer isEnabled) {
        PipelineDTO devopsPipelineDTO = new PipelineDTO();
        devopsPipelineDTO.setId(pipelineId);
        devopsPipelineDTO.setIsEnabled(isEnabled);
        devopsPipelineDTO.setObjectVersionNumber(pipelineMapper.selectByPrimaryKey(devopsPipelineDTO).getObjectVersionNumber());
        if (pipelineMapper.updateByPrimaryKeySelective(devopsPipelineDTO) != 1) {
            throw new CommonException("error.update.pipeline.is.enabled");
        }
        return devopsPipelineDTO;
    }

    @Override
    public PipelineDTO baseQueryById(Long pipelineId) {
        PipelineDTO devopsPipelineDTO = new PipelineDTO();
        devopsPipelineDTO.setId(pipelineId);
        return pipelineMapper.selectByPrimaryKey(devopsPipelineDTO);
    }

    @Override
    public void baseDelete(Long pipelineId) {
        PipelineDTO pipelineDO = new PipelineDTO();
        pipelineDO.setId(pipelineId);
        pipelineMapper.deleteByPrimaryKey(pipelineDO);
    }

    @Override
    public void baseCheckName(Long projectId, String name) {
        PipelineDTO pipelineDO = new PipelineDTO();
        pipelineDO.setProjectId(projectId);
        pipelineDO.setName(name);
        if (pipelineMapper.select(pipelineDO).size() > 0) {
            throw new CommonException("error.pipeline.name.exit");
        }
    }

    @Override
    public List<PipelineDTO> baseQueryByProjectId(Long projectId) {
        PipelineDTO devopsPipelineDTO = new PipelineDTO();
        devopsPipelineDTO.setProjectId(projectId);
        return pipelineMapper.select(devopsPipelineDTO);
    }
}

