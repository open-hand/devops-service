package io.choerodon.devops.app.eventhandler.payload;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:05 2019/8/6
 * Description:
 */
public class AppServiceDownloadPayload extends AppServicePayload {
    private Integer gitlabUserId;
    private Integer gitlabGroupId;
    private String path;

    public Integer getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Integer gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }

    public Integer getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Integer gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
