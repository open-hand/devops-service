package io.choerodon.devops.api.vo;

import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:58 2019/7/31
 * Description:
 */
public class AppServicePermissionVO {
    @Encrypt
    private List<Long> userIds;
    private Boolean skipCheckPermission;

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }
}
