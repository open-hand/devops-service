package io.choerodon.devops.app.eventhandler;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.app.service.impl.UpdateAppUserPermissionServiceImpl;
import io.choerodon.devops.app.service.impl.UpdateUserPermissionService;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.PipelineNoticeType;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Creator: Runge
 * Date: 2018/7/27
 * Time: 10:06
 * Description: Saga msg by DevOps self
 */
@Component
public class DevopsSagaHandler {
    private static final String TEMPLATE = "template";
    private static final String APPLICATION = "application";
    private static final String STATUS_FIN = "finished";
    private static final String STATUS_FAILED = "failed";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsSagaHandler.class);

    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private ApplicationTemplateService applicationTemplateService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private DevopsGitlabPipelineService devopsGitlabPipelineService;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private PipelineTaskRecordService taskRecordRepository;
    @Autowired
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private PipelineRecordService pipelineRecordService;


    /**
     * devops创建环境
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_ENV,
            description = "devops创建环境",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_ENV,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateEnv(String data) {
        GitlabProjectPayload gitlabProjectPayload = gson.fromJson(data, GitlabProjectPayload.class);
        try {
            devopsEnvironmentService.handleCreateEnvSaga(gitlabProjectPayload);
        } catch (Exception e) {
            devopsEnvironmentService.setEnvErrStatus(data, gitlabProjectPayload.getIamProjectId());
            throw e;
        }
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService
                .baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        if (devopsEnvironmentDTO.getFailed() != null && devopsEnvironmentDTO.getFailed()) {
            devopsEnvironmentDTO.setFailed(false);
            devopsEnvironmentService.baseUpdate(devopsEnvironmentDTO);
        }
        return data;
    }

    /**
     * 环境创建失败
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_ENV_ERROR,
            description = "环境创建失败",
            sagaCode = SagaTopicCodeConstants.DEVOPS_SET_ENV_ERR,
            maxRetryCount = 3,
            seq = 1)
    public String setEnvErr(String data) {
        GitlabProjectPayload gitlabProjectPayload = gson.fromJson(data, GitlabProjectPayload.class);
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService
                .baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        devopsEnvironmentDTO.setFailed(true);
        devopsEnvironmentService.baseUpdate(devopsEnvironmentDTO);
        return data;
    }

    /**
     * GitOps 事件处理
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_GIT_OPS,
            description = "gitops事件处理",
            sagaCode = SagaTopicCodeConstants.DEVOPS_SYNC_GITOPS,
            concurrentLimitNum = 1,
            maxRetryCount = 3,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            seq = 1)
    public String gitops(String data) {
        PushWebHookVO pushWebHookVO = null;
        try {
            pushWebHookVO = objectMapper.readValue(data, PushWebHookVO.class);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
        devopsGitService.fileResourceSync(pushWebHookVO);
        return data;
    }

    /**
     * 创建gitlab项目
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_OPERATE_GITLAB_PROJECT,
            description = "创建gitlab项目",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_GITLAB_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String createApp(String data) {
        DevOpsAppPayload devOpsAppPayload = gson.fromJson(data, DevOpsAppPayload.class);
        if (devOpsAppPayload.getType().equals(APPLICATION)) {
            try {
                applicationService.operationApplication(devOpsAppPayload);
            } catch (Exception e) {
                applicationService.setAppErrStatus(data, devOpsAppPayload.getIamProjectId());
                throw e;
            }
        }
        return data;
    }

    /**
     * GitOps 事件处理
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_GITLAB_PROJECT,
            description = "Devops从外部代码平台导入到gitlab项目",
            sagaCode = SagaTopicCodeConstants.DEVOPS_IMPORT_GITLAB_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String importApp(String data) {
        DevOpsAppImportPayload devOpsAppImportPayload = gson.fromJson(data, DevOpsAppImportPayload.class);
        if (devOpsAppImportPayload.getType().equals(APPLICATION)) {
            try {
                applicationService.operationApplicationImport(devOpsAppImportPayload);
            } catch (Exception e) {
                applicationService.setAppErrStatus(data, devOpsAppImportPayload.getIamProjectId());
                throw e;
            }
            ApplicationDTO applicationDTO = applicationService.baseQuery(devOpsAppImportPayload.getAppId());
            if (applicationDTO.getFailed() != null && applicationDTO.getFailed()) {
                applicationDTO.setFailed(false);
                if (1 != applicationService.baseUpdate(applicationDTO)) {
                    LOGGER.error("update application set create success status error");
                }
            }
//            gitlabRepository.batchAddProjectVariable(applicationE.getGitlabProjectE().getId(), TypeUtil.objToInteger(devOpsAppImportPayload.getGitlabUserId()),
//                    applicationService.setVariableDTO(applicationE.getHarborConfigE().getId(),applicationE.getChartConfigE().getId()));
        }
        return data;
    }

    /**
     * GitOps 用户权限分配处理
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_UPDATE_GITLAB_USERS,
            description = "GitOps 用户权限分配处理",
            sagaCode = SagaTopicCodeConstants.DEVOPS_UPDATE_GITLAB_USERS,
            maxRetryCount = 3,
            seq = 1)
    public String updateGitlabUser(String data) {
        DevOpsUserPayload devOpsUserPayload = gson.fromJson(data, DevOpsUserPayload.class);
        try {
            UpdateUserPermissionService updateUserPermissionService = new UpdateAppUserPermissionServiceImpl();
            updateUserPermissionService
                    .updateUserPermission(devOpsUserPayload.getIamProjectId(), devOpsUserPayload.getAppId(),
                            devOpsUserPayload.getIamUserIds(), devOpsUserPayload.getOption());
        } catch (Exception e) {
            LOGGER.error("update gitlab users {} error", devOpsUserPayload.getIamUserIds());
            throw e;
        }
        return data;
    }

    /**
     * GitOps 应用创建失败处理
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_GITLAB_PROJECT_ERROR,
            description = "GitOps 应用创建失败处理",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_APP_FAIL,
            maxRetryCount = 3,
            seq = 1)
    public String setAppErr(String data) {
        DevOpsAppPayload devOpsAppPayload = gson.fromJson(data, DevOpsAppPayload.class);
        ApplicationDTO applicationDTO = applicationService.baseQuery(devOpsAppPayload.getAppId());
        applicationDTO.setFailed(true);
        if (1 != applicationService.baseUpdate(applicationDTO)) {
            LOGGER.error("update application {} set create failed status error", applicationDTO.getCode());
        }
        return data;
    }

    /**
     * GitOps应用模板创建失败处理
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_GITLAB_PROJECT_TEMPLATE_ERROR,
            description = "GitOps应用模板创建失败处理",
            sagaCode = DEVOPS_SET_APPLICATION_TEMPLATE_ERROR,
            maxRetryCount = 3,
            seq = 1)
    public String setAppTemplateErr(String data) {
        DevOpsAppPayload devOpsAppPayload = gson.fromJson(data, DevOpsAppPayload.class);
        ApplicationTemplateDTO applicationTemplateDTO = applicationTemplateService.baseQueryByCode(
                devOpsAppPayload.getOrganizationId(), devOpsAppPayload.getPath());
        applicationTemplateDTO.setFailed(true);
        applicationTemplateService.baseUpdate(applicationTemplateDTO);
        return data;
    }

    /**
     * 模板事件处理
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_OPERATION_GITLAB_TEMPLATE_PROJECT,
            description = "模板事件处理",
            sagaCode = DEVOPS_CREATE_GITLAB_TEMPLATE_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String createTemplate(String data) {
        GitlabProjectPayload gitlabProjectEventDTO = gson.fromJson(data, GitlabProjectPayload.class);
        if (gitlabProjectEventDTO.getType().equals(TEMPLATE)) {
            try {
                applicationTemplateService.operationApplicationTemplate(gitlabProjectEventDTO);
            } catch (Exception e) {
                applicationTemplateService.setAppTemplateErrStatus(data, gitlabProjectEventDTO.getOrganizationId());
                throw e;
            }
            ApplicationTemplateDTO applicationTemplateDTO = applicationTemplateService.baseQueryByCode(
                    gitlabProjectEventDTO.getOrganizationId(), gitlabProjectEventDTO.getPath());
            if (applicationTemplateDTO.getFailed() != null && applicationTemplateDTO.getFailed()) {
                applicationTemplateDTO.setFailed(false);
                applicationTemplateService.baseUpdate(applicationTemplateDTO);
            }
        }
        return data;
    }

    /**
     * gitlab pipeline事件
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_GITLAB_PIPELINE,
            description = "gitlab pipeline事件",
            sagaCode = DEVOPS_GITLAB_PIPELINE,
            maxRetryCount = 3,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            seq = 1)
    public String gitlabPipeline(String data) {
        PipelineWebHookVO pipelineWebHookVO = null;
        try {
            pipelineWebHookVO = objectMapper.readValue(data, PipelineWebHookVO.class);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
        devopsGitlabPipelineService.handleCreate(pipelineWebHookVO);
        return data;
    }

    /**
     * 创建流水线自动部署实例
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_PIPELINE_CREATE_INSTANCE,
            description = "创建流水线自动部署实例",
            sagaCode = DEVOPS_PIPELINE_AUTO_DEPLOY_INSTANCE,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            maxRetryCount = 3,
            seq = 1)
    public void pipelineAutoDeployInstance(String data) {
        ApplicationDeployVO applicationDeployVO = gson.fromJson(data, ApplicationDeployVO.class);
        Long taskRecordId = applicationDeployVO.getRecordId();
        Long stageRecordId = taskRecordRepository.baseQueryRecordById(taskRecordId).getStageRecordId();
        PipelineStageRecordDTO stageRecordDTO = pipelineStageRecordService.baseQueryById(stageRecordId);
        PipelineTaskRecordDTO taskRecordDTO = taskRecordRepository.baseQueryRecordById(taskRecordId);
        Long pipelineRecordId = stageRecordDTO.getPipelineRecordId();
        try {
            ApplicationInstanceVO applicationInstanceVO = applicationInstanceService.createOrUpdate(applicationDeployVO);
            if (!pipelineRecordService.baseQueryById(pipelineRecordId).getStatus().equals(WorkFlowStatus.FAILED.toValue()) || stageRecordDTO.getIsParallel() == 1) {
                if (!taskRecordDTO.getStatus().equals(WorkFlowStatus.FAILED.toValue())) {
                    PipelineTaskRecordDTO pipelineTaskRecordDTO = new PipelineTaskRecordDTO();
                    pipelineTaskRecordDTO.setInstanceId(applicationInstanceVO.getId());
                    pipelineTaskRecordDTO.setStatus(WorkFlowStatus.SUCCESS.toString());
                    pipelineTaskRecordDTO.setId(applicationDeployVO.getRecordId());
                    taskRecordRepository.baseCreateOrUpdateRecord(pipelineTaskRecordDTO);
                    LOGGER.info("create pipeline auto deploy instance success");
                }
            }
        } catch (Exception e) {
            PipelineTaskRecordDTO pipelineTaskRecordDTO = new PipelineTaskRecordDTO();
            pipelineTaskRecordDTO.setId(applicationDeployVO.getRecordId());
            pipelineTaskRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
            taskRecordRepository.baseCreateOrUpdateRecord(pipelineTaskRecordDTO);
            pipelineService.updateStatus(pipelineRecordId, stageRecordId, WorkFlowStatus.FAILED.toValue(), e.getMessage());
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            user.setEmail(GitUserNameUtil.getEmail());
            user.setId(GitUserNameUtil.getUserId().longValue());
            pipelineService.sendSiteMessage(pipelineRecordId, PipelineNoticeType.PIPELINEFAILED.toValue(), Collections.singletonList(user), new HashMap<>());
            LOGGER.error("error create pipeline auto deploy instance {}", e);
        }
    }

    /**
     * devops创建分支
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_BRANCH,
            description = "devops创建分支",
            sagaCode = DEVOPS_CREATE_BRANCH,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateBranch(String data) {
        BranchSagaPayLoad branchSagaDTO = gson.fromJson(data, BranchSagaPayLoad.class);
        devopsGitService.createBranchBySaga(branchSagaDTO);
        return data;
    }


    /**
     * devops创建实例
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_INSTANCE,
            description = "devops创建实例",
            sagaCode = DEVOPS_CREATE_INSTANCE,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateInstance(String data) {
        InstanceSagaPayload instanceSagaPayload = gson.fromJson(data, InstanceSagaPayload.class);
        applicationInstanceService.createInstanceBySaga(instanceSagaPayload);
        return data;
    }
}
