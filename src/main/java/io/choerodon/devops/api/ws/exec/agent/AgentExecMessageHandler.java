package io.choerodon.devops.api.ws.exec.agent;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.exec.ExecMessageHandler;
import io.choerodon.websocket.receive.BinaryMessageHandler;

/**
 * Created by Sheep on 2019/8/19.
 */

@Component
public class AgentExecMessageHandler implements BinaryMessageHandler {
    private ExecMessageHandler execMessageHandler;

    @PostConstruct
    private void init() {
        execMessageHandler = new ExecMessageHandler();
    }

    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {
        execMessageHandler.handle(webSocketSession, message);
    }

    @Override
    public String matchPath() {
        return "/agent/exec";
    }

}
