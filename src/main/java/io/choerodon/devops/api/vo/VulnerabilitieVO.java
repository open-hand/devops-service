package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by wangxiang on 2021/3/25
 */
public class VulnerabilitieVO {
    @ApiModelProperty("漏洞码")
    @JsonProperty("VulnerabilityID")
    private String vulnerabilityCode;


    @ApiModelProperty("漏洞等级")
    private String severity;

    @ApiModelProperty("组件名称")
    private String pkgName;

    @ApiModelProperty("组件当前的版本")
    private String installedVersion;

    @ApiModelProperty("修复版本")
    private String fixedVersion;

    @ApiModelProperty("简介")
    private String description;


    public String getVulnerabilityCode() {
        return vulnerabilityCode;
    }

    public void setVulnerabilityCode(String vulnerabilityCode) {
        this.vulnerabilityCode = vulnerabilityCode;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getInstalledVersion() {
        return installedVersion;
    }

    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

    public String getFixedVersion() {
        return fixedVersion;
    }

    public void setFixedVersion(String fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
