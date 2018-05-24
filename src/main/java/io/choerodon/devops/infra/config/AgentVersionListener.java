package io.choerodon.devops.infra.config;



import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.impl.DevopsEnvironmentServiceImpl;
import io.choerodon.websocket.session.Session;
import io.choerodon.websocket.session.SessionListener;


public class AgentVersionListener implements SessionListener {


    @Autowired
    private DevopsEnvironmentServiceImpl devopsEnvironmentService;


    @Override
    public void onConnected(Session session) {
        devopsEnvironmentService.setAgentVersion(session.getWebSocketSession().getAttributes().get("version").toString());
    }



    @Override
    public Session onClose(String s) {
        return null;
    }

}
