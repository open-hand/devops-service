package io.choerodon.devops.infra.enums;

/**
 * deployment来源类型
 */
public enum DeploymentSourceTypeEnums {
    /**
     * 来自chart
     */
    CHART("chart"),
    /**
     * 来自工作负载
     */
    WORKLOAD("workload"),
    /**
     * 来自部署组
     */
    DEPLOY_GROUP("deploy_group");

    private String type;

    DeploymentSourceTypeEnums(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
