package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:35 2019/4/4
 * Description:
 */
public class PipelineAppDeployVO {
    private Long id;
    private Long applicationId;
    private List<String> triggerVersion;
    private Long envId;
    private Long instanceId;
    private Long stageId;
    private String instanceName;
    private String value;
    private Long valueId;
    private Long projectId;
    private String appName;
    private String envName;
    private Long objectVersionNumber;
    private Long appDeployId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public List<String> getTriggerVersion() {
        return triggerVersion;
    }

    public void setTriggerVersion(List<String> triggerVersion) {
        this.triggerVersion = triggerVersion;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getAppDeployId() {
        return appDeployId;
    }

    public void setAppDeployId(Long appDeployId) {
        this.appDeployId = appDeployId;
    }
}
