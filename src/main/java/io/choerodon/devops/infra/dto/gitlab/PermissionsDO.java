package io.choerodon.devops.infra.dto.gitlab;


public class PermissionsDO {

    private ProjectAccessDO projectAccess;
    private ProjectAccessDO groupAccess;

    public ProjectAccessDO getProjectAccess() {
        return this.projectAccess;
    }

    public ProjectAccessDO getGroupAccess() {
        return this.groupAccess;
    }
}
