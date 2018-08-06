package io.choerodon.devops.domain.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;

/**
 * Created by younger on 2018/4/18.
 */
@Service
public class DeployServiceImpl implements DeployService {

    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private CommandSender commandSender;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    @Value("${services.helm.url}")
    private String helmUrl;

    @Override
    public void sendCommand(DevopsEnvironmentE devopsEnvironmentE) {
        Msg msg = new Msg();
        msg.setKey("env:" + devopsEnvironmentE.getCode() + ".envId:" + devopsEnvironmentE.getId());
        msg.setType("git_ops_sync");
        msg.setPayload("");
        commandSender.sendMsg(msg);
    }

}
