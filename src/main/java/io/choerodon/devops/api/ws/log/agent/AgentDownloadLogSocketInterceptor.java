package io.choerodon.devops.api.ws.log.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_DOWNLOAD_LOG;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;

@Component
public class AgentDownloadLogSocketInterceptor extends AbstractSocketInterceptor {
    @Autowired
    private AgentExecAndLogSocketHandler agentExecAndLogSocketHandler;

    @Override
    public String processor() {
        return AGENT_DOWNLOAD_LOG;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return agentExecAndLogSocketHandler.beforeHandshake(request, response, attributes);
    }
}
