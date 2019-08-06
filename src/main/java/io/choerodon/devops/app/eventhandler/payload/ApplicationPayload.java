package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:56 2019/8/5
 * Description:
 */
public class ApplicationPayload {
    private Long userId;
    private Long organizationId;
    private Long appId;
    private String appCode;
    private List<AppServicePayload> appServicePayloads;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<AppServicePayload> getAppServicePayloads() {
        return appServicePayloads;
    }

    public void setAppServicePayloads(List<AppServicePayload> appServicePayloads) {
        this.appServicePayloads = appServicePayloads;
    }
}
