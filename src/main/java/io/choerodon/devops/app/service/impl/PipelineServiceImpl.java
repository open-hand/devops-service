package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.zaxxer.hikari.util.UtilityElf;
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

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.dto.ApplicationDeployDTO;
import io.choerodon.devops.api.dto.CheckAuditDTO;
import io.choerodon.devops.api.dto.DevopsEnviromentRepDTO;
import io.choerodon.devops.api.dto.IamUserDTO;
import io.choerodon.devops.api.dto.PipelineAppDeployDTO;
import io.choerodon.devops.api.dto.PipelineCheckDeployDTO;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.api.dto.PipelineRecordDTO;
import io.choerodon.devops.api.dto.PipelineRecordListDTO;
import io.choerodon.devops.api.dto.PipelineRecordReqDTO;
import io.choerodon.devops.api.dto.PipelineReqDTO;
import io.choerodon.devops.api.dto.PipelineStageDTO;
import io.choerodon.devops.api.dto.PipelineStageRecordDTO;
import io.choerodon.devops.api.dto.PipelineTaskDTO;
import io.choerodon.devops.api.dto.PipelineTaskRecordDTO;
import io.choerodon.devops.api.dto.PipelineUserRecordRelDTO;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.api.eventhandler.DemoEnvSetupSagaHandler;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.ApplicationVersionService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.entity.DevopsEnvUserPermissionE;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;
import io.choerodon.devops.domain.application.entity.PipelineE;
import io.choerodon.devops.domain.application.entity.PipelineRecordE;
import io.choerodon.devops.domain.application.entity.PipelineStageE;
import io.choerodon.devops.domain.application.entity.PipelineStageRecordE;
import io.choerodon.devops.domain.application.entity.PipelineTaskE;
import io.choerodon.devops.domain.application.entity.PipelineTaskRecordE;
import io.choerodon.devops.domain.application.entity.PipelineUserRecordRelE;
import io.choerodon.devops.domain.application.entity.PipelineUserRelE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository;
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.domain.application.repository.PipelineRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineRepository;
import io.choerodon.devops.domain.application.repository.PipelineStageRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineStageRepository;
import io.choerodon.devops.domain.application.repository.PipelineTaskRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineTaskRepository;
import io.choerodon.devops.domain.application.repository.PipelineUserRelRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineUserRelRepository;
import io.choerodon.devops.domain.application.repository.PipelineValueRepository;
import io.choerodon.devops.domain.application.repository.WorkFlowRepository;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;
import io.choerodon.devops.infra.common.util.CutomerContextUtil;
import io.choerodon.devops.infra.common.util.GenerateUUID;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.WorkFlowStatus;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineTaskDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
    private DevopsEnvironmentService environmentService;
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
    private PipelineValueRepository valueRepository;
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
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private ApplicationVersionService versionService;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;

    @Override
    public Page<PipelineDTO> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Page<PipelineDTO> pipelineDTOS = ConvertPageHelper.convertPage(pipelineRepository.listByOptions(projectId, pageRequest, params), PipelineDTO.class);
        Page<PipelineDTO> page = new Page<>();
        BeanUtils.copyProperties(pipelineDTOS, page);
        page.setContent(pipelineDTOS.getContent().stream().peek(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreatedBy());
            if (userE == null) {
                throw new CommonException("error.get.create.user");
            }
            t.setCreateUserName(userE.getLoginName());
            t.setCreateUserUrl(userE.getImageUrl());
            t.setCreateUserRealName(userE.getRealName());
            if (t.getIsEnabled() == 1) {
                t.setExecute(pipelineUserRelRepository.listByOptions(t.getId(), null, null)
                        .stream()
                        .map(PipelineUserRelE::getUserId)
                        .collect(Collectors.toList())
                        .contains(DetailsHelper.getUserDetails().getUserId()));
            } else {
                t.setExecute(false);
            }
            //运行中的流水线不可编辑
            List<PipelineRecordE> list = pipelineRecordRepository.queryByPipelineId(t.getId()).stream()
                    .filter(recordE -> recordE.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue()) || recordE.getStatus().equals(WorkFlowStatus.RUNNING.toValue()))
                    .collect(Collectors.toList());
            t.setEdit(true);
            if (list != null && list.size() > 0) {
                t.setEdit(false);
            } else {
                //是否拥有环境权限.没有环境权限不可编辑
                List<PipelineAppDeployE> appDeployEList = getAllAppDeploy(t.getId());
                if (!iamRepository.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE)) {
                    List<Long> envIds = devopsEnvUserPermissionRepository
                            .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                            .filter(DevopsEnvUserPermissionE::getPermitted)
                            .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
                    for (PipelineAppDeployE appDeployE : appDeployEList) {
                        if (!envIds.contains(appDeployE.getEnvId())) {
                            t.setEdit(false);
                            break;
                        }
                    }
                }
            }
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public Page<PipelineRecordDTO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Page<PipelineRecordDTO> pageRecordDTOS = ConvertPageHelper.convertPage(
                pipelineRecordRepository.listByOptions(projectId, pipelineId, pageRequest, params), PipelineRecordDTO.class);
        List<PipelineRecordDTO> pipelineRecordDTOS = pageRecordDTOS.getContent().stream().map(t -> {
            t.setIndex(false);
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
                            t.setIndex(checkTaskTriggerPermission(taskRecordE.get().getTaskId(), taskRecordE.get().getId()));
                            break;
                        }
                    } else if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                        t.setType(STAGE);
                        t.setStageName(t.getStageDTOList().get(i - 1).getStageName());
                        t.setStageRecordId(t.getStageDTOList().get(i).getId());
                        t.setIndex(checkTriggerPermission(null, t.getStageDTOList().get(i - 1).getStageId()));
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
                if (t.getTriggerType().equals(AUTO)) {
                    t.setIndex(true);
                } else {
                    t.setIndex(checkTriggerPermission(t.getPipelineId(), null));
                }
            }

            List<PipelineAppDeployE> appDeployEList = getAllAppDeploy(t.getPipelineId());
            if (!iamRepository.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE)) {
                List<Long> envIds = devopsEnvUserPermissionRepository
                        .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                        .filter(DevopsEnvUserPermissionE::getPermitted)
                        .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
                for (PipelineAppDeployE appDeployE : appDeployEList) {
                    if (!envIds.contains(appDeployE.getEnvId())) {
                        t.setIndex(false);
                        break;
                    }
                }
            }
            return t;
        }).collect(Collectors.toList());
        pageRecordDTOS.setContent(pipelineRecordDTOS);
        return pageRecordDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineReqDTO create(Long projectId, PipelineReqDTO pipelineReqDTO) {
        //pipeline
        PipelineE pipelineE = ConvertHelper.convert(pipelineReqDTO, PipelineE.class);
        pipelineE.setProjectId(projectId);
        checkName(projectId, pipelineReqDTO.getName());
        pipelineE = pipelineRepository.create(projectId, pipelineE);
        createUserRel(pipelineReqDTO.getPipelineUserRelDTOS(), pipelineE.getId(), null, null);

        //stage
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
            //task
            List<PipelineTaskDTO> taskDTOList = pipelineReqDTO.getPipelineStageDTOS().get(i).getPipelineTaskDTOS();
            if (taskDTOList != null && taskDTOList.size() > 0) {
                taskDTOList.forEach(t -> {
                    AddPipelineTask(t, projectId, stageId);
                });
            }
        }
        return pipelineReqDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineReqDTO update(Long projectId, PipelineReqDTO pipelineReqDTO) {
        pipelineReqDTO.setProjectId(projectId);
        //pipeline
        PipelineE pipelineE = ConvertHelper.convert(pipelineReqDTO, PipelineE.class);
        pipelineE = pipelineRepository.update(projectId, pipelineE);
        updateUserRel(pipelineReqDTO.getPipelineUserRelDTOS(), pipelineE.getId(), null, null);

        Long pipelineId = pipelineE.getId();
        //删除stage
        List<Long> newStageIds = ConvertHelper.convertList(pipelineReqDTO.getPipelineStageDTOS(), PipelineStageE.class)
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

        for (int i = 0; i < pipelineReqDTO.getPipelineStageDTOS().size(); i++) {
            //新增和修改stage
            PipelineStageE stageE = ConvertHelper.convert(pipelineReqDTO.getPipelineStageDTOS().get(i), PipelineStageE.class);
            if (stageE.getId() != null) {
                stageRepository.update(stageE);
            } else {
                stageE.setPipelineId(pipelineId);
                stageE.setProjectId(projectId);
                stageE = stageRepository.create(stageE);
                createUserRel(pipelineReqDTO.getPipelineStageDTOS().get(i).getStageUserRelDTOS(), null, stageE.getId(), null);
            }

            Long stageId = stageE.getId();
            updateUserRel(pipelineReqDTO.getPipelineStageDTOS().get(i).getStageUserRelDTOS(), null, stageId, null);
            //task删除
            List<PipelineTaskDTO> taskDTOList = pipelineReqDTO.getPipelineStageDTOS().get(i).getPipelineTaskDTOS();
            if (taskDTOList != null) {
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
                //task
                taskDTOList.stream().filter(Objects::nonNull).forEach(t -> {
                    if (t.getId() != null) {
                        if (AUTO.equals(t.getType())) {
                            t.setAppDeployId(appDeployRepository.update(ConvertHelper.convert(t.getAppDeployDTOS(), PipelineAppDeployE.class)).getId());
                        }
                        Long taskId = pipelineTaskRepository.update(ConvertHelper.convert(t, PipelineTaskE.class)).getId();
                        if (MANUAL.equals(t.getType())) {
                            updateUserRel(t.getTaskUserRelDTOS(), null, null, taskId);
                        }
                    } else {
                        AddPipelineTask(t, projectId, stageId);
                    }
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
        //回写记录状态
        pipelineRecordRepository.queryByPipelineId(pipelineId).forEach(t -> {
            t.setStatus(WorkFlowStatus.DELETED.toValue());
            pipelineRecordRepository.update(t);
        });
        pipelineUserRelRepository.listByOptions(pipelineId, null, null).forEach(t -> pipelineUserRelRepository.delete(t));
        //删除stage和task
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
        //删除pipeline
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
        List<PipelineAppDeployE> appDeployEList = getAllAppDeploy(pipelineId);
        List<Long> envIds = environmentService.listByProjectIdAndActive(projectId, true).stream().map(DevopsEnviromentRepDTO::getId).collect(Collectors.toList());
        pipelineReqDTO.setEdit(true);
        for (PipelineAppDeployE appDeployE : appDeployEList) {
            if (!envIds.contains(appDeployE.getEnvId())) {
                pipelineReqDTO.setEdit(false);
                break;
            }
        }
        return pipelineReqDTO;
    }

    @Override
    public void execute(Long projectId, Long pipelineId) {
        //校验当前触发人员是否有权限触发
        PipelineE pipelineE = pipelineRepository.queryById(pipelineId);
        if (AUTO.equals(pipelineE.getTriggerType()) || !checkTriggerPermission(pipelineId, null)) {
            throw new CommonException("error.permission.trigger.pipeline");
        }
        //保存pipeline 和 pipelineUserRel
        PipelineRecordE pipelineRecordE = new PipelineRecordE(pipelineId, pipelineE.getTriggerType(), projectId, WorkFlowStatus.RUNNING.toValue(), pipelineE.getName());
        pipelineRecordE.setBusinessKey(GenerateUUID.generateUUID());
        pipelineRecordE = pipelineRecordRepository.create(pipelineRecordE);
        PipelineUserRecordRelE pipelineUserRecordRelE = new PipelineUserRecordRelE();
        pipelineUserRecordRelE.setPipelineRecordId(pipelineRecordE.getId());
        pipelineUserRecordRelE.setUserId(DetailsHelper.getUserDetails().getUserId());
        pipelineUserRelRecordRepository.create(pipelineUserRecordRelE);

        //准备workFlow数据
        DevopsPipelineDTO devopsPipelineDTO = setWorkFlowDTO(pipelineRecordE.getId(), pipelineId);
        pipelineRecordE.setBpmDefinition(gson.toJson(devopsPipelineDTO));
        pipelineRecordRepository.update(pipelineRecordE);

        try {
            //发送请求给workflow，创建流程实例
            CustomUserDetails details = DetailsHelper.getUserDetails();
            createWorkFlow(projectId, devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
            pipelineRecordRepository.update(pipelineRecordE);
            updateFirstStage(pipelineRecordE.getId(), pipelineId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            pipelineRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
            pipelineRecordRepository.update(pipelineRecordE);
        }
    }

    @Override
    @Saga(code = "devops-pipeline-auto-deploy-instance",
            description = "创建流水线自动部署实例", inputSchema = "{}")
    public void autoDeploy(Long stageRecordId, Long taskId) {
        LOGGER.info("autoDeploy:stageRecordId: {} taskId: {}", stageRecordId, taskId);
        //获取数据
        PipelineTaskE pipelineTaskE = pipelineTaskRepository.queryById(taskId);
        CutomerContextUtil.setUserId(pipelineTaskE.getCreatedBy());
        PipelineAppDeployE appDeployE = appDeployRepository.queryById(pipelineTaskE.getAppDeployId());
        List<ApplicationVersionE> versionES = versionRepository.listByAppId(appDeployE.getApplicationId(), null)
                .stream().filter(t -> t.getCreationDate().getTime() > appDeployE.getCreationDate().getTime()).collect(Collectors.toList());
        Integer index = -1;
        for (int i = 0; i < versionES.size(); i++) {
            ApplicationVersionE versionE = versionES.get(i);
            if (appDeployE.getTriggerVersion() == null || appDeployE.getTriggerVersion().isEmpty()) {
                index = i;
                break;
            } else {
                List<String> list = Arrays.asList(appDeployE.getTriggerVersion().split(","));
                Optional<String> branch = list.stream().filter(t -> versionE.getVersion().contains(t)).findFirst();
                if (branch.isPresent() && !branch.get().isEmpty()) {
                    index = i;
                    break;
                }
            }
        }
        if (index == -1) {
            throw new CommonException("no.version.can.trigger.deploy");
        }
        //保存记录
        PipelineTaskRecordE pipelineTaskRecordE = taskRecordRepository.queryByStageRecordId(stageRecordId, taskId).get(0);
        pipelineTaskRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        pipelineTaskRecordE.setName(pipelineTaskE.getName());
        pipelineTaskRecordE.setVersionId(versionES.get(index).getId());
        pipelineTaskRecordE = taskRecordRepository.createOrUpdate(pipelineTaskRecordE);
        try {
            String type = appDeployE.getInstanceId() == null ? CommandType.CREATE.getType() : CommandType.UPDATE.getType();
            ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO(versionES.get(index).getId(), appDeployE.getEnvId(),
                    valueRepository.queryById(appDeployE.getValueId()).getValue(), appDeployE.getApplicationId(), type, appDeployE.getInstanceId(),
                    appDeployE.getInstanceName(), pipelineTaskRecordE.getId(), appDeployE.getId());
            if (type.equals(CommandType.UPDATE.getType())) {
                ApplicationInstanceE oldapplicationInstanceE = applicationInstanceRepository.selectById(applicationDeployDTO.getAppInstanceId());
                DevopsEnvCommandE olddevopsEnvCommandE = devopsEnvCommandRepository.query(oldapplicationInstanceE.getCommandId());
                if (olddevopsEnvCommandE.getObjectVersionId().equals(applicationDeployDTO.getAppVersionId())) {
                    String oldValue = applicationInstanceRepository.queryValueByInstanceId(applicationDeployDTO.getAppInstanceId());
                    ReplaceResult replaceResult = applicationInstanceService.getReplaceResult(applicationVersionRepository.queryValue(applicationDeployDTO.getAppVersionId()), applicationDeployDTO.getValues());
                    if (replaceResult.getDeltaYaml().trim().equals(oldValue.trim())) {
                        applicationDeployDTO.setIsNotChange(true);
                    }
                }
            }
            String input = gson.toJson(applicationDeployDTO);
            sagaClient.startSaga("devops-pipeline-auto-deploy-instance", new StartInstanceDTO(input, "env", appDeployE.getEnvId().toString(), ResourceLevel.PROJECT.value(), appDeployE.getProjectId()));
        } catch (Exception e) {
            pipelineTaskRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
            taskRecordRepository.createOrUpdate(pipelineTaskRecordE);
            Long pipelineRecordId = stageRecordRepository.queryById(stageRecordId).getPipelineRecordId();
            updateStatus(pipelineRecordId, stageRecordId, WorkFlowStatus.FAILED.toValue());
            throw new CommonException("error.create.pipeline.auto.deploy.instance", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<IamUserDTO> audit(Long projectId, PipelineUserRecordRelDTO recordRelDTO) {
        List<IamUserDTO> userDTOS = new ArrayList<>();
        String status;
        Boolean result = true;
        if (recordRelDTO.getIsApprove()) {
            try {
                CustomUserDetails details = DetailsHelper.getUserDetails();
                approveWorkFlow(projectId, pipelineRecordRepository.queryById(recordRelDTO.getPipelineRecordId()).getBusinessKey(), details.getUsername(), details.getUserId(), details.getOrganizationId());
            } catch (Exception e) {
                result = false;
            }
            status = result ? WorkFlowStatus.SUCCESS.toValue() : WorkFlowStatus.FAILED.toValue();
            if (STAGE.equals(recordRelDTO.getType())) {
                status = result ? WorkFlowStatus.RUNNING.toValue() : WorkFlowStatus.FAILED.toValue();
            }
        } else {
            status = WorkFlowStatus.STOP.toValue();
        }
        PipelineUserRecordRelE userRelE = new PipelineUserRecordRelE();
        userRelE.setUserId(DetailsHelper.getUserDetails().getUserId());
        switch (recordRelDTO.getType()) {
            case TASK:
                userRelE.setTaskRecordId(recordRelDTO.getTaskRecordId());
                pipelineUserRelRecordRepository.create(userRelE);
                PipelineTaskRecordE taskRecordE = taskRecordRepository.queryById(recordRelDTO.getTaskRecordId());
                //判断是否成功
                if (status.equals(WorkFlowStatus.SUCCESS.toValue())) {
                    //判断是否是会签
                    if (taskRecordE.getIsCountersigned() == 1) {
                        List<Long> userList = pipelineUserRelRepository.listByOptions(null, null, taskRecordE.getTaskId())
                                .stream().map(PipelineUserRelE::getUserId).collect(Collectors.toList());
                        List<Long> userRecordList = pipelineUserRelRecordRepository.queryByRecordId(null, null, recordRelDTO.getTaskRecordId())
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
                            break;
                        }
                    }
                    updateStatus(recordRelDTO.getPipelineRecordId(), null, WorkFlowStatus.RUNNING.toValue());
                    startNextTaskRecord(taskRecordE.getId(), recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                } else {
                    updateStatus(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId(), status);
                }
                taskRecordE.setStatus(status);
                taskRecordRepository.createOrUpdate(taskRecordE);
                break;
            case STAGE: {
                userRelE.setStageRecordId(recordRelDTO.getStageRecordId());
                pipelineUserRelRecordRepository.create(userRelE);
                if (status.equals(WorkFlowStatus.RUNNING.toValue())) {
                    updateStatus(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId(), status);
                    PipelineStageE stageE = stageRepository.queryById(stageRecordRepository.queryById(recordRelDTO.getStageRecordId()).getStageId());
                    if (!isEmptyStage(stageE.getId())) {
                        //阶段中的第一个任务为人工任务时
                        List<PipelineTaskE> pipelineTaskES = pipelineTaskRepository.queryByStageId(stageRecordRepository.queryById(recordRelDTO.getStageRecordId()).getStageId());
                        if (pipelineTaskES != null && pipelineTaskES.size() > 0) {
                            PipelineTaskE pipelineTaskE = pipelineTaskES.get(0);
                            if (MANUAL.equals(pipelineTaskE.getType())) {
                                startNextTask(pipelineTaskE, recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                            }
                        }
                    } else {
                        startEmptyStage(recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                    }
                } else {
                    updateStatus(recordRelDTO.getPipelineRecordId(), null, status);
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
                PipelineTaskE taskE = pipelineTaskRepository.queryById(taskRecordE.getTaskId());
                if (!taskRecordE.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                    if (taskE.getIsCountersigned() == 1) {
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
        //判断pipeline是否被禁用
        if (pipelineRepository.queryById(pipelineId).getIsEnabled() == 0) {
            throw new CommonException("error.pipeline.check.deploy");
        }
        PipelineCheckDeployDTO checkDeployDTO = new PipelineCheckDeployDTO();
        checkDeployDTO.setPermission(true);
        checkDeployDTO.setVersions(true);
        //获取所有appDeploy
        List<PipelineAppDeployE> appDeployEList = getAllAppDeploy(pipelineId);
        //如果全部为人工任务
        if (appDeployEList.isEmpty()) {
            return checkDeployDTO;
        }
        //检测环境权限
        if (projectId != null) {
            ProjectE projectE = iamRepository.queryIamProject(projectId);
            if (!iamRepository.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE)) {
                //判断当前用户是否是项目所有者
                List<Long> envIds = devopsEnvUserPermissionRepository
                        .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                        .filter(DevopsEnvUserPermissionE::getPermitted)
                        .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
                for (PipelineAppDeployE appDeployE : appDeployEList) {
                    if (!envIds.contains(appDeployE.getEnvId())) {
                        checkDeployDTO.setPermission(false);
                        checkDeployDTO.setEnvName(appDeployE.getEnvName());
                        return checkDeployDTO;
                    }
                }
            }
        }
        //检测自动部署是否生成版本
        for (PipelineAppDeployE appDeployE : appDeployEList) {
            if (appDeployE.getCreationDate().getTime() > versionRepository.getLatestVersion(appDeployE.getApplicationId()).getCreationDate().getTime()) {
                checkDeployDTO.setVersions(false);
                break;
            } else {
                if ((appDeployE.getTriggerVersion() != null) && !appDeployE.getTriggerVersion().isEmpty()) {
                    List<String> list = Arrays.asList(appDeployE.getTriggerVersion().split(","));
                    //是否有对应版本
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
    public DevopsPipelineDTO setWorkFlowDTO(Long pipelineRecordId, Long pipelineId) {
        //workflow数据
        DevopsPipelineDTO devopsPipelineDTO = new DevopsPipelineDTO();
        devopsPipelineDTO.setPipelineRecordId(pipelineRecordId);
        devopsPipelineDTO.setBusinessKey(pipelineRecordRepository.queryById(pipelineRecordId).getBusinessKey());
        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
        //stage
        List<PipelineStageE> stageES = stageRepository.queryByPipelineId(pipelineId);
        for (int i = 0; i < stageES.size(); i++) {
            PipelineStageE stageE = stageES.get(i);
            //创建所有stageRecord
            PipelineStageRecordE recordE = new PipelineStageRecordE();
            BeanUtils.copyProperties(stageE, recordE);
            recordE.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
            recordE.setStageId(stageE.getId());
            recordE.setPipelineRecordId(pipelineRecordId);
            recordE.setId(null);
            recordE = stageRecordRepository.createOrUpdate(recordE);

            //stage
            DevopsPipelineStageDTO devopsPipelineStageDTO = new DevopsPipelineStageDTO();
            devopsPipelineStageDTO.setStageRecordId(recordE.getId());
            devopsPipelineStageDTO.setParallel(stageE.getIsParallel() != null && stageE.getIsParallel() == 1);
            if (i != stageES.size() - 1) {
                devopsPipelineStageDTO.setNextStageTriggerType(stageE.getTriggerType());
            }
            List<PipelineUserRelE> relEList = pipelineUserRelRepository.listByOptions(null, stageE.getId(), null);
            devopsPipelineStageDTO.setMultiAssign(relEList.size() > 1);
            devopsPipelineStageDTO.setUsernames(relEList.stream()
                    .map(relE -> iamRepository.queryUserByUserId(relE.getUserId()).getLoginName())
                    .collect(Collectors.toList()));

            List<DevopsPipelineTaskDTO> devopsPipelineTaskDTOS = new ArrayList<>();
            Long stageRecordId = recordE.getId();
            pipelineTaskRepository.queryByStageId(stageE.getId()).forEach(task -> {
                //创建task记录
                PipelineTaskRecordE taskRecordE = new PipelineTaskRecordE();
                BeanUtils.copyProperties(task, taskRecordE);
                taskRecordE.setId(null);
                taskRecordE.setTaskId(task.getId());
                taskRecordE.setTaskType(task.getType());
                taskRecordE.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
                taskRecordE.setStageRecordId(stageRecordId);
                taskRecordE.setProjectId(task.getProjectId());

                if (task.getAppDeployId() != null) {
                    PipelineAppDeployE appDeployE = appDeployRepository.queryById(task.getAppDeployId());
                    taskRecordE.setApplicationId(appDeployE.getApplicationId());
                    taskRecordE.setEnvId(appDeployE.getEnvId());
                    taskRecordE.setTriggerVersion(appDeployE.getTriggerVersion());
                    taskRecordE.setApplicationId(appDeployE.getApplicationId());
                    taskRecordE.setInstanceId(appDeployE.getInstanceId());
                    taskRecordE.setValue(valueRepository.queryById(appDeployE.getValueId()).getValue());
                }
                List<PipelineUserRelE> taskUserRels = pipelineUserRelRepository.listByOptions(null, null, task.getId());
                taskRecordE.setAuditUser(StringUtils.join(taskUserRels.stream().map(PipelineUserRelE::getUserId).toArray(), ","));
                taskRecordRepository.createOrUpdate(taskRecordE);
                //task
                DevopsPipelineTaskDTO devopsPipelineTaskDTO = new DevopsPipelineTaskDTO();
                devopsPipelineTaskDTO.setTaskName(task.getName());
                devopsPipelineTaskDTO.setTaskType(task.getType());
                devopsPipelineTaskDTO.setMultiAssign(taskUserRels.size() > 1);
                devopsPipelineTaskDTO.setUsernames(taskUserRels.stream().map(relE -> iamRepository.queryUserByUserId(relE.getUserId()).getLoginName()).collect(Collectors.toList()));
                devopsPipelineTaskDTO.setTaskId(task.getId());
                if (task.getIsCountersigned() != null) {
                    devopsPipelineTaskDTO.setSign(task.getIsCountersigned().longValue());
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
    public String getAppDeployStatus(Long stageRecordId, Long taskId) {
        List<PipelineTaskRecordE> list = taskRecordRepository.queryByStageRecordId(stageRecordId, taskId);
        if (list != null && list.size() > 0) {
            return list.get(0).getStatus();
        }
        return WorkFlowStatus.FAILED.toValue();
    }

    @Override
    public void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long taskId, Boolean status) {
        LOGGER.info("setAppDeployStatus:pipelineRecordId: {} stageRecordId: {} taskId: {}", pipelineRecordId, stageRecordId, taskId);
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(stageRecordId);
        PipelineStageE stageE = stageRepository.queryById(stageRecordE.getStageId());
        if (status) {
            if (stageE.getIsParallel() == 1) {
                List<PipelineTaskE> taskList = pipelineTaskRepository.queryByStageId(stageE.getId());
                List<PipelineTaskRecordE> taskRecordEList = taskRecordRepository.queryByStageRecordId(stageRecordE.getId(), null)
                        .stream().filter(t -> t.getStatus().equals(WorkFlowStatus.SUCCESS.toValue())).collect(Collectors.toList());
                if (taskList.size() == taskRecordEList.size()) {
                    startNextTaskRecord(taskRecordRepository.queryByStageRecordId(stageRecordId, taskId).get(0).getId(), pipelineRecordId, stageRecordId);
                }
            } else {
                startNextTaskRecord(taskRecordRepository.queryByStageRecordId(stageRecordId, taskId).get(0).getId(), pipelineRecordId, stageRecordId);
            }
        } else {
            //停止实例
            workFlowRepository.stopInstance(pipelineRecordE.getProjectId(), pipelineRecordE.getBusinessKey());
        }
    }

    @Override
    public PipelineRecordReqDTO getRecordById(Long projectId, Long pipelineRecordId) {
        PipelineRecordReqDTO recordReqDTO = new PipelineRecordReqDTO();
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
        BeanUtils.copyProperties(pipelineRecordE, recordReqDTO);
        //获取pipeline触发人员
        UserE userE = getTriggerUser(pipelineRecordId, null);
        if (userE != null) {
            IamUserDTO userDTO = ConvertHelper.convert(userE, IamUserDTO.class);
            recordReqDTO.setUserDTO(userDTO);
        }
        //查询stage
        List<PipelineStageRecordDTO> recordDTOList = ConvertHelper.convertList(stageRecordRepository.queryByPipeRecordId(pipelineRecordId, null), PipelineStageRecordDTO.class);
        for (int i = 0; i < recordDTOList.size(); i++) {
            PipelineStageRecordDTO stageRecordDTO = recordDTOList.get(i);
            //获取触发人员
            if (stageRecordDTO.getTriggerType().equals(MANUAL)) {
                //不是最后一个阶段
                if (isLastStage(stageRecordDTO.getStageId()) == null) {
                    List<IamUserDTO> userDTOS = new ArrayList<>();
                    List<Long> userIds = pipelineUserRelRecordRepository.queryByRecordId(null, recordDTOList.get(i + 1).getId(), null)
                            .stream().map(PipelineUserRecordRelE::getUserId).collect(Collectors.toList());
                    Boolean audit = !userIds.isEmpty();
                    if (userIds.isEmpty()) {
                        userIds = pipelineUserRelRepository.listByOptions(null, recordDTOList.get(i).getStageId(), null)
                                .stream().map(PipelineUserRelE::getUserId).collect(Collectors.toList());
                    }
                    userIds.forEach(u -> {
                        IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(u), IamUserDTO.class);
                        userDTO.setAudit(audit);
                        userDTOS.add(userDTO);
                    });
                    stageRecordDTO.setUserDTOS(userDTOS);
                    if (recordDTOList.get(i).getStatus().equals(WorkFlowStatus.SUCCESS.toValue()) && recordDTOList.get(i + 1).getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                        stageRecordDTO.setIndex(true);
                    }
                }
            }

            List<PipelineTaskRecordDTO> taskRecordDTOS = new ArrayList<>();
            //获取所有已执行taskRecord
            taskRecordRepository.queryByStageRecordId(stageRecordDTO.getId(), null).forEach(r -> {
                        PipelineTaskRecordDTO taskRecordDTO = ConvertHelper.convert(r, PipelineTaskRecordDTO.class);
                        if (taskRecordDTO.getTaskType().equals(MANUAL)) {
                            List<IamUserDTO> userDTOS = new ArrayList<>();
                            if (!taskRecordDTO.getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                                //获取已经审核人员
                                List<Long> userIds = pipelineUserRelRecordRepository.queryByRecordId(null, null, r.getId())
                                        .stream().map(PipelineUserRecordRelE::getUserId).collect(Collectors.toList());
                                userIds.forEach(userId -> {
                                    IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(userId), IamUserDTO.class);
                                    userDTO.setAudit(true);
                                    userDTOS.add(userDTO);
                                });
                                //获取会签未审核人员
                                if (taskRecordDTO.getIsCountersigned() == 1) {
                                    List<Long> unExecuteUserIds = pipelineUserRelRepository.listByOptions(null, null, taskRecordDTO.getTaskId())
                                            .stream().map(PipelineUserRelE::getUserId)
                                            .filter(u -> !userIds.contains(u)).collect(Collectors.toList());
                                    unExecuteUserIds.forEach(userId -> {
                                        IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(userId), IamUserDTO.class);
                                        userDTO.setAudit(false);
                                        userDTOS.add(userDTO);
                                    });
                                }
                            } else {
                                if (r.getAuditUser() != null) {
                                    List<String> userIds = Arrays.asList(r.getAuditUser().split(","));
                                    userIds.forEach(userId -> {
                                        IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(TypeUtil.objToLong(userId)), IamUserDTO.class);
                                        userDTO.setAudit(false);
                                        userDTOS.add(userDTO);
                                    });
                                }
                            }
                            taskRecordDTO.setUserDTOList(userDTOS);
                        }
                        taskRecordDTOS.add(taskRecordDTO);
                    }
            );
            //获取所有未执行task
            List<Long> taskIds = taskRecordDTOS.stream().map(PipelineTaskRecordDTO::getTaskId).collect(Collectors.toList());
            pipelineTaskRepository.queryByStageId(stageRecordDTO.getStageId()).forEach(taskE -> {
                if (!taskIds.contains(taskE.getId())) {
                    PipelineTaskRecordDTO taskRecordDTO = new PipelineTaskRecordDTO();
                    BeanUtils.copyProperties(taskE, taskRecordDTO);
                    taskRecordDTO.setId(null);
                    taskRecordDTO.setTaskType(taskE.getType());
                    if (taskE.getType().equals(AUTO)) {
                        PipelineAppDeployE appDeployE = appDeployRepository.queryById(taskE.getAppDeployId());
                        taskRecordDTO.setAppName(appDeployE.getAppName());
                        taskRecordDTO.setEnvName(appDeployE.getEnvName());
                    }
                    taskRecordDTO.setStatus(WorkFlowStatus.UNEXECUTED.toValue());
                    List<IamUserDTO> userDTOS = pipelineUserRelRepository.listByOptions(null, null, taskE.getId())
                            .stream().map(PipelineUserRelE::getUserId)
                            .map(userId -> {
                                IamUserDTO userDTO = ConvertHelper.convert(iamRepository.queryUserByUserId(userId), IamUserDTO.class);
                                userDTO.setAudit(false);
                                return userDTO;
                            }).collect(Collectors.toList());
                    taskRecordDTO.setUserDTOList(userDTOS);
                    taskRecordDTOS.add(taskRecordDTO);
                }
            });
            stageRecordDTO.setTaskRecordDTOS(taskRecordDTOS);
        }
        recordReqDTO.setStageRecordDTOS(recordDTOList);

        return recordReqDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean retry(Long projectId, Long pipelineRecordId) {
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
        if (pipelineRecordE.getEdited() != null && pipelineRecordE.getEdited()) {
            return false;
        }
        String bpmDefinition = pipelineRecordE.getBpmDefinition();
        DevopsPipelineDTO pipelineDTO = gson.fromJson(bpmDefinition, DevopsPipelineDTO.class);
        String uuid = GenerateUUID.generateUUID();
        pipelineDTO.setBusinessKey(uuid);
        CustomUserDetails details = DetailsHelper.getUserDetails();
        createWorkFlow(projectId, pipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
        //清空之前数据
        pipelineRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        pipelineRecordE.setBusinessKey(uuid);
        pipelineRecordRepository.update(pipelineRecordE);
        stageRecordRepository.queryByPipeRecordId(pipelineRecordId, null).forEach(t -> {
            t.setStatus(null);
            stageRecordRepository.update(t);
            taskRecordRepository.queryByStageRecordId(t.getId(), null).forEach(taskRecordE -> {
                taskRecordRepository.delete(taskRecordE.getId());
            });
        });
        //更新第一阶段
        if (pipelineRecordE.getTriggerType().equals(MANUAL)) {
            updateFirstStage(pipelineRecordId, pipelineRecordE.getPipelineId());
        }
        return true;
    }

    @Override
    public List<PipelineRecordListDTO> queryByPipelineId(Long pipelineId) {
        return pipelineRecordRepository.queryByPipelineId(pipelineId).stream().map(t ->
                new PipelineRecordListDTO(t.getId(), t.getCreationDate())).collect(Collectors.toList());
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
    public void updateStatus(Long pipelineRecordId, Long stageRecordId, String status) {
        if (pipelineRecordId != null) {
            PipelineRecordE pipelineRecordE = new PipelineRecordE();
            pipelineRecordE.setId(pipelineRecordId);
            pipelineRecordE.setStatus(status);
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
    public void executeAppDeploy(Long pipelineId) {
        PipelineE pipelineE = pipelineRepository.queryById(pipelineId);
        CutomerContextUtil.setUserId(pipelineE.getCreatedBy());
        //保存pipeline
        PipelineRecordE pipelineRecordE = new PipelineRecordE(pipelineId, pipelineE.getTriggerType(), pipelineE.getProjectId(), WorkFlowStatus.RUNNING.toValue(), pipelineE.getName());
        String uuid = GenerateUUID.generateUUID();
        pipelineRecordE.setBusinessKey(uuid);
        pipelineRecordE = pipelineRecordRepository.create(pipelineRecordE);
        //准备workFlow数据
        DevopsPipelineDTO devopsPipelineDTO = setWorkFlowDTO(pipelineRecordE.getId(), pipelineId);
        pipelineRecordE.setBpmDefinition(gson.toJson(devopsPipelineDTO));
        pipelineRecordE = pipelineRecordRepository.update(pipelineRecordE);
        //发送请求给workflow，创建流程实例
        try {
            CustomUserDetails details = DetailsHelper.getUserDetails();
            createWorkFlow(pipelineE.getProjectId(), devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
            List<PipelineStageRecordE> stageRecordES = stageRecordRepository.queryByPipeRecordId(pipelineRecordE.getId(), null);
            if (stageRecordES != null && stageRecordES.size() > 0) {
                updateStatus(null, stageRecordES.get(0).getId(), WorkFlowStatus.RUNNING.toValue());
            }
        } catch (Exception e) {
            pipelineRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
            pipelineRecordRepository.update(pipelineRecordE);
            throw new CommonException(e);
        }
    }

    @Override
    public void stop(Long projectId, Long recordId) {
        PipelineRecordE recordE = pipelineRecordRepository.queryById(recordId);
        if (!recordE.getStatus().equals(WorkFlowStatus.RUNNING.toValue())) {
            throw new CommonException("error.pipeline.record.status");
        }
        List<PipelineStageRecordE> stageRecordES = stageRecordRepository.queryByPipeRecordId(recordId, null);

        for (PipelineStageRecordE stageRecordE : stageRecordES) {
            if (stageRecordE.getStatus().equals(WorkFlowStatus.RUNNING.toValue()) || stageRecordE.getStatus().equals(WorkFlowStatus.UNEXECUTED.toValue())) {
                updateStatus(recordId, stageRecordE.getId(), WorkFlowStatus.STOP.toValue());
                List<PipelineTaskRecordE> taskRecordEList = taskRecordRepository.queryByStageRecordId(stageRecordE.getId(), null);
                for (PipelineTaskRecordE taskRecordE : taskRecordEList) {
                    if (taskRecordE.getStatus().equals(WorkFlowStatus.RUNNING.toValue())) {
                        taskRecordE.setStatus(WorkFlowStatus.STOP.toValue());
                        taskRecordRepository.createOrUpdate(taskRecordE);
                        break;
                    }
                }
                break;
            }
        }
    }

    private PipelineTaskE getFirstTask(Long pipelineId) {
        return pipelineTaskRepository.queryByStageId(stageRepository.queryByPipelineId(pipelineId).get(0).getId()).get(0);
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

    private void updateFirstStage(Long pipelineRecordId, Long pipelineId) {
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryByPipeRecordId(pipelineRecordId,
                stageRepository.queryByPipelineId(pipelineId).get(0).getId()).get(0);
        updateStatus(null, stageRecordE.getId(), WorkFlowStatus.RUNNING.toValue());
        //更新第一个阶段状态
        if (isEmptyStage(stageRecordE.getStageId())) {
            startEmptyStage(pipelineRecordId, stageRecordE.getId());
        } else {
            PipelineTaskE taskE = getFirstTask(pipelineId);
            if (taskE.getType().equals(MANUAL)) {
                startNextTask(taskE, pipelineRecordId, stageRecordE.getId());
            }
        }
    }

    private void AddPipelineTask(PipelineTaskDTO t, Long projectId, Long stageId) {
        t.setProjectId(projectId);
        t.setStageId(stageId);
        if (AUTO.equals(t.getType())) {
            //appDeploy
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

    private void startNextTaskRecord(Long taskRecordId, Long pipelineRecordId, Long stageRecordId) {
        PipelineTaskRecordE taskRecordE = taskRecordRepository.queryById(taskRecordId);
        Long stageId = isStageLastTask(taskRecordId);
        //属于阶段的最后一个任务
        if (stageId != null) {
            PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(taskRecordE.getStageRecordId());
            stageRecordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
            Long time = System.currentTimeMillis() - stageRecordE.getLastUpdateDate().getTime();
            stageRecordE.setExecutionTime(time.toString());
            stageRecordRepository.createOrUpdate(stageRecordE);
            //属于pipeline最后一个任务
            Long pipelineId = isLastStage(stageId);
            PipelineRecordE recordE = pipelineRecordRepository.queryById(pipelineRecordId);
            if (pipelineId != null) {
                LOGGER.info("任务成功了");
                recordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
                pipelineRecordRepository.update(recordE);
            } else {
                //更新下一个阶段状态
                startNextStageRecord(stageId, recordE, stageRecordId);
            }
        } else {
            startNextTask(getNextTask(taskRecordE.getTaskId()), pipelineRecordId, stageRecordId);
        }
    }

    /**
     * 开始下一个阶段
     *
     * @param stageId       已完成的阶段Id
     * @param recordE
     * @param stageRecordId 已完成的阶段记录
     */
    private void startNextStageRecord(Long stageId, PipelineRecordE recordE, Long stageRecordId) {
        PipelineStageE nextStage = getNextStage(stageId);
        PipelineStageRecordE nextStageRecordE = stageRecordRepository.queryByPipeRecordId(recordE.getId(), nextStage.getId()).get(0);
        if (stageRecordRepository.queryById(stageRecordId).getTriggerType().equals(AUTO)) {
            if (!isEmptyStage(nextStage.getId())) {
                nextStageRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
                List<PipelineTaskE> list = pipelineTaskRepository.queryByStageId(nextStage.getId());
                if (list != null && list.size() > 0) {
                    if (list.get(0).getType().equals(MANUAL)) {
                        startNextTask(list.get(0), recordE.getId(), nextStageRecordE.getId());
                    }
                }
            } else {
                startEmptyStage(recordE.getId(), stageRecordRepository.queryByPipeRecordId(recordE.getId(), nextStage.getId()).get(0).getId());
            }
        } else {
            updateStatus(recordE.getId(), null, WorkFlowStatus.PENDINGCHECK.toValue());
        }
    }

    private Boolean isEmptyStage(Long stageId) {
        return pipelineTaskRepository.queryByStageId(stageId).size() == 0;
    }

    private void startEmptyStage(Long pipelineRecordId, Long stageRecordId) {
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.queryById(pipelineRecordId);
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(stageRecordId);
        stageRecordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
        Long time = 0L;
        stageRecordE.setExecutionTime(time.toString());
        stageRecordRepository.createOrUpdate(stageRecordE);
        PipelineStageE stageE = getNextStage(stageRecordE.getStageId());
        if (stageE != null) {
            startNextStageRecord(stageRecordE.getStageId(), pipelineRecordE, stageRecordE.getId());
        } else {
            updateStatus(pipelineRecordId, null, WorkFlowStatus.SUCCESS.toValue());
        }
    }

    private void startNextTask(PipelineTaskE taskE, Long pipelineRecordId, Long stageRecordId) {
        PipelineTaskRecordE taskRecordE = taskRecordRepository.queryByStageRecordId(stageRecordId, taskE.getId()).get(0);
        if (!taskE.getType().equals(AUTO)) {
            taskRecordE.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
            taskRecordE.setTaskId(taskE.getId());
            taskRecordE.setTaskType(taskE.getType());
            taskRecordRepository.createOrUpdate(taskRecordE);
            updateStatus(pipelineRecordId, stageRecordId, WorkFlowStatus.PENDINGCHECK.toValue());
        }
    }

    private PipelineStageE getNextStage(Long stageId) {
        List<PipelineStageE> list = stageRepository.queryByPipelineId(stageRepository.queryById(stageId).getPipelineId());
        return list.stream().filter(t -> t.getId() > stageId).findFirst().orElse(null);
    }

    private PipelineTaskE getNextTask(Long taskId) {
        List<PipelineTaskE> list = pipelineTaskRepository.queryByStageId(pipelineTaskRepository.queryById(taskId).getStageId());
        return list.stream().filter(t -> t.getId() > taskId).findFirst().orElse(null);
    }

    private Long isStageLastTask(Long taskRecordId) {
        PipelineTaskE pipelineTaskE = pipelineTaskRepository.queryById(taskRecordRepository.queryById(taskRecordId).getTaskId());
        List<PipelineTaskE> pipelineTaskES = pipelineTaskRepository.queryByStageId(pipelineTaskE.getStageId());
        return pipelineTaskES.get(pipelineTaskES.size() - 1).getId().equals(pipelineTaskE.getId()) ? pipelineTaskE.getStageId() : null;
    }

    private Long isLastStage(Long stageId) {
        PipelineStageE pipelineStageE = stageRepository.queryById(stageId);
        List<PipelineStageE> pipelineStageES = stageRepository.queryByPipelineId(pipelineStageE.getPipelineId());
        return pipelineStageES.get(pipelineStageES.size() - 1).getId().equals(pipelineStageE.getId()) ? pipelineStageE.getPipelineId() : null;
    }

    private Boolean checkTriggerPermission(Long pipelineId, Long stageId) {
        List<Long> userIds = pipelineUserRelRepository.listByOptions(pipelineId, stageId, null)
                .stream()
                .map(PipelineUserRelE::getUserId)
                .collect(Collectors.toList());
        return userIds.contains(DetailsHelper.getUserDetails().getUserId());
    }

    private Boolean checkTaskTriggerPermission(Long taskId, Long taskRecordId) {
        PipelineTaskE taskE = pipelineTaskRepository.queryById(taskId);
        List<Long> userIds = pipelineUserRelRepository.listByOptions(null, null, taskId)
                .stream()
                .map(PipelineUserRelE::getUserId)
                .collect(Collectors.toList());
        //未执行
        List<Long> userIdsUnExe = new ArrayList<>(userIds);
        if (taskE.getIsCountersigned() == 1) {
            List<Long> userIdRecords = pipelineUserRelRecordRepository.queryByRecordId(null, null, taskRecordId)
                    .stream()
                    .map(PipelineUserRecordRelE::getUserId)
                    .collect(Collectors.toList());
            //移除已经执行
            userIds.forEach(t -> {
                if (userIdRecords.contains(t)) {
                    userIdsUnExe.remove(t);
                }
            });
        }
        return userIdsUnExe.contains(DetailsHelper.getUserDetails().getUserId());
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

}

