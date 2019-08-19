package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.harbor.User;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:56 2019/8/5
 * Description:
 */
public class AppMarketDownloadVO {
    @ApiModelProperty("iam用户Id")
    private Long iamUserId;

    @ApiModelProperty("应用Id")
    private Long appId;

    @ApiModelProperty("文件下载地址")
    private String filePath;

    @ApiModelProperty("harbor用户")
    private User user;

    @ApiModelProperty("应用服务")
    private List<AppServiceDownloadVO> appServiceMarketDownloadVOS;

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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<AppServiceDownloadVO> getAppServiceMarketDownloadVOS() {
        return appServiceMarketDownloadVOS;
    }

    public void setAppServiceMarketDownloadVOS(List<AppServiceDownloadVO> appServiceMarketDownloadVOS) {
        this.appServiceMarketDownloadVOS = appServiceMarketDownloadVOS;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
