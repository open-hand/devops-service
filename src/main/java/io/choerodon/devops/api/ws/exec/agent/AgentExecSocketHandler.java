package io.choerodon.devops.api.ws.exec.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_EXEC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;
import io.choerodon.devops.api.ws.exec.ExecMessageHandler;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class AgentExecSocketHandler extends AbstractSocketHandler {
    @Autowired
    private ExecMessageHandler execMessageHandler;
    @Autowired
    private AgentExecAndLogSocketHandler agentExecAndLogSocketHandler;

    @Override
    public String processor() {
        return AGENT_EXEC;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        agentExecAndLogSocketHandler.afterConnectionEstablished(session);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        execMessageHandler.handle(session, message);
    }
}
