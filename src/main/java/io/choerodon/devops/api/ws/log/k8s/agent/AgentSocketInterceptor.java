package io.choerodon.devops.api.ws.log.k8s.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_LOG;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class AgentSocketInterceptor extends AbstractSocketInterceptor {
    @Autowired
    private AgentExecAndLogSocketHandler agentExecAndLogSocketHandler;

    @Override
    public String processor() {
        return AGENT_LOG;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return agentExecAndLogSocketHandler.beforeHandshake(request, response, attributes);
    }
}
