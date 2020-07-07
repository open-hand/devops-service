package io.choerodon.devops.infra.enums;

/**
 * @author scp
 * @date 2020/7/7
 * @description
 */
public enum HostDeployType {
    IMAGED_DEPLOY("image"),
    JAR_DEPLOY("jar"),
    CUSTOMIZE_DEPLOY("customize");

    private String status;

    HostDeployType(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
