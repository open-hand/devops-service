package io.choerodon.devops.infra.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.websocket.helper.EnvListener;
import io.choerodon.websocket.helper.EnvSession;

/**
 * Creator: Runge
 * Date: 2018/6/1
 * Time: 15:47
 * Description:
 */

@Service
public class EnvUtil {

    @Value("${agent.version}")
    private String agentExpectVersion;

    /**
     * 检查环境是否链接
     *
     * @param envId       环境ID
     * @param envListener EnvListener
     */
    public void checkEnvConnection(Long envId, EnvListener envListener) {
        Map<String, EnvSession> connectedEnv = envListener.connectedEnv();
        Boolean envConnected = connectedEnv.entrySet().stream()
                .anyMatch(t -> envId.equals(t.getValue().getEnvId())
                        && agentExpectVersion.compareTo(
                        t.getValue().getVersion() == null ? "0" : t.getValue().getVersion()) < 1);
        if (!envConnected) {
            throw new CommonException("error.env.disconnect");
        }
    }

    /**
     * 环境链接列表
     *
     * @param envListener EnvListener
     * @return 环境链接列表
     */
    public List<Long> getConnectedEnvList(EnvListener envListener) {
        Map<String, EnvSession> connectedEnv = envListener.connectedEnv();
        return connectedEnv.entrySet().stream()
                .map(t -> t.getValue().getEnvId())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 环境更新列表
     *
     * @param envListener EnvListener
     * @return 环境更新列表
     */
    public List<Long> getUpdatedEnvList(EnvListener envListener) {
        Map<String, EnvSession> connectedEnv = envListener.connectedEnv();
        return connectedEnv.entrySet().stream()
                .filter(t -> agentExpectVersion.compareTo(
                        t.getValue().getVersion() == null ? "0" : t.getValue().getVersion()) < 1)
                .map(t -> t.getValue().getEnvId())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
