package io.choerodon.devops.api.ws;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;

import java.util.Map;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.PipeRequestVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.util.EurekaInstanceUtil;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * Created by Sheep on 2019/7/25.
 */
@Component
public class DevopsExecAndLogSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DevopsExecAndLogSocketHandler.class);

    @Resource
    private DevopsEnvPodService devopsEnvPodService;
    @Resource
    private AgentCommandService agentCommandService;


    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, Map<String, Object> attributes) {
        //校验ws连接参数是否正确
        WebSocketTool.checkKey(attributes);
        WebSocketTool.checkGroup(attributes);
        WebSocketTool.checkEnv(attributes);
        WebSocketTool.checkPodName(attributes);
        WebSocketTool.checkContainerName(attributes);
        WebSocketTool.checkLogId(attributes);
        WebSocketTool.checkClusterId(attributes);
        WebSocketTool.checkProjectId(attributes);

        return checkUserPermission(attributes);
    }

    private boolean checkUserPermission(Map<String, Object> attributes) {
        // 校验用户的权限
        Long projectId = Long.parseLong(String.valueOf(attributes.get(PROJECT_ID)));
        Long clusterId = Long.parseLong(String.valueOf(attributes.get(CLUSTER_ID)));
        Long userId = Long.parseLong(String.valueOf(attributes.get(USER_ID)));
        String envCode = String.valueOf(attributes.get(ENV));
        String podName = String.valueOf(attributes.get(POD_NAME));

        return devopsEnvPodService.checkLogAndExecPermission(projectId, clusterId, envCode, userId, podName);
    }

    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //解析参数列表
        Map<String, Object> attribute = webSocketSession.getAttributes();

        String frontSessionGroup = WebSocketTool.getGroup(webSocketSession);
        String key = WebSocketTool.getKey(webSocketSession);
        String processor = WebSocketTool.getProcessor(webSocketSession);

        logger.info("Connection established from client. The sessionGroup is {} and the processor is {}", frontSessionGroup, processor);

        // 通过GitOps的ws连接，通知agent建立与前端对应的ws连接
        PipeRequestVO pipeRequest = new PipeRequestVO(
                attribute.get(POD_NAME).toString(),
                attribute.get(CONTAINER_NAME).toString(),
                attribute.get(LOG_ID).toString(),
                attribute.get(ENV).toString(),
                EurekaInstanceUtil.getInstanceId(),
                attribute.get(PREVIOUS) != null && Boolean.parseBoolean(attribute.get(PREVIOUS).toString()));

        Long clusterId = WebSocketTool.getClusterId(webSocketSession);

        if (FRONT_LOG.equals(processor)) {
            agentCommandService.startLogOrExecConnection(KUBERNETES_GET_LOGS, key, pipeRequest, clusterId);
        } else if (FRONT_DOWNLOAD_LOG.equals(processor)) {
            agentCommandService.startLogOrExecConnection(KubernetesDownloadLogs, key, pipeRequest, clusterId);
        } else {
            agentCommandService.startLogOrExecConnection(EXEC_COMMAND, key, pipeRequest, clusterId);
        }
    }

    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        // 关闭agent那边的web socket Session
        WebSocketTool.closeAgentSessionByKey(WebSocketTool.getKey(webSocketSession));
        WebSocketTool.closeSessionQuietly(webSocketSession);
    }
}
