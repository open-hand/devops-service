package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:24 2019/8/7
 * Description:
 */
public class AppServiceMarketVO {
    private Long appServiceId;
    private String appServiceCode;
    private String appServiceName;
    private List<AppServiceMarketVersionVO> appServiceMarketVersionVOS;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public List<AppServiceMarketVersionVO> getAppServiceMarketVersionVOS() {
        return appServiceMarketVersionVOS;
    }

    public void setAppServiceMarketVersionVOS(List<AppServiceMarketVersionVO> appServiceMarketVersionVOS) {
        this.appServiceMarketVersionVOS = appServiceMarketVersionVOS;
    }
}
