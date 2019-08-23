package io.choerodon.devops.api.ws.describe.devops;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import io.choerodon.devops.api.vo.DescribeResourceVO;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.helper.SocketHandlerRegistration;
import io.choerodon.websocket.helper.WebSocketHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class DevopsDescribeSocketHandlerRegistration implements SocketHandlerRegistration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDescribeSocketHandlerRegistration.class);


    @Autowired
    WebSocketHelper webSocketHelper;
    @Autowired
    AgentCommandService agentCommandService;

    @Override
    public String path() {
        return "/devops/describe";
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
        HttpServletRequest request = servletRequest.getServletRequest();


        //校验ws连接参数是否正确
        String key = request.getParameter("key");
        String env = request.getParameter("env");
        String kind = request.getParameter("kind");
        String name = request.getParameter("name");
        String describeId = request.getParameter("describeId");
        if (key == null || key.trim().isEmpty()) {
            throw new HandshakeFailureException("Key is null");
        }
        if (env == null || env.trim().isEmpty()) {
            throw new HandshakeFailureException("Env is null!");
        }
        if (kind == null || kind.trim().isEmpty()) {
            throw new HandshakeFailureException("kind is null!");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new HandshakeFailureException("name is null!");
        }
        if (describeId == null || describeId.trim().isEmpty()) {
            throw new HandshakeFailureException("describeId is null!");
        }
        return true;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //解析参数列表，并存储


        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));
        //将websocketSession和关联的key做关联
        webSocketHelper.contact(webSocketSession, registerKey);

        //通知agent建立与前端同样的ws连接
        DescribeResourceVO describeResourceVO = new DescribeResourceVO(attribute.get("kind").toString(), attribute.get("name").toString(), attribute.get("env").toString(), attribute.get("describeId").toString());
        Long clusterId = TypeUtil.objToLong(registerKey.split("\\.")[0].split(":")[1]);
        agentCommandService.startDescribeConnection(registerKey, describeResourceVO, clusterId);


    }

    @Override

    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);
        String registerKey = TypeUtil.objToString(attribute.get("key"));

        webSocketHelper.removeKeyContact(webSocketSession, registerKey);

        try {
            webSocketSession.close();
        } catch (IOException e) {
            LOGGER.error("close session failed!", e);
        }
    }
}
