package io.choerodon.devops.api.vo.iam.entity;

import java.util.Date;

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
    private String instanceStatus;
    private Long versionId;
    private Long instanceId;
    private Long projectId;

    public DevopsAutoDeployRecordE() {
    }

    public DevopsAutoDeployRecordE(Long autoDeployId, String taskName, String status, Long envId, Long appId, Long versionId, Long instanceId, Long projectId) {
        this.autoDeployId = autoDeployId;
        this.taskName = taskName;
        this.status = status;
        this.envId = envId;
        this.appId = appId;
        this.versionId = versionId;
        this.instanceId = instanceId;
        this.projectId = projectId;
    }

    public DevopsAutoDeployRecordE(Long id, String status, String instanceName, Long instanceId) {
        this.id = id;
        this.status = status;
        this.instanceName = instanceName;
        this.instanceId = instanceId;
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

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
