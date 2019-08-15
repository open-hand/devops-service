package io.choerodon.devops.infra.gitops;

import io.choerodon.devops.app.service.AgentMsgHandlerService;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.HelmType;
import io.choerodon.devops.infra.enums.InstanceStatus;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.process.AbstractAgentMsgHandler;
import io.choerodon.websocket.tool.KeyParseTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/4/17.
 */
@Component
public class AgentMessageHandler extends AbstractAgentMsgHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentMessageHandler.class);

    private AgentMsgHandlerService agentMsgHandlerService;

    @Autowired
    public AgentMessageHandler(AgentMsgHandlerService agentMsgHandlerService) {
        this.agentMsgHandlerService = agentMsgHandlerService;
    }

    @Override
    public void process(Msg msg) {
        HelmType helmType = HelmType.forValue(String.valueOf(msg.getType()));
        if (helmType == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(msg.toString());
        }
        msg.setDispatch(false);
        switch (helmType) {
            case HELM_RELEASE_PRE_INSTALL:
                agentMsgHandlerService.handlerPreInstall(
                        msg.getKey(),
                        msg.getPayload(),
                        TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_INSTALL_RELEASE:
                agentMsgHandlerService.handlerReleaseInstall(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_RELEASE_UPGRADE:
                agentMsgHandlerService.handlerReleaseUpgrade(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                agentMsgHandlerService.updateInstanceStatus(
                        msg.getKey(),
                        KeyParseTool.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_RELEASE_ROLLBACK:
                break;
            case HELM_RELEASE_START:
                agentMsgHandlerService.updateInstanceStatus(
                        msg.getKey(),
                        KeyParseTool.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_RELEASE_STOP:
                agentMsgHandlerService.updateInstanceStatus(
                        msg.getKey(),
                        KeyParseTool.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.STOPPED.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_RELEASE_PRE_UPGRADE:
                agentMsgHandlerService.helmReleasePreUpgrade(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case NETWORK_SERVICE:
                agentMsgHandlerService.handlerServiceCreateMessage(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case NETWORK_INGRESS:
                agentMsgHandlerService.handlerDomainCreateMessage(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case NETWORK_INGRESS_DELETE:
                break;
            case RESOURCE_UPDATE:
                agentMsgHandlerService.resourceUpdate(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case RESOURCE_DELETE:
                agentMsgHandlerService.resourceDelete(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_RELEASE_HOOK_LOGS:
                agentMsgHandlerService.helmReleaseHookLogs(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_RELEASE_DELETE_FAILED:
                agentMsgHandlerService.helmReleaseDeleteFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_RELEASE_INSTALL_FAILED:
                agentMsgHandlerService.helmReleaseInstallFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_RELEASE_START_FAILED:
                agentMsgHandlerService.helmReleaseStartFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_RELEASE_STOP_FAILED:
                agentMsgHandlerService.helmReleaseStopFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_RELEASE_ROLLBACK_FAILED:
                agentMsgHandlerService.helmReleaseRollBackFail(msg.getKey(), msg.getPayload());
                break;
            case HELM_RELEASE_UPGRADE_FAILED:
                agentMsgHandlerService.helmReleaseUpgradeFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case COMMAND_NOT_SEND:
                agentMsgHandlerService.commandNotSend(msg.getCommandId(), msg.getPayload());
                break;
            case RESOURCE_SYNC:
                agentMsgHandlerService.resourceSync(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case JOB_EVENT:
                agentMsgHandlerService.jobEvent(msg.getPayload());
                break;
            case RELEASE_POD_EVENT:
                agentMsgHandlerService.releasePodEvent(
                        msg.getPayload());
                break;
            case GIT_OPS_SYNC_EVENT:
                agentMsgHandlerService.gitOpsSyncEvent(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case STATUS_SYNC_EVENT:
                agentMsgHandlerService.gitOpsCommandSyncEvent(msg.getKey(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case CERT_ISSUED:
                agentMsgHandlerService.certIssued(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case CERT_FAILED:
                agentMsgHandlerService.certFailed(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case STATUS_SYNC:
                agentMsgHandlerService.gitOpsCommandSyncEventResult(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case NAMESPACE_UPDATE:
                agentMsgHandlerService.updateNamespaces(msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case UPGRADE_CLUSTER:
                agentMsgHandlerService.upgradeCluster(msg.getKey(), msg.getPayload());
                break;
            case TEST_POD_UPDATE:
                agentMsgHandlerService.testPodUpdate(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case TEST_JOB_LOG:
                agentMsgHandlerService.testJobLog(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case TEST_STATUS_RESPONSE:
                agentMsgHandlerService.getTestAppStatus(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case CERT_MANAGER_INFO:
                agentMsgHandlerService.getCertManagerInfo(msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case NODE_SYNC:
                agentMsgHandlerService.handleNodeSync(msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case CONFIG_UPDATE:
                agentMsgHandlerService.handleConfigUpdate(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case OPERATE_DOCKER_REGISTRY_SECRET_FAILED:
                agentMsgHandlerService.operateDockerRegistrySecretResp(msg.getKey(), "failed", TypeUtil.objToLong(msg.getClusterId()));
                break;
            case OPERATE_DOCKER_REGISTRY_SECRET:
                agentMsgHandlerService.operateDockerRegistrySecretResp(msg.getKey(), "success", TypeUtil.objToLong(msg.getClusterId()));
                break;
            case POD_REAL_TIME:
                agentMsgHandlerService.handlePodRealTimeData(msg.getPayload());
                break;
            default:
                break;
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
