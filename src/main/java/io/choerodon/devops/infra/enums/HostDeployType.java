package io.choerodon.devops.infra.enums;

/**
 * @author scp
 * @date 2020/7/7
 * @description
 */
public enum HostDeployType {
    IMAGED_DEPLOY("image"),
    ENV_DEPLOY("env"),
    JAR_DEPLOY("jar"),
    CUSTOMIZE_DEPLOY("customize");

    private String value;

    HostDeployType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
