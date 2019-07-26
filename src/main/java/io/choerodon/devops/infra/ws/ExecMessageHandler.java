package io.choerodon.devops.infra.ws;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import io.choerodon.websocket.receive.MessageHandler;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/7/26.
 */

@Component
public class ExecMessageHandler implements MessageHandler<BinaryMessage> {


    private static final Logger logger = LoggerFactory.getLogger(ExecMessageHandler.class);


    @Autowired
    private DefaultRelationshipDefining defaultRelationshipDefining;


    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);
        String msg = WebSocketTool.replaceR(new StringBuilder(new String(bytesArray, StandardCharsets.UTF_8)), 0);

        Set<WebSocketSession> webSocketSessions = defaultRelationshipDefining.getWebSocketSessionsByKey(WebSocketTool.getAttribute(webSocketSession).get("key").toString());
        for (WebSocketSession session : webSocketSessions) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(msg));
                }
            } catch (IOException e) {
                logger.warn("error.messageOperator.sendWebSocket.IOException, message: {}", message, e);
            }
        }
    }

    @Override
    public String matchPath() {
        return "/ws/exec";
    }

}
