package io.choerodon.devops.infra.dto.maven;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-14
 */
public class RepositoryPolicy {
    @ApiModelProperty("Whether to use this repository for downloading this type of artifact.")
    private Boolean enabled;
    @ApiModelProperty("The frequency for downloading updates - can be always; daily (default), interval:XXX(in minutes) or never;")
    private String updatePolicy;

    public RepositoryPolicy() {
    }

    public RepositoryPolicy(Boolean enabled) {
        this.enabled = enabled;
    }

    public RepositoryPolicy(Boolean enabled, String updatePolicy) {
        this.enabled = enabled;
        this.updatePolicy = updatePolicy;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUpdatePolicy() {
        return updatePolicy;
    }

    public void setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
    }

    @Override
    public String toString() {
        return "RepositoryPolicy{" +
                "enabled=" + enabled +
                ", updatePolicy='" + updatePolicy + '\'' +
                '}';
    }
}
