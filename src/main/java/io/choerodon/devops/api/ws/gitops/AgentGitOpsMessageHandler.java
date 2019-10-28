package io.choerodon.devops.api.ws.gitops;

import io.choerodon.devops.api.vo.AgentMsgVO;
import io.choerodon.devops.app.service.AgentMsgHandlerService;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.HelmType;
import io.choerodon.devops.infra.enums.InstanceStatus;
import io.choerodon.devops.infra.util.KeyParseUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.receive.TextMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/7/25.
 */

@Component
public class AgentGitOpsMessageHandler implements TextMessageHandler<AgentMsgVO> {

    private static final Logger logger = LoggerFactory.getLogger(AgentGitOpsMessageHandler.class);

    @Autowired
    private AgentMsgHandlerService agentMsgHandlerService;

    @Override
    public void handle(WebSocketSession webSocketSession, String type, String key, AgentMsgVO msg) {
        HelmType helmType = HelmType.forValue(String.valueOf(msg.getType()));
        logger.info("===========================查看msg.type:{}", msg.getType());
        if (helmType == null) {
            logger.info("找不到指令啊 {}", msg.getType());
            return;
        }
        //设置集群id
        msg.setClusterId(key.split(":")[1]);
        if (logger.isDebugEnabled()) {
            logger.debug(msg.toString());
        }
        switch (helmType) {
            // JOB的相关信息
            case HELM_INSTALL_JOB_INFO:
                agentMsgHandlerService.helmInstallJobInfo(
                        msg.getKey(),
                        msg.getPayload(),
                        TypeUtil.objToLong(msg.getClusterId()));
                break;
            // helm release包中的相关资源(除去JOB)信息同步
            // 可能因为消息缓冲池大小太小而接收不到消息
            case HELM_INSTALL_RESOURCE_INFO:
                agentMsgHandlerService.helmInstallResourceInfo(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // helm release更新时包中的相关资源(除去JOB)信息同步
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
            // 更新实例启动的状态
            case HELM_RELEASE_START:
                agentMsgHandlerService.updateInstanceStatus(
                        msg.getKey(),
                        KeyParseUtil.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            // 更新实例停止的状态
            case HELM_RELEASE_STOP:
                agentMsgHandlerService.updateInstanceStatus(
                        msg.getKey(),
                        KeyParseUtil.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.STOPPED.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        "");
                break;
            // 同步release升级时 JOB信息
            case HELM_UPGRADE_JOB_INFO:
                agentMsgHandlerService.helmUpgradeJobInfo(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 资源更新devops_env_resource和detail
            case RESOURCE_UPDATE:
                agentMsgHandlerService.resourceUpdate(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 资源删除
            case RESOURCE_DELETE:
                agentMsgHandlerService.resourceDelete(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 同步JOB的日志信息
            case HELM_JOB_LOG:
                agentMsgHandlerService.helmJobLog(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // helm release删除失败
            case HELM_RELEASE_DELETE_FAILED:
                agentMsgHandlerService.helmReleaseDeleteFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // helm release安装失败
            case HELM_RELEASE_INSTALL_FAILED:
                agentMsgHandlerService.helmReleaseInstallFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // helm release启动失败
            case HELM_RELEASE_START_FAILED:
                agentMsgHandlerService.helmReleaseStartFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // helm release停止失败
            case HELM_RELEASE_STOP_FAILED:
                agentMsgHandlerService.helmReleaseStopFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // helm release升级失败
            case HELM_RELEASE_UPGRADE_FAILED:
                agentMsgHandlerService.helmReleaseUpgradeFail(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // K8S的资源同步(Agent启动发送一次)，只是为了清脏数据
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
            // Agent解析GitOps后发送
            case GIT_OPS_SYNC_EVENT:
                agentMsgHandlerService.gitOpsSyncEvent(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 接收Agent定时发送此消息以通知DevOps进行资源状态同步，
            // DevOps将现在的创建三分钟以上还是处理中的资源相关信息发送过去
            case RESOURCE_STATUS_SYNC_EVENT:
                agentMsgHandlerService.resourceStatusSyncEvent(msg.getKey(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 证书创建成功，处理日期
            case CERT_ISSUED:
                agentMsgHandlerService.certIssued(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 证书失败
            case CERT_FAILED:
                agentMsgHandlerService.certFailed(
                        msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 和RESOURCE_STATUS_SYNC_EVENT事件中发送的数据对应，这是对应的响应
            case RESOURCE_STATUS_SYNC:
                agentMsgHandlerService.resourceStatusSync(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // Agent连接到DevOps时发送一次它所在集群的namespace信息
            case NAMESPACE_INFO:
                agentMsgHandlerService.namespaceInfo(msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 同步测试应用状态
            case TEST_POD_UPDATE:
                agentMsgHandlerService.testPodUpdate(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 测试应用的JOB的日志
            case TEST_JOB_LOG:
                agentMsgHandlerService.testJobLog(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 敏捷组定时任务调用DevOps接口发送给Agent之后, Agent返回的响应
            case TEST_STATUS_RESPONSE:
                agentMsgHandlerService.getTestAppStatus(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // Agent启动的时候发送给Devops,
            // 如果是空的（也就是说没有certManager），devops会返回一些安装certManager所需的数据
            case CERT_MANAGER_INFO:
                agentMsgHandlerService.getCertManagerInfo(msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 接收Agent定时发送的节点数据
            case NODE_SYNC:
                agentMsgHandlerService.handleNodeSync(msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 每个微服务会被go-registry创建一个配置映射(替代config-server实现逻辑的一环)，这是扫回逻辑
            case CONFIG_UPDATE:
                agentMsgHandlerService.handleConfigUpdate(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // docker registry secret失败2;状态同步
            case OPERATE_DOCKER_REGISTRY_SECRET_FAILED:
                agentMsgHandlerService.operateDockerRegistrySecretResp(msg.getKey(), "failed", TypeUtil.objToLong(msg.getClusterId()));
                break;
            // docker registry secret成功状态同步
            case OPERATE_DOCKER_REGISTRY_SECRET:
                agentMsgHandlerService.operateDockerRegistrySecretResp(msg.getKey(), "success", TypeUtil.objToLong(msg.getClusterId()));
                break;
            // 接收Agent定时发送的Pod实时数据(按照环境(namespace)发送)
            case POD_METRICS_SYNC:
                agentMsgHandlerService.handlePodMetricsSync(msg.getKey(),msg.getPayload(),TypeUtil.objToLong(msg.getClusterId()));
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
