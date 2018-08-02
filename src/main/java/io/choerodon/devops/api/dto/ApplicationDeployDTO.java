package io.choerodon.devops.api.dto;

public class ApplicationDeployDTO {
    private Long appVerisonId;
    private Long environmentId;
    private String values;
    private Long appId;
    private String type;
    private Long appInstanceId;
    private String instanceName;

    public Long getAppVerisonId() {
        return appVerisonId;
    }

    public void setAppVerisonId(Long appVerisonId) {
        this.appVerisonId = appVerisonId;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAppInstanceId() {
        return appInstanceId;
    }

    public void setAppInstanceId(Long appInstanceId) {
        this.appInstanceId = appInstanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
