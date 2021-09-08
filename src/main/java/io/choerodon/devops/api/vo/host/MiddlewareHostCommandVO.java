package io.choerodon.devops.api.vo.host;

import io.swagger.annotations.ApiModelProperty;

public class MiddlewareHostCommandVO {
    @ApiModelProperty("中间件类型 Redis/MySQL")
    private String MiddlewareType;
    @ApiModelProperty("部署模式 MiddlewareDeployModeEnum")
    private String mode;
    @ApiModelProperty("执行脚本")
    private String shell;
    @ApiModelProperty("实例记录")
    private String InstanceId;
    @ApiModelProperty("操作记录id")
    private String CommandId;
    @ApiModelProperty("中间件名称")
    private String name;

    public String getMiddlewareType() {
        return MiddlewareType;
    }

    public void setMiddlewareType(String middlewareType) {
        MiddlewareType = middlewareType;
    }

    public String getShell() {
        return shell;
    }

    public void setShell(String shell) {
        this.shell = shell;
    }

    public String getInstanceId() {
        return InstanceId;
    }

    public void setInstanceId(String instanceId) {
        InstanceId = instanceId;
    }

    public String getCommandId() {
        return CommandId;
    }

    public void setCommandId(String commandId) {
        CommandId = commandId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
