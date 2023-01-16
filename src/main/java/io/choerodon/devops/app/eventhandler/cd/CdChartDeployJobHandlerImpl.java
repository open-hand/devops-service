package io.choerodon.devops.app.eventhandler.cd;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppDeploy.DEVOPS_APP_DEPLOY_CONFIG_EMPTY;
import static io.choerodon.devops.infra.constant.ExceptionConstants.AppDeploy.DEVOPS_APP_SERVICE_ID_EMPTY;
import static io.choerodon.devops.infra.constant.ExceptionConstants.DeployValueCode.DEVOPS_DEPLOY_VALUE_ID_NULL;
import static io.choerodon.devops.infra.constant.PipelineConstants.GITLAB_ADMIN_ID;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.cd.PipelineChartDeployCfgVO;
import io.choerodon.devops.api.vo.cd.PipelineJobRecordVO;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.api.vo.pipeline.DeployInfo;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.cd.CdJobTypeEnum;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.enums.cd.PipelineTriggerTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
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
    private DevopsEnvCommandValueService devopsEnvCommandValueService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    protected DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    @Override
    protected void checkConfigInfo(Long projectId, PipelineJobVO pipelineJobVO) {
        PipelineChartDeployCfgVO appDeployConfigVO = pipelineJobVO.getChartDeployCfg();
        if (appDeployConfigVO == null) {
            throw new CommonException(DEVOPS_APP_DEPLOY_CONFIG_EMPTY, pipelineJobVO.getName());
        }
        if (appDeployConfigVO.getEnvId() == null) {
            throw new CommonException(DEVOPS_APP_DEPLOY_CONFIG_EMPTY, pipelineJobVO.getName());
        }
        if (appDeployConfigVO.getAppServiceId() == null) {
            throw new CommonException(DEVOPS_APP_SERVICE_ID_EMPTY, pipelineJobVO.getName());
        }
        if (StringUtils.isEmpty(appDeployConfigVO.getDeployType())) {
            throw new CommonException(ExceptionConstants.AppCode.DEVOPS_APP_DEPLOY_TYPE_IS_EMPTY);
        }
        if (DeployTypeEnum.CREATE.value().equals(appDeployConfigVO.getDeployType())) {
            if (StringUtils.isEmpty(appDeployConfigVO.getAppName())) {
                throw new CommonException(ExceptionConstants.AppCode.DEVOPS_APP_NAME_IS_EMPTY);
            }
            if (StringUtils.isEmpty(appDeployConfigVO.getAppCode())) {
                throw new CommonException(ExceptionConstants.AppCode.DEVOPS_APP_CODE_IS_EMPTY);
            }
            appDeployConfigVO.setAppId(null);
        } else {
            if (appDeployConfigVO.getAppId() == null) {
                throw new CommonException(ExceptionConstants.AppCode.DEVOPS_APP_ID_IS_EMPTY);
            }
        }
        if (DeployTypeEnum.CREATE.value().equals(appDeployConfigVO.getDeployType())) {
            // 校验应用编码和应用名称
            devopsDeployAppCenterService.checkNameAndCodeUniqueAndThrow(appDeployConfigVO.getEnvId(),
                    RdupmTypeEnum.CHART.value(),
                    null,
                    appDeployConfigVO.getAppName(),
                    appDeployConfigVO.getAppCode());
        } else {
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(appDeployConfigVO.getAppId());
            if (devopsDeployAppCenterEnvDTO == null) {
                throw new CommonException(PipelineCheckConstant.DEVOPS_APP_NOT_EXIST);
            }
            appDeployConfigVO.setAppCode(devopsDeployAppCenterEnvDTO.getCode());
            appDeployConfigVO.setAppName(devopsDeployAppCenterEnvDTO.getName());
            if (!devopsDeployAppCenterEnvDTO.getEnvId().equals(appDeployConfigVO.getEnvId())) {
                throw new CommonException(PipelineCheckConstant.DEVOPS_APP_EXIST_IN_OTHER_ENV, pipelineJobVO.getName());
            }
        }
        if (appDeployConfigVO.getValueId() == null) {
            throw new CommonException(DEVOPS_DEPLOY_VALUE_ID_NULL);
        }
    }

    @Override
    public void fillAdditionalRecordInfo(PipelineJobRecordVO pipelineJobRecordVO) {
        if (PipelineStatusEnum.SUCCESS.value().equals(pipelineJobRecordVO.getStatus())) {
            Long commandId = pipelineJobRecordVO.getCommandId();
            if (commandId != null) {
                DeployRecordVO deployRecordVO = devopsDeployRecordService.queryEnvDeployRecordByCommandId(commandId);
                if (deployRecordVO != null) {
                    DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(deployRecordVO.getAppId());
                    DeployInfo deployInfo = new DeployInfo();
                    deployInfo.setAppServiceName(deployRecordVO.getDeployObjectName());
                    deployInfo.setAppServiceVersion(deployRecordVO.getDeployObjectVersion());
                    deployInfo.setEnvId(deployRecordVO.getEnvId());
                    deployInfo.setEnvName(deployRecordVO.getDeployPayloadName());
                    deployInfo.setAppId(deployRecordVO.getAppId());
                    deployInfo.setAppName(deployRecordVO.getAppName());
                    if (!ObjectUtils.isEmpty(devopsDeployAppCenterEnvDTO)) {
                        deployInfo.setRdupmType(devopsDeployAppCenterEnvDTO.getRdupmType());
                        deployInfo.setChartSource(devopsDeployAppCenterEnvDTO.getChartSource());
                        deployInfo.setOperationType(devopsDeployAppCenterEnvDTO.getOperationType());
                        deployInfo.setDeployType(MiscConstants.ENV);
                        deployInfo.setDeployTypeId(devopsDeployAppCenterEnvDTO.getEnvId());
                        if (RdupmTypeEnum.CHART.value().equals(devopsDeployAppCenterEnvDTO.getRdupmType())) {
                            AppServiceInstanceInfoVO appServiceInstanceInfoVO = appServiceInstanceService.queryInfoById(devopsDeployAppCenterEnvDTO.getProjectId(), devopsDeployAppCenterEnvDTO.getObjectId());
                            if (!ObjectUtils.isEmpty(appServiceInstanceInfoVO)) {
                                deployInfo.setAppServiceId(appServiceInstanceInfoVO.getAppServiceId());
                                deployInfo.setStatus(appServiceInstanceInfoVO.getStatus());
                            }
                        }
                    }
                    pipelineJobRecordVO.setDeployInfo(deployInfo);
                }
            }
        }
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
    public void fillJobAdditionalInfo(PipelineJobVO pipelineJobVO) {
        PipelineDTO pipelineDTO = pipelineService.baseQueryById(pipelineJobVO.getPipelineId());
        PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO = pipelineChartDeployCfgService.queryByConfigId(pipelineJobVO.getConfigId());
        pipelineJobVO.setEdit(devopsEnvironmentService.hasEnvironmentPermission(pipelineChartDeployCfgDTO.getEnvId(), pipelineDTO.getProjectId()));
    }

    @Override
    public void fillJobConfigInfo(PipelineJobVO pipelineJobVO) {
        PipelineChartDeployCfgVO pipelineChartDeployCfgVO = pipelineChartDeployCfgService.queryVoByConfigId(pipelineJobVO.getConfigId());
        if (pipelineChartDeployCfgVO.getAppServiceId() != null) {
            AppServiceDTO appServiceDTO = appServiceService.baseQuery(pipelineChartDeployCfgVO.getAppServiceId());
            if (appServiceDTO != null) {
                pipelineChartDeployCfgVO.setAppServiceDTO(ConvertUtils.convertObject(appServiceDTO, AppServiceRepVO.class));
            }
        }
        DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(pipelineChartDeployCfgVO.getValueId());
        if (devopsDeployValueDTO != null) {
            pipelineChartDeployCfgVO.setValue(devopsDeployValueDTO.getValue());
        }
        pipelineJobVO.setChartDeployCfg(pipelineChartDeployCfgVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void execCommand(Long jobRecordId, StringBuilder log) {
        log.append("#####开始执行Chart包自动部署任务#####").append(System.lineSeparator());
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
        if (!pipelineAppDeployUtil.checkAutoMaticDeploy(log, envId)) {
            pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SKIPPED.value());
            pipelineJobRecordService.update(pipelineJobRecordDTO);
            return;
        }
        // 2. 校验用户权限
        if (!pipelineAppDeployUtil.checkUserPermission(log, userId, envId, skipCheckPermission)) {
            pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SKIPPED.value());
            pipelineJobRecordService.update(pipelineJobRecordDTO);
            return;
        }
        // 获取部署版本信息

        AppServiceVersionDTO appServiceVersionDTO;
        if (PipelineTriggerTypeEnum.APP_VERSION.value().equals(pipelineRecordDTO.getTriggerType())) {
            //
            if (!appServiceId.equals(pipelineRecordDTO.getAppServiceId())) {
                log.append("当前任务不满足触发条件'应用服务匹配'，跳过此任务");
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SKIPPED.value());
                pipelineJobRecordService.update(pipelineJobRecordDTO);
                return;
            }
            appServiceVersionDTO = appServiceVersionService.baseQuery(pipelineRecordDTO.getAppServiceVersionId());
            // 没有填写版本类型则任意版本都会触发,填写了之后则至少要匹配一个
            // version格式：master | master,release
            if (StringUtils.isNotBlank(version)
                    && Arrays.stream(version.split(",")).noneMatch(v -> {
                String versionRegex = ".*" + version + ".*";
                Pattern pattern = Pattern.compile(versionRegex);
                return pattern.matcher(appServiceVersionDTO.getVersion()).matches();
            })) {
                log.append("当前任务不满足触发条件'应用服务版本匹配'，跳过此任务。匹配版本格式：").append(version).append(", 当前版本：").append(appServiceVersionDTO.getVersion());
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SKIPPED.value());
                pipelineJobRecordService.update(pipelineJobRecordDTO);
                return;
            }
        } else {
            appServiceVersionDTO = appServiceVersionService.queryLatestByAppServiceIdVersionType(appServiceId, version);
            if (appServiceVersionDTO == null) {
                log.append("当前任务不满足触发条件'应用服务版本不存在'，跳过此任务。").append("目标版本类型：").append(version).append(", 不存在");
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SKIPPED.value());
                pipelineJobRecordService.update(pipelineJobRecordDTO);
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
                    DeployType.AUTO);
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
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SKIPPED.value());
                pipelineJobRecordService.update(pipelineJobRecordDTO);
                return;
            }
            // 存在则更新
            AppServiceInstanceDTO preInstance = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
            DevopsEnvCommandDTO preCommand = devopsEnvCommandService.baseQuery(preInstance.getCommandId());
            AppServiceVersionRespVO deploydAppServiceVersion = appServiceVersionService.queryById(preCommand.getObjectVersionId());
            log.append("应用存在, 开始更新应用.").append(System.lineSeparator());

            if (Boolean.TRUE.equals(appServiceInstanceService.isInstanceDeploying(preInstance.getId()))) {
                log.append("应用当前处于部署中状态，请等待此次部署完成后重试。").append(System.lineSeparator());
                throw new CommonException("devops.app.instance.deploying");
            }
            DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = devopsEnvCommandValueService.baseQueryById(preCommand.getValueId());
            DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(valueId);
            // 如果当前部署版本和流水线生成版本相同则重启
            if (preCommand.getObjectVersionId().equals(appServiceVersionDTO.getId())
                    && devopsDeployValueDTO.getValue().equals(devopsEnvCommandValueDTO.getValue())) {
                log.append("此次部署版本和应用当前版本一致，触发重新部署.").append(System.lineSeparator());

                DevopsEnvCommandDTO devopsEnvCommandDTO = appServiceInstanceService.restartInstance(projectId,
                        preInstance.getId(),
                        DeployType.CD,
                        true);
                commandId = devopsEnvCommandDTO.getId();
                log.append("重新部署成功.").append(System.lineSeparator());
            } else {
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
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
                        pipelineJobRecordDTO.setCommandId(commandId);
                        pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SKIPPED.value());
                        pipelineJobRecordService.update(pipelineJobRecordDTO);
                        return;
                    }
                }

                appServiceDeployVO = new AppServiceDeployVO(appServiceVersionDTO.getAppServiceId(),
                        appServiceVersionDTO.getId(),
                        envId,
                        devopsDeployValueDTO.getValue(),
                        valueId,
                        appCode,
                        devopsDeployAppCenterEnvDTO.getObjectId(),
                        CommandType.UPDATE.getType(),
                        appName,
                        appCode);
                appServiceDeployVO.setInstanceId(devopsDeployAppCenterEnvDTO.getObjectId());
                AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService.createOrUpdate(projectId,
                        appServiceDeployVO,
                        DeployType.CD);
                commandId = appServiceInstanceVO.getCommandId();
            }
        }
        if (commandId != null) {
            pipelineJobRecordDTO.setCommandId(commandId);
        } else {
            log.append("[warn] 部署命令未找到.").append(System.lineSeparator());
        }
        pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SUCCESS.value());
        pipelineJobRecordService.update(pipelineJobRecordDTO);

    }

    @Override
    public CdJobTypeEnum getType() {
        return CdJobTypeEnum.CD_CHART_DEPLOY;
    }

}
