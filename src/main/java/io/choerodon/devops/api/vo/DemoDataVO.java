package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;

/**
 * @author zmf
 */
public class DemoDataVO {
    private AppServiceReqVO applicationInfo;
    private AppServiceReleasingVO applicationRelease;
    private String templateSearchParam;
    private String appServiceVersionSearchParam;
    private DemoTagVO tagInfo;
    private DevopsBranchDTO branchInfo;
    private AppServiceVersionDTO appServiceVersionDTO;

    public AppServiceReqVO getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(AppServiceReqVO applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public AppServiceReleasingVO getApplicationRelease() {
        return applicationRelease;
    }

    public void setApplicationRelease(AppServiceReleasingVO applicationRelease) {
        this.applicationRelease = applicationRelease;
    }

    public String getTemplateSearchParam() {
        return templateSearchParam;
    }

    public void setTemplateSearchParam(String templateSearchParam) {
        this.templateSearchParam = templateSearchParam;
    }

    public String getAppServiceVersionSearchParam() {
        return appServiceVersionSearchParam;
    }

    public void setAppServiceVersionSearchParam(String appServiceVersionSearchParam) {
        this.appServiceVersionSearchParam = appServiceVersionSearchParam;
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

    public AppServiceVersionDTO getAppServiceVersionDTO() {
        return appServiceVersionDTO;
    }

    public void setAppServiceVersionDTO(AppServiceVersionDTO appServiceVersionDTO) {
        this.appServiceVersionDTO = appServiceVersionDTO;
    }
}
