package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.maven.Server;

/**
 * Created by wangxiang on 2021/2/5
 */
public class PipelineJarInfoVO {
    private String groupId;
    private String artifactId;
    private String version;
    private String downloadUrl;
    private Server server;

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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
