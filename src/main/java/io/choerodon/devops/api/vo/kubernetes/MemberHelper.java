package io.choerodon.devops.api.vo.kubernetes;

import io.choerodon.devops.infra.enums.AccessLevel;

public class MemberHelper {
    private AccessLevel projectOwnerAccessLevel;
    private AccessLevel projectDevelopAccessLevel;


    public MemberHelper() {
        this.projectDevelopAccessLevel = AccessLevel.NONE;
        this.projectOwnerAccessLevel = AccessLevel.NONE;
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
}
