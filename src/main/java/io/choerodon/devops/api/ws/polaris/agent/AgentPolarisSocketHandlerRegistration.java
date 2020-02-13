package io.choerodon.devops.api.ws.polaris.agent;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.websocket.connect.SocketHandlerRegistration;


@Component
public class AgentPolarisSocketHandlerRegistration implements SocketHandlerRegistration {
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Override
    public String path() {
        return "/agent/polaris";
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        return clusterConnectionHandler.validConnectionParameter((HttpServletRequest) serverHttpRequest);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }
}
