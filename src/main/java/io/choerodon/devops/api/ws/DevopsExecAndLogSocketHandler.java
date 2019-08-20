package io.choerodon.devops.api.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import io.choerodon.devops.api.vo.PipeRequestVO;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.helper.WebSocketHelper;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import io.choerodon.websocket.send.MessageSender;
import io.choerodon.websocket.send.WebSocketSendPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeFailureException;

/**
 * Created by Sheep on 2019/7/25.
 */
@Component
public class DevopsExecAndLogSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DevopsExecAndLogSocketHandler.class);
    public static final String KUBERNETES_GET_LOGS = "kubernetes_get_logs";
    public static final String EXEC_COMMAND = "kubernetes_exec";


    @Autowired
    private WebSocketHelper webSocketHelper;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private DefaultRelationshipDefining defaultRelationshipDefining;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MessageSender messageSender;


    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
        HttpServletRequest request = servletRequest.getServletRequest();

        //校验ws连接参数是否正确
        String key = request.getParameter("key");
        String env = request.getParameter("env");
        String podName = request.getParameter("podName");
        String containerName = request.getParameter("containerName");
        String logId = request.getParameter("logId");
        if (key == null || key.trim().isEmpty()) {
            throw new HandshakeFailureException("Key is null");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new HandshakeFailureException("Key is null");
        }
        if (env == null || env.trim().isEmpty()) {
            throw new HandshakeFailureException("Env is null!");
        }
        if (podName == null || podName.trim().isEmpty()) {
            throw new HandshakeFailureException("PodName is null!");
        }
        if (containerName == null || containerName.trim().isEmpty()) {
            throw new HandshakeFailureException("ContainerName is null!");
        }
        if (logId == null || logId.trim().isEmpty()) {
            throw new HandshakeFailureException("LogId is null!");
        }

        return true;
    }

    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //解析参数列表，并存储


        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));
        //将websocketSession和关联的key做关联
        webSocketHelper.contact(webSocketSession, registerKey);

        //通知agent建立与前端同样的ws连接
        PipeRequestVO pipeRequest = new PipeRequestVO(attribute.get("podName").toString(), attribute.get("containerName").toString(), attribute.get("logId").toString(), attribute.get("env").toString());
        Long clusterId = TypeUtil.objToLong(registerKey.split("\\.")[0].split(":")[1]);
        if (webSocketSession.getUri().getPath().equals("/devops/log")) {
            agentCommandService.startLogOrExecConnection(KUBERNETES_GET_LOGS, registerKey, pipeRequest, clusterId);
        } else {
            agentCommandService.startLogOrExecConnection(EXEC_COMMAND, registerKey, pipeRequest, clusterId);
        }
    }

    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);
        String registerKey = TypeUtil.objToString(attribute.get("key"));

        //当时log或者exec类型的ws,断开devops前端到devops的ws时,同时需要断开与该ws对应的agent到devops的ws连接
        List<WebSocketSession> webSocketSessions = new ArrayList<>(defaultRelationshipDefining.getWebSocketSessionsByKey(registerKey));

        //如果session列表的数量为2,证明devops前端到devops以及agent到devops的ws都连在同一个实例，则直接close
        if (webSocketSessions.size() == 2) {
            //解决npe
            for (int i = 0; i < webSocketSessions.size(); i++) {
                WebSocketSession session = webSocketSessions.get(i);
                if(session!=webSocketSession) {
                    closeSession(session);
                }
            }
        }
        //如果session列表的数量为1,证明agent到devops的ws连在其它实例中，需要借助redis channel发送管理agent到devops的ws
        else {
            Set<String> channels = defaultRelationshipDefining.getRedisChannelsByKey(registerKey, true);
            WebSocketSendPayload<String> closeAgentSessionPayLoad = new WebSocketSendPayload<>();
            closeAgentSessionPayLoad.setKey(registerKey);
            closeAgentSessionPayLoad.setType("closeAgentSession");
            closeAgentSessionPayLoad.setData("");
            if (channels != null && !channels.isEmpty()) {
                channels.forEach(channel -> messageSender.sendRedis(channel, closeAgentSessionPayLoad));
            }
        }
        closeSession(webSocketSession);
    }

    private void closeSession(WebSocketSession session) {

        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
