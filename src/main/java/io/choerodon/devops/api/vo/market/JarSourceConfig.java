package io.choerodon.devops.api.vo.market;

/**
 * Created by wangxiang on 2020/12/16
 */
public class JarSourceConfig {

    private String sourceType;
    private Long nexusRepoId;
    private Long nexusServerId;
    private String groupId;
    private String artifactId;
    private String version;

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getNexusRepoId() {
        return nexusRepoId;
    }

    public void setNexusRepoId(Long nexusRepoId) {
        this.nexusRepoId = nexusRepoId;
    }

    public Long getNexusServerId() {
        return nexusServerId;
    }

    public void setNexusServerId(Long nexusServerId) {
        this.nexusServerId = nexusServerId;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
