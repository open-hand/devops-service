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
import io.choerodon.devops.api.dto.PipelineUserRecordRelDTO;
import io.choerodon.devops.api.dto.PipelineUserRelDTO;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
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
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.WorkFlowStatus;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineTaskDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String[] TYPE = {"feature", "bugfix", "release", "hotfix", "custom", "master"};

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

        //准备workFlow数据
        DevopsPipelineDTO devopsPipelineDTO = setWorkFlowDTO(pipelineRecordE.getId(), pipelineId);
        pipelineRecordE.setTriggerType(gson.toJson(devopsPipelineDTO));
        pipelineRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        pipelineRecordRepository.update(pipelineRecordE);

        //发送请求给workflow，创建流程实例
        try {
            pipelineRecordE.setProcessInstanceId(workFlowRepository.create(projectId, devopsPipelineDTO));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            pipelineRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
        }
        pipelineRecordRepository.update(pipelineRecordE);

        //更新第一个任务状态
        PipelineTaskRecordE taskRecordE = new PipelineTaskRecordE();
        PipelineTaskE taskE = getFirsetTask(pipelineId);
        BeanUtils.copyProperties(taskE, taskRecordE);
        taskRecordE.setId(taskE.getId());
        taskRecordE.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
        taskRecordRepository.createOrUpdate(taskRecordE);
    }

    @Override
    @Saga(code = "devops-pipeline-auto-deploy-instance",
            description = "创建流水线自动部署实例", inputSchema = "{}")
    public void autoDeploy(Long stageRecordId, Long taskId) {
        //获取数据
        PipelineTaskE pipelineTaskE = pipelineTaskRepository.queryById(taskId);
        PipelineAppDeployE appDeployE = appDeployRepository.queryById(pipelineTaskE.getAppDeployId());
        List<ApplicationVersionE> versionES = versionRepository.listByAppId(appDeployE.getApplicationId(), null)
                .stream().filter(t -> t.getCreationDate().getTime() > appDeployE.getCreationDate().getTime()).collect(Collectors.toList());
        int len = versionES.size() - 1;
        while (len > 0) {
            ApplicationVersionE versionE = versionES.get(len);
            Optional<String> branch = Arrays.stream(TYPE).filter(t -> versionE.getVersion().contains(t)).findFirst();
            String version = branch.isPresent() && !branch.get().isEmpty() ? branch.get() : null;
            if (version != null && appDeployE.getTriggerVersion().contains(version)) {
                break;
            }
            len--;
        }
        //保存记录
        PipelineTaskRecordE pipelineTaskRecordE = new PipelineTaskRecordE(stageRecordId, pipelineTaskE.getType(),
                appDeployE.getTriggerVersion(), appDeployE.getApplicationId(),
                appDeployE.getEnvId(), appDeployE.getInstanceId(),
                valueRepository.queryById(appDeployE.getValueId()).getValue());
        pipelineTaskRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
        taskRecordRepository.createOrUpdate(pipelineTaskRecordE);
        try {
            String type = appDeployE.getInstanceId() == null ? CommandType.CREATE.getType() : CommandType.UPDATE.getType();
            ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO(versionES.get(len).getId(), appDeployE.getEnvId(),
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

//    public void conductTaskrecord(Long taskRecordId, String proInstacneId) {
//        //更新此任务状态
//        PipelineTaskRecordE pipelineTaskRecordE = new PipelineTaskRecordE();
//        pipelineTaskRecordE.setId(taskRecordId);
//        pipelineTaskRecordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
//        taskRecordRepository.createOrUpdate(pipelineTaskRecordE);
////        conNextTaskRecord(taskRecordId, proInstacneId, pipelineTaskRecordE.getStageRecordId());
//    }

    @Override
    public void audit(Long projectId, PipelineUserRecordRelDTO recordRelDTO) {
        String status = workFlowRepository.approveUserTask(projectId, pipelineRecordRepository.queryById(recordRelDTO.getPipelineRecordId()).getProcessInstanceId(), recordRelDTO.getIsApprove());

        PipelineUserRecordRelE userRelE = new PipelineUserRecordRelE();
        userRelE.setUserId(DetailsHelper.getUserDetails().getUserId());
        switch (recordRelDTO.getType()) {
            case "task": {
                PipelineTaskRecordE recordE = taskRecordRepository.queryById(recordRelDTO.getTaskRecordId());
                recordE.setStatus(status);
                taskRecordRepository.createOrUpdate(recordE);
                userRelE.setTaskRecordId(recordE.getId());
                conNextTaskRecord(recordE.getId(), recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                break;
            }
            case "stage": {
                PipelineStageRecordE recordE = stageRecordRepository.queryById(recordRelDTO.getStageRecordId());
                recordE.setStatus(status);
                stageRecordRepository.createOrUpdate(recordE);
                userRelE.setStageRecordId(recordE.getId());
                //阶段中的第一个任务为人工任务时
                if (status.equals(WorkFlowStatus.SUCCESS.toValue())) {
                    PipelineTaskE pipelineTaskE = pipelineTaskRepository.queryByStageId(stageRecordRepository.queryById(recordRelDTO.getStageRecordId()).getStageId()).get(0);
                    if ("manual".equals(pipelineTaskE.getType())) {
                        PipelineTaskRecordE taskRecordE = new PipelineTaskRecordE();
                        BeanUtils.copyProperties(pipelineTaskE, taskRecordE);
                        taskRecordE.setStageRecordId(recordRelDTO.getStageRecordId());
                        taskRecordE.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
                        taskRecordRepository.createOrUpdate(taskRecordE);
                    }
                }
                break;
            }
            case "pipeline": {
                PipelineRecordE recordE = pipelineRecordRepository.queryById(recordRelDTO.getPipelineRecordId());
                recordE.setStatus(status);
                pipelineRecordRepository.update(recordE);
                userRelE.setPipelineRecordId(recordE.getId());
                //启动第一个阶段
                if (status.equals(WorkFlowStatus.SUCCESS.toValue())) {
                    PipelineStageRecordE stageRecordE = stageRecordRepository.queryByPipeRecordId(recordRelDTO.getPipelineRecordId(), null).get(0);
                    stageRecordE.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
                    stageRecordRepository.createOrUpdate(stageRecordE);
                }
                break;
            }
            default: {
                break;
            }
        }
        pipelineUserRelRecordRepository.create(userRelE);
    }

    /**
     * 检测是否满足部署条件
     *
     * @param pipelineId
     * @return
     */
    @Override
    public Boolean checkDeploy(Long pipelineId) {
        List<PipelineAppDeployE> appDeployEList = new ArrayList<>();
        //获取所有appDeploy
        stageRepository.queryByPipelineId(pipelineId).forEach(stageE -> {
            pipelineTaskRepository.queryByStageId(stageE.getId()).forEach(taskE -> {
                if (taskE.getAppDeployId() != null) {
                    PipelineAppDeployE appDeployE = appDeployRepository.queryById(taskE.getAppDeployId());
                    appDeployEList.add(appDeployE);
                }
            });
        });
        //检测是否满足条件
        for (PipelineAppDeployE appDeployE : appDeployEList) {
            if (appDeployE.getCreationDate().getTime() > versionRepository.getLatestVersion(appDeployE.getApplicationId()).getCreationDate().getTime()) {
                return false;
            } else {
                //是否有对应版本
                List<ApplicationVersionE> list = versionRepository.listByAppId(appDeployE.getApplicationId(), null)
                        .stream()
                        .filter(versionE -> versionE.getCreationDate().getTime() > appDeployE.getCreationDate().getTime())
                        .collect(Collectors.toList());
                Boolean index = false;
                for (ApplicationVersionE versionE : list) {
                    Optional<String> branch = Arrays.stream(TYPE).filter(t -> versionE.getVersion().contains(t)).findFirst();
                    String version = branch.isPresent() && !branch.get().isEmpty() ? branch.get() : null;
                    if (version != null && appDeployE.getTriggerVersion().contains(version)) {
                        index = true;
                    }
                }
                if (!index) {
                    return false;
                }
            }
        }
        return true;
    }

    private PipelineTaskE getFirsetTask(Long pipelineId) {
        return pipelineTaskRepository.queryByStageId(stageRepository.queryByPipelineId(pipelineId).get(0).getId()).get(0);
    }

    /**
     * 准备workflow创建实例所需数据
     * 为此workflow下所有stage创建记录
     */
    @Override
    public DevopsPipelineDTO setWorkFlowDTO(Long pipelineRecordId, Long pipelineId) {
        //workflow数据
        DevopsPipelineDTO devopsPipelineDTO = new DevopsPipelineDTO();
        devopsPipelineDTO.setBussinessId(pipelineRecordId);
        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
        //stage
        stageRepository.queryByPipelineId(pipelineId).forEach(t -> {
            //创建所有stageRecord
            PipelineStageRecordE recordE = new PipelineStageRecordE();
            BeanUtils.copyProperties(t, recordE);
            recordE.setStageId(t.getId());
            recordE.setPipelineRecordId(pipelineRecordId);
            recordE = stageRecordRepository.createOrUpdate(recordE);
            Long stageRecordId = recordE.getId();

            //stage
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
                Map<String, Object> params = new HashMap<>();
                params.put("taskId", task.getId());
                params.put("stageRecordId", stageRecordId);
                devopsPipelineTaskDTO.setParams(params);
                devopsPipelineTaskDTOS.add(devopsPipelineTaskDTO);
            });
            devopsPipelineStageDTO.setTasks(devopsPipelineTaskDTOS);
            devopsPipelineStageDTOS.add(devopsPipelineStageDTO);


        });
        devopsPipelineDTO.setStages(devopsPipelineStageDTOS);
        return devopsPipelineDTO;
    }

    private void conNextTaskRecord(Long taskRecordId, Long pipelineRecordId, Long stageRecordId) {

        //属于阶段的最后一个任务
        Long stageId = isStageLastTask(taskRecordId);
        if (stageId != null) {
            PipelineStageRecordE stageRecordE = new PipelineStageRecordE();
            stageRecordE.setId(taskRecordRepository.queryById(taskRecordId).getStageRecordId());
            stageRecordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
            stageRecordRepository.createOrUpdate(stageRecordE);

            //属于pipeline最后一个任务
            Long pipelineId = isPipelineLastTask(stageId);
            PipelineRecordE recordE = pipelineRecordRepository.queryById(pipelineRecordId);
            if (pipelineId != null) {
                recordE.setStatus(WorkFlowStatus.SUCCESS.toValue());
                pipelineRecordRepository.update(recordE);
            } else {
                //更新下一个阶段状态
                PipelineStageE nextStage = getNextStage(stageId);
                PipelineStageRecordE pipelineStageRecordE = stageRecordRepository.queryByPipeRecordId(recordE.getId(), nextStage.getId()).get(0);
                pipelineStageRecordE.setStatus(WorkFlowStatus.RUNNING.toValue());
                stageRecordRepository.createOrUpdate(pipelineStageRecordE);
            }
        } else {
            //更新下一个任务记录
            //部署任务不处理.创建人工任务记录
            PipelineTaskE nextTask = getNextTask(taskRecordRepository.queryById(taskRecordId).getTaskId());
            if ("manual".equals(nextTask.getType())) {
                PipelineTaskRecordE nextTaskRecordE = new PipelineTaskRecordE();
                nextTaskRecordE.setStageRecordId(stageRecordId);
                nextTaskRecordE.setIsCountersigned(1);
                nextTaskRecordE.setStatus(WorkFlowStatus.PENDINGCHECK.toValue());
                nextTaskRecordE.setTaskId(nextTask.getId());
                taskRecordRepository.createOrUpdate(nextTaskRecordE);
            }
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

    private Long isPipelineLastTask(Long stageId) {
        PipelineStageE pipelineStageE = stageRepository.queryById(stageId);
        List<PipelineStageE> pipelineStageES = stageRepository.queryByPipelineId(pipelineStageE.getPipelineId());
        return pipelineStageES.get(pipelineStageES.size() - 1).getId().equals(pipelineStageE.getId()) ? pipelineStageE.getPipelineId() : null;
    }

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

