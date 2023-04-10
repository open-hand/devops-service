package io.choerodon.devops.api.ws;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;
import static org.hzero.websocket.constant.WebSocketConstant.Attributes.GROUP;
import static org.hzero.websocket.constant.WebSocketConstant.Attributes.PROCESSOR;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.hzero.core.util.StringPool;
import org.hzero.websocket.config.ClientWebSocketConfig;
import org.hzero.websocket.handler.DefaultSocketHandler;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeFailureException;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.devops.infra.util.KeyDecryptHelper;
import io.choerodon.devops.infra.util.KeyParseUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Sheep on 2019/7/25.
 */
public class WebSocketTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketTool.class);

    private WebSocketTool() {
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
     * 获取session的processor
     *
     * @param webSocketSession 连接会话
     * @return processor
     */
    public static String getProcessor(WebSocketSession webSocketSession) {
        return TypeUtil.objToString(webSocketSession.getAttributes().get(PROCESSOR));
    }

    public static Long getClusterId(WebSocketSession session) {
        return TypeUtil.objToLong(session.getAttributes().get(CLUSTER_ID));
    }

    public static String getVersion(WebSocketSession session) {
        return TypeUtil.objToString(session.getAttributes().get(VERSION));
    }

    public static String getKey(WebSocketSession session) {
        return TypeUtil.objToString(session.getAttributes().get(KEY));
    }

    public static String getHostId(WebSocketSession session) {
        return TypeUtil.objToString(session.getAttributes().get(HOST_ID));
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
     * 构造对应的agent的连接的group
     *
     * @param group 不带连接前缀的原始随机值
     * @return agent连接的group
     */
    public static String buildHostAgentGroup(String group) {
        return HOST_FROM_AGENT_GROUP_PREFIX + Objects.requireNonNull(group);
    }

    /**
     * 构造对应的前端的连接的group
     *
     * @param group 不带连接前缀的原始随机值
     * @return 前端连接的group
     */
    public static String buildHostFrontGroup(String group) {
        return HOST_FRONT_GROUP_PREFIX + Objects.requireNonNull(group);
    }

    /**
     * Group 形如 from_front:cluster:12.log:q1a
     * 获取rawKey， 用于拼接转发的目的地group  返回 cluster:12.log:q1a
     *
     * @param sessionGroup web socket连接提供的查询参数group
     * @return rawKey
     */
    public static String getRawKey(String sessionGroup) {
        int colonIndex = sessionGroup.indexOf(COLON);
        if (colonIndex == -1) {
            return sessionGroup;
        }
        return sessionGroup.substring(colonIndex);
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
     * 安静的关闭web socket session
     *
     * @param session 会话
     */
    public static void closeSessionQuietly(WebSocketSession session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                LOGGER.warn("close web socket session failed", e);
            }
        }
    }

    /**
     * 通过key关闭前端的session
     *
     * @param key key
     */
    public static void closeFrontSessionByKey(String key) {
        KeySocketSendHelper keySocketSendHelper = ApplicationContextHelper.getContext().getBean(KeySocketSendHelper.class);
        keySocketSendHelper.closeSessionByGroup(buildFrontGroup(key));
    }

    /**
     * 通过key关闭agent的对应session
     *
     * @param key key
     */
    public static void closeAgentSessionByKey(String key) {
        KeySocketSendHelper keySocketSendHelper = ApplicationContextHelper.getContext().getBean(KeySocketSendHelper.class);
        keySocketSendHelper.closeSessionByGroup(buildAgentGroup(key));
    }

    /**
     * 通过key关闭agent的对应session
     *
     * @param key key
     */
    public static void closeHostAgentSessionByKey(String key) {
        KeySocketSendHelper keySocketSendHelper = ApplicationContextHelper.getContext().getBean(KeySocketSendHelper.class);
        keySocketSendHelper.closeSessionByGroup(buildHostAgentGroup(key));
    }

    /**
     * 通过key关闭前端的session
     *
     * @param key key
     */
    public static void closeHostFrontSessionByKey(String key) {
        KeySocketSendHelper keySocketSendHelper = ApplicationContextHelper.getContext().getBean(KeySocketSendHelper.class);
        keySocketSendHelper.closeSessionByGroup(buildHostFrontGroup(key));
    }

    public static void checkGroup(Map<String, Object> attributes) {
        checkParameter(attributes, GROUP);
    }

    public static void checkKey(Map<String, Object> attributes) {
        checkParameter(attributes, KEY);
    }

    public static void checkEnv(Map<String, Object> attributes) {
        checkParameter(attributes, ENV);
    }

    public static void checkKind(Map<String, Object> attributes) {
        checkParameter(attributes, KIND);
    }

    public static void checkName(Map<String, Object> attributes) {
        checkParameter(attributes, NAME);
    }

    public static void checkDescribeId(Map<String, Object> attributes) {
        checkParameter(attributes, DESCRIBE_Id);
    }

    public static void checkPodName(Map<String, Object> attributes) {
        checkParameter(attributes, POD_NAME);
    }

    public static void checkContainerName(Map<String, Object> attributes) {
        checkParameter(attributes, CONTAINER_NAME);
    }

    public static void checkLogId(Map<String, Object> attributes) {
        checkParameter(attributes, LOG_ID);
    }

    public static void checkClusterId(Map<String, Object> attributes) {
        checkParameter(attributes, CLUSTER_ID);
    }

    public static void checkProjectId(Map<String, Object> attributes) {
        checkParameter(attributes, PROJECT_ID);
    }

    private static void checkParameter(Map<String, Object> attributes, String parameter) {
        Object value = attributes.get(parameter);
        if (value == null || isEmptyOrTrimmedEmpty(String.valueOf(attributes.get(parameter)))) {
            throw new HandshakeFailureException(String.format(PARAMETER_NULL_TEMPLATE, parameter));
        }
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

    /**
     * 校验连接参数中的 oauth token 是否正确
     *
     * @param attributes 连接的属性
     * @return true表示token正常，校验通过
     */
    public static boolean preCheckOAuthToken(Map<String, Object> attributes) {
        String token = (String) attributes.get(OAUTH_TOKEN);
        if (StringUtils.isEmpty(token)) {
            LOGGER.debug("OAuth token is absent");
            return false;
        }
        // 获取配置
        ClientWebSocketConfig config = ApplicationContextHelper.getContext().getBean(ClientWebSocketConfig.class);

        try {
            // 请求 oauth 服务获取用户信息
            CustomUserDetails customUserDetails = DefaultSocketHandler.getAuthentication(token, config.getOauthUrl());
            if (customUserDetails == null || customUserDetails.getUserId() == null) {
                LOGGER.info("Ws: user authentication failed, token is invalid");
                return false;
            }

            // 将解析的用户id放入到attributes中
            LOGGER.info("User with name {} and id {} connect from websocket", customUserDetails.getRealName(), customUserDetails.getUserId());
            attributes.put(USER_ID, customUserDetails.getUserId());
            return true;
        } catch (Exception ex) {
            LOGGER.debug("Failed to get user info due to ex", ex);
            return false;
        }
    }

    public static void preProcessAttributeAboutKeyEncryption(Map<String, Object> attributes) {
        Object clusterId = attributes.get(CLUSTER_ID);
        Object group = attributes.get(GROUP);
        Object key = attributes.get(KEY);
        Object describeId = attributes.get(DESCRIBE_Id);
        Object hostId = attributes.get(HOST_ID);

        if (clusterId != null) {
            attributes.put(CLUSTER_ID, KeyDecryptHelper.decryptValueOrIgnoreForWs(String.valueOf(clusterId)));
        }
        if (group != null) {
            // 按照冒号分成两段
            String[] values = String.valueOf(group).split(COLON, 2);
            attributes.put(GROUP, values[0] + COLON + decryptKey(values[1]));
        }
        if (key != null) {
            attributes.put(KEY, decryptKey(String.valueOf(key)));
        }
        if (describeId != null) {
            attributes.put(DESCRIBE_Id, KeyDecryptHelper.decryptValueOrIgnoreForWs(String.valueOf(describeId)));
        }
        if (hostId != null) {
            attributes.put(HOST_ID, KeyDecryptHelper.decryptValueOrIgnoreForWs(String.valueOf(hostId)));
        }
    }

    private static String decryptKey(String key) {
        Map<String, String> keyPairs = KeyParseUtil.parseKeyInOrder(key);
        new HashSet<>(keyPairs.keySet()).forEach(k -> {
            String value = keyPairs.get(k);
            if (value != null) {
                keyPairs.put(k, String.valueOf(KeyDecryptHelper.decryptValueOrIgnoreForWs(value)));
            }
        });
        StringBuilder result = new StringBuilder();
        keyPairs.forEach((k, v) -> {
            result.append(k).append(COLON).append(v);
            result.append(StringPool.DOT);
        });
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    public static void checkHostId(Map<String, Object> attributes) {
        checkParameter(attributes, HOST_ID);
    }

    public static String getToken(WebSocketSession session) {
        return TypeUtil.objToString(session.getAttributes().get(TOKEN));
    }
}
