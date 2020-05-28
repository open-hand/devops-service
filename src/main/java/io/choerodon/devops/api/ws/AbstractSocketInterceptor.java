package io.choerodon.devops.api.ws;

import java.util.Map;

import org.hzero.websocket.interceptor.SocketInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;

/**
 * 为了避免子类实现不必要实现的方法所提供的抽象类
 *
 * @author zmf
 * @since 20-5-9
 */
public abstract class AbstractSocketInterceptor implements SocketInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {
    }
}
