package io.choerodon.devops.api.vo.host;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 14:37
 */
public class HostAgentMsgVO {
    private String type;
    private String payload;
    private String commandId;
    private String hostId;
    private String configSetting;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getConfigSetting() {
        return configSetting;
    }

    public void setConfigSetting(String configSetting) {
        this.configSetting = configSetting;
    }
}
