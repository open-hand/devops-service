package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class EndPointPortVO {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("端口号")
    private int port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
