package io.choerodon.devops.api.ws.exec.agent;

import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;
import io.choerodon.websocket.helper.SocketHandlerRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/19.
 */
@Component
public class AgentExecSocketHandlerRegistration implements SocketHandlerRegistration {

    @Autowired
    AgentExecAndLogSocketHandler agentExecAndLogSocketHandler;


    @Override
    public String path() {
        return "/agent/exec";
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        return agentExecAndLogSocketHandler.beforeHandshake(serverHttpRequest, serverHttpResponse);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        agentExecAndLogSocketHandler.afterConnectionEstablished(webSocketSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
    }
}
