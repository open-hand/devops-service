package io.choerodon.devops.infra.dto.repo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 10:26
 */
public class JarPullInfoDTO {
    private String pullUserId;
    private String pullUserPassword;
    private String downloadUrl;

    public String getPullUserId() {
        return pullUserId;
    }

    public void setPullUserId(String pullUserId) {
        this.pullUserId = pullUserId;
    }

    public String getPullUserPassword() {
        return pullUserPassword;
    }

    public void setPullUserPassword(String pullUserPassword) {
        this.pullUserPassword = pullUserPassword;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
