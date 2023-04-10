package io.choerodon.devops.api.ws.gitops;

import static io.choerodon.devops.infra.handler.ClusterConnectionHandler.CLUSTER_SESSION;
import static org.hzero.websocket.constant.WebSocketConstant.Attributes.GROUP;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hzero.websocket.redis.BrokerSessionRedis;
import org.hzero.websocket.registry.BaseSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.AgentMsgVO;
import io.choerodon.devops.api.vo.ClusterSessionVO;
import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.AgentMsgHandlerService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.app.service.SendNotificationService;
import io.choerodon.devops.infra.constant.DevOpsWebSocketConstants;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.HelmType;
import io.choerodon.devops.infra.enums.InstanceStatus;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.KeyParseUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * @author zmf
 * @since 20-5-8
 */
@Component
public class AgentGitOpsSocketHandler extends AbstractSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentGitOpsSocketHandler.class);


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DevopsClusterService devopsClusterService;

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    private AgentCommandService agentCommandService;

    @Autowired
    private SendNotificationService sendNotificationService;

    @Autowired
    private AgentMsgHandlerService agentMsgHandlerService;

    @Override
    public String processor() {
        return DevOpsWebSocketConstants.AGENT;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 就是agent连接时应该传入的group参数，形如  front_agent:clusterId:21
        String group = WebSocketTool.getGroup(session);

        //将已连接的agent集群信息放到redis中,用于判断集群是否连接
        ClusterSessionVO clusterSession = new ClusterSessionVO();
        clusterSession.setWebSocketSessionId(session.getId());
        Long clusterId = WebSocketTool.getClusterId(session);
        clusterSession.setClusterId(clusterId);
        clusterSession.setVersion(WebSocketTool.getVersion(session));
        clusterSession.setRegisterKey(group);
        redisTemplate.opsForHash().put(CLUSTER_SESSION, clusterSession.getRegisterKey(), clusterSession);

        // 连接成功之后,如果agent版本不匹配则提示升级agent,匹配则返回集群下关联环境的ssh信息
        List<Long> unnecessaryToUpgrade = clusterConnectionHandler.getUpdatedClusterList();
        if (!unnecessaryToUpgrade.contains(clusterId)) {
            DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
            LOGGER.info("New Upgrade agent: upgrade agent with cluster id {} from version {}", clusterId, clusterSession.getVersion());
            agentCommandService.newUpgradeCluster(devopsClusterDTO, session);
        } else {
            LOGGER.info("Init agent: init agent with cluster id {} and version {}", clusterId, clusterSession.getVersion());
            agentCommandService.initCluster(clusterId, session);
            //集群链接成功发送web hook
            DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
            sendNotificationService.sendWhenActivateCluster(devopsClusterDTO);
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String registerKey = WebSocketTool.getGroup(session);

        removeRedisValueByRegisterKeyAndSessionId(registerKey, session.getId(), WebSocketTool.getClusterId(session));

        LOGGER.info("After connection closed, the cluster session with key {} is to be closed.", registerKey);
        WebSocketTool.closeSessionQuietly(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            doHandle(session, message);
            // 将异常捕获，否则未捕获的异常会导致外层的WebSocket框架关闭这个和Agent的连接
        } catch (Exception ex) {
            LOGGER.warn("Handle Agent Message: an unexpected exception occurred", ex);
        }
    }

    private void doHandle(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        AgentMsgVO msg = JsonHelper.unmarshalByJackson(payload, AgentMsgVO.class);
        HelmType helmType = HelmType.forValue(String.valueOf(msg.getType()));

        if (helmType == null) {
            LOGGER.info("找不到指令啊 {}", msg.getType());
            return;
        }

        LOGGER.debug("AgentGitOps: helm type: {}, msg: {}", helmType.value, msg);

        //设置集群id
        msg.setClusterId(TypeUtil.objToString(getClusterIdFromRegisterKey(TypeUtil.objToString(session.getAttributes().get(GROUP)))));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg.toString());
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
                LOGGER.debug("helm_install_resource: {}", msg);
                agentMsgHandlerService.helmInstallResourceInfo(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            // helm release更新时包中的相关资源(除去JOB)信息同步
            case HELM_UPGRADE_RESOURCE_INFO:
                LOGGER.debug("helm_update_resource: {}", msg);
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
                agentMsgHandlerService.updateStartOrStopInstanceStatus(
                        msg.getKey(),
                        KeyParseUtil.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.RUNNING.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        msg.getPayload());
                break;
            // 更新实例停止的状态
            case HELM_RELEASE_STOP:
                agentMsgHandlerService.updateStartOrStopInstanceStatus(
                        msg.getKey(),
                        KeyParseUtil.getResourceName(msg.getKey()),
                        TypeUtil.objToLong(msg.getClusterId()),
                        InstanceStatus.STOPPED.getStatus(),
                        CommandStatus.SUCCESS.getStatus(),
                        msg.getPayload());
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
                agentMsgHandlerService.helmJobEvent(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case HELM_POD_EVENT:
                agentMsgHandlerService.helmPodEvent(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case WORKLOAD_POD_EVENT:
                agentMsgHandlerService.workloadPodEvent(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
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
            case CERT_MANAGER_STATUS:
                agentMsgHandlerService.handleCertManagerInfo(msg, TypeUtil.objToLong(msg.getClusterId()));
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
                agentMsgHandlerService.handlePodMetricsSync(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()));
                break;
            case CLUSTER_INFO:
                LOGGER.info("Cluster info: {}", msg);
                agentMsgHandlerService.handleClusterInfo(msg);
                break;
            case DELETE_POD:
                agentMsgHandlerService.handleDeletePod(Long.parseLong(msg.getClusterId()), msg.getPayload());
                break;
            case OPERATE_POD_COUNT_SUCCEED:
                agentMsgHandlerService.operatePodCount(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()), true);
                break;
            case OPERATE_POD_COUNT_FAILED:
                agentMsgHandlerService.operatePodCount(msg.getKey(), msg.getPayload(), TypeUtil.objToLong(msg.getClusterId()), false);
                break;
            default:
                LOGGER.warn("UnExpected message type {}", msg.getType());
                break;
        }
    }

    private void removeRedisValueByRegisterKeyAndSessionId(String registerKey, String sessionId, Object clusterId) {
        Object registerKeyValue = redisTemplate.opsForHash().get(CLUSTER_SESSION, registerKey);
        removeRedisValueByRegisterKeyAndSessionId(registerKey, sessionId, registerKeyValue, clusterId);
    }

    private void removeRedisValueByRegisterKeyAndSessionId(String registerKey, String sessionId, Object registerKeyValue, Object clusterId) {
        if (registerKeyValue != null) {
            if (registerKeyValue instanceof ClusterSessionVO) {
                ClusterSessionVO clusterSessionVO = (ClusterSessionVO) registerKeyValue;
                // 只有这个registerKey的值中webSocketSessionId和当前sessionId一致时才删除key，避免旧的超时的连接
                // 误将新连接的key删掉（两者是同一个key）
                if (Objects.equals(sessionId, clusterSessionVO.getWebSocketSessionId())) {
                    //移除关联关系
                    redisTemplate.opsForHash().delete(CLUSTER_SESSION, registerKey);
                } else {
                    LOGGER.info("This is an elder session whose registerKey value was updated by a new session. the session cluster id is {}", clusterId);
                }
            } else {
                // 这个逻辑不应该进的
                LOGGER.warn("Value of register key is not of Class 'io.choerodon.devops.api.vo.ClusterSessionVO', and its real class is {}", registerKeyValue.getClass());
                redisTemplate.opsForHash().delete(CLUSTER_SESSION, registerKey);
            }
        }
    }

    private void doRemoveRedisKeyOfThisMicroService() {
        // 获取本实例所有的连接的web socket session 的 session id
        // （这里获取的是包括所有通过group方式连接的，也就是包括前端以及agent的）
        List<String> sessionIds = BrokerSessionRedis.getSessionIds(BaseSessionRegistry.getBrokerId());

        // 获取集群连接情况数据
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(CLUSTER_SESSION);
        entries.forEach((k, v) -> {
            if (sessionIds.contains(((ClusterSessionVO) v).getWebSocketSessionId())) {
                String registerKey = TypeUtil.objToString(k);
                // 清除这个实例的集群连接数据
                removeRedisValueByRegisterKeyAndSessionId(registerKey, ((ClusterSessionVO) v).getWebSocketSessionId(), v, getClusterIdFromRegisterKey(registerKey));
            }
        });
        // 清除这个实例的redis key
        BrokerSessionRedis.clearCache(BaseSessionRegistry.getBrokerId());
    }

    private Long getClusterIdFromRegisterKey(String registerKey) {
        String[] pairs = registerKey.split("\\.");
        String[] content = pairs[0].split(":");
        return TypeUtil.objToLong(content[1]);
    }

    /**
     * 释放这个微服务实例所持有的redis的键
     */
    public void removeRedisKeyOfThisMicroService() {
        LOGGER.info("The agent connection information in redis of this devops-service instance is to be removed.");
        doRemoveRedisKeyOfThisMicroService();
        LOGGER.info("The agent connection information in redis of this devops-service instance was successfully removed.");
    }
}
