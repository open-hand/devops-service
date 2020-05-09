package io.choerodon.devops.api.ws;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;
import static org.hzero.websocket.constant.WebSocketConstant.Attributes.GROUP;
import static org.hzero.websocket.constant.WebSocketConstant.Attributes.PROCESSOR;

import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.hzero.websocket.redis.BrokerServerSessionRedis;
import org.hzero.websocket.registry.BaseSessionRegistry;
import org.hzero.websocket.registry.GroupSessionRegistry;
import org.hzero.websocket.vo.ClientVO;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Sheep on 2019/7/25.
 */
public class WebSocketTool {

    private WebSocketTool() {
    }

    public static Map<String, Object> getAttribute(WebSocketSession webSocketSession) {
        Map<String, Object> attribute = new HashMap<>();
        Arrays.asList(webSocketSession.getUri().getQuery().split("&")).forEach(s -> {
            String[] result = s.split("=");
            attribute.put(result[0], result[1]);
        });
        return attribute;
    }

    /**
     * 获取session的group
     *
     * @param webSocketSession 连接会话
     * @return group
     */
    public static String getGroup(WebSocketSession webSocketSession) {
        return TypeUtil.objToString(webSocketSession.getAttributes().get(GROUP));
    }

    /**
     * 获取握手时的Group参数
     *
     * @param httpServletRequest 请求
     * @return group值
     */
    public static String getGroup(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter(GROUP);
    }


    /**
     * 获取session的processor
     *
     * @param webSocketSession 连接会话
     * @return processor
     */
    public static String getProcessor(WebSocketSession webSocketSession) {
        return TypeUtil.objToString(webSocketSession.getAttributes().get(PROCESSOR));
    }


    /**
     * 获取集群id
     *
     * @param key 从查询参数上的key
     * @return 集群id
     */
    public static Long getClusterId(String key) {
        return TypeUtil.objToLong(getLastValueInColonPair(key));
    }

    /**
     * 构造对应的agent的连接的group
     *
     * @param rawKey 不带连接前缀的原始随机值
     * @return agent连接的group
     */
    public static String buildAgentGroup(String rawKey) {
        return FROM_AGENT_GROUP_PREFIX + Objects.requireNonNull(rawKey);
    }

    /**
     * 构造对应的前端的连接的group
     *
     * @param rawKey 不带连接前缀的原始随机值
     * @return 前端连接的group
     */
    public static String buildFrontGroup(String rawKey) {
        return FROM_FRONT_GROUP_PREFIX + Objects.requireNonNull(rawKey);
    }

    /**
     * 从形如 a:b:c 的字符串中获取最后一截，
     *
     * @param colonPair 输入
     * @return 最后一截
     */
    public static String getLastValueInColonPair(String colonPair) {
        Assert.hasLength(colonPair, "ColonPair should be not null.");
        String[] pairs = colonPair.split(COLON);
        return pairs[pairs.length - 1];
    }

    /**
     * Group格式： log:${rawKey}  形如 from_front:ab124ac
     * 获取rawKey， 用于拼接转发的目的地group  返回 ab124ac
     *
     * @param sessionGroup web socket连接提供的查询参数group
     * @return rawKey
     */
    public static String getRawKey(String sessionGroup) {
        return getLastValueInColonPair(sessionGroup);
    }

    /**
     * 字符串为null或者为长度为0或者trim之后长度为0
     *
     * @param content 待判断的字符串
     * @return true表示是
     */
    public static boolean isEmptyOrTrimmedEmpty(String content) {
        return StringUtils.isEmpty(content) || content.trim().isEmpty();
    }

    /**
     * 获取本地监听这个group的Session
     *
     * @param group group
     * @return 本地关联这个group的ws session
     */
    public static List<WebSocketSession> getLocalSessionsByGroup(String group) {
        String brokerId = BaseSessionRegistry.getBrokerId();
        List<ClientVO> clientList = BrokerServerSessionRedis.getCache(brokerId, group);
        return clientList.stream()
                .map(ClientVO::getSessionId)
                .map(GroupSessionRegistry::getSession)
                .collect(Collectors.toList());
    }

    /**
     * 解决shell里面前端遇到\r不能换行的问题。目前解决方案是将返回结果中除了首尾部的\r替换成\r\n
     */
    public static String replaceR(StringBuilder a, int index) {
        int lastIndex = a.lastIndexOf("\r");
        if (lastIndex == -1 || index >= a.length() - 1) {
            return a.toString();
        }
        int indexResult = a.indexOf("\r", index);
        if (indexResult >= 0) {
            if (indexResult != a.length() - 1) {
                String r = a.substring(indexResult + 1, indexResult + 2);
                if (!r.equals("\n")) {
                    if (indexResult > 0) {
                        a = a.replace(indexResult, indexResult + 1, "\r\n");
                    }
                    return replaceR(a, indexResult + 1);
                } else {
                    return replaceR(a, indexResult + 1);
                }
            } else {
                a = a.replace(indexResult, indexResult + 1, "\r\n");
            }
        }
        return a.toString();
    }
}
