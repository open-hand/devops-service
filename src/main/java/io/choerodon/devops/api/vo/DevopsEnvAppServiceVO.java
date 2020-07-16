package io.choerodon.devops.api.vo;


import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 环境和应用服务关联关系, 可以一个环境关联多个应用
 */
public class DevopsEnvAppServiceVO {
    @Encrypt
    @ApiModelProperty(value = "环境id")
    private Long envId;

    @Encrypt
    @ApiModelProperty(value = "该环境对应的应用id")
    private List<Long> appServiceIds;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public List<Long> getAppServiceIds() {
        return appServiceIds;
    }

    public void setAppServiceIds(List<Long> appServiceIds) {
        this.appServiceIds = appServiceIds;
    }
}
