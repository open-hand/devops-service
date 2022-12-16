package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * devops_ci_template_sonar(CiTemplateSonar)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:18
 */

@ApiModel("devops_ci_template_sonar")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_sonar")
public class CiTemplateSonarDTO extends AuditDomain {
    private static final long serialVersionUID = 462693034654497162L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_SCANNER_TYPE = "scannerType";
    public static final String FIELD_SKIPTESTS = "skiptests";
    public static final String FIELD_SOURCES = "sources";
    public static final String FIELD_SONAR_URL = "sonarUrl";
    public static final String FIELD_AUTH_TYPE = "authType";
    public static final String FIELD_TOKEN = "token";

    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "扫描器类型 sonarmaven 、sonarscanner", required = true)
    @NotBlank
    private String scannerType;

    @ApiModelProperty("配置类型, 如果是default就不需要其他字段 / default或custom")
    private String configType;

    @ApiModelProperty(value = "是否跳过单测")
    private Boolean skipTests;

    @ApiModelProperty(value = "要扫描的文件目录，多个文件夹使用,隔开")
    private String sources;

    @ApiModelProperty(value = "外部sonar地址")
    private String sonarUrl;

    @ApiModelProperty(value = "外部sonar认证类型")
    private String authType;

    @ApiModelProperty(value = "外部sonar认证的用户名")
    private String username;

    @ApiModelProperty(value = "外部sonar认证的密码")
    private String password;

    @ApiModelProperty(value = "外部sonar认证的token")
    private String token;

    @ApiModelProperty(value = "流水线模板步骤Id")
    private Long ciTemplateStepId;

    @ApiModelProperty("SonarQualityGate")
    @Transient
    private DevopsCiTplSonarQualityGateDTO devopsCiSonarQualityGateVO;

    @ApiModelProperty("保存maven相关信息")
    @Transient
    private CiTemplateMavenBuildDTO mavenBuildConfig;



    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSonarUrl() {
        return sonarUrl;
    }

    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public Long getCiTemplateStepId() {
        return ciTemplateStepId;
    }

    public void setCiTemplateStepId(Long ciTemplateStepId) {
        this.ciTemplateStepId = ciTemplateStepId;
    }

    public DevopsCiTplSonarQualityGateDTO getDevopsCiSonarQualityGateVO() {
        return devopsCiSonarQualityGateVO;
    }

    public void setDevopsCiSonarQualityGateVO(DevopsCiTplSonarQualityGateDTO devopsCiSonarQualityGateVO) {
        this.devopsCiSonarQualityGateVO = devopsCiSonarQualityGateVO;
    }

    public CiTemplateMavenBuildDTO getMavenBuildConfig() {
        return mavenBuildConfig;
    }

    public void setMavenBuildConfig(CiTemplateMavenBuildDTO mavenBuildConfig) {
        this.mavenBuildConfig = mavenBuildConfig;
    }
}

