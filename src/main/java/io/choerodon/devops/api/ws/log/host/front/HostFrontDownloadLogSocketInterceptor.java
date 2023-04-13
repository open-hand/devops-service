package io.choerodon.devops.api.ws.log.host.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.HOST_FRONT_DOWNLOAD_LOG;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.api.ws.log.host.CommonHostAgentLogSocketHandler;

@Component
public class HostFrontDownloadLogSocketInterceptor extends AbstractSocketInterceptor {
    @Autowired
    private CommonHostAgentLogSocketHandler devopsExecAndLogSocketHandler;

    @Override
    public String processor() {
        return HOST_FRONT_DOWNLOAD_LOG;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 先校验token
        if (!WebSocketTool.preCheckOAuthToken(attributes)) {
            return false;
        }
        WebSocketTool.preProcessAttributeAboutKeyEncryption(attributes);
        return devopsExecAndLogSocketHandler.beforeHandshake(request, response, attributes, HOST_FRONT_DOWNLOAD_LOG);
    }
}
