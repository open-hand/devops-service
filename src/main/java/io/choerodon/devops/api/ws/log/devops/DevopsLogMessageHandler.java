package io.choerodon.devops.api.ws.log.devops;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.log.LogMessageHandler;
import io.choerodon.websocket.receive.BinaryMessageHandler;

/**
 * Created by Sheep on 2019/8/19.
 */
@Component
public class DevopsLogMessageHandler implements BinaryMessageHandler {
    @Autowired
    private LogMessageHandler logMessageHandler;

    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {
        logMessageHandler.handle(webSocketSession, message);
    }

    @Override
    public String matchPath() {
        return "/devops/log";
    }


}
