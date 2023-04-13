package io.choerodon.devops.api.ws.log.k8s;


import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.FRONT_LOG;

import java.nio.ByteBuffer;

import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.WebSocketTool;

/**
 * Created by Sheep on 2019/7/26.
 */
@Component
public class LogMessageHandler {
    public static final String VIEW_LOG = "viewLog";
    public static final String DOWNLOAD_LOG = "downloadLog";
    private static final Logger LOGGER = LoggerFactory.getLogger(LogMessageHandler.class);
    private static final String AGENT_LOG = "AgentLog";

    @Autowired
    @Lazy
    private KeySocketSendHelper keySocketSendHelper;

    public void handle(WebSocketSession webSocketSession, BinaryMessage message, String type) {
        // 获取rawKey， 用于拼接转发的目的地group
        String rawKey = WebSocketTool.getKey(webSocketSession);
        String destinationGroup;

        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);

        String processor = WebSocketTool.getProcessor(webSocketSession);

        switch (type) {
            case "viewLog":
                if (FRONT_LOG.equals(processor)) {
                    destinationGroup = WebSocketTool.buildAgentGroup(rawKey);
                } else {
                    destinationGroup = WebSocketTool.buildFrontGroup(rawKey);
                }
                keySocketSendHelper.sendByGroup(destinationGroup, AGENT_LOG, bytesArray);
                break;
            case "downloadLog":
                destinationGroup = WebSocketTool.buildFrontGroup(rawKey);
                keySocketSendHelper.sendByGroup(destinationGroup, AGENT_LOG, bytesArray);
                break;
        }
    }

}
