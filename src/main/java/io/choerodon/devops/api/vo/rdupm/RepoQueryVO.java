package io.choerodon.devops.api.vo.rdupm;

/**
 * @author scp
 * @date 2020/6/30
 * @description
 */
public class RepoQueryVO {
    /**
     * 仓库名称
     */
    private String repoName;

    /**
     * 镜像名称
     */
    private String iamgeName;


    /**
     * 版本正则表达式
     */
    private String versionRegular;

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getIamgeName() {
        return iamgeName;
    }

    public void setIamgeName(String iamgeName) {
        this.iamgeName = iamgeName;
    }

    public String getVersionRegular() {
        return versionRegular;
    }

    public void setVersionRegular(String versionRegular) {
        this.versionRegular = versionRegular;
    }
}
