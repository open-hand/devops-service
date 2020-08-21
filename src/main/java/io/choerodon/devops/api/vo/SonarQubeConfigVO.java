package io.choerodon.devops.api.vo;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/7 10:35
 */
public class SonarQubeConfigVO {
    private String sonarUrl;
    private String username;
    private String password;
    private String authType;
    private String token;

    @ApiModelProperty("配置类型, 如果是default就不需要其他字段 / default或custom")
    private String configType;
    @ApiModelProperty("sonar scanner 类型")
    private String scannerType;
    @ApiModelProperty("是否跳过单测， true 跳过， false 不跳过")
    private Boolean skipTests;
    @ApiModelProperty("要扫描的文件目录，多个文件夹使用','隔开")
    private String sources;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SonarQubeConfigVO that = (SonarQubeConfigVO) o;
        return Objects.equals(sonarUrl, that.sonarUrl) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(authType, that.authType) &&
                Objects.equals(token, that.token) &&
                Objects.equals(configType, that.configType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sonarUrl, username, password, authType, token, configType);
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
