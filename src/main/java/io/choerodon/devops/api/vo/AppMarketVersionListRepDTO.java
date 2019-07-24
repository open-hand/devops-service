package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * @author zmf
 */
public class AppMarketVersionListRepDTO {
    private Long applicationId;
    private List<AppMarketVersionVO> versions;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public List<AppMarketVersionVO> getVersions() {
        return versions;
    }

    public void setVersions(List<AppMarketVersionVO> versions) {
        this.versions = versions;
    }
}
