package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.harbor.RobotUser;
import io.choerodon.devops.infra.dto.harbor.User;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:59 2019/8/8
 * Description:
 */
public class AppMarketUploadPayload {
    @ApiModelProperty("市场应用Id")
    private Long mktAppId;

    @ApiModelProperty("市场应用code")
    private String mktAppCode;

    @ApiModelProperty("市场应用版本Id")
    private Long mktAppVersionId;

    @ApiModelProperty("应用Id")
    private Long appId;

    @ApiModelProperty("projectId")
    private Long projectId;

    @ApiModelProperty("应用code")
    private String appCode;

    @ApiModelProperty("应用服务")
    private List<AppServiceUploadPayload> appServiceUploadPayloads;

    @ApiModelProperty("状态: deploy_only,download_only,all")
    private String status;

    @ApiModelProperty("Harbor用户")
    private RobotUser user;

    @ApiModelProperty("SAAS平台getaway URL")
    private String saasGetawayUrl;

    @ApiModelProperty("应用版本")
    private String appVersion;

    @ApiModelProperty("更新发布应用版本")
    private Boolean updateVersion;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public RobotUser getUser() {
        return user;
    }

    public void setUser(RobotUser user) {
        this.user = user;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public List<AppServiceUploadPayload> getAppServiceUploadPayloads() {
        return appServiceUploadPayloads;
    }

    public void setAppServiceUploadPayloads(List<AppServiceUploadPayload> appServiceUploadPayloads) {
        this.appServiceUploadPayloads = appServiceUploadPayloads;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSaasGetawayUrl() {
        return saasGetawayUrl;
    }

    public void setSaasGetawayUrl(String saasGetawayUrl) {
        this.saasGetawayUrl = saasGetawayUrl;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Long getMktAppId() {
        return mktAppId;
    }

    public void setMktAppId(Long mktAppId) {
        this.mktAppId = mktAppId;
    }

    public Boolean getUpdateVersion() {
        return updateVersion;
    }

    public void setUpdateVersion(Boolean updateVersion) {
        this.updateVersion = updateVersion;
    }

    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public void setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
    }

    public String getMktAppCode() {
        return mktAppCode;
    }

    public void setMktAppCode(String mktAppCode) {
        this.mktAppCode = mktAppCode;
    }
}
