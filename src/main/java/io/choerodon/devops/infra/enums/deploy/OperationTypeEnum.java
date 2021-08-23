package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/20 11:32
 */
public enum OperationTypeEnum {
    /**
     * 创建应用
     */
    CREATE_APP("create_app"),
    /**
     * hzero快速部署
     */
    HZERO("hzero"),
    /**
     * 基础组件
     */
    BASE_COMPONENT("base_component"),
    /**
     * 批量部署
     */
    BATCH_DEPLOY("batch_deploy"),
    /**
     * 流水先部署
     */
    PIPELINE_DEPLOY("pipeline_deploy");

    private String value;

    OperationTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
