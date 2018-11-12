package io.choerodon.devops.domain.application.valueobject;

public class ClusterEnv {

    private String namespace;
    private Long envId;

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
}
