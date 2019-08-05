package io.choerodon.devops.api.vo.kubernetes;

import java.util.List;

public class UpgradeClusterVO {

    private String token;
    private String platformCode;
    private List<ClusterEnv> envs;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public List<ClusterEnv> getEnvs() {
        return envs;
    }

    public void setEnvs(List<ClusterEnv> envs) {
        this.envs = envs;
    }
}
