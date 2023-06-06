package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 漏洞扫描记录表(VulnScanRecord)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:38
 */

@ApiModel("漏洞扫描记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_vuln_scan_record")
public class VulnScanRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_ID = "appServiceId";
    public static final String FIELD_BRANCH_NAME = "branchName";
    public static final String FIELD_UNKNOWN = "unknown";
    public static final String FIELD_LOW = "low";
    public static final String FIELD_MEDIUM = "medium";
    public static final String FIELD_HIGH = "high";
    public static final String FIELD_CRITICAL = "critical";
    public static final String FIELD_SCORE = "score";
    private static final long serialVersionUID = -17914131753062244L;
    @Id
    @Encrypt
    @GeneratedValue
    private Long id;

    private Long projectId;

    @ApiModelProperty(value = "应用服务id", required = true)
    @NotNull
    private Long appServiceId;

    @ApiModelProperty(value = "分支名", required = true)
    @NotBlank
    private String branchName;

    @ApiModelProperty(value = "未知漏洞数", required = true)
    @NotNull
    private Long unknown;

    @ApiModelProperty(value = "较低漏洞数", required = true)
    @NotNull
    private Long low;

    @ApiModelProperty(value = "中等漏洞数", required = true)
    @NotNull
    private Long medium;

    @ApiModelProperty(value = "严重漏洞数", required = true)
    @NotNull
    private Long high;

    @ApiModelProperty(value = "危急漏洞数", required = true)
    @NotNull
    private Long critical;

    @ApiModelProperty(value = "评分", required = true)
    @NotNull
    private Long score;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Long getUnknown() {
        return unknown;
    }

    public void setUnknown(Long unknown) {
        this.unknown = unknown;
    }

    public Long getLow() {
        return low;
    }

    public void setLow(Long low) {
        this.low = low;
    }

    public Long getMedium() {
        return medium;
    }

    public void setMedium(Long medium) {
        this.medium = medium;
    }

    public Long getHigh() {
        return high;
    }

    public void setHigh(Long high) {
        this.high = high;
    }

    public Long getCritical() {
        return critical;
    }

    public void setCritical(Long critical) {
        this.critical = critical;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

}

