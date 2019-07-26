package io.choerodon.devops.infra.ws;


import java.nio.ByteBuffer;
import java.util.Set;

import io.choerodon.websocket.helper.WebSocketHelper;
import io.choerodon.websocket.receive.MessageHandler;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import org.slf4j.*;
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
    private WebSocketHelper webSocketHelper;

    @Autowired
    private DefaultRelationshipDefining defaultRelationshipDefining;


    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);

        Set<WebSocketSession> webSocketSessions = defaultRelationshipDefining.getWebSocketSessionsByKey(WebSocketTool.getAttribute(webSocketSession).get("key").toString());
        for (WebSocketSession session : webSocketSessions) {
            synchronized (session) {
                webSocketHelper.sendBinaryMessageBySession(session, message);
            }
        }
    }

    @Override
    public String matchPath() {
        return "/ws/log";
    }


}
