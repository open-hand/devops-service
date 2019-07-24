package io.choerodon.devops.api.vo.kubernetes;

import io.choerodon.devops.infra.enums.AccessLevel;

public class MemberHelper {

    private boolean isDeploy;
    private AccessLevel deployAdminAccessLevel;
    private AccessLevel projectOwnerAccessLevel;
    private AccessLevel projectDevelopAccessLevel;
    private AccessLevel organizationAccessLevel;


    public MemberHelper() {
        this.projectDevelopAccessLevel = AccessLevel.NONE;
        this.projectOwnerAccessLevel = AccessLevel.NONE;
        this.deployAdminAccessLevel = AccessLevel.NONE;
        this.organizationAccessLevel = AccessLevel.NONE;
    }

    public boolean isDeploy() {
        return isDeploy;
    }

    public void setDeploy(boolean deploy) {
        isDeploy = deploy;
    }

    public AccessLevel getDeployAdminAccessLevel() {
        return deployAdminAccessLevel;
    }

    public void setDeployAdminAccessLevel(AccessLevel deployAdminAccessLevel) {
        this.deployAdminAccessLevel = deployAdminAccessLevel;
    }

    public AccessLevel getProjectOwnerAccessLevel() {
        return projectOwnerAccessLevel;
    }

    public void setProjectOwnerAccessLevel(AccessLevel projectOwnerAccessLevel) {
        this.projectOwnerAccessLevel = projectOwnerAccessLevel;
    }

    public AccessLevel getProjectDevelopAccessLevel() {
        return projectDevelopAccessLevel;
    }

    public void setProjectDevelopAccessLevel(AccessLevel projectDevelopAccessLevel) {
        this.projectDevelopAccessLevel = projectDevelopAccessLevel;
    }


    public AccessLevel getOrganizationAccessLevel() {
        return organizationAccessLevel;
    }

    public void setOrganizationAccessLevel(AccessLevel organizationAccessLevel) {
        this.organizationAccessLevel = organizationAccessLevel;
    }

}
