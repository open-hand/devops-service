package io.choerodon.devops.infra.dataobject.gitlab;

import io.choerodon.devops.infra.enums.AccessLevel;

public class ProjectSharedGroupDO {
    private Integer groupId;
    private String groupName;
    private AccessLevel groupAccessLevel;

    public int getGroupId() {
        return this.groupId.intValue();
    }

    public void setGroupId(int groupId) {
        this.groupId = Integer.valueOf(groupId);
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public AccessLevel getGroupAccessLevel() {
        return this.groupAccessLevel;
    }

    public void setGroupAccessLevel(AccessLevel accessLevel) {
        this.groupAccessLevel = accessLevel;
    }
}

