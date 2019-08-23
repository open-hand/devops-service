package io.choerodon.devops.api.ws.describe.agent;

import java.io.IOException;
import java.util.Map;

import io.choerodon.devops.api.vo.AgentMsgVO;
import io.choerodon.devops.api.ws.DevopsReceiveRedisMessageListener;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.receive.MessageHandler;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import io.choerodon.websocket.send.MessageSender;
import io.choerodon.websocket.send.WebSocketSendPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/22.
 */
@Component
public class AgentDescribeMessageHandler implements MessageHandler<AgentMsgVO> {


    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsReceiveRedisMessageListener.class);


    @Autowired
    DefaultRelationshipDefining defaultRelationshipDefining;
    @Autowired
    MessageSender messageSender;


    @Override
    public void handle(WebSocketSession webSocketSession, String type, String key, AgentMsgVO msg) {

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));

        WebSocketSendPayload<String> webSocketSendPayload = new WebSocketSendPayload<>();
        webSocketSendPayload.setKey(registerKey);
        webSocketSendPayload.setType("Describe");
        webSocketSendPayload.setData(msg.getPayload());

        defaultRelationshipDefining.getWebSocketSessionsByKey(registerKey).forEach((session) -> {
            if (session != webSocketSession) {
                try {
                    session.sendMessage(new TextMessage(msg.getPayload()));
                } catch (IOException e) {
                    LOGGER.error("close session failed!", e);
                }
            }
        });
        defaultRelationshipDefining.getRedisChannelsByKey(registerKey, true).forEach((redis) -> {
            messageSender.sendRedis(redis, webSocketSendPayload);
        });
        try {
            defaultRelationshipDefining.removeKeyContact(webSocketSession,registerKey);
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
