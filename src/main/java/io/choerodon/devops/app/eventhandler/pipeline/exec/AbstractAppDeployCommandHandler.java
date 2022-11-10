package io.choerodon.devops.app.eventhandler.pipeline.exec;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.pipeline.AppDeployConfigVO;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.app.service.DevopsEnvUserPermissionService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.gitops.IamAdminIdHolder;
import io.choerodon.devops.infra.util.CustomContextUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 16:03
 */
@Service
public abstract class AbstractAppDeployCommandHandler extends AbstractCiCommandHandler {

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    @Autowired
    DevopsCiJobRecordService devopsCiJobRecordService;

    @Override
    public CiCommandTypeEnum getType() {
        return CiCommandTypeEnum.CHART_DEPLOY;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log, Map<String, Object> content) {
        log.append("Start pipeline auto deploy task.").append(System.lineSeparator());
        AppDeployConfigVO appDeployConfigVO = queryConfigById(configId);
        if (appDeployConfigVO == null) {
            throw new CommonException("devops.chart.deploy.config.not.found");
        }
        Long projectId = appServiceDTO.getProjectId();
        Long appServiceId = appServiceDTO.getId();
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        Long userId = userDetails.getUserId();
        Long envId = appDeployConfigVO.getEnvId();
        Boolean skipCheckPermission = appDeployConfigVO.getSkipCheckPermission();
        String appCode = appDeployConfigVO.getAppCode();
        String appName = appDeployConfigVO.getAppName();

        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryByAppServiceIdAndGitlabPipelineId(appServiceId, gitlabPipelineId);

        DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceId, gitlabJobId);
        // 1. 校验环境是否开启一键关闭自动部署
        if (checkAutoMaticDeploy(log, envId)) return;
        // 2. 校验用户权限
        if (checkUserPermission(log, userId, envId, skipCheckPermission)) return;
        // 获取部署版本信息
        deployApp(appServiceDTO, log, appDeployConfigVO, projectId, appServiceId, envId, appCode, appName, devopsCiPipelineRecordDTO, devopsCiJobRecordDTO);
    }

    protected abstract void deployApp(AppServiceDTO appServiceDTO,
                                      StringBuilder log,
                                      AppDeployConfigVO appDeployConfigVO,
                                      Long projectId,
                                      Long appServiceId,
                                      Long envId,
                                      String appCode,
                                      String appName,
                                      DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO,
                                      DevopsCiJobRecordDTO devopsCiJobRecordDTO);

    protected boolean checkUserPermission(StringBuilder log, Long userId, Long envId, Boolean skipCheckPermission) {
        log.append("## 2.Check user env permission.").append(System.lineSeparator());
        if (Boolean.FALSE.equals(skipCheckPermission)) {
            log.append("Skip check user permission Flag is false, check user permission.").append(System.lineSeparator());
            if (Boolean.FALSE.equals(devopsEnvUserPermissionService.checkUserEnvPermission(envId,
                    userId))) {
                log.append("User have no env Permission, skipped.").append(System.lineSeparator());
                return true;
            } else {
                log.append("Check user permission Passed.").append(System.lineSeparator());
            }
        } else {
            log.append("Skip check user permission Flag is true, choose deploy account.").append(System.lineSeparator());
            if (Boolean.FALSE.equals(devopsEnvUserPermissionService.checkUserEnvPermission(envId,
                    userId))) {
                log.append("User have no env Permission, use admin account to deploy.").append(System.lineSeparator());
                CustomContextUtil.setUserContext(IamAdminIdHolder.getAdminId());
            } else {
                log.append("User have env Permission, use self account to deploy.").append(System.lineSeparator());
            }
        }
        return false;
    }

    protected boolean checkAutoMaticDeploy(StringBuilder log, Long envId) {
        log.append("## 1.Check Environment automatic deploy enable.").append(System.lineSeparator());
        if (Boolean.FALSE.equals(devopsEnvironmentService.queryByIdOrThrowE(envId).getAutoDeploy())) {
            log.append("Environment automatic deploy has been turned off!").append(System.lineSeparator());
            return true;
        }
        return false;
    }


    protected abstract AppDeployConfigVO queryConfigById(Long configId);

}
