package io.choerodon.devops.api.ws.gitops;

import static io.choerodon.devops.infra.handler.ClusterConnectionHandler.CLUSTER_SESSION;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.ClusterSessionVO;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.connect.SocketHandlerRegistration;
import io.choerodon.websocket.helper.WebSocketHelper;

/**
 * Created by Sheep on 2019/7/25.
 */

@Component
public class AgentGitOpsSocketHandlerRegistration implements SocketHandlerRegistration {

    private static final String CLUSTER_ID = "clusterId";
    private static final Logger logger = LoggerFactory.getLogger(AgentGitOpsSocketHandlerRegistration.class);
    // TODO 这个是否导致内存泄露？
    private ConcurrentHashMap<String, Map<String, Object>> attributes = new ConcurrentHashMap<>();
    // TODO 是否需要考虑并发？
    private Set<WebSocketSession> webSocketSessions = new HashSet<>();


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DevopsClusterService devopsClusterService;

    @Autowired
    @Lazy
    private WebSocketHelper webSocketHelper;

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    private AgentCommandService agentCommandService;


    @Override
    public String path() {
        return "/agent/";
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;

        return clusterConnectionHandler.validConnectionParameter(servletRequest.getServletRequest());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        webSocketSessions.add(webSocketSession);

        //解析参数列表，并存储
        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);
        attributes.put(webSocketSession.getId(), attribute);

        String registerKey = TypeUtil.objToString(attribute.get("key"));

        //将websocketSession和关联的key做关联
        webSocketHelper.subscribe(registerKey, webSocketSession);


        //将已连接的agent集群信息放到redis中,用于判断集群是否连接
        ClusterSessionVO clusterSession = new ClusterSessionVO();
        clusterSession.setWebSocketSessionId(webSocketSession.getId());
        clusterSession.setClusterId(TypeUtil.objToLong(attribute.get(CLUSTER_ID)));
        clusterSession.setVersion(TypeUtil.objToString(attribute.get("version")));
        clusterSession.setRegisterKey(registerKey);
        redisTemplate.opsForHash().put(CLUSTER_SESSION, clusterSession.getRegisterKey(), clusterSession);

        //连接成功之后,如果agent版本不匹配则提示升级agent,匹配则返回集群下关联环境的ssh信息
        Long clusterId = (TypeUtil.objToLong(attribute.get(CLUSTER_ID)));
        List<Long> notUpgraded = clusterConnectionHandler.getUpdatedClusterList();
        if (!notUpgraded.contains(clusterId)) {
            DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
            logger.info("Upgrade agent: upgrade agent with cluster id {} from version {}", clusterId, clusterSession.getVersion());
            agentCommandService.upgradeCluster(devopsClusterDTO);
        } else {
            logger.info("Init agent: init agent with cluster id {} and version {}", clusterId, clusterSession.getVersion());
            agentCommandService.initCluster(clusterId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        Map<String, Object> attribute = attributes.get(webSocketSession.getId());
        String registerKey = TypeUtil.objToString(attribute.get("key"));

        removeRedisValueByRegisterKeyAndSessionId(registerKey, webSocketSession.getId(), TypeUtil.objToLong(attribute.get(CLUSTER_ID)));

        logger.info("After connection closed, the cluster session with key {} is to be closed.", registerKey);
        try {
            webSocketSession.close();
            webSocketSessions.remove(webSocketSession);
        } catch (IOException e) {
            logger.warn("close clean timeout session failed {}", e.getMessage());
        }
    }

    private void removeRedisValueByRegisterKeyAndSessionId(String registerKey, String sessionId, Object clusterId) {
        Object registerKeyValue = redisTemplate.opsForHash().get(CLUSTER_SESSION, registerKey);
        if (registerKeyValue != null) {
            if (registerKeyValue instanceof ClusterSessionVO) {
                ClusterSessionVO clusterSessionVO = (ClusterSessionVO) registerKeyValue;
                // 只有这个registerKey的值中webSocketSessionId和当前sessionId一致时才删除key，避免旧的超时的连接
                // 误将新连接的key删掉（两者是同一个key）
                if (Objects.equals(sessionId, clusterSessionVO.getWebSocketSessionId())) {
                    //移除关联关系
                    redisTemplate.opsForHash().delete(CLUSTER_SESSION, registerKey);
                } else {
                    logger.info("This is an elder session whose registerKey value was updated by a new session. the session cluster id is {}", clusterId);
                }
            } else {
                // 这个逻辑不应该进的
                logger.warn("Value of register key is not of Class 'io.choerodon.devops.api.vo.ClusterSessionVO', and its real class is {}", registerKeyValue.getClass());
                redisTemplate.opsForHash().delete(CLUSTER_SESSION, registerKey);
            }
        }
    }


    private void doRemoveRedisKeyOfThisMicroService() {
        List<String> sessionIds = webSocketSessions.stream().map(WebSocketSession::getId).collect(Collectors.toList());
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(CLUSTER_SESSION);
        entries.forEach((k, v) -> {
            if (sessionIds.contains(((ClusterSessionVO) v).getWebSocketSessionId())) {
                String registerKey = TypeUtil.objToString(k);
                removeRedisValueByRegisterKeyAndSessionId(registerKey, ((ClusterSessionVO) v).getWebSocketSessionId(), getClusterIdFromRegisterKey(registerKey));
            }
        });

        webSocketSessions.clear();
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
        logger.info("The agent connection information in redis of this devops-service instance is to be removed.");
        doRemoveRedisKeyOfThisMicroService();
        logger.info("The agent connection information in redis of this devops-service instance was successfully removed.");
    }

    /**
     * DevOps会定时发送Ping消息以保持连接。目前由于基础包的问题（2019/10/11），
     * 并没有对返回的Pong消息进行处理，（Agent发送的Ping消息DevOps没有处理）
     * 目前能够对连接状态判断，可能是如果Agent挂了，WebSocket会自动断开，我们这边能收到断开的事件。
     */
    @Scheduled(initialDelay = 10 * 1000, fixedRate = 10 * 1000)
    public void sendPing() {
        logger.info("Sending ping message...");
        for (WebSocketSession session : webSocketSessions) {
            try {
                session.sendMessage(new PingMessage());
            } catch (Exception e) {
                webSocketSessions.remove(session);
                afterConnectionClosed(session, new CloseStatus(500));
                logger.error("remove disconnected agent!");
            }
        }
    }
}
