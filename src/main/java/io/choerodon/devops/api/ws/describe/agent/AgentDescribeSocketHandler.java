package io.choerodon.devops.api.ws.describe.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_DESCRIBE;

import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class AgentDescribeSocketHandler extends AbstractSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentDescribeSocketHandler.class);

    @Autowired
    private KeySocketSendHelper keySocketSendHelper;

    @Override
    public String processor() {
        return AGENT_DESCRIBE;
    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String agentSessionGroup = WebSocketTool.getGroup(session);
        LOGGER.info("Agent Describe: receive describe message from agent of group: {}", agentSessionGroup);
        String key = WebSocketTool.getKey(session);
        String frontSessionGroup = WebSocketTool.buildFrontGroup(key);

        keySocketSendHelper.sendByGroup(frontSessionGroup, "Describe", message.getPayload());
        // 发送完一次消息后，断开与agent的连接
        WebSocketTool.closeSessionQuietly(session);
        // 发送完一次消息后，断开与前端的连接
        keySocketSendHelper.closeSessionByGroup(frontSessionGroup);
    }
}
