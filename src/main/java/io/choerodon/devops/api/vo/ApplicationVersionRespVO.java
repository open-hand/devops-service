package io.choerodon.devops.api.vo;

import java.io.Serializable;
import java.util.Date;


public class ApplicationVersionRespVO implements Serializable {

    private Long id;
    private String version;
    private String commit;
    private String appName;
    private String appCode;
    private Long appServiceId;
    private Boolean appStatus;
    private Date creationDate;
    private Date publishTime;
    private Boolean permission;

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void getAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Boolean getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(Boolean appStatus) {
        this.appStatus = appStatus;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }
}
