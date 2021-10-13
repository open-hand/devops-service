package io.choerodon.devops.infra.enums;

public enum DevopsHostRoleEnums {
    MEMBER("member"),
    ADMINISTRATOR("administrator");

    private String role;

    DevopsHostRoleEnums(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
