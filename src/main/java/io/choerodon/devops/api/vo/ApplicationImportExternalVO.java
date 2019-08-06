package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 */
public class ApplicationImportExternalVO extends AppServiceReqVO {
    @ApiModelProperty("外部代码库的clone地址")
    private String repositoryUrl;

    @ApiModelProperty("如果外部代码库是私有的，访问需要的token")
    private String accessToken;

    @ApiModelProperty("外部代码库的平台类型")
    private String platformType;

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
}
