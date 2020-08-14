package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Sheep on 2019/7/30.
 */
public class AppServiceInstanceRepVO {
    @Encrypt
    private Long instanceId;
    @Encrypt
    private Long appServiceId;
    @Encrypt
    private Long envId;
    private String appServiceName;
    private String appServiceVersion;
    private String envName;
    private String instanceName;

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

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
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

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
