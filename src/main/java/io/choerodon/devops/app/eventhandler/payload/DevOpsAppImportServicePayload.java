package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.infra.enums.GitPlatformType;

/**
 * @author zmf
 */
public class DevOpsAppImportServicePayload extends DevOpsAppServicePayload {
    private String repositoryUrl;
    private String accessToken;
    private GitPlatformType platformType;
    private Long gitlabUserId;

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public GitPlatformType getPlatformType() {
        return platformType;
    }

    public void setPlatformType(GitPlatformType platformType) {
        this.platformType = platformType;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }
}
