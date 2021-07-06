package io.choerodon.devops.api.ws.host;

import io.choerodon.devops.api.vo.host.HostMsgVO;
import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.eventhandler.host.HostMsgHandler;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.constant.DevOpsWebSocketConstants;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.enums.DevopsHostStatus;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.util.JsonHelper;

import org.hzero.websocket.constant.WebSocketConstant;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.hzero.websocket.vo.MsgVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wanghao
 * @since 21-6-24
 */
@Component
public class HostAgentSocketHandler extends AbstractSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostAgentSocketHandler.class);

    private final Map<String, HostMsgHandler> hostMsgHandlerMap = new HashMap<>();


    @Autowired
    private List<HostMsgHandler> hostMsgHandlers;

    @Autowired
    private DevopsHostService devopsHostService;

    @Autowired
    private KeySocketSendHelper webSocketHelper;

    @Override
    public String processor() {
        return DevOpsWebSocketConstants.HOST_AGENT;
    }

    @PostConstruct
    void init() {
        for (HostMsgHandler hostMsgHandler : hostMsgHandlers) {
            hostMsgHandlerMap.put(hostMsgHandler.getType(), hostMsgHandler);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 就是agent连接时应该传入的group参数，形如  front_agent:clusterId:21
        String hostId = WebSocketTool.getHostId(session);

        devopsHostService.baseUpdateHostStatus(Long.parseLong(hostId), DevopsHostStatus.CONNECTED);

        //
        HostMsgVO hostMsgVO = new HostMsgVO();
        hostMsgVO.setType(HostCommandEnum.INIT_AGENT.value());
        hostMsgVO.setHostId(hostId);

        // 为了保持和其他通过hzero发送的消息结构一致
        MsgVO msgVO = (new MsgVO()).setGroup(DevopsHostConstants.GROUP + hostId).setKey(HostCommandEnum.INIT_AGENT.value()).setMessage(JsonHelper.marshalByJackson(hostMsgVO)).setType(WebSocketConstant.SendType.S_GROUP);


        sendToSession(session, new TextMessage(JsonHelper.marshalByJackson(msgVO)));
    }

    private void sendToSession(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
        if (webSocketSession.isOpen()) {
            synchronized (webSocketSession) {
                try {
                    webSocketSession.sendMessage(webSocketMessage);
                } catch (IOException e) {
                    LOGGER.warn("Send to session: Failed to send message. the message is {}, and the ex is: ", webSocketMessage.getPayload(), e);
                }
            }
        } else {
            LOGGER.warn("Send to session: session is unexpectedly closed. the message is {}", webSocketMessage.getPayload());
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String hostId = WebSocketTool.getHostId(session);

        devopsHostService.baseUpdateHostStatus(Long.parseLong(hostId), DevopsHostStatus.DISCONNECT);

        WebSocketTool.closeSessionQuietly(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            doHandle(session, message);
            // 将异常捕获，否则未捕获的异常会导致外层的WebSocket框架关闭这个和Agent的连接
        } catch (Exception ex) {
            LOGGER.warn("Handle Agent Message: an unexpected exception occurred", ex);
        }
    }

    private void doHandle(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        LOGGER.info("Received agent msg: {}", payload);
        HostMsgVO msg = JsonHelper.unmarshalByJackson(payload, HostMsgVO.class);


        HostMsgHandler hostMsgHandler = hostMsgHandlerMap.get(msg.getType());
        if (hostMsgHandler == null) {
            LOGGER.info("unknown msg type, msg {}", msg);
            return;
        }
        hostMsgHandler.handler(msg.getHostId(), msg.getCommandId(), msg.getPayload());

    }


}
