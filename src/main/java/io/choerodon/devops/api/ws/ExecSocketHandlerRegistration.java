package io.choerodon.devops.api.ws;

import io.choerodon.websocket.helper.SocketHandlerRegistration;
import jdk.jfr.events.SocketReadEvent;
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
public class ExecSocketHandlerRegistration implements SocketHandlerRegistration {

    @Autowired
    ExecAndLogSocketHandler execAndLogSocketHandler;

    @Override
    public String path() {
        return "/ws/exec";
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        execAndLogSocketHandler.beforeHandshake(serverHttpRequest,serverHttpResponse);
        return true;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        execAndLogSocketHandler.afterConnectionEstablished(webSocketSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        execAndLogSocketHandler.afterConnectionClosed(webSocketSession,closeStatus);
    }
}
