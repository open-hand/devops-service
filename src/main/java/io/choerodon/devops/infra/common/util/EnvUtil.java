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

    public static int compareVersion(String a, String b) {
        if (!a.contains("-") && !b.contains("-")) {
            return compareTag(a, b);
        } else if (a.contains("-") && b.contains("-")) {
            String[] a1 = a.split("-");
            String[] b1 = b.split("-");
            int compareResult = compareTag(a1[0], b1[0]);
            if (compareResult == 0) {
                if (TypeUtil.objToLong(b1[1]) > TypeUtil.objToLong(a1[1])) {
                    return 1;
                } else if (TypeUtil.objToLong(b1[1]) < TypeUtil.objToLong(a1[1])) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return compareResult;
            }
        }
        return 1;
    }

    public static int compareTag(String a, String b) {
        String[] a1 = a.split("\\.");
        String[] b1 = b.split("\\.");
        if (TypeUtil.objToLong(b1[0]) > TypeUtil.objToLong(a1[0])) {
            return 1;
        } else if (TypeUtil.objToLong(b1[0]) < TypeUtil.objToLong(a1[0])) {
            return -1;
        } else {
            if (TypeUtil.objToLong(b1[1]) > TypeUtil.objToLong(a1[1])) {
                return 1;
            } else if (TypeUtil.objToLong(b1[1]) < TypeUtil.objToLong(a1[1])) {
                return -1;
            } else {
                if (TypeUtil.objToLong(b1[2]) > TypeUtil.objToLong(a1[2])) {
                    return 1;
                } else if (TypeUtil.objToLong(b1[2]) < TypeUtil.objToLong(a1[2])) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    /**
     * 检查环境是否链接
     *
     * @param envId       环境ID
     * @param envListener EnvListener
     */
    public void checkEnvConnection(Long envId, EnvListener envListener) {
        Map<String, EnvSession> connectedEnv = envListener.connectedEnv();
        Boolean envConnected = connectedEnv.entrySet().parallelStream()
                .anyMatch(t -> envId.equals(t.getValue().getEnvId())
                        && compareVersion(t.getValue().getVersion() == null ? "0" : t.getValue().getVersion(), agentExpectVersion) != 1);
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
        return connectedEnv.entrySet().parallelStream()
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
                .filter(t -> compareVersion(t.getValue().getVersion() == null ? "0" : t.getValue().getVersion(), agentExpectVersion) != 1)
                .map(t -> t.getValue().getEnvId())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
