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
import io.choerodon.devops.api.dto.PipelineRecordReqDTO;
import io.choerodon.devops.api.dto.PipelineReqDTO;
import io.choerodon.devops.api.dto.PipelineStageDTO;
import io.choerodon.devops.api.dto.PipelineStageRecordDTO;
import io.choerodon.devops.api.dto.PipelineTaskDTO;
import io.choerodon.devops.api.dto.PipelineTaskRecordDTO;
import io.choerodon.devops.api.dto.PipelineUserRecordRelDTO;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployValueE;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
    private static final String[] TYPE = {"feature", "bugfix", "release", "hotfix", "custom", "master"};
    private static final String MANUAL = "manual";
    private static final String AUTO = "auto";
    private static final String PIPELINE = "pipeline";
    private static final String STAGE = "stage";
    private static final String TASK = "task";



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
        BeanUtils.copyProperties(pipelineDTOS, page);
        page.setContent(pipelineDTOS.getContent().stream().peek(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreatedBy());
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
        List<PipelineRecordDTO> pipelineRecordDTOS = pageRecordDTOS.getContent().stream().map(t -> {
            t.setStageDTOList(ConvertHelper.convertList(stageRecordRepository.list(projectId, t.getId()), PipelineStageRecordDTO.class));
            if (t.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                for (int i = 0; i < t.getStageDTOList().size(); i++) {
                    if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())) {
                        t.setType(STAGE);
                        t.setStageName(t.getStageDTOList().get(i - 1).getStageName());
                        t.setRecordId(t.getStageDTOList().get(i).getId());
                        break;
                    } else if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.RUNNING.toValue())) {
                        Optional<PipelineTaskRecordE> taskRecordE = taskRecordRepository.queryByStageRecordId(t.getStageDTOList().get(i).getId(), null)
                                .stream().filter(task -> task.getStatus().equals(WorkFlowStatus.PENDINGCHECK.toValue())).findFirst();
                        t.setType(TASK);
                        t.setStageName(t.getStageDTOList().get(i).getStageName());
                        t.setRecordId(taskRecordE.get().getId());
                        break;
                    }
                }
            } else if (t.getStatus().equals(WorkFlowStatus.STOP.toValue())) {
                t.setType(TASK);
                for (int i = 0; i < t.getStageDTOList().size(); i++) {
                    if (t.getStageDTOList().get(i).getStatus().equals(WorkFlowStatus.STOP.toValue())) {
                        t.setType(STAGE);
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
            pipelineReqDTO.getPipelineStageDTOS().get(i).getPipelineTaskDTOS().forEach(t -> {
                t.setProjectId(projectId);
                t.setStageId(stageId);
                if (AUTO.equals(t.getType())) {
                    //appDeployValue
                    PipelineAppDeployValueE appDeployValueE = new PipelineAppDeployValueE();
                    appDeployValueE.setValue(t.getAppDeployDTOS().getValue());
                    appDeployValueE.setValueId(t.getAppDeployDTOS().getValueId());
                    appDeployValueE = valueRepository.create(appDeployValueE);
                    //appDeploy
                    PipelineAppDeployE appDeployE = ConvertHelper.convert(t.getAppDeployDTOS(), PipelineAppDeployE.class);
                    appDeployE.setValueId(appDeployValueE.getId());
                    appDeployE.setProjectId(projectId);
                    t.setAppDeployId(appDeployRepository.create(appDeployE).getId());
                }
                Long taskId = pipelineTaskRepository.create(ConvertHelper.convert(t, PipelineTaskE.class)).getId();
                if (MANUAL.equals(t.getType())) {
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
                    PipelineAppDeployValueE appDeployValueE = new PipelineAppDeployValueE();
                    appDeployValueE.setId(t.getAppDeployDTOS().getValueId());
                    appDeployValueE.setValue(t.getAppDeployDTOS().getValue());
                    valueRepository.update(appDeployValueE);
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
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId, Long pipelineId) {
        //回写记录状态
        pipelineRecordRepository.queryByPipelineId(pipelineId).forEach(t -> {
            PipelineRecordE recordE = new PipelineRecordE();
            recordE.setId(t.getId());
            recordE.setPipelineId(pipelineId);
            recordE.setStatus(WorkFlowStatus.DELETED.toValue());
            pipelineRecordRepository.update(recordE);
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
            case TASK: {
                PipelineTaskRecordE recordE = taskRecordRepository.queryById(recordRelDTO.getTaskRecordId());
                recordE.setStatus(status);
                taskRecordRepository.createOrUpdate(recordE);
                userRelE.setTaskRecordId(recordE.getId());
                conNextTaskRecord(recordE.getId(), recordRelDTO.getPipelineRecordId(), recordRelDTO.getStageRecordId());
                break;
            }
            case STAGE: {
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
            case PIPELINE: {
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
        //判断pipeline是否被禁用
        if (pipelineRepository.queryById(pipelineId).getIsEnabled() == 0) {
            return false;
        }
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
        devopsPipelineDTO.setPipelineRecordId(pipelineRecordId);
        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
        //stage
        stageRepository.queryByPipelineId(pipelineId).forEach(t -> {
            //创建所有stageRecord
            PipelineStageRecordE recordE = new PipelineStageRecordE();
            BeanUtils.copyProperties(t, recordE);
            recordE.setStageId(t.getId());
            recordE.setPipelineRecordId(pipelineRecordId);
            recordE = stageRecordRepository.createOrUpdate(recordE);

            //stage
            DevopsPipelineStageDTO devopsPipelineStageDTO = new DevopsPipelineStageDTO();
            devopsPipelineStageDTO.setStageRecordId(recordE.getId());
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
                devopsPipelineTaskDTO.setTaskId(task.getId());
                devopsPipelineTaskDTOS.add(devopsPipelineTaskDTO);
            });
            devopsPipelineStageDTO.setTasks(devopsPipelineTaskDTOS);
            devopsPipelineStageDTOS.add(devopsPipelineStageDTO);


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
        return null;
    }

    @Override
    public void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long taskId) {
        List<PipelineTaskRecordE> list = taskRecordRepository.queryByStageRecordId(stageRecordId, taskId);
        if (list != null && list.size() > 0) {
            conNextTaskRecord(list.get(0).getId(), pipelineRecordId, stageRecordId);
        }
    }

    @Override
    public PipelineRecordReqDTO getRecordById(Long projectId, Long pipelineRecordId) {
        PipelineRecordReqDTO recordReqDTO = new PipelineRecordReqDTO();
        BeanUtils.copyProperties(pipelineRecordRepository.queryById(pipelineRecordId), recordReqDTO);
        //获取pipeline触发人员
        UserE userE = getTriggerUser(pipelineRecordId, null);
        if (userE != null) {
            recordReqDTO.setTriggerUserId(userE.getId());
            recordReqDTO.setTriggerUserName(userE.getRealName());
        }
        //查询stage
        List<PipelineStageRecordDTO> recordDTOList = new ArrayList<>();
        stageRecordRepository.queryByPipeRecordId(pipelineRecordId, null).forEach(t ->
                recordDTOList.add(ConvertHelper.convert(t, PipelineStageRecordDTO.class))
        );
        recordDTOList.forEach(t -> {
            //获取stage触发人员
            UserE userE1 = getTriggerUser(null, t.getId());
            if (userE1 != null) {
                t.setTriggerUserId(userE1.getId());
                t.setTriggerUserName(userE1.getRealName());
            }
            List<PipelineTaskRecordDTO> taskRecordDTOS = new ArrayList<>();
            //查询task
            taskRecordRepository.queryByStageRecordId(t.getId(), null).forEach(r -> {
                        PipelineTaskRecordDTO taskRecordDTO = ConvertHelper.convert(r, PipelineTaskRecordDTO.class);
                        //获取task触发人员
                        taskRecordDTO.setAuditUsers(StringUtils.join(pipelineUserRelRecordRepository.queryByRecordId(null, null, r.getId())
                                .stream()
                                .map(u -> iamRepository.queryUserByUserId(u.getUserId()).getRealName())
                                .toArray(), ","));
                        taskRecordDTOS.add(taskRecordDTO);
                    }
            );
            t.setTaskRecordDTOS(taskRecordDTOS);
        });
        recordReqDTO.setStageRecordDTOS(recordDTOList);

        return recordReqDTO;
    }

    @Override
    public void retry(Long projectId, Long pipelineRecordId) {
        String bpmDefinition = pipelineRecordRepository.queryById(pipelineRecordId).getBpmDefinition();
        DevopsPipelineDTO pipelineDTO = gson.fromJson(bpmDefinition, DevopsPipelineDTO.class);
        workFlowRepository.create(projectId, pipelineDTO);
    }

    @Override
    public List<PipelineRecordDTO> queryByPipelineId(Long pipelineId) {
        return ConvertHelper.convertList(pipelineRecordRepository.queryByPipelineId(pipelineId), PipelineRecordDTO.class);
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

    private UserE getTriggerUser(Long pipelineRecordId, Long stageRecordId) {
        List<PipelineUserRecordRelE> taskUserRecordRelES = pipelineUserRelRecordRepository.queryByRecordId(pipelineRecordId, stageRecordId, null);
        if (taskUserRecordRelES != null && taskUserRecordRelES.size() > 0) {
            Long triggerUserId = taskUserRecordRelES.get(0).getUserId();
            return iamRepository.queryUserByUserId(triggerUserId);
        }
        return null;
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
        } else {
            relEList.forEach(delUserId -> {
                PipelineUserRelE addUserRelE = new PipelineUserRelE(delUserId, pipelineId, stageId, taskId);
                pipelineUserRelRepository.delete(addUserRelE);
            });
        }
    }

}

