package io.choerodon.devops.app.service.impl;

import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.dto.ApplicationDeployDTO;
import io.choerodon.devops.api.dto.PipelineAppDeployDTO;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.api.dto.PipelineRecordDTO;
import io.choerodon.devops.api.dto.PipelineReqDTO;
import io.choerodon.devops.api.dto.PipelineStageDTO;
import io.choerodon.devops.api.dto.PipelineStageRecordDTO;
import io.choerodon.devops.api.dto.PipelineTaskDTO;
import io.choerodon.devops.api.dto.PipelineUserRelDTO;
import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployE;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployRecordE;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;
import io.choerodon.devops.domain.application.entity.PipelineE;
import io.choerodon.devops.domain.application.entity.PipelineRecordE;
import io.choerodon.devops.domain.application.entity.PipelineStageE;
import io.choerodon.devops.domain.application.entity.PipelineStageRecordE;
import io.choerodon.devops.domain.application.entity.PipelineTaskE;
import io.choerodon.devops.domain.application.entity.PipelineTaskRecordE;
import io.choerodon.devops.domain.application.entity.PipelineUserRecordRelE;
import io.choerodon.devops.domain.application.entity.PipelineUserRelE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployValueRepository;
import io.choerodon.devops.domain.application.repository.PipelineRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineRepository;
import io.choerodon.devops.domain.application.repository.PipelineStageRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineStageRepository;
import io.choerodon.devops.domain.application.repository.PipelineTaskRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineTaskRepository;
import io.choerodon.devops.domain.application.repository.PipelineUserRelRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineUserRelRepository;
import io.choerodon.devops.domain.application.repository.WorkFlowRepository;
import io.choerodon.devops.infra.common.util.CutomerContextUtil;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.WorkFlowStatus;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineTaskDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/4/3
 * Description:
 */
