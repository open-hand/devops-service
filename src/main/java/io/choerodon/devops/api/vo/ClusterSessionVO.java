package io.choerodon.devops.api.vo;

import java.io.Serializable;

/**
 * Created by Sheep on 2019/7/25.
 */
public class ClusterSessionVO implements Serializable {
    /**
     * web socket session的id
     * {@link org.springframework.web.socket.WebSocketSession#getId()}
     */
    private String webSocketSessionId;
    private String registerKey;
    private Long clusterId;
    private String version;


    public String getRegisterKey() {
        return registerKey;
    }

    public void setRegisterKey(String registerKey) {
        this.registerKey = registerKey;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWebSocketSessionId() {
        return webSocketSessionId;
    }

    public void setWebSocketSessionId(String webSocketSessionId) {
        this.webSocketSessionId = webSocketSessionId;
    }
}
