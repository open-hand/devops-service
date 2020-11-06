package io.choerodon.devops.app.eventhandler;

import static io.choerodon.asgard.saga.SagaDefinition.TimeoutPolicy.ALERT_ONLY;
import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.kubernetes.client.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.api.vo.PushWebHookVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.app.service.impl.UpdateEnvUserPermissionServiceImpl;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsClusterOperationRecordMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.JsonHelper;
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
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private UpdateEnvUserPermissionServiceImpl updateUserEnvPermissionService;
    @Autowired
    private DevopsPvcService devopsPvcService;
    @Autowired
    private DevopsPvService devopsPvService;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    @Autowired
    private DevopsCdPipelineService devopsCdPipelineService;
    @Autowired
    private DevopsCdJobRecordService devopsCdJobRecordService;
    @Autowired
    private DevopsCdStageRecordService devopsCdStageRecordService;
    @Autowired
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;
    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
    @Autowired
    private DevopsClusterNodeService devopsClusterNodeService;
    @Autowired
    private DevopsClusterOperationRecordMapper devopsClusterOperationRecordMapper;
    @Autowired
    private DevopsClusterNodeOperatorService devopsClusterNodeOperatorService;

    @Autowired
    private PipelineTaskRecordService pipelineTaskRecordService;
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

        //既然环境创建成功，那就发webhook吧
        sendNotificationService.sendWhenEnvCreate(devopsEnvironmentDTO, gitlabProjectPayload.getOrganizationId());
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
        //环境创建失败发送web_hook
        sendNotificationService.sendWhenCreateEnvFailed(devopsEnvironmentDTO, gitlabProjectPayload.getOrganizationId());
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
        devopsGitService.fileResourceSync(JsonHelper.unmarshalByJackson(data, PushWebHookVO.class));
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
            appServiceService.setAppErrStatus(data, devOpsAppServicePayload.getIamProjectId(), devOpsAppServicePayload.getAppServiceId());
            throw e;
        }
        //创建成功发送webhook
        sendNotificationService.sendWhenAppServiceCreate(appServiceService.baseQuery(devOpsAppServicePayload.getAppServiceId()));
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
            appServiceService.setAppErrStatus(data, devOpsAppImportPayload.getIamProjectId(), devOpsAppImportPayload.getAppServiceId());
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
        DevopsEnvUserPayload devopsEnvUserPayload = JsonHelper.unmarshalByJackson(payload, DevopsEnvUserPayload.class);
        try {
            updateUserEnvPermissionService.updateUserPermission(devopsEnvUserPayload);
        } catch (Exception e) {
            LOGGER.error("update environment gitlab permission for iam users {} error", devopsEnvUserPayload.getIamUserIds());
            throw e;
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvUserPayload.getIamProjectId());
        if (Objects.isNull(projectDTO)) {
            return payload;
        }
        //
        sendNotificationService.sendWhenEnvUpdatePermissions(devopsEnvUserPayload, projectDTO);
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
        devopsGitlabPipelineService.handleCreate(JsonHelper.unmarshalByJackson(data, PipelineWebHookVO.class));
        return data;
    }

    /**
     * gitlab ci pipeline事件
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_GITLAB_CI_PIPELINE,
            description = "gitlab pipeline事件",
            sagaCode = DEVOPS_GITLAB_CI_PIPELINE,
            maxRetryCount = 3,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            seq = 1)
    public String gitlabCiPipeline(String data) {
        devopsCiPipelineRecordService.handleCreate(JsonHelper.unmarshalByJackson(data, PipelineWebHookVO.class));
        return data;
    }

    /**
     * 监听gitlab ci pipeline事件，触发cd逻辑
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_GITLAB_CD_PIPELINE,
            description = "gitlab pipeline事件",
            sagaCode = DEVOPS_GITLAB_CI_PIPELINE,
            maxRetryCount = 3,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            seq = 20)
    public String gitlabCDPipeline(String data) {
        devopsCdPipelineService.handleCiPipelineStatusUpdate(JsonHelper.unmarshalByJackson(data, PipelineWebHookVO.class));
        return data;
    }

    /**
     * 监听gitlab ci pipeline事件，触发cd逻辑
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_TRIGGER_SIMPLE_CD_PIPELINE,
            description = "gitlab pipeline事件",
            sagaCode = DEVOPS_CI_PIPELINE_SUCCESS_FOR_SIMPLE_CD,
            maxRetryCount = 3,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            seq = 10)
    public String trigerSimpleCDPipeline(String data) {
        devopsCdPipelineService.trigerSimpleCDPipeline(JsonHelper.unmarshalByJackson(data, PipelineWebHookVO.class));
        return data;
    }

    /**
     * 创建流水线环境自动部署实例
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_PIPELINE_CREATE_INSTANCE,
            description = "创建流水线环境自动部署实例",
            sagaCode = DEVOPS_PIPELINE_ENV_AUTO_DEPLOY_INSTANCE,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            maxRetryCount = 3,
            seq = 1)
    public void pipelineEnvAutoDeployInstance(String data) {
        AppServiceDeployVO appServiceDeployVO = gson.fromJson(data, AppServiceDeployVO.class);
        Long jobRecordId = appServiceDeployVO.getRecordId();
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(jobRecordId);
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordService.queryById(devopsCdJobRecordDTO.getStageRecordId());
        Long pipelineRecordId = devopsCdStageRecordDTO.getPipelineRecordId();
        try {
            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(null, appServiceDeployVO, true);
            // 对于新建实例的部署任务，部署成功后修改为替换实例
            updateDeployTypeToUpdate(appServiceDeployVO.getDeployInfoId(), appServiceInstanceVO);
            DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordService.queryById(devopsCdJobRecordDTO.getId());
            if (PipelineStatus.RUNNING.toValue().equals(cdJobRecordDTO.getStatus())) {
                // 更新job状态为success
                devopsCdJobRecordDTO.setCommandId(appServiceInstanceVO.getCommandId());
                devopsCdJobRecordDTO.setFinishedDate(new Date());
                if (devopsCdJobRecordDTO.getStartedDate() != null) {
                    devopsCdJobRecordDTO.setDurationSeconds((new Date().getTime() - devopsCdJobRecordDTO.getStartedDate().getTime()) / 1000);
                }
                devopsCdJobRecordDTO.setStatus(PipelineStatus.SUCCESS.toValue());
                devopsCdJobRecordService.update(devopsCdJobRecordDTO);
            }
            LOGGER.info("create pipeline auto deploy instance success");
        } catch (Exception e) {
            LOGGER.error("error create pipeline auto deploy instance {}", e);
            devopsCdJobRecordService.updateJobStatusFailed(jobRecordId);
            devopsCdStageRecordService.updateStageStatusFailed(devopsCdStageRecordDTO.getId());
            devopsCdPipelineRecordService.updatePipelineStatusFailed(pipelineRecordId, e.getMessage());

            Long userId = GitUserNameUtil.getUserId();
            sendNotificationService.sendCdPipelineNotice(pipelineRecordId,
                    MessageCodeConstants.PIPELINE_FAILED,
                    userId, GitUserNameUtil.getEmail(), new HashMap<>());
            LOGGER.info("send pipeline failed message to the user. The user id is {}", userId);
        }
    }

    /**
     * 更新部署配置为替换实例
     *
     * @param deployInfoId
     * @param appServiceInstanceVO
     */
    private void updateDeployTypeToUpdate(Long deployInfoId, AppServiceInstanceVO appServiceInstanceVO) {
        DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(deployInfoId);
        devopsCdEnvDeployInfoDTO.setDeployType(CommandType.UPDATE.getType());
        devopsCdEnvDeployInfoDTO.setInstanceId(appServiceInstanceVO.getId());
        devopsCdEnvDeployInfoDTO.setInstanceName(appServiceInstanceVO.getCode());
        devopsCdEnvDeployInfoService.update(devopsCdEnvDeployInfoDTO);
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
        appServiceInstanceService.createInstanceBySaga(JsonHelper.unmarshalByJackson(data, InstanceSagaPayload.class));
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
     * devops创建PVC
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_PERSISTENTVOLUMECLAIM,
            description = "devops创建PVC",
            sagaCode = DEVOPS_CREATE_PERSISTENTVOLUMECLAIM,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreatePVC(String data) {
        PersistentVolumeClaimPayload persistentVolumeClaimPayload = gson.fromJson(data, PersistentVolumeClaimPayload.class);
        devopsPvcService.operatePvcBySaga(persistentVolumeClaimPayload);
        return data;
    }

    /**
     * devops创建PV
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_PERSISTENTVOLUME,
            description = "devops创建PV",
            sagaCode = DEVOPS_CREATE_PERSISTENTVOLUME,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreatePV(String data) {
        PersistentVolumePayload persistentVolumePayload = gson.fromJson(data, PersistentVolumePayload.class);
        devopsPvService.operatePvBySaga(persistentVolumePayload);
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
            appServiceService.setAppErrStatus(gson.toJson(devOpsAppServicePayload), appServiceImportPayload.getProjectId(), appServiceImportPayload.getAppServiceId());
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

        // 考虑应用被删除的情况
        if (applicationDTO == null) {
            LOGGER.info("Set application-service failed: app-service with id {} does not exist. It may be deleted, so skip it...", devOpsAppServicePayload.getAppServiceId());
            return data;
        }

        applicationDTO.setSynchro(true);
        applicationDTO.setFailed(true);
        appServiceService.baseUpdate(applicationDTO);
        sendNotificationService.sendWhenAppServiceFailure(devOpsAppServicePayload.getAppServiceId());
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
        JsonObject JSONObject = gson.fromJson(data, JsonObject.class);
        Long envId = JSONObject.get("envId").getAsLong();
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        devopsEnvironmentService.deleteEnvSaga(envId);
        LOGGER.info("================删除环境成功，envId：{}", envId);
        //删除环境成功，发送webhook
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        if (Objects.isNull(projectDTO)) {
            return;
        }
        sendNotificationService.sendWhenEnvDelete(devopsEnvironmentDTO, projectDTO.getOrganizationId());
    }

    /**
     * Devops删除应用服务
     *
     * @param data
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_APP_DELETE,
            sagaCode = SagaTopicCodeConstants.DEVOPS_APP_DELETE,
            description = "Devops删除应用服务", maxRetryCount = 3,
            seq = 1)
    public String deleteAppService(String data) {
        DevOpsAppServicePayload devOpsAppServicePayload = JSONObject.parseObject(data, DevOpsAppServicePayload.class);
        appServiceService.deleteAppServiceSage(devOpsAppServicePayload.getIamProjectId(), devOpsAppServicePayload.getAppServiceId());
        //删除应用服务成功之后，发送消息
        if (!CollectionUtils.isEmpty(devOpsAppServicePayload.getDevopsUserPermissionVOS())) {
            sendNotificationService.sendWhenAppServiceDelete(devOpsAppServicePayload.getDevopsUserPermissionVOS(), devOpsAppServicePayload.getAppServiceDTO());
        }
        LOGGER.info("================删除应用服务执行成功，serviceId：{}", devOpsAppServicePayload.getAppServiceId());
        return data;
    }

    /**
     * Devops消费批量部署事件
     *
     * @param payload 数据
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_BATCH_DEPLOYMENT,
            sagaCode = SagaTopicCodeConstants.DEVOPS_BATCH_DEPLOYMENT,
            description = "Devops消费批量部署事件", maxRetryCount = 3,
            seq = 1)
    public void batchDeployment(String payload) {
        appServiceInstanceService.batchDeploymentSaga(new JSON().deserialize(payload, BatchDeploymentPayload.class));
    }

    /**
     * 检查节点
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_NODE_CHECK,
            sagaCode = DEVOPS_INSTALL_K8S,
            description = "Devops检查节点", seq = 1, maxRetryCount = 0, timeoutPolicy = ALERT_ONLY)
    public String checkNode(String payload) {
        DevopsClusterInstallPayload devopsClusterInstallPayload = devopsClusterNodeService.checkAndSaveNode(JsonHelper.unmarshalByJackson(payload, DevopsClusterInstallPayload.class));

        // 到达此处表示节点检查、数据保存成功，创建集群安装操作，接下来开始安装集群
        DevopsClusterOperationRecordDTO installOperation = new DevopsClusterOperationRecordDTO()
                .setClusterId(devopsClusterInstallPayload.getClusterId())
                .setType(ClusterOperationTypeEnum.INSTALL_K8S.getType())
                .setStatus(ClusterOperationStatusEnum.OPERATING.value());
        devopsClusterOperationRecordMapper.insert(installOperation);
        devopsClusterInstallPayload.setOperationRecordId(installOperation.getId());

        return JsonHelper.marshalByJackson(devopsClusterInstallPayload);
    }

    /**
     * 通过nohup执行k8s安装命令
     */
    @SagaTask(code = SagaTaskCodeConstants.EXECUTE_INSTALL_K8S_COMMAND,
            sagaCode = DEVOPS_INSTALL_K8S,
            description = "通过nohup执行k8s安装命令,该saga执行成功不代表k8s安装成功", seq = 2, maxRetryCount = 0, timeoutPolicy = ALERT_ONLY)
    public void installK8s(String payload) {
        devopsClusterNodeService.executeInstallK8sInBackground(JsonHelper.unmarshalByJackson(payload, DevopsClusterInstallPayload.class));
    }

    /**
     * 重试安装集群
     */
    @SagaTask(code = DEVOPS_RETRY_INSTALL_K8S,
            sagaCode = DEVOPS_RETRY_INSTALL_K8S,
            description = "通过nohup重试安装集群,该saga执行成功不代表k8s安装成功", seq = 1, maxRetryCount = 0, timeoutPolicy = ALERT_ONLY)
    public void retryInstallK8s(String payload) {
        devopsClusterNodeService.executeInstallK8sInBackground(JsonHelper.unmarshalByJackson(payload, DevopsClusterInstallPayload.class));
    }

    /**
     * 添加节点
     *
     * @param payload
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CLUSTER_ADD_NODE_TASK,
            sagaCode = SagaTopicCodeConstants.DEVOPS_CLUSTER_ADD_NODE,
            description = "Devops添加节点", seq = 1)
    public void addNode(String payload) {
        DevopsAddNodePayload devopsAddNodePayload = JsonHelper.unmarshalByJackson(payload, DevopsAddNodePayload.class);
        devopsClusterNodeOperatorService.addNode(devopsAddNodePayload.getProjectId(), devopsAddNodePayload.getClusterId(), devopsAddNodePayload.getOperatingId(), devopsAddNodePayload.getNodeVO());
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
            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(null, appServiceDeployVO, true);
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
            Long userId = GitUserNameUtil.getUserId();
            sendNotificationService.sendCdPipelineNotice(pipelineRecordId,
                    MessageCodeConstants.PIPELINE_FAILED,
                    userId, GitUserNameUtil.getEmail(), new HashMap<>());
            LOGGER.info("send pipeline failed message to the user. The user id is {}", userId);
        }
    }

}
