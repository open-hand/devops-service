package io.choerodon.devops.app.eventhandler.pipeline.exec;

import static io.choerodon.devops.infra.constant.PipelineConstants.GITLAB_ADMIN_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.AppServiceVersionRespVO;
import io.choerodon.devops.api.vo.pipeline.AppDeployConfigVO;
import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 16:03
 */
@Service
public class ChartDeployCommandHandler extends AbstractAppDeployCommandHandler {

    @Autowired
    private CiChartDeployConfigService ciChartDeployConfigService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Override
    public CiCommandTypeEnum getType() {
        return CiCommandTypeEnum.CHART_DEPLOY;
    }

//    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    protected void execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log, Object content) {
//        log.append("Start pipeline auto deploy task.").append(System.lineSeparator());
//        CiChartDeployConfigDTO ciChartDeployConfigDTO = ciChartDeployConfigService.queryConfigById(configId);
//        if (ciChartDeployConfigDTO == null) {
//            throw new CommonException("devops.chart.deploy.config.not.found");
//        }
//        Long projectId = appServiceDTO.getProjectId();
//        Long appServiceId = appServiceDTO.getId();
//        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
//        Long userId = userDetails.getUserId();
//        Long envId = ciChartDeployConfigDTO.getEnvId();
//        Boolean skipCheckPermission = ciChartDeployConfigDTO.getSkipCheckPermission();
//        Long valueId = ciChartDeployConfigDTO.getValueId();
//        String appCode = ciChartDeployConfigDTO.getAppCode();
//        String appName = ciChartDeployConfigDTO.getAppName();
//
//        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryByAppServiceIdAndGitlabPipelineId(appServiceId, gitlabPipelineId);
//
//
//        // 1. 校验环境是否开启一键关闭自动部署
//        log.append("## 1.Check Environment automatic deploy enable.").append(System.lineSeparator());
//        if (Boolean.FALSE.equals(devopsEnvironmentService.queryByIdOrThrowE(envId).getAutoDeploy())) {
//            log.append("Environment automatic deploy has been turned off!").append(System.lineSeparator());
//            return;
//        }
//        // 2. 校验用户权限
//        log.append("## 2.Check user env permission.").append(System.lineSeparator());
//        if (Boolean.FALSE.equals(skipCheckPermission)) {
//            log.append("Skip check user permission Flag is false, check user permission.").append(System.lineSeparator());
//            if (Boolean.FALSE.equals(devopsEnvUserPermissionService.checkUserEnvPermission(envId,
//                    userId))) {
//                log.append("User have no env Permission, skipped.").append(System.lineSeparator());
//                return;
//            } else {
//                log.append("Check user permission Passed.").append(System.lineSeparator());
//            }
//        } else {
//            log.append("Skip check user permission Flag is true, choose deploy account.").append(System.lineSeparator());
//            if (Boolean.FALSE.equals(devopsEnvUserPermissionService.checkUserEnvPermission(envId,
//                    userId))) {
//                log.append("User have no env Permission, use admin account to deploy.").append(System.lineSeparator());
//                CustomContextUtil.setUserContext(IamAdminIdHolder.getAdminId());
//            } else {
//                log.append("User have env Permission, use self account to deploy.").append(System.lineSeparator());
//            }
//        }
//        // 获取部署版本信息
//        log.append("## 3.Query Deploy version.").append(System.lineSeparator());
//        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.queryByCommitShaAndRef(appServiceId,
//                devopsCiPipelineRecordDTO.getCommitSha(),
//                devopsCiPipelineRecordDTO.getGitlabTriggerRef());
//        if (appServiceVersionDTO == null) {
//            log.append("Not Found App Version in this branch and commit, skipped.").append(System.lineSeparator());
//            return;
//        } else {
//            log.append("Deploy Version is ").append(appServiceVersionDTO.getVersion()).append(System.lineSeparator());
//        }
//
//        log.append("## 4.Deploy app instance.").append(System.lineSeparator());
//        AppServiceDeployVO appServiceDeployVO = null;
//        Long commandId = null;
//        Long appId = null;
//        if (DeployTypeEnum.CREATE.value().equals(ciChartDeployConfigDTO.getDeployType())) {
//            // 不存在应用则新建
//            log.append("App not exist, create it now.").append(System.lineSeparator());
//            appServiceDeployVO = new AppServiceDeployVO(appServiceVersionDTO.getAppServiceId(),
//                    appServiceVersionDTO.getId(),
//                    envId,
//                    devopsDeployValueService.baseQueryById(valueId).getValue(),
//                    valueId,
//                    appCode,
//                    null,
//                    CommandType.CREATE.getType(),
//                    appName,
//                    appCode);
//            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(projectId,
//                    appServiceDeployVO,
//                    true);
//            commandId = appServiceInstanceVO.getCommandId();
//            appId = appServiceInstanceVO.getAppId();
//
//            ciChartDeployConfigDTO.setDeployType(DeployTypeEnum.UPDATE.value());
//            ciChartDeployConfigService.baseUpdate(ciChartDeployConfigDTO);
//        } else {
//            // 3. 如果是更新应用，先判断应用是否存在。不存在则跳过。
//            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(envId, appCode);
//            if (devopsDeployAppCenterEnvDTO == null) {
//                log.append("App: ").append(appCode).append(" not found, is deleted? Skip this task.").append(System.lineSeparator());
//                return;
//            }
//            // 存在则更新
//            AppServiceInstanceDTO preInstance = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
//            DevopsEnvCommandDTO preCommand = devopsEnvCommandService.baseQuery(preInstance.getCommandId());
//            AppServiceVersionRespVO deploydAppServiceVersion = appServiceVersionService.queryById(preCommand.getObjectVersionId());
//            log.append("App exist, upgrade it now.").append(System.lineSeparator());
//            // 如果当前部署版本和流水线生成版本相同则重启
//            if (preCommand.getObjectVersionId().equals(appServiceVersionDTO.getId())) {
//                log.append("Deploy version is same to instance version, restart it.").append(System.lineSeparator());
//
//                DevopsEnvCommandDTO devopsEnvCommandDTO = appServiceInstanceService.restartInstance(projectId,
//                        preInstance.getId(),
//                        true,
//                        true);
//                log.append("Restart success.").append(System.lineSeparator());
//                return;
//            }
//            // 要部署版本的commit
//            CommitDTO currentCommit = gitlabServiceClientOperator.queryCommit(appServiceDTO.getGitlabProjectId(), appServiceVersionDTO.getCommit(), GITLAB_ADMIN_ID);
//            // 已经部署版本的commit
//            CommitDTO deploydCommit = gitlabServiceClientOperator.queryCommit(appServiceDTO.getGitlabProjectId(), deploydAppServiceVersion.getCommit(), GITLAB_ADMIN_ID);
//            if (deploydCommit != null
//                    && currentCommit != null
//                    && currentCommit.getCommittedDate().before(deploydCommit.getCommittedDate())) {
//                // 计算commitDate
//                // 如果要部署的版本的commitDate落后于环境中已经部署的版本，则跳过
//                // 如果现在部署的版本落后于已经部署的版本则跳过
//                log.append("Deploy version is behind to instance current version, skipped.").append(System.lineSeparator());
//                return;
//            }
//            appServiceDeployVO = new AppServiceDeployVO(appServiceVersionDTO.getAppServiceId(),
//                    appServiceVersionDTO.getId(),
//                    envId,
//                    devopsDeployValueService.baseQueryById(valueId).getValue(),
//                    valueId,
//                    appCode,
//                    devopsDeployAppCenterEnvDTO.getObjectId(),
//                    CommandType.UPDATE.getType(),
//                    null,
//                    null);
//            appServiceDeployVO.setInstanceId(devopsDeployAppCenterEnvDTO.getObjectId());
//            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(projectId, appServiceDeployVO, true);
//            commandId = appServiceInstanceVO.getCommandId();
//        }
//    }

