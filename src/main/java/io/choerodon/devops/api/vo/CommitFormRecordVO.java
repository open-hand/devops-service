package io.choerodon.devops.api.vo;

import java.util.Date;

import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 17:18
 * Description:
 */
public class CommitFormRecordVO {
    private Long userId;
    private Long appServiceId;
    private String imgUrl;
    private String commitContent;
    private String userName;
    private Date commitDate;
    private String commitSHA;
    private String appServiceName;
    private String url;

    public CommitFormRecordVO() {
    }

    public CommitFormRecordVO(Long userId, String imgUrl,
                              String userName,
                              DevopsGitlabCommitDTO devopsGitlabCommitDO) {
        this.userId = userId;
        this.appServiceId = devopsGitlabCommitDO.getAppServiceId();
        this.imgUrl = imgUrl;
        this.commitContent = devopsGitlabCommitDO.getCommitContent();
        this.userName = userName;
        this.commitDate = devopsGitlabCommitDO.getCommitDate();
        this.commitSHA = devopsGitlabCommitDO.getCommitSha();
        this.appServiceName = devopsGitlabCommitDO.getAppServiceName();
        this.url = devopsGitlabCommitDO.getUrl();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getCommitContent() {
        return commitContent;
    }

    public void setCommitContent(String commitContent) {
        this.commitContent = commitContent;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitSHA() {
        return commitSHA;
    }

    public void setCommitSHA(String commitSHA) {
        this.commitSHA = commitSHA;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
