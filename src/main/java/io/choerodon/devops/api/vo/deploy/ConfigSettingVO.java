package io.choerodon.devops.api.vo.deploy;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class ConfigSettingVO {
    @ApiModelProperty(value = "配置Id")
    @Encrypt
    private Long configId;
    @ApiModelProperty(value = "挂载路径")
    private String mountPath;
    @ApiModelProperty(value = "配置分组", required = true)
    private String configGroup;
    @ApiModelProperty(value = "配置编码", required = true)
    private String configCode;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getMountPath() {
        return mountPath;
    }

    public ConfigSettingVO setMountPath(String mountPath) {
        this.mountPath = mountPath;
        return this;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public ConfigSettingVO setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
        return this;
    }

    public String getConfigCode() {
        return configCode;
    }

    public ConfigSettingVO setConfigCode(String configCode) {
        this.configCode = configCode;
        return this;
    }
}
