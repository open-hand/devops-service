package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.AppServiceDTO;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface AgentMsgHandlerService {

    void helmInstallResourceInfo(String key, String msg, Long clusterId);

    void helmInstallJobInfo(String key, String msg, Long clusterId);

    void resourceUpdate(String key, String msg, Long clusterId);

    void resourceDelete(String key, String msg, Long clusterId);

    void helmJobLog(String key, String msg, Long clusterId);

    void updateInstanceStatus(String key, String releaseName, Long clusterId, String instanceStatus, String commandStatus, String commandMsg);

    void handlerDomainCreateMessage(String key, String msg, Long clusterId);

    void helmUpgradeJobInfo(String key, String msg, Long clusterId);

    void helmUpgradeResourceInfo(String key, String msg, Long clusterId);

    void helmReleaseDeleteFail(String key, String msg, Long clusterId);

    void helmReleaseStartFail(String key, String msg, Long clusterId);

    void helmReleaseRollBackFail(String key, String msg);

    void helmReleaseInstallFail(String key, String msg, Long clusterId);

    void helmReleaseUpgradeFail(String key, String msg, Long clusterId);

    void helmReleaseStopFail(String key, String msg, Long clusterId);

    void commandNotSend(Long commandId, String msg);

    void resourceSync(String key, String msg, Long clusterId);

    void helmJobEvent(String msg);

    void helmPodEvent(String msg);

    void gitOpsSyncEvent(String key, String msg, Long clusterId);

    List<AppServiceDTO> getApplication(String appServiceName, Long projectId, Long orgId);

    void resourceStatusSyncEvent(String key, Long clusterId);

    void certIssued(String key, String msg, Long clusterId);

    void certFailed(String key, String msg, Long clusterId);

    void resourceStatusSync(String key, String msg, Long clusterId);

    void handlerServiceCreateMessage(String key, String msg, Long clusterId);

    void namespaceInfo(String msg, Long clusterId);

    void upgradeCluster(String key, String msg);

    void testPodUpdate(String key, String msg, Long clusterId);

    void testJobLog(String key, String msg, Long clusterId);

    void getTestAppStatus(String key, String msg, Long clusterId);

    void getCertManagerInfo(String msg, Long clusterId);

    void handleNodeSync(String msg, Long clusterId);

    void handleConfigUpdate(String key, String msg, Long clusterId);

    void operateDockerRegistrySecretResp(String key, String msg, Long clusterId);

    void handlePodMetricsSync(String key, String result, Long clusterId);
}
