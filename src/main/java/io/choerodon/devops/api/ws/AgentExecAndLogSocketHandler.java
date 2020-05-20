package io.choerodon.devops.api.ws;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.helper.WebSocketHelper;

/**
 * Created by Sheep on 2019/8/19.
 */
@Component
public class AgentExecAndLogSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentExecAndLogSocketHandler.class);

    @Lazy
    @Autowired
    private WebSocketHelper webSocketHelper;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
        HttpServletRequest request = servletRequest.getServletRequest();

        //校验ws连接参数是否正确
        String key = request.getParameter("key");
        if (key == null || key.trim().isEmpty()) {
            throw new CommonException("Key is null");
        }

        return true;
    }

    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //解析参数列表，并存储
        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = "from_agent:" + TypeUtil.objToString(attribute.get("key"));
        String path = webSocketSession.getUri() == null ? null : webSocketSession.getUri().getPath();
        LOGGER.info("Connection established from agent. The registerKey is {} and the path is {}", registerKey, path);

        //将websocketSession和关联的key做关联
        webSocketHelper.subscribe(registerKey, webSocketSession);
    }
}
