package io.choerodon.devops.api.ws.log.k8s.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_DOWNLOAD_LOG;
import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.INSTANCE_ID;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;
import io.choerodon.devops.infra.util.EurekaInstanceUtil;

@Component
public class AgentDownloadLogSocketInterceptor extends AbstractSocketInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentDownloadLogSocketInterceptor.class);
    @Autowired
    private AgentExecAndLogSocketHandler agentExecAndLogSocketHandler;

    @Override
    public String processor() {
        return AGENT_DOWNLOAD_LOG;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String expectInstanceId = attributes.get(INSTANCE_ID).toString();
        LOGGER.info("establishing websocket connection. current: {} , expect: {}.", EurekaInstanceUtil.getInstanceId(), expectInstanceId);
        // 校验当前实例否是为agent期望建立连接的pod，如果不是返回false
        if (!EurekaInstanceUtil.getInstanceId().equals(expectInstanceId)) {
            LOGGER.info("refuse to established websocket connection. current: {} , expect: {}.", EurekaInstanceUtil.getInstanceId(), expectInstanceId);
            return false;
        }

        return agentExecAndLogSocketHandler.beforeHandshake(request, response, attributes);
    }
}
