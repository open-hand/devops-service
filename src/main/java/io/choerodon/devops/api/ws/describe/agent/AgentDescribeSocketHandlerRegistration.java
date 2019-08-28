package io.choerodon.devops.api.ws.describe.agent;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.connect.SocketHandlerRegistration;
import io.choerodon.websocket.helper.WebSocketHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeFailureException;

/**
 * Created by Sheep on 2019/8/22.
 */
@Component
public class AgentDescribeSocketHandlerRegistration implements SocketHandlerRegistration {

    @Autowired
    private WebSocketHelper webSocketHelper;


    @Override
    public String path() {
        return "/agent/describe";
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
        HttpServletRequest request = servletRequest.getServletRequest();

        //校验ws连接参数是否正确
        String key = request.getParameter("key");
        if (key == null || key.trim().isEmpty()) {
            throw new HandshakeFailureException("Key is null");
        }
        return true;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //解析参数列表，并存储
        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));
        //将websocketSession和关联的key做关联
        webSocketHelper.subscribe("from_agent:" + registerKey, webSocketSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {

    }
}
