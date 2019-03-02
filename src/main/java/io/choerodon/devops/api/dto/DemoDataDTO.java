package io.choerodon.devops.api.dto;

import io.choerodon.devops.infra.dataobject.ApplicationVersionDO;

/**
 * @author zmf
 */
public class DemoDataDTO {
    private ApplicationReqDTO applicationInfo;
    private ApplicationReleasingDTO applicationRelease;
    private String templateSearchParam;
    private String appVersionSearchParam;
    private DemoTagDTO tagInfo;
    private DevopsBranchDTO branchInfo;
    private ApplicationVersionDO appVersion;

    public ApplicationReqDTO getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(ApplicationReqDTO applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public ApplicationReleasingDTO getApplicationRelease() {
        return applicationRelease;
    }

    public void setApplicationRelease(ApplicationReleasingDTO applicationRelease) {
        this.applicationRelease = applicationRelease;
    }

    public String getTemplateSearchParam() {
        return templateSearchParam;
    }

    public void setTemplateSearchParam(String templateSearchParam) {
        this.templateSearchParam = templateSearchParam;
    }

    public String getAppVersionSearchParam() {
        return appVersionSearchParam;
    }

    public void setAppVersionSearchParam(String appVersionSearchParam) {
        this.appVersionSearchParam = appVersionSearchParam;
    }

    public DemoTagDTO getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(DemoTagDTO tagInfo) {
        this.tagInfo = tagInfo;
    }

    public DevopsBranchDTO getBranchInfo() {
        return branchInfo;
    }

    public void setBranchInfo(DevopsBranchDTO branchInfo) {
        this.branchInfo = branchInfo;
    }

    public ApplicationVersionDO getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(ApplicationVersionDO appVersion) {
        this.appVersion = appVersion;
    }
}
