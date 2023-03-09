package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.AgentMsgVO;
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

    void updateStartOrStopInstanceStatus(String key, String releaseName, Long clusterId, String instanceStatus, String commandStatus, String payload);

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

    void helmJobEvent(String key, String msg, Long clusterId);

    void helmPodEvent(String key, String msg, Long clusterId);

    void gitOpsSyncEvent(String key, String msg, Long clusterId);

    List<AppServiceDTO> getApplication(String appServiceName, Long projectId, Long orgId);

    void resourceStatusSyncEvent(String key, Long clusterId);

    void certIssued(String key, String msg, Long clusterId);

    void certFailed(String key, String msg, Long clusterId);

    void resourceStatusSync(String key, String msg, Long clusterId);

    void handlerServiceCreateMessage(String key, String msg, Long clusterId);

    void namespaceInfo(String msg, Long clusterId);

    void testPodUpdate(String key, String msg, Long clusterId);

    void testJobLog(String key, String msg, Long clusterId);

    void getTestAppStatus(String key, String msg, Long clusterId);

    void handleCertManagerInfo(AgentMsgVO msg, Long clusterId);

    void handleNodeSync(String msg, Long clusterId);

    void handleConfigUpdate(String key, String msg, Long clusterId);

    void operateDockerRegistrySecretResp(String key, String msg, Long clusterId);

    void handlePodMetricsSync(String key, String result, Long clusterId);

    /**
     * 处理agent启动时发来的集群信息
     */
    void handleClusterInfo(AgentMsgVO msg);

    /**
     * 处理工作负载产生的pod事件
     */
    void workloadPodEvent(String key, String msg, Long clusterId);

    void handleDeletePod(Long clusterId, String payload);

    void operatePodCount(String key, String payload, Long clusterId, boolean success);
}
