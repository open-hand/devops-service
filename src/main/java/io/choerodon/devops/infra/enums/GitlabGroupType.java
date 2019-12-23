package io.choerodon.devops.infra.enums;

public enum GitlabGroupType {

    /**
     * APP类型组
     */
    APP("app"),
    /**
     * 环境类型gitops组
     */
    ENV_GITOPS("env_gitops"),
    /**
     * 集群环境类型gitops组
     */
    CLUSTER_GITOPS("cluster_gitops");

    private String value;

    GitlabGroupType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GitlabGroupType forValue(String value) {
        return GitlabGroupType.valueOf(value.toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }

}
