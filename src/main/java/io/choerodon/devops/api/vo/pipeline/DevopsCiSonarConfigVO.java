package io.choerodon.devops.api.vo.pipeline;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:18
 */
@ModifyAudit
@VersionAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DevopsCiSonarConfigVO extends AuditDomain {
    @Id
    @Encrypt
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("外部sonar地址")
    private String sonarUrl;
    @ApiModelProperty("外部sonar认证类型")
    private String username;
    @ApiModelProperty("外部sonar认证的用户名")
    private String password;
    @ApiModelProperty("外部sonar认证的密码")
    private String authType;
    @ApiModelProperty("外部sonar认证的token")
    private String token;

    @ApiModelProperty("配置类型, 如果是default就不需要其他字段 / default或custom")
    private String configType;
    @ApiModelProperty("sonar scanner 类型")
    private String scannerType;
    @ApiModelProperty("是否跳过单测， true 跳过， false 不跳过")
    private Boolean skipTests;
    @ApiModelProperty("要扫描的文件目录，多个文件夹使用','隔开")
    private String sources;

    @ApiModelProperty("所属步骤id")
    private Long stepId;

    @ApiModelProperty("maven 构建步骤配置")
    private DevopsCiMavenBuildConfigVO mavenBuildConfig;

    private DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO;

    public DevopsCiSonarQualityGateVO getDevopsCiSonarQualityGateVO() {
        return devopsCiSonarQualityGateVO;
    }

    public void setDevopsCiSonarQualityGateVO(DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO) {
        this.devopsCiSonarQualityGateVO = devopsCiSonarQualityGateVO;
    }

    public DevopsCiMavenBuildConfigVO getMavenBuildConfig() {
        return mavenBuildConfig;
    }

    public void setMavenBuildConfig(DevopsCiMavenBuildConfigVO mavenBuildConfig) {
        this.mavenBuildConfig = mavenBuildConfig;
    }

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

    public String getSonarUrl() {
        return sonarUrl;
    }

    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getScannerType() {
        return scannerType;
    }

    public void setScannerType(String scannerType) {
        this.scannerType = scannerType;
    }

    public Boolean getSkipTests() {
        return skipTests;
    }

    public void setSkipTests(Boolean skipTests) {
        this.skipTests = skipTests;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }
}
