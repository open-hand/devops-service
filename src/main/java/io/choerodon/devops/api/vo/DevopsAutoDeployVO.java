package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:24 2019/2/26
 * Description:
 */
public class DevopsAutoDeployVO {
    private Long id;
    private String taskName;
    private Long appServiceId;
    private List<String> triggerVersion;
    private Long envId;
    @NotNull(message = "error.value.is.empty")
    private String value;
    private Long valueId;
    private Long projectId;
    private String appName;
    private String envName;
    private Long objectVersionNumber;
    private Date lastUpdateDate;
    private Boolean envStatus;
    private Long instanceId;
    private Integer isEnabled;
    private String instanceName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
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

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
