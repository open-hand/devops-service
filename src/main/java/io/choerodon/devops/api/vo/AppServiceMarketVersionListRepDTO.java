package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * @author zmf
 */
public class AppServiceMarketVersionListRepDTO {
    private Long applicationId;
    private List<AppServiceVersionUploadVO> versions;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public List<AppServiceVersionUploadVO> getVersions() {
        return versions;
    }

    public void setVersions(List<AppServiceVersionUploadVO> versions) {
        this.versions = versions;
    }
}
