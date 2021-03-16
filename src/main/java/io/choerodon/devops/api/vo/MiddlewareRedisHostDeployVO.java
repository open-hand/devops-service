package io.choerodon.devops.api.vo;

import java.util.Map;
import java.util.Set;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class MiddlewareRedisHostDeployVO {
    @ApiModelProperty("主机id")
    @Encrypt
    private Set<Long> hostIds;
    @ApiModelProperty("部署模式")
    private String mode;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("部署版本")
    private String version;
    @ApiModelProperty("配置内容")
    private Map<String, String> configuration;

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

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
}
