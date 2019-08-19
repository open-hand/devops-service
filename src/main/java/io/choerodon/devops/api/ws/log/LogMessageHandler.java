package io.choerodon.devops.api.ws.log;


import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/7/26.
 */
@Component
public class LogMessageHandler {


    private static final Logger logger = LoggerFactory.getLogger(LogMessageHandler.class);
    private static final String EXEC_LOG_SESSIONS_CATCH = "exec_log_sessions_catch";


    @Autowired
    private DefaultRelationshipDefining defaultRelationshipDefining;
    @Autowired
    private RedisTemplate<String, Object>  redisTemplate;

    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));
        List<WebSocketSession> webSocketSessions = (List<WebSocketSession>) redisTemplate.opsForHash().get(EXEC_LOG_SESSIONS_CATCH, registerKey);
        for (WebSocketSession session : webSocketSessions) {
            if (session != webSocketSession) {
                synchronized (session) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        logger.warn("error.messageOperator.sendWebSocket.IOException, message: {}", message, e);
                    }
                }
            }
        }
    }

}