@Service
public class PipelineServiceImpl implements PipelineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private static final Gson gson = new Gson();
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
    private PipelineAppDeployValueRepository valueRepository;
    @Autowired
    private PipelineTaskRecordRepository taskRecordRepository;
    @Autowired
    private WorkFlowRepository workFlowRepository;
    @Autowired
    private ApplicationVersionRepository versionRepository;
    @Autowired
    private SagaClient sagaClient;

    @Override
    public Page<PipelineDTO> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        Page<PipelineDTO> pipelineDTOS = ConvertPageHelper.convertPage(pipelineRepository.listByOptions(projectId, pageRequest, params), PipelineDTO.class);
        Page<PipelineDTO> page = new Page<>();
        page.setContent(pipelineDTOS.getContent().stream().peek(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreateBy());
            t.setCreateUserName(userE.getLoginName());
            t.setCreateUserUrl(userE.getImageUrl());
            t.setCreateUserRealName(userE.getRealName());
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public Page<PipelineRecordDTO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params) {
        Page<PipelineRecordDTO> pageRecordDTOS = ConvertPageHelper.convertPage(
                pipelineRecordRepository.listByOptions(projectId, pipelineId, pageRequest, params), PipelineRecordDTO.class);
        List<PipelineRecordDTO> pipelineRecordDTOS = pageRecordDTOS.getContent().stream().peek(t ->
                t.setStageDTOList(ConvertHelper.convertList(stageRecordRepository.list(projectId, pipelineId), PipelineStageRecordDTO.class)))
                .collect(Collectors.toList());
        pageRecordDTOS.setContent(pipelineRecordDTOS);
        return pageRecordDTOS;
    }

    @Override
    @Transactional
    public PipelineReqDTO create(Long projectId, PipelineReqDTO pipelineReqDTO) {
        //pipeline
        PipelineE pipelineE = ConvertHelper.convert(pipelineReqDTO, PipelineE.class);
        pipelineE = pipelineRepository.create(projectId, pipelineE);
        createUserRel(pipelineReqDTO.getPipelineUserRelDTOS(), null, pipelineE.getId(), null);

        //stage
        List<PipelineStageE> pipelineStageES = ConvertHelper.convertList(pipelineReqDTO.getPipelineStageDTOS(), PipelineStageE.class)
                .stream().map(t -> stageRepository.create(t)).collect(Collectors.toList());
        for (int i = 0; i < pipelineStageES.size(); i++) {
            createUserRel(pipelineReqDTO.getPipelineStageDTOS().get(i).getStageUserRelDTOS(), null, pipelineStageES.get(i).getId(), null);
            //task
            pipelineReqDTO.getPipelineStageDTOS().get(i).getPipelineTaskDTOS().forEach(t -> {
                if ("auto".equals(t.getType())) {
                    t.setAppDeployId(appDeployRepository.create(ConvertHelper.convert(t.getAppDeployDTOS(), PipelineAppDeployE.class)).getId());
                }
                Long taskId = pipelineTaskRepository.create(ConvertHelper.convert(t, PipelineTaskE.class)).getId();
                if ("maual".equals(t.getType())) {
                    createUserRel(t.getTaskUserRelDTOS(), null, null, taskId);
                }
            });
        }
        return pipelineReqDTO;
    }

    @Override
    public PipelineReqDTO update(Long projectId, PipelineReqDTO pipelineReqDTO) {
        //pipeline
        PipelineE pipelineE = ConvertHelper.convert(pipelineReqDTO, PipelineE.class);
        pipelineE = pipelineRepository.update(projectId, pipelineE);
        updateUserRel(pipelineReqDTO.getPipelineUserRelDTOS(), pipelineE.getId(), null, null);

        //stage
        List<PipelineStageE> pipelineStageES = ConvertHelper.convertList(pipelineReqDTO.getPipelineStageDTOS(), PipelineStageE.class)
                .stream().map(t -> stageRepository.update(t)).collect(Collectors.toList());
        for (int i = 0; i < pipelineStageES.size(); i++) {
            updateUserRel(pipelineReqDTO.getPipelineStageDTOS().get(i).getStageUserRelDTOS(), null, pipelineStageES.get(i).getId(), null);
            //task
            pipelineReqDTO.getPipelineStageDTOS().get(i).getPipelineTaskDTOS().forEach(t -> {
                if ("auto".equals(t.getType())) {
                    t.setAppDeployId(appDeployRepository.update(ConvertHelper.convert(t.getAppDeployDTOS(), PipelineAppDeployE.class)).getId());
                }
                Long taskId = pipelineTaskRepository.update(ConvertHelper.convert(t, PipelineTaskE.class)).getId();
                if ("maual".equals(t.getType())) {
                    updateUserRel(t.getTaskUserRelDTOS(), null, null, taskId);
                }
            });
        }
        return pipelineReqDTO;
    }

    @Override
    public PipelineDTO updateIsEnabled(Long projectId, Long pipelineId, Integer isEnabled) {
        return ConvertHelper.convert(pipelineRepository.updateIsEnabled(pipelineId, isEnabled), PipelineDTO.class);
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long pipelineId) {
        stageRepository.queryByPipelineId(pipelineId).forEach(stage -> {
            pipelineTaskRepository.queryByStageId(stage.getId()).forEach(task -> {
                if (task.getAppDeployId() != null) {
                    appDeployRepository.deleteById(task.getAppDeployId());
                }
                pipelineTaskRepository.deleteById(task.getId());
            });
            stageRepository.delete(stage.getId());
        });
        pipelineRepository.delete(pipelineId);
    }

    @Override
    public PipelineReqDTO queryById(Long projectId, Long pipelineId) {
        PipelineReqDTO pipelineReqDTO = ConvertHelper.convert(pipelineRepository.queryById(pipelineId), PipelineReqDTO.class);
        pipelineReqDTO.setPipelineUserRelDTOS(ConvertHelper.convertList(pipelineUserRelRepository.listByOptions(pipelineId, null, null), PipelineUserRelDTO.class));
        List<PipelineStageDTO> pipelineStageES = ConvertHelper.convertList(stageRepository.queryByPipelineId(pipelineId), PipelineStageDTO.class);
        pipelineStageES = pipelineStageES.stream()
                .peek(stage -> {
                    List<PipelineTaskDTO> pipelineTaskDTOS = ConvertHelper.convertList(pipelineTaskRepository.queryByStageId(stage.getId()), PipelineTaskDTO.class);
                    pipelineTaskDTOS = pipelineTaskDTOS.stream().peek(task -> {
                        if (task.getAppDeployId() != null) {
                            task.setAppDeployDTOS(ConvertHelper.convert(appDeployRepository.queryById(task.getAppDeployId()), PipelineAppDeployDTO.class));
                        } else {
                            task.setTaskUserRelDTOS(ConvertHelper.convertList(pipelineUserRelRepository.listByOptions(null, null, task.getId()), PipelineUserRelDTO.class));
                        }
                    }).collect(Collectors.toList());
                    stage.setPipelineTaskDTOS(pipelineTaskDTOS);
                }).collect(Collectors.toList());
        pipelineReqDTO.setPipelineStageDTOS(pipelineStageES);
        return pipelineReqDTO;
    }


    @Override
    public void execute(Long projectId, Long pipelineId) {
        //校验当前触发人员是否有权限触发
        PipelineE pipelineE = pipelineRepository.queryById(pipelineId);
        checkTriggerPermission(pipelineE, pipelineId);
        //保存pipeline 和 pipelineUserRel
        PipelineRecordE pipelineRecordE = pipelineRecordRepository.create(new PipelineRecordE(pipelineId, pipelineE.getTriggerType(), projectId));
        PipelineUserRecordRelE pipelineUserRecordRelE = new PipelineUserRecordRelE();
        pipelineUserRecordRelE.setPipelineRecordId(pipelineRecordE.getId());
        pipelineUserRecordRelE.setUserId(DetailsHelper.getUserDetails().getUserId());
        pipelineUserRelRecordRepository.create(pipelineUserRecordRelE);
        //pipeline
        DevopsPipelineDTO devopsPipelineDTO = new DevopsPipelineDTO();
        devopsPipelineDTO.setBussinessId(pipelineRecordE.getId());
        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
        //stage
        stageRepository.queryByPipelineId(pipelineId).forEach(t -> {
            DevopsPipelineStageDTO devopsPipelineStageDTO = new DevopsPipelineStageDTO();
            devopsPipelineStageDTO.setParallel(t.getIsParallel() == 1);
            devopsPipelineStageDTO.setNextStageTriggerType(t.getTriggerType());
            List<PipelineUserRelE> relEList = pipelineUserRelRepository.listByOptions(null, t.getId(), null);
            devopsPipelineStageDTO.setMultiAssign(relEList.size() > 1);
            devopsPipelineStageDTO.setUsernames(relEList.stream().map(relE -> iamRepository.queryUserByUserId(relE.getId()).getLoginName()).collect(Collectors.toList()));

            List<DevopsPipelineTaskDTO> devopsPipelineTaskDTOS = new ArrayList<>();

            pipelineTaskRepository.queryByStageId(t.getId()).forEach(task -> {
                //task
                List<PipelineUserRelE> taskUserRels = pipelineUserRelRepository.listByOptions(null, null, task.getId());
                DevopsPipelineTaskDTO devopsPipelineTaskDTO = new DevopsPipelineTaskDTO();
                devopsPipelineTaskDTO.setTaskName(task.getName());
                devopsPipelineTaskDTO.setTaskType(task.getType());
                devopsPipelineTaskDTO.setMultiAssign(taskUserRels.size() > 1);
                devopsPipelineTaskDTO.setUsernames(taskUserRels.stream().map(relE -> iamRepository.queryUserByUserId(relE.getId()).getLoginName()).collect(Collectors.toList()));

                devopsPipelineTaskDTOS.add(devopsPipelineTaskDTO);
            });
            devopsPipelineStageDTO.setTasks(devopsPipelineTaskDTOS);
            devopsPipelineStageDTOS.add(devopsPipelineStageDTO);
        });

        devopsPipelineDTO.setStages(devopsPipelineStageDTOS);
        pipelineRecordE.setTriggerType(gson.toJson(devopsPipelineDTO));
        pipelineRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        pipelineRecordRepository.update(pipelineRecordE);

        //更新pipelineRecord状态
        try {
            pipelineRecordE.setProcessInstanceId(workFlowRepository.create(projectId, devopsPipelineDTO));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            pipelineRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
        }
        pipelineRecordRepository.update(pipelineRecordE);

        //更新stageRecord
        PipelineStageE pipelineStageE = stageRepository.queryByPipelineId(pipelineId).get(0);
        PipelineStageRecordE stageRecordE = new PipelineStageRecordE(pipelineStageE.getTriggerType(), pipelineStageE.getIsParallel(), projectId, pipelineRecordE.getId());
        if ("auto".equals(pipelineStageE.getTriggerType())) {
            stageRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
            stageRecordE = stageRecordRepository.create(stageRecordE);

            PipelineTaskE pipelineTaskE = pipelineTaskRepository.queryByStageId(stageRecordE.getId()).get(0);
            PipelineAppDeployE appDeployE = appDeployRepository.queryById(pipelineTaskE.getAppDeployId());
            PipelineTaskRecordE taskRecordE = new PipelineTaskRecordE(stageRecordE.getId(), pipelineTaskE.getType(),
                    appDeployE.getTriggerVersion(), appDeployE.getApplicationId(),
                    appDeployE.getEnvId(), appDeployE.getInstanceId(),
                    valueRepository.queryById(appDeployE.getValueId()).getValue());
            taskRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
            taskRecordRepository.createOrUpdate(taskRecordE);
        } else {
            stageRecordE.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
            stageRecordRepository.create(stageRecordE);
        }

    }

    @Override
    @Saga(code = "devops-pipeline-auto-deploy-instance",
            description = "创建流水线自动部署实例", inputSchema = "{}")
    public void autoDeploy(Long stageRecordId, Long taskId) {
        //获取数据
        PipelineTaskE pipelineTaskE = pipelineTaskRepository.queryById(taskId);
        PipelineAppDeployE appDeployE = appDeployRepository.queryById(pipelineTaskE.getAppDeployId());
        ApplicationVersionE versionE = versionRepository.getLatestVersion(appDeployE.getApplicationId());

        //保存记录
        PipelineTaskRecordE pipelineTaskRecordE = new PipelineTaskRecordE(stageRecordId, pipelineTaskE.getType(),
                appDeployE.getTriggerVersion(), appDeployE.getApplicationId(),
                appDeployE.getEnvId(), appDeployE.getInstanceId(),
                valueRepository.queryById(appDeployE.getValueId()).getValue());
        pipelineTaskRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        taskRecordRepository.createOrUpdate(pipelineTaskRecordE);
        try {
            String type = appDeployE.getInstanceId() == null ? CommandType.CREATE.getType() : CommandType.UPDATE.getType();
            ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO(versionE.getId(), appDeployE.getEnvId(),
                    valueRepository.queryById(appDeployE.getValueId()).getValue(), appDeployE.getApplicationId(), type, appDeployE.getInstanceId(),
                    appDeployE.getInstanceName(), pipelineTaskRecordE.getId(), appDeployE.getId());
            String input = gson.toJson(applicationDeployDTO);
            sagaClient.startSaga("devops-pipeline-auto-deploy-instance", new StartInstanceDTO(input, "env", appDeployE.getEnvId().toString(), ResourceLevel.PROJECT.value(), appDeployE.getProjectId()));
        } catch (Exception e) {
            pipelineTaskRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
            taskRecordRepository.createOrUpdate(pipelineTaskRecordE);
            throw new CommonException("error.create.pipeline.auto.deploy.instance", e);
        }
    }

    @Override
    public void setTaskStatus(Long taskRecordId) {
        PipelineTaskRecordE pipelineTaskRecordE = new PipelineTaskRecordE();
        pipelineTaskRecordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
        taskRecordRepository.createOrUpdate(pipelineTaskRecordE);

    }
