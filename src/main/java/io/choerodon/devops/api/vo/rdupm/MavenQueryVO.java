package io.choerodon.devops.api.vo.rdupm;

/**
 * @author scp
 * @date 2020/6/30
 * @description
 */
public class MavenQueryVO {

    /**
     * 服务名
     */
    private String serverName;
    /**
     * 仓库名称
     */
    private String neRepositoryName;

    private String groupId;

    private String artifactId;

    /**
     * 版本正则
     */
    private String versionRegular;

    public String getNeRepositoryName() {
        return neRepositoryName;
    }

    public void setNeRepositoryName(String neRepositoryName) {
        this.neRepositoryName = neRepositoryName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersionRegular() {
        return versionRegular;
    }

    public void setVersionRegular(String versionRegular) {
        this.versionRegular = versionRegular;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
