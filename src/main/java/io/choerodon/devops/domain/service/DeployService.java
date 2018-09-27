package io.choerodon.devops.domain.service;

import io.choerodon.devops.domain.application.entity.*;
/**
 * Created by younger on 2018/4/18.
 */
public interface DeployService {
    void sendCommand(DevopsEnvironmentE devopsEnvironmentE);


    void deploy(ApplicationE applicationE, ApplicationVersionE applicationVersionE, ApplicationInstanceE applicationInstanceE, DevopsEnvironmentE devopsEnvironmentE, String values,Long commandId);

}
