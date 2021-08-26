package io.choerodon.devops.infra.enums;

/**
 * pod来源枚举
 */
public enum PodSourceEnums {
    /**
     * helm部署产生
     */
    HELM("helm"),
    /**
     * 工作负载生成
     */
    WORKLOAD("workload");

    private String value;

    PodSourceEnums(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
