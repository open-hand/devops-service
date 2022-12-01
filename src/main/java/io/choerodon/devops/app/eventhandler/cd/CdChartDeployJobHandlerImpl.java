package io.choerodon.devops.app.eventhandler.cd;

import static io.choerodon.devops.infra.constant.PipelineConstants.GITLAB_ADMIN_ID;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.AppServiceVersionRespVO;
import io.choerodon.devops.api.vo.cd.PipelineChartDeployCfgVO;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.cd.CdJobTypeEnum;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.enums.cd.PipelineTriggerTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PipelineAppDeployUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/24 16:50
 */
@Component
public class CdChartDeployJobHandlerImpl extends AbstractCdJobHandler {

    @Autowired
    private PipelineChartDeployCfgService pipelineChartDeployCfgService;
    @Autowired
    protected DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    private PipelineAppDeployUtil pipelineAppDeployUtil;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private AppServiceService appServiceService;

    @Override
    protected void checkConfigInfo(Long projectId, PipelineJobVO pipelineJobVO) {
        // todo
    }

    @Override
    public void deleteConfigByPipelineId(Long pipelineId) {
        pipelineChartDeployCfgService.deleteConfigByPipelineId(pipelineId);
    }


    @Override
    protected Long saveConfig(Long pipelineId, PipelineJobVO devopsCiJobVO) {
        PipelineChartDeployCfgVO chartDeployCfg = devopsCiJobVO.getChartDeployCfg();
        PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO = ConvertUtils.convertObject(chartDeployCfg, PipelineChartDeployCfgDTO.class);
        pipelineChartDeployCfgDTO.setId(null);
        pipelineChartDeployCfgDTO.setPipelineId(pipelineId);

        pipelineChartDeployCfgService.baseCreate(pipelineChartDeployCfgDTO);
        return pipelineChartDeployCfgDTO.getId();
    }

