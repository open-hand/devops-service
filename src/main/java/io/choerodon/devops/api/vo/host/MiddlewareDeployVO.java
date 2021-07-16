package io.choerodon.devops.api.vo.host;

public class MiddlewareDeployVO {
    private String MiddlewareType;
    private String DeployShell;
    private String InstanceId;
    private String CommandId;

    public String getMiddlewareType() {
        return MiddlewareType;
    }

    public void setMiddlewareType(String middlewareType) {
        MiddlewareType = middlewareType;
    }

    public String getDeployShell() {
        return DeployShell;
    }

    public void setDeployShell(String deployShell) {
        DeployShell = deployShell;
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
}
