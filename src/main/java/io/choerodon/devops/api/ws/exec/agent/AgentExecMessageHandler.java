package io.choerodon.devops.api.ws.exec.agent;

import io.choerodon.devops.api.ws.exec.ExecMessageHandler;
import io.choerodon.websocket.receive.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/19.
 */

@Component
public class AgentExecMessageHandler implements MessageHandler<BinaryMessage> {


    @Autowired
    private ExecMessageHandler execMessageHandler;

    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {
        execMessageHandler.handle(webSocketSession,message);
    }

    @Override
    public String matchPath() {
        return "/agent/exec";
    }

}
