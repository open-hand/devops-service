package io.choerodon.devops.domain.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.devops.domain.service.IDevopsServiceService;
import io.choerodon.devops.infra.common.util.enums.HelmType;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;


/**
 * Created by Zenger on 2018/4/20.
 */
@Service
public class IDevopsServiceServiceImpl implements IDevopsServiceService {

    private static final Logger logger = LoggerFactory.getLogger(IDevopsServiceServiceImpl.class);

    @Autowired
    private CommandSender commandSender;

    @Override
    @Async
    public void deploy(String serviceYaml, String name, String namespace, Long envId, Long commandId) {
        Msg msg = new Msg();
        msg.setKey("env:" + namespace + ".envId:" + envId + ".Service:" + name);
        msg.setType(HelmType.NETWORK_SERVICE.toValue());
        msg.setPayload(serviceYaml);
        msg.setCommandId(commandId);
        logger.info(String.format("send update service message: %s", msg));
        commandSender.sendMsg(msg);
    }

    @Override
    public void delete(String name, String namespace, Long envId, Long commandId) {
        Msg msg = new Msg();
        msg.setKey("env:" + namespace + ".envId:" + envId + ".Service:" + name);
        msg.setType(HelmType.NETWORK_SERVICE_DELETE.toValue());
        msg.setPayload(name);
        msg.setCommandId(commandId);
        logger.info(String.format("send delete service message: %s", msg));
        commandSender.sendMsg(msg);
    }
}
