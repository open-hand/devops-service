package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

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
public class DevopsCiDockerBuildConfigDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("Docker步骤的构建上下文")
    private String dockerContextDir;

    @ApiModelProperty("Dockerfile文件路径")
    private String dockerFilePath;

    @ApiModelProperty("是否跳过harbor的证书校验 / true表示跳过")
    private Boolean skipDockerTlsVerify;

    @ApiModelProperty("是否开启镜像扫描/true表示开启")
    private Boolean imageScan;

    @ApiModelProperty("是否开启安全门禁")
    private Boolean securityControl;

    @ApiModelProperty("漏洞危险程度")
    private String level;
    @ApiModelProperty("门禁条件")
    private String symbol;
    @ApiModelProperty("漏洞数量")
    private Integer condition;

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

    public Boolean getSkipDockerTlsVerify() {
        return skipDockerTlsVerify;
    }

    public void setSkipDockerTlsVerify(Boolean skipDockerTlsVerify) {
        this.skipDockerTlsVerify = skipDockerTlsVerify;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getCondition() {
        return condition;
    }

    public void setCondition(Integer condition) {
        this.condition = condition;
    }
}
