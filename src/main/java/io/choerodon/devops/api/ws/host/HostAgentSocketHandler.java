package io.choerodon.devops.api.ws.host;

import io.choerodon.devops.api.vo.ClusterSessionVO;
import io.choerodon.devops.api.vo.host.HostMsgVO;
import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.app.service.HostMsgHandler;
import io.choerodon.devops.infra.constant.DevOpsWebSocketConstants;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.choerodon.devops.infra.handler.ClusterConnectionHandler.CLUSTER_SESSION;

/**
 * @author wanghao
 * @since 21-6-24
 */
@Component
public class HostAgentSocketHandler extends AbstractSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostAgentSocketHandler.class);

    private final Map<String, HostMsgHandler> hostMsgHandlerMap = new HashMap<>();


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(Long.parseLong(hostId));

        // 更新主机连接状态
        if (devopsHostDTO != null) {
//            devopsHostDTO.setHostStatus();
        }



    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // todo 更新状态为未连接
        String registerKey = WebSocketTool.getGroup(session);

        removeRedisValueByRegisterKeyAndSessionId(registerKey, session.getId(), WebSocketTool.getClusterId(session));

        LOGGER.info("After connection closed, the cluster session with key {} is to be closed.", registerKey);
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

    private void removeRedisValueByRegisterKeyAndSessionId(String registerKey, String sessionId, Object clusterId) {
        Object registerKeyValue = redisTemplate.opsForHash().get(CLUSTER_SESSION, registerKey);
        removeRedisValueByRegisterKeyAndSessionId(registerKey, sessionId, registerKeyValue, clusterId);
    }

    private void removeRedisValueByRegisterKeyAndSessionId(String registerKey, String sessionId, Object registerKeyValue, Object clusterId) {
        if (registerKeyValue != null) {
            if (registerKeyValue instanceof ClusterSessionVO) {
                ClusterSessionVO clusterSessionVO = (ClusterSessionVO) registerKeyValue;
                // 只有这个registerKey的值中webSocketSessionId和当前sessionId一致时才删除key，避免旧的超时的连接
                // 误将新连接的key删掉（两者是同一个key）
                if (Objects.equals(sessionId, clusterSessionVO.getWebSocketSessionId())) {
                    //移除关联关系
                    redisTemplate.opsForHash().delete(CLUSTER_SESSION, registerKey);
                } else {
                    LOGGER.info("This is an elder session whose registerKey value was updated by a new session. the session cluster id is {}", clusterId);
                }
            } else {
                // 这个逻辑不应该进的
                LOGGER.warn("Value of register key is not of Class 'io.choerodon.devops.api.vo.ClusterSessionVO', and its real class is {}", registerKeyValue.getClass());
                redisTemplate.opsForHash().delete(CLUSTER_SESSION, registerKey);
            }
        }
    }


}
