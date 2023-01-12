package io.choerodon.devops.api.ws.host;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import org.hzero.websocket.constant.ClientWebSocketConstant;
import org.hzero.websocket.vo.MsgVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.api.vo.host.HostMsgVO;
import io.choerodon.devops.api.vo.host.HostSessionVO;
import io.choerodon.devops.api.ws.AbstractSocketHandler;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.eventhandler.host.HostMsgHandler;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.constant.DevOpsWebSocketConstants;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * @author wanghao
 * @since 21-6-24
 */
@Component
public class HostAgentSocketHandler extends AbstractSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostAgentSocketHandler.class);
    private static final String C7N_AGENT_UPGRADE_COUNT_REDIS_KEY = "host:%s";
    protected final Map<String, HostMsgHandler> hostMsgHandlerMap = new HashMap<>();

    @Value("${devops.host.agent-version}")
    private String agentVersion;
    @Value("${devops.host.binary-download-url}")
    private String agentUrl;
    @Value("${devops.host.agent-exit-if-upgrade-failed-time-exceed-max-count:true}")
    private Boolean c7nAgentExitIfUpgradeFailedTimeExceedMaxCount;
    @Value("${devops.host.max-agent-upgrade-failed-time:3}")
    private Integer maxAgentUpgradeFailedTime;

    @Autowired
    private List<HostMsgHandler> hostMsgHandlers;

    @Autowired
    private DevopsHostService devopsHostService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
        HostSessionVO hostSessionVO = new HostSessionVO();

        hostSessionVO.setWebSocketSessionId(session.getId());
        hostSessionVO.setHostId(Long.parseLong(hostId));
        hostSessionVO.setVersion(WebSocketTool.getVersion(session));
        hostSessionVO.setRegisterKey(WebSocketTool.getGroup(session));
        redisTemplate.opsForHash().put(DevopsHostConstants.HOST_SESSION, hostSessionVO.getRegisterKey(), hostSessionVO);

        MsgVO msgVO;
        // 版本不一致，需要升级
        if (!agentVersion.equals(WebSocketTool.getVersion(session))) {
            String redisKey = String.format(C7N_AGENT_UPGRADE_COUNT_REDIS_KEY, hostId);
            Integer count = (Integer) redisTemplate.opsForValue().get(redisKey);
            if (maxAgentUpgradeFailedTime.equals(count) && Boolean.TRUE.equals(c7nAgentExitIfUpgradeFailedTimeExceedMaxCount)) {
                // 表示agent进行了3次尝试升级，都失败了，并且系统设置升级失败超过限制退出，那么agent应该退出。手动处理升级失败问题
                HostMsgVO hostMsgVO = new HostMsgVO();
                hostMsgVO.setType(HostCommandEnum.EXIT_AGENT.value());
                hostMsgVO.setPayload("{}");
                msgVO = (new MsgVO()).setGroup(DevopsHostConstants.GROUP + hostId).setKey(HostCommandEnum.EXIT_AGENT.value()).setMessage(JsonHelper.marshalByJackson(hostMsgVO)).setType(ClientWebSocketConstant.SendType.S_GROUP);
            } else {
                if (count == null) {
                    count = 0;
                }
                count++;
                redisTemplate.opsForValue().set(redisKey, count, 60, TimeUnit.SECONDS);
                DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(Long.parseLong(hostId));
                HostMsgVO hostMsgVO = new HostMsgVO();
                hostMsgVO.setType(HostCommandEnum.UPGRADE_AGENT.value());
                Map<String, String> upgradeInfo = new HashMap<>();
                upgradeInfo.put("upgradeCommand", devopsHostService.getUpgradeString(devopsHostDTO.getProjectId(), devopsHostDTO));
                upgradeInfo.put("version", agentVersion);
                hostMsgVO.setPayload(JsonHelper.marshalByJackson(upgradeInfo));
                msgVO = (new MsgVO()).setGroup(DevopsHostConstants.GROUP + hostId).setKey(HostCommandEnum.UPGRADE_AGENT.value()).setMessage(JsonHelper.marshalByJackson(hostMsgVO)).setType(ClientWebSocketConstant.SendType.S_GROUP);
            }
        } else {
            HostMsgVO hostMsgVO = new HostMsgVO();
            hostMsgVO.setType(HostCommandEnum.INIT_AGENT.value());
            hostMsgVO.setHostId(hostId);

            // 为了保持和其他通过hzero发送的消息结构一致
            msgVO = (new MsgVO()).setGroup(DevopsHostConstants.GROUP + hostId).setKey(HostCommandEnum.INIT_AGENT.value()).setMessage(JsonHelper.marshalByJackson(hostMsgVO)).setType(ClientWebSocketConstant.SendType.S_GROUP);
        }

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
        String registerKey = WebSocketTool.getGroup(session);
        String hostId = WebSocketTool.getHostId(session);

        removeRedisValueByRegisterKeyAndSessionId(registerKey, session.getId(), hostId);

        WebSocketTool.closeSessionQuietly(session);
    }

    private void removeRedisValueByRegisterKeyAndSessionId(String registerKey, String sessionId, Object clusterId) {
        Object registerKeyValue = redisTemplate.opsForHash().get(DevopsHostConstants.HOST_SESSION, registerKey);
        removeRedisValueByRegisterKeyAndSessionId(registerKey, sessionId, registerKeyValue, clusterId);
    }

    private void removeRedisValueByRegisterKeyAndSessionId(String registerKey, String sessionId, Object registerKeyValue, Object clusterId) {
        if (registerKeyValue != null) {
            if (registerKeyValue instanceof HostSessionVO) {
                HostSessionVO hostSessionVO = (HostSessionVO) registerKeyValue;
                // 只有这个registerKey的值中webSocketSessionId和当前sessionId一致时才删除key，避免旧的超时的连接
                // 误将新连接的key删掉（两者是同一个key）
                if (Objects.equals(sessionId, hostSessionVO.getWebSocketSessionId())) {
                    //移除关联关系
                    redisTemplate.opsForHash().delete(DevopsHostConstants.HOST_SESSION, registerKey);
                } else {
                    LOGGER.info("This is an elder session whose registerKey value was updated by a new session. the session cluster id is {}", clusterId);
                }
            } else {
                // 这个逻辑不应该进的
                LOGGER.warn("Value of register key is not of Class 'io.choerodon.devops.api.vo.hostSessionVO', and its real class is {}", registerKeyValue.getClass());
                redisTemplate.opsForHash().delete(DevopsHostConstants.HOST_SESSION, registerKey);
            }
        }
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

    protected void doHandle(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        LOGGER.info("Received agent msg: {}", payload);
        HostMsgVO msg = JsonHelper.unmarshalByJackson(payload, HostMsgVO.class);


        HostMsgHandler hostMsgHandler = hostMsgHandlerMap.get(msg.getType());
        if (hostMsgHandler == null) {
            LOGGER.info("unknown msg type, msg {}", msg.getType());
            return;
        }
        hostMsgHandler.handler(msg.getHostId(), msg.getCommandId(), msg.getPayload());

    }


}
