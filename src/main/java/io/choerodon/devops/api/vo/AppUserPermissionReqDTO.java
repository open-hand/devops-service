package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 10:02
 * Description:
 */
public class AppUserPermissionReqDTO {
    private Boolean skipCheckAppPermission;
    private List<Long> userIds;

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getSkipCheckAppPermission() {
        return skipCheckAppPermission;
    }

    public void setSkipCheckAppPermission(Boolean skipCheckAppPermission) {
        this.skipCheckAppPermission = skipCheckAppPermission;
    }
}
