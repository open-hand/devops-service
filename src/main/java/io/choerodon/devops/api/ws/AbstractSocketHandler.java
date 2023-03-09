package io.choerodon.devops.api.ws;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hzero.websocket.handler.SocketHandler;
import org.hzero.websocket.registry.GroupSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession;

/**
 * 为了避免子类实现不必要实现的方法所提供的抽象类
 *
 * @author zmf
 * @since 20-5-9
 */
public abstract class AbstractSocketHandler implements SocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSocketHandler.class);
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = getSessionId(session);
        // 清理内存
        GroupSessionRegistry.removeSession(sessionId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {

    }

    @Override
    public void handlePongMessage(WebSocketSession session, PongMessage message) {

    }

    /**
     * 获取sessionId
     *
     * @param session session
     * @return sessionId
     */
    private String getSessionId(WebSocketSession session) {
        String sessionId = null;
        try {
            // 清理缓存
            if (session instanceof StandardWebSocketSession) {
                // websocket连接方式
                sessionId = session.getId();
            } else if (session instanceof WebSocketServerSockJsSession) {
                // sock js 连接
                sessionId = ((WebSocketSession) FieldUtils.readField(session, "webSocketSession", true)).getId();
            }
            return sessionId;
        } catch (Exception e) {
            logger.warn("webSocket disConnection failed.");
            return null;
        }
    }
}
