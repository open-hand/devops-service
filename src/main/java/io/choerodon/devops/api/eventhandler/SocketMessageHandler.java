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
            case HelmReleasePreInstall:
                deployMsgHandlerService.handlerPreInstall(msg.getPayload());
                break;
            case HelmInstallRelease:
                deployMsgHandlerService.handlerReleaseInstall(msg.getPayload());
                break;
            case HelmReleaseUpgrade:
                deployMsgHandlerService.handlerReleaseUpgrade(msg.getPayload());
                deployMsgHandlerService.updateInstanceStatus(
                        KeyParseTool.getResourceName(msg.getKey()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getCommandStatus(),
                        "");
                break;
            case HelmReleaseRollback:
                break;
            case HelmReleaseStart:
                deployMsgHandlerService.updateInstanceStatus(
                        KeyParseTool.getResourceName(msg.getKey()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getCommandStatus(),
                        "");
                break;
            case HelmReleaseStop:
                deployMsgHandlerService.updateInstanceStatus(
                        KeyParseTool.getResourceName(msg.getKey()),
                        InstanceStatus.STOPED.getStatus(),
                        CommandStatus.SUCCESS.getCommandStatus(),
                        "");
                break;
            case HelmReleaseDelete:
                deployMsgHandlerService.updateInstanceStatus(
                        KeyParseTool.getResourceName(msg.getKey()),
                        InstanceStatus.DELETED.getStatus(),
                        CommandStatus.SUCCESS.getCommandStatus(),
                        "");
                break;
            case HelmReleasePreUpgrade:
                deployMsgHandlerService.helmReleasePreUpgrade(msg.getPayload());
                break;
            case NetworkService:
                serviceMsgHandlerService.handlerServiceCreateMessage(msg.getKey(), msg.getPayload());
                break;
            case NetworkIngress:
                deployMsgHandlerService.handlerDomainCreateMessage(msg.getKey(), msg.getPayload());
                break;
            case NetworkIngressDelete:
                break;
            case ResourceUpdate:
                deployMsgHandlerService.resourceUpdate(msg.getKey(), TypeUtil.objToLong(msg.getEnvId()), msg.getPayload());
                break;
            case ResourceDelete:
                deployMsgHandlerService.resourceDelete(TypeUtil.objToLong(msg.getEnvId()), msg.getKey());
                break;
            case HelmReleaseHookLogs:
                deployMsgHandlerService.helmReleaseHookLogs(msg.getKey(), msg.getPayload());
                break;
            case NetworkServiceUpdate:
                deployMsgHandlerService.netWorkUpdate(msg.getKey(), msg.getPayload());
                break;
            case HelmReleaseDeleteFailed:
                deployMsgHandlerService.helmReleaseDeleteFail(msg.getKey(), msg.getPayload());
                break;
            case HelmReleaseInstallFailed:
                deployMsgHandlerService.helmReleaseInstallFail(msg.getKey(), msg.getPayload());
                break;
            case HelmReleaseStartFailed:
                deployMsgHandlerService.helmReleaseStartFail(msg.getKey(), msg.getPayload());
                break;
            case HelmReleaseStopFailed:
                deployMsgHandlerService.helmReleaeStopFail(msg.getKey(), msg.getPayload());
                break;
            case HelmReleaseRollbackFailed:
                deployMsgHandlerService.helmReleaseRollBackFail(msg.getKey(), msg.getPayload());
                break;
            case HelmReleaseUpgradeFailed:
                deployMsgHandlerService.helmReleaseUpgradeFail(msg.getKey(), msg.getPayload());
                break;
            case HelmReleaseGetContent:
                deployMsgHandlerService.helmReleaseGetContent(msg.getKey(), TypeUtil.objToLong(msg.getEnvId()), msg.getPayload());
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
