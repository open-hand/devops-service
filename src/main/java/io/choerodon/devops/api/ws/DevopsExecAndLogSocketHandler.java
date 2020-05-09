package io.choerodon.devops.api.ws;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeFailureException;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.PipeRequestVO;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Sheep on 2019/7/25.
 */
@Component
public class DevopsExecAndLogSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DevopsExecAndLogSocketHandler.class);
    public static final String KUBERNETES_GET_LOGS = "kubernetes_get_logs";
    public static final String EXEC_COMMAND = "kubernetes_exec";

    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private KeySocketSendHelper keySocketSendHelper;


    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
        HttpServletRequest request = servletRequest.getServletRequest();

        //校验ws连接参数是否正确
        String group = WebSocketTool.getGroup(request);

        String env = request.getParameter("env");
        String podName = request.getParameter("podName");
        String containerName = request.getParameter("containerName");
        String logId = request.getParameter("logId");
        //校验ws连接参数是否正确
        if (WebSocketTool.isEmptyOrTrimmedEmpty(group)) {
            throw new CommonException("group is unexpectedly null");
        }
        if (WebSocketTool.isEmptyOrTrimmedEmpty(env)) {
            throw new HandshakeFailureException("Env is null!");
        }
        if (WebSocketTool.isEmptyOrTrimmedEmpty(podName)) {
            throw new HandshakeFailureException("PodName is null!");
        }
        if (WebSocketTool.isEmptyOrTrimmedEmpty(containerName)) {
            throw new HandshakeFailureException("ContainerName is null!");
        }
        if (WebSocketTool.isEmptyOrTrimmedEmpty(logId)) {
            throw new HandshakeFailureException("LogId is null!");
        }

        return true;
    }

    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //解析参数列表
        Map<String, Object> attribute = webSocketSession.getAttributes();

        String registerKey = WebSocketTool.getGroup(webSocketSession);
        String processor = WebSocketTool.getProcessor(webSocketSession);

        String path = webSocketSession.getUri() == null ? null : webSocketSession.getUri().getPath();
        logger.info("Connection established from client. The registerKey is {} and the path is {}", registerKey, path);

        // 通过GitOps的ws连接，通知agent建立与前端对应的ws连接
        PipeRequestVO pipeRequest = new PipeRequestVO(
                attribute.get(POD_NAME).toString(),
                attribute.get(CONTAINER_NAME).toString(),
                attribute.get(LOG_ID).toString(),
                attribute.get(ENV).toString());

        Long clusterId = TypeUtil.objToLong(WebSocketTool.getLastValueInColonPair(TypeUtil.objToString(attribute.get(KEY))));
        if (FRONT_LOG.equals(processor)) {
            agentCommandService.startLogOrExecConnection(KUBERNETES_GET_LOGS, registerKey, pipeRequest, clusterId);
        } else {
            agentCommandService.startLogOrExecConnection(EXEC_COMMAND, registerKey, pipeRequest, clusterId);
        }
    }

    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        String registerKey = WebSocketTool.getGroup(webSocketSession);
        String rawKey = WebSocketTool.getRawKey(registerKey);

        // 关闭agent那边的web socket Session
        keySocketSendHelper.closeSessionByGroup(WebSocketTool.buildAgentGroup(rawKey));

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
