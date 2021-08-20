package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/20 11:32
 */
public enum OperationTypeEnum {
    CREATE_APP("create_app"),
    PIPELINE_DEPLOY("pipeline_deploy");

    private String value;

    OperationTypeEnum(String value) {
        this.value = value;
    }
    public String value() {
        return this.value;
    }
}
