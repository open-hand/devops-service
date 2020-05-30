package io.choerodon.devops.infra.enums;

/**
 * @author superlee
 */
public enum RoleLabel {

    PROJECT_DEPLOY_ADMIN("project.deploy.admin"),

    PROJECT_OWNER("project.owner"),

    ORGANIZATION_OWNER("organization.owner"),

    PROJECT_ADMIN("project-admin"),

    TENANT_ADMIN("administrator");

    private final String value;

    RoleLabel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
