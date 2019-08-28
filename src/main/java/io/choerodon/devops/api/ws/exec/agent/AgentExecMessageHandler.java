package io.choerodon.devops.api.ws.exec.agent;

import io.choerodon.devops.api.ws.exec.ExecMessageHandler;
import io.choerodon.websocket.receive.BinaryMessageHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/19.
 */

@Component
public class AgentExecMessageHandler implements BinaryMessageHandler {

    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {
        ExecMessageHandler execMessageHandler = new ExecMessageHandler();
        execMessageHandler.handle(webSocketSession, message);
    }

    @Override
    public String matchPath() {
        return "/agent/exec";
    }

}
