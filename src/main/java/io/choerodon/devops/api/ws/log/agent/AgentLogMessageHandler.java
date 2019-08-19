package io.choerodon.devops.api.ws.log.agent;

import io.choerodon.devops.api.ws.log.LogMessageHandler;
import io.choerodon.websocket.receive.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/19.
 */
@Component
public class AgentLogMessageHandler implements MessageHandler<BinaryMessage> {

    @Autowired
    LogMessageHandler logMessageHandler;


    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {
        logMessageHandler.handle(webSocketSession,message);
    }

    @Override
    public String matchPath() {
        return "/agent/log";
    }

}
