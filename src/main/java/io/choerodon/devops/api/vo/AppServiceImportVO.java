package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 */
public class AppServiceImportVO extends AppServiceReqVO {
    @ApiModelProperty("外部代码库的clone地址")
    private String repositoryUrl;

    @ApiModelProperty("如果外部代码库是私有的，访问需要的token")
    private String accessToken;

    @ApiModelProperty("外部代码库的平台类型")
    private String platformType;

    @ApiModelProperty("导入需要的用户名")
    private String username;

    @ApiModelProperty("导入需要的密码")
    private String password;

    /**
     * 不是原本写死的直接从github获取的模板
     */
    @Encrypt
    @ApiModelProperty("平台层或者组织层定义的模板Id")
    private Long devopsAppTemplateId;

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public Long getDevopsAppTemplateId() {
        return devopsAppTemplateId;
    }

    public void setDevopsAppTemplateId(Long devopsAppTemplateId) {
        this.devopsAppTemplateId = devopsAppTemplateId;
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
