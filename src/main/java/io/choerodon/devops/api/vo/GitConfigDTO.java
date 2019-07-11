package io.choerodon.devops.api.vo;

import java.util.List;

public class GitConfigDTO {
    private String gitHost;
    private List<GitEnvConfigDTO> envs;

    public String getGitHost() {
        return gitHost;
    }

    public void setGitHost(String gitHost) {
        this.gitHost = gitHost;
    }

    public List<GitEnvConfigDTO> getEnvs() {
        return envs;
    }

    public void setEnvs(List<GitEnvConfigDTO> envs) {
        this.envs = envs;
    }
}
