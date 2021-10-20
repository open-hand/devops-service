package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/28 22:10
 */
public class GitlabRepositoryInfo {
    private String gitlabUrl;
    private String namespaceCode;
    private String projectCode;

    public GitlabRepositoryInfo(String gitlabUrl, String namespaceCode, String projectCode) {
        this.gitlabUrl = gitlabUrl;
        this.namespaceCode = namespaceCode;
        this.projectCode = projectCode;
    }

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }

    public String getNamespaceCode() {
        return namespaceCode;
    }

    public void setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
}
