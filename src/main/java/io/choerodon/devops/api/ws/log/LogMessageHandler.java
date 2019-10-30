package io.choerodon.devops.api.ws.log;


import java.nio.ByteBuffer;
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
 * Created by Sheep on 2019/7/26.
 */
@Component
public class LogMessageHandler {
    public static final String AGENT_LOG = "AgentLog";

    @Autowired
    @Lazy
    private WebSocketHelper webSocketHelper;

    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));


        ByteBuffer buffer = message.getPayload();
        byte[] bytesArray = new byte[buffer.remaining()];
        buffer.get(bytesArray, 0, bytesArray.length);

        if (webSocketSession.getUri().getPath().equals("/devops/log")) {
            registerKey = "from_agent:" + registerKey;
        } else {
            registerKey = "from_devops:" + registerKey;
        }

        SendBinaryMessagePayload binaryMessageWebSocketSendPayload = new SendBinaryMessagePayload();
        binaryMessageWebSocketSendPayload.setKey(registerKey);
        binaryMessageWebSocketSendPayload.setData(bytesArray);
        binaryMessageWebSocketSendPayload.setType(AGENT_LOG);

        webSocketHelper.sendMessageByKey(registerKey, binaryMessageWebSocketSendPayload);

    }

}
