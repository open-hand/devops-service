package io.choerodon.devops.infra.enums;

/**
 * 集群类型
 */
public enum DevopsClusterTypeEnum {
    /**
     * 创建类型
     */
    CREATED("created"),
    /**
     * 导入类型
     */
    IMPORTED("imported");
    private String type;

    DevopsClusterTypeEnum(String type) {
        this.type = type;
    }

    public String getTYpe() {
        return this.type;
    }
}
