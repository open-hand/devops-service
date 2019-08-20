package io.choerodon.devops.api.ws.exec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import io.choerodon.websocket.send.MessageSender;
import io.choerodon.websocket.send.WebSocketSendPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Autowired
    private DefaultRelationshipDefining defaultRelationshipDefining;
    @Autowired
    private MessageSender messageSender;


    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);
        String msg = WebSocketTool.replaceR(new StringBuilder(new String(bytesArray, StandardCharsets.UTF_8)), 0);

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));
        Set<WebSocketSession> webSocketSessions = defaultRelationshipDefining.getWebSocketSessionsByKey(registerKey);
        //判断当前实例关心该key的ws是否有2个，如果有2个，证明前端连devops,以及agent连devops的ws都在同一个实例，此时直接发消息即可
        if (webSocketSessions.size() == 2) {
            for (WebSocketSession session : webSocketSessions) {
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
        //如果当前实例关心该key的ws只有1个，证明同时关心该key的目标ws在其它实例中，此时需要借助redis的channel发送消息到其它实例
        else {
            Set<String> channels = defaultRelationshipDefining.getRedisChannelsByKey(registerKey, true);
            WebSocketSendPayload<String> execMessageWebSocketSendPayload = new WebSocketSendPayload<>();
            execMessageWebSocketSendPayload.setKey(registerKey);
            execMessageWebSocketSendPayload.setData(msg);
            execMessageWebSocketSendPayload.setType("AgentExec");
            if (channels != null && !channels.isEmpty()) {
                channels.forEach(channel -> messageSender.sendRedis(channel, execMessageWebSocketSendPayload));
            }
        }
    }
}
