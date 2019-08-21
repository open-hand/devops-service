package io.choerodon.devops.api.ws.gitops;

import io.choerodon.devops.api.vo.AgentMsgVO;
import io.choerodon.devops.app.service.AgentMsgHandlerService;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.HelmType;
import io.choerodon.devops.infra.enums.InstanceStatus;
import io.choerodon.devops.infra.util.KeyParseUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.receive.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/7/25.
 */

@Component
public class AgentGitOpsMessageHandler implements MessageHandler<AgentMsgVO> {

    private static final Logger logger = LoggerFactory.getLogger(AgentGitOpsMessageHandler.class);

    @Autowired
    private AgentMsgHandlerService agentMsgHandlerService;

    @Override
    public void handle(WebSocketSession webSocketSession, String type, String key, AgentMsgVO msg) {
        HelmType helmType = HelmType.forValue(String.valueOf(msg.getType()));
        if(helmType==null) {
            logger.info("找不到指令啊" + msg.getType());
            return;
        }
        logger.info("收到了这个指令嘿嘿:" + helmType.toString());
        //设置集群id
        msg.setClusterId(key.split(":")[1]);

        if (helmType == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(msg.toString());
        }
        switch (helmType) {
            case HELM_INSTALL_JOB_INFO:
                agentMsgHandlerService.helmInstallJobInfo(
                        msg.getKey(),
                        msg.getPayload(),
                        TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_INSTALL_RESOURCE_INFO:
                agentMsgHandlerService.helmInstallResourceInfo(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_UPGRADE_RESOURCE_INFO:
                agentMsgHandlerService.helmUpgradeResourceInfo(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                agentMsgHandlerService.updateInstanceStatus(
                        msg.getKey(),
                        KeyParseUtil.getResourceName(msg.getKey()),
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
                        KeyParseUtil.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_RELEASE_STOP:
                agentMsgHandlerService.updateInstanceStatus(
                        msg.getKey(),
                        KeyParseUtil.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.STOPPED.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_UPGRADE_JOB_INFO:
                agentMsgHandlerService.helmUpgradeJobInfo(
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
            case HELM_JOB_LOG:
                agentMsgHandlerService.helmJobLog(
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
            case HELM_JOB_EVENT:
                agentMsgHandlerService.helmJobEvent(msg.getPayload());
                break;
            case HELM_POD_EVENT:
                agentMsgHandlerService.helmPodEvent(
                        msg.getPayload());
                break;
            case GIT_OPS_SYNC_EVENT:
                agentMsgHandlerService.gitOpsSyncEvent(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case RESOURCE_STATUS_SYNC_EVENT:
                agentMsgHandlerService.resourceStatusSyncEvent(msg.getKey(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case CERT_ISSUED:
                agentMsgHandlerService.certIssued(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case CERT_FAILED:
                agentMsgHandlerService.certFailed(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case RESOURCE_STATUS_SYNC:
                agentMsgHandlerService.resourceStatusSync(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case NAMESPACE_INFO:
                agentMsgHandlerService.namespaceInfo(msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
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
            default:
                break;
        }
    }

    @Override
    public String matchType() {
        return "agent";
    }

    @Override
    public Class<AgentMsgVO> payloadClass() {
        return AgentMsgVO.class;
    }
}