    @Override
    public void fillJobConfigInfo(PipelineJobVO pipelineJobVO) {
        pipelineJobVO.setChartDeployCfg(pipelineChartDeployCfgService.queryVoByConfigId(pipelineJobVO.getConfigId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void execCommand(Long jobRecordId, StringBuilder log) {
        log.append("Start pipeline auto deploy task.").append(System.lineSeparator());
        PipelineJobRecordDTO pipelineJobRecordDTO = pipelineJobRecordService.baseQueryById(jobRecordId);
        Long stageRecordId = pipelineJobRecordDTO.getStageRecordId();
        PipelineStageRecordDTO pipelineStageRecordDTO = pipelineStageRecordService.baseQueryById(stageRecordId);
        PipelineRecordDTO pipelineRecordDTO = pipelineRecordService.baseQueryById(pipelineStageRecordDTO.getPipelineRecordId());
        PipelineJobDTO pipelineJobDTO = pipelineJobService.baseQueryById(pipelineJobRecordDTO.getJobId());
        PipelineDTO pipelineDTO = pipelineService.baseQueryById(pipelineJobDTO.getPipelineId());
        PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO = pipelineChartDeployCfgService.queryByConfigId(pipelineJobDTO.getConfigId());

        if (pipelineChartDeployCfgDTO == null) {
            throw new CommonException("devops.chart.deploy.config.not.found");
        }
        Long projectId = pipelineDTO.getProjectId();
        Long appServiceId = pipelineChartDeployCfgDTO.getAppServiceId();
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        Long userId = userDetails.getUserId();
        Long envId = pipelineChartDeployCfgDTO.getEnvId();
        Boolean skipCheckPermission = pipelineChartDeployCfgDTO.getSkipCheckPermission();
        String appCode = pipelineChartDeployCfgDTO.getAppCode();
        String appName = pipelineChartDeployCfgDTO.getAppName();
        String version = pipelineChartDeployCfgDTO.getVersion();
        Long valueId = pipelineChartDeployCfgDTO.getValueId();

        // 1. 校验环境是否开启一键关闭自动部署
        if (!pipelineAppDeployUtil.checkAutoMaticDeploy(log, envId)) return;
        // 2. 校验用户权限
        if (!pipelineAppDeployUtil.checkUserPermission(log, userId, envId, skipCheckPermission)) return;
        // 获取部署版本信息

        AppServiceVersionDTO appServiceVersionDTO;
        if (PipelineTriggerTypeEnum.APP_VERSION.value().equals(pipelineRecordDTO.getTriggerType())) {
            //
            if (!appServiceId.equals(pipelineRecordDTO.getAppServiceId())) {
                log.append("当前任务不满足触发条件'应用服务匹配'，跳过此任务");
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SUCCESS.value());
                pipelineJobRecordService.baseUpdate(pipelineJobRecordDTO);
                // 更新阶段状态
                pipelineStageRecordService.updateStatus(stageRecordId);
                return;
            }
            appServiceVersionDTO = appServiceVersionService.baseQuery(pipelineRecordDTO.getAppServiceVersionId());
            String versionRegex = ".*" + version + ".*";
            Pattern pattern = Pattern.compile(versionRegex);
            if (!pattern.matcher(appServiceVersionDTO.getVersion()).matches()) {
                log.append("当前任务不满足触发条件'应用服务版本匹配'，跳过此任务");
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SUCCESS.value());
                pipelineJobRecordService.baseUpdate(pipelineJobRecordDTO);
                // 更新阶段状态
                pipelineStageRecordService.updateStatus(stageRecordId);
                return;
            }
        } else {
            appServiceVersionDTO = appServiceVersionService.queryLatestByAppServiceIdVersionType(appServiceId, version);
            if (appServiceVersionDTO == null) {
                log.append("当前任务不满足触发条件'应用服务版本不存在'，跳过此任务");
                return;
            }
        }
        log.append("#####部署应用#####").append(System.lineSeparator());
        AppServiceDeployVO appServiceDeployVO = null;
        Long commandId = null;
        Long appId = null;
        if (DeployTypeEnum.CREATE.value().equals(pipelineChartDeployCfgDTO.getDeployType())) {
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
                    true);
            commandId = appServiceInstanceVO.getCommandId();
            appId = appServiceInstanceVO.getAppId();
            pipelineChartDeployCfgDTO.setAppId(appId);
            pipelineChartDeployCfgDTO.setDeployType(DeployTypeEnum.UPDATE.value());
            pipelineChartDeployCfgService.baseUpdate(pipelineChartDeployCfgDTO);
        } else {
            // 3. 如果是更新应用，先判断应用是否存在。不存在则跳过。
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(envId, appCode);
            if (devopsDeployAppCenterEnvDTO == null) {
                log.append("应用: ").append(appCode).append(" 不存在, 请确认是否删除? 跳过此部署任务.").append(System.lineSeparator());
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SUCCESS.value());
                pipelineJobRecordService.baseUpdate(pipelineJobRecordDTO);
                // 更新阶段状态
                pipelineStageRecordService.updateStatus(stageRecordId);
                return;
            }
            // 存在则更新
            AppServiceInstanceDTO preInstance = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
            DevopsEnvCommandDTO preCommand = devopsEnvCommandService.baseQuery(preInstance.getCommandId());
            AppServiceVersionRespVO deploydAppServiceVersion = appServiceVersionService.queryById(preCommand.getObjectVersionId());
            log.append("应用存在, 开始更新应用.").append(System.lineSeparator());
            // 如果当前部署版本和流水线生成版本相同则重启
            if (preCommand.getObjectVersionId().equals(appServiceVersionDTO.getId())) {
                log.append("此次部署版本和应用当前版本一致，触发重新部署.").append(System.lineSeparator());

                DevopsEnvCommandDTO devopsEnvCommandDTO = appServiceInstanceService.restartInstance(projectId,
                        preInstance.getId(),
                        true,
                        true);
                commandId = devopsEnvCommandDTO.getId();
                log.append("重新部署成功.").append(System.lineSeparator());
            } else {
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
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
                    pipelineJobRecordDTO.setCommandId(commandId);
                    pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SUCCESS.value());
                    pipelineJobRecordService.baseUpdate(pipelineJobRecordDTO);
                    pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SUCCESS.value());
                    pipelineJobRecordService.baseUpdate(pipelineJobRecordDTO);
                    // 更新阶段状态
                    pipelineStageRecordService.updateStatus(stageRecordId);
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
        pipelineJobRecordDTO.setCommandId(commandId);
        pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SUCCESS.value());
        pipelineJobRecordService.baseUpdate(pipelineJobRecordDTO);
        // 更新阶段状态
        pipelineStageRecordService.updateStatus(stageRecordId);

    }

    @Override
    public CdJobTypeEnum getType() {
        return CdJobTypeEnum.CHART_DEPLOY;
    }

}
