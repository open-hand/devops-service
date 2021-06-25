package io.choerodon.devops.api.ws.host;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.infra.handler.HostConnectionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Map;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.HOST_AGENT;

@Component
public class HostAgentSocketInterceptor extends AbstractSocketInterceptor {

    @Autowired
    private HostConnectionHandler hostConnectionHandler;


    @Override
    public String processor() {
        return HOST_AGENT;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        return hostConnectionHandler.validConnectionParameter(servletRequest.getServletRequest());
    }
}
