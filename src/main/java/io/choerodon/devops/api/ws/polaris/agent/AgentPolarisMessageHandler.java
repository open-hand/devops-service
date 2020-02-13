package io.choerodon.devops.api.ws.polaris.agent;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.AgentMsgVO;
import io.choerodon.devops.infra.util.KeyParseUtil;
import io.choerodon.websocket.receive.TextMessageHandler;

@Component
public class AgentPolarisMessageHandler implements TextMessageHandler<AgentMsgVO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentPolarisMessageHandler.class);

    @Override
    public void handle(WebSocketSession webSocketSession, String type, String key, AgentMsgVO message) {
        //设置集群id
        message.setClusterId(key.split(":")[1]);
        String namespace = KeyParseUtil.getNamespace(message.getKey());
        // TODO by zmf 存数据库
        LOGGER.info("Polaris message received: {}", message);
        try {
            webSocketSession.close();
        } catch (IOException e) {
            LOGGER.warn("Exception occurred when close webSocketSession for cluster with id {} and for type polaris. The ex is: {}", e);
        }
    }

    @Override
    public String matchPath() {
        return "/agent/polaris";
    }

    @Override
    public String matchType() {
        return "polaris";
    }
}
