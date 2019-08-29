package io.choerodon.devops.app.eventhandler.payload;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:57 2019/8/29
 * Description:
 */
public class AppServiceImportPayload {
    private Long appServiceId;
    private Long iamUserId;
    private Integer gitlabGroupId;
    private Long versionId;
    private String orgCode;
    private String proCode;
    private Long oldAppServiceId;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Integer getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Integer gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getProCode() {
        return proCode;
    }

    public void setProCode(String proCode) {
        this.proCode = proCode;
    }

    public Long getOldAppServiceId() {
        return oldAppServiceId;
    }

    public void setOldAppServiceId(Long oldAppServiceId) {
        this.oldAppServiceId = oldAppServiceId;
    }
}
