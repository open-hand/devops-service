package io.choerodon.devops.api.ws.exec.devops;

import io.choerodon.devops.api.ws.exec.ExecMessageHandler;
import io.choerodon.websocket.receive.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/7/26.
 */

@Component
public class  DevopsExecMessageHandler implements MessageHandler<BinaryMessage> {


    @Autowired
    private ExecMessageHandler execMessageHandler;

    @Override
    public void handle(WebSocketSession webSocketSession, BinaryMessage message) {
        execMessageHandler.handle(webSocketSession,message);
    }

    @Override
    public String matchPath() {
        return "/devops/exec";
    }

}
