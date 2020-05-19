package io.choerodon.devops.api.ws.gitops;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.infra.constant.DevOpsWebSocketConstants;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;

/**
 * @author zmf
 * @since 20-5-8
 */
@Component
public class AgentGitOpsSocketHandlerInterceptor extends AbstractSocketInterceptor {
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;


    @Override
    public String processor() {
        return DevOpsWebSocketConstants.AGENT;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        return clusterConnectionHandler.validConnectionParameter(servletRequest.getServletRequest());
    }
}
