package io.choerodon.devops.infra.enums;

/**
 * 部署纪录的部署类型
 *
 * @author zmf
 * @since 2/26/20
 */
public enum DeployType {
    /**
     * 持续集成部署
     */
    CD("cd"),
    /**
     * 流水线部署的类型
     */
    AUTO("auto"),
    /**
     * 手动部署单个实例的类型
     */
    MANUAL("manual"),
    /**
     * 批量部署的类型
     */
    BATCH("batch"),
    /**
     * 基础组件
     */
    BASE_COMPONENT("baseComponent"),
    /**
     * hzero部署
     */
    HZERO("hzero");


    private String type;

    DeployType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
