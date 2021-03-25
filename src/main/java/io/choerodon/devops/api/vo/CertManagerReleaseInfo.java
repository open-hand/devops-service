package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * CertManager实例的信息
 */
public class CertManagerReleaseInfo {
    @ApiModelProperty("实例名称")
    private String releaseName;
    @ApiModelProperty("CertManager所在的命名空间")
    private String namespace;
    @ApiModelProperty("CertManager的版本")
    private String chartVersion;
    @ApiModelProperty("CertManager的状态")
    private String status;

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
