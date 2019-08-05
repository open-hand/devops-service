package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.ProjectConfigVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;

/**
 * Created by younger on 2018/4/18.
 */
public interface AgentCommandService {
    void sendCommand(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void deploy(AppServiceDTO applicationDTO, AppServiceVersionDTO appServiceVersionDTO,
                String releaseName, DevopsEnvironmentDTO devopsEnvironmentDTO, String values,
                Long commandId, String secretCode);

    void initCluster(Long clusterId);

    void deleteEnv(Long envId, String code, Long clusterId);

    void initEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, Long clusterId);

    void deployTestApp(AppServiceDTO applicationDTO, AppServiceVersionDTO appServiceVersionDTO, String releaseName, String secretName, Long clusterId, String values);

    void getTestAppStatus(Map<Long, List<String>> testReleases);

    void upgradeCluster(DevopsClusterDTO devopsClusterDTO);

    void createCertManager(Long clusterId);

    void operatePodCount(String deploymentName, String namespace, Long clusterId, Long count);

    void operateSecret(Long clusterId, String namespace, String secretName, ProjectConfigVO projectConfigVO, String type);
}
