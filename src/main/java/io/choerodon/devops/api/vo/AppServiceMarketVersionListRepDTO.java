package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * @author zmf
 */
public class AppServiceMarketVersionListRepDTO {
    private Long applicationId;
    private List<AppServiceMarketVersionVO> versions;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public List<AppServiceMarketVersionVO> getVersions() {
        return versions;
    }

    public void setVersions(List<AppServiceMarketVersionVO> versions) {
        this.versions = versions;
    }
}
