package io.choerodon.devops.api.vo;

import java.util.List;

public class AppServiceMarketDownloadVO {

    private Long appMarketId;

    private List<Long> appServiceVersionIds;

    public Long getAppMarketId() {
        return appMarketId;
    }

    public void setAppMarketId(Long appMarketId) {
        this.appMarketId = appMarketId;
    }

    public List<Long> getAppServiceVersionIds() {
        return appServiceVersionIds;
    }

    public void setAppServiceVersionIds(List<Long> appServiceVersionIds) {
        this.appServiceVersionIds = appServiceVersionIds;
    }
}
