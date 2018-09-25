package io.choerodon.devops.api.dto;

import java.util.Date;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 17:18
 * Description:
 */
public class CommitFormRecordDTO {
    private Long userId;
    private Long appId;
    private String imgUrl;
    private String commitContent;
    private String userName;
    private Date commitDate;
    private String commitSHA;
    private String appName;

    public CommitFormRecordDTO() {
    }

    public CommitFormRecordDTO(Long userId, Long appId, String imgUrl,
                               String commitContent, String userName, Date commitDate,
                               String commitSHA, String appName) {
        this.userId = userId;
        this.appId = appId;
        this.imgUrl = imgUrl;
        this.commitContent = commitContent;
        this.userName = userName;
        this.commitDate = commitDate;
        this.commitSHA = commitSHA;
        this.appName = appName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
