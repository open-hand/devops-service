package io.choerodon.devops.api.validator;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CdEnvDeployInfoDTOCode.DEVOPS_ENV_STOP_PIPELINE_APP_DEPLOY_EXIST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.enums.InstanceStatus;

/**
 * Creator: Runge
 * Date: 2018/6/13
 * Time: 11:25
 * Description:
 */
@Component
public class DevopsEnvironmentValidator {

    private static final String DEVOPS_ENV_STOP_INSTANCE_EXIST = "devops.env.stop.instanceExist";
    private static final String DEVOPS_ENV_STOP_SERVICE_EXIST = "devops.env.stop.serviceExist";
    private static final String DEVOPS_ENV_STOP_INGRESS_EXIST = "devops.env.stop.IngressExist";

    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;
    @Autowired
    private PipelineService pipelineService;


    /**
     * 验证环境是否可以禁用
     *
     * @param projectId
     * @param envId     环境ID
     */
    public void checkEnvCanDisabled(Long projectId, Long envId) {
        if (appServiceInstanceService.baseListByEnvId(envId).stream()
                .anyMatch(applicationInstanceE ->
                        InstanceStatus.RUNNING.getStatus().equals(applicationInstanceE.getStatus()))) {
            throw new CommonException(DEVOPS_ENV_STOP_INSTANCE_EXIST);
        }
        if (devopsServiceService.baseCheckServiceByEnv(envId)) {
            throw new CommonException(DEVOPS_ENV_STOP_SERVICE_EXIST);
        }
        if (devopsIngressService.baseCheckByEnv(envId)) {
            throw new CommonException(DEVOPS_ENV_STOP_INGRESS_EXIST);
        }

        checkPipelineRef(projectId, envId);
    }

    public void checkPipelineRef(Long projectId, Long envId) {
        if (!CollectionUtils.isEmpty(devopsCiPipelineService.listChartEnvReferencePipelineInfo(projectId, envId))
                || !CollectionUtils.isEmpty(devopsCiPipelineService.listDeployEnvReferencePipelineInfo(projectId, envId))) {
            throw new CommonException(DEVOPS_ENV_STOP_PIPELINE_APP_DEPLOY_EXIST);
        }
    }

    public Boolean envRefPipeline(Long projectId, Long envId) {
        return !CollectionUtils.isEmpty(devopsCiPipelineService.listChartEnvReferencePipelineInfo(projectId, envId))
                || !CollectionUtils.isEmpty(devopsCiPipelineService.listDeployEnvReferencePipelineInfo(projectId, envId));
    }
}
