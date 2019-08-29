package io.choerodon.devops.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.app.service.PipelineAppDeployService;
import io.choerodon.devops.infra.enums.InstanceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creator: Runge
 * Date: 2018/6/13
 * Time: 11:25
 * Description:
 */
@Component
public class DevopsEnvironmentValidator {
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;

    /**
     * 验证环境是否可以禁用
     *
     * @param envId 环境ID
     */
    public void checkEnvCanDisabled(Long envId) {
        if (appServiceInstanceService.baseListByEnvId(envId).stream()
                .anyMatch(applicationInstanceE ->
                        InstanceStatus.RUNNING.getStatus().equals(applicationInstanceE.getStatus()))) {
            throw new CommonException("error.env.stop.instanceExist");
        }
        if (devopsServiceService.baseCheckServiceByEnv(envId)) {
            throw new CommonException("error.env.stop.serviceExist");
        }
        if (devopsIngressService.baseCheckByEnv(envId)) {
            throw new CommonException("error.env.stop.IngressExist");
        }
        if (!pipelineAppDeployService.baseQueryByEnvId(envId).isEmpty()) {
            throw new CommonException("error.env.stop.pipeline.app.deploy.exist");
        }
    }
}
