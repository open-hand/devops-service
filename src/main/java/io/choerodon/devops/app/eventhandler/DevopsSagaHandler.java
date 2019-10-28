package io.choerodon.devops.app.eventhandler;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.api.vo.PushWebHookVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.app.service.impl.UpdateAppUserPermissionServiceImpl;
import io.choerodon.devops.app.service.impl.UpdateEnvUserPermissionServiceImpl;
import io.choerodon.devops.app.service.impl.UpdateUserPermissionService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.dto.PipelineTaskRecordDTO;
import io.choerodon.devops.infra.enums.PipelineNoticeType;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Creator: Runge
 * Date: 2018/7/27
 * Time: 10:06
 * Description: Saga msg by DevOps self
 */
@Component
public class DevopsSagaHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsSagaHandler.class);

    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsGitlabPipelineService devopsGitlabPipelineService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private PipelineTaskRecordService pipelineTaskRecordService;
    @Autowired
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private PipelineRecordService pipelineRecordService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private UpdateEnvUserPermissionServiceImpl updateUserEnvPermissionService;


    /**
     * devops创建环境
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_ENV,
            description = "devops创建环境",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_ENV,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateEnv(String data) {
        EnvGitlabProjectPayload gitlabProjectPayload = gson.fromJson(data, EnvGitlabProjectPayload.class);
        try {
            devopsEnvironmentService.handleCreateEnvSaga(gitlabProjectPayload);
        } catch (Exception e) {
            devopsEnvironmentService.setEnvErrStatus(data, gitlabProjectPayload.getIamProjectId());
            throw e;
        }

        // 执行到此处说明创建环境成功了
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService
                .baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        if (devopsEnvironmentDTO.getFailed() != null && devopsEnvironmentDTO.getFailed()) {
            devopsEnvironmentDTO.setSynchro(true);
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
        LOGGER.info("To set error status for environment with code: {}", gitlabProjectPayload.getPath());
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService
                .baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        devopsEnvironmentDTO.setSynchro(true);
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
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_APPLICATION_SERVICE,
            description = "创建gitlab项目",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_APPLICATION_SERVICE,
            maxRetryCount = 3,
            seq = 1)
    public String createAppService(String data) {
        DevOpsAppServicePayload devOpsAppServicePayload = gson.fromJson(data, DevOpsAppServicePayload.class);
        try {
            appServiceService.operationApplication(devOpsAppServicePayload);
        } catch (Exception e) {
            appServiceService.setAppErrStatus(data, devOpsAppServicePayload.getIamProjectId());
            throw e;
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
        DevOpsAppImportServicePayload devOpsAppImportPayload = gson.fromJson(data, DevOpsAppImportServicePayload.class);
        try {
            appServiceService.operationAppServiceImport(devOpsAppImportPayload);
        } catch (Exception e) {
            appServiceService.setAppErrStatus(data, devOpsAppImportPayload.getIamProjectId());
            throw e;
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
                    .updateUserPermission(devOpsUserPayload.getIamProjectId(), devOpsUserPayload.getAppServiceId(),
                            devOpsUserPayload.getIamUserIds(), devOpsUserPayload.getOption());
        } catch (Exception e) {
            LOGGER.error("update gitlab users {} error", devOpsUserPayload.getIamUserIds());
            throw e;
        }
        return data;
    }


    /**
     * devops处理环境权限分配相应的gitlab操作
     */
    @SagaTask(code = SagaTopicCodeConstants.DEVOPS_UPDATE_ENV_PERMISSION,
            description = "在gitlab更新环境的权限",
            sagaCode = SagaTopicCodeConstants.DEVOPS_UPDATE_ENV_PERMISSION,
            maxRetryCount = 3,
            seq = 1)
    public String operateEnvPermissionInGitlab(String payload) {
        DevopsEnvUserPayload devopsEnvUserPayload = gson.fromJson(payload, DevopsEnvUserPayload.class);
        try {
            updateUserEnvPermissionService.updateUserPermission(devopsEnvUserPayload);
        } catch (Exception e) {
            LOGGER.error("update environment gitlab permission for iam users {} error", devopsEnvUserPayload.getIamUserIds());
            throw e;
        }
        return payload;
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
        AppServiceDeployVO appServiceDeployVO = gson.fromJson(data, AppServiceDeployVO.class);
        Long taskRecordId = appServiceDeployVO.getRecordId();
        Long stageRecordId = pipelineTaskRecordService.baseQueryRecordById(taskRecordId).getStageRecordId();
        PipelineStageRecordDTO stageRecordDTO = pipelineStageRecordService.baseQueryById(stageRecordId);
        PipelineTaskRecordDTO taskRecordDTO = pipelineTaskRecordService.baseQueryRecordById(taskRecordId);
        Long pipelineRecordId = stageRecordDTO.getPipelineRecordId();
        try {
            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(appServiceDeployVO, true);
            if (!pipelineRecordService.baseQueryById(pipelineRecordId).getStatus().equals(WorkFlowStatus.FAILED.toValue()) || stageRecordDTO.getIsParallel() == 1) {
                if (!taskRecordDTO.getStatus().equals(WorkFlowStatus.FAILED.toValue())) {
                    PipelineTaskRecordDTO pipelineTaskRecordDTO = new PipelineTaskRecordDTO();
                    pipelineTaskRecordDTO.setInstanceId(appServiceInstanceVO.getId());
                    pipelineTaskRecordDTO.setStatus(WorkFlowStatus.SUCCESS.toString());
                    pipelineTaskRecordDTO.setId(appServiceDeployVO.getRecordId());
                    pipelineTaskRecordService.baseCreateOrUpdateRecord(pipelineTaskRecordDTO);
                    LOGGER.info("create pipeline auto deploy instance success");
                }
            }
        } catch (Exception e) {
            LOGGER.error("error create pipeline auto deploy instance {}", e);
            PipelineTaskRecordDTO pipelineTaskRecordDTO = new PipelineTaskRecordDTO();
            pipelineTaskRecordDTO.setId(appServiceDeployVO.getRecordId());
            pipelineTaskRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
            pipelineTaskRecordService.baseCreateOrUpdateRecord(pipelineTaskRecordDTO);

            Long time = System.currentTimeMillis() - TypeUtil.objToLong(stageRecordDTO.getExecutionTime());
            stageRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
            stageRecordDTO.setExecutionTime(time.toString());
            pipelineStageRecordService.baseCreateOrUpdate(stageRecordDTO);
            
            pipelineService.updateStatus(pipelineRecordId, null, WorkFlowStatus.FAILED.toValue(), e.getMessage());
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            user.setEmail(GitUserNameUtil.getEmail());
            user.setId(GitUserNameUtil.getUserId().longValue());
            pipelineService.sendSiteMessage(pipelineRecordId, PipelineNoticeType.PIPELINEFAILED.toValue(), Collections.singletonList(user), new HashMap<>());
            LOGGER.info("send pipeline failed message to the user. The user id is {}", user.getId());
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
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateInstance(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        InstanceSagaPayload instanceSagaPayload;
        try {
            instanceSagaPayload = objectMapper.readValue(data, InstanceSagaPayload.class);
        } catch (IOException e) {
            throw new CommonException("Error deserializing the data of instance when consuming create instance event");
        }
        appServiceInstanceService.createInstanceBySaga(instanceSagaPayload);
        return data;
    }


    /**
     * devops创建网络
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_SERVICE,
            description = "devops创建网络",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_SERVICE,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateService(String data) {
        ServiceSagaPayLoad serviceSagaPayLoad = gson.fromJson(data, ServiceSagaPayLoad.class);
        devopsServiceService.createServiceBySaga(serviceSagaPayLoad);
        return data;
    }


    /**
     * devops创建域名
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_INGRESS,
            description = "devops创建域名",
            sagaCode = SagaTopicCodeConstants.DEVOPS_CREATE_INGRESS,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateIngress(String data) {
        IngressSagaPayload ingressSagaPayload = gson.fromJson(data, IngressSagaPayload.class);
        devopsIngressService.operateIngressBySaga(ingressSagaPayload);
        return data;
    }

    /**
     * devops导入内部应用服务
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_IMPORT_INTERNAL_APPLICATION_SERVICE,
            description = "devops导入内部应用服务",
            sagaCode = SagaTopicCodeConstants.DEVOPS_IMPORT_INTERNAL_APPLICATION_SERVICE,
            maxRetryCount = 3,
            seq = 1)
    public String importAppServiceGitlab(String data) {
        AppServiceImportPayload appServiceImportPayload = gson.fromJson(data, AppServiceImportPayload.class);
        try {
            appServiceService.importAppServiceGitlab(appServiceImportPayload);
        } catch (Exception e) {
            DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
            devOpsAppServicePayload.setAppServiceId(appServiceImportPayload.getAppServiceId());
            appServiceService.setAppErrStatus(gson.toJson(devOpsAppServicePayload), appServiceImportPayload.getProjectId());
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
        DevOpsAppServicePayload devOpsAppServicePayload = gson.fromJson(data, DevOpsAppServicePayload.class);
        AppServiceDTO applicationDTO = appServiceService.baseQuery(devOpsAppServicePayload.getAppServiceId());
        applicationDTO.setSynchro(true);
        applicationDTO.setFailed(true);
        appServiceService.baseUpdate(applicationDTO);
        LOGGER.info("================应用服务创建失败执行回写失败状态成功，serviceId：{}", devOpsAppServicePayload.getAppServiceId());
        return data;
    }

    /**
     * devops删除环境
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_DELETE_ENV,
            description = "GitOps 应用创建失败处理",
            sagaCode = SagaTopicCodeConstants.DEVOPS_DELETE_ENV,
            maxRetryCount = 3,
            seq = 1)
    public void deleteEnv(String data) {
        JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
        Long envId = jsonObject.get("envId").getAsLong();
        devopsEnvironmentService.deleteEnvSaga(envId);
        LOGGER.info("================删除环境成功，envId：{}",envId);

    }

    /**
     * Devops删除应用服务
     * @param data
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_APP_DELETE,
            sagaCode = SagaTopicCodeConstants.DEVOPS_APP_DELETE,
            description = "Devops删除应用服务",maxRetryCount = 3,
            seq = 1)
    public void  deleteAppService(String data) {
        DevOpsAppServicePayload devOpsAppServicePayload = gson.fromJson(data, DevOpsAppServicePayload.class);
        appServiceService.deleteAppServiceSage(devOpsAppServicePayload.getIamProjectId(),devOpsAppServicePayload.getAppServiceId());
        LOGGER.info("================删除应用服务执行成功，serviceId：{}", devOpsAppServicePayload.getAppServiceId());
    }

}
