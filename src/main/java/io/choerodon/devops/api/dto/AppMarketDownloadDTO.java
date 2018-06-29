package io.choerodon.devops.api.dto;

import java.util.List;

public class AppMarketDownloadDTO {

    private Long appMarketId;

    private List<Long> appVersionIds;

    public Long getAppMarketId() {
        return appMarketId;
    }

    public void setAppMarketId(Long appMarketId) {
        this.appMarketId = appMarketId;
    }

    public List<Long> getAppVersionIds() {
        return appVersionIds;
    }

    public void setAppVersionIds(List<Long> appVersionIds) {
        this.appVersionIds = appVersionIds;
    }
}
