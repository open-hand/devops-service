package io.choerodon.devops.api.vo;

import java.util.List;

public class GitConfigVO {
    private String gitHost;
    private List<GitEnvConfigVO> envs;

    public String getGitHost() {
        return gitHost;
    }

    public void setGitHost(String gitHost) {
        this.gitHost = gitHost;
    }

    public List<GitEnvConfigVO> getEnvs() {
        return envs;
    }

    public void setEnvs(List<GitEnvConfigVO> envs) {
        this.envs = envs;
    }
}
