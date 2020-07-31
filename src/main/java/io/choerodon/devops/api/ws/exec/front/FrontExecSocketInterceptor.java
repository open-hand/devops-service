package io.choerodon.devops.api.ws.exec.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.FRONT_EXEC;

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
 * @since 20-5-9
 */
@Component
public class FrontExecSocketInterceptor extends AbstractSocketInterceptor {
    @Autowired
    private DevopsExecAndLogSocketHandler devopsExecAndLogSocketHandler;

    @Override
    public String processor() {
        return FRONT_EXEC;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        WebSocketTool.preProcessAttributeAboutKeyEncryption(attributes);
        return devopsExecAndLogSocketHandler.beforeHandshake(request, response, attributes);
    }
}
