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

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode;
    }
}
