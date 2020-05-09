package io.choerodon.devops.api.ws.describe.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;

import java.util.Map;

import org.hzero.websocket.helper.KeySocketSendHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.DescribeResourceVO;
import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class FrontDescribeSocketHandler extends AbstractSocketHandler {
    @Autowired
    @Lazy
    private KeySocketSendHelper webSocketHelper;

    @Autowired
    private AgentCommandService agentCommandService;

    @Override
    public String processor() {
        return FRONT_DESCRIBE;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> attribute = session.getAttributes();

        String frontSessionGroup = WebSocketTool.getGroup(session);
        String rawKey = WebSocketTool.getRawKey(frontSessionGroup);
        String agentSessionGroup = WebSocketTool.buildAgentGroup(rawKey);


        DescribeResourceVO describeResourceVO = new DescribeResourceVO(
                attribute.get(KIND).toString(),
                attribute.get(NAME).toString(),
                attribute.get(ENV).toString(),
                attribute.get(DESCRIBE_Id).toString());

        Long clusterId = WebSocketTool.getClusterId(TypeUtil.objToString(attribute.get(KEY)));

        // 通过GitOps长连接, 通知agent建立与前端对应的ws连接
        agentCommandService.startDescribeConnection(agentSessionGroup, describeResourceVO, clusterId);
    }
}
