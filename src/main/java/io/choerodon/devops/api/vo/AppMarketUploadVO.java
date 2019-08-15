package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.infra.dto.harbor.User;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:59 2019/8/8
 * Description:
 */
public class AppMarketUploadVO {
    private Long appId;
    private List<AppServiceMarketVO> appServiceMarketVOList;
    private String status;
    private Long iamUserId;
    private String harborUrl;
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public List<AppServiceMarketVO> getAppServiceMarketVOList() {
        return appServiceMarketVOList;
    }

    public void setAppServiceMarketVOList(List<AppServiceMarketVO> appServiceMarketVOList) {
        this.appServiceMarketVOList = appServiceMarketVOList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHarborUrl() {
        return harborUrl;
    }

    public void setHarborUrl(String harborUrl) {
        this.harborUrl = harborUrl;
    }
}
