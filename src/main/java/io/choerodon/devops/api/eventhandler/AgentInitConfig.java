package io.choerodon.devops.api.eventhandler;

import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsClusterE;
import io.choerodon.devops.domain.application.repository.DevopsClusterProPermissionRepository;
import io.choerodon.devops.domain.application.repository.DevopsClusterRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.websocket.helper.CommandSender;
import io.choerodon.websocket.helper.EnvListener;
import io.choerodon.websocket.session.AgentConfigurer;
import io.choerodon.websocket.session.AgentSessionManager;
import io.choerodon.websocket.session.Session;
import io.choerodon.websocket.session.SessionListener;

@Component
public class AgentInitConfig implements AgentConfigurer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String INIT_AGENT = "init_agent";
    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    @Autowired
    CommandSender commandSender;
    @Autowired
    DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    DevopsClusterRepository devopsClusterRepository;
    @Autowired
    DevopsClusterProPermissionRepository devopsClusterProPermissionRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DeployService deployService;
    @Autowired
    private EnvListener envListener;
    @Autowired
    private EnvUtil envUtil;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Override
    public void registerSessionListener(AgentSessionManager agentSessionManager) {
        AgentInitListener agentInitListener = new AgentInitListener();
        agentSessionManager.addListener(agentInitListener);
    }

    class AgentInitListener implements SessionListener {
        @Override
        public void onConnected(Session session) {
            try {
                Long clusterId = Long.valueOf(session.getRegisterKey().split(":")[1]);
                DevopsClusterE devopsClusterE = devopsClusterRepository.query(clusterId);
                deployService.upgradeCluster(devopsClusterE);
                deployService.initCluster(clusterId);
            } catch (Exception e) {
                throw new CommonException("read envId from agent session failed", e);
            }
        }

        @Override
        public Session onClose(String s) {
            return null;
        }
    }


}
