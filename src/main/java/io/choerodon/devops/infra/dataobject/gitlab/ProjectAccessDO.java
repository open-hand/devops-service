package io.choerodon.devops.infra.dataobject.gitlab;

import io.choerodon.devops.infra.common.util.enums.AccessLevel;

public class ProjectAccessDO {
    private AccessLevel accessLevel;
    private int notificationLevel;

    public AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public int getNotificationLevel() {
        return this.notificationLevel;
    }

    public void setNotificationLevel(int notificationLevel) {
        this.notificationLevel = notificationLevel;
    }
}
