package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 16:18
 */
@Table(name = "devops_ci_docker_build_config")
@ModifyAudit
@VersionAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DevopsCiDockerBuildConfigDTO extends AuditDomain {

    @Id
    @Encrypt
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("Docker步骤的构建上下文")
    private String dockerContextDir;

    @ApiModelProperty("Dockerfile文件路径")
    private String dockerFilePath;

    @ApiModelProperty("是否启用harbor的证书校验")
    private Boolean enableDockerTlsVerify;

    @ApiModelProperty("是否开启镜像扫描/true表示开启")
    private Boolean imageScan;

    @ApiModelProperty("是否开启安全门禁")
    private Boolean securityControl;

    @ApiModelProperty(value = "漏洞危险程度")
    private String severity;

    @ApiModelProperty(value = "门禁条件")
    private String securityControlConditions;

    @ApiModelProperty(value = "漏洞数量", required = true)
    private Integer vulnerabilityCount;
    @ApiModelProperty("所属步骤id")
    private Long stepId;

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDockerContextDir() {
        return dockerContextDir;
    }

    public void setDockerContextDir(String dockerContextDir) {
        this.dockerContextDir = dockerContextDir;
    }

    public String getDockerFilePath() {
        return dockerFilePath;
    }

    public void setDockerFilePath(String dockerFilePath) {
        this.dockerFilePath = dockerFilePath;
    }

    public Boolean getEnableDockerTlsVerify() {
        return enableDockerTlsVerify;
    }

    public void setEnableDockerTlsVerify(Boolean enableDockerTlsVerify) {
        this.enableDockerTlsVerify = enableDockerTlsVerify;
    }

    public Boolean getImageScan() {
        return imageScan;
    }

    public void setImageScan(Boolean imageScan) {
        this.imageScan = imageScan;
    }

    public Boolean getSecurityControl() {
        return securityControl;
    }

    public void setSecurityControl(Boolean securityControl) {
        this.securityControl = securityControl;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSecurityControlConditions() {
        return securityControlConditions;
    }

    public void setSecurityControlConditions(String securityControlConditions) {
        this.securityControlConditions = securityControlConditions;
    }

    public Integer getVulnerabilityCount() {
        return vulnerabilityCount;
    }

    public void setVulnerabilityCount(Integer vulnerabilityCount) {
        this.vulnerabilityCount = vulnerabilityCount;
    }
}
