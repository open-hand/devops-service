package io.choerodon.devops.api.vo.cd;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/12/2 17:40
 */
public class AppVersionTriggerVO {
    private Long appServiceId;
    private Long appVersionId;

    public AppVersionTriggerVO() {
    }

    public AppVersionTriggerVO(Long appServiceId, Long appVersionId) {
        this.appServiceId = appServiceId;
        this.appVersionId = appVersionId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getAppVersionId() {
        return appVersionId;
    }

    public void setAppVersionId(Long appVersionId) {
        this.appVersionId = appVersionId;
    }
}
