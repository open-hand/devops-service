package io.choerodon.devops.infra.dto.maven;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-15
 */
public class Activation {
    @ApiModelProperty("是否默认启用")
    private Boolean activeByDefault;

    public Activation() {
        activeByDefault = false;
    }

    public Activation(Boolean activeByDefault) {
        this.activeByDefault = activeByDefault;
    }

    public Boolean getActiveByDefault() {
        return activeByDefault;
    }

    public void setActiveByDefault(Boolean activeByDefault) {
        this.activeByDefault = activeByDefault;
    }
}
