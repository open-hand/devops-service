package io.choerodon.devops.api.ws.compatibility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 为处理旧版本agent的处理器注册
 *
 * @author zmf
 * @since 20-5-11
 */
@Configuration
@EnableWebSocket
public class ElderWebSocketConfiguration implements WebSocketConfigurer {
    @Autowired
    private ElderAgentSocketHandler elderAgentSocketHandler;
    @Autowired
    private ElderAgentSocketInterceptor elderAgentSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(elderAgentSocketHandler, "/agent/").addInterceptors(elderAgentSocketInterceptor).setAllowedOrigins("*");
    }
}
