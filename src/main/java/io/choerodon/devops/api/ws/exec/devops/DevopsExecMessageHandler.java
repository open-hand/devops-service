package io.choerodon.devops.api.ws.exec.devops;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.exec.ExecMessageHandler;
import io.choerodon.websocket.receive.BinaryMessageHandler;

/**
 * Created by Sheep on 2019/7/26.
 */
@Component
public class DevopsExecMessageHandler implements BinaryMessageHandler {
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
        return "/devops/exec";
    }
}
