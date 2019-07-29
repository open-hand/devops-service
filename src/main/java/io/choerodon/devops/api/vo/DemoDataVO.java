package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;

/**
 * @author zmf
 */
public class DemoDataVO {
    private ApplicationServiceReqVO applicationInfo;
    private ApplicationReleasingVO applicationRelease;
    private String templateSearchParam;
    private String appVersionSearchParam;
    private DemoTagVO tagInfo;
    private DevopsBranchDTO branchInfo;
    private ApplicationVersionDTO appVersion;

    public ApplicationServiceReqVO getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(ApplicationServiceReqVO applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public ApplicationReleasingVO getApplicationRelease() {
        return applicationRelease;
    }

    public void setApplicationRelease(ApplicationReleasingVO applicationRelease) {
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

    public DemoTagVO getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(DemoTagVO tagInfo) {
        this.tagInfo = tagInfo;
    }

    public DevopsBranchDTO getBranchInfo() {
        return branchInfo;
    }

    public void setBranchInfo(DevopsBranchDTO branchInfo) {
        this.branchInfo = branchInfo;
    }

    public ApplicationVersionDTO getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(ApplicationVersionDTO appVersion) {
        this.appVersion = appVersion;
    }
}
