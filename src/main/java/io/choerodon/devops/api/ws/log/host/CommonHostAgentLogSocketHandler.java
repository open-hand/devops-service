package io.choerodon.devops.api.ws.log.host;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;
import static io.choerodon.devops.infra.enums.host.HostCommandEnum.HOST_AGENT_LOG;

import java.util.Map;

import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.PipeRequestVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.app.service.DevopsHostUserPermissionService;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * Created by Sheep on 2019/7/25.
 */
@Component
public class CommonHostAgentLogSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommonHostAgentLogSocketHandler.class);

    @Autowired
    private DevopsHostUserPermissionService devopsHostUserPermissionService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    @Lazy
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private DevopsHostService devopsHostService;

    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, Map<String, Object> attributes) {
        //校验ws连接参数是否正确
        WebSocketTool.checkKey(attributes);
        WebSocketTool.checkGroup(attributes);
        WebSocketTool.checkHostId(attributes);

        return true;
    }

    private boolean checkUserPermission(Map<String, Object> attributes) {
        // 校验用户的权限
        Long projectId = Long.parseLong(String.valueOf(attributes.get(PROJECT_ID)));
        Long hostId = Long.parseLong(String.valueOf(attributes.get(HOST_ID)));
        Long userId = Long.parseLong(String.valueOf(attributes.get(USER_ID)));

        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostService.baseQuery(hostId), userId);

        return true;
    }

    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //解析参数列表
        Map<String, Object> attribute = webSocketSession.getAttributes();
        //解析参数列表
        String frontSessionGroup = WebSocketTool.getGroup(webSocketSession);
        String processor = WebSocketTool.getProcessor(webSocketSession);

        logger.info("Connection established from client. The sessionGroup is {} and the processor is {}", frontSessionGroup, processor);

        // 通过GitOps的ws连接，通知agent建立与前端对应的ws连接
        String hostId = WebSocketTool.getHostId(webSocketSession);
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(Long.parseLong(hostId));

        if (HOST_FRONT_LOG.equals(processor)) {
            // 通过GitOps的ws连接，通知agent建立与前端对应的ws连接
            PipeRequestVO pipeRequest = new PipeRequestVO(
                    attribute.get(LOG_ID).toString(),
                    false);
            startLogConnection(devopsHostDTO, HOST_AGENT_LOG.value(), pipeRequest);
        } else if (HOST_FRONT_DOWNLOAD_LOG.equals(processor)) {
            // 通过GitOps的ws连接，通知agent建立与前端对应的ws连接
            PipeRequestVO pipeRequest = new PipeRequestVO(
                    attribute.get(LOG_ID).toString(),
                    true);
            startLogConnection(devopsHostDTO, HostCommandEnum.HOST_AGENT_DOWNLOAD_LOG.value(), pipeRequest);
        }
    }

    public void afterHostFrontConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        // 关闭agent那边的web socket Session
        WebSocketTool.closeHostAgentSessionByKey(WebSocketTool.getKey(webSocketSession));
        WebSocketTool.closeSessionQuietly(webSocketSession);
    }

    public void afterHostAgentConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        // 关闭front那边的web socket Session
        WebSocketTool.closeHostFrontSessionByKey(WebSocketTool.getKey(webSocketSession));
        WebSocketTool.closeSessionQuietly(webSocketSession);
    }

    public void startLogConnection(DevopsHostDTO hostDTO, String commandType, PipeRequestVO pipeRequestVO) {
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostDTO.getId()));
        hostAgentMsgVO.setType(commandType);
        hostAgentMsgVO.setCommandId(commandType);
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(pipeRequestVO));
        sendHostDeployMsg(hostDTO, hostAgentMsgVO, commandType);
    }

    private void sendHostDeployMsg(DevopsHostDTO hostDTO, HostAgentMsgVO hostAgentMsgVO, String commandType) {
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostDTO.getId(),
                commandType,
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }
}
