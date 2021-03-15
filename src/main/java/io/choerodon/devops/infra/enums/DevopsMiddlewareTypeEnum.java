package io.choerodon.devops.infra.enums;

/**
 * 中间件类型枚举类
 */
public enum DevopsMiddlewareTypeEnum {
    REDIS("Redis");

    private String type;

    DevopsMiddlewareTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
