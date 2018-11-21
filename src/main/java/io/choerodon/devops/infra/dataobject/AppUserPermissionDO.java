package io.choerodon.devops.infra.dataobject;

import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:08
 * Description:
 */

@VersionAudit
@ModifyAudit
@Table(name = "devops_app_user_permission")
public class AppUserPermissionDO {
    private Long iamUserId;
    private String loginName;
    private String realName;
    private Long appId;

    public AppUserPermissionDO() {
    }

    public AppUserPermissionDO(Long iamUserId, String loginName, String realName, Long appId) {
        this.iamUserId = iamUserId;
        this.loginName = loginName;
        this.realName = realName;
        this.appId = appId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }
}
