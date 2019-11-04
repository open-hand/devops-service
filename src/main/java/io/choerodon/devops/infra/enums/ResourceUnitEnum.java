package io.choerodon.devops.infra.enums;

public enum ResourceUnitEnum {
    MI("Mi"),
    GI("Gi"),
    TI("Ti");

    private String unit;

    ResourceUnitEnum(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public static Boolean checkExist(String unit) {
        ResourceUnitEnum resourceUnitEnum = ResourceUnitEnum.valueOf(unit);
        return resourceUnitEnum != null;
    }
}
