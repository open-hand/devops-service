package io.choerodon.devops.api.ws.gitops;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;

import io.choerodon.devops.api.vo.ClusterSessionVO;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.util.KeyParseUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.connect.SocketHandlerRegistration;
import io.choerodon.websocket.helper.WebSocketHelper;
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
import org.springframework.web.socket.server.HandshakeFailureException;

/**
 * Created by Sheep on 2019/7/25.
 */

@Component
public class AgentGitOpsSocketHandlerRegistration implements SocketHandlerRegistration {

    private static final String CLUSTER_SESSION = "cluster-sessions-catch";
    private static final Logger logger = LoggerFactory.getLogger(AgentGitOpsSocketHandlerRegistration.class);
    private ConcurrentHashMap<String, Map<String, Object>> attributes = new ConcurrentHashMap<>();
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
        HttpServletRequest request = servletRequest.getServletRequest();

        //校验ws连接参数是否正确
        String key = request.getParameter("key");
        String clusterId = request.getParameter("clusterId");
        String token = request.getParameter("token");
        String version = request.getParameter("version");
        if (key == null || key.trim().isEmpty()) {
            throw new RuntimeException("Key is null");
        }
        if (!KeyParseUtil.matchPattern(key)) {
            throw new RuntimeException("Key not match the pattern");
        }
        if (clusterId == null || clusterId.trim().isEmpty()) {
            throw new RuntimeException("ClusterId is null");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token is null");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new RuntimeException("Version is null");
        }
        //检验连接过来的agent和集群是否匹配
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(TypeUtil.objToLong(clusterId));
        if (devopsClusterDTO == null || !token.equals(devopsClusterDTO.getToken())) {
            throw new HandshakeFailureException("agent token not match");
        }

        //校验是否已经有关联该key的agent连接到了devops,则删除已有key,连接之后建立新的关系
        Map<String, ClusterSessionVO> clusterSessions = (Map<String, ClusterSessionVO>) (Map) redisTemplate.opsForHash().entries(CLUSTER_SESSION);
        if (clusterSessions.getOrDefault(key, null) != null) {
            redisTemplate.opsForHash().delete(CLUSTER_SESSION, key);
        }
        return true;
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


        ClusterSessionVO clusterSession = new ClusterSessionVO();
        clusterSession.setClusterId(TypeUtil.objToLong(attribute.get("clusterId")));
        clusterSession.setVersion(TypeUtil.objToString(attribute.get("version")));
        clusterSession.setRegisterKey(registerKey);
        redisTemplate.opsForHash().put(CLUSTER_SESSION, clusterSession.getRegisterKey(), clusterSession);

        //连接成功之后,如果agent版本不匹配则提示升级agent,匹配则返回集群下关联环境的ssh信息
        Long clusterId = (TypeUtil.objToLong(attribute.get("clusterId")));
        List<Long> notUpgraded = clusterConnectionHandler.getUpdatedEnvList();
        if (!notUpgraded.contains(clusterId)) {
            DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
            agentCommandService.upgradeCluster(devopsClusterDTO);
        } else {
            //将已连接的agent集群信息放到redis中,用于判断集群是否连接
            agentCommandService.initCluster(clusterId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        Map<String, Object> attribute = attributes.get(webSocketSession.getId());
        String registerKey = TypeUtil.objToString(attribute.get("key"));

        //移除关联关系
        redisTemplate.opsForHash().delete(CLUSTER_SESSION, registerKey);
        try {
            webSocketSession.close();
        } catch (IOException e) {
            logger.warn("close clean timeout session failed {}", e.getMessage());
        }

    }

    @Scheduled(initialDelay = 10 * 1000, fixedRate = 10 * 1000)
    public void sendPing() {
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
