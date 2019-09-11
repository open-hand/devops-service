package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.harbor.RobotUser;
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
    private String appName;

    @ApiModelProperty("应用name")
    private String appCode;

    @ApiModelProperty("下载的应用类型")
    private String downloadAppType;

    @ApiModelProperty("harbor用户")
    private RobotUser user;

    @ApiModelProperty("Pass端应用版本id")
    private Long appVersionId;

    @ApiModelProperty("历史记录Id")
    private Long appDownloadRecordId;

    @ApiModelProperty("SasS端应用版本Id")
    private Long mktAppVersionId;

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

    public RobotUser getUser() {
        return user;
    }

    public void setUser(RobotUser user) {
        this.user = user;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Long getAppVersionId() {
        return appVersionId;
    }

    public void setAppVersionId(Long appVersionId) {
        this.appVersionId = appVersionId;
    }

    public Long getAppDownloadRecordId() {
        return appDownloadRecordId;
    }

    public void setAppDownloadRecordId(Long appDownloadRecordId) {
        this.appDownloadRecordId = appDownloadRecordId;
    }

    public String getDownloadAppType() {
        return downloadAppType;
    }

    public void setDownloadAppType(String downloadAppType) {
        this.downloadAppType = downloadAppType;
    }
    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public void setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
    }
}
