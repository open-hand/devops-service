package io.choerodon.devops.api.ws.describe.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;

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
        String key = request.getParameter(KEY);
        String env = request.getParameter(ENV);
        String kind = request.getParameter(KIND);
        String name = request.getParameter(NAME);
        String describeId = request.getParameter(DESCRIBE_Id);
        if (WebSocketTool.isEmptyOrTrimmedEmpty(key)) {
            throw new HandshakeFailureException("Key is unexpectedly null");
        }
        if (WebSocketTool.isEmptyOrTrimmedEmpty(env)) {
            throw new HandshakeFailureException("Env is null!");
        }
        if (WebSocketTool.isEmptyOrTrimmedEmpty(kind)) {
            throw new HandshakeFailureException("kind is null!");
        }
        if (WebSocketTool.isEmptyOrTrimmedEmpty(name)) {
            throw new HandshakeFailureException("name is null!");
        }
        if (WebSocketTool.isEmptyOrTrimmedEmpty(describeId)) {
            throw new HandshakeFailureException("describeId is null!");
        }
        return true;
    }
}
