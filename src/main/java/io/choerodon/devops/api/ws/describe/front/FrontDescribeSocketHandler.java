package io.choerodon.devops.api.ws.describe.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.DescribeResourceVO;
import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.AgentCommandService;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class FrontDescribeSocketHandler extends AbstractSocketHandler {

    @Autowired
    private AgentCommandService agentCommandService;

    @Override
    public String processor() {
        return FRONT_DESCRIBE;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> attribute = session.getAttributes();

        String key = WebSocketTool.getKey(session);

        DescribeResourceVO describeResourceVO = new DescribeResourceVO(
                attribute.get(KIND).toString(),
                attribute.get(NAME).toString(),
                attribute.get(ENV).toString(),
                attribute.get(DESCRIBE_Id).toString());

        Long clusterId = WebSocketTool.getClusterId(session);

        // 通过GitOps长连接, 通知agent建立与前端对应的ws连接
        agentCommandService.startDescribeConnection(key, describeResourceVO, clusterId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        WebSocketTool.closeAgentSessionByKey(WebSocketTool.getKey(session));
    }
}
