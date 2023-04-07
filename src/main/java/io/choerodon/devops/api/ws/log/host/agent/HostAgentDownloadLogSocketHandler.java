package io.choerodon.devops.api.ws.log.host.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.HOST_AGENT_DOWNLOAD_LOG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.log.host.CommonHostAgentLogMessageHandler;
import io.choerodon.devops.api.ws.log.host.CommonHostAgentLogSocketHandler;
import io.choerodon.devops.api.ws.log.k8s.LogMessageHandler;

@Component
public class HostAgentDownloadLogSocketHandler extends AbstractSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostAgentDownloadLogSocketHandler.class);

    @Autowired
    private CommonHostAgentLogSocketHandler agentExecAndLogSocketHandler;
    @Autowired
    private CommonHostAgentLogMessageHandler logMessageHandler;

    @Override
    public String processor() {
        return HOST_AGENT_DOWNLOAD_LOG;
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
        agentExecAndLogSocketHandler.afterHostAgentConnectionClosed(session, status);
    }
}
