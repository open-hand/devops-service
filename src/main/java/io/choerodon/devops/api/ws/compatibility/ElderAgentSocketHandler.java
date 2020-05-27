package io.choerodon.devops.api.ws.compatibility;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;

/**
 * 兼容0.21.x版本的agent
 *
 * @author zmf
 * @since 20-5-11
 */
@Component
public class ElderAgentSocketHandler extends AbstractWebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElderAgentSocketHandler.class);

    @Autowired
    private DevopsClusterService devopsClusterService;

    @Autowired
    private AgentCommandService agentCommandService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String version = WebSocketTool.getVersion(session);
        Long clusterId = WebSocketTool.getClusterId(session);
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(Objects.requireNonNull(clusterId));

        LOGGER.info("Upgrade elder agent: upgrade agent with cluster id {} from version {}", clusterId, version);
        // 直接向这个agent的会话写入升级agent的指令
        agentCommandService.upgradeCluster(devopsClusterDTO, session);
    }
}
