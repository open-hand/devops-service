package io.choerodon.devops.api.ws.log.agent;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import io.choerodon.devops.api.ws.AgentExecAndLogSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.helper.SocketHandlerRegistration;
import io.choerodon.websocket.helper.WebSocketHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/8/19.
 */

@Component
public class AgentLogSocketHandlerRegistration  implements SocketHandlerRegistration {

    @Autowired
    WebSocketHelper webSocketHelper;
    @Autowired
    AgentExecAndLogSocketHandler agentExecAndLogSocketHandler;

    @Override
    public String path() {
        return "/agent/log";
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
       return agentExecAndLogSocketHandler.beforeHandshake(serverHttpRequest,serverHttpResponse);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        agentExecAndLogSocketHandler.afterConnectionEstablished(webSocketSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {

    }


}
