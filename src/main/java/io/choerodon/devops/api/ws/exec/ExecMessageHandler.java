package io.choerodon.devops.api.ws.exec;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.FRONT_EXEC;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.hzero.websocket.helper.KeySocketSendHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.WebSocketTool;

/**
 * Created by Sheep on 2019/8/19.
 */
@Component
public class ExecMessageHandler {
    private static final String AGENT_EXEC = "AgentExec";

    @Autowired
    @Lazy
    private KeySocketSendHelper keySocketSendHelper;

    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);

        String msg = WebSocketTool.replaceR(new StringBuilder(new String(bytesArray, StandardCharsets.UTF_8)), 0);

        String key = WebSocketTool.getKey(webSocketSession);
        String processor = WebSocketTool.getProcessor(webSocketSession);

        String toSessionGroup;
        if (FRONT_EXEC.equals(processor)) {
            // 如果是前端来的消息就转发给agent
            toSessionGroup = WebSocketTool.buildAgentGroup(key);
        } else {
            // 如果是agent来的消息，就转发给前端
            toSessionGroup = WebSocketTool.buildFrontGroup(key);
        }

        //发送二进制消息
        keySocketSendHelper.sendByGroup(toSessionGroup, AGENT_EXEC, msg.getBytes(StandardCharsets.UTF_8));
    }
}
