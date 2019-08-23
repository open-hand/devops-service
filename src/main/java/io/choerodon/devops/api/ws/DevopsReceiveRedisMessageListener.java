package io.choerodon.devops.api.ws;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.websocket.receive.WebSocketReceivePayload;
import io.choerodon.websocket.relationship.DefaultRelationshipDefining;
import io.choerodon.websocket.send.MessageSender;
import io.choerodon.websocket.send.ReceiveRedisMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/19.
 */
@Component
@Primary
public class DevopsReceiveRedisMessageListener extends ReceiveRedisMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsReceiveRedisMessageListener.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    private MessageSender messageSender;
    private DefaultRelationshipDefining defaultRelationshipDefining;


    public DevopsReceiveRedisMessageListener(MessageSender messageSender, DefaultRelationshipDefining defaultRelationshipDefining) {
        super(messageSender);
        this.messageSender = messageSender;
        this.defaultRelationshipDefining = defaultRelationshipDefining;
    }

    public void receiveMessage(Object message) {
        LOGGER.debug("receive message from redis channels, message {}", message);
        if (message instanceof String) {
            try {
                JsonNode node = OBJECT_MAPPER.readTree((String) message);
                String key = node.get("key").asText();
                String type = node.get("type").asText();
                if (StringUtils.isEmpty(key)) {
                    LOGGER.debug("receive the message do not have key!");
                }
                if (StringUtils.isEmpty(type)) {
                    LOGGER.debug("receive the message do not have type!");
                }
                //分别处理日志消息，exec消息,关闭websocket消息,以及正常gitops处理消息
                if (type.equals("AgentLog")) {
                    JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(WebSocketReceivePayload.class, String.class);
                    WebSocketReceivePayload<String> webSocketReceivePayload = OBJECT_MAPPER.readValue((String) message, javaType);
                    Set<WebSocketSession> webSocketSessions = defaultRelationshipDefining.getWebSocketSessionsByKey(key);
                    if (webSocketSessions != null) {
                        for (WebSocketSession webSocketSession : webSocketSessions) {
                            synchronized (webSocketSession) {
                                try {
                                    webSocketSession.sendMessage(new BinaryMessage(webSocketReceivePayload.getData().getBytes()));
                                } catch (IOException e) {
                                    LOGGER.warn("error.messageOperator.sendWebSocket.IOException, message: {}", message, e);
                                }
                            }
                        }
                    }
                } else if (type.equals("AgentExec")||type.equals("Describe")) {
                    JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(WebSocketReceivePayload.class, String.class);
                    WebSocketReceivePayload<String> webSocketReceivePayload = OBJECT_MAPPER.readValue((String) message, javaType);
                    Set<WebSocketSession> webSocketSessions = defaultRelationshipDefining.getWebSocketSessionsByKey(key);
                    if (webSocketSessions != null) {
                        for (WebSocketSession webSocketSession : webSocketSessions) {
                            synchronized (webSocketSession) {
                                try {
                                    webSocketSession.sendMessage(new TextMessage(webSocketReceivePayload.getData()));
                                } catch (IOException e) {
                                    LOGGER.warn("error.messageOperator.sendWebSocket.IOException, message: {}", message, e);
                                }
                            }
                        }
                    }
                } else if (type.equals("closeAgentSession")) {
                    Set<WebSocketSession> webSocketSessions = defaultRelationshipDefining.getWebSocketSessionsByKey(key);
                    if (webSocketSessions != null) {
                        for (WebSocketSession webSocketSession : webSocketSessions) {
                            webSocketSession.close();
                        }
                    }
                } else {
                    messageSender.sendWebSocketByKey(key, (String) message);
                }
            } catch (IOException e) {
                LOGGER.warn("error.receiveRedisMessageListener.receiveMessage.send", e);
            }
        } else {
            LOGGER.warn("receive message from redis channels that type is not String, message: {}", message);
        }
    }
}
