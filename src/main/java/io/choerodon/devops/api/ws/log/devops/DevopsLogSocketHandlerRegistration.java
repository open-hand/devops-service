package io.choerodon.devops.api.ws.log.devops;

import io.choerodon.devops.api.ws.DevopsExecAndLogSocketHandler;
import io.choerodon.websocket.connect.SocketHandlerRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/7.
 */
@Component
public class DevopsLogSocketHandlerRegistration implements SocketHandlerRegistration {

    @Autowired
    DevopsExecAndLogSocketHandler devopsExecAndLogSocketHandler;

    @Override
    public String path() {
        return "/devops/log";
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        devopsExecAndLogSocketHandler.beforeHandshake(serverHttpRequest,serverHttpResponse);
        return true;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        devopsExecAndLogSocketHandler.afterConnectionEstablished(webSocketSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        devopsExecAndLogSocketHandler.afterConnectionClosed(webSocketSession,closeStatus);
    }
}
