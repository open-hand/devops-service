package io.choerodon.devops.api.vo;

import java.util.Date;

public class DeployDetailTableVO {

    private String appServiceInstanceCode;
    private String appServiceName;
    private String appServiceCode;
    private String appServiceVersion;
    private Date creationDate;
    private String deployTime;
    private String lastUpdatedName;
    private String status;
    private String error;


    public String getAppServiceInstanceCode() {
        return appServiceInstanceCode;
    }

    public void setAppServiceInstanceCode(String appServiceInstanceCode) {
        this.appServiceInstanceCode = appServiceInstanceCode;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
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


    public String getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(String deployTime) {
        this.deployTime = deployTime;
    }

    public String getLastUpdatedName() {
        return lastUpdatedName;
    }

    public void setLastUpdatedName(String lastUpdatedName) {
        this.lastUpdatedName = lastUpdatedName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
