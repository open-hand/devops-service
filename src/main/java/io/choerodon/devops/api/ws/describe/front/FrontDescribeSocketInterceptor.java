package io.choerodon.devops.api.ws.describe.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.FRONT_DESCRIBE;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.WebSocketTool;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class FrontDescribeSocketInterceptor extends AbstractSocketInterceptor {
    @Override
    public String processor() {
        return FRONT_DESCRIBE;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
        HttpServletRequest request = servletRequest.getServletRequest();

        //校验ws连接参数是否正确
        WebSocketTool.checkKey(request);
        WebSocketTool.checkEnv(request);
        WebSocketTool.checkKind(request);
        WebSocketTool.checkName(request);
        WebSocketTool.checkDescribeId(request);

        return true;
    }
}
