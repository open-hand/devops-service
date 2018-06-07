package io.choerodon.devops.domain.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.devops.domain.service.IDevopsIngressService;
import io.choerodon.devops.infra.common.util.enums.HelmType;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;

/**
 * Created by Zenger on 2018/5/14.
 */
@Service
public class IDevopsIngressServiceImpl implements IDevopsIngressService {

    private static final Logger logger = LoggerFactory.getLogger(IDevopsIngressServiceImpl.class);

    @Autowired
    private CommandSender commandSender;

    @Override
    @Async
    public void createIngress(String ingressYaml, String name, String namespace, Long envId, Long commandId) {
        Msg msg = new Msg();
        msg.setKey("env:" + namespace + ".envId:" + envId + ".Ingress:" + name);
        msg.setType(HelmType.NetworkIngress.toValue());
        msg.setCommandId(commandId);
        msg.setPayload(ingressYaml);
        logger.info("send update ingress message: " + msg);
        commandSender.sendMsg(msg);
    }

    @Override
    @Async
    public void deleteIngress(String name, String namespace, Long envId, Long commandId) {
        Msg msg = new Msg();
        msg.setKey("env:" + namespace + ".envId:" + envId + ".Ingress:" + name);
        msg.setType(HelmType.NetworkIngressDelete.toValue());
        msg.setPayload(name);
        msg.setCommandId(commandId);
        logger.info("send delete ingress message: " + msg);
        commandSender.sendMsg(msg);
    }
}
