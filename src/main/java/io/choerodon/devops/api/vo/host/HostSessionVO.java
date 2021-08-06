package io.choerodon.devops.api.vo.host;

import java.io.Serializable;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/6 14:03
 */
public class HostSessionVO implements Serializable {
    private Long hostId;

    private String webSocketSessionId;

    private String version;

    private String registerKey;

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getWebSocketSessionId() {
        return webSocketSessionId;
    }

    public void setWebSocketSessionId(String webSocketSessionId) {
        this.webSocketSessionId = webSocketSessionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRegisterKey() {
        return registerKey;
    }

    public void setRegisterKey(String registerKey) {
        this.registerKey = registerKey;
    }
}
