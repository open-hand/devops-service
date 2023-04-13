package io.choerodon.devops.api.ws.log.host.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.HOST_FRONT_DOWNLOAD_LOG;

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
public class HostFrontDownloadLogHandler extends AbstractSocketHandler {
    @Autowired
    private CommonHostAgentLogSocketHandler devopsExecAndLogSocketHandler;
    @Autowired
    private CommonHostAgentLogMessageHandler logMessageHandler;

    @Override
    public String processor() {
        return HOST_FRONT_DOWNLOAD_LOG;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        devopsExecAndLogSocketHandler.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        devopsExecAndLogSocketHandler.afterHostFrontConnectionClosed(session, status);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        logMessageHandler.handle(session, message, LogMessageHandler.DOWNLOAD_LOG);
    }
}
