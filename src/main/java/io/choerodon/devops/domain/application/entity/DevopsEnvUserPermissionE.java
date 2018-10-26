package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 11:11
 * Description:
 */

public class DevopsEnvUserPermissionE {
    private String loginName;
    private Long envId;
    private Boolean permission;

    public DevopsEnvUserPermissionE() {
    }

    public DevopsEnvUserPermissionE(String loginName, Long envId, Boolean permission) {
        this.loginName = loginName;
        this.envId = envId;
        this.permission = permission;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }
}
