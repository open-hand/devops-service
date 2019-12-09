package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Sheep on 2019/7/25.
 */
public class AgentMsgVO {

    private String key;
    private String type;
    private String payload;
    private Integer msgType;
    @Deprecated
    private Long commandId;
    @ApiModelProperty("实例对应的commandId/非必填")
    private Long command;
    private String clusterId;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    @Deprecated
    public Long getCommandId() {
        return commandId;
    }

    @Deprecated
    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Long getCommand() {
        return command;
    }

    public void setCommand(Long command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "AgentMsgVO{" +
                "key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", payload='" + payload + '\'' +
                ", msgType=" + msgType +
                ", commandId=" + commandId +
                ", command=" + command +
                ", clusterId='" + clusterId + '\'' +
                '}';
    }
}
