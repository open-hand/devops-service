package io.choerodon.devops.domain.service.impl;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.valueobject.CommandPayLoad;
import io.choerodon.devops.domain.application.valueobject.Payload;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.enums.HelmType;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;

/**
 * Created by younger on 2018/4/18.
 */
@Service
public class DeployServiceImpl implements DeployService {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private CommandSender commandSender;

    @Value("${services.helm.url}")
    private String helmUrl;

    @Autowired
    public DeployServiceImpl(CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @Override
    public void sendCommand(DevopsEnvironmentE devopsEnvironmentE) {
        Msg msg = new Msg();
        msg.setKey("env:" + devopsEnvironmentE.getCode() + ".envId:" + devopsEnvironmentE.getId());
        msg.setType("git_ops_sync");
        msg.setPayload("");
        commandSender.sendMsg(msg);
    }


    @Override
    public void deploy(ApplicationE applicationE, ApplicationVersionE applicationVersionE, ApplicationInstanceE applicationInstanceE, DevopsEnvironmentE devopsEnvironmentE, String values, Long commandId) {
        Msg msg = new Msg();
        Payload payload = new Payload(
                devopsEnvironmentE.getCode(),
                helmUrl + applicationVersionE.getRepository(),
                applicationE.getCode(),
                applicationVersionE.getVersion(),
                values, applicationInstanceE.getCode());
        msg.setKey(String.format("env:%s.envId:%d.release:%s",
                devopsEnvironmentE.getCode(),
                devopsEnvironmentE.getId(),
                applicationInstanceE.getCode()));
        msg.setType(HelmType.HELM_RELEASE_PRE_UPGRADE.toValue());
        try {
            msg.setPayload(mapper.writeValueAsString(payload));
            msg.setCommandId(commandId);
        } catch (IOException e) {
            throw new CommonException("error.payload.error");
        }
        commandSender.sendMsg(msg);
    }

    @Override
    public void sendCommandSyncEvent(Long envId, String envCode, List<DevopsEnvCommandE> devopsEnvCommandES) {
        Msg msg = new Msg();
        CommandPayLoad payload = new CommandPayLoad(
                devopsEnvCommandES);
        msg.setKey(String.format("env:%s.envId:%d",
                envCode,
                envId));
        msg.setType(HelmType.GIT_OPS_COMMAND_SYNC_EVENT_RESULT.toValue());
        try {
            msg.setPayload(mapper.writeValueAsString(payload));
        } catch (IOException e) {
            throw new CommonException("error.payload.error");
        }
        commandSender.sendMsg(msg);
    }
}
