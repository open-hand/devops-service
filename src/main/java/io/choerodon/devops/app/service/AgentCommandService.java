package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.ProjectConfigVO;
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionE;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.infra.dto.ApplicationDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;

/**
 * Created by younger on 2018/4/18.
 */
public interface AgentCommandService {
    void sendCommand(DevopsEnvironmentE devopsEnvironmentE);

    void deploy(ApplicationDTO applicationDTO, ApplicationVersionDTO applicationVersionDTO,
                String releaseName, DevopsEnvironmentDTO devopsEnvironmentDTO, String values,
                Long commandId, String secretCode);

    void initCluster(Long clusterId);

    void deleteEnv(Long envId, String code, Long clusterId);

    void initEnv(DevopsEnvironmentE devopsEnvironmentE, Long clusterId);

    void deployTestApp(ApplicationDTO applicationDTO, ApplicationVersionDTO applicationVersionDTO, String releaseName, String secretName, Long clusterId, String values);

    void getTestAppStatus(Map<Long, List<String>> testReleases);

    void upgradeCluster(DevopsClusterE devopsClusterE);

    void createCertManager(Long clusterId);

    void operatePodCount(String deploymentName, String namespace, Long clusterId, Long count);

    void operateSecret(Long clusterId, String namespace, String secretName, ProjectConfigVO projectConfigVO, String type);
}
