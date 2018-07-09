package io.choerodon.devops.infra.dataobject;


import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

@VersionAudit
@ModifyAudit
@Table(name = "devops_app_webhook")
public class DevopsAppWebHookDO {

    @Id
    @GeneratedValue
    private Long id;
    private Long appId;
    private Long hookId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getHookId() {
        return hookId;
    }

    public void setHookId(Long hookId) {
        this.hookId = hookId;
    }
}