    @Override
    protected void deployApp(AppServiceDTO appServiceDTO,
                             StringBuilder log,
                             AppDeployConfigVO appDeployConfigVO,
                             Long projectId,
                             Long appServiceId,
                             Long envId,
                             String appCode,
                             String appName,
                             DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO,
                             DevopsCiJobRecordDTO devopsCiJobRecordDTO) {
        log.append("## 3.Query Deploy version.").append(System.lineSeparator());
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.queryByCommitShaAndRef(appServiceId,
                devopsCiPipelineRecordDTO.getCommitSha(),
                devopsCiPipelineRecordDTO.getGitlabTriggerRef());
        if (appServiceVersionDTO == null) {
            log.append("Not Found App Version in this branch and commit, skipped.").append(System.lineSeparator());
            return;
        } else {
            log.append("Deploy Version is ").append(appServiceVersionDTO.getVersion()).append(System.lineSeparator());
        }
        CiChartDeployConfigDTO ciChartDeployConfigDTO = ciChartDeployConfigService.queryConfigById(appDeployConfigVO.getId());
        Long valueId = ciChartDeployConfigDTO.getValueId();

        log.append("## 4.Deploy app instance.").append(System.lineSeparator());
        AppServiceDeployVO appServiceDeployVO = null;
        Long commandId = null;
        Long appId = null;
        if (DeployTypeEnum.CREATE.value().equals(appDeployConfigVO.getDeployType())) {
            // 不存在应用则新建
            log.append("App not exist, create it now.").append(System.lineSeparator());
            appServiceDeployVO = new AppServiceDeployVO(appServiceVersionDTO.getAppServiceId(),
                    appServiceVersionDTO.getId(),
                    envId,
                    devopsDeployValueService.baseQueryById(valueId).getValue(),
                    valueId,
                    appCode,
                    null,
                    CommandType.CREATE.getType(),
                    appName,
                    appCode);
            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(projectId,
                    appServiceDeployVO,
                    true);
            commandId = appServiceInstanceVO.getCommandId();
            appId = appServiceInstanceVO.getAppId();
            ciChartDeployConfigDTO.setAppId(appId);
            ciChartDeployConfigDTO.setDeployType(DeployTypeEnum.UPDATE.value());
            ciChartDeployConfigService.baseUpdate(ciChartDeployConfigDTO);
        } else {
            // 3. 如果是更新应用，先判断应用是否存在。不存在则跳过。
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(envId, appCode);
            if (devopsDeployAppCenterEnvDTO == null) {
                log.append("App: ").append(appCode).append(" not found, is deleted? Skip this task.").append(System.lineSeparator());
                return;
            }
            // 存在则更新
            AppServiceInstanceDTO preInstance = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
            DevopsEnvCommandDTO preCommand = devopsEnvCommandService.baseQuery(preInstance.getCommandId());
            AppServiceVersionRespVO deploydAppServiceVersion = appServiceVersionService.queryById(preCommand.getObjectVersionId());
            log.append("App exist, upgrade it now.").append(System.lineSeparator());
            // 如果当前部署版本和流水线生成版本相同则重启
            if (preCommand.getObjectVersionId().equals(appServiceVersionDTO.getId())) {
                log.append("Deploy version is same to instance version, restart it.").append(System.lineSeparator());

                DevopsEnvCommandDTO devopsEnvCommandDTO = appServiceInstanceService.restartInstance(projectId,
                        preInstance.getId(),
                        true,
                        true);
                commandId = devopsEnvCommandDTO.getId();
                log.append("Restart success.").append(System.lineSeparator());
            } else {
                // 要部署版本的commit
                CommitDTO currentCommit = gitlabServiceClientOperator.queryCommit(appServiceDTO.getGitlabProjectId(), appServiceVersionDTO.getCommit(), GITLAB_ADMIN_ID);
                // 已经部署版本的commit
                CommitDTO deploydCommit = gitlabServiceClientOperator.queryCommit(appServiceDTO.getGitlabProjectId(), deploydAppServiceVersion.getCommit(), GITLAB_ADMIN_ID);
                if (deploydCommit != null
                        && currentCommit != null
                        && currentCommit.getCommittedDate().before(deploydCommit.getCommittedDate())) {
                    // 计算commitDate
                    // 如果要部署的版本的commitDate落后于环境中已经部署的版本，则跳过
                    // 如果现在部署的版本落后于已经部署的版本则跳过
                    log.append("Deploy version is behind to instance current version, skipped.").append(System.lineSeparator());
                    return;
                }
                appServiceDeployVO = new AppServiceDeployVO(appServiceVersionDTO.getAppServiceId(),
                        appServiceVersionDTO.getId(),
                        envId,
                        devopsDeployValueService.baseQueryById(valueId).getValue(),
                        valueId,
                        appCode,
                        devopsDeployAppCenterEnvDTO.getObjectId(),
                        CommandType.UPDATE.getType(),
                        null,
                        null);
                appServiceDeployVO.setInstanceId(devopsDeployAppCenterEnvDTO.getObjectId());
                AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(projectId, appServiceDeployVO, true);
                commandId = appServiceInstanceVO.getCommandId();
            }
        }
        devopsCiJobRecordDTO.setCommandId(commandId);
        devopsCiJobRecordService.baseUpdate(devopsCiJobRecordDTO);
    }

    @Override
    protected AppDeployConfigVO queryConfigById(Long configId) {
        CiChartDeployConfigDTO ciChartDeployConfigDTO = ciChartDeployConfigService.queryConfigById(configId);
        return ConvertUtils.convertObject(ciChartDeployConfigDTO, CiChartDeployConfigVO.class);
    }

}
