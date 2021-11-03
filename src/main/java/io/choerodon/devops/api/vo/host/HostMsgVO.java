package io.choerodon.devops.api.vo.host;

import io.swagger.annotations.ApiModelProperty;

/**
 * 主机ws传输数据vo
 */
public class HostMsgVO {
    /**
     * {@link io.choerodon.devops.infra.enums.host.HostMsgEventEnum}
     */
    @ApiModelProperty("消息事件类型")
    private String type;
    @ApiModelProperty("消息内容")
    private String payload;
    @ApiModelProperty("消息来源主机id")
    private String hostId;
    @ApiModelProperty("command_id")
    private Long commandId;

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

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
}
