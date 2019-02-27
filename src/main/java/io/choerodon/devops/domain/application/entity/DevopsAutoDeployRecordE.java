package io.choerodon.devops.domain.application.entity;

import java.sql.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:09 2019/2/27
 * Description:
 */
public class DevopsAutoDeployRecordE {
    private Long id;
    private Long autoDeployId;
    private String taskName;
    private String status;
    private String instanceName;
    private String version;
    private String envName;
    private String appName;
    private Date lastUpdateDate;
    private Long envId;
    private Long appId;
    private Boolean envStatus;


    public DevopsAutoDeployRecordE() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAutoDeployId() {
        return autoDeployId;
    }

    public void setAutoDeployId(Long autoDeployId) {
        this.autoDeployId = autoDeployId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }
}
