package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public class DevopsEnvApplicationVO {
    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long appServiceId;

    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;

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
}
