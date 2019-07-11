package io.choerodon.devops.app.service.impl;

import static java.util.Comparator.comparing;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.zaxxer.hikari.util.UtilityElf;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.UserDTO;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.app.eventhandler.DemoEnvSetupSagaHandler;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineTaskDTO;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.PipelineNoticeType;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.feign.NotifyClient;
import io.choerodon.devops.infra.util.CutomerContextUtil;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;
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
    private static final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new UtilityElf.DefaultThreadFactory("devops-workflow", false));
    @Autowired
    private PipelineRepository pipelineRepository;
    @Autowired
    private PipelineUserRelRepository pipelineUserRelRepository;
    @Autowired
    private PipelineUserRelRecordRepository pipelineUserRelRecordRepository;
    @Autowired
    private PipelineRecordRepository pipelineRecordRepository;
    @Autowired
    private PipelineStageRecordRepository stageRecordRepository;
    @Autowired
    private PipelineStageRepository stageRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private PipelineTaskRepository pipelineTaskRepository;
    @Autowired
    private PipelineAppDeployRepository appDeployRepository;
    @Autowired
    private DevopsDeployValueRepository valueRepository;
    @Autowired
    private PipelineTaskRecordRepository taskRecordRepository;
    @Autowired
    private WorkFlowRepository workFlowRepository;
    @Autowired
    private ApplicationVersionRepository versionRepository;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private NotifyClient notifyClient;

    @Override
    public PageInfo<PipelineDTO> listByOptions(Long projectId, Boolean creator, Boolean executor, List<String> envIds, PageRequest pageRequest, String params) {
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        Map<String, Object> classifyParam = new HashMap<>();
        classifyParam.put("creator", creator);
        classifyParam.put("executor", executor);
        classifyParam.put("userId", DetailsHelper.getUserDetails().getUserId());
        classifyParam.put("envIds", envIds);
        PageInfo<PipelineDTO> pipelineDTOS = ConvertPageHelper.convertPageInfo(pipelineRepository.listByOptions(projectId, pageRequest, params, classifyParam), PipelineDTO.class);
        PageInfo<PipelineDTO> page = new PageInfo<>();
        BeanUtils.copyProperties(pipelineDTOS, page);
        page.setList(pipelineDTOS.getList().stream().peek(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreatedBy());
            t.setCreateUserName(userE.getLoginName());
            t.setCreateUserUrl(userE.getImageUrl());
            t.setCreateUserRealName(userE.getRealName());
            List<Long> pipelineEnvIds = getAllAppDeploy(t.getId()).stream().map(PipelineAppDeployE::getEnvId).collect(Collectors.toList());
            t.setEdit(checkPipelineEnvPermission(projectE, pipelineEnvIds));
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public PageInfo<PipelineRecordDTO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params, Boolean pendingcheck, Boolean executed, Boolean reviewed) {
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        Map<String, Object> classifyParam = new HashMap<>();
        classifyParam.put("executed", executed);
        classifyParam.put("reviewed", reviewed);
        classifyParam.put("pendingcheck", pendingcheck);
        classifyParam.put("userId", DetailsHelper.getUserDetails().getUserId());
        PageInfo<PipelineRecordDTO> pageRecordDTOS = ConvertPageHelper.convertPageInfo(
                pipelineRecordRepository.listByOptions(projectId, pipelineId, pageRequest, params, classifyParam), PipelineRecordDTO.class);
        pageRecordDTOS.setList(pageRecordDTOS.getList().stream().peek(t -> {
            t.setExecute(false);
            t.setStageDTOList(ConvertHelper.convertList(stageRecordRepository.list(projectId, t.getId()), PipelineStageRecordDTO.class));
            if (t.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                for (int i = 0; i < t.getStageDTOList().size(); i++) {
                    if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                        List<PipelineTaskRecordE> list = taskRecordRepository.queryByStageRecordId(t.getStageDTOList().get(i).getId(), null);
                        if (list != null && list.size() > 0) {
                            Optional<PipelineTaskRecordE> taskRecordE = list.stream().filter(task -> task.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())).findFirst();
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
                        List<PipelineTaskRecordE> recordEList = taskRecordRepository.queryByStageRecordId(t.getStageDTOList().get(i).getId(), null);
                        Optional<PipelineTaskRecordE> optional = recordEList.stream().filter(recordE -> recordE.getStatus().equals(WorkFlowStatus.STOP.toValue())).findFirst();
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
                    List<Long> pipelineEnvIds = taskRecordRepository.queryAllAutoTaskRecord(t.getId()).stream().map(PipelineTaskRecordE::getEnvId).collect(Collectors.toList());
                    t.setExecute(checkPipelineEnvPermission(projectE, pipelineEnvIds));
                }
            }
        }).collect(Collectors.toList()));
        return pageRecordDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineReqDTO create(Long projectId, PipelineReqDTO pipelineReqDTO) {
        PipelineE pipelineE = ConvertHelper.convert(pipelineReqDTO, PipelineE.class);
        pipelineE.setProjectId(projectId);
        checkName(projectId, pipelineReqDTO.getName());
        pipelineE = pipelineRepository.create(projectId, pipelineE);
        createUserRel(pipelineReqDTO.getPipelineUserRelDTOS(), pipelineE.getId(), null, null);

        Long pipelineId = pipelineE.getId();
        List<PipelineStageE> pipelineStageES = ConvertHelper.convertList(pipelineReqDTO.getPipelineStageDTOS(), PipelineStageE.class)
                .stream().map(t -> {
                    t.setPipelineId(pipelineId);
                    t.setProjectId(projectId);
                    return stageRepository.create(t);
                }).collect(Collectors.toList());
        for (int i = 0; i < pipelineStageES.size(); i++) {
            Long stageId = pipelineStageES.get(i).getId();
            createUserRel(pipelineReqDTO.getPipelineStageDTOS().get(i).getStageUserRelDTOS(), null, stageId, null);
            List<PipelineTaskDTO> taskDTOList = pipelineReqDTO.getPipelineStageDTOS().get(i).getPipelineTaskDTOS();
            if (taskDTOList != null && taskDTOList.size() > 0) {
                taskDTOList.forEach(t -> createPipelineTask(t, projectId, stageId));
            }
        }
        return pipelineReqDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineReqDTO update(Long projectId, PipelineReqDTO pipelineReqDTO) {
        pipelineReqDTO.setProjectId(projectId);
        PipelineE pipelineE = ConvertHelper.convert(pipelineReqDTO, PipelineE.class);
        pipelineE = pipelineRepository.update(projectId, pipelineE);
        updateUserRel(pipelineReqDTO.getPipelineUserRelDTOS(), pipelineE.getId(), null, null);
        Long pipelineId = pipelineE.getId();
        removeStages(pipelineReqDTO.getPipelineStageDTOS(), pipelineId);

        for (int i = 0; i < pipelineReqDTO.getPipelineStageDTOS().size(); i++) {
            PipelineStageE stageE = createOrUpdateStage(pipelineReqDTO.getPipelineStageDTOS().get(i), pipelineId, projectId);
            List<PipelineTaskDTO> taskDTOList = pipelineReqDTO.getPipelineStageDTOS().get(i).getPipelineTaskDTOS();
            if (taskDTOList != null) {
                removeTasks(taskDTOList, stageE.getId());
                taskDTOList.stream().filter(Objects::nonNull).forEach(t -> {
                    createOrUpdateTask(t, stageE.getId(), projectId);
                });
            }
        }
        pipelineRecordRepository.updateEdited(pipelineId);
        return pipelineReqDTO;
    }

    @Override
    public PipelineDTO updateIsEnabled(Long projectId, Long pipelineId, Integer isEnabled) {
        return ConvertHelper.convert(pipelineRepository.updateIsEnabled(pipelineId, isEnabled), PipelineDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId, Long pipelineId) {
        pipelineRecordRepository.queryByPipelineId(pipelineId).forEach(t -> {
            t.setStatus(WorkFlowStatus.DELETED.toValue());
            pipelineRecordRepository.update(t);
        });
        pipelineUserRelRepository.listByOptions(pipelineId, null, null).forEach(t -> pipelineUserRelRepository.delete(t));
        stageRepository.queryByPipelineId(pipelineId).forEach(stage -> {
            pipelineTaskRepository.queryByStageId(stage.getId()).forEach(task -> {
                if (task.getAppDeployId() != null) {
                    appDeployRepository.deleteById(task.getAppDeployId());
                }
                pipelineTaskRepository.deleteById(task.getId());
                pipelineUserRelRepository.listByOptions(null, null, task.getId()).forEach(t -> pipelineUserRelRepository.delete(t));
            });
            stageRepository.delete(stage.getId());
            pipelineUserRelRepository.listByOptions(null, stage.getId(), null).forEach(t -> pipelineUserRelRepository.delete(t));
        });
        pipelineRepository.delete(pipelineId);
    }

    @Override
    public PipelineReqDTO queryById(Long projectId, Long pipelineId) {
        PipelineReqDTO pipelineReqDTO = ConvertHelper.convert(pipelineRepository.queryById(pipelineId), PipelineReqDTO.class);
        pipelineReqDTO.setPipelineUserRelDTOS(pipelineUserRelRepository.listByOptions(pipelineId, null, null).stream().map(PipelineUserRelE::getUserId).collect(Collectors.toList()));
        List<PipelineStageDTO> pipelineStageES = ConvertHelper.convertList(stageRepository.queryByPipelineId(pipelineId), PipelineStageDTO.class);
        pipelineStageES = pipelineStageES.stream()
                .peek(stage -> {
                    List<PipelineTaskDTO> pipelineTaskDTOS = ConvertHelper.convertList(pipelineTaskRepository.queryByStageId(stage.getId()), PipelineTaskDTO.class);
                    pipelineTaskDTOS = pipelineTaskDTOS.stream().peek(task -> {
                        if (task.getAppDeployId() != null) {
                            task.setAppDeployDTOS(ConvertHelper.convert(appDeployRepository.queryById(task.getAppDeployId()), PipelineAppDeployDTO.class));
                        } else {
                            task.setTaskUserRelDTOS(pipelineUserRelRepository.listByOptions(null, null, task.getId()).stream().map(PipelineUserRelE::getUserId).collect(Collectors.toList()));
                        }
                    }).collect(Collectors.toList());
                    stage.setPipelineTaskDTOS(pipelineTaskDTOS);
                    stage.setStageUserRelDTOS(pipelineUserRelRepository.listByOptions(null, stage.getId(), null).stream().map(PipelineUserRelE::getUserId).collect(Collectors.toList()));
                }).collect(Collectors.toList());
        pipelineReqDTO.setPipelineStageDTOS(pipelineStageES);
        return pipelineReqDTO;
    }

    @Override
    public void execute(Long projectId, Long pipelineId) {
        PipelineE pipelineE = pipelineRepository.queryById(pipelineId);
        if (AUTO.equals(pipelineE.getTriggerType()) || !checkTriggerPermission(pipelineId)) {
            throw new CommonException("error.permission.trigger.pipeline");
        }
        PipelineRecordE pipelineRecordE = new PipelineRecordE(pipelineId, pipelineE.getTriggerType(), projectId, WorkFlowStatus.RUNNING.toValue(), pipelineE.getName());
        pipelineRecordE.setBusinessKey(GenerateUUID.generateUUID());
        if (pipelineE.getTriggerType().equals(MANUAL)) {
            List<PipelineUserRelE> taskRelEList = pipelineUserRelRepository.listByOptions(pipelineId, null, null);
            pipelineRecordE.setAuditUser(StringUtils.join(taskRelEList.stream().map(PipelineUserRelE::getUserId).toArray(), ","));
        }
        pipelineRecordE = pipelineRecordRepository.create(pipelineRecordE);
        PipelineUserRecordRelE userRecordRelE = new PipelineUserRecordRelE();
        userRecordRelE.setPipelineRecordId(pipelineRecordE.getId());
        userRecordRelE.setUserId(DetailsHelper.getUserDetails().getUserId());
        pipelineUserRelRecordRepository.create(userRecordRelE);
        DevopsPipelineDTO devopsPipelineDTO = createWorkFlowDTO(pipelineRecordE.getId(), pipelineId, pipelineRecordE.getBusinessKey());
        pipelineRecordE.setBpmDefinition(gson.toJson(devopsPipelineDTO));
        pipelineRecordRepository.update(pipelineRecordE);
        try {
            CustomUserDetails details = DetailsHelper.getUserDetails();
            createWorkFlow(projectId, devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
            updateFirstStage(pipelineRecordE.getId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            sendFailedSiteMessage(pipelineRecordE.getId(), GitUserNameUtil.getUserId().longValue());
            pipelineRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
            pipelineRecordE.setErrorInfo(e.getMessage());
            pipelineRecordRepository.update(pipelineRecordE);
        }
    }

    @Override
    @Saga(code = "devops-pipeline-auto-deploy-instance",
            description = "创建流水线自动部署实例", inputSchema = "{}")
    public void autoDeploy(Long stageRecordId, Long taskRecordId) {
        LOGGER.info("autoDeploy:stageRecordId: {} taskId: {}", stageRecordId, taskRecordId);
        //获取数据
        PipelineTaskRecordE taskRecordE = taskRecordRepository.queryById(taskRecordId);
        Long pipelineRecordId = stageRecordRepository.queryById(stageRecordId).getPipelineRecordId();
        CutomerContextUtil.setUserId(taskRecordE.getCreatedBy());
        ApplicationVersionE appVersionE = getDeployVersion(pipelineRecordId, stageRecordId, taskRecordE);
        //保存记录
        taskRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        taskRecordE.setName(taskRecordE.getName());
        taskRecordE.setVersionId(appVersionE.getId());
        taskRecordE = taskRecordRepository.createOrUpdate(taskRecordE);
        try {
            ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(taskRecordE.getInstanceName(), taskRecordE.getEnvId());
            Long instanceId = instanceE == null ? null : instanceE.getId();
            String type = instanceId == null ? CommandType.CREATE.getType() : CommandType.UPDATE.getType();
            ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO(appVersionE.getId(), taskRecordE.getEnvId(),
                    valueRepository.queryById(taskRecordE.getValueId()).getValue(), taskRecordE.getApplicationId(), type, instanceId,
                    taskRecordE.getInstanceName(), taskRecordE.getId(), taskRecordE.getValueId());
            if (type.equals(CommandType.UPDATE.getType())) {
                ApplicationInstanceE oldapplicationInstanceE = applicationInstanceRepository.selectById(applicationDeployDTO.getAppInstanceId());
                DevopsEnvCommandE olddevopsEnvCommandE = devopsEnvCommandRepository.query(oldapplicationInstanceE.getCommandId());
                if (olddevopsEnvCommandE.getObjectVersionId().equals(applicationDeployDTO.getAppVersionId())) {
                    String oldValue = applicationInstanceRepository.queryValueByInstanceId(applicationDeployDTO.getAppInstanceId());
                    if (applicationDeployDTO.getValues().trim().equals(oldValue.trim())) {
                        applicationDeployDTO.setIsNotChange(true);
                    }
                }
            }
            String input = gson.toJson(applicationDeployDTO);
            sagaClient.startSaga("devops-pipeline-auto-deploy-instance", new StartInstanceDTO(input, "env", taskRecordE.getEnvId().toString(), ResourceLevel.PROJECT.value(), taskRecordE.getProjectId()));
        } catch (Exception e) {
            sendFailedSiteMessage(pipelineRecordId, GitUserNameUtil.getUserId().longValue());
            PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(stageRecordId);
            Long time = System.currentTimeMillis() - TypeUtil.objToLong(stageRecordE.getExecutionTime());
            stageRecordE.setExecutionTime(time.toString());
            stageRecordRepository.update(stageRecordE);
            setPipelineFailed(pipelineRecordId, stageRecordId, taskRecordE, e.getMessage());
            throw new CommonException("error.create.pipeline.auto.deploy.instance", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<IamUserDTO> audit(Long projectId, PipelineUserRecordRelDTO recordRelDTO) {
        List<IamUserDTO> userDTOS = new ArrayList<>();
        String status;
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(recordRelDTO.getPipelineRecordId());
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(recordRelDTO.getStageRecordId());
        String auditUser = new String();
        if ("task".equals(recordRelDTO.getType())) {
            auditUser = taskRecordRepository.queryById(recordRelDTO.getTaskRecordId()).getAuditUser();
        } else {
            Optional<PipelineStageRecordE> optional = stageRecordRepository.queryByPipeRecordId(recordRelDTO.getPipelineRecordId(), null).stream().filter(t -> t.getId() < recordRelDTO.getStageRecordId()).findFirst();
            auditUser = optional.isPresent() ? optional.get().getAuditUser() : auditUser;
        }
        status = getAuditResult(recordRelDTO, pipelineRecordE, auditUser, stageRecordE.getStageName());
        PipelineUserRecordRelE userRelE = new PipelineUserRecordRelE();
        userRelE.setUserId(DetailsHelper.getUserDetails().getUserId());
        switch (recordRelDTO.getType()) {
            case TASK: {
                userRelE.setTaskRecordId(recordRelDTO.getTaskRecordId());
                pipelineUserRelRecordRepository.create(userRelE);
                PipelineTaskRecordE taskRecordE = taskRecordRepository.queryById(recordRelDTO.getTaskRecordId());
                if (status.equals(WorkFlowStatus.SUCCESS.toValue())) {
                    //判断会签是否全部通过
                    if (taskRecordE.getIsCountersigned() == 1) {
                        if (!checkCouAllApprove(userDTOS, taskRecordE.getTaskId(), recordRelDTO.getTaskRecordId())) {
                            break;
                        }
                    } else {
                        sendAuditSiteMassage(PipelineNoticeType.PIPELINEPASS.toValue(), auditUser, recordRelDTO.getPipelineRecordId(), stageRecordE.getStageName());
                    }
                    updateStatus(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId(), WorkFlowStatus.RUNNING.toValue(), null);
                    startNextTask(taskRecordE.getId(), recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                } else {
                    Long time = System.currentTimeMillis() - TypeUtil.objToLong(stageRecordE.getExecutionTime());
                    stageRecordE.setStatus(status);
                    stageRecordE.setExecutionTime(time.toString());
                    stageRecordRepository.createOrUpdate(stageRecordE);
                    updateStatus(recordRelDTO.getPipelineRecordId(), null, status, null);
                }
                taskRecordE.setStatus(status);
                taskRecordRepository.createOrUpdate(taskRecordE);
                break;
            }
            case STAGE: {
                userRelE.setStageRecordId(recordRelDTO.getStageRecordId());
                pipelineUserRelRecordRepository.create(userRelE);
                if (status.equals(WorkFlowStatus.RUNNING.toValue())) {
                    updateStatus(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId(), status, null);
                    if (!isEmptyStage(recordRelDTO.getStageRecordId())) {
                        List<PipelineTaskRecordE> taskRecordEList = taskRecordRepository.queryByStageRecordId(recordRelDTO.getStageRecordId(), null);
                        PipelineTaskRecordE taskRecordE = taskRecordEList.get(0);
                        if (MANUAL.equals(taskRecordE.getTaskType())) {
                            startNextTask(taskRecordE, recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                        }
                    } else {
                        startEmptyStage(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                    }
                    sendAuditSiteMassage(PipelineNoticeType.PIPELINEPASS.toValue(), auditUser, recordRelDTO.getPipelineRecordId(), stageRecordE.getStageName());
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
    public CheckAuditDTO checkAudit(Long projectId, PipelineUserRecordRelDTO recordRelDTO) {
        CheckAuditDTO auditDTO = new CheckAuditDTO();
        switch (recordRelDTO.getType()) {
            case TASK: {
                PipelineTaskRecordE taskRecordE = taskRecordRepository.queryById(recordRelDTO.getTaskRecordId());
                if (!taskRecordE.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                    if (taskRecordE.getIsCountersigned() == 1) {
                        auditDTO.setIsCountersigned(1);
                    } else {
                        auditDTO.setIsCountersigned(0);
                    }
                    auditDTO.setUserName(iamRepository.queryUserByUserId(
                            pipelineUserRelRecordRepository.queryByRecordId(null, null, taskRecordE.getId()).get(0).getUserId())
                            .getRealName());
                }
                break;
            }
            case STAGE: {
                PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(recordRelDTO.getStageRecordId());
                if (!stageRecordE.getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                    auditDTO.setIsCountersigned(0);
                    auditDTO.setUserName(iamRepository.queryUserByUserId(
                            pipelineUserRelRecordRepository.queryByRecordId(null, stageRecordE.getId(), null).get(0).getUserId())
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
    public PipelineCheckDeployDTO checkDeploy(Long projectId, Long pipelineId) {
        PipelineE pipelineE = pipelineRepository.queryById(pipelineId);
        if (pipelineE.getIsEnabled() == 0) {
            throw new CommonException("error.pipeline.check.deploy");
        }
        Long userId = pipelineE.getTriggerType().equals(AUTO) ? pipelineE.getLastUpdatedBy() : TypeUtil.objToLong(GitUserNameUtil.getUserId());
        PipelineCheckDeployDTO checkDeployDTO = new PipelineCheckDeployDTO();
        checkDeployDTO.setPermission(true);
        checkDeployDTO.setVersions(true);
        List<PipelineAppDeployE> allAppDeploys = getAllAppDeploy(pipelineId);
        if (allAppDeploys.isEmpty()) {
            return checkDeployDTO;
        }
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        if (!iamRepository.isProjectOwner(userId, projectE)) {
            List<Long> envIds = devopsEnvUserPermissionRepository
                    .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                    .filter(DevopsEnvUserPermissionE::getPermitted)
                    .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
            for (PipelineAppDeployE appDeployE : allAppDeploys) {
                if (!envIds.contains(appDeployE.getEnvId())) {
                    checkDeployDTO.setPermission(false);
                    checkDeployDTO.setEnvName(appDeployE.getEnvName());
                    return checkDeployDTO;
                }
            }
        }
        for (PipelineAppDeployE appDeployE : allAppDeploys) {
            if (appDeployE.getCreationDate().getTime() > versionRepository.getLatestVersion(appDeployE.getApplicationId()).getCreationDate().getTime()) {
                checkDeployDTO.setVersions(false);
                break;
            } else {
                if ((appDeployE.getTriggerVersion() != null) && !appDeployE.getTriggerVersion().isEmpty()) {
                    List<String> list = Arrays.asList(appDeployE.getTriggerVersion().split(","));
                    List<ApplicationVersionE> versionES = versionRepository.listByAppId(appDeployE.getApplicationId(), null)
                            .stream()
                            .filter(versionE -> versionE.getCreationDate().getTime() > appDeployE.getCreationDate().getTime())
                            .collect(Collectors.toList());

                    int i = 0;
                    for (ApplicationVersionE versionE : versionES) {
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
    public DevopsPipelineDTO createWorkFlowDTO(Long pipelineRecordId, Long pipelineId, String businessKey) {
        DevopsPipelineDTO devopsPipelineDTO = new DevopsPipelineDTO();
        devopsPipelineDTO.setPipelineRecordId(pipelineRecordId);
        devopsPipelineDTO.setBusinessKey(businessKey);
        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
        List<PipelineStageE> stageES = stageRepository.queryByPipelineId(pipelineId);
        for (int i = 0; i < stageES.size(); i++) {
            PipelineStageE stageE = stageES.get(i);
            List<PipelineUserRelE> stageRelEList = pipelineUserRelRepository.listByOptions(null, stageE.getId(), null);
            PipelineStageRecordE recordE = createStageRecord(stageE, pipelineRecordId, stageRelEList);
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
            pipelineTaskRepository.queryByStageId(stageE.getId()).forEach(taskE -> {
                List<PipelineUserRelE> taskUserRels = pipelineUserRelRepository.listByOptions(null, null, taskE.getId());
                PipelineTaskRecordE taskRecordE = createTaskRecordE(taskE, stageRecordId, taskUserRels);
                DevopsPipelineTaskDTO devopsPipelineTaskDTO = new DevopsPipelineTaskDTO();
                devopsPipelineTaskDTO.setTaskRecordId(taskRecordE.getId());
                devopsPipelineTaskDTO.setTaskName(taskE.getName());
                devopsPipelineTaskDTO.setTaskType(taskE.getType());
                devopsPipelineTaskDTO.setMultiAssign(taskUserRels.size() > 1);
                devopsPipelineTaskDTO.setUsernames(taskUserRels.stream().map(userRelE -> userRelE.getUserId().toString()).collect(Collectors.toList()));
                devopsPipelineTaskDTO.setTaskRecordId(taskRecordE.getId());
                if (taskE.getIsCountersigned() != null) {
                    devopsPipelineTaskDTO.setSign(taskE.getIsCountersigned().longValue());
                }
                devopsPipelineTaskDTOS.add(devopsPipelineTaskDTO);

            });
            devopsPipelineStageDTO.setTasks(devopsPipelineTaskDTOS);
            devopsPipelineStageDTOS.add(devopsPipelineStageDTO);
        }
        stageRepository.queryByPipelineId(pipelineId).forEach(t -> {


        });
        devopsPipelineDTO.setStages(devopsPipelineStageDTOS);
        return devopsPipelineDTO;
    }

    @Override
    public String getAppDeployStatus(Long stageRecordId, Long taskRecordId) {
        PipelineTaskRecordE taskRecordE = taskRecordRepository.queryById(taskRecordId);
        if (taskRecordE != null) {
            return taskRecordE.getStatus();
        }
        return WorkFlowStatus.FAILED.toValue();
    }

    @Override
    public void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long taskRecordId, Boolean status) {
        LOGGER.info("setAppDeployStatus:pipelineRecordId: {} stageRecordId: {} taskId: {}", pipelineRecordId, stageRecordId, taskRecordId);
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(stageRecordId);
        if (status) {
            if (stageRecordE.getIsParallel() == 1) {
                List<PipelineTaskRecordE> taskRecordEList = taskRecordRepository.queryByStageRecordId(stageRecordId, null);
                List<PipelineTaskRecordE> taskSuccessRecordList = taskRecordEList.stream().filter(t -> t.getStatus().equals(WorkFlowStatus.SUCCESS.toValue())).collect(Collectors.toList());
                if (taskRecordEList.size() == taskSuccessRecordList.size() && !pipelineRecordE.getStatus().equals(WorkFlowStatus.FAILED.toValue())) {
                    startNextTask(taskRecordId, pipelineRecordId, stageRecordId);
                }
            } else {
                startNextTask(taskRecordId, pipelineRecordId, stageRecordId);
            }
        } else {
            if (stageRecordE.getIsParallel() == 1) {
                List<PipelineTaskRecordE> taskRecordEList = taskRecordRepository.queryByStageRecordId(stageRecordId, null);
                List<PipelineTaskRecordE> taskSuccessRecordList = taskRecordEList.stream().filter(t -> t.getStatus().equals(WorkFlowStatus.SUCCESS.toValue())).collect(Collectors.toList());
                List<PipelineTaskRecordE> taskFailedRecordList = taskRecordEList.stream().filter(t -> t.getStatus().equals(WorkFlowStatus.FAILED.toValue())).collect(Collectors.toList());
                if (taskRecordEList.size() == (taskSuccessRecordList.size() + taskFailedRecordList.size())) {
                    workFlowRepository.stopInstance(pipelineRecordE.getProjectId(), pipelineRecordE.getBusinessKey());
                }
            } else {
                workFlowRepository.stopInstance(pipelineRecordE.getProjectId(), pipelineRecordE.getBusinessKey());
            }
        }
    }

    @Override
    public PipelineRecordReqDTO getRecordById(Long projectId, Long pipelineRecordId) {
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        PipelineRecordReqDTO recordReqDTO = new PipelineRecordReqDTO();
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
        BeanUtils.copyProperties(pipelineRecordE, recordReqDTO);
        UserE userE = getTriggerUser(pipelineRecordId, null);
        if (userE != null) {
            IamUserDTO userDTO = ConvertHelper.convert(userE, IamUserDTO.class);
            recordReqDTO.setUserDTO(userDTO);
        }
        List<PipelineStageRecordDTO> recordDTOList = ConvertHelper.convertList(stageRecordRepository.queryByPipeRecordId(pipelineRecordId, null), PipelineStageRecordDTO.class);
        for (int i = 0; i < recordDTOList.size(); i++) {
            PipelineStageRecordDTO stageRecordDTO = recordDTOList.get(i);
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
            List<PipelineTaskRecordDTO> taskRecordDTOS = taskRecordRepository.queryByStageRecordId(stageRecordDTO.getId(), null).stream().map(r -> {
                PipelineTaskRecordDTO taskRecordDTO = ConvertHelper.convert(r, PipelineTaskRecordDTO.class);
                if (taskRecordDTO.getTaskType().equals(MANUAL)) {
                    taskRecordDTO.setUserDTOList(getTaskAuditUsers(r.getAuditUser(), r.getId()));
                    if (r.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                        recordReqDTO.setType(TASK);
                        recordReqDTO.setStageRecordId(r.getStageRecordId());
                        recordReqDTO.setTaskRecordId(r.getId());
                        recordReqDTO.setStageName(stageRecordRepository.queryById(r.getStageRecordId()).getStageName());
                        recordReqDTO.setExecute(checkTaskTriggerPermission(r.getId()));
                    }
                } else {
                    taskRecordDTO.setEnvPermission(getTaskEnvPermission(projectE));
                }
                return taskRecordDTO;
            }).collect(Collectors.toList());
            stageRecordDTO.setTaskRecordDTOS(taskRecordDTOS);
        }
        if (pipelineRecordE.getStatus().equals(WorkFlowStatus.FAILED.toValue())) {
            List<Long> pipelineEnvIds = taskRecordRepository.queryAllAutoTaskRecord(pipelineRecordId).stream().map(PipelineTaskRecordE::getEnvId).collect(Collectors.toList());
            if (checkRecordTriggerPermission(pipelineRecordE.getId(), null) && checkPipelineEnvPermission(projectE, pipelineEnvIds)) {
                recordReqDTO.setExecute(true);
            }
        }
        recordReqDTO.setStageRecordDTOS(recordDTOList);
        return recordReqDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(Long projectId, Long pipelineRecordId) {
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
        String bpmDefinition = pipelineRecordE.getBpmDefinition();
        DevopsPipelineDTO pipelineDTO = gson.fromJson(bpmDefinition, DevopsPipelineDTO.class);
        String uuid = GenerateUUID.generateUUID();
        pipelineDTO.setBusinessKey(uuid);
        CustomUserDetails details = DetailsHelper.getUserDetails();
        createWorkFlow(projectId, pipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
        //清空之前数据
        pipelineRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        pipelineRecordE.setBusinessKey(uuid);
        pipelineRecordE.setErrorInfo("");
        pipelineRecordRepository.update(pipelineRecordE);
        stageRecordRepository.queryByPipeRecordId(pipelineRecordId, null).forEach(t -> {
            t.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
            t.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis()));
            stageRecordRepository.update(t);
            taskRecordRepository.queryByStageRecordId(t.getId(), null).forEach(taskRecordE -> {
                taskRecordE.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
                taskRecordRepository.createOrUpdate(taskRecordE);
                if (taskRecordE.getTaskType().equals(MANUAL)) {
                    pipelineUserRelRecordRepository.deleteByIds(pipelineRecordId, t.getId(), taskRecordE.getId());
                }
            });
        });
        //更新第一阶段
        if (pipelineRecordE.getTriggerType().equals(MANUAL)) {
            updateFirstStage(pipelineRecordId);
        }
    }

    @Override
    public List<PipelineRecordListDTO> queryByPipelineId(Long pipelineId) {
        return pipelineRecordRepository.queryByPipelineId(pipelineId).stream()
                .sorted(comparing(PipelineRecordE::getId).reversed())
                .map(t -> new PipelineRecordListDTO(t.getId(), t.getCreationDate())).collect(Collectors.toList());
    }

    @Override
    public void checkName(Long projectId, String name) {
        pipelineRepository.checkName(projectId, name);
    }

    @Override
    public List<PipelineDTO> listPipelineDTO(Long projectId) {
        return ConvertHelper.convertList(pipelineRepository.queryByProjectId(projectId), PipelineDTO.class);
    }

    @Override
    public List<UserDTO> getAllUsers(Long projectId) {
        return iamRepository.getAllMember(projectId);
    }

    @Override
    public void updateStatus(Long pipelineRecordId, Long stageRecordId, String status, String errorInfo) {
        if (pipelineRecordId != null) {
            PipelineRecordE pipelineRecordE = new PipelineRecordE();
            pipelineRecordE.setId(pipelineRecordId);
            pipelineRecordE.setStatus(status);
            pipelineRecordE.setErrorInfo(errorInfo);
            pipelineRecordRepository.update(pipelineRecordE);
        }
        if (stageRecordId != null) {
            PipelineStageRecordE stageRecordE = new PipelineStageRecordE();
            stageRecordE.setId(stageRecordId);
            stageRecordE.setStatus(status);
            stageRecordRepository.createOrUpdate(stageRecordE);
        }
    }

    /**
     * 执行自动部署流水线
     */
    @Override
    public void executeAutoDeploy(Long pipelineId) {
        PipelineE pipelineE = pipelineRepository.queryById(pipelineId);
        CutomerContextUtil.setUserId(pipelineE.getCreatedBy());
        PipelineRecordE pipelineRecordE = new PipelineRecordE(pipelineId, pipelineE.getTriggerType(), pipelineE.getProjectId(), WorkFlowStatus.RUNNING.toValue(), pipelineE.getName());
        String uuid = GenerateUUID.generateUUID();
        pipelineRecordE.setBusinessKey(uuid);
        pipelineRecordE = pipelineRecordRepository.create(pipelineRecordE);
        DevopsPipelineDTO devopsPipelineDTO = createWorkFlowDTO(pipelineRecordE.getId(), pipelineId, uuid);
        pipelineRecordE.setBpmDefinition(gson.toJson(devopsPipelineDTO));
        pipelineRecordE = pipelineRecordRepository.update(pipelineRecordE);
        try {
            CustomUserDetails details = DetailsHelper.getUserDetails();
            createWorkFlow(pipelineE.getProjectId(), devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
            List<PipelineStageRecordE> stageRecordES = stageRecordRepository.queryByPipeRecordId(pipelineRecordE.getId(), null);
            if (stageRecordES != null && stageRecordES.size() > 0) {
                PipelineStageRecordE stageRecordE = stageRecordES.get(0);
                stageRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
                stageRecordE.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis()));
                stageRecordRepository.createOrUpdate(stageRecordE);
            }
        } catch (Exception e) {
            pipelineRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
            pipelineRecordRepository.update(pipelineRecordE);
            throw new CommonException(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void failed(Long projectId, Long recordId) {
        PipelineRecordE recordE = pipelineRecordRepository.queryById(recordId);
        if (!recordE.getStatus().equals(WorkFlowStatus.RUNNING.toValue())) {
            throw new CommonException("error.pipeline.record.status");
        }
        List<PipelineStageRecordE> stageRecordES = stageRecordRepository.queryByPipeRecordId(recordId, null);
        for (PipelineStageRecordE stageRecordE : stageRecordES) {
            if (stageRecordE.getStatus().equals(WorkFlowStatus.RUNNING.toValue()) || stageRecordE.getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                updateStatus(recordId, null, WorkFlowStatus.FAILED.toValue(), "Force failure");
                stageRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
                stageRecordE.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis() - TypeUtil.objToLong(stageRecordE.getExecutionTime())));
                stageRecordRepository.createOrUpdate(stageRecordE);
                Optional<PipelineTaskRecordE> optional = taskRecordRepository.queryByStageRecordId(stageRecordE.getId(), null).stream().filter(t -> t.getStatus().equals(WorkFlowStatus.RUNNING.toValue())).findFirst();
                if (optional.isPresent()) {
                    PipelineTaskRecordE taskRecordE = optional.get();
                    taskRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
                    taskRecordRepository.createOrUpdate(taskRecordE);
                }
                break;
            }
        }
        sendFailedSiteMessage(recordId, recordE.getCreatedBy());
    }

    @Override
    public void sendSiteMessage(Long pipelineRecordId, String type, List<NoticeSendDTO.User> users, Map<String, Object> params) {
        NotifyDTO notifyDTO = new NotifyDTO();
        notifyDTO.setTargetUsers(users);
        notifyDTO.setSourceId(pipelineRecordId);
        notifyDTO.setCode(type);
        notifyDTO.setCustomizedSendingTypes(Collections.singletonList("siteMessage"));
        PipelineRecordE recordE = pipelineRecordRepository.queryById(pipelineRecordId);
        params.put("pipelineId", recordE.getPipelineId().toString());
        params.put("pipelineName", recordE.getPipelineName());
        params.put("pipelineRecordId", pipelineRecordId.toString());
        params.put("projectId", recordE.getProjectId().toString());
        ProjectVO projectE = iamRepository.queryIamProject(recordE.getProjectId());
        params.put("projectName", projectE.getName());
        params.put("organizationId", projectE.getOrganization().getId().toString());
        notifyDTO.setParams(params);
        notifyClient.sendMessage(notifyDTO);
    }

    private void sendFailedSiteMessage(Long pipelineRecordId, Long userId) {
        UserE userE = iamRepository.queryUserByUserId(userId);
        NoticeSendDTO.User user = new NoticeSendDTO.User();
        user.setEmail(userE.getEmail());
        user.setId(userE.getId());
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
            UserE userE = iamRepository.queryUserByUserId(TypeUtil.objToLong(t));
            user.setEmail(userE.getEmail());
            user.setId(userE.getId());
            userList.add(user);
        });
        Map<String, Object> params = new HashMap<>();
        params.put("stageName", stageName);
        UserE userE = iamRepository.queryUserByUserId(GitUserNameUtil.getUserId().longValue());
        params.put("auditName", userE.getLoginName());
        params.put("realName", userE.getRealName());
        sendSiteMessage(pipelineRecordId, type, userList, params);
    }

    /**
     * 校验会签任务是否全部审核过
     *
     * @return
     */
    private Boolean checkCouAllApprove(List<IamUserDTO> userDTOS, Long taskId, Long taskRecordId) {
        List<Long> userList = pipelineUserRelRepository.listByOptions(null, null, taskId)
                .stream().map(PipelineUserRelE::getUserId).collect(Collectors.toList());
        List<Long> userRecordList = pipelineUserRelRecordRepository.queryByRecordId(null, null, taskRecordId)
                .stream().map(PipelineUserRecordRelE::getUserId).collect(Collectors.toList());
        //是否全部同意
        if (userList.size() != userRecordList.size()) {
            List<Long> userListUnExe = new ArrayList<>(userList);
            userList.forEach(u -> {
                if (userRecordList.contains(u)) {
                    userListUnExe.remove(u);
                }
            });
            userRecordList.forEach(u -> {
                IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(u), IamUserDTO.class);
                userDTO.setAudit(true);
                userDTOS.add(userDTO);
            });
            userListUnExe.forEach(u -> {
                IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(u), IamUserDTO.class);
                userDTO.setAudit(false);
                userDTOS.add(userDTO);
            });
            return false;
        }
        return true;
    }

    private ApplicationVersionE getDeployVersion(Long pipelineRecordId, Long stageRecordId, PipelineTaskRecordE taskRecordE) {
        List<ApplicationVersionE> versionES = versionRepository.listByAppId(taskRecordE.getApplicationId(), null);
        Integer index = -1;
        for (int i = 0; i < versionES.size(); i++) {
            ApplicationVersionE versionE = versionES.get(i);
            if (taskRecordE.getTriggerVersion() == null || taskRecordE.getTriggerVersion().isEmpty()) {
                index = i;
                break;
            } else {
                List<String> list = Arrays.asList(taskRecordE.getTriggerVersion().split(","));
                Optional<String> branch = list.stream().filter(t -> versionE.getVersion().contains(t)).findFirst();
                if (branch.isPresent() && !branch.get().isEmpty()) {
                    index = i;
                    break;
                }
            }
        }
        if (index == -1) {
            setPipelineFailed(pipelineRecordId, stageRecordId, taskRecordE, "No version can trigger deploy");
            throw new CommonException("error.version.can.trigger.deploy");
        }
        return versionES.get(index);
    }

    private void removeStages(List<PipelineStageDTO> stageDTOList, Long pipelineId) {
        List<Long> newStageIds = ConvertHelper.convertList(stageDTOList, PipelineStageE.class)
                .stream().filter(t -> t.getId() != null)
                .map(PipelineStageE::getId).collect(Collectors.toList());
        stageRepository.queryByPipelineId(pipelineId).forEach(t -> {
            if (!newStageIds.contains(t.getId())) {
                stageRepository.delete(t.getId());
                updateUserRel(null, null, t.getId(), null);
                pipelineTaskRepository.queryByStageId(t.getId()).forEach(taskE -> {
                    taskRecordRepository.delete(taskE.getId());
                    updateUserRel(null, null, null, taskE.getId());
                });
            }
        });
    }

    private void removeTasks(List<PipelineTaskDTO> taskDTOList, Long stageId) {
        List<Long> newTaskIds = taskDTOList.stream()
                .filter(t -> t.getId() != null)
                .map(PipelineTaskDTO::getId)
                .collect(Collectors.toList());
        pipelineTaskRepository.queryByStageId(stageId).forEach(t -> {
            if (!newTaskIds.contains(t.getId())) {
                pipelineTaskRepository.deleteById(t.getId());
                if (t.getType().equals(MANUAL)) {
                    updateUserRel(null, null, null, t.getId());
                } else {
                    appDeployRepository.deleteById(t.getAppDeployId());
                }
            }
        });
    }

    private PipelineStageE createOrUpdateStage(PipelineStageDTO stageDTO, Long pipelineId, Long projectId) {
        PipelineStageE stageE = ConvertHelper.convert(stageDTO, PipelineStageE.class);
        if (stageE.getId() != null) {
            stageRepository.update(stageE);
        } else {
            stageE.setPipelineId(pipelineId);
            stageE.setProjectId(projectId);
            stageE = stageRepository.create(stageE);
            createUserRel(stageDTO.getStageUserRelDTOS(), null, stageE.getId(), null);
        }
        updateUserRel(stageDTO.getStageUserRelDTOS(), null, stageE.getId(), null);
        return stageE;
    }

    private void createOrUpdateTask(PipelineTaskDTO taskDTO, Long stageId, Long projectId) {
        if (taskDTO.getId() != null) {
            if (AUTO.equals(taskDTO.getType())) {
                taskDTO.setAppDeployId(appDeployRepository.update(ConvertHelper.convert(taskDTO.getAppDeployDTOS(), PipelineAppDeployE.class)).getId());
            }
            Long taskId = pipelineTaskRepository.update(ConvertHelper.convert(taskDTO, PipelineTaskE.class)).getId();
            if (MANUAL.equals(taskDTO.getType())) {
                updateUserRel(taskDTO.getTaskUserRelDTOS(), null, null, taskId);
            }
        } else {
            createPipelineTask(taskDTO, projectId, stageId);
        }
    }

    private Boolean getTaskEnvPermission(ProjectVO projectE) {
        Boolean envPermission = true;
        if (!iamRepository.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE)) {
            List<Long> envIds = devopsEnvUserPermissionRepository
                    .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                    .filter(DevopsEnvUserPermissionE::getPermitted)
                    .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
            envPermission = envIds.contains(DetailsHelper.getUserDetails().getUserId());
        }
        return envPermission;
    }

    private List<IamUserDTO> getTaskAuditUsers(String auditUser, Long taskRecordId) {
        List<IamUserDTO> userDTOS = new ArrayList<>();
        //获取已经审核人员
        List<Long> userIds = pipelineUserRelRecordRepository.queryByRecordId(null, null, taskRecordId)
                .stream().map(PipelineUserRecordRelE::getUserId).collect(Collectors.toList());
        userIds.forEach(userId -> {
            IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(userId), IamUserDTO.class);
            userDTO.setAudit(true);
            userDTOS.add(userDTO);
        });
        //获取指定审核人员
        if (auditUser != null) {
            List<String> auditUserIds = Arrays.asList(auditUser.split(","));
            auditUserIds.forEach(userId -> {
                IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(TypeUtil.objToLong(userId)), IamUserDTO.class);
                userDTO.setAudit(false);
                userDTOS.add(userDTO);
            });
        }
        return userDTOS;
    }

    private List<IamUserDTO> getStageAuditUsers(Long stageRecordId, Long lastStageRecordId) {
        List<IamUserDTO> userDTOS = new ArrayList<>();
        List<Long> userIds = pipelineUserRelRecordRepository.queryByRecordId(null, lastStageRecordId, null)
                .stream().map(PipelineUserRecordRelE::getUserId).collect(Collectors.toList());
        Boolean audit = !userIds.isEmpty();
        if (userIds.isEmpty()) {
            userIds = pipelineUserRelRepository.listByOptions(null, stageRecordId, null)
                    .stream().map(PipelineUserRelE::getUserId).collect(Collectors.toList());
        }
        userIds.forEach(u -> {
            IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(u), IamUserDTO.class);
            userDTO.setAudit(audit);
            userDTOS.add(userDTO);
        });
        return userDTOS;
    }

    private PipelineStageRecordE createStageRecord(PipelineStageE stageE, Long pipelineRecordId, List<PipelineUserRelE> stageRelEList) {
        PipelineStageRecordE recordE = new PipelineStageRecordE();
        BeanUtils.copyProperties(stageE, recordE);
        recordE.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
        recordE.setStageId(stageE.getId());
        recordE.setPipelineRecordId(pipelineRecordId);
        recordE.setId(null);
        if (stageE.getTriggerType().equals(MANUAL)) {
            recordE.setAuditUser(StringUtils.join(stageRelEList.stream().map(PipelineUserRelE::getUserId).toArray(), ","));
        }
        return stageRecordRepository.createOrUpdate(recordE);
    }

    private PipelineTaskRecordE createTaskRecordE(PipelineTaskE taskE, Long stageRecordId, List<PipelineUserRelE> taskUserRels) {
        //创建task记录
        PipelineTaskRecordE taskRecordE = new PipelineTaskRecordE();
        BeanUtils.copyProperties(taskE, taskRecordE);
        taskRecordE.setTaskId(taskE.getId());
        taskRecordE.setTaskType(taskE.getType());
        taskRecordE.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
        taskRecordE.setStageRecordId(stageRecordId);
        if (taskE.getAppDeployId() != null) {
            PipelineAppDeployE appDeployE = appDeployRepository.queryById(taskE.getAppDeployId());
            BeanUtils.copyProperties(appDeployE, taskRecordE);
            if (appDeployE.getInstanceName() == null) {
                taskRecordE.setInstanceName(applicationInstanceRepository.selectById(appDeployE.getInstanceId()).getCode());
            }
            taskRecordE.setInstanceId(null);
            taskRecordE.setValueId(appDeployE.getValueId());
        }
        taskRecordE.setAuditUser(StringUtils.join(taskUserRels.stream().map(PipelineUserRelE::getUserId).toArray(), ","));
        taskRecordE.setId(null);
        return taskRecordRepository.createOrUpdate(taskRecordE);
    }

    private PipelineTaskRecordE getFirstTask(Long pipelineRecordId) {
        return taskRecordRepository.queryByStageRecordId(stageRecordRepository.queryByPipeRecordId(pipelineRecordId, null).get(0).getId(), null).get(0);
    }

    private List<PipelineAppDeployE> getAllAppDeploy(Long pipelineId) {
        List<PipelineAppDeployE> appDeployEList = new ArrayList<>();
        stageRepository.queryByPipelineId(pipelineId).forEach(stageE -> {
            pipelineTaskRepository.queryByStageId(stageE.getId()).forEach(taskE -> {
                if (taskE.getAppDeployId() != null) {
                    PipelineAppDeployE appDeployE = appDeployRepository.queryById(taskE.getAppDeployId());
                    appDeployEList.add(appDeployE);
                }
            });
        });
        return appDeployEList;
    }

    private void updateFirstStage(Long pipelineRecordId) {
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryByPipeRecordId(pipelineRecordId, null).get(0);
        stageRecordE.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis()));
        stageRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        stageRecordRepository.createOrUpdate(stageRecordE);
        if (isEmptyStage(stageRecordE.getId())) {
            startEmptyStage(pipelineRecordId, stageRecordE.getId());
        } else {
            PipelineTaskRecordE taskRecordE = getFirstTask(pipelineRecordId);
            if (taskRecordE.getTaskType().equals(MANUAL)) {
                startNextTask(taskRecordE, pipelineRecordId, stageRecordE.getId());
            }
        }
    }

    private void createPipelineTask(PipelineTaskDTO t, Long projectId, Long stageId) {
        t.setProjectId(projectId);
        t.setStageId(stageId);
        if (AUTO.equals(t.getType())) {
            PipelineAppDeployE appDeployE = ConvertHelper.convert(t.getAppDeployDTOS(), PipelineAppDeployE.class);
            appDeployE.setProjectId(projectId);
            t.setAppDeployId(appDeployRepository.create(appDeployE).getId());
        }
        Long taskId = pipelineTaskRepository.create(ConvertHelper.convert(t, PipelineTaskE.class)).getId();
        if (MANUAL.equals(t.getType())) {
            createUserRel(t.getTaskUserRelDTOS(), null, null, taskId);
        }
    }

    private UserE getTriggerUser(Long pipelineRecordId, Long stageRecordId) {
        List<PipelineUserRecordRelE> taskUserRecordRelES = pipelineUserRelRecordRepository.queryByRecordId(pipelineRecordId, stageRecordId, null);
        if (taskUserRecordRelES != null && taskUserRecordRelES.size() > 0) {
            Long triggerUserId = taskUserRecordRelES.get(0).getUserId();
            return iamRepository.queryUserByUserId(triggerUserId);
        }
        return null;
    }

    private void startNextTask(Long taskRecordId, Long pipelineRecordId, Long stageRecordId) {
        PipelineTaskRecordE taskRecordE = taskRecordRepository.queryById(taskRecordId);
        PipelineTaskRecordE nextTaskRecord = getNextTask(taskRecordId);
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(stageRecordId);
        //属于阶段的最后一个任务
        if (nextTaskRecord == null) {
            Long time = System.currentTimeMillis() - TypeUtil.objToLong(stageRecordE.getExecutionTime());
            stageRecordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
            stageRecordE.setExecutionTime(time.toString());
            stageRecordRepository.createOrUpdate(stageRecordE);
            //属于pipeline最后一个任务
            PipelineStageRecordE nextStageRecord = getNextStage(taskRecordE.getStageRecordId());
            PipelineRecordE recordE = pipelineRecordRepository.queryById(pipelineRecordId);
            if (nextStageRecord == null) {
                LOGGER.info("任务成功了");
                recordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
                pipelineRecordRepository.update(recordE);
                UserE userE = iamRepository.queryUserByUserId(recordE.getCreatedBy());
                NoticeSendDTO.User user = new NoticeSendDTO.User();
                user.setEmail(userE.getEmail());
                user.setId(userE.getId());
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
    private void startNextStageRecord(Long stageRecordId, PipelineRecordE recordE) {
        PipelineStageRecordE nextStageRecordE = getNextStage(stageRecordId);
        nextStageRecordE.setExecutionTime(TypeUtil.objToString(System.currentTimeMillis()));
        stageRecordRepository.createOrUpdate(nextStageRecordE);
        if (stageRecordRepository.queryById(stageRecordId).getTriggerType().equals(AUTO)) {
            if (!isEmptyStage(nextStageRecordE.getId())) {
                nextStageRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
                List<PipelineTaskRecordE> list = taskRecordRepository.queryByStageRecordId(nextStageRecordE.getId(), null);
                if (list != null && list.size() > 0) {
                    if (list.get(0).getTaskType().equals(MANUAL)) {
                        startNextTask(list.get(0), recordE.getId(), nextStageRecordE.getId());
                    }
                }
            } else {
                startEmptyStage(recordE.getId(), nextStageRecordE.getId());
            }
        } else {
            List<NoticeSendDTO.User> userList = new ArrayList<>();
            String auditUser = stageRecordRepository.queryById(stageRecordId).getAuditUser();
            if (auditUser != null && !auditUser.isEmpty()) {
                List<String> userIds = Arrays.asList(auditUser.split(","));
                userIds.forEach(t -> {
                    UserE userE = iamRepository.queryUserByUserId(TypeUtil.objToLong(t));
                    NoticeSendDTO.User user = new NoticeSendDTO.User();
                    user.setEmail(userE.getEmail());
                    user.setId(userE.getId());
                    userList.add(user);
                });
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("stageName", stageRecordRepository.queryById(stageRecordId).getStageName());
            sendSiteMessage(recordE.getId(), PipelineNoticeType.PIPELINEAUDIT.toValue(), userList, params);
            updateStatus(recordE.getId(), null, WorkFlowStatus.PENDINGCHECK.toValue(), null);
        }
    }

    private Boolean isEmptyStage(Long stageRecordId) {
        List<PipelineTaskRecordE> taskRecordEList = taskRecordRepository.queryByStageRecordId(stageRecordId, null);
        return taskRecordEList == null || taskRecordEList.isEmpty();
    }

    private void startEmptyStage(Long pipelineRecordId, Long stageRecordId) {
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(stageRecordId);
        stageRecordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
        Long time = 0L;
        stageRecordE.setExecutionTime(time.toString());
        stageRecordRepository.createOrUpdate(stageRecordE);
        PipelineStageRecordE nextStageRecordE = getNextStage(stageRecordE.getId());
        if (nextStageRecordE != null) {
            startNextStageRecord(nextStageRecordE.getId(), pipelineRecordE);
        } else {
            updateStatus(pipelineRecordId, null, WorkFlowStatus.SUCCESS.toValue(), null);
            UserE userE = iamRepository.queryUserByUserId(pipelineRecordE.getCreatedBy());
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            user.setEmail(userE.getEmail());
            user.setId(userE.getId());
            sendSiteMessage(pipelineRecordId, PipelineNoticeType.PIPELINESUCCESS.toValue(), Collections.singletonList(user), new HashMap<>());
        }
    }

    private void startNextTask(PipelineTaskRecordE taskRecordE, Long pipelineRecordId, Long stageRecordId) {
        if (!taskRecordE.getTaskType().equals(AUTO)) {
            taskRecordE.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
            taskRecordRepository.createOrUpdate(taskRecordE);
            updateStatus(pipelineRecordId, stageRecordId, WorkFlowStatus.PENDINGCHECK.toValue(), null);
            List<NoticeSendDTO.User> userList = new ArrayList<>();
            String auditUser = taskRecordE.getAuditUser();
            if (auditUser != null && !auditUser.isEmpty()) {
                List<String> userIds = Arrays.asList(auditUser.split(","));
                userIds.forEach(t -> {
                    UserE userE = iamRepository.queryUserByUserId(TypeUtil.objToLong(t));
                    NoticeSendDTO.User user = new NoticeSendDTO.User();
                    user.setEmail(userE.getEmail());
                    user.setId(userE.getId());
                    userList.add(user);
                });
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("stageName", stageRecordRepository.queryById(stageRecordId).getStageName());
            sendSiteMessage(pipelineRecordId, PipelineNoticeType.PIPELINEAUDIT.toValue(), userList, params);
        }
    }

    private PipelineStageRecordE getNextStage(Long stageRecordId) {
        List<PipelineStageRecordE> list = stageRecordRepository.queryByPipeRecordId(stageRecordRepository.queryById(stageRecordId).getPipelineRecordId(), null);
        return list.stream().filter(t -> t.getId() > stageRecordId).findFirst().orElse(null);
    }

    private PipelineTaskRecordE getNextTask(Long taskRecordId) {
        List<PipelineTaskRecordE> list = taskRecordRepository.queryByStageRecordId(taskRecordRepository.queryById(taskRecordId).getStageRecordId(), null);
        return list.stream().filter(t -> t.getId() > taskRecordId).findFirst().orElse(null);
    }

    private Boolean checkTriggerPermission(Long pipelineId) {
        List<Long> userIds = pipelineUserRelRepository.listByOptions(pipelineId, null, null)
                .stream()
                .map(PipelineUserRelE::getUserId)
                .collect(Collectors.toList());
        return userIds.contains(DetailsHelper.getUserDetails().getUserId());
    }

    private Boolean checkRecordTriggerPermission(Long pipelineRecordId, Long stageRecordId) {
        String auditUser = null;
        if (pipelineRecordId != null) {
            PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
            if (pipelineRecordE.getTriggerType().equals(AUTO)) {
                return true;
            }
            auditUser = pipelineRecordE.getAuditUser();
        }
        if (stageRecordId != null) {
            auditUser = stageRecordRepository.queryById(stageRecordId).getAuditUser();
        }
        List<String> userIds = new ArrayList<>();
        if (auditUser != null && !auditUser.isEmpty()) {
            userIds = Arrays.asList(auditUser.split(","));
        }
        return userIds.contains(TypeUtil.objToString(DetailsHelper.getUserDetails().getUserId()));
    }

    private Boolean checkTaskTriggerPermission(Long taskRecordId) {
        PipelineTaskRecordE taskRecordE = taskRecordRepository.queryById(taskRecordId);
        List<String> userIds = new ArrayList<>();
        if (taskRecordE.getAuditUser() != null && !taskRecordE.getAuditUser().isEmpty()) {
            userIds = Arrays.asList(taskRecordE.getAuditUser().split(","));
        }
        //未执行
        List<String> userIdsUnExe = new ArrayList<>(userIds);
        if (taskRecordE.getIsCountersigned() == 1) {
            List<Long> userIdRecords = pipelineUserRelRecordRepository.queryByRecordId(null, null, taskRecordId)
                    .stream()
                    .map(PipelineUserRecordRelE::getUserId)
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
                PipelineUserRelE userRelE = new PipelineUserRelE(t, pipelineId, stageId, taskId);
                pipelineUserRelRepository.create(userRelE);
            });
        }
    }

    private void updateUserRel(List<Long> relDTOList, Long pipelineId, Long stageId, Long taskId) {
        List<Long> addUserRelEList = new ArrayList<>();
        List<Long> relEList = pipelineUserRelRepository.listByOptions(pipelineId, stageId, taskId).stream().map(PipelineUserRelE::getUserId).collect(Collectors.toList());
        if (relDTOList != null) {
            relDTOList.forEach(relE -> {
                if (!relEList.contains(relE)) {
                    addUserRelEList.add(relE);
                } else {
                    relEList.remove(relE);
                }
            });
            addUserRelEList.forEach(addUserId -> {
                PipelineUserRelE addUserRelE = new PipelineUserRelE(addUserId, pipelineId, stageId, taskId);
                pipelineUserRelRepository.create(addUserRelE);
            });
        }
        relEList.forEach(delUserId -> {
            PipelineUserRelE addUserRelE = new PipelineUserRelE(delUserId, pipelineId, stageId, taskId);
            pipelineUserRelRepository.delete(addUserRelE);
        });
    }

    private void createWorkFlow(Long projectId, DevopsPipelineDTO pipelineDTO, String loginName, Long userId, Long orgId) {

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
                            workFlowRepository.create(projectId, pipelineDTO);
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
                            workFlowRepository.approveUserTask(projectId, businessKey);
                        } catch (Exception e) {
                            throw new CommonException(e);
                        }
                    }
                });
    }

    private Boolean checkPipelineEnvPermission(ProjectVO projectE, List<Long> pipelineEnvIds) {
        Boolean index = true;
        if (!iamRepository.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE)) {
            List<Long> envIds = devopsEnvUserPermissionRepository
                    .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                    .filter(DevopsEnvUserPermissionE::getPermitted)
                    .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
            for (Long pipelineEnvId : pipelineEnvIds) {
                if (!envIds.contains(pipelineEnvId)) {
                    index = false;
                    break;
                }
            }
        }
        return index;
    }

    private void setPipelineFailed(Long pipelineRecordId, Long stageRecordId, PipelineTaskRecordE taskRecordE, String errorInfo) {
        taskRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
        taskRecordRepository.createOrUpdate(taskRecordE);
        updateStatus(pipelineRecordId, stageRecordId, WorkFlowStatus.FAILED.toValue(), errorInfo);
    }

    private String getAuditResult(PipelineUserRecordRelDTO recordRelDTO, PipelineRecordE pipelineRecordE, String auditUser, String stageName) {
        Boolean result = true;
        String status;
        if (recordRelDTO.getIsApprove()) {
            try {
                CustomUserDetails details = DetailsHelper.getUserDetails();
                approveWorkFlow(pipelineRecordE.getProjectId(), pipelineRecordRepository.queryById(recordRelDTO.getPipelineRecordId()).getBusinessKey(), details.getUsername(), details.getUserId(), details.getOrganizationId());
            } catch (Exception e) {
                result = false;
                sendFailedSiteMessage(recordRelDTO.getPipelineRecordId(), pipelineRecordE.getCreatedBy());
            }
            status = result ? WorkFlowStatus.SUCCESS.toValue() : WorkFlowStatus.FAILED.toValue();
            if (STAGE.equals(recordRelDTO.getType())) {
                status = result ? WorkFlowStatus.RUNNING.toValue() : WorkFlowStatus.FAILED.toValue();
            }
        } else {
            status = WorkFlowStatus.STOP.toValue();
            auditUser = auditUser.contains(pipelineRecordE.getCreatedBy().toString()) ? auditUser : auditUser + "," + pipelineRecordE.getCreatedBy();
            sendAuditSiteMassage(PipelineNoticeType.PIPELINESTOP.toValue(), auditUser, recordRelDTO.getPipelineRecordId(), stageName);
        }
        return status;
    }
}

