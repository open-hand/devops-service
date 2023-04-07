package io.choerodon.devops.api.ws.log.host.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.HOST_FRONT_LOG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.log.host.CommonHostAgentLogMessageHandler;
import io.choerodon.devops.api.ws.log.host.CommonHostAgentLogSocketHandler;

/**
 * @author zmf
 * @since 20-5-8
 */
@Component
public class HostFrontLogHandler extends AbstractSocketHandler {
    @Autowired
    private CommonHostAgentLogSocketHandler commonHostAgentLogSocketHandler;
    @Autowired
    private CommonHostAgentLogMessageHandler logMessageHandler;

    @Override
    public String processor() {
        return HOST_FRONT_LOG;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        commonHostAgentLogSocketHandler.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        commonHostAgentLogSocketHandler.afterHostFrontConnectionClosed(session, status);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        logMessageHandler.handle(session, message, CommonHostAgentLogMessageHandler.VIEW_LOG);
    }
}
