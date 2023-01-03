package io.choerodon.devops.app.eventhandler.pipeline.exec;

import static io.choerodon.devops.infra.constant.PipelineConstants.GITLAB_ADMIN_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
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
import io.choerodon.devops.infra.enums.DeployType;
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
        AppServiceVersionDTO appServiceVersionDTO = pipelineAppDeployUtil.queryAppVersion(log,
                appServiceId,
                devopsCiPipelineRecordDTO.getCommitSha(),
                devopsCiPipelineRecordDTO.getGitlabTriggerRef());
        CiChartDeployConfigDTO ciChartDeployConfigDTO = ciChartDeployConfigService.queryConfigById(appDeployConfigVO.getId());
        Long valueId = ciChartDeployConfigDTO.getValueId();

        log.append("#####部署应用#####").append(System.lineSeparator());
        AppServiceDeployVO appServiceDeployVO = null;
        Long commandId = null;
        Long appId = null;
        if (DeployTypeEnum.CREATE.value().equals(appDeployConfigVO.getDeployType())) {
            // 不存在应用则新建
            log.append("应用不存在，新建应用.").append(System.lineSeparator());
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
                    DeployType.AUTO);
            commandId = appServiceInstanceVO.getCommandId();
            appId = appServiceInstanceVO.getAppId();
            ciChartDeployConfigDTO.setAppId(appId);
            ciChartDeployConfigDTO.setDeployType(DeployTypeEnum.UPDATE.value());
            ciChartDeployConfigService.baseUpdate(ciChartDeployConfigDTO);
        } else {
            // 3. 如果是更新应用，先判断应用是否存在。不存在则跳过。
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(envId, appCode);
            if (devopsDeployAppCenterEnvDTO == null) {
                log.append("应用: ").append(appCode).append(" 不存在, 请确认是否删除? 跳过此部署任务.").append(System.lineSeparator());
                return;
            }
            // 存在则更新
            AppServiceInstanceDTO preInstance = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
            DevopsEnvCommandDTO preCommand = devopsEnvCommandService.baseQuery(preInstance.getCommandId());
            AppServiceVersionRespVO deploydAppServiceVersion = appServiceVersionService.queryById(preCommand.getObjectVersionId());
            log.append("应用存在, 开始更新应用.").append(System.lineSeparator());

            if (Boolean.TRUE.equals(appServiceInstanceService.isInstanceDeploying(preInstance.getId()))) {
                log.append("应用当前处于部署中状态，请等待此次部署完成后重试.").append(System.lineSeparator());
                throw new CommonException("devops.app.instance.deploying");
            }

            // 如果当前部署版本和流水线生成版本相同则重启
            if (preCommand.getObjectVersionId().equals(appServiceVersionDTO.getId())) {
                log.append("此次部署版本和应用当前版本一致，触发重新部署.").append(System.lineSeparator());

                DevopsEnvCommandDTO devopsEnvCommandDTO = appServiceInstanceService.restartInstance(projectId,
                        preInstance.getId(),
                        DeployType.AUTO,
                        true);
                commandId = devopsEnvCommandDTO.getId();
                log.append("重新部署成功.").append(System.lineSeparator());
            } else {
                // 外置仓库不校验
                if (appServiceDTO.getExternalConfigId() == null) {
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
                        log.append("此次部署的版本落后于应用当前版本，跳过此次部署").append(System.lineSeparator());
                        return;
                    }
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
                AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(projectId, appServiceDeployVO, DeployType.AUTO);
                commandId = appServiceInstanceVO.getCommandId();
            }
        }
        if (commandId != null) {
            devopsCiJobRecordDTO.setCommandId(commandId);
            devopsCiJobRecordService.baseUpdate(devopsCiJobRecordDTO);
        } else {
            log.append("[warn] 部署命令未找到.").append(System.lineSeparator());
        }
    }

    @Override
    protected AppDeployConfigVO queryConfigById(Long configId) {
        CiChartDeployConfigDTO ciChartDeployConfigDTO = ciChartDeployConfigService.queryConfigById(configId);
        return ConvertUtils.convertObject(ciChartDeployConfigDTO, CiChartDeployConfigVO.class);
    }

}