//    private boolean isStageLastTask(Long taskRecordId){
//        taskRecordRepository.queryById(taskRecordId).get
//    }
//
//    private boolean isPipelineLastTask();

    private void checkTriggerPermission(PipelineE pipelineE, Long pipelineId) {
        if ("auto".equals(pipelineE.getTriggerType())) {
            throw new CommonException("error.permission.trigger.pipeline");
        }
        List<Long> userIds = pipelineUserRelRepository.listByOptions(pipelineId, null, null)
                .stream()
                .map(PipelineUserRelE::getUserId)
                .collect(Collectors.toList());
        if (!userIds.contains(DetailsHelper.getUserDetails().getUserId())) {
            throw new CommonException("error.permission.trigger.pipeline");
        }
    }

    private void createUserRel(List<PipelineUserRelDTO> pipelineUserRelDTOS, Long pipelineId, Long stageId, Long taskId) {
        if (pipelineUserRelDTOS != null) {
            ConvertHelper.convertList(pipelineUserRelDTOS, PipelineUserRelE.class)
                    .forEach(t -> {
                        t.setPipelineId(pipelineId);
                        t.setStageId(stageId);
                        t.setTaskId(taskId);
                        pipelineUserRelRepository.create(t);
                    });
        }
    }

    private void updateUserRel(List<PipelineUserRelDTO> userRelDTOS, Long pipelineId, Long stageId, Long taskId) {
        List<PipelineUserRelE> addUserRelEList = new ArrayList<>();
        List<PipelineUserRelE> relEList = pipelineUserRelRepository.listByOptions(pipelineId, stageId, taskId);
        if (userRelDTOS != null) {
            List<PipelineUserRelE> userRelES = ConvertHelper.convertList(userRelDTOS, PipelineUserRelE.class);
            userRelES.forEach(relE -> {
                if (!relEList.contains(relE)) {
                    addUserRelEList.add(relE);
                } else {
                    relEList.remove(relE);
                }
            });
            addUserRelEList.forEach(addUserRelE -> {
                addUserRelE.setPipelineId(pipelineId);
                addUserRelE.setStageId(stageId);
                addUserRelE.setTaskId(taskId);
                pipelineUserRelRepository.create(addUserRelE);
            });
            relEList.forEach(delUserRelE -> pipelineUserRelRepository.delete(delUserRelE.getId()));
        } else {
            relEList.forEach(delUserRelE -> pipelineUserRelRepository.delete(delUserRelE.getId()));
        }
    }

}

