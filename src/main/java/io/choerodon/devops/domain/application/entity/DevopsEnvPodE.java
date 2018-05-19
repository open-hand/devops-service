package io.choerodon.devops.domain.application.entity;

import java.util.Date;

/**
 * Created by Zenger on 2018/4/12.
 */
public class DevopsEnvPodE {

    private Long id;
    private String name;
    private String ip;
    private Boolean isReady;
    private ApplicationInstanceE applicationInstanceE;
    private String status;
    private String appName;
    private String appVersion;
    private Date creationDate;
    private String resourceVersion;
    private String namespace;
    private String instanceCode;
    private String envCode;
    private String envName;
    private Long objectVersionNumber;

    public DevopsEnvPodE() {
    }

    public DevopsEnvPodE(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public ApplicationInstanceE getApplicationInstanceE() {
        return applicationInstanceE;
    }

    public void setApplicationInstanceE(ApplicationInstanceE applicationInstanceE) {
        this.applicationInstanceE = applicationInstanceE;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void initApplicationInstanceE(Long id) {
        this.applicationInstanceE = new ApplicationInstanceE(id);
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getInstanceCode() {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode) {
        this.instanceCode = instanceCode;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }
}
