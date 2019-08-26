package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.harbor.User;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:56 2019/8/5
 * Description:
 */
public class AppMarketDownloadPayload {
    @ApiModelProperty("iam用户Id")
    private Long iamUserId;

    @ApiModelProperty("应用Id")
    private Long appId;

    @ApiModelProperty("应用name")
    private String name;

    @ApiModelProperty("应用name")
    private String code;

    @ApiModelProperty("harbor用户")
    private User user;

    @ApiModelProperty("Pass端应用版本id")
    Long appVersionId;

    @ApiModelProperty("应用服务")
    private List<AppServiceDownloadPayload> appServiceDownloadPayloads;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public List<AppServiceDownloadPayload> getAppServiceDownloadPayloads() {
        return appServiceDownloadPayloads;
    }

    public void setAppServiceDownloadPayloads(List<AppServiceDownloadPayload> appServiceDownloadPayloads) {
        this.appServiceDownloadPayloads = appServiceDownloadPayloads;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getAppVersionId() {
        return appVersionId;
    }

    public void setAppVersionId(Long appVersionId) {
        this.appVersionId = appVersionId;
    }
}
