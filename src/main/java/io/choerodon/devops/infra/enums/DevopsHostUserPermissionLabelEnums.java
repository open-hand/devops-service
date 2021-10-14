package io.choerodon.devops.infra.enums;

public enum DevopsHostUserPermissionLabelEnums {
    COMMON("common"),
    ADMINISTRATOR("administrator");

    private String value;

    DevopsHostUserPermissionLabelEnums(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
