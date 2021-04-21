package io.choerodon.devops.api.vo;

import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.constant.GitOpsConstants;

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
    @ApiModelProperty("密码")
    @Size(min = 6, max = 32, message = "error.middleware.redis.password.length")
    @NotBlank(message = "error.middleware.redis.password.empty")
    private String password;
    @ApiModelProperty("部署版本")
    private String version;
    @Pattern(regexp = GitOpsConstants.IP_PATTERN, message = "error.host.ip.invalid")
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
}
