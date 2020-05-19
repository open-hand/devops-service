package io.choerodon.devops.api.ws.exec.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.FRONT_EXEC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.DevopsExecAndLogSocketHandler;
import io.choerodon.devops.api.ws.exec.ExecMessageHandler;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class FrontExecSocketHandler extends AbstractSocketHandler {
    @Autowired
    private ExecMessageHandler execMessageHandler;
    @Autowired
    private DevopsExecAndLogSocketHandler devopsExecAndLogSocketHandler;

    @Override
    public String processor() {
        return FRONT_EXEC;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        devopsExecAndLogSocketHandler.afterConnectionEstablished(session);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        execMessageHandler.handle(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        devopsExecAndLogSocketHandler.afterConnectionClosed(session, status);
    }
}
