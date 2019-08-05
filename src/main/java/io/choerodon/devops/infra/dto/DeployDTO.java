package io.choerodon.devops.infra.dto;

import java.util.Date;

public class DeployDTO {

    private String appServiceInstanceCode;
    private String appServiceCode;
    private String appServiceName;
    private String appServiceVersion;
    private Date creationDate;
    private String status;
    private String error;
    private Date lastUpdateDate;
    private Long lastUpdatedBy;
    private Long createdBy;

    public String getAppServiceInstanceCode() {
        return appServiceInstanceCode;
    }

    public void setAppServiceInstanceCode(String appServiceInstanceCode) {
        this.appServiceInstanceCode = appServiceInstanceCode;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceVersion() {
        return appServiceVersion;
    }

    public void setAppServiceVersion(String appServiceVersion) {
        this.appServiceVersion = appServiceVersion;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
