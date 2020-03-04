package io.choerodon.devops.api.ws.polaris.agent;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.polaris.PolarisResponsePayloadVO;
import io.choerodon.devops.api.vo.polaris.PolarisScanAuditDataVO;
import io.choerodon.devops.app.service.PolarisScanningService;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.receive.TextMessageHandler;

@Component
public class AgentPolarisMessageHandler implements TextMessageHandler<PolarisResponsePayloadVO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentPolarisMessageHandler.class);

    @Autowired
    private PolarisScanningService polarisScanningService;

    @Override
    public void handle(WebSocketSession webSocketSession, String type, String key, PolarisResponsePayloadVO message) {
        LOGGER.info("Polaris: received message from agent...");
        //设置集群id
        Long clusterId = TypeUtil.objToLong(key.split(":")[1]);
        LOGGER.info("Polaris: the cluster id is {}", clusterId);

        polarisScanningService.handleAgentPolarisMessage(message);

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
