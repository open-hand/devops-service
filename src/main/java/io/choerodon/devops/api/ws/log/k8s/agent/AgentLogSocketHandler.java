package io.choerodon.devops.api.ws.log.k8s.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_LOG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;
import io.choerodon.devops.api.ws.log.k8s.LogMessageHandler;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class AgentLogSocketHandler extends AbstractSocketHandler {
    @Autowired
    private AgentExecAndLogSocketHandler agentExecAndLogSocketHandler;
    @Autowired
    private LogMessageHandler logMessageHandler;

    @Override
    public String processor() {
        return AGENT_LOG;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        agentExecAndLogSocketHandler.afterConnectionEstablished(session);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        logMessageHandler.handle(session, message, LogMessageHandler.VIEW_LOG);
    }
}
