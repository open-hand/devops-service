package io.choerodon.devops.api.ws.exec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.helper.WebSocketHelper;
import io.choerodon.websocket.send.SendBinaryMessagePayload;

/**
 * Created by Sheep on 2019/8/19.
 */
@Component
public class ExecMessageHandler {
    public static final String AGENT_EXEC = "AgentExec";

    @Autowired
    @Lazy
    private WebSocketHelper webSocketHelper;

    public ExecMessageHandler() {
    }

    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);
        String msg = WebSocketTool.replaceR(new StringBuilder(new String(bytesArray, StandardCharsets.UTF_8)), 0);

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));

        if (webSocketSession.getUri().getPath().equals("/devops/exec")) {
            registerKey = "from_agent:" + registerKey;
        } else {
            registerKey = "from_devops:" + registerKey;
        }

        SendBinaryMessagePayload binaryMessagePayload = new SendBinaryMessagePayload();
        binaryMessagePayload.setKey(registerKey);
        binaryMessagePayload.setData(msg.getBytes());
        binaryMessagePayload.setType(AGENT_EXEC);

        //发送二进制消息
        webSocketHelper.sendMessageByKey(registerKey, binaryMessagePayload);
    }
}
