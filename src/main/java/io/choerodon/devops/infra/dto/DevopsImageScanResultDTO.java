package io.choerodon.devops.infra.dto;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by wangxiang on 2021/3/25
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_image_scan_result")
public class DevopsImageScanResultDTO extends AuditDomain {

    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    @ApiModelProperty("漏洞码")
    private String vulnerabilityCode;

    @ApiModelProperty("appServiceId")
    private Long appServiceId;

    @ApiModelProperty("GITLAB_PIPELINE_ID")
    private Long gitlabPipelineId;

    @ApiModelProperty("任务名称")
    private String jobName;

    @ApiModelProperty("开始时间")
    private Date startDate;

    @ApiModelProperty("结束时间")
    private Date endDate;

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

    @ApiModelProperty("镜像名称")
    private String target;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVulnerabilityCode() {
        return vulnerabilityCode;
    }

    public void setVulnerabilityCode(String vulnerabilityCode) {
        this.vulnerabilityCode = vulnerabilityCode;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
