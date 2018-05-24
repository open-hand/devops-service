package io.choerodon.devops.infra.config;

import org.springframework.context.annotation.Configuration;

import io.choerodon.websocket.session.AgentConfigurer;
import io.choerodon.websocket.session.AgentSessionManager;

@Configuration
public class AgentConfiguration implements AgentConfigurer {

    @Override
    public void registerSessionListener(AgentSessionManager agentSessionManager) {
        agentSessionManager.addListener(new AgentVersionListener());
    }
}
