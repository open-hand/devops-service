package io.choerodon.devops.api.ws.log.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_DOWNLOAD_LOG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;
import io.choerodon.devops.api.ws.log.LogMessageHandler;

@Component
public class AgentDownloadLogSocketHandler extends AbstractSocketHandler {

    @Autowired
    private AgentExecAndLogSocketHandler agentExecAndLogSocketHandler;
    @Autowired
    private LogMessageHandler logMessageHandler;

    @Override
    public String processor() {
        return AGENT_DOWNLOAD_LOG;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        agentExecAndLogSocketHandler.afterConnectionEstablished(session);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        logMessageHandler.handle(session, message, LogMessageHandler.DOWNLOAD_LOG);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        agentExecAndLogSocketHandler.afterConnectionClosed(session, status);
    }
}
