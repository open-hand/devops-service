package io.choerodon.devops.infra.dto.gitlab;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/28 10:43
 */
public class AppExternalConfigVO {


    @ApiModelProperty("外部仓库地址")
    private String repositoryUrl;

    @ApiModelProperty("gitlab地址")
    private String gitlabUrl;
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

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
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
