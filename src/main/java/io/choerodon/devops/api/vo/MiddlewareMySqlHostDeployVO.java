package io.choerodon.devops.api.vo;

import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class MiddlewareMySqlHostDeployVO {
    @ApiModelProperty("主机id")
    @Encrypt
    private Set<Long> hostIds;
    @ApiModelProperty("部署模式")
    private String mode;
    @ApiModelProperty("名称")
    @Size(min = 1, max = 53, message = "error.app.instance.name.length")
    @NotBlank(message = "error.app.instance.name.null")
    private String name;
    @ApiModelProperty("应用中心应用名称")
    @Size(min = 1, max = 53, message = "error.env.app.center.name.length")
    @NotBlank(message = "error.app.instance.name.null")
    private String appName;

    @ApiModelProperty("应用中心应用code，同时也作为实例名称")
    @Size(min = 1, max = 53, message = "error.env.app.center.code.length")
    @NotBlank(message = "error.app.instance.code.null")
    private String appCode;
    @ApiModelProperty("密码")
    @Size(min = 6, max = 32, message = "error.middleware.redis.password.length")
    @NotBlank(message = "error.middleware.redis.password.empty")
    private String password;
    @ApiModelProperty("部署版本")
    private String version;
    @ApiModelProperty("虚拟ip地址")
    private String virtualIp;
    @ApiModelProperty("配置内容,一个节点一个配置 节点名称=>配置")
    private Map<String, Map<String, String>> configuration;

    public Set<Long> getHostIds() {
        return hostIds;
    }

    public void setHostIds(Set<Long> hostIds) {
        this.hostIds = hostIds;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Map<String, String>> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Map<String, String>> configuration) {
        this.configuration = configuration;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualIp() {
        return virtualIp;
    }

    public void setVirtualIp(String virtualIp) {
        this.virtualIp = virtualIp;
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
}
