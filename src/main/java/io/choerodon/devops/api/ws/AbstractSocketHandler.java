package io.choerodon.devops.api.ws;

import org.hzero.websocket.handler.SocketHandler;
import org.springframework.web.socket.*;

/**
 * 为了避免子类实现不必要实现的方法所提供的抽象类
 *
 * @author zmf
 * @since 20-5-9
 */
public abstract class AbstractSocketHandler implements SocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

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
}
