package io.choerodon.devops.api.ws.host;

import io.choerodon.devops.api.vo.host.HostMsgVO;
import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.app.service.HostMsgHandler;
import io.choerodon.devops.infra.constant.DevOpsWebSocketConstants;
import io.choerodon.devops.infra.enums.DevopsHostStatus;
import io.choerodon.devops.infra.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
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
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String hostId = WebSocketTool.getHostId(session);

        devopsHostService.baseUpdateHostStatus(Long.parseLong(hostId), DevopsHostStatus.CONNECTED);

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
        HostMsgVO msg = JsonHelper.unmarshalByJackson(payload, HostMsgVO.class);


        HostMsgHandler hostMsgHandler = hostMsgHandlerMap.get(msg.getType());
        if (hostMsgHandler == null) {
            LOGGER.info("unknown msg type, msg {}", msg);
        }
        hostMsgHandler.handler(msg.getHostId(), msg.getCommandId(), msg.getPayload());

    }


}
