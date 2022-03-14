package io.choerodon.devops.api.vo.template;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateDocker)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:56:48
 */


public class CiTemplateDockerVO {

    @Encrypt
    private Long id;

    @ApiModelProperty(value = "docker file 地址", required = true)
    @NotBlank
    private String dockerFilePath;

    @ApiModelProperty(value = "docker 上下文路径", required = true)
    @NotBlank
    private String dockerContextDir;

    @ApiModelProperty("是否启用harbor的证书校验")
    @NotNull
    private Long enableDockerTlsVerify;

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

    public Long getEnableDockerTlsVerify() {
        return enableDockerTlsVerify;
    }

    public void setEnableDockerTlsVerify(Long enableDockerTlsVerify) {
        this.enableDockerTlsVerify = enableDockerTlsVerify;
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

