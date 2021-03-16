package io.choerodon.devops.infra.enums.market;

/**
 * 市场应用类型
 */
public enum ApplicationTypeEnums {
    /**
     * 普通类型
     */
    COMMON("common"),
    /**
     * 中间件类型
     */
    MIDDLEWARE("middleware");

    private String type;

    ApplicationTypeEnums(String type) {
        this.type = type;
    }

    public String getValue(){
        return this.type;
    }
}
