package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:19 2019/8/5
 * Description:
 */
public class ApplicationImportInternalVO {
    private Long appServiceId;
    private Long versionId;
    private String type;
    private String appName;
    private String appCode;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
