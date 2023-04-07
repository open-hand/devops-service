package io.choerodon.devops.api.vo.pipeline;

import org.hzero.starter.keyencrypt.core.Encrypt;

public class DeployInfo {
    private String envName;
    private String appServiceName;
    private String appServiceVersion;
    @Encrypt
    private Long appServiceId;
    @Encrypt
    private Long envId;
    @Encrypt
    private Long appId;
    private String appName;
    private String status;
    private String rdupmType;
    private String chartSource;
    private String operationType;
    private String deployType;
    @Encrypt
    private Long deployTypeId;
    private String hostName;

    private Long podCount;

    private Long podRunningCount;

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

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRdupmType() {
        return rdupmType;
    }

    public void setRdupmType(String rdupmType) {
        this.rdupmType = rdupmType;
    }

    public String getChartSource() {
        return chartSource;
    }

    public void setChartSource(String chartSource) {
        this.chartSource = chartSource;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getDeployTypeId() {
        return deployTypeId;
    }

    public void setDeployTypeId(Long deployTypeId) {
        this.deployTypeId = deployTypeId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}