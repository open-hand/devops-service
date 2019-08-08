package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:59 2019/8/8
 * Description:
 */
public class AppMarketUploadVO {
    private Long appId;
    private List<AppServiceMarketVO> appServiceMarketVOList;
    private String status;
    private String harborUrl;

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
