package io.choerodon.devops.api.ws.compatibility;

import java.util.Map;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import io.choerodon.devops.infra.handler.ClusterConnectionHandler;

/**
 * @author zmf
 * @since 20-5-11
 */
@Component
public class ElderAgentSocketInterceptor implements HandshakeInterceptor {
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

        // 校验连接参数
        boolean valid = clusterConnectionHandler.validElderAgentGitOpsParameters(servletRequest.getServletRequest());
        if (valid) {
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            // 将所有路径参数添加到attributes
            for (Map.Entry<String, String[]> entry : serverHttpRequest.getServletRequest().getParameterMap().entrySet()) {
                attributes.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        return valid;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {

    }
}
