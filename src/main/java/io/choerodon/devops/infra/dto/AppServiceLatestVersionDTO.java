package io.choerodon.devops.infra.dto;

/**
 * Creator: Runge
 * Date: 2018/4/18
 * Time: 20:28
 * Description:
 */
public class AppServiceLatestVersionDTO {
    private String version;
    private Long appServiceId;
    private Long versionId;

    public AppServiceLatestVersionDTO() {
    }

    public AppServiceLatestVersionDTO(String version, Long appServiceId) {
        this.version = version;
        this.appServiceId = appServiceId;
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

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }
}
