package io.choerodon.devops.api.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsCdEnvDeployInfoService;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.infra.enums.InstanceStatus;

/**
 * Creator: Runge
 * Date: 2018/6/13
 * Time: 11:25
 * Description:
 */
@Component
public class DevopsEnvironmentValidator {
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;

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
        if (!CollectionUtils.isEmpty(devopsCdEnvDeployInfoService.queryCurrentByEnvId(envId))) {
            throw new CommonException("error.env.stop.pipeline.app.deploy.exist");
        }
    }
}
