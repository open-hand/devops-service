package io.choerodon.devops.infra.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.devops.app.service.DevopsEnvUserPermissionService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.gitops.IamAdminIdHolder;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/28 15:59
 */
@Component
public class PipelineAppDeployUtil {

    private static final String DEVOPS_PIPELINE_ARTIFACT_NOT_FOUND = "devops.pipeline.artifact.not.found";

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;

    public boolean checkAutoMaticDeploy(StringBuilder log, Long envId) {
        log.append("#####校验环境是否开启自动部署#####").append(System.lineSeparator());
        Boolean autoDeploy = devopsEnvironmentService.queryByIdOrThrowE(envId).getAutoDeploy();
        if (Boolean.FALSE.equals(autoDeploy)) {
            log.append("环境自动部署已关闭，跳过此部署任务。").append(System.lineSeparator());
        }
        return autoDeploy;
    }

    public boolean checkUserPermission(StringBuilder log, Long userId, Long envId, Boolean skipCheckPermission) {
        log.append("#####校验用户环境权限#####").append(System.lineSeparator());
        if (Boolean.FALSE.equals(skipCheckPermission)) {
            log.append("不允许非环境人员触发此部署任务，校验用户权限").append(System.lineSeparator());
            if (Boolean.FALSE.equals(devopsEnvUserPermissionService.checkUserEnvPermission(envId,
                    userId))) {
                log.append("用户没有环境权限，跳过此部署任务").append(System.lineSeparator());
                return false;
            } else {
                log.append("用户权限校验通过").append(System.lineSeparator());
            }
        } else {
            log.append("允许非环境人员触发此部署任务，选择部署账户").append(System.lineSeparator());
            if (Boolean.FALSE.equals(devopsEnvUserPermissionService.checkUserEnvPermission(envId,
                    userId))) {
                log.append("用户没有环境权限，使用admin账户部署").append(System.lineSeparator());
                CustomContextUtil.setUserContext(IamAdminIdHolder.getAdminId());
            } else {
                log.append("用户拥有环境权限，使用用户账户部署").append(System.lineSeparator());
            }
        }
        return true;
    }

    public AppServiceVersionDTO queryAppVersion(StringBuilder log, Long appServiceId, String commitSha, String gitlabTriggerRef) {
        log.append("#####查询部署版本#####").append(System.lineSeparator());
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.queryByCommitShaAndRef(appServiceId,
                commitSha,
                gitlabTriggerRef);
        if (appServiceVersionDTO == null) {
            log.append("流水线上游未生成应用服务版本，跳过此次部署").append(System.lineSeparator());
            throw new CommonException(DEVOPS_PIPELINE_ARTIFACT_NOT_FOUND);
        } else {
            log.append("部署版本为：").append(appServiceVersionDTO.getVersion()).append(System.lineSeparator());
        }
        return appServiceVersionDTO;
    }
}
