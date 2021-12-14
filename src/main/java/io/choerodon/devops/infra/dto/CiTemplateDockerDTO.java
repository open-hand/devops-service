package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateDocker)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:56:48
 */

@ApiModel("流水线任务模板与步骤模板关系表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_docker")
public class CiTemplateDockerDTO extends AuditDomain {
    private static final long serialVersionUID = 768455064232820362L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_DOCKER_FILE_PATH = "dockerFilePath";
    public static final String FIELD_DOCKER_CONTEXT_DIR = "dockerContextDir";
    public static final String FIELD_SKIP_DOCKER_TLS_VERIFY = "skipDockerTlsVerify";
    public static final String FIELD_IMAGE_SCAN = "imageScan";
    public static final String FIELD_SECURITY_CONTROL = "securityControl";
    public static final String FIELD_LEVEL = "level";
    public static final String FIELD_SYMBOL = "symbol";
    public static final String FIELD_CONDITION = "condition";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "docker file 地址", required = true)
    @NotBlank
    private String dockerFilePath;

    @ApiModelProperty(value = "docker 上下文路径", required = true)
    @NotBlank
    private String dockerContextDir;

    @ApiModelProperty(value = "是否跳过tls", required = true)
    @NotNull
    private Long skipDockerTlsVerify;

    @ApiModelProperty(value = "是否是否开启镜像扫描", required = true)
    @NotNull
    private Long imageScan;

    @ApiModelProperty(value = "是否开启门禁检查", required = true)
    @NotNull
    private Long securityControl;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDockerFilePath() {
        return dockerFilePath;
    }

    public void setDockerFilePath(String dockerFilePath) {
        this.dockerFilePath = dockerFilePath;
    }

    public String getDockerContextDir() {
        return dockerContextDir;
    }

    public void setDockerContextDir(String dockerContextDir) {
        this.dockerContextDir = dockerContextDir;
    }

    public Long getSkipDockerTlsVerify() {
        return skipDockerTlsVerify;
    }

    public void setSkipDockerTlsVerify(Long skipDockerTlsVerify) {
        this.skipDockerTlsVerify = skipDockerTlsVerify;
    }

    public Long getImageScan() {
        return imageScan;
    }

    public void setImageScan(Long imageScan) {
        this.imageScan = imageScan;
    }

    public Long getSecurityControl() {
        return securityControl;
    }

    public void setSecurityControl(Long securityControl) {
        this.securityControl = securityControl;
    }


}
