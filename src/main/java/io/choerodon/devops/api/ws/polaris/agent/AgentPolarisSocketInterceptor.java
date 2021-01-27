package io.choerodon.devops.api.ws.polaris.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_POLARIS;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;

/**
 * // TODO to be delete at 0.25
 * polaris结果的返回不再通过websocket，而是http，这个逻辑不再使用了
 *
 * @author zmf
 * @since 20-5-9
 */
@Deprecated
@Component
public class AgentPolarisSocketInterceptor extends AbstractSocketInterceptor {
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Override
    public String processor() {
        return AGENT_POLARIS;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return clusterConnectionHandler.validConnectionParameter(((ServletServerHttpRequest) request).getServletRequest());
    }
}
