package io.choerodon.devops.api.vo;

import java.util.UUID;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 */
public class ContainerVO {
    @ApiModelProperty("容器名称")
    private String name;
    @ApiModelProperty("容器是否就绪")
    private Boolean isReady;
    @ApiModelProperty(hidden = true)
    private String registry;
    @ApiModelProperty("ws查看日志时使用的logId")
    private String logId;

    public ContainerVO() {
        this.logId = UUID.randomUUID().toString();
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }
}
