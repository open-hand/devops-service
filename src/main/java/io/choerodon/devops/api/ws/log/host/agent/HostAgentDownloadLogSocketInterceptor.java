package io.choerodon.devops.api.ws.log.host.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.HOST_AGENT_DOWNLOAD_LOG;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.log.host.CommonHostAgentLogSocketHandler;

@Component
public class HostAgentDownloadLogSocketInterceptor extends AbstractSocketInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostAgentDownloadLogSocketInterceptor.class);
    @Autowired
    private CommonHostAgentLogSocketHandler agentExecAndLogSocketHandler;

    @Override
    public String processor() {
        return HOST_AGENT_DOWNLOAD_LOG;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return agentExecAndLogSocketHandler.beforeHandshake(request, response, attributes, HOST_AGENT_DOWNLOAD_LOG);
    }
}
