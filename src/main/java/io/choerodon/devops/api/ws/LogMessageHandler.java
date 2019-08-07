package io.choerodon.devops.api.ws;


import java.io.IOException;
import java.util.Set;

import io.choerodon.websocket.receive.MessageHandler;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/7/26.
 */
@Component
public class LogMessageHandler implements MessageHandler<BinaryMessage> {


    private static final Logger logger = LoggerFactory.getLogger(LogMessageHandler.class);

    @Autowired
    private DefaultRelationshipDefining defaultRelationshipDefining;


    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        Set<WebSocketSession> webSocketSessions = defaultRelationshipDefining.getWebSocketSessionsByKey(WebSocketTool.getAttribute(webSocketSession).get("key").toString());
        for (WebSocketSession session : webSocketSessions) {
            synchronized (session) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    logger.warn("error.messageOperator.sendWebSocket.IOException, message: {}", message, e);
                }
            }
        }
    }

    @Override
    public String matchPath() {
        return "/ws/log";
    }



}
