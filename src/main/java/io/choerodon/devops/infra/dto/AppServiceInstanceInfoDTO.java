package io.choerodon.devops.infra.dto;

import java.util.Date;

/**
 * 展示实例详情页面的单个实例的信息
 * @author zmf
 */
public class AppServiceInstanceInfoDTO {
    private Long id;
    private String code;
    private String status;
    private Long podCount;
    private Long podRunningCount;
    private Long appServiceId;
    private Long appServiceVersionId;
    private String appServiceName;
    private String versionName;
    private Date lastUpdateDate;
    private Long objectVersionNumber;
    private Long envId;
    private Long clusterId;
    private Long commandVersionId;
    private String commandVersion;
    private String commandType;
    private String commandStatus;
    private String error;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPodCount() {
        return podCount;
    }

    public void setPodCount(Long podCount) {
        this.podCount = podCount;
    }

    public Long getPodRunningCount() {
        return podRunningCount;
    }

    public void setPodRunningCount(Long podRunningCount) {
        this.podRunningCount = podRunningCount;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getCommandType() { return commandType; }

    public void setCommandType(String commandType) { this.commandType = commandType; }

    public Long getCommandVersionId() { return commandVersionId; }

    public void setCommandVersionId(Long commandVersionId) { this.commandVersionId = commandVersionId; }

    public String getCommandStatus() { return commandStatus; }

    public void setCommandStatus(String commandStatus) { this.commandStatus = commandStatus; }

    public String getCommandVersion() { return commandVersion; }

    public void setCommandVersion(String commandVersion) { this.commandVersion = commandVersion; }

    public String getError() { return error; }

    public void setError(String error) { this.error = error; }
}
