package io.choerodon.devops.api.ws.log.k8s.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.FRONT_LOG;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.DevopsExecAndLogSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;

/**
 * @author zmf
 * @since 20-5-8
 */
@Component
public class FrontLogSocketInterceptor extends AbstractSocketInterceptor {
    @Autowired
    private DevopsExecAndLogSocketHandler devopsExecAndLogSocketHandler;

    @Override
    public String processor() {
        return FRONT_LOG;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 先校验token
        if (!WebSocketTool.preCheckOAuthToken(attributes)) {
            return false;
        }
        WebSocketTool.preProcessAttributeAboutKeyEncryption(attributes);
        return devopsExecAndLogSocketHandler.beforeHandshake(request, response, attributes);
    }
}
