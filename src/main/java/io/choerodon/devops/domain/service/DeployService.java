package io.choerodon.devops.domain.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.dto.ProjectConfigDTO;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.DevopsClusterE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;

/**
 * Created by younger on 2018/4/18.
 */
public interface DeployService {
    void sendCommand(DevopsEnvironmentE devopsEnvironmentE);

    void deploy(ApplicationE applicationE, ApplicationVersionE applicationVersionE,
                String releaseName, DevopsEnvironmentE devopsEnvironmentE, String values,
                Long commandId);

    void initCluster(Long clusterId);

    void deleteEnv(Long envId, String code, Long clusterId);

    void initEnv(DevopsEnvironmentE devopsEnvironmentE, Long clusterId);

    void deployTestApp(ApplicationE applicationE, ApplicationVersionE applicationVersionE, String releaseName, String secretName, Long clusterId, String values);

    void getTestAppStatus(Map<Long, List<String>> testReleases);

    void upgradeCluster(DevopsClusterE devopsClusterE);

    void createCertManager(Long clusterId);

    void operatePodCount(String deploymentName, String namespace, Long clusterId, Long count);

    void operateSecret(Long clusterId, String namespace, String secretName, ProjectConfigDTO projectConfigDTO, String type);
}
