package io.choerodon.devops.api.eventhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DeployMsgHandlerService;
import io.choerodon.devops.app.service.ServiceMsgHandlerService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.HelmType;
import io.choerodon.devops.infra.common.util.enums.InstanceStatus;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.process.AbstractAgentMsgHandler;
import io.choerodon.websocket.tool.KeyParseTool;

/**
 * Created by Zenger on 2018/4/17.
 */
@Component
public class SocketMessageHandler extends AbstractAgentMsgHandler {

    private static final Logger logger = LoggerFactory.getLogger(SocketMessageHandler.class);

    private DeployMsgHandlerService deployMsgHandlerService;
    private ServiceMsgHandlerService serviceMsgHandlerService;


    @Autowired
    public SocketMessageHandler(DeployMsgHandlerService deployMsgHandlerService,
                                ServiceMsgHandlerService serviceMsgHandlerService) {
        this.deployMsgHandlerService = deployMsgHandlerService;
        this.serviceMsgHandlerService = serviceMsgHandlerService;
    }


    @Override
    public void process(Msg msg) {
        HelmType helmType = HelmType.forString(String.valueOf(msg.getType()));
        if (helmType == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(msg.toString());
        }
        switch (helmType) {
            case HELM_RELEASE_PRE_INSTALL:
                deployMsgHandlerService.handlerPreInstall(msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()), "create");
                break;
            case HELM_INSTALL_RELEASE:
                deployMsgHandlerService.handlerReleaseInstall(msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case HELM_RELEASE_UPGRADE:
                deployMsgHandlerService.handlerReleaseUpgrade(msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                deployMsgHandlerService.updateInstanceStatus(
                        KeyParseTool.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getEnvId()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_RELEASE_ROLLBACK:
                break;
            case HELM_RELEASE_START:
                deployMsgHandlerService.updateInstanceStatus(
                        KeyParseTool.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getEnvId()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_RELEASE_STOP:
                deployMsgHandlerService.updateInstanceStatus(
                        KeyParseTool.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getEnvId()),
                        InstanceStatus.STOPED.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_RELEASE_DELETE:
                deployMsgHandlerService.updateInstanceStatus(
                        KeyParseTool.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getEnvId()),
                        InstanceStatus.DELETED.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            case HELM_RELEASE_PRE_UPGRADE:
                deployMsgHandlerService.helmReleasePreUpgrade(msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()), "update");
                break;
            case NETWORK_SERVICE:
                serviceMsgHandlerService.handlerServiceCreateMessage(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case NETWORK_INGRESS:
                deployMsgHandlerService.handlerDomainCreateMessage(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case NETWORK_INGRESS_DELETE:
                break;
            case RESOURCE_UPDATE:
                deployMsgHandlerService.resourceUpdate(
                        msg.getKey(), TypeUtil.objToLong(msg.getEnvId()), msg.getPayload());
                break;
            case RESOURCE_DELETE:
                deployMsgHandlerService.resourceDelete(TypeUtil.objToLong(msg.getEnvId()), msg.getKey());
                break;
            case HELM_RELEASE_HOOK_LOGS:
                deployMsgHandlerService.helmReleaseHookLogs(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case NETWORK_SERVICE_UPDATE:
                deployMsgHandlerService.netWorkUpdate(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case HELM_RELEASE_DELETE_FAILED:
                deployMsgHandlerService.helmReleaseDeleteFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case HELM_RELEASE_INSTALL_FAILED:
                deployMsgHandlerService.helmReleaseInstallFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case HELM_RELEASE_START_FAILED:
                deployMsgHandlerService.helmReleaseStartFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case HELM_RELEASE_STOP_FAILED:
                deployMsgHandlerService.helmReleaeStopFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case HELM_RELEASE_ROLLBACK_FAILED:
                deployMsgHandlerService.helmReleaseRollBackFail(msg.getKey(), msg.getPayload());
                break;
            case HELM_RELEASE_UPGRADE_FAILED:
                deployMsgHandlerService.helmReleaseUpgradeFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case HELM_RELEASE_GET_CONTENT:
                deployMsgHandlerService.helmReleaseGetContent(
                        msg.getKey(), TypeUtil.objToLong(msg.getEnvId()), msg.getPayload());
                break;
            case COMMAND_NOT_SEND:
                deployMsgHandlerService.commandNotSend(msg.getCommandId(), msg.getPayload());
                break;
            case NETWORK_SERVICE_FAILED:
                deployMsgHandlerService.netWorkServiceFail(msg.getKey(), msg.getPayload());
                break;
            case NETWORK_INGRESS_FAILED:
                deployMsgHandlerService.netWorkIngressFail(
                        msg.getKey(), TypeUtil.objToLong(msg.getEnvId()), msg.getPayload());
                break;
            case NETWORK_SERVICE_DELETE_FAILED:
                deployMsgHandlerService.netWorkServiceDeleteFail(msg.getKey(), msg.getPayload());
                break;
            case NETWORK_INGRESS_DELETE_FAILED:
                deployMsgHandlerService.netWorkIngressDeleteFail(
                        msg.getKey(), TypeUtil.objToLong(msg.getEnvId()), msg.getPayload());
                break;
            case RESOURCE_SYNC:
                deployMsgHandlerService.resourceSync(
                        msg.getKey(), TypeUtil.objToLong(msg.getEnvId()), msg.getPayload());
                break;
            case JOB_EVENT:
                deployMsgHandlerService.jobEvent(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
                break;
            case RELEASE_POD_EVENT:
                deployMsgHandlerService.releasePodEvent(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getEnvId()));
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
