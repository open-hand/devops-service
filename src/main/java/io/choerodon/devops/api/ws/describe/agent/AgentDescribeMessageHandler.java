package io.choerodon.devops.api.ws.describe.agent;

import java.io.IOException;
import java.util.Map;

import io.choerodon.devops.api.vo.AgentMsgVO;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.helper.WebSocketHelper;
import io.choerodon.websocket.receive.TextMessageHandler;
import io.choerodon.websocket.send.SendMessagePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/22.
 */
@Component
public class AgentDescribeMessageHandler implements TextMessageHandler<AgentMsgVO> {


    private static final Logger LOGGER = LoggerFactory.getLogger(AgentDescribeMessageHandler.class);

    @Autowired
    WebSocketHelper webSocketHelper;


    @Override
    public void handle(WebSocketSession webSocketSession, String type, String key, AgentMsgVO msg) {

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = "from_devops:" + TypeUtil.objToString(attribute.get("key"));

        SendMessagePayload<String> webSocketSendPayload = new SendMessagePayload<>();
        webSocketSendPayload.setKey(registerKey);
        webSocketSendPayload.setType("Describe");
        webSocketSendPayload.setData(msg.getPayload());

        webSocketHelper.sendMessageByKey(registerKey, webSocketSendPayload);

        try {
            webSocketSession.close();
        } catch (IOException e) {
            LOGGER.error("close session failed!", e);
        }
    }


    @Override
    public String matchType() {
        return "/agent/describe";
    }

}
