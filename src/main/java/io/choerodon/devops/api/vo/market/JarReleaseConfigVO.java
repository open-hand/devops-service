package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 * @since 2020/11/26
 */
public class JarReleaseConfigVO {
    @ApiModelProperty("jar包的来源")
    private String sourceType;
    @ApiModelProperty("jar包所属制品库的id/来源是 nexus_repo 时才需要")
    @Encrypt
    private Long nexusRepoId;
    @ApiModelProperty("jar包仓库所属的nexus服务id/来源是 nexus_repo 时才需要")
    @Encrypt
    private Long nexusServerId;
    @ApiModelProperty("maven仓库地址/来源是custom时需要/形如: https://nexus.test.com.cn/repository/choerodon-maven/")
    private String nexusRepoUrl;
    @NotEmpty(message = "error.groupId.empty")
    private String groupId;
    @NotEmpty(message = "error.artifactId.empty")
    private String artifactId;
    @ApiModelProperty("nexus中的版本")
    @NotEmpty(message = "error.version.empty")
    private String version;
    @ApiModelProperty("拉取jar包的用户信息/来源是 custom 时才需要")
    private String username;
    @ApiModelProperty("拉取jar包的用户密码/来源是 custom 时才需要")
    private String password;
    @ApiModelProperty("时间戳")
    private String snapshotTimestamp;

    public String getSnapshotTimestamp() {
        return snapshotTimestamp;
    }

    public void setSnapshotTimestamp(String snapshotTimestamp) {
        this.snapshotTimestamp = snapshotTimestamp;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getNexusServerId() {
        return nexusServerId;
    }

    public void setNexusServerId(Long nexusServerId) {
        this.nexusServerId = nexusServerId;
    }

    public Long getNexusRepoId() {
        return nexusRepoId;
    }

    public void setNexusRepoId(Long nexusRepoId) {
        this.nexusRepoId = nexusRepoId;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNexusRepoUrl() {
        return nexusRepoUrl;
    }

    public void setNexusRepoUrl(String nexusRepoUrl) {
        this.nexusRepoUrl = nexusRepoUrl;
    }
}
