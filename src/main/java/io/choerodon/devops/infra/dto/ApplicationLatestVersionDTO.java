package io.choerodon.devops.infra.dto;

/**
 * Creator: Runge
 * Date: 2018/4/18
 * Time: 20:28
 * Description:
 */
public class ApplicationLatestVersionDTO {
    private String version;
    private Long appId;
    private Long versionId;

    public ApplicationLatestVersionDTO() {
    }

    public ApplicationLatestVersionDTO(String version, Long appId) {
        this.version = version;
        this.appId = appId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }
}
