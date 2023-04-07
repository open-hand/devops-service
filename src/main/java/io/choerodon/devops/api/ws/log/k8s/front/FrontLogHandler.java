package io.choerodon.devops.api.ws.log.k8s.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.FRONT_LOG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.DevopsExecAndLogSocketHandler;
import io.choerodon.devops.api.ws.log.k8s.LogMessageHandler;

/**
 * @author zmf
 * @since 20-5-8
 */
@Component
public class FrontLogHandler extends AbstractSocketHandler {
    @Autowired
    private DevopsExecAndLogSocketHandler devopsExecAndLogSocketHandler;
    @Autowired
    private LogMessageHandler logMessageHandler;

    @Override
    public String processor() {
        return FRONT_LOG;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        devopsExecAndLogSocketHandler.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        devopsExecAndLogSocketHandler.afterConnectionClosed(session, status);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        logMessageHandler.handle(session, message, LogMessageHandler.VIEW_LOG);
    }
}
