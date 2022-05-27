package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/23 16:42
 */
public class CustomCommitVO {
    @ApiModelProperty("提交用户")
    private String userName;
    @ApiModelProperty("提交用户头像")
    private String userHeadUrl;
    @ApiModelProperty("commit sha")
    private String commitSha;
    @ApiModelProperty("commit url")
    private String commitUrl;
    @ApiModelProperty("gitlab project url")
    private String gitlabProjectUrl;
    @ApiModelProperty("提交分支")
    private String ref;
    @ApiModelProperty("提交信息")
    private String commitContent;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserHeadUrl() {
        return userHeadUrl;
    }

    public void setUserHeadUrl(String userHeadUrl) {
        this.userHeadUrl = userHeadUrl;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getCommitContent() {
        return commitContent;
    }

    public void setCommitContent(String commitContent) {
        this.commitContent = commitContent;
    }

    public String getGitlabProjectUrl() {
        return gitlabProjectUrl;
    }

    public void setGitlabProjectUrl(String gitlabProjectUrl) {
        this.gitlabProjectUrl = gitlabProjectUrl;
    }

    @Override
    public String toString() {
        return "CustomCommitVO{" +
                "userName='" + userName + '\'' +
                ", userHeadUrl='" + userHeadUrl + '\'' +
                ", commitSha='" + commitSha + '\'' +
                ", commitUrl='" + commitUrl + '\'' +
                ", gitlabProjectUrl='" + gitlabProjectUrl + '\'' +
                ", ref='" + ref + '\'' +
                ", commitContent='" + commitContent + '\'' +
                '}';
    }
}
