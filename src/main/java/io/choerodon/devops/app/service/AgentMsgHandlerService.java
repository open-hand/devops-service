package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.AppServiceDTO;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface AgentMsgHandlerService {

    void handlerReleaseInstall(String key, String msg, Long clusterId);

    void handlerPreInstall(String key, String msg, Long clusterId);

    void resourceUpdate(String key, String msg, Long clusterId);

    void resourceDelete(String key, String msg, Long clusterId);

    void helmReleaseHookLogs(String key, String msg, Long clusterId);

    void updateInstanceStatus(String key, String releaseName, Long clusterId, String instanceStatus, String commandStatus, String commandMsg);

    void handlerDomainCreateMessage(String key, String msg, Long clusterId);

    void helmReleasePreUpgrade(String key, String msg, Long clusterId);

    void handlerReleaseUpgrade(String key, String msg, Long clusterId);

    void helmReleaseDeleteFail(String key, String msg, Long clusterId);

    void helmReleaseStartFail(String key, String msg, Long clusterId);

    void helmReleaseRollBackFail(String key, String msg);

    void helmReleaseInstallFail(String key, String msg, Long clusterId);

    void helmReleaseUpgradeFail(String key, String msg, Long clusterId);

    void helmReleaeStopFail(String key, String msg, Long clusterId);

    void commandNotSend(Long commandId, String msg);

    void resourceSync(String key, String msg, Long clusterId);

    void jobEvent(String msg);

    void releasePodEvent(String msg);

    void gitOpsSyncEvent(String key, String msg, Long clusterId);

    List<AppServiceDTO> getApplication(String appServiceName, Long projectId, Long orgId);

    void gitOpsCommandSyncEvent(String key, Long clusterId);

    void certIssued(String key, String msg, Long clusterId);

    void certFailed(String key, String msg, Long clusterId);

    void gitOpsCommandSyncEventResult(String key, String msg, Long clusterId);

    void handlerServiceCreateMessage(String key, String msg, Long clusterId);

    void updateNamespaces(String msg, Long clusterId);

    void upgradeCluster(String key, String msg);

    void testPodUpdate(String key, String msg, Long clusterId);

    void testJobLog(String key, String msg, Long clusterId);

    void getTestAppStatus(String key, String msg, Long clusterId);

    void getCertManagerInfo(String msg, Long clusterId);

    void handleNodeSync(String msg, Long clusterId);

    void handleConfigUpdate(String key, String msg, Long clusterId);

    void operateDockerRegistrySecretResp(String key, String msg, Long clusterId);
}
