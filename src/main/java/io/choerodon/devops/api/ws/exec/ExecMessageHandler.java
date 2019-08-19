package io.choerodon.devops.api.ws.exec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/19.
 */

@Component
public class ExecMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExecMessageHandler.class);
    private static final String EXEC_LOG_SESSIONS_CATCH = "exec_log_sessions_catch";


    @Autowired
    private DefaultRelationshipDefining defaultRelationshipDefining;
    @Autowired
    private RedisTemplate<String, Object>  redisTemplate;

    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);
        String msg = WebSocketTool.replaceR(new StringBuilder(new String(bytesArray, StandardCharsets.UTF_8)), 0);

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));
        List<WebSocketSession> webSocketSessions = (List<WebSocketSession>) redisTemplate.opsForHash().get(EXEC_LOG_SESSIONS_CATCH, registerKey);
        for (WebSocketSession session : webSocketSessions) {
            //同时会有2个ws关联同一个exec的key,此时只需要发给对方即可
            if (!session.equals(webSocketSession)) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(msg));
                    }
                } catch (IOException e) {
                    logger.warn("error.messageOperator.sendWebSocket.IOException, message: {}", message, e);
                }
            }
        }
    }
}
