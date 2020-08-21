package io.choerodon.devops.infra.dto.repo;

/**
 * @author scp
 * @date 2020/7/7
 * @description
 */
public class C7nNexusDeployDTO {

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

    private String jarName;

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

}
