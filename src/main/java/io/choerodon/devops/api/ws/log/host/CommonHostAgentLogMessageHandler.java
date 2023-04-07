package io.choerodon.devops.api.ws.log.host;


import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.HOST_AGENT_LOG;

import java.nio.ByteBuffer;

import org.hzero.websocket.helper.KeySocketSendHelper;
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
public class CommonHostAgentLogMessageHandler {
    public static final String VIEW_LOG = "viewLog";
    public static final String DOWNLOAD_LOG = "downloadLog";
    // $1 hostId $2 token
    public static final String GROUP_TEMPLATE = "host:%s.log:%s";

    @Autowired
    @Lazy
    private KeySocketSendHelper keySocketSendHelper;

    public void handle(WebSocketSession webSocketSession, BinaryMessage message, String type) {
        // 获取hostId， 用于拼接转发的目的地group
        String hostId = WebSocketTool.getHostId(webSocketSession);
        String token = WebSocketTool.getToken(webSocketSession);
        String destinationGroup;

        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);

        String processor = WebSocketTool.getProcessor(webSocketSession);

        switch (type) {
            case VIEW_LOG:
                if (HOST_AGENT_LOG.equals(processor)) {
                    destinationGroup = WebSocketTool.buildHostFrontGroup(String.format(GROUP_TEMPLATE, hostId, token));
                } else {
                    destinationGroup = WebSocketTool.buildHostAgentGroup(String.format(GROUP_TEMPLATE, hostId, token));
                }
                keySocketSendHelper.sendByGroup(destinationGroup, HOST_AGENT_LOG, bytesArray);
                break;
            case DOWNLOAD_LOG:
                destinationGroup = WebSocketTool.buildHostFrontGroup(String.format(GROUP_TEMPLATE, hostId, token));
                keySocketSendHelper.sendByGroup(destinationGroup, HOST_AGENT_LOG, bytesArray);
        }
    }

}
