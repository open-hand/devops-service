package io.choerodon.devops.api.vo;

public class GitEnvConfigVO {
    private String namespace;
    private Long envId;
    private String gitRsaKey;
    private String gitUrl;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getGitRsaKey() {
        return gitRsaKey;
    }

    public void setGitRsaKey(String gitRsaKey) {
        this.gitRsaKey = gitRsaKey;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }
}
