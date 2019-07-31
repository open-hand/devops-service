package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * 更新环境的权限分配
 *
 * @author zmf
 */
public class DevopsEnvPermissionUpdateVO {
    private Long envId;
    private List<Long> userIds;
    @NotNull(message = "error.env.is.skip.permission.check.null")
    private Boolean isSkipCheckPermission;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getSkipCheckPermission() {
        return isSkipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        isSkipCheckPermission = skipCheckPermission;
    }
}
