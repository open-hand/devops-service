package io.choerodon.devops.api.ws;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeFailureException;

import io.choerodon.devops.api.vo.PipeRequestVO;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.websocket.helper.WebSocketHelper;
import io.choerodon.websocket.send.MessageSender;

/**
 * Created by Sheep on 2019/7/25.
 */
@Component
public class DevopsExecAndLogSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DevopsExecAndLogSocketHandler.class);
    public static final String KUBERNETES_GET_LOGS = "kubernetes_get_logs";
    public static final String EXEC_COMMAND = "kubernetes_exec";
    public static final String CLOSE_AGENT_SESSION = "closeAgentSession";


    @Autowired
    @Lazy
    private WebSocketHelper webSocketHelper;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private MessageSender messageSender;


    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
        HttpServletRequest request = servletRequest.getServletRequest();

        //校验ws连接参数是否正确
        String key = request.getParameter("key");
        String env = request.getParameter("env");
        String podName = request.getParameter("podName");
        String containerName = request.getParameter("containerName");
        String logId = request.getParameter("logId");
        if (key == null || key.trim().isEmpty()) {
            throw new HandshakeFailureException("Key is null");
        }
        if (env == null || env.trim().isEmpty()) {
            throw new HandshakeFailureException("Env is null!");
        }
        if (podName == null || podName.trim().isEmpty()) {
            throw new HandshakeFailureException("PodName is null!");
        }
        if (containerName == null || containerName.trim().isEmpty()) {
            throw new HandshakeFailureException("ContainerName is null!");
        }
        if (logId == null || logId.trim().isEmpty()) {
            throw new HandshakeFailureException("LogId is null!");
        }

        return true;
    }

    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //解析参数列表，并存储


        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);

        String registerKey = TypeUtil.objToString(attribute.get("key"));

        //通知agent建立与前端同样的ws连接
        PipeRequestVO pipeRequest = new PipeRequestVO(attribute.get("podName").toString(), attribute.get("containerName").toString(), attribute.get("logId").toString(), attribute.get("env").toString());
        Long clusterId = TypeUtil.objToLong(registerKey.split("\\.")[0].split(":")[1]);
        if (webSocketSession.getUri().getPath().equals("/devops/log")) {
            agentCommandService.startLogOrExecConnection(KUBERNETES_GET_LOGS, registerKey, pipeRequest, clusterId);
        } else {
            agentCommandService.startLogOrExecConnection(EXEC_COMMAND, registerKey, pipeRequest, clusterId);
        }

        registerKey = "from_devops:" + registerKey;
        //将websocketSession和关联的key做关联
        webSocketHelper.subscribe(registerKey, webSocketSession);

    }

    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {

        Map<String, Object> attribute = WebSocketTool.getAttribute(webSocketSession);
        String registerKey = TypeUtil.objToString(attribute.get("key"));

        //关闭agent那边的websocketSession
        messageSender.closeSessionByKey("from_agent:" + registerKey);

        closeSession(webSocketSession);
    }

    private void closeSession(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            logger.error("session closed failed!", e);
        }

    }


}
