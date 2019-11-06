package io.choerodon.devops.infra.enums;

public enum ResourceUnitLevelEnum {
    MI, GI, TI;

    public static Boolean checkExist(String unit) {
        ResourceUnitLevelEnum resourceUnitLevelEnum = ResourceUnitLevelEnum.valueOf(unit);
        return resourceUnitLevelEnum != null;
    }
}
