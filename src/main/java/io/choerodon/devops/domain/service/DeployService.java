package io.choerodon.devops.domain.service;

import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;

/**
 * Created by younger on 2018/4/18.
 */
public interface DeployService {
    void sendCommand(DevopsEnvironmentE devopsEnvironmentE);

    void deploy(ApplicationE applicationE, ApplicationVersionE applicationVersionE,
                ApplicationInstanceE applicationInstanceE, DevopsEnvironmentE devopsEnvironmentE, String values,
                Long commandId);

    void initCluster(Long clusterId);

    void deleteEnv(Long envId, String code, Long clusterId);

    void initEnv(DevopsEnvironmentE devopsEnvironmentE, Long clusterId);
}
