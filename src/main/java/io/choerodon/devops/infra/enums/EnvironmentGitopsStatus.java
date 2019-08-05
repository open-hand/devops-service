package io.choerodon.devops.infra.enums;

/**
 * 表明环境的GitOps同步状态
 *
 * @author zmf
 */
public enum EnvironmentGitopsStatus {
    FINISHED("finished"), PROCESSING("processing"), FAILED("failed");

    private String value;

    EnvironmentGitopsStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
