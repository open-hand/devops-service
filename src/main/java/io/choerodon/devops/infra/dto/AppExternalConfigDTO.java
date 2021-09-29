package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
 * @since 2021/9/28 10:43
 */
@Table(name = "devops_app_external_config")
@ModifyAudit
@VersionAudit
public class AppExternalConfigDTO extends AuditDomain {

    @Id
    @Encrypt
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long appServiceId;

    @ApiModelProperty("外部仓库地址")
    private String repositoryUrl;
    /**
     * {@link io.choerodon.devops.infra.enums.ExternalAppAuthTypeEnum}
     */
    @ApiModelProperty("认证类型：用户名密码：username_password,Token: access_token")
    private String authType;
    @ApiModelProperty("用户gitlab access_token")
    private String accessToken;
    @ApiModelProperty("gitlab用户名")
    private String username;
    @ApiModelProperty("gitlab密码")
    private String password;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
}
