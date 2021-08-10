package io.choerodon.devops.app.eventhandler;

import static io.choerodon.asgard.saga.SagaDefinition.TimeoutPolicy.ALERT_ONLY;
import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.test.ApiTestCompleteEventVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.app.service.impl.UpdateEnvUserPermissionServiceImpl;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployResultEnum;
import io.choerodon.devops.infra.enums.test.ApiTestTriggerType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.mapper.DevopsClusterOperationRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.*;


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
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private MarketUseRecordService marketUseRecordService;
    @Autowired
    private DevopsHzeroDeployDetailsService devopsHzeroDeployDetailsService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private WorkFlowServiceOperator workFlowServiceOperator;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;

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
            devOpsAppImportPayload.setErrorMessage(getStackTrace(e));
            appServiceService.setAppErrStatus(JsonHelper.marshalByJackson(devOpsAppImportPayload), devOpsAppImportPayload.getIamProjectId(), devOpsAppImportPayload.getAppServiceId());
            throw e;
        }
        return JsonHelper.marshalByJackson(devOpsAppImportPayload);
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
            maxRetryCount = 0,
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
            maxRetryCount = 0,
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
            maxRetryCount = 0,
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
            maxRetryCount = 0,
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
                devopsCdJobRecordDTO.setLog(devopsCdJobRecordDTO.getLog() + "Deploy app success." + System.lineSeparator());
                devopsCdJobRecordDTO.setStatus(PipelineStatus.SUCCESS.toValue());
                devopsCdJobRecordService.update(devopsCdJobRecordDTO);
            }
            LOGGER.info("create pipeline auto deploy instance success");
        } catch (Exception e) {
            LOGGER.error("error create pipeline auto deploy instance", e);
            String log = devopsCdJobRecordDTO.getLog();
            if (log != null) {
                log = LogUtil.cutOutString(log + System.lineSeparator() + LogUtil.readContentOfThrowable(e), 2500);
            } else {
                log = LogUtil.cutOutString(LogUtil.readContentOfThrowable(e), 2500);
            }
            devopsCdJobRecordService.updateJobStatusFailed(jobRecordId, log);
            devopsCdStageRecordService.updateStageStatusFailed(devopsCdStageRecordDTO.getId());
            devopsCdPipelineRecordService.updatePipelineStatusFailed(pipelineRecordId);

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
     * devops创建市场实例
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_CREATE_MARKET_INSTANCE,
            description = "devops创建市场实例",
            sagaCode = DEVOPS_CREATE_MARKET_INSTANCE,
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateMarketInstance(String data) {
        appServiceInstanceService.createMarketInstanceBySaga(JsonHelper.unmarshalByJackson(data, MarketInstanceSagaPayload.class));
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
            //通过共享规则导入应用服务，如果出现错误记录到数据库中，
            DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
            devOpsAppServicePayload.setAppServiceId(appServiceImportPayload.getAppServiceId());
            devOpsAppServicePayload.setErrorMessage(getStackTrace(e));
            appServiceService.setAppErrStatus(gson.toJson(devOpsAppServicePayload), appServiceImportPayload.getProjectId(), appServiceImportPayload.getAppServiceId());
            throw e;
        }
        return data;
    }

    public static String getStackTrace(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    LOGGER.error("error.sw.close", e1);
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }


    /**
     * devops导入市场应用服务
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_IMPORT_MARKET_APPLICATION_SERVICE,
            description = "devops入市场应用服务",
            sagaCode = SagaTopicCodeConstants.DEVOPS_IMPORT_MARKET_APPLICATION_SERVICE,
            maxRetryCount = 3,
            seq = 1)
    public String importMarketAppServiceGitlab(String data) {
        AppServiceImportPayload appServiceImportPayload = gson.fromJson(data, AppServiceImportPayload.class);
        try {
            appServiceService.importMarketAppServiceGitlab(appServiceImportPayload);
            //插入市场使用记录
            DeploySourceVO deploySourceVO = new DeploySourceVO();
            deploySourceVO.setDeployObjectId(appServiceImportPayload.getDeployObjectId());
            marketUseRecordService.saveMarketUseRecord(UseRecordType.IMPORT.getValue(), appServiceImportPayload.getProjectId(), deploySourceVO, null);
        } catch (Exception e) {
            DevOpsAppServicePayload devOpsAppServicePayload = new DevOpsAppServicePayload();
            devOpsAppServicePayload.setAppServiceId(appServiceImportPayload.getAppServiceId());
            devOpsAppServicePayload.setErrorMessage(getStackTrace(e));
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
        //存入失败的信息
        LOGGER.info("》》》》errorMessage:{}》》》》", devOpsAppServicePayload.getErrorMessage());
        applicationDTO.setErrorMessage(devOpsAppServicePayload.getErrorMessage());
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
        JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
        Long envId = jsonObject.get("envId").getAsLong();
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(envId);
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
    @SagaTask(code = SagaTaskCodeConstants.HANDLE_API_TEST_TASK_COMPLETE_EVENT,
            description = "创建流水线自动部署实例",
            sagaCode = SagaTopicCodeConstants.API_TEST_TASK_COMPLETE_EVENT,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            maxRetryCount = 0,
            seq = 1)
    public void handleApiTestTaskCompleteEvent(String data) {
        ApiTestCompleteEventVO apiTestCompleteEventVO = JsonHelper.unmarshalByJackson(data, ApiTestCompleteEventVO.class);
        // 只处理流水线触发的api测试任务
        if (ApiTestTriggerType.PIPELINE.getValue().equals(apiTestCompleteEventVO.getTriggerType())) {
            devopsCdPipelineService.handleApiTestTaskCompleteEvent(apiTestCompleteEventVO);
        }
    }

    /**
     * 接收实例下pod ready的消息，用于通知hzero部署是否需要进行下一个流程
     */
    @SagaTask(code = SagaTaskCodeConstants.DEVOPS_POD_READY_HANDLER_FOR_HZERO_DEPLOY,
            description = "处理pod ready消息",
            sagaCode = DEVOPS_POD_READY,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            maxRetryCount = 0,
            seq = 1)
    public void handlePodReadyEvent(String data) {
        PodReadyEventVO podReadyEventVO = JsonHelper.unmarshalByJackson(data, PodReadyEventVO.class);
        DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsService.baseQueryDeployingByEnvIdAndInstanceCode(podReadyEventVO.getEnvId(), podReadyEventVO.getInstanceCode());
        if (devopsHzeroDeployDetailsDTO != null) {
            // pod的操作记录不是最新的则丢弃
            if (podReadyEventVO.getCommandId() < devopsHzeroDeployDetailsDTO.getCommandId()) {
                LOGGER.info(">>>>>>>>>>>>>>>pod commandId before details CommandId, skip<<<<<<<<<<<<<<<<<");
                return;
            }

            DevopsDeployRecordDTO devopsDeployRecordDTO = devopsDeployRecordService.baseQueryById(devopsHzeroDeployDetailsDTO.getDeployRecordId());
            // 查询实例
            AppServiceInstanceDTO instanceE = appServiceInstanceService.baseQueryByCodeAndEnv(podReadyEventVO.getInstanceCode(), podReadyEventVO.getEnvId());
            // 查询部署版本

            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(devopsDeployRecordDTO.getProjectId(), devopsHzeroDeployDetailsDTO.getMktDeployObjectId());
            // 查询当前实例运行时pod metadata
            List<PodResourceDetailsDTO> podResourceDetailsDTOS = devopsEnvPodService.queryResourceDetailsByInstanceId(instanceE.getId());

            if (CollectionUtils.isEmpty(podResourceDetailsDTOS)) {
                LOGGER.info(">>>>>>>>>>>>>>>podResourceDetailsDTOS is empty, skip<<<<<<<<<<<<<<<<<");
                return;
            }
//            if (!podResourceDetailsDTOS.stream().allMatch(v -> Boolean.TRUE.equals(v.getReady()))) {
//                LOGGER.info(">>>>>>>>>>>>>>>pod not all ready, skip<<<<<<<<<<<<<<<<<");
//                return;
//            }
            List<String> images = new ArrayList<>();
            for (PodResourceDetailsDTO podResourceDetailsDTO : podResourceDetailsDTOS) {
                V1Pod podInfo = K8sUtil.deserialize(podResourceDetailsDTO.getMessage(), V1Pod.class);
                images.addAll(podInfo.getSpec().getContainers().stream().map(V1Container::getImage).collect(Collectors.toList()));
            }
            if (!images.stream().allMatch(v -> marketServiceDeployObjectVO.getMarketDockerImageUrl().equals(v))) {
                LOGGER.info(">>>>>>>>>>>>>>>pod not all ready, skip<<<<<<<<<<<<<<<<<");
                return;
            }

            devopsHzeroDeployDetailsService.updateStatusById(devopsHzeroDeployDetailsDTO.getId(), HzeroDeployDetailsStatusEnum.SUCCESS);
            if (!DeployResultEnum.CANCELED.value().equals(devopsDeployRecordDTO.getDeployResult())) {
                // 1. 后续还有任务则通知下一任务执行
                // 2. 后续没有任务了，则更新部署记录状态为成功
                if (Boolean.TRUE.equals(devopsHzeroDeployDetailsService.completed(devopsHzeroDeployDetailsDTO.getDeployRecordId()))) {
                    devopsDeployRecordService.updateResultById(devopsHzeroDeployDetailsDTO.getDeployRecordId(), DeployResultEnum.SUCCESS);
                } else {
                    workFlowServiceOperator.approveUserTask(devopsDeployRecordDTO.getProjectId(),
                            devopsDeployRecordDTO.getBusinessKey(),
                            MiscConstants.WORKFLOW_ADMIN_NAME,
                            MiscConstants.WORKFLOW_ADMIN_ID,
                            MiscConstants.WORKFLOW_ADMIN_ORG_ID);
                }


            }
        }

    }

}
