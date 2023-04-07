package io.choerodon.devops.api.ws.log.k8s.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_DOWNLOAD_LOG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;
import io.choerodon.devops.api.ws.log.k8s.LogMessageHandler;

@Component
public class AgentDownloadLogSocketHandler extends AbstractSocketHandler {
    private static final Logger LOGGER= LoggerFactory.getLogger(AgentDownloadLogSocketHandler.class);

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
        LOGGER.info("log transfer completed");
        super.afterConnectionClosed(session, status);
        agentExecAndLogSocketHandler.afterConnectionClosed(session, status);
    }
}
